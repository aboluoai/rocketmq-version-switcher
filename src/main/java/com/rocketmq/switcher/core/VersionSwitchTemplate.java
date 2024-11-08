package com.lyz.rocketmq.switcher.core;

import com.lyz.rocketmq.switcher.config.RocketMQSwitchProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.apis.producer.Producer;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class VersionSwitchTemplate {
    private final RocketMQSwitchProperties properties;
    private final ConcurrentHashMap<String, DefaultMQProducer> v4Producers;
    private final ConcurrentHashMap<String, Producer> v5Producers;
    private final ConcurrentHashMap<String, Boolean> topicHealthStatus = new ConcurrentHashMap<>();

    public VersionSwitchTemplate(RocketMQSwitchProperties properties,
                               ConcurrentHashMap<String, DefaultMQProducer> v4Producers,
                               ConcurrentHashMap<String, Producer> v5Producers) {
        this.properties = properties;
        this.v4Producers = v4Producers;
        this.v5Producers = v5Producers;
    }

    public SendResult send(String topic, String tag, byte[] body) throws Exception {
        RocketMQSwitchProperties.TopicConfig topicConfig = properties.getTopics().get(topic);
        if (topicConfig == null) {
            throw new IllegalArgumentException("Topic configuration not found: " + topic);
        }

        DefaultMQProducer v4Producer = v4Producers.get(topic);
        if (v4Producer == null) {
            throw new IllegalStateException("No V4 Producer found for topic: " + topic);
        }

        Producer v5Producer = v5Producers.get(topic);

        if (!topicConfig.isEnableV5() || !isHealthy(topic) || !shouldUseV5(topicConfig)) {
            return sendV4Message(topic, tag, body, v4Producer);
        }

        try {
            return sendV5Message(topic, tag, body, v5Producer);
        } catch (Exception e) {
            log.error("Failed to send message using V5 producer for topic: " + topic, e);
            if (topicConfig.isEnableFallback()) {
                markUnhealthy(topic);
                return sendV4Message(topic, tag, body, v4Producer);
            }
            throw e;
        }
    }

    private boolean shouldUseV5(RocketMQSwitchProperties.TopicConfig config) {
        return random.nextInt(100) < config.getV5Percentage();
    }

    private boolean isHealthy(String topic) {
        return topicHealthStatus.getOrDefault(topic, true);
    }

    private void markUnhealthy(String topic) {
        topicHealthStatus.put(topic, false);
        // 可以添加定时任务，在一定时间后重置状态
    }

    private SendResult sendV4Message(String topic, String tag, byte[] body, DefaultMQProducer v4Producer) throws Exception {
        org.apache.rocketmq.common.message.Message message = 
            new org.apache.rocketmq.common.message.Message(topic, tag, body);
        return v4Producer.send(message);
    }

    private SendResult sendV5Message(String topic, String tag, byte[] body, Producer v5Producer) throws Exception {
        // 这里需要转换V5的返回结果为V4的SendResult
        org.apache.rocketmq.client.apis.message.Message message = 
            org.apache.rocketmq.client.apis.message.Message.builder()
                .setTopic(topic)
                .setTag(tag)
                .setBody(body)
                .build();
        return convertV5Result(v5Producer.send(message));
    }

    private SendResult convertV5Result(org.apache.rocketmq.client.apis.producer.SendReceipt v5Result) {
        // 实现V5到V4结果的转换逻辑
        SendResult result = new SendResult();
        // ... 设置转换后的属性
        return result;
    }
} 