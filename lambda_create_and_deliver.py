#nasa_create_message

import boto3
import json
import datetime
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from email.mime.image import MIMEImage
import urllib.request

# Initialize the S3 and DynamoDB clients
s3_client = boto3.client('s3')
dynamodb = boto3.resource('dynamodb')
# Create an instance of the SES client
ses_client = boto3.client('ses')

# Specify the bucket and DynamoDB table names
bucket_name = 'nasatarget'
dynamodb_table_name = 'nasa_next_file'
dynamodb_table_name_2 = 'nasa_users_email'

# Function to get the next file to be processed from DynamoDB
def get_next_file_to_process(year, table_name):
    table = dynamodb.Table(table_name)
    response = table.get_item(Key={'year': year})
    return response['Item']['next_file']


# Function to set the next file to be processed in DynamoDB
def set_next_file_to_process(year, table_name, next_file):
    table = dynamodb.Table(table_name)

    # Determine the name of the next file to be processed
    # Parse the current date from the file name
    current_date = datetime.datetime.strptime(next_file.split('/')[-1], '%Y-%m-%d')
    # Increment the date by 1 day
    next_date = current_date + datetime.timedelta(days=1)
    # Format the next date as a string and build the file path
    next_file_name = f"{year}/{next_date.strftime('%Y-%m-%d')}"
    
    table.update_item(
        Key={'year': year},
        UpdateExpression='SET next_file = :val',
        ExpressionAttributeValues={':val': next_file_name}
    )


# Function to get the list of all the emails for a given year
def get_email_addresses(year, table_name):
    table = dynamodb.Table(table_name)
    response = table.query(
        IndexName='year-index',  # Your Index Name
        KeyConditionExpression=boto3.dynamodb.conditions.Key('year').eq(year)
    )


    return [item['email'] for item in response['Items']]
    
def is_valid_url(url):
    try:
        # send a request to the URL
        response = urllib.request.urlopen(url)
        # if the request is successful, the URL is valid
        return response.getcode() == 200
    except:
        # if the request raised an error, the URL is not valid
        return False


def lambda_handler(event, context):
    # Define the years you want to process
    years_to_process = ['2020','2021','2022']

    # Iterate through each year
    for year in years_to_process:
        # step1 : Get the next file to be processed from DynamoDB
        next_file = get_next_file_to_process(year, dynamodb_table_name)
        
        # step2: Fetch the file from S3
        file_object = s3_client.get_object(Bucket=bucket_name, Key=next_file)
        file_content = file_object['Body'].read().decode(('utf-8'))
        #print(file_content)
        if not file_content.endswith('}'):
            file_content += '"}'
        #step 3:  Process the file content
        #print(file_content)
        #3.1 create a message 
        s3_dict = json.loads(file_content)
        url = s3_dict['url']
        explanation = s3_dict['explanation']
        
        # below code is to insure that url is not null, if null then we repat step 1 to 2 till we not get non null url
        ct=0
        if url is None or not is_valid_url(url):
            while(ct!=1):
                set_next_file_to_process(year, dynamodb_table_name, next_file)
                next_file = get_next_file_to_process(year, dynamodb_table_name)
                file_object = s3_client.get_object(Bucket=bucket_name, Key=next_file)
                file_content = file_object['Body'].read().decode('utf-8')
                s3_dict = json.loads(file_content)
                url = s3_dict['url']
                explanation = s3_dict['explanation']
                if url is not None:
                    ct=1                
     
        image_data = urllib.request.urlopen(url).read()
        
        # Set up the message
        msg = MIMEMultipart()
        msg['Subject'] = 'NASA Image of the Day'
        msg['From'] = 'mishramudit0311@gmail.com'
        
        # Create the text part of the message
        if "image" not in url:
            html_part = MIMEText(f'<p>{explanation}</p><p>Copy paste the link on a browser to watch the video.</p><p>{url}></p>', 'html')
            msg.attach(html_part)
        else:
            html_part = MIMEText(f'<p>{explanation}</p><img src="cid:image1">', 'html')
            msg.attach(html_part)
            
        # Create the image part of the message
        image_part = MIMEImage(image_data,_subtype='jpeg')
        image_part.add_header('Content-ID', '<image1>')
        msg.attach(image_part)
        
        #3.2  Get the email addresses for a given year 
        email_addresses = get_email_addresses(year, dynamodb_table_name_2)
        
        for email in email_addresses:
            print(email)
            msg['To'] = email
            response = ses_client.send_raw_email(
               Source=msg['From'],
               Destinations=[msg['To']],
               RawMessage={
                'Data': msg.as_string(),
            }
        )
        
            # Reset the 'To' header for the next recipient
            del msg['To']


        # step 4: Update DynamoDB with the next file to be processed
        set_next_file_to_process(year, dynamodb_table_name, next_file)
    
    return {
        'statusCode': 200,
        'body': json.dumps('Files processed successfully')
    }
