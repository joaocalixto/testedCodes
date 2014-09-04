package swing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class JSoapHtml {

	static DefaultMutableTreeNode listasTodosElementosHtml(Elements pElements, DefaultMutableTreeNode root) {

		for (Element element : pElements) {

			String attr = "";
			Attributes attributes = element.attributes();
			if (!attributes.toString().isEmpty()) {
				attr = " " + attributes.toString() + " ";
			}
			String vFullTag = "<" + element.tagName() + attr + ">";

			DefaultMutableTreeNode tag = new DefaultMutableTreeNode(vFullTag);
			root.add(tag);
			System.out.println("<" + element.tagName() + attr + ">");

			if (vFullTag.equals("<span  class=\"dica\" >")) {
				System.out.println("script");
				element.textNodes();
			}

			List<TextNode> vTextNodes = element.textNodes();

			for (TextNode vTextNode : vTextNodes) {
				if (!vTextNode.text().trim().isEmpty()) {
					tag.add(new DefaultMutableTreeNode(vTextNode.text()));
				}
			}
			if (element.children().isEmpty()) {

				if (!element.data().isEmpty()) {
					tag.add(new DefaultMutableTreeNode(element.data()));
				}
				// if (!element.text().isEmpty()) {
				// tag.add(new DefaultMutableTreeNode(element.text()));
				// }
			}

			JSoapHtml.listasTodosElementosHtml(element.children(), tag);
		}
		return root;
	}

	public static void main(String[] args) throws IOException {

		File input = new File("C:\\Users\\jjcc\\Desktop\\correiosTeste.html");

		Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

		Elements children = doc.getElementsByTag("html").get(0).children();

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Html");
		// JSoapHtml.listasTodosElementosHtml(children, root);

		// [Html/ <head>/ <title>/ Correios]
		Elements vSelect = doc.select("Html > head > title");

		Document vParse = Jsoup
				.parse("<table> <tr bgcolor=\"#ECF3F6\" onclick=\"javascript:detalharCep('1','2');\" style=\"cursor: pointer;\">");

		Document vParseBodyFragment = Jsoup
				.parseBodyFragment("<tr bgcolor=\"#ECF3F6\" onclick=\"javascript:detalharCep('1','2');\" style=\"cursor: pointer;\">");
		Element vChild = vParseBodyFragment.getElementsByTag("body").get(0).children().get(0);

		vChild.tagName();

		@SuppressWarnings("unused")
		Elements vSelect2 = doc.select("div[class=back] > div[class=wrap]");

		System.out.println("");

	}
}
