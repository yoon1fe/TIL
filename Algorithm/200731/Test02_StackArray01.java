package com.ssafy.day05;

import java.util.Arrays;
import java.util.Scanner;

public class Test02_StackArray01 {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		final int CAPACITY = 5;
		int[] data = new int[CAPACITY];
		int top = -1;
		
		
		while(true) {
			System.out.println("-----------------");
			System.out.println("1. 입력하기");
			System.out.println("2. 꺼내기");
			System.out.println("3. 전체 데이터 확인");
			System.out.println("0. 종료");
			System.out.println("-----------------");
			System.out.print("메뉴: ");
			int menu = sc.nextInt();
			switch(menu) {
			case 1:
				if(top == CAPACITY-1) {
					System.out.println("데이터가 꽉 참;;");
					break;
				}
				System.out.print("입력: ");
				data[++top] = sc.nextInt();
				break;
			case 2:
				if(top == -1) {
					System.out.println("뺄게 없다;;");
					break;
				}
				System.out.println("까낸 값: " + data[top]);
				data[top--] = 0;
				break;
			case 3:
				System.out.println(Arrays.toString(data));
				break;
			case 0:
				System.exit(0);
			}
			
			
			
		}
	}

}
