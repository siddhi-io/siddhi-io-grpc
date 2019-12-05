siddhi-io-grpc
======================================

  [![Jenkins Build Status](https://wso2.org/jenkins/job/siddhi/job/siddhi-io-grpc/badge/icon)](https://wso2.org/jenkins/job/siddhi/job/siddhi-io-grpc/)
  [![GitHub (pre-)Release](https://img.shields.io/github/release/siddhi-io/siddhi-io-grpc/all.svg)](https://github.com/siddhi-io/siddhi-io-grpc/releases)
  [![GitHub (Pre-)Release Date](https://img.shields.io/github/release-date-pre/siddhi-io/siddhi-io-grpc.svg)](https://github.com/siddhi-io/siddhi-io-grpc/releases)
  [![GitHub Open Issues](https://img.shields.io/github/issues-raw/siddhi-io/siddhi-io-grpc.svg)](https://github.com/siddhi-io/siddhi-io-grpc/issues)
  [![GitHub Last Commit](https://img.shields.io/github/last-commit/siddhi-io/siddhi-io-grpc.svg)](https://github.com/siddhi-io/siddhi-io-grpc/commits/master)
  [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


The **siddhi-io-grpc extension** is an extension to <a target="_blank" href="https://wso2.github.io/siddhi">Siddhi</a> that receives and publishes events via gRPC protocol, calls external services, and serves incoming requests and provide responses.

For information on <a target="_blank" href="https://siddhi.io/">Siddhi</a> and it's features refer <a target="_blank" href="https://siddhi.io/redirect/docs.html">Siddhi Documentation</a>. 

## Download

* from <a target="_blank" href="https://mvnrepository.com/artifact/io.siddhi.extension.io.grpc/siddhi-io-grpc/">here</a>.

## Latest API Docs 

Latest API Docs is <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6">1.0.6</a>.

## Features

The siddhi-io-grpc have two difference implementations, 
* Default gRPC service (<a target="_blank" href="https://github.com/siddhi-io/siddhi-io-grpc/blob/master/component/src/main/resources/EventService.proto">EventService</a>) service
* Generic (Custom) gRPC service implementation

In the default gRPC implementation Siddhi has a predefined gRPC service class called <a target="_blank" href="https://github.com/siddhi-io/siddhi-io-grpc/blob/master/component/src/main/resources/EventService.proto">EventService</a> which can receive and publish events as JSON,XML,Text strings.

In the generic gRPC implementation, we can define a custom gRPC service and use that service with `siddhi-io-grpc` to receive and publish events as `Protobuf` messages. But to that we have to create a jar from the auto-generated Protobuf and gRPC classes and add that jar in to the {SIDDHI_HOME}/jars directory.

* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-sink">grpc</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">Sink</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">gRPC sink will publish events to a given grpc service. We can use this sink when we just want to publish an event from Siddhi, and when we expect empty response back from the gRPC server (Fire and forget). We can also use stream methods (client stream) with this sink to publish stream of requests.</p></p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-call-sink">grpc-call</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">Sink</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;"> gRPC-call sink publish events to a given grpc service and expect a response back. In default mode this will use EventService `process` method. `grpc-call-response` source is used to receive the responses. A unique sink.id is used to correlate between the sink and its corresponding source.</p></p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-service-response-sink">grpc-service-response</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">Sink</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">This extension is used to send responses back to a gRPC client after receiving requests through grpc-service source. This correlates with the particular source using a unique source.id</p></p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-source">grpc</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">Source</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">gRPC source keep listening to gRPC clients and get the request into the relevant Siddhi streams from those clients. Once the grpc source gets the request from the client it sends an empty response back to the client. We can also use stream methods (client stream) with this source to receive stream of requests that publish from clients.</p></p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-call-response-source">grpc-call-response</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">Source</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">This grpc source receives responses received from gRPC server for requests sent from a grpc-call sink. The source will receive responses for sink with the same sink.id. For example if you have a gRPC sink with sink.id 15 then we need to set the sink.id as 15 in the source to receives responses. Sinks and sources have 1:1 mapping</p></p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-service-source">grpc-service</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">Source</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">gRPC service source receive requests from gRPC clients and send responses back to those clients. `grpc-service-response` sink is used to send responses back to those clients. A unique source.id is used to correlate between the source and its corresponding sink.</p></p></div>

## Dependencies 

Add following protobuf jar into {SIDDHI_HOME}/bundles
* <a target="_blank" href="https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java/3.9.1">protobuf-java-3.9.1.jar</a>

## Installation

For installing this extension on various siddhi execution environments refer Siddhi documentation section on <a target="_blank" href="https://siddhi.io/redirect/add-extensions.html">adding extensions</a>.

## Support and Contribution

* We encourage users to ask questions and get support via <a target="_blank" href="https://stackoverflow.com/questions/tagged/siddhi">StackOverflow</a>, make sure to add the `siddhi` tag to the issue for better response.

* If you find any issues related to the extension please report them on <a target="_blank" href="https://github.com/siddhi-io/siddhi-execution-string/issues">the issue tracker</a>.

* For production support and other contribution related information refer <a target="_blank" href="https://siddhi.io/community/">Siddhi Community</a> documentation.
