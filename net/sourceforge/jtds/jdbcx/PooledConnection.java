/*     */ package net.sourceforge.jtds.jdbcx;
/*     */ 
/*     */ import java.sql.Connection;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import javax.sql.ConnectionEvent;
/*     */ import javax.sql.ConnectionEventListener;
/*     */ import net.sourceforge.jtds.jdbc.Messages;
/*     */ import net.sourceforge.jtds.jdbcx.proxy.ConnectionProxy;
/*     */ 
/*     */ public class PooledConnection
/*     */   implements javax.sql.PooledConnection
/*     */ {
/*  34 */   private ArrayList listeners = new ArrayList();
/*     */   protected Connection connection;
/*     */ 
/*     */   public PooledConnection(Connection connection)
/*     */   {
/*  39 */     this.connection = connection;
/*     */   }
/*     */ 
/*     */   public synchronized void addConnectionEventListener(ConnectionEventListener listener)
/*     */   {
/*  53 */     this.listeners = ((ArrayList)this.listeners.clone());
/*     */ 
/*  55 */     this.listeners.add(listener);
/*     */   }
/*     */ 
/*     */   public synchronized void close()
/*     */     throws SQLException
/*     */   {
/*  64 */     this.connection.close();
/*  65 */     this.connection = null;
/*     */   }
/*     */ 
/*     */   public synchronized void fireConnectionEvent(boolean closed, SQLException sqlException)
/*     */   {
/*  77 */     if (this.listeners.size() > 0) {
/*  78 */       ConnectionEvent connectionEvent = new ConnectionEvent(this, sqlException);
/*  79 */       Iterator iterator = this.listeners.iterator();
/*     */ 
/*  81 */       while (iterator.hasNext()) {
/*  82 */         ConnectionEventListener listener = (ConnectionEventListener)iterator.next();
/*     */ 
/*  84 */         if (closed)
/*  85 */           listener.connectionClosed(connectionEvent);
/*     */         else
/*     */           try {
/*  88 */             if ((this.connection == null) || (this.connection.isClosed()))
/*  89 */               listener.connectionErrorOccurred(connectionEvent);
/*     */           }
/*     */           catch (SQLException ex)
/*     */           {
/*     */           }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized Connection getConnection()
/*     */     throws SQLException
/*     */   {
/* 105 */     if (this.connection == null) {
/* 106 */       fireConnectionEvent(false, new SQLException(Messages.get("error.jdbcx.conclosed"), "08003"));
/*     */ 
/* 110 */       return null;
/*     */     }
/*     */ 
/* 115 */     return new ConnectionProxy(this, this.connection);
/*     */   }
/*     */ 
/*     */   public synchronized void removeConnectionEventListener(ConnectionEventListener listener)
/*     */   {
/* 129 */     this.listeners = ((ArrayList)this.listeners.clone());
/*     */ 
/* 131 */     this.listeners.remove(listener);
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbcx.PooledConnection
 * JD-Core Version:    0.5.3
 */