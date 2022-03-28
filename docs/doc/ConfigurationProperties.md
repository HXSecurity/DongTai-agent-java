# 配置参数

## 手动安装

| Property name             | Description                                               | Value Type      | Default               |
| ------------------------- | :-------------------------------------------------------- | --------------- | --------------------- |
| `dongtai.app.name`        | 设置项目名称                                              | String          | Demo proJect          |
| `dongtai.app.version`     | 设置项目版本                                              | String          | V1.0                  |
| `dongtai.app.create`      | 设置是否自动创建项目                                      | Boolean         | false                 |
| `dongtai.debug`           | 开启后加载本地系统临时目录中的检测引擎                    | Boolean         | false                 |
| `iast.server.mode`        | local模式支持、POST请求包展示、污点位置及污点值展示等功能 | local \| remote | local                 |
| `iast.proxy.enable`       | HTTP代理模式是否启用                                      | Boolean         | false                 |
| `iast.proxy.host`         | HTTP 代理的域名 (IP)                                      | String          | null                  |
| `iast.proxy.port`         | HTTP 代理的端口                                           | String          | 80                    |
| `iast.engine.delay.time`  | 延迟启动功能，单位：秒                                    | Integer         | 0                     |
| `iast.dump.class.enable`  | 是否 dump 修改后的字节码                                  | Boolean         | false \| true         |
| `iast.dump.class.path`    | dump 字节码的路径                                         | Boolean         | /tmp/iast-class-dump/ |
| `dongtai.server.url`      | Dongtai OpenAPI Url                                       | String          |                       |
| `dongtai.server.token`    | Dongtai OpenAPI Token                                     | String          |                       |
| `dongtai.response.length` | 向 Dongtai OpenAPI 发送的响应体长度                       | Integer         | null                  |
| `dongtai.log`             | 是否把日志输出到本地文件                                  | Boolean         | true                  |
| `dongtai.log.path`        | 指定日志文件所在目录                                      | String          | agent.jar 当前目录    |
| `dongtai.log.level`       | 指定日志等级                                              | String          | info                  |
| `dongtai.server.package`       | 指定是否从洞态Server端下载agent依赖包           | boolean    |`true`, `false`      | true              |



## 自动安装

| Property name    | Description                            | Value Type | Optional             |
| ---------------- | -------------------------------------- | ---------- | -------------------- |
| `app_name`       | 设置项目名称                           | String     | proJect name         |
| `app_create`     | 设置是否自动创建项目                   | Boolean    | true \| false        |
| `app_version`    | 设置项目版本                           | String     | V1.0                 |
| `debug`          | 开启后加载本地系统临时目录中的检测引擎 | Boolean    | true \| false        |
| `dongtai_server` | Dongtai OpenAPI Url                    | String     |                      |
| `dongtai_token`  | Dongtai OpenAPI Token                  | String     |                      |
| `mode`           | agent 加载/卸载                        | String     | install \| uninstall |
| `pid`            | 应用程序进程 ID                        | String     |                      |
