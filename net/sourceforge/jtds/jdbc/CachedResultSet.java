/*      */ package net.sourceforge.jtds.jdbc;
/*      */ 
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.math.BigDecimal;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.SQLWarning;
/*      */ import java.sql.Statement;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashSet;
/*      */ 
/*      */ public class CachedResultSet extends JtdsResultSet
/*      */ {
/*      */   protected boolean onInsertRow;
/*      */   protected ParamInfo[] insertRow;
/*      */   protected ParamInfo[] updateRow;
/*      */   protected boolean rowUpdated;
/*      */   protected boolean rowDeleted;
/*      */   protected int initialRowCnt;
/*      */   protected final boolean tempResultSet;
/*      */   protected final TdsCore cursorTds;
/*      */   protected final TdsCore updateTds;
/*      */   protected boolean isSybase;
/*      */   protected boolean sizeChanged;
/*      */   protected String sql;
/*      */   protected final String procName;
/*      */   protected final ParamInfo[] procedureParams;
/*      */   protected boolean isKeyed;
/*      */   protected String tableName;
/*      */   protected ConnectionJDBC2 connection;
/*      */ 
/*      */   CachedResultSet(JtdsStatement statement, String sql, String procName, ParamInfo[] procedureParams, int resultSetType, int concurrency)
/*      */     throws SQLException
/*      */   {
/*  121 */     super(statement, resultSetType, concurrency, null);
/*  122 */     this.connection = ((ConnectionJDBC2)statement.getConnection());
/*  123 */     this.cursorTds = statement.getTds();
/*  124 */     this.sql = sql;
/*  125 */     this.procName = procName;
/*  126 */     this.procedureParams = procedureParams;
/*  127 */     if ((resultSetType == 1003) && (concurrency != 1007) && (this.cursorName != null))
/*      */     {
/*  131 */       this.updateTds = new TdsCore(this.connection, statement.getMessages());
/*      */     }
/*      */     else this.updateTds = this.cursorTds;
/*      */ 
/*  135 */     this.isSybase = (2 == this.connection.getServerType());
/*  136 */     this.tempResultSet = false;
/*      */ 
/*  140 */     cursorCreate();
/*      */   }
/*      */ 
/*      */   CachedResultSet(JtdsStatement statement, String[] colName, int[] colType)
/*      */     throws SQLException
/*      */   {
/*  153 */     super(statement, 1003, 1008, null);
/*      */ 
/*  157 */     this.columns = new ColInfo[colName.length];
/*  158 */     for (int i = 0; i < colName.length; ++i) {
/*  159 */       ColInfo ci = new ColInfo();
/*  160 */       ci.name = colName[i];
/*  161 */       ci.realName = colName[i];
/*  162 */       ci.jdbcType = colType[i];
/*  163 */       ci.isCaseSensitive = false;
/*  164 */       ci.isIdentity = false;
/*  165 */       ci.isWriteable = false;
/*  166 */       ci.nullable = 2;
/*  167 */       ci.scale = 0;
/*  168 */       TdsData.fillInType(ci);
/*  169 */       this.columns[i] = ci;
/*      */     }
/*  171 */     this.columnCount = getColumnCount(this.columns);
/*  172 */     this.rowData = new ArrayList(1000);
/*  173 */     this.rowsInResult = 0;
/*  174 */     this.initialRowCnt = 0;
/*  175 */     this.pos = 0;
/*  176 */     this.tempResultSet = true;
/*  177 */     this.cursorName = null;
/*  178 */     this.cursorTds = null;
/*  179 */     this.updateTds = null;
/*  180 */     this.procName = null;
/*  181 */     this.procedureParams = null;
/*      */   }
/*      */ 
/*      */   CachedResultSet(JtdsResultSet rs, boolean load)
/*      */     throws SQLException
/*      */   {
/*  193 */     super((JtdsStatement)rs.getStatement(), rs.getStatement().getResultSetType(), rs.getStatement().getResultSetConcurrency(), null);
/*      */ 
/*  197 */     JtdsStatement stmt = (JtdsStatement)rs.getStatement();
/*      */ 
/*  202 */     if (this.concurrency != 1007) {
/*  203 */       this.concurrency = 1007;
/*  204 */       stmt.addWarning(new SQLWarning(Messages.get("warning.cursordowngraded", "CONCUR_READ_ONLY"), "01000"));
/*      */     }
/*      */ 
/*  212 */     if (this.resultSetType >= 1005) {
/*  213 */       this.resultSetType = 1004;
/*  214 */       stmt.addWarning(new SQLWarning(Messages.get("warning.cursordowngraded", "TYPE_SCROLL_INSENSITIVE"), "01000"));
/*      */     }
/*      */ 
/*  219 */     this.columns = rs.getColumns();
/*  220 */     this.columnCount = getColumnCount(this.columns);
/*  221 */     this.rowData = new ArrayList(1000);
/*  222 */     this.rowsInResult = 0;
/*  223 */     this.initialRowCnt = 0;
/*  224 */     this.pos = 0;
/*  225 */     this.tempResultSet = true;
/*  226 */     this.cursorName = null;
/*  227 */     this.cursorTds = null;
/*  228 */     this.updateTds = null;
/*  229 */     this.procName = null;
/*  230 */     this.procedureParams = null;
/*      */ 
/*  234 */     if (load) {
/*  235 */       while (rs.next()) {
/*  236 */         this.rowData.add(copyRow(rs.getCurrentRow()));
/*      */       }
/*  238 */       this.rowsInResult = this.rowData.size();
/*  239 */       this.initialRowCnt = this.rowsInResult;
/*      */     }
/*      */   }
/*      */ 
/*      */   CachedResultSet(JtdsStatement statement, ColInfo[] columns, Object[] data)
/*      */     throws SQLException
/*      */   {
/*  253 */     super(statement, 1003, 1007, null);
/*  254 */     this.columns = columns;
/*  255 */     this.columnCount = getColumnCount(columns);
/*  256 */     this.rowData = new ArrayList(1);
/*  257 */     this.rowsInResult = 1;
/*  258 */     this.initialRowCnt = 1;
/*  259 */     this.pos = 0;
/*  260 */     this.tempResultSet = true;
/*  261 */     this.cursorName = null;
/*  262 */     this.rowData.add(copyRow(data));
/*  263 */     this.cursorTds = null;
/*  264 */     this.updateTds = null;
/*  265 */     this.procName = null;
/*  266 */     this.procedureParams = null;
/*      */   }
/*      */ 
/*      */   void setConcurrency(int concurrency)
/*      */   {
/*  278 */     this.concurrency = concurrency;
/*      */   }
/*      */ 
/*      */   private void cursorCreate()
/*      */     throws SQLException
/*      */   {
/*  289 */     boolean isSelect = false;
/*  290 */     int requestedConcurrency = this.concurrency;
/*  291 */     int requestedType = this.resultSetType;
/*      */ 
/*  299 */     if ((this.cursorName == null) && (this.connection.getUseCursors()) && (this.resultSetType == 1003) && (this.concurrency == 1007))
/*      */     {
/*  305 */       this.cursorName = this.connection.getCursorName();
/*      */     }
/*      */ 
/*  310 */     if ((this.resultSetType != 1003) || (this.concurrency != 1007) || (this.cursorName != null))
/*      */     {
/*  317 */       String[] tmp = SQLParser.parse(this.sql, new ArrayList(), (ConnectionJDBC2)this.statement.getConnection(), true);
/*      */ 
/*  320 */       if ("select".equals(tmp[2])) {
/*  321 */         isSelect = true;
/*  322 */         if ((tmp[3] != null) && (tmp[3].length() > 0))
/*      */         {
/*  324 */           this.tableName = tmp[3];
/*      */         }
/*      */         else
/*  327 */           this.concurrency = 1007;
/*      */       }
/*      */       else
/*      */       {
/*  331 */         this.cursorName = null;
/*  332 */         this.concurrency = 1007;
/*  333 */         if (this.resultSetType != 1003) {
/*  334 */           this.resultSetType = 1004;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  343 */     if (this.cursorName != null)
/*      */     {
/*  347 */       StringBuffer cursorSQL = new StringBuffer(this.sql.length() + this.cursorName.length() + 128);
/*      */ 
/*  349 */       cursorSQL.append("DECLARE ").append(this.cursorName).append(" CURSOR FOR ");
/*      */ 
/*  355 */       ParamInfo[] parameters = this.procedureParams;
/*  356 */       if ((this.procedureParams != null) && (this.procedureParams.length > 0)) {
/*  357 */         parameters = new ParamInfo[this.procedureParams.length];
/*  358 */         int offset = cursorSQL.length();
/*  359 */         for (int i = 0; i < parameters.length; ++i)
/*      */         {
/*  361 */           parameters[i] = ((ParamInfo)this.procedureParams[i].clone());
/*  362 */           parameters[i].markerPos += offset;
/*      */         }
/*      */       }
/*  365 */       cursorSQL.append(this.sql);
/*  366 */       this.cursorTds.executeSQL(cursorSQL.toString(), null, parameters, false, this.statement.getQueryTimeout(), this.statement.getMaxRows(), this.statement.getMaxFieldSize(), true);
/*      */ 
/*  369 */       this.cursorTds.clearResponseQueue();
/*  370 */       this.cursorTds.getMessages().checkErrors();
/*      */ 
/*  374 */       cursorSQL.setLength(0);
/*  375 */       cursorSQL.append("\r\nOPEN ").append(this.cursorName);
/*  376 */       if ((this.fetchSize > 1) && (this.isSybase)) {
/*  377 */         cursorSQL.append("\r\nSET CURSOR ROWS ").append(this.fetchSize);
/*  378 */         cursorSQL.append(" FOR ").append(this.cursorName);
/*      */       }
/*  380 */       cursorSQL.append("\r\nFETCH ").append(this.cursorName);
/*  381 */       this.cursorTds.executeSQL(cursorSQL.toString(), null, null, false, this.statement.getQueryTimeout(), this.statement.getMaxRows(), this.statement.getMaxFieldSize(), true);
/*      */ 
/*  387 */       while ((!(this.cursorTds.getMoreResults())) && (!(this.cursorTds.isEndOfResponse())));
/*  389 */       if (!(this.cursorTds.isResultSet()))
/*      */       {
/*  391 */         SQLException ex = new SQLException(Messages.get("error.statement.noresult"), "24000");
/*      */ 
/*  393 */         ex.setNextException(this.statement.getMessages().exceptions);
/*  394 */         throw ex;
/*      */       }
/*  396 */       this.columns = this.cursorTds.getColumns();
/*  397 */       if ((this.connection.getServerType() == 1) && 
/*  402 */         (this.columns.length > 0)) {
/*  403 */         this.columns[(this.columns.length - 1)].isHidden = true;
/*      */       }
/*      */ 
/*  406 */       this.columnCount = getColumnCount(this.columns);
/*  407 */       this.rowsInResult = ((this.cursorTds.isDataInResultSet()) ? 1 : 0);
/*      */     }
/*      */     else
/*      */     {
/*      */       SQLException ex;
/*  412 */       if ((isSelect) && (((this.concurrency != 1007) || (this.resultSetType >= 1005))))
/*      */       {
/*  419 */         this.cursorTds.executeSQL(this.sql + " FOR BROWSE", null, this.procedureParams, false, this.statement.getQueryTimeout(), this.statement.getMaxRows(), this.statement.getMaxFieldSize(), true);
/*      */ 
/*  423 */         while ((!(this.cursorTds.getMoreResults())) && (!(this.cursorTds.isEndOfResponse())));
/*  424 */         if (!(this.cursorTds.isResultSet()))
/*      */         {
/*  426 */           ex = new SQLException(Messages.get("error.statement.noresult"), "24000");
/*      */ 
/*  428 */           ex.setNextException(this.statement.getMessages().exceptions);
/*  429 */           throw ex;
/*      */         }
/*  431 */         this.columns = this.cursorTds.getColumns();
/*  432 */         this.columnCount = getColumnCount(this.columns);
/*  433 */         this.rowData = new ArrayList(1000);
/*      */ 
/*  437 */         cacheResultSetRows();
/*  438 */         this.rowsInResult = this.rowData.size();
/*  439 */         this.initialRowCnt = this.rowsInResult;
/*  440 */         this.pos = 0;
/*      */ 
/*  446 */         if (!(isCursorUpdateable()))
/*      */         {
/*  448 */           this.concurrency = 1007;
/*  449 */           if (this.resultSetType != 1003) {
/*  450 */             this.resultSetType = 1004;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  457 */         this.cursorTds.executeSQL(this.sql, this.procName, this.procedureParams, false, this.statement.getQueryTimeout(), this.statement.getMaxRows(), this.statement.getMaxFieldSize(), true);
/*      */ 
/*  460 */         while ((!(this.cursorTds.getMoreResults())) && (!(this.cursorTds.isEndOfResponse())));
/*  462 */         if (!(this.cursorTds.isResultSet()))
/*      */         {
/*  464 */           ex = new SQLException(Messages.get("error.statement.noresult"), "24000");
/*      */ 
/*  466 */           ex.setNextException(this.statement.getMessages().exceptions);
/*  467 */           throw ex;
/*      */         }
/*  469 */         this.columns = this.cursorTds.getColumns();
/*  470 */         this.columnCount = getColumnCount(this.columns);
/*  471 */         this.rowData = new ArrayList(1000);
/*      */ 
/*  475 */         cacheResultSetRows();
/*  476 */         this.rowsInResult = this.rowData.size();
/*  477 */         this.initialRowCnt = this.rowsInResult;
/*  478 */         this.pos = 0;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  484 */     if (this.concurrency < requestedConcurrency) {
/*  485 */       this.statement.addWarning(new SQLWarning(Messages.get("warning.cursordowngraded", "CONCUR_READ_ONLY"), "01000"));
/*      */     }
/*      */ 
/*  489 */     if (this.resultSetType < requestedType) {
/*  490 */       this.statement.addWarning(new SQLWarning(Messages.get("warning.cursordowngraded", "TYPE_SCROLL_INSENSITIVE"), "01000"));
/*      */     }
/*      */ 
/*  497 */     this.statement.getMessages().checkErrors();
/*      */   }
/*      */ 
/*      */   boolean isCursorUpdateable()
/*      */     throws SQLException
/*      */   {
/*  524 */     this.isKeyed = false;
/*  525 */     HashSet tableSet = new HashSet();
/*  526 */     for (int i = 0; i < this.columns.length; ++i) {
/*  527 */       ColInfo ci = this.columns[i];
/*  528 */       if (ci.isKey)
/*      */       {
/*  531 */         if (("text".equals(ci.sqlType)) || ("image".equals(ci.sqlType)))
/*  532 */           ci.isKey = false;
/*      */         else {
/*  534 */           this.isKeyed = true;
/*      */         }
/*      */       }
/*  537 */       else if (ci.isIdentity)
/*      */       {
/*  539 */         ci.isKey = true;
/*  540 */         this.isKeyed = true;
/*      */       }
/*  542 */       StringBuffer key = new StringBuffer();
/*  543 */       if ((ci.tableName != null) && (ci.tableName.length() > 0)) {
/*  544 */         key.setLength(0);
/*  545 */         if (ci.catalog != null) {
/*  546 */           key.append(ci.catalog).append('.');
/*  547 */           if (ci.schema == null) {
/*  548 */             key.append('.');
/*      */           }
/*      */         }
/*  551 */         if (ci.schema != null) {
/*  552 */           key.append(ci.schema).append('.');
/*      */         }
/*  554 */         key.append(ci.tableName);
/*  555 */         this.tableName = key.toString();
/*  556 */         tableSet.add(this.tableName);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  564 */     if ((this.tableName.startsWith("#")) && (this.cursorTds.getTdsVersion() >= 3)) {
/*  565 */       StringBuffer sql = new StringBuffer(1024);
/*  566 */       sql.append("SELECT ");
/*  567 */       for (int i = 1; i <= 8; ++i) {
/*  568 */         if (i > 1) {
/*  569 */           sql.append(',');
/*      */         }
/*  571 */         sql.append("index_col('tempdb..").append(this.tableName);
/*  572 */         sql.append("', indid, ").append(i).append(')');
/*      */       }
/*  574 */       sql.append(" FROM tempdb..sysindexes WHERE id = object_id('tempdb..");
/*  575 */       sql.append(this.tableName).append("') AND indid > 0 AND ");
/*  576 */       sql.append("(status & 2048) = 2048");
/*  577 */       this.cursorTds.executeSQL(sql.toString(), null, null, false, 0, this.statement.getMaxRows(), this.statement.getMaxFieldSize(), true);
/*      */ 
/*  579 */       while ((!(this.cursorTds.getMoreResults())) && (!(this.cursorTds.isEndOfResponse())));
/*  581 */       if ((this.cursorTds.isResultSet()) && (this.cursorTds.getNextRow()))
/*      */       {
/*  583 */         Object[] row = this.cursorTds.getRowData();
/*  584 */         for (int i = 0; i < row.length; ++i) {
/*  585 */           String name = (String)row[i];
/*  586 */           if (name != null) {
/*  587 */             for (int c = 0; c < this.columns.length; ++c) {
/*  588 */               if ((this.columns[c].realName == null) || (!(this.columns[c].realName.equalsIgnoreCase(name))))
/*      */                 continue;
/*  590 */               this.columns[c].isKey = true;
/*  591 */               this.isKeyed = true;
/*  592 */               break;
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  599 */       this.statement.getMessages().checkErrors();
/*      */     }
/*      */ 
/*  605 */     if (!(this.isKeyed)) {
/*  606 */       for (i = 0; i < this.columns.length; ++i) {
/*  607 */         String type = this.columns[i].sqlType;
/*  608 */         if (("ntext".equals(type)) || ("text".equals(type)) || ("image".equals(type)) || ("timestamp".equals(type)) || (this.columns[i].tableName == null))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  613 */         this.columns[i].isKey = true;
/*  614 */         this.isKeyed = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  619 */     return ((tableSet.size() == 1) && (this.isKeyed));
/*      */   }
/*      */ 
/*      */   private boolean cursorFetch(int rowNum)
/*      */     throws SQLException
/*      */   {
/*  631 */     this.rowUpdated = false;
/*      */ 
/*  633 */     if (this.cursorName != null)
/*      */     {
/*  637 */       if (!(this.cursorTds.getNextRow()))
/*      */       {
/*  639 */         StringBuffer sql = new StringBuffer(128);
/*  640 */         if ((this.isSybase) && (this.sizeChanged))
/*      */         {
/*  642 */           sql.append("SET CURSOR ROWS ").append(this.fetchSize);
/*  643 */           sql.append(" FOR ").append(this.cursorName);
/*  644 */           sql.append("\r\n");
/*      */         }
/*  646 */         sql.append("FETCH ").append(this.cursorName);
/*      */ 
/*  648 */         this.cursorTds.executeSQL(sql.toString(), null, null, false, this.statement.getQueryTimeout(), this.statement.getMaxRows(), this.statement.getMaxFieldSize(), true);
/*      */ 
/*  651 */         while ((!(this.cursorTds.getMoreResults())) && (!(this.cursorTds.isEndOfResponse())));
/*  653 */         this.sizeChanged = false;
/*      */ 
/*  655 */         if ((!(this.cursorTds.isResultSet())) || (!(this.cursorTds.getNextRow()))) {
/*  656 */           this.pos = -1;
/*  657 */           this.currentRow = null;
/*  658 */           this.statement.getMessages().checkErrors();
/*  659 */           return false;
/*      */         }
/*      */       }
/*  662 */       this.currentRow = this.statement.getTds().getRowData();
/*  663 */       this.pos += 1;
/*  664 */       this.rowsInResult = this.pos;
/*  665 */       this.statement.getMessages().checkErrors();
/*      */ 
/*  667 */       return (this.currentRow != null);
/*      */     }
/*      */ 
/*  673 */     if (this.rowsInResult == 0) {
/*  674 */       this.pos = 0;
/*  675 */       this.currentRow = null;
/*  676 */       return false;
/*      */     }
/*  678 */     if (rowNum == this.pos)
/*      */     {
/*  681 */       return true;
/*      */     }
/*  683 */     if (rowNum < 1) {
/*  684 */       this.currentRow = null;
/*  685 */       this.pos = 0;
/*  686 */       return false;
/*      */     }
/*  688 */     if (rowNum > this.rowsInResult) {
/*  689 */       this.currentRow = null;
/*  690 */       this.pos = -1;
/*  691 */       return false;
/*      */     }
/*  693 */     this.pos = rowNum;
/*  694 */     this.currentRow = ((Object[])this.rowData.get(rowNum - 1));
/*  695 */     this.rowDeleted = (this.currentRow == null);
/*      */ 
/*  697 */     if ((this.resultSetType >= 1005) && (this.currentRow != null))
/*      */     {
/*  699 */       refreshRow();
/*      */     }
/*      */ 
/*  702 */     return true;
/*      */   }
/*      */ 
/*      */   private void cursorClose()
/*      */     throws SQLException
/*      */   {
/*  709 */     if (this.cursorName != null) {
/*  710 */       this.statement.clearWarnings();
/*      */       String sql;
/*  712 */       if (this.isSybase) {
/*  713 */         sql = "CLOSE " + this.cursorName + "\r\nDEALLOCATE CURSOR " + this.cursorName;
/*      */       }
/*      */       else {
/*  716 */         sql = "CLOSE " + this.cursorName + "\r\nDEALLOCATE " + this.cursorName;
/*      */       }
/*      */ 
/*  719 */       this.cursorTds.submitSQL(sql);
/*      */     }
/*  721 */     this.rowData = null;
/*      */   }
/*      */ 
/*      */   protected static ParamInfo buildParameter(int pos, ColInfo info, Object value, boolean isUnicode)
/*      */     throws SQLException
/*      */   {
/*  735 */     int length = 0;
/*  736 */     if (value instanceof String) {
/*  737 */       length = ((String)value).length();
/*      */     }
/*  739 */     else if (value instanceof byte[]) {
/*  740 */       length = ((byte[])value).length;
/*      */     }
/*  742 */     else if (value instanceof BlobImpl) {
/*  743 */       BlobImpl blob = (BlobImpl)value;
/*  744 */       value = blob.getBinaryStream();
/*  745 */       length = (int)blob.length();
/*      */     }
/*  747 */     else if (value instanceof ClobImpl) {
/*  748 */       ClobImpl clob = (ClobImpl)value;
/*  749 */       value = clob.getCharacterStream();
/*  750 */       length = (int)clob.length();
/*      */     }
/*  752 */     ParamInfo param = new ParamInfo(info, null, value, length);
/*  753 */     param.isUnicode = (("nvarchar".equals(info.sqlType)) || ("nchar".equals(info.sqlType)) || ("ntext".equals(info.sqlType)) || (isUnicode));
/*      */ 
/*  757 */     param.markerPos = pos;
/*      */ 
/*  759 */     return param;
/*      */   }
/*      */ 
/*      */   protected Object setColValue(int colIndex, int jdbcType, Object value, int length)
/*      */     throws SQLException
/*      */   {
/*  772 */     value = super.setColValue(colIndex, jdbcType, value, length);
/*      */ 
/*  774 */     if ((!(this.onInsertRow)) && (this.currentRow == null)) {
/*  775 */       throw new SQLException(Messages.get("error.resultset.norow"), "24000");
/*      */     }
/*  777 */     --colIndex;
/*      */ 
/*  779 */     ColInfo ci = this.columns[colIndex];
/*  780 */     boolean isUnicode = TdsData.isUnicode(ci);
/*      */     ParamInfo pi;
/*  782 */     if (this.onInsertRow) {
/*  783 */       pi = this.insertRow[colIndex];
/*  784 */       if (pi == null) {
/*  785 */         pi = new ParamInfo(-1, isUnicode);
/*  786 */         pi.collation = ci.collation;
/*  787 */         pi.charsetInfo = ci.charsetInfo;
/*  788 */         this.insertRow[colIndex] = pi;
/*      */       }
/*      */     } else {
/*  791 */       if (this.updateRow == null) {
/*  792 */         this.updateRow = new ParamInfo[this.columnCount];
/*      */       }
/*  794 */       pi = this.updateRow[colIndex];
/*  795 */       if (pi == null) {
/*  796 */         pi = new ParamInfo(-1, isUnicode);
/*  797 */         pi.collation = ci.collation;
/*  798 */         pi.charsetInfo = ci.charsetInfo;
/*  799 */         this.updateRow[colIndex] = pi;
/*      */       }
/*      */     }
/*      */ 
/*  803 */     if (value == null) {
/*  804 */       pi.value = null;
/*  805 */       pi.length = 0;
/*  806 */       pi.jdbcType = ci.jdbcType;
/*  807 */       pi.isSet = true;
/*  808 */       if ((pi.jdbcType == 2) || (pi.jdbcType == 3))
/*  809 */         pi.scale = 10;
/*      */       else
/*  811 */         pi.scale = 0;
/*      */     }
/*      */     else {
/*  814 */       pi.value = value;
/*  815 */       pi.length = length;
/*  816 */       pi.isSet = true;
/*  817 */       pi.jdbcType = jdbcType;
/*  818 */       if (pi.value instanceof BigDecimal)
/*  819 */         pi.scale = ((BigDecimal)pi.value).scale();
/*      */       else {
/*  821 */         pi.scale = 0;
/*      */       }
/*      */     }
/*      */ 
/*  825 */     return value;
/*      */   }
/*      */ 
/*      */   ParamInfo[] buildWhereClause(StringBuffer sql, ArrayList params, boolean select)
/*      */     throws SQLException
/*      */   {
/*  843 */     sql.append(" WHERE ");
/*  844 */     if (this.cursorName != null)
/*      */     {
/*  848 */       sql.append(" CURRENT OF ").append(this.cursorName);
/*      */     } else {
/*  850 */       int count = 0;
/*  851 */       for (int i = 0; i < this.columns.length; ++i) {
/*  852 */         if (this.currentRow[i] == null) {
/*  853 */           if (("text".equals(this.columns[i].sqlType)) || ("ntext".equals(this.columns[i].sqlType)) || ("image".equals(this.columns[i].sqlType)) || (this.columns[i].tableName == null)) {
/*      */             continue;
/*      */           }
/*      */ 
/*  857 */           if (count > 0) {
/*  858 */             sql.append(" AND ");
/*      */           }
/*  860 */           sql.append(this.columns[i].realName);
/*  861 */           sql.append(" IS NULL");
/*      */         }
/*  864 */         else if ((this.isKeyed) && (select))
/*      */         {
/*  866 */           if (this.columns[i].isKey) {
/*  867 */             if (count > 0) {
/*  868 */               sql.append(" AND ");
/*      */             }
/*  870 */             sql.append(this.columns[i].realName);
/*  871 */             sql.append("=?");
/*  872 */             ++count;
/*  873 */             params.add(buildParameter(sql.length() - 1, this.columns[i], this.currentRow[i], this.connection.getUseUnicode()));
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  879 */           if (("text".equals(this.columns[i].sqlType)) || ("ntext".equals(this.columns[i].sqlType)) || ("image".equals(this.columns[i].sqlType)) || (this.columns[i].tableName == null)) {
/*      */             continue;
/*      */           }
/*      */ 
/*  883 */           if (count > 0) {
/*  884 */             sql.append(" AND ");
/*      */           }
/*  886 */           sql.append(this.columns[i].realName);
/*  887 */           sql.append("=?");
/*  888 */           ++count;
/*  889 */           params.add(buildParameter(sql.length() - 1, this.columns[i], this.currentRow[i], this.connection.getUseUnicode()));
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  896 */     return ((ParamInfo[])params.toArray(new ParamInfo[params.size()]));
/*      */   }
/*      */ 
/*      */   protected void refreshKeyedRows()
/*      */     throws SQLException
/*      */   {
/*  912 */     StringBuffer sql = new StringBuffer(100 + this.columns.length * 10);
/*  913 */     sql.append("SELECT ");
/*  914 */     int count = 0;
/*  915 */     for (int i = 0; i < this.columns.length; ++i) {
/*  916 */       if ((!(this.columns[i].isKey)) && (this.columns[i].tableName != null)) {
/*  917 */         if (count > 0) {
/*  918 */           sql.append(',');
/*      */         }
/*  920 */         sql.append(this.columns[i].realName);
/*  921 */         ++count;
/*      */       }
/*      */     }
/*  924 */     if (count == 0)
/*      */     {
/*  926 */       return;
/*      */     }
/*  928 */     sql.append(" FROM ");
/*  929 */     sql.append(this.tableName);
/*      */ 
/*  933 */     ArrayList params = new ArrayList();
/*  934 */     buildWhereClause(sql, params, true);
/*  935 */     ParamInfo[] parameters = (ParamInfo[])params.toArray(new ParamInfo[params.size()]);
/*      */ 
/*  939 */     TdsCore tds = this.statement.getTds();
/*  940 */     tds.executeSQL(sql.toString(), null, parameters, false, 0, this.statement.getMaxRows(), this.statement.getMaxFieldSize(), true);
/*      */ 
/*  942 */     if (!(tds.isEndOfResponse()))
/*  943 */       if ((tds.getMoreResults()) && (tds.getNextRow()))
/*      */       {
/*  945 */         Object[] col = tds.getRowData();
/*  946 */         count = 0;
/*  947 */         for (int i = 0; i < this.columns.length; ++i)
/*  948 */           if (!(this.columns[i].isKey))
/*  949 */             this.currentRow[i] = col[(count++)];
/*      */       }
/*      */       else
/*      */       {
/*  953 */         this.currentRow = null;
/*      */       }
/*      */     else {
/*  956 */       this.currentRow = null;
/*      */     }
/*  958 */     tds.clearResponseQueue();
/*  959 */     this.statement.getMessages().checkErrors();
/*  960 */     if (this.currentRow == null) {
/*  961 */       this.rowData.set(this.pos - 1, null);
/*  962 */       this.rowDeleted = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void refreshReRead()
/*      */     throws SQLException
/*      */   {
/*  974 */     int savePos = this.pos;
/*  975 */     cursorCreate();
/*  976 */     absolute(savePos);
/*      */   }
/*      */ 
/*      */   public void setFetchSize(int size)
/*      */     throws SQLException
/*      */   {
/*  984 */     this.sizeChanged = (size != this.fetchSize);
/*  985 */     super.setFetchSize(size);
/*      */   }
/*      */ 
/*      */   public void afterLast() throws SQLException {
/*  989 */     checkOpen();
/*  990 */     checkScrollable();
/*  991 */     if (this.pos != -1)
/*  992 */       cursorFetch(this.rowsInResult + 1);
/*      */   }
/*      */ 
/*      */   public void beforeFirst() throws SQLException
/*      */   {
/*  997 */     checkOpen();
/*  998 */     checkScrollable();
/*      */ 
/* 1000 */     if (this.pos != 0)
/* 1001 */       cursorFetch(0);
/*      */   }
/*      */ 
/*      */   public void cancelRowUpdates() throws SQLException
/*      */   {
/* 1006 */     checkOpen();
/* 1007 */     checkUpdateable();
/* 1008 */     if (this.onInsertRow) {
/* 1009 */       throw new SQLException(Messages.get("error.resultset.insrow"), "24000");
/*      */     }
/* 1011 */     if (this.updateRow != null) {
/* 1012 */       this.rowUpdated = false;
/* 1013 */       for (int i = 0; i < this.updateRow.length; ++i)
/* 1014 */         if (this.updateRow[i] != null)
/* 1015 */           this.updateRow[i].clearInValue();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void close()
/*      */     throws SQLException
/*      */   {
/* 1022 */     if (this.closed) return;
/*      */     try {
/* 1024 */       cursorClose();
/*      */     } finally {
/* 1026 */       this.closed = true;
/* 1027 */       this.statement = null;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void deleteRow() throws SQLException
/*      */   {
/* 1033 */     checkOpen();
/* 1034 */     checkUpdateable();
/*      */ 
/* 1036 */     if (this.currentRow == null) {
/* 1037 */       throw new SQLException(Messages.get("error.resultset.norow"), "24000");
/*      */     }
/*      */ 
/* 1040 */     if (this.onInsertRow) {
/* 1041 */       throw new SQLException(Messages.get("error.resultset.insrow"), "24000");
/*      */     }
/*      */ 
/* 1047 */     StringBuffer sql = new StringBuffer(128);
/* 1048 */     ArrayList params = new ArrayList();
/* 1049 */     sql.append("DELETE FROM ");
/* 1050 */     sql.append(this.tableName);
/*      */ 
/* 1054 */     ParamInfo[] parameters = buildWhereClause(sql, params, false);
/*      */ 
/* 1058 */     this.updateTds.executeSQL(sql.toString(), null, parameters, false, 0, this.statement.getMaxRows(), this.statement.getMaxFieldSize(), true);
/*      */ 
/* 1060 */     int updateCount = 0;
/* 1061 */     while (!(this.updateTds.isEndOfResponse())) {
/* 1062 */       if ((this.updateTds.getMoreResults()) || 
/* 1063 */         (!(this.updateTds.isUpdateCount()))) continue;
/* 1064 */       updateCount = this.updateTds.getUpdateCount();
/*      */     }
/*      */ 
/* 1068 */     this.updateTds.clearResponseQueue();
/* 1069 */     this.statement.getMessages().checkErrors();
/* 1070 */     if (updateCount == 0)
/*      */     {
/* 1072 */       throw new SQLException(Messages.get("error.resultset.deletefail"), "24000");
/*      */     }
/* 1074 */     this.rowDeleted = true;
/* 1075 */     this.currentRow = null;
/* 1076 */     if (this.resultSetType == 1003)
/*      */       return;
/* 1078 */     this.rowData.set(this.pos - 1, null);
/*      */   }
/*      */ 
/*      */   public void insertRow() throws SQLException
/*      */   {
/* 1083 */     checkOpen();
/*      */ 
/* 1085 */     checkUpdateable();
/*      */ 
/* 1087 */     if (!(this.onInsertRow)) {
/* 1088 */       throw new SQLException(Messages.get("error.resultset.notinsrow"), "24000");
/*      */     }
/*      */ 
/* 1091 */     if (!(this.tempResultSet))
/*      */     {
/* 1095 */       StringBuffer sql = new StringBuffer(128);
/* 1096 */       ArrayList params = new ArrayList();
/* 1097 */       sql.append("INSERT INTO ");
/* 1098 */       sql.append(this.tableName);
/* 1099 */       int sqlLen = sql.length();
/*      */ 
/* 1103 */       sql.append(" (");
/* 1104 */       int count = 0;
/* 1105 */       for (int i = 0; i < this.columnCount; ++i) {
/* 1106 */         if (this.insertRow[i] != null) {
/* 1107 */           if (count > 0) {
/* 1108 */             sql.append(", ");
/*      */           }
/* 1110 */           sql.append(this.columns[i].realName);
/* 1111 */           ++count;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1117 */       sql.append(") VALUES(");
/* 1118 */       count = 0;
/* 1119 */       for (i = 0; i < this.columnCount; ++i) {
/* 1120 */         if (this.insertRow[i] != null) {
/* 1121 */           if (count > 0) {
/* 1122 */             sql.append(", ");
/*      */           }
/* 1124 */           sql.append('?');
/* 1125 */           this.insertRow[i].markerPos = (sql.length() - 1);
/* 1126 */           params.add(this.insertRow[i]);
/* 1127 */           ++count;
/*      */         }
/*      */       }
/* 1130 */       sql.append(')');
/* 1131 */       if (count == 0)
/*      */       {
/* 1133 */         sql.setLength(sqlLen);
/* 1134 */         if (this.isSybase)
/* 1135 */           sql.append(" VALUES()");
/*      */         else {
/* 1137 */           sql.append(" DEFAULT VALUES");
/*      */         }
/*      */       }
/* 1140 */       ParamInfo[] parameters = (ParamInfo[])params.toArray(new ParamInfo[params.size()]);
/*      */ 
/* 1144 */       this.updateTds.executeSQL(sql.toString(), null, parameters, false, 0, this.statement.getMaxRows(), this.statement.getMaxFieldSize(), true);
/*      */ 
/* 1146 */       int updateCount = 0;
/* 1147 */       while (!(this.updateTds.isEndOfResponse())) {
/* 1148 */         if ((this.updateTds.getMoreResults()) || 
/* 1149 */           (!(this.updateTds.isUpdateCount()))) continue;
/* 1150 */         updateCount = this.updateTds.getUpdateCount();
/*      */       }
/*      */ 
/* 1154 */       this.updateTds.clearResponseQueue();
/* 1155 */       this.statement.getMessages().checkErrors();
/* 1156 */       if (updateCount < 1)
/*      */       {
/* 1159 */         throw new SQLException(Messages.get("error.resultset.insertfail"), "24000");
/*      */       }
/*      */     }
/*      */ 
/* 1163 */     if ((this.resultSetType >= 1005) || ((this.resultSetType == 1003) && (this.cursorName == null)))
/*      */     {
/* 1169 */       ConnectionJDBC2 con = (ConnectionJDBC2)this.statement.getConnection();
/* 1170 */       Object[] row = newRow();
/* 1171 */       for (int i = 0; i < this.insertRow.length; ++i) {
/* 1172 */         if (this.insertRow[i] != null) {
/* 1173 */           row[i] = Support.convert(con, this.insertRow[i].value, this.columns[i].jdbcType, con.getCharset());
/*      */         }
/*      */       }
/*      */ 
/* 1177 */       this.rowData.add(row);
/*      */     }
/* 1179 */     this.rowsInResult += 1;
/* 1180 */     this.initialRowCnt += 1;
/*      */ 
/* 1184 */     for (int i = 0; (this.insertRow != null) && (i < this.insertRow.length); ++i)
/* 1185 */       if (this.insertRow[i] != null)
/* 1186 */         this.insertRow[i].clearInValue();
/*      */   }
/*      */ 
/*      */   public void moveToCurrentRow()
/*      */     throws SQLException
/*      */   {
/* 1192 */     checkOpen();
/* 1193 */     checkUpdateable();
/* 1194 */     this.insertRow = null;
/* 1195 */     this.onInsertRow = false;
/*      */   }
/*      */ 
/*      */   public void moveToInsertRow() throws SQLException
/*      */   {
/* 1200 */     checkOpen();
/* 1201 */     checkUpdateable();
/* 1202 */     this.insertRow = new ParamInfo[this.columnCount];
/* 1203 */     this.onInsertRow = true;
/*      */   }
/*      */ 
/*      */   public void refreshRow() throws SQLException {
/* 1207 */     checkOpen();
/*      */ 
/* 1209 */     if (this.onInsertRow) {
/* 1210 */       throw new SQLException(Messages.get("error.resultset.insrow"), "24000");
/*      */     }
/*      */ 
/* 1216 */     if (this.concurrency != 1007) {
/* 1217 */       cancelRowUpdates();
/* 1218 */       this.rowUpdated = false;
/*      */     }
/* 1220 */     if ((this.resultSetType == 1003) || (this.currentRow == null))
/*      */     {
/* 1223 */       return;
/*      */     }
/*      */ 
/* 1232 */     if (this.isKeyed)
/*      */     {
/* 1234 */       refreshKeyedRows();
/*      */     }
/*      */     else
/* 1237 */       refreshReRead();
/*      */   }
/*      */ 
/*      */   public void updateRow() throws SQLException
/*      */   {
/* 1242 */     checkOpen();
/* 1243 */     checkUpdateable();
/*      */ 
/* 1245 */     this.rowUpdated = false;
/* 1246 */     this.rowDeleted = false;
/* 1247 */     if (this.currentRow == null) {
/* 1248 */       throw new SQLException(Messages.get("error.resultset.norow"), "24000");
/*      */     }
/*      */ 
/* 1251 */     if (this.onInsertRow) {
/* 1252 */       throw new SQLException(Messages.get("error.resultset.insrow"), "24000");
/*      */     }
/*      */ 
/* 1255 */     if (this.updateRow == null)
/*      */     {
/* 1257 */       return;
/*      */     }
/* 1259 */     boolean keysChanged = false;
/*      */ 
/* 1263 */     StringBuffer sql = new StringBuffer(128);
/* 1264 */     ArrayList params = new ArrayList();
/* 1265 */     sql.append("UPDATE ");
/* 1266 */     sql.append(this.tableName);
/*      */ 
/* 1270 */     sql.append(" SET ");
/* 1271 */     int count = 0;
/* 1272 */     for (int i = 0; i < this.columnCount; ++i) {
/* 1273 */       if (this.updateRow[i] != null) {
/* 1274 */         if (count > 0) {
/* 1275 */           sql.append(", ");
/*      */         }
/* 1277 */         sql.append(this.columns[i].realName);
/* 1278 */         sql.append("=?");
/* 1279 */         this.updateRow[i].markerPos = (sql.length() - 1);
/* 1280 */         params.add(this.updateRow[i]);
/* 1281 */         ++count;
/* 1282 */         if (!(this.columns[i].isKey)) {
/*      */           continue;
/*      */         }
/* 1285 */         keysChanged = true;
/*      */       }
/*      */     }
/*      */ 
/* 1289 */     if (count == 0)
/*      */     {
/* 1292 */       return;
/*      */     }
/*      */ 
/* 1297 */     ParamInfo[] parameters = buildWhereClause(sql, params, false);
/*      */ 
/* 1301 */     this.updateTds.executeSQL(sql.toString(), null, parameters, false, 0, this.statement.getMaxRows(), this.statement.getMaxFieldSize(), true);
/*      */ 
/* 1303 */     int updateCount = 0;
/* 1304 */     while (!(this.updateTds.isEndOfResponse())) {
/* 1305 */       if ((this.updateTds.getMoreResults()) || 
/* 1306 */         (!(this.updateTds.isUpdateCount()))) continue;
/* 1307 */       updateCount = this.updateTds.getUpdateCount();
/*      */     }
/*      */ 
/* 1311 */     this.updateTds.clearResponseQueue();
/* 1312 */     this.statement.getMessages().checkErrors();
/*      */ 
/* 1314 */     if (updateCount == 0)
/*      */     {
/* 1316 */       throw new SQLException(Messages.get("error.resultset.updatefail"), "24000");
/*      */     }
/*      */ 
/* 1321 */     if (this.resultSetType != 1004)
/*      */     {
/* 1324 */       ConnectionJDBC2 con = (ConnectionJDBC2)this.statement.getConnection();
/* 1325 */       for (int i = 0; i < this.updateRow.length; ++i) {
/* 1326 */         if (this.updateRow[i] != null) {
/* 1327 */           if ((this.updateRow[i].value instanceof byte[]) && (((this.columns[i].jdbcType == 1) || (this.columns[i].jdbcType == 12) || (this.columns[i].jdbcType == -1))))
/*      */           {
/*      */             try
/*      */             {
/* 1334 */               this.currentRow[i] = new String((byte[])this.updateRow[i].value, con.getCharset());
/*      */             } catch (UnsupportedEncodingException e) {
/* 1336 */               this.currentRow[i] = new String((byte[])this.updateRow[i].value);
/*      */             }
/*      */           }
/*      */           else this.currentRow[i] = Support.convert(con, this.updateRow[i].value, this.columns[i].jdbcType, con.getCharset());
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1348 */     if ((keysChanged) && (this.resultSetType >= 1005))
/*      */     {
/* 1350 */       this.rowData.add(this.currentRow);
/* 1351 */       this.rowsInResult = this.rowData.size();
/* 1352 */       this.rowData.set(this.pos - 1, null);
/* 1353 */       this.currentRow = null;
/* 1354 */       this.rowDeleted = true;
/*      */     } else {
/* 1356 */       this.rowUpdated = true;
/*      */     }
/*      */ 
/* 1361 */     cancelRowUpdates();
/*      */   }
/*      */ 
/*      */   public boolean first() throws SQLException {
/* 1365 */     checkOpen();
/* 1366 */     checkScrollable();
/* 1367 */     return cursorFetch(1);
/*      */   }
/*      */ 
/*      */   public boolean isLast() throws SQLException {
/* 1371 */     checkOpen();
/*      */ 
/* 1373 */     return ((this.pos == this.rowsInResult) && (this.rowsInResult != 0));
/*      */   }
/*      */ 
/*      */   public boolean last() throws SQLException {
/* 1377 */     checkOpen();
/* 1378 */     checkScrollable();
/* 1379 */     return cursorFetch(this.rowsInResult);
/*      */   }
/*      */ 
/*      */   public boolean next() throws SQLException {
/* 1383 */     checkOpen();
/* 1384 */     if (this.pos != -1) {
/* 1385 */       return cursorFetch(this.pos + 1);
/*      */     }
/* 1387 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean previous() throws SQLException
/*      */   {
/* 1392 */     checkOpen();
/* 1393 */     checkScrollable();
/* 1394 */     if (this.pos == -1) {
/* 1395 */       this.pos = (this.rowsInResult + 1);
/*      */     }
/* 1397 */     return cursorFetch(this.pos - 1);
/*      */   }
/*      */ 
/*      */   public boolean rowDeleted() throws SQLException {
/* 1401 */     checkOpen();
/*      */ 
/* 1403 */     return this.rowDeleted;
/*      */   }
/*      */ 
/*      */   public boolean rowInserted() throws SQLException {
/* 1407 */     checkOpen();
/*      */ 
/* 1410 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean rowUpdated() throws SQLException {
/* 1414 */     checkOpen();
/*      */ 
/* 1417 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean absolute(int row) throws SQLException {
/* 1421 */     checkOpen();
/* 1422 */     checkScrollable();
/* 1423 */     if (row < 1) {
/* 1424 */       row = this.rowsInResult + 1 + row;
/*      */     }
/*      */ 
/* 1427 */     return cursorFetch(row);
/*      */   }
/*      */ 
/*      */   public boolean relative(int row) throws SQLException {
/* 1431 */     checkScrollable();
/* 1432 */     if (this.pos == -1) {
/* 1433 */       return absolute(this.rowsInResult + 1 + row);
/*      */     }
/* 1435 */     return absolute(this.pos + row);
/*      */   }
/*      */ 
/*      */   public String getCursorName() throws SQLException
/*      */   {
/* 1440 */     checkOpen();
/*      */ 
/* 1442 */     if ((this.cursorName != null) && (!(this.cursorName.startsWith("_jtds")))) {
/* 1443 */       return this.cursorName;
/*      */     }
/* 1445 */     throw new SQLException(Messages.get("error.resultset.noposupdate"), "24000");
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.CachedResultSet
 * JD-Core Version:    0.5.3
 */