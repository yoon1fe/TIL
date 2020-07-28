package com.ssafy.day02;

import java.util.Arrays;
import java.util.Scanner;

//1~N까지의 수를 모두 뽑아 순서적으로 나열
public class Ex_DiceTest {

	private static int N, totalCnt;
	private static int[] numbers;
	private static boolean[] isSelected;
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		N = sc.nextInt();
		numbers = new int[N];
		isSelected = new boolean[7];
		
		int mode = sc.nextInt();
		totalCnt = 0;
		
		switch(mode) {
		case 1:		//중복 순열
			dice1(0);
			break;
			
		case 2:
			dice2(0);
			break;
			
		case 3:		//중복 조합
			dice3(0, 1);
			break;
			
		case 4:		//조합
			dice4(0, 1);
			break;
		}
		System.out.println("총 경우의 수: " + totalCnt);
	}

	private static void dice1(int cnt) {	// 중복 순열
		if(cnt == N) {
			++totalCnt;
			System.out.println(Arrays.toString(numbers));
			return;
		}
		for(int i = 1; i<=6;i++) {
			numbers[cnt] = i;
			dice1(cnt+1);
		}
		
	}
	
	private static void dice2(int cnt) {
		if(cnt == N) {
			++totalCnt;
			System.out.println(Arrays.toString(numbers));
			return;
		}
		for(int i = 1; i<=6;i++) {
			if(isSelected[i]) continue;
			numbers[cnt] = i;
			isSelected[i] = true;
			dice2(cnt+1);
			isSelected[i] = false;
		}
		
	}
	

	private static void dice3(int cnt, int start) {
		if(cnt == N) {
			++totalCnt;
			System.out.println(Arrays.toString(numbers));
			return;
		}
		
		for(int i = start; i <= 6; i++) {
			numbers[cnt] = i;
			dice3(cnt+1, i);
			
		}
	}
	
	private static void dice4(int cnt, int start) {
		if(cnt == N) {
			++totalCnt;
			System.out.println(Arrays.toString(numbers));
			return;
		}
		
		for(int i = start; i <= 6; i++) {
			numbers[cnt] = i;
			dice4(cnt+1, i + 1);
			
		}
	}
}
