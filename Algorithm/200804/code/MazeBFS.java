package com.ssafy.day07;

import java.util.LinkedList;
import java.util.Queue;

class Dir{
	int y, x;
	Dir(int y, int x){
		this.y = y; this.x = x;
	}
}

public class MazeBFS {
	static int[][] map;
	static final int N = 8;
	static int[] dy = {0, 0, 1, -1};
	static int[] dx = {1, -1, 0, 0};
	
	static boolean isIn(Dir cur) {
		if(0<= cur.y && cur.y < N && 0<= cur.x && cur.x < N) return true;
		else return false;
	}
	
	static boolean bfs(Dir start) {
		boolean [][] visited = new boolean[N][N];
		Queue<Dir> q = new LinkedList<Dir>();
		q.offer(start);
		visited[start.y][start.x] = true; 
		
		while(!q.isEmpty()) {
			Dir cur = q.poll();
			
			for(int i =0 ; i< 4; i++) {
				Dir next = new Dir(cur.y + dy[i], cur.x + dx[i]);
				if(!isIn(next)) continue;
				if(!visited[next.y][next.x]&& map[next.y][next.x] == 0) {
					visited[next.y][next.x] = true;
					q.offer(next);
				}
			}
		}
		
		return visited[N-1][N-1] == true ? true : false;
	}
	
	public static void main(String[] args) {
		 map = new int[][]{  
			 {0,0,1,1,1,1,1,1},
	         {1,0,0,0,0,0,0,1},
	         {1,1,1,0,1,1,1,1},
	         {1,1,1,0,1,1,1,1},
	         {1,0,0,0,0,0,0,1},
	         {1,0,1,0,1,1,1,1},
	         {1,0,0,0,0,0,0,0},
	         {1,1,1,1,1,1,1,0}
	      };
	      
	      System.out.println(bfs(new Dir(0, 0)));
	}

}
