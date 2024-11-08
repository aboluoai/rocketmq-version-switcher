package com.lyz.rocketmq.switcher.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Data
@ConfigurationProperties(prefix = "rocketmq.version-switch")
public class RocketMQSwitchProperties {
    private String v4NameServer;
    private String v5NameServer;
    
    @NestedConfigurationProperty
    private Map<String, TopicConfig> topics = new HashMap<>();

    // 支持从properties文件加载topic配置
    public void setTopicsProperties(Properties properties) {
        properties.forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.startsWith("topics.")) {
                String[] parts = keyStr.split("\\.");
                if (parts.length == 4) {
                    String topicName = parts[1];
                    String property = parts[2];
                    TopicConfig topicConfig = topics.computeIfAbsent(topicName, k -> new TopicConfig());
                    
                    switch (property) {
                        case "enable-v5":
                            topicConfig.setEnableV5(Boolean.parseBoolean(value.toString()));
                            break;
                        case "v5-percentage":
                            topicConfig.setV5Percentage(Integer.parseInt(value.toString()));
                            break;
                        case "producer-group":
                            topicConfig.setProducerGroup(value.toString());
                            break;
                        case "enable-fallback":
                            topicConfig.setEnableFallback(Boolean.parseBoolean(value.toString()));
                            break;
                        case "max-retry-times":
                            topicConfig.setMaxRetryTimes(Integer.parseInt(value.toString()));
                            break;
                        case "fallback-threshold":
                            topicConfig.setFallbackThreshold(Long.parseLong(value.toString()));
                            break;
                    }
                }
            }
        });
    }

    @Data
    public static class TopicConfig {
        private boolean enableV5;
        private int v5Percentage = 0; // 0-100
        private String producerGroup;
        private String consumerGroup;
        private boolean enableFallback = true;
        private int maxRetryTimes = 3;
        private long fallbackThreshold = 1000; // 毫秒
        private boolean useExistingV4Producer = false; // 新增配置项，控制是否使用已存在的V4生产者
    }
} 