package com.ssafy.day06;

import java.util.Arrays;

/**
 * 
 * @author taeheekim
 *
 */
public class CompleteBinaryTreeTest {

	public static void main(String[] args) {

		int size = 10;
		CompleteBinaryTree tree = new CompleteBinaryTree(size);
		
		for(int i=0; i<size; ++i){
			tree.add((char)(65+i));
		}
		System.out.println(Arrays.toString(tree.nodes));
//		tree.bfs();
//		tree.bfs2();
//		tree.printTreeByPreOrder();
//		tree.printTreeByInOrder();
//		tree.printTreeByPostOrder();
//		
	}

}
