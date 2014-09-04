/*      */ package net.sourceforge.jtds.jdbc;
/*      */ 
/*      */ import java.sql.CallableStatement;
/*      */ import java.sql.Connection;
/*      */ import java.sql.DatabaseMetaData;
/*      */ import java.sql.ResultSet;
/*      */ import java.sql.ResultSetMetaData;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.Statement;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collection;
/*      */ import java.util.Collections;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ 
/*      */ public class JtdsDatabaseMetaData
/*      */   implements DatabaseMetaData
/*      */ {
/*      */   static final int sqlStateXOpen = 1;
/*      */   private final int tdsVersion;
/*      */   private final int serverType;
/*      */   private final ConnectionJDBC2 connection;
/*   60 */   int sysnameLength = 30;
/*      */   Boolean caseSensitive;
/*      */ 
/*      */   public JtdsDatabaseMetaData(ConnectionJDBC2 connection)
/*      */   {
/*   70 */     this.connection = connection;
/*   71 */     this.tdsVersion = connection.getTdsVersion();
/*   72 */     this.serverType = connection.getServerType();
/*   73 */     if (this.tdsVersion >= 3)
/*   74 */       this.sysnameLength = 128;
/*      */   }
/*      */ 
/*      */   public boolean allProceduresAreCallable()
/*      */     throws SQLException
/*      */   {
/*   90 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean allTablesAreSelectable()
/*      */     throws SQLException
/*      */   {
/*  102 */     return (this.connection.getServerType() == 1);
/*      */   }
/*      */ 
/*      */   public boolean dataDefinitionCausesTransactionCommit()
/*      */     throws SQLException
/*      */   {
/*  113 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean dataDefinitionIgnoredInTransactions()
/*      */     throws SQLException
/*      */   {
/*  123 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean doesMaxRowSizeIncludeBlobs()
/*      */     throws SQLException
/*      */   {
/*  133 */     return false;
/*      */   }
/*      */ 
/*      */   public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable)
/*      */     throws SQLException
/*      */   {
/*  182 */     String[] colNames = { "SCOPE", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "PSEUDO_COLUMN" };
/*      */ 
/*  186 */     int[] colTypes = { 5, 12, 4, 12, 4, 4, 5, 5 };
/*      */ 
/*  191 */     String query = "sp_special_columns ?, ?, ?, ?, ?, ?, ?";
/*      */ 
/*  193 */     CallableStatement s = this.connection.prepareCall(syscall(catalog, query));
/*      */ 
/*  195 */     s.setString(1, table);
/*  196 */     s.setString(2, schema);
/*  197 */     s.setString(3, catalog);
/*  198 */     s.setString(4, "R");
/*  199 */     s.setString(5, "T");
/*  200 */     s.setString(6, "U");
/*  201 */     s.setInt(7, 3);
/*      */ 
/*  203 */     JtdsResultSet rs = (JtdsResultSet)s.executeQuery();
/*  204 */     CachedResultSet rsTmp = new CachedResultSet((JtdsStatement)s, colNames, colTypes);
/*  205 */     rsTmp.moveToInsertRow();
/*  206 */     int colCnt = rs.getMetaData().getColumnCount();
/*  207 */     while (rs.next()) {
/*  208 */       for (int i = 1; i <= colCnt; ++i) {
/*  209 */         if (i == 3) {
/*  210 */           int type = TypeInfo.normalizeDataType(rs.getInt(i), this.connection.getUseLOBs());
/*  211 */           rsTmp.updateInt(i, type);
/*      */         } else {
/*  213 */           rsTmp.updateObject(i, rs.getObject(i));
/*      */         }
/*      */       }
/*  216 */       rsTmp.insertRow();
/*      */     }
/*  218 */     rs.close();
/*      */ 
/*  220 */     rsTmp.moveToCurrentRow();
/*  221 */     rsTmp.setConcurrency(1007);
/*  222 */     return rsTmp;
/*      */   }
/*      */ 
/*      */   public ResultSet getCatalogs()
/*      */     throws SQLException
/*      */   {
/*  240 */     String query = "exec sp_tables '', '', '%', NULL";
/*  241 */     Statement s = this.connection.createStatement();
/*  242 */     JtdsResultSet rs = (JtdsResultSet)s.executeQuery(query);
/*      */ 
/*  244 */     rs.setColumnCount(1);
/*  245 */     rs.setColLabel(1, "TABLE_CAT");
/*      */ 
/*  247 */     upperCaseColumnNames(rs);
/*      */ 
/*  249 */     return rs;
/*      */   }
/*      */ 
/*      */   public String getCatalogSeparator()
/*      */     throws SQLException
/*      */   {
/*  259 */     return ".";
/*      */   }
/*      */ 
/*      */   public String getCatalogTerm()
/*      */     throws SQLException
/*      */   {
/*  269 */     return "database";
/*      */   }
/*      */ 
/*      */   public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern)
/*      */     throws SQLException
/*      */   {
/*  308 */     String query = "sp_column_privileges ?, ?, ?, ?";
/*      */ 
/*  310 */     CallableStatement s = this.connection.prepareCall(syscall(catalog, query));
/*      */ 
/*  312 */     s.setString(1, table);
/*  313 */     s.setString(2, schema);
/*  314 */     s.setString(3, catalog);
/*  315 */     s.setString(4, processEscapes(columnNamePattern));
/*      */ 
/*  317 */     JtdsResultSet rs = (JtdsResultSet)s.executeQuery();
/*      */ 
/*  319 */     rs.setColLabel(1, "TABLE_CAT");
/*  320 */     rs.setColLabel(2, "TABLE_SCHEM");
/*      */ 
/*  322 */     upperCaseColumnNames(rs);
/*      */ 
/*  324 */     return rs;
/*      */   }
/*      */ 
/*      */   public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern)
/*      */     throws SQLException
/*      */   {
/*  385 */     String[] colNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX", "NULLABLE", "REMARKS", "COLUMN_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATALOG", "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE" };
/*      */ 
/*  397 */     int[] colTypes = { 12, 12, 12, 12, 4, 12, 4, 4, 4, 4, 4, 12, 12, 4, 4, 4, 4, 12, 12, 12, 12, 5 };
/*      */ 
/*  408 */     String query = "sp_columns ?, ?, ?, ?, ?";
/*      */ 
/*  410 */     CallableStatement s = this.connection.prepareCall(syscall(catalog, query));
/*      */ 
/*  412 */     s.setString(1, processEscapes(tableNamePattern));
/*  413 */     s.setString(2, processEscapes(schemaPattern));
/*  414 */     s.setString(3, catalog);
/*  415 */     s.setString(4, processEscapes(columnNamePattern));
/*  416 */     s.setInt(5, 3);
/*      */ 
/*  418 */     JtdsResultSet rs = (JtdsResultSet)s.executeQuery();
/*      */ 
/*  420 */     CachedResultSet rsTmp = new CachedResultSet((JtdsStatement)s, colNames, colTypes);
/*  421 */     rsTmp.moveToInsertRow();
/*  422 */     int colCnt = rs.getMetaData().getColumnCount();
/*      */ 
/*  427 */     while (rs.next())
/*      */     {
/*      */       int i;
/*  428 */       if (this.serverType == 2)
/*      */       {
/*  430 */         for (i = 1; i <= 4; ++i) {
/*  431 */           rsTmp.updateObject(i, rs.getObject(i));
/*      */         }
/*  433 */         rsTmp.updateInt(5, TypeInfo.normalizeDataType(rs.getInt(5), this.connection.getUseLOBs()));
/*  434 */         String typeName = rs.getString(6);
/*  435 */         rsTmp.updateString(6, typeName);
/*  436 */         for (int i = 8; i <= 12; ++i) {
/*  437 */           rsTmp.updateObject(i, rs.getObject(i));
/*      */         }
/*  439 */         if (colCnt >= 20)
/*      */         {
/*  441 */           for (i = 13; i <= 18; ++i)
/*  442 */             rsTmp.updateObject(i, rs.getObject(i + 2));
/*      */         }
/*      */         else
/*      */         {
/*  446 */           rsTmp.updateObject(16, rs.getObject(8));
/*  447 */           rsTmp.updateObject(17, rs.getObject(14));
/*      */         }
/*  449 */         if (("image".equals(typeName)) || ("text".equals(typeName))) {
/*  450 */           rsTmp.updateInt(7, 2147483647);
/*  451 */           rsTmp.updateInt(16, 2147483647);
/*      */         }
/*  453 */         else if (("univarchar".equals(typeName)) || ("unichar".equals(typeName))) {
/*  454 */           rsTmp.updateInt(7, rs.getInt(7) / 2);
/*  455 */           rsTmp.updateObject(16, rs.getObject(7));
/*      */         } else {
/*  457 */           rsTmp.updateInt(7, rs.getInt(7));
/*      */         }
/*      */       }
/*      */       else {
/*  461 */         for (i = 1; i <= colCnt; ++i) {
/*  462 */           if (i == 5) {
/*  463 */             int type = TypeInfo.normalizeDataType(rs.getInt(i), this.connection.getUseLOBs());
/*  464 */             rsTmp.updateInt(i, type);
/*      */           }
/*  466 */           else if (i == 19)
/*      */           {
/*  470 */             rsTmp.updateString(6, TdsData.getMSTypeName(rs.getString(6), rs.getInt(19)));
/*      */           } else {
/*  472 */             rsTmp.updateObject(i, rs.getObject(i));
/*      */           }
/*      */         }
/*      */       }
/*  476 */       rsTmp.insertRow();
/*      */     }
/*  478 */     rs.close();
/*  479 */     rsTmp.moveToCurrentRow();
/*  480 */     rsTmp.setConcurrency(1007);
/*      */ 
/*  482 */     return rsTmp;
/*      */   }
/*      */ 
/*      */   public ResultSet getCrossReference(String primaryCatalog, String primarySchema, String primaryTable, String foreignCatalog, String foreignSchema, String foreignTable)
/*      */     throws SQLException
/*      */   {
/*  571 */     String[] colNames = { "PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME", "FKTABLE_CAT", "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ", "UPDATE_RULE", "DELETE_RULE", "FK_NAME", "PK_NAME", "DEFERRABILITY" };
/*      */ 
/*  578 */     int[] colTypes = { 12, 12, 12, 12, 12, 12, 12, 12, 5, 5, 5, 12, 12, 5 };
/*      */ 
/*  586 */     String query = "sp_fkeys ?, ?, ?, ?, ?, ?";
/*      */ 
/*  588 */     if (primaryCatalog != null)
/*  589 */       query = syscall(primaryCatalog, query);
/*  590 */     else if (foreignCatalog != null)
/*  591 */       query = syscall(foreignCatalog, query);
/*      */     else {
/*  593 */       query = syscall(null, query);
/*      */     }
/*      */ 
/*  596 */     CallableStatement s = this.connection.prepareCall(query);
/*      */ 
/*  598 */     s.setString(1, primaryTable);
/*  599 */     s.setString(2, processEscapes(primarySchema));
/*  600 */     s.setString(3, primaryCatalog);
/*  601 */     s.setString(4, foreignTable);
/*  602 */     s.setString(5, processEscapes(foreignSchema));
/*  603 */     s.setString(6, foreignCatalog);
/*      */ 
/*  605 */     JtdsResultSet rs = (JtdsResultSet)s.executeQuery();
/*  606 */     int colCnt = rs.getMetaData().getColumnCount();
/*  607 */     CachedResultSet rsTmp = new CachedResultSet((JtdsStatement)s, colNames, colTypes);
/*  608 */     rsTmp.moveToInsertRow();
/*  609 */     while (rs.next()) {
/*  610 */       for (int i = 1; i <= colCnt; ++i) {
/*  611 */         rsTmp.updateObject(i, rs.getObject(i));
/*      */       }
/*  613 */       if (colCnt < 14) {
/*  614 */         rsTmp.updateShort(14, 7);
/*      */       }
/*  616 */       rsTmp.insertRow();
/*      */     }
/*  618 */     rs.close();
/*  619 */     rsTmp.moveToCurrentRow();
/*  620 */     rsTmp.setConcurrency(1007);
/*      */ 
/*  622 */     return rsTmp;
/*      */   }
/*      */ 
/*      */   public String getDatabaseProductName()
/*      */     throws SQLException
/*      */   {
/*  632 */     return this.connection.getDatabaseProductName();
/*      */   }
/*      */ 
/*      */   public String getDatabaseProductVersion()
/*      */     throws SQLException
/*      */   {
/*  642 */     return this.connection.getDatabaseProductVersion();
/*      */   }
/*      */ 
/*      */   public int getDefaultTransactionIsolation()
/*      */     throws SQLException
/*      */   {
/*  657 */     return 2;
/*      */   }
/*      */ 
/*      */   public int getDriverMajorVersion()
/*      */   {
/*  666 */     return 1;
/*      */   }
/*      */ 
/*      */   public int getDriverMinorVersion()
/*      */   {
/*  675 */     return 2;
/*      */   }
/*      */ 
/*      */   public String getDriverName()
/*      */     throws SQLException
/*      */   {
/*  685 */     return "jTDS Type 4 JDBC Driver for MS SQL Server and Sybase";
/*      */   }
/*      */ 
/*      */   public String getDriverVersion()
/*      */     throws SQLException
/*      */   {
/*  695 */     return Driver.getVersion();
/*      */   }
/*      */ 
/*      */   public ResultSet getExportedKeys(String catalog, String schema, String table)
/*      */     throws SQLException
/*      */   {
/*  775 */     return getCrossReference(catalog, schema, table, null, null, null);
/*      */   }
/*      */ 
/*      */   public String getExtraNameCharacters()
/*      */     throws SQLException
/*      */   {
/*  787 */     return "$#@";
/*      */   }
/*      */ 
/*      */   public String getIdentifierQuoteString()
/*      */     throws SQLException
/*      */   {
/*  799 */     return "\"";
/*      */   }
/*      */ 
/*      */   public ResultSet getImportedKeys(String catalog, String schema, String table)
/*      */     throws SQLException
/*      */   {
/*  878 */     return getCrossReference(null, null, null, catalog, schema, table);
/*      */   }
/*      */ 
/*      */   public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate)
/*      */     throws SQLException
/*      */   {
/*  941 */     String[] colNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "NON_UNIQUE", "INDEX_QUALIFIER", "INDEX_NAME", "TYPE", "ORDINAL_POSITION", "COLUMN_NAME", "ASC_OR_DESC", "CARDINALITY", "PAGES", "FILTER_CONDITION" };
/*      */ 
/*  948 */     int[] colTypes = { 12, 12, 12, -7, 12, 12, 5, 5, 12, 12, 4, 4, 12 };
/*      */ 
/*  955 */     String query = "sp_statistics ?, ?, ?, ?, ?, ?";
/*      */ 
/*  957 */     CallableStatement s = this.connection.prepareCall(syscall(catalog, query));
/*      */ 
/*  959 */     s.setString(1, table);
/*  960 */     s.setString(2, schema);
/*  961 */     s.setString(3, catalog);
/*  962 */     s.setString(4, "%");
/*  963 */     s.setString(5, (unique) ? "Y" : "N");
/*  964 */     s.setString(6, (approximate) ? "Q" : "E");
/*      */ 
/*  966 */     JtdsResultSet rs = (JtdsResultSet)s.executeQuery();
/*  967 */     int colCnt = rs.getMetaData().getColumnCount();
/*  968 */     CachedResultSet rsTmp = new CachedResultSet((JtdsStatement)s, colNames, colTypes);
/*  969 */     rsTmp.moveToInsertRow();
/*  970 */     while (rs.next()) {
/*  971 */       for (int i = 1; i <= colCnt; ++i) {
/*  972 */         rsTmp.updateObject(i, rs.getObject(i));
/*      */       }
/*  974 */       rsTmp.insertRow();
/*      */     }
/*  976 */     rs.close();
/*  977 */     rsTmp.moveToCurrentRow();
/*  978 */     rsTmp.setConcurrency(1007);
/*      */ 
/*  980 */     return rsTmp;
/*      */   }
/*      */ 
/*      */   public int getMaxBinaryLiteralLength()
/*      */     throws SQLException
/*      */   {
/* 1000 */     return 131072;
/*      */   }
/*      */ 
/*      */   public int getMaxCatalogNameLength()
/*      */     throws SQLException
/*      */   {
/* 1011 */     return this.sysnameLength;
/*      */   }
/*      */ 
/*      */   public int getMaxCharLiteralLength()
/*      */     throws SQLException
/*      */   {
/* 1025 */     return 131072;
/*      */   }
/*      */ 
/*      */   public int getMaxColumnNameLength()
/*      */     throws SQLException
/*      */   {
/* 1037 */     return this.sysnameLength;
/*      */   }
/*      */ 
/*      */   public int getMaxColumnsInGroupBy()
/*      */     throws SQLException
/*      */   {
/* 1051 */     return ((this.tdsVersion >= 3) ? 0 : 16);
/*      */   }
/*      */ 
/*      */   public int getMaxColumnsInIndex()
/*      */     throws SQLException
/*      */   {
/* 1064 */     return 16;
/*      */   }
/*      */ 
/*      */   public int getMaxColumnsInOrderBy()
/*      */     throws SQLException
/*      */   {
/* 1077 */     return ((this.tdsVersion >= 3) ? 0 : 16);
/*      */   }
/*      */ 
/*      */   public int getMaxColumnsInSelect()
/*      */     throws SQLException
/*      */   {
/* 1089 */     return 4096;
/*      */   }
/*      */ 
/*      */   public int getMaxColumnsInTable()
/*      */     throws SQLException
/*      */   {
/* 1103 */     return ((this.tdsVersion >= 3) ? 1024 : 250);
/*      */   }
/*      */ 
/*      */   public int getMaxConnections()
/*      */     throws SQLException
/*      */   {
/* 1117 */     return 32767;
/*      */   }
/*      */ 
/*      */   public int getMaxCursorNameLength()
/*      */     throws SQLException
/*      */   {
/* 1128 */     return this.sysnameLength;
/*      */   }
/*      */ 
/*      */   public int getMaxIndexLength()
/*      */     throws SQLException
/*      */   {
/* 1141 */     return ((this.tdsVersion >= 3) ? 900 : 255);
/*      */   }
/*      */ 
/*      */   public int getMaxProcedureNameLength()
/*      */     throws SQLException
/*      */   {
/* 1152 */     return this.sysnameLength;
/*      */   }
/*      */ 
/*      */   public int getMaxRowSize()
/*      */     throws SQLException
/*      */   {
/* 1165 */     return ((this.tdsVersion >= 3) ? 8060 : 1962);
/*      */   }
/*      */ 
/*      */   public int getMaxSchemaNameLength()
/*      */     throws SQLException
/*      */   {
/* 1175 */     return this.sysnameLength;
/*      */   }
/*      */ 
/*      */   public int getMaxStatementLength()
/*      */     throws SQLException
/*      */   {
/* 1190 */     return 0;
/*      */   }
/*      */ 
/*      */   public int getMaxStatements()
/*      */     throws SQLException
/*      */   {
/* 1201 */     return 0;
/*      */   }
/*      */ 
/*      */   public int getMaxTableNameLength()
/*      */     throws SQLException
/*      */   {
/* 1212 */     return this.sysnameLength;
/*      */   }
/*      */ 
/*      */   public int getMaxTablesInSelect()
/*      */     throws SQLException
/*      */   {
/* 1226 */     return ((this.tdsVersion > 2) ? 256 : 16);
/*      */   }
/*      */ 
/*      */   public int getMaxUserNameLength()
/*      */     throws SQLException
/*      */   {
/* 1236 */     return this.sysnameLength;
/*      */   }
/*      */ 
/*      */   public String getNumericFunctions()
/*      */     throws SQLException
/*      */   {
/* 1249 */     return "abs,acos,asin,atan,atan2,ceiling,cos,cot,degrees,exp,floor,log,log10,mod,pi,power,radians,rand,round,sign,sin,sqrt,tan";
/*      */   }
/*      */ 
/*      */   public ResultSet getPrimaryKeys(String catalog, String schema, String table)
/*      */     throws SQLException
/*      */   {
/* 1278 */     String[] colNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "KEY_SEQ", "PK_NAME" };
/*      */ 
/* 1281 */     int[] colTypes = { 12, 12, 12, 12, 5, 12 };
/*      */ 
/* 1284 */     String query = "sp_pkeys ?, ?, ?";
/*      */ 
/* 1286 */     CallableStatement s = this.connection.prepareCall(syscall(catalog, query));
/*      */ 
/* 1288 */     s.setString(1, table);
/* 1289 */     s.setString(2, schema);
/* 1290 */     s.setString(3, catalog);
/*      */ 
/* 1292 */     JtdsResultSet rs = (JtdsResultSet)s.executeQuery();
/* 1293 */     CachedResultSet rsTmp = new CachedResultSet((JtdsStatement)s, colNames, colTypes);
/* 1294 */     rsTmp.moveToInsertRow();
/* 1295 */     int colCnt = rs.getMetaData().getColumnCount();
/* 1296 */     while (rs.next()) {
/* 1297 */       for (int i = 1; i <= colCnt; ++i) {
/* 1298 */         rsTmp.updateObject(i, rs.getObject(i));
/*      */       }
/* 1300 */       rsTmp.insertRow();
/*      */     }
/* 1302 */     rs.close();
/* 1303 */     rsTmp.moveToCurrentRow();
/* 1304 */     rsTmp.setConcurrency(1007);
/*      */ 
/* 1306 */     return rsTmp;
/*      */   }
/*      */ 
/*      */   public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern)
/*      */     throws SQLException
/*      */   {
/* 1374 */     String[] colNames = { "PROCEDURE_CAT", "PROCEDURE_SCHEM", "PROCEDURE_NAME", "COLUMN_NAME", "COLUMN_TYPE", "DATA_TYPE", "TYPE_NAME", "PRECISION", "LENGTH", "SCALE", "RADIX", "NULLABLE", "REMARKS" };
/*      */ 
/* 1381 */     int[] colTypes = { 12, 12, 12, 12, 5, 4, 12, 4, 4, 5, 5, 5, 12 };
/*      */ 
/* 1389 */     String query = "sp_sproc_columns ?, ?, ?, ?, ?";
/*      */ 
/* 1391 */     CallableStatement s = this.connection.prepareCall(syscall(catalog, query));
/*      */ 
/* 1393 */     s.setString(1, processEscapes(procedureNamePattern));
/* 1394 */     s.setString(2, processEscapes(schemaPattern));
/* 1395 */     s.setString(3, catalog);
/* 1396 */     s.setString(4, processEscapes(columnNamePattern));
/* 1397 */     s.setInt(5, 3);
/*      */ 
/* 1399 */     JtdsResultSet rs = (JtdsResultSet)s.executeQuery();
/* 1400 */     ResultSetMetaData rsmd = rs.getMetaData();
/* 1401 */     CachedResultSet rsTmp = new CachedResultSet((JtdsStatement)s, colNames, colTypes);
/* 1402 */     rsTmp.moveToInsertRow();
/* 1403 */     while (rs.next()) {
/* 1404 */       int offset = 0;
/* 1405 */       for (int i = 1; i + offset <= colNames.length; ++i) {
/* 1406 */         if ((i == 5) && (!("column_type".equalsIgnoreCase(rsmd.getColumnName(i)))))
/*      */         {
/* 1410 */           String colName = rs.getString(4);
/* 1411 */           if ("RETURN_VALUE".equals(colName))
/* 1412 */             rsTmp.updateInt(i, 5);
/*      */           else {
/* 1414 */             rsTmp.updateInt(i, 0);
/*      */           }
/* 1416 */           offset = 1;
/*      */         }
/* 1418 */         if (i == 3) {
/* 1419 */           String name = rs.getString(i);
/* 1420 */           if ((name != null) && (name.length() > 0)) {
/* 1421 */             int pos = name.lastIndexOf(59);
/* 1422 */             if (pos >= 0) {
/* 1423 */               name = name.substring(0, pos);
/*      */             }
/*      */           }
/* 1426 */           rsTmp.updateString(i + offset, name);
/* 1427 */         } else if ("data_type".equalsIgnoreCase(rsmd.getColumnName(i))) {
/* 1428 */           int type = TypeInfo.normalizeDataType(rs.getInt(i), this.connection.getUseLOBs());
/* 1429 */           rsTmp.updateInt(i + offset, type);
/*      */         } else {
/* 1431 */           rsTmp.updateObject(i + offset, rs.getObject(i));
/*      */         }
/*      */       }
/* 1434 */       if ((this.serverType == 2) && (rsmd.getColumnCount() >= 22))
/*      */       {
/* 1439 */         String mode = rs.getString(22);
/* 1440 */         if (mode != null) {
/* 1441 */           if (mode.equalsIgnoreCase("in")) {
/* 1442 */             rsTmp.updateInt(5, 1);
/*      */           }
/* 1444 */           else if (mode.equalsIgnoreCase("out")) {
/* 1445 */             rsTmp.updateInt(5, 2);
/*      */           }
/*      */         }
/*      */       }
/* 1449 */       if ((this.serverType == 2) || (this.tdsVersion == 1) || (this.tdsVersion == 3))
/*      */       {
/* 1456 */         String colName = rs.getString(4);
/* 1457 */         if ("RETURN_VALUE".equals(colName)) {
/* 1458 */           rsTmp.updateString(4, "@RETURN_VALUE");
/*      */         }
/*      */       }
/* 1461 */       rsTmp.insertRow();
/*      */     }
/* 1463 */     rs.close();
/* 1464 */     rsTmp.moveToCurrentRow();
/* 1465 */     rsTmp.setConcurrency(1007);
/* 1466 */     return rsTmp;
/*      */   }
/*      */ 
/*      */   public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern)
/*      */     throws SQLException
/*      */   {
/* 1509 */     String[] colNames = { "PROCEDURE_CAT", "PROCEDURE_SCHEM", "PROCEDURE_NAME", "RESERVED_1", "RESERVED_2", "RESERVED_3", "REMARKS", "PROCEDURE_TYPE" };
/*      */ 
/* 1513 */     int[] colTypes = { 12, 12, 12, 4, 4, 4, 12, 5 };
/*      */ 
/* 1518 */     String query = "sp_stored_procedures ?, ?, ?";
/*      */ 
/* 1520 */     CallableStatement s = this.connection.prepareCall(syscall(catalog, query));
/*      */ 
/* 1522 */     s.setString(1, processEscapes(procedureNamePattern));
/* 1523 */     s.setString(2, processEscapes(schemaPattern));
/* 1524 */     s.setString(3, catalog);
/*      */ 
/* 1526 */     JtdsResultSet rs = (JtdsResultSet)s.executeQuery();
/*      */ 
/* 1528 */     CachedResultSet rsTmp = new CachedResultSet((JtdsStatement)s, colNames, colTypes);
/* 1529 */     rsTmp.moveToInsertRow();
/* 1530 */     int colCnt = rs.getMetaData().getColumnCount();
/*      */ 
/* 1534 */     while (rs.next()) {
/* 1535 */       rsTmp.updateString(1, rs.getString(1));
/* 1536 */       rsTmp.updateString(2, rs.getString(2));
/* 1537 */       String name = rs.getString(3);
/* 1538 */       if ((name != null) && 
/* 1540 */         (name.endsWith(";1"))) {
/* 1541 */         name = name.substring(0, name.length() - 2);
/*      */       }
/*      */ 
/* 1544 */       rsTmp.updateString(3, name);
/*      */ 
/* 1546 */       for (int i = 4; i <= colCnt; ++i) {
/* 1547 */         rsTmp.updateObject(i, rs.getObject(i));
/*      */       }
/* 1549 */       if (colCnt < 8)
/*      */       {
/* 1551 */         rsTmp.updateShort(8, 2);
/*      */       }
/* 1553 */       rsTmp.insertRow();
/*      */     }
/* 1555 */     rsTmp.moveToCurrentRow();
/* 1556 */     rsTmp.setConcurrency(1007);
/* 1557 */     rs.close();
/* 1558 */     return rsTmp;
/*      */   }
/*      */ 
/*      */   public String getProcedureTerm()
/*      */     throws SQLException
/*      */   {
/* 1569 */     return "stored procedure";
/*      */   }
/*      */ 
/*      */   public ResultSet getSchemas()
/*      */     throws SQLException
/*      */   {
/* 1586 */     Statement statement = this.connection.createStatement();
/*      */ 
/* 1590 */     if ((this.connection.getServerType() == 1) && (this.connection.getDatabaseMajorVersion() >= 9)) {
/* 1591 */       sql = (Driver.JDBC3) ? "SELECT name AS TABLE_SCHEM, NULL as TABLE_CATALOG FROM sys.schemas" : "SELECT name AS TABLE_SCHEM FROM sys.schemas";
/*      */     }
/*      */     else
/*      */     {
/* 1595 */       sql = (Driver.JDBC3) ? "SELECT name AS TABLE_SCHEM, NULL as TABLE_CATALOG FROM dbo.sysusers" : "SELECT name AS TABLE_SCHEM FROM dbo.sysusers";
/*      */ 
/* 1602 */       if (this.tdsVersion >= 3)
/* 1603 */         sql = sql + " WHERE islogin=1";
/*      */       else {
/* 1605 */         sql = sql + " WHERE uid>0";
/*      */       }
/*      */     }
/*      */ 
/* 1609 */     String sql = sql + " ORDER BY TABLE_SCHEM";
/*      */ 
/* 1611 */     return statement.executeQuery(sql);
/*      */   }
/*      */ 
/*      */   public String getSchemaTerm()
/*      */     throws SQLException
/*      */   {
/* 1621 */     return "owner";
/*      */   }
/*      */ 
/*      */   public String getSearchStringEscape()
/*      */     throws SQLException
/*      */   {
/* 1637 */     return "\\";
/*      */   }
/*      */ 
/*      */   public String getSQLKeywords()
/*      */     throws SQLException
/*      */   {
/* 1651 */     return "ARITH_OVERFLOW,BREAK,BROWSE,BULK,CHAR_CONVERT,CHECKPOINT,CLUSTERED,COMPUTE,CONFIRM,CONTROLROW,DATA_PGS,DATABASE,DBCC,DISK,DUMMY,DUMP,ENDTRAN,ERRLVL,ERRORDATA,ERROREXIT,EXIT,FILLFACTOR,HOLDLOCK,IDENTITY_INSERT,IF,INDEX,KILL,LINENO,LOAD,MAX_ROWS_PER_PAGE,MIRROR,MIRROREXIT,NOHOLDLOCK,NONCLUSTERED,NUMERIC_TRUNCATION,OFF,OFFSETS,ONCE,ONLINE,OVER,PARTITION,PERM,PERMANENT,PLAN,PRINT,PROC,PROCESSEXIT,RAISERROR,READ,READTEXT,RECONFIGURE,REPLACE,RESERVED_PGS,RETURN,ROLE,ROWCNT,ROWCOUNT,RULE,SAVE,SETUSER,SHARED,SHUTDOWN,SOME,STATISTICS,STRIPE,SYB_IDENTITY,SYB_RESTREE,SYB_TERMINATE,TEMP,TEXTSIZE,TRAN,TRIGGER,TRUNCATE,TSEQUAL,UNPARTITION,USE,USED_PGS,USER_OPTION,WAITFOR,WHILE,WRITETEXT";
/*      */   }
/*      */ 
/*      */   public String getStringFunctions()
/*      */     throws SQLException
/*      */   {
/* 1672 */     if (this.connection.getServerType() == 1) {
/* 1673 */       return "ascii,char,concat,difference,insert,lcase,left,length,locate,ltrim,repeat,replace,right,rtrim,soundex,space,substring,ucase";
/*      */     }
/*      */ 
/* 1676 */     return "ascii,char,concat,difference,insert,lcase,length,ltrim,repeat,right,rtrim,soundex,space,substring,ucase";
/*      */   }
/*      */ 
/*      */   public String getSystemFunctions()
/*      */     throws SQLException
/*      */   {
/* 1688 */     return "database,ifnull,user,convert";
/*      */   }
/*      */ 
/*      */   public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern)
/*      */     throws SQLException
/*      */   {
/* 1729 */     String query = "sp_table_privileges ?, ?, ?";
/*      */ 
/* 1731 */     CallableStatement s = this.connection.prepareCall(syscall(catalog, query));
/*      */ 
/* 1733 */     s.setString(1, processEscapes(tableNamePattern));
/* 1734 */     s.setString(2, processEscapes(schemaPattern));
/* 1735 */     s.setString(3, catalog);
/*      */ 
/* 1737 */     JtdsResultSet rs = (JtdsResultSet)s.executeQuery();
/*      */ 
/* 1739 */     rs.setColLabel(1, "TABLE_CAT");
/* 1740 */     rs.setColLabel(2, "TABLE_SCHEM");
/*      */ 
/* 1742 */     upperCaseColumnNames(rs);
/*      */ 
/* 1744 */     return rs;
/*      */   }
/*      */ 
/*      */   public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types)
/*      */     throws SQLException
/*      */   {
/* 1794 */     String[] colNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "TABLE_TYPE", "REMARKS", "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "SELF_REFERENCING_COL_NAME", "REF_GENERATION" };
/*      */ 
/* 1799 */     int[] colTypes = { 12, 12, 12, 12, 12, 12, 12, 12, 12, 12 };
/*      */ 
/* 1804 */     String query = "sp_tables ?, ?, ?, ?";
/*      */ 
/* 1806 */     CallableStatement cstmt = this.connection.prepareCall(syscall(catalog, query));
/*      */ 
/* 1808 */     cstmt.setString(1, processEscapes(tableNamePattern));
/* 1809 */     cstmt.setString(2, processEscapes(schemaPattern));
/* 1810 */     cstmt.setString(3, catalog);
/*      */ 
/* 1812 */     if (types == null) {
/* 1813 */       cstmt.setString(4, null);
/*      */     } else {
/* 1815 */       StringBuffer buf = new StringBuffer(64);
/*      */ 
/* 1817 */       buf.append('"');
/*      */ 
/* 1819 */       for (int i = 0; i < types.length; ++i) {
/* 1820 */         buf.append('\'').append(types[i]).append("',");
/*      */       }
/*      */ 
/* 1823 */       if (buf.length() > 1) {
/* 1824 */         buf.setLength(buf.length() - 1);
/*      */       }
/*      */ 
/* 1827 */       buf.append('"');
/* 1828 */       cstmt.setString(4, buf.toString());
/*      */     }
/*      */ 
/* 1831 */     JtdsResultSet rs = (JtdsResultSet)cstmt.executeQuery();
/* 1832 */     CachedResultSet rsTmp = new CachedResultSet((JtdsStatement)cstmt, colNames, colTypes);
/* 1833 */     rsTmp.moveToInsertRow();
/* 1834 */     int colCnt = rs.getMetaData().getColumnCount();
/*      */ 
/* 1838 */     while (rs.next()) {
/* 1839 */       for (int i = 1; i <= colCnt; ++i) {
/* 1840 */         rsTmp.updateObject(i, rs.getObject(i));
/*      */       }
/* 1842 */       rsTmp.insertRow();
/*      */     }
/* 1844 */     rsTmp.moveToCurrentRow();
/* 1845 */     rsTmp.setConcurrency(1007);
/* 1846 */     rs.close();
/* 1847 */     return rsTmp;
/*      */   }
/*      */ 
/*      */   public ResultSet getTableTypes()
/*      */     throws SQLException
/*      */   {
/* 1865 */     String sql = "select 'SYSTEM TABLE' TABLE_TYPE union select 'TABLE' TABLE_TYPE union select 'VIEW' TABLE_TYPE order by TABLE_TYPE";
/*      */ 
/* 1869 */     Statement stmt = this.connection.createStatement();
/*      */ 
/* 1871 */     return stmt.executeQuery(sql);
/*      */   }
/*      */ 
/*      */   public String getTimeDateFunctions()
/*      */     throws SQLException
/*      */   {
/* 1881 */     return "curdate,curtime,dayname,dayofmonth,dayofweek,dayofyear,hour,minute,month,monthname,now,quarter,timestampadd,timestampdiff,second,week,year";
/*      */   }
/*      */ 
/*      */   public ResultSet getTypeInfo()
/*      */     throws SQLException
/*      */   {
/* 1936 */     Statement s = this.connection.createStatement();
/*      */     JtdsResultSet rs;
/*      */     try
/*      */     {
/* 1940 */       rs = (JtdsResultSet)s.executeQuery("exec sp_datatype_info @ODBCVer=3");
/*      */     } catch (SQLException ex) {
/* 1942 */       s.close();
/* 1943 */       throw ex;
/*      */     }
/*      */     try
/*      */     {
/* 1947 */       ex = createTypeInfoResultSet(rs, this.connection.getUseLOBs());
/*      */ 
/* 1950 */       return ex; } finally { rs.close();
/*      */     }
/*      */   }
/*      */ 
/*      */   public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)
/*      */     throws SQLException
/*      */   {
/* 1995 */     String[] colNames = { "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "CLASS_NAME", "DATA_TYPE", "REMARKS", "BASE_TYPE" };
/*      */ 
/* 1999 */     int[] colTypes = { 12, 12, 12, 12, 4, 12, 5 };
/*      */ 
/* 2006 */     JtdsStatement dummyStmt = (JtdsStatement)this.connection.createStatement();
/* 2007 */     CachedResultSet rs = new CachedResultSet(dummyStmt, colNames, colTypes);
/* 2008 */     rs.setConcurrency(1007);
/* 2009 */     return rs;
/*      */   }
/*      */ 
/*      */   public String getURL()
/*      */     throws SQLException
/*      */   {
/* 2019 */     return this.connection.getURL();
/*      */   }
/*      */ 
/*      */   public String getUserName()
/*      */     throws SQLException
/*      */   {
/* 2029 */     Statement s = null;
/* 2030 */     ResultSet rs = null;
/* 2031 */     String result = "";
/*      */     try
/*      */     {
/* 2034 */       s = this.connection.createStatement();
/*      */ 
/* 2037 */       if (this.connection.getServerType() == 2)
/* 2038 */         rs = s.executeQuery("select suser_name()");
/*      */       else {
/* 2040 */         rs = s.executeQuery("select system_user");
/*      */       }
/*      */ 
/* 2043 */       if (!(rs.next())) {
/* 2044 */         throw new SQLException(Messages.get("error.dbmeta.nouser"), "HY000");
/*      */       }
/*      */ 
/* 2047 */       result = rs.getString(1);
/*      */     } finally {
/* 2049 */       if (rs != null) {
/* 2050 */         rs.close();
/*      */       }
/*      */ 
/* 2053 */       if (s != null) {
/* 2054 */         s.close();
/*      */       }
/*      */     }
/* 2057 */     return result;
/*      */   }
/*      */ 
/*      */   public ResultSet getVersionColumns(String catalog, String schema, String table)
/*      */     throws SQLException
/*      */   {
/* 2093 */     String[] colNames = { "SCOPE", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "PSEUDO_COLUMN" };
/*      */ 
/* 2097 */     int[] colTypes = { 5, 12, 4, 12, 4, 4, 5, 5 };
/*      */ 
/* 2102 */     String query = "sp_special_columns ?, ?, ?, ?, ?, ?, ?";
/*      */ 
/* 2104 */     CallableStatement s = this.connection.prepareCall(syscall(catalog, query));
/*      */ 
/* 2106 */     s.setString(1, table);
/* 2107 */     s.setString(2, schema);
/* 2108 */     s.setString(3, catalog);
/* 2109 */     s.setString(4, "V");
/* 2110 */     s.setString(5, "C");
/* 2111 */     s.setString(6, "O");
/* 2112 */     s.setInt(7, 3);
/*      */ 
/* 2114 */     JtdsResultSet rs = (JtdsResultSet)s.executeQuery();
/* 2115 */     CachedResultSet rsTmp = new CachedResultSet((JtdsStatement)s, colNames, colTypes);
/* 2116 */     rsTmp.moveToInsertRow();
/* 2117 */     int colCnt = rs.getMetaData().getColumnCount();
/*      */ 
/* 2121 */     while (rs.next()) {
/* 2122 */       for (int i = 1; i <= colCnt; ++i) {
/* 2123 */         rsTmp.updateObject(i, rs.getObject(i));
/*      */       }
/* 2125 */       rsTmp.insertRow();
/*      */     }
/* 2127 */     rsTmp.moveToCurrentRow();
/* 2128 */     rsTmp.setConcurrency(1007);
/* 2129 */     rs.close();
/* 2130 */     return rsTmp;
/*      */   }
/*      */ 
/*      */   public boolean isCatalogAtStart()
/*      */     throws SQLException
/*      */   {
/* 2141 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean isReadOnly()
/*      */     throws SQLException
/*      */   {
/* 2151 */     return false;
/*      */   }
/*      */ 
/*      */   public Connection getConnection()
/*      */     throws SQLException
/*      */   {
/* 2161 */     return this.connection;
/*      */   }
/*      */ 
/*      */   public boolean nullPlusNonNullIsNull()
/*      */     throws SQLException
/*      */   {
/* 2178 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean nullsAreSortedAtEnd()
/*      */     throws SQLException
/*      */   {
/* 2188 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean nullsAreSortedAtStart()
/*      */     throws SQLException
/*      */   {
/* 2198 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean nullsAreSortedHigh()
/*      */     throws SQLException
/*      */   {
/* 2208 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean nullsAreSortedLow()
/*      */     throws SQLException
/*      */   {
/* 2218 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean storesLowerCaseIdentifiers()
/*      */     throws SQLException
/*      */   {
/* 2229 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean storesLowerCaseQuotedIdentifiers()
/*      */     throws SQLException
/*      */   {
/* 2240 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean storesMixedCaseIdentifiers()
/*      */     throws SQLException
/*      */   {
/* 2251 */     setCaseSensitiveFlag();
/*      */ 
/* 2253 */     return (!(this.caseSensitive.booleanValue()));
/*      */   }
/*      */ 
/*      */   public boolean storesMixedCaseQuotedIdentifiers()
/*      */     throws SQLException
/*      */   {
/* 2264 */     setCaseSensitiveFlag();
/*      */ 
/* 2266 */     return (!(this.caseSensitive.booleanValue()));
/*      */   }
/*      */ 
/*      */   public boolean storesUpperCaseIdentifiers()
/*      */     throws SQLException
/*      */   {
/* 2277 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean storesUpperCaseQuotedIdentifiers()
/*      */     throws SQLException
/*      */   {
/* 2288 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsAlterTableWithAddColumn()
/*      */     throws SQLException
/*      */   {
/* 2301 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsAlterTableWithDropColumn()
/*      */     throws SQLException
/*      */   {
/* 2311 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsANSI92EntryLevelSQL()
/*      */     throws SQLException
/*      */   {
/* 2322 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsANSI92FullSQL()
/*      */     throws SQLException
/*      */   {
/* 2332 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsANSI92IntermediateSQL()
/*      */     throws SQLException
/*      */   {
/* 2342 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsCatalogsInDataManipulation()
/*      */     throws SQLException
/*      */   {
/* 2352 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsCatalogsInIndexDefinitions()
/*      */     throws SQLException
/*      */   {
/* 2362 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsCatalogsInPrivilegeDefinitions()
/*      */     throws SQLException
/*      */   {
/* 2372 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsCatalogsInProcedureCalls()
/*      */     throws SQLException
/*      */   {
/* 2382 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsCatalogsInTableDefinitions()
/*      */     throws SQLException
/*      */   {
/* 2392 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsColumnAliasing()
/*      */     throws SQLException
/*      */   {
/* 2406 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsConvert()
/*      */     throws SQLException
/*      */   {
/* 2416 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsConvert(int fromType, int toType)
/*      */     throws SQLException
/*      */   {
/* 2429 */     if (fromType == toType) {
/* 2430 */       return true;
/*      */     }
/*      */ 
/* 2433 */     switch (fromType)
/*      */     {
/*      */     case -7:
/*      */     case -6:
/*      */     case -5:
/*      */     case 2:
/*      */     case 3:
/*      */     case 4:
/*      */     case 5:
/*      */     case 6:
/*      */     case 7:
/*      */     case 8:
/*      */     case 91:
/*      */     case 92:
/*      */     case 93:
/* 2450 */       return ((toType != -1) && (toType != -4) && (toType != 2004) && (toType != 2005));
/*      */     case -3:
/*      */     case -2:
/* 2455 */       return ((toType != 6) && (toType != 7) && (toType != 8));
/*      */     case -4:
/*      */     case 2004:
/* 2461 */       return ((toType == -2) || (toType == -3) || (toType == 2004) || (toType == -4));
/*      */     case -1:
/*      */     case 2005:
/* 2467 */       return ((toType == 1) || (toType == 12) || (toType == 2005) || (toType == -1));
/*      */     case 0:
/*      */     case 1:
/*      */     case 12:
/* 2474 */       return true;
/*      */     case 1111:
/*      */     }
/*      */ 
/* 2479 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsCoreSQLGrammar()
/*      */     throws SQLException
/*      */   {
/* 2490 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsCorrelatedSubqueries()
/*      */     throws SQLException
/*      */   {
/* 2500 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsDataDefinitionAndDataManipulationTransactions()
/*      */     throws SQLException
/*      */   {
/* 2514 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsDataManipulationTransactionsOnly()
/*      */     throws SQLException
/*      */   {
/* 2525 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsDifferentTableCorrelationNames()
/*      */     throws SQLException
/*      */   {
/* 2536 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsExpressionsInOrderBy()
/*      */     throws SQLException
/*      */   {
/* 2546 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsExtendedSQLGrammar()
/*      */     throws SQLException
/*      */   {
/* 2556 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsFullOuterJoins()
/*      */     throws SQLException
/*      */   {
/* 2566 */     if (this.connection.getServerType() == 2)
/*      */     {
/* 2568 */       return (getDatabaseMajorVersion() >= 12);
/*      */     }
/* 2570 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsGroupBy()
/*      */     throws SQLException
/*      */   {
/* 2580 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsGroupByBeyondSelect()
/*      */     throws SQLException
/*      */   {
/* 2592 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsGroupByUnrelated()
/*      */     throws SQLException
/*      */   {
/* 2602 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsIntegrityEnhancementFacility()
/*      */     throws SQLException
/*      */   {
/* 2612 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsLikeEscapeClause()
/*      */     throws SQLException
/*      */   {
/* 2624 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsLimitedOuterJoins()
/*      */     throws SQLException
/*      */   {
/* 2636 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsMinimumSQLGrammar()
/*      */     throws SQLException
/*      */   {
/* 2646 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsMixedCaseIdentifiers()
/*      */     throws SQLException
/*      */   {
/* 2657 */     setCaseSensitiveFlag();
/*      */ 
/* 2659 */     return this.caseSensitive.booleanValue();
/*      */   }
/*      */ 
/*      */   public boolean supportsMixedCaseQuotedIdentifiers()
/*      */     throws SQLException
/*      */   {
/* 2670 */     setCaseSensitiveFlag();
/*      */ 
/* 2672 */     return this.caseSensitive.booleanValue();
/*      */   }
/*      */ 
/*      */   public boolean supportsMultipleResultSets()
/*      */     throws SQLException
/*      */   {
/* 2682 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsMultipleTransactions()
/*      */     throws SQLException
/*      */   {
/* 2693 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsNonNullableColumns()
/*      */     throws SQLException
/*      */   {
/* 2703 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsOpenCursorsAcrossCommit()
/*      */     throws SQLException
/*      */   {
/* 2715 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsOpenCursorsAcrossRollback()
/*      */     throws SQLException
/*      */   {
/* 2727 */     return (this.connection.getServerType() == 2);
/*      */   }
/*      */ 
/*      */   public boolean supportsOpenStatementsAcrossCommit()
/*      */     throws SQLException
/*      */   {
/* 2738 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsOpenStatementsAcrossRollback()
/*      */     throws SQLException
/*      */   {
/* 2749 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsOrderByUnrelated()
/*      */     throws SQLException
/*      */   {
/* 2759 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsOuterJoins()
/*      */     throws SQLException
/*      */   {
/* 2769 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsPositionedDelete()
/*      */     throws SQLException
/*      */   {
/* 2779 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsPositionedUpdate()
/*      */     throws SQLException
/*      */   {
/* 2789 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsSchemasInDataManipulation()
/*      */     throws SQLException
/*      */   {
/* 2799 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsSchemasInIndexDefinitions()
/*      */     throws SQLException
/*      */   {
/* 2809 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsSchemasInPrivilegeDefinitions()
/*      */     throws SQLException
/*      */   {
/* 2819 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsSchemasInProcedureCalls()
/*      */     throws SQLException
/*      */   {
/* 2829 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsSchemasInTableDefinitions()
/*      */     throws SQLException
/*      */   {
/* 2839 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsSelectForUpdate()
/*      */     throws SQLException
/*      */   {
/* 2852 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsStoredProcedures()
/*      */     throws SQLException
/*      */   {
/* 2863 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsSubqueriesInComparisons()
/*      */     throws SQLException
/*      */   {
/* 2874 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsSubqueriesInExists()
/*      */     throws SQLException
/*      */   {
/* 2885 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsSubqueriesInIns()
/*      */     throws SQLException
/*      */   {
/* 2896 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsSubqueriesInQuantifieds()
/*      */     throws SQLException
/*      */   {
/* 2907 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsTableCorrelationNames()
/*      */     throws SQLException
/*      */   {
/* 2917 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsTransactionIsolationLevel(int level)
/*      */     throws SQLException
/*      */   {
/* 2931 */     switch (level) {
/*      */     case 1:
/*      */     case 2:
/*      */     case 4:
/*      */     case 8:
/* 2936 */       return true;
/*      */     case 0:
/*      */     case 3:
/*      */     case 5:
/*      */     case 6:
/*      */     case 7: }
/* 2942 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsTransactions()
/*      */     throws SQLException
/*      */   {
/* 2955 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsUnion()
/*      */     throws SQLException
/*      */   {
/* 2965 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsUnionAll()
/*      */     throws SQLException
/*      */   {
/* 2975 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean usesLocalFilePerTable()
/*      */     throws SQLException
/*      */   {
/* 2986 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean usesLocalFiles()
/*      */     throws SQLException
/*      */   {
/* 2996 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsResultSetType(int type)
/*      */     throws SQLException
/*      */   {
/* 3048 */     return ((type >= 1003) && (type <= 1006));
/*      */   }
/*      */ 
/*      */   public boolean supportsResultSetConcurrency(int type, int concurrency)
/*      */     throws SQLException
/*      */   {
/* 3103 */     if (!(supportsResultSetType(type))) {
/* 3104 */       return false;
/*      */     }
/*      */ 
/* 3107 */     if ((concurrency < 1007) || (concurrency > 1010))
/*      */     {
/* 3109 */       return false;
/*      */     }
/*      */ 
/* 3112 */     return ((type != 1004) || (concurrency == 1007));
/*      */   }
/*      */ 
/*      */   public boolean ownUpdatesAreVisible(int type)
/*      */     throws SQLException
/*      */   {
/* 3125 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean ownDeletesAreVisible(int type)
/*      */     throws SQLException
/*      */   {
/* 3137 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean ownInsertsAreVisible(int type)
/*      */     throws SQLException
/*      */   {
/* 3149 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean othersUpdatesAreVisible(int type)
/*      */     throws SQLException
/*      */   {
/* 3162 */     return (type >= 1005);
/*      */   }
/*      */ 
/*      */   public boolean othersDeletesAreVisible(int type)
/*      */     throws SQLException
/*      */   {
/* 3175 */     return (type >= 1005);
/*      */   }
/*      */ 
/*      */   public boolean othersInsertsAreVisible(int type)
/*      */     throws SQLException
/*      */   {
/* 3188 */     return (type == 1006);
/*      */   }
/*      */ 
/*      */   public boolean updatesAreDetected(int type)
/*      */     throws SQLException
/*      */   {
/* 3202 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean deletesAreDetected(int type)
/*      */     throws SQLException
/*      */   {
/* 3215 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean insertsAreDetected(int type)
/*      */     throws SQLException
/*      */   {
/* 3228 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsBatchUpdates()
/*      */     throws SQLException
/*      */   {
/* 3239 */     return true;
/*      */   }
/*      */ 
/*      */   private void setCaseSensitiveFlag() throws SQLException {
/* 3243 */     if (this.caseSensitive == null) {
/* 3244 */       Statement s = this.connection.createStatement();
/* 3245 */       ResultSet rs = s.executeQuery("sp_server_info 16");
/*      */ 
/* 3247 */       rs.next();
/*      */ 
/* 3249 */       this.caseSensitive = (("MIXED".equalsIgnoreCase(rs.getString(3))) ? Boolean.FALSE : Boolean.TRUE);
/*      */ 
/* 3251 */       s.close();
/*      */     }
/*      */   }
/*      */ 
/*      */   public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern)
/*      */     throws SQLException
/*      */   {
/* 3260 */     String[] colNames = { "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "ATTR_NAME", "DATA_TYPE", "ATTR_TYPE_NAME", "ATTR_SIZE", "DECIMAL_DIGITS", "NUM_PREC_RADIX", "NULLABLE", "REMARKS", "ATTR_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATALOG", "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE" };
/*      */ 
/* 3271 */     int[] colTypes = { 12, 12, 12, 12, 4, 12, 4, 4, 4, 4, 12, 12, 4, 4, 4, 4, 12, 12, 12, 12, 5 };
/*      */ 
/* 3285 */     JtdsStatement dummyStmt = (JtdsStatement)this.connection.createStatement();
/* 3286 */     CachedResultSet rs = new CachedResultSet(dummyStmt, colNames, colTypes);
/* 3287 */     rs.setConcurrency(1007);
/*      */ 
/* 3289 */     return rs;
/*      */   }
/*      */ 
/*      */   public int getDatabaseMajorVersion()
/*      */     throws SQLException
/*      */   {
/* 3296 */     return this.connection.getDatabaseMajorVersion();
/*      */   }
/*      */ 
/*      */   public int getDatabaseMinorVersion()
/*      */     throws SQLException
/*      */   {
/* 3303 */     return this.connection.getDatabaseMinorVersion();
/*      */   }
/*      */ 
/*      */   public int getJDBCMajorVersion()
/*      */     throws SQLException
/*      */   {
/* 3310 */     return 3;
/*      */   }
/*      */ 
/*      */   public int getJDBCMinorVersion()
/*      */     throws SQLException
/*      */   {
/* 3317 */     return 0;
/*      */   }
/*      */ 
/*      */   public int getResultSetHoldability() throws SQLException {
/* 3321 */     return 1;
/*      */   }
/*      */ 
/*      */   public int getSQLStateType() throws SQLException {
/* 3325 */     return 1;
/*      */   }
/*      */ 
/*      */   public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern)
/*      */     throws SQLException
/*      */   {
/* 3332 */     String[] colNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "SUPERTABLE_NAME" };
/*      */ 
/* 3334 */     int[] colTypes = { 12, 12, 12, 12 };
/*      */ 
/* 3339 */     JtdsStatement dummyStmt = (JtdsStatement)this.connection.createStatement();
/* 3340 */     CachedResultSet rs = new CachedResultSet(dummyStmt, colNames, colTypes);
/* 3341 */     rs.setConcurrency(1007);
/* 3342 */     return rs;
/*      */   }
/*      */ 
/*      */   public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern)
/*      */     throws SQLException
/*      */   {
/* 3349 */     String[] colNames = { "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "SUPERTYPE_CAT", "SUPERTYPE_SCHEM", "SUPERTYPE_NAME" };
/*      */ 
/* 3352 */     int[] colTypes = { 12, 12, 12, 12, 12, 12 };
/*      */ 
/* 3358 */     JtdsStatement dummyStmt = (JtdsStatement)this.connection.createStatement();
/* 3359 */     CachedResultSet rs = new CachedResultSet(dummyStmt, colNames, colTypes);
/* 3360 */     rs.setConcurrency(1007);
/* 3361 */     return rs;
/*      */   }
/*      */ 
/*      */   public boolean locatorsUpdateCopy()
/*      */     throws SQLException
/*      */   {
/* 3372 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsGetGeneratedKeys()
/*      */     throws SQLException
/*      */   {
/* 3380 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsMultipleOpenResults()
/*      */     throws SQLException
/*      */   {
/* 3388 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsNamedParameters()
/*      */     throws SQLException
/*      */   {
/* 3396 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsResultSetHoldability(int param) throws SQLException
/*      */   {
/* 3401 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean supportsSavepoints()
/*      */     throws SQLException
/*      */   {
/* 3409 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean supportsStatementPooling()
/*      */     throws SQLException
/*      */   {
/* 3417 */     return true;
/*      */   }
/*      */ 
/*      */   private static String processEscapes(String pattern)
/*      */   {
/* 3427 */     char escChar = '\\';
/*      */ 
/* 3429 */     if ((pattern == null) || (pattern.indexOf(92) == -1)) {
/* 3430 */       return pattern;
/*      */     }
/*      */ 
/* 3433 */     int len = pattern.length();
/* 3434 */     StringBuffer buf = new StringBuffer(len + 10);
/*      */ 
/* 3436 */     for (int i = 0; i < len; ++i) {
/* 3437 */       if (pattern.charAt(i) != '\\') {
/* 3438 */         buf.append(pattern.charAt(i));
/* 3439 */       } else if (i < len - 1) {
/* 3440 */         buf.append('[');
/* 3441 */         buf.append(pattern.charAt(++i));
/* 3442 */         buf.append(']');
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3449 */     return buf.toString();
/*      */   }
/*      */ 
/*      */   private String syscall(String catalog, String call)
/*      */   {
/* 3460 */     StringBuffer sql = new StringBuffer(30 + call.length());
/* 3461 */     sql.append("{call ");
/* 3462 */     if (catalog != null) {
/* 3463 */       if (this.tdsVersion >= 3)
/* 3464 */         sql.append('[').append(catalog).append(']');
/*      */       else {
/* 3466 */         sql.append(catalog);
/*      */       }
/* 3468 */       sql.append("..");
/*      */     }
/* 3470 */     sql.append(call).append('}');
/* 3471 */     return sql.toString();
/*      */   }
/*      */ 
/*      */   private static void upperCaseColumnNames(JtdsResultSet results)
/*      */     throws SQLException
/*      */   {
/* 3485 */     ResultSetMetaData rsmd = results.getMetaData();
/* 3486 */     int cnt = rsmd.getColumnCount();
/*      */ 
/* 3488 */     for (int i = 1; i <= cnt; ++i) {
/* 3489 */       String name = rsmd.getColumnLabel(i);
/* 3490 */       if ((name != null) && (name.length() > 0))
/* 3491 */         results.setColLabel(i, name.toUpperCase());
/*      */     }
/*      */   }
/*      */ 
/*      */   private static CachedResultSet createTypeInfoResultSet(JtdsResultSet rs, boolean useLOBs) throws SQLException
/*      */   {
/* 3497 */     CachedResultSet result = new CachedResultSet(rs, false);
/* 3498 */     if (result.getMetaData().getColumnCount() > 18) {
/* 3499 */       result.setColumnCount(18);
/*      */     }
/* 3501 */     result.setColLabel(3, "PRECISION");
/* 3502 */     result.setColLabel(11, "FIXED_PREC_SCALE");
/* 3503 */     upperCaseColumnNames(result);
/* 3504 */     result.setConcurrency(1008);
/* 3505 */     result.moveToInsertRow();
/*      */ 
/* 3507 */     for (Iterator iter = getSortedTypes(rs, useLOBs).iterator(); iter.hasNext(); ) {
/* 3508 */       TypeInfo ti = (TypeInfo)iter.next();
/* 3509 */       ti.update(result);
/* 3510 */       result.insertRow();
/*      */     }
/*      */ 
/* 3513 */     result.moveToCurrentRow();
/* 3514 */     result.setConcurrency(1007);
/*      */ 
/* 3516 */     return result;
/*      */   }
/*      */ 
/*      */   private static Collection getSortedTypes(ResultSet rs, boolean useLOBs) throws SQLException {
/* 3520 */     List types = new ArrayList(40);
/*      */ 
/* 3522 */     while (rs.next()) {
/* 3523 */       types.add(new TypeInfo(rs, useLOBs));
/*      */     }
/*      */ 
/* 3526 */     Collections.sort(types);
/*      */ 
/* 3528 */     return types;
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.JtdsDatabaseMetaData
 * JD-Core Version:    0.5.3
 */