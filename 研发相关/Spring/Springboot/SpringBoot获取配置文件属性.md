# SpringBoot 获取配置文件属性

## 一. 使用 @PropertySource + Environment

`@PropertySource` 指定配置文件位置， `Environment` 读取配置文件属性。

```
@Configuration
@PropertySource(value = {"classpath:test.properties"})
public class TestConfig {

    @Autowired
    private Environment env;

    public Map<String, Object> map=new HashMap<>();

    @Bean
    public Map<String, Object> test() {
        String testName = env.getProperty("test1.name");
        Integer testId = Integer.valueOf(env.getProperty("test1.id"));
        System.out.println("test1 -> | testName -> " + testName + " | test id -> " + testId);
        map.put("name", testName);
        map.put("id", testId);
        return map;
    }

}
```

## 二. 使用 @PropertySource + @Value

`@PropertySource` 指定配置文件位置，`@Value` 指定 `key对应的值。

```
@Configuration
@PropertySource(value = {"classpath:test2.properties"})
public class Test2Config {

    @Value("${test2.name}")
    private String testName;

    @Value("${test2.id}")
    private Integer testId;

    public Map<String, Object> map2=new HashMap<>();

    @Bean
    public Map<String, Object> test2() {

        System.out.println("test2 -> | testName -> " + testName + " | test id -> " + testId);
        map2.put("name", testName);
        map2.put("id", testId);
        return map2;
    }
}
```

## 三. 如果是默认的 yaml 文件属性，则可以直接使用 @Value

如果使用 `yaml` 文件可以不指定 `properties`，同时方式3会覆盖方式2

```
@Configuration
public class Test3Config {

    @Value("${test3.name}")
    private String testName;

    @Value("${test3.id}")
    private Integer testId;

    public Map<String, Object> map3=new HashMap<>();

    @Bean
    public Map<String, Object> test2() {

        System.out.println("test3 -> | testName -> " + testName + " | test id -> " + testId);
        map3.put("name", testName);
        map3.put("id", testId);
        return map3;
    }
}
```

## 四. getter、setter 方法注入及获取配置


```
@Component
@PropertySource(value = {"classpath:test4.properties"})
@ConfigurationProperties(prefix="test4")
public class Test4Config {

    private String name;

    private Integer id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
```

```
@Autowired
private Test4Config test4Config;

@GetMapping(value = "/test4_config")
public void Test4Config(){
    System.out.println("test4 -> | testName -> " + test4Config.getName() + " | test id -> " + test4Config.getId());
}
```

## 五. 直接读取配置文件

```
public class Test5Config {

    public static String name;
    public static int id;

    private static String property = "test5.properties";

    private static Test5Config myConfig;

    static {
        myConfig = loadConfig();
    }

    private static Test5Config loadConfig() {
        if (myConfig == null) {
            myConfig = new Test5Config();
            Properties properties;
            try {
                properties = PropertiesLoaderUtils.loadAllProperties(property);

                name = properties.getProperty("test5.name");
                id = Integer.valueOf(properties.getProperty("test5.id"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return myConfig;
    }

    public Test5Config getInstance() {
        return myConfig;
    }

}
```

```
@GetMapping(value = "/test5_config")
public void Test5ConfigX(){
    System.out.println("test5 -> | testName -> " + Test5Config.name + " | test id -> " + Test5Config.id);
}
```

## 附

- [项目地址](https://github.com/MyHerux/cases/tree/master/cases-configuration)