import os
import boto3
from botocore.auth import SigV4Auth
from botocore.awsrequest import AWSRequest
import requests

access_key = os.environ.get('AWS_ACCESS_KEY_ID')
secret_key = os.environ.get('AWS_SECRET_ACCESS_KEY')
session_token = os.environ.get('AWS_SESSION_TOKEN')
service = 'execute-api'
host = os.environ.get('RESTAPIHOST')
canonical_uri = os.environ.get('RESTAPIPATH')
region = 'us-east-1'
method = "GET"
url=f'https://{host}{canonical_uri}'

session = boto3.Session(
    aws_access_key_id=access_key,
    aws_secret_access_key=secret_key,
    aws_session_token=session_token,
    region_name=region
)

request = AWSRequest(
    method,
    url,
    headers={'Host': host}
)

SigV4Auth(session.get_credentials(), service, region).add_auth(request)

try:
    response = requests.request(method, url, headers=dict(request.headers), data={})
    print(f'Response Status: {response.status_code}')
    print(f'Response Body: {response.content.decode("utf-8")}')
except Exception as e:
    print(f'Error: {e}')