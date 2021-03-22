## 洞态IAST
> 原"灵芝IAST"，后更名为"洞态IAST"，产品改为SaaS版，agent端采集与污点相关的数据并发送至服务器端，服务器端接收数据并重构形成污点方法图，再根据深度优先算法搜索污点调用链

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
- Tomcat 8、Tomcat 9，其他版本及中间件待测试

### 覆盖漏洞列表
- [x] Sql注入
- [x] CMDI命令执行
- [x] LDAP注入
- [x] XPATH注入
- [x] SMTP注入
- [x] 路径穿越
- [x] 弱随机数算法
- [x] 弱加密算法
- [x] 弱哈希算法
- [x] 信任边界
- [x] 动态导入JNI包
- [x] XXE
- [x] SSRF
- [x] EL表达式注入(JSP EL、Spel、OGNL大部分用法覆盖，后续遇到再补充) 9.21-9.22
- [x] 不安全的XML Decode(覆盖JDK自带的XML Decode包) 9.23
- [x] 反射注入（未找到合适的判断filter点方法，后续提供自定义filter点进行处理）9.24
- [x] 不安全的JSON反序列化（ ObjectInputStream.readObject() ） -  9.25
- [x] 未验证的重定向 - 9.25
- [x] 未验证的跳转 - 9.25
- [x] HQL注入
- [x] Header头注入
- [ ] 越权漏洞（未授权访问、水平越权、垂直越权）（
- [ ] NoSql注入
- [ ] CSRF
- [ ] Spring AutoBind
- [ ] 模板逃逸 - 明天（未查找到相关资料，暂时搁置）
- [ ] 跨域策略文件配置不当(crossdomain.xml)
- [ ] CSP头配置不当
- [ ] 不安全的认证协议(Basic、Digest、Authorization)
- [ ] JSP访问
- [ ] HTTP参数污染
- [ ] 正则Dos
- [ ] session重写
- [ ] session超时
- [ ] 不安全的socket工厂
- [ ] HTTP方法篡改
- [ ] 不安全的viewstate
- [ ] 点击劫持(HTTP头x-frame-options缺失)
- [ ] strict-transport-security配置不当
- [ ] x-content-type-options缺失
- [ ] x-xss-protection缺失
- [ ] cache-controls缺失
- [ ] Agent端重放，IAST中心端检测
- [*] 登陆接口识别
- [*] 用户识别及用户与cookie绑定
- [*] Cookie管理（保证Cookie的生命周期）

## TODO
- [x] HQL注入漏洞分析与漏洞覆盖度检查
- [x] NoSql注入漏洞分析与漏洞覆盖度检查
- [x] 模板逃逸漏洞分析与漏洞覆盖度检查
- [x] 反射注入漏洞分析与漏洞覆盖度检查
- [x] CSRF、任意跳转漏洞覆盖度检查
- [x] 整体漏洞复测、自动化打包测试（避免每次发版都手工测试）
- [x] 心跳、漏洞数据的上报采用事件机制，避免上报线程代码的损耗
- [x] V0.5.0版本发布
- [x] agent使用jdk 1.6重新编译打包
- [x] hook点（agent）待优化：走统一入口
- [x] SSRF漏洞分析与覆盖度检查确认
- [x] EL表达式注入漏洞分析与漏洞覆盖度检查
- [x] 不安全的XML Decode漏洞分析与漏洞覆盖度检查
- [x] Logback配置文件与目标应用的logback.xml混淆，导致日志出现混乱
- [ ] hook点处理（agent）待优化：增加hook数据的缓存
- [ ] 日志（业务）
- [x] 数据发送至DC（业务）
- [x] 硬编码问题抽到配置文件
- [x] API接口（下载核心jar包、下载配置、鉴权接口、上传心跳数据、上传第三方组件数据、上传漏洞数据） - 鉴权、统一权限认证体系
- [x] HOOK点动态配置，包括：source点、sink点、filter点的动态配置
- [x] 优化代码逻辑，减少不必要的调用
- [x] Agent抽取配置文件，配置文件支持热更新
- [x] Agent的混淆
- [x] 收集第三方组件、技术栈的数据
- [x] 第三方组件和技术栈对接漏洞库
- [x] 水平越权demo验证
- [x] BUG：MySql注入点bug修复（解决人人信体验测试问题）
- [x] 数据获取完整度（应用名称、应用路径、环境变量、）2020-11-06
- [x] BUG：path variable未检测到（2020-11-09）
- [x] 优化：发现调用ClassReader的accept方法过早，导致字节码修改时间过长 2020-11-10
- [x] BUG：HOOK点继承关系的处理逻辑拆分（2020-11-10）
- [x] BUG：File相关漏洞无法检出（hook点继承关系：类、类及子类 、子类拆分后，部分配置需要调整继承关系来适配）（2020-11-11）
- [x] 优化：source点返回值如果为空则忽略，减小污点池及污点方法的数量，提高检测速度
- [x] BUG：高版本ASM可以适配高版本JDK，但是高版本ASM计算堆栈大小时存在bug，后续分析解决 （再次测试，发现该问题已不存在🤔）
- [x] BUG：跟进debug模式可转换Request，非debug模式转换失败(2020-11-11)
- [x] BUG：数据获取到但数据之间无法关联起来，排查是否与JDK中stream接口的函数式编程有关？（map数据通过forEach进行传递）(2020-11-12)
- [x] BUG：post请求数据获取未捕获到
    - [x] POST数据，绑定为JDK内置的引用数据类型，无法hook source点 （已解决，通过增加spring框架的特定source点解决）
    - [ ] POST数据，绑定为应用内部自定义类，无法获取数据的传播情况（暂不支持，后续出现该部分漏洞时，补充hook点，解决性能问题进行支持）
        - 作为对象传入ORM，整体处理
        - 获取对象的某个字段（hook getXxx方法解决）
        - 调用对象的某个方法获取数据
- [ ] BUG：Attach时，无法attach成功（来自客户akulaku）
- [*] BUG：SCA相关问题，如何更快速的解决间接依赖的问题？
- [ ] BUG：logback包重写命名空间后，导致日志报错（不重写命名空间，直接引用）；直接引用导致用户的日志与灵芝的日志冲突，考虑自己实现日志模块
- [ ] BUG：越权漏洞检测
- [ ] feature：由于JDK 1.8增加类接口的默认方法，所以不能直接屏蔽接口，如何快速找到接口是否存在默认方法进行屏蔽？
- [*] feature：水平越权检测正式功能实现
- [ ] Hook点洞态添加

核心问题：
- [x] 字节码修改（source点、传播节点、filter点、sink点）
- [x] SCA
- [x] 中间件识别
- [x] 开发技术识别
- [x] 类加载
- [x] source点数据源获取完整度排查（排查完之后，source点相关内容不再调整，主要是流）
- [x] 常规漏洞检出率排查（2020-11-16 增加Spring系列的source点后，漏洞检出正常）
- [*] 越权漏洞检测在不同JDBC中的实现
- [ ] 重新分析不同JDK中hook点的继承关系配置，解决接口默认方法带来的冲突

**notice:** `jdk 1.6`开发的Agent需要使用`Maven 3.2.5`进行构建，Maven3.5的位置：/usr/local/maven/apache-maven-3.2.5


##### 靶场漏洞复现/问题排查流程
1.还原漏洞环境；
2.查找/分析漏洞产生位置，
3.debug漏洞代码，增加断点，分析漏报原因；
4.解决漏报