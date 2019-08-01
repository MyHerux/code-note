public class convert {


    public static String convert(String s, int numRows) {
        int length = s.length();
        int nodeLength = numRows * 2 - 2;

        if (length == 0 | numRows == 0 | numRows == 1) {
            return s;
        }
        String result = "";
        for (int i = 0; i < numRows; i++) {
            for (int j = i; j < length; j += nodeLength) {
                // 整列数据直接等差
                result += s.charAt(j);
                int single = j - 2 * i + nodeLength;
                // 去除首行，尾行，且不能超过最长长度
                if (i != 0 && i != numRows - 1 && single < length) {
                    result += s.charAt(single);
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        System.out.println(convert("ABCDEFGHIGKLMNO", 5));
    }

}
