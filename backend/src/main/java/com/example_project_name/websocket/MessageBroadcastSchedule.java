package com.example_project_name.websocket;

import static com.example_project_name.websocket.WebSocketConfig.TOPIC_MY_EVENT;
import static com.example_project_name.websocket.WebSocketConfig.TOPIC_PREFIX;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example_project_name.generated.model.WebSocketEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Sample server push: flips a boolean every 2 seconds and broadcasts it.
// The payload type (WebSocketEvent) is defined in openapi/openapi.yaml so the frontend gets the same type.
// Delete this class once you have real events.
@EnableScheduling
@Component
@Slf4j
@RequiredArgsConstructor
public class MessageBroadcastSchedule {

    private final SimpMessagingTemplate template;
    private boolean status = false;

    @Scheduled(cron = "0/2 * * * * *")
    public void sendMessage() {
        String topic = TOPIC_PREFIX + TOPIC_MY_EVENT;
        log.debug("Broadcasting message on topic {}", topic);
        template.convertAndSend(topic, new WebSocketEvent().status(status));
        status = !status;
    }
}
