import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Data de Criação: 05/11/2013
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class Main {

	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

		String v = new String("asdasdasd1, ");

		String vr = v.substring(0, v.length() - 2);

		System.out.println(vr);
	}

	public static String name(String dasdas) {
		return null;
	}
}
