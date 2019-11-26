- brew 安装

    ```
    brew install kafka
    ```

    > 注意：安装会依赖 zookeeper
    > zookeeper -> /usr/local/Cellar/zookeeper/3.4.13
    > kafka -> /usr/local/Cellar/kafka/2.0.0

- 启动

    background service:
    
    ```
    brew services start zookeeper

    brew services start kafka
    ```

    or

    ```
    zkServer start

    zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties & kafka-server-start /usr/local/etc/kafka/server.properties
    ```

- create topic

    ```
    kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
    ```

- 查看创建的topic

    ```
    kafka-topics --list --zookeeper localhost:2181
    ```

- 生产与消费

    打开生产者客户端：

    ```
    cd /usr/local/Cellar/kafka/2.0.0/bin

    kafka-console-producer --broker-list localhost:9092 --topic test
    ```

    打开消费者客户端：

    ```
    cd /usr/local/Cellar/kafka/2.0.0/bin

    kafka-console-consumer --bootstrap-server localhost:9092 --topic test --from-beginning
    ```

    在生产者客户端键入消息，即可在消费者客户端收到相应的消息。
    ![](https://imgconvert.csdnimg.cn/aHR0cDovL2Nkbi5oZXJveHUuY29tLzIwMTgxMTE1MTU0MjI3NDg5NTE2NTY2LnBuZw?x-oss-process=image/format,png)

## 问题

- Broker may not be available. 

    ```
    $ echo dump | nc localhost 2181
    SessionTracker dump:
    Session Sets (3):
    0 expire at Thu Jan 01 23:27:08 CST 1970:
    0 expire at Thu Jan 01 23:27:10 CST 1970:
    1 expire at Thu Jan 01 23:27:12 CST 1970:
        0x10003502b360000
    ephemeral nodes dump:
    Sessions with Ephemerals (1):
    0x10003502b360000:
        /controller
        /brokers/ids/0
    ```

    ```
    $ /usr/local/Cellar/kafka/2.0.0/bin/zookeeper-shell localhost:2181 <<< "get /brokers/ids/0"
    Connecting to localhost:2181
    Welcome to ZooKeeper!
    JLine support is disabled

    WATCHER::

    WatchedEvent state:SyncConnected type:None path:null
    {"listener_security_protocol_map":{"PLAINTEXT":"PLAINTEXT"},"endpoints":["PLAINTEXT://10.29.129.13:9092"],"jmx_port":-1,"host":"10.29.129.13","timestamp":"1542613502150","port":9092,"version":4}
    cZxid = 0xf6
    ctime = Mon Nov 19 15:45:02 CST 2018
    mZxid = 0xf6
    mtime = Mon Nov 19 15:45:02 CST 2018
    pZxid = 0xf6
    cversion = 0
    dataVersion = 0
    aclVersion = 0
    ephemeralOwner = 0x10003502b360000
    dataLength = 194
    numChildren = 0
    ```

    `zk host 改变，修改host文件`

    ```
    sudo vim /etc/hosts
    ```

## 其他

- 查看所有 topic

    ```
    /usr/local/Cellar/kafka/2.0.0/bin/kafka-topics --zookeeper 127.0.0.1:2181 --list
    ```

- 创建 topic

    ```
    kafka-topics --create \
        --zookeeper localhost:2181 \
        --replication-factor 1 \
        --partitions 1 \
        --topic streams-plaintext-input
    ```

- 查看 topic 详细描述

    ```
    kafka-topics --zookeeper localhost:2181 --describe
    ```
