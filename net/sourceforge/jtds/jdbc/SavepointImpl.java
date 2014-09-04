/*    */ package net.sourceforge.jtds.jdbc;
/*    */ 
/*    */ import java.sql.SQLException;
/*    */ import java.sql.Savepoint;
/*    */ 
/*    */ class SavepointImpl
/*    */   implements Savepoint
/*    */ {
/*    */   private final int id;
/*    */   private final String name;
/*    */ 
/*    */   SavepointImpl(int id)
/*    */   {
/* 39 */     this(id, null);
/*    */   }
/*    */ 
/*    */   SavepointImpl(int id, String name)
/*    */   {
/* 49 */     this.id = id;
/* 50 */     this.name = name;
/*    */   }
/*    */ 
/*    */   public int getSavepointId() throws SQLException {
/* 54 */     if (this.name != null) {
/* 55 */       throw new SQLException(Messages.get("error.savepoint.named"), "HY024");
/*    */     }
/*    */ 
/* 58 */     return this.id;
/*    */   }
/*    */ 
/*    */   public String getSavepointName() throws SQLException {
/* 62 */     if (this.name == null) {
/* 63 */       throw new SQLException(Messages.get("error.savepoint.unnamed"), "HY024");
/*    */     }
/*    */ 
/* 66 */     return this.name;
/*    */   }
/*    */ 
/*    */   int getId()
/*    */   {
/* 76 */     return this.id;
/*    */   }
/*    */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.SavepointImpl
 * JD-Core Version:    0.5.3
 */