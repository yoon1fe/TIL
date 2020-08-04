package com.ssafy.day07;


import java.util.ArrayList;
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
public class G03_AdjListTest2 {

	static int N;
	static ArrayList<Integer>[] adjList;
	static boolean[] visited;
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		N = sc.nextInt(); // 정점수
		int C = sc.nextInt(); // 간선수
		
		adjList = new ArrayList[N];
		for (int i = 0; i < N; i++) {
			adjList[i] = new ArrayList<Integer>();
		}
		visited = new boolean[N];
		for(int i=0; i<C; ++i) {
			int from = sc.nextInt();
			int to = sc.nextInt();
			adjList[from].add(to);
			adjList[to].add(from);
		}
		
		System.out.println("=========dfs============");
		dfs(0);
		System.out.println("=========bfs============");
		bfs();
		
	}
	
	private static void dfs(int current) {
		visited[current] = true;
		System.out.println((char)(current+65));
		
		for(int temp : adjList[current]) {
			if(!visited[temp]) {
				dfs(temp);
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
			
			for(int temp : adjList[current]) {
				if(!visited[temp]) {
					queue.offer(temp);
					visited[temp] = true;
				}
			}
		}
	}
	
}
