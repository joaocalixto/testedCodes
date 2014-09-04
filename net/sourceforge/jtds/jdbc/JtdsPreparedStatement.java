/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import [Lnet.sourceforge.jtds.jdbc.ParamInfo;;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.Reader;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.math.BigDecimal;
/*     */ import java.net.URL;
/*     */ import java.sql.Array;
/*     */ import java.sql.Blob;
/*     */ import java.sql.Clob;
/*     */ import java.sql.Date;
/*     */ import java.sql.ParameterMetaData;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.Ref;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.ResultSetMetaData;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Time;
/*     */ import java.sql.Timestamp;
/*     */ import java.text.NumberFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Calendar;
/*     */ import java.util.Collection;
/*     */ 
/*     */ public class JtdsPreparedStatement extends JtdsStatement
/*     */   implements PreparedStatement
/*     */ {
/*     */   protected final String sql;
/*     */   private final String originalSql;
/*     */   protected String sqlWord;
/*     */   protected String procName;
/*     */   protected ParamInfo[] parameters;
/*     */   private boolean returnKeys;
/*     */   protected ParamInfo[] paramMetaData;
/*  67 */   private static final NumberFormat f = NumberFormat.getInstance();
/*     */   Collection handles;
/*     */ 
/*     */   JtdsPreparedStatement(ConnectionJDBC2 connection, String sql, int resultSetType, int concurrency, boolean returnKeys)
/*     */     throws SQLException
/*     */   {
/*  87 */     super(connection, resultSetType, concurrency);
/*     */ 
/*  90 */     this.originalSql = sql;
/*     */ 
/*  93 */     if (this instanceof JtdsCallableStatement) {
/*  94 */       sql = normalizeCall(sql);
/*     */     }
/*     */ 
/*  97 */     ArrayList params = new ArrayList();
/*  98 */     String[] parsedSql = SQLParser.parse(sql, params, connection, false);
/*     */ 
/* 100 */     if (parsedSql[0].length() == 0) {
/* 101 */       throw new SQLException(Messages.get("error.prepare.nosql"), "07000");
/*     */     }
/*     */ 
/* 104 */     if ((parsedSql[1].length() > 1) && 
/* 105 */       (this instanceof JtdsCallableStatement))
/*     */     {
/* 107 */       this.procName = parsedSql[1];
/*     */     }
/*     */ 
/* 110 */     this.sqlWord = parsedSql[2];
/*     */ 
/* 112 */     if ((returnKeys) && ("insert".equals(this.sqlWord))) {
/* 113 */       if ((connection.getServerType() == 1) && (connection.getDatabaseMajorVersion() >= 8))
/*     */       {
/* 115 */         this.sql = parsedSql[0] + " SELECT SCOPE_IDENTITY() AS ID";
/*     */       }
/*     */       else this.sql = parsedSql[0] + " SELECT @@IDENTITY AS ID";
/*     */ 
/* 119 */       this.returnKeys = true;
/*     */     } else {
/* 121 */       this.sql = parsedSql[0];
/* 122 */       this.returnKeys = false;
/*     */     }
/*     */ 
/* 125 */     this.parameters = ((ParamInfo[])params.toArray(new ParamInfo[params.size()]));
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 132 */     return this.originalSql;
/*     */   }
/*     */ 
/*     */   protected static String normalizeCall(String sql)
/*     */   {
/* 151 */     String original = sql;
/* 152 */     sql = sql.trim();
/*     */ 
/* 154 */     if ((sql.length() > 0) && (sql.charAt(0) == '{')) {
/* 155 */       return original;
/*     */     }
/*     */ 
/* 158 */     if ((sql.length() > 4) && (sql.substring(0, 5).equalsIgnoreCase("exec ")))
/* 159 */       sql = sql.substring(4).trim();
/* 160 */     else if ((sql.length() > 7) && (sql.substring(0, 8).equalsIgnoreCase("execute "))) {
/* 161 */       sql = sql.substring(7).trim();
/*     */     }
/*     */ 
/* 164 */     if ((sql.length() > 1) && (sql.charAt(0) == '?')) {
/* 165 */       sql = sql.substring(1).trim();
/*     */ 
/* 167 */       if ((sql.length() < 1) || (sql.charAt(0) != '=')) {
/* 168 */         return original;
/*     */       }
/*     */ 
/* 171 */       sql = sql.substring(1).trim();
/*     */ 
/* 174 */       return "{?=call " + sql + '}';
/*     */     }
/*     */ 
/* 177 */     return "{call " + sql + '}';
/*     */   }
/*     */ 
/*     */   protected void checkOpen()
/*     */     throws SQLException
/*     */   {
/* 186 */     if (this.closed)
/* 187 */       throw new SQLException(Messages.get("error.generic.closed", "PreparedStatement"), "HY010");
/*     */   }
/*     */ 
/*     */   protected void notSupported(String method)
/*     */     throws SQLException
/*     */   {
/* 199 */     throw new SQLException(Messages.get("error.generic.notsup", method), "HYC00");
/*     */   }
/*     */ 
/*     */   protected SQLException executeMSBatch(int size, int executeSize, ArrayList counts)
/*     */     throws SQLException
/*     */   {
/* 218 */     if (this.parameters.length == 0)
/*     */     {
/* 220 */       return super.executeMSBatch(size, executeSize, counts);
/*     */     }
/* 222 */     SQLException sqlEx = null;
/* 223 */     String[] procHandle = null;
/*     */ 
/* 226 */     if ((this.connection.getPrepareSql() == 1) || (this.connection.getPrepareSql() == 3))
/*     */     {
/* 228 */       procHandle = new String[size];
/* 229 */       for (i = 0; i < size; ++i)
/*     */       {
/* 231 */         procHandle[i] = this.connection.prepareSQL(this, this.sql, (ParamInfo[])this.batchValues.get(i), false, false);
/*     */       }
/*     */     }
/*     */ 
/* 235 */     for (int i = 0; i < size; ) {
/* 236 */       Object value = this.batchValues.get(i);
/* 237 */       String proc = (procHandle == null) ? this.procName : procHandle[i];
/* 238 */       ++i;
/*     */ 
/* 240 */       boolean executeNow = (i % executeSize == 0) || (i == size);
/*     */ 
/* 242 */       this.tds.startBatch();
/* 243 */       this.tds.executeSQL(this.sql, proc, (ParamInfo[])value, false, 0, -1, -1, executeNow);
/*     */ 
/* 246 */       if (executeNow) {
/* 247 */         sqlEx = this.tds.getBatchCounts(counts, sqlEx);
/*     */ 
/* 251 */         if ((sqlEx != null) && (counts.size() != i)) {
/*     */           break;
/*     */         }
/*     */       }
/*     */     }
/* 256 */     return sqlEx;
/*     */   }
/*     */ 
/*     */   protected SQLException executeSybaseBatch(int size, int executeSize, ArrayList counts)
/*     */     throws SQLException
/*     */   {
/* 272 */     if (this.parameters.length == 0)
/*     */     {
/* 275 */       return super.executeSybaseBatch(size, executeSize, counts);
/*     */     }
/*     */ 
/* 279 */     int maxParams = ((this.connection.getDatabaseMajorVersion() < 12) || ((this.connection.getDatabaseMajorVersion() == 12) && (this.connection.getDatabaseMinorVersion() < 50))) ? 200 : 1000;
/*     */ 
/* 282 */     StringBuffer sqlBuf = new StringBuffer(size * 32);
/* 283 */     SQLException sqlEx = null;
/* 284 */     if (this.parameters.length * executeSize > maxParams) {
/* 285 */       executeSize = maxParams / this.parameters.length;
/* 286 */       if (executeSize == 0) {
/* 287 */         executeSize = 1;
/*     */       }
/*     */     }
/* 290 */     ArrayList paramList = new ArrayList();
/* 291 */     for (int i = 0; i < size; ) {
/* 292 */       Object value = this.batchValues.get(i);
/* 293 */       ++i;
/*     */ 
/* 295 */       boolean executeNow = (i % executeSize == 0) || (i == size);
/*     */ 
/* 297 */       int offset = sqlBuf.length();
/* 298 */       sqlBuf.append(this.sql).append(' ');
/* 299 */       for (int n = 0; n < this.parameters.length; ++n) {
/* 300 */         ParamInfo p = ((ParamInfo[])value)[n];
/*     */ 
/* 302 */         p.markerPos += offset;
/* 303 */         paramList.add(p);
/*     */       }
/* 305 */       if (executeNow)
/*     */       {
/* 307 */         ParamInfo[] args = (ParamInfo[])paramList.toArray(new ParamInfo[paramList.size()]);
/* 308 */         this.tds.executeSQL(sqlBuf.toString(), null, args, false, 0, -1, -1, true);
/* 309 */         sqlBuf.setLength(0);
/* 310 */         paramList.clear();
/*     */ 
/* 312 */         sqlEx = this.tds.getBatchCounts(counts, sqlEx);
/*     */ 
/* 316 */         if ((sqlEx != null) && (counts.size() != i)) {
/*     */           break;
/*     */         }
/*     */       }
/*     */     }
/* 321 */     return sqlEx;
/*     */   }
/*     */ 
/*     */   protected ParamInfo getParameter(int parameterIndex)
/*     */     throws SQLException
/*     */   {
/* 337 */     checkOpen();
/*     */ 
/* 339 */     if ((parameterIndex < 1) || (parameterIndex > this.parameters.length)) {
/* 340 */       throw new SQLException(Messages.get("error.prepare.paramindex", Integer.toString(parameterIndex)), "07009");
/*     */     }
/*     */ 
/* 345 */     return this.parameters[(parameterIndex - 1)];
/*     */   }
/*     */ 
/*     */   public void setObjectBase(int parameterIndex, Object x, int targetSqlType, int scale)
/*     */     throws SQLException
/*     */   {
/* 358 */     checkOpen();
/*     */ 
/* 360 */     int length = 0;
/*     */ 
/* 362 */     if (targetSqlType == 2005)
/* 363 */       targetSqlType = -1;
/* 364 */     else if (targetSqlType == 2004) {
/* 365 */       targetSqlType = -4;
/*     */     }
/*     */ 
/* 368 */     if (x != null) {
/* 369 */       x = Support.convert(this, x, targetSqlType, this.connection.getCharset());
/*     */ 
/* 371 */       if (scale >= 0) {
/* 372 */         if (x instanceof BigDecimal)
/* 373 */           x = ((BigDecimal)x).setScale(scale, 4);
/* 374 */         else if (x instanceof Number) {
/* 375 */           synchronized (f) {
/* 376 */             f.setGroupingUsed(false);
/* 377 */             f.setMaximumFractionDigits(scale);
/* 378 */             x = Support.convert(this, f.format(x), targetSqlType, this.connection.getCharset());
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 384 */       if (x instanceof Blob) {
/* 385 */         Blob blob = (Blob)x;
/* 386 */         length = (int)blob.length();
/* 387 */         x = blob.getBinaryStream();
/* 388 */       } else if (x instanceof Clob) {
/* 389 */         Clob clob = (Clob)x;
/* 390 */         length = (int)clob.length();
/* 391 */         x = clob.getCharacterStream();
/*     */       }
/*     */     }
/*     */ 
/* 395 */     setParameter(parameterIndex, x, targetSqlType, scale, length);
/*     */   }
/*     */ 
/*     */   protected void setParameter(int parameterIndex, Object x, int targetSqlType, int scale, int length)
/*     */     throws SQLException
/*     */   {
/* 409 */     ParamInfo pi = getParameter(parameterIndex);
/*     */ 
/* 411 */     if ("ERROR".equals(Support.getJdbcTypeName(targetSqlType))) {
/* 412 */       throw new SQLException(Messages.get("error.generic.badtype", Integer.toString(targetSqlType)), "HY092");
/*     */     }
/*     */ 
/* 417 */     if ((targetSqlType == 3) || (targetSqlType == 2))
/*     */     {
/* 420 */       pi.precision = this.connection.getMaxPrecision();
/* 421 */       if (x instanceof BigDecimal) {
/* 422 */         x = Support.normalizeBigDecimal((BigDecimal)x, pi.precision);
/* 423 */         pi.scale = ((BigDecimal)x).scale();
/*     */       } else {
/* 425 */         pi.scale = ((scale < 0) ? 10 : scale);
/*     */       }
/*     */     } else {
/* 428 */       pi.scale = ((scale < 0) ? 0 : scale);
/*     */     }
/*     */ 
/* 431 */     if (x instanceof String)
/* 432 */       pi.length = ((String)x).length();
/* 433 */     else if (x instanceof byte[])
/* 434 */       pi.length = ((byte[])x).length;
/*     */     else {
/* 436 */       pi.length = length;
/*     */     }
/*     */ 
/* 439 */     if (x instanceof Date)
/* 440 */       x = new DateTime((Date)x);
/* 441 */     else if (x instanceof Time)
/* 442 */       x = new DateTime((Time)x);
/* 443 */     else if (x instanceof Timestamp) {
/* 444 */       x = new DateTime((Timestamp)x);
/*     */     }
/*     */ 
/* 447 */     pi.value = x;
/* 448 */     pi.jdbcType = targetSqlType;
/* 449 */     pi.isSet = true;
/* 450 */     pi.isUnicode = this.connection.getUseUnicode();
/*     */   }
/*     */ 
/*     */   void setColMetaData(ColInfo[] value)
/*     */   {
/* 459 */     this.colMetaData = value;
/*     */   }
/*     */ 
/*     */   void setParamMetaData(ParamInfo[] value)
/*     */   {
/* 468 */     for (int i = 0; (i < value.length) && (i < this.parameters.length); ++i) {
/* 469 */       if (this.parameters[i].isSet) {
/*     */         continue;
/*     */       }
/* 472 */       this.parameters[i].jdbcType = value[i].jdbcType;
/* 473 */       this.parameters[i].isOutput = value[i].isOutput;
/* 474 */       this.parameters[i].precision = value[i].precision;
/* 475 */       this.parameters[i].scale = value[i].scale;
/* 476 */       this.parameters[i].sqlType = value[i].sqlType;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws SQLException
/*     */   {
/*     */     try
/*     */     {
/* 485 */       super.close();
/*     */     }
/*     */     finally
/*     */     {
/* 489 */       this.handles = null;
/* 490 */       this.parameters = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   public int executeUpdate() throws SQLException {
/* 495 */     checkOpen();
/* 496 */     initialize();
/*     */ 
/* 498 */     if ((this.procName == null) && (!(this instanceof JtdsCallableStatement)))
/*     */     {
/* 502 */       synchronized (this.connection) {
/* 503 */         String spName = this.connection.prepareSQL(this, this.sql, this.parameters, this.returnKeys, false);
/* 504 */         executeSQL(this.sql, spName, this.parameters, this.returnKeys, true, false);
/*     */       }
/*     */     }
/*     */     else executeSQL(this.sql, this.procName, this.parameters, this.returnKeys, true, false);
/*     */ 
/* 510 */     int res = getUpdateCount();
/* 511 */     return ((res == -1) ? 0 : res);
/*     */   }
/*     */ 
/*     */   public void addBatch() throws SQLException {
/* 515 */     checkOpen();
/*     */ 
/* 517 */     if (this.batchValues == null) {
/* 518 */       this.batchValues = new ArrayList();
/*     */     }
/*     */ 
/* 521 */     if (this.parameters.length == 0)
/*     */     {
/* 525 */       this.batchValues.add(this.sql);
/*     */     } else {
/* 527 */       this.batchValues.add(this.parameters);
/*     */ 
/* 529 */       ParamInfo[] tmp = new ParamInfo[this.parameters.length];
/*     */ 
/* 531 */       for (int i = 0; i < this.parameters.length; ++i) {
/* 532 */         tmp[i] = ((ParamInfo)this.parameters[i].clone());
/*     */       }
/*     */ 
/* 535 */       this.parameters = tmp;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clearParameters() throws SQLException {
/* 540 */     checkOpen();
/*     */ 
/* 542 */     for (int i = 0; i < this.parameters.length; ++i)
/* 543 */       this.parameters[i].clearInValue();
/*     */   }
/*     */ 
/*     */   public boolean execute() throws SQLException
/*     */   {
/* 548 */     checkOpen();
/* 549 */     initialize();
/* 550 */     boolean useCursor = useCursor(this.returnKeys, this.sqlWord);
/*     */ 
/* 552 */     if ((this.procName == null) && (!(this instanceof JtdsCallableStatement)))
/*     */     {
/* 556 */       synchronized (this.connection) {
/* 557 */         String spName = this.connection.prepareSQL(this, this.sql, this.parameters, this.returnKeys, useCursor);
/* 558 */         return executeSQL(this.sql, spName, this.parameters, this.returnKeys, false, useCursor);
/*     */       }
/*     */     }
/* 561 */     return executeSQL(this.sql, this.procName, this.parameters, this.returnKeys, false, useCursor);
/*     */   }
/*     */ 
/*     */   public void setByte(int parameterIndex, byte x) throws SQLException
/*     */   {
/* 566 */     setParameter(parameterIndex, new Integer(x & 0xFF), -6, 0, 0);
/*     */   }
/*     */ 
/*     */   public void setDouble(int parameterIndex, double x) throws SQLException {
/* 570 */     setParameter(parameterIndex, new Double(x), 8, 0, 0);
/*     */   }
/*     */ 
/*     */   public void setFloat(int parameterIndex, float x) throws SQLException {
/* 574 */     setParameter(parameterIndex, new Float(x), 7, 0, 0);
/*     */   }
/*     */ 
/*     */   public void setInt(int parameterIndex, int x) throws SQLException {
/* 578 */     setParameter(parameterIndex, new Integer(x), 4, 0, 0);
/*     */   }
/*     */ 
/*     */   public void setNull(int parameterIndex, int sqlType) throws SQLException {
/* 582 */     if (sqlType == 2005)
/* 583 */       sqlType = -1;
/* 584 */     else if (sqlType == 2004) {
/* 585 */       sqlType = -4;
/*     */     }
/*     */ 
/* 588 */     setParameter(parameterIndex, null, sqlType, -1, 0);
/*     */   }
/*     */ 
/*     */   public void setLong(int parameterIndex, long x) throws SQLException {
/* 592 */     setParameter(parameterIndex, new Long(x), -5, 0, 0);
/*     */   }
/*     */ 
/*     */   public void setShort(int parameterIndex, short x) throws SQLException {
/* 596 */     setParameter(parameterIndex, new Integer(x), 5, 0, 0);
/*     */   }
/*     */ 
/*     */   public void setBoolean(int parameterIndex, boolean x) throws SQLException {
/* 600 */     setParameter(parameterIndex, (x) ? Boolean.TRUE : Boolean.FALSE, 16, 0, 0);
/*     */   }
/*     */ 
/*     */   public void setBytes(int parameterIndex, byte[] x) throws SQLException {
/* 604 */     setParameter(parameterIndex, x, -2, 0, 0);
/*     */   }
/*     */ 
/*     */   public void setAsciiStream(int parameterIndex, InputStream inputStream, int length) throws SQLException
/*     */   {
/* 609 */     if ((inputStream == null) || (length < 0))
/* 610 */       setParameter(parameterIndex, null, -1, 0, 0);
/*     */     else
/*     */       try {
/* 613 */         setCharacterStream(parameterIndex, new InputStreamReader(inputStream, "US-ASCII"), length);
/*     */       }
/*     */       catch (UnsupportedEncodingException e)
/*     */       {
/*     */       }
/*     */   }
/*     */ 
/*     */   public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException
/*     */   {
/* 622 */     checkOpen();
/*     */ 
/* 624 */     if ((x == null) || (length < 0))
/* 625 */       setBytes(parameterIndex, null);
/*     */     else
/* 627 */       setParameter(parameterIndex, x, -4, 0, length);
/*     */   }
/*     */ 
/*     */   public void setUnicodeStream(int parameterIndex, InputStream inputStream, int length)
/*     */     throws SQLException
/*     */   {
/* 633 */     checkOpen();
/* 634 */     if ((inputStream == null) || (length < 0))
/* 635 */       setString(parameterIndex, null);
/*     */     else
/*     */       try {
/* 638 */         length /= 2;
/* 639 */         char[] tmp = new char[length];
/* 640 */         int pos = 0;
/* 641 */         int b1 = inputStream.read();
/* 642 */         int b2 = inputStream.read();
/*     */ 
/* 644 */         while ((b1 >= 0) && (b2 >= 0) && (pos < length)) {
/* 645 */           tmp[(pos++)] = (char)(b1 << 8 & 0xFF00 | b2 & 0xFF);
/* 646 */           b1 = inputStream.read();
/* 647 */           b2 = inputStream.read();
/*     */         }
/* 649 */         setString(parameterIndex, new String(tmp, 0, pos));
/*     */       } catch (IOException e) {
/* 651 */         throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*     */       }
/*     */   }
/*     */ 
/*     */   public void setCharacterStream(int parameterIndex, Reader reader, int length)
/*     */     throws SQLException
/*     */   {
/* 659 */     if ((reader == null) || (length < 0))
/* 660 */       setParameter(parameterIndex, null, -1, 0, 0);
/*     */     else
/* 662 */       setParameter(parameterIndex, reader, -1, 0, length);
/*     */   }
/*     */ 
/*     */   public void setObject(int parameterIndex, Object x) throws SQLException
/*     */   {
/* 667 */     setObjectBase(parameterIndex, x, Support.getJdbcType(x), -1);
/*     */   }
/*     */ 
/*     */   public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException
/*     */   {
/* 672 */     setObjectBase(parameterIndex, x, targetSqlType, -1);
/*     */   }
/*     */ 
/*     */   public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException
/*     */   {
/* 677 */     checkOpen();
/* 678 */     if ((scale < 0) || (scale > this.connection.getMaxPrecision())) {
/* 679 */       throw new SQLException(Messages.get("error.generic.badscale"), "HY092");
/*     */     }
/* 681 */     setObjectBase(parameterIndex, x, targetSqlType, scale);
/*     */   }
/*     */ 
/*     */   public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
/* 685 */     notImplemented("PreparedStatement.setNull(int, int, String)");
/*     */   }
/*     */ 
/*     */   public void setString(int parameterIndex, String x) throws SQLException {
/* 689 */     setParameter(parameterIndex, x, 12, 0, 0);
/*     */   }
/*     */ 
/*     */   public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
/* 693 */     setParameter(parameterIndex, x, 3, -1, 0);
/*     */   }
/*     */ 
/*     */   public void setURL(int parameterIndex, URL url) throws SQLException {
/* 697 */     setString(parameterIndex, (url == null) ? null : url.toString());
/*     */   }
/*     */ 
/*     */   public void setArray(int arg0, Array arg1) throws SQLException {
/* 701 */     notImplemented("PreparedStatement.setArray");
/*     */   }
/*     */ 
/*     */   public void setBlob(int parameterIndex, Blob x) throws SQLException {
/* 705 */     if (x == null) {
/* 706 */       setBytes(parameterIndex, null);
/*     */     } else {
/* 708 */       long length = x.length();
/*     */ 
/* 710 */       if (length > 2147483647L) {
/* 711 */         throw new SQLException(Messages.get("error.resultset.longblob"), "24000");
/*     */       }
/*     */ 
/* 714 */       setBinaryStream(parameterIndex, x.getBinaryStream(), (int)x.length());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setClob(int parameterIndex, Clob x) throws SQLException {
/* 719 */     if (x == null) {
/* 720 */       setString(parameterIndex, null);
/*     */     } else {
/* 722 */       long length = x.length();
/*     */ 
/* 724 */       if (length > 2147483647L) {
/* 725 */         throw new SQLException(Messages.get("error.resultset.longclob"), "24000");
/*     */       }
/*     */ 
/* 728 */       setCharacterStream(parameterIndex, x.getCharacterStream(), (int)x.length());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setDate(int parameterIndex, Date x) throws SQLException {
/* 733 */     setParameter(parameterIndex, x, 91, 0, 0);
/*     */   }
/*     */ 
/*     */   public ParameterMetaData getParameterMetaData() throws SQLException {
/* 737 */     checkOpen();
/*     */ 
/* 742 */     if (this.connection.getServerType() == 2)
/*     */     {
/* 744 */       this.connection.prepareSQL(this, this.sql, new ParamInfo[0], false, false);
/*     */     }
/*     */     try
/*     */     {
/* 748 */       Class pmdClass = Class.forName("net.sourceforge.jtds.jdbc.ParameterMetaDataImpl");
/* 749 */       Class[] parameterTypes = { [Lnet.sourceforge.jtds.jdbc.ParamInfo.class, ConnectionJDBC2.class };
/* 750 */       Object[] arguments = { this.parameters, this.connection };
/* 751 */       Constructor pmdConstructor = pmdClass.getConstructor(parameterTypes);
/*     */ 
/* 753 */       return ((ParameterMetaData)pmdConstructor.newInstance(arguments));
/*     */     } catch (Exception e) {
/* 755 */       notImplemented("PreparedStatement.getParameterMetaData");
/*     */     }
/*     */ 
/* 758 */     return null;
/*     */   }
/*     */ 
/*     */   public void setRef(int parameterIndex, Ref x) throws SQLException {
/* 762 */     notImplemented("PreparedStatement.setRef");
/*     */   }
/*     */ 
/*     */   public ResultSet executeQuery() throws SQLException {
/* 766 */     checkOpen();
/* 767 */     initialize();
/* 768 */     boolean useCursor = useCursor(false, null);
/*     */ 
/* 770 */     if ((this.procName == null) && (!(this instanceof JtdsCallableStatement)))
/*     */     {
/* 774 */       synchronized (this.connection) {
/* 775 */         String spName = this.connection.prepareSQL(this, this.sql, this.parameters, false, useCursor);
/* 776 */         return executeSQLQuery(this.sql, spName, this.parameters, useCursor);
/*     */       }
/*     */     }
/* 779 */     return executeSQLQuery(this.sql, this.procName, this.parameters, useCursor);
/*     */   }
/*     */ 
/*     */   public ResultSetMetaData getMetaData() throws SQLException
/*     */   {
/* 784 */     checkOpen();
/*     */ 
/* 786 */     if (this.colMetaData == null) {
/* 787 */       if (this.currentResult != null) {
/* 788 */         this.colMetaData = this.currentResult.columns;
/* 789 */       } else if (this.connection.getServerType() == 2)
/*     */       {
/* 791 */         this.connection.prepareSQL(this, this.sql, new ParamInfo[0], false, false);
/*     */ 
/* 793 */         if (this.colMetaData == null) {
/* 794 */           return null;
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 800 */         if (!("select".equals(this.sqlWord))) {
/* 801 */           return null;
/*     */         }
/*     */ 
/* 806 */         ParamInfo[] params = new ParamInfo[this.parameters.length];
/* 807 */         for (int i = 0; i < params.length; ++i) {
/* 808 */           params[i] = new ParamInfo(this.parameters[i].markerPos, false);
/* 809 */           params[i].isSet = true;
/*     */         }
/*     */ 
/* 813 */         StringBuffer testSql = new StringBuffer(this.sql.length() + 128);
/* 814 */         testSql.append("SET FMTONLY ON ");
/* 815 */         testSql.append(Support.substituteParameters(this.sql, params, this.connection));
/*     */ 
/* 817 */         testSql.append(" SET FMTONLY OFF");
/*     */         try
/*     */         {
/* 820 */           this.tds.submitSQL(testSql.toString());
/* 821 */           this.colMetaData = this.tds.getColumns();
/*     */         }
/*     */         catch (SQLException e) {
/* 824 */           this.tds.submitSQL("SET FMTONLY OFF");
/* 825 */           return null;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 830 */     return new JtdsResultSetMetaData(this.colMetaData, JtdsResultSet.getColumnCount(this.colMetaData), this.connection.getUseLOBs());
/*     */   }
/*     */ 
/*     */   public void setTime(int parameterIndex, Time x)
/*     */     throws SQLException
/*     */   {
/* 836 */     setParameter(parameterIndex, x, 92, 0, 0);
/*     */   }
/*     */ 
/*     */   public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
/* 840 */     setParameter(parameterIndex, x, 93, 0, 0);
/*     */   }
/*     */ 
/*     */   public void setDate(int parameterIndex, Date x, Calendar cal)
/*     */     throws SQLException
/*     */   {
/* 846 */     if ((x != null) && (cal != null)) {
/* 847 */       x = new Date(Support.timeFromZone(x, cal));
/*     */     }
/*     */ 
/* 850 */     setDate(parameterIndex, x);
/*     */   }
/*     */ 
/*     */   public void setTime(int parameterIndex, Time x, Calendar cal)
/*     */     throws SQLException
/*     */   {
/* 856 */     if ((x != null) && (cal != null)) {
/* 857 */       x = new Time(Support.timeFromZone(x, cal));
/*     */     }
/*     */ 
/* 860 */     setTime(parameterIndex, x);
/*     */   }
/*     */ 
/*     */   public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
/*     */     throws SQLException
/*     */   {
/* 866 */     if ((x != null) && (cal != null)) {
/* 867 */       x = new Timestamp(Support.timeFromZone(x, cal));
/*     */     }
/*     */ 
/* 870 */     setTimestamp(parameterIndex, x);
/*     */   }
/*     */ 
/*     */   public int executeUpdate(String sql) throws SQLException {
/* 874 */     notSupported("executeUpdate(String)");
/* 875 */     return 0;
/*     */   }
/*     */ 
/*     */   public void addBatch(String sql) throws SQLException {
/* 879 */     notSupported("executeBatch(String)");
/*     */   }
/*     */ 
/*     */   public boolean execute(String sql) throws SQLException {
/* 883 */     notSupported("execute(String)");
/* 884 */     return false;
/*     */   }
/*     */ 
/*     */   public int executeUpdate(String sql, int getKeys) throws SQLException {
/* 888 */     notSupported("executeUpdate(String, int)");
/* 889 */     return 0;
/*     */   }
/*     */ 
/*     */   public boolean execute(String arg0, int arg1) throws SQLException {
/* 893 */     notSupported("execute(String, int)");
/* 894 */     return false;
/*     */   }
/*     */ 
/*     */   public int executeUpdate(String arg0, int[] arg1) throws SQLException {
/* 898 */     notSupported("executeUpdate(String, int[])");
/* 899 */     return 0;
/*     */   }
/*     */ 
/*     */   public boolean execute(String arg0, int[] arg1) throws SQLException {
/* 903 */     notSupported("execute(String, int[])");
/* 904 */     return false;
/*     */   }
/*     */ 
/*     */   public int executeUpdate(String arg0, String[] arg1) throws SQLException {
/* 908 */     notSupported("executeUpdate(String, String[])");
/* 909 */     return 0;
/*     */   }
/*     */ 
/*     */   public boolean execute(String arg0, String[] arg1) throws SQLException {
/* 913 */     notSupported("execute(String, String[])");
/* 914 */     return false;
/*     */   }
/*     */ 
/*     */   public ResultSet executeQuery(String sql) throws SQLException {
/* 918 */     notSupported("executeQuery(String)");
/* 919 */     return null;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.JtdsPreparedStatement
 * JD-Core Version:    0.5.3
 */