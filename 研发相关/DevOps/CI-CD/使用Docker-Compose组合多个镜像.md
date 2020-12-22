# 使用Docker-Compose组合多个镜像

## Docker for Mac

- 官网安装

    [官网地址](https://docs.docker.com/docker-for-mac/)，直接下载 `.dmg` 文件安装即可，[更多介绍](https://docs.docker.com/docker-for-mac/install/)。

- 使用 Homebrew 安装

    > brew cask install docker

- Check versions

    ```
    $ docker --version
    Docker version 18.03, build c97c6d6

    $ docker-compose --version
    docker-compose version 1.22.0, build 8dd22a9

    $ docker-machine --version
    docker-machine version 0.14.0, build 9ba6da9
    ```

## Docker Compose

> [Get started with Docker Compose](https://docs.docker.com/compose/gettingstarted/#prerequisites)

### Step 1: Setup

定义应用程序依赖项。

1. 为项目创建一个目录：

    ```
    $ mkdir composetest
    $ cd composetest
    ```

2. 在项目目录中创建一个名为 `app.py` 的文件，并将下面 👇 内容粘贴到其中：

    ```
    import time

    import redis
    from flask import Flask


    app = Flask(__name__)
    cache = redis.Redis(host='redis', port=6379)


    def get_hit_count():
        retries = 5
        while True:
            try:
                return cache.incr('hits')
            except redis.exceptions.ConnectionError as exc:
                if retries == 0:
                    raise exc
                retries -= 1
                time.sleep(0.5)


    @app.route('/')
    def hello():
        count = get_hit_count()
        return 'Hello World! I have been seen {} times.\n'.format(count)

    if __name__ == "__main__":
        app.run(host="0.0.0.0", debug=True)
    ```

    在此示例中，`redis` 是应用程序网络上redis容器的主机名。我们使用Redis的默认端口，`6379`。

3. 在项目目录中创建另一个名为 `requirements.txt` 的文件，并将下面 👇 内容粘贴到其中：

    ```
    flask
    redis
    ```


### Step 2: Create a Dockerfile

在此步骤中，您将编写一个构建 `Docker` 镜像的 `Dockerfile` 。该图像包含 `Python` 应用程序所需的所有依赖项，包括 `Python` 本身。 

在项目目录中，创建名为 `Dockerfile` 的文件并粘贴以下内容：

```
FROM python:3.4-alpine
ADD . /code
WORKDIR /code
RUN pip install -r requirements.txt
CMD ["python", "app.py"]
```

- FROM : 使用 `Python 3.4` 映像开始构建映像

- ADD : 添加当前目录 `.` 到镜像 `/code` 目录

- WORKDIR : 将工作目录设置为 `/code`

- RUN : 运行 `pip` 命令，安装Python依赖项

- CMD : 将容器的默认命令设置为 `python` `app.py` .

有关如何编写 `Dockerfiles` 的更多信息，请参阅 [Docker用户指南](https://docs.docker.com/get-started/) 和 [Dockerfile参考](https://docs.docker.com/engine/reference/builder/)。


### Step 3: Define services in a Compose file

在项目目录中创建名为 `docker-compose.yml` 的文件并粘贴以下内容：

```
version: '3'
services:
  web:
    build: .
    ports:
     - "5000:5000"
  redis:
    image: "redis:alpine"
```

- version: docker-compose版本

- services: 包含的服务

    - web: 用户自己自定义，它就是单个的服务名称 

    - image: 指定服务的镜像名称或镜像 `ID` 。如果镜像在本地不存在， `Compose` 将会尝试拉取这个镜像。

    - build: 使用 `up` 启动之时执行构建任务


### Step 4: Build and run your app with Compose

1. 从项目目录中，通过运行启动应用程序

    > docker-compose up

    ```
    $ docker-compose up
    Creating network "composetest_default" with the default driver
    Creating composetest_web_1 ...
    Creating composetest_redis_1 ...
    Creating composetest_web_1
    Creating composetest_redis_1 ... done
    Attaching to composetest_web_1, composetest_redis_1
    web_1    |  * Running on http://0.0.0.0:5000/ (Press CTRL+C to quit)
    redis_1  | 1:C 17 Aug 22:11:10.480 # oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo
    redis_1  | 1:C 17 Aug 22:11:10.480 # Redis version=4.0.1, bits=64, commit=00000000, modified=0, pid=1, just started
    redis_1  | 1:C 17 Aug 22:11:10.480 # Warning: no config file specified, using the default config. In order to specify a config file use redis-server /path/to/redis.conf
    web_1    |  * Restarting with stat
    redis_1  | 1:M 17 Aug 22:11:10.483 * Running mode=standalone, port=6379.
    redis_1  | 1:M 17 Aug 22:11:10.483 # WARNING: The TCP backlog setting of 511 cannot be enforced because /proc/sys/net/core/somaxconn is set to the lower value of 128.
    web_1    |  * Debugger is active!
    redis_1  | 1:M 17 Aug 22:11:10.483 # Server initialized
    redis_1  | 1:M 17 Aug 22:11:10.483 # WARNING you have Transparent Huge Pages (THP) support enabled in your kernel. This will create latency and memory usage issues with Redis. To fix this issue run the command 'echo never > /sys/kernel/mm/transparent_hugepage/enabled' as root, and add it to your /etc/rc.local in order to retain the setting after a reboot. Redis must be restarted after THP is disabled.
    web_1    |  * Debugger PIN: 330-787-903
    redis_1  | 1:M 17 Aug 22:11:10.483 * Ready to accept connections
    ```

    `Compose` 拉取 `Redis` 图像，为您的代码构建图像，并启动您定义的服务。在这种情况下，代码在构建时静态复制到映像中。

2. 在浏览器中输入http://0.0.0.0:5000/以查看应用程序是否正在运行。

    应该在浏览器中看到一条消息：

    ```
    Hello World! I have been seen 1 times.
    ```

3. 刷新页面。

    数字应该增加:

    ```
    Hello World! I have been seen 2 times.
    ```

4. 切换到另一个终端窗口，键入 `docker image ls` 列出本地图像。 此时列出图像应该返回 `redis` 和 `web` 。

    ```
    $ docker image ls
    REPOSITORY              TAG                 IMAGE ID            CREATED             SIZE
    composetest_web         latest              e2c21aa48cc1        4 minutes ago       93.8MB
    python                  3.4-alpine          84e6077c7ab6        7 days ago          82.5MB
    redis                   alpine              9d8fa9aa0e5b        3 weeks ago         27.5MB
    ```

    您可以使用 `docker inspect <tag或id>` 检查镜像。

5. 通过在第二个终端的项目目录中输入 `docker-compose down`，或者在启动应用程序的原始终端中按 `CTRL + C` 来停止应用程序。


### Step 5: Edit the Compose file to add a bind mount

编辑项目目录中的 `docker-compose.yml`，为 `Web` 服务添加 [bind mount](https://docs.docker.com/storage/bind-mounts/#choosing-the--v-or---mount-flag)：

```
version: '3'
services:
  web:
    build: .
    ports:
     - "5000:5000"
    volumes:
     - .:/code
  redis:
    image: "redis:alpine"
```

新添加的 `volumes` 关键字将主机上的项目目录（当前目录）安装到容器内的 `/code`，允许您动态修改代码，而无需重建映像。

### Step 6: Re-build and run the app with Compose

在项目目录中，输入 `docker-compose` 以使用更新的 `Compose` 文件构建应用程序，然后运行它。

```
$ docker-compose up
Creating network "composetest_default" with the default driver
Creating composetest_web_1 ...
Creating composetest_redis_1 ...
Creating composetest_web_1
Creating composetest_redis_1 ... done
Attaching to composetest_web_1, composetest_redis_1
web_1    |  * Running on http://0.0.0.0:5000/ (Press CTRL+C to quit)
...
```

再次在Web浏览器中检查Hello World消息，然后刷新以查看计数增量。

### Step 7: Update the application

由于应用程序代码现在使用卷安装到容器中，因此您可以更改其代码并立即查看更改，而无需重建镜像。

1. 在 `app.py` 中更改问候语并保存。例如，更改 `Hello World!` 为 `Hello from Docker!`：

    ```
    return 'Hello from Docker! I have been seen {} times.\n'.format(count)
    ```

2. 在浏览器中刷新应用程序。问候语应该更新，计数器仍然应该递增。

### Step 8: Experiment with some other commands

如果要在后台运行服务，可以将 `-d` 标志（用于“分离”模式）传递给 `docker-compose` 并使用 `docker-compose ps` 查看当前正在运行的内容：

```
$ docker-compose up -d
Starting composetest_redis_1...
Starting composetest_web_1...

$ docker-compose ps
Name                 Command            State       Ports
-------------------------------------------------------------------
composetest_redis_1   /usr/local/bin/run         Up
composetest_web_1     /bin/sh -c python app.py   Up      5000->5000/tcp
```

`docker-compose run` 命令允许您为服务运行一次性命令。例如，要查看 `Web` 服务可用的环境变量：

> $ docker-compose run web env

参阅 `docker-compose --help` 以查看其他可用命令。您还可以为 `bash` 和 `zsh shell` 安装 [command completion](https://docs.docker.com/compose/completion/)，它还会显示可用的命令。

如果您使用 `docker-compose up -d` 启动 `Compose` ，请在完成后停止服务：

> $ docker-compose stop

您可以使用 `down` 命令将所有内容放下，完全删除容器。添加 `--volumes` 参数也可以删除 `Redis` 容器使用的数据量：

> $ docker-compose down --volumes