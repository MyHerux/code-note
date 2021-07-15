# 什么是Merkle Tree

## Overview

### Bitcoin 的第一个 Block 数据

```
{
    "result":{
        "hash":"00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048",
        "confirmations":690940,
        "strippedsize":215,
        "size":215,
        "weight":860,
        "height":1,
        "version":1,
        "versionHex":"00000001",
        "merkleroot":"0e3e2357e806b6cdb1f70b54c3a3a17b6714ee1f0e68bebb44a74b1efd512098",
        "tx":[
            "0e3e2357e806b6cdb1f70b54c3a3a17b6714ee1f0e68bebb44a74b1efd512098"
        ],
        "time":1231469665,
        "mediantime":1231469665,
        "nonce":2573394689,
        "bits":"1d00ffff",
        "difficulty":1,
        "chainwork":"0000000000000000000000000000000000000000000000000000000200020002",
        "nTx":1,
        "previousblockhash":"000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f",
        "nextblockhash":"000000006a625f06636b8bb6ac7b960a8d03705d1ace08b1a19da3fdcc99ddbd"
    },
    "error":null,
    "id":"0"
}
```

可以看到，`block` 数据里面有一个 `key` 是 `merkleroot` ，同时在这个 `block` 里面 `merkleroot` 的值和 `tx` 的值相同。

### 一个简单的 Merkle Tree 过程

![6018050AFC425873](http://cdn.heroxu.com/6018050AFC425873.png)

`Bitcoin` 创世区块中因为只有一个 `tx` ，所以自然 `merkleroot` 和叶子节点的数据相同。

## 什么是 Merkle Tree？

`MerkleTree` 是一棵用哈希值搭建起来的树，树的所有节点都存储了哈希值，所以也叫哈希树。

## Merkle Tree 的节点构成

- 叶节点

    每个数据块进行哈希运算后，得到的哈希值就是叶节点。在比特币中，对于一个区块而言，每一笔交易数据进行哈希之后的结果就是叶节点。

- 中间节点

    子节点两两匹配，子节点哈希值合并成新的字符串，对合并结果再次进行哈希运算，得到的哈希值，就是对应的中间节点。

- 根节点

    有且只有一个，不断Hash之后的最终结果，也叫 `ＭerkleRoot` ，这是终止节点。比特币的每个 `block` 中的数据就有 `ＭerkleRoot`。

## Merkle Tree 的特点

- 首先是它的树的结构， `Merkle Tree` 常见的结构是二叉树，但它也可以是多叉树，它具有树结构的全部特点。

- `Merkle Tree` 的基础数据不是固定的，想存什么数据由你说了算，因为它只要数据经过哈希运算得到的 `Ｈash` 值。

- `Merkle Tree` 是从下往上逐层计算的，就是说每个中间节点，都是根据相邻的两个叶子节点组合计算得出的，而根节点是根据两个中间节点组合计算得出的，所以叶子节点是基础。

## 如何构造一棵 Merkle Tree 

### 1. 建立 Merkle Tree 

![97D7D75DB7696C58](http://cdn.heroxu.com/97D7D75DB7696C58.png)

- 初始有7个数据块
- 分别对数据块做 `hash` 运算，`node01=hash(data01)`
- 相邻两个 `hash` 块串联，然后继续做 `hash` 运算，重复这个操作，直到生成 `ＭerkleRoot`

### 2. 检索不同的数据块

对于 `Bitcoin` ，只需要知道 `ＭerkleRoot` 是否一致即可判断 `tx` 是否被篡改。同理，如果想知道具体哪个数据被篡改，使用 `MerkleTree` 也非常方便。假设有 `7` 个数据块从服务器 `A` 同步数据到服务器 `B` ，需要知道中间是否有数据同步错误。
- 首先，分别构造 `MerkleTree` 
- 比较 `root` 节点，如果不同，检索其孩子节点 `node21` 和 `node22`
- 重复比较，直到发现是叶子节点 `node05` 不同，则说明是 `data05` 的数据同步错误

![6378CF030C85D082](http://cdn.heroxu.com/6378CF030C85D082.png)

> 在比特币中，Merkle Tree 验证交易的方式于此相反，从叶子节点验证到根节点。

## Bitcoin 构造的 Merkle Tree 

### height=1

```
{
    "result":{
        "hash":"00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048",
        "confirmations":690940,
        "strippedsize":215,
        "size":215,
        "weight":860,
        "height":1,
        "version":1,
        "versionHex":"00000001",
        "merkleroot":"0e3e2357e806b6cdb1f70b54c3a3a17b6714ee1f0e68bebb44a74b1efd512098",
        "tx":[
            "0e3e2357e806b6cdb1f70b54c3a3a17b6714ee1f0e68bebb44a74b1efd512098"
        ],
        "time":1231469665,
        "mediantime":1231469665,
        "nonce":2573394689,
        "bits":"1d00ffff",
        "difficulty":1,
        "chainwork":"0000000000000000000000000000000000000000000000000000000200020002",
        "nTx":1,
        "previousblockhash":"000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f",
        "nextblockhash":"000000006a625f06636b8bb6ac7b960a8d03705d1ace08b1a19da3fdcc99ddbd"
    },
    "error":null,
    "id":"0"
}
```

创世区块中因为只有一个 `tx` ，所以自然 `merkleroot` 和叶子节点 `tx` 的数据相同。

### height=181

- 查看 `block` 信息

    ```
    {
        "result":{
            "hash":"00000000dc55860c8a29c58d45209318fa9e9dc2c1833a7226d86bc465afc6e5",
            "confirmations":690794,
            "strippedsize":490,
            "size":490,
            "weight":1960,
            "height":181,
            "version":1,
            "versionHex":"00000001",
            "merkleroot":"ed92b1db0b3e998c0a4351ee3f825fd5ac6571ce50c050b4b45df015092a6c36",
            "tx":[
                "8347cee4a1cb5ad1bb0d92e86e6612dbf6cfc7649c9964f210d4069b426e720a",
                "a16f3ce4dd5deb92d98ef5cf8afeaf0775ebca408f708b2146c4fb42b41e14be"
            ],
            "time":1231740133,
            "mediantime":1231735142,
            "nonce":792669465,
            "bits":"1d00ffff",
            "difficulty":1,
            "chainwork":"000000000000000000000000000000000000000000000000000000b600b600b6",
            "nTx":2,
            "previousblockhash":"00000000b5ef0ea215becad97402ce59d1416fe554261405cda943afd2a8c8f2",
            "nextblockhash":"0000000054487811fc4ff7a95be738aa5ad9320c394c482b27c0da28b227ad5d"
        },
        "error":null,
        "id":"0"
    }
    ```

- 对 `tx` 翻转 `bytes`（从小端到大端）

   `python` 翻转程序：

    ```
    #!/usr/bin/env python
    line = raw_input("Input original hex string\n")
    n = 2
    orig_list = [line[i:i+n] for i in range(0, len(line), n)]
    reversed_list = orig_list[::-1]
    reversed = ''.join(reversed_list)
    print reversed
    ```

    ```
    8347cee4a1cb5ad1bb0d92e86e6612dbf6cfc7649c9964f210d4069b426e720a ->
    0a726e429b06d410f264999c64c7cff6db12666ee8920dbbd15acba1e4ce4783
    ```

    ```
    a16f3ce4dd5deb92d98ef5cf8afeaf0775ebca408f708b2146c4fb42b41e14be ->
    be141eb442fbc446218b708f40caeb7507affe8acff58ed992eb5ddde43c6fa1
    ```
    

- 链接两个结果并计算 `sha256 digest`

    ```
    printf "0a726e429b06d410f264999c64c7cff6db12666ee8920dbbd15acba1e4ce4783be141eb442fbc446218b708f40caeb7507affe8acff58ed992eb5ddde43c6fa1" | xxd -r -p | openssl sha256

    4bb234b71d205dba7936c5e6241e1666479e56f0e370e7725de2c99e6cf5de81
    ```

- 对结果再进行 `sha256` 操作

    ```
    printf "4bb234b71d205dba7936c5e6241e1666479e56f0e370e7725de2c99e6cf5de81" | xxd -r -p | openssl sha256

    366c2a0915f05db4b450c050ce7165acd55f823fee51430a8c993e0bdbb192ed
    ```
    

- 最后，将顺序从小到大颠倒

    ```
    366c2a0915f05db4b450c050ce7165acd55f823fee51430a8c993e0bdbb192ed ->
    ed92b1db0b3e998c0a4351ee3f825fd5ac6571ce50c050b4b45df015092a6c36
    ```

    最终结果 `ed92b1db0b3e998c0a4351ee3f825fd5ac6571ce50c050b4b45df015092a6c36` 和 `"merkleroot":"ed92b1db0b3e998c0a4351ee3f825fd5ac6571ce50c050b4b45df015092a6c36"` 一致


## Merkle Tree 的作用

- 校验文件完整性，比如 `P2P` 网络

    在 `P2P` 网路中， `MerkleTree` 用来确保从其他节点接受的资料块没有损坏且没有被替换，甚至检查其他节点不会欺骗或者释出虚假的块。

- 快速比较大量数据

    当两个 `MerkleTree` 树根相同时，则意味着所代表的数据必然相同。

- BitCoin

    这样做的好处，也就是中本聪描述到的“简化支付验证”（`Simplified Payment Verification，SPV`）的概念:一个“轻客户端”（`light client`）可以仅下载链的区块头即每个区块中的 `80byte` 的资料块，仅包含五个元素，而不是下载每一笔交易以及每一个区块：
    - 上一区块头的哈希值 
    - 时间戳 
    - 挖矿难度值 
    - 工作量证明随机数（`nonce`）
    - 包含该区块交易的 `MerkleTree` 的 `root` 值
    
    如果客户端想要确认一个交易的状态，它只需简单的发起一个 `Merkleproof` 请求，这个请求显示出这个特定的交易在 `Merkletrees` 的一个之中，而且这个 `MerkleTree` 的树根在主链的一个区块头中。



## 参考

- [Bitcoin: A Peer-to-Peer Electronic Cash System](https://bitcoin.org/bitcoin.pdf)

- [Merkle Root (Cryptocurrency)](https://www.investopedia.com/terms/m/merkle-root-cryptocurrency.asp)

- [calculating-the-merkle-root-for-a-block](https://bitcoindev.network/calculating-the-merkle-root-for-a-block/)

- [How to manually verify the merkle root of a Bitcoin block (command line)](https://medium.com/coinmonks/how-to-manually-verify-the-merkle-root-of-a-bitcoin-block-command-line-7881397d4db1)