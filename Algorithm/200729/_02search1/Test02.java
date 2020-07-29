/**
 * 순서가 있는 나열 : 1, 2 와 2, 1을 각각 다르게 생각한다.
 * 
 * n개 중에서 r개를 뽑는 것(부분집합의 개념)
 * 
 */

import java.util.Arrays;

public class Test02 {
	static int[] arr = { 1, 2, 3, 4 };
	static boolean[] visited;
	static int[] result;

	public static void main(String[] args) {
		// 4개 중에 2개를 선택
		result = new int[2];
		System.out.println("중복을 허용하지 않는 순열 - 반복 사용");
		visited = new boolean[arr.length];
		solve1();
		System.out.println("중복을 허용하지 않는 순열 - 재귀 사용");
		visited = new boolean[arr.length];
		solve2(0);
	}
	static void solve1() {
		for (int i = 0; i < arr.length; i++) {
			if (visited[i] == false) { 
				result[0] = arr[i];
				visited[i] = true;
			}
			
			for (int j = 0; j < arr.length; j++) {
				if (visited[j] == false) {
					result[1] = arr[j];
					System.out.println(Arrays.toString(result));
				}
			}
			visited[i] = false;
		}
	}

	static void solve2(int cnt) {
		// base case : 빠져나갈 조건(기저조건)
		if (cnt == result.length) {
			System.out.println(Arrays.toString(result));
			return;
		}
		for (int i = 0; i < arr.length; i++) {
			if (visited[i] == false) {
				result[cnt] = arr[i];
				visited[i] = true;
				solve2(cnt + 1);
				visited[i] = false;
			}
		}
	}
}