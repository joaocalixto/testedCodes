/*      */ package net.sourceforge.jtds.jdbc;
/*      */ 
/*      */ import java.io.BufferedReader;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.InputStreamReader;
/*      */ import java.io.OutputStream;
/*      */ import java.io.Reader;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.math.BigDecimal;
/*      */ import java.math.BigInteger;
/*      */ import java.sql.SQLException;
/*      */ import net.sourceforge.jtds.util.BlobBuffer;
/*      */ 
/*      */ public class TdsData
/*      */ {
/*      */   private static final int SYBCHAR = 47;
/*      */   private static final int SYBVARCHAR = 39;
/*      */   private static final int SYBINTN = 38;
/*      */   private static final int SYBINT1 = 48;
/*      */   private static final int SYBDATE = 49;
/*      */   private static final int SYBTIME = 51;
/*      */   private static final int SYBINT2 = 52;
/*      */   private static final int SYBINT4 = 56;
/*      */   private static final int SYBINT8 = 127;
/*      */   private static final int SYBFLT8 = 62;
/*      */   private static final int SYBDATETIME = 61;
/*      */   private static final int SYBBIT = 50;
/*      */   private static final int SYBTEXT = 35;
/*      */   private static final int SYBNTEXT = 99;
/*      */   private static final int SYBIMAGE = 34;
/*      */   private static final int SYBMONEY4 = 122;
/*      */   private static final int SYBMONEY = 60;
/*      */   private static final int SYBDATETIME4 = 58;
/*      */   private static final int SYBREAL = 59;
/*      */   private static final int SYBBINARY = 45;
/*      */   private static final int SYBVOID = 31;
/*      */   private static final int SYBVARBINARY = 37;
/*      */   private static final int SYBNVARCHAR = 103;
/*      */   private static final int SYBBITN = 104;
/*      */   private static final int SYBNUMERIC = 108;
/*      */   private static final int SYBDECIMAL = 106;
/*      */   private static final int SYBFLTN = 109;
/*      */   private static final int SYBMONEYN = 110;
/*      */   private static final int SYBDATETIMN = 111;
/*      */   private static final int SYBDATEN = 123;
/*      */   private static final int SYBTIMEN = 147;
/*      */   private static final int XSYBCHAR = 175;
/*      */   private static final int XSYBVARCHAR = 167;
/*      */   private static final int XSYBNVARCHAR = 231;
/*      */   private static final int XSYBNCHAR = 239;
/*      */   private static final int XSYBVARBINARY = 165;
/*      */   private static final int XSYBBINARY = 173;
/*      */   private static final int SYBUNITEXT = 174;
/*      */   private static final int SYBLONGBINARY = 225;
/*      */   private static final int SYBSINT1 = 64;
/*      */   private static final int SYBUINT2 = 65;
/*      */   private static final int SYBUINT4 = 66;
/*      */   private static final int SYBUINT8 = 67;
/*      */   private static final int SYBUINTN = 68;
/*      */   private static final int SYBUNIQUE = 36;
/*      */   private static final int SYBVARIANT = 98;
/*      */   private static final int SYBSINT8 = 191;
/*      */   static final int SYBLONGDATA = 36;
/*      */   private static final int UDT_CHAR = 1;
/*      */   private static final int UDT_VARCHAR = 2;
/*      */   private static final int UDT_BINARY = 3;
/*      */   private static final int UDT_VARBINARY = 4;
/*      */   private static final int UDT_SYSNAME = 18;
/*      */   private static final int UDT_NCHAR = 24;
/*      */   private static final int UDT_NVARCHAR = 25;
/*      */   private static final int UDT_UNICHAR = 34;
/*      */   private static final int UDT_UNIVARCHAR = 35;
/*      */   private static final int UDT_UNITEXT = 36;
/*      */   private static final int UDT_LONGSYSNAME = 42;
/*      */   private static final int UDT_TIMESTAMP = 80;
/*      */   private static final int UDT_NEWSYSNAME = 256;
/*      */   private static final int VAR_MAX = 255;
/*      */   private static final int SYB_LONGVAR_MAX = 16384;
/*      */   private static final int MS_LONGVAR_MAX = 8000;
/*      */   private static final int SYB_CHUNK_SIZE = 8192;
/*  203 */   private static final TypeInfo[] types = new TypeInfo[256];
/*      */   static final int DEFAULT_SCALE = 10;
/*      */   static final int DEFAULT_PRECISION_28 = 28;
/*      */   static final int DEFAULT_PRECISION_38 = 38;
/*      */ 
/*      */   static int getCollation(ResponseStream in, ColInfo ci)
/*      */     throws IOException
/*      */   {
/*  273 */     if (isCollation(ci))
/*      */     {
/*  275 */       ci.collation = new byte[5];
/*  276 */       in.read(ci.collation);
/*      */ 
/*  278 */       return 5;
/*      */     }
/*      */ 
/*  281 */     return 0;
/*      */   }
/*      */ 
/*      */   static void setColumnCharset(ColInfo ci, ConnectionJDBC2 connection)
/*      */     throws SQLException
/*      */   {
/*  299 */     if (connection.isCharsetSpecified())
/*      */     {
/*  302 */       ci.charsetInfo = connection.getCharsetInfo(); } else {
/*  303 */       if (ci.collation == null) {
/*      */         return;
/*      */       }
/*  306 */       byte[] collation = ci.collation;
/*  307 */       byte[] defaultCollation = connection.getCollation();
/*      */ 
/*  310 */       for (int i = 0; i < 5; ++i) {
/*  311 */         if (collation[i] != defaultCollation[i]) {
/*      */           break;
/*      */         }
/*      */       }
/*      */ 
/*  316 */       if (i == 5)
/*  317 */         ci.charsetInfo = connection.getCharsetInfo();
/*      */       else
/*  319 */         ci.charsetInfo = CharsetInfo.getCharset(collation);
/*      */     }
/*      */   }
/*      */ 
/*      */   static int readType(ResponseStream in, ColInfo ci)
/*      */     throws IOException, ProtocolException
/*      */   {
/*  346 */     int tdsVersion = in.getTdsVersion();
/*  347 */     boolean isTds8 = tdsVersion >= 4;
/*  348 */     boolean isTds7 = tdsVersion >= 3;
/*  349 */     boolean isTds5 = tdsVersion == 2;
/*  350 */     boolean isTds42 = tdsVersion == 1;
/*  351 */     int bytesRead = 1;
/*      */ 
/*  353 */     int type = in.read();
/*      */ 
/*  355 */     if ((types[type] == null) || ((isTds5) && (type == 36)))
/*      */     {
/*  357 */       throw new ProtocolException("Invalid TDS data type 0x" + Integer.toHexString(type & 0xFF));
/*      */     }
/*      */ 
/*  360 */     ci.tdsType = type;
/*  361 */     ci.jdbcType = types[type].jdbcType;
/*  362 */     ci.bufferSize = types[type].size;
/*      */ 
/*  365 */     if (ci.bufferSize == -5)
/*      */     {
/*  368 */       ci.bufferSize = in.readInt();
/*  369 */       bytesRead += 4;
/*  370 */     } else if (ci.bufferSize == -4)
/*      */     {
/*  372 */       ci.bufferSize = in.readInt();
/*      */ 
/*  374 */       if (isTds8) {
/*  375 */         bytesRead += getCollation(in, ci);
/*      */       }
/*      */ 
/*  378 */       int lenName = in.readShort();
/*      */ 
/*  380 */       ci.tableName = in.readString(lenName);
/*  381 */       bytesRead += 6 + ((in.getTdsVersion() >= 3) ? lenName * 2 : lenName);
/*  382 */     } else if (ci.bufferSize == -2)
/*      */     {
/*  384 */       if ((isTds5) && (ci.tdsType == 175)) {
/*  385 */         ci.bufferSize = in.readInt();
/*  386 */         bytesRead += 4;
/*      */       } else {
/*  388 */         ci.bufferSize = in.readShort();
/*  389 */         bytesRead += 2;
/*      */       }
/*      */ 
/*  392 */       if (isTds8) {
/*  393 */         bytesRead += getCollation(in, ci);
/*      */       }
/*      */     }
/*  396 */     else if (ci.bufferSize == -1)
/*      */     {
/*  398 */       ++bytesRead;
/*  399 */       ci.bufferSize = in.read();
/*      */     }
/*      */ 
/*  403 */     ci.displaySize = types[type].displaySize;
/*  404 */     ci.precision = types[type].precision;
/*  405 */     ci.sqlType = types[type].sqlType;
/*      */ 
/*  408 */     switch (type)
/*      */     {
/*      */     case 61:
/*  413 */       ci.scale = 3;
/*  414 */       break;
/*      */     case 111:
/*  417 */       if (ci.bufferSize == 8) {
/*  418 */         ci.displaySize = types[61].displaySize;
/*  419 */         ci.precision = types[61].precision;
/*  420 */         ci.scale = 3;
/*      */       } else {
/*  422 */         ci.displaySize = types[58].displaySize;
/*  423 */         ci.precision = types[58].precision;
/*  424 */         ci.sqlType = types[58].sqlType;
/*  425 */         ci.scale = 0;
/*      */       }
/*  427 */       break;
/*      */     case 109:
/*  430 */       if (ci.bufferSize == 8) {
/*  431 */         ci.displaySize = types[62].displaySize;
/*  432 */         ci.precision = types[62].precision;
/*      */       } else {
/*  434 */         ci.displaySize = types[59].displaySize;
/*  435 */         ci.precision = types[59].precision;
/*  436 */         ci.jdbcType = 7;
/*  437 */         ci.sqlType = types[59].sqlType;
/*      */       }
/*  439 */       break;
/*      */     case 38:
/*  442 */       if (ci.bufferSize == 8) {
/*  443 */         ci.displaySize = types[127].displaySize;
/*  444 */         ci.precision = types[127].precision;
/*  445 */         ci.jdbcType = -5;
/*  446 */         ci.sqlType = types[127].sqlType;
/*  447 */       } else if (ci.bufferSize == 4) {
/*  448 */         ci.displaySize = types[56].displaySize;
/*  449 */         ci.precision = types[56].precision;
/*  450 */       } else if (ci.bufferSize == 2) {
/*  451 */         ci.displaySize = types[52].displaySize;
/*  452 */         ci.precision = types[52].precision;
/*  453 */         ci.jdbcType = 5;
/*  454 */         ci.sqlType = types[52].sqlType;
/*      */       } else {
/*  456 */         ci.displaySize = types[48].displaySize;
/*  457 */         ci.precision = types[48].precision;
/*  458 */         ci.jdbcType = -6;
/*  459 */         ci.sqlType = types[48].sqlType;
/*      */       }
/*  461 */       break;
/*      */     case 68:
/*  464 */       if (ci.bufferSize == 8) {
/*  465 */         ci.displaySize = types[67].displaySize;
/*  466 */         ci.precision = types[67].precision;
/*  467 */         ci.jdbcType = types[67].jdbcType;
/*  468 */         ci.sqlType = types[67].sqlType;
/*  469 */       } else if (ci.bufferSize == 4) {
/*  470 */         ci.displaySize = types[66].displaySize;
/*  471 */         ci.precision = types[66].precision;
/*  472 */       } else if (ci.bufferSize == 2) {
/*  473 */         ci.displaySize = types[65].displaySize;
/*  474 */         ci.precision = types[65].precision;
/*  475 */         ci.jdbcType = types[65].jdbcType;
/*  476 */         ci.sqlType = types[65].sqlType;
/*      */       } else {
/*  478 */         throw new ProtocolException("unsigned int null (size 1) not supported");
/*      */       }
/*      */     case 60:
/*      */     case 122:
/*  486 */       ci.scale = 4;
/*  487 */       break;
/*      */     case 110:
/*  490 */       if (ci.bufferSize == 8) {
/*  491 */         ci.displaySize = types[60].displaySize;
/*  492 */         ci.precision = types[60].precision;
/*      */       } else {
/*  494 */         ci.displaySize = types[122].displaySize;
/*  495 */         ci.precision = types[122].precision;
/*  496 */         ci.sqlType = types[122].sqlType;
/*      */       }
/*  498 */       ci.scale = 4;
/*  499 */       break;
/*      */     case 106:
/*      */     case 108:
/*  504 */       ci.precision = in.read();
/*  505 */       ci.scale = in.read();
/*  506 */       ci.displaySize = (((ci.scale > 0) ? 2 : 1) + ci.precision);
/*  507 */       bytesRead += 2;
/*  508 */       ci.sqlType = types[type].sqlType;
/*  509 */       break;
/*      */     case 34:
/*  513 */       ci.precision = 2147483647;
/*  514 */       ci.displaySize = 2147483647;
/*  515 */       break;
/*      */     case 37:
/*      */     case 45:
/*      */     case 165:
/*      */     case 173:
/*      */     case 225:
/*  522 */       ci.precision = ci.bufferSize;
/*  523 */       ci.displaySize = (ci.precision * 2);
/*  524 */       break;
/*      */     case 99:
/*  528 */       ci.precision = 1073741823;
/*  529 */       ci.displaySize = 1073741823;
/*  530 */       break;
/*      */     case 174:
/*  534 */       ci.precision = 1073741823;
/*  535 */       ci.displaySize = 1073741823;
/*  536 */       break;
/*      */     case 231:
/*      */     case 239:
/*  541 */       ci.displaySize = (ci.bufferSize / 2);
/*  542 */       ci.precision = ci.displaySize;
/*  543 */       break;
/*      */     case 35:
/*      */     case 39:
/*      */     case 47:
/*      */     case 103:
/*      */     case 167:
/*      */     case 175:
/*  552 */       ci.precision = ci.bufferSize;
/*  553 */       ci.displaySize = ci.precision;
/*      */     }
/*      */ 
/*  558 */     if (ci.isIdentity) {
/*  559 */       ci.sqlType += " identity";
/*      */     }
/*      */ 
/*  563 */     if ((isTds42) || (isTds5)) {
/*  564 */       switch (ci.userType)
/*      */       {
/*      */       case 1:
/*  566 */         ci.sqlType = "char";
/*  567 */         ci.displaySize = ci.bufferSize;
/*  568 */         ci.jdbcType = 1;
/*  569 */         break;
/*      */       case 2:
/*  571 */         ci.sqlType = "varchar";
/*  572 */         ci.displaySize = ci.bufferSize;
/*  573 */         ci.jdbcType = 12;
/*  574 */         break;
/*      */       case 3:
/*  576 */         ci.sqlType = "binary";
/*  577 */         ci.displaySize = (ci.bufferSize * 2);
/*  578 */         ci.jdbcType = -2;
/*  579 */         break;
/*      */       case 4:
/*  581 */         ci.sqlType = "varbinary";
/*  582 */         ci.displaySize = (ci.bufferSize * 2);
/*  583 */         ci.jdbcType = -3;
/*  584 */         break;
/*      */       case 18:
/*  586 */         ci.sqlType = "sysname";
/*  587 */         ci.displaySize = ci.bufferSize;
/*  588 */         ci.jdbcType = 12;
/*  589 */         break;
/*      */       case 80:
/*  591 */         ci.sqlType = "timestamp";
/*  592 */         ci.displaySize = (ci.bufferSize * 2);
/*  593 */         ci.jdbcType = -3;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  599 */     if (isTds5) {
/*  600 */       switch (ci.userType)
/*      */       {
/*      */       case 24:
/*  602 */         ci.sqlType = "nchar";
/*  603 */         ci.displaySize = ci.bufferSize;
/*  604 */         ci.jdbcType = 1;
/*  605 */         break;
/*      */       case 25:
/*  607 */         ci.sqlType = "nvarchar";
/*  608 */         ci.displaySize = ci.bufferSize;
/*  609 */         ci.jdbcType = 12;
/*  610 */         break;
/*      */       case 34:
/*  612 */         ci.sqlType = "unichar";
/*  613 */         ci.displaySize = (ci.bufferSize / 2);
/*  614 */         ci.precision = ci.displaySize;
/*  615 */         ci.jdbcType = 1;
/*  616 */         break;
/*      */       case 35:
/*  618 */         ci.sqlType = "univarchar";
/*  619 */         ci.displaySize = (ci.bufferSize / 2);
/*  620 */         ci.precision = ci.displaySize;
/*  621 */         ci.jdbcType = 12;
/*  622 */         break;
/*      */       case 42:
/*  624 */         ci.sqlType = "longsysname";
/*  625 */         ci.jdbcType = 12;
/*  626 */         ci.displaySize = ci.bufferSize;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  631 */     if (isTds7) {
/*  632 */       switch (ci.userType)
/*      */       {
/*      */       case 80:
/*  634 */         ci.sqlType = "timestamp";
/*  635 */         ci.jdbcType = -2;
/*  636 */         break;
/*      */       case 256:
/*  638 */         ci.sqlType = "sysname";
/*  639 */         ci.jdbcType = 12;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  645 */     return bytesRead;
/*      */   }
/*      */ 
/*      */   static Object readData(ConnectionJDBC2 connection, ResponseStream in, ColInfo ci)
/*      */     throws IOException, ProtocolException
/*      */   {
/*      */     int len;
/*      */     int dataLen;
/*      */     byte[] bytes;
/*  667 */     switch (ci.tdsType)
/*      */     {
/*      */     case 38:
/*  669 */       switch (in.read()) { case 1:
/*  671 */         return new Integer(in.read() & 0xFF);
/*      */       case 2:
/*  673 */         return new Integer(in.readShort());
/*      */       case 4:
/*  675 */         return new Integer(in.readInt());
/*      */       case 8:
/*  677 */         return new Long(in.readLong());
/*      */       case 3:
/*      */       case 5:
/*      */       case 6:
/*      */       case 7: } break;
/*      */     case 68:
/*  684 */       switch (in.read()) { case 1:
/*  686 */         return new Integer(in.read() & 0xFF);
/*      */       case 2:
/*  688 */         return new Integer(in.readShort() & 0xFFFF);
/*      */       case 4:
/*  690 */         return new Long(in.readInt() & 0xFFFFFFFF);
/*      */       case 8:
/*  692 */         return in.readUnsignedLong();
/*      */       case 3:
/*      */       case 5:
/*      */       case 6:
/*      */       case 7: } break;
/*      */     case 48:
/*  697 */       return new Integer(in.read() & 0xFF);
/*      */     case 52:
/*  700 */       return new Integer(in.readShort());
/*      */     case 56:
/*  703 */       return new Integer(in.readInt());
/*      */     case 127:
/*  707 */       return new Long(in.readLong());
/*      */     case 191:
/*  711 */       return new Long(in.readLong());
/*      */     case 65:
/*  715 */       return new Integer(in.readShort() & 0xFFFF);
/*      */     case 66:
/*  719 */       return new Long(in.readInt() & 0xFFFFFFFF);
/*      */     case 67:
/*  723 */       return in.readUnsignedLong();
/*      */     case 34:
/*  726 */       len = in.read();
/*      */ 
/*  728 */       if (len <= 0) break label2663;
/*  729 */       in.skip(24);
/*  730 */       dataLen = in.readInt();
/*      */ 
/*  732 */       if ((dataLen == 0) && (in.getTdsVersion() <= 2))
/*      */         break label2663;
/*      */       BlobImpl blob;
/*  737 */       if (dataLen <= connection.getLobBuffer())
/*      */       {
/*  741 */         byte[] data = new byte[dataLen];
/*  742 */         in.read(data);
/*  743 */         blob = new BlobImpl(connection, data);
/*      */       }
/*      */       else {
/*      */         try {
/*  747 */           blob = new BlobImpl(connection);
/*  748 */           OutputStream out = blob.setBinaryStream(1L);
/*  749 */           byte[] buffer = new byte[1024];
/*      */ 
/*  753 */           while (((result = in.read(buffer, 0, Math.min(dataLen, buffer.length))) != -1) && (dataLen != 0))
/*      */           {
/*      */             int result;
/*  754 */             out.write(buffer, 0, result);
/*  755 */             dataLen -= result;
/*      */           }
/*  757 */           out.close();
/*      */         }
/*      */         catch (SQLException e) {
/*  760 */           throw new IOException(e.getMessage());
/*      */         }
/*      */       }
/*  763 */       return blob;
/*      */     case 35:
/*  769 */       len = in.read();
/*      */ 
/*  771 */       if (len <= 0)
/*      */         break label2663;
/*      */       String charset;
/*  773 */       if (ci.charsetInfo != null)
/*  774 */         charset = ci.charsetInfo.getCharset();
/*      */       else {
/*  776 */         charset = connection.getCharset();
/*      */       }
/*  778 */       in.skip(24);
/*  779 */       int dataLen = in.readInt();
/*  780 */       if ((dataLen == 0) && (in.getTdsVersion() <= 2))
/*      */       {
/*      */         break label2663;
/*      */       }
/*      */ 
/*  785 */       ClobImpl clob = new ClobImpl(connection);
/*  786 */       BlobBuffer blobBuffer = clob.getBlobBuffer();
/*      */       BufferedReader rdr;
/*  787 */       if (dataLen <= connection.getLobBuffer())
/*      */       {
/*  791 */         rdr = new BufferedReader(new InputStreamReader(in.getInputStream(dataLen), charset), 1024);
/*      */ 
/*  796 */         byte[] data = new byte[dataLen * 2];
/*  797 */         int p = 0;
/*      */ 
/*  799 */         while ((c = rdr.read()) >= 0)
/*      */         {
/*      */           int c;
/*  800 */           data[(p++)] = (byte)c;
/*  801 */           data[(p++)] = (byte)(c >> 8);
/*      */         }
/*  803 */         rdr.close();
/*  804 */         blobBuffer.setBuffer(data, false);
/*  805 */         if ((p == 2) && (data[0] == 32) && (data[1] == 0) && (in.getTdsVersion() < 3))
/*      */         {
/*  808 */           p = 0;
/*      */         }
/*      */ 
/*  812 */         blobBuffer.setLength(p);
/*      */       }
/*      */       else {
/*  815 */         rdr = new BufferedReader(new InputStreamReader(in.getInputStream(dataLen), charset), 1024);
/*      */         try
/*      */         {
/*  821 */           OutputStream out = blobBuffer.setBinaryStream(1L, false);
/*      */ 
/*  823 */           while ((c = rdr.read()) >= 0)
/*      */           {
/*      */             int c;
/*  824 */             out.write(c);
/*  825 */             out.write(c >> 8);
/*      */           }
/*  827 */           out.close();
/*  828 */           rdr.close();
/*      */         }
/*      */         catch (SQLException e) {
/*  831 */           throw new IOException(e.getMessage());
/*      */         }
/*      */       }
/*  834 */       return clob;
/*      */     case 99:
/*      */     case 174:
/*  841 */       len = in.read();
/*      */ 
/*  843 */       if (len <= 0) break label2663;
/*  844 */       in.skip(24);
/*  845 */       dataLen = in.readInt();
/*  846 */       if ((dataLen == 0) && (in.getTdsVersion() <= 2))
/*      */       {
/*      */         break label2663;
/*      */       }
/*      */ 
/*  851 */       ClobImpl clob = new ClobImpl(connection);
/*  852 */       BlobBuffer blobBuffer = clob.getBlobBuffer();
/*  853 */       if (dataLen <= connection.getLobBuffer())
/*      */       {
/*  857 */         byte[] data = new byte[dataLen];
/*  858 */         in.read(data);
/*  859 */         blobBuffer.setBuffer(data, false);
/*  860 */         if ((dataLen == 2) && (data[0] == 32) && (data[1] == 0) && (in.getTdsVersion() == 2))
/*      */         {
/*  863 */           dataLen = 0;
/*      */         }
/*      */ 
/*  867 */         blobBuffer.setLength(dataLen);
/*      */       }
/*      */       else {
/*      */         try {
/*  871 */           OutputStream out = blobBuffer.setBinaryStream(1L, false);
/*  872 */           byte[] buffer = new byte[1024];
/*      */ 
/*  876 */           while (((result = in.read(buffer, 0, Math.min(dataLen, buffer.length))) != -1) && (dataLen != 0))
/*      */           {
/*      */             int result;
/*  877 */             out.write(buffer, 0, result);
/*  878 */             dataLen -= result;
/*      */           }
/*  880 */           out.close();
/*      */         }
/*      */         catch (SQLException e) {
/*  883 */           throw new IOException(e.getMessage());
/*      */         }
/*      */       }
/*  886 */       return clob;
/*      */     case 39:
/*      */     case 47:
/*  893 */       len = in.read();
/*      */ 
/*  895 */       if (len <= 0) break label2663;
/*  896 */       String value = in.readNonUnicodeString(len, (ci.charsetInfo == null) ? connection.getCharsetInfo() : ci.charsetInfo);
/*      */ 
/*  899 */       if ((len == 1) && (ci.tdsType == 39) && (in.getTdsVersion() < 3))
/*      */       {
/*  903 */         return ((" ".equals(value)) ? "" : value);
/*      */       }
/*      */ 
/*  906 */       return value;
/*      */     case 103:
/*  912 */       len = in.read();
/*      */ 
/*  914 */       if (len <= 0) break label2663;
/*  915 */       return in.readUnicodeString(len / 2);
/*      */     case 167:
/*      */     case 175:
/*  922 */       if (in.getTdsVersion() == 2)
/*      */       {
/*  924 */         len = in.readInt();
/*  925 */         if (len <= 0) break label2663;
/*  926 */         String tmp = in.readNonUnicodeString(len);
/*  927 */         if ((" ".equals(tmp)) && (!("char".equals(ci.sqlType)))) {
/*  928 */           tmp = "";
/*      */         }
/*  930 */         return tmp;
/*      */       }
/*      */ 
/*  934 */       len = in.readShort();
/*  935 */       if (len == -1) break label2663;
/*  936 */       return in.readNonUnicodeString(len, (ci.charsetInfo == null) ? connection.getCharsetInfo() : ci.charsetInfo);
/*      */     case 231:
/*      */     case 239:
/*  945 */       len = in.readShort();
/*      */ 
/*  947 */       if (len == -1) break label2663;
/*  948 */       return in.readUnicodeString(len / 2);
/*      */     case 37:
/*      */     case 45:
/*  955 */       len = in.read();
/*      */ 
/*  957 */       if (len <= 0) break label2663;
/*  958 */       bytes = new byte[len];
/*      */ 
/*  960 */       in.read(bytes);
/*      */ 
/*  962 */       return bytes;
/*      */     case 165:
/*      */     case 173:
/*  969 */       len = in.readShort();
/*      */ 
/*  971 */       if (len == -1) break label2663;
/*  972 */       bytes = new byte[len];
/*      */ 
/*  974 */       in.read(bytes);
/*      */ 
/*  976 */       return bytes;
/*      */     case 225:
/*  982 */       len = in.readInt();
/*  983 */       if (len == 0) break label2663;
/*  984 */       if (("unichar".equals(ci.sqlType)) || ("univarchar".equals(ci.sqlType)))
/*      */       {
/*  986 */         char[] buf = new char[len / 2];
/*  987 */         in.read(buf);
/*  988 */         if ((len & 0x1) != 0)
/*      */         {
/*  990 */           in.skip(1);
/*      */         }
/*  992 */         if ((len == 2) && (buf[0] == ' ')) {
/*  993 */           return "";
/*      */         }
/*  995 */         return new String(buf);
/*      */       }
/*      */ 
/*  998 */       bytes = new byte[len];
/*  999 */       in.read(bytes);
/* 1000 */       return bytes;
/*      */     case 60:
/*      */     case 110:
/*      */     case 122:
/* 1008 */       return getMoneyValue(in, ci.tdsType);
/*      */     case 58:
/*      */     case 61:
/*      */     case 111:
/* 1013 */       return getDatetimeValue(in, ci.tdsType);
/*      */     case 49:
/*      */     case 123:
/* 1017 */       len = (ci.tdsType == 123) ? in.read() : 4;
/* 1018 */       if (len == 4) {
/* 1019 */         return new DateTime(in.readInt(), -2147483648);
/*      */       }
/*      */ 
/* 1022 */       in.skip(len);
/*      */ 
/* 1024 */       break;
/*      */     case 51:
/*      */     case 147:
/* 1028 */       len = (ci.tdsType == 147) ? in.read() : 4;
/* 1029 */       if (len == 4) {
/* 1030 */         return new DateTime(-2147483648, in.readInt());
/*      */       }
/*      */ 
/* 1033 */       in.skip(len);
/*      */ 
/* 1035 */       break;
/*      */     case 50:
/* 1038 */       return ((in.read() != 0) ? Boolean.TRUE : Boolean.FALSE);
/*      */     case 104:
/* 1041 */       len = in.read();
/*      */ 
/* 1043 */       if (len <= 0) break label2663;
/* 1044 */       return ((in.read() != 0) ? Boolean.TRUE : Boolean.FALSE);
/*      */     case 59:
/* 1050 */       return new Float(Float.intBitsToFloat(in.readInt()));
/*      */     case 62:
/* 1053 */       return new Double(Double.longBitsToDouble(in.readLong()));
/*      */     case 109:
/* 1056 */       len = in.read();
/*      */ 
/* 1058 */       if (len == 4)
/* 1059 */         return new Float(Float.intBitsToFloat(in.readInt()));
/* 1060 */       if (len != 8) break label2663;
/* 1061 */       return new Double(Double.longBitsToDouble(in.readLong()));
/*      */     case 36:
/* 1067 */       len = in.read();
/*      */ 
/* 1069 */       if (len <= 0) break label2663;
/* 1070 */       bytes = new byte[len];
/*      */ 
/* 1072 */       in.read(bytes);
/*      */ 
/* 1074 */       return new UniqueIdentifier(bytes);
/*      */     case 106:
/*      */     case 108:
/* 1081 */       len = in.read();
/*      */ 
/* 1083 */       if (len <= 0) break label2663;
/* 1084 */       int sign = in.read();
/*      */ 
/* 1086 */       --len;
/* 1087 */       byte[] bytes = new byte[len];
/*      */       BigInteger bi;
/* 1090 */       if (in.getServerType() == 2)
/*      */       {
/* 1092 */         for (int i = 0; i < len; ++i) {
/* 1093 */           bytes[i] = (byte)in.read();
/*      */         }
/*      */ 
/* 1096 */         bi = new BigInteger((sign == 0) ? 1 : -1, bytes);
/*      */       } else {
/* 1098 */         while (len-- > 0) {
/* 1099 */           bytes[len] = (byte)in.read();
/*      */         }
/*      */ 
/* 1102 */         bi = new BigInteger((sign == 0) ? -1 : 1, bytes);
/*      */       }
/*      */ 
/* 1105 */       return new BigDecimal(bi, ci.scale);
/*      */     case 98:
/* 1111 */       return getVariant(connection, in);
/*      */     case 40:
/*      */     case 41:
/*      */     case 42:
/*      */     case 43:
/*      */     case 44:
/*      */     case 46:
/*      */     case 53:
/*      */     case 54:
/*      */     case 55:
/*      */     case 57:
/*      */     case 63:
/*      */     case 64:
/*      */     case 69:
/*      */     case 70:
/*      */     case 71:
/*      */     case 72:
/*      */     case 73:
/*      */     case 74:
/*      */     case 75:
/*      */     case 76:
/*      */     case 77:
/*      */     case 78:
/*      */     case 79:
/*      */     case 80:
/*      */     case 81:
/*      */     case 82:
/*      */     case 83:
/*      */     case 84:
/*      */     case 85:
/*      */     case 86:
/*      */     case 87:
/*      */     case 88:
/*      */     case 89:
/*      */     case 90:
/*      */     case 91:
/*      */     case 92:
/*      */     case 93:
/*      */     case 94:
/*      */     case 95:
/*      */     case 96:
/*      */     case 97:
/*      */     case 100:
/*      */     case 101:
/*      */     case 102:
/*      */     case 105:
/*      */     case 107:
/*      */     case 112:
/*      */     case 113:
/*      */     case 114:
/*      */     case 115:
/*      */     case 116:
/*      */     case 117:
/*      */     case 118:
/*      */     case 119:
/*      */     case 120:
/*      */     case 121:
/*      */     case 124:
/*      */     case 125:
/*      */     case 126:
/*      */     case 128:
/*      */     case 129:
/*      */     case 130:
/*      */     case 131:
/*      */     case 132:
/*      */     case 133:
/*      */     case 134:
/*      */     case 135:
/*      */     case 136:
/*      */     case 137:
/*      */     case 138:
/*      */     case 139:
/*      */     case 140:
/*      */     case 141:
/*      */     case 142:
/*      */     case 143:
/*      */     case 144:
/*      */     case 145:
/*      */     case 146:
/*      */     case 148:
/*      */     case 149:
/*      */     case 150:
/*      */     case 151:
/*      */     case 152:
/*      */     case 153:
/*      */     case 154:
/*      */     case 155:
/*      */     case 156:
/*      */     case 157:
/*      */     case 158:
/*      */     case 159:
/*      */     case 160:
/*      */     case 161:
/*      */     case 162:
/*      */     case 163:
/*      */     case 164:
/*      */     case 166:
/*      */     case 168:
/*      */     case 169:
/*      */     case 170:
/*      */     case 171:
/*      */     case 172:
/*      */     case 176:
/*      */     case 177:
/*      */     case 178:
/*      */     case 179:
/*      */     case 180:
/*      */     case 181:
/*      */     case 182:
/*      */     case 183:
/*      */     case 184:
/*      */     case 185:
/*      */     case 186:
/*      */     case 187:
/*      */     case 188:
/*      */     case 189:
/*      */     case 190:
/*      */     case 192:
/*      */     case 193:
/*      */     case 194:
/*      */     case 195:
/*      */     case 196:
/*      */     case 197:
/*      */     case 198:
/*      */     case 199:
/*      */     case 200:
/*      */     case 201:
/*      */     case 202:
/*      */     case 203:
/*      */     case 204:
/*      */     case 205:
/*      */     case 206:
/*      */     case 207:
/*      */     case 208:
/*      */     case 209:
/*      */     case 210:
/*      */     case 211:
/*      */     case 212:
/*      */     case 213:
/*      */     case 214:
/*      */     case 215:
/*      */     case 216:
/*      */     case 217:
/*      */     case 218:
/*      */     case 219:
/*      */     case 220:
/*      */     case 221:
/*      */     case 222:
/*      */     case 223:
/*      */     case 224:
/*      */     case 226:
/*      */     case 227:
/*      */     case 228:
/*      */     case 229:
/*      */     case 230:
/*      */     case 232:
/*      */     case 233:
/*      */     case 234:
/*      */     case 235:
/*      */     case 236:
/*      */     case 237:
/*      */     case 238:
/*      */     default:
/* 1114 */       throw new ProtocolException("Unsupported TDS data type 0x" + Integer.toHexString(ci.tdsType & 0xFF));
/*      */     }
/*      */ 
/* 1118 */     label2663: return null;
/*      */   }
/*      */ 
/*      */   static boolean isSigned(ColInfo ci)
/*      */   {
/* 1128 */     int type = ci.tdsType;
/*      */ 
/* 1130 */     if ((type < 0) || (type > 255) || (types[type] == null)) {
/* 1131 */       throw new IllegalArgumentException("TDS data type " + type + " invalid");
/*      */     }
/*      */ 
/* 1134 */     if ((type == 38) && (ci.bufferSize == 1)) {
/* 1135 */       type = 48;
/*      */     }
/* 1137 */     return types[type].isSigned;
/*      */   }
/*      */ 
/*      */   static boolean isCollation(ColInfo ci)
/*      */   {
/* 1149 */     int type = ci.tdsType;
/*      */ 
/* 1151 */     if ((type < 0) || (type > 255) || (types[type] == null)) {
/* 1152 */       throw new IllegalArgumentException("TDS data type " + type + " invalid");
/*      */     }
/*      */ 
/* 1156 */     return types[type].isCollation;
/*      */   }
/*      */ 
/*      */   static boolean isCurrency(ColInfo ci)
/*      */   {
/* 1166 */     int type = ci.tdsType;
/*      */ 
/* 1168 */     if ((type < 0) || (type > 255) || (types[type] == null)) {
/* 1169 */       throw new IllegalArgumentException("TDS data type " + type + " invalid");
/*      */     }
/*      */ 
/* 1173 */     return ((type == 60) || (type == 122) || (type == 110));
/*      */   }
/*      */ 
/*      */   static boolean isSearchable(ColInfo ci)
/*      */   {
/* 1183 */     int type = ci.tdsType;
/*      */ 
/* 1185 */     if ((type < 0) || (type > 255) || (types[type] == null)) {
/* 1186 */       throw new IllegalArgumentException("TDS data type " + type + " invalid");
/*      */     }
/*      */ 
/* 1190 */     return (types[type].size != -4);
/*      */   }
/*      */ 
/*      */   static boolean isUnicode(ColInfo ci)
/*      */   {
/* 1200 */     int type = ci.tdsType;
/*      */ 
/* 1202 */     if ((type < 0) || (type > 255) || (types[type] == null)) {
/* 1203 */       throw new IllegalArgumentException("TDS data type " + type + " invalid");
/*      */     }
/*      */ 
/* 1207 */     switch (type)
/*      */     {
/*      */     case 98:
/*      */     case 99:
/*      */     case 103:
/*      */     case 175:
/*      */     case 231:
/*      */     case 239:
/* 1214 */       return true;
/*      */     }
/*      */ 
/* 1217 */     return false;
/*      */   }
/*      */ 
/*      */   static void fillInType(ColInfo ci)
/*      */     throws SQLException
/*      */   {
/* 1229 */     switch (ci.jdbcType)
/*      */     {
/*      */     case 12:
/* 1231 */       ci.tdsType = 39;
/* 1232 */       ci.bufferSize = 8000;
/* 1233 */       ci.displaySize = 8000;
/* 1234 */       ci.precision = 8000;
/* 1235 */       break;
/*      */     case 4:
/* 1237 */       ci.tdsType = 56;
/* 1238 */       ci.bufferSize = 4;
/* 1239 */       ci.displaySize = 11;
/* 1240 */       ci.precision = 10;
/* 1241 */       break;
/*      */     case 5:
/* 1243 */       ci.tdsType = 52;
/* 1244 */       ci.bufferSize = 2;
/* 1245 */       ci.displaySize = 6;
/* 1246 */       ci.precision = 5;
/* 1247 */       break;
/*      */     case -7:
/* 1249 */       ci.tdsType = 50;
/* 1250 */       ci.bufferSize = 1;
/* 1251 */       ci.displaySize = 1;
/* 1252 */       ci.precision = 1;
/* 1253 */       break;
/*      */     default:
/* 1255 */       throw new SQLException(Messages.get("error.baddatatype", Integer.toString(ci.jdbcType)), "HY000");
/*      */     }
/*      */ 
/* 1259 */     ci.sqlType = types[ci.tdsType].sqlType;
/* 1260 */     ci.scale = 0;
/*      */   }
/*      */ 
/*      */   static void getNativeType(ConnectionJDBC2 connection, ParamInfo pi)
/*      */     throws SQLException
/*      */   {
/* 1272 */     int jdbcType = pi.jdbcType;
/*      */ 
/* 1274 */     if (jdbcType == 1111)
/* 1275 */       jdbcType = Support.getJdbcType(pi.value);
/*      */     int len;
/* 1278 */     switch (jdbcType)
/*      */     {
/*      */     case -1:
/*      */     case 1:
/*      */     case 12:
/*      */     case 2005:
/* 1283 */       if (pi.value == null)
/* 1284 */         len = 0;
/*      */       else {
/* 1286 */         len = pi.length;
/*      */       }
/* 1288 */       if (connection.getTdsVersion() < 3) {
/* 1289 */         String charset = connection.getCharset();
/* 1290 */         if ((len > 0) && (((len <= 8192) || (connection.getSybaseInfo(32)))) && (connection.getSybaseInfo(16)) && (connection.getUseUnicode()) && (!("UTF-8".equals(charset))))
/*      */         {
/*      */           try
/*      */           {
/* 1312 */             String tmp = pi.getString(charset);
/* 1313 */             if (!(canEncode(tmp, charset)))
/*      */             {
/* 1315 */               pi.length = tmp.length();
/* 1316 */               if (pi.length > 8192) {
/* 1317 */                 pi.sqlType = "unitext";
/* 1318 */                 pi.tdsType = 36;
/*      */               } else {
/* 1320 */                 pi.sqlType = "univarchar(" + pi.length + ')';
/* 1321 */                 pi.tdsType = 225;
/*      */               }
/* 1323 */               return;
/*      */             }
/*      */           } catch (IOException e) {
/* 1326 */             throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1334 */         if ((connection.isWideChar()) && (len <= 16384)) {
/*      */           try {
/* 1336 */             byte[] tmp = pi.getBytes(charset);
/* 1337 */             len = (tmp == null) ? 0 : tmp.length;
/*      */           } catch (IOException tmp) {
/* 1339 */             throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */           }
/*      */         }
/*      */ 
/* 1343 */         if (len <= 255) {
/* 1344 */           pi.tdsType = 39;
/* 1345 */           pi.sqlType = "varchar(255)";
/*      */         }
/* 1347 */         else if (connection.getSybaseInfo(1)) {
/* 1348 */           if (len > 16384)
/*      */           {
/* 1352 */             pi.tdsType = 36;
/* 1353 */             pi.sqlType = "text";
/*      */           }
/*      */           else
/*      */           {
/* 1357 */             pi.tdsType = 175;
/* 1358 */             pi.sqlType = "varchar(" + len + ')';
/*      */           }
/*      */         } else {
/* 1361 */           pi.tdsType = 35;
/* 1362 */           pi.sqlType = "text"; } return;
/*      */       }
/*      */ 
/* 1366 */       if ((pi.isUnicode) && (len <= 4000)) {
/* 1367 */         pi.tdsType = 231;
/* 1368 */         pi.sqlType = "nvarchar(4000)"; return; }
/* 1369 */       if ((!(pi.isUnicode)) && (len <= 8000)) {
/* 1370 */         CharsetInfo csi = connection.getCharsetInfo();
/*      */         try {
/* 1372 */           if ((len > 0) && (csi.isWideChars()) && (pi.getBytes(csi.getCharset()).length > 8000)) {
/* 1373 */             pi.tdsType = 35;
/* 1374 */             pi.sqlType = "text";
/*      */           } else {
/* 1376 */             pi.tdsType = 167;
/* 1377 */             pi.sqlType = "varchar(8000)";
/*      */           }
/*      */         } catch (IOException tmp) {
/* 1380 */           throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */         }
/* 1382 */         return;
/*      */       }
/* 1384 */       if (pi.isOutput) {
/* 1385 */         throw new SQLException(Messages.get("error.textoutparam"), "HY000");
/*      */       }
/*      */ 
/* 1389 */       if (pi.isUnicode) {
/* 1390 */         pi.tdsType = 99;
/* 1391 */         pi.sqlType = "ntext"; return;
/*      */       }
/* 1393 */       pi.tdsType = 35;
/* 1394 */       pi.sqlType = "text";
/*      */ 
/* 1398 */       break;
/*      */     case -6:
/*      */     case 4:
/*      */     case 5:
/* 1403 */       pi.tdsType = 38;
/* 1404 */       pi.sqlType = "int";
/* 1405 */       break;
/*      */     case -7:
/*      */     case 16:
/* 1409 */       if ((connection.getTdsVersion() >= 3) || (connection.getSybaseInfo(4)))
/*      */       {
/* 1411 */         pi.tdsType = 104;
/*      */       }
/*      */       else pi.tdsType = 50;
/*      */ 
/* 1416 */       pi.sqlType = "bit";
/* 1417 */       break;
/*      */     case 7:
/* 1420 */       pi.tdsType = 109;
/* 1421 */       pi.sqlType = "real";
/* 1422 */       break;
/*      */     case 6:
/*      */     case 8:
/* 1426 */       pi.tdsType = 109;
/* 1427 */       pi.sqlType = "float";
/* 1428 */       break;
/*      */     case 91:
/* 1431 */       if (connection.getSybaseInfo(2)) {
/* 1432 */         pi.tdsType = 123;
/* 1433 */         pi.sqlType = "date"; return;
/*      */       }
/* 1435 */       pi.tdsType = 111;
/* 1436 */       pi.sqlType = "datetime";
/*      */ 
/* 1438 */       break;
/*      */     case 92:
/* 1440 */       if (connection.getSybaseInfo(2)) {
/* 1441 */         pi.tdsType = 147;
/* 1442 */         pi.sqlType = "time"; return;
/*      */       }
/* 1444 */       pi.tdsType = 111;
/* 1445 */       pi.sqlType = "datetime";
/*      */ 
/* 1447 */       break;
/*      */     case 93:
/* 1449 */       pi.tdsType = 111;
/* 1450 */       pi.sqlType = "datetime";
/* 1451 */       break;
/*      */     case -4:
/*      */     case -3:
/*      */     case -2:
/*      */     case 2004:
/* 1457 */       if (pi.value == null)
/* 1458 */         len = 0;
/*      */       else {
/* 1460 */         len = pi.length;
/*      */       }
/*      */ 
/* 1463 */       if (connection.getTdsVersion() < 3) {
/* 1464 */         if (len <= 255) {
/* 1465 */           pi.tdsType = 37;
/* 1466 */           pi.sqlType = "varbinary(255)"; return;
/*      */         }
/* 1468 */         if (connection.getSybaseInfo(1)) {
/* 1469 */           if (len > 16384)
/*      */           {
/* 1471 */             pi.tdsType = 36;
/* 1472 */             pi.sqlType = "image"; return;
/*      */           }
/*      */ 
/* 1475 */           pi.tdsType = 225;
/* 1476 */           pi.sqlType = "varbinary(" + len + ')'; return;
/*      */         }
/*      */ 
/* 1480 */         pi.tdsType = 34;
/* 1481 */         pi.sqlType = "image"; return;
/*      */       }
/*      */ 
/* 1485 */       if (len <= 8000) {
/* 1486 */         pi.tdsType = 165;
/* 1487 */         pi.sqlType = "varbinary(8000)"; return;
/*      */       }
/* 1489 */       if (pi.isOutput) {
/* 1490 */         throw new SQLException(Messages.get("error.textoutparam"), "HY000");
/*      */       }
/*      */ 
/* 1494 */       pi.tdsType = 34;
/* 1495 */       pi.sqlType = "varbinary(max)";
/*      */ 
/* 1499 */       break;
/*      */     case -5:
/* 1502 */       if ((connection.getTdsVersion() >= 4) || (connection.getSybaseInfo(64)))
/*      */       {
/* 1504 */         pi.tdsType = 38;
/* 1505 */         pi.sqlType = "bigint"; return;
/*      */       }
/*      */ 
/* 1508 */       pi.tdsType = 106;
/* 1509 */       pi.sqlType = "decimal(" + connection.getMaxPrecision() + ')';
/* 1510 */       pi.scale = 0;
/*      */ 
/* 1513 */       break;
/*      */     case 2:
/*      */     case 3:
/* 1517 */       pi.tdsType = 106;
/* 1518 */       int prec = connection.getMaxPrecision();
/* 1519 */       int scale = 10;
/* 1520 */       if (pi.value instanceof BigDecimal)
/* 1521 */         scale = ((BigDecimal)pi.value).scale();
/* 1522 */       else if ((pi.scale >= 0) && (pi.scale <= prec)) {
/* 1523 */         scale = pi.scale;
/*      */       }
/* 1525 */       pi.sqlType = "decimal(" + prec + ',' + scale + ')';
/*      */ 
/* 1527 */       break;
/*      */     case 0:
/*      */     case 1111:
/* 1532 */       pi.tdsType = 39;
/* 1533 */       pi.sqlType = "varchar(255)";
/* 1534 */       break;
/*      */     default:
/* 1537 */       throw new SQLException(Messages.get("error.baddatatype", Integer.toString(pi.jdbcType)), "HY000");
/*      */     }
/*      */   }
/*      */ 
/*      */   static int getTds5ParamSize(String charset, boolean isWideChar, ParamInfo pi, boolean useParamNames)
/*      */   {
/* 1556 */     int size = 8;
/* 1557 */     if ((pi.name != null) && (useParamNames))
/*      */     {
/* 1559 */       if (isWideChar) {
/* 1560 */         byte[] buf = Support.encodeString(charset, pi.name);
/*      */ 
/* 1562 */         size += buf.length;
/*      */       } else {
/* 1564 */         size += pi.name.length();
/*      */       }
/*      */     }
/*      */ 
/* 1568 */     switch (pi.tdsType)
/*      */     {
/*      */     case 37:
/*      */     case 38:
/*      */     case 39:
/*      */     case 109:
/*      */     case 111:
/*      */     case 123:
/*      */     case 147:
/* 1576 */       ++size;
/* 1577 */       break;
/*      */     case 36:
/*      */     case 106:
/* 1580 */       size += 3;
/* 1581 */       break;
/*      */     case 175:
/*      */     case 225:
/* 1584 */       size += 4;
/* 1585 */       break;
/*      */     case 50:
/* 1587 */       break;
/*      */     default:
/* 1589 */       throw new IllegalStateException("Unsupported output TDS type 0x" + Integer.toHexString(pi.tdsType));
/*      */     }
/*      */ 
/* 1593 */     return size;
/*      */   }
/*      */ 
/*      */   static void writeTds5ParamFmt(RequestStream out, String charset, boolean isWideChar, ParamInfo pi, boolean useParamNames)
/*      */     throws IOException
/*      */   {
/* 1612 */     if ((pi.name != null) && (useParamNames))
/*      */     {
/* 1614 */       if (isWideChar) {
/* 1615 */         byte[] buf = Support.encodeString(charset, pi.name);
/*      */ 
/* 1617 */         out.write((byte)buf.length);
/* 1618 */         out.write(buf);
/*      */       } else {
/* 1620 */         out.write((byte)pi.name.length());
/* 1621 */         out.write(pi.name);
/*      */       }
/*      */     }
/*      */     else out.write(0);
/*      */ 
/* 1627 */     out.write((byte)((pi.isOutput) ? 1 : 0));
/* 1628 */     if (pi.sqlType.startsWith("univarchar"))
/* 1629 */       out.write(35);
/* 1630 */     else if ("unitext".equals(pi.sqlType))
/* 1631 */       out.write(36);
/*      */     else {
/* 1633 */       out.write(0);
/*      */     }
/* 1635 */     out.write((byte)pi.tdsType);
/*      */ 
/* 1638 */     switch (pi.tdsType)
/*      */     {
/*      */     case 37:
/*      */     case 39:
/* 1641 */       out.write(-1);
/* 1642 */       break;
/*      */     case 175:
/* 1644 */       out.write(2147483647);
/* 1645 */       break;
/*      */     case 36:
/* 1650 */       out.write(("text".equals(pi.sqlType)) ? 3 : 4);
/* 1651 */       out.write(0);
/* 1652 */       out.write(0);
/* 1653 */       break;
/*      */     case 225:
/* 1655 */       out.write(2147483647);
/* 1656 */       break;
/*      */     case 50:
/* 1658 */       break;
/*      */     case 38:
/* 1660 */       out.write(("bigint".equals(pi.sqlType)) ? 8 : 4);
/* 1661 */       break;
/*      */     case 109:
/* 1663 */       if (pi.value instanceof Float)
/* 1664 */         out.write(4);
/*      */       else {
/* 1666 */         out.write(8);
/*      */       }
/* 1668 */       break;
/*      */     case 111:
/* 1670 */       out.write(8);
/* 1671 */       break;
/*      */     case 123:
/*      */     case 147:
/* 1674 */       out.write(4);
/* 1675 */       break;
/*      */     case 106:
/* 1677 */       out.write(17);
/* 1678 */       out.write(38);
/*      */ 
/* 1680 */       if (pi.jdbcType == -5) {
/* 1681 */         out.write(0);
/*      */       }
/* 1683 */       else if (pi.value instanceof BigDecimal) {
/* 1684 */         out.write((byte)((BigDecimal)pi.value).scale());
/*      */       }
/* 1686 */       else if ((pi.scale >= 0) && (pi.scale <= 38))
/* 1687 */         out.write((byte)pi.scale);
/*      */       else {
/* 1689 */         out.write(10);
/*      */       }
/*      */ 
/* 1694 */       break;
/*      */     default:
/* 1696 */       throw new IllegalStateException("Unsupported output TDS type " + Integer.toHexString(pi.tdsType));
/*      */     }
/*      */ 
/* 1700 */     out.write(0);
/*      */   }
/*      */ 
/*      */   static void writeTds5Param(RequestStream out, CharsetInfo charsetInfo, ParamInfo pi)
/*      */     throws IOException, SQLException
/*      */   {
/* 1717 */     if (pi.charsetInfo == null)
/* 1718 */       pi.charsetInfo = charsetInfo;
/*      */     byte[] buf;
/* 1720 */     switch (pi.tdsType)
/*      */     {
/*      */     case 39:
/* 1723 */       if (pi.value == null) {
/* 1724 */         out.write(0); return;
/*      */       }
/* 1726 */       buf = pi.getBytes(pi.charsetInfo.getCharset());
/*      */ 
/* 1728 */       if (buf.length == 0) {
/* 1729 */         buf = new byte[1];
/* 1730 */         buf[0] = 32;
/*      */       }
/*      */ 
/* 1733 */       if (buf.length > 255) {
/* 1734 */         throw new SQLException(Messages.get("error.generic.truncmbcs"), "HY000");
/*      */       }
/*      */ 
/* 1738 */       out.write((byte)buf.length);
/* 1739 */       out.write(buf);
/*      */ 
/* 1742 */       break;
/*      */     case 37:
/* 1745 */       if (pi.value == null) {
/* 1746 */         out.write(0); return;
/*      */       }
/* 1748 */       buf = pi.getBytes(pi.charsetInfo.getCharset());
/* 1749 */       if ((out.getTdsVersion() < 3) && (buf.length == 0))
/*      */       {
/* 1751 */         out.write(1); out.write(0); break label276:
/*      */       }
/* 1753 */       out.write((byte)buf.length);
/* 1754 */       out.write(buf);
/*      */ 
/* 1758 */       break;
/*      */     case 175:
/* 1761 */       if (pi.value == null) {
/* 1762 */         out.write(0); return;
/*      */       }
/* 1764 */       buf = pi.getBytes(pi.charsetInfo.getCharset());
/*      */ 
/* 1766 */       if (buf.length == 0) {
/* 1767 */         buf = new byte[1];
/* 1768 */         buf[0] = 32;
/*      */       }
/* 1770 */       out.write(buf.length);
/* 1771 */       out.write(buf);
/*      */ 
/* 1773 */       break;
/*      */     case 36:
/* 1779 */       out.write(0);
/* 1780 */       out.write(0);
/* 1781 */       out.write(0);
/*      */       int len;
/* 1785 */       if (pi.value instanceof InputStream) {
/* 1786 */         byte[] buffer = new byte[8192];
/* 1787 */         len = ((InputStream)pi.value).read(buffer);
/* 1788 */         while (len > 0) {
/* 1789 */           out.write((byte)len);
/* 1790 */           out.write((byte)(len >> 8));
/* 1791 */           out.write((byte)(len >> 16));
/* 1792 */           out.write((byte)(len >> 24 | 0x80));
/* 1793 */           out.write(buffer, 0, len);
/* 1794 */           len = ((InputStream)pi.value).read(buffer);
/*      */         }
/*      */ 
/*      */       }
/* 1800 */       else if ((pi.value instanceof Reader) && (!(pi.charsetInfo.isWideChars())))
/*      */       {
/* 1804 */         char[] buffer = new char[8192];
/* 1805 */         len = ((Reader)pi.value).read(buffer);
/* 1806 */         while (len > 0) {
/* 1807 */           out.write((byte)len);
/* 1808 */           out.write((byte)(len >> 8));
/* 1809 */           out.write((byte)(len >> 16));
/* 1810 */           out.write((byte)(len >> 24 | 0x80));
/* 1811 */           out.write(Support.encodeString(pi.charsetInfo.getCharset(), new String(buffer, 0, len)));
/*      */ 
/* 1814 */           len = ((Reader)pi.value).read(buffer);
/*      */         }
/*      */ 
/*      */       }
/* 1820 */       else if (pi.value != null)
/*      */       {
/*      */         int pos;
/* 1825 */         if ("unitext".equals(pi.sqlType))
/*      */         {
/* 1827 */           String buf = pi.getString(pi.charsetInfo.getCharset());
/* 1828 */           pos = 0;
/* 1829 */           while (pos < buf.length()) {
/* 1830 */             int clen = (buf.length() - pos >= 4096) ? 4096 : buf.length() - pos;
/*      */ 
/* 1832 */             int len = clen * 2;
/* 1833 */             out.write((byte)len);
/* 1834 */             out.write((byte)(len >> 8));
/* 1835 */             out.write((byte)(len >> 16));
/* 1836 */             out.write((byte)(len >> 24 | 0x80));
/*      */ 
/* 1838 */             out.write(buf.substring(pos, pos + clen).toCharArray(), 0, clen);
/* 1839 */             pos += clen;
/*      */           }
/*      */         }
/*      */         else {
/* 1843 */           buf = pi.getBytes(pi.charsetInfo.getCharset());
/* 1844 */           pos = 0;
/* 1845 */           while (pos < buf.length) {
/* 1846 */             int len = (buf.length - pos >= 8192) ? 8192 : buf.length - pos;
/*      */ 
/* 1848 */             out.write((byte)len);
/* 1849 */             out.write((byte)(len >> 8));
/* 1850 */             out.write((byte)(len >> 16));
/* 1851 */             out.write((byte)(len >> 24 | 0x80));
/*      */ 
/* 1853 */             for (int i = 0; i < len; ++i) {
/* 1854 */               out.write(buf[(pos++)]);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 1860 */       out.write(0);
/* 1861 */       break;
/*      */     case 225:
/* 1865 */       if (pi.value == null) {
/* 1866 */         out.write(0); return;
/*      */       }
/* 1868 */       if (pi.sqlType.startsWith("univarchar")) {
/* 1869 */         String tmp = pi.getString(pi.charsetInfo.getCharset());
/* 1870 */         if (tmp.length() == 0) {
/* 1871 */           tmp = " ";
/*      */         }
/* 1873 */         out.write(tmp.length() * 2);
/* 1874 */         out.write(tmp.toCharArray(), 0, tmp.length()); return;
/*      */       }
/* 1876 */       buf = pi.getBytes(pi.charsetInfo.getCharset());
/* 1877 */       if (buf.length > 0) {
/* 1878 */         out.write(buf.length);
/* 1879 */         out.write(buf); break label973:
/*      */       }
/* 1881 */       out.write(1);
/* 1882 */       out.write(0);
/*      */ 
/* 1886 */       break;
/*      */     case 38:
/* 1889 */       if (pi.value == null) {
/* 1890 */         out.write(0); return;
/*      */       }
/* 1892 */       if ("bigint".equals(pi.sqlType)) {
/* 1893 */         out.write(8);
/* 1894 */         out.write(((Number)pi.value).longValue()); return;
/*      */       }
/* 1896 */       out.write(4);
/* 1897 */       out.write(((Number)pi.value).intValue());
/*      */ 
/* 1901 */       break;
/*      */     case 109:
/* 1904 */       if (pi.value == null) {
/* 1905 */         out.write(0); return;
/*      */       }
/* 1907 */       if (pi.value instanceof Float) {
/* 1908 */         out.write(4);
/* 1909 */         out.write(((Number)pi.value).floatValue()); return;
/*      */       }
/* 1911 */       out.write(8);
/* 1912 */       out.write(((Number)pi.value).doubleValue());
/*      */ 
/* 1916 */       break;
/*      */     case 111:
/* 1919 */       putDateTimeValue(out, (DateTime)pi.value);
/* 1920 */       break;
/*      */     case 123:
/* 1923 */       if (pi.value == null) {
/* 1924 */         out.write(0); return;
/*      */       }
/* 1926 */       out.write(4);
/* 1927 */       out.write(((DateTime)pi.value).getDate());
/*      */ 
/* 1929 */       break;
/*      */     case 147:
/* 1932 */       if (pi.value == null) {
/* 1933 */         out.write(0); return;
/*      */       }
/* 1935 */       out.write(4);
/* 1936 */       out.write(((DateTime)pi.value).getTime());
/*      */ 
/* 1938 */       break;
/*      */     case 50:
/* 1941 */       if (pi.value == null) {
/* 1942 */         out.write(0); return;
/*      */       }
/* 1944 */       out.write((byte)((((Boolean)pi.value).booleanValue()) ? 1 : 0));
/*      */ 
/* 1947 */       break;
/*      */     case 106:
/*      */     case 108:
/* 1951 */       BigDecimal value = null;
/*      */ 
/* 1953 */       if (pi.value != null) {
/* 1954 */         if (pi.value instanceof Long)
/*      */         {
/* 1957 */           value = new BigDecimal(pi.value.toString());
/*      */         }
/*      */         else value = (BigDecimal)pi.value;
/*      */ 
/*      */       }
/*      */ 
/* 1963 */       out.write(value);
/* 1964 */       break;
/*      */     default:
/* 1967 */       label276: label973: throw new IllegalStateException("Unsupported output TDS type " + Integer.toHexString(pi.tdsType));
/*      */     }
/*      */   }
/*      */ 
/*      */   static void putCollation(RequestStream out, ParamInfo pi)
/*      */     throws IOException
/*      */   {
/* 1985 */     if (types[pi.tdsType].isCollation)
/* 1986 */       if (pi.collation != null) {
/* 1987 */         out.write(pi.collation);
/*      */       } else {
/* 1989 */         byte[] collation = { 0, 0, 0, 0, 0 };
/*      */ 
/* 1991 */         out.write(collation);
/*      */       }
/*      */   }
/*      */ 
/*      */   static void writeParam(RequestStream out, CharsetInfo charsetInfo, byte[] collation, ParamInfo pi)
/*      */     throws IOException
/*      */   {
/* 2012 */     boolean isTds8 = out.getTdsVersion() >= 4;
/*      */ 
/* 2014 */     if ((isTds8) && 
/* 2015 */       (pi.collation == null)) {
/* 2016 */       pi.collation = collation;
/*      */     }
/*      */ 
/* 2019 */     if (pi.charsetInfo == null)
/* 2020 */       pi.charsetInfo = charsetInfo;
/*      */     byte[] buf;
/*      */     String tmp;
/*      */     int len;
/* 2023 */     switch (pi.tdsType)
/*      */     {
/*      */     case 167:
/* 2026 */       if (pi.value == null) {
/* 2027 */         out.write((byte)pi.tdsType);
/* 2028 */         out.write(8000);
/*      */ 
/* 2030 */         if (isTds8) {
/* 2031 */           putCollation(out, pi);
/*      */         }
/*      */ 
/* 2034 */         out.write(-1); return;
/*      */       }
/* 2036 */       buf = pi.getBytes(pi.charsetInfo.getCharset());
/*      */ 
/* 2038 */       if (buf.length > 8000) {
/* 2039 */         out.write(35);
/* 2040 */         out.write(buf.length);
/*      */ 
/* 2042 */         if (isTds8) {
/* 2043 */           putCollation(out, pi);
/*      */         }
/*      */ 
/* 2046 */         out.write(buf.length);
/* 2047 */         out.write(buf); return;
/*      */       }
/* 2049 */       out.write((byte)pi.tdsType);
/* 2050 */       out.write(8000);
/*      */ 
/* 2052 */       if (isTds8) {
/* 2053 */         putCollation(out, pi);
/*      */       }
/*      */ 
/* 2056 */       out.write((short)buf.length);
/* 2057 */       out.write(buf);
/*      */ 
/* 2061 */       break;
/*      */     case 39:
/* 2064 */       if (pi.value == null) {
/* 2065 */         out.write((byte)pi.tdsType);
/* 2066 */         out.write(-1);
/* 2067 */         out.write(0); return;
/*      */       }
/* 2069 */       buf = pi.getBytes(pi.charsetInfo.getCharset());
/*      */ 
/* 2071 */       if (buf.length > 255) {
/* 2072 */         if ((buf.length <= 8000) && (out.getTdsVersion() >= 3)) {
/* 2073 */           out.write(-89);
/* 2074 */           out.write(8000);
/*      */ 
/* 2076 */           if (isTds8) {
/* 2077 */             putCollation(out, pi);
/*      */           }
/*      */ 
/* 2080 */           out.write((short)buf.length);
/* 2081 */           out.write(buf); return;
/*      */         }
/* 2083 */         out.write(35);
/* 2084 */         out.write(buf.length);
/*      */ 
/* 2086 */         if (isTds8) {
/* 2087 */           putCollation(out, pi);
/*      */         }
/*      */ 
/* 2090 */         out.write(buf.length);
/* 2091 */         out.write(buf); return;
/*      */       }
/*      */ 
/* 2094 */       if (buf.length == 0) {
/* 2095 */         buf = new byte[1];
/* 2096 */         buf[0] = 32;
/*      */       }
/*      */ 
/* 2099 */       out.write((byte)pi.tdsType);
/* 2100 */       out.write(-1);
/* 2101 */       out.write((byte)buf.length);
/* 2102 */       out.write(buf);
/*      */ 
/* 2106 */       break;
/*      */     case 231:
/* 2109 */       out.write((byte)pi.tdsType);
/* 2110 */       out.write(8000);
/*      */ 
/* 2112 */       if (isTds8) {
/* 2113 */         putCollation(out, pi);
/*      */       }
/*      */ 
/* 2116 */       if (pi.value == null) {
/* 2117 */         out.write(-1); return;
/*      */       }
/* 2119 */       tmp = pi.getString(pi.charsetInfo.getCharset());
/* 2120 */       out.write((short)(tmp.length() * 2));
/* 2121 */       out.write(tmp);
/*      */ 
/* 2124 */       break;
/*      */     case 35:
/* 2127 */       if (pi.value == null) {
/* 2128 */         len = 0;
/*      */       } else {
/* 2130 */         len = pi.length;
/*      */ 
/* 2132 */         if ((len == 0) && (out.getTdsVersion() < 3)) {
/* 2133 */           pi.value = " ";
/* 2134 */           len = 1;
/*      */         }
/*      */       }
/*      */ 
/* 2138 */       out.write((byte)pi.tdsType);
/*      */ 
/* 2140 */       if (len > 0) {
/* 2141 */         if (pi.value instanceof InputStream)
/*      */         {
/* 2143 */           out.write(len);
/*      */ 
/* 2145 */           if (isTds8) {
/* 2146 */             putCollation(out, pi);
/*      */           }
/*      */ 
/* 2149 */           out.write(len);
/* 2150 */           out.writeStreamBytes((InputStream)pi.value, len); return; }
/* 2151 */         if ((pi.value instanceof Reader) && (!(pi.charsetInfo.isWideChars())))
/*      */         {
/* 2153 */           out.write(len);
/*      */ 
/* 2155 */           if (isTds8) {
/* 2156 */             putCollation(out, pi);
/*      */           }
/*      */ 
/* 2159 */           out.write(len);
/* 2160 */           out.writeReaderBytes((Reader)pi.value, len); return;
/*      */         }
/* 2162 */         buf = pi.getBytes(pi.charsetInfo.getCharset());
/* 2163 */         out.write(buf.length);
/*      */ 
/* 2165 */         if (isTds8) {
/* 2166 */           putCollation(out, pi);
/*      */         }
/*      */ 
/* 2169 */         out.write(buf.length);
/* 2170 */         out.write(buf); return;
/*      */       }
/*      */ 
/* 2173 */       out.write(len);
/*      */ 
/* 2175 */       if (isTds8) {
/* 2176 */         putCollation(out, pi);
/*      */       }
/*      */ 
/* 2179 */       out.write(len);
/*      */ 
/* 2182 */       break;
/*      */     case 99:
/* 2185 */       if (pi.value == null)
/* 2186 */         len = 0;
/*      */       else {
/* 2188 */         len = pi.length;
/*      */       }
/*      */ 
/* 2191 */       out.write((byte)pi.tdsType);
/*      */ 
/* 2193 */       if (len > 0) {
/* 2194 */         if (pi.value instanceof Reader) {
/* 2195 */           out.write(len);
/*      */ 
/* 2197 */           if (isTds8) {
/* 2198 */             putCollation(out, pi);
/*      */           }
/*      */ 
/* 2201 */           out.write(len * 2);
/* 2202 */           out.writeReaderChars((Reader)pi.value, len); return; }
/* 2203 */         if ((pi.value instanceof InputStream) && (!(pi.charsetInfo.isWideChars()))) {
/* 2204 */           out.write(len);
/*      */ 
/* 2206 */           if (isTds8) {
/* 2207 */             putCollation(out, pi);
/*      */           }
/*      */ 
/* 2210 */           out.write(len * 2);
/* 2211 */           out.writeReaderChars(new InputStreamReader((InputStream)pi.value, pi.charsetInfo.getCharset()), len); return;
/*      */         }
/*      */ 
/* 2214 */         tmp = pi.getString(pi.charsetInfo.getCharset());
/* 2215 */         len = tmp.length();
/* 2216 */         out.write(len);
/*      */ 
/* 2218 */         if (isTds8) {
/* 2219 */           putCollation(out, pi);
/*      */         }
/*      */ 
/* 2222 */         out.write(len * 2);
/* 2223 */         out.write(tmp); return;
/*      */       }
/*      */ 
/* 2226 */       out.write(len);
/*      */ 
/* 2228 */       if (isTds8) {
/* 2229 */         putCollation(out, pi);
/*      */       }
/*      */ 
/* 2232 */       out.write(len);
/*      */ 
/* 2235 */       break;
/*      */     case 165:
/* 2238 */       out.write((byte)pi.tdsType);
/* 2239 */       out.write(8000);
/*      */ 
/* 2241 */       if (pi.value == null) {
/* 2242 */         out.write(-1); return;
/*      */       }
/* 2244 */       buf = pi.getBytes(pi.charsetInfo.getCharset());
/* 2245 */       out.write((short)buf.length);
/* 2246 */       out.write(buf);
/*      */ 
/* 2249 */       break;
/*      */     case 37:
/* 2252 */       out.write((byte)pi.tdsType);
/* 2253 */       out.write(-1);
/*      */ 
/* 2255 */       if (pi.value == null) {
/* 2256 */         out.write(0); return;
/*      */       }
/* 2258 */       buf = pi.getBytes(pi.charsetInfo.getCharset());
/* 2259 */       if ((out.getTdsVersion() < 3) && (buf.length == 0))
/*      */       {
/* 2261 */         out.write(1); out.write(0); return;
/*      */       }
/* 2263 */       out.write((byte)buf.length);
/* 2264 */       out.write(buf);
/*      */ 
/* 2268 */       break;
/*      */     case 34:
/* 2271 */       if (pi.value == null)
/* 2272 */         len = 0;
/*      */       else {
/* 2274 */         len = pi.length;
/*      */       }
/*      */ 
/* 2277 */       out.write((byte)pi.tdsType);
/*      */ 
/* 2279 */       if (len > 0) {
/* 2280 */         if (pi.value instanceof InputStream) {
/* 2281 */           out.write(len);
/* 2282 */           out.write(len);
/* 2283 */           out.writeStreamBytes((InputStream)pi.value, len); return;
/*      */         }
/* 2285 */         buf = pi.getBytes(pi.charsetInfo.getCharset());
/* 2286 */         out.write(buf.length);
/* 2287 */         out.write(buf.length);
/* 2288 */         out.write(buf); return;
/*      */       }
/*      */ 
/* 2291 */       if (out.getTdsVersion() < 3)
/*      */       {
/* 2293 */         out.write(1);
/* 2294 */         out.write(1);
/* 2295 */         out.write(0); return;
/*      */       }
/* 2297 */       out.write(len);
/* 2298 */       out.write(len);
/*      */ 
/* 2302 */       break;
/*      */     case 38:
/* 2305 */       out.write((byte)pi.tdsType);
/*      */ 
/* 2307 */       if (pi.value == null) {
/* 2308 */         out.write(("bigint".equals(pi.sqlType)) ? 8 : 4);
/* 2309 */         out.write(0); return;
/*      */       }
/* 2311 */       if ("bigint".equals(pi.sqlType)) {
/* 2312 */         out.write(8);
/* 2313 */         out.write(8);
/* 2314 */         out.write(((Number)pi.value).longValue()); return;
/*      */       }
/* 2316 */       out.write(4);
/* 2317 */       out.write(4);
/* 2318 */       out.write(((Number)pi.value).intValue());
/*      */ 
/* 2322 */       break;
/*      */     case 109:
/* 2325 */       out.write((byte)pi.tdsType);
/* 2326 */       if (pi.value instanceof Float) {
/* 2327 */         out.write(4);
/* 2328 */         out.write(4);
/* 2329 */         out.write(((Number)pi.value).floatValue()); return;
/*      */       }
/* 2331 */       out.write(8);
/* 2332 */       if (pi.value == null) {
/* 2333 */         out.write(0); return;
/*      */       }
/* 2335 */       out.write(8);
/* 2336 */       out.write(((Number)pi.value).doubleValue());
/*      */ 
/* 2340 */       break;
/*      */     case 111:
/* 2343 */       out.write(111);
/* 2344 */       out.write(8);
/* 2345 */       putDateTimeValue(out, (DateTime)pi.value);
/* 2346 */       break;
/*      */     case 50:
/* 2349 */       out.write((byte)pi.tdsType);
/*      */ 
/* 2351 */       if (pi.value == null) {
/* 2352 */         out.write(0); return;
/*      */       }
/* 2354 */       out.write((byte)((((Boolean)pi.value).booleanValue()) ? 1 : 0));
/*      */ 
/* 2357 */       break;
/*      */     case 104:
/* 2360 */       out.write(104);
/* 2361 */       out.write(1);
/*      */ 
/* 2363 */       if (pi.value == null) {
/* 2364 */         out.write(0); return;
/*      */       }
/* 2366 */       out.write(1);
/* 2367 */       out.write((byte)((((Boolean)pi.value).booleanValue()) ? 1 : 0));
/*      */ 
/* 2370 */       break;
/*      */     case 106:
/*      */     case 108:
/* 2374 */       out.write((byte)pi.tdsType);
/* 2375 */       BigDecimal value = null;
/* 2376 */       int prec = out.getMaxPrecision();
/*      */       int scale;
/* 2379 */       if (pi.value == null) {
/* 2380 */         if (pi.jdbcType == -5) {
/* 2381 */           scale = 0;
/*      */         }
/* 2383 */         else if ((pi.scale >= 0) && (pi.scale <= prec))
/* 2384 */           scale = pi.scale;
/*      */         else {
/* 2386 */           scale = 10;
/*      */         }
/*      */ 
/*      */       }
/* 2390 */       else if (pi.value instanceof Long) {
/* 2391 */         value = new BigDecimal(((Long)pi.value).toString());
/* 2392 */         scale = 0;
/*      */       } else {
/* 2394 */         value = (BigDecimal)pi.value;
/* 2395 */         scale = value.scale();
/*      */       }
/*      */ 
/* 2399 */       out.write(out.getMaxDecimalBytes());
/* 2400 */       out.write((byte)prec);
/* 2401 */       out.write((byte)scale);
/* 2402 */       out.write(value);
/* 2403 */       break;
/*      */     default:
/* 2406 */       throw new IllegalStateException("Unsupported output TDS type " + Integer.toHexString(pi.tdsType));
/*      */     }
/*      */   }
/*      */ 
/*      */   private static Object getDatetimeValue(ResponseStream in, int type)
/*      */     throws IOException, ProtocolException
/*      */   {
/*      */     int len;
/* 2436 */     if (type == 111)
/* 2437 */       len = in.read();
/* 2438 */     else if (type == 58)
/* 2439 */       len = 4;
/*      */     else
/* 2441 */       len = 8;
/*      */     int daysSince1900;
/* 2444 */     switch (len)
/*      */     {
/*      */     case 0:
/* 2446 */       return null;
/*      */     case 8:
/* 2454 */       daysSince1900 = in.readInt();
/* 2455 */       int time = in.readInt();
/* 2456 */       return new DateTime(daysSince1900, time);
/*      */     case 4:
/* 2463 */       daysSince1900 = in.readShort() & 0xFFFF;
/* 2464 */       int minutes = in.readShort();
/* 2465 */       return new DateTime((short)daysSince1900, (short)minutes);
/*      */     }
/* 2467 */     throw new ProtocolException("Invalid DATETIME value with size of " + len + " bytes.");
/*      */   }
/*      */ 
/*      */   private static void putDateTimeValue(RequestStream out, DateTime value)
/*      */     throws IOException
/*      */   {
/* 2481 */     if (value == null) {
/* 2482 */       out.write(0);
/* 2483 */       return;
/*      */     }
/* 2485 */     out.write(8);
/* 2486 */     out.write(value.getDate());
/* 2487 */     out.write(value.getTime());
/*      */   }
/*      */ 
/*      */   private static Object getMoneyValue(ResponseStream in, int type)
/*      */     throws IOException, ProtocolException
/*      */   {
/*      */     int len;
/* 2503 */     if (type == 60)
/* 2504 */       len = 8;
/* 2505 */     else if (type == 110)
/* 2506 */       len = in.read();
/*      */     else {
/* 2508 */       len = 4;
/*      */     }
/*      */ 
/* 2511 */     BigInteger x = null;
/*      */ 
/* 2513 */     if (len == 4) {
/* 2514 */       x = BigInteger.valueOf(in.readInt());
/* 2515 */     } else if (len == 8) {
/* 2516 */       byte b4 = (byte)in.read();
/* 2517 */       byte b5 = (byte)in.read();
/* 2518 */       byte b6 = (byte)in.read();
/* 2519 */       byte b7 = (byte)in.read();
/* 2520 */       byte b0 = (byte)in.read();
/* 2521 */       byte b1 = (byte)in.read();
/* 2522 */       byte b2 = (byte)in.read();
/* 2523 */       byte b3 = (byte)in.read();
/* 2524 */       long l = (b0 & 0xFF) + ((b1 & 0xFF) << 8) + ((b2 & 0xFF) << 16) + ((b3 & 0xFF) << 24) + ((b4 & 0xFF) << 32) + ((b5 & 0xFF) << 40) + ((b6 & 0xFF) << 48) + ((b7 & 0xFF) << 56);
/*      */ 
/* 2529 */       x = BigInteger.valueOf(l);
/* 2530 */     } else if (len != 0) {
/* 2531 */       throw new ProtocolException("Invalid money value.");
/*      */     }
/*      */ 
/* 2534 */     return new BigDecimal(x, 4);
/*      */   }
/*      */ 
/*      */   private static Object getVariant(ConnectionJDBC2 connection, ResponseStream in)
/*      */     throws IOException, ProtocolException
/*      */   {
/* 2556 */     int len = in.readInt();
/*      */ 
/* 2558 */     if (len == 0)
/*      */     {
/* 2560 */       return null;
/*      */     }
/*      */ 
/* 2563 */     ColInfo ci = new ColInfo();
/* 2564 */     len -= 2;
/* 2565 */     ci.tdsType = in.read();
/* 2566 */     len -= in.read();
/*      */     byte[] bytes;
/* 2568 */     switch (ci.tdsType)
/*      */     {
/*      */     case 48:
/* 2570 */       return new Integer(in.read() & 0xFF);
/*      */     case 52:
/* 2573 */       return new Integer(in.readShort());
/*      */     case 56:
/* 2576 */       return new Integer(in.readInt());
/*      */     case 127:
/* 2579 */       return new Long(in.readLong());
/*      */     case 167:
/*      */     case 175:
/* 2584 */       getCollation(in, ci);
/*      */       try {
/* 2586 */         setColumnCharset(ci, connection);
/*      */       }
/*      */       catch (SQLException ex) {
/* 2589 */         in.skip(2 + len);
/* 2590 */         throw new ProtocolException(ex.toString() + " [SQLState: " + ex.getSQLState() + ']');
/*      */       }
/*      */ 
/* 2594 */       in.skip(2);
/* 2595 */       return in.readNonUnicodeString(len);
/*      */     case 231:
/*      */     case 239:
/* 2600 */       in.skip(7);
/*      */ 
/* 2602 */       return in.readUnicodeString(len / 2);
/*      */     case 165:
/*      */     case 173:
/* 2606 */       in.skip(2);
/* 2607 */       bytes = new byte[len];
/* 2608 */       in.read(bytes);
/*      */ 
/* 2610 */       return bytes;
/*      */     case 60:
/*      */     case 122:
/* 2614 */       return getMoneyValue(in, ci.tdsType);
/*      */     case 58:
/*      */     case 61:
/* 2618 */       return getDatetimeValue(in, ci.tdsType);
/*      */     case 50:
/* 2621 */       return ((in.read() != 0) ? Boolean.TRUE : Boolean.FALSE);
/*      */     case 59:
/* 2624 */       return new Float(Float.intBitsToFloat(in.readInt()));
/*      */     case 62:
/* 2627 */       return new Double(Double.longBitsToDouble(in.readLong()));
/*      */     case 36:
/* 2630 */       bytes = new byte[len];
/* 2631 */       in.read(bytes);
/*      */ 
/* 2633 */       return new UniqueIdentifier(bytes);
/*      */     case 106:
/*      */     case 108:
/* 2637 */       ci.precision = in.read();
/* 2638 */       ci.scale = in.read();
/* 2639 */       int sign = in.read();
/* 2640 */       --len;
/* 2641 */       bytes = new byte[len];
/*      */ 
/* 2644 */       while (len-- > 0) {
/* 2645 */         bytes[len] = (byte)in.read();
/*      */       }
/*      */ 
/* 2648 */       BigInteger bi = new BigInteger((sign == 0) ? -1 : 1, bytes);
/*      */ 
/* 2650 */       return new BigDecimal(bi, ci.scale);
/*      */     }
/*      */ 
/* 2653 */     throw new ProtocolException("Unsupported TDS data type 0x" + Integer.toHexString(ci.tdsType) + " in sql_variant");
/*      */   }
/*      */ 
/*      */   public static String getMSTypeName(String typeName, int tdsType)
/*      */   {
/* 2681 */     if ((typeName.equalsIgnoreCase("text")) && (tdsType != 35))
/* 2682 */       return "varchar";
/* 2683 */     if ((typeName.equalsIgnoreCase("ntext")) && (tdsType != 35))
/* 2684 */       return "nvarchar";
/* 2685 */     if ((typeName.equalsIgnoreCase("image")) && (tdsType != 34)) {
/* 2686 */       return "varbinary";
/*      */     }
/* 2688 */     return typeName;
/*      */   }
/*      */ 
/*      */   public static int getTdsVersion(int rawTdsVersion)
/*      */   {
/* 2701 */     if (rawTdsVersion >= 1895825409)
/* 2702 */       return 5;
/* 2703 */     if (rawTdsVersion >= 117506048)
/* 2704 */       return 4;
/* 2705 */     if (rawTdsVersion >= 117440512)
/* 2706 */       return 3;
/* 2707 */     if (rawTdsVersion >= 83886080) {
/* 2708 */       return 2;
/*      */     }
/* 2710 */     return 1;
/*      */   }
/*      */ 
/*      */   private static boolean canEncode(String value, String charset)
/*      */   {
/* 2723 */     if (value == null) {
/* 2724 */       return true;
/*      */     }
/* 2726 */     if ("UTF-8".equals(charset))
/*      */     {
/* 2728 */       return true;
/*      */     }
/*      */     int i;
/* 2730 */     if ("ISO-8859-1".equals(charset))
/*      */     {
/* 2732 */       for (i = value.length() - 1; i >= 0; --i) {
/* 2733 */         if (value.charAt(i) > 255) {
/* 2734 */           return false;
/*      */         }
/*      */       }
/* 2737 */       return true;
/*      */     }
/* 2739 */     if (("ISO-8859-15".equals(charset)) || ("Cp1252".equals(charset)))
/*      */     {
/* 2741 */       for (i = value.length() - 1; i >= 0; --i)
/*      */       {
/* 2746 */         char c = value.charAt(i);
/* 2747 */         if ((c > 255) && (c != 8364)) {
/* 2748 */           return false;
/*      */         }
/*      */       }
/* 2751 */       return true;
/*      */     }
/* 2753 */     if ("US-ASCII".equals(charset)) {
/* 2754 */       for (i = value.length() - 1; i >= 0; --i) {
/* 2755 */         if (value.charAt(i) > '') {
/* 2756 */           return false;
/*      */         }
/*      */       }
/* 2759 */       return true;
/*      */     }
/*      */     try
/*      */     {
/* 2763 */       return new String(value.getBytes(charset), charset).equals(value); } catch (UnsupportedEncodingException e) {
/*      */     }
/* 2765 */     return false;
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*  209 */     types[47] = new TypeInfo("char", -1, -1, 1, false, false, 1);
/*  210 */     types[39] = new TypeInfo("varchar", -1, -1, 1, false, false, 12);
/*  211 */     types[38] = new TypeInfo("int", -1, 10, 11, true, false, 4);
/*  212 */     types[48] = new TypeInfo("tinyint", 1, 3, 4, false, false, -6);
/*  213 */     types[52] = new TypeInfo("smallint", 2, 5, 6, true, false, 5);
/*  214 */     types[56] = new TypeInfo("int", 4, 10, 11, true, false, 4);
/*  215 */     types[127] = new TypeInfo("bigint", 8, 19, 20, true, false, -5);
/*  216 */     types[62] = new TypeInfo("float", 8, 15, 24, true, false, 8);
/*  217 */     types[61] = new TypeInfo("datetime", 8, 23, 23, false, false, 93);
/*  218 */     types[50] = new TypeInfo("bit", 1, 1, 1, false, false, -7);
/*  219 */     types[35] = new TypeInfo("text", -4, -1, -1, false, true, 2005);
/*  220 */     types[99] = new TypeInfo("ntext", -4, -1, -1, false, true, 2005);
/*  221 */     types[174] = new TypeInfo("unitext", -4, -1, -1, false, true, 2005);
/*  222 */     types[34] = new TypeInfo("image", -4, -1, -1, false, false, 2004);
/*  223 */     types[122] = new TypeInfo("smallmoney", 4, 10, 12, true, false, 3);
/*  224 */     types[60] = new TypeInfo("money", 8, 19, 21, true, false, 3);
/*  225 */     types[58] = new TypeInfo("smalldatetime", 4, 16, 19, false, false, 93);
/*  226 */     types[59] = new TypeInfo("real", 4, 7, 14, true, false, 7);
/*  227 */     types[45] = new TypeInfo("binary", -1, -1, 2, false, false, -2);
/*  228 */     types[31] = new TypeInfo("void", -1, 1, 1, false, false, 0);
/*  229 */     types[37] = new TypeInfo("varbinary", -1, -1, -1, false, false, -3);
/*  230 */     types[103] = new TypeInfo("nvarchar", -1, -1, -1, false, false, 12);
/*  231 */     types[104] = new TypeInfo("bit", -1, 1, 1, false, false, -7);
/*  232 */     types[108] = new TypeInfo("numeric", -1, -1, -1, true, false, 2);
/*  233 */     types[106] = new TypeInfo("decimal", -1, -1, -1, true, false, 3);
/*  234 */     types[109] = new TypeInfo("float", -1, 15, 24, true, false, 8);
/*  235 */     types[110] = new TypeInfo("money", -1, 19, 21, true, false, 3);
/*  236 */     types[111] = new TypeInfo("datetime", -1, 23, 23, false, false, 93);
/*  237 */     types[49] = new TypeInfo("date", 4, 10, 10, false, false, 91);
/*  238 */     types[51] = new TypeInfo("time", 4, 8, 8, false, false, 92);
/*  239 */     types[123] = new TypeInfo("date", -1, 10, 10, false, false, 91);
/*  240 */     types[147] = new TypeInfo("time", -1, 8, 8, false, false, 92);
/*  241 */     types[175] = new TypeInfo("char", -2, -1, -1, false, true, 1);
/*  242 */     types[167] = new TypeInfo("varchar", -2, -1, -1, false, true, 12);
/*  243 */     types[231] = new TypeInfo("nvarchar", -2, -1, -1, false, true, 12);
/*  244 */     types[239] = new TypeInfo("nchar", -2, -1, -1, false, true, 1);
/*  245 */     types[165] = new TypeInfo("varbinary", -2, -1, -1, false, false, -3);
/*  246 */     types[173] = new TypeInfo("binary", -2, -1, -1, false, false, -2);
/*  247 */     types[225] = new TypeInfo("varbinary", -5, -1, 2, false, false, -2);
/*  248 */     types[64] = new TypeInfo("tinyint", 1, 2, 3, false, false, -6);
/*  249 */     types[65] = new TypeInfo("unsigned smallint", 2, 5, 6, false, false, 4);
/*  250 */     types[66] = new TypeInfo("unsigned int", 4, 10, 11, false, false, -5);
/*  251 */     types[67] = new TypeInfo("unsigned bigint", 8, 20, 20, false, false, 3);
/*  252 */     types[68] = new TypeInfo("unsigned int", -1, 10, 11, true, false, -5);
/*  253 */     types[36] = new TypeInfo("uniqueidentifier", -1, 36, 36, false, false, 1);
/*  254 */     types[98] = new TypeInfo("sql_variant", -5, 0, 8000, false, false, 12);
/*  255 */     types[191] = new TypeInfo("bigint", 8, 19, 20, true, false, -5);
/*      */   }
/*      */ 
/*      */   private static class TypeInfo
/*      */   {
/*      */     public final String sqlType;
/*      */     public final int size;
/*      */     public final int precision;
/*      */     public final int displaySize;
/*      */     public final boolean isSigned;
/*      */     public final boolean isCollation;
/*      */     public final int jdbcType;
/*      */ 
/*      */     TypeInfo(String sqlType, int size, int precision, int displaySize, boolean isSigned, boolean isCollation, int jdbcType)
/*      */     {
/*   99 */       this.sqlType = sqlType;
/*  100 */       this.size = size;
/*  101 */       this.precision = precision;
/*  102 */       this.displaySize = displaySize;
/*  103 */       this.isSigned = isSigned;
/*  104 */       this.isCollation = isCollation;
/*  105 */       this.jdbcType = jdbcType;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.TdsData
 * JD-Core Version:    0.5.3
 */