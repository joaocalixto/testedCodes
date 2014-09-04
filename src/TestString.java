import java.net.MalformedURLException;
import java.net.URL;

/**
 * Data de Criação: 12/03/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class TestString {

	private static String getUrl(String pString) {

		String retorno = "";
		try {
			URL vUrl = new URL(pString);
			int vPort = vUrl.getPort();

			if (vPort == -1) {
				vPort = vUrl.getDefaultPort();
			}

			retorno = "\"" + vUrl.getHost() + "\" , " + vPort + " , \"" + vUrl.getProtocol() + "\", 30000,  \""
					+ vUrl.getPath() + "\"";

		} catch (MalformedURLException e1) {
			retorno = "\"" + pString + "\" , 0 , \"\"";
		} catch (Exception e) {
			retorno = "\"" + pString + "\" , 0 , \"\"";
		}
		return retorno;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println(TestString.getUrl("http://www.correios.com.br"));

	}

}
