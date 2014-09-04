/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.InputStream;
/*     */ import java.io.Reader;
/*     */ import java.math.BigDecimal;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.sql.Array;
/*     */ import java.sql.Blob;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Clob;
/*     */ import java.sql.Date;
/*     */ import java.sql.Ref;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Time;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Calendar;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class JtdsCallableStatement extends JtdsPreparedStatement
/*     */   implements CallableStatement
/*     */ {
/*     */   protected boolean paramWasNull;
/*     */ 
/*     */   JtdsCallableStatement(ConnectionJDBC2 connection, String sql, int resultSetType, int concurrency)
/*     */     throws SQLException
/*     */   {
/*  59 */     super(connection, sql, resultSetType, concurrency, false);
/*     */   }
/*     */ 
/*     */   int findParameter(String name, boolean set)
/*     */     throws SQLException
/*     */   {
/*  72 */     checkOpen();
/*  73 */     for (int i = 0; i < this.parameters.length; ++i) {
/*  74 */       if ((this.parameters[i].name != null) && (this.parameters[i].name.equalsIgnoreCase(name))) {
/*  75 */         return (i + 1);
/*     */       }
/*     */     }
/*  78 */     if ((set) && (!(name.equalsIgnoreCase("@return_status")))) {
/*  79 */       for (i = 0; i < this.parameters.length; ++i) {
/*  80 */         if (this.parameters[i].name == null) {
/*  81 */           this.parameters[i].name = name;
/*     */ 
/*  83 */           return (i + 1);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*  88 */     throw new SQLException(Messages.get("error.callable.noparam", name), "07000");
/*     */   }
/*     */ 
/*     */   protected Object getOutputValue(int parameterIndex)
/*     */     throws SQLException
/*     */   {
/* 100 */     checkOpen();
/* 101 */     ParamInfo parameter = getParameter(parameterIndex);
/* 102 */     if (!(parameter.isOutput)) {
/* 103 */       throw new SQLException(Messages.get("error.callable.notoutput", new Integer(parameterIndex)), "07000");
/*     */     }
/*     */ 
/* 108 */     Object value = parameter.getOutValue();
/* 109 */     this.paramWasNull = (value == null);
/* 110 */     return value;
/*     */   }
/*     */ 
/*     */   protected void checkOpen()
/*     */     throws SQLException
/*     */   {
/* 119 */     if (this.closed)
/* 120 */       throw new SQLException(Messages.get("error.generic.closed", "CallableStatement"), "HY010");
/*     */   }
/*     */ 
/*     */   protected SQLException executeMSBatch(int size, int executeSize, ArrayList counts)
/*     */     throws SQLException
/*     */   {
/* 135 */     if (this.parameters.length == 0)
/*     */     {
/* 137 */       return super.executeMSBatch(size, executeSize, counts);
/*     */     }
/* 139 */     SQLException sqlEx = null;
/* 140 */     for (int i = 0; i < size; ) {
/* 141 */       Object value = this.batchValues.get(i);
/* 142 */       ++i;
/*     */ 
/* 144 */       boolean executeNow = (i % executeSize == 0) || (i == size);
/*     */ 
/* 146 */       this.tds.startBatch();
/* 147 */       this.tds.executeSQL(this.sql, this.procName, (ParamInfo[])value, false, 0, -1, -1, executeNow);
/*     */ 
/* 150 */       if (executeNow) {
/* 151 */         sqlEx = this.tds.getBatchCounts(counts, sqlEx);
/*     */ 
/* 155 */         if ((sqlEx != null) && (counts.size() != i)) {
/*     */           break;
/*     */         }
/*     */       }
/*     */     }
/* 160 */     return sqlEx;
/*     */   }
/*     */ 
/*     */   protected SQLException executeSybaseBatch(int size, int executeSize, ArrayList counts)
/*     */     throws SQLException
/*     */   {
/* 180 */     if (this.parameters.length == 0)
/*     */     {
/* 182 */       return super.executeSybaseBatch(size, executeSize, counts);
/*     */     }
/*     */ 
/* 185 */     SQLException sqlEx = null;
/*     */ 
/* 187 */     for (int i = 0; i < size; ) {
/* 188 */       Object value = this.batchValues.get(i);
/* 189 */       ++i;
/* 190 */       this.tds.executeSQL(this.sql, this.procName, (ParamInfo[])value, false, 0, -1, -1, true);
/*     */ 
/* 193 */       sqlEx = this.tds.getBatchCounts(counts, sqlEx);
/*     */ 
/* 197 */       if ((sqlEx != null) && (counts.size() != i)) {
/*     */         break;
/*     */       }
/*     */     }
/* 201 */     return sqlEx;
/*     */   }
/*     */ 
/*     */   public boolean wasNull()
/*     */     throws SQLException
/*     */   {
/* 208 */     checkOpen();
/*     */ 
/* 210 */     return this.paramWasNull;
/*     */   }
/*     */ 
/*     */   public byte getByte(int parameterIndex) throws SQLException {
/* 214 */     return ((Integer)Support.convert(this, getOutputValue(parameterIndex), -6, null)).byteValue();
/*     */   }
/*     */ 
/*     */   public double getDouble(int parameterIndex) throws SQLException {
/* 218 */     return ((Double)Support.convert(this, getOutputValue(parameterIndex), 8, null)).doubleValue();
/*     */   }
/*     */ 
/*     */   public float getFloat(int parameterIndex) throws SQLException {
/* 222 */     return ((Float)Support.convert(this, getOutputValue(parameterIndex), 7, null)).floatValue();
/*     */   }
/*     */ 
/*     */   public int getInt(int parameterIndex) throws SQLException {
/* 226 */     return ((Integer)Support.convert(this, getOutputValue(parameterIndex), 4, null)).intValue();
/*     */   }
/*     */ 
/*     */   public long getLong(int parameterIndex) throws SQLException {
/* 230 */     return ((Long)Support.convert(this, getOutputValue(parameterIndex), -5, null)).longValue();
/*     */   }
/*     */ 
/*     */   public short getShort(int parameterIndex) throws SQLException {
/* 234 */     return ((Integer)Support.convert(this, getOutputValue(parameterIndex), 5, null)).shortValue();
/*     */   }
/*     */ 
/*     */   public boolean getBoolean(int parameterIndex) throws SQLException {
/* 238 */     return ((Boolean)Support.convert(this, getOutputValue(parameterIndex), 16, null)).booleanValue();
/*     */   }
/*     */ 
/*     */   public byte[] getBytes(int parameterIndex) throws SQLException {
/* 242 */     checkOpen();
/* 243 */     return ((byte[])Support.convert(this, getOutputValue(parameterIndex), -3, this.connection.getCharset()));
/*     */   }
/*     */ 
/*     */   public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
/* 247 */     if ((sqlType == 3) || (sqlType == 2))
/*     */     {
/* 249 */       registerOutParameter(parameterIndex, sqlType, 10);
/*     */     }
/*     */     else registerOutParameter(parameterIndex, sqlType, 0);
/*     */   }
/*     */ 
/*     */   public void registerOutParameter(int parameterIndex, int sqlType, int scale)
/*     */     throws SQLException
/*     */   {
/* 257 */     checkOpen();
/*     */ 
/* 259 */     if ((scale < 0) || (scale > this.connection.getMaxPrecision())) {
/* 260 */       throw new SQLException(Messages.get("error.generic.badscale"), "HY092");
/*     */     }
/*     */ 
/* 263 */     ParamInfo pi = getParameter(parameterIndex);
/*     */ 
/* 265 */     pi.isOutput = true;
/*     */ 
/* 267 */     if ("ERROR".equals(Support.getJdbcTypeName(sqlType))) {
/* 268 */       throw new SQLException(Messages.get("error.generic.badtype", Integer.toString(sqlType)), "HY092");
/*     */     }
/*     */ 
/* 272 */     if (sqlType == 2005)
/* 273 */       pi.jdbcType = -1;
/* 274 */     else if (sqlType == 2004)
/* 275 */       pi.jdbcType = -4;
/*     */     else {
/* 277 */       pi.jdbcType = sqlType;
/*     */     }
/*     */ 
/* 280 */     pi.scale = scale;
/*     */   }
/*     */ 
/*     */   public Object getObject(int parameterIndex) throws SQLException {
/* 284 */     Object value = getOutputValue(parameterIndex);
/*     */ 
/* 288 */     if (value instanceof UniqueIdentifier) {
/* 289 */       return value.toString();
/*     */     }
/*     */ 
/* 293 */     if (!(this.connection.getUseLOBs())) {
/* 294 */       value = Support.convertLOB(value);
/*     */     }
/*     */ 
/* 297 */     return value;
/*     */   }
/*     */ 
/*     */   public String getString(int parameterIndex) throws SQLException {
/* 301 */     checkOpen();
/* 302 */     return ((String)Support.convert(this, getOutputValue(parameterIndex), 12, this.connection.getCharset()));
/*     */   }
/*     */ 
/*     */   public void registerOutParameter(int parameterIndex, int sqlType, String typeName)
/*     */     throws SQLException
/*     */   {
/* 308 */     notImplemented("CallableStatement.registerOutParameter(int, int, String");
/*     */   }
/*     */ 
/*     */   public byte getByte(String parameterName) throws SQLException {
/* 312 */     return getByte(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public double getDouble(String parameterName) throws SQLException {
/* 316 */     return getDouble(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public float getFloat(String parameterName) throws SQLException {
/* 320 */     return getFloat(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public int getInt(String parameterName) throws SQLException {
/* 324 */     return getInt(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public long getLong(String parameterName) throws SQLException {
/* 328 */     return getLong(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public short getShort(String parameterName) throws SQLException {
/* 332 */     return getShort(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public boolean getBoolean(String parameterName) throws SQLException {
/* 336 */     return getBoolean(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public byte[] getBytes(String parameterName) throws SQLException {
/* 340 */     return getBytes(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public void setByte(String parameterName, byte x) throws SQLException {
/* 344 */     setByte(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public void setDouble(String parameterName, double x) throws SQLException {
/* 348 */     setDouble(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public void setFloat(String parameterName, float x) throws SQLException {
/* 352 */     setFloat(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public void registerOutParameter(String parameterName, int sqlType) throws SQLException
/*     */   {
/* 357 */     registerOutParameter(findParameter(parameterName, true), sqlType);
/*     */   }
/*     */ 
/*     */   public void setInt(String parameterName, int x) throws SQLException {
/* 361 */     setInt(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public void setNull(String parameterName, int sqlType) throws SQLException {
/* 365 */     setNull(findParameter(parameterName, true), sqlType);
/*     */   }
/*     */ 
/*     */   public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException
/*     */   {
/* 370 */     registerOutParameter(findParameter(parameterName, true), sqlType, scale);
/*     */   }
/*     */ 
/*     */   public void setLong(String parameterName, long x) throws SQLException {
/* 374 */     setLong(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public void setShort(String parameterName, short x) throws SQLException {
/* 378 */     setShort(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public void setBoolean(String parameterName, boolean x) throws SQLException {
/* 382 */     setBoolean(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public void setBytes(String parameterName, byte[] x) throws SQLException {
/* 386 */     setBytes(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
/* 390 */     return ((BigDecimal)Support.convert(this, getOutputValue(parameterIndex), 3, null));
/*     */   }
/*     */ 
/*     */   public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException
/*     */   {
/* 395 */     BigDecimal bd = (BigDecimal)Support.convert(this, getOutputValue(parameterIndex), 3, null);
/*     */ 
/* 398 */     return bd.setScale(scale);
/*     */   }
/*     */ 
/*     */   public URL getURL(int parameterIndex) throws SQLException {
/* 402 */     checkOpen();
/* 403 */     String url = (String)Support.convert(this, getOutputValue(parameterIndex), 12, this.connection.getCharset());
/*     */     try
/*     */     {
/* 408 */       return new URL(url);
/*     */     } catch (MalformedURLException e) {
/* 410 */       throw new SQLException(Messages.get("error.resultset.badurl", url), "22000");
/*     */     }
/*     */   }
/*     */ 
/*     */   public Array getArray(int parameterIndex) throws SQLException {
/* 415 */     notImplemented("CallableStatement.getArray");
/* 416 */     return null;
/*     */   }
/*     */ 
/*     */   public Blob getBlob(int parameterIndex) throws SQLException {
/* 420 */     byte[] value = getBytes(parameterIndex);
/*     */ 
/* 422 */     if (value == null) {
/* 423 */       return null;
/*     */     }
/*     */ 
/* 426 */     return new BlobImpl(this.connection, value);
/*     */   }
/*     */ 
/*     */   public Clob getClob(int parameterIndex) throws SQLException {
/* 430 */     String value = getString(parameterIndex);
/*     */ 
/* 432 */     if (value == null) {
/* 433 */       return null;
/*     */     }
/*     */ 
/* 436 */     return new ClobImpl(this.connection, value);
/*     */   }
/*     */ 
/*     */   public Date getDate(int parameterIndex) throws SQLException {
/* 440 */     return ((Date)Support.convert(this, getOutputValue(parameterIndex), 91, null));
/*     */   }
/*     */ 
/*     */   public Ref getRef(int parameterIndex) throws SQLException
/*     */   {
/* 445 */     notImplemented("CallableStatement.getRef");
/* 446 */     return null;
/*     */   }
/*     */ 
/*     */   public Time getTime(int parameterIndex) throws SQLException {
/* 450 */     return ((Time)Support.convert(this, getOutputValue(parameterIndex), 92, null));
/*     */   }
/*     */ 
/*     */   public Timestamp getTimestamp(int parameterIndex) throws SQLException {
/* 454 */     return ((Timestamp)Support.convert(this, getOutputValue(parameterIndex), 93, null));
/*     */   }
/*     */ 
/*     */   public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException
/*     */   {
/* 459 */     setAsciiStream(findParameter(parameterName, true), x, length);
/*     */   }
/*     */ 
/*     */   public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException
/*     */   {
/* 464 */     setBinaryStream(findParameter(parameterName, true), x, length);
/*     */   }
/*     */ 
/*     */   public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException
/*     */   {
/* 469 */     setCharacterStream(findParameter(parameterName, true), reader, length);
/*     */   }
/*     */ 
/*     */   public Object getObject(String parameterName) throws SQLException {
/* 473 */     return getObject(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public void setObject(String parameterName, Object x) throws SQLException {
/* 477 */     setObject(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException
/*     */   {
/* 482 */     setObject(findParameter(parameterName, true), x, targetSqlType);
/*     */   }
/*     */ 
/*     */   public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException
/*     */   {
/* 487 */     setObject(findParameter(parameterName, true), x, targetSqlType, scale);
/*     */   }
/*     */ 
/*     */   public Object getObject(int parameterIndex, Map map) throws SQLException {
/* 491 */     notImplemented("CallableStatement.getObject(int, Map)");
/* 492 */     return null;
/*     */   }
/*     */ 
/*     */   public String getString(String parameterName) throws SQLException {
/* 496 */     return getString(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException
/*     */   {
/* 501 */     notImplemented("CallableStatement.registerOutParameter(String, int, String");
/*     */   }
/*     */ 
/*     */   public void setNull(String parameterName, int sqlType, String typeName) throws SQLException
/*     */   {
/* 506 */     notImplemented("CallableStatement.setNull(String, int, String");
/*     */   }
/*     */ 
/*     */   public void setString(String parameterName, String x) throws SQLException {
/* 510 */     setString(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public BigDecimal getBigDecimal(String parameterName) throws SQLException {
/* 514 */     return getBigDecimal(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException
/*     */   {
/* 519 */     setBigDecimal(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public URL getURL(String parameterName) throws SQLException {
/* 523 */     return getURL(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public void setURL(String parameterName, URL x) throws SQLException {
/* 527 */     setObject(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public Array getArray(String parameterName) throws SQLException {
/* 531 */     return getArray(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public Blob getBlob(String parameterName) throws SQLException {
/* 535 */     return getBlob(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public Clob getClob(String parameterName) throws SQLException {
/* 539 */     return getClob(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public Date getDate(String parameterName) throws SQLException {
/* 543 */     return getDate(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public void setDate(String parameterName, Date x) throws SQLException {
/* 547 */     setDate(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
/* 551 */     Date date = getDate(parameterIndex);
/*     */ 
/* 553 */     if ((date != null) && (cal != null)) {
/* 554 */       date = new Date(Support.timeToZone(date, cal));
/*     */     }
/*     */ 
/* 557 */     return date;
/*     */   }
/*     */ 
/*     */   public Ref getRef(String parameterName) throws SQLException {
/* 561 */     return getRef(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public Time getTime(String parameterName) throws SQLException {
/* 565 */     return getTime(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public void setTime(String parameterName, Time x) throws SQLException {
/* 569 */     setTime(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
/* 573 */     Time time = getTime(parameterIndex);
/*     */ 
/* 575 */     if ((time != null) && (cal != null)) {
/* 576 */       time = new Time(Support.timeToZone(time, cal));
/*     */     }
/*     */ 
/* 579 */     return time;
/*     */   }
/*     */ 
/*     */   public Timestamp getTimestamp(String parameterName) throws SQLException {
/* 583 */     return getTimestamp(findParameter(parameterName, false));
/*     */   }
/*     */ 
/*     */   public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
/* 587 */     setTimestamp(findParameter(parameterName, true), x);
/*     */   }
/*     */ 
/*     */   public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException
/*     */   {
/* 592 */     Timestamp timestamp = getTimestamp(parameterIndex);
/*     */ 
/* 594 */     if ((timestamp != null) && (cal != null)) {
/* 595 */       timestamp = new Timestamp(Support.timeToZone(timestamp, cal));
/*     */     }
/*     */ 
/* 598 */     return timestamp;
/*     */   }
/*     */ 
/*     */   public Object getObject(String parameterName, Map map) throws SQLException {
/* 602 */     return getObject(findParameter(parameterName, false), map);
/*     */   }
/*     */ 
/*     */   public Date getDate(String parameterName, Calendar cal) throws SQLException {
/* 606 */     return getDate(findParameter(parameterName, false), cal);
/*     */   }
/*     */ 
/*     */   public Time getTime(String parameterName, Calendar cal) throws SQLException {
/* 610 */     return getTime(findParameter(parameterName, false), cal);
/*     */   }
/*     */ 
/*     */   public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException
/*     */   {
/* 615 */     return getTimestamp(findParameter(parameterName, false), cal);
/*     */   }
/*     */ 
/*     */   public void setDate(String parameterName, Date x, Calendar cal) throws SQLException
/*     */   {
/* 620 */     setDate(findParameter(parameterName, true), x, cal);
/*     */   }
/*     */ 
/*     */   public void setTime(String parameterName, Time x, Calendar cal) throws SQLException
/*     */   {
/* 625 */     setTime(findParameter(parameterName, true), x, cal);
/*     */   }
/*     */ 
/*     */   public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException
/*     */   {
/* 630 */     setTimestamp(findParameter(parameterName, true), x, cal);
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.JtdsCallableStatement
 * JD-Core Version:    0.5.3
 */