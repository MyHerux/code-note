## Springboot 对缓存的支持

Spring Framework 支持透明地向应用程序添加缓存。从本质上讲，抽象将缓存应用于方法，从而根据缓存中可用的信息减少执行次数。缓存逻辑是透明应用的，不会对调用者造成任何干扰。只要通过 `@EnableCaching` 批注启用了缓存支持，Spring Boot 就会自动配置缓存基础结构。

比如：

```
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class MathService {

	@Cacheable("piDecimals")
	public int computePiDecimal(int i) {
		// ...
	}

}
```

## 缓存的实现

缓存抽象不提供实际存储，而是依赖于 `org.springframework.cache.Cache` 和`org.springframework.cache.CacheManager` 接口实现的抽象。

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9jZG4uaGVyb3h1LmNvbS8yMDE5MDQxMTE1NTQ5NTQ0NTc2ODU1MS5wbmc?x-oss-process=image/format,png)

如果您尚未定义 `CacheManager` 类型的 `bean` 或名为 `cacheResolver` 的 `CacheResolver`，则 Spring Boot 会尝试检测以下提供程序（按指示的顺序）：

1. Generic
2. JCache (JSR-107) (EhCache 3, Hazelcast, Infinispan, and others)
3. EhCache 2.x
4. Hazelcast
5. Infinispan
6. Couchbase
7. Redis
8. Caffeine
9. Simple

## Caffeine

[Caffeine](https://github.com/ben-manes/caffeine) 是 Java 8重写的 Guava 缓存，取代了对 Guava 的支持。如果存在 Caffeine，则会自动配置 `CaffeineCacheManager`（由 `spring-boot-starter-cache` “Starter”提供）

![](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9jZG4uaGVyb3h1LmNvbS8yMDE5MDQxMTE1NTQ5NTQ2MjE2NDQ0NC5wbmc?x-oss-process=image/format,png)

## 定义 CacheManager 来管理缓存

由上文可知，当存在 `CacheManager` 类型的 `bean` 时，Spring Boot 会优先使用应用定义的 CacheManager。

> 本例只使用了 Caffeine 来作为缓存，实际可以定义多种不同的缓存，通过不同的 cacheNames 来使用

```
@Configuration
@EnableCaching
public class LocalCacheConfig {

    private static final int DEFAULT_MAXSIZE = 1000;
    private static final int DEFAULT_TTL = 1;

    /**
     * 定义不同的cache名称、超时市场（秒）、最大容量。
     *
     * 每个cache缺省：1秒超时、最多缓存1000条数据，需要修改可以在构造方法的参数中指定。
     */
    public enum Caches {
        TEST_A(10, 10),
        TEST_B(10 * 6, 100);


        Caches() {
        }

        Caches(int ttl) {
            this.ttl = ttl;
        }

        Caches(int ttl, int maxSize) {
            this.ttl = ttl;
            this.maxSize = maxSize;
        }

        private int maxSize = DEFAULT_MAXSIZE;    //最大数量
        private int ttl = DEFAULT_TTL;        //过期时间（秒）

        public int getMaxSize() {
            return maxSize;
        }

        public int getTtl() {
            return ttl;
        }
    }

    /**
     * 创建基于Caffeine的Cache Manager
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<CaffeineCache> caches = new ArrayList<>();
        for (Caches c : Caches.values()) {
            caches.add(new CaffeineCache(c.name(),
                Caffeine.newBuilder().recordStats()
                    .expireAfterWrite(c.getTtl(), TimeUnit.SECONDS)
                    .maximumSize(c.getMaxSize())
                    .build())
            );
        }

        cacheManager.setCaches(caches);

        return cacheManager;
    }

}
```

测试：

```
@Slf4j
@Service
public class TestService {

    @Cacheable(cacheNames = "TEST_A")
    public int testA(){
        log.info("testA not get from cache!");
        return 100;
    }

    @Cacheable(cacheNames = "TEST_B")
    public int testB(){
        log.info("testB not get from cache!");
        return 99;
    }

    @Cacheable(cacheNames = "TEST_A")
    public String testC(){
        log.info("testC not get from cache!");
        return "testC";
    }

}
```

## 项目地址

[cases-caffeine](https://github.com/MyHerux/cases/tree/master/cases-caffeine)