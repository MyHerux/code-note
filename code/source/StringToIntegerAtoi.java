public class StringToIntegerAtoi {

    public static int myAtoi(String str) {

        if (str.isEmpty()) {
            return 0;
        }

        int length = str.length();
        int sign = 1;
        int result = 0;
        int i = 0;

        while (i < length && str.charAt(i) == ' ') {
            i++;
        }

        if (i < length && (str.charAt(i) == '-'|| str.charAt(i) == '+')) {
            sign = str.charAt(i++) == '-' ? -1 : 1;
        }

        while (i < length && str.charAt(i) >= '0' && str.charAt(i) <= '9') {
            if (result > Integer.MAX_VALUE / 10 || result == Integer.MAX_VALUE / 10 && str.charAt(i) - '0' > 7) {
                return sign == 1 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            }
            result = result * 10 + (str.charAt(i++) - '0');
        }

        return result * sign;
    }

    public static void main(String[] args) {
        System.out.println(myAtoi(" "));
    }
}
