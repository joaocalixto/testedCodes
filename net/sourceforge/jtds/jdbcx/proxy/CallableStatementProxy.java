/*      */ package net.sourceforge.jtds.jdbcx.proxy;
/*      */ 
/*      */ import java.io.InputStream;
/*      */ import java.io.Reader;
/*      */ import java.math.BigDecimal;
/*      */ import java.net.URL;
/*      */ import java.sql.Array;
/*      */ import java.sql.Blob;
/*      */ import java.sql.CallableStatement;
/*      */ import java.sql.Clob;
/*      */ import java.sql.Date;
/*      */ import java.sql.Ref;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.Time;
/*      */ import java.sql.Timestamp;
/*      */ import java.util.Calendar;
/*      */ import java.util.Map;
/*      */ import net.sourceforge.jtds.jdbc.JtdsCallableStatement;
/*      */ 
/*      */ public class CallableStatementProxy extends PreparedStatementProxy
/*      */   implements CallableStatement
/*      */ {
/*      */   private JtdsCallableStatement _callableStatement;
/*      */ 
/*      */   CallableStatementProxy(ConnectionProxy connection, JtdsCallableStatement callableStatement)
/*      */   {
/*   42 */     super(connection, callableStatement);
/*      */ 
/*   44 */     this._callableStatement = callableStatement;
/*      */   }
/*      */ 
/*      */   public void registerOutParameter(int parameterIndex, int sqlType)
/*      */     throws SQLException
/*      */   {
/*   55 */     validateConnection();
/*      */     try
/*      */     {
/*   58 */       this._callableStatement.registerOutParameter(parameterIndex, sqlType);
/*      */     } catch (SQLException sqlException) {
/*   60 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void registerOutParameter(int parameterIndex, int sqlType, int scale)
/*      */     throws SQLException
/*      */   {
/*   72 */     validateConnection();
/*      */     try
/*      */     {
/*   75 */       this._callableStatement.registerOutParameter(parameterIndex, sqlType, scale);
/*      */     } catch (SQLException sqlException) {
/*   77 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean wasNull()
/*      */     throws SQLException
/*      */   {
/*   89 */     validateConnection();
/*      */     try
/*      */     {
/*   92 */       return this._callableStatement.wasNull();
/*      */     } catch (SQLException sqlException) {
/*   94 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*   97 */     return false;
/*      */   }
/*      */ 
/*      */   public String getString(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  108 */     validateConnection();
/*      */     try
/*      */     {
/*  111 */       return this._callableStatement.getString(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  113 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  116 */     return null;
/*      */   }
/*      */ 
/*      */   public boolean getBoolean(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  127 */     validateConnection();
/*      */     try
/*      */     {
/*  130 */       return this._callableStatement.getBoolean(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  132 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  135 */     return false;
/*      */   }
/*      */ 
/*      */   public byte getByte(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  146 */     validateConnection();
/*      */     try
/*      */     {
/*  149 */       return this._callableStatement.getByte(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  151 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  154 */     return -128;
/*      */   }
/*      */ 
/*      */   public short getShort(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  165 */     validateConnection();
/*      */     try
/*      */     {
/*  168 */       return this._callableStatement.getShort(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  170 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  173 */     return -32768;
/*      */   }
/*      */ 
/*      */   public int getInt(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  184 */     validateConnection();
/*      */     try
/*      */     {
/*  187 */       return this._callableStatement.getInt(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  189 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  192 */     return -2147483648;
/*      */   }
/*      */ 
/*      */   public long getLong(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  203 */     validateConnection();
/*      */     try
/*      */     {
/*  206 */       return this._callableStatement.getLong(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  208 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  211 */     return -9223372036854775808L;
/*      */   }
/*      */ 
/*      */   public float getFloat(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  222 */     validateConnection();
/*      */     try
/*      */     {
/*  225 */       return this._callableStatement.getFloat(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  227 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  230 */     return 1.4E-45F;
/*      */   }
/*      */ 
/*      */   public double getDouble(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  241 */     validateConnection();
/*      */     try
/*      */     {
/*  244 */       return this._callableStatement.getDouble(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  246 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  249 */     return 4.9E-324D;
/*      */   }
/*      */ 
/*      */   public BigDecimal getBigDecimal(int parameterIndex, int scale)
/*      */     throws SQLException
/*      */   {
/*  260 */     validateConnection();
/*      */     try
/*      */     {
/*  263 */       return this._callableStatement.getBigDecimal(parameterIndex, scale);
/*      */     } catch (SQLException sqlException) {
/*  265 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  268 */     return null;
/*      */   }
/*      */ 
/*      */   public byte[] getBytes(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  279 */     validateConnection();
/*      */     try
/*      */     {
/*  282 */       return this._callableStatement.getBytes(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  284 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  287 */     return null;
/*      */   }
/*      */ 
/*      */   public Date getDate(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  298 */     validateConnection();
/*      */     try
/*      */     {
/*  301 */       return this._callableStatement.getDate(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  303 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  306 */     return null;
/*      */   }
/*      */ 
/*      */   public Time getTime(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  317 */     validateConnection();
/*      */     try
/*      */     {
/*  320 */       return this._callableStatement.getTime(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  322 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  325 */     return null;
/*      */   }
/*      */ 
/*      */   public Timestamp getTimestamp(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  336 */     validateConnection();
/*      */     try
/*      */     {
/*  339 */       return this._callableStatement.getTimestamp(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  341 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  344 */     return null;
/*      */   }
/*      */ 
/*      */   public Object getObject(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  355 */     validateConnection();
/*      */     try
/*      */     {
/*  358 */       return this._callableStatement.getObject(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  360 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  363 */     return null;
/*      */   }
/*      */ 
/*      */   public BigDecimal getBigDecimal(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  374 */     validateConnection();
/*      */     try
/*      */     {
/*  377 */       return this._callableStatement.getBigDecimal(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  379 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  382 */     return null;
/*      */   }
/*      */ 
/*      */   public Object getObject(int parameterIndex, Map map)
/*      */     throws SQLException
/*      */   {
/*  393 */     validateConnection();
/*      */     try
/*      */     {
/*  396 */       return this._callableStatement.getObject(parameterIndex, map);
/*      */     } catch (SQLException sqlException) {
/*  398 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  401 */     return null;
/*      */   }
/*      */ 
/*      */   public Ref getRef(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  412 */     validateConnection();
/*      */     try
/*      */     {
/*  415 */       return this._callableStatement.getRef(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  417 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  420 */     return null;
/*      */   }
/*      */ 
/*      */   public Blob getBlob(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  431 */     validateConnection();
/*      */     try
/*      */     {
/*  434 */       return this._callableStatement.getBlob(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  436 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  439 */     return null;
/*      */   }
/*      */ 
/*      */   public Clob getClob(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  450 */     validateConnection();
/*      */     try
/*      */     {
/*  453 */       return this._callableStatement.getClob(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  455 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  458 */     return null;
/*      */   }
/*      */ 
/*      */   public Array getArray(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  469 */     validateConnection();
/*      */     try
/*      */     {
/*  472 */       return this._callableStatement.getArray(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  474 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  477 */     return null;
/*      */   }
/*      */ 
/*      */   public Date getDate(int parameterIndex, Calendar cal)
/*      */     throws SQLException
/*      */   {
/*  488 */     validateConnection();
/*      */     try
/*      */     {
/*  491 */       return this._callableStatement.getDate(parameterIndex, cal);
/*      */     } catch (SQLException sqlException) {
/*  493 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  496 */     return null;
/*      */   }
/*      */ 
/*      */   public Time getTime(int parameterIndex, Calendar cal)
/*      */     throws SQLException
/*      */   {
/*  507 */     validateConnection();
/*      */     try
/*      */     {
/*  510 */       return this._callableStatement.getTime(parameterIndex, cal);
/*      */     } catch (SQLException sqlException) {
/*  512 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  515 */     return null;
/*      */   }
/*      */ 
/*      */   public Timestamp getTimestamp(int parameterIndex, Calendar cal)
/*      */     throws SQLException
/*      */   {
/*  526 */     validateConnection();
/*      */     try
/*      */     {
/*  529 */       return this._callableStatement.getTimestamp(parameterIndex, cal);
/*      */     } catch (SQLException sqlException) {
/*  531 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  534 */     return null;
/*      */   }
/*      */ 
/*      */   public void registerOutParameter(int parameterIndex, int sqlType, String typeName)
/*      */     throws SQLException
/*      */   {
/*  545 */     validateConnection();
/*      */     try
/*      */     {
/*  548 */       this._callableStatement.registerOutParameter(parameterIndex, sqlType, typeName);
/*      */     } catch (SQLException sqlException) {
/*  550 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void registerOutParameter(String parameterName, int sqlType)
/*      */     throws SQLException
/*      */   {
/*  562 */     validateConnection();
/*      */     try
/*      */     {
/*  565 */       this._callableStatement.registerOutParameter(parameterName, sqlType);
/*      */     } catch (SQLException sqlException) {
/*  567 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void registerOutParameter(String parameterName, int sqlType, int scale)
/*      */     throws SQLException
/*      */   {
/*  579 */     validateConnection();
/*      */     try
/*      */     {
/*  582 */       this._callableStatement.registerOutParameter(parameterName, sqlType, scale);
/*      */     } catch (SQLException sqlException) {
/*  584 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void registerOutParameter(String parameterName, int sqlType, String typeName)
/*      */     throws SQLException
/*      */   {
/*  596 */     validateConnection();
/*      */     try
/*      */     {
/*  599 */       this._callableStatement.registerOutParameter(parameterName, sqlType, typeName);
/*      */     } catch (SQLException sqlException) {
/*  601 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public URL getURL(int parameterIndex)
/*      */     throws SQLException
/*      */   {
/*  613 */     validateConnection();
/*      */     try
/*      */     {
/*  616 */       return this._callableStatement.getURL(parameterIndex);
/*      */     } catch (SQLException sqlException) {
/*  618 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/*  621 */     return null;
/*      */   }
/*      */ 
/*      */   public void setURL(String parameterName, URL val)
/*      */     throws SQLException
/*      */   {
/*  632 */     validateConnection();
/*      */     try
/*      */     {
/*  635 */       this._callableStatement.setURL(parameterName, val);
/*      */     } catch (SQLException sqlException) {
/*  637 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setNull(String parameterName, int sqlType)
/*      */     throws SQLException
/*      */   {
/*  649 */     validateConnection();
/*      */     try
/*      */     {
/*  652 */       this._callableStatement.setNull(parameterName, sqlType);
/*      */     } catch (SQLException sqlException) {
/*  654 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setBoolean(String parameterName, boolean x)
/*      */     throws SQLException
/*      */   {
/*  666 */     validateConnection();
/*      */     try
/*      */     {
/*  669 */       this._callableStatement.setBoolean(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  671 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setByte(String parameterName, byte x)
/*      */     throws SQLException
/*      */   {
/*  683 */     validateConnection();
/*      */     try
/*      */     {
/*  686 */       this._callableStatement.setByte(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  688 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setShort(String parameterName, short x)
/*      */     throws SQLException
/*      */   {
/*  700 */     validateConnection();
/*      */     try
/*      */     {
/*  703 */       this._callableStatement.setShort(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  705 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setInt(String parameterName, int x)
/*      */     throws SQLException
/*      */   {
/*  717 */     validateConnection();
/*      */     try
/*      */     {
/*  720 */       this._callableStatement.setInt(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  722 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setLong(String parameterName, long x)
/*      */     throws SQLException
/*      */   {
/*  734 */     validateConnection();
/*      */     try
/*      */     {
/*  737 */       this._callableStatement.setLong(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  739 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setFloat(String parameterName, float x)
/*      */     throws SQLException
/*      */   {
/*  751 */     validateConnection();
/*      */     try
/*      */     {
/*  754 */       this._callableStatement.setFloat(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  756 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setDouble(String parameterName, double x)
/*      */     throws SQLException
/*      */   {
/*  768 */     validateConnection();
/*      */     try
/*      */     {
/*  771 */       this._callableStatement.setDouble(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  773 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setBigDecimal(String parameterName, BigDecimal x)
/*      */     throws SQLException
/*      */   {
/*  785 */     validateConnection();
/*      */     try
/*      */     {
/*  788 */       this._callableStatement.setBigDecimal(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  790 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setString(String parameterName, String x)
/*      */     throws SQLException
/*      */   {
/*  802 */     validateConnection();
/*      */     try
/*      */     {
/*  805 */       this._callableStatement.setString(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  807 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setBytes(String parameterName, byte[] x)
/*      */     throws SQLException
/*      */   {
/*  819 */     validateConnection();
/*      */     try
/*      */     {
/*  822 */       this._callableStatement.setBytes(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  824 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setDate(String parameterName, Date x)
/*      */     throws SQLException
/*      */   {
/*  836 */     validateConnection();
/*      */     try
/*      */     {
/*  839 */       this._callableStatement.setDate(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  841 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setTime(String parameterName, Time x)
/*      */     throws SQLException
/*      */   {
/*  853 */     validateConnection();
/*      */     try
/*      */     {
/*  856 */       this._callableStatement.setTime(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  858 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setTimestamp(String parameterName, Timestamp x)
/*      */     throws SQLException
/*      */   {
/*  870 */     validateConnection();
/*      */     try
/*      */     {
/*  873 */       this._callableStatement.setTimestamp(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  875 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setAsciiStream(String parameterName, InputStream x, int length)
/*      */     throws SQLException
/*      */   {
/*  887 */     validateConnection();
/*      */     try
/*      */     {
/*  890 */       this._callableStatement.setAsciiStream(parameterName, x, length);
/*      */     } catch (SQLException sqlException) {
/*  892 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setBinaryStream(String parameterName, InputStream x, int length)
/*      */     throws SQLException
/*      */   {
/*  904 */     validateConnection();
/*      */     try
/*      */     {
/*  907 */       this._callableStatement.setBinaryStream(parameterName, x, length);
/*      */     } catch (SQLException sqlException) {
/*  909 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setObject(String parameterName, Object x, int targetSqlType, int scale)
/*      */     throws SQLException
/*      */   {
/*  921 */     validateConnection();
/*      */     try
/*      */     {
/*  924 */       this._callableStatement.setObject(parameterName, x, targetSqlType, scale);
/*      */     } catch (SQLException sqlException) {
/*  926 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setObject(String parameterName, Object x, int targetSqlType)
/*      */     throws SQLException
/*      */   {
/*  938 */     validateConnection();
/*      */     try
/*      */     {
/*  941 */       this._callableStatement.setObject(parameterName, x, targetSqlType);
/*      */     } catch (SQLException sqlException) {
/*  943 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setObject(String parameterName, Object x)
/*      */     throws SQLException
/*      */   {
/*  955 */     validateConnection();
/*      */     try
/*      */     {
/*  958 */       this._callableStatement.setObject(parameterName, x);
/*      */     } catch (SQLException sqlException) {
/*  960 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setCharacterStream(String parameterName, Reader x, int length)
/*      */     throws SQLException
/*      */   {
/*  972 */     validateConnection();
/*      */     try
/*      */     {
/*  975 */       this._callableStatement.setCharacterStream(parameterName, x, length);
/*      */     } catch (SQLException sqlException) {
/*  977 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setDate(String parameterName, Date x, Calendar cal)
/*      */     throws SQLException
/*      */   {
/*  989 */     validateConnection();
/*      */     try
/*      */     {
/*  992 */       this._callableStatement.setDate(parameterName, x, cal);
/*      */     } catch (SQLException sqlException) {
/*  994 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setTime(String parameterName, Time x, Calendar cal)
/*      */     throws SQLException
/*      */   {
/* 1006 */     validateConnection();
/*      */     try
/*      */     {
/* 1009 */       this._callableStatement.setTime(parameterName, x, cal);
/*      */     } catch (SQLException sqlException) {
/* 1011 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
/*      */     throws SQLException
/*      */   {
/* 1023 */     validateConnection();
/*      */     try
/*      */     {
/* 1026 */       this._callableStatement.setTimestamp(parameterName, x, cal);
/*      */     } catch (SQLException sqlException) {
/* 1028 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setNull(String parameterName, int sqlType, String typeName)
/*      */     throws SQLException
/*      */   {
/* 1040 */     validateConnection();
/*      */     try
/*      */     {
/* 1043 */       this._callableStatement.setNull(parameterName, sqlType, typeName);
/*      */     } catch (SQLException sqlException) {
/* 1045 */       processSQLException(sqlException);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getString(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1057 */     validateConnection();
/*      */     try
/*      */     {
/* 1060 */       return this._callableStatement.getString(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1062 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1065 */     return null;
/*      */   }
/*      */ 
/*      */   public boolean getBoolean(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1076 */     validateConnection();
/*      */     try
/*      */     {
/* 1079 */       return this._callableStatement.getBoolean(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1081 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1084 */     return false;
/*      */   }
/*      */ 
/*      */   public byte getByte(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1095 */     validateConnection();
/*      */     try
/*      */     {
/* 1098 */       return this._callableStatement.getByte(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1100 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1103 */     return -128;
/*      */   }
/*      */ 
/*      */   public short getShort(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1114 */     validateConnection();
/*      */     try
/*      */     {
/* 1117 */       return this._callableStatement.getShort(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1119 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1122 */     return -32768;
/*      */   }
/*      */ 
/*      */   public int getInt(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1133 */     validateConnection();
/*      */     try
/*      */     {
/* 1136 */       return this._callableStatement.getInt(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1138 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1141 */     return -2147483648;
/*      */   }
/*      */ 
/*      */   public long getLong(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1152 */     validateConnection();
/*      */     try
/*      */     {
/* 1155 */       return this._callableStatement.getLong(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1157 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1160 */     return -9223372036854775808L;
/*      */   }
/*      */ 
/*      */   public float getFloat(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1171 */     validateConnection();
/*      */     try
/*      */     {
/* 1174 */       return this._callableStatement.getFloat(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1176 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1179 */     return 1.4E-45F;
/*      */   }
/*      */ 
/*      */   public double getDouble(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1190 */     validateConnection();
/*      */     try
/*      */     {
/* 1193 */       return this._callableStatement.getDouble(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1195 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1198 */     return 4.9E-324D;
/*      */   }
/*      */ 
/*      */   public byte[] getBytes(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1209 */     validateConnection();
/*      */     try
/*      */     {
/* 1212 */       return this._callableStatement.getBytes(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1214 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1217 */     return null;
/*      */   }
/*      */ 
/*      */   public Date getDate(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1228 */     validateConnection();
/*      */     try
/*      */     {
/* 1231 */       return this._callableStatement.getDate(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1233 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1236 */     return null;
/*      */   }
/*      */ 
/*      */   public Time getTime(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1247 */     validateConnection();
/*      */     try
/*      */     {
/* 1250 */       return this._callableStatement.getTime(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1252 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1255 */     return null;
/*      */   }
/*      */ 
/*      */   public Timestamp getTimestamp(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1266 */     validateConnection();
/*      */     try
/*      */     {
/* 1269 */       return this._callableStatement.getTimestamp(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1271 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1274 */     return null;
/*      */   }
/*      */ 
/*      */   public Object getObject(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1285 */     validateConnection();
/*      */     try
/*      */     {
/* 1288 */       return this._callableStatement.getObject(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1290 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1293 */     return null;
/*      */   }
/*      */ 
/*      */   public BigDecimal getBigDecimal(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1304 */     validateConnection();
/*      */     try
/*      */     {
/* 1307 */       return this._callableStatement.getBigDecimal(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1309 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1312 */     return null;
/*      */   }
/*      */ 
/*      */   public Object getObject(String parameterName, Map map)
/*      */     throws SQLException
/*      */   {
/* 1323 */     validateConnection();
/*      */     try
/*      */     {
/* 1326 */       return this._callableStatement.getObject(parameterName, map);
/*      */     } catch (SQLException sqlException) {
/* 1328 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1331 */     return null;
/*      */   }
/*      */ 
/*      */   public Ref getRef(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1342 */     validateConnection();
/*      */     try
/*      */     {
/* 1345 */       return this._callableStatement.getRef(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1347 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1350 */     return null;
/*      */   }
/*      */ 
/*      */   public Blob getBlob(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1361 */     validateConnection();
/*      */     try
/*      */     {
/* 1364 */       return this._callableStatement.getBlob(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1366 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1369 */     return null;
/*      */   }
/*      */ 
/*      */   public Clob getClob(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1380 */     validateConnection();
/*      */     try
/*      */     {
/* 1383 */       return this._callableStatement.getClob(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1385 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1388 */     return null;
/*      */   }
/*      */ 
/*      */   public Array getArray(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1399 */     validateConnection();
/*      */     try
/*      */     {
/* 1402 */       return this._callableStatement.getArray(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1404 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1407 */     return null;
/*      */   }
/*      */ 
/*      */   public Date getDate(String parameterName, Calendar cal)
/*      */     throws SQLException
/*      */   {
/* 1418 */     validateConnection();
/*      */     try
/*      */     {
/* 1421 */       return this._callableStatement.getDate(parameterName, cal);
/*      */     } catch (SQLException sqlException) {
/* 1423 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1426 */     return null;
/*      */   }
/*      */ 
/*      */   public Time getTime(String parameterName, Calendar cal)
/*      */     throws SQLException
/*      */   {
/* 1437 */     validateConnection();
/*      */     try
/*      */     {
/* 1440 */       return this._callableStatement.getTime(parameterName, cal);
/*      */     } catch (SQLException sqlException) {
/* 1442 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1445 */     return null;
/*      */   }
/*      */ 
/*      */   public Timestamp getTimestamp(String parameterName, Calendar cal)
/*      */     throws SQLException
/*      */   {
/* 1456 */     validateConnection();
/*      */     try
/*      */     {
/* 1459 */       return this._callableStatement.getTimestamp(parameterName, cal);
/*      */     } catch (SQLException sqlException) {
/* 1461 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1464 */     return null;
/*      */   }
/*      */ 
/*      */   public URL getURL(String parameterName)
/*      */     throws SQLException
/*      */   {
/* 1475 */     validateConnection();
/*      */     try
/*      */     {
/* 1478 */       return this._callableStatement.getURL(parameterName);
/*      */     } catch (SQLException sqlException) {
/* 1480 */       processSQLException(sqlException);
/*      */     }
/*      */ 
/* 1483 */     return null;
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbcx.proxy.CallableStatementProxy
 * JD-Core Version:    0.5.3
 */