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

* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-sink">grpc</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">Sink</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">This extension publishes event data encoded into GRPC Classes as defined in the user input jar. This extension has a default gRPC service classes added. The default service is called "EventService". Please find the protobuf definition [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/EventService.proto). If we want to use our custom gRPC services, we have to  pack auto-generated gRPC service classes and  protobuf classes into a jar file and add it into the project classpath (or to the <code>jars</code> folder in the <code>siddhi-tooling</code> folder if we use it with <code>siddhi-tooling</code>). Please find the custom protobuf definition that uses in examples [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/sample.proto). This grpc sink is used for scenarios where we send a request and don't expect a response back. I.e getting a google.protobuf.Empty response back.</p></p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-call-sink">grpc-call</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">Sink</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">This extension publishes event data encoded into GRPC Classes as defined in the user input jar. This extension has a default gRPC service classes jar added. The default service is called "EventService". Please find the protobuf definition [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/EventService.proto). If we want to use our custom gRPC services, we have to  pack auto-generated gRPC service classes and  protobuf classes into a jar file and add it into the project classpath (or to the <code>jars</code> folder in the <code>siddhi-tooling</code> folder if we use it with <code>siddhi-tooling</code>). Please find the custom protobuf definition that uses in examples [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/sample.proto). This grpc-call sink is used for scenarios where we send a request out and expect a response back. In default mode this will use EventService process method. grpc-call-response source is used to receive the responses. A unique sink.id is used to correlate between the sink and its corresponding source.</p></p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-service-response-sink">grpc-service-response</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">Sink</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">This extension is used to send responses back to a gRPC client after receiving requests through grpc-service source. This correlates with the particular source using a unique source.id</p></p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-source">grpc</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">Source</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">This extension starts a grpc server during initialization time. The server listens to requests from grpc stubs. This source has a default mode of operation and custom user defined grpc service mode. By default this uses EventService. Please find the proto definition [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/EventService.proto). In the default mode this source will use EventService consume method. If we want to use our custom gRPC services, we have to  pack auto-generated gRPC service classes and  protobuf classes into a jar file and add it into the project classpath (or to the <code>jars</code> folder in the <code>siddhi-tooling</code> folder if we use it with <code>siddhi-tooling</code>). Please find the custom protobuf definition that uses in examples [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/sample.proto). This method will receive requests and injects them into stream through a mapper.</p></p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-call-response-source">grpc-call-response</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">Source</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">This grpc source receives responses received from gRPC server for requests sent from a grpc-call sink. The source will receive responses for sink with the same sink.id. For example if you have a gRPC sink with sink.id 15 then we need to set the sink.id as 15 in the source to receives responses. Sinks and sources have 1:1 mapping</p></p></div>
* <a target="_blank" href="https://siddhi-io.github.io/siddhi-io-grpc/api/1.0.6/#grpc-service-source">grpc-service</a> *(<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">Source</a>)*<br> <div style="padding-left: 1em;"><p><p style="word-wrap: break-word;margin: 0;">This extension implements a grpc server for receiving and responding to requests. During initialization time a grpc server is started on the user specified port exposing the required service as given in the url. This source also has a default mode and a user defined grpc service mode. By default this uses EventService. Please find the proto definition [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/EventService.proto) In the default mode this will use the EventService process method. If we want to use our custom gRPC services, we have to  pack auto-generated gRPC service classes and  protobuf classes into a jar file and add it into the project classpath (or to the <code>jars</code> folder in the <code>siddhi-tooling</code> folder if we use it with <code>siddhi-tooling</code>). Please find the custom protobuf definition that uses in examples [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/sample.proto). This accepts grpc message class Event as defined in the EventService proto. This uses GrpcServiceResponse sink to send reponses back in the same Event message format.</p></p></div>

## Dependencies 

There are no other dependencies needed for this extension. 

## Installation

For installing this extension on various siddhi execution environments refer Siddhi documentation section on <a target="_blank" href="https://siddhi.io/redirect/add-extensions.html">adding extensions</a>.

## Support and Contribution

* We encourage users to ask questions and get support via <a target="_blank" href="https://stackoverflow.com/questions/tagged/siddhi">StackOverflow</a>, make sure to add the `siddhi` tag to the issue for better response.

* If you find any issues related to the extension please report them on <a target="_blank" href="https://github.com/siddhi-io/siddhi-execution-string/issues">the issue tracker</a>.

* For production support and other contribution related information refer <a target="_blank" href="https://siddhi.io/community/">Siddhi Community</a> documentation.
