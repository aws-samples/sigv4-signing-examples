public class ApiGatewaySample
{
    const string SERVICE = "execute-api";
    public static void Run(string region, string apiHost, string apiPath)
    {
        var requestUrl = $"https://{apiHost}{apiPath}";
        var responseBody = HttpHelpers.Get(SERVICE, region, requestUrl);
        Console.WriteLine(responseBody);
    }
}