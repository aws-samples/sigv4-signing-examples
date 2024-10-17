import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/***
 * Sample code for POSTING against an AWS API endpoint
 * See
 * https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_sigv-create-signed-request.html
 * */
public class AWSSignerPostExample {
    private static final String AWS_ACCESS_KEY_ID = System.getenv("AWS_ACCESS_KEY_ID");
    private static final String AWS_SECRET_ACCESS_KEY = System.getenv("AWS_SECRET_ACCESS_KEY");
    private static final String X_API_KEY = System.getenv("X_API_KEY");
    private static final String RESTAPIHOST = System.getenv("RESTAPIHOST");
    private static final String RESTAPIPATH = System.getenv("RESTAPIPATH");

    private static final String METHOD = "POST";
    private static final String SERVICE = "execute-api";
    private static final String REGION = "us-east-1";
    private static final String ALGORITHM = "AWS4-HMAC-SHA256";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, Exception {

        String json = "{\"data\": \"123\"}";

        // Create a datetime object for signing
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String amzDate = dateFormat.format(new Date());
        String dateStamp = amzDate.substring(0,8);

        // Create the canonical request
        String canonicalUri = RESTAPIPATH;
        String canonicalQuerystring = "";

        String payloadHash = sha256Hex(json);
        // CanonicalHeaders:
        // - "For the purpose of calculating an authorization signature, only the host and any x-amz-* headers 
        //      are required; however, in order to prevent data tampering, you should consider including all 
        //      the headers in the signature calculation."
        // - "must appear in alphabetical order" / "sorted by header name"
        // - "lowercase with values trimmed"
        // - "If the Content-Type header is present in the request, you must add it to the CanonicalHeaders list. "
        // - The last header is also \n terminated
        String canonicalHeaders = "content-type:application/json\n" + 
                            "host:" + RESTAPIHOST + "\n" +
                            "x-amz-content-sha256:" + payloadHash + "\n" +
                            "x-amz-date:" + amzDate + "\n" + 
                            "x-api-key:" + X_API_KEY + "\n";  // terminate last with \n
        String signedHeaders = "content-type;host;x-amz-content-sha256;x-amz-date;x-api-key"; 

        String canonicalRequest = METHOD + "\n" +
                                  canonicalUri + "\n" +
                                  canonicalQuerystring + "\n" +
                                  canonicalHeaders + "\n" +
                                  signedHeaders + "\n" +
                                  payloadHash;

        String credentialScope = String.format("%s/%s/%s/aws4_request", dateStamp, REGION, SERVICE); 

        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = ALGORITHM + "\n" + 
                            amzDate + "\n" + 
                            credentialScope + "\n" + 
                            hashedCanonicalRequest;

        byte[] signingKey = getSignatureKey(AWS_SECRET_ACCESS_KEY, dateStamp, REGION, SERVICE);
        String signature = hmacSha256Hex(signingKey, stringToSign);

        // Add signing information to the request
        String authorizationHeader = String.format("%s Credential=%s/%s, SignedHeaders=%s, Signature=%s", 
                                    ALGORITHM, AWS_ACCESS_KEY_ID, credentialScope, signedHeaders, signature);

        System.out.println("==========");
        System.out.println("POSTING " + json + " to " + RESTAPIPATH);
        System.out.println("authorizationHeader: " + authorizationHeader);
        System.out.println("==========");

        // Make the request
        String path = "https://" + RESTAPIHOST + canonicalUri;
        URL url = new URL(path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod(METHOD);
        con.setRequestProperty("content-type", "application/json");
        con.setRequestProperty("host", RESTAPIHOST);
        con.setRequestProperty("x-amz-content-sha256", payloadHash);
        con.setRequestProperty("x-amz-date", amzDate);
        con.setRequestProperty("x-api-key", X_API_KEY);
        con.setRequestProperty("Authorization", authorizationHeader);
        
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = json.getBytes("utf-8"); 
            os.write(input, 0, input.length);			
        }

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String responseBody = new String(con.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println(responseBody);
        } else {
            System.out.println("Error: " + responseCode + " " + con.getResponseMessage());
        }
    }

    private static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws NoSuchAlgorithmException {
        byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSha256(kSecret, dateStamp);
        byte[] kRegion = hmacSha256(kDate, regionName);
        byte[] kService = hmacSha256(kRegion, serviceName);
        return hmacSha256(kService, "aws4_request");
    }

    private static String hmacSha256Hex(byte[] key, String data) throws NoSuchAlgorithmException {
        return bytesToHex(hmacSha256(key, data));
    }

    private static byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: HmacSHA256 algorithm not available", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Error: Invalid key for HmacSHA256", e);
        }
    }

    private static String sha256Hex(String data) throws NoSuchAlgorithmException {
        return bytesToHex(MessageDigest.getInstance("SHA-256").digest(data.getBytes(StandardCharsets.UTF_8)));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
