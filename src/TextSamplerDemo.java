/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

/*
 * TextSamplerDemo.java requires the following files:
 *   TextSamplerDemoHelp.html (which references images/dukeWaveRed.gif)
 *   images/Pig.gif
 *   images/sound.gif
 */

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;              //for layout managers and more
import java.awt.event.*;        //for action events

import java.net.URL;
import java.io.IOException;

public class TextSamplerDemo extends JPanel
                             implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String newline = "\n";
    protected static final String textFieldString = "JTextField";
    protected static final String passwordFieldString = "JPasswordField";
    protected static final String ftfString = "JFormattedTextField";
    protected static final String buttonString = "JButton";

    protected JLabel actionLabel;

    public TextSamplerDemo() {
        setLayout(new BorderLayout());

        //Create a regular text field.
        JTextField textField = new JTextField(10);
        textField.setActionCommand(textFieldString);
        textField.addActionListener(this);

        //Create a password field.
        JPasswordField passwordField = new JPasswordField(10);
        passwordField.setActionCommand(passwordFieldString);
        passwordField.addActionListener(this);

        //Create a formatted text field.
        JFormattedTextField ftf = new JFormattedTextField(
                java.util.Calendar.getInstance().getTime());
        ftf.setActionCommand(textFieldString);
        ftf.addActionListener(this);

        //Create some labels for the fields.
        JLabel textFieldLabel = new JLabel(textFieldString + ": ");
        textFieldLabel.setLabelFor(textField);
        JLabel passwordFieldLabel = new JLabel(passwordFieldString + ": ");
        passwordFieldLabel.setLabelFor(passwordField);
        JLabel ftfLabel = new JLabel(ftfString + ": ");
        ftfLabel.setLabelFor(ftf);

        //Create a label to put messages during an action event.
        actionLabel = new JLabel("Type text in a field and press Enter.");
        actionLabel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        //Lay out the text controls and the labels.
        JPanel textControlsPane = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        textControlsPane.setLayout(gridbag);

        JLabel[] labels = {textFieldLabel, passwordFieldLabel, ftfLabel};
        JTextField[] textFields = {textField, passwordField, ftf};
        addLabelTextRows(labels, textFields, gridbag, textControlsPane);

        c.gridwidth = GridBagConstraints.REMAINDER; //last
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        textControlsPane.add(actionLabel, c);
        textControlsPane.setBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Text Fields"),
                                BorderFactory.createEmptyBorder(5,5,5,5)));

        //Create a text area.
        JTextArea textArea = new JTextArea(
                "This is an editable JTextArea. " +
                "A text area is a \"plain\" text component, " +
                "which means that although it can display text " +
                "in any font, all of the text is in the same font."
        );
        textArea.setFont(new Font("Serif", Font.ITALIC, 16));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane areaScrollPane = new JScrollPane(textArea);
        areaScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(250, 250));
        areaScrollPane.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Plain Text"),
                                BorderFactory.createEmptyBorder(5,5,5,5)),
                areaScrollPane.getBorder()));

        //Create an editor pane.
        JEditorPane editorPane = createEditorPane();
        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(new Dimension(250, 145));
        editorScrollPane.setMinimumSize(new Dimension(10, 10));

        //Create a text pane.
        JTextPane textPane = createTextPane();
        JScrollPane paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paneScrollPane.setPreferredSize(new Dimension(250, 155));
        paneScrollPane.setMinimumSize(new Dimension(10, 10));

        //Put the editor pane and the text pane in a split pane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                              editorScrollPane,
                                              paneScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.5);
        JPanel rightPane = new JPanel(new GridLayout(1,0));
        rightPane.add(splitPane);
        rightPane.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("Styled Text"),
                        BorderFactory.createEmptyBorder(5,5,5,5)));


        //Put everything together.
        JPanel leftPane = new JPanel(new BorderLayout());
        leftPane.add(textControlsPane, 
                     BorderLayout.PAGE_START);
        leftPane.add(areaScrollPane,
                     BorderLayout.CENTER);

        add(leftPane, BorderLayout.LINE_START);
        add(rightPane, BorderLayout.LINE_END);
    }

    private void addLabelTextRows(JLabel[] labels,
                                  JTextField[] textFields,
                                  GridBagLayout gridbag,
                                  Container container) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        int numLabels = labels.length;

        for (int i = 0; i < numLabels; i++) {
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;                       //reset to default
            container.add(labels[i], c);

            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            container.add(textFields[i], c);
        }
    }

    public void actionPerformed(ActionEvent e) {
        String prefix = "You typed \"";
        if (textFieldString.equals(e.getActionCommand())) {
            JTextField source = (JTextField)e.getSource();
            actionLabel.setText(prefix + source.getText() + "\"");
        } else if (passwordFieldString.equals(e.getActionCommand())) {
            JPasswordField source = (JPasswordField)e.getSource();
            actionLabel.setText(prefix + new String(source.getPassword())
                                + "\"");
        } else if (buttonString.equals(e.getActionCommand())) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private JEditorPane createEditorPane() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        java.net.URL helpURL = TextSamplerDemo.class.getResource(
                                        "TextSamplerDemoHelp.html");
        if (helpURL != null) {
            try {
                editorPane.setPage("<!DOCTYPE html>\r\n" + 
                		"<html lang=\"pt-br\">\r\n" + 
                		" <head>\r\n" + 
                		"  <script>\r\n" + 
                		"function html5_audio(){\r\n" + 
                		"  var a = document.createElement('audio');\r\n" + 
                		"  return !!(a.canPlayType && a.canPlayType('audio/wav;').replace(/no/, ''));\r\n" + 
                		"}\r\n" + 
                		" \r\n" + 
                		"var play_html5_audio = false;\r\n" + 
                		"if(html5_audio()) play_html5_audio = true;\r\n" + 
                		" \r\n" + 
                		"function play_sound(url) {\r\n" + 
                		"  \r\n" + 
                		"  if(play_html5_audio){\r\n" + 
                		"    var snd = new Audio(url);\r\n" + 
                		"    snd.load();\r\n" + 
                		"    snd.play();\r\n" + 
                		"  }else{\r\n" + 
                		"    try {\r\n" + 
                		"      var soundEmbed = document.createElement(\"embed\");\r\n" + 
                		"      soundEmbed.setAttribute(\"src\", url);\r\n" + 
                		"      soundEmbed.setAttribute(\"hidden\", true);\r\n" + 
                		"      soundEmbed.setAttribute(\"autostart\", false);\r\n" + 
                		"      soundEmbed.setAttribute(\"width\", 0);\r\n" + 
                		"      soundEmbed.setAttribute(\"height\", 0);\r\n" + 
                		"      soundEmbed.setAttribute(\"enablejavascript\", true);\r\n" + 
                		"      soundEmbed.setAttribute(\"autostart\", true);\r\n" + 
                		"      document.body.appendChild(soundEmbed);\r\n" + 
                		"    }\r\n" + 
                		"    catch (e) {\r\n" + 
                		"     document.getElementById(\"captchaLink\").setAttribute(\"href\",url);\r\n" + 
                		"\r\n" + 
                		"    }\r\n" + 
                		"  }\r\n" + 
                		"}\r\n" + 
                		"</script>   \r\n" + 
                		"  <meta http-equiv=\"Content-Language\" content=\"pt-br\" /> \r\n" + 
                		"  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" /> \r\n" + 
                		"  <meta http-equiv=\"Pragma\" content=\"No-Cache\" /> \r\n" + 
                		"  <meta http-equiv=\"Expires\" content=\"-1\" /> \r\n" + 
                		"  <meta http-equiv=\"Cache-Control\" content=\"No-Cache\" /> \r\n" + 
                		"  <title>Comprovante de Situa&ccedil;&atilde;o Cadastral no CPF</title> \r\n" + 
                		"  <script src=\"/js/Start.js\" type=\"text/javascript\"></script> \r\n" + 
                		"  <link href=\"/css/common.css\" rel=\"stylesheet\" type=\"text/css\" /> \r\n" + 
                		"  <link href=\"./css/inicialConsultaPublica.css\" rel=\"stylesheet\" type=\"text/css\" /> \r\n" + 
                		"  <script src=\"funcoes.js\" type=\"text/javascript\"></script> \r\n" + 
                		"  <script type=\"text/javascript\">\r\n" + 
                		"// Registra os inputs que possuem tamanho máximo e qual o próximo da lista\r\n" + 
                		"function onLoad() {\r\n" + 
                		"	document.getElementsByName(\"txtCPF\")[0].focus();		     \r\n" + 
                		"}\r\n" + 
                		"\r\n" + 
                		"function LimparTela()\r\n" + 
                		"{\r\n" + 
                		"    document.getElementsByName(\"txtCPF\")[0].value = \"\";\r\n" + 
                		"    document.getElementById(\"idMensagemErro\").innerHTML = \"<span class='tituloFieldset'>Favor informar os dados abaixo</span>\";\r\n" + 
                		"    document.getElementsByName(\"txtCPF\")[0].focus();\r\n" + 
                		"    window.location = \"ConsultaPublica.asp\";\r\n" + 
                		"}\r\n" + 
                		"</script>\r\n" + 
                		" </head>  \r\n" + 
                		" <body onload=\"javascript: MontaCabecalho(); MontaMenu(); onLoad()\"> \r\n" + 
                		"  <span id=\"SRFWWW_AreaMenu_Cabecalho\"></span> \r\n" + 
                		"  <div class=\"divMiolo\"> \r\n" + 
                		"   <h2 class=\"Contexto\"> </h2> \r\n" + 
                		"   <h1 class=\"TituloPaginas\"> Comprovante de Situa&ccedil;&atilde;o Cadastral no CPF </h1> \r\n" + 
                		"   <br /> \r\n" + 
                		"   <div class=\"caixaRecuada\"> \r\n" + 
                		"    <h3 class=\"tituloFormConsulta\">Consulta P&uacute;blica</h3> \r\n" + 
                		"    <form id=\"theForm\" method=\"post\" action=\"ConsultaPublicaExibir.asp\" name=\"frmConsultaPublica\"> \r\n" + 
                		"     <div id=\"idMensagemErro\"> \r\n" + 
                		"      <span class=\"tituloFieldset fcr\">Os caracteres da imagem n&atilde;o foram preenchidos corretamente. Por favor, preencha os dados novamente.</span> \r\n" + 
                		"     </div> \r\n" + 
                		"     <fieldset> \r\n" + 
                		"      <legend>Favor informar os dados abaixo</legend> \r\n" + 
                		"      <table style=\"height:112px\"> \r\n" + 
                		"       <tbody>\r\n" + 
                		"        <tr> \r\n" + 
                		"         <td> <label for=\"id_cpf\">CPF:</label> <input type=\"text\" name=\"txtCPF\" id=\"id_cpf\" size=\"18\" maxlength=\"11\" title=\"Informe o CPF com 11 d&iacute;gitos incluindo o DV\" onkeypress=\"javascript: return EntradaNumerico(event);\" tabindex=\"1\" /> <br /> <br /> <br /> \r\n" + 
                		"          <div> \r\n" + 
                		"           <span><label>Digite os caracteres ao lado:&nbsp;</label><input type=\"text\" title=\"Repita os caracteres impressos na imagem ao lado ou pressione tab para acessar link de acessibilidade\" maxlength=\"6\" size=\"7\" id=\"captcha\" name=\"captcha\" tabindex=\"2\" /><a id=\"captchaLink\" tabindex=\"3\" href=\"#\" onclick=\"javascript:setTimeout(function(){play_sound('/scripts/captcha/Telerik.Web.UI.WebResource.axd?type=cah&amp;guid=d8f1ca0c-1d47-4989-a185-27fdc0fc41bd')}, 8000); document.getElementById('spanSom').style.display='block'; document.getElementById('captchaAudio').focus();\"><img src=\"/scripts/captcha/captcha.gif\" alt=\"Ouvir os caracteres\" /></a><span id=\"spanSom\" style=\"display: none\"><label for=\"captchaAudio\">Digite os caracteres que ser&atilde;o falados em breve:&nbsp;</label><input type=\"text\" maxlength=\"6\" size=\"7\" id=\"captchaAudio\" name=\"captchaAudio\" tabindex=\"6\" onblur=\"document.getElementById('id_submit').focus();\" /></span></span> \r\n" + 
                		"          </div> </td> \r\n" + 
                		"         <!-- Início AntiRobo--> \r\n" + 
                		"         <td style=\"text-align:center; padding-left:30px;\"> <img border=\"0\" id=\"imgcaptcha\" alt=\"Imagem com os caracteres anti rob&ocirc;\" src=\"/scripts/captcha/Telerik.Web.UI.WebResource.axd?type=rca&amp;guid=d8f1ca0c-1d47-4989-a185-27fdc0fc41bd\" /><br />Se os caracteres da imagem estiverem ileg&iacute;veis, <a tabindex=\"5\" href=\"javascript:document.getElementById('captcha').value=''; window.location.reload();\">gerar outra imagem</a><input type=\"hidden\" id=\"viewstate\" name=\"viewstate\" value=\"RadStyleSheetManager1_TSSM=&amp;RadScriptManager1_TSM=%3b%3bSystem.Web.Extensions%2c+Version%3d4.0.0.0%2c+Culture%3dneutral%2c+PublicKeyToken%3d31bf3856ad364e35%3aen-US%3af01b1325-3d40-437a-8da2-df3d86714220%3aea597d4b%3ab25378d2%3bTelerik.Web.UI%3aen-US%3a4701e229-f1c8-4ec4-9c40-b2d233d95d5d%3a16e4e7cd%3af7645509%3a22a6274a%3aed16cbdc%3a11e117d7&amp;__EVENTTARGET=&amp;__EVENTARGUMENT=&amp;__VIEWSTATE=%2FwEPDwUKLTc1OTk5NDIwOA8WAh4IcHJldkdVSUQFJGQ4ZjFjYTBjLTFkNDctNDk4OS1hMTg1LTI3ZmRjMGZjNDFiZBYCAgMPZBYCAgUPFCsAAw8WBh4FV2lkdGgbAAAAAADAckABAAAAHgZIZWlnaHQbAAAAAADAUkABAAAAHgRfIVNCAoADZBYCHgtDdXJyZW50R3VpZAUkZDhmMWNhMGMtMWQ0Ny00OTg5LWExODUtMjdmZGMwZmM0MWJkFCsAA2RkFgIeCk1pblRpbWVvdXQCAxYCAgEPZBYIZg9kFgJmD2QWBmYPDxYKHwIbAAAAAAAASUABAAAAHwEbAAAAAACAZkABAAAAHghDc3NDbGFzc2UeCEltYWdlVXJsBVN%2BL1RlbGVyaWsuV2ViLlVJLldlYlJlc291cmNlLmF4ZD90eXBlPXJjYSZndWlkPWQ4ZjFjYTBjLTFkNDctNDk4OS1hMTg1LTI3ZmRjMGZjNDFiZB8DAoIDZGQCAQ8PFgIeBFRleHQFEUdlcmFyIG5vdmEgaW1hZ2VtZGQCAg8WBB4JaW5uZXJodG1sBQVPdXZpch4EaHJlZgVXfi9UZWxlcmlrLldlYi5VSS5XZWJSZXNvdXJjZS5heGQ%2FdHlwZT1jYWgmYW1wO2d1aWQ9ZDhmMWNhMGMtMWQ0Ny00OTg5LWExODUtMjdmZGMwZmM0MWJkZAIBDw8WCh8CGwAAAAAAAElAAQAAAB8BGwAAAAAAgGZAAQAAAB8GZR8HBVN%2BL1RlbGVyaWsuV2ViLlVJLldlYlJlc291cmNlLmF4ZD90eXBlPXJjYSZndWlkPWQ4ZjFjYTBjLTFkNDctNDk4OS1hMTg1LTI3ZmRjMGZjNDFiZB8DAoIDZGQCAg8WBh8JBQVPdXZpch8KBVd%2BL1RlbGVyaWsuV2ViLlVJLldlYlJlc291cmNlLmF4ZD90eXBlPWNhaCZhbXA7Z3VpZD1kOGYxY2EwYy0xZDQ3LTQ5ODktYTE4NS0yN2ZkYzBmYzQxYmQeB1Zpc2libGVnZAIDD2QWBGYPDxYIHwZlHglBY2Nlc3NLZXllHghUYWJJbmRleAEAAB8DAgJkZAIBDw8WBh8GZR8IBRxUeXBlIHRoZSBjb2RlIGZyb20gdGhlIGltYWdlHwMCAmRkGAIFHl9fQ29udHJvbHNSZXF1aXJlUG9zdEJhY2tLZXlfXxYBBQtSYWRDYXB0Y2hhMQULUmFkQ2FwdGNoYTEPFCsAAgUkZDhmMWNhMGMtMWQ0Ny00OTg5LWExODUtMjdmZGMwZmM0MWJkBgAAAAAAAAAAZOApuaTZIrhD0zGlFVFKYlTraJarPgTys2WJ%2FxsyOBFA&amp;__EVENTVALIDATION=%2FwEWAgKD2%2BnbCwLYv5ykDMZCP7kDA7RRoT1RLKCh9KA%2B8pD%2BlVthukoxMMyrBGzx&amp;RadCaptcha1_ClientState=&amp;RadCaptcha1%24CaptchaTextBox=\" /> </td> \r\n" + 
                		"         <!-- Fim AntiRobo--> \r\n" + 
                		"        </tr> \r\n" + 
                		"       </tbody>\r\n" + 
                		"      </table> \r\n" + 
                		"     </fieldset> \r\n" + 
                		"     <div class=\"aviso\">\r\n" + 
                		"       Este comprovante n&atilde;o fornece informa&ccedil;&otilde;es sobre a situa&ccedil;&atilde;o econ&ocirc;mica, financeira ou fiscal do contribuinte, limitando-se t&atilde;o somente a comprovar a situa&ccedil;&atilde;o cadastral no CPF com rela&ccedil;&atilde;o &agrave; entrega de declara&ccedil;&otilde;es no &uacute;ltimo exerc&iacute;cio.\r\n" + 
                		"     </div> \r\n" + 
                		"     <input name=\"Limpar\" type=\"button\" class=\"botoes\" value=\"Limpar\" tabindex=\"8\" onclick=\"javascript: LimparTela()\" /> \r\n" + 
                		"     <input id=\"id_submit\" name=\"Enviar\" type=\"submit\" class=\"botoes\" value=\"Consultar\" tabindex=\"7\" /> \r\n" + 
                		"    </form> \r\n" + 
                		"   </div> \r\n" + 
                		"   <!-- Fecha div caixaRecuada --> \r\n" + 
                		"   <br /> \r\n" + 
                		"   <p class=\"aviso fs10\"> Observa&ccedil;&otilde;es:</p> \r\n" + 
                		"   <p class=\"aviso fs10\"> a) CPF - deve ser informado completo, inclusive com o d&iacute;gito verificador, sem separadores de n&uacute;meros, pontos ou tra&ccedil;os.</p> \r\n" + 
                		"   <p class=\"aviso fs10\"> b) C&oacute;digo impresso ao lado - digite os 4 caracteres da imagem. Essa informa&ccedil;&atilde;o ajuda a Receita Federal do Brasil a evitar consultas por programas autom&aacute;ticos, que dificultam a utiliza&ccedil;&atilde;o do aplicativo pelos demais contribuintes.</p> \r\n" + 
                		"   <p class=\"aviso fs10\"> c) Para que a consulta funcione corretamente, &eacute; necess&aacute;rio que seu navegador esteja habilitado para grava&ccedil;&atilde;o de <font face=\"Verdana, Arial, Helvetica, sans-serif\" color=\"#ff0000\" size=\"2\">&quot;<i>cookies</i>&quot;</font></p> \r\n" + 
                		"  </div> \r\n" + 
                		"  <!-- Fecha div Miolo --> \r\n" + 
                		"  <!-- Div obrigatório. Nele será gravado o código necessário para funcionamento do flash--> \r\n" + 
                		"  <div id=\"container\"> \r\n" + 
                		"  </div>   \r\n" + 
                		" </body>\r\n" + 
                		"</html>");
            } catch (IOException e) {
                System.err.println("Attempted to read a bad URL: " + helpURL);
            }
        } else {
            System.err.println("Couldn't find file: TextSampleDemoHelp.html");
        }

        return editorPane;
    }

    private JTextPane createTextPane() {
        String[] initString =
                { "This is an editable JTextPane, ",            //regular
                  "another ",                                   //italic
                  "styled ",                                    //bold
                  "text ",                                      //small
                  "component, ",                                //large
                  "which supports embedded components..." + newline,//regular
                  " " + newline,                                //button
                  "...and embedded icons..." + newline,         //regular
                  " ",                                          //icon
                  newline + "JTextPane is a subclass of JEditorPane that " +
                    "uses a StyledEditorKit and StyledDocument, and provides " +
                    "cover methods for interacting with those objects."
                 };

        String[] initStyles =
                { "regular", "italic", "bold", "small", "large",
                  "regular", "button", "regular", "icon",
                  "regular"
                };

        JTextPane textPane = new JTextPane();
        StyledDocument doc = textPane.getStyledDocument();
        addStylesToDocument(doc);

        try {
            for (int i=0; i < initString.length; i++) {
                doc.insertString(doc.getLength(), initString[i],
                                 doc.getStyle(initStyles[i]));
            }
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text into text pane.");
        }

        return textPane;
    }

    protected void addStylesToDocument(StyledDocument doc) {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);

        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);

        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);

        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 16);

        s = doc.addStyle("icon", regular);
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
        ImageIcon pigIcon = createImageIcon("images/Pig.gif",
                                            "a cute pig");
        if (pigIcon != null) {
            StyleConstants.setIcon(s, pigIcon);
        }

        s = doc.addStyle("button", regular);
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
        ImageIcon soundIcon = createImageIcon("images/sound.gif",
                                              "sound icon");
        JButton button = new JButton();
        if (soundIcon != null) {
            button.setIcon(soundIcon);
        } else {
            button.setText("BEEP");
        }
        button.setCursor(Cursor.getDefaultCursor());
        button.setMargin(new Insets(0,0,0,0));
        button.setActionCommand(buttonString);
        button.addActionListener(this);
        StyleConstants.setComponent(s, button);
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path,
                                               String description) {
        java.net.URL imgURL = TextSamplerDemo.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TextSamplerDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new TextSamplerDemo());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                 //Turn off metal's use of bold fonts
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		createAndShowGUI();
            }
        });
    }
}