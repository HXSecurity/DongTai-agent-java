## DongTai-agent-java

[![django-project](https://img.shields.io/badge/django%20versions-3.0.3-blue)](https://www.djangoproject.com/)
[![dongtai-project](https://img.shields.io/badge/dongtai%20versions-beta-green)](https://github.com/huoxianclub/dongtai)
[![dongtai--agent--java](https://img.shields.io/badge/dongtai--agent--java-v1.0.0-lightgrey)](https://github.com/huoxianclub/dongtai-web)
[![license GPL-3.0](https://img.shields.io/github/license/HXSecurity/DongTai-agent-java)](https://github.com/HXSecurity/DongTai-agent-java/blob/main/LICENSE)

- [中文版本(Chinese version)](README_CN.md)

## Project Introduction

Dongtai-agent-java is DongTai Iast's data acquisition tool for Java applications. In a Java application with the iast agent added, the required data is collected by rewriting class bytecode, and then the data is sent to dongtai-OpenAPI service, and then the cloud engine processes the data to determine whether there are security holes.

Dongtai-agent-java consists of `agent.jar`, `iast-core-jar`, `iast-inject. Jar` and `dongtai-servlet.jar`:

- `agent.jar` It is used to manage agent life cycle and configuration. The life cycle of the Agent includes downloading, installing, starting, stopping, restarting, and uninstalling the agent. Agent configuration includes application startup mode, vulnerability verification mode, whether to enable agent, etc.
- `iast-core.jar ` The main functions of iast-core.jar are: bytecode piling, data collection, data preprocessing, data reporting, third-party component management, etc.
- `iast-inject.jar` It is used to inject into the BootStrap ClassLoader. The data collection method in 'iast-core.jar' is then invoked in the target application.
- `dongtai-servlet.jar` It is used to obtain the requests sent by the application and the responses received. It is used for data display and request replay.

## Application Scenarios

- DevOps
- Security test the application before it goes online
- Third-party Component Management
- Code audit
- 0 Day digging

## Quick Start

Please refer to the [Quick Start](https://hxsecurity.github.io/DongTai-Doc/#/doc/tutorial/quickstart).

## Quick Development

1. Fork the [DongTai-agent-java](https://github.com/HXSecurity/DongTai-agent-java) , clone your fork:

   ```
   git clone https://github.com/<your-username>/DongTai-agent-java
   ```

2. Write code to your needs.

3. Compile Dongtai-agent-Java using Maven:

   ```
   mvn clean package -Dmaven.test.skip=true
   ```

    - notice: JDK version is 1.8.

4. folder `./release` is generated in the project root directory after compilation:

   ```
   release
   ├── iast-agent.jar
   └── lib
       ├── dongtai-servlet.jar
       ├── iast-core.jar
       └── iast-inject.jar
   ```

5. Copy `iast-core.jar`、`iast-inject.jar`、`dongtai-servlet.jar` to the system temporary directory. Get the system temporary directory to run the following Java code:

   ```
   System.getProperty("java.io.tmpdir");
   ```

6. Run the application and test the code (for example, SpringBoot) : `java -javaagent:/path/to/iast-agent.jar -Ddebug=true -jar app.jar`

7. Contribute code. If you want to contribute code to the DongTai IAST team, please read the full [contribution guide](https://github.com/HXSecurity/DongTai/blob/main/CONTRIBUTING.md).

### Supported Java versions and middleware

- Java 1.6+
- Tomcat, Jetty, WebLogic, WebSphere, SpringBoot and Mainstream software and middleware.

<img src="https://static.scarf.sh/a.png?x-pxid=0c73ae79-fd43-46b9-a449-b8fcc259db85" />
