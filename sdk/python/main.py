import datetime
import os
import botocore
import boto3
import requests

# AWS access keys
access_key = os.environ['AWS_ACCESS_KEY_ID']
secret_key = os.environ['AWS_SECRET_ACCESS_KEY'] 
session_token = os.environ['AWS_SESSION_TOKEN']

# Request parameters
method = 'GET'
service = 'execute-api'
host = os.environ['RESTAPIHOST']
region = 'us-east-1'
endpoint = os.environ['RESTAPIPATH']

session = boto3.Session(
    aws_access_key_id=access_key,
    aws_secret_access_key=secret_key,
    aws_session_token=session_token
)

signer = botocore.signers.RequestSigner(
  service,
  region_name=region,
  signing_name=service, 
  credentials=session.get_credentials(),
  signature_version='v4',
  event_emitter=botocore.hooks.HierarchicalEmitter
)
