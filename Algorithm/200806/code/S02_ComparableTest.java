package com.ssafy.day09;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 
 * @author taeheekim
 *
 */
public class S02_ComparableTest {

	static class Student implements Comparable<Student> 
	{
		int no, score;

		public Student(int no, int score) {
			super();
			this.no = no;
			this.score = score;
		}

		//@Override
		public int compareTo(Student o) {
			return this.no - o.no;
		}

		@Override
		public String toString() {
			return "Student [no=" + no + ", score=" + score + "]";
		}

	}

	
	static class StudentComparator implements Comparator<Student> {

		@Override
		public int compare(Student o1, Student o2) {
			if(o1.score == o2.score) return (o1.no - o2.no) * 1;
			else return (o1.score - o2.score) * -1;
		}
	}
	
	static class Data1 implements Comparable<Data1>{
		int num;
		Data1(){}
		Data1(int num){
			this.num = num;
		}
		@Override
		public int compareTo(Data1 o) {
			System.out.println("call compareTo");
			
			return this.num - o.num;
		}
	}

	public static void main(String[] args) {
		
		Data1[] a = new Data1[3];
		int index = 0;
		a[index++] = new Data1(3);
		a[index++] = new Data1(10);
		a[index++] = new Data1(7);
		
		Arrays.sort(a);
		for(Data1 d : a) {
			System.out.println(d.num);
		}

		Student[] arr = new Student[] { new Student(1, 10), new Student(3, 50), new Student(2, 80),
				new Student(4, 10) };
		System.out.println("=========정렬 전=============");
		print(arr);
		Arrays.sort(arr);
		System.out.println("=========번호 오름차순=============");
		print(arr);

		Student[] arr2 = new Student[] { new Student(1, 10), new Student(3, 50), new Student(2, 80),
				new Student(4, 10) };
		System.out.println("=========정렬 전=============");
		print(arr2);
		Arrays.sort(arr2, new StudentComparator());
		System.out.println("=========번호 오름차순=============");
		print(arr2);
	}
	private static void print(Student[] arr) {
		for (Student student : arr) {
			System.out.println(student);
		}
	}

}