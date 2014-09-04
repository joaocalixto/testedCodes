package codeChef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainAtm {

	public static void main(String[] args) throws NumberFormatException, IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String comand[] = input.readLine().split(" ");

		int withdraw = Integer.parseInt(comand[0]);
		double balance = Double.parseDouble(comand[1]);

		if ((balance < (withdraw + 0.5)) || ((withdraw % 5) != 0)) {
			System.out.println(balance);
		} else {
			System.out.println(balance - withdraw - 0.5);
		}
		input.close();

	}
}
