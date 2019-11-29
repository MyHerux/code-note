# 使用 remix-ide 开发以太坊智能合约

## Remix-ide 搭建

> 以太坊官方推荐的智能合约开发IDE，适合新手，可以在浏览器中快速部署测试智能合约。

- 在线编译

    > 线上地址：https://remix.ethereum.org/


- 本地安装 remix-ide

    > 官方地址: https://github.com/ethereum/remix-ide

    npm安装（需要先安装 npm+node.js）：

    ```
    sudo npm install remix-ide -g
    remix-ide
    ```

    clone仓库安装（安装过程中发现需要wget）

    ```
    git clone https://github.com/ethereum/remix-ide.git
    cd remix-ide
    npm install
    npm run setupremix  # this will clone https://github.com/ethereum/remix for you and link it to remix-ide
    npm start
    ```

    服务启动后访问： http://localhost:8080 就可以看到IDE了。

    ![](http://cdn.heroxu.com/20180515152636521395163.png)

## Remix的使用

- 创建新的合约

    Ide有一个默认的投票的合约，现在创建一个更简单的示例合约。

    ![](http://cdn.heroxu.com/20180515152636542078931.png)

- 新合约

    合约内容：

    ```js
    pragma solidity ^0.4.0;

    contract SampleStorage {
        
        uint data;
        
        function setData(uint x) public{
            
            data = x;
        }
        
        function getData() constant public returns (uint) {
            
            return data;
        }
    }
    ```

    `solidity` 的版本是 `^0.4.0`。^代表的意思是支持 0.4.0 到0.5.0 不包括0.5.0之间的版本。

- 编译合约

    点击编译，或者勾选自动编译（每次修改之后会自动编译），下面会出现编译好的合约。

    ![](http://cdn.heroxu.com/20180515152636593745989.png)

- 部署合约

    编译好的合约可以部署到以太坊的区块链上：

    - Envroment是合约的运行环境，默认的是Injected web3. 你也可以选择其他的环境。
    - Account是当前调用和月的地址（MetaMask上默认的账户地址，也可以自己再创建）。
    - Gas limit是调用合约所准备的gas。
    - value 是我们可以给合约账户转账的金额。

    ![](http://cdn.heroxu.com/20180515152636620276604.png)

- 选择测试网络

    - 直接发布的合约会直接放到主网（main network）上：

        ![](http://cdn.heroxu.com/20180515152636674691311.png)

    - 发布到主网的合约都需要支付Eth，所以修改MetaMask的网络，选择测试网络：

        ![](http://cdn.heroxu.com/20180515152636684669880.png)

    - 测试网络点击buy可以申请测试用的Eth：

        ![](http://cdn.heroxu.com/20180515152636976398544.png)

- 支付Eth，提交合约

    申请到测试用的Eth之后就可以提交合约了：

    ![](http://cdn.heroxu.com/20180515152636985213983.png)

- 查看区块链上的合约

    - 点击合约的交易记录查看合约信息：

        ![](http://cdn.heroxu.com/20180515152636999213084.png)

    - 区块链上的合约信息：

        ![](http://cdn.heroxu.com/20180515152637008071090.png)

## 发行TOKEN

- 代币合约

    代币合约的范例很多，[Ethereum](https://www.ethereum.org/token) 官网有提供一个最小可执行的代币合约（MINIMUM VIABLE TOKEN）：

    ```
    pragma solidity ^0.4.20;

    contract MyToken {
        /* This creates an array with all balances */
        mapping (address => uint256) public balanceOf;

        /* Initializes contract with initial supply tokens to the creator of the contract */
        function MyToken(
            uint256 initialSupply
            ) public {
            balanceOf[msg.sender] = initialSupply;              // Give the creator all initial tokens
        }

        /* Send coins */
        function transfer(address _to, uint256 _value) public {
            require(balanceOf[msg.sender] >= _value);           // Check if the sender has enough
            require(balanceOf[_to] + _value >= balanceOf[_to]); // Check for overflows
            balanceOf[msg.sender] -= _value;                    // Subtract from the sender
            balanceOf[_to] += _value;                           // Add the same to the recipient
        }
    }
    ```

    `MyToken` 的合约只能做两件事：

    - 创建代币：发起合约时创建指定数量的代币，代币拥有者是发起合约的 Ethereum 帐户

    - 转移代币：转移指定数量的代币到指定的 Ethereum 帐户
    
    一个完整的代币合约需要的要素：[ERC20 Token使用手冊](https://medium.com/taipei-ethereum-meetup/erc20-token%E4%BD%BF%E7%94%A8%E6%89%8B%E5%86%8A-3d7871c58bea)。

- 部署合约，发行100000个币

    - 填入需要发行的币：100000个，部署合约：

        ![](http://cdn.heroxu.com/20180515152637156174485.png)

    - 查看合约内容

        `Remix` 会自动根据合约的內容，产生对应的合约使用界面。可以看到合约有两个功能：`balanceOf`(查询余额) 和 `transfer`(转移代币)。

        ![](http://cdn.heroxu.com/20180515152637209747548.png)

- 执行合约

    查看我账号上的代币余额：

    ![](http://cdn.heroxu.com/20180515152637221843132.png)

