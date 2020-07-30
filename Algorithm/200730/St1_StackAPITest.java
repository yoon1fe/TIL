package com.ssafy.day03;

import java.util.Arrays;
import java.util.Scanner;

public class St1_StackAPITest {

	static boolean[] isSelected;
	static int N, S, totalCnt = 0;
	static int[] input;
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		N = sc.nextInt();
		S = sc.nextInt();
		
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
			int sum = 0;
			for(int i = 0; i< N; i++) {
				if(isSelected[i]) sum+= input[i];
			}
			if(sum == S) {
				++totalCnt;
				for(int i = 0; i<N; i++) {
					System.out.print( isSelected[i] ? input[i] : "X");
					System.out.print("\t");
				}
				System.out.println();
			}
			return;
		}
		
		//부분집합 구성에 포함
		isSelected[cnt] = true;
		generateSubset(cnt+1);
		//부분집합 구성에 미포함
		isSelected[cnt] = false;
		generateSubset(cnt+1);
	}

}
