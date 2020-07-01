# 跳台阶问题

## 题目描述

一只青蛙一次可以跳上1级台阶，也可以跳上2级。求该青蛙跳上一个n级的台阶总共有多少种跳法（先后次序不同算不同的结果）。

## 跳台阶规律

![20200701230719](http://cdn.heroxu.com/20200701230719.png)

```
当 n = 1的时候， h = f(1) = 1
当 n = 2的时候， h = f(2) = 2
当 n = 3的时候， h = f(2) + f(1) = 2 + 1 = 3
当 n = 4的时候， h = f(3) + f(2) = 3 + 2 = 5
```

## 实现-递归

```
public class Solution {
    public int JumpFloor(int target) {
        if(target==0){
            return 0;
        }else if(target==1){
            return 1;
        }else if(target==2){
            return 2;
        }else{
            int ret=0;
            int a=1;
            int b=2;
            for(int i=3;i<target+1;i++){
                ret=a+b;
                a=b;
                b=ret;
            }
            return ret;
        }
    }
}
```

---

## 题目描述

一只青蛙一次可以跳上1级台阶，也可以跳上2级……它也可以跳上n级。求该青蛙跳上一个n级的台阶总共有多少种跳法。  

## 找规律

```
当 n = 1   1
当 n = 2   2
当 n = 3   4
当 n = 4   8
当 n = n   2^(n-1)
```

## 实现-贪心

```
public class Solution {
    public int JumpFloorII(int target) {
        if(target==1){
            return 1;
        }else{
            int ret=0;
            int a=1;
            for(int i=2;i<target+1;i++){
                ret=2*a;
                a=ret;
            };
            return ret;
        }
        
    }
}
```