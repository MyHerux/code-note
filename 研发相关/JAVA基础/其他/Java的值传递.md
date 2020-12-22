## 1.问题
一直在使用线程，但是突然发现一个问题，原始类型的数据不会改变，引用类型的数据会发生改变。
代码如下：
```
public class thread {
    public static void main(String args[]) {
        JSONObject mobileInfo = new JSONObject();
        String phone = "111";
        MobileThread mobileThread = new MobileThread();
        mobileThread.setPhone(phone);
        mobileThread.setMobileInfo(mobileInfo);
        Thread thread = new Thread(mobileThread);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("mobileInfo:" + mobileInfo.toString());
        System.out.println("phone:" + phone);
        System.out.println("phone:" + mobileThread.getPhone());

    }
}

class MobileThread implements Runnable {
    private String phone;
    private JSONObject mobileInfo;
    public void setMobileInfo(JSONObject mobileInfo) {
        this.mobileInfo = mobileInfo;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(8000);
            System.out.println("====");
            mobileInfo.put("key", "11");
            phone = "333";
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
结果：
![运行结果](http://img.blog.csdn.net/20160725202716679)
	 
可以看出 `JSONOBJECT` 对象里面的值是改变了的，但是 `phone` 的值是没有改变的，必须要使用 `get` 方法才可以获取发生改变的那个值。

## 2.想法
    
难道是java里面的值传递和引用传递？

## 3.测试结果

查阅了很多文档和博客，原来 `java` 只有值传递没有引用传递。

`Java` 参数，不管是原始类型还是引用类型，传递的都是副本(有另外一种说法是传值，但是说传副本更好理解吧，传值通常是相对传址而言)。

如果参数类型是原始类型，那么传过来的就是这个参数的一个副本，也就是这个原始参数的值，这个跟之前所谈的传值是一样的，如果在函数中改变了副本的值不会改变原始的值。所以 `phone` 的值是没有发生改变的。
    
如果参数类型是引用类型，那么传过来的就是这个引用参数的副本，这个副本存放的是参数的地址。如果在函数中改变了副本的地址，如 `new` 一个，那么副本就指向了一个新的地址，此时传入的参数还是指向原来的地址，所以不会改变参数的值。

测试如下：
```
package com.xu.test;

import net.sf.json.JSONObject;

public class ParamTest {
    public static void main(String[] args) {
        /**
         * Test 1: 原始类型数据
         */
        int percent = 10;
        System.out.println("Before: percent=" + percent);
        t1(percent);
        System.out.println("After: percent=" + percent);

        /**
         * Test 2: 引用类型数据
         */
        System.out.println("\nTesting t2:");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key","123");
        System.out.println("Before: jsonObject=" +jsonObject);
        t2(jsonObject);
        System.out.println("After: jsonObject=" + jsonObject.toString());

        /**
         * Test 3: 地址改变
         */
        System.out.println("\nTesting t3:");
        JSONObject a = new JSONObject();
        a.put("key","123");
        JSONObject b = new JSONObject();
        b.put("key","456");
        System.out.println("Before: a=" + a.toString());
        System.out.println("Before: b=" + b.toString());
        t3(a, b);
        System.out.println("After: a=" + a.toString());
        System.out.println("After: b=" + b.toString());
    }

    private static void t3(JSONObject x, JSONObject y) {
        JSONObject temp = x;
        x = y;
        y = temp;
        System.out.println("End of method: x=" + x.toString());
        System.out.println("End of method: y=" + y.toString());
    }

    private static void t2(JSONObject x) {
        x.put("key2","456");
        System.out.println("End of method: x=" + x.toString());
    }

    private static void t1(int x) {
        x = 3 * x;
        System.out.println("End of Method X= " + x);
    }
}
```
结果：
![这里写图片描述](http://img.blog.csdn.net/20160725204746157)

## 4.结论

- Test1

    基本数据类型，对于形参的修改不会影响实参。

- Test2

    引用类型传引用，形参和实参指向同一个内存地址，对于形参的修改会影响实际的对象。

- Test3

    地址改变，对于形参的修改不会影响实际的对象