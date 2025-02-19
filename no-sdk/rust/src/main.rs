use std::env;
use chrono::Utc;
use hmac::{Hmac, Mac};
use sha2::{Sha256, Digest};
use hex::encode;

type HmacSha256 = Hmac<Sha256>;

fn main() {
    dotenv::dotenv().ok();

    // AWS credentials
    let access_key = env::var("AWS_ACCESS_KEY_ID").expect("AWS_ACCESS_KEY_ID not set");
    let secret_key = env::var("AWS_SECRET_ACCESS_KEY").expect("AWS_SECRET_ACCESS_KEY not set");
    let session_token = env::var("AWS_SESSION_TOKEN").ok();
    
    // Request parameters
    let method = "GET";
    let service = "execute-api";
    let host = env::var("RESTAPIHOST").expect("RESTAPIHOST not set");
    let region = "us-east-1";
    let endpoint = env::var("RESTAPIPATH").expect("RESTAPIPATH not set");

    // Create datetime strings for signing
    let now = Utc::now();
    let amz_date = now.format("%Y%m%dT%H%M%SZ").to_string();
    let date_stamp = now.format("%Y%m%d").to_string();

    // Create canonical request
    let canonical_uri = endpoint.clone();
    let canonical_querystring = "";
    let canonical_headers = format!("host:{}\nx-amz-date:{}\n", host, amz_date);
    let signed_headers = "host;x-amz-date";
    let payload_hash = encode(Sha256::digest(b""));

    let canonical_request = format!("{}\n{}\n{}\n{}\n{}\n{}",
        method,
        canonical_uri,
        canonical_querystring,
        canonical_headers,
        signed_headers,
        payload_hash
    );

    // Create string to sign
    let algorithm = "AWS4-HMAC-SHA256";
    let credential_scope = format!("{}/{}/{}/aws4_request", date_stamp, region, service);
    let string_to_sign = format!("{}\n{}\n{}\n{}",
        algorithm,
        amz_date,
        credential_scope,
        encode(Sha256::digest(canonical_request.as_bytes()))
    );

    // Calculate signature
    let k_date = sign(format!("AWS4{}", secret_key).as_bytes(), date_stamp.as_bytes());
    let k_region = sign(&k_date, region.as_bytes());
    let k_service = sign(&k_region, service.as_bytes());
    let k_signing = sign(&k_service, b"aws4_request");
    let signature = encode(sign(&k_signing, string_to_sign.as_bytes()));

    // Create authorization header
    let credential = format!("{}/{}", access_key, credential_scope);
    let auth_header = format!("{} Credential={}, SignedHeaders={}, Signature={}",
        algorithm, credential, signed_headers, signature
    );

    // Create client and send request
    let client = reqwest::blocking::Client::new();
    let mut request = client.get(format!("https://{}{}", host, endpoint))
        .header("Authorization", auth_header)
        .header("x-amz-date", amz_date);

    if let Some(token) = session_token {
        request = request.header("x-amz-security-token", token);
    }

    match request.send() {
        Ok(response) => {
            println!("Status: {}", response.status());
            println!("Body: {}", response.text().unwrap());
        },
        Err(e) => println!("Error: {}", e),
    }
}

fn sign(key: &[u8], msg: &[u8]) -> Vec<u8> {
    let mut mac = HmacSha256::new_from_slice(key)
        .expect("HMAC can take key of any size");
    mac.update(msg);
    mac.finalize().into_bytes().to_vec()
}