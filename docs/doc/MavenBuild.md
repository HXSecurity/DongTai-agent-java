# 构建部署

1. 确保你的计算机安装了 JDK8 和 maven

2. 克隆 [DongTai-agent-java](https://github.com/HXSecurity/DongTai-agent-java)

3. 修改配置文件`dongtai-agent/src/main/resources/iast.properties`，更改以下关于服务端配置：

   ```
   iast.server.url=
   iast.server.token=
   ```

4. 在项目根目录执行：

   ```
   mvn clean package -Dmaven.test.skip=true
   ```

5. 在项目根目录会生成文件夹 `release`：

   ```
   release
   ├── dongtai-agent.jar
   └── lib
       ├── dongtai-api.jar
       ├── dongtai-core.jar
       ├── dongtai-log.jar
       ├── dongtai-spring-api.jar
       └── dongtai-spy.jar
   ```

6. 将 `release/lib`目录下的所有`.jar`文件放入系统临时目录

7. 启动前添加参数 `-Ddongtai.debug=true`，例：

   ```
   java -javaagent:/path/to/dongtai-agent.jar -Ddongtai.debug=true -jar app.jar
   ```

