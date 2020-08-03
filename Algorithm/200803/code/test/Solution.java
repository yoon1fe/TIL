package com.ssafy.day06.test;

import java.util.Scanner;

class LinkedList{
	Node head = new Node(0, null);
	class Node{
		int data;
		Node next;
		Node(int data, Node next){
			this.data = data;
			this.next = next;
		}
	}
	
	public void insert(int data) {
		Node newNode = new Node(data, null);
		
		for(Node cur = head; cur != null; cur = cur.next) {
			if(cur.next == null) {
				cur.next = newNode;
				return;
			}
		}
	}
	
	public void insert(int idx, int data) {
		int i = 0;
		for(Node cur = head; cur != null; cur = cur.next, i++) {
			if(i == idx) {
				Node newNode = new Node(data, cur.next);
				cur.next = newNode;
				break;
			}
		}
	}
	 
	public void delete(int x, int y) {
		int i = 0;
		for(Node cur = head; cur != null; cur = cur.next, i++) {
			if(i == x) {
				int cnt = 0;
				while(cnt < y) {
					cur.next = cur.next.next;
					cnt++;
				}
				break;
			}
		}
	}
	
	public void add(int data) {
		for(Node cur = head; cur != null; cur = cur.next) {
			if(cur.next == null) {
				Node newNode = new Node(data, null);
				cur.next = newNode;
				break;
			}
		}
	}
	
	public void printList() {
		int cnt = 0;
		for(Node cur = head.next; cnt <10; cnt++, cur=cur.next)
			System.out.print(cur.data + " ");
		System.out.println();
	}
}

public class Solution {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int T = 1;
		
		for(int test_case = 1; test_case <= T; test_case++) {
			int N = sc.nextInt();
			LinkedList list = new LinkedList();
			for(int i = 0; i< N; i++) list.insert(sc.nextInt());

			int O = sc.nextInt();
			for(int i = 0; i< O; i++) {
				char order = sc.next().charAt(0);
				int x = sc.nextInt();
				int y = sc.nextInt();
				switch(order) {
				case 'I':
					for(int j = 0; j< y ; j++) {
						int num = sc.nextInt();
						list.insert(x++, num);
					}
					break;
				case 'D':
					list.delete(x, y);
					break;
				case 'A':
					list.add(y);
					for(int j = 0; j< x-1 ; j++) {
						int num = sc.nextInt();
						list.add(num);
					}
					break;
				}
				
			}
			System.out.print("#" + test_case + " ");
			list.printList();
		}
	}

}
