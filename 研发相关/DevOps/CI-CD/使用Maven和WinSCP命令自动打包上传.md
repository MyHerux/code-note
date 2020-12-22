# 使用Maven和WinSCP命令自动打War包上传

## Windows批处理命令

- ECHO 和 @

  - 打开回显或关闭回显功能

      > 格式：`echo [{ on|off }]`。如果想关闭“ECHO OFF”命令行自身的显示，则需要在该命令行前加上“@”。

  - 显示当前ECHO设置状态

      > 格式: `echo`

  - 输出提示信息 

      > 格式：`echo 信息内容`

- PAUSE

  - 停止系统命令的执行并显示

      > 格式：`pause` 响应：请按任意键继续. . .

- CALL

  - 批处理执行过程中调用另一个批处理，当另一个批处理执行完后，再继续执行原来的批处理
  
      > 格式：`call command`

- 更多命令

    [参考](http://www.cnblogs.com/iTlijun/p/6137027.html)

## WinSCP

- 下载

- 修改环境变量

- 命令

    - call 执行任意远程Shell命令
    - cd 改变远程工作目录
    - chmod 改变远程文件权限
    - close 关闭会话
    - exit 关闭所有会话并结束程序
    - get 从远程目录下载文件到本地目录
    - help 显示帮助
    - keepuptodate 在一个远程目录连续反映本地目录的改变
    - lcd 改变本地工作目录
    - lls 列出本地目录的内容
    - ln 新建远程符号链接
    - lpwd 显示本地工作目录
    - ls 列出远程目录的内容
    - mkdir 新建远程目录
    - mv 移动或者重命名远程文件
    - open 连接到服务器
    - option 设置或显示脚本选项的值
    - put 从本地目录上传文件到远程目录
    - pwd 显示远程工作目录
    - rm 删除远程文件
    - rmdir 删除远程目录
    - session 列出连接的会话或者选择活动会话
    - synchronize 用一个本地目录同步远程目录

## Maven命令

- mvn clean
  > 清除先前构建的 `artifacts`
- mvn validate
  > 验证工程是否正确，所有需要的资源是否可用
- mvn compile
  > 编译项目的源代码
- mvn test
  > 使用合适的单元测试框架来测试已编译的源代码。这些测试不需要已打包和布署。
- mvn package
  > 把已编译的代码打包成可发布的格式
- mvn verify
  > 运行所有检查，验证包是否有效且达到质量标准
- mvn install
  > 把包安装在本地的repository中，可以被其他工程作为依赖来使用。
- mvn site
  > 为项目生成文档站点。
- mvn deploy
  > 在集成或者发布环境下执行，将最终版本的包拷贝到远程的repository，使得其他的开发者或者工程可以共享。
- mvn war:war
  > 插件命令，将项目打成war包

## Bat脚本

  使用start.bat，先用mvn打包，然后再调用upload.bat上传war包。

- start.bat

  ```
    echo
    e:
    cd \Blacklist\Pcredit
    call mvn install -DskipTests=true
    pause
    call WinSCP.com /script=\Blacklist\Pcredit\upload.bat
    pause
  ```
- upload.bat

  ```
    option confirm off
    open user:pwd@服务器ip
    put E:\Blacklist\Pcredit\target\credit.war
    close
    exit
  ```