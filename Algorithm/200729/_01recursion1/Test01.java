/**
 * 재귀호출 이해하기1
 * 
 * - 자기자신을 호출하는 함수
 * - 베이스 조건이 없을 경우 무한반복 될 수 있다.
 * - 재귀와 반복은 서로 변환이 가능하다.
 * - 재귀함수는 콜스텍이 계속 쌓이게 된다.
 */
public class Test01 {
	// 베이스 조건이 없어 무한반복되는 케이스
	public static void main(String[] args) {
		System.out.println("main");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		main(null);
	}
}
