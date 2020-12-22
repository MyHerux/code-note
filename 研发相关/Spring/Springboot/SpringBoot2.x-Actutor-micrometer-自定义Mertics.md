# SpringBoot2.x-Actutor-micrometer-自定义Mertics

## 前言

- [SpringBoot2.x使用Actuator来做应用监控](https://blog.csdn.net/MyHerux/article/details/80670557)

## 示例

- 注册 Metrics

    实现 `MeterBinder` 接口的 `bindTo` 方法，将要采集的指标注册到 `MeterRegistry`

    ```
    @Component
    public class JobMetrics implements MeterBinder {


        public Counter job1Counter;

        public Counter job2Counter;

        public Map<String, Double> map;

        JobMetrics() {
            map = new HashMap<>();
        }

        @Override
        public void bindTo(MeterRegistry meterRegistry) {
            this.job1Counter = Counter.builder("my_job")
                    .tags(new String[]{"name", "job1"})
                    .description("Job 1 execute count").register(meterRegistry);

            this.job2Counter = Counter.builder("my_job")
                    .tags(new String[]{"name", "job2"})
                    .description("Job 2 execute count").register(meterRegistry);

            Gauge.builder("my_job_gauge", map, x -> x.get("x"))
                    .tags("name", "job1")
                    .description("")
                    .register(meterRegistry);

        }

    }

    ```

- 更新 Metrics

    示例采集了 `job` 执行的一些信息。

    ```
    @Slf4j
    @Component
    public class MyJob {

        private Integer count1 = 0;

        private Integer count2 = 0;

        @Autowired
        private JobMetrics jobMetrics;

        @Async("main")
        @Scheduled(fixedDelay = 1000)
        public void doSomething() {
            count1++;
            jobMetrics.job1Counter.increment();
            jobMetrics.map.put("x", Double.valueOf(count1));
            System.out.println("task1 count:" + count1);
        }

        @Async
        @Scheduled(fixedDelay = 10000)
        public void doSomethingOther() {
            count2++;
            jobMetrics.job2Counter.increment();
            System.out.println("task2 count:" + count2);
        }
    }
    ```

## 自定义 Metrics 指标

- `Counter:` 只增不减的计数器

    计数器可以用于记录只会增加不会减少的指标类型，比如记录应用请求的总量(http_requests_total)。

    ```
    this.job1Counter = Counter.builder("my_job") //指定指标的名称
                    .tags(new String[]{"name", "job1"}) // 指定相同指标的不同tag
                    .description("Job 1 execute count").register(meterRegistry);
    ```

    ```
    Counter.increment()  // 每次增加1
    Counter.increment(5D) // 每次增加指定数目
    ```

    访问 `http://localhost:8080//actuator/metrics/` 可以看到新增了我们添加的指标名称 `my_job`

    ![](http://cdn.heroxu.com/20180817153449776998953.png)

    进入下一级目录：`http://localhost:8080//actuator/metrics/my_job` ，可以看到总的执行次数是 `602` ，有两种不同的 `tag`

    ```
    {"name":"my_job","measurements":[{"statistic":"COUNT","value":602.0}],"availableTags":[{"tag":"name","values":["job2","job1"]}]}
    ```

    查看 `tag:job1` 的详情：`http://localhost:8080//actuator/metrics/my_job?tag=name:job1` 

    ```
    {"name":"my_job","measurements":[{"statistic":"COUNT","value":130.0}],"availableTags":[]}
    ```

- `Gauge:` 可增可减的仪表盘

    对于这类可增可减的指标，可以用于反应应用的 `当前状态`，比如主机当前空闲的内存大小(node_memory_MemFree)

    ```
    Gauge.builder("my_job_gauge", map, x -> x.get("x"))
                    .tags("name", "job1")
                    .description("")
                    .register(meterRegistry);
    ```

    ```
    jobMetrics.map.put("x", Double.valueOf(count1));
    ```

## 项目地址

[spring-boot-2.x-scaffold](https://github.com/MyHerux/spring-boot-2.x-scaffold)   