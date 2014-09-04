/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.sql.ParameterMetaData;
/*     */ import java.sql.SQLException;
/*     */ 
/*     */ public class ParameterMetaDataImpl
/*     */   implements ParameterMetaData
/*     */ {
/*     */   private final ParamInfo[] parameterList;
/*     */   private final int maxPrecision;
/*     */   private final boolean useLOBs;
/*     */ 
/*     */   public ParameterMetaDataImpl(ParamInfo[] parameterList, ConnectionJDBC2 connection)
/*     */   {
/*  40 */     if (parameterList == null) {
/*  41 */       parameterList = new ParamInfo[0];
/*     */     }
/*     */ 
/*  44 */     this.parameterList = parameterList;
/*  45 */     this.maxPrecision = connection.getMaxPrecision();
/*  46 */     this.useLOBs = connection.getUseLOBs();
/*     */   }
/*     */ 
/*     */   public int getParameterCount() throws SQLException {
/*  50 */     return this.parameterList.length;
/*     */   }
/*     */ 
/*     */   public int isNullable(int param) throws SQLException {
/*  54 */     return 2;
/*     */   }
/*     */ 
/*     */   public int getParameterType(int param) throws SQLException {
/*  58 */     if (this.useLOBs) {
/*  59 */       return getParameter(param).jdbcType;
/*     */     }
/*  61 */     return Support.convertLOBType(getParameter(param).jdbcType);
/*     */   }
/*     */ 
/*     */   public int getScale(int param) throws SQLException
/*     */   {
/*  66 */     ParamInfo pi = getParameter(param);
/*     */ 
/*  68 */     return ((pi.scale >= 0) ? pi.scale : 0);
/*     */   }
/*     */ 
/*     */   public boolean isSigned(int param) throws SQLException {
/*  72 */     ParamInfo pi = getParameter(param);
/*     */ 
/*  74 */     switch (pi.jdbcType) { case -5:
/*     */     case 2:
/*     */     case 3:
/*     */     case 4:
/*     */     case 5:
/*     */     case 6:
/*     */     case 7:
/*     */     case 8:
/*  83 */       return true;
/*     */     case -4:
/*     */     case -3:
/*     */     case -2:
/*     */     case -1:
/*     */     case 0:
/*     */     case 1: } return false;
/*     */   }
/*     */ 
/*     */   public int getPrecision(int param) throws SQLException
/*     */   {
/*  90 */     ParamInfo pi = getParameter(param);
/*     */ 
/*  92 */     return ((pi.precision >= 0) ? pi.precision : this.maxPrecision);
/*     */   }
/*     */ 
/*     */   public String getParameterTypeName(int param) throws SQLException {
/*  96 */     return getParameter(param).sqlType;
/*     */   }
/*     */ 
/*     */   public String getParameterClassName(int param) throws SQLException {
/* 100 */     return Support.getClassName(getParameterType(param));
/*     */   }
/*     */ 
/*     */   public int getParameterMode(int param) throws SQLException {
/* 104 */     ParamInfo pi = getParameter(param);
/*     */ 
/* 106 */     if (pi.isOutput) {
/* 107 */       return ((pi.isSet) ? 2 : 4);
/*     */     }
/*     */ 
/* 110 */     return ((pi.isSet) ? 1 : 0);
/*     */   }
/*     */ 
/*     */   private ParamInfo getParameter(int param) throws SQLException {
/* 114 */     if ((param < 1) || (param > this.parameterList.length)) {
/* 115 */       throw new SQLException(Messages.get("error.prepare.paramindex", Integer.toString(param)), "07009");
/*     */     }
/*     */ 
/* 120 */     return this.parameterList[(param - 1)];
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.ParameterMetaDataImpl
 * JD-Core Version:    0.5.3
 */