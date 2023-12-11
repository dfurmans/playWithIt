# Play with it

#### Thanks for the time and your attention!

A simple microservice application based on https://www.playframework.com/getting-started
Scala seed template 
```
sbt new playframework/play-scala-seed.g8
```

### Project Structure
```
.
├── app             << Code here please
├── build.sbt
├── conf            << Routing and Microservice Configuration
├── instructions.md 
├── logs
├── project
├── public
├── target
└── test             << Unit test here please
```

## System requirements
* Java 17
* Scala 3.3.x

## Additional Libs
* cats-core - M[_] 
* caffeine  - in the memory storage API

## Build
```
sbt clean compile
```
## Run on 8085 HTTP Socket Port

```
sbt "run 8085"
```

## Tests

### Unit tests

Unit tests are executable by running the `test` sbt task.

## Microservice Endpoints
### Register a Shipment entry
```
POST    /api/register               controllers.ShipmentController.register
```
* **Example request** 
```
curl -X POST http://localhost:8085/api/register -d @test/data/shipment.json --header "Content-Type: application/json"
```
### Register a Tracking entry
```
PUT     /api/push                   controllers.TrackingController.tracking
```
* **Example request**
```
curl -X PUT http://localhost:8085/api/push -d @test/data/trackingA.json --header "Content-Type: application/json"
```

## _ => have fun with that
