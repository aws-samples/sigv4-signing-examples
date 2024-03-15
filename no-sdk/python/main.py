import datetime
import hashlib
import hmac
import requests
import os

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

# Create a datetime object for signing
t = datetime.datetime.utcnow()
amzdate = t.strftime('%Y%m%dT%H%M%SZ')
datestamp = t.strftime('%Y%m%d') 

# Create the canonical request
canonical_uri = endpoint
canonical_querystring = ''
canonical_headers = 'host:' + host + '\n'
signed_headers = 'host'
payload_hash = hashlib.sha256(''.encode('utf-8')).hexdigest()
canonical_request = (method + '\n' + canonical_uri + '\n' + canonical_querystring + '\n'
                     + canonical_headers + '\n' + signed_headers + '\n' + payload_hash)

# Create the string to sign
algorithm = 'AWS4-HMAC-SHA256'
credential_scope = datestamp + '/' + region + '/' + service + '/' + 'aws4_request'
string_to_sign = (algorithm + '\n' +  amzdate + '\n' +  credential_scope + '\n' +  
                  hashlib.sha256(canonical_request.encode('utf-8')).hexdigest())

def sign(key, msg):
    return hmac.new(key, msg.encode("utf-8"), hashlib.sha256).digest()

def getSignatureKey(key, dateStamp, regionName, serviceName):
    kDate = sign(("AWS4" + key).encode("utf-8"), dateStamp)
    kRegion = sign(kDate, regionName)
    kService = sign(kRegion, serviceName)
    kSigning = sign(kService, "aws4_request")
    return kSigning

# Sign the string    
signing_key = getSignatureKey(secret_key, datestamp, region, service)
signature = hmac.new(signing_key, (string_to_sign).encode('utf-8'), hashlib.sha256).hexdigest()

# Add signing information to the request
authorization_header = (algorithm + ' ' + 'Credential=' + access_key + '/' + credential_scope + ', ' +  
                        'SignedHeaders=' + signed_headers + ', ' + 'Signature=' + signature)

# Make the request
headers = {'Host': host,
           'x-amz-date': amzdate,
           'x-amz-security-token': session_token,
           'Authorization': authorization_header}
request_url = 'https://' + host + canonical_uri
response = requests.get(request_url, headers=headers)

print(response.text)
