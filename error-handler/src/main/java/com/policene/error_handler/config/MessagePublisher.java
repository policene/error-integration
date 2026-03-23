package com.policene.error_handler.config;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class MessagePublisher {

    private final RedisTemplate<String, String> template;
    private final ChannelTopic topic;

    public MessagePublisher(RedisTemplate<String, String> template, ChannelTopic topic) {
        this.template = template;
        this.topic = topic;
    }

    public void publish (String message) {
        template.convertAndSend(topic.getTopic(), message);
    }

}
