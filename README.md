Distributed Noracle Backend
===================

[![Build Status](https://jenkins.dbis.rwth-aachen.de/buildStatus/icon?job=Distributed-Noracle-Backend)](https://jenkins.dbis.rwth-aachen.de/job/Distributed-Noracle-Backend/)

This suite of microservices forms the backend of the Distributed Noracle Project.
Please try out our app at: https://noracle.tech4comp.dbis.rwth-aachen.de/

---------------

## Java

las2peer uses **Java 14**.

## Run
```
docker build -t noracle-service .
docker run -p 8080:8080 -p 9011:9011 noracle-service
```
The services are then available under http://localhost:8080.

