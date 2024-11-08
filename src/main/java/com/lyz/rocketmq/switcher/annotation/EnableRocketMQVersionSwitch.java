package com.lyz.rocketmq.switcher.annotation;

import com.lyz.rocketmq.switcher.config.RocketMQSwitchAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RocketMQSwitchAutoConfiguration.class)
public @interface EnableRocketMQVersionSwitch {
} 