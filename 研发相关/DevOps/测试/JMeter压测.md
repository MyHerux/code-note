# JMeter压测

## 准备

- 下载地址

    [Download Apache JMeter](http://jmeter.apache.org/download_jmeter.cgi)

- 打开批处理文件

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180131/iL86eacGGd.png?imageslim)

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180131/GDlG687Ke9.png?imageslim)

## 基本配置

- 添加线程组

    测试计划右键->添加->threads(Users)->线程组

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180131/gA87CG7A3l.png?imageslim)

- 添加HTTP请求默认值

    线程组上右键->添加->配置元件->HTTP请求默认值

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180131/GE5BKC4EF8.png?imageslim)

- 添加HTTP信息头

    线程组上右键->添加->配置元件->HTTP信息头管理器

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180131/g4LeECiDGe.png?imageslim)

- http请求构造

    线程组上右键->添加->samlper->HTTP请求

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180131/fKI24d7B8i.png?imageslim)

- 查看结果树

    线程组上右键->添加->监听器->查看结果树、聚合报告

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180131/IHLI8AhJL1.png?imageslim)

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180131/0J9Dhl2h44.png?imageslim)

    聚合报告：

    - Samples：发出请求数量。如第三行记录，模拟20个用户，循环100次，所以显示了2000

    - Average：平均响应时间（单位：）。默认是单个Request的平均响应时间，当使用了Transaction Controller时，也可以以Transaction为单位显示平均响应时间

    - Median：中位数，也就是50%用户的响应时间

    - 90%Line：90%用户的响应时间

    - 95%Line：95%用户的响应时间

    - 99%Line：99%用户的响应时间

    - Min：最小响应时间

    - Max：最大响应时间

    - Error%：本次测试中出现错误的请求的数量/请求的总数

    - Throughput：吞吐量。默认情况下标示每秒完成的请求数（具体单位如下图）

    - KB/sec：每秒从服务器端接收到的数据量。

## 额外的插件

- 插件下载

    [jmeter-plugins.org ](https://jmeter-plugins.org/)

- TPS插件

    [JMeter 每秒事务数 TPS 插件](https://jmeter-plugins.org/wiki/TransactionsPerSecond/)
