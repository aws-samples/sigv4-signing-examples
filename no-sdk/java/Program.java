package sigv4.signing;

import sigv4.signing.samples.ApiGatewaySample;
import sigv4.signing.samples.BedrockConverseSample;

public class Program {
    public static void main(String[] args) {

        if (args.length == 0 || args[0].equals("ApiGatewaySample")) {
            System.out.println("\n\n*****************API Gateway Sample*******************************");
            String RESTAPIHOST = System.getProperty("RESTAPIHOST");
            String RESTAPIPATH = System.getProperty("RESTAPIPATH");
            String apiRegion = "eu-west-2";
            ApiGatewaySample.run(apiRegion, RESTAPIHOST, RESTAPIPATH);
        }

        if (args.length == 0 || args[0].equals("BedrockConverseSample")) {
            System.out.println("\n\n*****************Bedrock Converse Sample*******************************");
            String modelId = "amazon.titan-text-express-v1";
            String bedrockRegion = "us-east-1";
            String prompt = "What is the capital of England?";
            BedrockConverseSample.run(bedrockRegion, modelId, prompt);
        }
    }
}
