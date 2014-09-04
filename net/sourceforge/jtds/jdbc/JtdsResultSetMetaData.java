/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.sql.ResultSetMetaData;
/*     */ import java.sql.SQLException;
/*     */ 
/*     */ public class JtdsResultSetMetaData
/*     */   implements ResultSetMetaData
/*     */ {
/*     */   private final ColInfo[] columns;
/*     */   private final int columnCount;
/*     */   private final boolean useLOBs;
/*     */ 
/*     */   JtdsResultSetMetaData(ColInfo[] columns, int columnCount, boolean useLOBs)
/*     */   {
/*  48 */     this.columns = columns;
/*  49 */     this.columnCount = columnCount;
/*  50 */     this.useLOBs = useLOBs;
/*     */   }
/*     */ 
/*     */   ColInfo getColumn(int column)
/*     */     throws SQLException
/*     */   {
/*  61 */     if ((column < 1) || (column > this.columnCount)) {
/*  62 */       throw new SQLException(Messages.get("error.resultset.colindex", Integer.toString(column)), "07009");
/*     */     }
/*     */ 
/*  67 */     return this.columns[(column - 1)];
/*     */   }
/*     */ 
/*     */   public int getColumnCount()
/*     */     throws SQLException
/*     */   {
/*  73 */     return this.columnCount;
/*     */   }
/*     */ 
/*     */   public int getColumnDisplaySize(int column) throws SQLException {
/*  77 */     return getColumn(column).displaySize;
/*     */   }
/*     */ 
/*     */   public int getColumnType(int column) throws SQLException {
/*  81 */     if (this.useLOBs) {
/*  82 */       return getColumn(column).jdbcType;
/*     */     }
/*  84 */     return Support.convertLOBType(getColumn(column).jdbcType);
/*     */   }
/*     */ 
/*     */   public int getPrecision(int column) throws SQLException
/*     */   {
/*  89 */     return getColumn(column).precision;
/*     */   }
/*     */ 
/*     */   public int getScale(int column) throws SQLException {
/*  93 */     return getColumn(column).scale;
/*     */   }
/*     */ 
/*     */   public int isNullable(int column) throws SQLException {
/*  97 */     return getColumn(column).nullable;
/*     */   }
/*     */ 
/*     */   public boolean isAutoIncrement(int column) throws SQLException {
/* 101 */     return getColumn(column).isIdentity;
/*     */   }
/*     */ 
/*     */   public boolean isCaseSensitive(int column) throws SQLException {
/* 105 */     return getColumn(column).isCaseSensitive;
/*     */   }
/*     */ 
/*     */   public boolean isCurrency(int column) throws SQLException {
/* 109 */     return TdsData.isCurrency(getColumn(column));
/*     */   }
/*     */ 
/*     */   public boolean isDefinitelyWritable(int column) throws SQLException {
/* 113 */     getColumn(column);
/*     */ 
/* 115 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean isReadOnly(int column) throws SQLException {
/* 119 */     return (!(getColumn(column).isWriteable));
/*     */   }
/*     */ 
/*     */   public boolean isSearchable(int column) throws SQLException {
/* 123 */     return TdsData.isSearchable(getColumn(column));
/*     */   }
/*     */ 
/*     */   public boolean isSigned(int column) throws SQLException {
/* 127 */     return TdsData.isSigned(getColumn(column));
/*     */   }
/*     */ 
/*     */   public boolean isWritable(int column) throws SQLException {
/* 131 */     return getColumn(column).isWriteable;
/*     */   }
/*     */ 
/*     */   public String getCatalogName(int column) throws SQLException {
/* 135 */     ColInfo col = getColumn(column);
/*     */ 
/* 137 */     return ((col.catalog == null) ? "" : col.catalog);
/*     */   }
/*     */ 
/*     */   public String getColumnClassName(int column) throws SQLException {
/* 141 */     String c = Support.getClassName(getColumnType(column));
/*     */ 
/* 143 */     if (!(this.useLOBs)) {
/* 144 */       if ("java.sql.Clob".equals(c)) {
/* 145 */         return "java.lang.String";
/*     */       }
/*     */ 
/* 148 */       if ("java.sql.Blob".equals(c)) {
/* 149 */         return "[B";
/*     */       }
/*     */     }
/*     */ 
/* 153 */     return c;
/*     */   }
/*     */ 
/*     */   public String getColumnLabel(int column) throws SQLException {
/* 157 */     return getColumn(column).name;
/*     */   }
/*     */ 
/*     */   public String getColumnName(int column) throws SQLException {
/* 161 */     return getColumn(column).name;
/*     */   }
/*     */ 
/*     */   public String getColumnTypeName(int column) throws SQLException {
/* 165 */     return getColumn(column).sqlType;
/*     */   }
/*     */ 
/*     */   public String getSchemaName(int column) throws SQLException {
/* 169 */     ColInfo col = getColumn(column);
/*     */ 
/* 171 */     return ((col.schema == null) ? "" : col.schema);
/*     */   }
/*     */ 
/*     */   public String getTableName(int column) throws SQLException {
/* 175 */     ColInfo col = getColumn(column);
/*     */ 
/* 177 */     return ((col.tableName == null) ? "" : col.tableName);
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.JtdsResultSetMetaData
 * JD-Core Version:    0.5.3
 */