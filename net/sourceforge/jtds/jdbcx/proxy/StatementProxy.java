/*     */ package net.sourceforge.jtds.jdbcx.proxy;
/*     */ 
/*     */ import java.sql.Connection;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.SQLWarning;
/*     */ import java.sql.Statement;
/*     */ import net.sourceforge.jtds.jdbc.JtdsStatement;
/*     */ import net.sourceforge.jtds.jdbc.Messages;
/*     */ 
/*     */ public class StatementProxy
/*     */   implements Statement
/*     */ {
/*     */   private ConnectionProxy _connection;
/*     */   private JtdsStatement _statement;
/*     */ 
/*     */   StatementProxy(ConnectionProxy connection, JtdsStatement statement)
/*     */   {
/*  37 */     this._connection = connection;
/*  38 */     this._statement = statement;
/*     */   }
/*     */ 
/*     */   public ResultSet executeQuery(String sql)
/*     */     throws SQLException
/*     */   {
/*  48 */     validateConnection();
/*     */     try
/*     */     {
/*  51 */       return this._statement.executeQuery(sql);
/*     */     } catch (SQLException sqlException) {
/*  53 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/*  56 */     return null;
/*     */   }
/*     */ 
/*     */   public int executeUpdate(String sql)
/*     */     throws SQLException
/*     */   {
/*  66 */     validateConnection();
/*     */     try
/*     */     {
/*  69 */       return this._statement.executeUpdate(sql);
/*     */     } catch (SQLException sqlException) {
/*  71 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/*  74 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws SQLException
/*     */   {
/*  84 */     validateConnection();
/*     */     try
/*     */     {
/*  87 */       this._statement.close();
/*     */     } catch (SQLException sqlException) {
/*  89 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getMaxFieldSize()
/*     */     throws SQLException
/*     */   {
/* 100 */     validateConnection();
/*     */     try
/*     */     {
/* 103 */       return this._statement.getMaxFieldSize();
/*     */     } catch (SQLException sqlException) {
/* 105 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 108 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public void setMaxFieldSize(int max)
/*     */     throws SQLException
/*     */   {
/* 118 */     validateConnection();
/*     */     try
/*     */     {
/* 121 */       this._statement.setMaxFieldSize(max);
/*     */     } catch (SQLException sqlException) {
/* 123 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getMaxRows()
/*     */     throws SQLException
/*     */   {
/* 134 */     validateConnection();
/*     */     try
/*     */     {
/* 137 */       return this._statement.getMaxRows();
/*     */     } catch (SQLException sqlException) {
/* 139 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 142 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public void setMaxRows(int max)
/*     */     throws SQLException
/*     */   {
/* 152 */     validateConnection();
/*     */     try
/*     */     {
/* 155 */       this._statement.setMaxRows(max);
/*     */     } catch (SQLException sqlException) {
/* 157 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setEscapeProcessing(boolean enable)
/*     */     throws SQLException
/*     */   {
/* 168 */     validateConnection();
/*     */     try
/*     */     {
/* 171 */       this._statement.setEscapeProcessing(enable);
/*     */     } catch (SQLException sqlException) {
/* 173 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getQueryTimeout()
/*     */     throws SQLException
/*     */   {
/* 184 */     validateConnection();
/*     */     try
/*     */     {
/* 187 */       return this._statement.getQueryTimeout();
/*     */     } catch (SQLException sqlException) {
/* 189 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 192 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public void setQueryTimeout(int seconds)
/*     */     throws SQLException
/*     */   {
/* 202 */     validateConnection();
/*     */     try
/*     */     {
/* 205 */       this._statement.setQueryTimeout(seconds);
/*     */     } catch (SQLException sqlException) {
/* 207 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cancel()
/*     */     throws SQLException
/*     */   {
/* 218 */     validateConnection();
/*     */     try
/*     */     {
/* 221 */       this._statement.cancel();
/*     */     } catch (SQLException sqlException) {
/* 223 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public SQLWarning getWarnings()
/*     */     throws SQLException
/*     */   {
/* 234 */     validateConnection();
/*     */     try
/*     */     {
/* 237 */       return this._statement.getWarnings();
/*     */     } catch (SQLException sqlException) {
/* 239 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 242 */     return null;
/*     */   }
/*     */ 
/*     */   public void clearWarnings()
/*     */     throws SQLException
/*     */   {
/* 252 */     validateConnection();
/*     */     try
/*     */     {
/* 255 */       this._statement.clearWarnings();
/*     */     } catch (SQLException sqlException) {
/* 257 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setCursorName(String name)
/*     */     throws SQLException
/*     */   {
/* 268 */     validateConnection();
/*     */     try
/*     */     {
/* 271 */       this._statement.setCursorName(name);
/*     */     } catch (SQLException sqlException) {
/* 273 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean execute(String sql)
/*     */     throws SQLException
/*     */   {
/* 284 */     validateConnection();
/*     */     try
/*     */     {
/* 287 */       return this._statement.execute(sql);
/*     */     } catch (SQLException sqlException) {
/* 289 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 292 */     return false;
/*     */   }
/*     */ 
/*     */   public ResultSet getResultSet()
/*     */     throws SQLException
/*     */   {
/* 302 */     validateConnection();
/*     */     try
/*     */     {
/* 305 */       return this._statement.getResultSet();
/*     */     } catch (SQLException sqlException) {
/* 307 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 310 */     return null;
/*     */   }
/*     */ 
/*     */   public int getUpdateCount()
/*     */     throws SQLException
/*     */   {
/* 320 */     validateConnection();
/*     */     try
/*     */     {
/* 323 */       return this._statement.getUpdateCount();
/*     */     } catch (SQLException sqlException) {
/* 325 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 328 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public boolean getMoreResults()
/*     */     throws SQLException
/*     */   {
/* 338 */     validateConnection();
/*     */     try
/*     */     {
/* 341 */       return this._statement.getMoreResults();
/*     */     } catch (SQLException sqlException) {
/* 343 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 346 */     return false;
/*     */   }
/*     */ 
/*     */   public void setFetchDirection(int direction)
/*     */     throws SQLException
/*     */   {
/* 356 */     validateConnection();
/*     */     try
/*     */     {
/* 359 */       this._statement.setFetchDirection(direction);
/*     */     } catch (SQLException sqlException) {
/* 361 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getFetchDirection()
/*     */     throws SQLException
/*     */   {
/* 372 */     validateConnection();
/*     */     try
/*     */     {
/* 375 */       return this._statement.getFetchDirection();
/*     */     } catch (SQLException sqlException) {
/* 377 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 380 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public void setFetchSize(int rows)
/*     */     throws SQLException
/*     */   {
/* 390 */     validateConnection();
/*     */     try
/*     */     {
/* 393 */       this._statement.setFetchSize(rows);
/*     */     } catch (SQLException sqlException) {
/* 395 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getFetchSize()
/*     */     throws SQLException
/*     */   {
/* 406 */     validateConnection();
/*     */     try
/*     */     {
/* 409 */       return this._statement.getFetchSize();
/*     */     } catch (SQLException sqlException) {
/* 411 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 414 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public int getResultSetConcurrency()
/*     */     throws SQLException
/*     */   {
/* 424 */     validateConnection();
/*     */     try
/*     */     {
/* 427 */       return this._statement.getResultSetConcurrency();
/*     */     } catch (SQLException sqlException) {
/* 429 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 432 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public int getResultSetType()
/*     */     throws SQLException
/*     */   {
/* 442 */     validateConnection();
/*     */     try
/*     */     {
/* 445 */       return this._statement.getResultSetType();
/*     */     } catch (SQLException sqlException) {
/* 447 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 450 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public void addBatch(String sql)
/*     */     throws SQLException
/*     */   {
/* 460 */     validateConnection();
/*     */     try
/*     */     {
/* 463 */       this._statement.addBatch(sql);
/*     */     } catch (SQLException sqlException) {
/* 465 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clearBatch()
/*     */     throws SQLException
/*     */   {
/* 476 */     validateConnection();
/*     */     try
/*     */     {
/* 479 */       this._statement.clearBatch();
/*     */     } catch (SQLException sqlException) {
/* 481 */       processSQLException(sqlException);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int[] executeBatch()
/*     */     throws SQLException
/*     */   {
/* 492 */     validateConnection();
/*     */     try
/*     */     {
/* 495 */       return this._statement.executeBatch();
/*     */     } catch (SQLException sqlException) {
/* 497 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 500 */     return null;
/*     */   }
/*     */ 
/*     */   public Connection getConnection()
/*     */     throws SQLException
/*     */   {
/* 510 */     validateConnection();
/*     */     try
/*     */     {
/* 513 */       return this._statement.getConnection();
/*     */     } catch (SQLException sqlException) {
/* 515 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 518 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean getMoreResults(int current)
/*     */     throws SQLException
/*     */   {
/* 528 */     validateConnection();
/*     */     try
/*     */     {
/* 531 */       return this._statement.getMoreResults(current);
/*     */     } catch (SQLException sqlException) {
/* 533 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 536 */     return false;
/*     */   }
/*     */ 
/*     */   public ResultSet getGeneratedKeys()
/*     */     throws SQLException
/*     */   {
/* 546 */     validateConnection();
/*     */     try
/*     */     {
/* 549 */       return this._statement.getGeneratedKeys();
/*     */     } catch (SQLException sqlException) {
/* 551 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 554 */     return null;
/*     */   }
/*     */ 
/*     */   public int executeUpdate(String sql, int autoGeneratedKeys)
/*     */     throws SQLException
/*     */   {
/* 564 */     validateConnection();
/*     */     try
/*     */     {
/* 567 */       return this._statement.executeUpdate(sql, autoGeneratedKeys);
/*     */     } catch (SQLException sqlException) {
/* 569 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 572 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public int executeUpdate(String sql, int[] columnIndexes)
/*     */     throws SQLException
/*     */   {
/* 582 */     validateConnection();
/*     */     try
/*     */     {
/* 585 */       return this._statement.executeUpdate(sql, columnIndexes);
/*     */     } catch (SQLException sqlException) {
/* 587 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 590 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public int executeUpdate(String sql, String[] columnNames)
/*     */     throws SQLException
/*     */   {
/* 600 */     validateConnection();
/*     */     try
/*     */     {
/* 603 */       return this._statement.executeUpdate(sql, columnNames);
/*     */     } catch (SQLException sqlException) {
/* 605 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 608 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   public boolean execute(String sql, int autoGeneratedKeys)
/*     */     throws SQLException
/*     */   {
/* 618 */     validateConnection();
/*     */     try
/*     */     {
/* 621 */       return this._statement.execute(sql, autoGeneratedKeys);
/*     */     } catch (SQLException sqlException) {
/* 623 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 626 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean execute(String sql, int[] columnIndexes)
/*     */     throws SQLException
/*     */   {
/* 636 */     validateConnection();
/*     */     try
/*     */     {
/* 639 */       return this._statement.execute(sql, columnIndexes);
/*     */     } catch (SQLException sqlException) {
/* 641 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 644 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean execute(String sql, String[] columnNames)
/*     */     throws SQLException
/*     */   {
/* 654 */     validateConnection();
/*     */     try
/*     */     {
/* 657 */       return this._statement.execute(sql, columnNames);
/*     */     } catch (SQLException sqlException) {
/* 659 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 662 */     return false;
/*     */   }
/*     */ 
/*     */   public int getResultSetHoldability()
/*     */     throws SQLException
/*     */   {
/* 672 */     validateConnection();
/*     */     try
/*     */     {
/* 675 */       return this._statement.getResultSetHoldability();
/*     */     } catch (SQLException sqlException) {
/* 677 */       processSQLException(sqlException);
/*     */     }
/*     */ 
/* 680 */     return -2147483648;
/*     */   }
/*     */ 
/*     */   protected void validateConnection()
/*     */     throws SQLException
/*     */   {
/* 687 */     if (this._connection.isClosed())
/* 688 */       throw new SQLException(Messages.get("error.conproxy.noconn"), "HY010");
/*     */   }
/*     */ 
/*     */   protected void processSQLException(SQLException sqlException)
/*     */     throws SQLException
/*     */   {
/* 696 */     this._connection.processSQLException(sqlException);
/*     */ 
/* 698 */     throw sqlException;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbcx.proxy.StatementProxy
 * JD-Core Version:    0.5.3
 */