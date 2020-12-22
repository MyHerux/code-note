# SpringBoot2.x中的应用监控：Actuator+Prometheus+Grafana

## 总览

`Actuator` 提供端点将数据暴露出来， `Prometheus` 定时去拉取数据并保存和提供搜索和展示， `Grafana` 提供更加精美的图像化展示

## Actuator

[SpringBoot2.x使用Actuator来做应用监控](https://blog.csdn.net/myherux/article/details/80670557)

## Prometheus

[Prometheus](https://github.com/prometheus/prometheus) 是 [Cloud Native Computing Foundation](https://www.cncf.io/) 项目之一，是一个系统和服务监控系统。它按给定的时间间隔从配置的目标收集指标，评估规则表达式，显示结果，并且如果观察到某些条件为真，则可触发警报。

### 特性

- `多维度` 数据模型（由度量名称和键/值维度集定义的时间序列）

- `灵活的查询语言` 来利用这种维度

- 不依赖分布式存储；`单个服务器节点是自治的`

- 时间序列采集通过HTTP上的 `pull model` 发生

- `推送时间序列` 通过中间网关得到支持

- 通过 `服务发现` 或 `静态配置` 来发现目标

- 多种模式的 `图形和仪表盘支持`

- 支持分级和水平 `federation`

### 架构图

![](http://cdn.heroxu.com/20180611152870866231216.png)

### 集成到应用

- 添加依赖

    ```
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
    ```

- 启动端点

    启用 `/actuator/prometheus` 端点，供 `Prometheus` 来抓取指标。在启动的端点中，添加 prometheus。

    ```
    management:
        endpoints:
            web:
            exposure:
                include: health,info,env,metrics,prometheus
    ```

- 启动 SpringBoot 服务

    部署自己的 SpringBoot 项目，查看 `/actuator/prometheus`:

    ```
    # HELP jvm_gc_max_data_size_bytes Max size of old generation memory pool
    # TYPE jvm_gc_max_data_size_bytes gauge
    jvm_gc_max_data_size_bytes 1.395654656E9
    ...
    ```

- 通过 Prometheus 来抓取数据

    `Prometheus` 会按照配置的时间周期去 `pull` 暴露的端点（`/actuator/prometheus`）中的指标数据

    - prometheus.yml 配置

        参考 [`官方的配置`](https://github.com/prometheus/prometheus/blob/master/documentation/examples/prometheus.yml)

        我的配置（SpringBoot项目是部署在8077端口的）：

        ```
        # my global config
        global:
        scrape_interval:     15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
        evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
        # scrape_timeout is set to the global default (10s).

        # Alert manager configuration
        alerting:
        alertmanagers:
        - static_configs:
            - targets:
            # - alertmanager:9093

        # Load rules once and periodically evaluate them according to the global 'evaluation_interval'.
        rule_files:
        # - "first_rules.yml"
        # - "second_rules.yml"


        scrape_configs:
        - job_name: 'prometheus'
            static_configs:
            - targets: ['localhost:9090']
        - job_name: 'spring'
            metrics_path: '/actuator/prometheus'
            static_configs:
            - targets: ['localhost:8077']
        ```

    - 启动 prometheus docker

        指定刚才的 `prometheus.yml` 配置地址 `/opt/demo/prometheus.yml` ，创建镜像
        
        ```
        docker run -p 9090:9090 -v /opt/demo/prometheus.yml:/etc/prometheus/prometheus.yml \
        prom/prometheus
        ```

    - 访问 9090 端口

        ![](http://cdn.heroxu.com/20180612152879174729237.png)

## Grafana

The open platform for beautiful 
analytics and monitoring.

- 安装

    - 官网下载

        > https://grafana.com/

    - Mac 安装：brew install grafana

- 启动本地服务

    > brew services start grafana

    访问 `http://127.0.0.1:3000/`（默认账号密码是 `admin/admin` ）：

    ![](http://cdn.heroxu.com/20180612152880672344532.png)

- 配置 Prometheus 数据源

    URL填入 Prometheus 服务的地址：

    ![](http://cdn.heroxu.com/20180612152880725293179.png)

- 添加 Dashboards

    ![](http://cdn.heroxu.com/2018061215288088143797.png)

- 查看监控页面

    ![](http://cdn.heroxu.com/20180612152880910476496.png)

