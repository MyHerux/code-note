# 


## 朋友圈点赞

- 点赞

    SADD like:{消息id} {用户id}

- 取消点赞

    SREM like:{消息id} {用户id}

- 检查用户是否点过赞

    SISMEMBER like:{消息id} {用户id}

- 获取点赞的用户列表

    SMEMBERS like:{消息id}

- 获取点赞用户数

    SCARD like:{消息id}


## 集合操作实现微博微信关注模型

- zhu关注的人：

    zhuSet -> {a}

- yang关注的人：

    yangSet -> {a,b,c}

- zhu和yang的共同关注：

    SINTER zhuSet yangSet -> {a}

- zhu关注的人也关注yang

    SISMEMBER zhuSet yangSet

- zhu可能认识的人：

    SDIFF yangSet zhuSet

## Zset集合实现排行榜

- 点击新闻

    ZINCRBY hotnows: