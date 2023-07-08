#nasa_s3_yearwise

import json
import boto3

s3_client = boto3.client('s3')

def lambda_handler(event, context):
    # Extract the bucket name and file key from the Lambda event trigger
    bucket = event['bucket']
    file_name = event['file_name']
    
    # Extract the year from the file name 
    year = file_name.split('/')[-1].split('-')[0]
    
    # Destination directory within the bucket
    destination_directory = f"{year}/"
    
    # Copy the file to the new directory
    copy_source = {'Bucket': bucket, 'Key': file_name}
    new_key = destination_directory + file_name.split('/')[-1]
    s3_client.copy(copy_source, bucket, new_key)
    
    # Optionally, you can delete the original file after copy
    # s3_client.delete_object(Bucket=bucket, Key=file_name)

    return {
        'statusCode': 200,
        'body': json.dumps(f'File {file_name} has been moved to {destination_directory}')
    }
