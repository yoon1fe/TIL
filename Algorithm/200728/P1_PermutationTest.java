package com.ssafy.day02;

import java.util.Arrays;

//1~N������ ���� ��� �̾� ���������� ����
public class P1_PermutationTest {

	private static int N = 4, R = 2;
	private static int[] numbers;
	private static boolean[] isSelected;

	public static void main(String[] args) {
		numbers = new int[R];
		isSelected = new boolean[N + 1];

		permutation(0);
		
	}

	private static void permutation(int cnt) {	// cnt: ������� ���� ������ ����
		if(cnt == R) {
			System.out.println(Arrays.toString(numbers));
			return;
		}
		for(int i =1; i <= N; i++) {
			if(isSelected[i]) continue;
			
			numbers[cnt] = i;
			isSelected[i] = true;
			permutation(cnt+1);
			isSelected[i] = false;
		}
	}

}
