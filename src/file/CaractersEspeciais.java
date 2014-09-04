package file;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Data de Criação: 14/05/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class CaractersEspeciais {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		String vPath = "C:\\JoaoCalixto\\workspace\\workspace.gateway2\\Testes\\testeEspeciaisCaracters.txt";
		String conteudoSemAcentos = "sem acentos";
		String conteudoComAcentos = "com acentos joão";

		FileOutputStream vFileOutputStream = null;
		try {
			vFileOutputStream = new FileOutputStream(vPath);
			// CERTO
			vFileOutputStream.write(conteudoComAcentos.getBytes("UTF-8"));
			// ERRADO
			// vFileOutputStream.write(conteudoComAcentos.getBytes());

			System.out.println("Finalizado");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			vFileOutputStream.flush();
		}

	}

}
