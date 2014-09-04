package regex;

/**
 * Data de Criação: 27/08/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class StringRegex {

	/**
	 * @param args
	 */
	public static void main(StringRegex[] args) {
		String teste = "teste";
		String numero = "12";

		if (teste.matches("//d")) {
			System.out.println("Verdade");
		} else {
			System.out.println("Falso");
		}
	}

}
