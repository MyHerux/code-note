public class reverse {

    public static int reverse(int x) {
        int result = 0;
        while (x != 0) {
            int pop = x % 10;
            if (result > (Integer.MAX_VALUE / 10) || (result == (Integer.MAX_VALUE / 10) && pop > 7)) {
                return 0;
            }

            if (result < (Integer.MIN_VALUE / 10)|| (result == (Integer.MIN_VALUE / 10) && pop < -8)) {
                return 0;
            }
            result = result * 10 + pop;
            x = x / 10;
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(reverse(Integer.MAX_VALUE-1000-Integer.MIN_VALUE%10+9));
    }
}
