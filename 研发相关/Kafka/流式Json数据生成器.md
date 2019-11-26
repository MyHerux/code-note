# 流式Json数据生成器

## Overview

在看法流式应用处理时，经常需要一些流式数据来测试，自己生成这些数据比较麻烦，可以使用 `json-data-generator` 来帮助生成。

## 项目地址

- [Github](https://github.com/everwatchsolutions/json-data-generator)

## 配置 Kafka 数据

- jackieChan.config

    需要生成的 `kafka` 数据配置。

    ```
    {
        "workflows": [{
                "workflowName": "jackieChan",
                "workflowFilename": "jackieChanWorkflow.json"
            }],
        "producers": [{
            "type": "kafka",
            "broker.server": "192.168.59.103",
            "broker.port": 9092,
            "topic": "jackieChanCommand",
            "flatten": false,
            "sync": false

        }]
    }
    ```

- jackieChanWorkflow.json

    配置每一次生成的数据。

    ```
    {
        "eventFrequency": 400,
        "varyEventFrequency": true,
        "repeatWorkflow": true,
        "timeBetweenRepeat": 1500,
        "varyRepeatFrequency": true,
        "steps": [{
                "config": [{
                        "timestamp": "now()",
                        "style": "random('KUNG_FU','WUSHU','DRUNKEN_BOXING')",
                        "action": "random('KICK','PUNCH','BLOCK','JUMP')",
                        "weapon": "random('BROAD_SWORD','STAFF','CHAIR','ROPE')",
                        "target": "random('HEAD','BODY','LEGS','ARMS')",
                        "strength": "double(1.0,10.0)"
                    }
                ],
                "duration": 0
            }]
    }
    ```

## 数据生成流程

- 如果您还没有，请继续下载最新版本的 [json-data-generator](https://github.com/everwatchsolutions/json-data-generator/releases)。

- 将下载的文件解压缩到目录中。

- 将自定义配置复制到 `conf` 目录中

- 然后像这样运行生成器：

    ```
    java -jar json-data-generator-1.4.0.jar jackieChan.json
    ```