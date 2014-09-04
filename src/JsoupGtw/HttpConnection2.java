package JsoupGtw;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TokenQueue;

/**
 * Data de Criação: 13/03/2014
 * 
 * @author Joao Calixto
 * @since XXX_vYYYYMMa
 * @version XXX_vYYYYMMa
 */
public class HttpConnection2 implements Connection2 {

	private static abstract class Base<T extends Connection.Base> implements Connection.Base<T> {
		URL url;
		Connection.Method method;
		Map<String, String> headers;
		Map<String, String> cookies;

		private Base() {
			this.headers = new LinkedHashMap();
			this.cookies = new LinkedHashMap();
		}

		public String cookie(String name) {
			Validate.notNull(name, "Cookie name must not be null");
			return (this.cookies.get(name));
		}

		public T cookie(String name, String value) {
			Validate.notEmpty(name, "Cookie name must not be empty");
			Validate.notNull(value, "Cookie value must not be null");
			this.cookies.put(name, value);
			return (T) this;
		}

		public Map<String, String> cookies() {
			return this.cookies;
		}

		private String getHeaderCaseInsensitive(String name) {
			Validate.notNull(name, "Header name must not be null");

			String value = this.headers.get(name);
			if (value == null) {
				value = this.headers.get(name.toLowerCase());
			}
			if (value == null) {
				Map.Entry entry = this.scanHeaders(name);
				if (entry != null) {
					value = (String) entry.getValue();
				}
			}
			return value;
		}

		public boolean hasCookie(String name) {
			Validate.notEmpty("Cookie name must not be empty");
			return this.cookies.containsKey(name);
		}

		public boolean hasHeader(String name) {
			Validate.notEmpty(name, "Header name must not be empty");
			return (this.getHeaderCaseInsensitive(name) != null);
		}

		public String header(String name) {
			Validate.notNull(name, "Header name must not be null");
			return this.getHeaderCaseInsensitive(name);
		}

		public T header(String name, String value) {
			Validate.notEmpty(name, "Header name must not be empty");
			Validate.notNull(value, "Header value must not be null");
			this.removeHeader(name);
			this.headers.put(name, value);
			return (T) this;
		}

		public Map<String, String> headers() {
			return this.headers;
		}

		public Connection2.Method method() {
			return this.method;
		}

		public T method(Connection2.Method method) {
			Validate.notNull(method, "Method must not be null");
			this.method = method;
			return (T) this;
		}

		public T removeCookie(String name) {
			Validate.notEmpty("Cookie name must not be empty");
			this.cookies.remove(name);
			return (T) this;
		}

		public T removeHeader(String name) {
			Validate.notEmpty(name, "Header name must not be empty");
			Map.Entry entry = this.scanHeaders(name);
			if (entry != null) {
				this.headers.remove(entry.getKey());
			}
			return (T) this;
		}

		private Map.Entry<String, String> scanHeaders(String name) {
			String lc = name.toLowerCase();
			for (Map.Entry entry : this.headers.entrySet()) {
				if (((String) entry.getKey()).toLowerCase().equals(lc)) {
					return entry;
				}
			}
			return null;
		}

		public URL url() {
			return this.url;
		}

		public T url(URL url) {
			Validate.notNull(url, "URL must not be null");
			this.url = url;
			return (T) this;
		}
	}

	public static class KeyVal implements Connection2.KeyVal {
		private String key;
		private String value;

		private KeyVal(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String key() {
			return this.key;
		}

		public KeyVal key(String key) {
			Validate.notEmpty(key, "Data key must not be empty");
			this.key = key;
			return this;
		}

		@Override
		public String toString() {
			return this.key + "=" + this.value;
		}

		public String value() {
			return this.value;
		}

		public KeyVal value(String value) {
			Validate.notNull(value, "Data value must not be null");
			this.value = value;
			return this;
		}

		public static KeyVal create(String key, String value) {
			Validate.notEmpty(key, "Data key must not be empty");
			Validate.notNull(value, "Data value must not be null");
			return new KeyVal(key, value);
		}
	}

	public static class Request2 extends HttpConnection2.Base<Connection2.Request> implements Connection2.Request {
		private int timeoutMilliseconds;
		private int maxBodySizeBytes;
		private boolean followRedirects;
		private Collection<Connection2.KeyVal> data;
		private boolean ignoreHttpErrors = false;
		private boolean ignoreContentType = false;
		private Parser parser;

		private Request2() {
			super();
			this.timeoutMilliseconds = 3000;
			this.maxBodySizeBytes = 1048576;
			this.followRedirects = true;
			this.data = new ArrayList();
			this.method = Connection2.Method.GET;
			this.headers.put("Accept-Encoding", "gzip");
			this.parser = Parser.htmlParser();
		}

		public Collection<Connection2.KeyVal> data() {
			return this.data;
		}

		public Request data(Connection2.KeyVal keyval) {
			Validate.notNull(keyval, "Key val must not be null");
			this.data.add(keyval);
			return this;
		}

		public boolean followRedirects() {
			return this.followRedirects;
		}

		public Connection2.Request followRedirects(boolean followRedirects) {
			this.followRedirects = followRedirects;
			return this;
		}

		public boolean ignoreContentType() {
			return this.ignoreContentType;
		}

		public Connection2.Request ignoreContentType(boolean ignoreContentType) {
			this.ignoreContentType = ignoreContentType;
			return this;
		}

		public boolean ignoreHttpErrors() {
			return this.ignoreHttpErrors;
		}

		public Connection2.Request ignoreHttpErrors(boolean ignoreHttpErrors) {
			this.ignoreHttpErrors = ignoreHttpErrors;
			return this;
		}

		public int maxBodySize() {
			return this.maxBodySizeBytes;
		}

		public Connection2.Request maxBodySize(int bytes) {
			Validate.isTrue(bytes >= 0, "maxSize must be 0 (unlimited) or larger");
			this.maxBodySizeBytes = bytes;
			return this;
		}

		public Parser parser() {
			return this.parser;
		}

		public Request parser(Parser parser) {
			this.parser = parser;
			return this;
		}

		public int timeout() {
			return this.timeoutMilliseconds;
		}

		public Request timeout(int millis) {
			Validate.isTrue(millis >= 0, "Timeout milliseconds must be 0 (infinite) or greater");
			this.timeoutMilliseconds = millis;
			return this;
		}

	}

	public static class Response2 extends HttpConnection2.Base<Connection2.Response> implements Connection2.Response {
		private static final int MAX_REDIRECTS = 20;
		private int statusCode;
		private String statusMessage;
		private ByteBuffer byteData;
		private String charset;
		private String contentType;
		private boolean executed = false;
		private int numRedirects = 0;
		private Connection2.Request req;

		Response2() {
			super();
		}

		private Response2(Response2 previousResponse) throws IOException {
			super();
			if (previousResponse != null) {
				this.numRedirects = (previousResponse.numRedirects + 1);
				if (this.numRedirects >= 20) {
					throw new IOException(String.format("Too many redirects occurred trying to load URL %s",
							new Object[] { previousResponse.url() }));
				}
			}
		}

		public String body() {
			Validate.isTrue(this.executed,
					"Request must be executed (with .execute(), .get(), or .post() before getting response body");
			String body;
			if (this.charset == null) {
				body = Charset.forName("UTF-8").decode(this.byteData).toString();
			} else {
				body = Charset.forName(this.charset).decode(this.byteData).toString();
			}
			this.byteData.rewind();
			return body;
		}

		public byte[] bodyAsBytes() {
			Validate.isTrue(this.executed,
					"Request must be executed (with .execute(), .get(), or .post() before getting response body");
			return this.byteData.array();
		}

		public String charset() {
			return this.charset;
		}

		public String contentType() {
			return this.contentType;
		}

		public Document parse() throws IOException {
			Validate.isTrue(this.executed,
					"Request must be executed (with .execute(), .get(), or .post() before parsing response");
			Document doc = DataUtil2.parseByteData(this.byteData, this.charset, this.url.toExternalForm(),
					this.req.parser());
			this.byteData.rewind();
			this.charset = doc.outputSettings().charset().name();
			return doc;
		}

		void processResponseHeaders(Map<String, List<String>> resHeaders) {
			for (Map.Entry entry : resHeaders.entrySet()) {
				String name = (String) entry.getKey();
				if (name == null) {
					continue;
				}
				List<String> values = (List<String>) entry.getValue();
				if (name.equalsIgnoreCase("Set-Cookie")) {
					for (String value : values) {
						if (value == null) {
							continue;
						}
						TokenQueue cd = new TokenQueue(value);
						String cookieName = cd.chompTo("=").trim();
						String cookieVal = cd.consumeTo(";").trim();
						if (cookieVal == null) {
							cookieVal = "";
						}

						if ((cookieName != null) && (cookieName.length() > 0)) {
							this.cookie(cookieName, cookieVal);
						}
					}
				} else if (!(values.isEmpty())) {
					this.header(name, values.get(0));
				}
			}
		}

		private void setupFromConnection(HttpURLConnection conn, Connection2.Response previousResponse)
				throws IOException {
			this.method = Connection2.Method.valueOf(conn.getRequestMethod());
			this.url = conn.getURL();
			this.statusCode = conn.getResponseCode();
			this.statusMessage = conn.getResponseMessage();
			this.contentType = conn.getContentType();

			Map resHeaders = conn.getHeaderFields();
			this.processResponseHeaders(resHeaders);

			if (previousResponse != null) {
				for (Map.Entry prevCookie : previousResponse.cookies().entrySet()) {
					if (!(this.hasCookie((String) prevCookie.getKey()))) {
						this.cookie((String) prevCookie.getKey(), (String) prevCookie.getValue());
					}
				}
			}
		}

		public int statusCode() {
			return this.statusCode;
		}

		public String statusMessage() {
			return this.statusMessage;
		}

		private static HttpURLConnection createConnection(Connection2.Request req) throws IOException {
			HttpURLConnection conn = (HttpURLConnection) req.url().openConnection();
			conn.setRequestMethod(req.method().name());
			conn.setInstanceFollowRedirects(false);
			conn.setConnectTimeout(req.timeout());
			conn.setReadTimeout(req.timeout());
			if (req.method() == Connection2.Method.POST) {
				conn.setDoOutput(true);
			}
			if (req.cookies().size() > 0) {
				conn.addRequestProperty("Cookie", Response2.getRequestCookieString(req));
			}
			for (Map.Entry header : req.headers().entrySet()) {
				conn.addRequestProperty((String) header.getKey(), (String) header.getValue());
			}
			return conn;
		}

		static Response2 execute(Connection2.Request req) throws IOException {
			return Response2.execute(req, null);
		}

		static Response2 execute(Connection2.Request req, Response2 previousResponse) throws IOException {
			Validate.notNull(req, "Request must not be null");
			String protocol = req.url().getProtocol();
			if ((!(protocol.equals("http"))) && (!(protocol.equals("https")))) {
				throw new MalformedURLException("Only http & https protocols supported");
			}

			if ((req.method() == Connection2.Method.GET) && (req.data().size() > 0)) {
				Response2.serialiseRequestUrl(req);
			}
			HttpURLConnection conn = Response2.createConnection(req);
			Response2 res;
			try {
				conn.connect();
				if (req.method() == Connection.Method.POST) {
					Response2.writePost(req.data(), conn.getOutputStream());
				}
				int status = conn.getResponseCode();
				boolean needsRedirect = false;
				if (status != 200) {
					if ((status == 302) || (status == 301) || (status == 303)) {
						needsRedirect = true;
					} else if (!(req.ignoreHttpErrors())) {
						throw new HttpStatusException("HTTP error fetching URL", status, req.url().toString());
					}
				}
				res = new Response2(previousResponse);
				res.setupFromConnection(conn, previousResponse);
				if ((needsRedirect) && (req.followRedirects())) {
					req.method(Connection2.Method.GET);
					req.data().clear();

					String location = res.header("Location");
					if ((location != null) && (location.startsWith("http:/")) && (location.charAt(6) != '/')) {
						location = location.substring(6);
					}
					req.url(new URL(req.url(), HttpConnection2.encodeUrl(location)));

					for (Map.Entry cookie : res.cookies.entrySet()) {
						req.cookie((String) cookie.getKey(), (String) cookie.getValue());
					}
					return Response2.execute(req, res);
				}
				res.req = req;

				String contentType = res.contentType();
				if ((contentType != null) && (!(req.ignoreContentType())) && (!(contentType.startsWith("text/")))
						&& (!(contentType.startsWith("application/xml")))
						&& (!(contentType.startsWith("application/xhtml+xml")))) {
					throw new UnsupportedMimeTypeException(
							"Unhandled content type. Must be text/*, application/xml, or application/xhtml+xml",
							contentType, req.url().toString());
				}

				InputStream bodyStream = null;
				InputStream dataStream = null;
				try {
					dataStream = (conn.getErrorStream() != null) ? conn.getErrorStream() : conn.getInputStream();
					bodyStream = new BufferedInputStream(dataStream);

					res.byteData = DataUtil2.readToByteBuffer(bodyStream, req.maxBodySize());
					res.charset = DataUtil2.getCharsetFromContentType(res.contentType);
				} finally {
					if (bodyStream != null) {
						bodyStream.close();
					}
					if (dataStream != null) {
						dataStream.close();
					}
				}
			} finally {
				conn.disconnect();
			}

			res.executed = true;
			return res;
		}

		private static String getRequestCookieString(Connection2.Request req) {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Map.Entry cookie : req.cookies().entrySet()) {
				if (!(first)) {
					sb.append("; ");
				} else {
					first = false;
				}
				sb.append((String) cookie.getKey()).append('=').append((String) cookie.getValue());
			}

			return sb.toString();
		}

		private static void serialiseRequestUrl(Connection2.Request req) throws IOException {
			URL in = req.url();
			StringBuilder url = new StringBuilder();
			boolean first = true;

			url.append(in.getProtocol()).append("://").append(in.getAuthority()).append(in.getPath()).append("?");

			if (in.getQuery() != null) {
				url.append(in.getQuery());
				first = false;
			}
			for (Connection2.KeyVal keyVal : req.data()) {
				if (!(first)) {
					url.append('&');
				} else {
					first = false;
				}
				url.append(URLEncoder.encode(keyVal.key(), "UTF-8")).append('=')
						.append(URLEncoder.encode(keyVal.value(), "UTF-8"));
			}

			req.url(new URL(url.toString()));
			req.data().clear();
		}

		private static void writePost(Collection<Connection2.KeyVal> data, OutputStream outputStream)
				throws IOException {
			OutputStreamWriter w = new OutputStreamWriter(outputStream, "UTF-8");
			boolean first = true;
			for (Connection2.KeyVal keyVal : data) {
				if (!(first)) {
					w.append('&');
				} else {
					first = false;
				}
				w.write(URLEncoder.encode(keyVal.key(), "UTF-8"));
				w.write(61);
				w.write(URLEncoder.encode(keyVal.value(), "UTF-8"));
			}
			w.close();
		}
	}

	private Connection2.Request req;

	private Connection2.Response res;

	private HttpConnection2() {
		this.req = new Request2();
		this.res = new Response2();
	}

	public Connection2 cookie(String name, String value) {
		this.req.cookie(name, value);
		return this;
	}

	public Connection2 cookies(Map<String, String> cookies) {
		Validate.notNull(cookies, "Cookie map must not be null");
		for (Map.Entry entry : cookies.entrySet()) {
			this.req.cookie((String) entry.getKey(), (String) entry.getValue());
		}
		return this;
	}

	public Connection2 data(Collection<Connection2.KeyVal> data) {
		Validate.notNull(data, "Data collection must not be null");
		for (Connection2.KeyVal entry : data) {
			this.req.data(entry);
		}
		return this;
	}

	public Connection2 data(Map<String, String> data) {
		Validate.notNull(data, "Data map must not be null");
		for (Map.Entry entry : data.entrySet()) {
			this.req.data(KeyVal.create((String) entry.getKey(), (String) entry.getValue()));
		}
		return this;
	}

	public Connection2 data(String key, String value) {
		this.req.data(KeyVal.create(key, value));
		return this;
	}

	public Connection2 data(String[] keyvals) {
		Validate.notNull(keyvals, "Data key value pairs must not be null");
		Validate.isTrue((keyvals.length % 2) == 0, "Must supply an even number of key value pairs");
		for (int i = 0; i < keyvals.length; i += 2) {
			String key = keyvals[i];
			String value = keyvals[(i + 1)];
			Validate.notEmpty(key, "Data key must not be empty");
			Validate.notNull(value, "Data value must not be null");
			this.req.data(KeyVal.create(key, value));
		}
		return this;
	}

	public Connection2.Response execute() throws IOException {
		this.res = Response2.execute(this.req);
		return this.res;
	}

	public Connection2 followRedirects(boolean followRedirects) {
		this.req.followRedirects(followRedirects);
		return this;
	}

	public Document get() throws IOException {
		this.req.method(Connection2.Method.GET);
		this.execute();
		return this.res.parse();
	}

	public Connection2 header(Map<String, String> header) {
		Validate.notNull(header, "Header map must not be null");
		for (Map.Entry entry : header.entrySet()) {
			this.req.header((String) entry.getKey(), (String) entry.getValue());
		}
		return this;
	}

	public Connection2 header(String name, String value) {
		this.req.header(name, value);
		return this;
	}

	public Connection2 ignoreContentType(boolean ignoreContentType) {
		this.req.ignoreContentType(ignoreContentType);
		return this;
	}

	public Connection2 ignoreHttpErrors(boolean ignoreHttpErrors) {
		this.req.ignoreHttpErrors(ignoreHttpErrors);
		return this;
	}

	public Connection2 maxBodySize(int bytes) {
		this.req.maxBodySize(bytes);
		return this;
	}

	public Connection2 method(Connection2.Method method) {
		this.req.method(method);
		return this;
	}

	public Connection2 parser(Parser parser) {
		this.req.parser(parser);
		return this;
	}

	public Document post() throws IOException {
		this.req.method(Connection2.Method.POST);
		this.execute();
		return this.res.parse();
	}

	public Connection2 referrer(String referrer) {
		Validate.notNull(referrer, "Referrer must not be null");
		this.req.header("Referer", referrer);
		return this;
	}

	public Connection2.Request request() {
		return this.req;
	}

	public Connection2 request(Connection2.Request request) {
		this.req = request;
		return this;
	}

	public Connection2.Response response() {
		return this.res;
	}

	public Connection2 response(Connection2.Response response) {
		this.res = response;
		return this;
	}

	public Connection2 timeout(int millis) {
		this.req.timeout(millis);
		return this;
	}

	public Connection2 url(String url) {
		Validate.notEmpty(url, "Must supply a valid URL");
		try {
			this.req.url(new URL(HttpConnection2.encodeUrl(url)));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Malformed URL: " + url, e);
		}
		return this;
	}

	public Connection2 url(URL url) {
		this.req.url(url);
		return this;
	}

	public Connection2 userAgent(String userAgent) {
		Validate.notNull(userAgent, "User agent must not be null");
		this.req.header("User-Agent", userAgent);
		return this;
	}

	public static Connection2 connect(String url) {
		Connection2 con = new HttpConnection2();
		con.url(url);
		return con;
	}

	public static Connection2 connect(URL url) {
		Connection2 con = new HttpConnection2();
		con.url(url);
		return con;
	}

	private static String encodeUrl(String url) {
		if (url == null) {
			return null;
		}
		return url.replaceAll(" ", "%20");
	}

}
