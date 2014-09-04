package string;

/**
 * Data de Criação: 02/09/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class ParserInteger {

	public static void main(String[] args) {
		String v = "8006430020";

		String vValorInformado = v;
		String vValorCep = String.valueOf(Integer.parseInt(vValorInformado));

		System.out.println(vValorCep);
	}
}
