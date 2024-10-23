public static class AwsCredentials
{
    public static string AWS_ACCESS_KEY_ID => Environment.GetEnvironmentVariable("AWS_ACCESS_KEY_ID");
    public static string AWS_SECRET_ACCESS_KEY => Environment.GetEnvironmentVariable("AWS_SECRET_ACCESS_KEY");
    public static string? AWS_SESSION_TOKEN => Environment.GetEnvironmentVariable("AWS_SESSION_TOKEN");
}