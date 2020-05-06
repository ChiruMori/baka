## TODO List
* Session, Cookie 支持
* 静态文件服务
* 支持动态代码
* 多半不会支持数据存储的解析，将要求用户自己写、执行 sql并处理结果。
* 包含文件的 HTTP 请求

## DONE
* 2020.05.03 配置解析、自动化测试工具
* 2020.05.05 NIO 请求响应框架搭建完成
* 2020.05.05 解析 HTTP 请求（HttpRequest, HttpResponse）
* 2020.05.05 直接将部分 org.json 的代码放到源码中实现 json 数据解析
* 2020.05.06 URL 映射（正则表达式）

## BUG List
* 暂未发现

## Fixed Bugs
* 请求丢失，MainReactor 分发后 SubReactor 接收不到
  + Debug 耗时：约 10 个小时
  + 浏览器发送空请求，这种空请求在浏览器的 Network 中无法捕获，可能可以通过抓包分析其存在
  + 上述的空请求导致 SubReactor 内逻辑抛出 NullPointerException ，由于捕获处理不当导致所在线程死亡，无法处理请求
  + 主从 Reactor 并发控制不当，导致 SubReactor 在不该阻塞的位置阻塞