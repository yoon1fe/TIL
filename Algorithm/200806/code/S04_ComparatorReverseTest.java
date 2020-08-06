package com.ssafy.day09;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 
 * @author taeheekim
 *
 */
public class S04_ComparatorReverseTest {
	static class Student implements Comparable<Student> {
		int no, score;

		public Student(int no, int score) {
			super();
			this.no = no;
			this.score = score;
		}

		@Override
		public int compareTo(Student o) {
			return this.no - o.no;
		}

		@Override
		public String toString() {
			return "Student [no=" + no + ", score=" + score + "]";
		}

	}

	public static void main(String[] args) {
		Integer[] arr = new Integer[] { 4, 3, 7, 9, 10 };
		System.out.println("정렬 전 : " + Arrays.toString(arr));
		Arrays.sort(arr, Comparator.reverseOrder()); // 해당원소의 Comparable 기능을 이용하여 a.compareTo(b) ==> b.compareTo(a) 형태로
														// 호출
		System.out.println("정렬 후 : " + Arrays.toString(arr));

		String[] location = { "서울", "대전", "광주", "구미" };
		System.out.println("정렬 전 : " + Arrays.toString(location));
		Arrays.sort(location, Comparator.reverseOrder());
		System.out.println("정렬 후 : " + Arrays.toString(location));

		Student[] students = new Student[] { new Student(1, 10), new Student(3, 50), new Student(2, 80),
				new Student(4, 10) };
		System.out.println("=========정렬 전=============");
		for (Student student : students) {
			System.out.println(student);
		}
		Arrays.sort(students, Comparator.reverseOrder());
		System.out.println("=========번호 내림차순=============");
		for (Student student : students) {
			System.out.println(student);
		}
	}
}
