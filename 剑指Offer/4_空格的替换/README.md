# 替换空格

## 题目描述

请实现一个函数，将一个字符串中的每个空格替换成“%20”。例如，当字符串为We Are Happy.则经过替换之后的字符串为We%20Are%20Happy。

## 调用自带的函数

```
public class Solution {
    public String replaceSpace(StringBuffer str) {
    	return str.toString().replace(" ","%20");
    }
}
```

## 用新的字符串来存储

```
public class Solution {
    public String replaceSpace(StringBuffer str) {
        StringBuffer newStr = new StringBuffer();
        for(int i=0;i<str.length();i++){
            char c=str.charAt(i);
            if(' ' == c){
                newStr.append("%20");
            }else{
                newStr.append(c);
            }
        }
    	return newStr.toString();
    }
}
```

## 在当前字符串上进行替换

- 先计算替换后的字符串需要多大的空间，并对原字符串空间进行扩容；

- 从后往前替换字符串的话，每个字符串只需要移动一次；

- 如果从前往后，每个字符串需要多次移动，效率较低。


```
public class Solution {
    public String replaceSpace(StringBuffer str) {
        int spaceNum=0;
        for(int i=0;i<str.length();i++){
            if(str.charAt(i)==' '){
                spaceNum++;
            }
        }
        int oldStrLength=str.length();
        int newStrLenth=oldStrLength+2*spaceNum;
        str.setLength(newStrLenth);
        int oldStrIndex=oldStrLength-1;
        int newStrIndex=newStrLenth-1;
        for(; oldStrIndex >= 0 && oldStrIndex<newStrIndex; oldStrIndex--){
            char c=str.charAt(oldStrIndex);
            if(c==' '){
                str.setCharAt(newStrIndex--,'0');
                str.setCharAt(newStrIndex--,'2');
                str.setCharAt(newStrIndex--,'%');
            }else{
                str.setCharAt(newStrIndex--,c);
            }
        }
        return str.toString();
    }
}
```