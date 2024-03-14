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

java AWSSigner  1.25s user 0.17s system 106% cpu 1.342 total

dotnet build
dotnet run

dotnet run  1.90s user 0.63s system 108% cpu 2.329 total

node main.js

node main.js  0.11s user 0.05s system 21% cpu 0.764 total

python3 main.py

python3 main.py  0.24s user 0.12s system 36% cpu 0.981 total

go run main.go

./main  0.01s user 0.01s system 7% cpu 0.344 total