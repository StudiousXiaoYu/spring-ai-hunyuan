# Spring AI HunYuan 

## 📌 项目概述

本项目是基于 **Spring AI** 集成 **腾讯云 HunYuan 大模型 API** 的 Java SDK，提供了对 HunYuan Pro、Embedding 模型的封装调用支持，并通过自动配置简化集成过程。


当前以支持功能：
- ✅ Chat ( 流式回答、图片理解 )
- ✅ Embedding

---

## 🚀 快速开始

### 1. 环境准备

确保你已安装以下工具：

- ✅ JDK 17+
- ✅ Maven 3.8+
- ✅ 腾讯云账号 + HunYuan 秘钥（Secret ID / Secret Key）

---

### 2. 创建 Spring Boot 项目并集成

在你的 Spring Boot 项目中添加如下依赖（以 [pom.xml](file://D:\project\spring-ai-hunyuan\pom.xml) 为例）：

```xml
<dependency>
    <groupId>io.github.studiousxiaoyu</groupId>
    <artifactId>spring-ai-starter-model-hunyuan</artifactId>
    <version>1.0.0</version>
</dependency>
```


---

### 3. 配置 HunYuan 参数

在 `application.properties` 或 `application.yml` 中添加 HunYuan 认证信息和模型配置：

```properties
spring.ai.hunyuan.secret-id=你的-secret-id
spring.ai.hunyuan.secret-key=你的-secret-key
spring.ai.hunyuan.chat.options.model=hunyuan-pro
spring.ai.hunyuan.embedding.options.model=hunyuan-embedding
spring.ai.hunyuan.embedding.options.dimensions=1024
```


---

### 3. 编写测试代码

#### 使用 `ChatClient` 发起对话

```java
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HunYuanService {

    private final ChatClient chatClient;

    public HunYuanService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String askQuestion(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
```


#### 控制器示例

```java
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final HunYuanService hunYuanService;

    public ChatController(HunYuanService hunYuanService) {
        this.hunYuanService = hunYuanService;
    }

    @GetMapping("/q")
    public String chat(@RequestParam("text") String text) {
        return hunYuanService.askQuestion(text);
    }
}
```


---

### 4. 启动并测试

启动 Spring Boot 应用，访问：

```
http://localhost:8080/chat/q?text=你好
```


你应该能收到来自 HunYuan Pro 模型的回复。

---

该项目为 Spring Boot 开发者提供了一个轻量级、易集成的 HunYuan 大模型调用方案，适用于构建智能问答、文档检索、多模态对话等 AI 应用场景。

---

🎉 Happy Coding! 如有疑问或建议，请提交 Issues 或 PR！