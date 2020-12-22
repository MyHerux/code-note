# VSCode插件开发

## 工具准备

- 安装Node.js

- 升级Npm

    需要保证 `npm` 是最新的版本，不然安装 `Yeoman` 可能出错
    > npm i -g npm

- 安装Yeoman

    > npm install -g yo generator-code

## 编码准备

- 生成插件

    最先是使用 `Win10` 来安装 `Yeoman`，然后各种问题，特别是无法命名继续往下面执行，所以改用 `Ubuntu`，结果一切正常。

    已经安装了 `Yeoman`，所以直接命令：
    > yo code

    ![](http://of0qa2hzs.bkt.clouddn.com/%5BP%7D%7BCI@Z2M3YBB%5BC1UR8$BV.png)

    选择插件类型`Typescript`。然后顺序执行：

    ![](http://of0qa2hzs.bkt.clouddn.com/B%25Y0X8%7BJ%25J~%28GLB$%60JQ$YNV.png)

- 调试插件

  - 进入刚建的文件目录
    > cd X

  - 使用VSCode打开项目
    > code .
  - 按F5进入调试模式

    一个新的VS Code实例将以一种特殊的模式(Extension Development Host)启动，这个新的实例是可以使用你的插件的。
  - 测试插件

    在新的VS Code实例命令窗口输入：`Hello World` 命令。可以看到有一个自动输出。

## Typescript语法

  略。

## 开始写第一个插件

- 插件结构

```
.
├── .gitignore
├── .vscode                     // VS Code 集成配置
│   ├── launch.json
│   ├── settings.json
│   └── tasks.json
├── .vscodeignore
├── README.md
├── src                         // 源码
│   └── extension.ts			// 如果是JavaScript插件，那么此处就是extension.js
├── test                        // 测试文件夹
│   ├── extension.test.ts	   // 如果是JavaScript插件，那么此处就是extension.test.js
│   └── index.ts	            // 如果是JavaScript插件，那么此处就是index.js
├── node_modules
│   ├── vscode                  // 语言服务
│   └── typescript              // typescript编译器(仅TypeScript插件才有)
├── out                         // 编译结果(仅TypeScript插件才有)
│   ├── src
│   |   ├── extension.js
│   |   └── extension.js.map
│   └── test
│       ├── extension.test.js
│       ├── extension.test.js.map
│       ├── index.js
│       └── index.js.map
├── package.json                // 插件的清单
├── tsconfig.json               // 如果是JavaScript插件，那么此处就是jsconfig.json
├── typings                     // 类型定义文件
│   ├── node.d.ts               // 链接到Node.js的API
│   └── vscode-typings.d.ts     // 链接到VS Code的API
└── vsc-extension-quickstart.md // 插件开发快速入门文档
```

- 插件清单：`package.json`

  名称 | 是否必要 | 类型 | 说明
  ---- |:--------:| ---- | -------
  `name` | 是 | `string` | 扩展的名称，该名称必须为小写且不能有空格。
  `version` | 是 | `string` | [SemVer](http://semver.org/) 兼容版本.
  `publisher` | 是 | `string` | 发布人名字
  `engines` | 是 | `object` | 一个至少包含`vscode`键值对的对象，该键表示的是本扩展可兼容的VS Code的版本，其值不能为`*`。比如 `^0.10.5` 表示扩展兼容VS Code的最低版本是`0.10.5`。
  `license` | 否 | `string` | 参考 [npm's 文档](https://docs.npmjs.com/files/package.json#license). 如果你确实需要在扩展根目录下有一个授权文档，那么应该把`license`值设为`"SEE LICENSE IN <filename>"`。
  `displayName` | 否 | `string`| 用于在扩展市场中本扩展显示的名字。
  `description` | 否 | `string` | 一份简短的说明，用来说明本插件是什么以及做什么
  `categories` | 否 | `string[]` | 你希望你的扩展属于哪一类，只允许使用这几种值：`[Languages, Snippets, Linters, Themes, Debuggers, Other]`
  `keywords` | 否 | `array` | 一组 **关键字** 或者 **标记**，方便在市场中查找。
  `galleryBanner` | 否 | `object` | 帮助格式化市场标题以匹配你的图标，详情如下。
  `preview` | 否 | `boolean` | 在市场中把本扩展标记为预览版本。
  `main` | 否 | `string` | 扩展的入口点。
  [`contributes`](/docs/extensionAPI/extension-points.md) | 否 | `object` | 一个描述扩展贡献点的对象。
  [`activationEvents`](/docs/extensionAPI/activation-events.md) | 否 | `array` | 一组用于本扩展的激活事件。
  `dependencies` | 否 | `object` | 你的扩展所需的任何运行时的Node.js依赖项，和 [npm's `dependencies`](https://docs.npmjs.com/files/package.json#dependencies)一样。
  `devDependencies` | 否 | `object` | 你的扩展所需的任何开发的Node.js依赖项. 和 [npm's `devDependencies`](https://docs.npmjs.com/files/package.json#devdependencies)一样。
  `extensionDependencies` | 否 | `array` | 一组本扩展所需的其他扩展的ID值。扩展的ID值始终是 `${publisher}.${name}`。比如：`vscode.csharp`。
  `scripts` | 否 | `object` | 和 [npm's `scripts`](https://docs.npmjs.com/misc/scripts)一样.
  `icon` | 否 | `string` | 一个128x128像素图标的路径。

- Generated Code

  `extension.ts` :

  ```javascript
  // 'vscode'模块包含了VS Code插件API
  // 导入模块并且在下面你的代码中用vscode的别名引用这个模块
  import * as vscode from 'vscode';

  // 这个函数将在你的插件被激活时被调用
  // 你的插件在第一次被执行命令的时候被激活
  export function activate(context: vscode.ExtensionContext) {

	  // 使用控制台去输出诊断信息(console.log)和错误信息(console.error)
	  // 只有当你的插件被激活时才会执行下面这行代码
	  console.log('Congratulations, your extension "my-first-extension" is now active!');

	  // 这条命令被定义在package.json文件里
	  // 现在使用registerCommand来提供这条命令的实现
	  // commandId参数必须和package.json文件中的command成员匹配
	  var disposable = vscode.commands.registerCommand('extension.sayHello', () => {
		  // 每次命令被执行的时候都将执行你这里的代码

		  // 向用户显示一个消息提示框
		  vscode.window.showInformationMessage('Hello World!');
	  });
	
	  context.subscriptions.push(disposable);
  }
  ```

  - 插件激活  Extension Activation

    * VS Code插件开发实例发现插件然后读取插件的`package.json`文件。
    * 然后当你按下`kb(workbench.action.showCommands)`时：
      * 注册的命令被现实在命令面板里。
      * 在出现的命令列表中有一个我们在`package.json`文件中定义的`"Hello world"`条目。
    * 当我们选择了`"Hello world"`条目时:
      * `"extension.sayHello"`命令被调用:
        * 一个`"onCommand:extension.sayHello"`激活事件被创建了出来。
        * 所有在`activationEvents`成员中监听这个激活事件的插件将被激活。
          * `./out/src/extension.js`文件被JavaScript虚拟机加载。
          * VS Code寻找其中的的导出函数`activate`并且调用它。
          * `"extension.sayHello"`命令被注册并且定义了这条命令的具体实现。
      * `"extension.sayHello"`命令的实现函数被调用。
      * 这条命令的具体实现显示一条"Hello World"消息。

## 项目地址

[vscode-pomodoro](https://github.com/MyHerux/vscode-pomodoro)