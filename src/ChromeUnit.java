import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * Data de Criação: 03/07/2013
 * 
 * @author David José Ribeiro
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class ChromeUnit {
	WebDriver driver;

	@Test
	public void googleScreenshot() {
		try {
			byte[] scrnsht = ((TakesScreenshot) this.driver).getScreenshotAs(OutputType.BYTES);

			String vTemp = ChromeUnit.saveFile("testePDF-TJSC", ".zip", scrnsht);
			System.out.println(vTemp);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterTest
	public void kill() {
		this.driver.close();
	}

	@BeforeMethod
	public void launchChrome() {
		System.setProperty("webdriver.chrome.driver", "C:\\JoaoCalixto\\chromedriver.exe\\chromedriver.exe");
		this.driver = new ScreenCaptureHtmlUnitDriver(BrowserVersion.getDefault());
		this.driver.get("http://www.detran.goias.gov.br/");
	}

	protected static String saveFile(String pFileName, String pFileExtension, byte[] pFileBytes) throws IOException {
		File vFile = File.createTempFile(pFileName, pFileExtension);
		// vFile.deleteOnExit();
		String vPathImagem = vFile.getAbsolutePath();

		FileOutputStream vOutputStreamHTML = null;
		try {
			vOutputStreamHTML = new FileOutputStream(vFile);
			vOutputStreamHTML.write(pFileBytes);
		} finally {
			if (vOutputStreamHTML != null) {
				vOutputStreamHTML.flush();
				vOutputStreamHTML.close();
			}
		}

		return vPathImagem;
	}
}