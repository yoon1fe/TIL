/**
 *  재귀호출3 - n번째 피보나치 수 구하기
  
	피보나치 수열이란 첫 번째 항과 두 번째 항이 1이고, 
	세 번째 항부터는 바로 앞의 두 항의 합으로 정의된 수열입니다.
	예를 들어서 세 번째 항은 첫 번째 항(1)과 두 번째 항(1)을 더한 2이며, 
	네 번째 항은 두 번째 항(1)과 세 번째 항(2)을 더한 3이 될 것입니다.

	이러한 방식으로 피보나치 수열의 첫 10개 항은 
	1, 1, 2, 3, 5, 8, 13, 21, 34, 55입니다.
 */

public class Test05 {
	public static void main(String[] args) {
		System.out.println("반복을 이용한 n제곱 구하기 : " + solve1(10));
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("재귀를 이용한 n제곱 구하기 : " + solve2(10));		
	}
	private static int solve1(int cnt) {
		int [] arr = new int[cnt];
		// 배열에 기본값(1, 1) 설정
		arr[0] = 1;
		arr[1] = 1;
		
		for (int i = 2; i < cnt; i++) { 
			// 배열에 값 입력 시 앞에 있는 연속된 두 숫자의 합을 구합니다.
			arr[i] = arr[i-1] + arr[i-2];   
		}
		return arr[cnt - 1];
	}
	private static int solve2(int n) {
		if (n < 2) {
			return n;
		}
		return solve2(n - 1) + solve2(n - 2);
	}
}






