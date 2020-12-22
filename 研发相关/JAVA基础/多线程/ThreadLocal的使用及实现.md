# ThreadLocal的使用及实现

## 使用ThreadLocal保证线程安全

> ThreadLocal是一个线程的局部变量，只有当前线程可以访问，自然是线程安全的。

- 多线程使用SimpleDateFormat

    示例：
    ```
    public class ParseDateNormal {
        private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public static class ParseDate implements Runnable {

            int i = 0;

            ParseDate(int i) {
                this.i = i;
            }

            @Override
            public void run() {
                try {
                    Date t = sdf.parse("2017-08-02 11:22:" + i % 60);
                    System.out.println(i+":"+t);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        public static void main(String[] args) {
            ExecutorService service = Executors.newFixedThreadPool(10);
            for (int i = 0; i < 1000; i++) {
                service.execute(new ParseDate(i));
            }
        }
    }
    ```

    引发的错误：

    > Exception in thread "pool-1-thread-17" java.lang.NumberFormatException: multiple points

- 使用ThreadLocal为每个线程产生一个SimpleDateFormat对象

    示例：
    ```
    public class ParseDateThreadLocal {

        static ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<>();

        public static class ParseDate implements Runnable {
            int i = 0;

            ParseDate(int i) {
                this.i = i;
            }

            @Override
            public void run() {
                if (threadLocal.get() == null) {
                    threadLocal.set(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                }
                try {
                    Date t = threadLocal.get().parse("2017-2-21 14:29:" + i % 60);
                    System.out.println(t);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        public static void main(String[] args) throws InterruptedException {
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            for (int i = 0; i < 10; i++) {
                executorService.execute(new ParseDate(i));
            }
        }
    }
    ```

    上述代码中，如果当前线程不持有 `SimpleDateFormat` 对象实例则新建一个并把它放到当前线程中，如果已经持有，则直接使用。

    > 为每一个线程分配不同的对象，需要在应用层面保证。ThreadLocal只是起到了简单的容器作用。

## ThreadLocal的实现原理

- Set方法

    源码：

    ```
    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }
    ```

    Thread类：

    ```
    ThreadLocal.ThreadLocalMap threadLocals = null;

    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

    ```

    在 `set` 时，首先获取当前线程对象，然后通过 `getMap()` 拿到当前线程的 `ThreadLocalMap` ，并 `set` 值。

    `set` 到 `ThreadLocalMap` 中的值，其实就是写入了 `threadLocals` 这个 `Map` 中。 `threadLocals` 本身就保存了当前所在线程的所有“局部变量”，也就是一个 `ThreadLocal` 变量的集合。

- Get方法

    源码：

    ```
     public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }

    private T setInitialValue() {
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }

    protected T initialValue() {
        return null;
    }
    ```

    `get()` 操作就是将 `set()` 操作中存到 `ThreadLocalMap` 中的数据取出来。

- 对象的销毁

    > 有上述可以看出变量都是维护在Thread类内部的，所以只要线程不退出，对象的引用将一直存在。

    线程退出,Thread类：
    ```
     private void exit() {
        if (group != null) {
            group.threadTerminated(this);
            group = null;
        }
        /* Aggressively null out all reference fields: see bug 4006245 */
        target = null;
        /* Speed the release of some of these resources */
        threadLocals = null;
        inheritableThreadLocals = null;
        inheritedAccessControlContext = null;
        blocker = null;
        uncaughtExceptionHandler = null;
    }
    ```

    当使用线程池时，当前线程可能不会退出。如果这样，将一些比较大的对象 `set` 到 `ThreadLocal` 中，就有可能引起内存泄露（内存被不会被引用的对象占用）。
    如果希望及时回收对象，可以使用 `ThreadLoacl.remove()` 将变量移除。

    如果不想使用 `ThreadLoacl.remove()` ，可以手动设置其为 `null`，因为 `ThreadLoaclMap` 内部的 `Entry` 都是 `WeakReference<ThreadLocal<?>>`。JVM在GC时，发现弱引用会直接回收掉这部分内存。

    ```
     static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }
    ```


