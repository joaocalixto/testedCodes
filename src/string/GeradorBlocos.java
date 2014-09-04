package string;

/**
 * Data de Cria��o: 01/09/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class GeradorBlocos {

	public static void main(String[] args) {
		String v = "vArrayDeCampos.add(new CampoUF(\"Cmb_Uf\", \"Unidade Federativa do Documento\",\r\n"
				+ "				\"Unidade Federativa do Documento Consultado\"));\r\n"
				+ "		vArrayDeCampos.add(new CampoTexto(\"Txt_Cidade\", \"Cidade do Documento\", \"Cidade do Documento Consultado\"));\r\n"
				+ "		vArrayDeCampos.add(new CampoDdd(\"Num_DddTelefone\", \"C�digo de �rea do Telefone\",\r\n"
				+ "				\"C�digo de �rea do Telefone do Documento Consultado\"));\r\n"
				+ "		vArrayDeCampos.add(new CampoTelefone(\"Num_Telefone\", \"N�mero do Telefone\",\r\n"
				+ "				\"N�mero do Telefone do Documento Consultado\"));\r\n" + "\r\n"
				+ "		vArrayDeCampos.add(new CampoTexto(\"Txt_NomeAssociado\", \"Nome do Associado � Entidade\",\r\n"
				+ "				\"Nome do Associado � Entidade para o Documento Consultado\"));\r\n"
				+ "		vArrayDeCampos.add(new CampoTexto(\"Num_CodigoEntidade\", \"C�digo da Entidade do SPC\",\r\n"
				+ "				\"C�digo da Entidade do SPC Para o Documento Consultado\"));\r\n" + "\r\n"
				+ "		vArrayDeCampos.add(new CampoDate(\"Dta_Inclusao\", \"Data de Inclus�o\", \"Data de Inclus�o do Documento no SPC\"));\r\n"
				+ "\r\n" + "		vArrayDeCampos.add(new CampoDate(\"Dta_DataVencimento\", \"Data do Vencimento do Documento\",\r\n"
				+ "				\"Data do Vencimento do Documento no SPC\"));\r\n"
				+ "		vArrayDeCampos.add(new CampoHora(\"Dta_HoraVencimento\", \"Hora do Vencimento do Documento\",\r\n"
				+ "				\"Hora do Vencimento do Documento no SPC\"));\r\n" + "\r\n"
				+ "		vArrayDeCampos.add(new CampoTexto(\"Txt_NomeEntidade\", \"Nome da Entidade\", \"Nome da Entidade Associada ao SPC\"));\r\n"
				+ "		vArrayDeCampos.add(new CampoTexto(\"Num_NumeroContrato\", \"N�mero do Contrato\",\r\n"
				+ "				\"N�mero do Contrato do Documento no SPC\"));\r\n"
				+ "		vArrayDeCampos.add(new CampoTexto(\"Txt_CompradorFiadorAvalista\", \"Comprador Fiador ou Avalista\",\r\n"
				+ "				\"Comprador Fiador ou Avalista\"));\r\n"
				+ "		vArrayDeCampos.add(new CampoDecimal(\"Vlr_ValorDivida\", \"Valor da D�vida do Documento\",\r\n"
				+ "				\"Valor da D�vida do Documento no SPC\"));";

		String[] vSplit = v.split(";");

		for (int vI = 0; vI < vSplit.length; vI++) {
			String vString = vSplit[vI];

			String[] vSplit2 = vString.split("\"");

			String vNomeCampo = vSplit2[1];

			String vBloco = "Spc";

			String template = "<function id=\"LST_" + vNomeCampo + "\" descricao=\"" + vNomeCampo + "\" type=\"LISTA\">\r\n"
					+ "      <parameters>\r\n" + "        <parameter id=\"bloco1\" value=\"" + vBloco + "\" type=\"BLOCO\"/>\r\n"
					+ "        <parameter id=\"campo1\" value=\"" + vNomeCampo + "\" type=\"CAMPO\"/>\r\n" + "      </parameters>\r\n"
					+ "      <filters/>\r\n" + "      <filterExternal/>\r\n" + "    </function>";
			System.out.println(template);
		}
	}
}
