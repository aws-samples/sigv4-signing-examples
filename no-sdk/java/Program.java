package sigv4.signing;

import sigv4.signing.samples.ApiGatewaySample;
import sigv4.signing.samples.BedrockConverseSample;

public class Program {
    public static void main(String[] args) {

        String region = System.getenv("AWS_DEFAULT_REGION") != null 
            ? System.getenv("AWS_DEFAULT_REGION") 
            : "us-east-1";

        if (args.length == 0 || args[0].equals("ApiGatewaySample")) {
            System.out.println("\n\n*****************API Gateway Sample*******************************");
            String RESTAPIHOST = System.getenv("RESTAPIHOST");
            String RESTAPIPATH = System.getenv("RESTAPIPATH");
            ApiGatewaySample.run(region, RESTAPIHOST, RESTAPIPATH);
        }

        if (args.length == 0 || args[0].equals("BedrockConverseSample")) {
            System.out.println("\n\n*****************Bedrock Converse Sample*******************************");
            String modelId = "amazon.titan-text-express-v1";
            String prompt = "What is the capital of England?";
            BedrockConverseSample.run(region, modelId, prompt);
        }
    }
}
