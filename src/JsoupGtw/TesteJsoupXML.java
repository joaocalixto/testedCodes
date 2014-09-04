package JsoupGtw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/**
 * Data de Criação: 14/04/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class TesteJsoupXML {

	public static String vPathXML = "C:\\Users\\jjcc\\Desktop\\teste.xml";

	/**
	 * @param vFileXMl
	 */
	public static void isDocumentXML(String vFileXMl) {
		Pattern pattern = Pattern.compile("\\s*<\\s*\\?xml");
		Matcher matcher = pattern.matcher(vFileXMl);
		boolean vLookingAt = matcher.lookingAt();

		if (vLookingAt) {
			System.out.println("Achou");
		} else {
			System.out.println("Nao Achou");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String vFileXMl = TesteJsoupXML.readFile(TesteJsoupXML.vPathXML);

		TesteJsoupXML.isDocumentXML(vFileXMl);
		Document vDocumentXML = Jsoup.parse(vFileXMl, "", Parser.xmlParser());

		// Elements children =
		// vDocumentXML.getElementsByTag("xml").get(0).children();
		Elements children123 = vDocumentXML.children();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("xml");

		System.out.println(vDocumentXML.toString());

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
		return vRetorno;
	}

}
