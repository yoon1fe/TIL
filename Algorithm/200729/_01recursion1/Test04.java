
public class Test04 {

	public static void main(String[] args) {
		System.out.println("반복을 이용한 제곱: " + solve1(10, 4));
		System.out.println("재귀를 이용한 제곱: " + solve2(10, 4));
	}

	private static int solve1(int num, int cnt) {
		int answer = 1;
		for(int i =0; i<cnt; i++) {
			answer *= num;
		}
		return answer;
	}
	
	private static int solve2(int num, int cnt) {
		if(cnt == 1) {
			return num;
		}
		return num * solve2(num, cnt-1);
	}
}
