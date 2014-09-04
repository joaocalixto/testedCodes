import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;

/**
 * Data de Criação: 03/07/2013
 * 
 * @author David José Ribeiro
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class Chrome {
	WebDriver driver;

	@Test
	public void googleScreenshot() {
		try {
			File scrnsht = ((TakesScreenshot) this.driver).getScreenshotAs(OutputType.FILE);

			FileUtils.copyFile(scrnsht, new File("c:\\google_page.png"));

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
		this.driver = new FirefoxDriver();
		this.driver.get("http://sefaznet.ac.gov.br/nfe/");
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