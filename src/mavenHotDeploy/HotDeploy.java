package mavenHotDeploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Data de Criação: 26/08/2013
 * 
 * @author David José Ribeiro
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class HotDeploy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Process p;
		File vPathProjeto = new File("C:\\JoaoCalixto\\workspace\\workspace.gateway2\\gateway2-sites-oab");
		try {
			String command[] = new String[2];
			command[0] = "cmd /c mvn -version";
			command[1] = "";
			File dir = new File("C:\\JoaoCalixto\\workspace\\workspace.gateway2\\gateway2-sites-oab");
			Runtime run = Runtime.getRuntime();
			Process proc = run.exec(command, null, dir);
			proc.waitFor();
			InputStream is = proc.getInputStream();
			InputStreamReader isreader = new InputStreamReader(is);
			BufferedReader input = new BufferedReader(isreader);

			List linhas = new ArrayList();
			String linha = "";
			while ((linha = input.readLine()) != null) {
				linhas.add(linha);
			}
			input.close();

			for (Object vObject : linhas) {
				System.out.println(vObject.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
