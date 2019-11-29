# Elasticsearch+Logstash+kibana搭建可视化日志分析平台

## Elasticsearch安装

- 关闭防火墙（开放对应端口也可以）

    > systemctl stop firewalld           

- 安装 JDK8

    `Elasticsearch` 需要 `Java 8` 的环境。

    ```
    # yum -y list java*

    # yum -y install java-1.8.0-openjdk*
    ```

- 安装 `Elasticsearch`

    - 官方的安装文档

        > https://www.elastic.co/guide/en/elastic-stack/current/installing-elastic-stack.html

    - 下载安装公钥:

        > rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch

    - 在 `/etc/yum.repos.d/elastic.repo` 中增加以下内容

        > vim /etc/yum.repos.d/elastic.repo

        ```
        [elasticsearch-6.x]
        name=Elasticsearch repository for 6.x packages
        baseurl=https://artifacts.elastic.co/packages/6.x/yum
        gpgcheck=1
        gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
        enabled=1
        autorefresh=1
        type=rpm-md
        ```

        安装 `Elasticsearch`：

        > sudo yum install elasticsearch

    - 启动 `Elasticsearch` 并查看状态

        启动服务：

        > systemctl start elasticsearch

        查看状态：

        > systemctl status elasticsearch

    - 可能出现的问题与解决

        查看系统日志：

        > tail -n100 /var/log/messages

        ```
        OpenJDK 64-Bit Server VM warning: If the number of processors is expected to increase from one, then you should conf...CThreads=N
        OpenJDK 64-Bit Server VM warning: INFO: os::commit_memory(0x0000000085330000, 2060255232, 0) failed; error='Cannot a ...'(errno=12)
        # There is insufficient memory for the Java Runtime Environment to continue.
        # Native memory allocation (mmap) failed to map 2060255232 bytes for committing reserved memory.
        # An error report file with more information is saved as:
        # /tmp/hs_err_pid11084.log
        ```

        配置 `elasticsearch` 下的 `jvm.options`：

        > vi /etc/elasticsearch/jvm.options

        修改如下内容，修改内存大小（注意两个参数大小需要保持一致）：

        ```
        -Xms1g                
        -Xmx1g
        ```

        如果服务器内存本身只有1g的话应该分配更小的内存。

    - 查看监听端口

        ElasticSearch默认的对外服务的HTTP端口是9200，节点间交互的TCP端口是9300：

        > ss -tlnp |grep -E '9200|9300'

    - 测试服务是否可用

        > curl -X GET http://localhost:9200

    - 外网访问或者内网IP访问

        修改elasticsearch的配置yml：

        > vi /etc/elasticsearch/elasticsearch.yml

        将绑定地址设置为内网IP（绑定后内网可以通过这个IP访问）：

        ```
        network.host: 内网ip
        ```

        重启elasticsearch服务。

        阿里云安全组添加规则，自定义TCP/IP，端口：9200/9300 。

        外网浏览器访问：外网IP:9200

## Logstash安装

- Logstash同样需要Java8环境

- 官方的安装文档

    > https://www.elastic.co/guide/en/elastic-stack/current/installing-elastic-stack.html

- 下载安装公钥（如果已经安装可省略）:

    > rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch

- 在 `/etc/yum.repos.d/logstash.repo` 中增加以下内容

    > vim /etc/yum.repos.d/logstash.repo

    ```
    [logstash-6.x]
    name=Elastic repository for 6.x packages
    baseurl=https://artifacts.elastic.co/packages/6.x/yum
    gpgcheck=1
    gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
    enabled=1
    autorefresh=1
    type=rpm-md
    ```
    
    安装 `Logstash`：

    > sudo yum install logstash

- 启动 `Logstash` 并查看状态

    启动服务：

    > systemctl start logstash

    查看状态：

    > systemctl status logstash

## Kibana安装

- 官方的安装文档

    > https://www.elastic.co/guide/en/elastic-stack/current/installing-elastic-stack.html

- 下载安装公钥（如果已经安装可省略）:

    > rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch

- 在 `/etc/yum.repos.d/kibana.repo` 中增加以下内容

    > vim /etc/yum.repos.d/kibana.repo

    ```
    [kibana-6.x]
    name=Kibana repository for 6.x packages
    baseurl=https://artifacts.elastic.co/packages/6.x/yum
    gpgcheck=1
    gpgkey=https://artifacts.elastic.co/GPG-KEY-elasticsearch
    enabled=1
    autorefresh=1
    type=rpm-md
    ```
    
    安装 `Kibana`：

    > sudo yum install kibana

- 启动 `Kibana` 并查看状态

    启动服务：

    > systemctl start kibana

    查看状态：

    > systemctl status kibana

- 外网访问或者内网IP访问

    - 修改 `kibana` 的配置 `kibana.yml`：

        > vi /etc/kibana/kibana.yml

    - 将绑定地址设置为内网IP（绑定后内网可以通过这个IP访问）：

        ```
        server.host: 内网ip
        ```

    - 重启kibana服务。

    - 阿里云安全组添加规则，自定义TCP/IP，端口：5601。

    - 外网浏览器访问：外网IP:5601

        ![](http://cdn.heroxu.com/20180511152600954252933.png)

## 配置ELK

- 配置Logstash

    - 配置 `logstash` 收集 `syslog` 日志，将收集到的日志输出到 `ElasticSearch` 中

        > vim /etc/logstash/conf.d/syslog.conf

        ```
        input {
        syslog {
            type => "system-syslog"
            port => 10514
        }
        }
        output {
        elasticsearch {
            hosts => ["es内网ip:9200"]  # 定义es服务器的ip
            index => "system-syslog-%{+YYYY.MM}" # 定义索引
        }
        }
        ```
    - 检查配置文件是否正确

        > cd /usr/share/logstash/bin

        > ./logstash --path.settings /etc/logstash/ -f /etc/logstash/conf.d/syslog.conf --config.test_and_exit

        命令说明：

            -path.settings 用于指定logstash的配置文件所在的目录
            f 指定需要被检测的配置文件的路径
            -config.test_and_exit 指定检测完之后就退出，不然就会直接启动了

    - 配置 `kibana` 服务器的 `ip` 以及配置的监听端口

        > vim /etc/rsyslog.conf

        ```
        *.* @@内网IP:10514
        ```

    - 重启 `rsyslog` ，让配置生效

       > systemctl restart rsyslog

    - 指定配置文件，启动logstash

        > cd /usr/share/logstash/bin

        > ./logstash --path.settings /etc/logstash/ -f /etc/logstash/conf.d/syslog.conf

    - 重启 `logstash` 服务

        > systemctl restart logstash

    - 查看监听端口，服务是否启动成功

        > netstat -lntp |grep 9600

        > netstat -lntp |grep 10514
    
    - 修改`logstash` 为内网 `ip`

        > vim /etc/logstash/logstash.yml

        ```
        http.host: "logstash内网ip"
        ```

        重启服务

- 配置kibana

    - 配置 `elasticsearch` 地址

        > vim /etc/kibana/kibana.yml

        ```
        elasticsearch.url: "http://es内网ip:9200"
        ```
    - 重启服务

    - 查看索引

        curl 'es内网ip:9200/_cat/indices?v'

        如果system-syslog索引获取成功，证明配置没问题，logstash与es通信正常。

- 在kibana上查看日志

    - 浏览器访问 `http://kibana外网ip:5601`

    - 配置索引，使用 `-*` 通配符进行批量匹配
    
        ![](http://cdn.heroxu.com/20180511152601517798487.png)

        ![](http://cdn.heroxu.com/20180511152601520912193.png)

    - 配置成功后点击 `Discover` 即可查看日志
    
        ![](http://cdn.heroxu.com/20180511152601539511837.png)
