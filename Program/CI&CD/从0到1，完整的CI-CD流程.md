# 从0到1，完整的CI-CD流程

## 1. 项目的基本配置

- OverView

    项目使用 `dev` ， `qa` ， `prod` 三套不同的环境，具体可以参考项目代码。

## 2. 完整的Build脚本

#### 2.1. 使用maven打包

- `docker-compose-maven.yml`

    ```
    version: "3.7"
    networks:
        build:
    services:
        maven:
            image: maven:3.6.0-jdk-8
            working_dir: /ci-cd-demo
            volumes:
                - ../:/ci-cd-demo
                - ~/m2/repository:/root/.m2/repository
                command: mvn -Pprod -DskipTests=true package -f pom.xml
            networks:
                - build
    ```

- 执行 `docker-compose` 命令

    `$ docker-compose -f provisioning/docker-compose-maven.yml run --rm maven`

    运行结果：

    ![](http://cdn.heroxu.com/20190918156877799037354.png)

- 直接运行，检查 `jar` 包是否正常

    `$ java -jar ci-cd-demo.jar`

    运行结果：  

    ![](http://cdn.heroxu.com/20190918156877771978453.png)

- 请求接口

    `http://localhost:8080/test`

    返回数据：X_TEST_CONFIG

    说明使用的 `prod` 的配置，但是变量没有被替换

#### 2.2. 编写 `Dockerfile` 

- OverView

    有了应用的 `jar` 包，可以开始编译镜像了，实际上镜像里面就是一个 `java` 程序来执行 `jar` 包，但是我们需要替换一些配置。

- `provisioning/Dockerfile`

    ```
    FROM java:openjdk-8-jdk
    ARG project=ci-cd-demo
    ARG profile=prod

    ENV PATH=.:$PATH

    WORKDIR /opt/ci-cd-demo


    COPY ./target/ci-cd-demo.jar                 /opt/ci-cd-demo/ci-cd-demo.jar
    COPY ./target/classes/application.yml        /opt/ci-cd-demo/application.yml
    COPY ./provisioning/docker-entrypoint.sh     /opt/ci-cd-demo/docker-entrypoint.sh

    EXPOSE 8080
    VOLUME /opt/ci-cd-demo/logs
    VOLUME /opt/ci-cd-demo/data

    ENTRYPOINT ["docker-entrypoint.sh"]
    CMD [""]
    ```

- `docker-entrypoint.sh`

    ```
    #!/bin/bash

    profile="${profile}"
    project="${project}"

    JAVA_OPT="${JAVA_OPT:=-Xmx2g}"

    X_TEST_CONFIG="${X_TEST_CONFIG}"

    config(){
        if [[ -z "${X_TEST_CONFIG}" ]]; then
            echo -e "\n\"X_TEST_CONFIG\"  can not be empty!\n"
        else
            sed -i "s#X_TEST_CONFIG#${X_TEST_CONFIG}#g" /opt/${project}/application.yml
        fi
    }

    config

    exec java ${JAVA_OPT} -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:./logs/gc-$(date +%F).log -Dfile.encoding=utf-8 -jar ${project}.jar --spring.config.location=application.yml --spring.profiles.active=${profile} ${@}
    ```

#### 2.3 编写 `docker-compose` ，构建镜像

- OverView

    准备好了 `Dockerfile` ，可以通过 `docker-compose` 来 `build` 应用了。

- `docker-compose.yml`

    ```
    version: "3.7"
    networks:
        netdci:

    services:
        ci-cd-demo:
            build:
                context: ./
                dockerfile: ./provisioning/Dockerfile
                args:
                    - project=ci-cd-demo
                    - profile=prod
            image: skycitygalaxy/ci-cd-demo
            environment:
                - project=ci-cd-demo
                - profile=prod
                - X_TEST_CONFIG=xu
            volumes:
                - /opt/logs/ci-cd-demo/:/opt/ci-cd-demo/logs/
                - /etc/hosts:/etc/hosts
            ports:
                - 17001:8080
            networks:
                - netdci
            restart: always
    ```

- 直接执行命令

    `docker-compose build`

    ![](http://cdn.heroxu.com/20190920156896341020629.png)

#### 2.4 上传镜像

- OverView

    把本地的镜像上传到我们的镜像仓库，这里使用 `Dockerhub` 的仓库

- Tag

    `docker tag skycitygalaxy/ci-cd-demo:latest skycitygalaxy/ci-cd-demo:v1`

- Push

    `docker push skycitygalaxy/ci-cd-demo:v1`

#### 2.5 使用脚本来快捷触发操作

- `docker.sh`

    ```
     #!/bin/bash

    PS4='+ $(date +"%F %T%z") ${BASH_SOURCE}:${LINENO}): ${FUNCNAME[0]:+${FUNCNAME[0]}(): }'

    #set -xue
    set -x


    VERSION="release-v"

    AP_PROJECT="ci-cd-demo"
    AP_REGISTRY="skycitygalaxy"
    AP_NAMESPACE="ci-cd-demo"


    cd "$(dirname $0)"
    basedir="$(dirname $(pwd))"
    cd ${basedir}


    docker_build() {
        git log -n 1|head -3                     >  VERSION
        echo -e "Build:\t$(date '+%F %T')"       >> VERSION
        echo -e "Branch:\t$(git branch|grep ^*)" >> VERSION

        docker images | grep ${AP_PROJECT} | awk '{print $3}' | xargs docker rmi  -f
        docker-compose -f provisioning/docker-compose-maven.yml run --rm maven
        docker-compose build
    }


    docker_push() {
        tag="$1"
        [[ "$tag" == "" ]] && tag="${VERSION}-$(git rev-parse --short HEAD)"
        for i in $(docker images | grep "${AP_REGISTRY}/${AP_NAMESPACE}" | awk '{print $1}');
        do
            docker tag ${i} ${i}:${tag};
            docker push $i:${tag};
            docker push $i;
            docker rmi $i:${tag};
        done
    }


    docker_$1
    ```

- build

    `./provisioning/docker.sh build`   

- push

    `./provisioning/docker.sh push`   


## 3. 使用Jenkins实现自动部署

- Overview

    参考 -> [使用Jenkins实现自动部署](./Jenkins/使用Jenkins实现自动部署.md)


## 4. 在Rancher上部署新的应用

- 配置服务

    ![](http://cdn.heroxu.com/20190920156896427441678.png)

- 测试服务

    ![](http://cdn.heroxu.com/20190920156896436878741.png)

    返回数据：xu

    说明返回了正确的配置数据

## 5. 项目Demo

- Overview

    参考 -> [ci-cd-demo](https://github.com/MyHerux/ci-cd-demo)