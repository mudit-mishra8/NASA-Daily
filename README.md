# AWS Serverless Data Engineering Project: Daily NASA Image Email Delivery

## Overview

This repository contains the implementation of a serverless architecture project in AWS, which extracts NASA image data using Kafka, stores it in an S3 bucket, processes the data using multiple AWS Lambda functions, and then sends a daily email to registered users using SES. The email contains a description and the image itself from NASA's APOD (Astronomy Picture of the Day) service.

## Architecture Diagram

Please find the architecture diagram ![here](link-to-architecture-diagram).

## Technologies Used

- Apache Kafka
- Amazon S3
- AWS Lambda
- Amazon DynamoDB
- AWS SES

## Project Steps

1. **Data Collection** - An Apache Kafka producer extracts the image data from the NASA APOD API and writes to a Kafka cluster.
2. **Data Transfer** - An Apache Kafka consumer consumes the data from the Kafka topic and writes it to an S3 bucket. The data for the years 2020, 2021, and 2022 is collected.
3. **Data Processing** - AWS Lambda functions are invoked to create year-specific folders in the S3 bucket and arrange the files accordingly.
4. **Email Preparation and Delivery** - The main AWS Lambda function creates an email message and sends it via AWS SES. It reads the filename from DynamoDB, fetches the file from the S3 bucket, processes it as needed, and updates the filename in DynamoDB for the next processing cycle.

## Code Samples

#### Kafka Producer - Java

View the Kafka Producer code [here](link-to-kafka-producer-file).

#### Kafka Data Transformation - Java

View the Kafka Consumer code [here](link-to-kafka-transformation-file).

#### Kafka Consumer - Java

View the Kafka Consumer code [here](link-to-kafka-consumer-file).

#### Lambda Function: nasa_invoke_yearwise_lambda

View the code for the nasa_invoke_yearwise_lambda function [here](link-to-nasa-invoke-yearwise-lambda-function-file).

#### Lambda Function: nasa_s3_yearwise

View the code for the nasa_s3_yearwise function [here](link-to-nasa-s3-yearwise-function-file).

#### Lambda Function: nasa_create_message

View the code for the Main Lambda function [here](link-to-main-lambda-function-file).

## Daily Email Structure

The daily email sent to the registered users contains an image and its description fetched from NASA's APOD service. The email looks like below: 
Please find the architecture diagram ![here](link-to-architecture-diagram).


