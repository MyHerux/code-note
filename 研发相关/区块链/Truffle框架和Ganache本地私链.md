# Truffle框架和Ganache本地私链

## 安装Truffle

> Truffle是一个世界级的开发环境，测试框架，以太坊的资源管理通道，致力于让以太坊上的开发变得简单。

- Github源码地址

    > https://github.com/trufflesuite/truffle

- 安装Npm和NodeJS

- 安装Truffle

    ```
    npm install -g truffle
    ```

## 创建工程

- 创建一个空工程

    ```
    truffle init
    ```

- 创建包含metacoin的工程

    新版本truffle引入了box的概念，所有的示例代码都以box的形式提供。下载metacoin的示例代码：

    ```
    truffle unbox metacoin
    ```

- 工程结构

    工程结构如图：

    ![](http://of0qa2hzs.bkt.clouddn.com/20180516152645842074616.png)

    - `contracts` 目录中包含 `Solidity` 合约代码，其中 `Migrations.sol` 是必须的，其他的是合约代码（这里是示例的 `MetaCoin` 代码）。

    - `migrations` 目录中包含合约部署脚本， `1_initial_migration.js` 用来部署 `Migrations.sol` ，其他的脚本会按照顺序依次执行。

    - `test` 目录中是测试代码。

- MetaCoin

    MetaCoin的代码主要实现了三个接口：发币，查看余额，查看Eth余额。

    ```js
    contract MetaCoin {
        mapping (address => uint) balances;

        event Transfer(address indexed _from, address indexed _to, uint256 _value);

        function MetaCoin() public {
            balances[tx.origin] = 10000;
        }

        function sendCoin(address receiver, uint amount) public returns(bool sufficient) {
            if (balances[msg.sender] < amount) return false;
            balances[msg.sender] -= amount;
            balances[receiver] += amount;
            Transfer(msg.sender, receiver, amount);
            return true;
        }

        function getBalanceInEth(address addr) public view returns(uint){
            return ConvertLib.convert(getBalance(addr),2);
        }

        function getBalance(address addr) public view returns(uint) {
            return balances[addr];
        }
    }
    ```

## 安装以太坊客户端Ganache

智能合约必须要部署到链上进行测试。可以选择部署到一些公共的测试链比如 `Rinkeby` 或者 `Ropsten` 上，缺点是：`部署和测试时间比较长`，`需要申请一些假的代币`。所以对于开发者，最好的方式是部署到私链上。
`Ganache`是​​您以太坊开发的个人区块链。他的前身是 `testRPC` ，很多旧的教程介绍的都是 `testRPC` 。

- GitHub源码地址

    > https://github.com/trufflesuite/ganache

- 图像界面

    `Ganache` 提供图像界面（挺好用的）的版本：

    > https://github.com/trufflesuite/ganache/releases

- 命令行版本

    ```
    npm install -g ganache-cli  
    ```

## 编译和部署合约

- `Ganache` 默认运行在本地 `7545` 端口，运行后默认创建10个账号，每个账号里有100ETH的余额。

    ![](http://of0qa2hzs.bkt.clouddn.com/201805161526461060219.png)

- 修改truffle.js

    要部署到链上，需要把IP、端口、网络ID告诉truffle。修改truffle.js：

    ```
    module.exports = {  
        networks: {  
            development: {  
                host: 'localhost',  
                port: '7545',  
                network_id: '*' // Match any network id  
            }  
        }  
    }; 
    ```
- 编译和部署

    ```
    truffle compile  
    truffle migrate
    ``` 
## 测试合约

- 测试内容

    MetaCoin的示例代码里已经把测试代码写好了，主要测试 `MetaCoin` 的接口是否可用:

    ```js
    contract TestMetacoin {

    function testInitialBalanceUsingDeployedContract() public {
        MetaCoin meta = MetaCoin(DeployedAddresses.MetaCoin());

        uint expected = 10000;

        Assert.equal(meta.getBalance(tx.origin), expected, "Owner should have 10000 MetaCoin initially");
    }

    function testInitialBalanceWithNewMetaCoin() public {
        MetaCoin meta = new MetaCoin();

        uint expected = 10000;

        Assert.equal(meta.getBalance(tx.origin), expected, "Owner should have 10000 MetaCoin initially");
    }

    }
    ```

- 测试合约

    直接输入测试合约的命令：

    ```
    truffle test
    ```

    结果显示5个测试都通过：

    ![](http://of0qa2hzs.bkt.clouddn.com/20180516152646197069199.png)

- Ganache

    查看Ganache上的运行结果：

    - Accounts标签：第一个账户里ETH略有减少，因为交易消耗了gas

    - Blocks标签：Ganache是自动挖矿，生成了6个新区块，每个区块里有一个交易

    - Transactions标签：有6笔新交易，可以点开看交易详情

    - Logs标签：显示交易和挖矿日志

    ![](http://of0qa2hzs.bkt.clouddn.com/20180516152646230427177.png)