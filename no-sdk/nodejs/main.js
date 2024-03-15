const crypto = require('crypto');
const https = require('https');

// AWS access keys 
const accessKey = process.env.AWS_ACCESS_KEY_ID;
const secretKey = process.env.AWS_SECRET_ACCESS_KEY;
const sessionToken = process.env.AWS_SESSION_TOKEN;

// Request parameters
const method = 'GET';
const service = 'execute-api';
const host = process.env.RESTAPIHOST;
const region = 'us-east-1';
const endpoint = process.env.RESTAPIPATH;

// Create a datetime object for signing
const now = new Date();
const year = now.getUTCFullYear();
const month = String(now.getUTCMonth() + 1).padStart(2, '0');
const day = String(now.getUTCDate()).padStart(2, '0');
const hours = String(now.getUTCHours()).padStart(2, '0');
const minutes = String(now.getUTCMinutes()).padStart(2, '0');
const seconds = String(now.getUTCSeconds()).padStart(2, '0');
const amzDate = `${year}${month}${day}T${hours}${minutes}${seconds}Z`;
const dateStamp = amzDate.slice(0, 8);

// Create the canonical request
const canonicalUri = endpoint;
const canonicalQuerystring = '';
const canonicalHeaders = `host:${host}\n`;
const signedHeaders = 'host';
const payloadHash = 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855'; // SHA-256 hash of an empty string
const canonicalRequest = `${method}\n${canonicalUri}\n${canonicalQuerystring}\n${canonicalHeaders}\n${signedHeaders}\n${payloadHash}`;

// Create the string to sign
const algorithm = 'AWS4-HMAC-SHA256';
const credentialScope = `${dateStamp}/${region}/${service}/aws4_request`;
const hashCanonicalRequest = crypto.createHash('sha256').update(canonicalRequest).digest('hex');
const stringToSign = `${algorithm}\n${amzDate}\n${credentialScope}\n${hashCanonicalRequest}`;

// Sign the string
const getSignatureKey = (key, dateStamp, regionName, serviceName) => {
  const kDate = crypto.createHmac('SHA256', `AWS4${key}`).update(dateStamp).digest();
  const kRegion = crypto.createHmac('SHA256', kDate).update(regionName).digest();
  const kService = crypto.createHmac('SHA256', kRegion).update(serviceName).digest();
  const kSigning = crypto.createHmac('SHA256', kService).update('aws4_request').digest();
  return kSigning;
};
const signingKey = getSignatureKey(secretKey, dateStamp, region, service);
const signature = crypto.createHmac('sha256', signingKey).update(stringToSign).digest('hex');

// Add signing information to the request
const authorizationHeader = `${algorithm} Credential=${accessKey}/${credentialScope}, SignedHeaders=${signedHeaders}, Signature=${signature}`;

// Make the request
const options = {
  hostname: host,
  path: canonicalUri,
  method,
  headers: {
    'host': host,
    'x-amz-date': amzDate,
    'x-amz-security-token': sessionToken,
    'Authorization': authorizationHeader
  }
};

const req = https.request(options, (res) => {
  let responseBody = '';

  res.on('data', (chunk) => {
    responseBody += chunk;
  });

  res.on('end', () => {
    console.log(responseBody);
  });
});

req.on('error', (err) => {
  console.error(`Error: ${err.message}`);
});

req.end();
