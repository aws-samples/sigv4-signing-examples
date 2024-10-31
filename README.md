# SigV4 Signing Examples

This repository contains example code implementing the [AWS Signature Version 4 (SigV4)](https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_aws-signing.html) protocol for signing requests. 

It is recommended wherever possible that you instead [use the AWS SDKs for creating signed requests](https://docs.aws.amazon.com/IAM/latest/UserGuide/create-signed-request.html#code-signing-examples). There are some scenarios where that might not be possible, such as in IoT or embedded applications where the AWS SDK is not available. As such, the examples in this repository are split in two sets; "sdk" and "no-sdk". The SDK examples show how to use built-in features of the AWS SDK to construct SigV4 signed requests. The No-SDK examples show how to implement the signing from scratch, without the AWS SDK.

The examples in this repository use an AWS API Gateway execute-api request. The API Gateway has an IAM Authorizer, which requires the request to be signed using the SigV4 protocol. You can adjust the examples depending on the AWS API you wish to call. Note that, 'no-sdk' section has multiple examples for some of the languages. Please follow instructions in the respective sections.

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

### Java

`GET` example

```
cd ./java
javac AWSSigner.java
java AWSSigner
```

`POST` example
This example demonstrates a signed `POST` with additional canonical headers and an `x-api-key`.
It is taken from a real world use case and is untested against the Sample Application.

```
cd ./java
javac AWSSignerPostExample.java
# This example uses an x-api-key header
export X_API_KEY=<my api key>
java AWSSignerPostExample
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

## SDK

This section provides examples in the following frameworks:

* NodeJS
* Python3
* Go
* Ruby

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
