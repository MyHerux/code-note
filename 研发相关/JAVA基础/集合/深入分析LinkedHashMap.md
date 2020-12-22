# 深入分析LinkedHashMap (JDK1.8)

- 类名和继承关系

    ```
    public class LinkedHashMap<K,V>
        extends HashMap<K,V>
        implements Map<K,V>
    {
    ```

- 内部存储结构

    ```
    /**
    * HashMap.Node subclass for normal LinkedHashMap entries.
    */
    static class Entry<K,V> extends HashMap.Node<K,V> {
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }

    /**
    * 用于指向双向链表的头部
    */
    transient LinkedHashMap.Entry<K,V> head;

    /**
    * 用于指向双向链表的尾部
    */
    transient LinkedHashMap.Entry<K,V> tail;
    ```

    `LinkedHashMap` 实际是在 `HashMap` 的 `Node` 结构里面加上双向链表

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180313/AJh94baEg9.png?imageslim)

    在 `LinkedHashMapMap` 中，所有 `put` 进来的 `Entry` 都保存在 `HashMap` 中，但由于它又额外定义了一个以 `head` 为头结点的空的双向链表，因此对于每次 `put` 进来 `Entry` 还会将其插入到双向链表的尾部。

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180313/2aa6jef7ac.png?imageslim)

    > 由上图，除去红色双向箭头就是一个 `HashMap` ，只看红色双向箭头则是一个双向链表。所以， `LinkedHashMap` 就是一个标准的 `HashMap+LinkedList` 。

- LinkedHashMap的构造方法

    ```
    public LinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        accessOrder = false;
    }
    ```

    > 比 `HashMap` 多了一个 `accessOrder` 的参数，用来指定按照 `LRU` 排列方式还是顺序插入的排序方式

- get()方法

    ```
    public V get(Object key) {
        Node<K,V> e;
        //调用HashMap的getNode的方法
        if ((e = getNode(hash(key), key)) == null)
            return null;
        //在取值后对参数accessOrder进行判断，如果为true，执行afterNodeAccess
        if (accessOrder)
            afterNodeAccess(e);
        return e.value;
    }
    ```
    > accessOrder为true则表示按照基于访问的顺序来排列

    ```
    //将最近使用的Node，放在链表的最末尾
    void afterNodeAccess(Node<K,V> e) { // move node to last
        LinkedHashMap.Entry<K,V> last;
        //仅当按照LRU原则且e不在最末尾，才执行修改链表，将e移到链表最末尾的操作
        if (accessOrder && (last = tail) != e) {
            //将e赋值临时节点p， b是e的前一个节点， a是e的后一个节点
            LinkedHashMap.Entry<K,V> p =
                (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
            //节点移动
            p.after = null;
            if (b == null)
                head = a;
            else
                b.after = a;
            if (a != null)
                a.before = b;
            else
                last = b;
            if (last == null)
                head = p;
            else {
                p.before = last;
                last.after = p;
            }
            tail = p;
            ++modCount;
        }
    }
    ```

- put()方法 

    > `LinkedHashMap` 没有重写 `put` 方法，调用的都是 `HashMap` 的 `put` 方法。为什么它也会执行 `afterNodeAccess()` 方法呢，因为这个方法 `HashMap` 就是存在的，但是没有实现， `LinkedHashMap` 重写了 `afterNodeAccess()` 这个方法。