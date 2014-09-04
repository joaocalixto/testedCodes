package file;

/**
 * Data de Criação: 29/08/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class CountString {

	public static void main(String[] args) {
		String v = "new GeradorSpcBrasilBlocoRendaPresumida(), new GeradorSpcBrasilBlocoFaixaRendaPresumida(), new GeradorSpcBrasilBlocoSpcScore12Meses(),\r\n"
				+ "				new GeradorSpcBrasilBlocoTelefoneConsultado(), new GeradorSpcBrasilBlocoCapitalSocial(), new GeradorSpcBrasilBlocoGrafiaPJ(),\r\n"
				+ "				new GeradorSpcBrasilBlocoAlertaDocumento(), new GeradorSpcBrasilBlocoAntecessora(), new GeradorSpcBrasilBlocoAtividadeEmpresa(),\r\n"
				+ "				new GeradorSpcBrasilBlocoBaseInoperante(), new GeradorSpcBrasilBlocoChequeLojista(), new GeradorSpcBrasilBlocoChequesSemFundo(),\r\n"
				+ "				new GeradorSpcBrasilBlocoConsultaRealizada(), new GeradorSpcBrasilBlocoConsumidorPessoaFisica(),\r\n"
				+ "				new GeradorSpcBrasilBlocoConsumidorPessoaJuridica(), new GeradorSpcBrasilBlocoContraOrdemAgenciaDiferente(),\r\n"
				+ "				new GeradorSpcBrasilBlocoContraOrdemDocumentoDiferente(), new GeradorSpcBrasilBlocoCreditoConcedido(),\r\n"
				+ "				new GeradorSpcBrasilBlocoDadosAgenciaBancaria(), new GeradorSpcBrasilBlocoDataConsulta(),\r\n"
				+ "				new GeradorSpcBrasilBlocoEnderecoCepConsultado(), new GeradorSpcBrasilBlocoInformacaoPoderJudiciario(),\r\n"
				+ "				new GeradorSpcBrasilBlocoInformacoesBancariasDocumentoDiferente(), new GeradorSpcBrasilBlocoMensagemComplementar(),\r\n"
				+ "				new GeradorSpcBrasilBlocoOperador(), new GeradorSpcBrasilBlocoParticipacaoFalencia(), new GeradorSpcBrasilBlocoPendenciaFinanceira(),\r\n"
				+ "				new GeradorSpcBrasilBlocoPrincipaisProdutos(), new GeradorSpcBrasilBlocoProtestos(), new GeradorSpcBrasilBlocoProtocolo(),\r\n"
				+ "				new GeradorSpcBrasilBlocoReferenciaComercial(), new GeradorSpcBrasilBlocoReferenciaisNegocios(),\r\n"
				+ "				new GeradorSpcBrasilBlocoRegistroConsulta(), new GeradorSpcBrasilBlocoRelacionamentoMaisAntigoFornecedores(),\r\n"
				+ "				new GeradorSpcBrasilBlocoRestricaoCredito(), new GeradorSpcBrasilBlocoRisckscoring12Meses(),\r\n"
				+ "				new GeradorSpcBrasilBlocoRiskscoring6Meses(), new GeradorSpcBrasilBlocoSpc(), new GeradorSpcBrasilBlocoTelefoneVinculadoConsumidor(),\r\n"
				+ "				new GeradorSpcBrasilBlocoUltimasConsultas(), new GeradorSpcBrasilBlocoUltimoEnderecoInformado(),\r\n"
				+ "				new GeradorSpcBrasilBlocoUltimoTelefoneInformado(), new GeradorSpcBrasilBlocoChequesSemFundoAchei(),\r\n"
				+ "				new GeradorSpcBrasilBlocoRestricaoFinanceira(), new GeradorSpcBrasilBlocoSpcScore03Meses(),\r\n"
				+ "				new GeradorSpcBrasilBlocoSpcScore12Meses()";

		String[] vSplit = v.split(",");

		System.out.println("tamanhao = " + vSplit.length);
	}

}
