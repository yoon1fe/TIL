/**
 *   pop()  : ������ ����
 *   peek() : ������
 *   push() : �Է�
 *   isEmpty(): ����ִ��� üũ
 */
package com.ssafy.day06;

import java.util.Arrays;

public class MyLinkedStack {
	Node top = null;
	
	class Node{
		String data;
		Node link;
	}
	
	
	public void push(String item) {
		Node newNode = new Node();
		newNode.data = item;
		if(top == null) {
			top = newNode;
		}
		else {
			newNode.link = top;
			top = newNode;
		}
	}
	
	public boolean isEmpty(){
		return top == null;
	}
	
	public String peek() {
		return top.data;
	}
	
	public String pop() {
		String data = peek();
		top = top.link;
		
		return data;
	}
	
	public String toString() {
		StringBuilder output = new StringBuilder("List [");
		if(top == null) return output.append("]").toString();
		
		for(Node cur = top; cur != null; cur = cur.link) output.append(cur.data + " ");
		output.append("]");
		return output.toString();
	}
	
	
}