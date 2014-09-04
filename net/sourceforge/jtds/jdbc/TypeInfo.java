/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.ResultSetMetaData;
/*     */ import java.sql.SQLException;
/*     */ 
/*     */ public class TypeInfo
/*     */   implements Comparable
/*     */ {
/*     */   static final int NUM_COLS = 18;
/*     */   private final String typeName;
/*     */   private final int dataType;
/*     */   private final int precision;
/*     */   private final String literalPrefix;
/*     */   private final String literalSuffix;
/*     */   private final String createParams;
/*     */   private final short nullable;
/*     */   private final boolean caseSensitive;
/*     */   private final short searchable;
/*     */   private final boolean unsigned;
/*     */   private final boolean fixedPrecScale;
/*     */   private final boolean autoIncrement;
/*     */   private final String localTypeName;
/*     */   private final short minimumScale;
/*     */   private final short maximumScale;
/*     */   private final int sqlDataType;
/*     */   private final int sqlDatetimeSub;
/*     */   private final int numPrecRadix;
/*     */   private final int normalizedType;
/*     */   private final int distanceFromJdbcType;
/*     */ 
/*     */   public TypeInfo(ResultSet rs, boolean useLOBs)
/*     */     throws SQLException
/*     */   {
/*  60 */     this.typeName = rs.getString(1);
/*  61 */     this.dataType = rs.getInt(2);
/*  62 */     this.precision = rs.getInt(3);
/*  63 */     this.literalPrefix = rs.getString(4);
/*  64 */     this.literalSuffix = rs.getString(5);
/*  65 */     this.createParams = rs.getString(6);
/*  66 */     this.nullable = rs.getShort(7);
/*  67 */     this.caseSensitive = rs.getBoolean(8);
/*  68 */     this.searchable = rs.getShort(9);
/*  69 */     this.unsigned = rs.getBoolean(10);
/*  70 */     this.fixedPrecScale = rs.getBoolean(11);
/*  71 */     this.autoIncrement = rs.getBoolean(12);
/*  72 */     this.localTypeName = rs.getString(13);
/*  73 */     if (rs.getMetaData().getColumnCount() >= 18)
/*     */     {
/*  75 */       this.minimumScale = rs.getShort(14);
/*  76 */       this.maximumScale = rs.getShort(15);
/*  77 */       this.sqlDataType = rs.getInt(16);
/*  78 */       this.sqlDatetimeSub = rs.getInt(17);
/*  79 */       this.numPrecRadix = rs.getInt(18);
/*     */     }
/*     */     else {
/*  82 */       this.minimumScale = 0;
/*  83 */       this.maximumScale = 0;
/*  84 */       this.sqlDataType = 0;
/*  85 */       this.sqlDatetimeSub = 0;
/*  86 */       this.numPrecRadix = 0;
/*     */     }
/*  88 */     this.normalizedType = normalizeDataType(this.dataType, useLOBs);
/*  89 */     this.distanceFromJdbcType = determineDistanceFromJdbcType();
/*     */   }
/*     */ 
/*     */   public TypeInfo(String typeName, int dataType, boolean autoIncrement)
/*     */   {
/*  98 */     this.typeName = typeName;
/*  99 */     this.dataType = dataType;
/* 100 */     this.autoIncrement = autoIncrement;
/* 101 */     this.precision = 0;
/* 102 */     this.literalPrefix = null;
/* 103 */     this.literalSuffix = null;
/* 104 */     this.createParams = null;
/* 105 */     this.nullable = 0;
/* 106 */     this.caseSensitive = false;
/* 107 */     this.searchable = 0;
/* 108 */     this.unsigned = false;
/* 109 */     this.fixedPrecScale = false;
/* 110 */     this.localTypeName = null;
/* 111 */     this.minimumScale = 0;
/* 112 */     this.maximumScale = 0;
/* 113 */     this.sqlDataType = 0;
/* 114 */     this.sqlDatetimeSub = 0;
/* 115 */     this.numPrecRadix = 0;
/*     */ 
/* 117 */     this.normalizedType = normalizeDataType(dataType, true);
/* 118 */     this.distanceFromJdbcType = determineDistanceFromJdbcType();
/*     */   }
/*     */ 
/*     */   public boolean equals(Object o) {
/* 122 */     if (o instanceof TypeInfo) {
/* 123 */       return (compareTo(o) == 0);
/*     */     }
/*     */ 
/* 126 */     return false;
/*     */   }
/*     */ 
/*     */   public int hashCode() {
/* 130 */     return (this.normalizedType * this.dataType * ((this.autoIncrement) ? 7 : 11));
/*     */   }
/*     */ 
/*     */   public String toString() {
/* 134 */     return this.typeName + " (" + ((this.dataType != this.normalizedType) ? this.dataType + "->" : "") + this.normalizedType + ')';
/*     */   }
/*     */ 
/*     */   public void update(ResultSet rs)
/*     */     throws SQLException
/*     */   {
/* 140 */     rs.updateString(1, this.typeName);
/* 141 */     rs.updateInt(2, this.normalizedType);
/* 142 */     rs.updateInt(3, this.precision);
/* 143 */     rs.updateString(4, this.literalPrefix);
/* 144 */     rs.updateString(5, this.literalSuffix);
/* 145 */     rs.updateString(6, this.createParams);
/* 146 */     rs.updateShort(7, this.nullable);
/* 147 */     rs.updateBoolean(8, this.caseSensitive);
/* 148 */     rs.updateShort(9, this.searchable);
/* 149 */     rs.updateBoolean(10, this.unsigned);
/* 150 */     rs.updateBoolean(11, this.fixedPrecScale);
/* 151 */     rs.updateBoolean(12, this.autoIncrement);
/* 152 */     rs.updateString(13, this.localTypeName);
/* 153 */     if (rs.getMetaData().getColumnCount() < 18)
/*     */       return;
/* 155 */     rs.updateShort(14, this.minimumScale);
/* 156 */     rs.updateShort(15, this.maximumScale);
/* 157 */     rs.updateInt(16, this.sqlDataType);
/* 158 */     rs.updateInt(17, this.sqlDatetimeSub);
/* 159 */     rs.updateInt(18, this.numPrecRadix);
/*     */   }
/*     */ 
/*     */   public int compareTo(Object o)
/*     */   {
/* 172 */     TypeInfo other = (TypeInfo)o;
/*     */ 
/* 175 */     return (compare(this.normalizedType, other.normalizedType) * 10 + compare(this.distanceFromJdbcType, other.distanceFromJdbcType));
/*     */   }
/*     */ 
/*     */   private int compare(int i1, int i2)
/*     */   {
/* 180 */     return ((i1 == i2) ? 0 : (i1 < i2) ? -1 : 1);
/*     */   }
/*     */ 
/*     */   private int determineDistanceFromJdbcType()
/*     */   {
/* 194 */     switch (this.dataType)
/*     */     {
/*     */     case 6:
/*     */     case 9:
/*     */     case 10:
/*     */     case 11:
/* 201 */       return 0;
/*     */     case 12:
/* 203 */       if (this.typeName.equalsIgnoreCase("varchar")) {
/* 204 */         return 0;
/*     */       }
/* 206 */       if (this.typeName.equalsIgnoreCase("nvarchar")) {
/* 207 */         return 1;
/*     */       }
/* 209 */       return 2;
/*     */     case -9:
/* 214 */       return ((this.typeName.equalsIgnoreCase("sysname")) ? 4 : 3);
/*     */     case -11:
/* 218 */       return 9;
/*     */     case -150:
/* 220 */       return 8;
/*     */     }
/*     */ 
/* 226 */     return (((this.dataType == this.normalizedType) && (!(this.autoIncrement))) ? 0 : 5);
/*     */   }
/*     */ 
/*     */   public static int normalizeDataType(int serverDataType, boolean useLOBs)
/*     */   {
/* 238 */     switch (serverDataType)
/*     */     {
/*     */     case 35:
/* 240 */       return 12;
/*     */     case 11:
/* 242 */       return 93;
/*     */     case 10:
/* 244 */       return 92;
/*     */     case 9:
/* 246 */       return 91;
/*     */     case 6:
/* 248 */       return 8;
/*     */     case -1:
/* 250 */       return ((useLOBs) ? 2005 : -1);
/*     */     case -4:
/* 252 */       return ((useLOBs) ? 2004 : -4);
/*     */     case -8:
/* 254 */       return 1;
/*     */     case -9:
/* 256 */       return 12;
/*     */     case -10:
/* 258 */       return ((useLOBs) ? 2005 : -1);
/*     */     case -11:
/* 260 */       return 1;
/*     */     case -150:
/* 262 */       return 12;
/*     */     }
/* 264 */     return serverDataType;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.TypeInfo
 * JD-Core Version:    0.5.3
 */