

public class Test03 {

	public static void main(String[] args) {
		System.out.println("반복을 이용한 합계: " + solve1(10));
		System.out.println("재귀를 이용한 합계: " + solve2(10));
	}

	private static int solve1(int val) {
		int sum = 0 ;
		for (int i = 1; i<=val;i++) {
			sum+=i;
		}
		return sum;
	}
	
	private static int solve2(int val) {
		if(val < 1) {
			return val;
		}
		
		return val + solve2(val-1);
	}
}
