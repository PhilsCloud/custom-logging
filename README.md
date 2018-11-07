# Custom-logging Extension

This custom logging component is based on the MuleSoft Consulting minimal-logging component but includes additional features and operations. The component out of the box logs a standard set of properties, but also provides the capability for you to add your own properties or variables.

## Generate Log

Generate log should be placed at the beginning of your Mule application, generate log looks for headers client, transaction, and correlation ids.

### Client Id Enforcement

Client Id header format often varies, this component looks for client_id or x-client-id.

### Correlation Id

A Correlation Id is used to track a message through experience, process and system API's the full end to end process. A Correlation Id should be generated at the experience layer, but subsequent HTTP requests to process or system API's should pass the correlation Id as a header.

The component looks for x-correlation-id or x_correlation_id.

### Transaction Id

A transaction Id allows tracking of a message through the current mule application, and the component looks for x-transaction-id or x_transaction_id.

### Out of the box Generate Log entry:

```
{
    dateTimeStamp":"2018-11-06T22:35:20.447",
    "action":"Generated Log",
    "flowName":"api-main",
    "client_id":"123",
    "x-correlation-id":"2f25e5db-62b1-4ecb-984f-5370d733b66f",
    "x-transaction-id":"2ffwi22b-6h71-4e44-9098-53hdur87dgwf"
}
```

The log message can easily be extended, under the 'Generate Log' general settings area enable 'Custom Attributes' and edit inline, give the attribute a name (camelCase) and assign a variable, property, attribute or additional header:

```
<custom-logging:generate-log doc:name="Generate log" doc:id="c676329d-3d0d-4343-a471-833d0d1e2d1d" target="transactionProperties" headers="#[attributes.headers]">
    <custom-logging:custom-attributes >
        <custom-logging:custom-attribute key="applicationName" value="${api.name}" />
        <custom-logging:custom-attribute key="applicationVersion" value="${api.version}" />
        <custom-logging:custom-attribute key="relativePath" value="#[attributes.relativePath]" />
        <custom-logging:custom-attribute key="remoteAddress" value="#[attributes.remoteAddress]" />
        <custom-logging:custom-attribute key="host" value="#[attributes.localAddress]" />
        <custom-logging:custom-attribute key="method" value="#[attributes.method]" />
        <custom-logging:custom-attribute key="contentType" value="#[attributes.headers.'content-type']" />
    </custom-logging:custom-attributes>
</custom-logging:generate-log>

```
The above configuration outputs JSON like so:

```
{
   "dateTimeStamp":"2018-11-06T23:03:21.531",
   "action":"Generated Log",
   "flowName":"api-main",
   "client_id":"123",
   "x-correlation-id":"e378bc55-7686-48e2-89b1-6ab1ece80d15",
   "x-transaction-id":"2ffwi22b-6h71-4e44-9098-53hdur87dgwf",
   "applicationName":"test-application",
   "applicationVersion":"v1",
   "relativePath":"/api/v1/test-application/customers",
   "remoteAddress":"/127.0.0.1:60978",
  "host":"localhost/127.0.0.1:8081",
   "method":"GET",
   "contentType":"application/json"
}

```

When configuring the Generate Log feature, you must remember to set the target variable, the XML example above assigns the transaction variables to 'transactionProperties'.

## Execution time

Execution time allows you to capture in milliseconds the time elapsed from entering and exiting the component. Great for tracking the execution time of flows or even components such as database queries or HTTP requests.

To enable easy reading of logs 'Execution Description' enables you to enter a description for example 'Execute Database Query for Customer ID', transaction properties should also be set to the variable we created when using the generate log feature.

Example config:

```
<custom-logging:execution-time doc:name="Execution time" doc:id="32600e86-cce1-4ab2-9394-75ab4d5a9636" executionDescription="Routing Execution" transactionProperties="#[vars.transactionProperties]">
        <apikit:router config-ref="api-config" />
</custom-logging:execution-time>
```

Example log output when entering the execution:
```
{
  "dateTimeStamp":"2018-11-06T23:03:21.750",
  "action":"Entering Routing Execution",
  "flowName":"api-main",
  "client_id":"123",
  "x-correlation-id":"e378bc55-7686-48e2-89b1-6ab1ece80d15",
  "x-transaction-id":"2ffwi22b-6h71-4e44-9098-53hdur87dgwf"
}
```

Example log output when exiting the execution:
```
{
  "dateTimeStamp":"2018-11-06T23:03:21.750",
  "action":"Exiting Routing Execution",
  "flowName":"api-main",
  "client_id":"123",
  "x-correlation-id":"e378bc55-7686-48e2-89b1-6ab1ece80d15",
  "x-transaction-id":"2ffwi22b-6h71-4e44-9098-53hdur87dgwf",
  "elapsedMS":"280"
}
```

## Log

Sometimes you may want to capture a log entry with a specific set of attributes, for example when exiting a Mule Application you want to capture the httpStatus variable you set within your flow. This can be done by enabling new properties and editing inline, a message can also be included to allow you to read the logs easily, in this example 'Exiting Application':
```
<custom-logging:log doc:name="Log" doc:id="55f432c2-e62a-4a4f-96d8-eb6d40dabca5" msg="Exiting application" transactionProperties="#[vars.transactionProperties]">
        <custom-logging:new-properties>
            <custom-logging:new-property key="httpStatus" value="#[vars.httpStatus default 200]" />
        </custom-logging:new-properties>
</custom-logging:log>    
```

The example log response would be along the lines of:
```
{
  "dateTimeStamp":"2018-11-06T23:03:21.990",
  "Message":"Exiting application",
  "httpStatus":"200",
  "flowName":"api-main",
  "client_id":"123",
  "x-correlation-id":"67f36dfb-0a0d-4d21-937b-cae8a6785236",
  "x-transaction-id":"2ffwi22b-6h71-4e44-9098-53hdur87dgwf"
}
```

## Using this component

Download, run 'mvn clean install' then add this dependency to your application pom.xml:
 
```
<dependency>
  <groupId>com.tms</groupId>
	<artifactId>custom-logging</artifactId>
	<version>1.0.1</version>
  <classifier>mule-plugin</classifier>
</dependency>
```
