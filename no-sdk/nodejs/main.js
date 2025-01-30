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
const region = process.env.AWS_DEFAULT_REGION || 'us-east-1';
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
const requestBody = '';
const requestBodyHash = crypto.createHash('sha256').update(requestBody).digest('hex');
const headers = {
  'Host': host,
  'X-Amz-Date': amzDate,
  'X-Amz-Content-Sha256': requestBodyHash,
};
if (sessionToken) {
  headers['X-Amz-Security-Token'] = sessionToken;
}

const canonicalRequest = createCanonicalRequest(method, endpoint, canonicalQuerystring, headers, requestBodyHash);

// Create the string to sign
const algorithm = 'AWS4-HMAC-SHA256';
const credentialScope = `${dateStamp}/${region}/${service}/aws4_request`;

function getSignedHeaders(headers) {
  return Object.keys(headers)
    .map(key => key.toLowerCase())
    .filter(key => true)
    .sort()
    .join(';');
}

function getCanonicalPath(path, isS3) {
    if (!isS3) {
        // Non-S3 services, we normalize the path and then double URI encode it.
        // Ref: "Remove Dot Segments" https://datatracker.ietf.org/doc/html/rfc3986#section-5.2.4
        const normalizedPathSegments = [];
        for (const pathSegment of path.split("/")) {
            if (pathSegment?.length === 0)
                continue;
            if (pathSegment === ".")
                continue;
            if (pathSegment === "..") {
                normalizedPathSegments.pop();
            }
            else {
                normalizedPathSegments.push(pathSegment);
            }
        }
        // Joining by single slashes to remove consecutive slashes.
        const normalizedPath = `${path?.startsWith("/") ? "/" : ""}${normalizedPathSegments.join("/")}${normalizedPathSegments.length > 0 && path?.endsWith("/") ? "/" : ""}`;
        const doubleEncoded = encodeURIComponent(normalizedPath);
        return doubleEncoded.replace(/%2F/g, "/");
    }
    // For S3, we shouldn't normalize the path. For example, object name
    // my-object//example//photo.user should not be normalized to
    // my-object/example/photo.user
    return path;
}

// Function to create a canonical request for AWS Signature Version 4
function createCanonicalRequest(method, path, queryParams, headers, payloadHash) {
    path = getCanonicalPath(path, service === 's3');
    const canonicalHeaders = Object.keys(headers)
        .map(key => `${key.toLowerCase()}:${headers[key].trim()}\n`)
        .sort()
        .join('');

    const signedHeaders = getSignedHeaders(headers);
    return `${method}\n${path}\n${queryParams}\n${canonicalHeaders}\n${signedHeaders}\n${payloadHash}`;
}

// Sign the string
const getSignatureKey = (key, dateStamp, regionName, serviceName) => {
  const kDate = crypto.createHmac('SHA256', `AWS4${key}`).update(dateStamp).digest();
  const kRegion = crypto.createHmac('SHA256', kDate).update(regionName).digest();
  const kService = crypto.createHmac('SHA256', kRegion).update(serviceName).digest();
  const kSigning = crypto.createHmac('SHA256', kService).update('aws4_request').digest();
  return kSigning;
};
const signingKey = getSignatureKey(secretKey, dateStamp, region, service);

function getSignatureSubject(algorithm, amzDate, scope, canonicalRequest) {
  let hashedCanonicalRequest = crypto.createHash("sha256").update(canonicalRequest).digest("hex");
  return `${algorithm}\n${amzDate}\n${scope}\n${hashedCanonicalRequest}`;
}

const stringToSign = getSignatureSubject(algorithm, amzDate, credentialScope, canonicalRequest);
const signature = crypto.createHmac('sha256', signingKey).update(stringToSign).digest('hex');

// Add signing information to the request
const authorizationHeader = `${algorithm} Credential=${accessKey}/${credentialScope}, SignedHeaders=${getSignedHeaders(headers)}, Signature=${signature}`;

// Make the request
const options = {
  hostname: host,
  path: canonicalUri,
  method,
  headers: {
    ...headers,
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
