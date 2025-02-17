# SigV4 Signing Examples

This repository contains example code implementing the [AWS Signature Version 4 (SigV4)](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_aws-signing.html) protocol for signing requests. 

It is recommended wherever possible that you instead [use the AWS SDKs for creating signed requests](https://docs.aws.amazon.com/IAM/latest/UserGuide/create-signed-request.html#code-signing-examples). There are some scenarios where that might not be possible, such as in IoT or embedded applications where the AWS SDK is not available. As such, the examples in this repository are split in two sets; "sdk" and "no-sdk". The SDK examples show how to use built-in features of the AWS SDK to construct SigV4 signed requests. The No-SDK examples show how to implement the signing from scratch, without the AWS SDK.


This repository primarily demonstrates how to make authenticated requests to AWS API Gateway with IAM Authorization using SigV4 signing protocol, with examples showing both API Gateway execute-api and Amazon Bedrock API calls. The examples are implemented without using AWS SDKs to help understand the underlying SigV4 signing process, and include multiple programming language implementations in the 'no-sdk' section. Before running any sample, ensure you have valid AWS credentials set up in your environment variables (AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY) and the appropriate permissions to access the APIs.


The examples in this repository use temporary credentials. These are are short-lived access credentials and are preferred to long-lived security credentials where possible. For example these might be provided by assuming a role or vended by a token management service. If you want to change any of the examples to use long-lived security credentials instead, simply remove the `x-amz-security-token` header from the request. 

For example, in NodeJS, change the headers from this:

```javascript
  headers: {
    'host': host,
    'x-amz-date': amzDate,
    'x-amz-security-token': sessionToken,
    'Authorization': authorizationHeader
  }
```

To this:

```javascript
  headers: {
    'host': host,
    'x-amz-date': amzDate,
    'Authorization': authorizationHeader
  }
```

Note this repository only contains examples implementing SigV4. For Signature Version 4A (SigV4A) visit the [SigV4a Signing Examples repository](https://github.com/aws-samples/sigv4a-signing-examples).

## Deploying the sample application

A sample application is provided for you to test the SigV4 protocol with. This application deploys a simple serverless API with an AWS API Gateway backed by an AWS Lambda function. 

To deploy the application, the Serverless Application Model (SAM) is used:

```
sam deploy --guided
```

```
Configuring SAM deploy
======================

        Looking for config file [samconfig.toml] :  Not found

        Setting default arguments for 'sam deploy'
        =========================================
        Stack Name [sam-app]: sigv4api
        AWS Region [eu-west-2]: us-east-1
        #Shows you resources changes to be deployed and require a 'Y' to initiate deploy
        Confirm changes before deploy [y/N]: N
        #SAM needs permission to be able to create roles to connect to the resources in your template
        Allow SAM CLI IAM role creation [Y/n]: Y
        #Preserves the state of previously provisioned resources when an operation fails
        Disable rollback [y/N]: y
        HelloWorldFunction has no authentication. Is this okay? [y/N]: y
        Save arguments to configuration file [Y/n]: Y
        SAM configuration file [samconfig.toml]: 
        SAM configuration environment [default]: 
```

Once deployed, retrieve the ApiUrl from the Outputs section and set this and the path as environment variables:

```
export RESTAPIHOST="myapi123.execute-api.us-east-1.amazonaws.com"
export RESTAPIPATH="/Prod/hello"
```

You must also have AWS Credentials set in the environment:

```
export AWS_ACCESS_KEY_ID=ASIAUZABC123456
export AWS_SECRET_ACCESS_KEY=5wfFi0FEaaaaacccc1111111111111
export AWS_SESSION_TOKEN=IQoJb3JpZ2luX2VjE...
```

## Using the examples

For all the examples, a simple "Hello World!" response indicates things are working as expected.

## No-SDK

This section provides examples in the following frameworks:

* Java
* .NET (C#)
* NodeJS
* Python3
* Go
* Ruby
* Rust

### Java

#### APIGatewaySample


`GET` example

This sample demonstrates how to make authenticated HTTP GET requests to Amazon API Gateway using SigV4 signing without using the AWS SDK. The example shows how to properly construct the canonical request, create the string to sign, and generate the final signature required for AWS request authentication. Before running the sample, ensure you have valid AWS credentials set up in your environment variables (AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY) and update the API Gateway endpoint URL in the code to match your deployed API.


```
cd ./java
javac *.java samples/*.java -d build
java -cp build sigv4.signing.Program ApiGatewaySample
```

#### BedrockConverseSample

`POST` example

This sample demonstrates how to make authenticated HTTP POST requests to Amazon Bedrock's model inference API using SigV4 signing without using the AWS SDK. The example shows how to properly construct and sign requests to Bedrock's conversation endpoints, including how to format the request body with your prompt and model parameters. Before running the sample, ensure you have valid AWS credentials set up in your environment variables (AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY) and verify you have access to the Bedrock models in your AWS account.


```
cd ./java
javac *.java samples/*.java -d build
java -cp build sigv4.signing.Program BedrockConverseSample
```

### .NET
dotnet folder has multiple samples.

To run all the samples:

```
cd ./dotnet
dotnet build
dotnet run
```

To run any specific sample:

```
dotnet run ApiGatewaySample
```

```
dotnet run BedrockConverseSample
```

### NodeJS

```
cd ./nodejs
node main.js
```

### Python

```
cd ./python
python3 main.py
```

### Go

```
cd ./golang
go build
./main
```

### Ruby
```
cd ./ruby
ruby main.rb
```

### Rust
```
cd ./rust
cargo run
```


## SDK

This section provides examples in the following frameworks:

* NodeJS
* Python3
* Go
* Ruby
* Rust

### NodeJS

```
cd ./nodejs
npm i
node main.js
```

### Python

```
cd ./python
pip3 install -r requirements.txt
python3 main.py
```

### Go

```
cd ./golang
go get
go build
./main
```

### Ruby

```
cd ./ruby
bundle install
ruby main.rb
```

### Rust
```
cd ./rust
cargo run
```
