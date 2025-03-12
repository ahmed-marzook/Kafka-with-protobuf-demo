package com.kfaka.demo.controller;

import com.kfaka.demo.protos.AddressBook;
import com.kfaka.demo.protos.Person;
import com.kfaka.demo.records.AddressBookRequest;
import com.kfaka.demo.records.PersonRequest;
import com.kfaka.demo.records.PhoneRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/message")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final KafkaTemplate<String, AddressBook> kafkaTemplate;

    @Value("${kafka.topic.addressbook}")
    private String topicName;

    @PostMapping
    public ResponseEntity<String> publishAddressBook(@RequestBody AddressBookRequest request) {
        try {
            // Build Protobuf AddressBook from the request
            AddressBook.Builder addressBookBuilder = AddressBook.newBuilder();

            // Convert each person in the request to a Protobuf Person
            for (PersonRequest personRequest : request.people()) {
                Person.Builder personBuilder = Person.newBuilder()
                        .setName(personRequest.name())
                        .setId(personRequest.id())
                        .setEmail(personRequest.email());

                // Add phone numbers if present
                if (personRequest.phones() != null) {
                    for (PhoneRequest phoneRequest : personRequest.phones()) {
                        Person.PhoneType phoneType = convertPhoneType(phoneRequest.type());

                        Person.PhoneNumber phoneNumber = Person.PhoneNumber.newBuilder()
                                .setNumber(phoneRequest.number())
                                .setType(phoneType)
                                .build();

                        personBuilder.addPhones(phoneNumber);
                    }
                }

                addressBookBuilder.addPeople(personBuilder.build());
            }

            // Build the final AddressBook
            AddressBook addressBook = addressBookBuilder.build();

            // Generate a key and send the message
            String key = UUID.randomUUID().toString();
            log.info("Sending AddressBook to topic {}, key: {}, people: {}",
                    topicName, key, addressBook.getPeopleCount());

            kafkaTemplate.send(topicName, key, addressBook);

            return ResponseEntity.ok("AddressBook sent successfully with " +
                    addressBook.getPeopleCount() + " people");
        } catch (Exception e) {
            log.error("Error sending AddressBook", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send message: " + e.getMessage());
        }
    }

    // Helper method to convert string phone type to Protocol Buffer enum
    private Person.PhoneType convertPhoneType(String type) {
        if (type == null) {
            return Person.PhoneType.PHONE_TYPE_UNSPECIFIED;
        }

        switch (type.toUpperCase()) {
            case "MOBILE":
                return Person.PhoneType.PHONE_TYPE_MOBILE;
            case "HOME":
                return Person.PhoneType.PHONE_TYPE_HOME;
            case "WORK":
                return Person.PhoneType.PHONE_TYPE_WORK;
            default:
                return Person.PhoneType.PHONE_TYPE_UNSPECIFIED;
        }
    }

}