## 洞态IAST
[![django-project](https://img.shields.io/badge/django%20versions-3.0.3-blue)](https://www.djangoproject.com/)
[![dongtai-project](https://img.shields.io/badge/dongtai%20versions-beta-green)](https://github.com/huoxianclub/dongtai)
[![dongtai--agent--java](https://img.shields.io/badge/dongtai--agent--java-v1.0.0-lightgrey)](https://github.com/huoxianclub/dongtai-web)

> 原"灵芝IAST"，后更名为"洞态IAST"，产品改为SaaS版，agent端采集与污点相关的数据并发送至服务器端，服务器端接收数据并重构形成污点方法图，再根据深度优先算法搜索污点调用链

## 项目介绍
“火线～洞态IAST”是一款专为甲方安全人员、甲乙代码审计工程师和0 Day漏洞挖掘人员量身打造的辅助工具，可用于集成devops环境进行漏洞检测、作为代码审计的辅助工具和自动化挖掘0 Day。

“火线～洞态IAST”具有五大模块，分别是`dongtai-webapi`、`dongtai-openapi`、`dongtai-engine`、`dongtai-web`、`agent`，其中：
- `dongtai-webapi`用于与`dongtai-web`交互，负责用户相关的API请求；
- `dongtai-openapi`用于与`agent`交互，处理agent上报的数据，向agent下发策略，控制agent的运行等
- `dongtai-engine`用于对`dongtai-openapi`接收到的数据进行分析、处理，计算存在的漏洞和可用的污点调用链等
- `dongtai-web`为“火线～洞态IAST”的前端项目，负责页面展示
- `agent`为各语言的数据采集端，从安装探针的项目中采集相对应的数据，发送至`dongtai-openapi`服务


## 应用场景
“火线～洞态IAST”可应用于：`devsecops`阶段做自动化漏洞检测、开源软件/组件挖掘通用漏洞、上线前安全测试等场景，主要目的是降低现有漏洞检测的工作量，释放安全从业人员的生产力来做更专业的事情。

## JavaAgent详细介绍

### Agent功能结构
- 启动器
- 动态插桩（采集数据）
- 动态插桩（hook点处理）
- 数据计算
- 数据压缩后发送至dc

### IAST-CORE包结构
- enhance，存放字节码增强的相关代码
- handler，存放AOP捕获数据后的处理代码
- service，存放报告的后端发送代码逻辑
- start，存放IAST检测引擎的启动代码

#### 支持的Java版本及中间件
- Java 1.6+
- Tomcat、Jetty、WebLogic、WebSphere、SpringBoot等主流软件和中间件

**notice:** `jdk 1.6`开发的Agent需要使用`Maven 3.2.5`进行构建
