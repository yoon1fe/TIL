package com.ssafy.day02;

import java.util.Arrays;
import java.util.Scanner;

//1~N������ ���� ��� �̾� ���������� ����
public class C1_CombinationTest {

	private static int N, R;
	private static int[] numbers, input;

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		N = sc.nextInt();
		R = sc.nextInt();
		
		numbers = new int[R];
		input = new int[N];
		
		for(int i =0; i< N; i++) {
			input[i] = sc.nextInt();
		}
		
		combination(0, 0);
	}

	//������ �ڸ��� ���ռ� �̱�
	private static void combination(int cnt, int start) {	// cnt: ������� ���� ������ ����
		if(cnt == R) {
			System.out.println(Arrays.toString(numbers));
			return;
		}
		
		for(int i = start; i < N; i++) {
			numbers[cnt] = input[i];
			combination(cnt+1, i+1);
		}
	}

}
