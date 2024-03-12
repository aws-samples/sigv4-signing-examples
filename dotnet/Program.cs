using System;
using System.Net.Http;
using System.Security.Cryptography;
using System.Text;

class AWSSigner
{
    private static readonly string AWS_ACCESS_KEY_ID = Environment.GetEnvironmentVariable("AWS_ACCESS_KEY_ID");
    private static readonly string AWS_SECRET_ACCESS_KEY = Environment.GetEnvironmentVariable("AWS_SECRET_ACCESS_KEY");
    private static readonly string AWS_SESSION_TOKEN = Environment.GetEnvironmentVariable("AWS_SESSION_TOKEN");
    private static readonly string RESTAPIHOST = Environment.GetEnvironmentVariable("RESTAPIHOST");
    private static readonly string RESTAPIPATH = Environment.GetEnvironmentVariable("RESTAPIPATH");

    private static readonly string METHOD = "GET";
    private static readonly string SERVICE = "execute-api";
    private static readonly string REGION = "us-east-1";
    private static readonly string ALGORITHM = "AWS4-HMAC-SHA256";

    static void Main()
    {
        // Create a datetime object for signing
        DateTime now = DateTime.UtcNow;
        string amzDate = now.ToString("yyyyMMddTHHmmssZ");
        string datestamp = now.ToString("yyyyMMdd");

        // Create the canonical request
        string canonicalUri = RESTAPIPATH;
        string canonicalQuerystring = "";
        string canonicalHeaders = $"host:{RESTAPIHOST}\n";
        string signedHeaders = "host";
        string payloadHash = CalculateHash("");
        string canonicalRequest = $"{METHOD}\n{canonicalUri}\n{canonicalQuerystring}\n{canonicalHeaders}\n{signedHeaders}\n{payloadHash}";

        // Create the string to sign
        string credentialScope = $"{datestamp}/{REGION}/{SERVICE}/aws4_request";
        string hashedCanonicalRequest = CalculateHash(canonicalRequest);
        string stringToSign = $"{ALGORITHM}\n{amzDate}\n{credentialScope}\n{hashedCanonicalRequest}";

        // Sign the string
        byte[] signingKey = GetSignatureKey(AWS_SECRET_ACCESS_KEY, datestamp, REGION, SERVICE);
        string signature = CalculateHmacHex(signingKey, stringToSign);

        // Add signing information to the request
        string authorizationHeader = $"{ALGORITHM} Credential={AWS_ACCESS_KEY_ID}/{credentialScope}, SignedHeaders={signedHeaders}, Signature={signature}";

        // Make the request
        using (HttpClient client = new HttpClient())
        {
            client.DefaultRequestHeaders.Add("Host", RESTAPIHOST);
            client.DefaultRequestHeaders.Add("x-amz-date", amzDate);
            client.DefaultRequestHeaders.Add("x-amz-security-token", AWS_SESSION_TOKEN);
            client.DefaultRequestHeaders.Add("Authorization", authorizationHeader);

            string requestUrl = $"https://{RESTAPIHOST}{canonicalUri}";
            HttpResponseMessage response = client.GetAsync(requestUrl).Result;

            if (response.IsSuccessStatusCode)
            {
                string responseBody = response.Content.ReadAsStringAsync().Result;
                Console.WriteLine(responseBody);
            }
            else
            {
                Console.WriteLine($"Error: {(int)response.StatusCode} {response.ReasonPhrase}");
            }
        }
    }

    private static byte[] GetSignatureKey(string key, string dateStamp, string regionName, string serviceName)
    {
        byte[] kSecret = Encoding.UTF8.GetBytes($"AWS4{key}");
        byte[] kDate = HmacSha256(kSecret, dateStamp);
        byte[] kRegion = HmacSha256(kDate, regionName);
        byte[] kService = HmacSha256(kRegion, serviceName);
        return HmacSha256(kService, "aws4_request");
    }

    private static string CalculateHmacHex(byte[] key, string data)
    {
        byte[] hash = HmacSha256(key, data);
        return BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
    }

    private static byte[] HmacSha256(byte[] key, string data)
    {
        using (HMACSHA256 hmac = new HMACSHA256(key))
        {
            return hmac.ComputeHash(Encoding.UTF8.GetBytes(data));
        }
    }

    private static string CalculateHash(string data)
    {
        using (SHA256 sha256 = SHA256.Create())
        {
            byte[] hash = sha256.ComputeHash(Encoding.UTF8.GetBytes(data));
            return BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
        }
    }
}
