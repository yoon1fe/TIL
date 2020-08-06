package com.ssafy.day09;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 
 * @author taeheekim
 *
 */
public class S01_SortTest {

	public static void main(String[] args) {
	      int[] arr = new int[] {4,3,7,9,10};
	      System.out.println("정렬 전 : "+Arrays.toString(arr));
	      Arrays.sort(arr);
	      System.out.println("정렬 후 : "+Arrays.toString(arr));
	      
	      String[] location = {"서울","대전","광주","구미"};
	      System.out.println("정렬 전 : "+Arrays.toString(location));
	      Arrays.sort(location);
	      System.out.println("정렬 후 : "+Arrays.toString(location));
	}
}
