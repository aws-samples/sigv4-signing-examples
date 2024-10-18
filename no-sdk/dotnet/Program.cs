
Console.WriteLine("\n\n*****************API Gateway Sample*******************************");

var RESTAPIHOST = Environment.GetEnvironmentVariable("RESTAPIHOST");
var RESTAPIPATH = Environment.GetEnvironmentVariable("RESTAPIPATH");
var apiRegion = "us-east-1";
ApiGatwaySample.Run(apiRegion, RESTAPIHOST, RESTAPIPATH);

Console.WriteLine("\n\n*****************Bedrock Converse Sample*******************************");

var modelId = "amazon.titan-text-express-v1";
var bedrockRegion = "us-east-1";
var prompt = "Which is the capital of England?";
BedrockConverseSample.Run(bedrockRegion, modelId, prompt);

Console.WriteLine("\n\n");