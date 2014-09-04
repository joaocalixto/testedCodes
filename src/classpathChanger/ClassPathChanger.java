package classpathChanger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class ClassPathChanger {

	static String HOME = "java.home";
	static String CLASS_PATH = "java.class.path";
	static String JAVA_VERSION = "java.version";
	static String JDK_32 = "jdk.32";
	static String JDK_64 = "jdk.364";

	
	public static void main(String[] args) {

		showSystemStatus();
		Integer userInput = getUserInput();
		
		String newPathJDK = "";
		
		if(userInput == 32 || userInput == 64){
			if(userInput == 32){
				newPathJDK = PropertiesManager.get32JDK();
				System.out.println("32");
			} else if(userInput == 64){
				System.out.println("64");
				newPathJDK = PropertiesManager.get64JDK();
			}
			System.out.println("System.setProperty(HOME, newPathJDK);");
		}
		System.out.println("Programa finalizado.");

	}

	private static void showSystemStatus() {
		System.out.println("==================================");
		System.out.println("Systema atualmente");
		System.out.println("==================================");
		String property = System.getProperty(HOME);
		System.out.println("JAVA_HOME : " + property);
		String property3 = System.getProperty(JAVA_VERSION);
		System.out.println("JAVA_VERSION : " + property3);
		System.out.println("==================================");
	}

	private static Integer getUserInput() {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Digite o numero da arquitetura (32 ou 64) : ");
		int i = -1;
		try {
			String s = br.readLine();
			i = Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			System.err.println("Formato invalido.");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}
	
	static class PropertiesManager{
		
		private static String getPropertie(String pName){

			 
			Properties prop = new Properties();
			InputStream input = null;
			String retorno = "";
		 
			try {
		 
				input = new FileInputStream("jdk.properties");
		 
				// load a properties file
				prop.load(input);
		 
				// get the property value and print it out
				retorno= prop.getProperty(pName);
		 
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		 
		  return retorno;
		}
		
		
		public static String get32JDK(){
			return getPropertie(ClassPathChanger.JDK_32);
			
		}
		public static String get64JDK(){
			return getPropertie(ClassPathChanger.JDK_64);
		}
	}

}
