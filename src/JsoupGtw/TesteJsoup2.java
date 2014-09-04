package JsoupGtw;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

/**
 * Data de Criação: 13/03/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class TesteJsoup2 {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		Connection vConnection = Jsoup.connect("http://www.buscacep.correios.com.br/");

		vConnection.header("User-Agent", "aasdasd");
		vConnection.header("Referer", "http://en.wikipedia.org/wiki/Main_Page");

		Response vExecute = vConnection.method(Method.GET).execute();

		System.out.println(vExecute.body());

	}
}
