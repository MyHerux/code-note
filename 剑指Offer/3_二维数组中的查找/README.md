# 二维数组中的查找

## 题目描述

在一个二维数组中（每个一维数组的长度相同），每一行都按照从左到右递增的顺序排序，每一列都按照从上到下递增的顺序排序。请完成一个函数，输入这样的一个二维数组和一个整数，判断数组中是否含有该整数。



## 遍历全部数组 

下面是这样一个规律的二维数组

```
1 2 3 4 
2 3 4 5 
4 6 7 10
9 11 13 15
```

对数组中的每一个数字进行遍历

```
public class Solution {
    public boolean Find(int target, int [][] array) {
        if(array.length==0){
            return false;
        }
        int m=array.length;
        int n=array[0].length;
        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++){
                if(array[i][j]==target){
                    return true;
                }
            }
        }
        return false;
    }
}
```

## 利用数组本身的有序性

二维数组是有序的，从右上角来看，向左数字递减，向下数字递增。

因此从右上角开始查找，

当要查找数字比右上角数字大时，下移；
当要查找数字比右上角数字小时，左移；
如果出了边界，则说明二维数组中不存在该整数。

Case：
当我们要查询 `7` 的时候

```
首先比较的是 4，然后发现 4 < 7 ，因此肯定不在第一行，下移
然后比较 5 ， 5 < 7，那么也不在第二行，下移
然后比较 10， 10 > 7，因此可能就在这一行，左移，最终找到了7
```

实现：

```
public class Solution {
    public boolean Find(int target, int [][] array) {
        if (array == null || array.length == 0 || array[0].length == 0) {
            return false;
        }
        int m=0;
        int n=array[0].length-1;
        int tem=array[m][n];
        while(tem!=target){
            if((m<array.length-1)&&(n>0)){
               if(tem>target){
                    n--;
               }else{
                    m++;
                }
                tem=array[m][n];
            }else{
                return false;
            }
            
        }
        return true;
    }
}
```