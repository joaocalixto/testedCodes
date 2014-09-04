/**
 * Data de Criação: 15/10/2013
 * 
 * @author David José Ribeiro
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class TestesGTW {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println(TestesGTW.validarRenavam("712687050"));

	}

	public static boolean validarRenavam(String renavam) {
		boolean vRetorno = false;
		int soma = 0;

		boolean vEhDigito = true;

		for (int i = 0; i < renavam.length(); i++) {
			if (!Character.isDigit(renavam.charAt(i))) {
				vEhDigito = false;
				break;
			}
		}

		if (vEhDigito) {
			int vDigitoVerificador = Integer.parseInt(renavam.substring(renavam.length() - 1));
			for (int i = 0; i < 8; i++) {
				soma += Integer.parseInt(renavam.substring(i, i + 1)) * (i + 2);
			}
			soma = soma % 11;
			soma = (soma == 10) ? 0 : soma;
			if (soma == vDigitoVerificador) {
				vRetorno = true;
			}
		}

		return vRetorno;
	}

}
