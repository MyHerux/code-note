# volatile关键字的作用和原理

## 关键字作用

- volatile 保证可见性

    一旦一个共享变量（类的成员变量、类的静态成员变量）被volatile修饰之后，那么就具备了两层语义：

    - 保证了不同线程对这个变量进行操作时的可见性，即一个线程修改了某个变量的值，这新值对其他线程来说是立即可见的。

    - 禁止进行指令重排序。


- volatile 不能确保原子性

    ```
    public class VolatileVisibility {

        public static volatile int i =0;

        public static void increase(){
            i++;
        }
    }
    ```
    读数据和操作数据是两次操作

## volatile原理

> “观察加入volatile关键字和没有加入volatile关键字时所生成的汇编代码发现，加入volatile关键字时，会多出一个lock前缀指令”

lock前缀指令实际上相当于一个内存屏障（也成内存栅栏），内存屏障会提供3个功能：

- 它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障这句指令时，在它前面的操作已经全部完成；

- 它会强制将对缓存的修改操作立即写入主存；

- 如果是写操作，它会导致其他CPU中对应的缓存行无效。

## 应用场景

`synchronized` 关键字是防止多个线程同时执行一段代码，那么就会很影响程序执行效率，而 `volatile` 关键字在某些情况下性能要优于 `synchronized` ，但是要注意 `volatile` 关键字是无法替代 `synchronized` 关键字的，因为 `volatile` 关键字无法保证操作的原子性。通常来说，使用 `volatile` 必须具备以下 `2` 个条件：

- 对变量的写操作不依赖于当前值

- 该变量没有包含在具有其他变量的不变式中


## 具体应用情况

- 状态标记量

    ```java
    volatile boolean flag = false;
 
    while(!flag){
        doSomething();
    }
    
    public void setFlag() {
        flag = true;
    }
    ```

- double check

    ```java
    class Singleton{
        private volatile static Singleton instance = null;
        
        private Singleton() {
            
        }
        
        public static Singleton getInstance() {
            if(instance==null) {
                synchronized (Singleton.class) {
                    if(instance==null)
                        instance = new Singleton();
                }
            }
            return instance;
        }
    }
    ```

## 参考

- [就是要你懂Java中volatile关键字实现原理](https://www.cnblogs.com/xrq730/p/7048693.html) 