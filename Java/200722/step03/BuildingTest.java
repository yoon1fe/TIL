package com.ssafy.ws.step03;

import java.util.Scanner;

class Dir {
	int y;
	int x;

	Dir(int y, int x) {
		this.y = y;
		this.x = x;
	}
}

public class BuildingTest {
	static char[][] map = null;
	static int N = 0;

	static boolean isIn(Dir cur) {
		if (0 <= cur.y && cur.y < N && 0 <= cur.x && cur.x < N)
			return true;
		else
			return false;
	}

	static boolean checkPark(Dir cur) { // true: G 없음 false: G 있음
		int[] dy = { 1, 1, 1, -1, -1, -1, 0, 0 };
		int[] dx = { -1, 0, 1, -1, 0, 1, -1, 1 };
		for (int i = 0; i < 8; i++) {
			Dir next = new Dir(cur.y + dy[i], cur.x + dx[i]);
			if (isIn(next)) {
				if (map[next.y][next.x] == 'G')
					return false;
			}
		}
		return true;
	}

	static int solve(Dir cur) {
		int[] dy = { 1, -1, 0, 0 };
		int[] dx = { 0, 0, 1, -1 };

		int cnt = 1;

		for (int i = 0; i < 4; i++) {
			Dir next = new Dir(cur.y + dy[i], cur.x + dx[i]);
			while (isIn(next)) {
				if (map[next.y][next.x] == 'B')
					cnt++;
				next.y += dy[i];
				next.x += dx[i];
			}
		}

		return cnt;
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int T = sc.nextInt();
		int[] answers = new int[T];

		for (int k = 0; k < T; k++) {
			N = sc.nextInt();
			map = new char[N][N];
			int ans = 2;
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					map[i][j] = sc.next().charAt(0);
				}
			}

			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					if (map[i][j] == 'G')
						continue;
					Dir cur = new Dir(i, j);
					if (checkPark(cur)) {
						ans = ans > solve(cur) ? ans : solve(cur);
					}
				}
			}

			answers[k] = ans;
		}

		for (int i = 0; i < T; i++) 
			System.out.println("#" + (i + 1) + " " + answers[i]);
		

	}

}
