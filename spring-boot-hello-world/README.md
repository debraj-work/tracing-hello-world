# Spring Boot Hello World

**A simple Spring Boot 2.x app to send hello world message to a user**

## How to Run Application

**Start the application using any of the commands mentioned below**

> **Note:** First two commands need to run inside the root folder of this project i.e inside the **spring-boot-hello-world** folder


- **Using maven** <br/>``` mvn spring-boot:run```


- **From jar file**
  Create a jar file using '**mvn clean install**' command and then execute
  <br/>```java -jar target/spring-boot-2-hello-world-1.0.2-SNAPSHOT.jar```


- **Directly from IDE**
  <br/>```Right click on HelloWorldApplication.java and click on 'Run' option```
  <br/><br/>

> **Note:** By default spring boot application starts on port number 8080. If port 8080 is occupied in your system then you can change the port number by uncommenting and updating the **server.port** property inside the **application.properties** file that is available inside the **src > main > resources** folder.

<br/>

**Send an HTTP GET request to '/hello' endpoint using any of the two methods**

- **Browser or REST client**
  <br/>```http://localhost:10002/hello```


- **cURL**
  <br/>```curl --request GET 'http://localhost:10002/hello'```

## Spring Boot Actuator Endpoints

This application includes Spring Boot Actuator which provides production-ready features to help monitor and manage the application.

### Health Endpoint

The health endpoint provides information about the application's health status.

- **Browser or REST client**
  <br/>```http://localhost:10002/actuator/health```

- **cURL**
  <br/>```curl --request GET 'http://localhost:10002/actuator/health'```

The health endpoint will return detailed health information as it's configured with `management.endpoint.health.show-details=always`.

### Metrics Endpoint

The metrics endpoint provides metrics information about the application.

- **Browser or REST client**
  <br/>```http://localhost:10002/actuator/metrics```

- **cURL**
  <br/>```curl --request GET 'http://localhost:10002/actuator/metrics'```

To view specific metrics, append the metric name to the URL:
  <br/>```http://localhost:10002/actuator/metrics/{metric.name}```

For example:
  <br/>```http://localhost:10002/actuator/metrics/jvm.memory.used```
