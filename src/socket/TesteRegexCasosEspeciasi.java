package socket;

/**
 * Data de Criação: 19/03/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class TesteRegexCasosEspeciasi {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String vString = "234é";

		TesteRegexCasosEspeciasi.validarCampoCaractersEspeciais(vString);

	}

	public static void validarCampoCaractersEspeciais(String pInputNome) {
		String txtRegExp = "[0-9A-Za-z]*";

		if (!pInputNome.matches(txtRegExp)) {
			System.out.println("Idetificador Invalido");
		} else {
			System.out.println("Indentificador Valido");
		}

	}

}
