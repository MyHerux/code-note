# Springboot-2.x-scaffold

SpringBoot2.x 脚手架，适用于新项目 init。

## Overview

- SpringBoot 2.x
- 应用监控：Actuator
- Swagger 2.8
- 统一异常处理
- 数据库连接池：HikariCP
- Mybatis
- Redis
- MongoDB

## 基本概念

- spring-boot-starter-parent

    使用 `Maven` 可以继承 `spring-boot-starter-parent` 项目来获得一些合理的默认配置

    ```
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.2.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    ```

    默认配置包括以下特性：

    - 默认使用 `Java 8` 的编译器等级

    - 使用 `UTF-8` 编码 

    - 一个引用管理的功能，在 `dependencies` 里的部分配置可以不用填写 `version` 信息，这些`version` 信息会从 `spring-boot-dependencies` 里得到继承。

    - 识别资源过滤（Sensible resource filtering.）

    - 识别插件的配置（Sensible plugin configuration (exec plugin, surefire, Git commit ID, shade).）

    - 能够识别 `application.properties` 和 `application.yml` 类型的文件，同时也能支持`profile-specific` 类型的文件（如： `application-foo.properties` and   `application-foo.yml`)。

    - 把 `application.properties` 和 `application.yml` 默认的占位符 `${…}` 改为了`@..@`

- @Configuration

    `@Configuration` 用于定义配置类，可替换xml配置文件,被注解的类内部包含有一个或多个被 `@Bean` 注解的方法，这些方法将会被 `AnnotationConfigApplicationContext` 或`AnnotationConfigWebApplicationContext` 类进行扫描，并用于构建bean定义，初始化Spring容器。

    `@Configuration` 注解的配置类有如下要求：

    - `@Configuration` 不可以是final类型；

    - `@Configuration` 不可以是匿名类；

    - 嵌套的 `configuration` 必须是静态类。

    `@Configuration` 标注在类上，相当于把该类作为spring的xml配置文件中的，作用为：配置spring容器(应用上下文)

- @Bean

    `@Bean` 标注在方法上(返回某个实例的方法)，等价于spring的xml配置文件中的，作用为：注册bean对象。

- 导入 XML 配置

    如果不得不使用 `XML` 来导入配置，建议使用 `@ImportResource` 和 `@Value` 进行资源文件读取。

    ```
    @Configuration
    @ImportResource("classpath:/com/cloud/skyme/properties-config.xml")
    public class AppConfig{
        @Value("${jdbc.url}")
        private String url;
        @Value("${jdbc.username}")
        private String username;
        @Value("${jdbc.password}")
        private String password;
        @Bean
        public DataSource dataSource() {
            return new DriverManagerDataSource(url,username,password);
        }
    }
    ```

- 自动配置(Auto-configuration)

    `Spring Boot auto-configuration` 将基于你的jar包依赖自动配置你的 `Spring application`。比如：如果在你的classpath中配置了H2（在pom.xml文件中添加了H2的依赖），你不必维护任何数据连接，Spring Boot会自动配置一个内存数据库。

    通过在 `@Configuration` classes 上使用 `@SpringBootApplication` or `@EnableAutoConfiguration` 注解来选择是否开启自动配置。

- Spring Beans and Dependency Injection

    > 在 `Springboot` 可以使用任何在 `Spring` 中使用的方式来定义 bean。 为了简便，我们经常使用 `@ComponentScan` (to find your beans) and `@autowired` (to do constructor injection) 。
    
    如果 `SpringApplication` 是放在根目录下，则可以使用 `@ComponentScan` 而不带任何参数。所有的应用程序组件（`@Component`，`@Service`，`@Repository`，`@Controller`等）都会自动注册为 `Spring Bean`。

    使用构造函数来注入 `RiskAssessor` bean（如果bean有一个构造函数，可以省略 `@Autowired`）:

    ```
    package com.example.service;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    @Service
    public class DatabaseAccountService implements AccountService {

        private final RiskAssessor riskAssessor;

        @Autowired
        public DatabaseAccountService(RiskAssessor riskAssessor) {
            this.riskAssessor = riskAssessor;
        }

        // ...

    }
    ```

- Hot Swapping

    `SpringBoot` 提供 `spring-boot-devtools` 工具来实现热部署。

    `spring-boot-devtools` 模块可以包含在任何项目中，它可以节省大量的时间。 想要使用devtools支持，只需将模块依赖关系添加到你的构建中：

    ```
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
    ```

    运行打包的应用程序时，devtools 会自动禁用。如果你通过 java -jar 或者其他特殊的类加载器进行启动时，都会被认为是“生产环境的应用”。

## Spring Boot features

- SpringApplication

    一般建议放在文件的根目录下，因为它会隐式的扫描它下面的所有目录。

    项目的目录结构一般如下：

    ```
    com
    +- example
        +- myapplication
            +- Application.java
            |
            +- customer
            |   +- Customer.java
            |   +- CustomerController.java
            |   +- CustomerService.java
            |   +- CustomerRepository.java
            |
            +- order
                +- Order.java
                +- OrderController.java
                +- OrderService.java
                +- OrderRepository.java
    ```

    `Application.java` 文件使用基本的注解 `@SpringBootApplication` 描述 `main` method，如下：

    ```java
    package com.example.myapplication;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;

    @SpringBootApplication
    public class Application {

        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }

    }
    ```

- Startup Failure

    如果应用程序无法启动，那么Spring Boot提供的 `FailureAnalyzers` 会帮助你分析失败的原因。

    比如启动时端口被占用，会看到如下信息：

    ```
    ***************************
    APPLICATION FAILED TO START
    ***************************

    Description:

    Embedded servlet container failed to start. Port 8080 was already in use.

    Action:

    Identify and stop the process that's listening on port 8080 or configure this application to listen on another port.
    ```

    可以通过如下方式启动调试：

    > $ java -jar myproject-0.0.1-SNAPSHOT.jar --debug

- Customizing the Banner

    默认的 `Banner` ：

    ```
      .   ____          _            __ _ _
     /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
    ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
     \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
      '  |____| .__|_| |_|_| |_\__, | / / / /
    =========|_|==============|___/=/_/_/_/
    :: Spring Boot ::        (v2.0.2.RELEASE)
    ```

    如果要修改其内容，只需要在 `Spring Boot` 工程的 `/src/main/resources` 目录下创建一个 `banner.txt` 文件，然后将ASCII字符画复制进去，就能替换默认的 `banner` 了。

    也可以在程序中修改，使用 `SpringApplication.setBanner(…)` 方法或者使用 `org.springframework.boot.Banner` 接口并实现你自己的 `printBanner()` 方法.

- Customizing SpringApplication

    如果默认的 `SpringApplication` 的默认设置不符合您的喜好，那么可以通过自定义对其进行设置。

    比如不显示bannner，我们可以这样写：

    ```java
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MySpringConfiguration.class);
            app.setBannerMode(Banner.Mode.OFF);
            app.run(args);
    }
    ```

    > 传递给 `SpringApplication` 的构造函数参数是 `Spring bean` 的配置源。在大多数情况下，它们都是对              `@Configuration` 类的引用，但它们也可能是对XML配置或应扫描的包的引用。

## 开启 Actuator

[Spring-boot-actuator](https://github.com/spring-projects/spring-boot/tree/v2.0.2.RELEASE/spring-boot-project/spring-boot-actuator) module 可帮助您在将应用程序投入生产时监视和管理应用程序。您可以选择使用 HTTP 端点或 JMX 来管理和监控您的应用程序。Auditing, health, and metrics gathering 也可以自动应用于您的应用程序。


- 引入依赖

    ```
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    ```

- 监控详情

    [SpringBoot 2.x 中使用 Actuator 来做应用监控](https://blog.csdn.net/myherux/article/details/80670557)

## 配置 Swagger

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

- 更多使用方式

    [使用 Swagger 来进行前后端协作](https://blog.csdn.net/myherux/article/details/78655686)

## 添加统一的异常处理

- 异常的类型: `ExceptionType.java`

    - 每一个已知的业务异常 `必须声明`

    - 异常必须包含：`异常码`，`对外输出信息`，`异常等级`

    - 异常等级需要符合标准

    ```java
        INCOMING_DATA_ERROR(300003, "传入数据出错", Level.ignore);
        /**
        * @param code    异常码
        * @param message 对外输出信息
        * @param level   异常等级
        */
        ExceptionType(Integer code, String message, Level level) {
            this.code = code;
            this.message = message;
            this.level = level.getLevel();
        }
    ```

- 异常的等级: `Level.java`

    - 异常分为3个等级：

    - `high`，影响系统运行或者对客户输出，需要通知到负责人

    - `normal`，由于客户输入数据不足或其他已知原因产生的错误，需要每日采集并分析

    - `ignore`，一般的操作异常，响应信息明确，只需要打印日志即可

    ```java
        /**
        * high level,需要通知到人紧急处理
        */
        high(3),
        /**
        * normal level,采集并统一分析
        */
        normal(2),
        /**
        * ignore level,只需要打印即可
        */
        ignore(1);

    ```
- 异常的统一处理：`GlobalExceptionHandler.java`

    - `businessExceptionHandler` 处理主动抛出的异常，按照等级不同进行不同处理。

    - `otherExceptionHandler` 处理未捕获的异常，需要通知。

## 数据库连接池-HikariCP

`SpringBoot 2.0` 开始推 `HikariCP` ，将默认的数据库连接池从 `tomcat jdbc pool` 改为了 `hikari` ， `HikariCP` 在性能和并发方面确实表现不俗（号称最快的连接池）。

如果你使用 `spring-boot-starter-jdbc` 或 `spring-boot-starter-data-jpa` ，会自动添加对 `HikariCP` 的依赖，也就是说此时使用 `HikariCP` 。当然你也可以强制使用其它的连接池技术，可以通过在 `application.properties` 或 `application.yml` 中配置 `spring.datasource.type` 指定。

- 基本配置

    因为已经默认使用 `HikariCP`，所以只需要在 yaml 中添加数据库配置即可：

    ```
    url: jdbc:mysql://
    username:
    password:
    ```

- 配置详解

    [HikariCP配置详解+多数据源](https://blog.csdn.net/MyHerux/article/details/80730690)

## Mybatis

- 添加依赖

    ```
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>1.3.2</version>
    </dependency>
    ```

- 具体使用

    ```
    @Mapper
    public interface UserMapper {

        @Select("select * from user where id = #{id}")
        User SelectUserById(@Param("id") int id);
    }
    ```

    ```
    @Autowired
    private UserMapper userMapper;

    @ApiOperation(value = "测试 Mybatis")
    @GetMapping("/test8")
    public User test8(){
       return userMapper.SelectUserById(1);
    }
    ```

## Redis

SpringBoot 2.0 中 `Redis` 客户端驱动现在由 `Jedis` 变为了 `Lettuce` 。

- 基本配置

    直接使用 `spring-boot-starter-data-redis` 即可，默认会使用 `Lettuce` 。

    ```
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    ```

    ```
    spring:
      redis:
        port: 6379
        host: localhost
        password:
    ```

- StringRedisTemplate

    ```
    @Autowired
    private StringRedisTemplate template;

    @ApiOperation(value = "测试Redis")
    @GetMapping("/test5")
    public String test5() {
        template.opsForValue().set("aaa", "111");

        return template.opsForValue().get("aaa");
    }
    ```

## MongoDB

- 基本配置

    ```
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>
    ```

    ```
    spring:
      data:
        mongodb:
        uri: mongodb://localhost/test
        username: test
        password: 123456
    ```
- MongoTemplate

    同 SpringBoot 1.0 ，依然提供 `MongoTemplate`（Spring Boot为你自动配置一个bean来注入模板） 的方式：

    ```
    @Data
    @Builder
    public class User {

        @Id
        public String id;

        public String name;

        public String password;
    }
    ```

    ```
    @Autowired
    private MongoTemplate mongoTemplate;

    @ApiOperation(value = "测试 MongoTemplate")
    @GetMapping("/test7")
    public List<User> test7() {
        User user = User.builder().name("test").password("123").build();
        mongoTemplate.save(user);
        return mongoTemplate.findAll(User.class);
    }
    ```

- Spring Data MongoDB Repositories

    `Spring Data` 包含对 MongoDB 的存储库支持。与JPA存储库一样，基本原则是所有查询都是基于方法名称自动构建的。

    ```
    public interface UserRepository extends MongoRepository<User, String> {
    }
    ```

    ```
    @Autowired
    private UserRepository userRepository;

    @ApiOperation(value = "测试 MongoDB Repositories")
    @GetMapping("/test6")
    public List<User> test6() {
        User user = User.builder().name("test").password("123").build();
        userRepository.save(user);

        return userRepository.findAll();
    }
    ```

## 项目地址

[https://github.com/MyHerux/spring-boot-2.x-scaffold](https://github.com/MyHerux/spring-boot-2.x-scaffold)
















