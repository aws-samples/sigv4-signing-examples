require 'openssl'
require 'time'
require 'uri'
require 'net/http'

# AWS access keys
access_key = ENV['AWS_ACCESS_KEY_ID']
secret_key = ENV['AWS_SECRET_ACCESS_KEY']
session_token = ENV['AWS_SESSION_TOKEN']

# Request parameters
method = 'GET'
service = 'execute-api'
host = ENV['RESTAPIHOST']
region = 'us-east-1'
endpoint = ENV['RESTAPIPATH']

# Create datetime for signing
t = Time.now.utc
amzdate = t.strftime('%Y%m%dT%H%M%SZ')
datestamp = t.strftime('%Y%m%d')

# Create the canonical request
canonical_uri = endpoint
canonical_querystring = ''
canonical_headers = "host:#{host}\n"
signed_headers = 'host'
payload_hash = OpenSSL::Digest::SHA256.hexdigest('')
canonical_request = [
  method,
  canonical_uri,
  canonical_querystring,
  canonical_headers,
  signed_headers,
  payload_hash
].join("\n")

# Create the string to sign
algorithm = 'AWS4-HMAC-SHA256'
credential_scope = "#{datestamp}/#{region}/#{service}/aws4_request"
string_to_sign = [
  algorithm,
  amzdate,
  credential_scope,
  OpenSSL::Digest::SHA256.hexdigest(canonical_request)
].join("\n")

def sign(key, msg)
  OpenSSL::HMAC.digest('sha256', key, msg)
end

def get_signature_key(key, date_stamp, region_name, service_name)
  k_date = sign("AWS4#{key}", date_stamp)
  k_region = sign(k_date, region_name)
  k_service = sign(k_region, service_name)
  sign(k_service, 'aws4_request')
end

# Sign the string
signing_key = get_signature_key(secret_key, datestamp, region, service)
signature = OpenSSL::HMAC.hexdigest('sha256', signing_key, string_to_sign)

# Add signing information to the request
authorization_header = [
  "#{algorithm} Credential=#{access_key}/#{credential_scope}",
  "SignedHeaders=#{signed_headers}",
  "Signature=#{signature}"
].join(', ')

# Make the request
uri = URI("https://#{host}#{canonical_uri}")
request = Net::HTTP::Get.new(uri)
request['Host'] = host
request['x-amz-date'] = amzdate
request['x-amz-security-token'] = session_token
request['Authorization'] = authorization_header

http = Net::HTTP.new(uri.host, uri.port)
http.use_ssl = true
http.read_timeout = 5

response = http.request(request)
raise "Request failed: #{response.code}" unless response.is_a?(Net::HTTPSuccess)

puts response.body
