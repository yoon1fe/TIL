package com.ssafy.ws.step03;

import java.util.Scanner;

class Moving{
	int dir;
	int moveCnt;
	Moving(int dir, int moveCnt){
		this.dir = dir;
		this.moveCnt = moveCnt;
	}
}

public class Maze {
	static int[] dy = {0, -1, 0, 1, 0};
	static int[] dx = {0, 0, 1, 0, -1};
	static int N = 0;
	static Dir start = null;
	static char[][] map = null;
	static Moving[] moves = null;
	static String output = "";
	
	public static boolean isIn(Dir cur) {
		if(1<= cur.y && cur.y <= N && 1<= cur.x && cur.x <= N) return true;
		else return false;
	}
	
	public static void solve(int idx) {
		Dir ans = start;
		boolean flag = false;
		for(Moving m : moves) {
			Dir next = new Dir(ans.y, ans.x);
			
			for(int i = 0; i<m.moveCnt;i++) {
				next.y += dy[m.dir];
				next.x += dx[m.dir];

				if(!isIn(next) || map[next.y][next.x] == 'J' ) {
					ans.y = 0; ans.x = 0;
					flag = true;
					break;
				}
			}
			if(flag) break;
			ans = next;
		}
		output += "#" + (idx+1) + " " + ans.y + " " + ans.x + "\n";
	}
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int T = sc.nextInt();
		
		for(int i =0 ; i< T; i++) {
			N = sc.nextInt();
			map = new char[N+1][N+1];
			
			int y = sc.nextInt();
			int x = sc.nextInt();
			
			start = new Dir(y, x);
			map[y][x] = 'S';
			int jNum = sc.nextInt();

			for(int j = 0; j<jNum; j++) 
				map[sc.nextInt()][sc.nextInt()] = 'J';
			
			int moveNum = sc.nextInt();
			moves = new Moving[moveNum];
			for(int j =0;j < moveNum;j++)
				moves[j] = new Moving(sc.nextInt(), sc.nextInt());
			
			solve(i);
		}
		System.out.println(output);
	}

}
