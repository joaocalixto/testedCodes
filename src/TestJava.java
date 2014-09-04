import java.io.IOException;

public class TestJava {

	public static void main(String[] args) throws NumberFormatException, IOException {

		String teste = "spc-score-12-meses";
		String numero = "12";

		if (!numero.matches("\\d*")) {
			System.out.println("Novo Jeito");
		} else {
			System.out.println("Jeito Velho");
		}

	}
}
