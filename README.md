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

export RESTAPIHOST="pyx4v5cl1k.execute-api.us-east-1.amazonaws.com"
export RESTAPIPATH="/Prod/hello"

javac AWSSigner.java
java AWSSigner

dotnet build
dotnet run

cargo run main.go

node main.js

python3 main.py

go run main.go