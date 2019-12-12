# 从0开始服务化-0-调用链-Zipkin

## Zipkin

`Zipkin` 是一个分布式跟踪系统，用于收集、管理和查找跟踪数据。它可以把分布式链路调用的顺序串起来，并计算链路中每个 `RPC` 调用的耗时，可以很直观的看出在整个调用链路中延迟问题。 `Zipkin` 的设计基于 `GoogleDapper` 论文实现的。

`ZipkinServer` 提供了 `UI` 操作，可以非常方便地查看和搜索跟踪数据，直观的查看到链调用依赖关系。

该项目包括一个无依赖库和一个 `spring-boot` 服务器。存储支持包括内存， `JDBC（mysql）`， `Cassandra` 和 `Elasticsearch` 。

在没有使用外部存储时，则默认使用内存存储数据，内存数据是有限且不可持久化的，所以建议使用外部存储，因日志数据通常很大，为了搜索日志的效率，所以建议使用 `Elasticsearch` 。

Zipkin 的基础架构由 4 个核心组件构成：

- `Collector`：收集器组件，处理从外部系统发过来的跟踪信息，将这些信息转换为 Zipkin 内部处理的 Span 格式，以支持后续的存储、分析、展示等功能。
- `Stroage`：存储组件，主要处理收集器收到的跟踪信息，默认存储在内存中，也可通过 ES 或 JDBC 来存储。
- `Restful API`：API 组件，提供外部访问接口。
- `Web UI`：UI组件，基于 API 组件实现的 Web 控制台，用户可以很方便直观地查询、搜索和分析跟踪信息。

## 部署测试服务

使用 `docker` 部署本地 `zipkin` 的 `server` 端

```
docker run -d -p 9411:9411 \
--name zipkin \
docker.io/openzipkin/zipkin
```

![20191211171340](http://cdn.heroxu.com/20191211171340.png)

访问：`http://localhost:9411/zipkin/`

![20191211171424](http://cdn.heroxu.com/20191211171424.png)

## 构建测试项目

- Service A

    ```
    server:
        port: 8081

    spring:
        application:
            name: server-a
        sleuth:
            web:
            client:
                enabled: true
            sampler:
            probability: 1.0 # 采用比例，默认 0.1 全部采样 1.0
        zipkin:
            base-url: http://localhost:9411/ # 指定了Zipkin服务器的地址
    ```

    ```
    @Slf4j
    @RestController
    public class ServiceAController {

        @Resource
        private RestTemplate restTemplate;

        @GetMapping(value = "/servicea")
        public String servicea() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("This is service a!");
            return restTemplate.getForObject("http://localhost:8082/serviceb", String.class);
        }
    }
    ```

- Service B

    ```
    server:
       port: 8082

    spring:
        application:
            name: server-b
        sleuth:
            web:
            client:
                enabled: true
            sampler:
            probability: 1.0 # 采用比例，默认 0.1 全部采样 1.0
        zipkin:
            base-url: http://localhost:9411/ # 指定了Zipkin服务器的地址
    ```

    ```
    @Slf4j
    @RestController
    public class ServiceBController {

        @Autowired
        private RestTemplate restTemplate;

        @GetMapping(value = "/serviceb")
        public String serviceb() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("This is service b!");
            return restTemplate.getForObject("http://localhost:8083/servicec", String.class);
        }
    }
    ```

- Service C

    ```
    server:
        port: 8083

    spring:
        application:
            name: server-c
        sleuth:
            web:
            client:
                enabled: true
            sampler:
            probability: 1.0 # 采用比例，默认 0.1 全部采样 1.0
        zipkin:
            base-url: http://localhost:9411/ # 指定了Zipkin服务器的地址
    ```

    ```
    @Slf4j
    @RestController
    public class ServiceCController {

        @Resource
        private RestTemplate restTemplate;

        @GetMapping(value = "/servicec")
        public String servicec() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("This is service c!");
            return "hello,this is server c!";
        }
    }
    ```

## 测试服务调用链-Sleuth

`Zipkin` 的依赖中包含了 `Sleuth`，`Sleuth` 功能：

- 将 `SpanID` 和 `TraceID` 添加到 `Slf4JMDC` 中，这样可以在日志聚合器中根据 `SpanID` 和 `TraceID` 提取日志。
- 提供对常见分布式跟踪数据模型的抽象：`traces(跟踪)`， `spans(形成DAG(有向无环图))`，注释， `key-value` 注释。松散地基于 `HTrace` ，但兼容 `Zipkin（Dapper）`。
- `Sleuth` 常见的入口和出口点来自 `Spring` 应用（`Servlet` 过滤器、`Rest Template`、`Scheduled Actions`、消息通道、`Zuul Filter、`Feign Client`）。
- 如果 `spring-cloud-sleuth-zipkin` 可用，`Sleuth` 将通过 `HTTP` 生成并收集与 `Zipkin` 兼容的跟踪。默认情况下，将跟踪数据发送到 `localhost`(端口：9411)上的 `Zipkin` 收集服务应用，可使用 `spring.zipkin.baseUrl` 修改服务器地址。

启动3个服务，访问：`http://localhost:8081/servicea`，可以看到3个服务的日志输出如下：

![20191211173729](http://cdn.heroxu.com/20191211173729.png)

![20191211173754](http://cdn.heroxu.com/20191211173754.png)

![20191211173809](http://cdn.heroxu.com/20191211173809.png)

在输出的日志中，多了些内容，这些内容就是由 `sleuth` 为服务调用提供的链路信息
可以看到内容组成：`[appname,traceId,spanId,exportable]`，具体含义如下：

- `appname`：服务的名称，即 spring.application.name 的值。
- `traceId`：整个请求链路的唯一ID。
- `spanId`：基本的工作单元，一个 RPC 调用就是一个新的 span。启动跟踪的初始 span 称为 root span ，此 spanId 的值与 traceId 的值相同。见上面示例消费者服务日志输出。
- `exportable`：是否将数据导入到 Zipkin 中，true 表示导入成功，false 表示导入失败。

## Zipkin Server 

再次访问：`http://localhost:9411/zipkin/`，点击查询，可以看到刚才执行的服务调用链：

![20191211174929](http://cdn.heroxu.com/20191211174929.png)

点击可以看到具体的调用时间与链路：

![20191211175049](http://cdn.heroxu.com/20191211175049.png)

点击具体的服务，可以看到服务调用的详情以及父级子级 `trace`

![20191211175127](http://cdn.heroxu.com/20191211175127.png)