# HashMap与Hashtable探究

## HashMap（JDK1.8）

- 内部存储结构

    > `java8` 使用 `Node<K,V>` 来替换 `java7` 中的 `Entry<K,V>`
    ```
    transient Node<K,V>[] table;
 
    static class Node<K,V> implements Map.Entry<K,V> {
            final int hash;
            final K key;
            V value;
            Node<K,V> next;
    }
    ```

    可以看到，在 `HashMap` 内部存储使用了一个 `Node` 数组(默认值是 `16` )。所有计算出 `hash` 值的 `key` 会被存到 `table` 里面，如果 `hash` 值相同会存到 `node` 链表里面。

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180228/2I2e5AGb0A.png?imageslim)

    > 为什么使用 `Node<K,V>` 来替换 `Entry<K,V>`？

    由上图可知，当产生 `hash` 冲突时，相同 `hash` 值的 `key` 会被保存到一个链表里面，如果这样的情况过多，这个链表会变得很长。在最坏的情况下（所有 `key` 的 `hash` 值都相同），这种方式会将 `HashMap` 的 `get` 方法的性能从 `O(1)` 降低到 `O(n)`。

    `java8` 的策略是，如果链表长度 `>= TREEIFY_THRESHOLD` (TREEIFY_THRESHOLD默认为8) ，则用平衡树(O(log(n)))来替代链表存储冲突的元素。

    ```
    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            do {
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
                hd.treeify(tab);
        }
    }
    ```

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180228/FkJeibaamI.png?imageslim)

- 自动扩容机制

    `HashMap` 内部数组大小的默认值是16，如果需要存储 `2000000` 个值。在最好的情况下，每个链表将有 `125000` 个条目 `（2000000/16）` 的大小。因此，每个 `get()`， `remove()` 和 `put()` 将导致 `125000` 次迭代/操作。为了避免这种情况， `HashMap` 使用自动扩容机制增加其内部数组，以保持非常短的链表。

    ```
     /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }
    ```
    > 机制原理：当元素个数达到数组大小的 `loadFactor` 后会扩大数组的大小。默认情况下，数组大小是 `16` ， `loadFactor` 是 `0` . `75` ，所有只要 `HashMap` 中的元素超过 `16*0.75=12` 时，数组大小会被扩展到 `2*16=32`。

    ![mark](http://of0qa2hzs.bkt.clouddn.com/blog/180228/h0kK2jB3BD.png?imageslim)

- 线程安全？

    > 了解 `HashMap` 的都应该知道它不是线程安全的，但是为什么呢？

    简单来说：当2个线程同时放入数据的时候， `2` 个 `put()` 操作会同时调整 `Map` 的大小。由于两个线程同时修改链接列表，因此Map可能最终在其链接列表中有一个内部循环（ `Node` 链表形成环形数据结构）。如果您尝试使用内部循环获取列表中的数据，`get()` 将永远不会结束。

    更详细的解释：
    - [酷壳-Java HashMap的死循环](https://coolshell.cn/articles/9606.html)

## Hashtable

> 既然HashMap是线程不安全的，那给它上锁是不是就能保证线程安全了？

- 内部结构

    ```
    public synchronized V put(K key, V value){
        // 省略实现
    }

    public synchronized V get(Object key) {
       // 省略实现
    }

    public synchronized V remove(Object key) {
       // 省略实现
    }
    ```

    可以看到 `Hashtable` 在 `put()`、`get()`、`remove()` 操作上使用了 `synchronized` 来保证线程安全。
    > 加锁固然保证了线程安全，但是也使性能变低。

- 和 `HashMap` 的异同

    - 二者的存储结构和解决冲突的方法都是相同的。

    - `HashTable` 在不指定容量的情况下的默认容量为 `11` ，而 `HashMap` 为 `16` ， `Hashtable` 不要求底层数组的容量一定要为 `2` 的整数次幂，而 `HashMap` 则要求一定为 `2` 的整数次幂。

    - `Hashtable` 中 `key` 和 `value` 都不允许为 `null` ，而 `HashMap` 中 `key` 和 `value` 都允许为 `null（key` 只能有一个为 `null` ，而 `value` 则可以有多个为 `null）

    - `Hashtable` 扩容时，将容量变为原来的 `2` 倍加 `1` ，而 `HashMap` 扩容时，将容量变为原来的 `2` 倍。

    - `Hash`值计算方式不同。

        HashMap：

        ```
        static final int hash(Object key) {
            int h;
            return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
        }
        ```
        > 让低16位同时包含了高位和低位的信息，在计算下标时，由于高位和低位的同时参与，减少 `hash` 的碰撞。

        Hashtable:

        ```
        int hash = key.hashCode();
        ```

        > 直接使用 `key` 的 `Hash` 值

## 参考文章

- [How does a HashMap work in JAVA](http://coding-geek.com/how-does-a-hashmap-work-in-java/)