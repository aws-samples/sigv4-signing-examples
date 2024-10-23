public static class HttpHelpers
{
    public static string Get(string service, string region, string url)
    {
        var uri = new Uri(url);        
        var now = DateTime.UtcNow;
        var amzDate = AWSSigner.ToAmzDate(now);
        var authorizationHeader = AWSSigner.GetAuthorizationHeader(service, region,"GET", uri, now);

        // Make the request
        using var client = new HttpClient();
        client.DefaultRequestHeaders.Add("Host", uri.Host);
        client.DefaultRequestHeaders.Add("x-amz-date", amzDate);
        if(!string.IsNullOrWhiteSpace(AwsCredentials.AWS_SESSION_TOKEN))
        {
            client.DefaultRequestHeaders.Add("x-amz-security-token", AwsCredentials.AWS_SESSION_TOKEN);
        }        
        client.DefaultRequestHeaders.TryAddWithoutValidation("Authorization", authorizationHeader);

        var response = client.GetAsync(uri).Result;
        
        var responseBody = response.IsSuccessStatusCode
            ? response.Content.ReadAsStringAsync().Result
            : $"Error: {(int)response.StatusCode} {response.ReasonPhrase} {response.Content.ReadAsStringAsync().Result}";
        
        return responseBody;
    }

    public static string Post(string service, string region, string url, string payload)
    {
        var uri = new Uri(url);        
        var now = DateTime.UtcNow;
        var amzDate = AWSSigner.ToAmzDate(now);
        var payloadHash = AWSSigner.CalculateHash(payload);
        var headers = new Dictionary<string, string>
        {
            {"x-amz-content-sha256", payloadHash},
            {"content-length", payload.Length.ToString()},
            {"content-type", "application/json"}
        };
        var authorizationHeader = AWSSigner.GetAuthorizationHeader(service, region,"POST", uri, now, headers, payloadHash);

        // Make the request
        using var client = new HttpClient();
        client.DefaultRequestHeaders.Add("Host", uri.Host);
        client.DefaultRequestHeaders.Add("x-amz-date", amzDate);
        if(!string.IsNullOrWhiteSpace(AwsCredentials.AWS_SESSION_TOKEN))
        {
            client.DefaultRequestHeaders.Add("x-amz-security-token", AwsCredentials.AWS_SESSION_TOKEN);
        }        
        client.DefaultRequestHeaders.TryAddWithoutValidation("Authorization", authorizationHeader);

        var requestContent = new StringContent(payload);
        requestContent.Headers.Remove("content-type");
        requestContent.Headers.Add("content-type", "application/json");
        requestContent.Headers.Add("content-length", payload.Length.ToString());
        requestContent.Headers.Add("x-amz-content-sha256", payloadHash);

        var response = client.PostAsync(uri, requestContent).Result;
        
        var responseBody = response.IsSuccessStatusCode
            ? response.Content.ReadAsStringAsync().Result
            : $"Error: {(int)response.StatusCode} {response.ReasonPhrase} {response.Content.ReadAsStringAsync().Result}";
        
        return responseBody;
    }
}