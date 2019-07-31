# API Docs - v1.0.0-SNAPSHOT

## Sink

### grpc *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#sink">(Sink)</a>*

<p style="word-wrap: break-word">This extension publishes event data encoded into GRPC Classes as defined in the user input jar. This extension has a default gRPC service classes jar added. The default service is called "EventService" and it has 2 rpc's. They are process and consume. Process sends a request of type Event and receives a response of the same type. Consume sends a request of type Event and expects no response from gRPC server. Please note that the Event type mentioned here is not io.siddhi.core.event.Event but a type defined in the default service protobuf given in the readme.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
@sink(type="grpc", url="<STRING>", sink.id="<INT>", sequence="<STRING>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">url</td>
        <td style="vertical-align: top; word-wrap: break-word">The url to which the outgoing events should be published via this extension. This url should consist the host address, port, service name, method name in the following format. hostAddress:port/serviceName/methodName</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">sink.id</td>
        <td style="vertical-align: top; word-wrap: break-word">a unique ID that should be set for each gRPC sink. There is a 1:1 mapping between gRPC sinks and sources. Each sink has one particular source listening to the responses to requests published from that sink. So the same sink.id should be given when writing the source also.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">sequence</td>
        <td style="vertical-align: top; word-wrap: break-word">This is an optional parameter to be used when connecting to Micro Integrator sequences from Siddhi. Micro integrator will expose a service called EventService which has 2 rpc's as mentioned in the extension description. Both of these rpc can access many different sequences in Micro Integrator. This parameter is used to specify the sequence which we want to use. When this parameter is given gRPC sink will comunicate with MI. Json map type should be used in this case to encode event data and send to MI</td>
        <td style="vertical-align: top">NA. When sequence is not given the service name and method name should be specified in the url</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='grpc', url = '194.23.98.100:8080/EventService/process', sequence = 'mySeq', sink.id= '1', @map(type='json')) define stream FooStream (message String);
```
<p style="word-wrap: break-word">Here a stream named FooStream is defined with grpc sink. Since sequence is specified here sink will be in default mode. i.e communicating to MI. The MicroIntegrator should be running at 194.23.98.100 host and listening on port 8080. The sequence called mySeq will be accessed. sink.id is set to 1 here. So we can write a source with sink.id 1 so that it will listen to responses for requests published from this stream.</p>

## Source

### grpc *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#source">(Source)</a>*

<p style="word-wrap: break-word">This grpc source receives responses received from gRPC server for requests sent from a gRPC sink. The source will receive responses for sink with the same sink.id. For example if you have a gRPC sink with sink.id 15 then we need to set the sink.id as 15 in the source to receives responses. Sinks and sources have 1:1 mapping. When using the source to listen to responses from Micro Integrator the optional parameter sequence should be given. Since the default Micro Integrator connection service can provide access to many different sequences this should be specified to separate and listen to only one sequence responses.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
@source(type="grpc", sink.id="<INT>", sequence="<STRING>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">sink.id</td>
        <td style="vertical-align: top; word-wrap: break-word">a unique ID that should be set for each gRPC source. There is a 1:1 mapping between gRPC sinks and sources. Each sink has one particular source listening to the responses to requests published from that sink. So the same sink.id should be given when writing the sink also.</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">sequence</td>
        <td style="vertical-align: top; word-wrap: break-word">This is an optional parameter to be used when connecting to Micro Integrator sequences from Siddhi. Micro integrator will expose a service called EventService which has 2 rpc's as mentioned in the extension description. Both of these rpc can access many different sequences in Micro Integrator. This parameter is used to specify the sequence which we want to use. When this parameter is given gRPC source will listen to MI.</td>
        <td style="vertical-align: top">NA. When sequence is not given the service name and method name should be specified in the url</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='grpc', sequence='mySeq', sink.id= '1') define stream BarStream (message String);
```
<p style="word-wrap: break-word">Here we are listening to responses from a sequence called mySeq. In addition, only the responses for requests sent from the sink with sink.id 1 will be received here. The results will be injected into BarStream</p>

