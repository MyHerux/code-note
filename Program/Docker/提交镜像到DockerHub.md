# 提交镜像到DockerHub

- pre

    注册 `dockerhub` 账号

- 登录

    ```
    docker login
    ```

    ![](http://cdn.heroxu.com/20190801156465366122767.png)

- 查看当前镜像，选择一个比较小的image

    ```
    docker images|grep node
    ```

    ![](http://cdn.heroxu.com/20190801156465369750974.png)

- 标记本地镜像，将其归入远程仓库

    ```
    docker tag node:9.11.1-alpine skycitygalaxy/node:v1
    ```

- push镜像

    ```
    docker push skycitygalaxy/node
    ```

    ![](http://cdn.heroxu.com/2019080115646542367565.png)

- 查看仓库中的镜像

    ![](http://cdn.heroxu.com/20190801156465945393031.png)