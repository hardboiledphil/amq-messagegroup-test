# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/maven-plugin/)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/htmlsingle/#boot-features-jpa-and-spring-data)
* [Spring for Apache ActiveMQ Artemis](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/htmlsingle/#boot-features-artemis)
* [JDBC API](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/reference/htmlsingle/#boot-features-sql)

### Guides
The following guides illustrate how to use some features concretely:

* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Messaging with JMS](https://spring.io/guides/gs/messaging-jms/)
* [Accessing Relational Data using JDBC with Spring](https://spring.io/guides/gs/relational-data-access/)
* [Managing Transactions](https://spring.io/guides/gs/managing-transactions/)


### To send a message to the application
curl -d'{ "ID" : "2", "MESSAGE" : "Hello world2" }' -H"content-type: application/json" http://localhost:8080\?rollback\=false

### To shut down the application
curl -X POST localhost:8080/actuator/shutdown

### To spin up the application
 ./mvnw spring-boot:run -s /c/dev/azurerepo/mot/eodos/configuration/settings.xml