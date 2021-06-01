## DongTai-agent-java贡献指南

首先，感谢使用洞态IAST

洞态IAST是一款开源的**被动式IAST**，与OpenRASP的主动式IAST不同，洞态IAST根据**污点传播算法**检测漏洞，不需要重放HTTP请求。被动式IAST实时检测漏洞、不产生脏数据、支持分布式/微服务/验证码/一次性签名等场景下的漏洞检测，可与**DevOps**集成，将安全测试融入开发流程中。


## 环境设置
请先fork一份对应的仓库，不要直接在仓库下建分支。然后参考[官方文档]()搭建环境进行测试。

在决定提交issue或提交任何更改之前，请查一下项目的 [open issues](https://github.com/HXSecurity/DongTai-agent-java/issues?q=is%3Aopen)，避免重复。我们也准备了几个issue label来帮助大家筛选：
1. 如果想找一些适合上手的issue，可以看下 [`good first issue`](https://github.com/HXSecurity/DongTai-agent-java/labels/good%20first%20issue)有没有你感兴趣的
2. 如果想寻找更进阶的，可以看下 [`good intermediate issue`](https://github.com/HXSecurity/DongTai-agent-java/labels/good%20intermediate%20issue)
3. 如果想找些bug来fix，可以看下 [`bug`](https://github.com/HXSecurity/DongTai-agent-java/labels/bug)

## 贡献方式1 - 提交issue
贡献者提交issue时，需要考虑一下几种类型：
1. [`bug`](https://github.com/HXSecurity/DongTai-agent-java/labels/bug)
2. [`feature`](https://github.com/HXSecurity/DongTai-agent-java/labels/feature)
3. [`question`](https://github.com/HXSecurity/DongTai-agent-java/labels/question)
4. [`enhancement`](https://github.com/HXSecurity/DongTai-agent-java/labels/enhancement)

请首先检查下我们的 [open issues](https://github.com/HXSecurity/DongTai-agent-java/issues?q=is%3Aopen)，确保不要提交重复的issue。确认没有重复后，请选择上面类型之一的 label，并且按issue模板填好，尽可能详细的解释你的issue —— 原则是只看issue就可以很容易的看懂。

### Issue提交后会被如何处理？
我们项目的 maintainer 会监控所有的 issue，并做相应的处理：
1. 他们会再次确认新创建的 issue 是不是添加了上述四种 label 里正确的 label，如果不是的话他们会进行更新。
2. 他们同时也会决定是不是 accept issue，参见下一条。
3. 如果适用的话，他们可能会将以下四种新的 tag 加到 issue 上：
    1) [`duplicate`](https://github.com/HXSecurity/DongTai-agent-java/labels/duplicate): 重复的 issue
    2) [`wontfix`](https://github.com/HXSecurity/DongTai-agent-java/labels/wontfix)：决定不采取行动。maintainer 会说明不修复的具体原因，比如 work as intended, obsolete, infeasible, out of scope
    3) [`good first issue`](https://github.com/HXSecurity/DongTai-agent-java/labels/good%20first%20issue)：见上文，适合新人上手的 issue。
    4) [`good intermediate issue`](https://github.com/HXSecurity/DongTai-agent-java/labels/good%20intermediate%20issue): 见上文，比较 进阶的 issue，欢迎社区的贡献者来挑战。
4. issue 如果没有被关掉的话，现在就正式可以被认领（在issue上留言）了。
5. Maintainer 同时也会定期的检查和清理所有 issue，移除过期的 issue。

## 贡献方式2 - 提交代码
对于**任何**的代码改动，你都需要有相应的issue来跟踪：不管是先有的issue还是创建一个新的issue。

> 请在对应的issue下留言，表明你要 WORK ON 这个 issue，避免重复

### 代码改动
1. 你可以在对应的issue上简单描述下你的设计方案，收取反馈；当然如果你很自信改动非常简单直观并且你的改动基本不可能有什么问题的话，你也完全可以跳过这个步骤。
2. 做相应的改动
3. 遵循PR/Commit指导，提交PR，我们的 maintainer 会去 review。

## 贡献者资源

### 贡献者晋级

**贡献者** 提交有效ISSUE、通过的PR或在社区为用户解答问题均可成为贡献者

**维护者** 首先，需要是贡献者；其次，提交过重要的ISSUE/PR或其它突出贡献；然后，由现有的维护者和核心开发共同讨论，决定是否允许加入维护者团队

**核心成员** 核心成员需要是维护者，然后，对产品的发展有自己的想法和见解，可以提出关键性意见或开发相关功能；由现有核心成员共同讨论，决定是否允许加入核心团队。

## 贡献者
- [data54388](https://github.com/data54388)
- [tcsecchen](https://github.com/tcsecchen)
- [BiteFoo](https://github.com/BiteFoo)
