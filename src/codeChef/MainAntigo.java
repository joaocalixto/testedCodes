package codeChef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainAntigo {

	public static void main(String[] args) throws NumberFormatException, IOException {

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		int qtd = Integer.parseInt(input.readLine());

		for (int vI = 0; vI < qtd; vI++) {
			Integer number = Integer.parseInt(input.readLine());
			int result = 1;
			for (int vI2 = 1; vI2 <= number; vI2++) {
				result *= vI2;
			}
			System.out.println(result);
		}

	}
}
