# 209 - Minimum Size Subarray Sum

## 题目

给定一个含有 `n` 个正整数的数组和一个正整数 `s` ，找出该数组中满足其和 `≥s` 的长度最小的连续子数组。如果不存在符合条件的连续子数组，返回 `0` 。

示例：

```
输入: s = 7, nums = [2,3,1,2,4,3]
输出: 2
解释: 子数组 [4,3] 是该条件下的长度最小的连续子数组。
```

进阶:

如果你已经完成了 `O(n)` 时间复杂度的解法, 请尝试 `O(n log n)` 时间复杂度的解法。

## Solution 1

> 时间复杂度O(n)

定义2个指针 `left` 和 `right` 指针：

1. `right` 先右移，直到第一次满足 `sum≥s`

2. `left` 开始右移，直到第一次满足 `sum < s`

重复上面的步骤，直到 right 到达末尾，且 left 到达临界位置，即要么到达边界，要么再往右移动，和就会小于给定值。

```
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
```

## Solution 2

> 时间复杂度 O(nlogn)

1. 建立一个比原数组长一位的 `sum` 数组，其中 `sum[i]` 表示 `nums` 数组中 `[0, i - 1]` 的和

2. 对于 `sum` 中每一个值 `sum[i]`，用二分查找法找到子数组的右边界位置，使该子数组之和大于 `sum[i] + s`，然后更新最短长度的距离

```
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
```