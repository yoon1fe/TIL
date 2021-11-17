class Solution {
    public int searchInsert(int[] nums, int target) {
        return binarySearch(0, nums.length - 1, nums, target);
    }
    
    public int binarySearch(int start, int end, int[] nums, int target) {
        if (start > end) return start;
        
        int mid = (start + end) / 2;
        
        if (nums[mid] == target) return mid;
        else if (nums[mid] < target) return binarySearch(mid + 1, end, nums, target);
        else return binarySearch(start, mid - 1, nums, target);
    }
}