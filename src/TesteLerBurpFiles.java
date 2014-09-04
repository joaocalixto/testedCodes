import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Data de Criação: 19/03/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class TesteLerBurpFiles {

	static String msg = "WindowL";

	public static boolean existeMsg(String pConteudo) {
		return pConteudo.contains(TesteLerBurpFiles.msg);

	}

	public static void listFilesForFolder(final File folder) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				TesteLerBurpFiles.listFilesForFolder(fileEntry);
			} else {
				TesteLerBurpFiles.readFile(fileEntry.getAbsolutePath());
				// System.out.println(fileEntry.getName());
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final File folder = new File("C:\\Users\\jjcc\\Desktop\\burpsuite_v1.4.01.src");
		TesteLerBurpFiles.listFilesForFolder(folder);

	}

	public static String readFile(String pFile) {
		BufferedReader br = null;
		String vRetorno = "";

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(pFile));

			while ((sCurrentLine = br.readLine()) != null) {
				vRetorno += sCurrentLine + "\n";
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		if (TesteLerBurpFiles.existeMsg(vRetorno)) {
			System.out.println("Achou");
			System.out.println("Arquivo = " + pFile);
		}
		return vRetorno;
	}

}
