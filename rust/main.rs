extern crate chrono;
extern crate hmac; 
extern crate sha2;

use chrono::{DateTime, Utc};
use hmac::{Hmac, Mac};
use sha2::{Sha256, Digest};
use std::env;

type HmacSha256 = Hmac<Sha256>;

fn make_signature(
    t: DateTime<Utc>,
    payload_hash: &str,
    region: &str,
    service: &str,
    secret_access_key: &str,
    algorithm: &str,
) -> String {
    // Create the Task1 string
    let _task1 = format!(
        "{}\n{}\n{}/{}/{}/{}/aws4_request\n{}",
        algorithm,
        t.format("%Y%m%dT%H%M%SZ"),
        t.format("%Y%m%d"),
        region,
        service,
        "aws4_request",
        payload_hash
    );

    // Create the signing key
    let signing_key = format!("AWS4{}", secret_access_key);
    let mut k_signing = HmacSha256::new_from_slice(signing_key.as_bytes()).unwrap();
    let mut k_date = HmacSha256::new_from_slice(signing_key.as_bytes())
        .unwrap()
        .clone();
    k_date.update(t.format("%Y%m%d").to_string().as_bytes());
    let mut k_region = HmacSha256::new_from_slice(&k_date.finalize().into_bytes()).unwrap();
    k_region.update(region.as_bytes());
    let mut k_service = HmacSha256::new_from_slice(&k_region.finalize().into_bytes()).unwrap();
    k_service.update(service.as_bytes());
    k_signing.update(&k_service.finalize().into_bytes());
    k_signing.update(b"aws4_request");

    // Sign the string
    let signature = hex::encode(k_signing.finalize().into_bytes());

    signature
}

fn main() {
    let access_key = env::var("AWS_ACCESS_KEY_ID").unwrap();
    let secret_key = env::var("AWS_SECRET_ACCESS_KEY").unwrap();
    let session_token = env::var("AWS_SESSION_TOKEN").unwrap();
    let service = "execute-api";
    let host = env::var("RESTAPIHOST").unwrap();
    let canonical_uri = env::var("RESTAPIPATH").unwrap();
    let region = "us-east-1";
    let algorithm = "AWS4-HMAC-SHA256";
    let api_gateway_url = format!("https://{}", host);
    let now = Utc::now();
    let signed_headers = "host;x-amz-date";

    // Create the canonical request
    let payload_hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"; // For GET requests, the payload is always an empty string
    let canonical_request = format!(
        "GET\n{}\n\n{}\n\n{}\n{}",
        canonical_uri,
        format!(
            "host:{}\nx-amz-date:{}",
            api_gateway_url.split("://").last().unwrap(),
            now.format("%Y%m%dT%H%M%SZ")
        ),
        signed_headers,
        payload_hash
    );

    // Create the string to sign
    let mut hasher = Sha256::new();
    hasher.update(canonical_request.as_bytes());
    let hash_canonical_request = hex::encode(hasher.finalize());
    let string_to_sign = make_signature(
        now,
        &hash_canonical_request,
        &region,
        &service,
        &secret_key,
        &algorithm,
    );

    // Create the authorization header
    let credential = format!(
        "{}/{}/{}/{}/aws4_request",
        access_key,
        now.format("%Y%m%d"),
        region,
        service
    );
    let auth_header = format!(
        "{} Credential={}, SignedHeaders={}, Signature={}",
        algorithm, credential, signed_headers, string_to_sign
    );

    // Create the HTTP request
    let client = reqwest::blocking::Client::new();
    let res = client
        .get(&format!("{}{}", api_gateway_url, canonical_uri))
        .header("Authorization", auth_header)
        .header("X-Amz-Date", now.format("%Y%m%dT%H%M%SZ").to_string())
        .header("X-Amz-Security-Token", session_token)
        .header("Host", api_gateway_url.split("://").last().unwrap())
        .send()
        .unwrap();

    // Print the response
    println!("Response Status: {}", res.status());
    println!("Response Body: {}", res.text().unwrap());
}