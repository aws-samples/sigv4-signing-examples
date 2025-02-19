DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
source "$DIR/../AWSSigner.sh"

## read from environment variables
aws_secret_access_key=$AWS_SECRET_ACCESS_KEY
aws_access_key_id=$AWS_ACCESS_KEY_ID
region=$AWS_DEFAULT_REGION

s3_bucket_name=bucket-name
path=/object-key

host="$s3_bucket_name.s3.$region.amazonaws.com"


http_method=GET
service=s3
timestamp=$(date -u +"%Y%m%dT%H%M%SZ")
payload=""
payload_hash=$( calculate_hash $payload )
payload_length=${#payload}
content_type="application/json"

## get authorization header
authorization_header=$( get_authorization_header \
                            $service \
                            $region \
                            $http_method \
                            $host \
                            $path \
                            $timestamp \
                            $payload_hash \
                            $payload_length \
                            $content_type )

echo "authorization_header:"$authorization_header

## get request
wget -d --header="Authorization:$authorization_header" \
        --header="x-amz-date:$timestamp" \
        --header="x-amz-content-sha256:$payload_hash" \
        "https://$host$path"
