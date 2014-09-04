import com.google.protobuf.Message;
import com.googlecode.protobuf.format.XmlFormat;

/**
 * Data de Criação: 17/01/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class CromeXML {

	public static void main(String[] args) {
		Message someProto = 
		String xmlFormat = XmlFormat.printToString(someProto);
	}
}
