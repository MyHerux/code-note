# 209 - Minimum Size Subarray Sum

```
class Solution {
public:
    int minSubArrayLen(int s, vector<int>& nums) {
        int windowSum = 0, windowStart = 0;
        int minWindowSize = numeric_limits<int>::max();

        for(int windowEnd=0; windowEnd<nums.size(); windowEnd++) {
            windowSum += nums[windowEnd];

            while(windowSum >= s) {
                minWindowSize = min(minWindowSize, windowEnd-windowStart+1);
                windowSum -= nums[windowStart];
                windowStart++;
            }
        }

        return minWindowSize == numeric_limits<int>::max() ? 0 : minWindowSize;
    }
};
```