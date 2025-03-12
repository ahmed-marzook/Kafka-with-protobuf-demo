# Protocol Buffer Kafka Demo

This application demonstrates how to use Protocol Buffers (protobuf) with Kafka in a Spring Boot application. It provides a simple API for creating and sending structured data that is serialized using Protocol Buffers.

## Features

- Spring Boot REST API for sending messages
- Custom Protocol Buffer serializer/deserializer for Kafka
- Kafka producer and consumer implementation
- Protocol Buffer schema for an `AddressBook` with `Person` records

## Prerequisites

- Java 21 or higher
- Gradle
- Docker and Docker Compose (for running Kafka infrastructure)

## Getting Started

### 1. Clone the repository

```bash
git clone gh repo clone ahmed-marzook/Kafka-with-protobuf-demo
cd Kafka-with-protobuf-demo
```

### 2. Start Kafka infrastructure

The project includes a Docker Compose file to run Kafka, Zookeeper, and Kafka UI:

```bash
docker-compose up -d
```

This will start:

- Zookeeper on port 2181
- Kafka broker on ports 9092 (internal) and 29092 (external)
- Kafka UI on port 8081 (accessible at http://localhost:8081)

### 3. Generate Protocol Buffer classes

You can generate the Protocol Buffer Java classes manually using:

```bash
protoc --java_out=./src/main/java src/main/resources/proto/addressbook.proto
```

Or let Gradle handle it during the build process.

### 4. Build the application

```bash
./gradlew clean build
```

### 5. Run the application

```bash
./gradlew bootRun
```

Or run the JAR file:

```bash
java -jar build/libs/kafka-protobuf-demo-0.0.1-SNAPSHOT.jar
```

## Usage

### Send an AddressBook message

Send a POST request to `http://localhost:8080/api/v1/message` with a JSON body:

```bash
curl -X POST http://localhost:8080/api/v1/message \
  -H "Content-Type: application/json" \
  -d '{
  "people": [
    {
      "name": "John Doe",
      "id": 1,
      "email": "john.doe@example.com",
      "phones": [
        {
          "number": "555-1234",
          "type": "MOBILE"
        },
        {
          "number": "555-5678",
          "type": "HOME"
        }
      ]
    },
    {
      "name": "Jane Smith",
      "id": 2,
      "email": "jane.smith@example.com",
      "phones": [
        {
          "number": "555-4321",
          "type": "WORK"
        }
      ]
    }
  ]
}'
```

### Verify the message was received

The application includes a Kafka listener that will log received messages. Check the application logs to see the message being processed.

## Configuration

Key application properties (in `src/main/resources/application.properties`):

```properties
# Kafka
spring.kafka.bootstrap-servers=localhost:29092
kafka.topic.addressbook=address-book-topic

# Server
server.port=8080
```

Note: The Kafka bootstrap server is set to port 29092 which is the externally accessible port configured in the Docker Compose file.

## Project Structure

- **src/main/proto/**: Contains Protocol Buffer definition files
- **src/main/java/com/kfaka/demo/**: Main application code
  - **config/**: Kafka and app configuration
  - **controller/**: REST API endpoints
  - **protos/**: Generated Protocol Buffer classes
  - **serializer/**: Custom Kafka serializers/deserializers
  - **listener/**: Kafka consumers/listeners

## How It Works

1. The application provides a REST API to accept JSON data representing an address book with people.
2. The controller converts this JSON to a Protocol Buffer message.
3. The message is serialized using the custom `SimpleProtobufSerializer` and sent to a Kafka topic.
4. The Kafka listener receives the message, deserializes it with `SimpleProtobufDeserializer`, and processes it.

## Protocol Buffer Schema

The application uses the following Protocol Buffer schema:

```protobuf
syntax = "proto3";
package tutorial;

option java_package = "com.kfaka.demo.protos";
option java_multiple_files = true;
option java_outer_classname = "AddressBookProtos";

message Person {
  string name = 1;
  int32 id = 2;
  string email = 3;

  enum PhoneType {
    PHONE_TYPE_UNSPECIFIED = 0;
    PHONE_TYPE_MOBILE = 1;
    PHONE_TYPE_HOME = 2;
    PHONE_TYPE_WORK = 3;
  }

  message PhoneNumber {
    string number = 1;
    PhoneType type = 2;
  }

  repeated PhoneNumber phones = 4;
}

message AddressBook {
  repeated Person people = 1;
}
```

## Additional Notes

- The application implements custom Protocol Buffer serializers to avoid the complexity and dependencies of the Confluent Schema Registry.
- This approach is simpler but doesn't provide schema evolution/validation features that the Schema Registry offers.

## Kafka Infrastructure

The included Docker Compose file provides a complete Kafka setup:

```yaml
version: "3"

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    networks:
      - kafka-network
    volumes:
      - zookeeper-data:/var/lib/zookeeper/data
      - zookeeper-logs:/var/lib/zookeeper/log

  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
      - "29092:29092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    networks:
      - kafka-network
    volumes:
      - kafka-data:/var/lib/kafka/data

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    depends_on:
      - kafka
    ports:
      - "8081:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181
    networks:
      - kafka-network

networks:
  kafka-network:
    driver: bridge

volumes:
  zookeeper-data:
  zookeeper-logs:
  kafka-data:
```

This setup provides:

- A Zookeeper instance for Kafka coordination
- A single-node Kafka broker
- A Kafka UI for monitoring and managing your Kafka cluster
- Persistent volumes for data storage

You can access the Kafka UI at http://localhost:8081 to view topics, messages, and monitor your Kafka cluster.
