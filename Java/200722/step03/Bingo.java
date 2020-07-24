package com.ssafy.ws.step03;

import java.util.Scanner;

public class Bingo {
	static boolean[][] bingoCheck = new boolean[5][5];
	static int[][] bingo = new int[5][5];

	public static int checkLine(Dir cur) {
		int cnt = 0;
		if (cur.y == cur.x) {
			boolean makeLine = true;
			for (int i = 0; i < 5; i++) {
				if (bingo[i][i] != 0) {
					makeLine = false;
					break;
				}
			}
			if (makeLine)
				cnt++;

		} else if (cur.y + cur.x == 4) {
			boolean makeLine = true;
			for (int i = 0; i < 5; i++) {
				if (bingo[i][4 - i] != 0) {
					makeLine = false;
					break;
				}
			}
			if (makeLine)
				cnt++;
		}

		boolean makeLine = true;
		for (int i = 0; i < 5; i++) {
			if (bingo[cur.y][i] != 0) {
				makeLine = false;
				break;
			}
		}

		if (makeLine)
			cnt++;
		makeLine = true;
		for (int i = 0; i < 5; i++) {
			if (bingo[i][cur.x] != 0) {
				makeLine = false;
				break;
			}
		}
		if (makeLine)
			cnt++;

		return cnt;
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		boolean[] check = new boolean[26];

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				int n = (int) (Math.random() * 25) + 1;
				if (check[n]) {
					j--;
					continue;
				}
				bingo[i][j] = n;
				check[n] = true;
			}

		}

		int comCnt = 0;
		int userCnt = 0;
		boolean userTurn = true;
		check = new boolean[26]; // 컴퓨터 난수 생성

		while (comCnt != 5 && userCnt != 5) {
			System.out.println("사용자 빙고 번호");
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 5; j++) {
					System.out.print(bingo[i][j] + "\t");
				}
				System.out.println();
			}

			if (userTurn) {
				System.out.print("빙고 번호를 선택하세요 : ");
				int n = sc.nextInt();
				check[n] = true;
				boolean flag = false;
				Dir cur = null;
				for (int i = 0; i < 5; i++) {
					for (int j = 0; j < 5; j++) {
						if (bingo[i][j] == n) {
							bingo[i][j] = 0;
							cur = new Dir(i, j);
							flag = true;
							break;
						}
					}
					if (flag)
						break;
				}

				userCnt += checkLine(cur);

				userTurn = false;
			} else {
				int n = (int) (Math.random() * 25) + 1;
				while (check[n]) {
					n = (int) (Math.random() * 25) + 1;
				}
				check[n] = true;
				System.out.println("컴퓨터가 " + n + "번을 선택했습니다.");
				boolean flag = false;
				Dir cur = null;
				for (int i = 0; i < 5; i++) {
					for (int j = 0; j < 5; j++) {
						if (bingo[i][j] == n) {
							bingo[i][j] = 0;
							cur = new Dir(i, j);
							flag = true;
							break;
						}
					}
					if (flag)
						break;
				}

				comCnt += checkLine(cur);

				userTurn = true;
			}
			System.out.println("사용자 빙고 수: " + userCnt);
			System.out.println("컴퓨터 빙고 수: " + comCnt);
		}

		String output = "";
		output += userCnt >= 5 ? "사용자 승!" : "컴퓨터 승!";

		System.out.println(output);

	}
}
