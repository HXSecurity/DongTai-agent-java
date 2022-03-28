# Agent 调试

1. 构建好即将调试的 Agent，构建方式请查看 [Agent 构建]()
2. 使用 IDE 打开 Agent 代码，推荐 IntelliJ IDEA

3. 使用 Remote JVM Debug 功能

   ![springtest_config](/Users/erzhuangniu/Documents/png/springtest_config.png)

4. 进行配置

   ![remote_debug](/Users/erzhuangniu/Documents/png/remote_debug.png)

   参数：

   ```
   Host：运行该项目的远程IP
   Port：远程 IP 的端口
   Command：远程主机在启动 Java 应用时需要添加的参数
   ```

5. 配置应用的启动命令

   ```
   java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -javaagent:/path/to/dongtai-agent.jar -Ddongtai.debug=true -jar app.jar
   ```

   - -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005：JVM Remote Debug 参数
   - -javaagent:/path/to/agent.jar：被远程 Debug 的DongTAi-iast-agent
   - -Ddongtai.debug=true：使用本地的 agent 包
   - app.jar 使用 agent 启动的 JAVA 应用

6. 返回 IDEA 界面，点击 debug 启动标志开始调试 Agent