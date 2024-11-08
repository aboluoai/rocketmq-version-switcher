package com.lyz.rocketmq.switcher.config;

import com.lyz.rocketmq.switcher.core.VersionSwitchTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.RelaxedPropertyResolver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;
import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties(RocketMQSwitchProperties.class)
public class RocketMQSwitchAutoConfiguration {
    
    @Autowired
    private Environment environment;
    
    @PostConstruct
    public void init() {
        Properties topicProperties = new Properties();
        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(environment, "rocketmq.version-switch.");
        
        resolver.getSubProperties("topics.").forEach((key, value) -> {
            topicProperties.setProperty("topics." + key, value);
        });
        
        properties.setTopicsProperties(topicProperties);
    }
    
    private final ConcurrentHashMap<String, DefaultMQProducer> v4Producers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Producer> v5Producers = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private Map<String, DefaultMQProducer> existingV4Producers;

    @Bean
    @ConditionalOnMissingBean
    public DefaultMQProducer v4Producer(RocketMQSwitchProperties properties) {
        try {
            // 遍历所有topic配置
            properties.getTopics().forEach((topic, config) -> {
                try {
                    // 检查是否使用已存在的V4生产者
                    if (config.isUseExistingV4Producer()) {
                        DefaultMQProducer existingProducer = findExistingProducer(topic);
                        if (existingProducer != null) {
                            v4Producers.put(topic, existingProducer);
                            log.info("Using existing V4 Producer for topic: {}", topic);
                            return;
                        }
                        log.warn("No existing V4 Producer found for topic: {}, will create new one", topic);
                    }
                
                    // 创建新的生产者
                    DefaultMQProducer topicProducer = new DefaultMQProducer(config.getProducerGroup());
                    topicProducer.setNamesrvAddr(properties.getV4NameServer());
                    topicProducer.start();
                    v4Producers.put(topic, topicProducer);
                    log.info("V4 Producer initialized for topic: {}, group: {}", topic, config.getProducerGroup());
                } catch (Exception e) {
                    log.error("Failed to initialize V4 producer for topic: " + topic, e);
                }
            });
            
            return new DefaultMQProducer(); // 返回默认实例
        } catch (Exception e) {
            log.error("Failed to initialize V4 producers", e);
            throw new RuntimeException("Failed to initialize V4 producers", e);
        }
    }

    private DefaultMQProducer findExistingProducer(String topic) {
        if (existingV4Producers != null) {
            // 这里可以根据您的命名规则来匹配对应的producer
            // 例如：如果producer的bean名称包含topic名称
            return existingV4Producers.entrySet().stream()
                .filter(entry -> entry.getKey().contains(topic))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    public Producer v5Producer(RocketMQSwitchProperties properties) {
        try {
            // 遍历所有topic配置，为每个topic创建对应的producer
            properties.getTopics().forEach((topic, config) -> {
                try {
                    ClientServiceProvider provider = ClientServiceProvider.loadService();
                    ClientConfiguration configuration = ClientConfiguration.newBuilder()
                            .setEndpoints(properties.getV5NameServer())
                            .enableSsl(false)
                            .build();
                    
                    Producer producer = provider.newProducerBuilder()
                            .setTopics(topic)
                            .setClientConfiguration(configuration)
                            .build();
                    
                    v5Producers.put(topic, producer);
                    log.info("V5 Producer initialized for topic: {}", topic);
                } catch (Exception e) {
                    log.error("Failed to initialize V5 producer for topic: " + topic, e);
                }
            });
            
            // 返回一个默认的V5 producer用于注入
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            ClientConfiguration configuration = ClientConfiguration.newBuilder()
                    .setEndpoints(properties.getV5NameServer())
                    .enableSsl(false)
                    .build();
            
            return provider.newProducerBuilder()
                    .setClientConfiguration(configuration)
                    .build();
        } catch (Exception e) {
            log.error("Failed to initialize V5 producer", e);
            throw new RuntimeException("Failed to initialize V5 producer", e);
        }
    }

    @Bean
    public VersionSwitchTemplate versionSwitchTemplate(RocketMQSwitchProperties properties,
                                                      DefaultMQProducer v4Producer,
                                                      Producer v5Producer) {
        return new VersionSwitchTemplate(properties, v4Producers, v5Producers);
    }

    @PreDestroy
    public void destroy() {
        // 关闭所有V4 producers
        v4Producers.forEach((topic, producer) -> {
            try {
                producer.shutdown();
                log.info("V4 Producer shutdown for topic: {}", topic);
            } catch (Exception e) {
                log.error("Error shutting down V4 producer for topic: " + topic, e);
            }
        });

        // 关闭所有V5 producers
        v5Producers.forEach((topic, producer) -> {
            try {
                producer.close();
                log.info("V5 Producer shutdown for topic: {}", topic);
            } catch (Exception e) {
                log.error("Error shutting down V5 producer for topic: " + topic, e);
            }
        });
    }
} 