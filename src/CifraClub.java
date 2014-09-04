import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Data de Criação: 03/07/2013
 * 
 * @author David José Ribeiro
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class CifraClub {

	public static void main(String[] args) throws IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet("http://www.cifraclub.com.br");
		HttpResponse response = client.execute(request);

		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuilder textView = new StringBuilder();

		String line = "";
		while ((line = rd.readLine()) != null) {
			System.out.println(line);
			// textView.append(line);
		}

	}
}
