# 深入理解ConcurrentHashMap

## 写在前面

前面分析了 [HashMap和Hashtable](http://blog.csdn.net/myherux/article/details/79400962) 。在多线程下， `HashMap` 的链表可能会出现死循环，所以 `HashMap` 是线程不安全的。 `Hashtable` 在所有涉及到多线程操作的都加上了 `synchronized` 关键字来锁住整个 `table` ，虽然保证了线程安全，但是无疑效率是地下的。

针对 `HashMap` 线程不安全的问题， `Java` 给出的推荐是使用 `ConcurrentHashMap` 。 `ConcurrentHashMap` 使用 `降低锁粒度` 的策略，利用多个锁来控制多个小的 `table`（实际上每一个节点都加了锁） ，减少竞争。

## ConcurrentHashMap（JDK1.8）

> `JDK1.8` 和 `JDK1.7` 都是使用 `降低锁粒度` 的策略，只是实现不同。在 `JDK1` . `8` 中， `HashMap` 开始使用 `Node数组`+`链表`+`红黑树` 的数据结构来实现， `ConcurrentHashMap` 在并发控制上加上了 `Synchronized` 和 `CAS` 操作（可以参考[同步锁，乐观锁，悲观锁](http://blog.csdn.net/myherux/article/details/79385871)）。

- 内部存储结构 Node

    ```
    transient volatile Node<K,V>[] table;
 
    static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;
        final K key;
        volatile V val;
        volatile Node<K,V> next;

        Node(int hash, K key, V val, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }

        public final K getKey()       { return key; }
        public final V getValue()     { return val; }
        public final int hashCode()   { return key.hashCode() ^ val.hashCode(); }
        public final String toString(){ return key + "=" + val; }
        // 不允许修改value值
        public final V setValue(V value) {
            throw new UnsupportedOperationException();
        }
        /*
         * Virtualized support for map.get(); overridden in subclasses.
         * 增加find方法辅助get方法  ，HashMap中的Node类中没有此方法
         */
        Node<K,V> find(int h, Object k) {
            Node<K,V> e = this;
            if (k != null) {
                do {
                    K ek;
                    if (e.hash == h &&
                        ((ek = e.key) == k || (ek != null && k.equals(ek))))
                        return e;
                } while ((e = e.next) != null);
            }
            return null;
        }
        ...
    }
    ```

    > `Node` 数据结构和 `HashMap` 基本相同， `ConcurrentHashMap` 的 `Node` 链表只允许对数据进行查找，不允许进行修改。同时， `Node` 数组也加上了 `volatile` 关键字。

- Hash冲突的处理 TreeNode

    ```
    static final class TreeNode<K,V> extends Node<K,V> {
        //树形结构的属性定义
        TreeNode<K,V> parent;  // red-black tree links
        TreeNode<K,V> left;
        TreeNode<K,V> right;
        TreeNode<K,V> prev;    // needed to unlink next upon deletion
        boolean red; //标志红黑树的红节点
        TreeNode(int hash, K key, V val, Node<K,V> next,
                TreeNode<K,V> parent) {
            super(hash, key, val, next);
            this.parent = parent;
        }
        ...
    }
    ```

    > `TreeNode` 继承与 `Node` ,但是数据结构换成了二叉树结构。如果链表长度 `>= TREEIFY_THRESHOLD` (TREEIFY_THRESHOLD默认为8) ，则用平衡树 `(O(log(n)))` 来替代链表存储冲突的元素。

- TreeNode容器 TreeBin

    ```
    static final class TreeBin<K,V> extends Node<K,V> {
        TreeNode<K,V> root;
        volatile TreeNode<K,V> first;
        volatile Thread waiter;
        volatile int lockState;
        // values for lockState
        static final int WRITER = 1; // set while holding write lock
        static final int WAITER = 2; // set when waiting for write lock
        static final int READER = 4; // increment value for setting read lock

        /**
         * Creates bin with initial set of nodes headed by b.
         */
        TreeBin(TreeNode<K,V> b) {
            super(TREEBIN, null, null, null);
            this.first = b;
            TreeNode<K,V> r = null;
            for (TreeNode<K,V> x = b, next; x != null; x = next) {
                next = (TreeNode<K,V>)x.next;
                x.left = x.right = null;
                if (r == null) {
                    x.parent = null;
                    x.red = false;
                    r = x;
                }
                else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K,V> p = r;;) {
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null &&
                                  (kc = comparableClassFor(k)) == null) ||
                                 (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);
                            TreeNode<K,V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            r = balanceInsertion(r, x);
                            break;
                        }
                    }
                }
            }
            this.root = r;
            assert checkInvariants(root);
        }
        ...
    }
    ```

    > `TreeBin` 用于封装维护 `TreeNode` ,当链表转树时( `hash` 冲突过多)，用于封装 `TreeNode` 。实际上， `ConcurrentHashMap` 的红黑树存放的时 `TreeBin` ，而不是 `treeNode` 。

- put() 方法实现分析

    ```
    public V put(K key, V value) {
        return putVal(key, value, false);
    }

       /** Implementation for put and putIfAbsent */
    final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) throw new NullPointerException();
        int hash = spread(key.hashCode());//计算hash值，两次hash操作
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) {//类似于while(true)，死循环，直到插入成功 
            Node<K,V> f; int n, i, fh;
            if (tab == null || (n = tab.length) == 0)//检查是否初始化了，如果没有，则初始化
                tab = initTable();
                /*
                    i=(n-1)&hash 等价于i=hash%n(前提是n为2的幂次方).即取出table中位置的节点用f表示。
                    有如下两种情况：
                    1、如果table[i]==null(即该位置的节点为空，没有发生碰撞)，则利用CAS操作直接存储在该位置，
                        如果CAS操作成功则退出死循环。
                    2、如果table[i]!=null(即该位置已经有其它节点，发生碰撞)
                */
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                if (casTabAt(tab, i, null,
                             new Node<K,V>(hash, key, value, null)))
                    break;                   // no lock when adding to empty bin
            }
            else if ((fh = f.hash) == MOVED)//检查table[i]的节点的hash是否等于MOVED，如果等于，则检测到正在扩容，则帮助其扩容
                tab = helpTransfer(tab, f);//帮助其扩容
            else {//运行到这里，说明table[i]的节点的hash值不等于MOVED。
                V oldVal = null;
                synchronized (f) {//锁定,（hash值相同的链表的头节点）
                    if (tabAt(tab, i) == f) {//避免多线程，需要重新检查
                        if (fh >= 0) {//链表节点
                            binCount = 1;
                            /*
                            下面的代码就是先查找链表中是否出现了此key，如果出现，则更新value，并跳出循环，
                            否则将节点加入到链表末尾并跳出循环
                            */
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)//仅putIfAbsent()方法中onlyIfAbsent为true
                                        e.val = value;//putIfAbsent()包含key则返回get，否则put并返回  
                                    break;
                                }
                                Node<K,V> pred = e;
                                if ((e = e.next) == null) {//插入到链表末尾并跳出循环
                                    pred.next = new Node<K,V>(hash, key,
                                                              value, null);
                                    break;
                                }
                            }
                        }
                        else if (f instanceof TreeBin) { //树节点，
                            Node<K,V> p;
                            binCount = 2;
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                           value)) != null) {//插入到树中
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                    }
                }
                //插入成功后，如果插入的是链表节点，则要判断下该桶位是否要转化为树
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD)//实则是>8,执行else,说明该桶位本就有Node
                        treeifyBin(tab, i);//若length<64,直接tryPresize,两倍table.length;不转树 
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount);
        return null;
    }
    ```

    代码执行思路：
    - 检查 `key/value` 是否为空，处理 `hash` 值

    - 进入 `for` 死循环，因为 `CAS` 的无锁操作需要一直尝试直至成功

    - 检查 `table` 是否初始化，没有则初始化 `initTable()`

    - 根据 `key` 的 `hash` 值找到在 `table` 中的位置 `i` ，取出 `table[i]`的节点 `f`

        - 如果 `f==null` (即该位置的节点为空，没有发生碰撞)
        
            直接 `CAS` 存储,退出循环

        - 如果 `f!=null` (即该位置已经有其它节点，发生碰撞)，检查 `f` 的节点的 `hash` 是否等于 `MOVED`

            a.如果等于，则检测到正在扩容，则帮助其扩容
            b.如果不等于，如果f是链表节点，则直接插入链表；如果是树节点，则插入树中

    - 判断 `f` 是否需要将链表转换为平衡树

    并发控制：

    - 使用 `CAS` 操作插入数据

    - 在每个链表的头结点都使用 `Synchronized` 上锁

## 总结

- JDK1.8的 `ConcurrentHashMap` 中 `Segment` 虽保留，但已经简化属性，仅仅是为了兼容旧版本。

- `ConcurrentHashMap` 的底层与Java1.8的 `HashMap` 有相通之处，底层依然由 `Node数组+链表+红黑树` 来实现的，底层结构存放的是 `TreeBin` 对象，而不是 `TreeNode` 对象；

- `ConcurrentHashMap` 使用 `CAS` 和 `Synchronized` 来保证线程安全


## 参考文章

- [《Java源码分析》：ConcurrentHashMap JDK1.8](http://blog.csdn.net/u010412719/article/details/52145145)