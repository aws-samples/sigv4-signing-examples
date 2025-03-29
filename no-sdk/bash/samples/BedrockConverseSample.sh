DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
source "$DIR/../AWSSigner.sh"

## read from environment variables
aws_secret_access_key=$AWS_SECRET_ACCESS_KEY
aws_access_key_id=$AWS_ACCESS_KEY_ID
region=$AWS_DEFAULT_REGION

model_id="amazon.titan-text-express-v1"

host="bedrock-runtime.$region.amazonaws.com"
path="/model/$model_id/converse"

httpMethod=POST
service=bedrock
timestamp=$(date -u +"%Y%m%dT%H%M%SZ")
payload='{"messages":[{"role":"user","content":[{"text":"Which is the capital of England?"}]}]}'
payload_length=${#payload}
payloadHash=$( calculate_hash "$payload" )
payload_length=${#payload}
content_type="application/json"

## get authorization header
authorization_header=$( get_authorization_header \
                            $service \
                            $region \
                            $httpMethod \
                            $host \
                            $path \
                            $timestamp \
                            $payloadHash \
                            $payload_length \
                            $content_type )

echo "authorization_header:"$authorization_header

## get request
wget -d --header="Authorization:$authorization_header" \
        --header="x-amz-date:$timestamp" \
        --header="x-amz-content-sha256:$payloadHash" \
        --header="content-length:${#payload}" \
        --header="content-type:application/json" \
        --post-data="$payload" \
        "https://$host$path"