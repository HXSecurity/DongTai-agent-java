## 洞态IAST
[![django-project](https://img.shields.io/badge/django%20versions-3.0.3-blue)](https://www.djangoproject.com/)
[![dongtai-project](https://img.shields.io/badge/dongtai%20versions-beta-green)](https://github.com/huoxianclub/dongtai)
[![dongtai--agent--java](https://img.shields.io/badge/dongtai--agent--java-v1.0.0-lightgrey)](https://github.com/huoxianclub/dongtai-web)
[![license GPL-3.0](https://img.shields.io/github/license/HXSecurity/DongTai-agent-java)](https://github.com/HXSecurity/DongTai-agent-java/blob/main/LICENSE)


## 项目介绍
"火线 - 洞态IAST" 是一款**被动式IAST**，专为甲方安全人员、代码审计工程师和0 Day挖掘人员量身打造的辅助工具，可用于集成devops环境进行漏洞检测、作为代码审计的辅助工具和自动化挖掘0 Day。

## 应用场景
- DevOps流程
- 上线前安全测试
- 第三方组件管理
- 代码审计
- 0 Day挖掘


## 快速上手

### 快速使用
请参考：[快速开始](https://hxsecurity.github.io/DongTaiDoc/#/doc/tutorial/quickstart)

### 快速开发
1. fork仓库,在自己的仓库中修改代码
2. 编译Agent并拷贝iast-core.jar、iast-inject.jar到临时目录中
3. 运行应用，测试代码（以SpringBoot应用为例）：`java -javaagent:/path/to/iast-home/release/iast-agent.jar -Ddebug=true -jar app.jar`
4. 针对bug，创建ISSUES、PR并关联ISSUES到PR上；针对feature，提交PR

请阅读完整的[贡献指南](CONTRIBUTING.md)，该包含参与贡献的方式、流程、格式、如何部署、哪里可以获取帮助等。

#### 支持的Java版本及中间件
- Java 1.6+
- Tomcat、Jetty、WebLogic、WebSphere、SpringBoot等主流软件和中间件

**notice:** `jdk 1.6`开发的Agent需要使用`Maven 3.2.5`进行构建
