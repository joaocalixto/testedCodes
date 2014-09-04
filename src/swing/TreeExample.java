package swing;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TreeExample extends JFrame {
	private JTree tree;
	private JTextField jtf;
	private JTextField output;
	private Document doc;
	private JComboBox extracaoList;

	public TreeExample() throws IOException {

		File input = new File("C:\\Users\\jjcc\\Desktop\\correiosTeste.html");

		this.doc = Jsoup.parse(input, "UTF-8", "http://example.com/");

		Elements children = this.doc.getElementsByTag("html").get(0).children();

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Html");
		JSoapHtml.listasTodosElementosHtml(children, root);

		// create the tree by passing in the root node
		this.tree = new JTree(root);
		String[] extracoesStrings = { "text", "toString", "data", "val" };
		this.extracaoList = new JComboBox(extracoesStrings);

		this.tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				TreeExample.this.doMouseClicked(me);
			}
		});

		this.setLayout(new BorderLayout());

		this.jtf = new JTextField("...path");
		this.output = new JTextField("...output");

		this.add(this.tree, BorderLayout.CENTER);
		this.add(this.jtf, BorderLayout.SOUTH);
		this.add(this.output, BorderLayout.EAST);
		this.add(this.extracaoList, BorderLayout.WEST);
		this.add(new JScrollPane(this.tree));

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("JTree Example");
		this.pack();
		this.setVisible(true);
	}

	/**
	 * @param pPathFormatado
	 * @param pChild
	 */
	private Element checkTableChild(String pPathFormatado, Element pChild) {

		Element vRetorno = null;

		if (pPathFormatado.matches("<\\s*tbody.*")) {
			vRetorno = Jsoup.parse("<table>" + pPathFormatado).getElementsByTag("table").get(0).children().get(0);
		} else if (pPathFormatado.matches("<\\s*tr.*")) {
			vRetorno = Jsoup.parse("<table>" + pPathFormatado).getElementsByTag("tbody").get(0).children().get(0);
		} else if (pPathFormatado.matches("<\\s*td.*")) {
			vRetorno = Jsoup.parse("<table>" + pPathFormatado).getElementsByTag("tr").get(0).children().get(0);
		}

		return vRetorno;

	}

	/**
	 * @param pString
	 */
	private String construirStringBusca(String pToString) {

		String[] vSplit = pToString.split(", ");
		ArrayList<String> vComandos = new ArrayList<String>();

		for (int vI = 1; vI < vSplit.length; vI++) {
			Document vParseBodyFragment = Jsoup.parseBodyFragment(this.formatarPath(vSplit[vI]));

			String vPathFormatado = this.formatarPath(vSplit[vI]);

			Document vParse = Jsoup.parse(vPathFormatado);
			String defaulf = "body";
			Element vChild = null;

			if (vI == 9) {
				System.out.println("");
			}

			if (vPathFormatado.contains("body") && !vSplit[vI].contains("tbody")) {
				defaulf = "html";
				vChild = vParseBodyFragment.getElementsByTag("html").get(0);
			} else {
				try {

					vChild = this.checkTableChild(vSplit[vI], vChild);

					if (vChild == null) {
						vChild = vParseBodyFragment.getElementsByTag(defaulf).get(0).children().get(0);
					}

				} catch (IndexOutOfBoundsException ex) {
					ex.printStackTrace();
					System.out.println(vParseBodyFragment);
				}
			}

			String vTagName = vChild.tagName();

			Attributes vAttributes = vChild.attributes();
			String lastCmd = vTagName;
			for (Attribute vAttribute : vAttributes) {
				lastCmd += "[" + vAttribute.getKey() + "=" + vAttribute.getValue() + "]";
			}
			vComandos.add(lastCmd);

		}

		String vCmdRetorno = "";

		for (String vString : vComandos) {
			vCmdRetorno += vString + " > ";
		}

		return vCmdRetorno;

	}

	void doMouseClicked(MouseEvent me) {
		try {
			TreePath tp = this.tree.getPathForLocation(me.getX(), me.getY());
			if (tp != null) {

				this.jtf.setText(tp.toString());
				String vStringBusca = this.construirStringBusca(tp.toString());
				vStringBusca = vStringBusca.substring(0, vStringBusca.length() - 3);
				Elements vExecutarSelect = this.executarSelect(vStringBusca);

				if (!vExecutarSelect.isEmpty()) {

					int vExtracaoSelecionada = this.extracaoList.getSelectedIndex();

					String vCmdFinal = "this.doc.select(\"" + vStringBusca + "\")";
					switch (vExtracaoSelecionada) {
					case 0:
						vCmdFinal += ".text();";
						System.out.println(vExecutarSelect.get(0).text());
						break;
					case 1:
						vCmdFinal += ".toString();";
						System.out.println(vExecutarSelect.get(0).toString());
						break;
					case 2:
						vCmdFinal += ".data();";
						System.out.println(vExecutarSelect.get(0).data());
						break;
					case 3:
						vCmdFinal += ".val();";
						System.out.println(vExecutarSelect.get(0).val());
						break;
					default:
						break;
					}
					System.out.println(vCmdFinal);
					// / this.output.setText(vExecutarSelect.get(0).toString());

				}
			} else {
				this.jtf.setText("");
			}
		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}

	public Elements executarSelect(String pSelect) {
		pSelect = pSelect.replace("html > ", "");
		Elements vSelect2 = this.doc.select(pSelect);

		System.out.println("COMANDO = " + pSelect);

		return vSelect2;
	}

	public String formatarPath(String pPath) {

		return pPath.replace("[", "");
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new TreeExample();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}