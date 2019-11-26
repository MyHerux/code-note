# OpenZeppelin集成：编写健壮安全的智能合约

因为智能合约往往涉及金钱，保证Soldity代码没有错误，以及足够的安全是非常根本的。[Zeppelin Solutions](https://zeppelin.solutions/)，一个智能合约审查服务商，已经意识到相关的需求。建立在他们的合约审查经验之上，他们把一些最佳实践整理到了[OpenZeppelin](http://truffleframework.com/tutorials/robust-smart-contracts-with-openzeppelin)。

## 开箱即用的前端

开发的主要精力应该放在智能合约上。为达到这个目的，`Truffle` 以 `truffle box` 的方式提供了拆箱即用的前端。

- 下载tutorialtoken

    ```
    truffle unbox tutorialtoken
    ```

- 工程结构

    ![](http://of0qa2hzs.bkt.clouddn.com/20180517152652531971684.png)

- 集成 `OpenZeppelin`

    ```
    npm install openzeppelin-solidity
    ```
    安装完成之后可以在npm包中看到最新版本的OpenZeppelin：

    ![](http://of0qa2hzs.bkt.clouddn.com/201805171526525843950.png)

## 创建TutorialToken智能合约

使用已经搭建好的前端，我们可以关注于智能合约本身。

- 在 `contracts` 目录下，创建名为 `TutorialToken.sol` 的智能合约，内容如下：

    ```
    pragma solidity ^0.4.17;

    import 'openzeppelin-solidity/contracts/token/ERC20/StandardToken.sol';

    contract TutorialToken is StandardToken {

    }
    ```

    需要注意：

    - 除了标准的智能合约以外，我们还导入了StandardToken.sol合约并声明我们的TutorialToken

    - is StandardToken 代表继承了StandardToken合约中所有变量和函数。继承的合约可以被覆盖，只要在子类重定义对应的变量与函数就行了。

- 设置代币的参数，需要定义自己的 `name`， `symbol` ， `decimals` 和 `INITIAL_SUPPLY`

    ```
    string public name = 'TutorialToken';
    string public symbol = 'TT';
    uint8 public decimals = 2;
    uint public INITIAL_SUPPLY = 12000;
    ```

    - name和symbol给我们的token一个特有的身份

    - decimals定义了token可以细分的程度

    - INITIAL_SUPPLY定义了在合约部署时，代币将创建的数量

- 在构造函数中我们简单设置 `totalSupply` 来等于 `INITIAL_SUPPLY` ，同时把所有的币赋值给部署者的帐户

    ```
    function TutorialToken() {
        totalSupply = INITIAL_SUPPLY;
        balances[msg.sender] = INITIAL_SUPPLY;
    }
    ```

## 编译与部署智能合约

- 在 `/migrations` 目录下，用下述内容创建文件 `2_deploy_contracts.js`：

    ```
    var TutorialToken = artifacts.require("TutorialToken");

    module.exports = function(deployer) {
    deployer.deploy(TutorialToken);
    };
    ```

    TutorialToken合约内的import语句会由编译器进行自动处理，它会自动导入StandardToken内的相关引用包。

- 安装本地私链 `Ganache`

    [Truffle框架和Ganache本地私链](https://blog.csdn.net/myherux/article/details/80340095)

- 编译合约

    ```
    truffle compile
    ```

- 部署合约到私链上

    ```
    truffle migrate
    ```

    ![](http://of0qa2hzs.bkt.clouddn.com/20180517152652965323652.png)

    在Ganache上查看交易详情：

    ![](http://of0qa2hzs.bkt.clouddn.com/20180517152652981939829.png)
    
## 与新token交互 

- 前置

    [以太坊轻钱包MetaMask安装](https://blog.csdn.net/MyHerux/article/details/80310595)

- 本地的简单页面

    ```
    npm run dev
    ```

    ![](http://of0qa2hzs.bkt.clouddn.com/20180517152654075345506.png)

- 切换 `MetaMask` 到本地网络

    ![](http://of0qa2hzs.bkt.clouddn.com/20180517152654108186265.png)

- 用私钥导入本地账户

    导入本地环境的第一个账户（Ganache默认使用的第一个账户）

    ![](http://of0qa2hzs.bkt.clouddn.com/20180517152654128324674.png)

- 刷新页面，查看代币

    ![](http://of0qa2hzs.bkt.clouddn.com/20180517152654376555130.png)

- 给第二个账户转2000代币

    ![](http://of0qa2hzs.bkt.clouddn.com/20180517152654395675531.png)

- 切换到第二个账户，刷新页面，查看token

    ![](http://of0qa2hzs.bkt.clouddn.com/20180517152654414191368.png)