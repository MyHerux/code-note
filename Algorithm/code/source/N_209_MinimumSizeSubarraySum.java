public class N_209_MinimumSizeSubarraySum {

    public static int solution(int s, int[] nums) {
        int left = 0, sum = 0, minLength = Integer.MAX_VALUE;
        for (int right = 0; right < nums.length; right++) {
            sum += nums[right];
            while (sum >= s) {
                minLength = Math.min(minLength, right - left + 1);
                sum -= nums[left++];
            }
        }
        return minLength == Integer.MAX_VALUE ? 0 : minLength;
    }

    public static int solution2(int s, int[] nums) {
        int[] sum = new int[nums.length];
        int minLength = Integer.MAX_VALUE;
        if (nums.length != 0) {
            sum[0] = nums[0];
        }
        for (int i = 1; i < nums.length; i++) {
            sum[i] = sum[i - 1] + nums[i];
        }
        for (int i = 0; i < nums.length; i++) {
            if (sum[i] >= s) {
                minLength = Math.min(minLength, i - binarySearchLastIndexNotBiggerThanTarget(0, i, sum[i] - s, sum));
            }
        }
        return minLength == Integer.MAX_VALUE ? 0 : minLength;
    }

    static int binarySearchLastIndexNotBiggerThanTarget(int left, int right, int target, int[] sum) {
        while (left <= right) {
            int mid = (left + right) >> 1;
            if (sum[mid] > target) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return right;
    }

    public static void main(String[] args) {
        int s = 7;
        int[] nums = {2, 3, 1, 2, 4, 3};
        System.out.println(solution(s, nums));
        System.out.println(solution2(s, nums));
    }

}
