/*      */ package net.sourceforge.jtds.jdbc;
/*      */ 
/*      */ import [B;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStreamWriter;
/*      */ import java.io.Reader;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.io.Writer;
/*      */ import java.lang.reflect.Method;
/*      */ import java.math.BigDecimal;
/*      */ import java.math.BigInteger;
/*      */ import java.sql.Blob;
/*      */ import java.sql.Clob;
/*      */ import java.sql.Connection;
/*      */ import java.sql.ResultSet;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.SQLWarning;
/*      */ import java.sql.Statement;
/*      */ import java.sql.Time;
/*      */ import java.sql.Timestamp;
/*      */ import java.util.Calendar;
/*      */ import java.util.GregorianCalendar;
/*      */ import java.util.HashMap;
/*      */ import net.sourceforge.jtds.util.Logger;
/*      */ 
/*      */ public class Support
/*      */ {
/*   50 */   private static final Integer INTEGER_ZERO = new Integer(0);
/*   51 */   private static final Integer INTEGER_ONE = new Integer(1);
/*   52 */   private static final Long LONG_ZERO = new Long(0L);
/*   53 */   private static final Long LONG_ONE = new Long(1L);
/*   54 */   private static final Float FLOAT_ZERO = new Float(0.0D);
/*   55 */   private static final Float FLOAT_ONE = new Float(1.0D);
/*   56 */   private static final Double DOUBLE_ZERO = new Double(0.0D);
/*   57 */   private static final Double DOUBLE_ONE = new Double(1.0D);
/*   58 */   private static final BigDecimal BIG_DECIMAL_ZERO = new BigDecimal(0.0D);
/*   59 */   private static final BigDecimal BIG_DECIMAL_ONE = new BigDecimal(1.0D);
/*   60 */   private static final java.sql.Date DATE_ZERO = new java.sql.Date(0L);
/*   61 */   private static final Time TIME_ZERO = new Time(0L);
/*   62 */   private static final BigInteger MIN_VALUE_LONG_BI = new BigInteger(String.valueOf(-9223372036854775808L));
/*   63 */   private static final BigInteger MAX_VALUE_LONG_BI = new BigInteger(String.valueOf(9223372036854775807L));
/*   64 */   private static final BigDecimal MIN_VALUE_LONG_BD = new BigDecimal(String.valueOf(-9223372036854775808L));
/*   65 */   private static final BigDecimal MAX_VALUE_LONG_BD = new BigDecimal(String.valueOf(9223372036854775807L));
/*   66 */   private static final BigInteger MAX_VALUE_28 = new BigInteger("9999999999999999999999999999");
/*   67 */   private static final BigInteger MAX_VALUE_38 = new BigInteger("99999999999999999999999999999999999999");
/*      */ 
/*   72 */   private static final HashMap typeMap = new HashMap();
/*      */   private static final char[] hex;
/*      */   private static final ThreadLocal calendar;
/*      */ 
/*      */   public static String toHex(byte[] bytes)
/*      */   {
/*  117 */     int len = bytes.length;
/*      */ 
/*  119 */     if (len > 0) {
/*  120 */       StringBuffer buf = new StringBuffer(len * 2);
/*      */ 
/*  122 */       for (int i = 0; i < len; ++i) {
/*  123 */         int b1 = bytes[i] & 0xFF;
/*      */ 
/*  125 */         buf.append(hex[(b1 >> 4)]);
/*  126 */         buf.append(hex[(b1 & 0xF)]);
/*      */       }
/*      */ 
/*  129 */       return buf.toString();
/*      */     }
/*      */ 
/*  132 */     return "";
/*      */   }
/*      */ 
/*      */   static BigDecimal normalizeBigDecimal(BigDecimal value, int maxPrecision)
/*      */     throws SQLException
/*      */   {
/*  148 */     if (value == null) {
/*  149 */       return null;
/*      */     }
/*      */ 
/*  152 */     if (value.scale() < 0)
/*      */     {
/*  155 */       value = value.setScale(0);
/*      */     }
/*      */ 
/*  158 */     if (value.scale() > maxPrecision)
/*      */     {
/*  162 */       value = value.setScale(maxPrecision, 4);
/*      */     }
/*      */ 
/*  165 */     BigInteger max = (maxPrecision == 28) ? MAX_VALUE_28 : MAX_VALUE_38;
/*      */ 
/*  167 */     while (value.abs().unscaledValue().compareTo(max) > 0)
/*      */     {
/*  171 */       int scale = value.scale() - 1;
/*      */ 
/*  173 */       if (scale < 0)
/*      */       {
/*  175 */         throw new SQLException(Messages.get("error.normalize.numtoobig", String.valueOf(maxPrecision)), "22000");
/*      */       }
/*      */ 
/*  179 */       value = value.setScale(scale, 4);
/*      */     }
/*      */ 
/*  182 */     return value;
/*      */   }
/*      */ 
/*      */   static Object castNumeric(Object orig, int sourceType, int targetType)
/*      */   {
/*  189 */     return null;
/*      */   }
/*      */ 
/*      */   static Object convert(Object callerReference, Object x, int jdbcType, String charSet)
/*      */     throws SQLException
/*      */   {
/*  208 */     if (x == null) {
/*  209 */       switch (jdbcType) { case -7:
/*      */       case 16:
/*  212 */         return Boolean.FALSE;
/*      */       case -6:
/*      */       case 4:
/*      */       case 5:
/*  217 */         return INTEGER_ZERO;
/*      */       case -5:
/*  220 */         return LONG_ZERO;
/*      */       case 7:
/*  223 */         return FLOAT_ZERO;
/*      */       case 6:
/*      */       case 8:
/*  227 */         return DOUBLE_ZERO;
/*      */       case -4:
/*      */       case -3:
/*      */       case -2:
/*      */       case -1:
/*      */       case 0:
/*      */       case 1:
/*      */       case 2:
/*      */       case 3:
/*      */       case 9:
/*      */       case 10:
/*      */       case 11:
/*      */       case 12:
/*      */       case 13:
/*      */       case 14:
/*      */       case 15: } return null;
/*      */     }
/*      */     try
/*      */     {
/*      */       long val;
/*      */       Clob clob;
/*      */       long length;
/*      */       Blob blob;
/*      */       GregorianCalendar cal;
/*  235 */       switch (jdbcType)
/*      */       {
/*      */       case -6:
/*  237 */         if (x instanceof Boolean)
/*  238 */           return ((((Boolean)x).booleanValue()) ? INTEGER_ONE : INTEGER_ZERO);
/*  239 */         if (x instanceof Byte) {
/*  240 */           return new Integer(((Byte)x).byteValue() & 0xFF);
/*      */         }
/*      */ 
/*  243 */         if (x instanceof Number) {
/*  244 */           val = ((Number)x).longValue(); } else {
/*  245 */           if (!(x instanceof String)) break label2661;
/*  246 */           val = new Long(((String)x).trim()).longValue();
/*      */         }
/*      */ 
/*  250 */         if ((val < -128L) || (val > 127L)) {
/*  251 */           throw new SQLException(Messages.get("error.convert.numericoverflow", x, getJdbcTypeName(jdbcType)), "22003");
/*      */         }
/*  253 */         return new Integer(new Long(val).intValue());
/*      */       case 5:
/*  258 */         if (x instanceof Boolean)
/*  259 */           return ((((Boolean)x).booleanValue()) ? INTEGER_ONE : INTEGER_ZERO);
/*  260 */         if (x instanceof Short)
/*  261 */           return new Integer(((Short)x).shortValue());
/*  262 */         if (x instanceof Byte) {
/*  263 */           return new Integer(((Byte)x).byteValue() & 0xFF);
/*      */         }
/*      */ 
/*  266 */         if (x instanceof Number) {
/*  267 */           val = ((Number)x).longValue(); } else {
/*  268 */           if (!(x instanceof String)) break label2661;
/*  269 */           val = new Long(((String)x).trim()).longValue();
/*      */         }
/*      */ 
/*  273 */         if ((val < -32768L) || (val > 32767L)) {
/*  274 */           throw new SQLException(Messages.get("error.convert.numericoverflow", x, getJdbcTypeName(jdbcType)), "22003");
/*      */         }
/*  276 */         return new Integer(new Long(val).intValue());
/*      */       case 4:
/*  281 */         if (x instanceof Integer) {
/*  282 */           return x;
/*      */         }
/*  284 */         if (x instanceof Boolean)
/*  285 */           return ((((Boolean)x).booleanValue()) ? INTEGER_ONE : INTEGER_ZERO);
/*  286 */         if (x instanceof Short)
/*  287 */           return new Integer(((Short)x).shortValue());
/*  288 */         if (x instanceof Byte) {
/*  289 */           return new Integer(((Byte)x).byteValue() & 0xFF);
/*      */         }
/*      */ 
/*  292 */         if (x instanceof Number) {
/*  293 */           val = ((Number)x).longValue(); } else {
/*  294 */           if (!(x instanceof String)) break label2661;
/*  295 */           val = new Long(((String)x).trim()).longValue();
/*      */         }
/*      */ 
/*  299 */         if ((val < -2147483648L) || (val > 2147483647L)) {
/*  300 */           throw new SQLException(Messages.get("error.convert.numericoverflow", x, getJdbcTypeName(jdbcType)), "22003");
/*      */         }
/*  302 */         return new Integer(new Long(val).intValue());
/*      */       case -5:
/*  307 */         if (x instanceof BigDecimal) {
/*  308 */           BigDecimal val = (BigDecimal)x;
/*  309 */           if ((val.compareTo(MIN_VALUE_LONG_BD) < 0) || (val.compareTo(MAX_VALUE_LONG_BD) > 0)) {
/*  310 */             throw new SQLException(Messages.get("error.convert.numericoverflow", x, getJdbcTypeName(jdbcType)), "22003");
/*      */           }
/*  312 */           return new Long(val.longValue());
/*      */         }
/*  314 */         if (x instanceof Long)
/*  315 */           return x;
/*  316 */         if (x instanceof Boolean)
/*  317 */           return ((((Boolean)x).booleanValue()) ? LONG_ONE : LONG_ZERO);
/*  318 */         if (x instanceof Byte)
/*  319 */           return new Long(((Byte)x).byteValue() & 0xFF);
/*  320 */         if (x instanceof BigInteger) {
/*  321 */           BigInteger val = (BigInteger)x;
/*  322 */           if ((val.compareTo(MIN_VALUE_LONG_BI) < 0) || (val.compareTo(MAX_VALUE_LONG_BI) > 0)) {
/*  323 */             throw new SQLException(Messages.get("error.convert.numericoverflow", x, getJdbcTypeName(jdbcType)), "22003");
/*      */           }
/*  325 */           return new Long(val.longValue());
/*      */         }
/*  327 */         if (x instanceof Number)
/*  328 */           return new Long(((Number)x).longValue());
/*  329 */         if (x instanceof String)
/*  330 */           return new Long(((String)x).trim());
/*      */       case 7:
/*  336 */         if (x instanceof Float)
/*  337 */           return x;
/*  338 */         if (x instanceof Byte)
/*  339 */           return new Float(((Byte)x).byteValue() & 0xFF);
/*  340 */         if (x instanceof Number)
/*  341 */           return new Float(((Number)x).floatValue());
/*  342 */         if (x instanceof String)
/*  343 */           return new Float(((String)x).trim());
/*  344 */         if (x instanceof Boolean)
/*  345 */           return ((((Boolean)x).booleanValue()) ? FLOAT_ONE : FLOAT_ZERO);
/*      */       case 6:
/*      */       case 8:
/*  352 */         if (x instanceof Double)
/*  353 */           return x;
/*  354 */         if (x instanceof Byte)
/*  355 */           return new Double(((Byte)x).byteValue() & 0xFF);
/*  356 */         if (x instanceof Number)
/*  357 */           return new Double(((Number)x).doubleValue());
/*  358 */         if (x instanceof String)
/*  359 */           return new Double(((String)x).trim());
/*  360 */         if (x instanceof Boolean)
/*  361 */           return ((((Boolean)x).booleanValue()) ? DOUBLE_ONE : DOUBLE_ZERO);
/*      */       case 2:
/*      */       case 3:
/*  368 */         if (x instanceof BigDecimal)
/*  369 */           return x;
/*  370 */         if (x instanceof Number)
/*  371 */           return new BigDecimal(x.toString());
/*  372 */         if (x instanceof String)
/*  373 */           return new BigDecimal((String)x);
/*  374 */         if (x instanceof Boolean)
/*  375 */           return ((((Boolean)x).booleanValue()) ? BIG_DECIMAL_ONE : BIG_DECIMAL_ZERO);
/*      */       case 1:
/*      */       case 12:
/*  382 */         if (x instanceof String)
/*  383 */           return x;
/*  384 */         if (x instanceof Number)
/*  385 */           return x.toString();
/*  386 */         if (x instanceof Boolean)
/*  387 */           return ((((Boolean)x).booleanValue()) ? "1" : "0");
/*  388 */         if (x instanceof Clob) {
/*  389 */           clob = (Clob)x;
/*  390 */           length = clob.length();
/*      */ 
/*  392 */           if (length > 2147483647L) {
/*  393 */             throw new SQLException(Messages.get("error.normalize.lobtoobig"), "22000");
/*      */           }
/*      */ 
/*  397 */           return clob.getSubString(1L, (int)length); }
/*  398 */         if (x instanceof Blob) {
/*  399 */           blob = (Blob)x;
/*  400 */           length = blob.length();
/*      */ 
/*  402 */           if (length > 2147483647L) {
/*  403 */             throw new SQLException(Messages.get("error.normalize.lobtoobig"), "22000");
/*      */           }
/*      */ 
/*  407 */           x = blob.getBytes(1L, (int)length);
/*      */         }
/*      */ 
/*  410 */         if (x instanceof byte[]) {
/*  411 */           return toHex((byte[])x);
/*      */         }
/*      */ 
/*  414 */         return x.toString();
/*      */       case -7:
/*      */       case 16:
/*  418 */         if (x instanceof Boolean)
/*  419 */           return x;
/*  420 */         if (x instanceof Number)
/*  421 */           return ((((Number)x).intValue() == 0) ? Boolean.FALSE : Boolean.TRUE);
/*  422 */         if (x instanceof String) {
/*  423 */           String tmp = ((String)x).trim();
/*      */ 
/*  425 */           return ((("1".equals(tmp)) || ("true".equalsIgnoreCase(tmp))) ? Boolean.TRUE : Boolean.FALSE);
/*      */         }
/*      */       case -3:
/*      */       case -2:
/*  432 */         if (x instanceof byte[])
/*  433 */           return x;
/*  434 */         if (x instanceof Blob) {
/*  435 */           blob = (Blob)x;
/*      */ 
/*  437 */           return blob.getBytes(1L, (int)blob.length()); }
/*  438 */         if (x instanceof Clob) {
/*  439 */           blob = (Clob)x;
/*  440 */           length = blob.length();
/*      */ 
/*  442 */           if (length > 2147483647L) {
/*  443 */             throw new SQLException(Messages.get("error.normalize.lobtoobig"), "22000");
/*      */           }
/*      */ 
/*  447 */           x = blob.getSubString(1L, (int)length);
/*      */         }
/*      */ 
/*  450 */         if (x instanceof String)
/*      */         {
/*  455 */           if (charSet == null) {
/*  456 */             charSet = "ISO-8859-1";
/*      */           }
/*      */           try
/*      */           {
/*  460 */             return ((String)x).getBytes(charSet);
/*      */           } catch (UnsupportedEncodingException e) {
/*  462 */             return ((String)x).getBytes(); }
/*      */         }
/*  464 */         if (x instanceof UniqueIdentifier)
/*  465 */           return ((UniqueIdentifier)x).getBytes();
/*      */       case 93:
/*  471 */         if (x instanceof DateTime)
/*  472 */           return ((DateTime)x).toTimestamp();
/*  473 */         if (x instanceof Timestamp)
/*  474 */           return x;
/*  475 */         if (x instanceof java.sql.Date)
/*  476 */           return new Timestamp(((java.sql.Date)x).getTime());
/*  477 */         if (x instanceof Time)
/*  478 */           return new Timestamp(((Time)x).getTime());
/*  479 */         if (x instanceof String)
/*  480 */           return Timestamp.valueOf(((String)x).trim());
/*      */       case 91:
/*  486 */         if (x instanceof DateTime)
/*  487 */           return ((DateTime)x).toDate();
/*  488 */         if (x instanceof java.sql.Date)
/*  489 */           return x;
/*  490 */         if (x instanceof Time)
/*  491 */           return DATE_ZERO;
/*  492 */         if (x instanceof Timestamp) {
/*  493 */           cal = (GregorianCalendar)calendar.get();
/*  494 */           cal.setTime((java.util.Date)x);
/*  495 */           cal.set(11, 0);
/*  496 */           cal.set(12, 0);
/*  497 */           cal.set(13, 0);
/*  498 */           cal.set(14, 0);
/*      */ 
/*  500 */           return new java.sql.Date(cal.getTime().getTime()); }
/*  501 */         if (x instanceof String)
/*  502 */           return java.sql.Date.valueOf(((String)x).trim());
/*      */       case 92:
/*  508 */         if (x instanceof DateTime)
/*  509 */           return ((DateTime)x).toTime();
/*  510 */         if (x instanceof Time)
/*  511 */           return x;
/*  512 */         if (x instanceof java.sql.Date)
/*  513 */           return TIME_ZERO;
/*  514 */         if (x instanceof Timestamp) {
/*  515 */           cal = (GregorianCalendar)calendar.get();
/*      */ 
/*  517 */           cal.setTime((java.util.Date)x);
/*  518 */           cal.set(1, 1970);
/*  519 */           cal.set(2, 0);
/*  520 */           cal.set(5, 1);
/*      */ 
/*  522 */           return new Time(cal.getTime().getTime()); }
/*  523 */         if (x instanceof String)
/*  524 */           return Time.valueOf(((String)x).trim().split("\\.")[0]);
/*      */       case 1111:
/*  530 */         return x;
/*      */       case 2000:
/*  533 */         throw new SQLException(Messages.get("error.convert.badtypes", x.getClass().getName(), getJdbcTypeName(jdbcType)), "22005");
/*      */       case -4:
/*      */       case 2004:
/*  540 */         if (x instanceof Blob)
/*  541 */           return x;
/*  542 */         if (x instanceof byte[])
/*  543 */           return new BlobImpl(getConnection(callerReference), (byte[])x);
/*  544 */         if (x instanceof Clob)
/*      */         {
/*  549 */           blob = (Clob)x;
/*      */           try {
/*  551 */             if (charSet == null) {
/*  552 */               charSet = "ISO-8859-1";
/*      */             }
/*  554 */             Reader rdr = blob.getCharacterStream();
/*  555 */             BlobImpl blob = new BlobImpl(getConnection(callerReference));
/*  556 */             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(blob.setBinaryStream(1L), charSet));
/*      */ 
/*  560 */             while ((c = rdr.read()) >= 0)
/*      */             {
/*      */               int c;
/*  561 */               out.write(c);
/*      */             }
/*  563 */             out.close();
/*  564 */             rdr.close();
/*  565 */             return blob;
/*      */           }
/*      */           catch (UnsupportedEncodingException e) {
/*  568 */             x = blob.getSubString(1L, (int)blob.length());
/*      */           } catch (IOException e) {
/*  570 */             throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  575 */         if (x instanceof String)
/*      */         {
/*  580 */           BlobImpl blob = new BlobImpl(getConnection(callerReference));
/*  581 */           String data = (String)x;
/*      */ 
/*  583 */           if (charSet == null) {
/*  584 */             charSet = "ISO-8859-1";
/*      */           }
/*      */           try
/*      */           {
/*  588 */             blob.setBytes(1L, data.getBytes(charSet));
/*      */           } catch (UnsupportedEncodingException e) {
/*  590 */             blob.setBytes(1L, data.getBytes());
/*      */           }
/*      */ 
/*  593 */           return blob;
/*      */         }
/*      */       case -1:
/*      */       case 2005:
/*  600 */         if (x instanceof Clob)
/*  601 */           return x;
/*  602 */         if (x instanceof Blob)
/*      */         {
/*  606 */           blob = (Blob)x;
/*      */           try {
/*  608 */             InputStream is = blob.getBinaryStream();
/*  609 */             ClobImpl clob = new ClobImpl(getConnection(callerReference));
/*  610 */             Writer out = clob.setCharacterStream(1L);
/*      */ 
/*  614 */             while ((b = is.read()) >= 0)
/*      */             {
/*      */               int b;
/*  615 */               out.write(hex[(b >> 4)]);
/*  616 */               out.write(hex[(b & 0xF)]);
/*      */             }
/*  618 */             out.close();
/*  619 */             is.close();
/*  620 */             return clob;
/*      */           } catch (IOException is) {
/*  622 */             throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */           }
/*      */         }
/*  625 */         if (x instanceof Boolean)
/*  626 */           x = (((Boolean)x).booleanValue()) ? "1" : "0";
/*  627 */         else if (!(x instanceof byte[])) {
/*  628 */           x = x.toString();
/*      */         }
/*      */ 
/*  631 */         if (x instanceof byte[]) {
/*  632 */           ClobImpl clob = new ClobImpl(getConnection(callerReference));
/*  633 */           clob.setString(1L, toHex((byte[])x));
/*      */ 
/*  635 */           return clob; }
/*  636 */         if (x instanceof String) {
/*  637 */           return new ClobImpl(getConnection(callerReference), (String)x);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  643 */       throw new SQLException(Messages.get("error.convert.badtypeconst", String.valueOf(x), getJdbcTypeName(jdbcType)), "HY004");
/*      */ 
/*  648 */       label2661: throw new SQLException(Messages.get("error.convert.badtypes", x.getClass().getName(), getJdbcTypeName(jdbcType)), "22005");
/*      */     }
/*      */     catch (NumberFormatException nfe)
/*      */     {
/*  653 */       throw new SQLException(Messages.get("error.convert.badnumber", String.valueOf(x), getJdbcTypeName(jdbcType)), "22000");
/*      */     }
/*      */   }
/*      */ 
/*      */   static int getJdbcType(Object value)
/*      */   {
/*  666 */     if (value == null) {
/*  667 */       return 0;
/*      */     }
/*      */ 
/*  670 */     return getJdbcType(value.getClass());
/*      */   }
/*      */ 
/*      */   static int getJdbcType(Class typeClass)
/*      */   {
/*  680 */     if (typeClass == null) {
/*  681 */       return 2000;
/*      */     }
/*      */ 
/*  684 */     Object type = typeMap.get(typeClass);
/*      */ 
/*  686 */     if (type == null)
/*      */     {
/*  688 */       return getJdbcType(typeClass.getSuperclass());
/*      */     }
/*      */ 
/*  691 */     return ((Integer)type).intValue();
/*      */   }
/*      */ 
/*      */   static String getJdbcTypeName(int jdbcType)
/*      */   {
/*  701 */     switch (jdbcType)
/*      */     {
/*      */     case 2003:
/*  702 */       return "ARRAY";
/*      */     case -5:
/*  703 */       return "BIGINT";
/*      */     case -2:
/*  704 */       return "BINARY";
/*      */     case -7:
/*  705 */       return "BIT";
/*      */     case 2004:
/*  706 */       return "BLOB";
/*      */     case 16:
/*  707 */       return "BOOLEAN";
/*      */     case 1:
/*  708 */       return "CHAR";
/*      */     case 2005:
/*  709 */       return "CLOB";
/*      */     case 70:
/*  710 */       return "DATALINK";
/*      */     case 91:
/*  711 */       return "DATE";
/*      */     case 3:
/*  712 */       return "DECIMAL";
/*      */     case 2001:
/*  713 */       return "DISTINCT";
/*      */     case 8:
/*  714 */       return "DOUBLE";
/*      */     case 6:
/*  715 */       return "FLOAT";
/*      */     case 4:
/*  716 */       return "INTEGER";
/*      */     case 2000:
/*  717 */       return "JAVA_OBJECT";
/*      */     case -4:
/*  718 */       return "LONGVARBINARY";
/*      */     case -1:
/*  719 */       return "LONGVARCHAR";
/*      */     case 0:
/*  720 */       return "NULL";
/*      */     case 2:
/*  721 */       return "NUMERIC";
/*      */     case 1111:
/*  722 */       return "OTHER";
/*      */     case 7:
/*  723 */       return "REAL";
/*      */     case 2006:
/*  724 */       return "REF";
/*      */     case 5:
/*  725 */       return "SMALLINT";
/*      */     case 2002:
/*  726 */       return "STRUCT";
/*      */     case 92:
/*  727 */       return "TIME";
/*      */     case 93:
/*  728 */       return "TIMESTAMP";
/*      */     case -6:
/*  729 */       return "TINYINT";
/*      */     case -3:
/*  730 */       return "VARBINARY";
/*      */     case 12:
/*  731 */       return "VARCHAR"; }
/*  732 */     return "ERROR";
/*      */   }
/*      */ 
/*      */   static String getClassName(int jdbcType)
/*      */   {
/*  744 */     switch (jdbcType)
/*      */     {
/*      */     case -7:
/*      */     case 16:
/*  747 */       return "java.lang.Boolean";
/*      */     case -6:
/*      */     case 4:
/*      */     case 5:
/*  752 */       return "java.lang.Integer";
/*      */     case -5:
/*  755 */       return "java.lang.Long";
/*      */     case 2:
/*      */     case 3:
/*  759 */       return "java.math.BigDecimal";
/*      */     case 7:
/*  762 */       return "java.lang.Float";
/*      */     case 6:
/*      */     case 8:
/*  766 */       return "java.lang.Double";
/*      */     case 1:
/*      */     case 12:
/*  770 */       return "java.lang.String";
/*      */     case -3:
/*      */     case -2:
/*  774 */       return "[B";
/*      */     case -4:
/*      */     case 2004:
/*  778 */       return "java.sql.Blob";
/*      */     case -1:
/*      */     case 2005:
/*  782 */       return "java.sql.Clob";
/*      */     case 91:
/*  785 */       return "java.sql.Date";
/*      */     case 92:
/*  788 */       return "java.sql.Time";
/*      */     case 93:
/*  791 */       return "java.sql.Timestamp";
/*      */     }
/*      */ 
/*  794 */     return "java.lang.Object";
/*      */   }
/*      */ 
/*      */   static void embedData(StringBuffer buf, Object value, boolean isUnicode, ConnectionJDBC2 connection)
/*      */     throws SQLException
/*      */   {
/*  807 */     buf.append(' ');
/*  808 */     if (value == null) {
/*  809 */       buf.append("NULL ");
/*  810 */       return;
/*      */     }
/*      */ 
/*  813 */     if (value instanceof Blob) {
/*  814 */       Blob blob = (Blob)value;
/*      */ 
/*  816 */       value = blob.getBytes(1L, (int)blob.length());
/*  817 */     } else if (value instanceof Clob) {
/*  818 */       Clob clob = (Clob)value;
/*      */ 
/*  820 */       value = clob.getSubString(1L, (int)clob.length());
/*      */     }
/*      */ 
/*  823 */     if (value instanceof DateTime) {
/*  824 */       buf.append('\'');
/*  825 */       buf.append(value);
/*  826 */       buf.append('\'');
/*      */     }
/*      */     else
/*      */     {
/*      */       int len;
/*      */       int i;
/*  828 */       if (value instanceof byte[]) {
/*  829 */         byte[] bytes = (byte[])value;
/*      */ 
/*  831 */         len = bytes.length;
/*      */ 
/*  833 */         if (len >= 0) {
/*  834 */           buf.append('0').append('x');
/*  835 */           if ((len == 0) && (connection.getTdsVersion() < 3))
/*      */           {
/*  837 */             buf.append('0').append('0');
/*      */           }
/*      */           else for (i = 0; i < len; ++i) {
/*  840 */               int b1 = bytes[i] & 0xFF;
/*      */ 
/*  842 */               buf.append(hex[(b1 >> 4)]);
/*  843 */               buf.append(hex[(b1 & 0xF)]);
/*      */             }
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*      */         String tmp;
/*  847 */         if (value instanceof String) {
/*  848 */           tmp = (String)value;
/*  849 */           len = tmp.length();
/*      */ 
/*  851 */           if (isUnicode) {
/*  852 */             buf.append('N');
/*      */           }
/*  854 */           buf.append('\'');
/*      */ 
/*  856 */           for (i = 0; i < len; ++i) {
/*  857 */             char c = tmp.charAt(i);
/*      */ 
/*  859 */             if (c == '\'') {
/*  860 */               buf.append('\'');
/*      */             }
/*      */ 
/*  863 */             buf.append(c);
/*      */           }
/*      */ 
/*  866 */           buf.append('\'');
/*      */         }
/*      */         else
/*      */         {
/*      */           DateTime dt;
/*  867 */           if (value instanceof java.sql.Date) {
/*  868 */             dt = new DateTime((java.sql.Date)value);
/*  869 */             buf.append('\'');
/*  870 */             buf.append(dt);
/*  871 */             buf.append('\'');
/*  872 */           } else if (value instanceof Time) {
/*  873 */             dt = new DateTime((Time)value);
/*  874 */             buf.append('\'');
/*  875 */             buf.append(dt);
/*  876 */             buf.append('\'');
/*  877 */           } else if (value instanceof Timestamp) {
/*  878 */             dt = new DateTime((Timestamp)value);
/*  879 */             buf.append('\'');
/*  880 */             buf.append(dt);
/*  881 */             buf.append('\'');
/*  882 */           } else if (value instanceof Boolean) {
/*  883 */             buf.append((((Boolean)value).booleanValue()) ? '1' : '0');
/*  884 */           } else if (value instanceof BigDecimal)
/*      */           {
/*  891 */             tmp = value.toString();
/*  892 */             int maxlen = connection.getMaxPrecision();
/*  893 */             if (tmp.charAt(0) == '-') {
/*  894 */               ++maxlen;
/*      */             }
/*  896 */             if (tmp.indexOf(46) >= 0) {
/*  897 */               ++maxlen;
/*      */             }
/*  899 */             if (tmp.length() > maxlen)
/*  900 */               buf.append(tmp.substring(0, maxlen));
/*      */             else
/*  902 */               buf.append(tmp);
/*      */           }
/*      */           else {
/*  905 */             buf.append(value.toString()); } } }
/*      */     }
/*  907 */     buf.append(' ');
/*      */   }
/*      */ 
/*      */   static String getStatementKey(String sql, ParamInfo[] params, int serverType, String catalog, boolean autoCommit, boolean cursor)
/*      */   {
/*      */     StringBuffer key;
/*  927 */     if (serverType == 1) {
/*  928 */       key = new StringBuffer(1 + catalog.length() + sql.length() + 11 * params.length);
/*      */ 
/*  932 */       key.append((cursor) ? 'C' : 'X');
/*      */ 
/*  935 */       key.append(catalog);
/*      */ 
/*  937 */       key.append(sql);
/*      */ 
/*  941 */       for (int i = 0; i < params.length; ++i)
/*  942 */         key.append(params[i].sqlType);
/*      */     }
/*      */     else {
/*  945 */       key = new StringBuffer(sql.length() + 2);
/*      */ 
/*  948 */       key.append((autoCommit) ? 'T' : 'F');
/*      */ 
/*  950 */       key.append(sql);
/*      */     }
/*      */ 
/*  953 */     return key.toString();
/*      */   }
/*      */ 
/*      */   static String getParameterDefinitions(ParamInfo[] parameters)
/*      */   {
/*  965 */     StringBuffer sql = new StringBuffer(parameters.length * 15);
/*      */ 
/*  968 */     for (int i = 0; i < parameters.length; ++i) {
/*  969 */       if (parameters[i].name == null) {
/*  970 */         sql.append("@P");
/*  971 */         sql.append(i);
/*      */       } else {
/*  973 */         sql.append(parameters[i].name);
/*      */       }
/*      */ 
/*  976 */       sql.append(' ');
/*  977 */       sql.append(parameters[i].sqlType);
/*      */ 
/*  979 */       if (i + 1 < parameters.length) {
/*  980 */         sql.append(',');
/*      */       }
/*      */     }
/*      */ 
/*  984 */     return sql.toString();
/*      */   }
/*      */ 
/*      */   static String substituteParamMarkers(String sql, ParamInfo[] list)
/*      */   {
/*  999 */     char[] buf = new char[sql.length() + list.length * 7];
/* 1000 */     int bufferPtr = 0;
/* 1001 */     int start = 0;
/* 1002 */     StringBuffer number = new StringBuffer(4);
/*      */ 
/* 1004 */     for (int i = 0; i < list.length; ++i) {
/* 1005 */       int pos = list[i].markerPos;
/*      */ 
/* 1007 */       if (pos > 0) {
/* 1008 */         sql.getChars(start, pos, buf, bufferPtr);
/* 1009 */         bufferPtr += pos - start;
/* 1010 */         start = pos + 1;
/*      */ 
/* 1013 */         buf[(bufferPtr++)] = ' ';
/* 1014 */         buf[(bufferPtr++)] = '@';
/* 1015 */         buf[(bufferPtr++)] = 'P';
/*      */ 
/* 1020 */         number.setLength(0);
/* 1021 */         number.append(i);
/* 1022 */         number.getChars(0, number.length(), buf, bufferPtr);
/* 1023 */         bufferPtr += number.length();
/*      */ 
/* 1026 */         buf[(bufferPtr++)] = ' ';
/*      */       }
/*      */     }
/*      */ 
/* 1030 */     if (start < sql.length()) {
/* 1031 */       sql.getChars(start, sql.length(), buf, bufferPtr);
/* 1032 */       bufferPtr += sql.length() - start;
/*      */     }
/*      */ 
/* 1035 */     return new String(buf, 0, bufferPtr);
/*      */   }
/*      */ 
/*      */   static String substituteParameters(String sql, ParamInfo[] list, ConnectionJDBC2 connection)
/*      */     throws SQLException
/*      */   {
/* 1049 */     int len = sql.length();
/*      */ 
/* 1051 */     for (int i = 0; i < list.length; ++i) {
/* 1052 */       if ((!(list[i].isRetVal)) && (!(list[i].isSet)) && (!(list[i].isOutput))) {
/* 1053 */         throw new SQLException(Messages.get("error.prepare.paramnotset", Integer.toString(i + 1)), "07000");
/*      */       }
/*      */ 
/* 1058 */       Object value = list[i].value;
/*      */ 
/* 1060 */       if ((value instanceof InputStream) || (value instanceof Reader)) {
/*      */         try
/*      */         {
/* 1063 */           if ((list[i].jdbcType == -1) || (list[i].jdbcType == 2005) || (list[i].jdbcType == 12))
/*      */           {
/* 1067 */             value = list[i].getString("US-ASCII");
/*      */           }
/*      */           else value = list[i].getBytes("US-ASCII");
/*      */ 
/* 1072 */           list[i].value = value;
/*      */         } catch (IOException e) {
/* 1074 */           throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1080 */       if (value instanceof String)
/* 1081 */         len += ((String)value).length() + 5;
/* 1082 */       else if (value instanceof byte[])
/* 1083 */         len += ((byte[])value).length * 2 + 4;
/*      */       else {
/* 1085 */         len += 32;
/*      */       }
/*      */     }
/*      */ 
/* 1089 */     StringBuffer buf = new StringBuffer(len + 16);
/* 1090 */     int start = 0;
/*      */ 
/* 1092 */     for (int i = 0; i < list.length; ++i) {
/* 1093 */       int pos = list[i].markerPos;
/*      */ 
/* 1095 */       if (pos > 0) {
/* 1096 */         buf.append(sql.substring(start, list[i].markerPos));
/* 1097 */         start = pos + 1;
/* 1098 */         boolean isUnicode = (connection.getTdsVersion() >= 3) && (list[i].isUnicode);
/* 1099 */         embedData(buf, list[i].value, isUnicode, connection);
/*      */       }
/*      */     }
/*      */ 
/* 1103 */     if (start < sql.length()) {
/* 1104 */       buf.append(sql.substring(start));
/*      */     }
/*      */ 
/* 1107 */     return buf.toString();
/*      */   }
/*      */ 
/*      */   static byte[] encodeString(String cs, String value)
/*      */   {
/*      */     try
/*      */     {
/* 1119 */       return value.getBytes(cs); } catch (UnsupportedEncodingException e) {
/*      */     }
/* 1121 */     return value.getBytes();
/*      */   }
/*      */ 
/*      */   public static SQLWarning linkException(SQLWarning sqle, Throwable cause)
/*      */   {
/* 1136 */     return ((SQLWarning)linkException(sqle, cause));
/*      */   }
/*      */ 
/*      */   public static SQLException linkException(SQLException sqle, Throwable cause)
/*      */   {
/* 1150 */     return ((SQLException)linkException(sqle, cause));
/*      */   }
/*      */ 
/*      */   public static Throwable linkException(Exception exception, Throwable cause)
/*      */   {
/* 1166 */     Class exceptionClass = exception.getClass();
/* 1167 */     Class[] parameterTypes = { Throwable.class };
/* 1168 */     Object[] arguments = { cause };
/*      */     try
/*      */     {
/* 1171 */       Method initCauseMethod = exceptionClass.getMethod("initCause", parameterTypes);
/* 1172 */       initCauseMethod.invoke(exception, arguments);
/*      */     }
/*      */     catch (NoSuchMethodException e) {
/* 1175 */       if (Logger.isActive()) {
/* 1176 */         Logger.logException((Exception)cause);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */ 
/* 1183 */     return exception;
/*      */   }
/*      */ 
/*      */   public static long timeToZone(java.util.Date value, Calendar target)
/*      */   {
/* 1194 */     java.util.Date tmp = target.getTime();
/*      */     try {
/* 1196 */       GregorianCalendar cal = (GregorianCalendar)calendar.get();
/* 1197 */       cal.setTime(value);
/* 1198 */       if ((!(Driver.JDBC3)) && (value instanceof Timestamp))
/*      */       {
/* 1200 */         cal.set(14, ((Timestamp)value).getNanos() / 1000000);
/*      */       }
/*      */ 
/* 1203 */       target.set(11, cal.get(11));
/* 1204 */       target.set(12, cal.get(12));
/* 1205 */       target.set(13, cal.get(13));
/* 1206 */       target.set(14, cal.get(14));
/* 1207 */       target.set(1, cal.get(1));
/* 1208 */       target.set(2, cal.get(2));
/* 1209 */       target.set(5, cal.get(5));
/* 1210 */       long l = target.getTime().getTime();
/*      */ 
/* 1213 */       return l; } finally { target.setTime(tmp);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static long timeFromZone(java.util.Date value, Calendar target)
/*      */   {
/* 1224 */     java.util.Date tmp = target.getTime();
/*      */     try {
/* 1226 */       GregorianCalendar cal = (GregorianCalendar)calendar.get();
/* 1227 */       target.setTime(value);
/* 1228 */       if ((!(Driver.JDBC3)) && (value instanceof Timestamp))
/*      */       {
/* 1230 */         target.set(14, ((Timestamp)value).getNanos() / 1000000);
/*      */       }
/*      */ 
/* 1233 */       cal.set(11, target.get(11));
/* 1234 */       cal.set(12, target.get(12));
/* 1235 */       cal.set(13, target.get(13));
/* 1236 */       cal.set(14, target.get(14));
/* 1237 */       cal.set(1, target.get(1));
/* 1238 */       cal.set(2, target.get(2));
/* 1239 */       cal.set(5, target.get(5));
/* 1240 */       long l = cal.getTime().getTime();
/*      */ 
/* 1243 */       return l; } finally { target.setTime(tmp);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object convertLOB(Object value)
/*      */     throws SQLException
/*      */   {
/* 1259 */     if (value instanceof Clob) {
/* 1260 */       Clob c = (Clob)value;
/* 1261 */       return c.getSubString(1L, (int)c.length());
/*      */     }
/*      */ 
/* 1264 */     if (value instanceof Blob) {
/* 1265 */       Blob b = (Blob)value;
/* 1266 */       return b.getBytes(1L, (int)b.length());
/*      */     }
/*      */ 
/* 1269 */     return value;
/*      */   }
/*      */ 
/*      */   public static int convertLOBType(int type)
/*      */   {
/* 1285 */     switch (type)
/*      */     {
/*      */     case 2004:
/* 1287 */       return -4;
/*      */     case 2005:
/* 1289 */       return -1;
/*      */     }
/* 1291 */     return type;
/*      */   }
/*      */ 
/*      */   public static boolean isWindowsOS()
/*      */   {
/* 1303 */     return System.getProperty("os.name").toLowerCase().startsWith("windows");
/*      */   }
/*      */ 
/*      */   private static ConnectionJDBC2 getConnection(Object callerReference)
/*      */   {
/* 1318 */     if (callerReference == null) {
/* 1319 */       throw new IllegalArgumentException("callerReference cannot be null.");
/*      */     }
/*      */ 
/*      */     Connection connection;
/*      */     try
/*      */     {
/* 1325 */       if (callerReference instanceof Connection)
/* 1326 */         connection = (Connection)callerReference;
/* 1327 */       else if (callerReference instanceof Statement)
/* 1328 */         connection = ((Statement)callerReference).getConnection();
/* 1329 */       else if (callerReference instanceof ResultSet)
/* 1330 */         connection = ((ResultSet)callerReference).getStatement().getConnection();
/*      */       else
/* 1332 */         throw new IllegalArgumentException("callerReference is invalid.");
/*      */     }
/*      */     catch (SQLException e) {
/* 1335 */       throw new IllegalStateException(e.getMessage());
/*      */     }
/*      */ 
/* 1338 */     return ((ConnectionJDBC2)connection);
/*      */   }
/*      */ 
/*      */   static int calculateNamedPipeBufferSize(int tdsVersion, int packetSize)
/*      */   {
/* 1363 */     if (packetSize == 0) {
/* 1364 */       if (tdsVersion >= 3) {
/* 1365 */         return 4096;
/*      */       }
/*      */ 
/* 1368 */       return 512;
/*      */     }
/*      */ 
/* 1371 */     return packetSize;
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*   75 */     typeMap.put(Byte.class, new Integer(-6));
/*   76 */     typeMap.put(Short.class, new Integer(5));
/*   77 */     typeMap.put(Integer.class, new Integer(4));
/*   78 */     typeMap.put(Long.class, new Integer(-5));
/*   79 */     typeMap.put(Float.class, new Integer(7));
/*   80 */     typeMap.put(Double.class, new Integer(8));
/*   81 */     typeMap.put(BigDecimal.class, new Integer(3));
/*   82 */     typeMap.put(Boolean.class, new Integer(16));
/*   83 */     typeMap.put([B.class, new Integer(-3));
/*   84 */     typeMap.put(java.sql.Date.class, new Integer(91));
/*   85 */     typeMap.put(Time.class, new Integer(92));
/*   86 */     typeMap.put(Timestamp.class, new Integer(93));
/*   87 */     typeMap.put(BlobImpl.class, new Integer(-4));
/*   88 */     typeMap.put(ClobImpl.class, new Integer(-1));
/*   89 */     typeMap.put(String.class, new Integer(12));
/*   90 */     typeMap.put(Blob.class, new Integer(-4));
/*   91 */     typeMap.put(Clob.class, new Integer(-1));
/*      */ 
/*   97 */     hex = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
/*      */ 
/*  104 */     calendar = new ThreadLocal() {
/*      */       protected Object initialValue() {
/*  106 */         return new GregorianCalendar();
/*      */       }
/*      */     };
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.Support
 * JD-Core Version:    0.5.3
 */