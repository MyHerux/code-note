# 用两个栈实现队列

## 题目描述

用两个栈来实现一个队列，完成队列的Push和Pop操作。 队列中的元素为int类型。

## 思考

栈：先进后出

队列：先进先出

- 栈A用来作入队列

- 栈B用来出队列，当栈B为空时，栈A全部出栈到栈B,栈B再出栈（即出队列）

## Code

```
import java.util.Stack;

public class Solution {
    Stack<Integer> stack1 = new Stack<Integer>();
    Stack<Integer> stack2 = new Stack<Integer>();
    
    public void push(int node) {
        stack1.push(node);
    }
    
    public int pop() {
        if(stack2.empty()){
            while(!stack1.empty()){
                stack2.push(stack1.pop());
            }
        }
        return stack2.pop();
    }
}
```