# 检测能力

#### 通用漏洞类型

| 漏洞名称                                                     | 漏洞等级 |
| ------------------------------------------------------------ | -------- |
| Sql 注入                                                     | 高危     |
| 文件包含                                                     | 高危     |
| ORM Injection                                                | 高危     |
| JNI 注入                                                     | 高危     |
| LDAP 注入                                                    | 高危     |
| NoSql 注入                                                   | 高危     |
| SMTP 注入                                                    | 高危     |
| XPath 注入                                                   | 高危     |
| 反射注入                                                     | 高危     |
| 表达式注入（EL Injection）                                   | 高危     |
| OGNL 注入                                                    | 高危     |
| JNDI 注入                                                    | 高危     |
| Groovy 注入                                                  | 高危     |
| 命令执行                                                     | 高危     |
| 不安全的反序列化                                             | 高危     |
| 服务器端请求伪造（SSRF）                                     | 高危     |
| 路径穿越（任意文件上传、任意文件读取、任意文件写入、任意文件删除等） | 高危     |
| 代码执行                                                     | 高危     |
| 硬编码检测                                                   | 高危     |
| 敏感信息泄漏                                                 | 高危     |
| 不安全的XML Decode                                           | 中危     |
| XXE                                                          | 中危     |
| Header 头注入                                                | 中危     |
| 反射型 XSS                                                   | 中危     |
| 存储型 XSS                                                   | 中危     |
| CORS misconfiguration                                        | 低危     |
| 数据明文传输                                                 | 低危     |
| 弱加密算法（weak cryptographic algorithms）                  | 低危     |
| 弱哈希算法                                                   | 低危     |
| 弱随机数算法                                                 | 低危     |
| 点击劫持                                                     | 低危     |
| 正则 Dos                                                     | 低危     |
| 不安全的 readline                                            | 低危     |
| 信任边界                                                     | 低危     |
| Cookie 未设置 Secure                                         | 提示     |
| 硬编码检测                                                   | 提示     |
| 手机号码泄露                                                 | 提示     |
| Response Without X-Content-Type-Options Header               | 提示     |
| Pages Without Anti-Clickjacking Controls                     | 提示     |
| Response With Insecurely Configured Strict-Transport-Security Header | 提示     |
| Response With X-XSS-Protection Disabled                      | 提示     |
| Response Without Content-Security-Policy Header              | 提示     |
| 不安全的转发                                                 | 低危     |
| 不安全的重定向                                               | 低危     |
| 不安全的readline                                             | 低危     |
| HQL注入                                                      | 高危     |

#### 跨请求漏洞

| 漏洞名称     | 漏洞等级 | 是否支持检测 |
| ------------ | -------- | ------------ |
| 漏洞链路追踪 | 高危     | 已支持       |
| 未授权访问   | 高危     |              |
| 水平越权     | 高危     |              |
| 垂直越权     | 高危     |              |

