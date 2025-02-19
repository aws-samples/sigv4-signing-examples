use reqwest::Client;
use std::env;
use aws_sigv4::{
    http_request::{sign, SignableBody, SignableRequest, SigningSettings},
    sign::v4,
};
use std::time::SystemTime;
use aws_credential_types::Credentials;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let host = env::var("RESTAPIHOST").expect("RESTAPIHOST not set");
    let endpoint = env::var("RESTAPIPATH").expect("RESTAPIPATH not set");
    let access_key = env::var("AWS_ACCESS_KEY_ID").expect("AWS_ACCESS_KEY_ID not set");
    let secret_key = env::var("AWS_SECRET_ACCESS_KEY").expect("AWS_SECRET_ACCESS_KEY not set");
    let session_token = env::var("AWS_SESSION_TOKEN").expect("AWS_SESSION_TOKEN not set");
    let url = format!("https://{}{}", host, endpoint);
    let region = "us-east-1";
    let service = "execute-api";
    
    // Create the HTTP request
    let mut request = http::Request::builder()
        .method("GET")
        .uri(&url)
        .header("host", &host)
        .body("")?;

    let creds = Credentials::new(
        access_key,
        secret_key,
        Some(session_token),
        None,
        &service,
    );
    let identity = creds.into();

    // Set up signing parameters
    let signing_settings = SigningSettings::default();
    let signing_params = v4::SigningParams::builder()
        .identity(&identity)
        .region(&region)
        .name(&service)
        .time(SystemTime::now())
        .settings(signing_settings)
        .build()?
        .into();

    let signable_request = SignableRequest::new(
        request.method().as_str(),
        request.uri().to_string(),
        request
            .headers()
            .iter()
            .map(|(k, v)| (k.as_str(), std::str::from_utf8(v.as_bytes()).unwrap())),
        SignableBody::Bytes(request.body().as_bytes()),
    )?;

    // Sign the request
    let (signing_instructions, _signature) = sign(signable_request, &signing_params)?.into_parts();
    signing_instructions.apply_to_request_http1x(&mut request);
    let reqwest_req: reqwest::Request = request.try_into()?;
    let res = Client::new().execute(reqwest_req).await?;

    println!("Status: {}", res.status());
    println!("Body: {}", res.text().await?);

    Ok(())
}