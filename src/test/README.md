# RocketMQ Version Switcher Demo

这是一个演示 RocketMQ Version Switcher 使用方法的示例项目。该示例展示了如何发送普通消息和延时消息。

## 项目结构

```
src/test/
├── java/
│   └── com/rocketmq/switcher/example/
│       └── MessageSenderExample.java    # 示例应用主类
└── resources/
    └── application.yml                  # 配置文件
```

## 环境要求

- Java 8+
- Maven 3.6+
- RocketMQ 4.x Server (端口: 9876)
- RocketMQ 5.x Server (端口: 8081)

## 运行步骤

1. 确保 RocketMQ 服务已启动
   ```bash
   # 启动 RocketMQ 4.x
   nohup sh bin/mqnamesrv &
   nohup sh bin/mqbroker -n localhost:9876 &

   # 启动 RocketMQ 5.x
   nohup sh bin/mqnamesrv -p 8081 &
   nohup sh bin/mqbroker -n localhost:8081 &
   ```

2. 修改配置文件
   根据实际环境修改 `application.yml` 中的 nameserver 地址：
   ```yaml
   rocketmq:
     version-switch:
       v4-name-server: localhost:9876
       v5-name-server: localhost:8081
   ```

3. 运行应用
   ```bash
   mvn spring-boot:run
   ```

## 接口测试

### 1. 发送普通消息

```bash
curl -X POST "http://localhost:8080/send/test-topic?tag=TagA" \
     -H "Content-Type: text/plain" \
     -d "Hello RocketMQ Version Switcher!"
```

预期响应：
```json
"Message sent successfully, msgId: C0A80165464018B4AAC26B3FA9800000"
```

### 2. 发送延时消息

```bash
curl -X POST "http://localhost:8080/send/delay/test-topic?tag=TagA&delayLevel=3" \
     -H "Content-Type: text/plain" \
     -d "This is a delayed message!"
```

预期响应：
```json
"Delay message sent successfully, msgId: C0A80165464018B4AAC26B3FA9800001"
```

延时级别说明：
- 1=1s
- 2=5s
- 3=10s
- 4=30s
- 5=1m
- 6=2m
- 7=3m
- 8=4m
- 9=5m
- 10=6m
- 11=7m
- 12=8m
- 13=9m
- 14=10m
- 15=20m
- 16=30m
- 17=1h
- 18=2h

## 配置说明

示例中包含两个 Topic 的配置：

1. test-topic
   - 启用了 v5 版本
   - 20% 的消息通过 v5 发送
   - 开启故障转移
   - 最大重试次数为 3

2. order-topic
   - 仅使用 v4 版本
   - 开启故障转移
   - 最大重试次数为 3

## 监控和日志

应用会输出详细的日志信息，包括：
- 消息发送结果
- 版本切换情况
- 错误和异常信息

可以通过日志监控消息发送情况：
```bash
tail -f logs/application.log
```

## 注意事项

1. 确保两个版本的 RocketMQ 服务都已正确启动
2. 检查网络连接和端口是否正常开放
3. 建议先使用小流量测试 v5 版本
4. 观察日志中的错误信息进行故障排查

## 常见问题

1. 连接超时
   - 检查 nameserver 地址是否正确
   - 确认防火墙设置

2. 消息发送失败
   - 查看 broker 是否正常运行
   - 检查 topic 是否已创建

3. 版本切换不生效
   - 确认配置文件中的百分比设置
   - 检查日志中的切换记录
