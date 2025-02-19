package sigv4.signing;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class HttpHelpers {

    public static String get(String service, String region, String requestUrl) throws IOException, InterruptedException, URISyntaxException {
        URI uri = new URI(requestUrl);
        ZonedDateTime now = ZonedDateTime.now();
        String amzDate = AWSSigner.toAmzDate(now);

        String authorizationHeader = AWSSigner.getAuthorizationHeader(service, region, "GET", uri, now);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .header("x-amz-date", amzDate)
                .header("Authorization", authorizationHeader);
        if (AwsCredentials.getAwsSessionToken() != null && !AwsCredentials.getAwsSessionToken().isEmpty()) {
            requestBuilder.header("x-amz-security-token", AwsCredentials.getAwsSessionToken());
        }
        HttpRequest request = requestBuilder.build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == HttpURLConnection.HTTP_OK){
            return response.body().toString();
        }else{
            return String.format("Error: %d %s", response.statusCode(), response.body().toString());
        }    
    }

    public static String post(String service, String region, String requestUrl, String payload) throws IOException, InterruptedException, URISyntaxException {
        URI uri = new URI(requestUrl);
        ZonedDateTime now = ZonedDateTime.now();
        String amzDate = AWSSigner.toAmzDate(now);

        String payloadHash = AWSSigner.calculateHash(payload);
        Map<String, String> headers = new HashMap<>();
        headers.put("x-amz-content-sha256", payloadHash);
        headers.put("content-length", String.valueOf(payload.length()));
        headers.put("content-type", "application/json");

        String authorizationHeader = AWSSigner.getAuthorizationHeader(service, region, "POST", uri, now, headers, payloadHash);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .header("x-amz-date", amzDate)
                .header("Authorization", authorizationHeader)
                .header("x-amz-content-sha256", payloadHash)
                .header("content-type", "application/json");

        if (AwsCredentials.getAwsSessionToken() != null && !AwsCredentials.getAwsSessionToken().isEmpty()) {
            requestBuilder.header("x-amz-security-token", AwsCredentials.getAwsSessionToken());
        }

        HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(payload)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == HttpURLConnection.HTTP_OK){
            return response.body().toString();
        }else{
            return String.format("Error: %d %s", response.statusCode(), response.body().toString());
        }
    }
}