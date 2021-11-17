/* The isBadVersion API is defined in the parent class VersionControl.
      boolean isBadVersion(int version); */

public class Solution extends VersionControl {
    public int firstBadVersion(int n) {
        return binarySearch(1, n);
    }
    
    public int binarySearch(int start, int end) {
        if (start >= end) return start;
        int mid = start / 2 + end / 2;
        
        if (!isBadVersion(mid)) {
            return binarySearch(mid + 1, end);
        } else return binarySearch(start, mid);
        
    }
}