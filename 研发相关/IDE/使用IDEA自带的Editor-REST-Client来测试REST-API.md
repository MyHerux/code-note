# 使用IDEA自带的 Editor REST Client 来测试 REST API

## Overview

开发 REST API 的时候，必然少不了测试。测试 API 可以采用以下方式：

- Chrome 请求

    不方便构造 POST 请求

- Postman 等 Post 工具ß

    需要下载工具，好处是可以记录请求，批量测试

- Swagger

    项目集成，方便操作，同时也提供给前端使用

- Editor REST Client

    IDEA自带，操作方便，不需要要切换到浏览器页面，所以开发的时候测试使用特别方便

## Editor REST Client

- 集成

    IDEA 2017.1 之后默认集成 Editor REST Client 插件，只需要启动项目即可。

    ![](http://cdn.heroxu.com/20180613152886003275704.png)

- 测试

    点击 `Run HTTP Request` 按钮即可开始测试，GET请求多一个按钮可以在浏览器中打开资源或页面。

    - POST请求

        ![](http://cdn.heroxu.com/20180613152885997427551.png)

    - GET请求

        ![](http://cdn.heroxu.com/20180613152886030584590.png)

- 配置

    点击 `Open in HTTP Request Editer` 进行配置

    ![](http://cdn.heroxu.com/20180613152886075540719.png)


## 官方描述

> https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html


