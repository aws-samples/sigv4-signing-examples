package main

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"time"
)

func makeSignature(t time.Time, payloadHash, region, service, secretAccessKey, algorithm string) string {
	// Create the string to sign
	stringToSign := strings.Join([]string{
		algorithm,
		t.Format("20060102T150405Z"),
		strings.Join([]string{
			t.Format("20060102"),
			region,
			service,
			"aws4_request",
		}, "/"),
		payloadHash,
	}, "\n")

	// Create the signing key
	hash := func(data string, key []byte) []byte {
		h := hmac.New(sha256.New, key)
		h.Write([]byte(data))
		return h.Sum(nil)
	}

	kDate := hash(t.Format("20060102"), []byte("AWS4"+secretAccessKey))
	kRegion := hash(region, kDate)
	kService := hash(service, kRegion)
	kSigning := hash("aws4_request", kService)

	// Sign the string
	signature := hex.EncodeToString(hash(stringToSign, kSigning))

	return signature
}

func main() {
	accessKey := os.Getenv("AWS_ACCESS_KEY_ID")
	secretKey := os.Getenv("AWS_SECRET_ACCESS_KEY")
	sessionToken := os.Getenv("AWS_SESSION_TOKEN")
	service := "execute-api"
	host := os.Getenv("RESTAPIHOST")
	canonicalURI := os.Getenv("RESTAPIPATH")
	region := "us-east-1"
	algorithm := "AWS4-HMAC-SHA256"
	apiGatewayURL := "https://" + host
	now := time.Now().UTC()
	signedHeaders := "host;x-amz-date"

	// Create the canonical request
	payloadHash := "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" // For GET requests, the payload is always an empty string
	canonicalRequest := strings.Join([]string{
		"GET",
		canonicalURI,
		"", // No query string
		strings.Join([]string{
			"host:" + host,
			"x-amz-date:" + now.Format("20060102T150405Z"),
		}, "\n"),
		"",
		signedHeaders,
		payloadHash,
	}, "\n")

	// Create the string to sign
	hashCanonicalRequest := sha256.Sum256([]byte(canonicalRequest))
	stringToSign := makeSignature(now, hex.EncodeToString(hashCanonicalRequest[:]), region, service, secretKey, algorithm)

	// Create the authorization header
	credential := strings.Join([]string{
		accessKey,
		strings.Join([]string{
			now.Format("20060102"),
			region,
			service,
			"aws4_request",
		}, "/"),
	}, "/")
	authHeader := fmt.Sprintf("%s Credential=%s, SignedHeaders=%s, Signature=%s",
		algorithm, credential, signedHeaders, stringToSign)

	// Create the HTTP request
	req, err := http.NewRequest("GET", apiGatewayURL+canonicalURI, nil)
	if err != nil {
		fmt.Println("Error creating request:", err)
		return
	}

	// Add headers
	req.Header.Add("Authorization", authHeader)
	req.Header.Add("X-Amz-Date", now.Format("20060102T150405Z"))
	req.Header.Add("X-Amz-Security-Token", sessionToken)
	req.Header.Add("Host", host)

	// Send the request
	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		fmt.Println("Error sending request:", err)
		return
	}
	defer resp.Body.Close()

	// Read the response body
	body, err := io.ReadAll(io.Reader(resp.Body))
	if err != nil {
		fmt.Println("Error reading response body:", err)
		return
	}

	// Print the response
	fmt.Println("Response Status:", resp.Status)
	fmt.Println("Response Body:", string(body))
}
