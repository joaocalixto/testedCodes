/*    */ package net.sourceforge.jtds.jdbc.cache;
/*    */ 
/*    */ import net.sourceforge.jtds.jdbc.ConnectionJDBC2;
/*    */ 
/*    */ public class SQLCacheKey
/*    */ {
/*    */   private final String sql;
/*    */   private final int serverType;
/*    */   private final int majorVersion;
/*    */   private final int minorVersion;
/*    */   private final int hashCode;
/*    */ 
/*    */   public SQLCacheKey(String sql, ConnectionJDBC2 connection)
/*    */   {
/* 38 */     this.sql = sql;
/* 39 */     this.serverType = connection.getServerType();
/* 40 */     this.majorVersion = connection.getDatabaseMajorVersion();
/* 41 */     this.minorVersion = connection.getDatabaseMinorVersion();
/*    */ 
/* 43 */     this.hashCode = (sql.hashCode() ^ (this.serverType << 24 | this.majorVersion << 16 | this.minorVersion));
/*    */   }
/*    */ 
/*    */   public int hashCode()
/*    */   {
/* 48 */     return this.hashCode;
/*    */   }
/*    */ 
/*    */   public boolean equals(Object object) {
/*    */     try {
/* 53 */       SQLCacheKey key = (SQLCacheKey)object;
/*    */ 
/* 55 */       return ((this.hashCode == key.hashCode) && (this.majorVersion == key.majorVersion) && (this.minorVersion == key.minorVersion) && (this.serverType == key.serverType) && (this.sql.equals(key.sql)));
/*    */     }
/*    */     catch (ClassCastException e)
/*    */     {
/* 61 */       return false; } catch (NullPointerException e) {
/*    */     }
/* 63 */     return false;
/*    */   }
/*    */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.cache.SQLCacheKey
 * JD-Core Version:    0.5.3
 */