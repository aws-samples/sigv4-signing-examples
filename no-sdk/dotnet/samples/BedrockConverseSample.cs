using System.Text;
using System.Text.Json;

public class BedrockConverseSample
{
    const string SERVICE = "bedrock";
    public static void Run(string region, string modelId, string prompt)
    {
        var requestUrl = $"https://bedrock-runtime.{region}.amazonaws.com/model/{modelId}/converse";
        var payload = JsonSerializer.Serialize(new BedrockConverseRequest(prompt), options: new JsonSerializerOptions(JsonSerializerDefaults.Web));
        var responseBody = HttpHelpers.Post(SERVICE, region, requestUrl, payload);
        Console.WriteLine(responseBody);
    }
}

class BedrockConverseRequest
{
    public BedrockConverseRequest(string prompt)
    {
        Messages = new List<BedrockConverseRequestMessage>{
            new BedrockConverseRequestMessage{
                Content = new List<BedrockConverseRequestMessageContent>{
                    new BedrockConverseRequestMessageContent{
                        Text = prompt
                    }
                },
                Role = "user"
            }
        };
    }
    public List<BedrockConverseRequestMessage> Messages { get; set; }
}

class BedrockConverseRequestMessage
{
    public string Role { get; set; }
    public List<BedrockConverseRequestMessageContent> Content { get; set; }
}

class BedrockConverseRequestMessageContent
{
    public string Text { get; set; }
}