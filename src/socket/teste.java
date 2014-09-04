package socket;

/**
 * Data de Criação: 06/03/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class teste {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Provider server = new Provider();
		while (true) {
			server.run();
		}

	}

}
