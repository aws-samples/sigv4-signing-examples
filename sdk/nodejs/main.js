const https = require('https');
const aws4 = require('aws4');

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

const signer = aws4.sign({
  service: service,
  region: region,
  path: canonicalURI,
  headers: options.headers,
  method: options.method,
  body: '',
}, {
  accessKeyId: accessKey,
  secretAccessKey: secretKey,
  sessionToken: sessionToken,
});

Object.assign(options.headers, signer.headers);

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