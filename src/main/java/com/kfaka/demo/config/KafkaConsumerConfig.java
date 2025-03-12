package com.kfaka.demo.config;

import com.kfaka.demo.protos.AddressBook;
import com.kfaka.demo.serializer.SimpleProtobufDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value( "${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, AddressBook> addressBookConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "addressbook-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SimpleProtobufDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                configProps,
                new StringDeserializer(),
                new SimpleProtobufDeserializer<>(AddressBook.parser())
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AddressBook> addressBookKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AddressBook> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(addressBookConsumerFactory());
        return factory;
    }

}
