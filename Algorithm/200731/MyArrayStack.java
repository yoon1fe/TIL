package com.ssafy.day05;

import java.util.Arrays;

public class MyArrayStack {
	final int CAPACITY = 5;
	int[] data = new int[CAPACITY];
	int top = -1;
	
	public boolean isFull() {
		if (top == CAPACITY - 1) return true;
		else return false;
	}
	
	public boolean isEmpty() {
		if(top == -1) return true;
		else return false;
	}
	
	public int peek() {
		return data[top];
	}
	
	public void push(int item) {
		data[++top] = item;
	}

	public int pop() {
		int ret = peek();
		data[top--] = 0;
		return ret;
	}

	@Override
	public String toString() {
		return "MyArrayStack [data=" + Arrays.toString(data) + "]";
	}
	

}
