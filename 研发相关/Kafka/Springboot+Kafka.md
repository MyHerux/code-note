# Springboot+Kafka

## 1. Overview

- [Kafka Documentation](http://kafka.apache.org/documentation/)
- [Spring for Apache Kafka](https://docs.spring.io/spring-kafka/reference/html/)

## 2. Kafka 搭建

- [Mac 安装 Kafka](https://blog.csdn.net/MyHerux/article/details/84108223)

## 3. 初试

- 依赖

    ```
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
        <version>2.1.7.RELEASE</version>
    </dependency>
    ```

- 配置

    ```
    spring:
        kafka:
            bootstrap-servers: localhost:9092
            consumer:
                group-id: myGroup
    ```

- 消费

    `topic` 为测试时建立的 `topic`

    ```
    @Component
    public class KafkaConsumer {

        @KafkaListener(topics = "test")
        public void consume(String content){
            System.out.println(content);
        }
    }
    ```

- 启动

    启动项目，并且在前面自己安装的 `Kafka Producer` 产生消息，可以看到启动的项目里面收到消息：

    ![](http://cdn.heroxu.com/20181115154227642642510.png)


## 4. Kafka架构

### 4.1. 术语

- Broker

   `Kafka` 集群包含一个或多个服务器，这种服务器被称为 `broker`

- Topic

    每条发布到 `Kafka` 集群的消息都有一个类别，这个类别被称为 `Topic` 。（物理上不同 `Topic` 的消息分开存储，逻辑上一个 `Topic` 的消息虽然保存于一个或多个 `broker` 上但用户只需指定消息的 `Topic` 即可生产或消费数据而不必关心数据存于何处）

- Partition

  `Parition` 是物理上的概念，每个 `Topic` 包含一个或多个 `Partition`.

- Producer

    负责发布消息到 `Kafka broker`

- Consumer

    消息消费者，向 `Kafka broker` 读取消息的客户端。

- Consumer Group

    每个 `Consumer` 属于一个特定的 `Consumer Group`（可为每个 `Consumer` 指定 `groupname` ，若不指定 `group name` 则属于默认的 `group`）。


## 5. 更多用法

- 多消费者组消费同一条消息

    根据 `Kafka` 的设计原理可知，如果两个不同的 `consumer` 分别处于两个不同的 `consumer group` ，那么它们就可以同时消费同一条消息：

    ```
    @KafkaListener(topics = "test",groupId = "myGroup")
    public void consume(String content){
        System.out.println("myGroup message: "+content);
    }

    @KafkaListener(topics = "test",groupId = "myGroup2")
    public void consume2(String content){
        System.out.println("myGroup2 message: "+content);
    }
    ```

- 批量消费消息

    配置：

    ```
    @Bean("batchContainerFactory")
    public KafkaListenerContainerFactory<?> batchContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Object, Object> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(consumerFactory());
        containerFactory.setConcurrency(4);
        containerFactory.setBatchListener(true); //批量消费

        return containerFactory;
    }
    ```

    消费：

    ```
    @KafkaListener(topics = "test", groupId = "myGroup3", containerFactory = "batchContainerFactory")
    public void consume3(List<String> content) {
        System.out.println("myGroup3 list->string message: " + content.stream().reduce((a, b) -> a + b).get());
    }
    ```
