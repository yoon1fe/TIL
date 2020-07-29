/**
 * 조합(Combination) : 1, 2 와 2, 1을 같은 것으로 생각한다.
 * 
 * n개 중에서 r개를 뽑는 것(부분집합의 개념)
 */

import java.util.Arrays;

public class Test03 {
	static int[] arr = { 1, 2, 3, 4 };
	static int[] result;

	public static void main(String[] args) {
		// 4개 중에 2개 뽑는 연습입니당
		result = new int[2];
		System.out.println("중복을 허용하지 않는 조합 - 반복 사용");
		solve1();
		System.out.println("중복을 허용하는 않는 조합 - 재귀 사용");
		solve2(0, 0);
	}
	static void solve1() {
		for (int i = 0; i < arr.length; i++) {
			result[0] = arr[i];
			for (int j = i + 1; j < arr.length; j++) {
				result[1]= arr[j];
				System.out.println(Arrays.toString(result));
			}
		}
	}
	
	// 조합은 인자가 두개
	static void solve2(int begin, int cnt) {
		if (cnt == result.length) {
			System.out.println(Arrays.toString(result));
			return;
		}
		
		for (int i = begin; i < arr.length; i++) {
			result[cnt] = arr[i];
			solve2(i + 1, cnt + 1);
		}
	}
}