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
public class G01_AdjMatrixTest {

	static int N;
	static boolean adjMatrix[][];
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		N = sc.nextInt(); // 정점수
		int C = sc.nextInt(); // 간선수
		
		adjMatrix = new boolean[N][N];
		visited = new boolean[N];
		for(int i=0; i<C; ++i) {
			int from = sc.nextInt();
			int to = sc.nextInt();
			adjMatrix[to][from] = adjMatrix[from][to] = true;
		}
		System.out.println("=========dfs============");
		dfs(0);
		System.out.println("=========bfs============");
		bfs();
		
	}
	
	private static boolean[] visited;
	private static void dfs(int current) {
		
		visited[current] = true;
		System.out.println((char)(current+65));
		
		for(int i=0; i<N; ++i) {
			if(adjMatrix[current][i] 
					&& !visited[i]) {
				dfs(i);
			}
		}
	}
	
	// 나와서 방문 처리
	private static void bfs() {
		Queue<Integer>  queue = new LinkedList<Integer>();
		boolean visited[] = new boolean[N];
		
		int current = 0;
		queue.offer(current);
//		visited[current] = true; 	// 방법2 : 들어갈때 방문 처리
		
		while(!queue.isEmpty()) {
			current = queue.poll();
			visited[current] = true; 	// 방법1 : 나올 때 방문 처리
			System.out.println((char)(current+65));
			for(int i=0; i<N; ++i) {
				if(adjMatrix[current][i] 
						&& !visited[i]) {
					queue.offer(i);
//					visited[i] = true; 	// 방법2 : 들어갈때 방문 처리
				}
			}
		}
	}


}