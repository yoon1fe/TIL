

public class Test06 {

	public static void main(String[] args) {
		System.out.println("반복을 이용한 자리수 합 구하기: " + solve1(13689));
		System.out.println("재귀를 이용한 자리수 합 구하기: " + solve2(13689));
	}

	private static int solve1(int num) {
		int answer = 0;

		while(true) {
			answer += num % 10;
			if(num < 10) break;
			num/= 10;
		}
		return answer;
	}
	
	private static int solve2(int num) {
		if(num < 1) {
			return num;
		}
		return num%10 + solve2(num/10);
	}
}
