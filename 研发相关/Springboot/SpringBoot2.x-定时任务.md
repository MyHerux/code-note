# SpringBoot2.x-定时任务

## Timer

`Timer` 是 `jdk 1.3` 之后自带的 `java.util.Timer` 类。

- 这个类允许你按一定的规则调度一个 `java.util.TimerTask` 任务。主要是按照一定速率去执行任务，不支持 `cron` 表达式。

- 示例

    ```java
    public class TimerTest {

        public static void main(String[] args) {
            TimerTask timerTask = new TimerTask() {

                private Integer count = 0;

                @Override
                public void run() {
                    count++;
                    System.out.println("task count:"+count);
                }
            };

            Timer timer=new Timer();
            timer.schedule(timerTask,1000,1000);
        }
    }

    ```

    `Timer` 类本身比较简单，用法有限，所以使用比较少，具体内容可以直接看源码。

## ScheduledExecutorService

`ScheduledExecutorService` 是 `jdk 1.5` 之后自带的一个类。

- 是基于线程池设计的定时任务类,每个调度任务都会分配到线程池中的一个线程去执行（任务是并发执行,互不影响）。

- `schedule` 方法返回 `Future` 对象

- 示例

    ```java
    public class ScheduledExecutorServiceTest {

        public static void main(String[] args) {
            ScheduledExecutorService service = Executors.newScheduledThreadPool(5);

            service.scheduleAtFixedRate(new Runnable() {

                private Integer count = 0;

                @Override
                public void run() {
                    count++;
                    System.out.println("task count:" + count);
                }

            }, 1000,1000, TimeUnit.MILLISECONDS);
        }
    }

    ```

## Spring Task

`Spring3.0` 以后引入了一个 `TaskScheduler` ，它具有各种方法，可以在将来的某个时刻调度任务。

- 使用线程池，支持异步

- 可以使用 `Trigger` ，支持 `cron` 表达式。

- `schedule` 方法返回 `Future` 对象

    ```java
    public interface TaskScheduler {

        ScheduledFuture schedule(Runnable task, Trigger trigger);

        ScheduledFuture schedule(Runnable task, Date startTime);

        ScheduledFuture scheduleAtFixedRate(Runnable task, Date startTime, long period);

        ScheduledFuture scheduleAtFixedRate(Runnable task, long period);

        ScheduledFuture scheduleWithFixedDelay(Runnable task, Date startTime, long delay);

        ScheduledFuture scheduleWithFixedDelay(Runnable task, long delay);

    }
    ```

- 参考文档

    - [Spring Framework](https://docs.spring.io/spring/docs/5.0.6.RELEASE/spring-framework-reference/integration.html#scheduling)

    - [Spring Framework 中文](https://github.com/MyHerux/spring-framework-documentation-zh/blob/master/Integration/7.Task-Execution-and-Scheduling.md)

- 示例

    - 开启注解

        要启用对 `@Scheduled` 和 `@Async` 注释的支持，请将 `@EnableScheduling` 和 `@EnableAsync` 添加到您的 `@Configuration` 类之一：

        ```java
        @Configuration
        @EnableAsync
        @EnableScheduling
        public class AppConfig {

            @Bean("main")
            public Executor mainExecutor() {
                ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                executor.setCorePoolSize(10);
                executor.setMaxPoolSize(20);
                executor.setQueueCapacity(100);
                executor.initialize();
                return executor;
            }

            @Bean("other")
            public Executor otherExecutor() {
                return Executors.newSingleThreadExecutor();
            }
        }
        ```

        > :sunny: 对使用 `@Scheduled` 注解的 `job` 类需要添加 `@Component`

    - `@Scheduled` 注解

        `@Scheduled` 注释可以与触发器元数据一起添加到方法中。例如，以固定延迟每 `5` 秒调用以下方法，这意味着将从每个前一次调用的完成时间开始测量该时间段。

        ```
        @Scheduled(fixedDelay=5000)
        public void doSomething() {
            // something that should execute periodically
        }
        ```

        如果需要固定速率执行，只需更改注释中指定的属性名称即可。在每次调用的连续开始时间之间测量的每5秒执行以下操作。

        ```
        @Scheduled(fixedRate=5000)
        public void doSomething() {
            // something that should execute periodically
        }
        ```

        对于固定延迟和固定速率任务，可以指定初始延迟，指示在第一次执行该方法之前等待的毫秒数。

        ```
        @Scheduled(initialDelay=1000, fixedRate=5000)
        public void doSomething() {
            // something that should execute periodically
        }
        ```

        如果简单的周期性调度不够表达，则可以提供 `cron` 表达式。例如，以下内容仅在工作日执行。

        ```
        @Scheduled(cron="*/5 * * * * MON-FRI")
        public void doSomething() {
            // something that should execute on weekdays only
        }
        ```

        > :sunny:
        > 您还可以使用 `zone` 属性指定解析 cron 表达式的时区。

        请注意，要调度的方法必须具有 void 返回值，并且不得指望任何参数。如果该方法需要与 Application Context 中的其他对象进行交互，则通常会通过依赖注入提供这些对象。

    - `@Async` 注解

        可以在方法上提供 `@Async` 注释，以便异步调用该方法。换句话说，调用者将在调用时立即返回，并且该方法的实际执行将发生在已提交给 `Spring TaskExecutor` 的任务中。在最简单的情况下，注释可以应用于返回空隙的方法。

        ```
        @Async
        void doSomething() {
            // this will be executed asynchronously
        }
        ```

        与使用 `@Scheduled` 注释注释的方法不同，这些方法可以使用参数，因为它们将在运行时由调用者以 “正常” 方式调用，而不是由容器管理的调度任务调用。例如，以下是 `@Async` 注释的合法应用程序。

        ```
        @Async
        void doSomething(String s) {
            // this will be executed asynchronously
        }
        ```

        甚至可以异步调用返回值的方法。但是，这些方法需要具有 `Future` 类型的返回值。这仍然提供了异步执行的好处，以便调用者可以在调用 `Future` 上的 `get()` 之前执行其他任务。

        ```
        @Async
        Future<String> returnSomething(int i) {
            // this will be executed asynchronously
        }
        ```

        如果需要指定执行程序，可以使用 `@Async` 批注的 `value` 属性。

        ```
        @Async("other")
        void doSomething(String s) {
            // this will be executed asynchronously by "otherExecutor"
        }
        ```

    - `cron` 表达式详解

        一个cron表达式有至少6个（也可能7个）有空格分隔的时间元素。按顺序依次为：

        - 1 秒（0~59）
        - 2 分钟（0~59）
        - 3 小时（0~23）
        - 4 天（0~31）
        - 5 月（0~11）
        - 6 星期（1~7 1=SUN 或 SUN，MON，TUE，WED，THU，FRI，SAT）
        - 7 年份（1970－2099）

        在线cron表达式生成：http://qqe2.com/cron/index

## Quartz

`Quartz` 提供更强大的任务调度功能， `SpringBoot2.0.0` 以后，在 `spring-boot-starter` 中已经包含了 `quart` 的依赖。

- 更复杂的功能，可以参考文档：[quartz-scheduler](http://www.quartz-scheduler.org/)

- 示例

    - job

        ```
        public class TestQuartz extends QuartzJobBean {

            private Integer count=0;

            @Override
            protected void executeInternal(JobExecutionContext jobExecutionContext) {
                count++;
                System.out.println("quartz task count:" + count);
            }
        }
        ```

    - config

        ```
        @Configuration
        public class QuartzConfig {

            @Bean
            public JobDetail teatQuartzDetail() {
                return JobBuilder
                        .newJob(TestQuartz.class)
                        .withIdentity("job1", "group1")
                        .storeDurably()
                        .build();
            }

            @Bean
            public Trigger testQuartzTrigger() {
                SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(1)
                        .repeatForever();

                return TriggerBuilder.newTrigger().forJob(teatQuartzDetail())
                        .withIdentity("trigger1", "group1")
                        .withSchedule(scheduleBuilder)
                        .build();
            }
        }
        ```












