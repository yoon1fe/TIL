package com.ssafy.day02;

import java.util.Arrays;
import java.util.Scanner;

//1~N������ ���� ��� �̾� ���������� ����
public class P2_PermutationInputTest {

	private static int N, R;
	private static int[] numbers, input;
	private static boolean[] isSelected;

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		N = sc.nextInt();
		R = sc.nextInt();
		
		numbers = new int[R];
		isSelected = new boolean[N + 1];
		input = new int[N];
		
		for(int i =0; i< N; i++) {
			input[i] = sc.nextInt();
		}
		
		permutation(0);
	}

	private static void permutation(int cnt) {	// cnt: ������� ���� ������ ����
		if(cnt == R) {
			System.out.println(Arrays.toString(numbers));
			return;
		}
		for(int i =0; i < N; i++) {
			if(isSelected[i]) continue;
			
			numbers[cnt] = input[i];
			isSelected[i] = true;
			permutation(cnt+1);
			isSelected[i] = false;
		}
	}

}
