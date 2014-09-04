/*      */ package net.sourceforge.jtds.jdbc;
/*      */ 
/*      */ import java.math.BigDecimal;
/*      */ import java.sql.Connection;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.SQLWarning;
/*      */ 
/*      */ public class MSCursorResultSet extends JtdsResultSet
/*      */ {
/*   47 */   private static final Integer FETCH_FIRST = new Integer(1);
/*   48 */   private static final Integer FETCH_NEXT = new Integer(2);
/*   49 */   private static final Integer FETCH_PREVIOUS = new Integer(4);
/*   50 */   private static final Integer FETCH_LAST = new Integer(8);
/*   51 */   private static final Integer FETCH_ABSOLUTE = new Integer(16);
/*   52 */   private static final Integer FETCH_RELATIVE = new Integer(32);
/*   53 */   private static final Integer FETCH_REPEAT = new Integer(128);
/*   54 */   private static final Integer FETCH_INFO = new Integer(256);
/*      */   private static final int CURSOR_TYPE_KEYSET = 1;
/*      */   private static final int CURSOR_TYPE_DYNAMIC = 2;
/*      */   private static final int CURSOR_TYPE_FORWARD = 4;
/*      */   private static final int CURSOR_TYPE_STATIC = 8;
/*      */   private static final int CURSOR_TYPE_FASTFORWARDONLY = 16;
/*      */   private static final int CURSOR_TYPE_PARAMETERIZED = 4096;
/*      */   private static final int CURSOR_TYPE_AUTO_FETCH = 8192;
/*      */   private static final int CURSOR_CONCUR_READ_ONLY = 1;
/*      */   private static final int CURSOR_CONCUR_SCROLL_LOCKS = 2;
/*      */   private static final int CURSOR_CONCUR_OPTIMISTIC = 4;
/*      */   private static final int CURSOR_CONCUR_OPTIMISTIC_VALUES = 8;
/*   69 */   private static final Integer CURSOR_OP_INSERT = new Integer(4);
/*   70 */   private static final Integer CURSOR_OP_UPDATE = new Integer(33);
/*   71 */   private static final Integer CURSOR_OP_DELETE = new Integer(34);
/*      */ 
/*   76 */   private static final Integer SQL_ROW_DIRTY = new Integer(0);
/*      */ 
/*   81 */   private static final Integer SQL_ROW_SUCCESS = new Integer(1);
/*      */ 
/*   86 */   private static final Integer SQL_ROW_DELETED = new Integer(2);
/*      */   private boolean onInsertRow;
/*      */   private ParamInfo[] insertRow;
/*      */   private ParamInfo[] updateRow;
/*      */   private Object[][] rowCache;
/*      */   private int cursorPos;
/*      */   private boolean asyncCursor;
/*  108 */   private final ParamInfo PARAM_CURSOR_HANDLE = new ParamInfo(4, null, 0);
/*      */ 
/*  111 */   private final ParamInfo PARAM_FETCHTYPE = new ParamInfo(4, null, 0);
/*      */ 
/*  114 */   private final ParamInfo PARAM_ROWNUM_IN = new ParamInfo(4, null, 0);
/*      */ 
/*  117 */   private final ParamInfo PARAM_NUMROWS_IN = new ParamInfo(4, null, 0);
/*      */ 
/*  120 */   private final ParamInfo PARAM_ROWNUM_OUT = new ParamInfo(4, null, 1);
/*      */ 
/*  123 */   private final ParamInfo PARAM_NUMROWS_OUT = new ParamInfo(4, null, 1);
/*      */ 
/*  126 */   private final ParamInfo PARAM_OPTYPE = new ParamInfo(4, null, 0);
/*      */ 
/*  129 */   private final ParamInfo PARAM_ROWNUM = new ParamInfo(4, new Integer(1), 0);
/*      */ 
/*  132 */   private final ParamInfo PARAM_TABLE = new ParamInfo(12, "", 4);
/*      */ 
/*      */   MSCursorResultSet(JtdsStatement statement, String sql, String procName, ParamInfo[] procedureParams, int resultSetType, int concurrency)
/*      */     throws SQLException
/*      */   {
/*  149 */     super(statement, resultSetType, concurrency, null);
/*      */ 
/*  151 */     this.PARAM_NUMROWS_IN.value = new Integer(this.fetchSize);
/*  152 */     this.rowCache = new Object[this.fetchSize][];
/*      */ 
/*  154 */     cursorCreate(sql, procName, procedureParams);
/*  155 */     if (!(this.asyncCursor))
/*      */       return;
/*  157 */     cursorFetch(FETCH_REPEAT, 0);
/*      */   }
/*      */ 
/*      */   protected Object setColValue(int colIndex, int jdbcType, Object value, int length)
/*      */     throws SQLException
/*      */   {
/*  171 */     value = super.setColValue(colIndex, jdbcType, value, length);
/*      */ 
/*  173 */     if ((!(this.onInsertRow)) && (getCurrentRow() == null)) {
/*  174 */       throw new SQLException(Messages.get("error.resultset.norow"), "24000");
/*      */     }
/*  176 */     --colIndex;
/*      */ 
/*  178 */     ColInfo ci = this.columns[colIndex];
/*      */     ParamInfo pi;
/*  180 */     if (this.onInsertRow) {
/*  181 */       pi = this.insertRow[colIndex];
/*      */     } else {
/*  183 */       if (this.updateRow == null) {
/*  184 */         this.updateRow = new ParamInfo[this.columnCount];
/*      */       }
/*  186 */       pi = this.updateRow[colIndex];
/*      */     }
/*      */ 
/*  189 */     if (pi == null) {
/*  190 */       pi = new ParamInfo(-1, TdsData.isUnicode(ci));
/*  191 */       pi.name = '@' + ci.realName;
/*  192 */       pi.collation = ci.collation;
/*  193 */       pi.charsetInfo = ci.charsetInfo;
/*  194 */       if (this.onInsertRow)
/*  195 */         this.insertRow[colIndex] = pi;
/*      */       else {
/*  197 */         this.updateRow[colIndex] = pi;
/*      */       }
/*      */     }
/*      */ 
/*  201 */     if (value == null) {
/*  202 */       pi.value = null;
/*  203 */       pi.length = 0;
/*  204 */       pi.jdbcType = ci.jdbcType;
/*  205 */       pi.isSet = true;
/*  206 */       if ((pi.jdbcType == 2) || (pi.jdbcType == 3))
/*  207 */         pi.scale = 10;
/*      */       else
/*  209 */         pi.scale = 0;
/*      */     }
/*      */     else {
/*  212 */       pi.value = value;
/*  213 */       pi.length = length;
/*  214 */       pi.isSet = true;
/*  215 */       pi.jdbcType = jdbcType;
/*  216 */       pi.isUnicode = (("ntext".equals(ci.sqlType)) || ("nchar".equals(ci.sqlType)) || ("nvarchar".equals(ci.sqlType)));
/*      */ 
/*  219 */       if (pi.value instanceof BigDecimal)
/*  220 */         pi.scale = ((BigDecimal)pi.value).scale();
/*      */       else {
/*  222 */         pi.scale = 0;
/*      */       }
/*      */     }
/*      */ 
/*  226 */     return value;
/*      */   }
/*      */ 
/*      */   protected Object getColumn(int index)
/*      */     throws SQLException
/*      */   {
/*  238 */     checkOpen();
/*      */ 
/*  240 */     if ((index < 1) || (index > this.columnCount))
/*  241 */       throw new SQLException(Messages.get("error.resultset.colindex", Integer.toString(index)), "07009");
/*      */     Object[] currentRow;
/*  247 */     if ((this.onInsertRow) || ((currentRow = getCurrentRow()) == null)) {
/*  248 */       throw new SQLException(Messages.get("error.resultset.norow"), "24000");
/*      */     }
/*      */ 
/*  252 */     if (SQL_ROW_DIRTY.equals(currentRow[(this.columns.length - 1)])) {
/*  253 */       cursorFetch(FETCH_REPEAT, 0);
/*  254 */       currentRow = getCurrentRow();
/*      */     }
/*      */ 
/*  257 */     Object data = currentRow[(index - 1)];
/*  258 */     this.wasNull = (data == null);
/*      */ 
/*  260 */     return data;
/*      */   }
/*      */ 
/*      */   static int getCursorScrollOpt(int resultSetType, int resultSetConcurrency, boolean parameterized)
/*      */   {
/*      */     int scrollOpt;
/*  281 */     switch (resultSetType)
/*      */     {
/*      */     case 1004:
/*  283 */       scrollOpt = 8;
/*  284 */       break;
/*      */     case 1005:
/*  287 */       scrollOpt = 1;
/*  288 */       break;
/*      */     case 1006:
/*  291 */       scrollOpt = 2;
/*  292 */       break;
/*      */     case 1003:
/*      */     default:
/*  296 */       scrollOpt = (resultSetConcurrency == 1007) ? 8208 : 4;
/*      */     }
/*      */ 
/*  306 */     if (parameterized) {
/*  307 */       scrollOpt |= 4096;
/*      */     }
/*      */ 
/*  310 */     return scrollOpt;
/*      */   }
/*      */ 
/*      */   static int getCursorConcurrencyOpt(int resultSetConcurrency)
/*      */   {
/*  324 */     switch (resultSetConcurrency)
/*      */     {
/*      */     case 1008:
/*  326 */       return 4;
/*      */     case 1009:
/*  329 */       return 2;
/*      */     case 1010:
/*  332 */       return 8;
/*      */     case 1007:
/*      */     }
/*      */ 
/*  336 */     return 1;
/*      */   }
/*      */ 
/*      */   private void cursorCreate(String sql, String procName, ParamInfo[] parameters)
/*      */     throws SQLException
/*      */   {
/*  352 */     TdsCore tds = this.statement.getTds();
/*  353 */     int prepareSql = this.statement.connection.getPrepareSql();
/*  354 */     Integer prepStmtHandle = null;
/*      */ 
/*  362 */     if ((this.cursorName != null) && (this.resultSetType == 1003) && (this.concurrency == 1007))
/*      */     {
/*  365 */       this.concurrency = 1008;
/*      */     }
/*      */ 
/*  370 */     if ((parameters != null) && (parameters.length == 0)) {
/*  371 */       parameters = null;
/*      */     }
/*      */ 
/*  377 */     if (tds.getTdsVersion() == 1) {
/*  378 */       prepareSql = 0;
/*  379 */       if (parameters != null) {
/*  380 */         procName = null;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  387 */     if ((parameters != null) && (prepareSql == 0)) {
/*  388 */       sql = Support.substituteParameters(sql, parameters, this.statement.connection);
/*  389 */       parameters = null;
/*      */     }
/*      */ 
/*  395 */     if ((parameters != null) && ((
/*  396 */       (procName == null) || (!(procName.startsWith("#jtds")))))) {
/*  397 */       sql = Support.substituteParamMarkers(sql, parameters);
/*      */     }
/*      */ 
/*  408 */     if (procName != null) {
/*  409 */       if (procName.startsWith("#jtds")) {
/*  410 */         StringBuffer buf = new StringBuffer(procName.length() + 16 + ((parameters != null) ? parameters.length * 5 : 0));
/*      */ 
/*  412 */         buf.append("EXEC ").append(procName).append(' ');
/*  413 */         for (int i = 0; (parameters != null) && (i < parameters.length); ++i) {
/*  414 */           if (i != 0) {
/*  415 */             buf.append(',');
/*      */           }
/*  417 */           if (parameters[i].name != null)
/*  418 */             buf.append(parameters[i].name);
/*      */           else {
/*  420 */             buf.append("@P").append(i);
/*      */           }
/*      */         }
/*  423 */         sql = buf.toString();
/*  424 */       } else if (TdsCore.isPreparedProcedureName(procName))
/*      */       {
/*      */         try
/*      */         {
/*  433 */           prepStmtHandle = new Integer(procName);
/*      */         } catch (NumberFormatException e) {
/*  435 */           throw new IllegalStateException("Invalid prepared statement handle: " + procName);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  446 */     int scrollOpt = getCursorScrollOpt(this.resultSetType, this.concurrency, parameters != null);
/*      */ 
/*  448 */     int ccOpt = getCursorConcurrencyOpt(this.concurrency);
/*      */ 
/*  454 */     ParamInfo pScrollOpt = new ParamInfo(4, new Integer(scrollOpt), 1);
/*      */ 
/*  458 */     ParamInfo pConCurOpt = new ParamInfo(4, new Integer(ccOpt), 1);
/*      */ 
/*  462 */     ParamInfo pRowCount = new ParamInfo(4, new Integer(this.fetchSize), 1);
/*      */ 
/*  466 */     ParamInfo pCursor = new ParamInfo(4, null, 1);
/*      */ 
/*  470 */     ParamInfo pStmtHand = null;
/*  471 */     if (prepareSql == 3) {
/*  472 */       pStmtHand = new ParamInfo(4, prepStmtHandle, 1);
/*      */     }
/*      */ 
/*  477 */     ParamInfo pParamDef = null;
/*  478 */     if (parameters != null)
/*      */     {
/*  480 */       for (int i = 0; i < parameters.length; ++i) {
/*  481 */         TdsData.getNativeType(this.statement.connection, parameters[i]);
/*      */       }
/*      */ 
/*  484 */       pParamDef = new ParamInfo(-1, Support.getParameterDefinitions(parameters), 4);
/*      */     }
/*      */ 
/*  491 */     ParamInfo pSQL = new ParamInfo(-1, sql, 4);
/*      */     ParamInfo[] params;
/*  495 */     if ((prepareSql == 3) && (prepStmtHandle != null))
/*      */     {
/*  497 */       procName = "sp_cursorexecute";
/*  498 */       if (parameters == null) {
/*  499 */         parameters = new ParamInfo[5];
/*      */       } else {
/*  501 */         params = new ParamInfo[5 + parameters.length];
/*  502 */         System.arraycopy(parameters, 0, params, 5, parameters.length);
/*  503 */         parameters = params;
/*      */       }
/*      */ 
/*  506 */       pStmtHand.isOutput = false;
/*  507 */       pStmtHand.value = prepStmtHandle;
/*  508 */       parameters[0] = pStmtHand;
/*      */ 
/*  510 */       parameters[1] = pCursor;
/*      */ 
/*  512 */       pScrollOpt.value = new Integer(scrollOpt & 0xFFFFEFFF);
/*      */     }
/*      */     else {
/*  515 */       procName = "sp_cursoropen";
/*  516 */       if (parameters == null) {
/*  517 */         parameters = new ParamInfo[5];
/*      */       } else {
/*  519 */         params = new ParamInfo[6 + parameters.length];
/*  520 */         System.arraycopy(parameters, 0, params, 6, parameters.length);
/*  521 */         parameters = params;
/*  522 */         parameters[5] = pParamDef;
/*      */       }
/*      */ 
/*  525 */       parameters[0] = pCursor;
/*      */ 
/*  527 */       parameters[1] = pSQL;
/*      */     }
/*      */ 
/*  530 */     parameters[2] = pScrollOpt;
/*      */ 
/*  532 */     parameters[3] = pConCurOpt;
/*      */ 
/*  534 */     parameters[4] = pRowCount;
/*      */ 
/*  536 */     tds.executeSQL(null, procName, parameters, false, this.statement.getQueryTimeout(), this.statement.getMaxRows(), this.statement.getMaxFieldSize(), true);
/*      */ 
/*  541 */     processOutput(tds, true);
/*  542 */     if ((scrollOpt & 0x2000) != 0)
/*      */     {
/*  544 */       this.cursorPos = 1;
/*      */     }
/*      */ 
/*  548 */     Integer retVal = tds.getReturnStatus();
/*  549 */     if ((retVal == null) || ((retVal.intValue() != 0) && (retVal.intValue() != 2))) {
/*  550 */       throw new SQLException(Messages.get("error.resultset.openfail"), "24000");
/*      */     }
/*      */ 
/*  554 */     this.asyncCursor = (retVal.intValue() == 2);
/*      */ 
/*  559 */     this.PARAM_CURSOR_HANDLE.value = pCursor.getOutValue();
/*  560 */     int actualScroll = ((Integer)pScrollOpt.getOutValue()).intValue();
/*  561 */     int actualCc = ((Integer)pConCurOpt.getOutValue()).intValue();
/*  562 */     this.rowsInResult = ((Integer)pRowCount.getOutValue()).intValue();
/*      */ 
/*  569 */     if (this.cursorName != null) {
/*  570 */       ParamInfo[] params = new ParamInfo[3];
/*  571 */       params[0] = this.PARAM_CURSOR_HANDLE;
/*  572 */       this.PARAM_OPTYPE.value = new Integer(2);
/*  573 */       params[1] = this.PARAM_OPTYPE;
/*  574 */       params[2] = new ParamInfo(12, this.cursorName, 4);
/*  575 */       tds.executeSQL(null, "sp_cursoroption", params, true, 0, -1, -1, true);
/*  576 */       tds.clearResponseQueue();
/*  577 */       if (tds.getReturnStatus().intValue() != 0) {
/*  578 */         this.statement.getMessages().addException(new SQLException(Messages.get("error.resultset.openfail"), "24000"));
/*      */       }
/*      */ 
/*  581 */       this.statement.getMessages().checkErrors();
/*      */     }
/*      */ 
/*  586 */     if ((actualScroll != (scrollOpt & 0xFFF)) || (actualCc != ccOpt)) {
/*  587 */       boolean downgradeWarning = false;
/*      */ 
/*  589 */       if (actualScroll != scrollOpt)
/*      */       {
/*      */         int resultSetType;
/*  591 */         switch (actualScroll)
/*      */         {
/*      */         case 4:
/*      */         case 16:
/*  594 */           resultSetType = 1003;
/*  595 */           break;
/*      */         case 8:
/*  598 */           resultSetType = 1004;
/*  599 */           break;
/*      */         case 1:
/*  602 */           resultSetType = 1005;
/*  603 */           break;
/*      */         case 2:
/*  606 */           resultSetType = 1006;
/*  607 */           break;
/*      */         default:
/*  610 */           resultSetType = this.resultSetType;
/*  611 */           this.statement.getMessages().addWarning(new SQLWarning(Messages.get("warning.cursortype", Integer.toString(actualScroll)), "01000"));
/*      */         }
/*      */ 
/*  615 */         downgradeWarning = resultSetType < this.resultSetType;
/*  616 */         this.resultSetType = resultSetType;
/*      */       }
/*      */ 
/*  619 */       if (actualCc != ccOpt)
/*      */       {
/*      */         int concurrency;
/*  621 */         switch (actualCc)
/*      */         {
/*      */         case 1:
/*  623 */           concurrency = 1007;
/*  624 */           break;
/*      */         case 4:
/*  627 */           concurrency = 1008;
/*  628 */           break;
/*      */         case 2:
/*  631 */           concurrency = 1009;
/*  632 */           break;
/*      */         case 8:
/*  635 */           concurrency = 1010;
/*  636 */           break;
/*      */         case 3:
/*      */         case 5:
/*      */         case 6:
/*      */         case 7:
/*      */         default:
/*  639 */           concurrency = this.concurrency;
/*  640 */           this.statement.getMessages().addWarning(new SQLWarning(Messages.get("warning.concurrtype", Integer.toString(actualCc)), "01000"));
/*      */         }
/*      */ 
/*  644 */         downgradeWarning = concurrency < this.concurrency;
/*  645 */         this.concurrency = concurrency;
/*      */       }
/*      */ 
/*  648 */       if (!(downgradeWarning))
/*      */         return;
/*  650 */       this.statement.addWarning(new SQLWarning(Messages.get("warning.cursordowngraded", this.resultSetType + "/" + this.concurrency), "01000"));
/*      */     }
/*      */   }
/*      */ 
/*      */   private boolean cursorFetch(Integer fetchType, int rowNum)
/*      */     throws SQLException
/*      */   {
/*  668 */     TdsCore tds = this.statement.getTds();
/*      */ 
/*  670 */     this.statement.clearWarnings();
/*      */ 
/*  672 */     if ((fetchType != FETCH_ABSOLUTE) && (fetchType != FETCH_RELATIVE)) {
/*  673 */       rowNum = 1;
/*      */     }
/*      */ 
/*  676 */     ParamInfo[] param = new ParamInfo[4];
/*      */ 
/*  678 */     param[0] = this.PARAM_CURSOR_HANDLE;
/*      */ 
/*  681 */     this.PARAM_FETCHTYPE.value = fetchType;
/*  682 */     param[1] = this.PARAM_FETCHTYPE;
/*      */ 
/*  685 */     this.PARAM_ROWNUM_IN.value = new Integer(rowNum);
/*  686 */     param[2] = this.PARAM_ROWNUM_IN;
/*      */ 
/*  688 */     if (((Integer)this.PARAM_NUMROWS_IN.value).intValue() != this.fetchSize)
/*      */     {
/*  690 */       this.PARAM_NUMROWS_IN.value = new Integer(this.fetchSize);
/*  691 */       this.rowCache = new Object[this.fetchSize][];
/*      */     }
/*  693 */     param[3] = this.PARAM_NUMROWS_IN;
/*      */ 
/*  695 */     synchronized (tds)
/*      */     {
/*  698 */       tds.executeSQL(null, "sp_cursorfetch", param, true, 0, 0, this.statement.getMaxFieldSize(), false);
/*      */ 
/*  702 */       this.PARAM_FETCHTYPE.value = FETCH_INFO;
/*  703 */       param[1] = this.PARAM_FETCHTYPE;
/*      */ 
/*  706 */       this.PARAM_ROWNUM_OUT.clearOutValue();
/*  707 */       param[2] = this.PARAM_ROWNUM_OUT;
/*      */ 
/*  709 */       this.PARAM_NUMROWS_OUT.clearOutValue();
/*  710 */       param[3] = this.PARAM_NUMROWS_OUT;
/*      */ 
/*  714 */       tds.executeSQL(null, "sp_cursorfetch", param, true, this.statement.getQueryTimeout(), -1, -1, true);
/*      */     }
/*      */ 
/*  719 */     processOutput(tds, false);
/*      */ 
/*  721 */     this.cursorPos = ((Integer)this.PARAM_ROWNUM_OUT.getOutValue()).intValue();
/*  722 */     if (fetchType != FETCH_REPEAT)
/*      */     {
/*  724 */       this.pos = this.cursorPos;
/*      */     }
/*  726 */     this.rowsInResult = ((Integer)this.PARAM_NUMROWS_OUT.getOutValue()).intValue();
/*  727 */     if (this.rowsInResult < 0)
/*      */     {
/*  730 */       this.rowsInResult = (0 - this.rowsInResult);
/*      */     }
/*      */ 
/*  733 */     return (getCurrentRow() != null);
/*      */   }
/*      */ 
/*      */   private void cursor(Integer opType, ParamInfo[] row)
/*      */     throws SQLException
/*      */   {
/*  744 */     TdsCore tds = this.statement.getTds();
/*      */ 
/*  746 */     this.statement.clearWarnings();
/*      */     ParamInfo[] param;
/*  749 */     if (opType == CURSOR_OP_DELETE)
/*      */     {
/*  751 */       param = new ParamInfo[3];
/*      */     } else {
/*  753 */       if (row == null) {
/*  754 */         throw new SQLException(Messages.get("error.resultset.update"), "24000");
/*      */       }
/*      */ 
/*  757 */       param = new ParamInfo[4 + this.columnCount];
/*      */     }
/*      */ 
/*  761 */     param[0] = this.PARAM_CURSOR_HANDLE;
/*      */ 
/*  764 */     this.PARAM_OPTYPE.value = opType;
/*  765 */     param[1] = this.PARAM_OPTYPE;
/*      */ 
/*  768 */     this.PARAM_ROWNUM.value = new Integer(this.pos - this.cursorPos + 1);
/*  769 */     param[2] = this.PARAM_ROWNUM;
/*      */ 
/*  772 */     if (row != null)
/*      */     {
/*  774 */       param[3] = this.PARAM_TABLE;
/*      */ 
/*  776 */       int colCnt = this.columnCount;
/*      */ 
/*  779 */       int crtCol = 4;
/*      */ 
/*  781 */       String tableName = null;
/*      */ 
/*  783 */       for (int i = 0; i < colCnt; ++i) {
/*  784 */         ParamInfo pi = row[i];
/*  785 */         ColInfo col = this.columns[i];
/*      */ 
/*  787 */         if ((pi != null) && (pi.isSet)) {
/*  788 */           if (!(col.isWriteable))
/*      */           {
/*  790 */             throw new SQLException(Messages.get("error.resultset.insert", Integer.toString(i + 1), col.realName), "24000");
/*      */           }
/*      */ 
/*  794 */           param[(crtCol++)] = pi;
/*      */         }
/*  796 */         if ((tableName == null) && (col.tableName != null)) {
/*  797 */           if ((col.catalog != null) || (col.schema != null)) {
/*  798 */             tableName = ((col.catalog != null) ? col.catalog : "") + '.' + ((col.schema != null) ? col.schema : "") + '.' + col.tableName;
/*      */           }
/*      */           else
/*      */           {
/*  802 */             tableName = col.tableName;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  807 */       if (crtCol == 4) {
/*  808 */         if (opType == CURSOR_OP_INSERT)
/*      */         {
/*  813 */           param[crtCol] = new ParamInfo(12, "insert " + tableName + " default values", 4);
/*      */ 
/*  816 */           ++crtCol;
/*      */         }
/*      */         else {
/*  819 */           return;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  825 */       if (crtCol != colCnt + 4) {
/*  826 */         ParamInfo[] newParam = new ParamInfo[crtCol];
/*      */ 
/*  828 */         System.arraycopy(param, 0, newParam, 0, crtCol);
/*  829 */         param = newParam;
/*      */       }
/*      */     }
/*      */ 
/*  833 */     synchronized (tds)
/*      */     {
/*  837 */       tds.executeSQL(null, "sp_cursor", param, false, 0, -1, -1, false);
/*      */ 
/*  839 */       if (param.length != 4) {
/*  840 */         param = new ParamInfo[4];
/*  841 */         param[0] = this.PARAM_CURSOR_HANDLE;
/*      */       }
/*      */ 
/*  845 */       this.PARAM_FETCHTYPE.value = FETCH_INFO;
/*  846 */       param[1] = this.PARAM_FETCHTYPE;
/*      */ 
/*  849 */       this.PARAM_ROWNUM_OUT.clearOutValue();
/*  850 */       param[2] = this.PARAM_ROWNUM_OUT;
/*      */ 
/*  852 */       this.PARAM_NUMROWS_OUT.clearOutValue();
/*  853 */       param[3] = this.PARAM_NUMROWS_OUT;
/*      */ 
/*  857 */       tds.executeSQL(null, "sp_cursorfetch", param, true, this.statement.getQueryTimeout(), -1, -1, true);
/*      */     }
/*      */ 
/*  862 */     tds.consumeOneResponse();
/*  863 */     this.statement.getMessages().checkErrors();
/*  864 */     Integer retVal = tds.getReturnStatus();
/*  865 */     if (retVal.intValue() != 0) {
/*  866 */       throw new SQLException(Messages.get("error.resultset.cursorfail"), "24000");
/*      */     }
/*      */ 
/*  873 */     if (row != null) {
/*  874 */       for (int i = 0; i < row.length; ++i) {
/*  875 */         if (row[i] != null) {
/*  876 */           row[i].clearInValue();
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  882 */     tds.clearResponseQueue();
/*  883 */     this.statement.getMessages().checkErrors();
/*  884 */     this.cursorPos = ((Integer)this.PARAM_ROWNUM_OUT.getOutValue()).intValue();
/*  885 */     this.rowsInResult = ((Integer)this.PARAM_NUMROWS_OUT.getOutValue()).intValue();
/*      */ 
/*  888 */     if ((opType == CURSOR_OP_DELETE) || (opType == CURSOR_OP_UPDATE)) {
/*  889 */       Object[] currentRow = getCurrentRow();
/*  890 */       if (currentRow == null) {
/*  891 */         throw new SQLException(Messages.get("error.resultset.updatefail"), "24000");
/*      */       }
/*      */ 
/*  895 */       currentRow[(this.columns.length - 1)] = ((opType == CURSOR_OP_DELETE) ? SQL_ROW_DELETED : SQL_ROW_DIRTY);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void cursorClose()
/*      */     throws SQLException
/*      */   {
/*  906 */     TdsCore tds = this.statement.getTds();
/*      */ 
/*  908 */     this.statement.clearWarnings();
/*      */ 
/*  911 */     tds.clearResponseQueue();
/*  912 */     SQLException ex = this.statement.getMessages().exceptions;
/*      */ 
/*  914 */     ParamInfo[] param = new ParamInfo[1];
/*      */ 
/*  917 */     param[0] = this.PARAM_CURSOR_HANDLE;
/*      */ 
/*  919 */     tds.executeSQL(null, "sp_cursorclose", param, false, this.statement.getQueryTimeout(), -1, -1, true);
/*      */ 
/*  921 */     tds.clearResponseQueue();
/*      */ 
/*  923 */     if (ex != null) {
/*  924 */       ex.setNextException(this.statement.getMessages().exceptions);
/*  925 */       throw ex;
/*      */     }
/*  927 */     this.statement.getMessages().checkErrors();
/*      */   }
/*      */ 
/*      */   private void processOutput(TdsCore tds, boolean setMeta)
/*      */     throws SQLException
/*      */   {
/*  943 */     if ((tds.getMoreResults()) || (tds.isEndOfResponse()));
/*  945 */     int i = 0;
/*  946 */     if (tds.isResultSet())
/*      */     {
/*  948 */       if (setMeta) {
/*  949 */         this.columns = copyInfo(tds.getColumns());
/*  950 */         this.columnCount = getColumnCount(this.columns);
/*      */       }
/*      */ 
/*  956 */       if ((tds.isRowData()) || (tds.getNextRow()))
/*      */         do
/*  958 */           this.rowCache[(i++)] = copyRow(tds.getRowData());
/*  959 */         while (tds.getNextRow());
/*      */     }
/*  961 */     else if (setMeta) {
/*  962 */       this.statement.getMessages().addException(new SQLException(Messages.get("error.statement.noresult"), "24000"));
/*      */     }
/*      */ 
/*  967 */     for (; i < this.rowCache.length; ++i) {
/*  968 */       this.rowCache[i] = null;
/*      */     }
/*      */ 
/*  971 */     tds.clearResponseQueue();
/*  972 */     this.statement.messages.checkErrors();
/*      */   }
/*      */ 
/*      */   public void afterLast()
/*      */     throws SQLException
/*      */   {
/*  980 */     checkOpen();
/*  981 */     checkScrollable();
/*      */ 
/*  983 */     if (this.pos == -1)
/*      */       return;
/*  985 */     cursorFetch(FETCH_ABSOLUTE, 2147483647);
/*      */   }
/*      */ 
/*      */   public void beforeFirst() throws SQLException
/*      */   {
/*  990 */     checkOpen();
/*  991 */     checkScrollable();
/*      */ 
/*  993 */     if (this.pos != 0)
/*  994 */       cursorFetch(FETCH_ABSOLUTE, 0);
/*      */   }
/*      */ 
/*      */   public void cancelRowUpdates() throws SQLException
/*      */   {
/*  999 */     checkOpen();
/* 1000 */     checkUpdateable();
/*      */ 
/* 1002 */     if (this.onInsertRow) {
/* 1003 */       throw new SQLException(Messages.get("error.resultset.insrow"), "24000");
/*      */     }
/*      */ 
/* 1006 */     for (int i = 0; (this.updateRow != null) && (i < this.updateRow.length); ++i)
/* 1007 */       if (this.updateRow[i] != null)
/* 1008 */         this.updateRow[i].clearInValue();
/*      */   }
/*      */ 
/*      */   public void close()
/*      */     throws SQLException
/*      */   {
/* 1014 */     if (this.closed) return;
/*      */     try {
/* 1016 */       if (!(this.statement.getConnection().isClosed()))
/* 1017 */         cursorClose();
/*      */     }
/*      */     finally {
/* 1020 */       this.closed = true;
/* 1021 */       this.statement = null;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void deleteRow() throws SQLException
/*      */   {
/* 1027 */     checkOpen();
/* 1028 */     checkUpdateable();
/*      */ 
/* 1030 */     if (getCurrentRow() == null) {
/* 1031 */       throw new SQLException(Messages.get("error.resultset.norow"), "24000");
/*      */     }
/*      */ 
/* 1034 */     if (this.onInsertRow) {
/* 1035 */       throw new SQLException(Messages.get("error.resultset.insrow"), "24000");
/*      */     }
/*      */ 
/* 1038 */     cursor(CURSOR_OP_DELETE, null);
/*      */   }
/*      */ 
/*      */   public void insertRow() throws SQLException {
/* 1042 */     checkOpen();
/* 1043 */     checkUpdateable();
/*      */ 
/* 1045 */     if (!(this.onInsertRow)) {
/* 1046 */       throw new SQLException(Messages.get("error.resultset.notinsrow"), "24000");
/*      */     }
/*      */ 
/* 1049 */     cursor(CURSOR_OP_INSERT, this.insertRow);
/*      */   }
/*      */ 
/*      */   public void moveToCurrentRow() throws SQLException {
/* 1053 */     checkOpen();
/* 1054 */     checkUpdateable();
/*      */ 
/* 1056 */     this.onInsertRow = false;
/*      */   }
/*      */ 
/*      */   public void moveToInsertRow() throws SQLException {
/* 1060 */     checkOpen();
/* 1061 */     checkUpdateable();
/* 1062 */     if (this.insertRow == null) {
/* 1063 */       this.insertRow = new ParamInfo[this.columnCount];
/*      */     }
/* 1065 */     this.onInsertRow = true;
/*      */   }
/*      */ 
/*      */   public void refreshRow() throws SQLException {
/* 1069 */     checkOpen();
/*      */ 
/* 1071 */     if (this.onInsertRow) {
/* 1072 */       throw new SQLException(Messages.get("error.resultset.insrow"), "24000");
/*      */     }
/*      */ 
/* 1075 */     cursorFetch(FETCH_REPEAT, 0);
/*      */   }
/*      */ 
/*      */   public void updateRow() throws SQLException {
/* 1079 */     checkOpen();
/* 1080 */     checkUpdateable();
/*      */ 
/* 1082 */     if (getCurrentRow() == null) {
/* 1083 */       throw new SQLException(Messages.get("error.resultset.norow"), "24000");
/*      */     }
/*      */ 
/* 1086 */     if (this.onInsertRow) {
/* 1087 */       throw new SQLException(Messages.get("error.resultset.insrow"), "24000");
/*      */     }
/*      */ 
/* 1090 */     if (this.updateRow != null)
/* 1091 */       cursor(CURSOR_OP_UPDATE, this.updateRow);
/*      */   }
/*      */ 
/*      */   public boolean first() throws SQLException
/*      */   {
/* 1096 */     checkOpen();
/* 1097 */     checkScrollable();
/*      */ 
/* 1099 */     this.pos = 1;
/* 1100 */     if (getCurrentRow() == null) {
/* 1101 */       return cursorFetch(FETCH_FIRST, 0);
/*      */     }
/* 1103 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean isLast()
/*      */     throws SQLException
/*      */   {
/* 1109 */     checkOpen();
/*      */ 
/* 1111 */     return ((this.pos == this.rowsInResult) && (this.rowsInResult != 0));
/*      */   }
/*      */ 
/*      */   public boolean last() throws SQLException {
/* 1115 */     checkOpen();
/* 1116 */     checkScrollable();
/*      */ 
/* 1118 */     this.pos = this.rowsInResult;
/* 1119 */     if ((this.asyncCursor) || (getCurrentRow() == null)) {
/* 1120 */       if (cursorFetch(FETCH_LAST, 0))
/*      */       {
/* 1122 */         this.pos = this.rowsInResult;
/* 1123 */         return true;
/*      */       }
/* 1125 */       return false;
/*      */     }
/*      */ 
/* 1128 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean next() throws SQLException
/*      */   {
/* 1133 */     checkOpen();
/*      */ 
/* 1135 */     this.pos += 1;
/* 1136 */     if (getCurrentRow() == null) {
/* 1137 */       return cursorFetch(FETCH_NEXT, 0);
/*      */     }
/* 1139 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean previous() throws SQLException
/*      */   {
/* 1144 */     checkOpen();
/* 1145 */     checkScrollable();
/*      */ 
/* 1148 */     if (this.pos == 0) {
/* 1149 */       return false;
/*      */     }
/*      */ 
/* 1153 */     int initPos = this.pos;
/*      */ 
/* 1155 */     this.pos -= 1;
/* 1156 */     if ((initPos == -1) || (getCurrentRow() == null)) {
/* 1157 */       boolean res = cursorFetch(FETCH_PREVIOUS, 0);
/* 1158 */       this.pos = ((initPos == -1) ? this.rowsInResult : initPos - 1);
/* 1159 */       return res;
/*      */     }
/* 1161 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean rowDeleted() throws SQLException
/*      */   {
/* 1166 */     checkOpen();
/*      */ 
/* 1168 */     Object[] currentRow = getCurrentRow();
/*      */ 
/* 1171 */     if (currentRow == null) {
/* 1172 */       return false;
/*      */     }
/*      */ 
/* 1176 */     if (SQL_ROW_DIRTY.equals(currentRow[(this.columns.length - 1)])) {
/* 1177 */       cursorFetch(FETCH_REPEAT, 0);
/* 1178 */       currentRow = getCurrentRow();
/*      */     }
/*      */ 
/* 1181 */     return SQL_ROW_DELETED.equals(currentRow[(this.columns.length - 1)]);
/*      */   }
/*      */ 
/*      */   public boolean rowInserted() throws SQLException {
/* 1185 */     checkOpen();
/*      */ 
/* 1187 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean rowUpdated() throws SQLException {
/* 1191 */     checkOpen();
/*      */ 
/* 1193 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean absolute(int row) throws SQLException {
/* 1197 */     checkOpen();
/* 1198 */     checkScrollable();
/*      */ 
/* 1200 */     this.pos = ((row >= 0) ? row : this.rowsInResult - row + 1);
/* 1201 */     if (getCurrentRow() == null) {
/* 1202 */       boolean result = cursorFetch(FETCH_ABSOLUTE, row);
/* 1203 */       if ((this.cursorPos == 1) && (row + this.rowsInResult < 0)) {
/* 1204 */         this.pos = 0;
/* 1205 */         result = false;
/*      */       }
/* 1207 */       return result;
/*      */     }
/* 1209 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean relative(int row) throws SQLException
/*      */   {
/* 1214 */     checkOpen();
/* 1215 */     checkScrollable();
/*      */ 
/* 1217 */     this.pos = ((this.pos == -1) ? this.rowsInResult + 1 + row : this.pos + row);
/* 1218 */     if (getCurrentRow() == null) {
/* 1219 */       if (this.pos < this.cursorPos)
/*      */       {
/* 1222 */         int savePos = this.pos;
/* 1223 */         boolean result = cursorFetch(FETCH_RELATIVE, this.pos - this.cursorPos - this.fetchSize + 1);
/*      */ 
/* 1225 */         if (result)
/* 1226 */           this.pos = savePos;
/*      */         else {
/* 1228 */           this.pos = 0;
/*      */         }
/* 1230 */         return result;
/*      */       }
/* 1232 */       return cursorFetch(FETCH_RELATIVE, this.pos - this.cursorPos);
/*      */     }
/*      */ 
/* 1235 */     return true;
/*      */   }
/*      */ 
/*      */   protected Object[] getCurrentRow()
/*      */   {
/* 1240 */     if ((this.pos < this.cursorPos) || (this.pos >= this.cursorPos + this.rowCache.length)) {
/* 1241 */       return null;
/*      */     }
/*      */ 
/* 1244 */     return this.rowCache[(this.pos - this.cursorPos)];
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.MSCursorResultSet
 * JD-Core Version:    0.5.3
 */