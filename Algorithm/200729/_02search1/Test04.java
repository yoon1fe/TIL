/**
 * 순서가 있는 나열 : 1, 2 와 2, 1을 각각 다르게 생각한다.
 * 
 * n개 중에서 r개를 뽑는 것(부분집합의 개념)
 */
import java.util.Arrays;

public class Test04 {
	static int[] arr = { 1, 2, 3, 4 };
	static int[] result;

	public static void main(String[] args) {
		result = new int[2];
		System.out.println("중복을 허용하는 조합 - 반복 사용");
		solve1();
		System.out.println("중복을 허용하는 조합 - 재귀 사용");
		solve2(0, 0);
	}
	
	static void solve1() {
		for (int i = 0; i < arr.length; i++) {
			result[0] = arr[i];
			for (int j = i; j < arr.length; j++) {
				result[1]= arr[j];
				System.out.println(Arrays.toString(result));
			}
		}
	}

	static void solve2(int n, int cnt) {
		if (cnt == result.length) {
			System.out.println(Arrays.toString(result));
			return;
		}
		for (int i = n; i < arr.length; i++) {
			result[cnt] = arr[i];
			solve2(i, cnt + 1);
		}
	}
}