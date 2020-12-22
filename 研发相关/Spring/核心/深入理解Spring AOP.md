# 深入理解Spring AOP

## 基本知识

### 面向切面编程 AOP

AOP(Aspect-Oriented Programming), 即 `面向切面编程`。

![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180307/fcEIc40J9k.png?imageslim)

### AOP 术语

![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180307/CB7DB7mEIl.png?imageslim)

- 通知（Advice）

    > Advice 定义了切面是什么以及何时使用

    - 前置通知（Before）：在目标方法被调用之前调用通知功能；
    - 后置通知（After）：在目标方法完成之后调用通知，此时不会关心方法的输出是什么；
    - 返回通知（After-returning）：在目标方法成功执行之后调用通知；
    - 异常通知（After-throwing）：在目标方法抛出异常后调用通知；
    - 环绕通知（Around）：通知包裹了被通知的方法，在被通知的方法调用之前和调用之后执行自定义的行为。


- 连接点（Join point）

    > 连接点是在应用执行过程中能够插入切面的一个点。这个点可以是调用方法时、抛出异常时、甚至修改一个字段时。切面代码可以利用这些点插入到应用的正常流程之中，并添加新的行为。

- 切点（Pointcut）

    > 切点的定义会匹配通知所要织入的一个或多个连接点。我们通常使用明确的类和方法名称，或是利用正则表达式定义所匹配的类和方法名称来指定这些切点。

- 切面（Aspect）

    > 切面是通知和切点的结合。通知和切点共同定义了切面的全部内容：`它是什么，在何时和何处完成其功能`。

- 引入（Introduction）

    > 引入允许我们向现有的类添加新方法或属性。

- 织入（Weaving）

    > 织入是把切面应用到目标对象并创建新的代理对象的过程。切面在指定的连接点被织入到目标对象中。

    在目标对象的生命周期里有多个点可以进行织入：

    - 编译期：切面在目标类编译时被织入。这种方式需要特殊的编译器。
    `AspectJ` 的织入编译器就是以这种方式织入切面的。

    - 类加载期：切面在目标类加载到JVM时被织入。这种方式需要特殊的类加载器`（ClassLoader）`，它可以在目标类被引入应用之前增强该目标类的字节码。
    `AspectJ 5` 的加载时织入`（load-timeweaving，LTW）`就支持以这种方式织入切面。

    - 运行期：切面在应用运行的某个时刻被织入。一般情况下，在织入切面时，`AOP` 容器会为目标对象动态地创建一个代理对象。
    `Spring AOP` 就是以这种方式织入切面的。

## Spring对AOP的支持

- 基于代理的经典Spring AOP

- 纯POJO切面

- @AspectJ注解驱动的切面

- 注入式AspectJ切面（适用于Spring各版本）

## AOP的两种代理方式

- JAVA动态代理

    `JDK` 的动态代理主要涉及 `java` . `lang` . `reflect` 包中的两个类：`Proxy` 和 `InvocationHandler` 。其中 `InvocationHandler` 只是一个接口，可以通过实现该接口定义横切逻辑，并通过反射机制调用目标类的代码，动态的将横切逻辑与业务逻辑织在一起。而 `Proxy` 利用 `InvocationHandler` 动态创建一个符合某一接口的实例，生成目标类的代理对象。（只能为接口创建代理实例）

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180419/m7cGD5Ec5C.png?imageslim)


    ```java
    //需要被代理的接口
    public interface ForumService {
        public void removeTopic(int topicId);
        public void removeForum(int forumId);
    }

    //被代理接口的实现类，包含核心的业务逻辑
    public class ForumServiceImpl implements ForumService{
        @Override
        public void removeTopic(int topicId) {
            System.out.println("模拟删除Topic记录："+ topicId);
            try {
                Thread.currentThread().sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void removeForum(int forumId) {
            System.out.println("模拟删除Forum记录："+ forumId);
            try {
                Thread.currentThread().sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }
    ```

    ```java
    //性能监控核心代码生成
    public class MethodPerformance {
        private long begin;
        private long end;
        private String serviceMethod;
        
        public MethodPerformance(String serviceMethod) {
            this.serviceMethod = serviceMethod;
            this.begin = System.currentTimeMillis();
        };
        
        public void printPerformance(){
            this.end = System.currentTimeMillis();
            long elapse = this.end - this.begin;
            System.out.println(serviceMethod + " cost " + elapse +"ms");
        }
    }

    //线程安全的横切逻辑
    public class PerformanceMonitor {
        private static ThreadLocal<MethodPerformance> tl= new ThreadLocal<MethodPerformance>();
        
        public static void begin(String method){
            System.out.println("begin monitor");
            MethodPerformance mp = new MethodPerformance(method);
            tl.set(mp);
        }
        
        public static void end(){
            System.out.println("end monitor");
            MethodPerformance mp = tl.get();
            mp.printPerformance();
        }
    }

    //AOP横切模块
    public class PerfermanceHandler implements InvocationHandler{
        private Object target;
        
        public PerfermanceHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            PerformanceMonitor.begin(target.getClass().getName()+"."+method.getName());
            Object object = method.invoke(target, args);
            PerformanceMonitor.end();
            return object;
        }

    }
    ```

    ```java
    //测试
    public class TestForumService {
        public static void main(String[] args) {
            ForumService target = new ForumServiceImpl();
            //将目标业务类与横切代码编织到一起
            PerfermanceHandler handler = new PerfermanceHandler(target);
            //创建代理实例
            ForumService proxy = (ForumService) Proxy.newProxyInstance(target.getClass().getClassLoader(), 
                                                        target.getClass().getInterfaces(), handler);
            proxy.removeForum(10);
            proxy.removeTopic(1012);
            
        }
    }
    ```

- CGLIB代理

    `CGLib` 采用底层的字节码技术，为一个类创建子类，并在子类中采用方法拦截的技术拦截所有父类的调用方法，并顺势织入横切逻辑。

    ```java
    public class CglibProxy implements MethodInterceptor{

        private Enhancer enhancer = new Enhancer();
        
        public Object getProxy(Class clazz){
            enhancer.setSuperclass(clazz);//设置创建子类的类
            enhancer.setCallback(this);
            return enhancer.create();//通过字节码技术动态创建子类实例
        }
        
        @Override
        public Object intercept(Object target, Method method, Object[] args,
                MethodProxy proxy) throws Throwable {
            PerformanceMonitor.begin(target.getClass().getName()+"."+method.getName());
            Object object = proxy.invokeSuper(target, args);
            PerformanceMonitor.end();
            return object;
        }
    }
    ```

    ```java
    //测试
    public class TestForumService {
        public static void main(String[] args) {	
            CglibProxy cglibProxy = new CglibProxy();
            ForumServiceImpl service = (ForumServiceImpl) cglibProxy.getProxy(ForumServiceImpl.class);
            service.removeForum(10);
            service.removeTopic(1012);
        }
    }
    ```


## Spring AOP 实战

> 从AOP的功能来讲，AOP比较适合用于鉴权和日志等功能。

### 鉴权

- 需求

    > 针对需要权限验证的接口进行鉴权验证

- 实现

    - 注解 `Auth`

        ```java
        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface Auth {
        }
        ```

    - Aspect实现

        ```java
        @Component
        @Aspect
        public class AopAdvice {

            /**
            * 定义切点：使用Auth注解的地方
            */
            @Pointcut("@annotation(com.xu.aop.Auth)")
            public void pointcut() {
            }

            /**
            * 定义 advice
            *
            * @param joinPoint 连接点
            * @return token验证结果
            * @throws Throwable
            */
            @Around("pointcut()")
            public Object checkAuth(ProceedingJoinPoint joinPoint) throws Throwable {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();

                //检查需要权限的token是否合法
                String token = getToken(request);
                if (!token.equals("123")) {
                    return "token不合法";
                }

                return joinPoint.proceed();
            }

            private String getToken(HttpServletRequest request) {
                Cookie[] cookies = request.getCookies();
                if (cookies == null) {
                    return "";
                }
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equalsIgnoreCase("user_token")) {
                        return cookie.getValue();
                    }
                }
                return "";
            }
        }

        ```

### 日志

- 需求

    > 统一的通用日志记录

- 实现

    - Aspect 

        ```java
        @Component
        @Aspect
        public class LogAopAdviseDefine {
            private Logger logger = LoggerFactory.getLogger(getClass());

            // 定义一个 Pointcut, 使用 <切点表达式函数> 来描述对哪些 Join point 使用 advise (此处是LogService).
            @Pointcut("within(LogService)")
            public void pointcut() {
            }

            // 定义 advice，前置通知：日志输出入参
            @Before("pointcut()")
            public void logMethodInvokeParam(JoinPoint joinPoint) {
                logger.info("---Before method {} invoke, param: {}---", joinPoint.getSignature().toShortString(), joinPoint.getArgs());
            }

            // 定义 advice，返回通知：日志输出返回的结果
            @AfterReturning(pointcut = "pointcut()", returning = "retVal")
            public void logMethodInvokeResult(JoinPoint joinPoint, Object retVal) {
                logger.info("---After method {} invoke, result: {}---", joinPoint.getSignature().toShortString(), joinPoint.getArgs());
            }
            // 定义 advice，异常通知：日志输出异常内容
            @AfterThrowing(pointcut = "pointcut()", throwing = "exception")
            public void logMethodInvokeException(JoinPoint joinPoint, Exception exception) {
                logger.info("---method {} invoke exception: {}---", joinPoint.getSignature().toShortString(), exception.getMessage());
            }
        }
        ```


























