import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Data de Criação: 21/01/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class TestReflection {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, SecurityException,
			NoSuchMethodException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {

		Class[] paramString = new Class[1];
		paramString[0] = String.class;

		Class<?> clazzDocument = Class.forName("org.jsoup.nodes.Document");
		Class<?> clazzElement = Class.forName("org.jsoup.nodes.Element");
		Class<?> clazzJsoap = Class.forName("org.jsoup.Jsoup");

		Method vJsopParser = clazzJsoap.getDeclaredMethod("parse", new Class[] { String.class });

		Object vInvokedDocument = vJsopParser.invoke(null, "");

		if (vInvokedDocument instanceof Document) {
			System.out.println("Ele é um document");
		}
		if (vInvokedDocument instanceof Element) {
			System.out.println("Ele é um Element");
		}

		Document vElementReflect = (Document) vInvokedDocument;
		Class<Element> vTest = (Class<Element>) vElementReflect.getClass().getSuperclass();

		Method[] vDeclaredMethods = vTest.getDeclaredMethods();
		Method vMethodToString = vTest.getDeclaredMethod("toString", new Class[] {});
		Object vInvoke = vMethodToString.invoke(vElementReflect, null);

		if (vInvoke instanceof String) {
			System.out.println("Ele é uma string");
			System.out.println(((String) vInvoke));
		}

		System.out.println(vElementReflect.toString());
		System.out.println(vTest.toString());

		System.out.println("Pegando todos os metodos publicos");

	}
}
