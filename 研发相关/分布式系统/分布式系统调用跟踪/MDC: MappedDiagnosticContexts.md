# MDC: Mapped Diagnostic Contexts

## Class MDC

| Modifier and Type          | Method                                         | Description                                                                        |
| -------------------------- | ---------------------------------------------- | ---------------------------------------------------------------------------------- |
| static void                | clear()                                        | 清除 `MDC` 底层实现（继承 `MDCAdapter`）的所有数据。                               |
| static String              | get​(String key)                               | 获取由 `key` 参数标识的诊断上下文。                                                |
| static Map<String,​String> | getCopyOfContextMap()                          | 返回当前线程的上下文映射的副本，`Key` 和 `Value` 为 `String` 的值。。              |
| static MDCAdapter          | getMDCAdapter()                                | 返回当前使用的 `MDCAdapter` 实例。                                                 |
| static void                | put​(String key, String val)                   | 将由 `key` 参数标识的诊断上下文值（`val` 参数）放入当前线程的诊断上下文映射中。    |
| static MDC.MDCCloseable    | putCloseable​(String key, String val)          | 此方法返回一个 `Closeable` 对象，该对象可以在调用 `close` 时删除键。               |
| static void                | remove​(String key)                            | 使用基础系统的 `MDC` 实现，删除由 `key` 参数标识的诊断上下文。                       |
| static void                | setContextMap​(Map<String,​String> contextMap) | 通过首先清除任何现有映射，然后复制作为参数传递的映射，来设置当前线程的上下文映射。 |

```java
public class MDC {
    static MDCAdapter mdcAdapter;

    private MDC() {
    }

    private static MDCAdapter bwCompatibleGetMDCAdapterFromBinder() throws NoClassDefFoundError {
        try {
            return StaticMDCBinder.getSingleton().getMDCA();
        } catch (NoSuchMethodError var1) {
            return StaticMDCBinder.SINGLETON.getMDCA();
        }
    }

    public static void put(String key, String val) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("key parameter cannot be null");
        } else if (mdcAdapter == null) {
            throw new IllegalStateException("MDCAdapter cannot be null. See also http://www.slf4j.org/codes.html#null_MDCA");
        } else {
            mdcAdapter.put(key, val);
        }
    }
    ...
}
```

`MDC` 类仅包含静态方法。它使开发人员可以将信息放置在诊断上下文中，随后可以由某些日志组件检索这些信息。 `MDC` 在每个线程的基础上管理上下文信息。通常，在开始为新的客户请求提供服务时，开发人员会将相​​关的上下文信息（例如客户 `ID` ，客户的 `IP` 地址，请求参数等）插入 `MDC` 。如果配置正确，则日志组件将自动在每个日志条目中包含此信息。

## MDCAdapter

```java
public interface MDCAdapter {
    void put(String var1, String var2);

    String get(String var1);

    void remove(String var1);

    void clear();

    Map<String, String> getCopyOfContextMap();

    void setContextMap(Map<String, String> var1);
}
```

## MDCAdapter 的一种基础实现

```java
public class BasicMDCAdapter implements MDCAdapter {
    private InheritableThreadLocal<Map<String, String>> inheritableThreadLocal = new InheritableThreadLocal<Map<String, String>>() {
        protected Map<String, String> childValue(Map<String, String> parentValue) {
            return parentValue == null ? null : new HashMap(parentValue);
        }
    };

    public BasicMDCAdapter() {
    }

    public void put(String key, String val) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        } else {
            Map<String, String> map = (Map)this.inheritableThreadLocal.get();
            if (map == null) {
                map = new HashMap();
                this.inheritableThreadLocal.set(map);
            }

            ((Map)map).put(key, val);
        }
    }
    ...
}
```

> 本质是使用 `ThreadLocal` 来存储同一线程的上下文信息，这里使用 `InheritableThreadLocal` 来避免多线程的情况下出现问题。

## MDC 在 Logback 中的简单应用

```java
package chapters.mdc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.ConsoleAppender;

public class SimpleMDC {
  static public void main(String[] args) throws Exception {

    // You can put values in the MDC at any time. Before anything else
    // we put the first name
    MDC.put("first", "Dorothy");

    [ SNIP ]
    
    Logger logger = LoggerFactory.getLogger(SimpleMDC.class);
    // We now put the last name
    MDC.put("last", "Parker");

    // The most beautiful two words in the English language according
    // to Dorothy Parker:
    logger.info("Check enclosed.");
    logger.debug("The most beautiful two words in English.");

    MDC.put("first", "Richard");
    MDC.put("last", "Nixon");
    logger.info("I am not a crook.");
    logger.info("Attributed to the former US president. 17 Nov 1973.");
  }

  [ SNIP ]

}
```

主要方法是首先将 `Value``Dorothy` 与 `MDC` 中的 `Key` 相关联。您可以根据需要在 `MDC` 中放置任意数量的 `Key/Value` 关联。使用相同 `Key` 的多次插入将覆盖较早的值。然后，代码继续进行配置，以配置 `logback` 。

在配置文件 `XML` 中添加如下：
```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender"> 
  <layout>
    <Pattern>%X{first} %X{last} - %m%n</Pattern>
  </layout> 
</appender>
```

请注意 `PatternLayout` 转换模式中 `％X` 说明符的用法。`％X` 转换说明符被使用两次，一次用于名为 `first` 的密钥，一次用于 `last` 的密钥。获得与 `SimpleMDC.class` 对应的记录器后，该代码将值 `Parker` 与名为 `last` 的键相关联。然后，它使用不同的消息两次调用记录器。通过将 `MDC` 设置为不同的值并发出多个日志记录请求来完成代码。

```
Dorothy Parker - Check enclosed.
Dorothy Parker - The most beautiful two words in English.
Richard Nixon - I am not a crook.
Richard Nixon - Attributed to the former US president. 17 Nov 1973.
```

`SimpleMDC` 应用程序说明了如果配置适当的话， `logback` 布局可以如何自动输出 `MDC` 信息。而且，放置在 `MDC` 中的信息可以由多个 `logger` 调用使用。

## MDC 在 Logback 中的进一步应用

### 自动填充 MDC

对于管理用户身份验证的 `Web` 应用程序，一种简单的解决方案可以是在 `MDC` 中设置用户名，并在用户注销后将其删除。不幸的是，使用这种技术并不总是可能获得可靠的结果。由于 `MDC` 在每个线程的基础上管理数据，因此回收线程的服务器可能会导致 `MDC` 中包含错误的信息。

为了使 `MDC` 中包含的信息在处理请求时始终正确，一种可能的方法是在过程开始时存储用户名，并在过程结束时将其删除。在这种情况下，可以使用 `Servlet` 过滤器。##参考文章

在 `Servlet` 过滤器的 `doFilter` 方法中，我们可以通过请求（或其中的 `cookie`）检索相关的用户数据，并将其存储在 `MDC` 中。其他过滤器和 `Servlet` 的后续处理将自动受益于先前存储的 `MDC` 数据。最后，当我们的 `servlet` 过滤器重新获得控制权时，我们就有机会清除 `MDC` 数据。

```java
package chapters.mdc;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.MDC;

public class UserServletFilter implements Filter {

  private final String USER_KEY = "username";
  
  public void destroy() {
  }

  public void doFilter(ServletRequest request, ServletResponse response,
    FilterChain chain) throws IOException, ServletException {

    boolean successfulRegistration = false;

    HttpServletRequest req = (HttpServletRequest) request;    
    Principal principal = req.getUserPrincipal();
    // Please note that we could have also used a cookie to 
    // retrieve the user name

    if (principal != null) {
      String username = principal.getName();
      successfulRegistration = registerUsername(username);
    } 

    try {
      chain.doFilter(request, response);
    } finally {
      if (successfulRegistration) {
        MDC.remove(USER_KEY);
      }
    }
  }

  public void init(FilterConfig arg0) throws ServletException {
  }
  

  /**
   * Register the user in the MDC under USER_KEY.
   * 
   * @param username
   * @return true id the user can be successfully registered
   */
  private boolean registerUsername(String username) {
    if (username != null && username.trim().length() > 0) {
      MDC.put(USER_KEY, username);
      return true;
    }
    return false;
  }
}
```

调用过滤器的 `doFilter()` 方法时，它将首先在请求中查找 `java.security.Principal` 对象。该对象包含当前已认证用户的名称。如果找到用户信息，则将其注册在 `MDC` 中。

筛选器链完成后，筛选器将从 `MDC` 中删除用户信息。

我们刚刚概述的方法仅在请求期间和处理线程的过程中设置 `MDC` 数据。其他线程不受影响。此外，任何给定线程在任何时间点都将包含正确的 `MDC` 数据。

### 多线程下的 MDC

`MDC` 的副本不能始终由工作线程从发起线程继承。当 `java.util.concurrent.Executors` 用于线程管理时，就是这种情况。例如， `newCachedThreadPool` 方法创建 `ThreadPoolExecutor` ，并且像其他线程池代码一样，它具有复杂的线程创建逻辑。

在这种情况下，建议在将任务提交给执行者之前，在原始（主）线程上调用 `MDC.getCopyOfContextMap()`。当任务运行时，作为第一个动作，它应调用 `MDC.setContextMapValues` 将原始 `MDC` 值的存储副本与新的 `Executor` 托管线程相关联。

## 参考文档

- [Class MDC](http://www.slf4j.org/api/org/slf4j/MDC.html)

- [Chapter 8: Mapped Diagnostic Context](http://logback.qos.ch/manual/mdc.html)
