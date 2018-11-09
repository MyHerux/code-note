import struct.ListNode;

public class addTwoNumbers {

    public static ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode temp = null;
        ListNode l3 = null;

        int mod = 0;
        // 只有满足三种情况才退出计算
        while (l1 != null || l2 != null || mod != 0) {
            int sum = (l1 == null ? 0 : l1.val) + (l2 == null ? 0 : l2.val) + mod;
            mod = sum / 10;
            //产生 l3 当前节点
            ListNode node = new ListNode(sum % 10);
            if (temp == null) {
                // 产生 head 节点
                temp = node;
                l3 = temp;
            } else {
                temp.next = node;
                // 移动指针
                temp = temp.next;
            }
            // l1 l2 指向下一个节点
            l1 = l1 == null ? null : l1.next;
            l2 = l2 == null ? null : l2.next;
        }
        return l3;
    }

    public static void main(String[] args) {
        ListNode l1=new ListNode(2);
        ListNode l1N1=new ListNode(4);
        ListNode l1N2=new ListNode(3);
        l1.next=l1N1;
        l1N1.next=l1N2;

        ListNode l2=new ListNode(5);
        ListNode l2N1=new ListNode(6);
        ListNode l2N2=new ListNode(4);
        l2.next=l2N1;
        l2N1.next=l2N2;

        System.out.println(addTwoNumbers(l1,l2).val);
        System.out.println(addTwoNumbers(l1,l2).next.val);
        System.out.println(addTwoNumbers(l1,l2).next.next.val);

    }

}
