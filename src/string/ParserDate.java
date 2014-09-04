package string;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Data de Criação: 03/09/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class ParserDate {

	public static void main(String[] args) throws ParseException {

		int i = 5;
		String vFormat = String.format("%02d", 5);
		System.out.println(vFormat);

		SimpleDateFormat vSDF = new SimpleDateFormat("ddMMyyyy");
		vSDF.set
		Date vTempData = vSDF.parse("31042014");
		System.out.println("fim");

	}
}
