import java.util.Date;

/**
 * Data de Criação: 23/09/2013
 * 
 * @author David José Ribeiro
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class ScrennShotUnit {

	public static void main(String[] args) {

		Date d = new Date();
		System.out.println(d.getMinutes() + ":" + d.getSeconds());
		ChromeUnit c = new ChromeUnit();
		c.launchChrome();
		c.googleScreenshot();
		c.kill();
		d = new Date();
		System.out.println(d.getMinutes() + ":" + d.getSeconds());
	}
}
