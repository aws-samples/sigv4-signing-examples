using System.Security.Cryptography;
using System.Text;

var AWS_ACCESS_KEY_ID = Environment.GetEnvironmentVariable("AWS_ACCESS_KEY_ID");
var AWS_SECRET_ACCESS_KEY = Environment.GetEnvironmentVariable("AWS_SECRET_ACCESS_KEY");
var AWS_SESSION_TOKEN = Environment.GetEnvironmentVariable("AWS_SESSION_TOKEN");
var RESTAPIHOST = Environment.GetEnvironmentVariable("RESTAPIHOST");
var RESTAPIPATH = Environment.GetEnvironmentVariable("RESTAPIPATH");

var METHOD = "GET";
var SERVICE = "execute-api";
var REGION = "us-east-1";
var ALGORITHM = "AWS4-HMAC-SHA256";

// Create a datetime object for signing
var now = DateTime.UtcNow;
var amzDate = now.ToString("yyyyMMddTHHmmssZ");
var datestamp = now.ToString("yyyyMMdd");

// Create the canonical request
var canonicalQuerystring = "";
var canonicalHeaders = $"host:{RESTAPIHOST}\n";
var signedHeaders = "host";
var payloadHash = CalculateHash("");
var canonicalRequest = $"{METHOD}\n{RESTAPIPATH}\n{canonicalQuerystring}\n{canonicalHeaders}\n{signedHeaders}\n{payloadHash}";

// Create the string to sign
var credentialScope = $"{datestamp}/{REGION}/{SERVICE}/aws4_request";
var hashedCanonicalRequest = CalculateHash(canonicalRequest);
var stringToSign = $"{ALGORITHM}\n{amzDate}\n{credentialScope}\n{hashedCanonicalRequest}";

// Sign the string
var signingKey = GetSignatureKey(AWS_SECRET_ACCESS_KEY, datestamp, REGION, SERVICE);
var signature = CalculateHmacHex(signingKey, stringToSign);

// Add signing information to the request
var authorizationHeader = $"{ALGORITHM} Credential={AWS_ACCESS_KEY_ID}/{credentialScope}, SignedHeaders={signedHeaders}, Signature={signature}";

// Make the request
using var client = new HttpClient();
client.DefaultRequestHeaders.Add("Host", RESTAPIHOST);
client.DefaultRequestHeaders.Add("x-amz-date", amzDate);
client.DefaultRequestHeaders.Add("x-amz-security-token", AWS_SESSION_TOKEN);
client.DefaultRequestHeaders.Add("Authorization", authorizationHeader);

var requestUrl = $"https://{RESTAPIHOST}{RESTAPIPATH}";
var response = client.GetAsync(requestUrl).Result;

if (response.IsSuccessStatusCode)
{
    var responseBody = response.Content.ReadAsStringAsync().Result;
    Console.WriteLine(responseBody);
}
else
{
    Console.WriteLine($"Error: {(int)response.StatusCode} {response.ReasonPhrase}");
}

byte[] GetSignatureKey(string key, string dateStamp, string regionName, string serviceName)
{
    var kSecret = Encoding.UTF8.GetBytes($"AWS4{key}");
    var kDate = HmacSha256(kSecret, dateStamp);
    var kRegion = HmacSha256(kDate, regionName);
    var kService = HmacSha256(kRegion, serviceName);
    return HmacSha256(kService, "aws4_request");
}

string CalculateHmacHex(byte[] key, string data)
{
    var hash = HmacSha256(key, data);
    return BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
}

byte[] HmacSha256(byte[] key, string data)
{
    using HMACSHA256 hmac = new HMACSHA256(key);
    return hmac.ComputeHash(Encoding.UTF8.GetBytes(data));
}

string CalculateHash(string data)
{
    using SHA256 sha256 = SHA256.Create();
    var hash = sha256.ComputeHash(Encoding.UTF8.GetBytes(data));
    return BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
}
