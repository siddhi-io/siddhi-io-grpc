# API Docs - v1.0.0-SNAPSHOT

## Sink

### grpc *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#sink">(Sink)</a>*

<p style="word-wrap: break-word">This extension publishes event data encoded into GRPC Classes as defined in the user input jar. This extension has a default gRPC service classes added. The default service is called "EventService" and it has 2 rpc's. They are process and consume. Process sends a request of type Event and receives a response of the same type. Consume sends a request of type Event and expects no response from gRPC server. Please note that the Event type mentioned here is not io.siddhi.core.event.Event but org.wso2.grpc.Event. This grpc sink is used for scenarios where we send a request and don't expect a response back. I.e getting a google.protobuf.Empty response back.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
@sink(type="grpc", url="<STRING>", headers="<STRING>", idle.timeout="<LONG>", keep.alive.time="<LONG>", keep.alive.timeout="<LONG>", keep.alive.without.calls="<BOOL>", enable.retry="<BOOL>", max.retry.attempts="<INT>", max.hedged.attempts="<INT>", retry.buffer.size="<LONG>", per.rpc.buffer.size="<LONG>", channel.termination.waiting.time="<LONG>", @map(...)))
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
        <td style="vertical-align: top; word-wrap: break-word">The url to which the outgoing events should be published via this extension. This url should consist the host address, port, service name, method name in the following format. grpc://hostAddress:port/serviceName/methodName</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word">GRPC Request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>. If header parameter is not provided just the payload is sent</td>
        <td style="vertical-align: top">N/A</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word">Set the duration in seconds without ongoing RPCs before going to idle mode.</td>
        <td style="vertical-align: top">1800</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.time</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the time in seconds without read activity before sending a keepalive ping. Keepalives can increase the load on services so must be used with caution. By default set to Long.MAX_VALUE which disables keep alive pinging.</td>
        <td style="vertical-align: top">Long.MAX_VALUE</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the time in seconds waiting for read activity after sending a keepalive ping.</td>
        <td style="vertical-align: top">20</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.without.calls</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets whether keepalive will be performed when there are no outstanding RPC on a connection.</td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">enable.retry</td>
        <td style="vertical-align: top; word-wrap: break-word">Enables the retry and hedging mechanism provided by the gRPC library.</td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.retry.attempts</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets max number of retry attempts. The total number of retry attempts for each RPC will not exceed this number even if service config may allow a higher number.</td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.hedged.attempts</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets max number of hedged attempts. The total number of hedged attempts for each RPC will not exceed this number even if service config may allow a higher number.</td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">retry.buffer.size</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the retry buffer size in bytes. If the buffer limit is exceeded, no RPC could retry at the moment, and in hedging case all hedges but one of the same RPC will cancel.</td>
        <td style="vertical-align: top">16777216</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">per.rpc.buffer.size</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the per RPC buffer limit in bytes used for retry. The RPC is not retriable if its buffer limit is exceeded.</td>
        <td style="vertical-align: top">1048576</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">channel.termination.waiting.time</td>
        <td style="vertical-align: top; word-wrap: break-word">The time in seconds to wait for the channel to become terminated, giving up if the timeout is reached.</td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='grpc', url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume', @map(type='json')) define stream FooStream (message String);
```
<p style="word-wrap: break-word">Here a stream named FooStream is defined with grpc sink. A grpc server should be running at 194.23.98.100 listening to port 8080. sink.id is set to 1 here. So we can write a source with sink.id 1 so that it will listen to responses for requests published from this stream. Note that since we are using EventService/consume the sink will be operating in default mode</p>

<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@sink(type='grpc', url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume', headers='{{headers}}', @map(type='json'), @payload('{{message}}')) define stream FooStream (message String, headers String);
```
<p style="word-wrap: break-word">A similar example to above but with headers. Headers are also send into the stream as a data. In the sink headers dynamic property reads the value and sends it as MetaData with the request</p>

### grpc-call *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#sink">(Sink)</a>*

<p style="word-wrap: break-word">This extension publishes event data encoded into GRPC Classes as defined in the user input jar. This extension has a default gRPC service classes jar added. The default service is called "EventService" and it has 2 rpc's. They are process and consume. Process sends a request of type Event and receives a response of the same type. Consume sends a request of type Event and expects no response from gRPC server. Please note that the Event type mentioned here is not io.siddhi.core.event.Event but a type defined in the default service protobuf given in the readme. This grpc-call sink is used for scenarios where we send a request out and expect a response back. In default mode this will use EventService process method.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
@sink(type="grpc-call", url="<STRING>", headers="<STRING>", idle.timeout="<LONG>", keep.alive.time="<LONG>", keep.alive.timeout="<LONG>", keep.alive.without.calls="<BOOL>", enable.retry="<BOOL>", max.retry.attempts="<INT>", max.hedged.attempts="<INT>", retry.buffer.size="<LONG>", per.rpc.buffer.size="<LONG>", channel.termination.waiting.time="<LONG>", max.inbound.message.size="<LONG>", max.inbound.metadata.size="<LONG>", @map(...)))
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
        <td style="vertical-align: top; word-wrap: break-word">The url to which the outgoing events should be published via this extension. This url should consist the host address, port, service name, method name in the following format. grpc://hostAddress:port/serviceName/methodName</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word">GRPC Request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>. If header parameter is not provided just the payload is sent</td>
        <td style="vertical-align: top">N/A</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word">Set the duration in seconds without ongoing RPCs before going to idle mode.</td>
        <td style="vertical-align: top">1800</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.time</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the time in seconds without read activity before sending a keepalive ping. Keepalives can increase the load on services so must be used with caution. By default set to Long.MAX_VALUE which disables keep alive pinging.</td>
        <td style="vertical-align: top">Long.MAX_VALUE</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the time in seconds waiting for read activity after sending a keepalive ping.</td>
        <td style="vertical-align: top">20</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.without.calls</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets whether keepalive will be performed when there are no outstanding RPC on a connection.</td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">enable.retry</td>
        <td style="vertical-align: top; word-wrap: break-word">Enables the retry and hedging mechanism provided by the gRPC library.</td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.retry.attempts</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets max number of retry attempts. The total number of retry attempts for each RPC will not exceed this number even if service config may allow a higher number.</td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.hedged.attempts</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets max number of hedged attempts. The total number of hedged attempts for each RPC will not exceed this number even if service config may allow a higher number.</td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">retry.buffer.size</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the retry buffer size in bytes. If the buffer limit is exceeded, no RPC could retry at the moment, and in hedging case all hedges but one of the same RPC will cancel.</td>
        <td style="vertical-align: top">16777216</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">per.rpc.buffer.size</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the per RPC buffer limit in bytes used for retry. The RPC is not retriable if its buffer limit is exceeded.</td>
        <td style="vertical-align: top">1048576</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">channel.termination.waiting.time</td>
        <td style="vertical-align: top; word-wrap: break-word">The time in seconds to wait for the channel to become terminated, giving up if the timeout is reached.</td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.message.size</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the maximum message size allowed to be received on the channel in bytes</td>
        <td style="vertical-align: top">4194304</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.metadata.size</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the maximum size of metadata allowed to be received in bytes</td>
        <td style="vertical-align: top">8192</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='grpc-call', url = 'grpc://194.23.98.100:8080/EventService/process', sink.id= '1', @map(type='json')) define stream FooStream (message String);
```
<p style="word-wrap: break-word">Here a stream named FooStream is defined with grpc sink. A grpc server should be running at 194.23.98.100 listening to port 8080. sink.id is set to 1 here. So we can write a source with sink.id 1 so that it will listen to responses for requests published from this stream. Note that since we are using EventService/process the sink will be operating in default mode</p>

### grpc-service-response *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#sink">(Sink)</a>*

<p style="word-wrap: break-word">This extension is used to send responses back to a gRPC client after receiving requests through grpc-service source. This calls a callback in grpc-service source to put response back in StreamObserver.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
@sink(type="grpc-service-response", url="<STRING>", source.id="<INT>", @map(...)))
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
        <td style="vertical-align: top; word-wrap: break-word">The url to which the outgoing events should be published via this extension. This url should consist the host address, port, service name, method name in the following format. grpc://hostAddress:port/serviceName/methodName/sequenceName</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">source.id</td>
        <td style="vertical-align: top; word-wrap: break-word">A unique id to identify the correct source to which this sink is mapped. There is a 1:1 mapping between source and sink</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='grpc-service-response', url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume/mySequence', source.id='1'@map(type='json')) define stream FooStream (messageId String, message String);
```
<p style="word-wrap: break-word">This sink sends the responses for requests received via grpc-service source with the source.id 1</p>

## Source

### grpc *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#source">(Source)</a>*

<p style="word-wrap: break-word">This extension starts a grpc server during initialization time. The server listens to requests from grpc stubs. This source has a default mode of operation and custom user defined grpc service mode. In the default mode this source will use EventService consume method. This method will receive requests and injects them into stream through a mapper.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
@source(type="grpc", url="<STRING>", max.inbound.message.size="<INT>", max.inbound.metadata.size="<INT>", server.shutdown.waiting.time="<LONG>", @map(...)))
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
        <td style="vertical-align: top; word-wrap: break-word">The url which can be used by a client to access the grpc server in this extension. This url should consist the host address, port, service name, method name in the following format. grpc://hostAddress:port/serviceName/methodName</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.message.size</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the maximum message size in bytes allowed to be received on the server.</td>
        <td style="vertical-align: top">4194304</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.metadata.size</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the maximum size of metadata in bytes allowed to be received.</td>
        <td style="vertical-align: top">8192</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">server.shutdown.waiting.time</td>
        <td style="vertical-align: top; word-wrap: break-word">The time in seconds to wait for the server to shutdown, giving up if the timeout is reached.</td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='grpc', url='grpc://locanhost:8888/org.wso2.grpc.EventService/consume', @map(type='json')) define stream BarStream (message String);
```
<p style="word-wrap: break-word">Here the port is given as 8888. So a grpc server will be started on port 8888 and the server will expose EventService. This is the default service packed with the source. In EventService the consume method is</p>

<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@source(type='grpc', url='grpc://locanhost:8888/org.wso2.grpc.EventService/consume', @map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) define stream BarStream (message String, name String, age int);
```
<p style="word-wrap: break-word">Here we are getting headers sent with the request as transport properties and injecting them into the stream. With each request a header will be sent in MetaData in the following format: 'Name:John', 'Age:23'</p>

### grpc-call-response *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#source">(Source)</a>*

<p style="word-wrap: break-word">This grpc source receives responses received from gRPC server for requests sent from a gRPC sink. The source will receive responses for sink with the same sink.id. For example if you have a gRPC sink with sink.id 15 then we need to set the sink.id as 15 in the source to receives responses. Sinks and sources have 1:1 mapping. When using the source to listen to responses from Micro Integrator the optional parameter sequence should be given. Since the default Micro Integrator connection service can provide access to many different sequences this should be specified to separate and listen to only one sequence responses.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
@source(type="grpc-call-response", sink.id="<INT>", @map(...)))
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
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='grpc-call-response', sink.id= '1') define stream BarStream (message String);
```
<p style="word-wrap: break-word">Here we are listening to responses  for requests sent from the sink with sink.id 1 will be received here. The results will be injected into BarStream</p>

### grpc-service *<a target="_blank" href="http://siddhi.io/documentation/siddhi-5.x/query-guide-5.x/#source">(Source)</a>*

<p style="word-wrap: break-word">This extension implements a grpc server for receiving and responding to requests. During initialization time a grpc server is started on the user specified port exposing the required service as given in the url. This source also has a default mode and a user defined grpc service mode. In the default mode this will use the EventService process method. This accepts grpc message class Event as defined in the EventService proto. This uses GrpcServiceResponse sink to send reponses back in the same Event message format.</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
@source(type="grpc-service", url="<STRING>", max.inbound.message.size="<INT>", max.inbound.metadata.size="<INT>", service.timeout="<INT>", server.shutdown.waiting.time="<LONG>", @map(...)))
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
        <td style="vertical-align: top; word-wrap: break-word">The url which can be used by a client to access the grpc server in this extension. This url should consist the host address, port, service name, method name in the following format. grpc://hostAddress:port/serviceName/methodName</td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.message.size</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the maximum message size in bytes allowed to be received on the server.</td>
        <td style="vertical-align: top">4194304</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.metadata.size</td>
        <td style="vertical-align: top; word-wrap: break-word">Sets the maximum size of metadata in bytes allowed to be received.</td>
        <td style="vertical-align: top">8192</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">service.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word">The period of time in milliseconds to wait for siddhi to respond to a request received. After this time period of receiving a request it will be closed with an error message.</td>
        <td style="vertical-align: top">10000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">server.shutdown.waiting.time</td>
        <td style="vertical-align: top; word-wrap: break-word">The time in seconds to wait for the server to shutdown, giving up if the timeout is reached.</td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='grpc-service', url='grpc://localhost:8888/org.wso2.grpc.EventService/process', source.id='1', @map(type='json', @attributes(messageId='trp:messageId', message='message'))) define stream FooStream (messageId String, message String);
```
<p style="word-wrap: break-word">Here a grpc server will be started at port 8888. The process method of EventService will be exposed for clients. source.id is set as 1. So a grpc-service-response sink with source.id = 1 will send responses back for requests received to this source. Note that it is required to specify the transport property messageId since we need to correlate the request message with the response.</p>

<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@source(type='grpc-service', source.id='1' url='grpc://locanhost:8888/org.wso2.grpc.EventService/consume', @map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) define stream BarStream (message String, name String, age int);
```
<p style="word-wrap: break-word">Here we are getting headers sent with the request as transport properties and injecting them into the stream. With each request a header will be sent in MetaData in the following format: 'Name:John', 'Age:23'</p>

