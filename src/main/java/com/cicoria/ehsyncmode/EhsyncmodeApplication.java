package com.cicoria.ehsyncmode;

import com.azure.spring.integration.core.EventHubHeaders;
import com.azure.spring.integration.core.api.reactor.Checkpointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

import static com.azure.spring.integration.core.AzureHeaders.CHECKPOINTER;

@SpringBootApplication
public class EhsyncmodeApplication {

    public static final Logger LOGGER = LoggerFactory.getLogger(EhsyncmodeApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EhsyncmodeApplication.class, args);
    }

    @Bean
    public Consumer<Message<String>> consume() {
        return message -> {
            Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
            LOGGER.info(
                    "New message received: '{}', partition key: {}, sequence number: {}, offset: {}, enqueued time: {}",
                    message.getPayload(), message.getHeaders().get(EventHubHeaders.PARTITION_KEY),
                    message.getHeaders().get(EventHubHeaders.SEQUENCE_NUMBER),
                    message.getHeaders().get(EventHubHeaders.OFFSET),
                    message.getHeaders().get(EventHubHeaders.ENQUEUED_TIME));
            checkpointer.success()
                    .doOnSuccess(success -> LOGGER.info("Message '{}' successfully checkpointed", message.getPayload()))
                    .doOnError(error -> LOGGER.error("Exception found", error)).subscribe();
        };
    }

    // https://github.com/Azure/azure-sdk-for-java/blob/azure-spring-cloud-stream-binder-eventhubs_2.5.0/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-sample-eventhubs-binder/src/main/java/com/azure/spring/sample/eventhubs/binder/EventHubBinderApplication.java
    // Replace destination with
    // spring.cloud.stream.bindings.consume-in-0.destination
    // Replace group with spring.cloud.stream.bindings.consume-in-0.group
    @ServiceActivator(inputChannel = "wingtiptoyshub.$Default.errors")
    public void consumerError(Message<?> message) {
        LOGGER.error("Handling customer ERROR: " + message);
    }

    // Replace destination with
    // spring.cloud.stream.bindings.supply-out-0.destination
    @ServiceActivator(inputChannel = "wingtiptoyshub.errors")
    public void producerError(Message<?> message) {
        LOGGER.error("Handling Producer ERROR: " + message);
    }
}