import java.util.HashMap;
import java.util.Map;

class twoSum {

    public static int[] twoSum(int[] nums, int target) throws Exception {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            map.put(nums[i], i);
        }
        for (int i = 0; i < nums.length; i++) {
            Integer key = target - nums[i];
            if (map.containsKey(key) && i != map.get(key)) {
                return new int[]{i, map.get(key)};
            }
        }
        throw new Exception("no solution");
    }

    public static void main(String[] args) throws Exception {
        int nums[] = {2, 7, 11, 15};
        int result[] = twoSum(nums, 9);
    }

}