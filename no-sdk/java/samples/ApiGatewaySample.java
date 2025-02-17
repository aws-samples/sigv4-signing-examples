package sigv4.signing.samples;

import sigv4.signing.HttpHelpers;

public class ApiGatewaySample {
    private static final String SERVICE = "execute-api";

    public static void run(String region, String apiHost, String apiPath) {
        try {
            String requestUrl = String.format("https://%s%s", apiHost, apiPath);
            String responseBody = HttpHelpers.get(SERVICE, region, requestUrl);
            System.out.println(responseBody);
        } catch (Exception e) {
            System.err.println("Error in ApiGatewaySample: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
