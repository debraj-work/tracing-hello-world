# Hello Server

**A simple HTTP server that returns a "Hello, OpenTelemetry!" message with OpenTelemetry tracing**

## How to Run Application

**Start the application using any of the commands mentioned below**

> **Note:** These commands need to run inside the root folder of this project i.e inside the **hello-server** folder


- **Using maven** <br/>``` mvn exec:java -Dexec.mainClass="org.example.HelloServer"```


- **From jar file**
  Create a jar file using '**mvn clean install**' command and then execute
  <br/>```java -jar target/hello-server-1.0-SNAPSHOT.jar```


- **Directly from IDE**
  <br/>```Right click on HelloServer.java and click on 'Run' option```
  <br/><br/>

> **Note:** By default the server starts on port number 8080. If port 8080 is occupied in your system then you can change the port number by modifying the port in the HelloServer.java file.

<br/>

**Access the server using any of the two methods**

- **Browser or REST client**
  <br/>```http://localhost:8080/```


- **cURL**
  <br/>```curl --request GET 'http://localhost:8080/'```

## OpenTelemetry Integration

This application is instrumented with OpenTelemetry for distributed tracing. The `@WithSpan` annotation is used to create spans for the HTTP handler method.

When running with the OpenTelemetry Java agent, the application will automatically collect and export traces.

The application can be configured to run with the OpenTelemetry Java Agent like below

```aiignore
java -cp $PROJECT_ROOT/hello-server/target/hello-server-1.0-SNAPSHOT.jar \
  -javaagent:$PROJECT_ROOT/opentelemetry-javaagent-2.14.0.jar \
  -Dotel.service.name=hello-plain-java \
  -Dotel.exporter.otlp.endpoint=http://0.0.0.0:4318 \
  -Dotel.javaagent.debug=true \
  -Dotel.exporter.otlp.protocol=http/protobuf \
  -Dotel.metrics.exporter=none \
  org.example.HelloServer
```