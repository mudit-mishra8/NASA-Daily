# AWS Serverless : Daily NASA Image Email Delivery

## Overview

This repository contains the implementation of a serverless architecture in AWS, which extracts NASA image data using Kafka, stores it in an S3 bucket, processes the data using multiple AWS Lambda functions, and then sends a daily email to registered users using SES. The email contains a description and the image itself from NASA's APOD (Astronomy Picture of the Day) service.

## Architecture Diagram
![here](https://github.com/mudit-mishra8/NASA-Daily/blob/main/nasa%20(3).png).

## Technologies Used

- Apache Kafka
- Amazon S3
- AWS Lambda
- Amazon DynamoDB
- AWS SES
- AWS Event Bridge

## Project Steps

1. **Data Collection** - An Apache Kafka producer extracts the image data from the NASA APOD API and writes to a Kafka cluster.
2. **Data Transformation** - An Apache Kafka application consumes the data from the Kafka topic and clean and transformed the data and produce to other topic. 
3. **Data Transfer** - An Apache Kafka consumer consumes the data from the Kafka topic and writes it to an S3 bucket. The data for the years 2018 to 2022 is collected.
4. **Data Processing** - AWS Lambda functions are invoked to create year-specific folders in the S3 bucket and arrange the files accordingly.
5. **Email Preparation and Delivery** - The AWS Lambda function creates an email message and sends it via AWS SES.
   
    This Lambda function reads the filename from the DynamoDB table.
   
    This Lambda function fetches the file from the S3 bucket based on the filename obtained from DynamoDB.
   
    This Lambda function processes the file as needed.
   
    This Lambda function updates the filename in DynamoDB to the next file to be processed.

## Code Samples

#### Kafka Producer - Java

View the Kafka Producer code [here](https://github.com/mudit-mishra8/NASA-Daily/blob/main/nasa_producer.java).

#### Kafka Data Transformation - Java

View the Kafka Transformation code [here](https://github.com/mudit-mishra8/NASA-Daily/blob/main/nasa_transformation_application.java).

#### Kafka Consumer - Java

View the Kafka Consumer code [here](https://github.com/mudit-mishra8/NASA-Daily/blob/main/nasa_s3_consumer.java).

#### Lambda Function: nasa_invoke_yearwise_lambda

View the code for the nasa_invoke_yearwise_lambda function [here](https://github.com/mudit-mishra8/NASA-Daily/blob/main/nasa_invoke_yearwise_lambda.py).

#### Lambda Function: nasa_s3_yearwise

View the code for the nasa_s3_yearwise function [here](https://github.com/mudit-mishra8/NASA-Daily/blob/main/nasa_s3_yearwise.py).

#### Lambda Function: nasa_create_message

View the code for the Main Lambda function [here](https://github.com/mudit-mishra8/NASA-Daily/blob/main/lambda_create_and_deliver.py).

## Daily Email Structure

The daily email sent to the registered users contains an image and its description fetched from NASA's APOD service. 

The email looks like below: 
 [here](https://github.com/mudit-mishra8/NASA-Daily/blob/main/nasa_email.png).


