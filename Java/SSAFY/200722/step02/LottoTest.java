package com.ssafy.ws.step02;

public class LottoTest {

	public static void main(String[] args) {
		boolean []check = new boolean[46];
		
		int cnt = 0;
		
		while(cnt < 6) {
			int n = (int)(Math.random() * 45) + 1;
			if(check[n]) continue;
			
			System.out.println(n);
			check[n] = true;
			cnt++;
		}
	}

}
