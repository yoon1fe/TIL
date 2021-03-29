package com.ssafy.ws.step02;

import java.util.Arrays;

public class ArrayTest {
	static int [] list = new int[10];
	
	static void print() {
		for(int i : list) System.out.print(i + " ");
		System.out.println();
	}
	
	static void total() {
		int sum = 0;
		for(int i : list) sum +=i;
		System.out.println("배열의 합 : " + sum);
		Arrays.stream(list).forEach(v -> System.out.println(v));
	}
	
	static void average() {
		int sum = 0;
		for(int i : list) sum +=i;
		System.out.println("배열의 평균 : " + (double)sum/10);
	}
	
	static void minimum() {
		int min = 101;
		for(int i : list) {
			if(min > i) min = i;
		}
		System.out.println("배열의 최소값 : " + min);
	}
	
	public static void main(String[] args) {
		for(int i = 0;i < 10; i++) 
			list[i] = (int) (Math.random() * 100) + 1;
		
		print();
		total();
		average();
		minimum();
	}

}
