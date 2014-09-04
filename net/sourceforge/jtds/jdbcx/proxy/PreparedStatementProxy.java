/*     */ package net.sourceforge.jtds.jdbcx.proxy;
/*     */ 
/*     */ import java.io.InputStream;
/*     */ import java.io.Reader;
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
/*     */ import java.util.Calendar;
/*     */ import net.sourceforge.jtds.jdbc.JtdsPreparedStatement;
/*     */ 
/*     */ public class PreparedStatementProxy extends StatementProxy
/*     */   implements PreparedStatement
/*     */ {
/*     */   private JtdsPreparedStatement _preparedStatement;
/*     */ 
/*     */   PreparedStatementProxy(ConnectionProxy connection, JtdsPreparedStatement preparedStatement)
/*     */   {
/*  42 */     super(connection, preparedStatement);
/*     */ 
/*  44 */     this._preparedStatement = preparedStatement;
/*     */   }
/*     */ 
/*     */   public ResultSet executeQuery()
/*     */     throws SQLException
/*     */   {
/*  55 */     validateConnection();
/*     */     try
/*     */     {
/*  58 */       return this._preparedStatement.executeQuery();
/*     */     } catch (SQLException sqlException) {
/*  60 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/*  63 */     return null;
/*     */   }
/*     */ 
/*     */   public int executeUpdate()
/*     */     throws SQLException
/*     */   {
/*  74 */     validateConnection();
/*     */     try
/*     */     {
/*  77 */       return this._preparedStatement.executeUpdate();
/*     */     } catch (SQLException sqlException) {
/*  79 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/*  82 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public void setNull(int parameterIndex, int sqlType)
/*     */     throws SQLException
/*     */   {
/*  93 */     validateConnection();
/*     */     try
/*     */     {
/*  96 */       this._preparedStatement.setNull(parameterIndex, sqlType);
/*     */     } catch (SQLException sqlException) {
/*  98 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setBoolean(int parameterIndex, boolean x)
/*     */     throws SQLException
/*     */   {
/* 110 */     validateConnection();
/*     */     try
/*     */     {
/* 113 */       this._preparedStatement.setBoolean(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 115 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setByte(int parameterIndex, byte x)
/*     */     throws SQLException
/*     */   {
/* 127 */     validateConnection();
/*     */     try
/*     */     {
/* 130 */       this._preparedStatement.setByte(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 132 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setShort(int parameterIndex, short x)
/*     */     throws SQLException
/*     */   {
/* 144 */     validateConnection();
/*     */     try
/*     */     {
/* 147 */       this._preparedStatement.setShort(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 149 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setInt(int parameterIndex, int x)
/*     */     throws SQLException
/*     */   {
/* 161 */     validateConnection();
/*     */     try
/*     */     {
/* 164 */       this._preparedStatement.setInt(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 166 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setLong(int parameterIndex, long x)
/*     */     throws SQLException
/*     */   {
/* 178 */     validateConnection();
/*     */     try
/*     */     {
/* 181 */       this._preparedStatement.setLong(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 183 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setFloat(int parameterIndex, float x)
/*     */     throws SQLException
/*     */   {
/* 195 */     validateConnection();
/*     */     try
/*     */     {
/* 198 */       this._preparedStatement.setFloat(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 200 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setDouble(int parameterIndex, double x)
/*     */     throws SQLException
/*     */   {
/* 212 */     validateConnection();
/*     */     try
/*     */     {
/* 215 */       this._preparedStatement.setDouble(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 217 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setBigDecimal(int parameterIndex, BigDecimal x)
/*     */     throws SQLException
/*     */   {
/* 229 */     validateConnection();
/*     */     try
/*     */     {
/* 232 */       this._preparedStatement.setBigDecimal(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 234 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setString(int parameterIndex, String x)
/*     */     throws SQLException
/*     */   {
/* 246 */     validateConnection();
/*     */     try
/*     */     {
/* 249 */       this._preparedStatement.setString(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 251 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setBytes(int parameterIndex, byte[] x)
/*     */     throws SQLException
/*     */   {
/* 263 */     validateConnection();
/*     */     try
/*     */     {
/* 266 */       this._preparedStatement.setBytes(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 268 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setDate(int parameterIndex, Date x)
/*     */     throws SQLException
/*     */   {
/* 280 */     validateConnection();
/*     */     try
/*     */     {
/* 283 */       this._preparedStatement.setDate(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 285 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setTime(int parameterIndex, Time x)
/*     */     throws SQLException
/*     */   {
/* 297 */     validateConnection();
/*     */     try
/*     */     {
/* 300 */       this._preparedStatement.setTime(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 302 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setTimestamp(int parameterIndex, Timestamp x)
/*     */     throws SQLException
/*     */   {
/* 314 */     validateConnection();
/*     */     try
/*     */     {
/* 317 */       this._preparedStatement.setTimestamp(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 319 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setAsciiStream(int parameterIndex, InputStream x, int length)
/*     */     throws SQLException
/*     */   {
/* 331 */     validateConnection();
/*     */     try
/*     */     {
/* 334 */       this._preparedStatement.setAsciiStream(parameterIndex, x, length);
/*     */     } catch (SQLException sqlException) {
/* 336 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setUnicodeStream(int parameterIndex, InputStream x, int length)
/*     */     throws SQLException
/*     */   {
/* 348 */     validateConnection();
/*     */     try
/*     */     {
/* 351 */       this._preparedStatement.setUnicodeStream(parameterIndex, x, length);
/*     */     } catch (SQLException sqlException) {
/* 353 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setBinaryStream(int parameterIndex, InputStream x, int length)
/*     */     throws SQLException
/*     */   {
/* 365 */     validateConnection();
/*     */     try
/*     */     {
/* 368 */       this._preparedStatement.setBinaryStream(parameterIndex, x, length);
/*     */     } catch (SQLException sqlException) {
/* 370 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clearParameters()
/*     */     throws SQLException
/*     */   {
/* 382 */     validateConnection();
/*     */     try
/*     */     {
/* 385 */       this._preparedStatement.clearParameters();
/*     */     } catch (SQLException sqlException) {
/* 387 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
/*     */     throws SQLException
/*     */   {
/* 399 */     validateConnection();
/*     */     try
/*     */     {
/* 402 */       this._preparedStatement.setObject(parameterIndex, x, targetSqlType, scale);
/*     */     } catch (SQLException sqlException) {
/* 404 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setObject(int parameterIndex, Object x, int targetSqlType)
/*     */     throws SQLException
/*     */   {
/* 416 */     validateConnection();
/*     */     try
/*     */     {
/* 419 */       this._preparedStatement.setObject(parameterIndex, x, targetSqlType);
/*     */     } catch (SQLException sqlException) {
/* 421 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setObject(int parameterIndex, Object x)
/*     */     throws SQLException
/*     */   {
/* 433 */     validateConnection();
/*     */     try
/*     */     {
/* 436 */       this._preparedStatement.setObject(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 438 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean execute()
/*     */     throws SQLException
/*     */   {
/* 450 */     validateConnection();
/*     */     try
/*     */     {
/* 453 */       return this._preparedStatement.execute();
/*     */     } catch (SQLException sqlException) {
/* 455 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 458 */     return false;
/*     */   }
/*     */ 
/*     */   public void addBatch()
/*     */     throws SQLException
/*     */   {
/* 469 */     validateConnection();
/*     */     try
/*     */     {
/* 472 */       this._preparedStatement.addBatch();
/*     */     } catch (SQLException sqlException) {
/* 474 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setCharacterStream(int parameterIndex, Reader x, int length)
/*     */     throws SQLException
/*     */   {
/* 486 */     validateConnection();
/*     */     try
/*     */     {
/* 489 */       this._preparedStatement.setCharacterStream(parameterIndex, x, length);
/*     */     } catch (SQLException sqlException) {
/* 491 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setRef(int parameterIndex, Ref x)
/*     */     throws SQLException
/*     */   {
/* 503 */     validateConnection();
/*     */     try
/*     */     {
/* 506 */       this._preparedStatement.setRef(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 508 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setBlob(int parameterIndex, Blob x)
/*     */     throws SQLException
/*     */   {
/* 520 */     validateConnection();
/*     */     try
/*     */     {
/* 523 */       this._preparedStatement.setBlob(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 525 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setClob(int parameterIndex, Clob x)
/*     */     throws SQLException
/*     */   {
/* 537 */     validateConnection();
/*     */     try
/*     */     {
/* 540 */       this._preparedStatement.setClob(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 542 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setArray(int parameterIndex, Array x)
/*     */     throws SQLException
/*     */   {
/* 554 */     validateConnection();
/*     */     try
/*     */     {
/* 557 */       this._preparedStatement.setArray(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 559 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public ResultSetMetaData getMetaData()
/*     */     throws SQLException
/*     */   {
/* 571 */     validateConnection();
/*     */     try
/*     */     {
/* 574 */       return this._preparedStatement.getMetaData();
/*     */     } catch (SQLException sqlException) {
/* 576 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 579 */     return null;
/*     */   }
/*     */ 
/*     */   public void setDate(int parameterIndex, Date x, Calendar cal)
/*     */     throws SQLException
/*     */   {
/* 590 */     validateConnection();
/*     */     try
/*     */     {
/* 593 */       this._preparedStatement.setDate(parameterIndex, x, cal);
/*     */     } catch (SQLException sqlException) {
/* 595 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setTime(int parameterIndex, Time x, Calendar cal)
/*     */     throws SQLException
/*     */   {
/* 607 */     validateConnection();
/*     */     try
/*     */     {
/* 610 */       this._preparedStatement.setTime(parameterIndex, x, cal);
/*     */     } catch (SQLException sqlException) {
/* 612 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
/*     */     throws SQLException
/*     */   {
/* 624 */     validateConnection();
/*     */     try
/*     */     {
/* 627 */       this._preparedStatement.setTimestamp(parameterIndex, x, cal);
/*     */     } catch (SQLException sqlException) {
/* 629 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setNull(int parameterIndex, int sqlType, String typeName)
/*     */     throws SQLException
/*     */   {
/* 641 */     validateConnection();
/*     */     try
/*     */     {
/* 644 */       this._preparedStatement.setNull(parameterIndex, sqlType, typeName);
/*     */     } catch (SQLException sqlException) {
/* 646 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setURL(int parameterIndex, URL x)
/*     */     throws SQLException
/*     */   {
/* 658 */     validateConnection();
/*     */     try
/*     */     {
/* 661 */       this._preparedStatement.setURL(parameterIndex, x);
/*     */     } catch (SQLException sqlException) {
/* 663 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public ParameterMetaData getParameterMetaData()
/*     */     throws SQLException
/*     */   {
/* 675 */     validateConnection();
/*     */     try
/*     */     {
/* 678 */       return this._preparedStatement.getParameterMetaData();
/*     */     } catch (SQLException sqlException) {
/* 680 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 683 */     return null;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbcx.proxy.PreparedStatementProxy
 * JD-Core Version:    0.5.3
 */