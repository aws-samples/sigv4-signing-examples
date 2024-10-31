require 'aws-sdk-core'
require 'net/http'
require 'uri'
require 'json'

access_key = ENV['AWS_ACCESS_KEY_ID']
secret_key = ENV['AWS_SECRET_ACCESS_KEY']
session_token = ENV['AWS_SESSION_TOKEN']
service = 'execute-api'
host = ENV['RESTAPIHOST']
canonical_uri = ENV['RESTAPIPATH']
region = 'us-east-1'
method = 'GET'
url = "https://#{host}#{canonical_uri}"

credentials = Aws::Credentials.new(
  access_key,
  secret_key,
  session_token
)

signer = Aws::Sigv4::Signer.new(
  service: service,
  region: region,
  credentials_provider: credentials
)

uri = URI.parse(url)
request_options = {
  method: method,
  url: url,
  headers: {
    'Host' => host
  }
}

signed_headers = signer.sign_request(
  http_method: method,
  url: uri,
  headers: request_options[:headers]
)

begin
  uri = URI(url)
  http = Net::HTTP.new(uri.host, uri.port)
  http.use_ssl = true
  
  request = Net::HTTP::Get.new(uri)
  signed_headers.headers.each do |key, value|
    request[key] = value
  end
  
  response = http.request(request)
  
  puts "Response Status: #{response.code}"
  puts "Response Body: #{response.body}"
rescue StandardError => e
  puts "Error: #{e.message}"
end
