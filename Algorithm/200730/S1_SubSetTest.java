package com.ssafy.day03;

import java.util.Scanner;

public class S1_SubSetTest {

	static boolean[] isSelected;
	static int N;
	static int[] input;
	static int totalCnt = 0;
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		N = sc.nextInt();
		input = new int[N];
		isSelected = new boolean[N];
		
		for(int i = 0; i< N; i++) {
			input[i] = sc.nextInt();
		}
		

		generateSubset(0);
		System.out.println("총 경우의 수: " + totalCnt);
	}
	
	private static void generateSubset(int cnt) {
		if(cnt == N) {
			++totalCnt;
			for(int i = 0; i<N; i++) {
				System.out.print( isSelected[i] ? input[i] : "X");
				System.out.print("\t");
			}
			System.out.println();
			return;
		}
		
		//부분집합 구성에 초함
		isSelected[cnt] = true;
		generateSubset(cnt+1);
		isSelected[cnt] = false;
		generateSubset(cnt+1);
	}

}
