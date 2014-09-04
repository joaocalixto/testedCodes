/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.Reader;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.sql.SQLException;
/*     */ 
/*     */ class ParamInfo
/*     */   implements Cloneable
/*     */ {
/*     */   static final int INPUT = 0;
/*     */   static final int OUTPUT = 1;
/*     */   static final int RETVAL = 2;
/*     */   static final int UNICODE = 4;
/*     */   int tdsType;
/*     */   int jdbcType;
/*     */   String name;
/*     */   String sqlType;
/*  52 */   int markerPos = -1;
/*     */   Object value;
/*  56 */   int precision = -1;
/*     */ 
/*  58 */   int scale = -1;
/*     */ 
/*  60 */   int length = -1;
/*     */   boolean isOutput;
/*     */   boolean isRetVal;
/*     */   boolean isSet;
/*     */   boolean isUnicode;
/*     */   byte[] collation;
/*     */   CharsetInfo charsetInfo;
/*     */   boolean isSetOut;
/*     */   Object outValue;
/*     */ 
/*     */   ParamInfo(int pos, boolean isUnicode)
/*     */   {
/*  85 */     this.markerPos = pos;
/*  86 */     this.isUnicode = isUnicode;
/*     */   }
/*     */ 
/*     */   ParamInfo(String name, int pos, boolean isRetVal, boolean isUnicode)
/*     */   {
/*  98 */     this.name = name;
/*  99 */     this.markerPos = pos;
/* 100 */     this.isRetVal = isRetVal;
/* 101 */     this.isUnicode = isUnicode;
/*     */   }
/*     */ 
/*     */   ParamInfo(int jdbcType, Object value, int flags)
/*     */   {
/* 113 */     this.jdbcType = jdbcType;
/* 114 */     this.value = value;
/* 115 */     this.isSet = true;
/* 116 */     this.isOutput = (((flags & 0x1) > 0) || ((flags & 0x2) > 0));
/* 117 */     this.isRetVal = ((flags & 0x2) > 0);
/* 118 */     this.isUnicode = ((flags & 0x4) > 0);
/* 119 */     if (value instanceof String) {
/* 120 */       this.length = ((String)value).length();
/*     */     }
/* 122 */     else if (value instanceof byte[])
/* 123 */       this.length = ((byte[])value).length;
/*     */   }
/*     */ 
/*     */   ParamInfo(ColInfo ci, String name, Object value, int length)
/*     */   {
/* 136 */     this.name = name;
/* 137 */     this.tdsType = ci.tdsType;
/* 138 */     this.scale = ci.scale;
/* 139 */     this.precision = ci.precision;
/* 140 */     this.jdbcType = ci.jdbcType;
/* 141 */     this.sqlType = ci.sqlType;
/* 142 */     this.collation = ci.collation;
/* 143 */     this.charsetInfo = ci.charsetInfo;
/* 144 */     this.isUnicode = TdsData.isUnicode(ci);
/* 145 */     this.isSet = true;
/* 146 */     this.value = value;
/* 147 */     this.length = length;
/*     */   }
/*     */ 
/*     */   Object getOutValue()
/*     */     throws SQLException
/*     */   {
/* 158 */     if (!(this.isSetOut)) {
/* 159 */       throw new SQLException(Messages.get("error.callable.outparamnotset"), "HY010");
/*     */     }
/*     */ 
/* 162 */     return this.outValue;
/*     */   }
/*     */ 
/*     */   void setOutValue(Object value)
/*     */   {
/* 170 */     this.outValue = value;
/* 171 */     this.isSetOut = true;
/*     */   }
/*     */ 
/*     */   void clearOutValue()
/*     */   {
/* 179 */     this.outValue = null;
/* 180 */     this.isSetOut = false;
/*     */   }
/*     */ 
/*     */   void clearInValue()
/*     */   {
/* 188 */     this.value = null;
/* 189 */     this.isSet = false;
/*     */   }
/*     */ 
/*     */   String getString(String charset)
/*     */     throws IOException
/*     */   {
/* 198 */     if ((this.value == null) || (this.value instanceof String)) {
/* 199 */       return ((String)this.value);
/*     */     }
/*     */ 
/* 202 */     if (this.value instanceof InputStream) {
/*     */       try {
/* 204 */         this.value = loadFromReader(new InputStreamReader((InputStream)this.value, charset), this.length);
/* 205 */         this.length = ((String)this.value).length();
/*     */ 
/* 207 */         return ((String)this.value);
/*     */       } catch (UnsupportedEncodingException e) {
/* 209 */         throw new IOException("I/O Error: UnsupportedEncodingException: " + e.getMessage());
/*     */       }
/*     */     }
/*     */ 
/* 213 */     if (this.value instanceof Reader) {
/* 214 */       this.value = loadFromReader((Reader)this.value, this.length);
/* 215 */       return ((String)this.value);
/*     */     }
/*     */ 
/* 218 */     return this.value.toString();
/*     */   }
/*     */ 
/*     */   byte[] getBytes(String charset)
/*     */     throws IOException
/*     */   {
/* 227 */     if ((this.value == null) || (this.value instanceof byte[])) {
/* 228 */       return ((byte[])this.value);
/*     */     }
/*     */ 
/* 231 */     if (this.value instanceof InputStream) {
/* 232 */       this.value = loadFromStream((InputStream)this.value, this.length);
/*     */ 
/* 234 */       return ((byte[])this.value);
/*     */     }
/*     */ 
/* 237 */     if (this.value instanceof Reader) {
/* 238 */       String tmp = loadFromReader((Reader)this.value, this.length);
/* 239 */       this.value = Support.encodeString(charset, tmp);
/* 240 */       return ((byte[])this.value);
/*     */     }
/*     */ 
/* 243 */     if (this.value instanceof String) {
/* 244 */       return Support.encodeString(charset, (String)this.value);
/*     */     }
/*     */ 
/* 247 */     return new byte[0];
/*     */   }
/*     */ 
/*     */   private static byte[] loadFromStream(InputStream in, int length)
/*     */     throws IOException
/*     */   {
/* 260 */     byte[] buf = new byte[length];
/*     */ 
/* 262 */     int pos = 0;
/* 263 */     while ((pos != length) && ((res = in.read(buf, pos, length - pos)) != -1))
/*     */     {
/*     */       int res;
/* 264 */       pos += res;
/*     */     }
/* 266 */     if (pos != length) {
/* 267 */       throw new IOException("Data in stream less than specified by length");
/*     */     }
/*     */ 
/* 271 */     if (in.read() >= 0) {
/* 272 */       throw new IOException("More data in stream than specified by length");
/*     */     }
/*     */ 
/* 276 */     return buf;
/*     */   }
/*     */ 
/*     */   private static String loadFromReader(Reader in, int length)
/*     */     throws IOException
/*     */   {
/* 289 */     char[] buf = new char[length];
/*     */ 
/* 291 */     int pos = 0;
/* 292 */     while ((pos != length) && ((res = in.read(buf, pos, length - pos)) != -1))
/*     */     {
/*     */       int res;
/* 293 */       pos += res;
/*     */     }
/* 295 */     if (pos != length) {
/* 296 */       throw new IOException("Data in stream less than specified by length");
/*     */     }
/*     */ 
/* 300 */     if (in.read() >= 0) {
/* 301 */       throw new IOException("More data in stream than specified by length");
/*     */     }
/*     */ 
/* 305 */     return new String(buf);
/*     */   }
/*     */ 
/*     */   public Object clone()
/*     */   {
/*     */     try
/*     */     {
/* 315 */       return super.clone();
/*     */     } catch (CloneNotSupportedException ex) {
/*     */     }
/* 318 */     return null;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.ParamInfo
 * JD-Core Version:    0.5.3
 */