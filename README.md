# Gatling Stress Testing

## Environment

Install Java8+:  

* [Java8](https://www.oracle.com/java/technologies/downloads/)  

Install Maven:  

* [Maven](https://maven.apache.org/download.cgi)  

## Configuration files  

* resources/application.conf - Edit endpoint files and authentication parameters
* resources/users.csv - Feed data to test with different information

## Test  

To test it out, simply execute the following command:

```bash
    $mvn gatling:test -Dgatling.simulationClass=microservice.PingUsersSimulation
    $mvn gatling:test -Dgatling.simulationClass=microservice.SaveUserSimulation
    $mvn gatling:test -Dgatling.simulationClass=microservice.LoginSimulation
```

or simply:

```bash
    $mvn gatling:test  
```  
