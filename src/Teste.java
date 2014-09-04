/**
 * Data de Criação: 03/07/2013
 * 
 * @author jjcc
 * @since
 * @version XXX_vYYYYMMa
 */
public class Teste {

	public static void main(String[] args) throws InterruptedException {

		for (int vI = 11; vI < 21; vI++) {
			String vString = new String();
			vString += " || t" + vI + ".isAlive()";
			System.out.print(vString);
		}

	}
}
