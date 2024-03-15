package main

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"os"
	"time"

	"github.com/aws/aws-sdk-go-v2/aws"
	v4 "github.com/aws/aws-sdk-go-v2/aws/signer/v4"
)

func main() {
	ctx := context.Background()
	accessKey := os.Getenv("AWS_ACCESS_KEY_ID")
	secretKey := os.Getenv("AWS_SECRET_ACCESS_KEY")
	sessionToken := os.Getenv("AWS_SESSION_TOKEN")
	service := "execute-api"
	host := os.Getenv("RESTAPIHOST")
	canonicalURI := os.Getenv("RESTAPIPATH")
	region := "us-east-1"
	payloadHash := "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" // For GET requests, the payload is always an empty string

	creds := aws.Credentials{
		AccessKeyID:     accessKey,
		SecretAccessKey: secretKey,
		SessionToken:    sessionToken,
	}

	apiUrl := "https://" + host + canonicalURI
	req, err := http.NewRequest("GET", apiUrl, nil)
	if err != nil {
		fmt.Printf("Error creating request: %v", err)
		return
	}

	req.Host = host
	signer := v4.NewSigner()
	if err = signer.SignHTTP(ctx, creds, req, payloadHash, service, region, time.Now()); err != nil {
		fmt.Printf("Error signing request: %v", err)
		return
	}

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		fmt.Printf("Error making request: %v", err)
		return
	}
	defer resp.Body.Close()

	fmt.Printf("Response Status: %s\n", resp.Status)
	body, err := io.ReadAll(io.Reader(resp.Body))
	if err != nil {
		fmt.Printf("Error reading response body: %v", err)
		return
	}
	fmt.Printf("Response Body: %s\n", body)
}
