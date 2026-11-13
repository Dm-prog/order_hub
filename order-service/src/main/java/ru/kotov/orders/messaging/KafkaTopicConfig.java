package ru.kotov.orders.messaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDER_CREATED_TOPIC = "order.created";
    public static final String ORDER_CONFIRMED_TOPIC = "order.confirmed";
    public static final String ORDER_REJECTED_TOPIC = "order.rejected";

    public static final String ORDER_CREATED_DLQ = "order.created.dlq";
    public static final String ORDER_CONFIRMED_DLQ = "order.confirmed.dlq";
    public static final String ORDER_REJECTED_DLQ = "order.rejected.dlq";

    @Bean
    public KafkaAdmin.NewTopics topics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(ORDER_CREATED_TOPIC).partitions(1).replicas(1).build(),
                TopicBuilder.name(ORDER_CONFIRMED_TOPIC).partitions(1).replicas(1).build(),
                TopicBuilder.name(ORDER_REJECTED_TOPIC).partitions(1).replicas(1).build(),
                TopicBuilder.name(ORDER_CREATED_DLQ).partitions(1).replicas(1).build(),
                TopicBuilder.name(ORDER_CONFIRMED_DLQ).partitions(1).replicas(1).build(),
                TopicBuilder.name(ORDER_REJECTED_DLQ).partitions(1).replicas(1).build()
        );
    }
}
