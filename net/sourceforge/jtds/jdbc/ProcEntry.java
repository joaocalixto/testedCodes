/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ public class ProcEntry
/*     */ {
/*     */   public static final int PROCEDURE = 1;
/*     */   public static final int PREPARE = 2;
/*     */   public static final int CURSOR = 3;
/*     */   public static final int PREP_FAILED = 4;
/*     */   private String name;
/*     */   private ColInfo[] colMetaData;
/*     */   private ParamInfo[] paramMetaData;
/*     */   private int type;
/*     */   private int refCount;
/*     */ 
/*     */   public final String toString()
/*     */   {
/*  52 */     return this.name;
/*     */   }
/*     */ 
/*     */   public void setName(String name)
/*     */   {
/*  61 */     this.name = name;
/*     */   }
/*     */ 
/*     */   public void setHandle(int handle)
/*     */   {
/*  70 */     this.name = Integer.toString(handle);
/*     */   }
/*     */ 
/*     */   public ColInfo[] getColMetaData()
/*     */   {
/*  79 */     return this.colMetaData;
/*     */   }
/*     */ 
/*     */   public void setColMetaData(ColInfo[] colMetaData)
/*     */   {
/*  88 */     this.colMetaData = colMetaData;
/*     */   }
/*     */ 
/*     */   public ParamInfo[] getParamMetaData()
/*     */   {
/*  97 */     return this.paramMetaData;
/*     */   }
/*     */ 
/*     */   public void setParamMetaData(ParamInfo[] paramMetaData)
/*     */   {
/* 106 */     this.paramMetaData = paramMetaData;
/*     */   }
/*     */ 
/*     */   public void setType(int type)
/*     */   {
/* 115 */     this.type = type;
/*     */   }
/*     */ 
/*     */   public int getType()
/*     */   {
/* 124 */     return this.type;
/*     */   }
/*     */ 
/*     */   public void appendDropSQL(StringBuffer sql)
/*     */   {
/* 131 */     switch (this.type)
/*     */     {
/*     */     case 1:
/* 133 */       sql.append("DROP PROC ").append(this.name).append('\n');
/* 134 */       break;
/*     */     case 2:
/* 136 */       sql.append("EXEC sp_unprepare ").append(this.name).append('\n');
/* 137 */       break;
/*     */     case 3:
/* 139 */       sql.append("EXEC sp_cursorunprepare ").append(this.name).append('\n');
/* 140 */       break;
/*     */     case 4:
/* 142 */       break;
/*     */     default:
/* 144 */       throw new IllegalStateException("Invalid cached statement type " + this.type);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addRef()
/*     */   {
/* 152 */     this.refCount += 1;
/*     */   }
/*     */ 
/*     */   public void release()
/*     */   {
/* 159 */     if (this.refCount > 0)
/* 160 */       this.refCount -= 1;
/*     */   }
/*     */ 
/*     */   public int getRefCount()
/*     */   {
/* 170 */     return this.refCount;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.ProcEntry
 * JD-Core Version:    0.5.3
 */