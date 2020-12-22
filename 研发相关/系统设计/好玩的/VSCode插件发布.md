---
title: %s
tags: %s
keywords: 帐户,的
---
# VSCode插件发布

## 获取 Personal Access Token（PAT）

- 注册账号

     [Visual Studio Team Services](https://www.visualstudio.com/zh-hans/team-services/?rr=https%3A%2F%2Fdocs.microsoft.com%2Fzh-cn%2Fvsts%2Faccounts%2Fcreate-account-msa-or-work-student)

- 创建 `Visual Studio Team Services` 的的帐户户后

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171130/GaDHKgbDF4.png?imageslim)

- 创建PAT

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171130/3mbm1E371H.png?imageslim)

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171130/5J5IiCebb5.png?imageslim)

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/171130/6IBHl3h86A.png?imageslim)

    下一个屏幕将显示您新创建的个人访问令牌。复制并保持（密码不会被 `Visual Studio Team Services` 永久保存，需要自己保存），你将需要它来创建一个发布者。

## 创建发布者

- 创建新的发布者

    > vsce create-publisher (publisher name)
    
- 登录发布者账号

    > vsce create-publisher (publisher name)

## 发布插件

- 安装 `vsce`

    > npm install -g vsce

- 使用 `vsce` 发布插件

    ```
    $ vsce publish
    Publishing uuid@0.0.1...
    Successfully published uuid@0.0.1!
    ```