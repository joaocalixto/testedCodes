/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Savepoint;
/*     */ import java.sql.Statement;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ConnectionJDBC3 extends ConnectionJDBC2
/*     */ {
/*     */   private ArrayList savepoints;
/*     */   private Map savepointProcInTran;
/*     */   private int savepointId;
/*     */ 
/*     */   ConnectionJDBC3(String url, Properties props)
/*     */     throws SQLException
/*     */   {
/*  50 */     super(url, props);
/*     */   }
/*     */ 
/*     */   private void setSavepoint(SavepointImpl savepoint)
/*     */     throws SQLException
/*     */   {
/*  60 */     Statement statement = null;
/*     */     try
/*     */     {
/*  63 */       statement = createStatement();
/*  64 */       statement.execute("IF @@TRANCOUNT=0 BEGIN SET IMPLICIT_TRANSACTIONS OFF; BEGIN TRAN; SET IMPLICIT_TRANSACTIONS ON; END SAVE TRAN jtds" + savepoint.getId());
/*     */     }
/*     */     finally
/*     */     {
/*  69 */       if (statement != null) {
/*  70 */         statement.close();
/*     */       }
/*     */     }
/*     */ 
/*  74 */     synchronized (this) {
/*  75 */       if (this.savepoints == null) {
/*  76 */         this.savepoints = new ArrayList();
/*     */       }
/*     */ 
/*  79 */       this.savepoints.add(savepoint);
/*     */     }
/*     */   }
/*     */ 
/*     */   synchronized void clearSavepoints()
/*     */   {
/*  88 */     if (this.savepoints != null) {
/*  89 */       this.savepoints.clear();
/*     */     }
/*     */ 
/*  92 */     if (this.savepointProcInTran != null) {
/*  93 */       this.savepointProcInTran.clear();
/*     */     }
/*     */ 
/*  96 */     this.savepointId = 0;
/*     */   }
/*     */ 
/*     */   public synchronized void releaseSavepoint(Savepoint savepoint)
/*     */     throws SQLException
/*     */   {
/* 104 */     checkOpen();
/*     */ 
/* 106 */     if (this.savepoints == null) {
/* 107 */       throw new SQLException(Messages.get("error.connection.badsavep"), "25000");
/*     */     }
/*     */ 
/* 111 */     int index = this.savepoints.indexOf(savepoint);
/*     */ 
/* 113 */     if (index == -1) {
/* 114 */       throw new SQLException(Messages.get("error.connection.badsavep"), "25000");
/*     */     }
/*     */ 
/* 118 */     Object tmpSavepoint = this.savepoints.remove(index);
/*     */ 
/* 120 */     if (this.savepointProcInTran != null) {
/* 121 */       if (index != 0)
/*     */       {
/* 126 */         List keys = (List)this.savepointProcInTran.get(savepoint);
/*     */ 
/* 128 */         if (keys != null) {
/* 129 */           Savepoint wrapping = (Savepoint)this.savepoints.get(index - 1);
/* 130 */           List wrappingKeys = (List)this.savepointProcInTran.get(wrapping);
/*     */ 
/* 132 */           if (wrappingKeys == null) {
/* 133 */             wrappingKeys = new ArrayList();
/*     */           }
/* 135 */           wrappingKeys.addAll(keys);
/* 136 */           this.savepointProcInTran.put(wrapping, wrappingKeys);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 142 */       this.savepointProcInTran.remove(tmpSavepoint);
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void rollback(Savepoint savepoint) throws SQLException {
/* 147 */     checkOpen();
/* 148 */     checkLocal("rollback");
/*     */ 
/* 150 */     if (this.savepoints == null) {
/* 151 */       throw new SQLException(Messages.get("error.connection.badsavep"), "25000");
/*     */     }
/*     */ 
/* 155 */     int index = this.savepoints.indexOf(savepoint);
/*     */ 
/* 157 */     if (index == -1) {
/* 158 */       throw new SQLException(Messages.get("error.connection.badsavep"), "25000");
/*     */     }
/* 160 */     if (getAutoCommit()) {
/* 161 */       throw new SQLException(Messages.get("error.connection.savenorollback"), "25000");
/*     */     }
/*     */ 
/* 165 */     Statement statement = null;
/*     */     try
/*     */     {
/* 168 */       statement = createStatement();
/* 169 */       statement.execute("ROLLBACK TRAN jtds" + ((SavepointImpl)savepoint).getId());
/*     */     } finally {
/* 171 */       if (statement != null) {
/* 172 */         statement.close();
/*     */       }
/*     */     }
/*     */ 
/* 176 */     int size = this.savepoints.size();
/*     */     Iterator iterator;
/* 178 */     for (int i = size - 1; i >= index; --i) {
/* 179 */       Object tmpSavepoint = this.savepoints.remove(i);
/*     */ 
/* 181 */       if (this.savepointProcInTran == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 185 */       List keys = (List)this.savepointProcInTran.get(tmpSavepoint);
/*     */ 
/* 187 */       if (keys == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 191 */       for (iterator = keys.iterator(); iterator.hasNext(); ) {
/* 192 */         String key = (String)iterator.next();
/*     */ 
/* 194 */         removeCachedProcedure(key);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 199 */     setSavepoint((SavepointImpl)savepoint);
/*     */   }
/*     */ 
/*     */   public synchronized Savepoint setSavepoint() throws SQLException {
/* 203 */     checkOpen();
/* 204 */     checkLocal("setSavepoint");
/*     */ 
/* 206 */     if (getAutoCommit()) {
/* 207 */       throw new SQLException(Messages.get("error.connection.savenoset"), "25000");
/*     */     }
/*     */ 
/* 211 */     SavepointImpl savepoint = new SavepointImpl(getNextSavepointId());
/*     */ 
/* 213 */     setSavepoint(savepoint);
/*     */ 
/* 215 */     return savepoint;
/*     */   }
/*     */ 
/*     */   public synchronized Savepoint setSavepoint(String name) throws SQLException {
/* 219 */     checkOpen();
/* 220 */     checkLocal("setSavepoint");
/*     */ 
/* 222 */     if (getAutoCommit()) {
/* 223 */       throw new SQLException(Messages.get("error.connection.savenoset"), "25000");
/*     */     }
/* 225 */     if (name == null) {
/* 226 */       throw new SQLException(Messages.get("error.connection.savenullname", "savepoint"), "25000");
/*     */     }
/*     */ 
/* 231 */     SavepointImpl savepoint = new SavepointImpl(getNextSavepointId(), name);
/*     */ 
/* 233 */     setSavepoint(savepoint);
/*     */ 
/* 235 */     return savepoint;
/*     */   }
/*     */ 
/*     */   private int getNextSavepointId()
/*     */   {
/* 244 */     return (++this.savepointId);
/*     */   }
/*     */ 
/*     */   void addCachedProcedure(String key, ProcEntry proc)
/*     */   {
/* 254 */     super.addCachedProcedure(key, proc);
/* 255 */     if ((getServerType() != 1) || (proc.getType() != 1)) {
/*     */       return;
/*     */     }
/* 258 */     addCachedProcedure(key);
/*     */   }
/*     */ 
/*     */   synchronized void addCachedProcedure(String key)
/*     */   {
/* 268 */     if ((this.savepoints == null) || (this.savepoints.size() == 0)) {
/* 269 */       return;
/*     */     }
/*     */ 
/* 272 */     if (this.savepointProcInTran == null) {
/* 273 */       this.savepointProcInTran = new HashMap();
/*     */     }
/*     */ 
/* 277 */     Object savepoint = this.savepoints.get(this.savepoints.size() - 1);
/*     */ 
/* 279 */     List keys = (List)this.savepointProcInTran.get(savepoint);
/*     */ 
/* 281 */     if (keys == null) {
/* 282 */       keys = new ArrayList();
/*     */     }
/*     */ 
/* 285 */     keys.add(key);
/*     */ 
/* 287 */     this.savepointProcInTran.put(savepoint, keys);
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.ConnectionJDBC3
 * JD-Core Version:    0.5.3
 */