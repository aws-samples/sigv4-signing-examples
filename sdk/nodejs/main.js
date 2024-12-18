const https = require('https');
const { SignatureV4 } = require('@aws-sdk/signature-v4');
const { Sha256 } = require('@aws-crypto/sha256-js');

async function makeSignedRequest() {
  const accessKey = process.env.AWS_ACCESS_KEY_ID;
  const secretKey = process.env.AWS_SECRET_ACCESS_KEY;
  const sessionToken = process.env.AWS_SESSION_TOKEN;
  const service = 'execute-api';
  const host = process.env.RESTAPIHOST;
  const canonicalURI = process.env.RESTAPIPATH;
  const region = 'us-east-1';

  const options = {
    hostname: host,
    path: canonicalURI,
    method: 'GET',
    headers: {
      'Host': host,
    },
  };

  const signer = new SignatureV4({
    credentials: {
      accessKeyId: accessKey,
      secretAccessKey: secretKey,
      sessionToken: sessionToken
    },
    region: region,
    service: service,
    sha256: Sha256
  });

  const signedRequest = await signer.sign({
    method: options.method,
    headers: options.headers,
    hostname: host,
    path: canonicalURI,
    protocol: 'https:'
  });

  Object.assign(options.headers, signedRequest.headers);

  const req = https.request(options, (res) => {
    console.log(`response Status: ${res.statusCode}`);

    res.on('data', (chunk) => {
      console.log(`response Body: ${chunk}`);
    });
  });

  req.on('error', (e) => {
    console.error(`problem with request: ${e.message}`);
  });

  req.end();
}

makeSignedRequest().catch(error => {
  console.error('Error:', error);
});
