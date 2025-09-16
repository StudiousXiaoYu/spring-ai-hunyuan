# Spring AI HunYuan

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.studiousxiaoyu/spring-ai-starter-model-hunyuan.svg)](https://search.maven.org/artifact/io.github.studiousxiaoyu/spring-ai-starter-model-hunyuan)
[![Documentation](https://img.shields.io/badge/Documentation-Spring%20AI%20HunYuan-green.svg)](https://github.com/StudiousXiaoYu/spring-ai-hunyuan)


> 🚀 **Spring AI HunYuan** 是基于 Spring AI 框架的腾讯云混元大模型集成库，为 Java 开发者提供简单易用的 AI 应用开发体验。

## ✨ 核心特性

- 🎯 **开箱即用** - 基于 Spring Boot 自动配置，零配置快速集成
- 💬 **多模态对话** - 支持文本对话、图片理解、流式响应
- 🧠 **智能嵌入** - 提供高质量的文本向量化服务
- 🔧 **函数调用** - 支持工具函数调用，构建智能应用
- ⚡ **高性能** - 基于 Spring AI 框架，性能优异
- 🛡️ **企业级** - 支持重试、监控、安全认证等企业特性

## 🎯 支持的功能

| 功能 | 状态 | 说明 |
|------|------|------|
| 💬 Chat 对话 | ✅ | 支持 HunYuan 模型，流式和非流式响应 |
| 🖼️ 图片理解 | ✅ | 支持本地图片和网络图片分析 |
| 🧠 Embedding | ✅ | 文本向量化，支持批量处理 |
| 🔧 函数调用 | ✅ | 支持工具函数调用和结构化输出 |
| 🧩 Chain of Thought | ✅ | 思考链模式，提升推理能力 |
| 📊 使用统计 | ✅ | 完整的 Token 使用量统计 |

---

## 🚀 快速开始

### 📋 环境要求

- **JDK**: 17 或更高版本
- **Maven**: 3.8 或更高版本  
- **Spring Boot**: 3.3.3 或更高版本
- **腾讯云账号**: 需要开通 HunYuan 服务并获取 API 密钥

### 📦 添加依赖

在你的 Spring Boot 项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>io.github.studiousxiaoyu</groupId>
    <artifactId>spring-ai-starter-model-hunyuan</artifactId>
    <version>1.0.0.2</version>
</dependency>
```

### ⚙️ 配置参数

在 `application.yml` 或 `application.properties` 中配置 HunYuan 参数：

**application.yml:**
```yaml
spring:
  ai:
    hunyuan:
      secret-id: 你的-secret-id
      secret-key: 你的-secret-key
      chat:
        options:
          model: hunyuan-pro
          temperature: 0.7
          max-tokens: 2048
      embedding:
        options:
          model: hunyuan-embedding
          dimensions: 1024
```

**application.properties:**
```properties
spring.ai.hunyuan.secret-id=你的-secret-id
spring.ai.hunyuan.secret-key=你的-secret-key
spring.ai.hunyuan.chat.options.model=hunyuan-pro
spring.ai.hunyuan.chat.options.temperature=0.7
spring.ai.hunyuan.chat.options.max-tokens=2048
spring.ai.hunyuan.embedding.options.model=hunyuan-embedding
spring.ai.hunyuan.embedding.options.dimensions=1024
```

## ⚙️ 配置选项

### Chat 配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.ai.hunyuan.secret-id` | - | 腾讯云 Secret ID（必填） |
| `spring.ai.hunyuan.secret-key` | - | 腾讯云 Secret Key（必填） |
| `spring.ai.hunyuan.chat.options.model` | `hunyuan-pro` | 使用的聊天模型 |
| `spring.ai.hunyuan.chat.options.temperature` | `0.7` | 温度参数，控制输出随机性 |
| `spring.ai.hunyuan.chat.options.max-tokens` | `2048` | 最大输出 token 数 |
| `spring.ai.hunyuan.chat.options.top-p` | `0.8` | Top-p 采样参数 |

### Embedding 配置

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.ai.hunyuan.embedding.options.model` | `hunyuan-embedding` | 使用的嵌入模型 |
| `spring.ai.hunyuan.embedding.options.dimensions` | `1024` | 嵌入向量维度 |


## 🏗️ 项目结构

```
spring-ai-hunyuan/
├── spring-ai-hunyuan-core/           # 核心实现
│   ├── api/                          # API 接口定义
│   ├── chat/                         # 聊天功能
│   ├── metadata/                     # 元数据
│   └── aot/                          # AOT 支持
├── spring-ai-autoconfigure-model-hunyuan/  # 自动配置
└── spring-ai-starter-model-hunyuan/  # Starter 模块
```

## 🤝 贡献指南

我们欢迎所有形式的贡献！请遵循以下步骤：

### 1. Fork 项目
```bash
git clone https://github.com/你的用户名/spring-ai-hunyuan.git
cd spring-ai-hunyuan
```

### 2. 创建功能分支
```bash
git checkout -b feature/你的功能名称
```

### 3. 提交更改
```bash
git commit -m "feat: 添加新功能"
```

### 4. 推送分支
```bash
git push origin feature/你的功能名称
```

### 5. 创建 Pull Request

### 开发规范
- 遵循 Spring 代码风格
- 添加必要的单元测试
- 更新相关文档
- 确保所有测试通过

## 📄 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源协议。

## 🙏 致谢

- [Spring AI](https://spring.io/projects/spring-ai) - 强大的 AI 应用开发框架
- [腾讯云混元](https://cloud.tencent.com/product/hunyuan) - 优秀的大语言模型服务
- 所有贡献者和用户的支持

## 📞 联系方式

- **作者**: JunYu Guo
- **邮箱**: 1316356098@qq.com
- **GitHub**: [@StudiousXiaoYu](https://github.com/StudiousXiaoYu)
- **项目地址**: [https://github.com/StudiousXiaoYu/spring-ai-hunyuan](https://github.com/StudiousXiaoYu/spring-ai-hunyuan)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给我们一个 Star！**

Made with ❤️ by [StudiousXiaoYu](https://github.com/StudiousXiaoYu)

</div>