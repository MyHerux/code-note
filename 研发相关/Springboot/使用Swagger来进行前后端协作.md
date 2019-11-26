# 使用 Swagger 来进行前后端协作

## 配置 Swagger 

- 文档与 Github 库

    - [Swagger规范](https://github.com/swagger-api)

    - [基于 Spring 生态系统的 Swagger 规范的实现：springfox-swagger2](https://github.com/springfox/springfox/tree/master/springfox-swagger2)

    - [封装好的UI：springfox-swagger-ui](https://github.com/springfox/springfox/tree/master/springfox-swagger-ui)

    - [官方英文文档：Springfox Reference Documentation](http://springfox.github.io/springfox/docs/current/)

- 引入依赖

    ```
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger2</artifactId>
        <version>2.8.0</version>
    </dependency>

    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-swagger-ui</artifactId>
        <version>2.8.0</version>
    </dependency>
    ```

- 开启注解

    在 `Application` 上添加注解 `@EnableSwagger2`

## 具体使用方式

- 需要注解的字段

    -  `ApiOperation`

        因为使用前后端分离，推荐所有参数为 `json格式` ，所以在 `notes` 里面注明 `需要 Mock 的静态输入`。

        ```java
        @ApiOperation(value = "测试接口", notes = "示例数据：\n" + test)
        @PostMapping("/test2")
        public JSONObject test2() {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", "test");
            return jsonObject;
        }

        private final static String test="{\n" +
                "    \"input\":\"test\",\n" +
                "    \"input2\":\"test2\"\n" +
                "}";
        ```

        Swagger-ui上的展示：

        ![](http://of0qa2hzs.bkt.clouddn.com/20180606152827449623722.png)

    - `ApiResponse` 
    
        > 返回的状态码与数据

- 使用方式

    前端访问 `http://localhost:8080/swagger-ui.html` ，即可使用所有的 `Mock接口`。

## 生产环境不启用 swagger 文档

- Application 中添加配置

    通过 yml 里面的 swagger.show 来判断是否加载：

    ```java
    @Value("${swagger.show}")
    private boolean swaggerShow;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(swaggerShow)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.xx.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("RESTful APIs")
                .description("用于项目前端接口调用")
                .termsOfServiceUrl("")
                .version("1.0")
                .termsOfServiceUrl("11")
                .build();
    }
    ```

- 使用单独的 Config 文件

    当然也可以单独使用 `@Configuration` 注解来配置你的 Swagger 属性。

## 使用范围

- 所有前后端分离的接口

    满足契约测试标准，后端所有接口信息通过注解定义。前端通过 `swagger` 测试接口与开发。

- 所有 `api` 接口（内部使用的）

## 开发模式

> 虽然原则上在设计之初就应该定好接口的输入输出，实际开发中仍然无法避免接口变动。

- 明确的输入输出

    对于明确的接口，后端可以优先定义好接收的方法参数和输出的 `json` ，通过静态变量的方式提供 `mock数据` 给前端以供前端开发。

- 不明确的接口

    对于不明确的接口，建议直接开发，在开发的过程中掌握细节，提供稳定的完整接口给前端。

## 其他常用注解

Swagger 的使用是为了简便开发，请始终遵守 `约定大于配置` 的理念，对于注解应该选择性的使用，不必过分追求完美。

- @Api
    
    用在类上，描述该类的作用。

    ```
    @Api(value = "/test", description = "Operations about test")
    ```

- @ApiOperation

    用在方法上，描述方法的作用，定义使用方式,定义返回的结果类型。

    ```
     @ApiOperation(value = "测试接口", notes = "示例数据：\n" + test,response = Test.class)
    ```

- @ApiParam

    用在方法签名上，定义请求的入参属性，form表单。

    ```
    public Test queryTest(
            @ApiParam(value = "测试类型", required = true)
            @RequestParam("type") String type) 
    ```

- @ApiResponse

    配置响应的类型，可以配置多个。

    ```
    @ApiResponses({ @ApiResponse(code = 400, message = "type error") })
    ```

- @ApiModel

    用在模型类上，对模型类做注释（这种一般用在post创建的时候，使用@RequestBody这样的场景，请求参数无法使用@ApiImplicitParam注解进行描述的时候；

