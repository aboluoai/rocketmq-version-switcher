package com.rocketmq.switcher.example;

import com.lyz.rocketmq.switcher.annotation.EnableRocketMQVersionSwitch;
import com.lyz.rocketmq.switcher.core.VersionSwitchTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@SpringBootApplication
@EnableRocketMQVersionSwitch
public class MessageSenderExample {

    @Autowired
    private VersionSwitchTemplate versionSwitchTemplate;

    public static void main(String[] args) {
        SpringApplication.run(MessageSenderExample.class, args);
    }

    @PostMapping("/send/{topic}")
    public String sendMessage(@PathVariable String topic, 
                            @RequestParam String tag,
                            @RequestBody String message) {
        try {
            SendResult result = versionSwitchTemplate.send(topic, tag, message.getBytes());
            log.info("Message sent successfully, msgId: {}", result.getMsgId());
            return "Message sent successfully, msgId: " + result.getMsgId();
        } catch (Exception e) {
            log.error("Failed to send message", e);
            return "Failed to send message: " + e.getMessage();
        }
    }

    @PostMapping("/send/delay/{topic}")
    public String sendDelayMessage(@PathVariable String topic,
                                 @RequestParam String tag,
                                 @RequestParam Integer delayLevel,
                                 @RequestBody String message) {
        try {
            SendResult result = versionSwitchTemplate.sendDelay(topic, tag, message.getBytes(), delayLevel);
            log.info("Delay message sent successfully, msgId: {}", result.getMsgId());
            return "Delay message sent successfully, msgId: " + result.getMsgId();
        } catch (Exception e) {
            log.error("Failed to send delay message", e);
            return "Failed to send delay message: " + e.getMessage();
        }
    }
} 