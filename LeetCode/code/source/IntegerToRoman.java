public class IntegerToRoman {

    public String intToRoman(int num) {
        StringBuilder res = new StringBuilder();
        char roman[] = {'M', 'D', 'C', 'L', 'X', 'V', 'I'};
        int value[] = {1000, 500, 100, 50, 10, 5, 1};

        for (int i = 0; i < 7; i = i + 2) {
            int x = num / value[i];
            if (x < 4) {
                for (int j = 0; j < x; j++) {
                    res.append(roman[i]);
                }
            } else if (x == 4) {
                res.append(roman[i]).append(roman[i - 1]);
            } else if (x < 9) {
                res.append(roman[i - 1]);
                for (int j = 5; j < x; j++) {
                    res.append(roman[i]);
                }
            } else {
                res.append(roman[i]).append(roman[i - 2]);
            }
            num = num % value[i];
        }

        return res.toString();
    }
}
