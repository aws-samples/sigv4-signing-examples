<?php
require './vendor/autoload.php';

use Aws\Signature\SignatureV4;
use Aws\Credentials\Credentials;
use GuzzleHttp\Psr7\Request;
use GuzzleHttp\Client;

$body = array(); //fill the body
$body = json_encode($body);

$service = '*********';
$region = '************';
$signer = new SignatureV4($service, $region);

$headers = array( 'Content-Type' => 'application/json');
$request = new Request(
  'POST',
  'https://runtime.*********.eu-central-1.amazonaws.com/endpoints/**************/invocations',
  $headers,
  $body
);

$awsKey = '********************';
$awsSecret = '****************************************';
$credentials = new Credentials($awsKey, $awsSecret);

$request = $signer->signRequest($request, $credentials);
$client = new Client();
$response = $client->send($request);
