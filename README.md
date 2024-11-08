# RocketMQ Version Switcher

一个轻量级的 Spring Boot Starter，用于帮助应用平滑从 RocketMQ 4.x 迁移到 5.x。

## 功能特性

- 支持 RocketMQ 4.x 和 5.x 双版本生产者
- 基于百分比的流量切换
- 自动故障转移机制
- Topic 级别的精细化配置
- 健康状态监控
- 支持复用已存在的 V4 生产者

## 快速开始
### 使用demo
[demo示例](/src/test)
### Maven 依赖

```xml
<dependency>
    <groupId>com.yanzhiliu</groupId>
    <artifactId>rocketmq-version-switch-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 配置说明

支持 properties 和 yaml 两种配置方式:

#### YAML 配置示例
```yaml
rocketmq:
  version-switch:
    v4-name-server: localhost:9876
    v5-name-server: localhost:8081
    topics:
      test-topic:  # topic 名称
        enable-v5: true  # 是否启用 v5
        v5-percentage: 20  # v5 版本流量百分比
        producer-group: test-producer-group  # 生产者组
        enable-fallback: true  # 是否启用故障转移
        max-retry-times: 3  # 最大重试次数
        fallback-threshold: 1000  # 故障转移阈值(毫秒)
        use-existing-v4-producer: false  # 是否使用已存在的 v4 生产者
```

#### Properties 配置示例
```properties
rocketmq.version-switch.v4-name-server=localhost:9876
rocketmq.version-switch.v5-name-server=localhost:8081
rocketmq.version-switch.topics.test-topic.enable-v5=true
rocketmq.version-switch.topics.test-topic.v5-percentage=20
rocketmq.version-switch.topics.test-topic.producer-group=test-producer-group
rocketmq.version-switch.topics.test-topic.enable-fallback=true
rocketmq.version-switch.topics.test-topic.max-retry-times=3
rocketmq.version-switch.topics.test-topic.fallback-threshold=1000
rocketmq.version-switch.topics.test-topic.use-existing-v4-producer=false
```

### 配置项说明

| 配置项 | 说明 | 默认值 |
|-------|------|--------|
| v4-name-server | RocketMQ 4.x 的 NameServer 地址 | - |
| v5-name-server | RocketMQ 5.x 的 NameServer 地址 | - |
| enable-v5 | 是否启用 5.x 版本发送 | false |
| v5-percentage | 使用 5.x 版本的流量百分比(0-100) | 0 |
| producer-group | 生产者组名称 | - |
| enable-fallback | 是否启用故障转移 | true |
| max-retry-times | 最大重试次数 | 3 |
| fallback-threshold | 故障转移阈值(毫秒) | 1000 |
| use-existing-v4-producer | 是否使用已存在的 V4 生产者 | false |


## 特性说明

1. **双版本支持**
   - 同时支持 RocketMQ 4.x 和 5.x 版本
   - 可以按照百分比灰度切换流量

2. **故障转移**
   - 当 5.x 发送失败时自动回退到 4.x
   - 可配置失败重试次数和阈值

3. **精细化配置**
   - 支持针对不同 Topic 的独立配置
   - 可以复用已存在的 V4 生产者

## 注意事项

1. 确保 RocketMQ 4.x 和 5.x 的客户端版本兼容性
2. 建议先小比例测试 5.x 的稳定性
3. 合理配置故障转移阈值，避免频繁切换
4. 监控日志中的警告和错误信息

## 版本要求

- Java 8+
- Spring Boot 2.4.2+
- RocketMQ 4.9.4+
- RocketMQ 5.1.3+

## License

[Apache License 2.0](LICENSE)
