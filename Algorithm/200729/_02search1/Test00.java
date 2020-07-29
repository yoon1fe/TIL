
import java.util.Arrays;

public class Test00 {
	static int[] arr = { 1, 2, 3, 4 };
	static int[] result;

	public static void main(String[] args) {
		// 4개 중에 2개를 선택
		result = new int[4];
		System.out.println("중복을 허용하는 순열 - 반복 사용");
		solve1();
		System.out.println("중복을 허용하는 순열 - 재귀 사용");
		solve2(0);
	}
	static void solve1() {
		for (int i = 0; i < arr.length; i++) {
			result[0] = arr[i];
			for (int j = 0; j < arr.length; j++) {
				result[1] = arr[j];
				for (int k = 0; k < arr.length; k++) {
					result[2] = arr[k];
					for (int m = 0; m < arr.length; m++) {
						result[3] = arr[m];
						System.out.println(Arrays.toString(result));
					}
				}
			}
		}
	}
	static void solve2(int cnt) {
		if (cnt == result.length) {
			System.out.println(Arrays.toString(result));
			return;
		}
		for (int i = 0; i < arr.length; i++) {
			result[cnt] = arr[i];
			solve2(cnt + 1);
		}
	}
}















