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

		// Insert left zeros
		int i = 5;
		String vFormat = String.format("%02d", 5);
		System.out.println(vFormat);

		// setLenient study
		// set liniente validate the date
		// eg. 31/02/2014 not exists so throw exception
		SimpleDateFormat vSDF = new SimpleDateFormat("ddMMyyyy");
		vSDF.setLenient(false);
		Date vTempData = vSDF.parse("31112013");
		System.out.println("fim");

	}
}
