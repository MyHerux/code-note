# 斐波那契数列

## 题目描述

大家都知道斐波那契数列，现在要求输入一个整数 n，请你输出斐波那契数列的第 n 项（从 0 开始，第 0 项为 0，第 1 项是 1）。
n<=39

## 菲波那切数列是什么

```
0 1 1 2 3 5 8 13

n = 0, num = 0
n = 1, num = 1
n = 2, num = 1
.....

当 n = k(n>1),f(k) = f(k-1) + f(k-2)
当 n = 1, f(1) = 1
当 n = 0, f(0) = 0
```

## 递归方法实现

```
public class Solution {
    public int Fibonacci(int n) {
        if(n==0){
            return 0;
        }else if(n==1){
            return 1;
        }else if(n>1){
            return Fibonacci(n-1)+Fibonacci(n-2);
        }else{
            return 0;
        }
    }
}
```

### 存在问题

随着 n 提升，时间复杂度不断增加，时间复杂度：`2^n`

![20200701145656](http://cdn.heroxu.com/20200701145656.png)

我们发现对于某个值，我们重复计算了很多遍，是否可以不重复计算这些值？

## 非递归实现

我们继续查看刚刚的规律

```
当 n = 2的时候， h = f(1) + f(0) = 1 + 0 = 1
当 n = 3的时候， h = f(2) + f(1) = 1 + 1 = 2
当 n = 4的时候， h = f(3) + f(2) = 3 + 1 = 4
```

我们可以从 n = 2，一直循环，直到我们计算出我们输入的值为 n 的时候

我们只需要从最小的开始计算，每次保留中间结果，最后得出我们的第 n 个的结果。

```
public class Solution {
    public int Fibonacci(int n) {
        if(n==0){
            return 0;
        }else if(n==1){
            return 1;
        }else if(n>1){
            int a=0;
            int b=1;
            int ret=0;
            for(int i=0;i<n-1;i++){
                ret=a+b;
                a=b;
                b=ret;
            }
           return ret;
        }else{
            return 0;
        }
    }
}
```

时间复杂度：`n`