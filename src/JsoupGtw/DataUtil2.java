/*     */package JsoupGtw;

/*     */
/*     */import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */public class DataUtil2
/*     */{
	/* 20 */private static final Pattern charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*(?:\"|')?([^\\s,;\"']*)");
	/*     */static final String defaultCharset = "UTF-8";
	/*     */private static final int bufferSize = 131072;

	/*     */
	/*     */static String getCharsetFromContentType(String contentType)
	/*     */{
		/* 168 */if (contentType == null) {
			return null;
		}
		/* 169 */Matcher m = DataUtil2.charsetPattern.matcher(contentType);
		/* 170 */if (m.find()) {
			/* 171 */String charset = m.group(1).trim();
			/* 172 */charset = charset.replace("charset=", "");
			/* 173 */if (charset.isEmpty()) {
				return null;
			}
			/*     */try {
				/* 175 */if (Charset.isSupported(charset)) {
					return charset;
				}
				/* 176 */charset = charset.toUpperCase(Locale.ENGLISH);
				/* 177 */if (Charset.isSupported(charset)) {
					return charset;
					/*     */
				}
			}
			/*     */catch (IllegalCharsetNameException e) {
				/* 180 */return null;
				/*     */}
			/*     */}
		/* 183 */return null;
		/*     */}

	/*     */
	/*     */
	/*     */public static Document load(File in, String charsetName, String baseUri)
	/*     */throws IOException
	/*     */{
		/* 35 */FileInputStream inStream = null;
		/*     */try {
			/* 37 */inStream = new FileInputStream(in);
			/* 38 */ByteBuffer byteData = DataUtil2.readToByteBuffer(inStream);
			/* 39 */Document localDocument = DataUtil2.parseByteData(byteData, charsetName, baseUri,
					Parser.htmlParser());
			/*     */
			/* 42 */return localDocument;
			/*     */}
		/*     */finally
		/*     */{
			/* 41 */if (inStream != null) {
				/* 42 */inStream.close();
				/*     */
			}
		}
		/*     */}

	/*     */
	/*     */public static Document load(InputStream in, String charsetName, String baseUri)
	/*     */throws IOException
	/*     */{
		/* 55 */ByteBuffer byteData = DataUtil2.readToByteBuffer(in);
		/* 56 */return DataUtil2.parseByteData(byteData, charsetName, baseUri, Parser.htmlParser());
		/*     */}

	/*     */
	/*     */public static Document load(InputStream in, String charsetName, String baseUri, Parser parser)
	/*     */throws IOException
	/*     */{
		/* 69 */ByteBuffer byteData = DataUtil2.readToByteBuffer(in);
		/* 70 */return DataUtil2.parseByteData(byteData, charsetName, baseUri, parser);
		/*     */}

	/*     */
	/*     */static Document parseByteData(ByteBuffer byteData, String charsetName, String baseUri, Parser parser)
	/*     */{
		/* 77 */Document doc = null;
		/*     */String docData;
		/* 78 */if (charsetName == null)
		/*     */{
			/* 80 */docData = Charset.forName("UTF-8").decode(byteData).toString();
			/* 81 */doc = parser.parseInput(docData, baseUri);
			/* 82 */Element meta = doc.select("meta[http-equiv=content-type], meta[charset]").first();
			/* 83 */if (meta != null)
			/*     */{
				/*     */String foundCharset;
				/* 86 */if (meta.hasAttr("http-equiv")) {
					/* 87 */foundCharset = DataUtil2.getCharsetFromContentType(meta.attr("content"));
					/* 88 */if ((foundCharset == null) && (meta.hasAttr("charset"))) {
						/*     */try {
							/* 90 */if (Charset.isSupported(meta.attr("charset"))) {
								/* 91 */foundCharset = meta.attr("charset");
								/*     */
							}
						}
						/*     */catch (IllegalCharsetNameException e) {
							/* 94 */foundCharset = null;
							/*     */}
						/*     */
					}
				}
				/*     */else {
					/* 98 */foundCharset = meta.attr("charset");
					/*     */}
				/*     */
				/* 101 */if ((foundCharset != null) && (foundCharset.length() != 0)
						&& (!(foundCharset.equals("UTF-8")))) {
					/* 102 */foundCharset = foundCharset.trim().replaceAll("[\"']", "");
					/* 103 */charsetName = foundCharset;
					/* 104 */byteData.rewind();
					/* 105 */docData = Charset.forName(foundCharset).decode(byteData).toString();
					/* 106 */doc = null;
					/*     */}
				/*     */}
			/*     */} else {
			/* 110 */Validate
					.notEmpty(charsetName,
							"Must set charset arg to character set of file to parse. Set to null to attempt to detect from HTML");
			/* 111 */docData = Charset.forName(charsetName).decode(byteData).toString();
			/*     */}
		/* 113 */if (doc == null)
		/*     */{
			/* 117 */if ((docData.length() > 0) && (docData.charAt(0) == 65279)) {
				/* 118 */docData = docData.substring(1);
				/*     */}
			/* 120 */doc = parser.parseInput(docData, baseUri);
			/* 121 */doc.outputSettings().charset(charsetName);
			/*     */}
		/* 123 */return doc;
		/*     */}

	/*     */
	/*     */static ByteBuffer readToByteBuffer(InputStream inStream) throws IOException {
		/* 158 */return DataUtil2.readToByteBuffer(inStream, 0);
		/*     */}

	/*     */
	/*     */static ByteBuffer readToByteBuffer(InputStream inStream, int maxSize)
	/*     */throws IOException
	/*     */{
		/* 134 */Validate.isTrue(maxSize >= 0, "maxSize must be 0 (unlimited) or larger");
		/* 135 */boolean capped = maxSize > 0;
		/* 136 */byte[] buffer = new byte[131072];
		/* 137 */ByteArrayOutputStream outStream = new ByteArrayOutputStream(131072);
		/*     */
		/* 139 */int remaining = maxSize;
		/*     */while (true)
		/*     */{
			/* 142 */int read = inStream.read(buffer);
			/* 143 */if (read == -1) {
				break;
			}
			/* 144 */if (capped) {
				/* 145 */if (read > remaining) {
					/* 146 */outStream.write(buffer, 0, remaining);
					/* 147 */break;
					/*     */}
				/* 149 */remaining -= read;
				/*     */}
			/* 151 */outStream.write(buffer, 0, read);
			/*     */}
		/* 153 */ByteBuffer byteData = ByteBuffer.wrap(outStream.toByteArray());
		/* 154 */return byteData;
		/*     */}
}

/*
 * Location: C:\Users\jjcc\Downloads\jsoup-1.7.3.jar Qualified Name:
 * org.jsoup.helper.DataUtil JD-Core Version: 0.5.3
 */