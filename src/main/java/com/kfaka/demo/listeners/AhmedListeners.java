package com.kfaka.demo.listeners;

import com.kfaka.demo.protos.AddressBook;
import com.kfaka.demo.protos.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AhmedListeners {

    @KafkaListener(
            topics = "${kafka.topic.addressbook}",
            groupId = "address-book-group",
            containerFactory = "addressBookKafkaListenerContainerFactory"
    )
    public void listen(AddressBook addressBook) {
        log.info("Received AddressBook with {} people", addressBook.getPeopleCount());

        for (Person person : addressBook.getPeopleList()) {
            log.info("Person: name={}, id={}, email={}",
                    person.getName(), person.getId(), person.getEmail());

            log.info("  Phone numbers: {}", person.getPhonesCount());
            person.getPhonesList().forEach(phone ->
                    log.info("    {}: {}", phone.getType(), phone.getNumber())
            );
        }
    }
}
