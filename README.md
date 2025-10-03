# MSC Web框架
用于快速构建简单Web服务器应用。设计思想是Model-Static-Controller。
# 简介
一个轻量简便的Web框架，类似封装了SpringMVC的SpringBoot。但仅聚焦在Web上，更适合平常自己写简单Web。

学了SpringMVC，之前玩过Vue不想搞模板引擎了。拥抱大前端时代的 MSC应运而生！

MSC遵循轻量框架的核心原则：

1. 零配置启动：开发者无需手动配置路由、静态资源路径，框架默认约定路径规则。
2. 插件化核心：将请求处理、路由映射、响应转换等拆分为独立组件，便于扩展。
3. 开发者友好：提供简洁注解（如@Api、@Get），降低学习成本。
4. 适配单页应用：原生支持 SPA 静态资源（Vue/React 打包文件）和前端路由（history 模式）。

更适合个人学习和小项目的Web框架。

# 执行流程
一个简化版的MVC：

客户端请求->MscServlet->Dispatcher分发->Router路由匹配->StaticHandler静态资源查找。

Dispatcher分发请求时会解析请求的参数。支持
1. @Parma 使用Get参数绑定到方法。
2. 当Post_Handler只有一个参数时，请求体Json转Java对象。