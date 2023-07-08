# lambda function which extract all the files present in s3- nasatarget and for each file, invoking another lambda function: nasa_s3_yearwise which will put files in specific folder(year).
# nasa_invoke_yearwise_lambda
import json
import boto3

lambda_client = boto3.client('lambda')
s3_client = boto3.client('s3')

def lambda_handler(event, context):
    bucket_name = 'nasatarget'
    folder_name = 'api_producer_transformed/'
    
    # List all files in the specified folder of the bucket
    response = s3_client.list_objects_v2(Bucket=bucket_name, Prefix=folder_name)
    for item in response['Contents']:
        file_name = item['Key']
        
        # Invoke the second Lambda function
        lambda_client.invoke(
            FunctionName='nasa_s3_yearwise',
            InvocationType='Event',
            Payload=json.dumps({
                'bucket': bucket_name,
                'file_name': file_name
            })
        )
    return {
        'statusCode': 200,
        'body': 'nasa_s3_yearwise invoked for all files'
    }
