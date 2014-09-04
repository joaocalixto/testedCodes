import java.io.IOException;

import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Data de Criação: 14/02/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class EngineHttps {
	public static void main(String[] args) throws IOException {

		EngineHttps.spiderSVN();
	}

	/**
	 * @param args
	 * @return
	 * @throws IOException
	 */

	static void spiderSVN() {

		try {
			Document vParse = Jsoup.connect("http://neuroad/svn/Production/trunk/TI/Gateway2/Software/gateway2-sites")
					.method(Method.GET).execute().parse();
			Elements vElementsByTag = vParse.getElementsByTag("a");

			for (Element vElement : vElementsByTag) {
				String vText = vElement.text();
				if (vText.contains("gateway2-sites-") && !vText.equalsIgnoreCase("gateway2-sites/")) {

					if (vText.contains("gateway2-sites-crc") || vText.contains("gateway2-sites-fak")) {
						continue;
					}

					Document vParse2 = Jsoup
							.connect(
									"http://neuroad/svn/Production/trunk/TI/Gateway2/Software/gateway2-sites/" + vText
											+ "/src/main/java/br/com/neurotech/gateway2/sites/").method(Method.GET)
							.execute().parse();

					Elements vElementsByTag2 = vParse2.getElementsByTag("a");

					for (Element vElement2 : vElementsByTag2) {
						String vText2 = vElement2.text();
						if (!vText2.equalsIgnoreCase("..") && !vText2.equalsIgnoreCase("Subversion")) {
							Document vParse3 = Jsoup
									.connect(
											"http://neuroad/svn/Production/trunk/TI/Gateway2/Software/gateway2-sites/"
													+ vText + "/src/main/java/br/com/neurotech/gateway2/sites/"
													+ vText2 + "server/").method(Method.GET).execute().parse();
							Elements vElementsByTag3 = vParse3.getElementsByTag("a");

							for (Element vElement3 : vElementsByTag3) {
								String vText3 = vElement3.text();
								if (!vText3.equalsIgnoreCase("..") && !vText3.equalsIgnoreCase("Subversion")) {
									Document vParse4 = Jsoup
											.connect(
													"http://neuroad/svn/Production/trunk/TI/Gateway2/Software/gateway2-sites/"
															+ vText + "/src/main/java/br/com/neurotech/gateway2/sites/"
															+ vText2 + "server/" + vText3).method(Method.GET).execute()
											.parse();

									String vText4 = vParse4.text();
									if (vText4.contains("https")) {
										System.out.println("Site = " + vText);
									}

								}
							}
						}
					}
					String vText2 = vParse2.text();
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
