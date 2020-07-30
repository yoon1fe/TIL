package com.ssafy.day03;

import java.util.Scanner;
import java.util.Stack;

public class S2_SubSetTest {

	static boolean[] isSelected;
	static int N, S, totalCnt = 0;
	static int[] input;
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		Stack<String> stack = new Stack<String>();
		stack.push("ÀÌµ¿¿í");
		stack.push("ÀÌÁö¤¿");
		stack.push("±èÅÂÈñ");

		System.out.println(stack.size() + "//" + stack.isEmpty());
		System.out.println(stack.pop());
		System.out.println(stack.size());
		System.out.println(stack.peek());
		System.out.println(stack.size());
	}
	
 
}
