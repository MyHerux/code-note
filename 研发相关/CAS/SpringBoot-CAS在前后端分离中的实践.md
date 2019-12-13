## Cas

CAS is an open and well-documented authentication protocol. The primary implementation of the protocol is an open-source Java server component by the same name hosted here, with support for a plethora of additional authentication protocols and features.

项目地址：

[https://github.com/apereo/cas](https://github.com/apereo/cas)

协议过程：

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9jZG4uaGVyb3h1LmNvbS8yMDE5MDQyNDE1NTYwOTA0NzcyMjIyOS5wbmc?x-oss-process=image/format,png)

## Cas-server

直接使用template搭建一个Server：
[https://github.com/apereo/cas-overlay-template](https://github.com/apereo/cas-overlay-template)

## 在SpringBoot中集成Cas-client

依赖：

```
<dependency>
    <groupId>net.unicon.cas</groupId>
    <artifactId>cas-client-autoconfig-support</artifactId>
    <version>1.7.0-GA</version>
</dependency>
```

application.yml：

```
cas:
  server-url-prefix: http://server-url
  server-login-url: http://server-url/login
  client-host-url: http://localhost:9100
  validation-type: cas
  authentication-url-patterns:

server:
  port: 9100
```

[项目地址](https://github.com/MyHerux/cases/tree/master/cases-cas)

## Cas在前后端分离项目中遇到的问题

对于访问后端的请求分为两种：

- HTTP请求

  像浏览器地址栏发起的请求、浏览器自发的访问某个网址、Postman测试接口，这些行为其实都是发起的HTTP请求，不会有跨域问题。最开始后端测试的时候发起的都是此类请求，所以没有问题，结果前后端联调时就出现了问题。

- AJAX(XMLHttpRequest)请求

  这是浏览器内部的 `XMLHttpRequest` 对象发起的请求，浏览器会禁止其发起跨域的请求，主要是为了防止跨站脚本伪造的攻击(`CSRF`)。

所以即使已经登录（浏览器有对应的 `cookie` ），前后端分离时（ `cookie` 的 `domain` 不一致），前端发起的所有请求都会收到 `302` 错误码。

## 前后端分离中的实践-Nginx代理

既然是因为 `cookie` 的 `domain` 不一致才导致的 `302` ，直接让 `domain` 一致不就可以了么，首先想到的就是使用 `Nginx` 。

`nginx.conf` 配置如下：

```
#user  nobody;
worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  1024;
}


http {
    include       mime.types;
    default_type  application/octet-stream;

    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;

    server {
        listen       8070;
        server_name  [server-ip];

        location /api/ {
                 proxy_set_header Host $host;
                 proxy_set_header X-Real-IP $remote_addr;
                 proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                 proxy_pass http://[server-ip]:8080/api/;
        }
        location / {
                 proxy_set_header Host $host;
                 proxy_set_header X-Real-IP $remote_addr;
                 proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                 proxy_pass  http://[front-ip]:8088/;
        }
    }

    include servers/*;
}
```

如配置所示，统一通过入口 `http://[server-ip]:8070` 进入，然后请求会转发到 `http://[front-ip]:8088/`（即前端页面），`http://[front-ip]:8088/`（前端页面）里面的所有请求指向 `http://[server-ip]:8070/api`，这些请求会再被转发到 `http://[server-ip]:8080/api/` （`注意端口的变化`）

## 前后端分离中的实践-Token化

使用 `Token` 来鉴权当然是前后端分离的最佳选择，但是老旧系统里面的 `cas` 授权又不能去掉，所以只能让新系统里面的 `cas` 来转换为 `token` 。