# SpringBoot 2.x 中使用 Actuator 来做应用监控

## Actuator

[Spring-boot-actuator](https://github.com/spring-projects/spring-boot/tree/v2.0.2.RELEASE/spring-boot-project/spring-boot-actuator) module 可帮助您在将应用程序投入生产时监视和管理应用程序。您可以选择使用 HTTP 端点或 JMX 来管理和监控您的应用程序。Auditing, health, and metrics gathering 也可以自动应用于您的应用程序。

- 添加依赖，开启监控

    ```
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
    ```
- 特性

    - Endpoints

        `Actuator endpoints` 允许你去监控和操作你的应用。SpringBoot包含了许多内置的端点，当然你也可以添加自己的端点。比如 `health` 端点就提供了基本的应用健康信息。

    - Metrics

        `Spring Boot Actuator` 提供 `dimensional metrics` 通过集成 [Micrometer](https://micrometer.io/).

    - Audit

        `Spring Boot Actuator` 有一套灵活的审计框架会发布事件到 `AuditEventRepository`。

- 2.0 更新

    - 基础路径更新

        基础路径由 `/` 调整到 `/actuator` 下

    - 启动端点 endpoint

        默认只启动了 `health` 和 `info` 端点，可以通过 `application.yml` 配置修改：

        ```
        management:
            endpoints:
                web:
                exposure:
                    include: health,info,env,metrics
        ```

        项目启动时可以看到暴露出来的接口信息：

        ![](http://of0qa2hzs.bkt.clouddn.com/2018060715283596955543.png)

- 主要的端点

    HTTP方法|	路径|	描述|	鉴权
    - | :-|:-|-:
    GET	|/autoconfig|	查看自动配置的使用情况|	true
    GET	|/configprops|	查看配置属性，包括默认配置|	true
    GET	|/beans|	查看bean及其关系列表	|true
    GET	|/dump|	打印线程栈	|true
    GET	|/env	|查看所有环境变量	|true
    GET	|/env/{name}|	查看具体变量值	|true
    GET	|/health	|查看应用健康指标	|false
    GET	|/info	|查看应用信息	|false
    GET	|/mappings|	查看所有url映射	|true
    GET	|/metrics	|查看应用基本指标|	true
    GET	|/metrics/{name}|	查看具体指标|	true
    POST |	/shutdown	|关闭应用	|true
    GET	|/trace	|查看基本追踪信息	|true

- 通过web访问暴露的端点

    - http://localhost:8077/actuator/metrics

        ```
        {"names":["jvm.memory.max","process.files.max","jvm.gc.memory.promoted","tomcat.cache.hit","system.load.average.1m","tomcat.cache.access","jvm.memory.used","jvm.gc.max.data.size","jvm.memory.committed","system.cpu.count","logback.events","tomcat.global.sent","jvm.buffer.memory.used","tomcat.sessions.created","jvm.threads.daemon","system.cpu.usage","jvm.gc.memory.allocated","tomcat.global.request.max","tomcat.global.request","tomcat.sessions.expired","jvm.threads.live","jvm.threads.peak","tomcat.global.received","process.uptime","tomcat.sessions.rejected","process.cpu.usage","tomcat.threads.config.max","jvm.classes.loaded","jvm.gc.pause","jvm.classes.unloaded","tomcat.global.error","tomcat.sessions.active.current","tomcat.sessions.alive.max","jvm.gc.live.data.size","tomcat.servlet.request.max","tomcat.threads.current","tomcat.servlet.request","process.files.open","jvm.buffer.count","jvm.buffer.total.capacity","tomcat.sessions.active.max","tomcat.threads.busy","process.start.time","tomcat.servlet.error"]}
        ```

    - http://localhost:8077/actuator/metrics/jvm.memory.max

        ```
        {"name":"jvm.memory.max","measurements":[{"statistic":"VALUE","value":3.455057919E9}],"availableTags":[{"tag":"area","values":["heap","nonheap"]},{"tag":"id","values":["Compressed Class Space","PS Survivor Space","PS Old Gen","Metaspace","PS Eden Space","Code Cache"]}]}
        ```

## Micrometer

`Springboot2` 在 `spring-boot-actuator` 中引入了 `micrometer` ，对 `1.x` 的 `metrics` 进行了重构，另外支持对接的监控系统也更加丰富( `Atlas、Datadog、Ganglia、Graphite、Influx、JMX、NewRelic、Prometheus、SignalFx、StatsD、Wavefront` )。

### Prometheus

[SpringBoot2.x中的应用监控：Actuator+Prometheus+Grafana](https://blog.csdn.net/myherux/article/details/80667524)

### Influx

- 添加依赖

    ```
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-influx</artifactId>
    </dependency>
    ```

