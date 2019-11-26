# 从0开始完成DApp（三）-实践篇

> http://truffleframework.com/tutorials/pet-shop

## 项目背景

Pete想做一个Dapp来卖他的宠物。

## 搭建项目环境

- [Truffle框架和Ganache本地私链](https://blog.csdn.net/myherux/article/details/80340095)

## 创建项目

- 创建项目目录

    ```
    mkdir pet-shop-tutorial

    cd pet-shop-tutorial
    ```

- 使用 `truffle unbox` 创建项目

    pet-shop项目里面包含一些接本的项目结构和前端代码，方便你专心于智能合约的开发。当然你也可以用 `truffle init` 创建一个空的项目。

    ```
    truffle unbox pet-shop
    ```

- 项目目录结构

    ![](http://of0qa2hzs.bkt.clouddn.com/20180517152655251941578.png)

    - `contracts/`: 智能合约的文件夹，所有的智能合约文件都放置在这里，里面包含一个重要的合约 `Migrations.sol`。

    - `migrations/`: 用来处理部署智能合约 ，迁移是一个额外特别的合约用来保存合约的变化。

    - `test/`: 智能合约测试用例文件夹。

    - `truffle.js/`: 配置文件。

## 编写智能合约

智能合约承担着分布式应用的后台逻辑和存储。

- 在contracts目录下，添加合约文件Adoption.sol

- 编写合约内容

    ```
    pragma solidity ^0.4.17;

    contract Adoption {
        address[16] public adopters;  // 保存领养者的地址
        // 领养宠物
        function adopt(uint petId) public returns (uint) {
            require(petId >= 0 && petId <= 15);  // 确保id在数组长度内
            adopters[petId] = msg.sender;        // 保存调用这地址 
            return petId;
        }
        // 返回领养者
        function getAdopters() public view returns (address[16]) {
            return adopters;
        }
    }
    ```

    - `pragma solidity ^0.4.17` 代表支持 `0.4.17` 版本以上的 `Solidity` 语言 `^` 代表以上

    - Address

        `Solidity` 是一种静态类型语言，意味着必须定义数据类型，如字符串，整数和数组。 `Solidity` 具有称为 `Address` 的独特类型。 `Address` 是以太坊地址，存储为 `20` 个字节的值。以太坊区块链上的每个账户和智能合约都有一个地址，并可以通过此地址发送和接收以太网。


