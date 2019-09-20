# API Docs - v1.0.0-beta

!!! Info "Tested Siddhi Core version: *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/">5.1.3</a>*"
    It could also support other Siddhi Core minor versions.

## Sink

### grpc *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">(Sink)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">This extension publishes event data encoded into GRPC Classes as defined in the user input jar. This extension has a default gRPC service classes added. The default service is called "EventService". Please find the protobuf definition [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/EventService.proto). This grpc sink is used for scenarios where we send a request and don't expect a response back. I.e getting a google.protobuf.Empty response back.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@sink(type="grpc", publisher.url="<STRING>", headers="<STRING>", idle.timeout="<LONG>", keep.alive.time="<LONG>", keep.alive.timeout="<LONG>", keep.alive.without.calls="<BOOL>", enable.retry="<BOOL>", max.retry.attempts="<INT>", retry.buffer.size="<LONG>", per.rpc.buffer.size="<LONG>", channel.termination.waiting.time="<LONG>", truststore.file="<STRING>", truststore.password="<STRING>", truststore.algorithm="<STRING>", tls.store.type="<STRING>", keystore.file="<STRING>", keystore.password="<STRING>", keystore.algorithm="<STRING>", @map(...)))
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
        <td style="vertical-align: top">publisher.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The url to which the outgoing events should be published via this extension. This url should consist the host address, port, service name, method name in the following format. <code>grpc://0.0.0.0:9763/&lt;serviceName&gt;/&lt;methodName&gt;</code></p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">GRPC Request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>. If header parameter is not provided just the payload is sent</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Set the duration in seconds without ongoing RPCs before going to idle mode.</p></td>
        <td style="vertical-align: top">1800</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the time in seconds without read activity before sending a keepalive ping. Keepalives can increase the load on services so must be used with caution. By default set to Long.MAX_VALUE which disables keep alive pinging.</p></td>
        <td style="vertical-align: top">Long.MAX_VALUE</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the time in seconds waiting for read activity after sending a keepalive ping.</p></td>
        <td style="vertical-align: top">20</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.without.calls</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets whether keepalive will be performed when there are no outstanding RPC on a connection.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">enable.retry</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enables the retry mechanism provided by the gRPC library.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.retry.attempts</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets max number of retry attempts. The total number of retry attempts for each RPC will not exceed this number even if service config may allow a higher number.</p></td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">retry.buffer.size</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the retry buffer size in bytes. If the buffer limit is exceeded, no RPC could retry at the moment, and in hedging case all hedges but one of the same RPC will cancel.</p></td>
        <td style="vertical-align: top">16777216</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">per.rpc.buffer.size</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the per RPC buffer limit in bytes used for retry. The RPC is not retriable if its buffer limit is exceeded.</p></td>
        <td style="vertical-align: top">1048576</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">channel.termination.waiting.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The time in seconds to wait for the channel to become terminated, giving up if the timeout is reached.</p></td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the file path of truststore. If this is provided then server authentication is enabled</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the password of truststore. If this is provided then the integrity of the keystore is checked</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.algorithm</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the encryption algorithm to be used for server authentication</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tls.store.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">TLS store type</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the file path of keystore. If this is provided then client authentication is enabled</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the password of keystore</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.algorithm</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the encryption algorithm to be used for client authentication</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='grpc',
      publisher.url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume',
      @map(type='json'))
define stream FooStream (message String);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here a stream named FooStream is defined with grpc sink. A grpc server should be running at 194.23.98.100 listening to port 8080. sink.id is set to 1 here. So we can write a source with sink.id 1 so that it will listen to responses for requests published from this stream. Note that since we are using EventService/consume the sink will be operating in default mode</p>
<p></p>
<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@sink(type='grpc',
      publisher.url = 'grpc://134.23.43.35:8080/org.wso2.grpc.EventService/consume',
      headers='{{headers}}',
      @map(type='json'),
           @payload('{{message}}'))
define stream FooStream (message String, headers String);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">A similar example to above but with headers. Headers are also send into the stream as a data. In the sink headers dynamic property reads the value and sends it as MetaData with the request</p>
<p></p>
<span id="example-3" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 3</span>
```
@sink(type='grpc',
      publisher.url = 'grpc://134.23.43.35:8080/org.wso2.grpc.test.MyService/send',
      @map(type='protobuf'),
define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool,floatValue float,doubleValue double);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here a stream named FooStream is defined with grpc sink.A grpc servershould be running at 194.23.98.100 listening to port 8080, since there is no mapper provided, attributes of stream definition should be same as the attributes of protobuf message definition</p>
<p></p>
### grpc-call *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">(Sink)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">This extension publishes event data encoded into GRPC Classes as defined in the user input jar. This extension has a default gRPC service classes jar added. The default service is called "EventService". Please find the protobuf definition [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/EventService.proto). This grpc-call sink is used for scenarios where we send a request out and expect a response back. In default mode this will use EventService process method. grpc-call-response source is used to receive the responses. A unique sink.id is used to correlate between the sink and its corresponding source.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@sink(type="grpc-call", publisher.url="<STRING>", sink.id="<INT>", headers="<STRING>", idle.timeout="<LONG>", keep.alive.time="<LONG>", keep.alive.timeout="<LONG>", keep.alive.without.calls="<BOOL>", enable.retry="<BOOL>", max.retry.attempts="<INT>", retry.buffer.size="<LONG>", per.rpc.buffer.size="<LONG>", channel.termination.waiting.time="<LONG>", max.inbound.message.size="<LONG>", max.inbound.metadata.size="<LONG>", truststore.file="<STRING>", truststore.password="<STRING>", truststore.algorithm="<STRING>", tls.store.type="<STRING>", keystore.file="<STRING>", keystore.password="<STRING>", keystore.algorithm="<STRING>", @map(...)))
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
        <td style="vertical-align: top">publisher.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The url to which the outgoing events should be published via this extension. This url should consist the host address, port, service name, method name in the following format. <code>grpc://0.0.0.0:9763/&lt;serviceName&gt;/&lt;methodName&gt;</code></p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">sink.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">a unique ID that should be set for each grpc-call-sink. There is a 1:1 mapping between grpc-call sinks and grpc-call-response sources. Each sink has one particular source listening to the responses to requests published from that sink. So the same sink.id should be given when writing the source also.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">headers</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">GRPC Request headers in format <code>"'&lt;key&gt;:&lt;value&gt;','&lt;key&gt;:&lt;value&gt;'"</code>. If header parameter is not provided just the payload is sent</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">idle.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Set the duration in seconds without ongoing RPCs before going to idle mode.</p></td>
        <td style="vertical-align: top">1800</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the time in seconds without read activity before sending a keepalive ping. Keepalives can increase the load on services so must be used with caution. By default set to Long.MAX_VALUE which disables keep alive pinging.</p></td>
        <td style="vertical-align: top">Long.MAX_VALUE</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the time in seconds waiting for read activity after sending a keepalive ping.</p></td>
        <td style="vertical-align: top">20</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keep.alive.without.calls</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets whether keepalive will be performed when there are no outstanding RPC on a connection.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">enable.retry</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Enables the retry and hedging mechanism provided by the gRPC library.</p></td>
        <td style="vertical-align: top">false</td>
        <td style="vertical-align: top">BOOL</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.retry.attempts</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets max number of retry attempts. The total number of retry attempts for each RPC will not exceed this number even if service config may allow a higher number.</p></td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">retry.buffer.size</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the retry buffer size in bytes. If the buffer limit is exceeded, no RPC could retry at the moment, and in hedging case all hedges but one of the same RPC will cancel.</p></td>
        <td style="vertical-align: top">16777216</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">per.rpc.buffer.size</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the per RPC buffer limit in bytes used for retry. The RPC is not retriable if its buffer limit is exceeded.</p></td>
        <td style="vertical-align: top">1048576</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">channel.termination.waiting.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The time in seconds to wait for the channel to become terminated, giving up if the timeout is reached.</p></td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.message.size</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the maximum message size allowed to be received on the channel in bytes</p></td>
        <td style="vertical-align: top">4194304</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.metadata.size</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the maximum size of metadata allowed to be received in bytes</p></td>
        <td style="vertical-align: top">8192</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the file path of truststore. If this is provided then server authentication is enabled</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the password of truststore. If this is provided then the integrity of the keystore is checked</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.algorithm</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the encryption algorithm to be used for server authentication</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tls.store.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">TLS store type</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the file path of keystore. If this is provided then client authentication is enabled</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the password of keystore</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.algorithm</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the encryption algorithm to be used for client authentication</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='grpc-call',
      publisher.url = 'grpc://194.23.98.100:8080/EventService/process',
      sink.id= '1', @map(type='json'))
define stream FooStream (message String);
@source(type='grpc-call-response', sink.id= '1')
define stream BarStream (message String);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here a stream named FooStream is defined with grpc sink. A grpc server should be running at 194.23.98.100 listening to port 8080. sink.id is set to 1 here. So we can write a source with sink.id 1 so that it will listen to responses for requests published from this stream. Note that since we are using EventService/process the sink will be operating in default mode</p>
<p></p>
<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@sink(type='grpc-call',
      publisher.url = 'grpc://194.23.98.100:8080/EventService/process',
      sink.id= '1', @map(type='json'))
define stream FooStream (message String);

@source(type='grpc-call-response', sink.id= '1')
define stream BarStream (message String);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here with the same FooStream definition we have added a BarStream which has a grpc-call-response source with the same sink.id 1. So the responses for calls sent from the FooStream will be added to BarStream.</p>
<p></p>
<span id="example-3" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 3</span>
```
@sink(type='grpc-call',
      publisher.url = 'grpc://194.23.98.100:8888/org.wso2.grpc.test.MyService/process',
      sink.id= '1', @map(type='protobuf'))
define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool,floatValue float,doubleValue double);

@source(type='grpc-call-response', sink.id= '1')
define stream FooStream (stringValue string, intValue int,longValue long,booleanValue bool,floatValue float,doubleValue double);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here with the same FooStream definition we have added a BarStream which has a grpc-call-response source with the same sink.id 1. So the responses for calls sent from the FooStream will be added to BarStream. since there is no mapping available stream definition attributes names should be as same as the attributes of the protobuf message definition</p>
<p></p>
### grpc-service-response *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#sink">(Sink)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">This extension is used to send responses back to a gRPC client after receiving requests through grpc-service source. This correlates with the particular source using a unique source.id</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@sink(type="grpc-service-response", source.id="<INT>", @map(...)))
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
        <td style="vertical-align: top">source.id</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">A unique id to identify the correct source to which this sink is mapped. There is a 1:1 mapping between source and sink</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@sink(type='grpc-service-response',
      source.id='1',
      @map(type='json'))
define stream BarStream (messageId String, message String);

@source(type='grpc-service',
        url='grpc://134.23.43.35:8080/org.wso2.grpc.EventService/process',
        source.id='1',
        @map(type='json',
             @attributes(messageId='trp:messageId', message='message')))
define stream FooStream (messageId String, message String);
from FooStream
select * 
insert into BarStream;

```
<p></p>
<p style="word-wrap: break-word;margin: 0;">The grpc requests are received through the grpc-service sink. Each received event is sent back through grpc-service-source. This is just a passthrough through Siddhi as we are selecting everything from FooStream and inserting into BarStream.</p>
<p></p>
## Source

### grpc *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">(Source)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">This extension starts a grpc server during initialization time. The server listens to requests from grpc stubs. This source has a default mode of operation and custom user defined grpc service mode. By default this uses EventService. Please find the proto definition [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/EventService.proto) In the default mode this source will use EventService consume method. This method will receive requests and injects them into stream through a mapper.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@source(type="grpc", receiver.url="<STRING>", max.inbound.message.size="<INT>", max.inbound.metadata.size="<INT>", server.shutdown.waiting.time="<LONG>", truststore.file="<STRING>", truststore.password="<STRING>", truststore.algorithm="<STRING>", tls.store.type="<STRING>", keystore.file="<STRING>", keystore.password="<STRING>", keystore.algorithm="<STRING>", @map(...)))
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
        <td style="vertical-align: top">receiver.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The url which can be used by a client to access the grpc server in this extension. This url should consist the host address, port, service name, method name in the following format. <code>grpc://0.0.0.0:9763/&lt;serviceName&gt;/&lt;methodName&gt;</code></p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.message.size</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the maximum message size in bytes allowed to be received on the server.</p></td>
        <td style="vertical-align: top">4194304</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.metadata.size</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the maximum size of metadata in bytes allowed to be received.</p></td>
        <td style="vertical-align: top">8192</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">server.shutdown.waiting.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The time in seconds to wait for the server to shutdown, giving up if the timeout is reached.</p></td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the file path of truststore. If this is provided then server authentication is enabled</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the password of truststore. If this is provided then the integrity of the keystore is checked</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.algorithm</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the encryption algorithm to be used for server authentication</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tls.store.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">TLS store type</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the file path of keystore. If this is provided then client authentication is enabled</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the password of keystore</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.algorithm</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the encryption algorithm to be used for client authentication</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='grpc',
       receiver.url='grpc://194.23.98.100:8888/org.wso2.grpc.EventService/consume',
       @map(type='json'))
define stream BarStream (message String);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here the port is given as 8888. So a grpc server will be started on port 8888 and the server will expose EventService. This is the default service packed with the source. In EventService the consume method is</p>
<p></p>
<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@source(type='grpc',
       receiver.url='grpc://194.23.98.100:8888/org.wso2.grpc.EventService/consume',
       @map(type='json', @attributes(name='trp:name', age='trp:age', message='message')))
define stream BarStream (message String, name String, age int);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here we are getting headers sent with the request as transport properties and injecting them into the stream. With each request a header will be sent in MetaData in the following format: 'Name:John', 'Age:23'</p>
<p></p>
<span id="example-3" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 3</span>
```
@source(type='grpc',
       receiver.url='grpc://194.23.98.100:8888/org.wso2.grpc.test.MyService/send',
       @map(type='protobuf', ))
define stream BarStream (stringValue string, intValue int,longValue long,booleanValue bool,floatValue float,doubleValue double);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here the port is give as 8888. So a grpc server will be started on port 8888 and sever will be keep listening to the 'send method in the 'MyService' service.</p>
<p></p>
### grpc-call-response *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">(Source)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">This grpc source receives responses received from gRPC server for requests sent from a grpc-call sink. The source will receive responses for sink with the same sink.id. For example if you have a gRPC sink with sink.id 15 then we need to set the sink.id as 15 in the source to receives responses. Sinks and sources have 1:1 mapping</p>
<p></p>
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
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">a unique ID that should be set for each grpc-call source. There is a 1:1 mapping between grpc-call sinks and grpc-call-response sources. Each sink has one particular source listening to the responses to requests published from that sink. So the same sink.id should be given when writing the sink also.</p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='grpc-call-response', sink.id= '1')
define stream BarStream (message String);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here we are listening to responses  for requests sent from the sink with sink.id 1 will be received here. The results will be injected into BarStream</p>
<p></p>
### grpc-service *<a target="_blank" href="http://siddhi.io/en/v5.1/docs/query-guide/#source">(Source)</a>*
<p></p>
<p style="word-wrap: break-word;margin: 0;">This extension implements a grpc server for receiving and responding to requests. During initialization time a grpc server is started on the user specified port exposing the required service as given in the url. This source also has a default mode and a user defined grpc service mode. By default this uses EventService. Please find the proto definition [here](https://github.com/siddhi-io/siddhi-io-grpc/tree/master/component/src/main/resources/EventService.proto) In the default mode this will use the EventService process method. This accepts grpc message class Event as defined in the EventService proto. This uses GrpcServiceResponse sink to send reponses back in the same Event message format.</p>
<p></p>
<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>

```
@source(type="grpc-service", receiver.url="<STRING>", max.inbound.message.size="<INT>", max.inbound.metadata.size="<INT>", service.timeout="<INT>", server.shutdown.waiting.time="<LONG>", truststore.file="<STRING>", truststore.password="<STRING>", truststore.algorithm="<STRING>", tls.store.type="<STRING>", keystore.file="<STRING>", keystore.password="<STRING>", keystore.algorithm="<STRING>", @map(...)))
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
        <td style="vertical-align: top">receiver.url</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The url which can be used by a client to access the grpc server in this extension. This url should consist the host address, port, service name, method name in the following format. <code>grpc://0.0.0.0:9763/&lt;serviceName&gt;/&lt;methodName&gt;</code></p></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.message.size</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the maximum message size in bytes allowed to be received on the server.</p></td>
        <td style="vertical-align: top">4194304</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">max.inbound.metadata.size</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">Sets the maximum size of metadata in bytes allowed to be received.</p></td>
        <td style="vertical-align: top">8192</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">service.timeout</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The period of time in milliseconds to wait for siddhi to respond to a request received. After this time period of receiving a request it will be closed with an error message.</p></td>
        <td style="vertical-align: top">10000</td>
        <td style="vertical-align: top">INT</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">server.shutdown.waiting.time</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">The time in seconds to wait for the server to shutdown, giving up if the timeout is reached.</p></td>
        <td style="vertical-align: top">5</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the file path of truststore. If this is provided then server authentication is enabled</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the password of truststore. If this is provided then the integrity of the keystore is checked</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">truststore.algorithm</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the encryption algorithm to be used for server authentication</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">tls.store.type</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">TLS store type</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.file</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the file path of keystore. If this is provided then client authentication is enabled</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.password</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the password of keystore</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">keystore.algorithm</td>
        <td style="vertical-align: top; word-wrap: break-word"><p style="word-wrap: break-word;margin: 0;">the encryption algorithm to be used for client authentication</p></td>
        <td style="vertical-align: top">-</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='grpc-service',
       receiver.url='grpc://localhost:8888/org.wso2.grpc.EventService/process',
       source.id='1',
       @map(type='json', @attributes(messageId='trp:messageId', message='message')))
define stream FooStream (messageId String, message String);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here a grpc server will be started at port 8888. The process method of EventService will be exposed for clients. source.id is set as 1. So a grpc-service-response sink with source.id = 1 will send responses back for requests received to this source. Note that it is required to specify the transport property messageId since we need to correlate the request message with the response.</p>
<p></p>
<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@sink(type='grpc-service-response',
      source.id='1',
      @map(type='json'))
define stream BarStream (messageId String, message String);

@source(type='grpc-service',
       receiver.url='grpc://134.23.43.35:8080/org.wso2.grpc.EventService/process',
       source.id='1',
       @map(type='json', @attributes(messageId='trp:messageId', message='message')))
define stream FooStream (messageId String, message String);

from FooStream
select * 
insert into BarStream;
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">The grpc requests are received through the grpc-service sink. Each received event is sent back through grpc-service-source. This is just a passthrough through Siddhi as we are selecting everything from FooStream and inserting into BarStream.</p>
<p></p>
<span id="example-3" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 3</span>
```
@source(type='grpc-service', source.id='1'        receiver.url='grpc://locanhost:8888/org.wso2.grpc.EventService/consume',        @map(type='json', @attributes(name='trp:name', age='trp:age', message='message'))) define stream BarStream (message String, name String, age int);
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here we are getting headers sent with the request as transport properties and injecting them into the stream. With each request a header will be sent in MetaData in the following format: 'Name:John', 'Age:23'</p>
<p></p>
<span id="example-4" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 4</span>
```
@sink(type='grpc-service-response',
      source.id='1',
      message.id='{{messageId}}',
      @map(type='protobuf',
@payload(stringValue='a',intValue='b',longValue='c',booleanValue='d',floatValue = 'e', doubleValue ='f')))
define stream BarStream (a string,messageId string, b int,c long,d bool,e float,f double);

@source(type='grpc-service',
       receiver.url='grpc://134.23.43.35:8888/org.wso2.grpc.test.MyService/process',
       source.id='1',
       @map(type='protobuf', @attributes(messageId='trp:message.id', a = 'stringValue', b = 'intValue', c = 'longValue',d = 'booleanValue', e = 'floatValue', f ='doubleValue')))
define stream FooStream (a string,messageId string, b int,c long,d bool,e float,f double);

from FooStream
select * 
insert into BarStream;
```
<p></p>
<p style="word-wrap: break-word;margin: 0;">Here a grpc server will be started at port 8888. The process method of the MyService will be exposed to the clients.source.id is set as 1.So a grpc-service-response sink with source.id = 1 will send responses back for requests received to this source. Note that it is required to specify the transport property messageId since we need to correlate the request message with the response.</p>
<p></p>
