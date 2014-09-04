/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public final class DefaultProperties
/*     */ {
/*     */   public static final String APP_NAME = "jTDS";
/*     */   public static final String AUTO_COMMIT = "true";
/*     */   public static final String BATCH_SIZE_SQLSERVER = "0";
/*     */   public static final String BATCH_SIZE_SYBASE = "1000";
/*     */   public static final String BIND_ADDRESS = "";
/*     */   public static final String BUFFER_MAX_MEMORY = "1024";
/*     */   public static final String BUFFER_MIN_PACKETS = "8";
/*     */   public static final String CACHEMETA = "false";
/*     */   public static final String CHARSET = "";
/*     */   public static final String DATABASE_NAME = "";
/*     */   public static final String INSTANCE = "";
/*     */   public static final String DOMAIN = "";
/*     */   public static final String LAST_UPDATE_COUNT = "true";
/*     */   public static final String LOB_BUFFER_SIZE = "32768";
/*     */   public static final String LOGIN_TIMEOUT = "0";
/*     */   public static final String MAC_ADDRESS = "000000000000";
/*     */   public static final String MAX_STATEMENTS = "500";
/*     */   public static final String NAMED_PIPE = "false";
/*     */   public static final String NAMED_PIPE_PATH_SQLSERVER = "/sql/query";
/*     */   public static final String NAMED_PIPE_PATH_SYBASE = "/sybase/query";
/*  95 */   public static final String PACKET_SIZE_42 = String.valueOf(512);
/*     */   public static final String PACKET_SIZE_50 = "0";
/*     */   public static final String PACKET_SIZE_70_80 = "0";
/*     */   public static final String PASSWORD = "";
/*     */   public static final String PORT_NUMBER_SQLSERVER = "1433";
/*     */   public static final String PORT_NUMBER_SYBASE = "7100";
/*     */   public static final String LANGUAGE = "";
/* 109 */   public static final String PREPARE_SQLSERVER = String.valueOf(3);
/*     */ 
/* 111 */   public static final String PREPARE_SYBASE = String.valueOf(1);
/*     */   public static final String PROG_NAME = "jTDS";
/*     */   public static final String TCP_NODELAY = "true";
/* 117 */   public static final String BUFFER_DIR = new File(System.getProperty("java.io.tmpdir")).toString();
/*     */   public static final String USE_UNICODE = "true";
/*     */   public static final String USECURSORS = "false";
/*     */   public static final String USEJCIFS = "false";
/*     */   public static final String USELOBS = "true";
/*     */   public static final String USER = "";
/*     */   public static final String WSID = "";
/*     */   public static final String XAEMULATION = "true";
/*     */   public static final String LOGFILE = "";
/*     */   public static final String SOCKET_TIMEOUT = "0";
/*     */   public static final String SOCKET_KEEPALIVE = "false";
/*     */   public static final String PROCESS_ID = "123";
/*     */   public static final String SERVER_TYPE_SQLSERVER = "sqlserver";
/*     */   public static final String SERVER_TYPE_SYBASE = "sybase";
/*     */   public static final String TDS_VERSION_42 = "4.2";
/*     */   public static final String TDS_VERSION_50 = "5.0";
/*     */   public static final String TDS_VERSION_70 = "7.0";
/*     */   public static final String TDS_VERSION_80 = "8.0";
/*     */   public static final String SSL = "off";
/* 170 */   private static final HashMap tdsDefaults = new HashMap(2);
/*     */   private static final HashMap portNumberDefaults;
/*     */   private static final HashMap packetSizeDefaults;
/*     */   private static final HashMap batchSizeDefaults;
/*     */   private static final HashMap prepareSQLDefaults;
/*     */ 
/*     */   public static Properties addDefaultProperties(Properties props)
/*     */   {
/* 205 */     String serverType = props.getProperty(Messages.get("prop.servertype"));
/*     */ 
/* 207 */     if (serverType == null) {
/* 208 */       return null;
/*     */     }
/*     */ 
/* 211 */     addDefaultPropertyIfNotSet(props, "prop.tds", "prop.servertype", tdsDefaults);
/*     */ 
/* 213 */     addDefaultPropertyIfNotSet(props, "prop.portnumber", "prop.servertype", portNumberDefaults);
/*     */ 
/* 215 */     addDefaultPropertyIfNotSet(props, "prop.user", "");
/* 216 */     addDefaultPropertyIfNotSet(props, "prop.password", "");
/*     */ 
/* 218 */     addDefaultPropertyIfNotSet(props, "prop.databasename", "");
/* 219 */     addDefaultPropertyIfNotSet(props, "prop.instance", "");
/* 220 */     addDefaultPropertyIfNotSet(props, "prop.domain", "");
/* 221 */     addDefaultPropertyIfNotSet(props, "prop.appname", "jTDS");
/* 222 */     addDefaultPropertyIfNotSet(props, "prop.autocommit", "true");
/* 223 */     addDefaultPropertyIfNotSet(props, "prop.progname", "jTDS");
/* 224 */     addDefaultPropertyIfNotSet(props, "prop.wsid", "");
/* 225 */     addDefaultPropertyIfNotSet(props, "prop.batchsize", "prop.servertype", batchSizeDefaults);
/* 226 */     addDefaultPropertyIfNotSet(props, "prop.lastupdatecount", "true");
/* 227 */     addDefaultPropertyIfNotSet(props, "prop.lobbuffer", "32768");
/* 228 */     addDefaultPropertyIfNotSet(props, "prop.logintimeout", "0");
/* 229 */     addDefaultPropertyIfNotSet(props, "prop.sotimeout", "0");
/* 230 */     addDefaultPropertyIfNotSet(props, "prop.sokeepalive", "false");
/* 231 */     addDefaultPropertyIfNotSet(props, "prop.processid", "123");
/* 232 */     addDefaultPropertyIfNotSet(props, "prop.macaddress", "000000000000");
/* 233 */     addDefaultPropertyIfNotSet(props, "prop.maxstatements", "500");
/* 234 */     addDefaultPropertyIfNotSet(props, "prop.namedpipe", "false");
/* 235 */     addDefaultPropertyIfNotSet(props, "prop.packetsize", "prop.tds", packetSizeDefaults);
/* 236 */     addDefaultPropertyIfNotSet(props, "prop.cachemetadata", "false");
/* 237 */     addDefaultPropertyIfNotSet(props, "prop.charset", "");
/* 238 */     addDefaultPropertyIfNotSet(props, "prop.language", "");
/* 239 */     addDefaultPropertyIfNotSet(props, "prop.preparesql", "prop.servertype", prepareSQLDefaults);
/* 240 */     addDefaultPropertyIfNotSet(props, "prop.useunicode", "true");
/* 241 */     addDefaultPropertyIfNotSet(props, "prop.tcpnodelay", "true");
/* 242 */     addDefaultPropertyIfNotSet(props, "prop.xaemulation", "true");
/* 243 */     addDefaultPropertyIfNotSet(props, "prop.logfile", "");
/* 244 */     addDefaultPropertyIfNotSet(props, "prop.ssl", "off");
/* 245 */     addDefaultPropertyIfNotSet(props, "prop.usecursors", "false");
/* 246 */     addDefaultPropertyIfNotSet(props, "prop.buffermaxmemory", "1024");
/* 247 */     addDefaultPropertyIfNotSet(props, "prop.bufferminpackets", "8");
/* 248 */     addDefaultPropertyIfNotSet(props, "prop.uselobs", "true");
/* 249 */     addDefaultPropertyIfNotSet(props, "prop.bindaddress", "");
/* 250 */     addDefaultPropertyIfNotSet(props, "prop.usejcifs", "false");
/* 251 */     addDefaultPropertyIfNotSet(props, "prop.bufferdir", BUFFER_DIR);
/*     */ 
/* 253 */     return props;
/*     */   }
/*     */ 
/*     */   private static void addDefaultPropertyIfNotSet(Properties props, String key, String defaultValue)
/*     */   {
/* 265 */     String messageKey = Messages.get(key);
/*     */ 
/* 267 */     if (props.getProperty(messageKey) == null)
/* 268 */       props.setProperty(messageKey, defaultValue);
/*     */   }
/*     */ 
/*     */   private static void addDefaultPropertyIfNotSet(Properties props, String key, String defaultKey, Map defaults)
/*     */   {
/* 286 */     String defaultKeyValue = props.getProperty(Messages.get(defaultKey));
/*     */ 
/* 288 */     if (defaultKeyValue == null) {
/* 289 */       return;
/*     */     }
/*     */ 
/* 292 */     String messageKey = Messages.get(key);
/*     */ 
/* 294 */     if (props.getProperty(messageKey) == null) {
/* 295 */       Object defaultValue = defaults.get(defaultKeyValue);
/*     */ 
/* 297 */       if (defaultValue != null)
/* 298 */         props.setProperty(messageKey, String.valueOf(defaultValue));
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getNamedPipePath(int serverType)
/*     */   {
/* 311 */     if ((serverType == 0) || (serverType == 1)) {
/* 312 */       return "/sql/query";
/*     */     }
/* 314 */     if (serverType == 2) {
/* 315 */       return "/sybase/query";
/*     */     }
/* 317 */     throw new IllegalArgumentException("Unknown serverType: " + serverType);
/*     */   }
/*     */ 
/*     */   public static String getServerType(int serverType)
/*     */   {
/* 327 */     if (serverType == 1)
/* 328 */       return "sqlserver";
/* 329 */     if (serverType == 2) {
/* 330 */       return "sybase";
/*     */     }
/*     */ 
/* 333 */     return null;
/*     */   }
/*     */ 
/*     */   public static Integer getServerType(String serverType)
/*     */   {
/* 344 */     if ("sqlserver".equals(serverType))
/* 345 */       return new Integer(1);
/* 346 */     if ("sybase".equals(serverType)) {
/* 347 */       return new Integer(2);
/*     */     }
/*     */ 
/* 350 */     return null;
/*     */   }
/*     */ 
/*     */   public static String getServerTypeWithDefault(int serverType)
/*     */   {
/* 362 */     if (serverType == 0)
/* 363 */       return "sqlserver";
/* 364 */     if ((serverType == 1) || (serverType == 2))
/*     */     {
/* 366 */       return getServerType(serverType);
/*     */     }
/* 368 */     throw new IllegalArgumentException("Only 0, 1 and 2 accepted for serverType");
/*     */   }
/*     */ 
/*     */   public static Integer getTdsVersion(String tdsVersion)
/*     */   {
/* 380 */     if ("4.2".equals(tdsVersion))
/* 381 */       return new Integer(1);
/* 382 */     if ("5.0".equals(tdsVersion))
/* 383 */       return new Integer(2);
/* 384 */     if ("7.0".equals(tdsVersion))
/* 385 */       return new Integer(3);
/* 386 */     if ("8.0".equals(tdsVersion)) {
/* 387 */       return new Integer(4);
/*     */     }
/*     */ 
/* 390 */     return null;
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/* 171 */     tdsDefaults.put(String.valueOf(1), "8.0");
/* 172 */     tdsDefaults.put(String.valueOf(2), "5.0");
/*     */ 
/* 174 */     portNumberDefaults = new HashMap(2);
/* 175 */     portNumberDefaults.put(String.valueOf(1), "1433");
/* 176 */     portNumberDefaults.put(String.valueOf(2), "7100");
/*     */ 
/* 178 */     packetSizeDefaults = new HashMap(4);
/* 179 */     packetSizeDefaults.put("4.2", PACKET_SIZE_42);
/* 180 */     packetSizeDefaults.put("5.0", "0");
/* 181 */     packetSizeDefaults.put("7.0", "0");
/* 182 */     packetSizeDefaults.put("8.0", "0");
/*     */ 
/* 184 */     batchSizeDefaults = new HashMap(2);
/* 185 */     batchSizeDefaults.put(String.valueOf(1), "0");
/*     */ 
/* 187 */     batchSizeDefaults.put(String.valueOf(2), "1000");
/*     */ 
/* 190 */     prepareSQLDefaults = new HashMap(2);
/* 191 */     prepareSQLDefaults.put(String.valueOf(1), PREPARE_SQLSERVER);
/*     */ 
/* 193 */     prepareSQLDefaults.put(String.valueOf(2), PREPARE_SYBASE);
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.DefaultProperties
 * JD-Core Version:    0.5.3
 */