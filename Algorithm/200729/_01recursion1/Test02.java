/**
 * 재귀호출 이해하기2
 */


public class Test02 {
	public static void main(String[] args) {
		System.out.println("반복 출력");
		solve1(10);  // 반복 이용

		System.out.println("재귀 출력");
		solve2(10);  // 재귀 이용
	}

	private static void solve1(int val) {
		for (int i = 1; i <= val; i++) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(i);
		}
	}
	
	private static void solve2(int val) {
		if (val == 0) {
			return;
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		solve2(val - 1);
		System.out.println(val);
	}
	

}




