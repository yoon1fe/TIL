package com.ssafy.day06;

import java.util.Scanner;

public class MyLinkedStackTest {
	Scanner sc = new Scanner(System.in);
	MyLinkedStack list = new MyLinkedStack();

	private int menu() {
		System.out.println("------------------");
		System.out.println("1. �Է�");
		System.out.println("2. ������");
		System.out.println("3. ��ü������ Ȯ��");
		System.out.println("0. ����");
		System.out.println("------------------");
		System.out.print("�޴� : ");
		return Integer.parseInt(sc.nextLine());
	}
	
	public void service() {
		while (true) {
			switch (menu()) {
			case 1: addData(); break;
			case 2: getData(); break;
			case 3:	viewData();  break;
			case 0: exit();
			}
		}
	}
	
	public void addData() {
		System.out.print("�Է� �� : ");
		list.push(sc.nextLine());
	}
	
	public void getData() {
		if (list.isEmpty()) {
			System.out.println("�Էµ� �����Ͱ� �����ϴ�.\n");
			return;
		}
		System.out.println("������ : " + list.pop());	
	}
	
	public void viewData() { System.out.println(list.toString()); }
	
	public void exit() { System.exit(0);}
	
	public static void main(String[] args) {
		MyLinkedStackTest mls = new MyLinkedStackTest();
		mls.service();
	}	
}
