package org.example.userservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondto.dto.event.UserEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;


    @Value("${app.kafka.user-notifications-topic}")
    private String topicName;

    public void sendUserEvent(UserEvent event) {

        log.info("Отправка события в Kafka. Топик: '{}', Ключ: '{}', Событие: {}",
                topicName, event.getEmail(), event);

        try {

            kafkaTemplate.send(topicName, event.getEmail(), event);
        } catch (Exception e) {

            log.error("Ошибка при отправке события в Kafka: {}", event, e);
        }
    }
}