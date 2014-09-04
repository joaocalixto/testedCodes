package codeChef;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;

/**
 * Data de Cria��o: 20/01/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class JsoupTester {

	public static void main(String[] args) throws IOException {

		String vString = new String(
				"\r\n"
						+ " <!DOCTYPE html SYSTEM \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\r\n"
						+ "\r\n"
						+ " \r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "<html>\r\n"
						+ "<head>\r\n"
						+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">\r\n"
						+ "\r\n"
						+ "<title>Correios</title>\r\n"
						+ "\r\n"
						+ "<link rel=\"icon\" href=\"../img/icon.gif\" type=\"image/gif\"/>\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ " <script type=\"text/javascript\" src='/web/dnec/js/jquery-1.4.2.min.js' language=\"JavaScript\"></script>\r\n"
						+ " <script type=\"text/javascript\" src='/web/dnec/js/ddaccordion.js' language=\"JavaScript\"></script>\r\n"
						+ " <script type=\"text/javascript\" src='/web/dnec/js/jtabber.js' language=\"JavaScript\"></script>\r\n"
						+ " <script type=\"text/javascript\" src='/web/dnec/js/jquery.colorbox-min.js' language=\"JavaScript\"></script>\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "<link rel=\"STYLESHEET\" type=\"text/css\" href='/web/dnec/css/colorbox.css'/>\r\n"
						+ "<link rel=\"STYLESHEET\" type=\"text/css\" href='/web/dnec/css/configuracao_geral.css'/>\r\n"
						+ "<link rel=\"STYLESHEET\" type=\"text/css\" href='/web/dnec/css/layout.css'/>\r\n"
						+ "<link rel=\"STYLESHEET\" type=\"text/css\" href='/web/dnec/css/layoutIE6lt.css'/>\r\n"
						+ "<link rel=\"STYLESHEET\" type=\"text/css\" href='/web/dnec/css/layoutIE7.css'/>\r\n"
						+ "<link rel=\"STYLESHEET\" type=\"text/css\" href='/web/dnec/css/layoutIE8.css'/>\r\n"
						+ "	\r\n"
						+ "\r\n"
						+ "</head>\r\n"
						+ "\r\n"
						+ "<!--[if lt IE 7]> <body class=\"ie6\"> <![endif]-->\r\n"
						+ "<!--[if IE 7]>    <body class=\"ie7\"> <![endif]-->\r\n"
						+ "<!--[if IE 8]>    <body class=\"ie8\"> <![endif]-->\r\n"
						+ "<!--[if IE 9]>    <body class=\"ie8\"> <![endif]-->\r\n"
						+ "<!--[if !IE]>-->  \r\n"
						+ "\r\n"
						+ "<body>             <!--<![endif]-->\r\n"
						+ "\r\n"
						+ "<div class=\"back\">\r\n"
						+ "<div class=\"wrap\">\r\n"
						+ "<div class=\"header\">\r\n"
						+ "<div class=\"logo float-left\"> <a href=\"http://www.correios.com.br/\" alt=\"home\"><img src='/web/dnec/img/logo.png' alt=\"Logo Correios\"/></a> </div>\r\n"
						+ "<div class=\"acesso_rapido\">\r\n"
						+ "<div class=\"text-right\">\r\n"
						+ "<a href=\"http://www.correios.com.br/esp/default.cfm\">Espa�ol</a> | \r\n"
						+ "<a href=\"http://www.correios.com.br/eng/default.cfm\">English</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\r\n"
						+ "<a href=\"http://www.correios.com.br/servicos/falecomoscorreios/default.cfm\">Fale com os Correios</a><br/>\r\n"
						+ "</div>\r\n"
						+ "<div class=\"produtosaz\">\r\n"
						+ "<div class=\"expo\" >Correios de A a Z</div>\r\n"
						+ "<span class=\"dados abaaz\">\r\n"
						+ "<div class=\"dadosaz\"> <span class=\"dica\"> Escolha pela letra inicial dos nossos produtos, servi�os e assuntos<br/>\r\n"
						+ "\r\n"
						+ "</span><br/>\r\n"
						+ "<a href=\"http://www.correios.com.br/produtosaz/default.cfm\"><b>Todos os itens</b></a><br/>\r\n"
						+ "<a href=\"http://www.correios.com.br/produtosaz/default.cfm?filtro=A/C\"><b>Correios de A-C</b></a><br/>\r\n"
						+ "<a href=\"http://www.correios.com.br/produtosaz/default.cfm?filtro=D/F\"><b>Correios de D-F</b></a><br/>\r\n"
						+ "<a href=\"http://www.correios.com.br/produtosaz/default.cfm?filtro=G/Q\"><b>Correios de G-Q</b></a><br/>\r\n"
						+ "<a href=\"http://www.correios.com.br/produtosaz/default.cfm?filtro=R/Z\"><b>Correios de R-Z</b></a>\r\n"
						+ "</div>\r\n"
						+ "</span> </div>\r\n"
						+ "<span class=\"float-right\">\r\n"
						+ "<form name=\"formbusca\" id=\"formbusca\" action=\"resultadoBusca.cfm\" method=\"post\" target=\"_blank\" >\r\n"
						+ "<input type=\"text\" id=\"criterioDeBusca\" name=\"criterioDeBusca\" value=\"Buscar\" onFocus=\"document.formbusca.criterioDeBusca.value='';\">\r\n"
						+ "\r\n"
						+ "<a class=\"btnbuscar\" href=\"javascript:void(0);\" class=\"btnbuscar\" onclick=\"validate_form(document.getElementById('formbusca'));\" title=\"bot&atilde;o Buscar\"></a>\r\n"
						+ "</form>\r\n"
						+ "</span> <br class=\"clr\"/>\r\n"
						+ "</div>\r\n"
						+ "<!-- acesso r&aacute;pido -->\r\n"
						+ "<br class=\"clr\"/>\r\n"
						+ "<!--menu Nav -->\r\n"
						+ "<ul class=\"mnav text-center\">         \r\n"
						+ "<li class=\"itemnav\"><a href=\"http://www.buscacep.correios.com.br/\" target=\"_self\">Busca CEP</a></li>\r\n"
						+ "<li class=\"itemnav\"><a href=\"http://www.correios.com.br/precosPrazos/default.cfm\" target=\"_self\">Pre&ccedil;os e Prazos</a></li>\r\n"
						+ "<li class=\"itemnav\"><a href=\"http://www.correios.com.br/enderecador/default.cfm\" target=\"_self\">Endere&ccedil;ador</a></li>\r\n"
						+ "\r\n"
						+ "<li class=\"itemnav\"><a href=\"http://www.correios.com.br/servicos/agencias/default.cfm\" target=\"_self\">Ag&ecirc;ncias</a></li>\r\n"
						+ "<li class=\"itemnav\"><a href=\"http://www.correios.com.br/disqueColeta/default.cfm\" target=\"_self\">Disque Coleta</a></li>\r\n"
						+ "<li class=\"itemnav\"><a href=\"http://www.correios.com.br/servicos/rastreamento/rastreamento.cfm\" target=\"_self\">Rastreamento</a></li>\r\n"
						+ "</ul>\r\n"
						+ "<!--/menu Nav -->\r\n"
						+ "</div>\r\n"
						+ "<div id=\"nav\"> \r\n"
						+ "<a referencia=\"lamina\"  id=\"orelhavoce\" class=\"selected\" href=\"http://www.correios.com.br/default.cfm\">Para Voc&ecirc;</a> \r\n"
						+ "<a referencia=\"lamina2\" id=\"orelhaempresas\"  href=\"http://www.correios.com.br/default.cfm?lam=2\">Para sua Empresa</a> \r\n"
						+ "<a referencia=\"lamina3\" id=\"orelhafornecedores\"  href=\"http://www.correios.com.br/default.cfm?lam=3\">Para Fornecedores</a> \r\n"
						+ "\r\n"
						+ "<a referencia=\"lamina4\" id=\"orelhacorreios\" href=\"http://www.correios.com.br/default.cfm?lam=4\">Sobre os Correios</a>\r\n"
						+ "<div class=\"clr\"></div>\r\n"
						+ "</div>\r\n"
						+ "<div id=\"lamina\" class=\"laminas voce\">\r\n"
						+ "<!-- menu horiz -->\r\n"
						+ "<ul class=\"iconeaba\">\r\n"
						+ "        <li class=\"abarotulo  ico1\" > <a href=\"javascript:void(0);\" class=\"icone\">Enviar</a>\r\n"
						+ "          <ul class=\"submenu\">\r\n"
						+ "            <li> <a href=\"voce/enviar/Encomendas.cfm\" target=\"_self\">Encomendas</a> </li>\r\n"
						+ "            <li> <a href=\"voce/enviar/Documentos.cfm\" target=\"_self\">Documentos</a> </li>\r\n"
						+ "            <li> <a href=\"voce/enviar/Telegramas.cfm\" target=\"_self\">Telegrama</a> </li>\r\n"
						+ "            <li> <a href=\"voce/enviar/Cartas.cfm\" target=\"_self\">Cartas</a> </li>\r\n"
						+ "            <li> <a href=\"voce/enviar/valePostalEletronico.cfm\" target=\"_self\">Dinheiro</a> </li>\r\n"
						+ "          </ul>\r\n"
						+ "        </li>\r\n"
						+ "        <li class=\"abarotulo  ico2\" > <a href=\"javascript:void(0);\" class=\"icone\">Acompanhar</a>\r\n"
						+ "          <ul class=\"submenu\">\r\n"
						+ "            <li> <a href=\"servicos/rastreamento/rastreamento.cfm\" target=\"_blank\">Entregas</a> </li>\r\n"
						+ "            <li> <a href=\"disqueColeta/consultas/default.cfm\" target=\"_blank\">Coletas</a> </li>\r\n"
						+ "            <li> <a href=\"http://www.receita.fazenda.gov.br/Aplicacoes/ATCTA/CPF/ConsultaAndamento.asp\" target=\"_blank\">CPF - Receita Federal</a> </li>\r\n"
						+ "            <li> <a href=\"servicos/achados_perdidos/default.cfm\" target=\"_blank\">Documentos perdidos</a> </li>\r\n"
						+ "            <li> <a href=\"internacional/cfm/cotacao_moedas.cfm\" target=\"_self\">Cota��o de moedas</a> </li>\r\n"
						+ "            <li> <a href=\"produtos_servicos/catalogo/regionais/reg_detran_ce.cfm\" target=\"_self\">Consultas DETRAN-CE</a> </li>\r\n"
						+ "          </ul>\r\n"
						+ "        </li>\r\n"
						+ "        <li class=\"abarotulo  ico3\" > <a href=\"javascript:void(0);\" class=\"icone\">Receber</a>\r\n"
						+ "          <ul class=\"submenu\">\r\n"
						+ "            <li> <a href=\"voce/receber/valePostalEletronico.cfm\" target=\"_self\">Dinheiro</a> </li>\r\n"
						+ "            <li> <a href=\"impFacil/default.cfm\" target=\"_blank\">Importa��es</a> </li>\r\n"
						+ "          </ul>\r\n"
						+ "        </li>\r\n"
						+ "        <li class=\"abarotulo  ico4\" > <a href=\"javascript:void(0);\" class=\"icone\">Comprar</a>\r\n"
						+ "          <ul class=\"submenu\">\r\n"
						+ "            <li> <a href=\"http://www.correios.com.br/shopping/correiosonline\" target=\"_blank\">Selos e Cole��es</a> </li>\r\n"
						+ "            <li> <a href=\"http://shopping.correios.com.br/wbm/store/script/wbm2400902p01.aspx?cd_company=ErZW8Dm9i54=&cd_department=3ErDTeanVp8=\" target=\"_blank\">Cart�es Postais</a> </li>\r\n"
						+ "            <li> <a href=\"selos/prod_conveniencia/pre_selados_person/preselado_personal.cfm\" target=\"_blank\">Produtos Personalizados</a> </li>\r\n"
						+ "            <li> <a href=\"http://shopping.correios.com.br/wbm/store/script/wbm2400902p01.aspx?cd_company=ErZW8Dm9i54=&cd_department=GH4Z6pxe4OE=\" target=\"_blank\">Grife Via Postal</a> </li>\r\n"
						+ "            <li> <a href=\"http://www.correios.com.br/shopping/correiosonline\" target=\"_blank\">Aerogramas e Cart�es</a> </li>\r\n"
						+ "            <li> <a href=\"http://shopping.correios.com.br/wbm/store/script/wbm2400902p01.aspx?cd_company=ErZW8Dm9i54=&cd_department=R9kapHuB0uA=\" target=\"_blank\">Embalagens</a> </li>\r\n"
						+ "            <li> <a href=\"http://www.correios.com.br/shopping/correiosonline\" target=\"_blank\">Outros Produtos</a> </li>\r\n"
						+ "          </ul>\r\n"
						+ "        </li>\r\n"
						+ "        <li class=\"abarotulo  ico5\" > <a href=\"javascript:void(0);\" class=\"icone\">Solicitar</a>\r\n"
						+ "          <ul class=\"submenu\">\r\n"
						+ "            <li> <a href=\"disqueColeta/pedido/default.cfm\" target=\"_self\">Coletas</a> </li>\r\n"
						+ "            <li> <a href=\"produtos_servicos/certificacaoDigital/default.cfm\" target=\"_self\">Certificado Digital</a> </li>\r\n"
						+ "          </ul>\r\n"
						+ "        </li>\r\n"
						+ "        <li class=\"abarotulo text-center ico6\"  id=\"last\"> <a class=\"icone\" href=\"http://shopping.correios.com.br\" target=\"_blank\" >CorreiosNet Shopping</a>\r\n"
						+ "          <ul class=\"submenu\">\r\n"
						+ "          </ul>\r\n"
						+ "        </li>\r\n"
						+ "      </ul>\r\n"
						+ "<!--  menu horiz -->\r\n"
						+ "<br class=\"clr\"/>	\r\n"
						+ " 		<div class=\"column1\">\r\n"
						+ "<span class=\"mver\">\r\n"
						+ "<span class=\"dominio\"></span>\r\n"
						+ "<h1>Busca CEP</h1>\r\n"
						+ "<ul>\r\n"
						+ "	<li><a href=\"/servicos/dnec/menuAction.do?Metodo=menuEndereco\"  target=\"_self\" >CEP ou Endere�o</a>\r\n"
						+ "	<li><a href=\"/servicos/dnec/menuAction.do?Metodo=menuLogradouro\" target=\"_self\" >CEP por localidade | Logradouro</a>\r\n"
						+ "	<li><a href=\"/servicos/dnec/menuAction.do?Metodo=menuCep\"  target=\"_self\" >Endere�o por CEP</a>\r\n"
						+ "	<li><a href=\"/servicos/dnec/menuAction.do?Metodo=menuLogradouroBairro\" target=\"_self\" >CEP de Logradouro por Bairro</a>\r\n"
						+ "	<li><a href=\"/servicos/dnec/menuAction.do?Metodo=menuFaixaCep\" target=\"_self\" >Faixas de CEP</a>\r\n"
						+ "	<li><a href=\"/servicos/dnec/menuAction.do?Metodo=menuUnidadeOperacional\" target=\"_self\" >CEPs de unidades operacionais</a>\r\n"
						+ "	<li><a href=\"/servicos/dnec/menuAction.do?Metodo=menuCepEspecial\" target=\"_self\" >CEPs especiais</a>\r\n"
						+ "	<li><a href=\"/servicos/dnec/menuAction.do?Metodo=menuCaixaComunitaria\" target=\"_self\" >Caixa postal comunit�ria</a>\r\n"
						+ "	<li><a href=\"/servicos/dnec/menuAction.do?Metodo=menuCaixaPostal\" target=\"_self\" >CEP de caixa postal</a>\r\n"
						+ "	<li><a href=\"/servicos/dnec/menuAction.do?Metodo=menuCepPromocional\" target=\"_self\" >CEP promocional</a>\r\n"
						+ "\r\n"
						+ "</ul>\r\n"
						+ "</span>\r\n"
						+ "\r\n"
						+ "		</div><!-- column1 -->\r\n"
						+ "		<div class=\"column2\">\r\n"
						+ "			<div class=\"breadcrumb\"></div><!-- breadcrumb -->\r\n"
						+ "			\r\n"
						+ "			<div class=\"content\">\r\n"
						+ "				<div class=\"tituloimagem\"><h1>Busca CEP</h1></div>\r\n"
						+ "				\r\n"
						+ "				<div class=\"ctrlcontent\">\r\n"
						+ "				<p>Fa�a suas consultas individuais de CEP, destinadas a endere�amentos de objetos de correspond�ncias a serem postadas nos Correios.</p>\r\n"
						+ "\r\n"
						+ "             <script language=\"JavaScript\" type=\"text/javascript\">\r\n"
						+ "                 function detalharCep(posicao, tipoCep) {\r\n"
						+ "                     document.DetalheGeral.TipoCep.value=tipoCep;\r\n"
						+ "                     document.DetalheGeral.Posicao.value=posicao;\r\n"
						+ "                     document.DetalheGeral.submit();\r\n"
						+ "                 }\r\n"
						+ "            </script>  \r\n"
						+ "                                         \r\n"
						+ "            \r\n"
						+ "                \r\n"
						+ "            \r\n"
						+ "\r\n"
						+ "                \r\n"
						+ "                \r\n"
						+ "                    \r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "<!--Mensagem de Advertencia. Erros não fatais -->\r\n"
						+ "<b>\r\n"
						+ "\r\n"
						+ "</b>\r\n"
						+ "\r\n"
						+ "<!-- Cabecalho da tabela -->	\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "               \r\n"
						+ "\r\n"
						+ "\r\n"
						+ "<b>1 Logradouro(s) </b>\r\n"
						+ "<br>\r\n"
						+ "\r\n"
						+ "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\r\n"
						+ "  <tr>\r\n"
						+ "    <td bgcolor=\"gray\">\r\n"
						+ "      <table border=\"0\" cellpadding=\"1\" cellspacing=\"1\">\r\n"
						+ "        <tr>        \r\n"
						+ "          <td width=\"266\" align=\"left\" bgcolor=\"#ECF3F6\">\r\n"
						+ "             <font style=\"font-family: Arial, Helvetica, sans-serif; font-size: 8pt; color: #002A78; font-weight: bolder\"> \r\n"
						+ "                &nbsp;Logradouro\r\n"
						+ "             </font>\r\n"
						+ "          </td>\r\n"
						+ "          <td width=\"141\" align=\"left\" bgcolor=\"#ECF3F6\">\r\n"
						+ "              <font style=\"font-family: Arial, Helvetica, sans-serif; font-size: 8pt; color: #002A78; font-weight: bolder\">\r\n"
						+ "                 &nbsp;Bairro\r\n"
						+ "              </font>\r\n"
						+ "          </td>\r\n"
						+ "          <td width=\"141\" align=\"left\" bgcolor=\"#ECF3F6\">\r\n"
						+ "              <font style=\"font-family: Arial, Helvetica, sans-serif; font-size: 8pt; color: #002A78; font-weight: bolder\">\r\n"
						+ "                 &nbsp;Localidade\r\n"
						+ "              </font>\r\n"
						+ "          </td>\r\n"
						+ "          <td width=\"29\" align=\"left\" bgcolor=\"#ECF3F6\">\r\n"
						+ "              <font style=\"font-family: Arial, Helvetica, sans-serif; font-size: 8pt; color: #002A78; font-weight: bolder\">\r\n"
						+ "                &nbsp;UF\r\n"
						+ "              </font>\r\n"
						+ "          </td>\r\n"
						+ "          <td width=\"68\" align=\"left\" bgcolor=\"#ECF3F6\">\r\n"
						+ "              <font style=\"font-family: Arial, Helvetica, sans-serif; font-size: 8pt; color: #002A78; font-weight: bolder\">\r\n"
						+ "                &nbsp;CEP\r\n"
						+ "              </font>\r\n"
						+ "          </td>\r\n"
						+ "        </tr>\r\n"
						+ "      </table>\r\n"
						+ "    </td>\r\n"
						+ "  </tr>\r\n"
						+ "</table>\r\n"
						+ "\r\n"
						+ "<!-- Fim cabecalho da tabela -->	\r\n"
						+ "\r\n"
						+ "<div> \r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "<?xml version = '1.0' encoding = 'ISO-8859-1'?>\r\n"
						+ "<table border=\"0\" cellspacing=\"1\" cellpadding=\"5\" bgcolor=\"gray\">\r\n"
						+ "   <tr bgcolor=\"#ECF3F6\" onclick=\"javascript:detalharCep('1','2');\" style=\"cursor: pointer;\">\r\n"
						+ "      <td width=\"268\" style=\"padding: 2px\">Rua �bano</td>\r\n"
						+ "      <td width=\"140\" style=\"padding: 2px\">Ouro Preto</td>\r\n"
						+ "      <td width=\"140\" style=\"padding: 2px\">Olinda</td>\r\n"
						+ "      <td width=\"25\" style=\"padding: 2px\">PE</td>\r\n"
						+ "      <td width=\"65\" style=\"padding: 2px\">53370-580</td>\r\n"
						+ "   </tr>\r\n"
						+ "</table>\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "<table width=\"645\">\r\n"
						+ "  <tr>\r\n"
						+ "    <td align=\"left\" width=\"50\">\r\n"
						+ "      \r\n"
						+ "    </td>\r\n"
						+ "    <br>\r\n"
						+ "    <br>\r\n"
						+ "    \r\n"
						+ "    <td align=\"center\"  width=\"545\">\r\n"
						+ "     <font color=\"#002A78\"><b>Para mais informa��es, clique no registro desejado.</b></font> <font color=\"White\">&nbsp;&nbsp;&nbsp;260&nbsp;&nbsp;&nbsp;</font>\r\n"
						+ "    </td>\r\n"
						+ "    \r\n"
						+ "    <td align=\"right\" width=\"50\">\r\n"
						+ "        \r\n"
						+ "    </td>\r\n"
						+ "  </tr>\r\n"
						+ "</table>\r\n"
						+ "<form name=\"Geral\" method=\"post\" action=\"/servicos/dnec/consultaLogradouroAction.do;jsessionid=c0a81e1730d6ece530b11c154c3baccaee47bf3619c1.e38Ob30TahuKc40LbxqRaN8Sc3ySe6fznA5Pp7ftolbGmkTy\">\r\n"
						+ "    <input type=\"hidden\" name='StartRow'   value=\"\">\r\n"
						+ "    <input type=\"hidden\" name='EndRow'     value=\"\">\r\n"
						+ "    <input type=\"hidden\" name='Metodo'     value=\"pagina\">\r\n"
						+ "    <input type=\"hidden\" name=\"TipoConsulta\" value=\"relaxation\"> \r\n"
						+ "</form>\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ "  \r\n"
						+ "<form name=\"DetalheGeral\" action='/servicos/dnec/detalheCEPAction.do' method=\"post\">\r\n"
						+ "   <input type=\"hidden\" name=\"Metodo\" value=\"detalhe\">\r\n"
						+ "   <input type=\"hidden\" name=\"Posicao\">\r\n"
						+ "   <input type=\"hidden\" name=\"TipoCep\">   \r\n"
						+ "   <input type=\"hidden\" name=\"CEP\"> \r\n"
						+ "</form>\r\n"
						+ "  \r\n"
						+ "</div>\r\n"
						+ "                \r\n"
						+ "                \r\n"
						+ "                \r\n"
						+ "                  \r\n"
						+ "                  \r\n"
						+ "                  \r\n"
						+ "                  \r\n"
						+ "                \r\n"
						+ "                \r\n"
						+ "                \r\n"
						+ "                \r\n"
						+ "                \r\n"
						+ "                \r\n"
						+ "                \r\n"
						+ "                  \r\n"
						+ "\r\n"
						+ "            \r\n"
						+ "                                          \r\n"
						+ "			</div><!-- ctrlcontent -->\r\n"
						+ "			</div><!-- content -->\r\n"
						+ "			\r\n"
						+ "		</div><!-- column2 -->\r\n"
						+ "		<br class=\"clr\"/>\r\n"
						+ "	</div><!-- laminas -->\r\n"
						+ "\r\n"
						+ "	\r\n"
						+ "\r\n"
						+ "<div class=\"footer text-center\">\r\n"
						+ "	<div class=\"mfooter\">\r\n"
						+ "		<ul>\r\n"
						+ "        	<li><a href=\"servicos/falecomoscorreios/default.cfm\">Fale com os Correios</a></li>\r\n"
						+ "			<li><a href=\"produtosaz/produto.cfm?id=C3A9909B-5056-9163-89CEB93799820DE2\">Contatos Comerciais</a></li>\r\n"
						+ "			<li><a href=\"sobreCorreios/salaImprensa/faleAsCom.cfm\">Sala de Imprensa</a></li> \r\n"
						+ "			<li><a href=\"http://www.mc.gov.br/\">Minist�rio das Comunica��es</a></li> \r\n"
						+ "			<li class=\"end\"><a href=\"http://m.correios.com.br/\">Correios Mobile</a></li>\r\n"
						+ "    	</ul>\r\n"
						+ "    </div>\r\n"
						+ "    <div class=\"copy\">\r\n"
						+ "	    <a href=\"politica/default.cfm\">Pol�tica de Privacidade e notas legais</a> -  &copy; Copyright  2012 Correios - Todos os direitos reservados.\r\n"
						+ "    </div>\r\n"
						+ "</div>\r\n"
						+ "<br class=\"clr\"/>\r\n"
						+ "</div><!-- class=\"wrap\" -->\r\n"
						+ "</div><!-- class=\"back\" -->\r\n"
						+ "</body>\r\n"
						+ "</html>\r\n"
						+ "<!-- inicio Google Analytics -->\r\n"
						+ "\r\n"
						+ "	<script src=\"http://www.google-analytics.com/urchin.js\" type=\"text/javascript\"></script> \r\n"
						+ "	<script type=\"text/javascript\"> \r\n"
						+ "		_uacct = \"UA-564464-1\"; \r\n"
						+ "		urchinTracker(); \r\n"
						+ "	</script>\r\n"
						+ "	\r\n"
						+ "<!-- FIM Google Analytics -->\r\n"
						+ "<script type=\"text/javascript\">\r\n"
						+ "$(document).ready(function(){\r\n"
						+ "$.jtabber({\r\n"
						+ "mainLinkTag: \"#seletorforms select option\", // much like a css selector, you must have a 'title' attribute that links to the div id name\r\n"
						+ "activeLinkClass: \"selected\", // class that is applied to the tab once it's clicked\r\n"
						+ "hiddenContentClass: \"hiddencontent2\", // the class of the content you are hiding until the tab is clicked\r\n"
						+ "showDefaultTab: null, // 1 will open the first tab, 2 will open the second etc.  null will open nothing by default\r\n"
						+ "showErrors: false, // true/false - if you want errors to be alerted to you\r\n"
						+ "effect: 'slide', // null, 'slide' or 'fade' - do you want your content to fade in or slide in?\r\n"
						+ "effectSpeed: 'fast' // 'slow', 'medium' or 'fast' - the speed of the effect\r\n"
						+ "})\r\n"
						+ "})\r\n"
						+ "$(document).ready(function(){\r\n"
						+ "$.jtabber({\r\n"
						+ "mainLinkTag: \"#seletorforms2 select option\", // much like a css selector, you must have a 'title' attribute that links to the div id name\r\n"
						+ "activeLinkClass: \"selected\", // class that is applied to the tab once it's clicked\r\n"
						+ "hiddenContentClass: \"hiddencontent3\", // the class of the content you are hiding until the tab is clicked\r\n"
						+ "showDefaultTab: null, // 1 will open the first tab, 2 will open the second etc.  null will open nothing by default\r\n"
						+ "showErrors: false, // true/false - if you want errors to be alerted to you\r\n"
						+ "effect: 'slide', // null, 'slide' or 'fade' - do you want your content to fade in or slide in?\r\n"
						+ "effectSpeed: 'fast' // 'slow', 'medium' or 'fast' - the speed of the effect\r\n"
						+ "})\r\n"
						+ "})\r\n"
						+ "$(document).ready(function(){\r\n"
						+ "$.jtabber({\r\n"
						+ "mainLinkTag: \"#wizard a\", // much like a css selector, you must have a 'title' attribute that links to the div id name\r\n"
						+ "activeLinkClass: \"selected\", // class that is applied to the tab once it's clicked\r\n"
						+ "hiddenContentClass: \"hiddencontent4\", // the class of the content you are hiding until the tab is clicked\r\n"
						+ "showDefaultTab: 4, // 1 will open the first tab, 2 will open the second etc.  null will open nothing by default\r\n"
						+ "showErrors: false, // true/false - if you want errors to be alerted to you\r\n"
						+ "effect: null, // null, 'slide' or 'fade' - do you want your content to fade in or slide in?\r\n"
						+ "effectSpeed: 'fast' // 'slow', 'medium' or 'fast' - the speed of the effect\r\n"
						+ "})\r\n"
						+ "})\r\n"
						+ "</script>\r\n"
						+ "<script type=\"text/javascript\">\r\n"
						+ "$(document).ready(function(){\r\n"
						+ "//Examples of how to assign the ColorBox event to elements\r\n"
						+ "$(\"a[rel='example1']\").colorbox();\r\n"
						+ "$(\"a[rel='example2']\").colorbox({transition:\"fade\"});\r\n"
						+ "$(\"a[rel='example3']\").colorbox({transition:\"none\", width:\"75%\", height:\"75%\"});\r\n"
						+ "$(\"a[rel='example4']\").colorbox({slideshow:true});\r\n"
						+ "$(\".example5\").colorbox();\r\n"
						+ "$(\".example6\").colorbox({iframe:true, innerWidth:425, innerHeight:344});\r\n"
						+ "$(\".example7\").colorbox({width:\"333px\", height:\"350px\", iframe:true});\r\n"
						+ "$(\".example8\").colorbox({width:\"50%\", inline:true, href:\"#inline_example1\"});\r\n"
						+ "$(\".example9\").colorbox({\r\n"
						+ "onOpen:function(){ alert('onOpen: colorbox is about to open'); },\r\n"
						+ "onLoad:function(){ alert('onLoad: colorbox has started to load the targeted content'); },\r\n"
						+ "onComplete:function(){ alert('onComplete: colorbox has displayed the loaded content'); },\r\n"
						+ "onCleanup:function(){ alert('onCleanup: colorbox has begun the close process'); },\r\n"
						+ "onClosed:function(){ alert('onClosed: colorbox has completely closed'); }\r\n"
						+ "});\r\n"
						+ "//Example of preserving a JavaScript event for inline calls.\r\n"
						+ "$(\"#click\").click(function(){ \r\n"
						+ "$('#click').css({\"background-color\":\"#f00\", \"color\":\"#fff\", \"cursor\":\"inherit\"}).text(\"Open this window again and this message will still be here.\");\r\n"
						+ "return false;\r\n"
						+ "});\r\n"
						+ "});\r\n"
						+ "</script>\r\n"
						+ "\r\n"
						+ "<script type=\"text/javascript\">\r\n"
						+ "ddaccordion.init({\r\n"
						+ "headerclass: \"expo\", //Shared CSS class name of headers group\r\n"
						+ "contentclass: \"dados\", //Shared CSS class name of contents group\r\n"
						+ "revealtype: \"click\", //Reveal content when user clicks or onmouseover the header? Valid value: \"click\", \"clickgo\", or \"mouseover\"\r\n"
						+ "mouseoverdelay: 100, //if revealtype=\"mouseover\", set delay in milliseconds before header expands onMouseover\r\n"
						+ "collapseprev: false, //Collapse previous content (so only one open at any time)? true/false\r\n"
						+ "defaultexpanded: [], //index of content(s) open by default [index1, index2, etc] [] denotes no content\r\n"
						+ "onemustopen: false, //Specify whether at least one header should be open always (so never all headers closed)\r\n"
						+ "animatedefault: false, //Should contents open by default be animated into view?\r\n"
						+ "persiststate: false, //persist state of opened contents within browser session?\r\n"
						+ "toggleclass: [\"\", \"\"], //Two CSS classes to be applied to the header when it's collapsed and expanded, respectively [\"class1\", \"class2\"]\r\n"
						+ "togglehtml: [\"\", \"\", \"\"], //Additional HTML added to the header when it's collapsed and expanded, respectively  [\"position\", \"html1\", \"html2\"] (see docs)\r\n"
						+ "animatespeed: \"normal\", //speed of animation: integer in milliseconds (ie: 200), or keywords \"fast\", \"normal\", or \"slow\"\r\n"
						+ "oninit:function(headers, expandedindices){ //custom code to run when headers have initalized\r\n"
						+ "//do nothing\r\n"
						+ "},\r\n"
						+ "onopenclose:function(header, index, state, isuseractivated){ //custom code to run whenever a header is opened or closed\r\n"
						+ "}\r\n"
						+ "})\r\n"
						+ "ddaccordion.init({\r\n"
						+ "headerclass: \"abarotulo\", //Shared CSS class name of headers group\r\n"
						+ "contentclass: \"submenu\", //Shared CSS class name of contents group\r\n"
						+ "revealtype: \"mouseover\", //Reveal content when user clicks or onmouseover the header? Valid value: \"click\", \"clickgo\", or \"mouseover\"\r\n"
						+ "mouseoverdelay: 100, //if revealtype=\"mouseover\", set delay in milliseconds before header expands onMouseover\r\n"
						+ "collapseprev: true, //Collapse previous content (so only one open at any time)? true/false\r\n"
						+ "defaultexpanded: [], //index of content(s) open by default [index1, index2, etc] [] denotes no content\r\n"
						+ "onemustopen: false, //Specify whether at least one header should be open always (so never all headers closed)\r\n"
						+ "animatedefault: false, //Should contents open by default be animated into view?\r\n"
						+ "persiststate: false, //persist state of opened contents within browser session?\r\n"
						+ "toggleclass: [\"\", \"selected\"], //Two CSS classes to be applied to the header when it's collapsed and expanded, respectively [\"class1\", \"class2\"]\r\n"
						+ "togglehtml: [\"\", \"\", \"\"], //Additional HTML added to the header when it's collapsed and expanded, respectively  [\"position\", \"html1\", \"html2\"] (see docs)\r\n"
						+ "animatespeed: \"fast\", //speed of animation: integer in milliseconds (ie: 200), or keywords \"fast\", \"normal\", or \"slow\"\r\n"
						+ "oninit:function(headers, expandedindices){ //custom code to run when headers have initalized\r\n"
						+ "//do nothing\r\n"
						+ "},\r\n"
						+ "onopenclose:function(header, index, state, isuseractivated){ //custom code to run whenever a header is opened or closed\r\n"
						+ "}\r\n"
						+ "})\r\n"
						+ "/*Agencias*/\r\n"
						+ "ddaccordion.init({\r\n"
						+ "headerclass: \"expodados\", //Shared CSS class name of headers group\r\n"
						+ "contentclass: \"contentexpodados\", //Shared CSS class name of contents group\r\n"
						+ "revealtype: \"click\", //Reveal content when user clicks or onmouseover the header? Valid value: \"click\", \"clickgo\", or \"mouseover\"\r\n"
						+ "mouseoverdelay: 0, //if revealtype=\"mouseover\", set delay in milliseconds before header expands onMouseover\r\n"
						+ "collapseprev: false, //Collapse previous content (so only one open at any time)? true/false\r\n"
						+ "defaultexpanded: [], //index of content(s) open by default [index1, index2, etc] [] denotes no content\r\n"
						+ "onemustopen: false, //Specify whether at least one header should be open always (so never all headers closed)\r\n"
						+ "animatedefault: false, //Should contents open by default be animated into view?\r\n"
						+ "persiststate: false, //persist state of opened contents within browser session?\r\n"
						+ "toggleclass: [\"\", \"selected\"], //Two CSS classes to be applied to the header when it's collapsed and expanded, respectively [\"class1\", \"class2\"]\r\n"
						+ "togglehtml: [\"\", \"\", \"\"], //Additional HTML added to the header when it's collapsed and expanded, respectively  [\"position\", \"html1\", \"html2\"] (see docs)\r\n"
						+ "animatespeed: \"fast\", //speed of animation: integer in milliseconds (ie: 200), or keywords \"fast\", \"normal\", or \"slow\"\r\n"
						+ "oninit:function(headers, expandedindices){ //custom code to run when headers have initalized\r\n"
						+ "//do nothing\r\n"
						+ "},\r\n"
						+ "onopenclose:function(header, index, state, isuseractivated){ //custom code to run whenever a header is opened or closed\r\n"
						+ "//do nothing\r\n" + "}\r\n" + "})\r\n" + "function iconeAbaMouseOut(){\r\n"
						+ "$('ul.iconeaba').mouseleave(function() {\r\n" + "ddaccordion.collapseall('abarotulo');\r\n"
						+ "});	\r\n" + "}\r\n" + "function produtosAZMouseOut(){\r\n"
						+ "$('div.produtosaz').mouseleave(function() {\r\n" + "ddaccordion.collapseall('expo');\r\n"
						+ "});	\r\n" + "}\r\n" + "function produtosAZtrocaMostraTudo(){\r\n"
						+ "$('#abreTudo').css('display','block');\r\n" + "$('#abreFecha a').click(function() {\r\n"
						+ "$(\"#abreFecha a\").toggle();\r\n" + "});	\r\n" + "}\r\n" + "function cbDivMouseOut(){\r\n"
						+ "$('div.cbDiv').mouseleave(function() {\r\n" + "ddaccordion.collapseall('expo');\r\n"
						+ "});	\r\n" + "}\r\n" + "$(document).ready(function(){\r\n" + "iconeAbaMouseOut();\r\n"
						+ "produtosAZMouseOut();\r\n" + "produtosAZtrocaMostraTudo();\r\n" + "cbDivMouseOut();\r\n"
						+ "});\r\n" + "</script>>");
		Document vDocument = Jsoup.parse(vString);

		System.out.println(vDocument.html());

		System.out.println("ESCAPADO ----------------------------");
		vDocument.outputSettings().escapeMode(EscapeMode.xhtml);

		System.out.println(vDocument.html());

	}
}