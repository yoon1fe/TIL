
public class Test07 {

	public static void main(String[] args) {
		int [] arr = {1, 3, 5, 7, 9};
		System.out.println("반복을 이용한 배열 합 구하기: " + solve1(arr));
		System.out.println("재귀를 이용한 배열 합 구하기: " + solve2(arr.length, arr));
	}

	private static int solve1(int[] data) {
		int answer = 0;

		for(int i : data) {
			answer += i;
		}
		return answer;
	}
	
	private static int solve2(int len, int[] data) {
		if(len == 0) {
			return 0;
		}
		
		return data[len-1] + solve2(len-1, data);
	}
}
