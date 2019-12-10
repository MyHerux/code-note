# Spring-cloud-feign添加统一的Header

## Overview

- [Spring Cloud OpenFeign Doc](https://cloud.spring.io/spring-cloud-openfeign/reference/html/)
- [Github](https://github.com/OpenFeign/feign)

## 业务需求

请求某些机密服务，需要做加密操作，所以需要添加统一的签名 `Header` 。

## Code

```
@Slf4j
@Component
public class testFeignInterceptor implements RequestInterceptor {

    public void apply(RequestTemplate requestTemplate) {
        String url = requestTemplate.url();
        // 只对特定url进行签名添加Header的操作
        if (!url.startsWith("/test/api")) {
            return;
        }
        String accessKeyId = "ak";
        String privateKey = "MI";

        String body = "";
        if (requestTemplate.body() != null) {
            body = new String(requestTemplate.body());
        }
        String contentSHA256 = getContentSHA256(body);
        String date = new Date().toString();
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String signature = getSignature(privateKey, requestTemplate.url(), requestTemplate.method(), contentSHA256, date, nonce);
        
        requestTemplate.header("Accept", "application/json");
        requestTemplate.header("Content-SHA256", contentSHA256);
        requestTemplate.header("Content-Type", "application/json");
        requestTemplate.header("Date", date);
        requestTemplate.header("x-signature-nonce", nonce);
        requestTemplate.header("Authorization", accessKeyId + ":" + signature);
    }
}
```

## 其他

过程中，需要打印请求和响应的详细信息

添加 `FeignConfiguration`：
```
@Configuration
public class FeignConfiguration {
    public static int connectTimeOutMillis = 30000;
    public static int readTimeOutMillis = 30000;

    @Bean
    public Request.Options options() {
        return new Request.Options(connectTimeOutMillis, readTimeOutMillis);
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        //这里记录所有，根据实际情况选择合适的日志level
        return Logger.Level.FULL;
    }
}
```

添加配置：
```
logging:
  level:
    com:
      test:
        client: debug
```