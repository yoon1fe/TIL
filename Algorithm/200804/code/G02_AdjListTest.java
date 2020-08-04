package com.ssafy.day07;


import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
/*	
7
8
0 1
0 2	
1 3
1 4
2 4
3 5
4 5
5 6	
*/
/**
 * 
 * @author taeheekim
 *
 */
public class G02_AdjListTest {

	static class Node{
		int vertex;
		Node next;
		public Node(int vertex, Node next) {
			this.vertex = vertex;
			this.next = next;
		}
		public Node(int vertex) {
			this.vertex = vertex;
		}
		@Override
		public String toString() {
			return "Node [vertex=" + vertex + ", next=" + next + "]";
		}
		
	}
	
	static int N;
	static Node[] adjList;
	static boolean[] visited;
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		N = sc.nextInt(); // 정점수
		int C = sc.nextInt(); // 간선수
		
		adjList = new Node[N];
		visited = new boolean[N];
		for(int i=0; i<C; ++i) {
			int from = sc.nextInt();
			int to = sc.nextInt();
			adjList[from] = new Node(to,adjList[from]);
			adjList[to] = new Node(from,adjList[to]);
		}
		System.out.println("=========dfs============");
		dfs(0);
		System.out.println("=========bfs============");
		bfs();
		
	}
	
	private static void dfs(int current) {
		visited[current] = true;
		System.out.println((char)(current+65));
		
		for(Node temp = adjList[current]; temp != null; temp = temp.next) {
			if(!visited[temp.vertex]) {
				dfs(temp.vertex);
			}
		}
	
	}
	
	private static void bfs() {
		Queue<Integer>  queue = new LinkedList<Integer>();
		boolean visited[] = new boolean[N];
		
		int current = 0;
		queue.offer(current);
		visited[current] = true;
		
		while(!queue.isEmpty()) {
			current = queue.poll();
			System.out.println((char)(current+65));
			
			for(Node temp = adjList[current]; temp != null; temp = temp.next) {
				if(!visited[temp.vertex]) {
					queue.offer(temp.vertex);
					visited[temp.vertex] = true;
				}
			}
			
		}
		
	}
	
}
