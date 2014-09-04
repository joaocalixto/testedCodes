package JsoupGtw;

import java.util.Map;

import org.jsoup.Connection;

/**
 * Data de Criação: 13/03/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public interface Connection2 extends Connection {

	public abstract Connection header(Map<String, String> vMapHeader);

}
