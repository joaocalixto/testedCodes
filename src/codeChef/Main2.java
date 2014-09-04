package codeChef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main2 {

	public static void main(String[] args) throws NumberFormatException, IOException {

		int qtd = 0;
		int div = 0;
		int inc = 0;

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String comand[] = input.readLine().split(" ");

		qtd = Integer.parseInt(comand[0]);
		div = Integer.parseInt(comand[1]);

		for (int vI = 0; vI < qtd; vI++) {
			if ((Integer.parseInt(input.readLine()) % div) == 0) {
				++inc;
			}

		}

		System.out.println(inc);
	}
}
