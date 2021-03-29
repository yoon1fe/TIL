package com.ssafy.ws.step05;

import java.util.Scanner;

public class MovieTest {
	private Scanner sc = new Scanner(System.in);
	private MovieMgr mgr = MovieMgr.getInstance();
	
	private void service() {
		System.out.println("<<영화 관리 프로그램>>");
		while(true) {			
			switch(getMenu()) {
			case 1:
				addMovie();
				break;
			case 2:
				showMovies();
				break;
			case 3:
				System.out.print("제목을 입력하세요. ");
				String searchTitle = sc.nextLine();
				showMovies(searchTitle);
				break;
			case 4:
				System.out.print("장르를 입력하세요. ");
				String searchDirector = sc.nextLine();
				showMoviesByDirector(searchDirector);
				break;
			case 5:
				System.out.print("장르를 입력하세요. ");
				String searchGenre = sc.nextLine();
				showMoviesByGenre(searchGenre);
				break;
			case 6:
				System.out.print("삭제할 영화를 입력하세요. ");
				String deleteTitle = sc.nextLine();
				deleteMovie(deleteTitle);
				break;
			case 0:
				System.exit(0);
			default:
				System.out.println("올바른 번호를 선택하세요.\n");
			}
		}
	}
	

	private int getMenu() {
		System.out.println("1. 영화 정보 입력");
		System.out.println("2. 영화 정보 전체 검색");
		System.out.println("3. 영화명 검색");
		System.out.println("4. 감독 검색");
		System.out.println("5. 영화명 장르별 검색");
		System.out.println("6. 영화 정보 삭제");
		System.out.println("0. 종료");
		
		return scanInt("원하는 번호를 선택하세요. ");
	}
	
	private String scanString(String msg) {
		System.out.print(msg);
		return sc.nextLine();
	}
	
	private int scanInt(String msg) {
		return Integer.parseInt(scanString(msg));
	}
	
	private void addMovie() {
		System.out.println("1. 영화 정보 입력");
		String title = scanString("제목: ");
		String director = scanString("감독: ");
		int grade = scanInt("관람 등급: ");
		String genre = scanString("장르: ");				
		String summary = scanString("줄거리: ");
		
		mgr.add(new Movie(title, director, grade, genre, summary));
		System.out.println("영화 정보가 등록되었습니다.\n");
	}
	
	private void showMovies() {
		System.out.println("---------------");
		System.out.println("영화 정보");
		System.out.println("---------------");
		Movie[] movies = mgr.search();
		System.out.println("제목\t감독\t장르");
		for(Movie m : movies) 
			System.out.println(m.getTitle() + "\t" + m.getDirector() + "\t" + m.getGenre());

		System.out.println("---------------");
	}

	private void showMovies(String searchTitle) {
		System.out.println("---------------");
		System.out.println("영화 정보");
		System.out.println("---------------");
		Movie[] movies = mgr.search(searchTitle);
		System.out.println("제목\t감독\t장르");
		for(Movie m : movies) 
			System.out.println(m.getTitle() + "\t" + m.getDirector() + "\t" + m.getGenre());

		System.out.println("---------------");
	}
	
	private void showMoviesByDirector(String searchDirector) {
		System.out.println("---------------");
		System.out.println("영화 정보");
		System.out.println("---------------");
		Movie m = mgr.searchDirector(searchDirector);
		System.out.println("제목\t감독\t장르");
		if(m != null)
			System.out.println(m.getTitle() + "\t" + m.getDirector() + "\t" + m.getGenre());
		
		System.out.println("---------------");
	}
	
	private void showMoviesByGenre(String searchGenre) {
		System.out.println("---------------");
		System.out.println("영화 정보");
		System.out.println("---------------");
		Movie[] movies = mgr.searchGenre(searchGenre);
		System.out.println("제목\t감독\t장르");
		for(Movie m : movies) 
			System.out.println(m.getTitle() + "\t" + m.getDirector() + "\t" + m.getGenre());

		System.out.println("---------------");
	}
	
	private void deleteMovie(String title) {
		if(mgr.delete(title))
			System.out.println("삭제하였습니다.");
		else System.out.println("삭제할 데이터가 없습니다.");
	}
	
	public static void main(String[] args) {
		MovieTest mt = new MovieTest();
		
		mt.service();
	}

}
