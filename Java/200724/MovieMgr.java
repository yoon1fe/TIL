package com.ssafy.ws.step05;

import java.util.Arrays;

public class MovieMgr {
	public static MovieMgr getInstance() {
		if (instance == null)
			instance = new MovieMgr();
		return instance;
	}

	private Movie[] movies = new Movie[100];
	// 데이터가 들어갈 위치와 입력된 데이터의 크기
	private int index = 0;

	static private MovieMgr instance;

	private MovieMgr() {
	}

	public void add(Movie m) {
		if (index == movies.length) {
			movies = Arrays.copyOf(movies, index * 2);
		}
		movies[index++] = m;
	}

	public Movie[] search() {
		Movie[] movies = Arrays.copyOf(this.movies, index);
		return movies;
	}

	public Movie[] search(String title) {
		int cnt = 0;
		Movie[] ret = new Movie[index];
		for (int i = 0; i < index; i++) {
			if (movies[i].getTitle().contains(title)) {
				ret[cnt++] = movies[i];
			}
		}

		return Arrays.copyOf(ret, cnt);
	}

	public Movie searchDirector(String name) {
		int cnt = 0;
		for (int i = 0; i < index; i++) {
			if (movies[i].getDirector().equals(name)) {
				return movies[i];
			}
		}
		return null;
	}

	public Movie[] searchGenre(String genre) {
		int cnt = 0;
		Movie[] ret = new Movie[index];
		for (int i = 0; i < index; i++) {
			if (movies[i].getGenre().equals(genre)) {
				ret[cnt++] = movies[i];
			}
		}

		return Arrays.copyOf(ret, cnt);
	}

	public boolean delete(String title) {
		boolean ret = false;
		for (int i = 0; i < index; i++) {
			if (movies[i].getTitle().equals(title)) {
				ret = true;
//				for(int j = i+1; j< index; j++) 
//					movies[j - 1] = movies[j];
				int moveCnt = index - (i + 1);
				if (moveCnt != 0) {
					System.arraycopy(movies, i + 1, movies, i, moveCnt);
				}
				movies[--index] = null;
				break;
			}

		}

		return ret;
	}

	public int getSize() {
		return index;
	}

}
