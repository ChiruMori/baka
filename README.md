### BAKA
  * 始于 2020.05.01
  * 基础功能完成与 2020.05.07

### How To Use
  * 基本环境需求：JDK11+
  * 调整 config.properties 的配置，比如端口，也可以使用默认值
  * 在 user 包下实现自己的业务逻辑，并进行编译、运行即可
  * 更多信息见本文末尾

### 这是什么
  - 基于 Java nio 实现的简单 json 服务器程序
  - 部署运行方式与普通 Java 程序相同
  - 因实现比较简单，故取名 BAKA
  
  + 请勿在重要场景使用本程序（因为不保证程序健壮性）
  + 本程序仅可用于学习交流，如需交流或更多帮助，可发送邮件至 cxlm@cxlm.wrok
  
### 使用本程序的示例

  [简单的启动页: http://cxlm.work:8547](http://cxlm.work:8547/)
  
### 更多信息

#### 代码结构

##### src 源码包
+ org 处理 Json 的第三方源码，为保证程序易用性，未采用第三方 jar 包的方式
+ user 用户代码
  + controller 默认的 controller 类包，自己实现的 Controller 需要防止到该包中 DefaultController 提供了静态资源解析、index 解析的默认实现
  + start 该包下提供了 Process 类，用于运行一些需要在程序启动期间运行的代码，可以直接删除该类
+ work.cxlm 核心代码包，一般不建议用户修改，但是允许修改
  + anno 提供关键的两个注解：Controller Mapping 后文会单独解释
  + http 提供 HTTP 请求需要的类包，请求、响应、Session 等处理均在该包中
  + main 核心架构相关代码
    + Application: 程序入口，代码编译后，需要执行本类启动程序
    + ControllerLinker: Controller 责任连，复杂请求分发到用户代码并返回
    + MainReactor, SubReactor: 主从 Reactor, nio 核心处理，负责请求分发
  + util 提供配置、日志功能
  
##### test 代码包： 测试时使用，运行时直接删除即可（不删除也没影响）
##### README.md 本文件
##### todo.md 本程序开发进程记录

#### 注解使用

+ @Controller 

  被本注解注解的类如果在 config.properties 中指定的 controllers 路径下，则在程序启动时会被注册到 Controller 责任链中。
  
  本类用于处理用户请求的方法必须标注有 @Mapping 注解，并且为 public。程序将通过反射调用这些方法
  
  本类处理用户请求的方法，参数可以为 HttpRequest 或 HttpResponse 二者顺序无所谓
  
  本类的返回值仅支持 JSONObject, JSONArray, String, File 类型，如果为 File 则解析为文本文件，如果返回了不支持的类型，则丢弃返回值
  
+ @Mapping

  Mapping 支持两个值：url, method， url 指示绑定的 URL，使用正则表达式进行匹配， method 支持的值为 RequestType 中指示的值，如果不够用可进行拓展
  
  一个请求只有在同时匹配 Mapping 指示的两个值时才有可能被成功映射到对应方法。
  
### 打包指令

jar cvfm ../baka.jar MANIFEST.MF -C ./out/production/baka .
  