package validacoes;

/**
 * Data de Criação: 17/04/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class TesteCaractersEspeciais {

	public static boolean isAtributoLetraDigitoEspacoValido(String pAtributoValor, String pAtributoNome, int pSize) {

		boolean isValid = true;

		if ((pAtributoValor == null) || pAtributoValor.equals("")) {
			// || !Utils.isAtributoComprimentoOk(pAtributoValor, pAtributoNome,
			// pSize)) {
			isValid = false;
		} else {
			char[] vCharArray = pAtributoValor.toCharArray();

			for (int i = 0; i < vCharArray.length; i++) {
				if (!Character.isLetterOrDigit(vCharArray[i]) && !Character.isSpaceChar(vCharArray[i])) {
					isValid = false;
				} else {
					String txtRegExp = "[0-9A-Za-z]*";
					if (!pAtributoValor.matches(txtRegExp)) {
						isValid = false;
					}
				}
			}
		}

		return isValid;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		boolean isValid = TesteCaractersEspeciais.isAtributoLetraDigitoEspacoValido("", "asda", 12);

		System.out.println((isValid == true ? "Valido" : "Nao Valido"));

	}
}
