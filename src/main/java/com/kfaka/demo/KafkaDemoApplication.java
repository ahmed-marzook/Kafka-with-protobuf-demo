package com.kfaka.demo;

import com.google.protobuf.GeneratedMessage;
import com.kfaka.demo.protos.AddressBook;
import com.kfaka.demo.protos.Person;
import com.kfaka.demo.serializer.SimpleProtobufDeserializer;
import com.kfaka.demo.serializer.SimpleProtobufSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;

@SpringBootApplication
@Slf4j
public class KafkaDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaDemoApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(KafkaTemplate<String, AddressBook> kafkaTemplate) {
		return args -> {
			Person person = Person.newBuilder().setName("Ahmed").setEmail("ahmed@gmail.com").build();
			Person person2 = Person.newBuilder().setName("Jhon").setEmail("jhon@gmail.com").addPhones((Person.PhoneNumber.newBuilder().setType(Person.PhoneType.PHONE_TYPE_MOBILE).setNumber("0123235235").build())).build();
			AddressBook addressBook = AddressBook.newBuilder().addPeople(person).addPeople(person2).build();
			try {
				SimpleProtobufSerializer<GeneratedMessage> serializer = new SimpleProtobufSerializer<>();
				SimpleProtobufDeserializer<AddressBook> deserializer = new SimpleProtobufDeserializer<>(AddressBook.parser());
				byte[] bytes = serializer.serialize("addressbook", addressBook);
				log.info("Serialized AddressBook: {}", Arrays.toString(bytes));
				AddressBook addressBook1 = (AddressBook) deserializer.deserialize("addressbook", bytes);
				log.info("Deserialized AddressBook: {}", addressBook1);
			} catch (Exception e) {
				e.printStackTrace();
			}

//			kafkaTemplate.send("addressbook", addressBook);
		};
	}

}
