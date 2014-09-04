import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;

/**
 * Data de Criação: 03/07/2013
 * 
 * @author David José Ribeiro
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class CopyOfCorreiosBuscaCep {

	public static String getIdUsuario(String html) {

		String aux = "";
		if (html.contains("\"IdUsuario\" value=\"")) {

			String ini = "\"IdUsuario\" value=\"";
			int indexIni = html.indexOf("\"IdUsuario\" value=\"");
			indexIni = indexIni + ini.length();
			int indexEnd = html.indexOf("\"  />", indexIni);

			aux = html.substring(indexIni, indexEnd);

		}

		if (html.contains("número </b></td><td>")) {
			String ini = "número </b></td><td>";
			int indexIni = html.indexOf("número </b></td><td>");
			indexIni = indexIni + ini.length();
			int indexEnd = html.indexOf("</td>", indexIni);

			aux = html.substring(indexIni, indexEnd);
		}

		return aux;
	}

	public static void keepAlive() {

		HttpClient client5 = new DefaultHttpClient();
		HttpGet get5 = new HttpGet("http://priples.com/Escritorio/Login/funcoes/seguranca/autenticacao.continua.php");
		HttpResponse response5;
		try {
			response5 = client5.execute(get5);
			EntityUtils.consume(response5.getEntity());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws ClientProtocolException, IOException {

		StringBuilder textView = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		HttpGet get = new HttpGet("http://priples.com/Escritorio/Login/index.php");
		response = client.execute(get);
		EntityUtils.consume(response.getEntity());

		HttpPost post = new HttpPost("http://priples.com/Escritorio/Login/funcoes/seguranca/logar.php");
		post.addHeader("Referer", "http://priples.com/Escritorio/Login/index.php");
		post.addHeader("Accept-Language", "pt-BR");
		post.addHeader("Host", "priples.com");
		post.addHeader("DNT", "1");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("Email", new String(EncodingUtils
				.getAsciiBytes("alealmeida_pe@hotmail.com"))));
		nameValuePairs.add(new BasicNameValuePair("Senha", "Zion2010"));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		response = client.execute(post);

		EntityUtils.consume(response.getEntity());
		HttpGet get2 = new HttpGet("http://priples.com/Escritorio/Login/redirecionar-usuarios.php");
		response = client.execute(get2);

		EntityUtils.consume(response.getEntity());
		HttpGet get3 = new HttpGet("http://priples.com/Escritorio/Login/escritorio/home/index.php");
		HttpResponse response4 = client.execute(get3);

		// EntityUtils.consume(response4.getEntity());
		String html = EntityUtils.toString(response4.getEntity());
		String idUsuario = CopyOfCorreiosBuscaCep.getIdUsuario(html);

		// Chamando div de perguntas
		HttpGet get4 = new HttpGet("http://priples.com/Escritorio/Login/escritorio/atividades/perguntas.php");
		response = client.execute(get4);
		EntityUtils.consume(response.getEntity());

		HttpPost post2 = new HttpPost(
				"http://priples.com/Escritorio/Login/escritorio/atividades/programacao/perguntas.php");
		List<NameValuePair> nameValuePairs2 = new ArrayList<NameValuePair>();
		nameValuePairs2.add(new BasicNameValuePair("Pergunta1", "Qual a maior biblioteca do mundo?"));
		nameValuePairs2.add(new BasicNameValuePair("Pergunta2", "Qual o maior estádio de futebol do mundo?"));
		nameValuePairs2.add(new BasicNameValuePair("Pergunta3", "Qual é o maior pais do mundo?"));
		nameValuePairs2.add(new BasicNameValuePair("Pergunta4", "Qual é o maior planeta do sistema solar?"));
		nameValuePairs2.add(new BasicNameValuePair("Pergunta5", "Qual é o menor planeta do sistema solar?"));
		nameValuePairs2.add(new BasicNameValuePair("idUsuario", idUsuario));

		post2.setEntity(new UrlEncodedFormEntity(nameValuePairs2));
		response = client.execute(post2);

		System.out.println(EntityUtils.toString(response.getEntity()));

	}

	public static String responseToString(HttpResponse e) {

		// EntityUtils.toString(response2.getEntity())
		BufferedReader rd;
		String line = "";
		StringBuilder textView = new StringBuilder();
		try {
			rd = new BufferedReader(new InputStreamReader(e.getEntity().getContent()));
			while ((line = rd.readLine()) != null) {
				textView.append(line);
			}
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return textView.toString();
	}

}
