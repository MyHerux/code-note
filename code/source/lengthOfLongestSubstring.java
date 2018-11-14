import java.util.HashMap;

public class lengthOfLongestSubstring {

    public static int lengthOfLongestSubstring(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }

        HashMap<Character, Integer> map = new HashMap<>();
        int max = 0;
        int pre = -1;
        for (int i = 0, length = s.length(); i < length; i++) {
            char ch = s.charAt(i);
            Integer chIndex = map.get(ch);
            map.put(ch, i);
            // 更新重复单词位置
            if (chIndex != null) {
                pre = Math.max(pre, chIndex);
            }
            max = Math.max(max, i - pre);
        }

        return max;
    }

    public static int lengthOfLongestSubstring2(String s) {

        int index[] = new int[128];
        int max = 0;
        int pre = 0;
        for (int i = 0, length = s.length(); i < length; i++) {
            char ch = s.charAt(i);
            Integer chIndex = index[ch];
            pre = Math.max(pre, chIndex);
            max = Math.max(max, i - pre + 1);
            index[ch] = i + 1;
        }

        return max;
    }

    public static void main(String[] args) {
        //System.out.println(lengthOfLongestSubstring("apwwkew"));
        System.out.println(lengthOfLongestSubstring2("abcdpwwkew"));
    }
}
