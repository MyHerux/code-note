public class LongestCommonPrefix {

    public String longestCommonPrefix(String[] strs) {

        if (strs.length == 0) {
            return "";
        }

        String s0 = strs[0];

        for (int i = 0; i < s0.length(); i++) {
            char c = s0.charAt(i);
            for (int j = 1; j < strs.length; j++) {
                if (i == strs[j].length() || strs[j].charAt(i) != c) {
                    return s0.substring(0, i);
                }
            }
        }

        return s0;
    }
}
