/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.security.AccessController;
/*     */ import java.security.PrivilegedAction;
/*     */ import java.sql.SQLException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Properties;
/*     */ import net.sourceforge.jtds.util.Logger;
/*     */ 
/*     */ public final class CharsetInfo
/*     */ {
/*     */   private static final String CHARSETS_RESOURCE_NAME = "net/sourceforge/jtds/jdbc/Charsets.properties";
/*  52 */   private static final HashMap charsets = new HashMap();
/*     */ 
/*  55 */   private static final HashMap lcidToCharsetMap = new HashMap();
/*     */ 
/*  58 */   private static final CharsetInfo[] sortToCharsetMap = new CharsetInfo[256];
/*     */   private final String charset;
/*     */   private final boolean wideChars;
/*     */   static Class class$net$sourceforge$jtds$jdbc$CharsetInfo;
/*     */ 
/*     */   public static CharsetInfo getCharset(String serverCharset)
/*     */   {
/* 142 */     return ((CharsetInfo)charsets.get(serverCharset.toUpperCase()));
/*     */   }
/*     */ 
/*     */   public static CharsetInfo getCharsetForLCID(int lcid)
/*     */   {
/* 153 */     return ((CharsetInfo)lcidToCharsetMap.get(new Integer(lcid)));
/*     */   }
/*     */ 
/*     */   public static CharsetInfo getCharsetForSortOrder(int sortOrder)
/*     */   {
/* 164 */     return sortToCharsetMap[sortOrder];
/*     */   }
/*     */ 
/*     */   public static CharsetInfo getCharset(byte[] collation)
/*     */     throws SQLException
/*     */   {
/*     */     CharsetInfo charset;
/* 178 */     if (collation[4] != 0)
/*     */     {
/* 180 */       charset = getCharsetForSortOrder(collation[4] & 0xFF);
/*     */     }
/*     */     else {
/* 183 */       charset = getCharsetForLCID((collation[2] & 0xF) << 16 | (collation[1] & 0xFF) << 8 | collation[0] & 0xFF);
/*     */     }
/*     */ 
/* 189 */     if (charset == null) {
/* 190 */       throw new SQLException(Messages.get("error.charset.nocollation", Support.toHex(collation)), "2C000");
/*     */     }
/*     */ 
/* 195 */     return charset;
/*     */   }
/*     */ 
/*     */   public CharsetInfo(String descriptor)
/*     */   {
/* 216 */     this.wideChars = (!("1".equals(descriptor.substring(0, 1))));
/* 217 */     this.charset = descriptor.substring(2);
/*     */   }
/*     */ 
/*     */   public String getCharset()
/*     */   {
/* 224 */     return this.charset;
/*     */   }
/*     */ 
/*     */   public boolean isWideChars()
/*     */   {
/* 231 */     return this.wideChars;
/*     */   }
/*     */ 
/*     */   public boolean equals(Object o) {
/* 235 */     if (this == o) {
/* 236 */       return true;
/*     */     }
/* 238 */     if (!(o instanceof CharsetInfo)) {
/* 239 */       return false;
/*     */     }
/*     */ 
/* 242 */     CharsetInfo charsetInfo = (CharsetInfo)o;
/*     */ 
/* 244 */     return (this.charset.equals(charsetInfo.charset));
/*     */   }
/*     */ 
/*     */   public int hashCode()
/*     */   {
/* 251 */     return this.charset.hashCode();
/*     */   }
/*     */ 
/*     */   public String toString() {
/* 255 */     return this.charset;
/*     */   }
/*     */ 
/*     */   static Class class$(String x0)
/*     */   {
/*     */     try
/*     */     {
/*  81 */       return Class.forName(x0); } catch (ClassNotFoundException x1) { throw new NoClassDefFoundError(x1.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*     */     try
/*     */     {
/*  63 */       InputStream stream = null;
/*     */ 
/*  66 */       ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
/*     */ 
/*  69 */       if (classLoader != null) {
/*  70 */         stream = classLoader.getResourceAsStream("net/sourceforge/jtds/jdbc/Charsets.properties");
/*     */       }
/*     */ 
/*  74 */       if (stream == null)
/*     */       {
/*  78 */         stream = (InputStream)AccessController.doPrivileged(new PrivilegedAction()
/*     */         {
/*     */           public Object run() {
/*  81 */             ClassLoader loader = ((CharsetInfo.class$net$sourceforge$jtds$jdbc$CharsetInfo == null) ? (CharsetInfo.class$net$sourceforge$jtds$jdbc$CharsetInfo = CharsetInfo.class$("net.sourceforge.jtds.jdbc.CharsetInfo")) : CharsetInfo.class$net$sourceforge$jtds$jdbc$CharsetInfo).getClassLoader();
/*     */ 
/*  84 */             if (loader == null) {
/*  85 */               loader = ClassLoader.getSystemClassLoader();
/*     */             }
/*     */ 
/*  88 */             return loader.getResourceAsStream("net/sourceforge/jtds/jdbc/Charsets.properties");
/*     */           }
/*     */         });
/*     */       }
/*     */       Properties tmp;
/*     */       HashMap instances;
/*     */       Enumeration e;
/*  95 */       if (stream != null) {
/*  96 */         tmp = new Properties();
/*  97 */         tmp.load(stream);
/*     */ 
/*  99 */         instances = new HashMap();
/*     */ 
/* 101 */         for (e = tmp.propertyNames(); e.hasMoreElements(); ) {
/* 102 */           String key = (String)e.nextElement();
/* 103 */           CharsetInfo value = new CharsetInfo(tmp.getProperty(key));
/*     */ 
/* 106 */           CharsetInfo prevInstance = (CharsetInfo)instances.get(value.getCharset());
/*     */ 
/* 108 */           if (prevInstance != null) {
/* 109 */             if (prevInstance.isWideChars() != value.isWideChars()) {
/* 110 */               throw new IllegalStateException("Inconsistent Charsets.properties");
/*     */             }
/*     */ 
/* 113 */             value = prevInstance;
/*     */           }
/*     */ 
/* 116 */           if (key.startsWith("LCID_")) {
/* 117 */             Integer lcid = new Integer(key.substring(5));
/* 118 */             lcidToCharsetMap.put(lcid, value);
/* 119 */           } else if (key.startsWith("SORT_")) {
/* 120 */             sortToCharsetMap[Integer.parseInt(key.substring(5))] = value;
/*     */           } else {
/* 122 */             charsets.put(key, value);
/*     */           }
/*     */         }
/*     */       } else {
/* 126 */         Logger.println("Can't load Charsets.properties");
/*     */       }
/*     */     }
/*     */     catch (IOException e) {
/* 130 */       Logger.logException(e);
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.CharsetInfo
 * JD-Core Version:    0.5.3
 */