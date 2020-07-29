
public class Test08 {

	public static void main(String[] args) {
		int [] arr = {1, 3, 5, 7, 9};
		System.out.println("반복을 이용한 배열 원소 구하기: " + search1(arr, 9));
		System.out.println("재귀를 이용한 배열 원소 구하기: " + search2(arr, 0, 9));
	}

	private static int search1(int[] arr, int num) {
		for(int i = 0; i< arr.length; i++) {
			if(arr[i] == num) return i;
		}
		return -1;
	}
	
	private static int search2(int[] arr, int idx, int num) {
		if(idx == arr.length) {
			return -1;
		}
		if(arr[idx] == num) return idx;
		else return search2(arr, idx+1, num);
	}
}
