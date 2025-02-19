#!/bin/bash

function calculate_hash() {
  local payload=$1
  local hash=$(echo -en "$payload" | sha256sum | awk '{print $1}')
  echo $hash
}

function hmac_sha256() {
  local key=$1
  local data=$2
  local hmac=$(printf "$data" | openssl dgst -binary -sha256 -mac HMAC -macopt "hexkey:$key" | xxd -p -c 256)
  echo $hmac
}

function get_signing_key() {
    local secret_key="$1"    # AWS Secret Access Key
    local datestamp="$2"  # YYYYMMDD format
    local region="$3"
    local service="$4"

    secret_key_bytes=$(printf "AWS4$secret_key" | xxd -p -c 256)
    datestamp_hmac=$(hmac_sha256 "$secret_key_bytes" "$datestamp")
    region_hmac=$(hmac_sha256 "$datestamp_hmac" "$region")
    service_hmac=$(hmac_sha256 "$region_hmac" "$service")
    signing_hmac=$(hmac_sha256 "$service_hmac" "aws4_request")
    echo "$signing_hmac"
}

function get_signature() {
    local string_to_sign="$1"
    local datestamp="$2"
    local region="$3"
    local service="$4"
    local secret_key="$5"
    
    # First get the signing key
    local signing_key=$(get_signing_key "$secret_key" "$datestamp" "$region" "$service")
    
    # Then create the signature
    local signature=$(hmac_sha256 "$signing_key" "$string_to_sign")
    
    echo "$signature"
}


function get_authorization_header() {
  local service=$1
  local region=$2
  local httpMethod=$3
  local host=$4
  local path=$5
  local amzdate=$6
  local payloadHash=$7
  local payload_length=$8
  local content_type=$9

  local algorithm="AWS4-HMAC-SHA256"
  local datestamp=$(echo $amzdate | cut -d'T' -f1)

  ##split string "%Y%m%dT%H%M%SZ" format and save first part in a variable
  local datestamp=$(echo $amzdate | cut -d'T' -f1)

  ##array to store key value pairs
  local canonical_headers
  local signed_headers

  if [ "$httpMethod" = "GET" ]; then
    signed_headers="host;x-amz-date"
    canonical_headers="host:"$host"\n"x-amz-date:"$amzdate""\n"    
  else ##POST
    signed_headers="content-length;content-type;host;x-amz-content-sha256;x-amz-date"
    canonical_headers="content-length:"$payload_length"\ncontent-type:"$content_type"\nhost:"$host"\nx-amz-content-sha256:"$payloadHash"\nx-amz-date:"$amzdate"\n"
  fi

  ##canonical request
  local canonical_request="$httpMethod\n$path\n\n$canonical_headers\n$signed_headers\n$payloadHash"
  local string_to_sign="$algorithm\n$amzdate\n$datestamp/$region/$service/aws4_request\n$( calculate_hash $canonical_request )"

  ##create signature
  local signature=$(get_signature "$string_to_sign" "$datestamp" "$region" "$service" "$aws_secret_access_key")

  ##get authorization header
  local authorization_header="$algorithm Credential=$aws_access_key_id/$datestamp/$region/$service/aws4_request, SignedHeaders=$signed_headers, Signature=$signature"  
  echo $authorization_header
}