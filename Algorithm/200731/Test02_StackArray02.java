package com.ssafy.day05;

import java.util.Arrays;
import java.util.Scanner;


public class Test02_StackArray02 {
	Scanner sc = new Scanner(System.in);
	MyArrayStack stack = new MyArrayStack();
	private int menu() {
		System.out.println("-----------------");
		System.out.println("1. 입력하기");
		System.out.println("2. 꺼내기");
		System.out.println("3. 전체 데이터 확인");
		System.out.println("0. 종료");
		System.out.println("-----------------");
		System.out.print("메뉴: ");
		return sc.nextInt();
	}
	
	private void insert() {
		if(stack.isFull()) {
			System.out.println("데이터가 꽉 참;;");
			return;
		}
		System.out.print("입력: ");
		stack.push(sc.nextInt());
	}
	
	private void getData() {
		if(stack.isEmpty()) {
			System.out.println("뺄게 없다;;");
			return;
		}
		System.out.println("꺼낸 값: " + stack.pop());
	}
	
	private void select() {
		System.out.println(stack);
	}
	
	private void exit() {
		System.exit(0);
	}
	
	public void service() {
		while(true) {
			switch(menu()) {
			case 1: insert(); break;
			case 2: getData(); break;
			case 3: select(); break;
			case 0: exit();
			}
		}
	}
	
	public static void main(String[] args) {
		new Test02_StackArray02().service();
	}

}
