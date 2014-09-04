/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ import java.sql.Connection;
/*     */ import java.sql.DriverManager;
/*     */ import java.sql.DriverPropertyInfo;
/*     */ import java.sql.SQLException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class Driver
/*     */   implements java.sql.Driver
/*     */ {
/*  52 */   private static String driverPrefix = "jdbc:jtds:";
/*     */   static final int MAJOR_VERSION = 1;
/*     */   static final int MINOR_VERSION = 2;
/*     */   static final String MISC_VERSION = ".6";
/*  60 */   public static final boolean JDBC3 = "1.4".compareTo(System.getProperty("java.specification.version")) <= 0;
/*     */   public static final int TDS42 = 1;
/*     */   public static final int TDS50 = 2;
/*     */   public static final int TDS70 = 3;
/*     */   public static final int TDS80 = 4;
/*     */   public static final int TDS81 = 5;
/*     */   public static final int SQLSERVER = 1;
/*     */   public static final int SYBASE = 2;
/*     */   public static final String APPNAME = "prop.appname";
/*     */   public static final String AUTOCOMMIT = "prop.autocommit";
/*     */   public static final String BATCHSIZE = "prop.batchsize";
/*     */   public static final String BINDADDRESS = "prop.bindaddress";
/*     */   public static final String BUFFERDIR = "prop.bufferdir";
/*     */   public static final String BUFFERMAXMEMORY = "prop.buffermaxmemory";
/*     */   public static final String BUFFERMINPACKETS = "prop.bufferminpackets";
/*     */   public static final String CACHEMETA = "prop.cachemetadata";
/*     */   public static final String CHARSET = "prop.charset";
/*     */   public static final String DATABASENAME = "prop.databasename";
/*     */   public static final String DOMAIN = "prop.domain";
/*     */   public static final String INSTANCE = "prop.instance";
/*     */   public static final String LANGUAGE = "prop.language";
/*     */   public static final String LASTUPDATECOUNT = "prop.lastupdatecount";
/*     */   public static final String LOBBUFFER = "prop.lobbuffer";
/*     */   public static final String LOGFILE = "prop.logfile";
/*     */   public static final String LOGINTIMEOUT = "prop.logintimeout";
/*     */   public static final String MACADDRESS = "prop.macaddress";
/*     */   public static final String MAXSTATEMENTS = "prop.maxstatements";
/*     */   public static final String NAMEDPIPE = "prop.namedpipe";
/*     */   public static final String PACKETSIZE = "prop.packetsize";
/*     */   public static final String PASSWORD = "prop.password";
/*     */   public static final String PORTNUMBER = "prop.portnumber";
/*     */   public static final String PREPARESQL = "prop.preparesql";
/*     */   public static final String PROGNAME = "prop.progname";
/*     */   public static final String SERVERNAME = "prop.servername";
/*     */   public static final String SERVERTYPE = "prop.servertype";
/*     */   public static final String SOTIMEOUT = "prop.sotimeout";
/*     */   public static final String SOKEEPALIVE = "prop.sokeepalive";
/*     */   public static final String PROCESSID = "prop.processid";
/*     */   public static final String SSL = "prop.ssl";
/*     */   public static final String TCPNODELAY = "prop.tcpnodelay";
/*     */   public static final String TDS = "prop.tds";
/*     */   public static final String USECURSORS = "prop.usecursors";
/*     */   public static final String USEJCIFS = "prop.usejcifs";
/*     */   public static final String USENTLMV2 = "prop.usentlmv2";
/*     */   public static final String USELOBS = "prop.uselobs";
/*     */   public static final String USER = "prop.user";
/*     */   public static final String SENDSTRINGPARAMETERSASUNICODE = "prop.useunicode";
/*     */   public static final String WSID = "prop.wsid";
/*     */   public static final String XAEMULATION = "prop.xaemulation";
/*     */ 
/*     */   public int getMajorVersion()
/*     */   {
/* 131 */     return 1;
/*     */   }
/*     */ 
/*     */   public int getMinorVersion() {
/* 135 */     return 2;
/*     */   }
/*     */ 
/*     */   public static final String getVersion()
/*     */   {
/* 146 */     return "1.2" + ((".6" == null) ? "" : ".6");
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 161 */     return "jTDS " + getVersion();
/*     */   }
/*     */ 
/*     */   public boolean jdbcCompliant() {
/* 165 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean acceptsURL(String url) throws SQLException {
/* 169 */     if (url == null) {
/* 170 */       return false;
/*     */     }
/*     */ 
/* 173 */     return url.toLowerCase().startsWith(driverPrefix);
/*     */   }
/*     */ 
/*     */   public Connection connect(String url, Properties info) throws SQLException
/*     */   {
/* 178 */     if ((url == null) || (!(url.toLowerCase().startsWith(driverPrefix)))) {
/* 179 */       return null;
/*     */     }
/*     */ 
/* 182 */     Properties props = setupConnectProperties(url, info);
/*     */ 
/* 184 */     if (JDBC3) {
/* 185 */       return new ConnectionJDBC3(url, props);
/*     */     }
/*     */ 
/* 188 */     return new ConnectionJDBC2(url, props);
/*     */   }
/*     */ 
/*     */   public DriverPropertyInfo[] getPropertyInfo(String url, Properties props)
/*     */     throws SQLException
/*     */   {
/* 194 */     Properties parsedProps = parseURL(url, (props == null) ? new Properties() : props);
/*     */ 
/* 196 */     if (parsedProps == null) {
/* 197 */       throw new SQLException(Messages.get("error.driver.badurl", url), "08001");
/*     */     }
/*     */ 
/* 201 */     parsedProps = DefaultProperties.addDefaultProperties(parsedProps);
/*     */ 
/* 203 */     Map propertyMap = new HashMap();
/* 204 */     Map descriptionMap = new HashMap();
/* 205 */     Messages.loadDriverProperties(propertyMap, descriptionMap);
/*     */ 
/* 207 */     Map choicesMap = createChoicesMap();
/* 208 */     Map requiredTrueMap = createRequiredTrueMap();
/*     */ 
/* 210 */     DriverPropertyInfo[] dpi = new DriverPropertyInfo[propertyMap.size()];
/* 211 */     Iterator iterator = propertyMap.entrySet().iterator();
/* 212 */     for (int i = 0; iterator.hasNext(); ++i)
/*     */     {
/* 214 */       Map.Entry entry = (Map.Entry)iterator.next();
/* 215 */       String key = (String)entry.getKey();
/* 216 */       String name = (String)entry.getValue();
/*     */ 
/* 218 */       DriverPropertyInfo info = new DriverPropertyInfo(name, parsedProps.getProperty(name));
/* 219 */       info.description = ((String)descriptionMap.get(key));
/* 220 */       info.required = requiredTrueMap.containsKey(name);
/*     */ 
/* 222 */       if (choicesMap.containsKey(name)) {
/* 223 */         info.choices = ((String[])choicesMap.get(name));
/*     */       }
/*     */ 
/* 226 */       dpi[i] = info;
/*     */     }
/*     */ 
/* 229 */     return dpi;
/*     */   }
/*     */ 
/*     */   private Properties setupConnectProperties(String url, Properties info)
/*     */     throws SQLException
/*     */   {
/* 243 */     Properties props = parseURL(url, info);
/*     */ 
/* 245 */     if (props == null) {
/* 246 */       throw new SQLException(Messages.get("error.driver.badurl", url), "08001");
/*     */     }
/*     */ 
/* 249 */     if (props.getProperty(Messages.get("prop.logintimeout")) == null) {
/* 250 */       props.setProperty(Messages.get("prop.logintimeout"), Integer.toString(DriverManager.getLoginTimeout()));
/*     */     }
/*     */ 
/* 254 */     props = DefaultProperties.addDefaultProperties(props);
/* 255 */     return props;
/*     */   }
/*     */ 
/*     */   private static Map createChoicesMap()
/*     */   {
/* 271 */     HashMap choicesMap = new HashMap();
/*     */ 
/* 273 */     String[] booleanChoices = { "true", "false" };
/* 274 */     choicesMap.put(Messages.get("prop.cachemetadata"), booleanChoices);
/* 275 */     choicesMap.put(Messages.get("prop.lastupdatecount"), booleanChoices);
/* 276 */     choicesMap.put(Messages.get("prop.namedpipe"), booleanChoices);
/* 277 */     choicesMap.put(Messages.get("prop.tcpnodelay"), booleanChoices);
/* 278 */     choicesMap.put(Messages.get("prop.useunicode"), booleanChoices);
/* 279 */     choicesMap.put(Messages.get("prop.usecursors"), booleanChoices);
/* 280 */     choicesMap.put(Messages.get("prop.uselobs"), booleanChoices);
/* 281 */     choicesMap.put(Messages.get("prop.xaemulation"), booleanChoices);
/*     */ 
/* 283 */     String[] prepareSqlChoices = { String.valueOf(0), String.valueOf(1), String.valueOf(2), String.valueOf(3) };
/*     */ 
/* 289 */     choicesMap.put(Messages.get("prop.preparesql"), prepareSqlChoices);
/*     */ 
/* 291 */     String[] serverTypeChoices = { String.valueOf(1), String.valueOf(2) };
/*     */ 
/* 295 */     choicesMap.put(Messages.get("prop.servertype"), serverTypeChoices);
/*     */ 
/* 297 */     String[] tdsChoices = { "4.2", "5.0", "7.0", "8.0" };
/*     */ 
/* 303 */     choicesMap.put(Messages.get("prop.tds"), tdsChoices);
/*     */ 
/* 305 */     String[] sslChoices = { "off", "request", "require", "authenticate" };
/*     */ 
/* 311 */     choicesMap.put(Messages.get("prop.ssl"), sslChoices);
/*     */ 
/* 313 */     return choicesMap;
/*     */   }
/*     */ 
/*     */   private static Map createRequiredTrueMap()
/*     */   {
/* 329 */     HashMap requiredTrueMap = new HashMap();
/* 330 */     requiredTrueMap.put(Messages.get("prop.servername"), null);
/* 331 */     requiredTrueMap.put(Messages.get("prop.servertype"), null);
/* 332 */     return requiredTrueMap;
/*     */   }
/*     */ 
/*     */   private static Properties parseURL(String url, Properties info)
/*     */   {
/* 344 */     Properties props = new Properties();
/*     */ 
/* 347 */     for (Enumeration e = info.propertyNames(); e.hasMoreElements(); ) {
/* 348 */       String key = (String)e.nextElement();
/* 349 */       String value = info.getProperty(key);
/*     */ 
/* 351 */       if (value != null) {
/* 352 */         props.setProperty(key.toUpperCase(), value);
/*     */       }
/*     */     }
/*     */ 
/* 356 */     StringBuffer token = new StringBuffer(16);
/* 357 */     int pos = 0;
/*     */ 
/* 359 */     pos = nextToken(url, pos, token);
/*     */ 
/* 361 */     if (!("jdbc".equalsIgnoreCase(token.toString()))) {
/* 362 */       return null;
/*     */     }
/*     */ 
/* 365 */     pos = nextToken(url, pos, token);
/*     */ 
/* 367 */     if (!("jtds".equalsIgnoreCase(token.toString()))) {
/* 368 */       return null;
/*     */     }
/*     */ 
/* 371 */     pos = nextToken(url, pos, token);
/* 372 */     String type = token.toString().toLowerCase();
/*     */ 
/* 374 */     Integer serverType = DefaultProperties.getServerType(type);
/* 375 */     if (serverType == null) {
/* 376 */       return null;
/*     */     }
/* 378 */     props.setProperty(Messages.get("prop.servertype"), String.valueOf(serverType));
/*     */ 
/* 380 */     pos = nextToken(url, pos, token);
/*     */ 
/* 382 */     if (token.length() > 0) {
/* 383 */       return null;
/*     */     }
/*     */ 
/* 386 */     pos = nextToken(url, pos, token);
/* 387 */     String host = token.toString();
/*     */ 
/* 389 */     if (host.length() == 0) {
/* 390 */       host = props.getProperty(Messages.get("prop.servername"));
/* 391 */       if ((host == null) || (host.length() == 0)) {
/* 392 */         return null;
/*     */       }
/*     */     }
/*     */ 
/* 396 */     props.setProperty(Messages.get("prop.servername"), host);
/*     */ 
/* 398 */     if ((url.charAt(pos - 1) == ':') && (pos < url.length())) {
/* 399 */       pos = nextToken(url, pos, token);
/*     */       try
/*     */       {
/* 402 */         int port = Integer.parseInt(token.toString());
/* 403 */         props.setProperty(Messages.get("prop.portnumber"), Integer.toString(port));
/*     */       } catch (NumberFormatException e) {
/* 405 */         return null;
/*     */       }
/*     */     }
/*     */ 
/* 409 */     if ((url.charAt(pos - 1) == '/') && (pos < url.length())) {
/* 410 */       pos = nextToken(url, pos, token);
/* 411 */       props.setProperty(Messages.get("prop.databasename"), token.toString());
/*     */     }
/*     */ 
/* 417 */     while ((url.charAt(pos - 1) == ';') && (pos < url.length())) {
/* 418 */       pos = nextToken(url, pos, token);
/* 419 */       String tmp = token.toString();
/* 420 */       int index = tmp.indexOf(61);
/*     */ 
/* 422 */       if ((index > 0) && (index < tmp.length() - 1))
/* 423 */         props.setProperty(tmp.substring(0, index).toUpperCase(), tmp.substring(index + 1));
/*     */       else {
/* 425 */         props.setProperty(tmp.toUpperCase(), "");
/*     */       }
/*     */     }
/*     */ 
/* 429 */     return props;
/*     */   }
/*     */ 
/*     */   private static int nextToken(String url, int pos, StringBuffer token)
/*     */   {
/* 441 */     token.setLength(0);
/* 442 */     boolean inQuote = false;
/* 443 */     while (pos < url.length()) {
/* 444 */       char ch = url.charAt(pos++);
/*     */ 
/* 446 */       if (!(inQuote)) {
/* 447 */         if (ch == ':') break; if (ch == ';') {
/*     */           break;
/*     */         }
/*     */ 
/* 451 */         if (ch == '/') {
/* 452 */           if ((pos >= url.length()) || (url.charAt(pos) != '/')) break;
/* 453 */           ++pos; break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 460 */       if (ch == '[') {
/* 461 */         inQuote = true;
/*     */       }
/*     */ 
/* 465 */       if (ch == ']') {
/* 466 */         inQuote = false;
/*     */       }
/*     */ 
/* 470 */       token.append(ch);
/*     */     }
/*     */ 
/* 473 */     return pos;
/*     */   }
/*     */ 
/*     */   public static void main(String[] args) {
/* 477 */     System.out.println("jTDS " + getVersion());
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*     */     try
/*     */     {
/* 125 */       DriverManager.registerDriver(new Driver());
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.Driver
 * JD-Core Version:    0.5.3
 */