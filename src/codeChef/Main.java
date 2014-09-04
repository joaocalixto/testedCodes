package codeChef;

import java.io.BufferedReader;
import java.io.IOException;

public class Main {

	private static int[] helper;
	private static int[] numbers;

	private static int number;

	public static void main(String[] args) throws NumberFormatException, IOException {

		BufferedReader r = new BufferedReader(new java.io.InputStreamReader(System.in));
		int n = Integer.parseInt(r.readLine().trim());
		int a[] = new int[n];
		for (int i = 0; i < n; i++) {
			a[i] = Integer.parseInt(r.readLine().trim());
		}
		long startTime = System.currentTimeMillis();
		Main.numbers = a;
		Main.number = a.length;
		Main.helper = new int[Main.number];
		Main.mergesort(0, Main.number - 1);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println(elapsedTime);

	}

	private static void merge(int low, int middle, int high) {

		// Copy both parts into the helper array
		for (int i = low; i <= high; i++) {
			Main.helper[i] = Main.numbers[i];
		}

		int i = low;
		int j = middle + 1;
		int k = low;
		// Copy the smallest values from either the left or the right side back
		// to the original array
		while ((i <= middle) && (j <= high)) {
			if (Main.helper[i] <= Main.helper[j]) {
				Main.numbers[k] = Main.helper[i];
				i++;
			} else {
				Main.numbers[k] = Main.helper[j];
				j++;
			}
			k++;
		}
		// Copy the rest of the left side of the array into the target array
		while (i <= middle) {
			Main.numbers[k] = Main.helper[i];
			k++;
			i++;
		}

	}

	private static void mergesort(int low, int high) {
		// check if low is smaller then high, if not then the array is sorted
		if (low < high) {
			// Get the index of the element which is in the middle
			int middle = low + ((high - low) / 2);
			// Sort the left side of the array
			Main.mergesort(low, middle);
			// Sort the right side of the array
			Main.mergesort(middle + 1, high);
			// Combine them both
			Main.merge(low, middle, high);
		}
	}

}
