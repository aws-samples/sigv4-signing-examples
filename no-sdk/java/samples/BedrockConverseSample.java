package sigv4.signing.samples;

import sigv4.signing.HttpHelpers;

public class BedrockConverseSample {
    private static final String SERVICE = "bedrock";

    public static void run(String region, String modelId, String prompt) {
        try {
            String requestUrl = String.format("https://bedrock-runtime.%s.amazonaws.com/model/%s/converse", region, modelId);
            String payload = String.format("{\"messages\":[{\"role\":\"user\",\"content\":[{\"text\":\"%s\"}]}]}", prompt);
            String responseBody = HttpHelpers.post(SERVICE, region, requestUrl, payload);
            System.out.println(responseBody);
        } catch (Exception e) {
            System.err.println("Error in BedrockConverseSample: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
