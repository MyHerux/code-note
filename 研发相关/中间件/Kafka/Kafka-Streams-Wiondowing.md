# Kafka-Streams-Wiondowing

Wiondowing 使您可以控制如何对具有相同键的记录进行分组以进行有状态操作。

## Note

相关操作是分组，它将具有相同密钥的所有记录分组，以确保数据被正确分区（“键入”）以用于后续操作。分组后，窗口允许您进一步对键的记录进行子分组。

例如，在连接操作中，窗口状态存储用于存储到目前为止在定义的窗口边界内接收的所有记录。在聚合操作中，窗口状态存储用于存储每个窗口的最新聚合结果。在指定的窗口保留期后，状态存储中的旧记录将被清除。 Kafka Streams 保证至少在指定的时间内保持一个窗口;默认值为一天，可以通过 `Materialized#withRetention()` 进行更改。

DSL支持以下类型的窗口：

Window name|Behavior|Short description
-|:-|:-|
Tumbling time windows|基于时间的|固定尺寸，不重叠，无间隙的窗户
Hopping time window|基于时间的|固定大小的重叠窗口
Sliding time window|基于时间的|固定大小的重叠窗口，用于处理记录时间戳之间的差异
Session window|基于Session的|动态大小，不重叠，数据驱动的窗口

## Tumbling time windows

- 定义

    `翻滚时间窗口`（Tumbling time windows）实际是 `跳跃时间窗口`（Hopping time window） 的一种特例。他们模拟固定尺寸，不重叠，无间隙的窗口。翻滚窗口由单个属性定义：窗口的大小。翻滚窗口是一个跳跃窗口，其窗口大小等于其提前间隔。由于翻滚窗口从不重叠，因此数据记录将属于一个且仅一个窗口。

    ![](http://cdn.heroxu.com/2018-11-23-095304.png)

    此图显示了使用翻滚窗口对数据记录流进行窗口化。 Windows 不重叠，因为根据定义，提前间隔与窗口大小相同。在此图中，时间数字代表分钟：例如 t = 5 表示“在五分钟标记处”。

    翻滚时间窗口与时期对齐，下限间隔包含在内，上限为独占。 “与时期对齐”意味着第一个窗口在时间戳零开始。例如，大小为5000毫秒的翻滚窗口具有可预测的窗口边界 `[0;5000),[5000;10000),...` - 而不是 `[1000;6000],[6000;11000],...` 当然也不是某些东西“随机”如 `[1452;6452],[6452;11452],...`

## Hopping time window

- 定义

    跳跃时间窗口是基于时间间隔的窗口。他们模拟固定大小的（可能）重叠窗口。跳跃窗口由两个属性定义：窗口的大小和其提前间隔（又名“跳”）。提前间隔指定窗口相对于前一个窗口向前移动的程度。例如，您可以配置大小为5分钟且提前间隔为1分钟的跳跃窗口。由于跳跃窗口可以重叠 - 并且通常它们可以 - 数据记录可以属于多于一个这样的窗口。

    ![](http://cdn.heroxu.com/2018-11-27-032105.png)

    此图显示了使用跳跃窗口窗口化数据记录流。在此图中，时间数字代表分钟;例如t = 5表示“在五分钟标记处”。

    跳跃时间窗口与时期对齐，下限间隔包含在内，上限为独占。 “与时期对齐”意味着第一个窗口在时间戳零开始。例如，大小为5000毫秒的翻滚窗口具有可预测的窗口边界 `[0;5000),[5000;10000),...` - 而不是 `[1000;6000],[6000;11000],...` 当然也不是某些东西“随机”如 `[1452;6452],[6452;11452],...`

    与我们之前看到的非窗口聚合不同，窗口聚合返回一个窗口化的 `KTable` ，其键类型为 `Windowed<K>`。这是为了将聚合值与来自不同窗口的相同键区分开来。相应的窗口实例和嵌入的密钥可以分别作为 `Windowed#window()` 和 `Windowed#key()` 检索。

- Hopping time windows vs. Tumbling time windows

    ```
    Duration windowSizeMs = Duration.ofMinutes(5);
    TimeWindows.of(windowSizeMs);

    // 使用 Hopping time windows 的写法来写 Tumbling time windows --> TimeWindows.of(windowSizeMs) == TimeWindows.of(windowSizeMs).advanceBy(windowSizeMs)
    TimeWindows.of(windowSizeMs).advanceBy(windowSizeMs);
    ```

    ```
    Duration windowSizeMs = Duration.ofMinutes(5);
    Duration advanceMs =    Duration.ofMinutes(1);
    TimeWindows.of(windowSizeMs).advanceBy(advanceMs);
    ```

## Sliding time window

- 定义

    该窗口只用于2个KStream进行Join计算时。

## Session window

- 定义

    该窗口用于对Key做Group后的聚合操作中。它需要对Key做分组，然后对组内的数据根据业务需求定义一个窗口的起始点和结束点。

## Window Final Results

- 定义

    在 `Kafka Streams` 中，窗口计算会不断更新其结果。当新数据到达窗口时，下游会发出新计算的结果。对于许多应用来说，这是理想的，因为始终可以获得新的结果。和 `KafkaStreams` 旨在使编程连续计算无缝。但是，某些应用程序只需对窗口计算的最终结果采取操作。常见的示例是向不支持更新的系统发送警报或传递结果。

    假设您每个用户每小时都有窗口事件数。如果您想在一小时内用户少于三个事件时发送提醒，那么您就有了真正的挑战。所有用户都会首先匹配此条件，直到他们产生足够的事件，因此当某人符合条件时，您不能简单地发送警报;你必须等到你知道你不会再看到特定窗口的事件然后发送警报。

    `Kafka Streams` 提供了一种定义此逻辑的简洁方法：在定义窗口计算后，您可以抑制中间结果，在窗口关闭时为每个用户发出最终计数。

    比如：

    ```
    KGroupedStream<UserId, Event> grouped = ...;
    grouped
        .windowedBy(TimeWindows.of(Duration.ofHours(1)).grace(ofMinutes(10)))
        .count()
        .suppress(Suppressed.untilWindowCloses(unbounded()))
        .filter((windowedUserId, count) -> count < 3)
        .toStream()
        .foreach((windowedUserId, count) -> sendAlert(windowedUserId.window(), windowedUserId.key(), count));
    ```

    - grace(ofMinutes(10))

        这允许我们限制窗口将接受的事件的迟到。例如， `09:00` 到 `10:00` 窗口将接受迟到的记录，直到 `10:10` ，此时窗口关闭。

    - .suppress(Suppressed.untilWindowCloses(...))

        这会将抑制运算符配置为在窗口关闭之前不为窗口发出任何内容，然后发出最终结果。例如，如果用户 `U` 在 `09:00` 和 `10:10` 之间获得 `10` 个事件，则抑制下游的过滤器将不会获得窗口密钥 `U@09:00-10:00` 的事件直到 `10:10` ，然后它将得到一个值为 `10`.这是窗口计数的最终结果。

    - unbounded()

        这将配置用于存储事件的缓冲区，直到它们关闭窗口。生产代码能够限制用于缓冲区的内存量，但是这个简单的示例创建了一个没有上限的缓冲区。

- 更多信息 -> [KIP-328: Ability to suppress updates for KTables](https://cwiki.apache.org/confluence/display/KAFKA/KIP-328%3A+Ability+to+suppress+updates+for+KTables)

## 代码示例

- kafka消息的生产

    - [流式Json数据生成器](https://blog.csdn.net/MyHerux/article/details/84315146)

    - test-config.json

        ```
        {
            "workflows":[
                {
                    "workflowName":"test",
                    "workflowFilename":"test-data.json"
                }
            ],
            "producers":[
                {
                    "type":"kafka",
                    "broker.server":"127.0.0.1",
                    "broker.port":9092,
                    "topic":"x-items",
                    "flatten":false,
                    "sync":false
                },
                {
                    "type":"logger"
                }
            ]
        }
        ```
    - test-data.json

        ```
        {
            "eventFrequency":1000,
            "varyEventFrequency":true,
            "repeatWorkflow":true,
            "timeBetweenRepeat":1000,
            "varyRepeatFrequency":true,
            "steps":[
                {
                    "config":[
                        {
                            "itemName":"iphone",
                            "address":"BJ",
                            "type":"phone",
                            "price":"5388.88"
                        }
                    ],
                    "duration":0
                }
            ]
        }
        ```

- AbstractTopology

    ```
    public abstract class AbstractTopology
    {
        // 转换数据 kv
        KeyValueMapper<String, String, KeyValue<String, String>> keyValueMapper = (k, v) -> {
            JSONObject data = JSONObject.parseObject(v);
            if (data == null) {
                return new KeyValue<>(null, null);
            } else {
                return new KeyValue<>(data.getString("itemName"), data.getString("price"));
            }
        };

        // 设定初始数据
        Initializer<String> initializer = () -> "0";

        // 聚合数据
        Aggregator<String, String, String> aggregator = (aggKey, newValue, aggValue) -> {
            BigDecimal b1 = new BigDecimal(aggValue);
            BigDecimal b2 = new BigDecimal(newValue);
            return b1.add(b2).toString();
        };

    }
    ```

- TumblingTopology

    ```
    public class TumblingTopology extends AbstractTopology {

        void buildTopology(KStream<String, String> source) {

            // 窗口间隔
            Duration windowSizeMs = Duration.ofMinutes(5);

            // 窗口数据存储
            String storeName = "tumbling_item_store";
            Materialized<String, String, WindowStore<Bytes, byte[]>> materialized =
                    Materialized.<String, String, WindowStore<Bytes, byte[]>>as(storeName).withValueSerde(Serdes.String());

            // Topology
            source.filter((k, v) -> v != null)
                    .map(keyValueMapper)
                    .filter((k, v) -> v != null)
                    .groupByKey()
                    .windowedBy(TimeWindows.of(windowSizeMs))
                    .aggregate(initializer, aggregator, materialized)
                    .toStream()
                    .map((k, v) -> new KeyValue<>(k.key(), v))
                    .to("tumbling-item-sum-price", Produced.with(Serdes.String(), Serdes.String()));
        }
    }
    ```

- HoppingTopology

    ```
    public class HoppingTopology extends AbstractTopology {

        void buildTopology(KStream<String, String> source) {

            // 窗口间隔
            Duration windowSizeMs = Duration.ofMinutes(5);
            Duration advanceWindowSizeMs = Duration.ofMinutes(1);

            // 窗口数据存储
            String storeName = "hopping_item_store";
            Materialized<String, String, WindowStore<Bytes, byte[]>> materialized =
                    Materialized.<String, String, WindowStore<Bytes, byte[]>>as(storeName).withValueSerde(Serdes.String());

            // Topology
            source.filter((k, v) -> v != null)
                    .map(keyValueMapper)
                    .filter((k, v) -> v != null)
                    .groupByKey()
                    .windowedBy(TimeWindows.of(windowSizeMs).advanceBy(advanceWindowSizeMs))
                    .aggregate(initializer, aggregator, materialized)
                    .toStream()
                    .map((k, v) -> new KeyValue<>(k.key(), v))
                    .to("hopping-item-sum-price", Produced.with(Serdes.String(), Serdes.String()));
        }
    }
    ```

- FinalTopology

    ```
    public class FinalTopology extends AbstractTopology {

        void buildTopology(KStream<String, String> source) {

            // 窗口间隔
            Duration windowSizeMs = Duration.ofMinutes(5);
            Duration advanceWindowSizeMs = Duration.ofMinutes(1);

            // 窗口延迟
            Duration graceSizeMs = Duration.ofSeconds(5);

            // 窗口数据存储
            String storeName = "final_item_store";
            Materialized<String, String, WindowStore<Bytes, byte[]>> materialized =
                    Materialized.<String, String, WindowStore<Bytes, byte[]>>as(storeName).withValueSerde(Serdes.String());

            Serde<Windowed<String>> windowedSerde = Serdes.serdeFrom(new TimeWindowedSerializer<>(new StringSerializer()), new TimeWindowedDeserializer<>(new StringDeserializer()));

            // Topology
            source.filter((k, v) -> v != null)
                    .map(keyValueMapper)
                    .filter((k, v) -> v != null)
                    .groupBy((k, v) -> k, Serialized.with(Serdes.String(), Serdes.String()))
                    .windowedBy(TimeWindows.of(windowSizeMs).advanceBy(advanceWindowSizeMs).grace(graceSizeMs))
                    .aggregate(initializer, aggregator, materialized)
                    .suppress(untilWindowCloses(unbounded()))
                    .toStream()
                    .map((k, v) -> new KeyValue<>(k.key(), v))
                    .to("final-item-sum-price", Produced.with(Serdes.String(), Serdes.String()));
        }
    }
    ```
## 比较

- 生成的消息

    ![](http://cdn.heroxu.com/20181130154357062482817.png)

    - TumblingTopology

        `1条/s` --> 因为只有一个窗口，而生成者的数据是 `1条/s` ，所以 `stream` 产生的数据也是 `1条/s`

    - HoppingTopology

        `5条/s` --> 因为会同时产生五个窗口，而生成者的数据是 `1条/s` ，所以 `stream` 产生的数据也是 `5条/s`

    - FinalTopology

        `1/min` --> 因为会同时产生五个窗口，间隔时间为 `1min` ，所以每分钟会销毁一个窗口，所以 `stream` 产生的数据是 `1条/s`

- 产生的窗口

    - TumblingTopology

        1

    - HoppingTopology

        5

    - FinalTopology

        5

## 附

- [kafka-streams-dsl-api](https://kafka.apache.org/21/documentation/streams/developer-guide/dsl-api.html#windowing)

- [项目地址](https://github.com/MyHerux/cases/tree/master/cases-kafka)