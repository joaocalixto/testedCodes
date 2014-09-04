/*    */ package net.sourceforge.jtds.jdbcx;
/*    */ 
/*    */ import java.sql.Connection;
/*    */ import java.sql.SQLException;
/*    */ import javax.sql.XAConnection;
/*    */ import javax.transaction.xa.XAResource;
/*    */ import net.sourceforge.jtds.jdbc.XASupport;
/*    */ 
/*    */ public class JtdsXAConnection extends PooledConnection
/*    */   implements XAConnection
/*    */ {
/*    */   private final XAResource resource;
/*    */   private final JtdsDataSource dataSource;
/*    */   private final int xaConnectionId;
/*    */ 
/*    */   public JtdsXAConnection(JtdsDataSource dataSource, Connection connection)
/*    */     throws SQLException
/*    */   {
/* 46 */     super(connection);
/* 47 */     this.resource = new JtdsXAResource(this, connection);
/* 48 */     this.dataSource = dataSource;
/* 49 */     this.xaConnectionId = XASupport.xa_open(connection);
/*    */   }
/*    */ 
/*    */   int getXAConnectionID()
/*    */   {
/* 58 */     return this.xaConnectionId;
/*    */   }
/*    */ 
/*    */   public XAResource getXAResource()
/*    */     throws SQLException
/*    */   {
/* 66 */     return this.resource;
/*    */   }
/*    */ 
/*    */   public synchronized void close() throws SQLException {
/*    */     try {
/* 71 */       XASupport.xa_close(this.connection, this.xaConnectionId);
/*    */     }
/*    */     catch (SQLException e) {
/*    */     }
/* 75 */     super.close();
/*    */   }
/*    */ 
/*    */   protected JtdsDataSource getXADataSource() {
/* 79 */     return this.dataSource;
/*    */   }
/*    */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbcx.JtdsXAConnection
 * JD-Core Version:    0.5.3
 */