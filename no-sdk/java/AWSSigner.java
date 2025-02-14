package sigv4.signing;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AWSSigner {
    private static final String ALGORITHM = "AWS4-HMAC-SHA256";

    public static String getAuthorizationHeader(String service, String region, String httpMethod, URI uri, ZonedDateTime now) {
        return getAuthorizationHeader(service, region, httpMethod, uri, now, new HashMap<String, String>(), calculateHash(""));
    }

    public static String getAuthorizationHeader(String service, String region, String httpMethod, URI uri, ZonedDateTime now,
                                                Map<String, String> headers, String payloadHash) {
        String amzDate = toAmzDate(now);
        String datestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        headers.putIfAbsent("host", uri.getHost());
        headers.putIfAbsent("x-amz-date", amzDate);
        if (AwsCredentials.getAwsSessionToken() != null && !AwsCredentials.getAwsSessionToken().isEmpty()) {
            headers.putIfAbsent("x-amz-security-token", AwsCredentials.getAwsSessionToken());
        }

        // Create the canonical request
        String canonicalQuerystring = "";
        String canonicalHeaders = canonicalizeHeaders(headers);
        String signedHeaders = canonicalizeHeaderNames(headers);
        String canonicalRequest = httpMethod + "\n" + uri.getPath() + "\n" + canonicalQuerystring + "\n" +
                canonicalHeaders + "\n" + signedHeaders + "\n" + payloadHash;

        // Create the string to sign
        String credentialScope = datestamp + "/" + region + "/" + service + "/aws4_request";
        String stringToSign = ALGORITHM + "\n" + amzDate + "\n" + credentialScope + "\n" + calculateHash(canonicalRequest);

        // Calculate the signature
        byte[] signingKey = getSignatureKey(AwsCredentials.getAwsSecretAccessKey(), datestamp, region, service);
        String signature = calculateHmacHex(signingKey, stringToSign);

        // Create the authorization header
        return ALGORITHM + " Credential=" + AwsCredentials.getAwsAccessKeyId() + "/" + credentialScope +
                ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;
    }

    public static String toAmzDate(ZonedDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
        //return "20250214T131509Z";
    }

    private static String canonicalizeHeaderNames(Map<String, String> headers) {
        return headers.keySet().stream()
                .map(String::toLowerCase)
                .sorted()
                .collect(Collectors.joining(";"));
    }

    private static String canonicalizeHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .map(entry -> entry.getKey().toLowerCase() + ":" + entry.getValue().trim() + "\n")
                .collect(Collectors.joining());
    }

    private static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) {
        byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSHA256(kSecret, dateStamp);
        byte[] kRegion = hmacSHA256(kDate, regionName);
        byte[] kService = hmacSHA256(kRegion, serviceName);
        return hmacSHA256(kService, "aws4_request");
    }

    private static String calculateHmacHex(byte[] key, String data) {
        byte[] hmac = hmacSHA256(key, data);
        return bytesToHex(hmac);
    }

    private static byte[] hmacSHA256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }

    public static String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate SHA-256 hash", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));            
        }
        return result.toString();
    }
}
