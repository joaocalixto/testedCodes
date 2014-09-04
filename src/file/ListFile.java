package file;

import java.io.File;
import java.io.FileFilter;

/**
 * Data de Criação: 29/08/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class ListFile {

	public static void main(String[] args) {
		File vFile = new File(
				"C:\\JoaoCalixto\\workspace\\workspace.gateway2\\gateway_site_SPCBR_v20140827\\src\\main\\java\\br\\com\\neurotech\\gateway2\\sites\\spcbr\\builder\\blocos");

		File[] vListFiles = vFile.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pPathname) {

				if (pPathname.getName().endsWith(".java")) {
					return true;
				}
				return false;

			}
		});

		System.out.println("Total = " + vListFiles.length);
		for (int vI = 0; vI < vListFiles.length; vI++) {
			System.out.println("new " + vListFiles[vI].getName().replace(".java", "") + "(),");
		}

	}

}
