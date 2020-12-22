# ArrayList、Vector、LinkedList分析

## ArrayList

> 实质是包装了一个数组，遍历时很快，但是插入、删除时都需要移动后面的元素，效率略差些。

- add()方法

    ```
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
    ```

- 自动扩容

    ```
    private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
    
        ensureExplicitCapacity(minCapacity);
    }
    
    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;
    
        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
    
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        // 扩展为原来的1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        // 如果扩为1.5倍还不满足需求，直接扩为需求值
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
    ```

- set()与get()

    ```
    public E set(int index, E element) {
        rangeCheck(index);

        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }

    public E get(int index) {
        rangeCheck(index);

        return elementData(index);
    }
    ```
## Vector

> 被称为线程安全的ArrayList

- 存储结构

    > 和ArrayList一样，也是一个数组 

    ```
    protected Object[] elementData;
    ```

- add()方法

    ```
    public synchronized boolean add(E e) {
        modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = e;
        return true;
    }
    ```

- 自动扩容

    ```
    private void ensureCapacityHelper(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        //如果增长量 capacityIncrement 不大于 0 ，就扩容 2 倍
        int newCapacity = oldCapacity + ((capacityIncrement > 0) ?
                                         capacityIncrement : oldCapacity);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
    ```

- set()方法

    > 使用 `synchronized` 字段保证线程安全，效率降低。 `Vector` 在可能出现线程问题的地方都添加了 `synchronized` 字段

    ```
    public synchronized E set(int index, E element) {
        if (index >= elementCount)
            throw new ArrayIndexOutOfBoundsException(index);

        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }
    ```

## LinkedList

> LinkedList是以链表实现的，插入、删除时只需要改变前后两个节点指针指向即可。这样同时带来一个缺点：不能随机访问，需要移动指针。

- 类定义

    ```
    public class LinkedList<E>
        extends AbstractSequentialList<E>
        implements List<E>, Deque<E>, Cloneable, java.io.Serializable
    {}
    ```
    > AbstractSequenceList 提供了 `List` 接口骨干性的实现以减少实现 `List` 接口的复杂度， `Deque` 接口定义了双端队列的操作。

- 内部存储结构

    ```
    //头节点
    transient Node<E> first;
    //尾节点
    transient Node<E> last;
    //双向节点
    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
    ```
- add()


    普通的在尾部添加元素：
    ```
    public void add(int index, E element) {
        checkPositionIndex(index);

        if (index == size)
            linkLast(element);
        else
            linkBefore(element, node(index));
    }
    ```

    ```
    //插入数据到尾节点
    void linkLast(E e) {
        //获取尾部节点
        final Node<E> l = last;
        //新建一个节点，头部指向之前的 尾节点 last
        final Node<E> newNode = new Node<>(l, e, null);
        //last 指向新建的节点
        last = newNode;
        //如果之前是空链表， 新建的节点也是第一个节点
        if (l == null)
            first = newNode;
        else
            //原来的尾节点尾部指向新建的尾节点
            l.next = newNode;
        size++;
        modCount++;
    }
    ```

    在指定位置添加元素：
    ```
    public void add(int index, E element) {
        checkPositionIndex(index);
        //指定位置也有可能是在尾部
        if (index == size)
            linkLast(element);
        else
            linkBefore(element, node(index));
    }
    ```

    ```
    //在 指定节点 前插入一个元素，这里假设 指定节点不为 null
    void linkBefore(E e, Node<E> succ) {
        // 获取指定节点 succ 前面的一个节点
        final Node<E> pred = succ.prev;
        //新建一个节点，头部指向 succ 前面的节点，尾部指向 succ 节点，数据为 e
        final Node<E> newNode = new Node<>(pred, e, succ);
        //让 succ 节点头部指向 新建的节点
        succ.prev = newNode;
        //如果 succ 前面的节点为空，说明 succ 就是第一个节点，那现在新建的节点就变成第一个节点了
        if (pred == null)
            first = newNode;
        else
            //如果前面有节点，让前面的节点
            pred.next = newNode;
        size++;
        modCount++;
    }
    ```
- set()

    > 直接遍历找到节点替换值

    ```
    public E set(int index, E element) {
        checkElementIndex(index);
        Node<E> x = node(index);
        E oldVal = x.item;
        x.item = element;
        return oldVal;
    }
    ```

- get()

    > 遍历找到节点，返回值

    ```
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }
    ```
