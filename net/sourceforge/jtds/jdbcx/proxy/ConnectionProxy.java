/*     */ package net.sourceforge.jtds.jdbcx.proxy;
/*     */ 
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Connection;
/*     */ import java.sql.DatabaseMetaData;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.SQLWarning;
/*     */ import java.sql.Savepoint;
/*     */ import java.sql.Statement;
/*     */ import java.util.Map;
/*     */ import net.sourceforge.jtds.jdbc.ConnectionJDBC2;
/*     */ import net.sourceforge.jtds.jdbc.JtdsCallableStatement;
/*     */ import net.sourceforge.jtds.jdbc.JtdsPreparedStatement;
/*     */ import net.sourceforge.jtds.jdbc.JtdsStatement;
/*     */ import net.sourceforge.jtds.jdbc.Messages;
/*     */ import net.sourceforge.jtds.jdbcx.PooledConnection;
/*     */ 
/*     */ public class ConnectionProxy
/*     */   implements Connection
/*     */ {
/*     */   private PooledConnection _pooledConnection;
/*     */   private ConnectionJDBC2 _connection;
/*     */   private boolean _closed;
/*     */ 
/*     */   public ConnectionProxy(PooledConnection pooledConnection, Connection connection)
/*     */   {
/*  45 */     this._pooledConnection = pooledConnection;
/*  46 */     this._connection = ((ConnectionJDBC2)connection);
/*     */   }
/*     */ 
/*     */   public void clearWarnings()
/*     */     throws SQLException
/*     */   {
/*  56 */     validateConnection();
/*     */     try
/*     */     {
/*  59 */       this._connection.clearWarnings();
/*     */     } catch (SQLException sqlException) {
/*  61 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/*  70 */     if (this._closed) {
/*  71 */       return;
/*     */     }
/*     */ 
/*  74 */     this._pooledConnection.fireConnectionEvent(true, null);
/*  75 */     this._closed = true;
/*     */   }
/*     */ 
/*     */   public void commit()
/*     */     throws SQLException
/*     */   {
/*  85 */     validateConnection();
/*     */     try
/*     */     {
/*  88 */       this._connection.commit();
/*     */     } catch (SQLException sqlException) {
/*  90 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Statement createStatement()
/*     */     throws SQLException
/*     */   {
/* 101 */     validateConnection();
/*     */     try
/*     */     {
/* 104 */       return new StatementProxy(this, (JtdsStatement)this._connection.createStatement());
/*     */     } catch (SQLException sqlException) {
/* 106 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 109 */     return null;
/*     */   }
/*     */ 
/*     */   public Statement createStatement(int resultSetType, int resultSetConcurrency)
/*     */     throws SQLException
/*     */   {
/* 119 */     validateConnection();
/*     */     try
/*     */     {
/* 122 */       return new StatementProxy(this, (JtdsStatement)this._connection.createStatement(resultSetType, resultSetConcurrency));
/*     */     } catch (SQLException sqlException) {
/* 124 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 127 */     return null;
/*     */   }
/*     */ 
/*     */   public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
/*     */     throws SQLException
/*     */   {
/* 137 */     validateConnection();
/*     */     try
/*     */     {
/* 140 */       return new StatementProxy(this, (JtdsStatement)this._connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
/*     */     } catch (SQLException sqlException) {
/* 142 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 145 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean getAutoCommit()
/*     */     throws SQLException
/*     */   {
/* 155 */     validateConnection();
/*     */     try
/*     */     {
/* 158 */       return this._connection.getAutoCommit();
/*     */     } catch (SQLException sqlException) {
/* 160 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 163 */     return false;
/*     */   }
/*     */ 
/*     */   public String getCatalog()
/*     */     throws SQLException
/*     */   {
/* 173 */     validateConnection();
/*     */     try
/*     */     {
/* 176 */       return this._connection.getCatalog();
/*     */     } catch (SQLException sqlException) {
/* 178 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 181 */     return null;
/*     */   }
/*     */ 
/*     */   public int getHoldability()
/*     */     throws SQLException
/*     */   {
/* 191 */     validateConnection();
/*     */     try
/*     */     {
/* 194 */       return this._connection.getHoldability();
/*     */     } catch (SQLException sqlException) {
/* 196 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 199 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public int getTransactionIsolation()
/*     */     throws SQLException
/*     */   {
/* 209 */     validateConnection();
/*     */     try
/*     */     {
/* 212 */       return this._connection.getTransactionIsolation();
/*     */     } catch (SQLException sqlException) {
/* 214 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 217 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public Map getTypeMap()
/*     */     throws SQLException
/*     */   {
/* 227 */     validateConnection();
/*     */     try
/*     */     {
/* 230 */       return this._connection.getTypeMap();
/*     */     } catch (SQLException sqlException) {
/* 232 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 235 */     return null;
/*     */   }
/*     */ 
/*     */   public SQLWarning getWarnings()
/*     */     throws SQLException
/*     */   {
/* 245 */     validateConnection();
/*     */     try
/*     */     {
/* 248 */       return this._connection.getWarnings();
/*     */     } catch (SQLException sqlException) {
/* 250 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 253 */     return null;
/*     */   }
/*     */ 
/*     */   public DatabaseMetaData getMetaData()
/*     */     throws SQLException
/*     */   {
/* 263 */     validateConnection();
/*     */     try
/*     */     {
/* 266 */       return this._connection.getMetaData();
/*     */     } catch (SQLException sqlException) {
/* 268 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 271 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean isClosed()
/*     */     throws SQLException
/*     */   {
/* 281 */     if (this._closed) {
/* 282 */       return true;
/*     */     }
/*     */     try
/*     */     {
/* 286 */       return this._connection.isClosed();
/*     */     } catch (SQLException sqlException) {
/* 288 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 291 */     return this._closed;
/*     */   }
/*     */ 
/*     */   public boolean isReadOnly()
/*     */     throws SQLException
/*     */   {
/* 301 */     validateConnection();
/*     */     try
/*     */     {
/* 304 */       return this._connection.isReadOnly();
/*     */     } catch (SQLException sqlException) {
/* 306 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 309 */     return false;
/*     */   }
/*     */ 
/*     */   public String nativeSQL(String sql)
/*     */     throws SQLException
/*     */   {
/* 319 */     validateConnection();
/*     */     try
/*     */     {
/* 322 */       return this._connection.nativeSQL(sql);
/*     */     } catch (SQLException sqlException) {
/* 324 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 327 */     return null;
/*     */   }
/*     */ 
/*     */   public CallableStatement prepareCall(String sql)
/*     */     throws SQLException
/*     */   {
/* 337 */     validateConnection();
/*     */     try
/*     */     {
/* 340 */       return new CallableStatementProxy(this, (JtdsCallableStatement)this._connection.prepareCall(sql));
/*     */     } catch (SQLException sqlException) {
/* 342 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 345 */     return null;
/*     */   }
/*     */ 
/*     */   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
/*     */     throws SQLException
/*     */   {
/* 355 */     validateConnection();
/*     */     try
/*     */     {
/* 358 */       return new CallableStatementProxy(this, (JtdsCallableStatement)this._connection.prepareCall(sql, resultSetType, resultSetConcurrency));
/*     */     } catch (SQLException sqlException) {
/* 360 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 363 */     return null;
/*     */   }
/*     */ 
/*     */   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
/*     */     throws SQLException
/*     */   {
/* 373 */     validateConnection();
/*     */     try
/*     */     {
/* 376 */       return new CallableStatementProxy(this, (JtdsCallableStatement)this._connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
/*     */     } catch (SQLException sqlException) {
/* 378 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 381 */     return null;
/*     */   }
/*     */ 
/*     */   public PreparedStatement prepareStatement(String sql)
/*     */     throws SQLException
/*     */   {
/* 391 */     validateConnection();
/*     */     try
/*     */     {
/* 394 */       return new PreparedStatementProxy(this, (JtdsPreparedStatement)this._connection.prepareStatement(sql));
/*     */     } catch (SQLException sqlException) {
/* 396 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 399 */     return null;
/*     */   }
/*     */ 
/*     */   public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
/*     */     throws SQLException
/*     */   {
/* 409 */     validateConnection();
/*     */     try
/*     */     {
/* 412 */       return new PreparedStatementProxy(this, (JtdsPreparedStatement)this._connection.prepareStatement(sql, autoGeneratedKeys));
/*     */     } catch (SQLException sqlException) {
/* 414 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 417 */     return null;
/*     */   }
/*     */ 
/*     */   public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
/*     */     throws SQLException
/*     */   {
/* 427 */     validateConnection();
/*     */     try
/*     */     {
/* 430 */       return new PreparedStatementProxy(this, (JtdsPreparedStatement)this._connection.prepareStatement(sql, columnIndexes));
/*     */     } catch (SQLException sqlException) {
/* 432 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 435 */     return null;
/*     */   }
/*     */ 
/*     */   public PreparedStatement prepareStatement(String sql, String[] columnNames)
/*     */     throws SQLException
/*     */   {
/* 445 */     validateConnection();
/*     */     try
/*     */     {
/* 448 */       return new PreparedStatementProxy(this, (JtdsPreparedStatement)this._connection.prepareStatement(sql, columnNames));
/*     */     } catch (SQLException sqlException) {
/* 450 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 453 */     return null;
/*     */   }
/*     */ 
/*     */   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
/*     */     throws SQLException
/*     */   {
/* 463 */     validateConnection();
/*     */     try
/*     */     {
/* 466 */       return new PreparedStatementProxy(this, (JtdsPreparedStatement)this._connection.prepareStatement(sql, resultSetType, resultSetConcurrency));
/*     */     } catch (SQLException sqlException) {
/* 468 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 471 */     return null;
/*     */   }
/*     */ 
/*     */   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
/*     */     throws SQLException
/*     */   {
/* 481 */     validateConnection();
/*     */     try
/*     */     {
/* 484 */       return new PreparedStatementProxy(this, (JtdsPreparedStatement)this._connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
/*     */     } catch (SQLException sqlException) {
/* 486 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 489 */     return null;
/*     */   }
/*     */ 
/*     */   public void releaseSavepoint(Savepoint savepoint)
/*     */     throws SQLException
/*     */   {
/* 499 */     validateConnection();
/*     */     try
/*     */     {
/* 502 */       this._connection.releaseSavepoint(savepoint);
/*     */     } catch (SQLException sqlException) {
/* 504 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void rollback()
/*     */     throws SQLException
/*     */   {
/* 515 */     validateConnection();
/*     */     try
/*     */     {
/* 518 */       this._connection.rollback();
/*     */     } catch (SQLException sqlException) {
/* 520 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void rollback(Savepoint savepoint)
/*     */     throws SQLException
/*     */   {
/* 531 */     validateConnection();
/*     */     try
/*     */     {
/* 534 */       this._connection.rollback(savepoint);
/*     */     } catch (SQLException sqlException) {
/* 536 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setAutoCommit(boolean autoCommit)
/*     */     throws SQLException
/*     */   {
/* 547 */     validateConnection();
/*     */     try
/*     */     {
/* 550 */       this._connection.setAutoCommit(autoCommit);
/*     */     } catch (SQLException sqlException) {
/* 552 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setCatalog(String catalog)
/*     */     throws SQLException
/*     */   {
/* 563 */     validateConnection();
/*     */     try
/*     */     {
/* 566 */       this._connection.setCatalog(catalog);
/*     */     } catch (SQLException sqlException) {
/* 568 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setHoldability(int holdability)
/*     */     throws SQLException
/*     */   {
/* 579 */     validateConnection();
/*     */     try
/*     */     {
/* 582 */       this._connection.setHoldability(holdability);
/*     */     } catch (SQLException sqlException) {
/* 584 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setReadOnly(boolean readOnly)
/*     */     throws SQLException
/*     */   {
/* 595 */     validateConnection();
/*     */     try
/*     */     {
/* 598 */       this._connection.setReadOnly(readOnly);
/*     */     } catch (SQLException sqlException) {
/* 600 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Savepoint setSavepoint()
/*     */     throws SQLException
/*     */   {
/* 611 */     validateConnection();
/*     */     try
/*     */     {
/* 614 */       return this._connection.setSavepoint();
/*     */     } catch (SQLException sqlException) {
/* 616 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 619 */     return null;
/*     */   }
/*     */ 
/*     */   public Savepoint setSavepoint(String name)
/*     */     throws SQLException
/*     */   {
/* 629 */     validateConnection();
/*     */     try
/*     */     {
/* 632 */       return this._connection.setSavepoint(name);
/*     */     } catch (SQLException sqlException) {
/* 634 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 637 */     return null;
/*     */   }
/*     */ 
/*     */   public void setTransactionIsolation(int level)
/*     */     throws SQLException
/*     */   {
/* 647 */     validateConnection();
/*     */     try
/*     */     {
/* 650 */       this._connection.setTransactionIsolation(level);
/*     */     } catch (SQLException sqlException) {
/* 652 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setTypeMap(Map map)
/*     */     throws SQLException
/*     */   {
/* 663 */     validateConnection();
/*     */     try
/*     */     {
/* 666 */       this._connection.setTypeMap(map);
/*     */     } catch (SQLException sqlException) {
/* 668 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void validateConnection()
/*     */     throws SQLException
/*     */   {
/* 676 */     if (this._closed)
/* 677 */       throw new SQLException(Messages.get("error.conproxy.noconn"), "HY010");
/*     */   }
/*     */ 
/*     */   void processSQLException(SQLException sqlException)
/*     */     throws SQLException
/*     */   {
/* 685 */     this._pooledConnection.fireConnectionEvent(false, sqlException);
/*     */ 
/* 687 */     throw sqlException;
/*     */   }
/*     */ 
/*     */   protected void finalize()
/*     */   {
/* 694 */     close();
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbcx.proxy.ConnectionProxy
 * JD-Core Version:    0.5.3
 */