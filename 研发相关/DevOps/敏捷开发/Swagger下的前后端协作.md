# Swagger下的前后端协作

## Swagger的使用

- 官方网站

    > [Github-Swagger-samples](https://github.com/swagger-api/swagger-samples)

- 引入依赖

    ```
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
            <version>2.7.0</version>
        </dependency>
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger-ui</artifactId>
            <version>2.7.0</version>
        </dependency>
    ```

- 开启注解

    > 在 `Application` 上添加注解 `@EnableSwagger2`

## 使用范围

- 所有前后端分离的接口

    >满足契约测试标准，后端所有接口信息通过注解定义。前端通过swagger测试接口与开发。

- 所有api接口（内部使用的）

## 具体使用

- 需要注解的字段

    -  `ApiOperation`
    
        > @ApiOperation(value = "风险预警-设置阈值", notes = risk)

        因为使用前后端分离，推荐所有参数为 `json格式` ，所以在 `notes` 里面注明 `需要 Mock 的静态输入`。
    - `ApiResponse` 
    
        > 返回的状态码与数据

- 使用方式

    前端访问 `http://localhost:8080/swagger-ui.html` ，即可使用所有的 `Mock接口`。

## 开发模式

> 虽然原则上在设计之初就应该定好接口的输入输出，实际开发中仍然无法避免接口变动。

- 明确的输入输出

    对于明确的接口，后端可以优先定义好接收的方法参数和输出的json，通过静态变量的方式提供mock数据给前端以供前端开发。

- 不明确的接口

    对于不明确的接口，建议直接开发，在开发的过程中掌握细节，提供稳定的完整接口给前端。