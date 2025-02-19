package sigv4.signing;

public class AwsCredentials {
    public static String getAwsAccessKeyId() {
        return System.getenv("AWS_ACCESS_KEY_ID");
    }

    public static String getAwsSecretAccessKey() {
        return System.getenv("AWS_SECRET_ACCESS_KEY");
    }

    public static String getAwsSessionToken() {
        return System.getenv("AWS_SESSION_TOKEN");
    }
}
