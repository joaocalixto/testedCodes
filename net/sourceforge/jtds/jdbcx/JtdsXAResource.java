/*     */ package net.sourceforge.jtds.jdbcx;
/*     */ 
/*     */ import java.sql.Connection;
/*     */ import javax.transaction.xa.XAException;
/*     */ import javax.transaction.xa.XAResource;
/*     */ import javax.transaction.xa.Xid;
/*     */ import net.sourceforge.jtds.jdbc.ConnectionJDBC2;
/*     */ import net.sourceforge.jtds.jdbc.XASupport;
/*     */ import net.sourceforge.jtds.util.Logger;
/*     */ 
/*     */ public class JtdsXAResource
/*     */   implements XAResource
/*     */ {
/*     */   private final Connection connection;
/*     */   private final JtdsXAConnection xaConnection;
/*     */   private final String rmHost;
/*     */ 
/*     */   public JtdsXAResource(JtdsXAConnection xaConnection, Connection connection)
/*     */   {
/*  40 */     this.xaConnection = xaConnection;
/*  41 */     this.connection = connection;
/*  42 */     this.rmHost = ((ConnectionJDBC2)connection).getRmHost();
/*  43 */     Logger.println("JtdsXAResource created");
/*     */   }
/*     */ 
/*     */   protected JtdsXAConnection getResourceManager() {
/*  47 */     return this.xaConnection;
/*     */   }
/*     */ 
/*     */   protected String getRmHost() {
/*  51 */     return this.rmHost;
/*     */   }
/*     */ 
/*     */   public int getTransactionTimeout()
/*     */     throws XAException
/*     */   {
/*  59 */     Logger.println("XAResource.getTransactionTimeout()");
/*  60 */     return 0;
/*     */   }
/*     */ 
/*     */   public boolean setTransactionTimeout(int arg0) throws XAException {
/*  64 */     Logger.println("XAResource.setTransactionTimeout(" + arg0 + ')');
/*  65 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean isSameRM(XAResource xares) throws XAException {
/*  69 */     Logger.println("XAResource.isSameRM(" + xares.toString() + ')');
/*     */ 
/*  72 */     return ((xares instanceof JtdsXAResource) && 
/*  71 */       (((JtdsXAResource)xares).getRmHost().equals(this.rmHost)));
/*     */   }
/*     */ 
/*     */   public Xid[] recover(int flags)
/*     */     throws XAException
/*     */   {
/*  79 */     Logger.println("XAResource.recover(" + flags + ')');
/*  80 */     return XASupport.xa_recover(this.connection, this.xaConnection.getXAConnectionID(), flags);
/*     */   }
/*     */ 
/*     */   public int prepare(Xid xid) throws XAException {
/*  84 */     Logger.println("XAResource.prepare(" + xid.toString() + ')');
/*  85 */     return XASupport.xa_prepare(this.connection, this.xaConnection.getXAConnectionID(), xid);
/*     */   }
/*     */ 
/*     */   public void forget(Xid xid) throws XAException {
/*  89 */     Logger.println("XAResource.forget(" + xid + ')');
/*  90 */     XASupport.xa_forget(this.connection, this.xaConnection.getXAConnectionID(), xid);
/*     */   }
/*     */ 
/*     */   public void rollback(Xid xid) throws XAException {
/*  94 */     Logger.println("XAResource.rollback(" + xid.toString() + ')');
/*  95 */     XASupport.xa_rollback(this.connection, this.xaConnection.getXAConnectionID(), xid);
/*     */   }
/*     */ 
/*     */   public void end(Xid xid, int flags) throws XAException {
/*  99 */     Logger.println("XAResource.end(" + xid.toString() + ')');
/* 100 */     XASupport.xa_end(this.connection, this.xaConnection.getXAConnectionID(), xid, flags);
/*     */   }
/*     */ 
/*     */   public void start(Xid xid, int flags) throws XAException {
/* 104 */     Logger.println("XAResource.start(" + xid.toString() + ',' + flags + ')');
/* 105 */     XASupport.xa_start(this.connection, this.xaConnection.getXAConnectionID(), xid, flags);
/*     */   }
/*     */ 
/*     */   public void commit(Xid xid, boolean commit) throws XAException {
/* 109 */     Logger.println("XAResource.commit(" + xid.toString() + ',' + commit + ')');
/* 110 */     XASupport.xa_commit(this.connection, this.xaConnection.getXAConnectionID(), xid, commit);
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbcx.JtdsXAResource
 * JD-Core Version:    0.5.3
 */