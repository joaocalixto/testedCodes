/*      */ package net.sourceforge.jtds.jdbc;
/*      */ 
/*      */ import java.sql.BatchUpdateException;
/*      */ import java.sql.Connection;
/*      */ import java.sql.ResultSet;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.SQLWarning;
/*      */ import java.sql.Statement;
/*      */ import java.util.ArrayList;
/*      */ import java.util.LinkedList;
/*      */ 
/*      */ public class JtdsStatement
/*      */   implements Statement
/*      */ {
/*      */   static final int RETURN_GENERATED_KEYS = 1;
/*      */   static final int NO_GENERATED_KEYS = 2;
/*      */   static final int CLOSE_CURRENT_RESULT = 1;
/*      */   static final int KEEP_CURRENT_RESULT = 2;
/*      */   static final int CLOSE_ALL_RESULTS = 3;
/*      */   static final int BOOLEAN = 16;
/*      */   static final int DATALINK = 70;
/*   70 */   static final Integer SUCCESS_NO_INFO = new Integer(-2);
/*   71 */   static final Integer EXECUTE_FAILED = new Integer(-3);
/*      */   static final int DEFAULT_FETCH_SIZE = 100;
/*      */   protected ConnectionJDBC2 connection;
/*      */   protected TdsCore tds;
/*      */   protected int queryTimeout;
/*      */   protected JtdsResultSet currentResult;
/*   83 */   private int updateCount = -1;
/*      */ 
/*   85 */   protected int fetchDirection = 1000;
/*      */ 
/*   87 */   protected int resultSetType = 1003;
/*      */ 
/*   89 */   protected int resultSetConcurrency = 1007;
/*      */ 
/*   93 */   protected int fetchSize = 100;
/*      */   protected String cursorName;
/*      */   protected boolean closed;
/*      */   protected int maxFieldSize;
/*      */   protected int maxRows;
/*  103 */   protected boolean escapeProcessing = true;
/*      */   protected final SQLDiagnostic messages;
/*      */   protected ArrayList batchValues;
/*      */   protected JtdsResultSet genKeyResultSet;
/*  114 */   protected final LinkedList resultQueue = new LinkedList();
/*      */   protected ArrayList openResultSets;
/*      */   protected ColInfo[] colMetaData;
/*      */ 
/*      */   JtdsStatement(ConnectionJDBC2 connection, int resultSetType, int resultSetConcurrency)
/*      */     throws SQLException
/*      */   {
/*      */     String method;
/*  133 */     if ((resultSetType < 1003) || (resultSetType > 1006))
/*      */     {
/*  136 */       if (this instanceof JtdsCallableStatement)
/*  137 */         method = "prepareCall";
/*  138 */       else if (this instanceof JtdsPreparedStatement)
/*  139 */         method = "prepareStatement";
/*      */       else {
/*  141 */         method = "createStatement";
/*      */       }
/*  143 */       throw new SQLException(Messages.get("error.generic.badparam", "resultSetType", method), "HY092");
/*      */     }
/*      */ 
/*  152 */     if ((resultSetConcurrency < 1007) || (resultSetConcurrency > 1010))
/*      */     {
/*  155 */       if (this instanceof JtdsCallableStatement)
/*  156 */         method = "prepareCall";
/*  157 */       else if (this instanceof JtdsPreparedStatement)
/*  158 */         method = "prepareStatement";
/*      */       else {
/*  160 */         method = "createStatement";
/*      */       }
/*  162 */       throw new SQLException(Messages.get("error.generic.badparam", "resultSetConcurrency", method), "HY092");
/*      */     }
/*      */ 
/*  169 */     this.connection = connection;
/*  170 */     this.resultSetType = resultSetType;
/*  171 */     this.resultSetConcurrency = resultSetConcurrency;
/*      */ 
/*  173 */     this.tds = connection.getCachedTds();
/*  174 */     if (this.tds == null) {
/*  175 */       this.messages = new SQLDiagnostic(connection.getServerType());
/*  176 */       this.tds = new TdsCore(this.connection, this.messages);
/*      */     } else {
/*  178 */       this.messages = this.tds.getMessages();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void finalize()
/*      */     throws Throwable
/*      */   {
/*  187 */     super.finalize();
/*      */     try {
/*  189 */       close();
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*      */     }
/*      */   }
/*      */ 
/*      */   TdsCore getTds()
/*      */   {
/*  201 */     return this.tds;
/*      */   }
/*      */ 
/*      */   SQLDiagnostic getMessages()
/*      */   {
/*  210 */     return this.messages;
/*      */   }
/*      */ 
/*      */   protected void checkOpen()
/*      */     throws SQLException
/*      */   {
/*  219 */     if ((this.closed) || (this.connection == null) || (this.connection.isClosed()))
/*  220 */       throw new SQLException(Messages.get("error.generic.closed", "Statement"), "HY010");
/*      */   }
/*      */ 
/*      */   protected void checkCursorException(SQLException e)
/*      */     throws SQLException
/*      */   {
/*  233 */     if ((this.connection == null) || (this.connection.isClosed()) || ("HYT00".equals(e.getSQLState())) || ("HY008".equals(e.getSQLState())))
/*      */     {
/*  238 */       throw e;
/*      */     }
/*  240 */     if (this.connection.getServerType() == 2)
/*      */     {
/*  242 */       return;
/*      */     }
/*      */ 
/*  247 */     int error = e.getErrorCode();
/*  248 */     if ((error >= 16900) && (error <= 16999))
/*      */     {
/*  251 */       return;
/*      */     }
/*  253 */     if (error == 6819)
/*      */     {
/*  255 */       return;
/*      */     }
/*  257 */     if (error == 8654)
/*      */     {
/*  259 */       return;
/*      */     }
/*  261 */     if (error == 8162)
/*      */     {
/*  265 */       return;
/*      */     }
/*      */ 
/*  271 */     throw e;
/*      */   }
/*      */ 
/*      */   static void notImplemented(String method)
/*      */     throws SQLException
/*      */   {
/*  281 */     throw new SQLException(Messages.get("error.generic.notimp", method), "HYC00");
/*      */   }
/*      */ 
/*      */   void closeCurrentResultSet()
/*      */     throws SQLException
/*      */   {
/*      */     try
/*      */     {
/*  290 */       if (this.currentResult != null) {
/*  291 */         this.currentResult.close();
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  296 */       this.currentResult = null;
/*      */     }
/*      */   }
/*      */ 
/*      */   void closeAllResultSets()
/*      */     throws SQLException
/*      */   {
/*      */     try
/*      */     {
/*  305 */       if (this.openResultSets != null) {
/*  306 */         for (int i = 0; i < this.openResultSets.size(); ++i) {
/*  307 */           JtdsResultSet rs = (JtdsResultSet)this.openResultSets.get(i);
/*  308 */           if (rs != null) {
/*  309 */             rs.close();
/*      */           }
/*      */         }
/*      */       }
/*  313 */       closeCurrentResultSet();
/*      */     } finally {
/*  315 */       this.openResultSets = null;
/*      */     }
/*      */   }
/*      */ 
/*      */   void addWarning(SQLWarning w)
/*      */   {
/*  325 */     this.messages.addWarning(w);
/*      */   }
/*      */ 
/*      */   protected SQLException executeMSBatch(int size, int executeSize, ArrayList counts)
/*      */     throws SQLException
/*      */   {
/*  338 */     SQLException sqlEx = null;
/*  339 */     for (int i = 0; i < size; ) {
/*  340 */       Object value = this.batchValues.get(i);
/*  341 */       ++i;
/*      */ 
/*  343 */       boolean executeNow = (i % executeSize == 0) || (i == size);
/*      */ 
/*  345 */       this.tds.startBatch();
/*  346 */       this.tds.executeSQL((String)value, null, null, false, 0, -1, -1, executeNow);
/*      */ 
/*  349 */       if (executeNow) {
/*  350 */         sqlEx = this.tds.getBatchCounts(counts, sqlEx);
/*      */ 
/*  354 */         if ((sqlEx != null) && (counts.size() != i)) {
/*      */           break;
/*      */         }
/*      */       }
/*      */     }
/*  359 */     return sqlEx;
/*      */   }
/*      */ 
/*      */   protected SQLException executeSybaseBatch(int size, int executeSize, ArrayList counts)
/*      */     throws SQLException
/*      */   {
/*  375 */     StringBuffer sql = new StringBuffer(size * 32);
/*  376 */     SQLException sqlEx = null;
/*      */ 
/*  378 */     for (int i = 0; i < size; ) {
/*  379 */       Object value = this.batchValues.get(i);
/*  380 */       ++i;
/*      */ 
/*  382 */       boolean executeNow = (i % executeSize == 0) || (i == size);
/*      */ 
/*  384 */       sql.append((String)value).append(' ');
/*      */ 
/*  386 */       if (executeNow) {
/*  387 */         this.tds.executeSQL(sql.toString(), null, null, false, 0, -1, -1, true);
/*  388 */         sql.setLength(0);
/*      */ 
/*  390 */         sqlEx = this.tds.getBatchCounts(counts, sqlEx);
/*      */ 
/*  394 */         if ((sqlEx != null) && (counts.size() != i)) {
/*      */           break;
/*      */         }
/*      */       }
/*      */     }
/*  399 */     return sqlEx;
/*      */   }
/*      */ 
/*      */   protected ResultSet executeSQLQuery(String sql, String spName, ParamInfo[] params, boolean useCursor)
/*      */     throws SQLException
/*      */   {
/*  416 */     String warningMessage = null;
/*      */ 
/*  421 */     if (useCursor);
/*      */     try
/*      */     {
/*  423 */       if (this.connection.getServerType() == 1) {
/*  424 */         this.currentResult = new MSCursorResultSet(this, sql, spName, params, this.resultSetType, this.resultSetConcurrency);
/*      */ 
/*  432 */         return this.currentResult;
/*      */       }
/*      */ 
/*  435 */       this.currentResult = new CachedResultSet(this, sql, spName, params, this.resultSetType, this.resultSetConcurrency);
/*      */ 
/*  443 */       return this.currentResult;
/*      */     }
/*      */     catch (SQLException e) {
/*  446 */       checkCursorException(e);
/*  447 */       warningMessage = '[' + e.getSQLState() + "] " + e.getMessage();
/*      */ 
/*  454 */       if ((spName != null) && (this.connection.getUseMetadataCache()) && (this.connection.getPrepareSql() == 3) && (this.colMetaData != null) && (this.connection.getServerType() == 1))
/*      */       {
/*  461 */         this.tds.setColumns(this.colMetaData);
/*  462 */         this.tds.executeSQL(sql, spName, params, true, this.queryTimeout, this.maxRows, this.maxFieldSize, true);
/*      */       }
/*      */       else {
/*  465 */         this.tds.executeSQL(sql, spName, params, false, this.queryTimeout, this.maxRows, this.maxFieldSize, true);
/*      */       }
/*      */ 
/*  470 */       if (warningMessage != null) {
/*  471 */         addWarning(new SQLWarning(Messages.get("warning.cursordowngraded", warningMessage), "01000"));
/*      */       }
/*      */ 
/*  477 */       while ((!(this.tds.getMoreResults())) && (!(this.tds.isEndOfResponse())));
/*  480 */       this.messages.checkErrors();
/*      */ 
/*  482 */       if (this.tds.isResultSet()) {
/*  483 */         this.currentResult = new JtdsResultSet(this, 1003, 1007, this.tds.getColumns());
/*      */       }
/*      */       else
/*      */       {
/*  488 */         throw new SQLException(Messages.get("error.statement.noresult"), "24000");
/*      */       }
/*      */     }
/*      */ 
/*  492 */     return this.currentResult;
/*      */   }
/*      */ 
/*      */   protected boolean executeSQL(String sql, String spName, ParamInfo[] params, boolean returnKeys, boolean update, boolean useCursor)
/*      */     throws SQLException
/*      */   {
/*  515 */     String warningMessage = null;
/*      */ 
/*  521 */     if ((this.connection.getServerType() == 1) && (!(update)) && (useCursor));
/*      */     try
/*      */     {
/*  523 */       this.currentResult = new MSCursorResultSet(this, sql, spName, params, this.resultSetType, this.resultSetConcurrency);
/*      */ 
/*  526 */       return true;
/*      */     } catch (SQLException nextResult) {
/*  528 */       checkCursorException(e);
/*  529 */       warningMessage = '[' + e.getSQLState() + "] " + e.getMessage();
/*      */ 
/*  537 */       this.tds.executeSQL(sql, spName, params, false, this.queryTimeout, this.maxRows, this.maxFieldSize, true);
/*      */ 
/*  540 */       if (warningMessage != null)
/*      */       {
/*  542 */         addWarning(new SQLWarning(Messages.get("warning.cursordowngraded", warningMessage), "01000"));
/*      */       }
/*      */ 
/*  546 */       if (processResults(returnKeys, update)) {
/*  547 */         Object nextResult = this.resultQueue.removeFirst();
/*      */ 
/*  550 */         if (nextResult instanceof Integer) {
/*  551 */           this.updateCount = ((Integer)nextResult).intValue();
/*  552 */           return false;
/*      */         }
/*      */ 
/*  556 */         this.currentResult = ((JtdsResultSet)nextResult);
/*  557 */         return true; }
/*      */     }
/*  559 */     return false;
/*      */   }
/*      */ 
/*      */   private boolean processResults(boolean returnKeys, boolean update)
/*      */     throws SQLException
/*      */   {
/*  578 */     if (!(this.resultQueue.isEmpty())) {
/*  579 */       throw new IllegalStateException("There should be no queued results.");
/*      */     }
/*      */     while (true)
/*      */     {
/*  583 */       if (this.tds.isEndOfResponse()) break label212;
/*  584 */       if (!(this.tds.getMoreResults())) {
/*  585 */         if (this.tds.isUpdateCount());
/*  586 */         if ((update) && (this.connection.getLastUpdateCount())) {
/*  587 */           this.resultQueue.clear();
/*      */         }
/*  589 */         this.resultQueue.addLast(new Integer(this.tds.getUpdateCount()));
/*      */       }
/*      */ 
/*  592 */       if (!(returnKeys)) {
/*      */         break;
/*      */       }
/*  595 */       if (this.tds.getNextRow());
/*  596 */       this.genKeyResultSet = new CachedResultSet(this, this.tds.getColumns(), this.tds.getRowData());
/*      */     }
/*      */ 
/*  601 */     if ((update) && (this.resultQueue.isEmpty()))
/*      */     {
/*  603 */       SQLException ex = new SQLException(Messages.get("error.statement.nocount"), "07000");
/*      */ 
/*  605 */       ex.setNextException(this.messages.exceptions);
/*  606 */       throw ex;
/*      */     }
/*      */ 
/*  609 */     this.resultQueue.add(new JtdsResultSet(this, 1003, 1007, this.tds.getColumns()));
/*      */ 
/*  620 */     label212: getMessages().checkErrors();
/*      */ 
/*  622 */     return (!(this.resultQueue.isEmpty()));
/*      */   }
/*      */ 
/*      */   protected void cacheResults()
/*      */     throws SQLException
/*      */   {
/*  632 */     processResults(false, false);
/*      */   }
/*      */ 
/*      */   protected void initialize()
/*      */     throws SQLException
/*      */   {
/*  642 */     this.updateCount = -1;
/*  643 */     this.resultQueue.clear();
/*  644 */     this.genKeyResultSet = null;
/*  645 */     this.tds.clearResponseQueue();
/*      */ 
/*  647 */     this.messages.exceptions = null;
/*  648 */     this.messages.clearWarnings();
/*  649 */     closeAllResultSets();
/*      */   }
/*      */ 
/*      */   private boolean executeImpl(String sql, int autoGeneratedKeys, boolean update)
/*      */     throws SQLException
/*      */   {
/*  675 */     initialize();
/*      */ 
/*  677 */     if ((sql == null) || (sql.length() == 0)) {
/*  678 */       throw new SQLException(Messages.get("error.generic.nosql"), "HY000");
/*      */     }
/*      */ 
/*  682 */     String sqlWord = "";
/*  683 */     if (this.escapeProcessing) {
/*  684 */       String[] tmp = SQLParser.parse(sql, null, this.connection, false);
/*      */ 
/*  686 */       if (tmp[1].length() != 0) {
/*  687 */         throw new SQLException(Messages.get("error.statement.badsql"), "07000");
/*      */       }
/*      */ 
/*  691 */       sql = tmp[0];
/*  692 */       sqlWord = tmp[2];
/*      */     }
/*      */     else
/*      */     {
/*  696 */       sql = sql.trim();
/*  697 */       if (sql.length() > 5)
/*  698 */         sqlWord = sql.substring(0, 6).toLowerCase();
/*      */     }
/*      */     boolean returnKeys;
/*  702 */     if (autoGeneratedKeys == 1)
/*  703 */       returnKeys = "insert".equals(sqlWord);
/*  704 */     else if (autoGeneratedKeys == 2)
/*  705 */       returnKeys = false;
/*      */     else {
/*  707 */       throw new SQLException(Messages.get("error.generic.badoption", Integer.toString(autoGeneratedKeys), "autoGeneratedKeys"), "HY092");
/*      */     }
/*      */ 
/*  714 */     if (returnKeys) {
/*  715 */       if ((this.connection.getServerType() == 1) && (this.connection.getDatabaseMajorVersion() >= 8))
/*      */       {
/*  717 */         sql = sql + " SELECT SCOPE_IDENTITY() AS ID";
/*      */       }
/*      */       else sql = sql + " SELECT @@IDENTITY AS ID";
/*      */ 
/*      */     }
/*      */ 
/*  723 */     return executeSQL(sql, null, null, returnKeys, update, (!(update)) && (useCursor(returnKeys, sqlWord)));
/*      */   }
/*      */ 
/*      */   protected boolean useCursor(boolean returnKeys, String sqlWord)
/*      */   {
/*  742 */     return ((((this.resultSetType != 1003) || (this.resultSetConcurrency != 1007) || (this.connection.getUseCursors()) || (this.cursorName != null))) && (!(returnKeys)) && (((sqlWord == null) || ("select".equals(sqlWord)) || (sqlWord.startsWith("exec")))));
/*      */   }
/*      */ 
/*      */   int getDefaultFetchSize()
/*      */   {
/*  756 */     return (((0 < this.maxRows) && (this.maxRows < 100)) ? this.maxRows : 100);
/*      */   }
/*      */ 
/*      */   public int getFetchDirection()
/*      */     throws SQLException
/*      */   {
/*  763 */     checkOpen();
/*      */ 
/*  765 */     return this.fetchDirection;
/*      */   }
/*      */ 
/*      */   public int getFetchSize() throws SQLException {
/*  769 */     checkOpen();
/*      */ 
/*  771 */     return this.fetchSize;
/*      */   }
/*      */ 
/*      */   public int getMaxFieldSize() throws SQLException {
/*  775 */     checkOpen();
/*      */ 
/*  777 */     return this.maxFieldSize;
/*      */   }
/*      */ 
/*      */   public int getMaxRows() throws SQLException {
/*  781 */     checkOpen();
/*      */ 
/*  783 */     return this.maxRows;
/*      */   }
/*      */ 
/*      */   public int getQueryTimeout() throws SQLException {
/*  787 */     checkOpen();
/*      */ 
/*  789 */     return this.queryTimeout;
/*      */   }
/*      */ 
/*      */   public int getResultSetConcurrency() throws SQLException {
/*  793 */     checkOpen();
/*      */ 
/*  795 */     return this.resultSetConcurrency;
/*      */   }
/*      */ 
/*      */   public int getResultSetHoldability() throws SQLException {
/*  799 */     checkOpen();
/*      */ 
/*  801 */     return 1;
/*      */   }
/*      */ 
/*      */   public int getResultSetType() throws SQLException {
/*  805 */     checkOpen();
/*      */ 
/*  807 */     return this.resultSetType;
/*      */   }
/*      */ 
/*      */   public int getUpdateCount() throws SQLException {
/*  811 */     checkOpen();
/*      */ 
/*  813 */     return this.updateCount;
/*      */   }
/*      */ 
/*      */   public void cancel() throws SQLException {
/*  817 */     checkOpen();
/*      */ 
/*  819 */     if (this.tds != null)
/*  820 */       this.tds.cancel(false);
/*      */   }
/*      */ 
/*      */   public void clearBatch() throws SQLException
/*      */   {
/*  825 */     checkOpen();
/*      */ 
/*  827 */     if (this.batchValues != null)
/*  828 */       this.batchValues.clear();
/*      */   }
/*      */ 
/*      */   public void clearWarnings() throws SQLException
/*      */   {
/*  833 */     checkOpen();
/*      */ 
/*  835 */     this.messages.clearWarnings();
/*      */   }
/*      */ 
/*      */   public void close() throws SQLException {
/*  839 */     if (!(this.closed)) {
/*  840 */       SQLException closeEx = null;
/*  841 */       SQLException releaseEx = null;
/*      */       try
/*      */       {
/*      */         try {
/*  845 */           closeAllResultSets();
/*      */         } catch (SQLException ex) {
/*  847 */           if ((!("HYT00".equals(ex.getSQLState()))) && (!("HY008".equals(ex.getSQLState()))))
/*      */           {
/*  850 */             closeEx = ex;
/*      */           }
/*      */         } finally {
/*      */           try {
/*  854 */             if (!(this.connection.isClosed())) {
/*  855 */               this.connection.releaseTds(this.tds);
/*      */             }
/*      */ 
/*  858 */             this.tds.getMessages().checkErrors();
/*      */           }
/*      */           catch (SQLException ex) {
/*  861 */             releaseEx = ex;
/*      */           }
/*      */           finally {
/*  864 */             this.tds = null;
/*  865 */             this.connection.removeStatement(this);
/*  866 */             this.connection = null;
/*      */           }
/*      */         }
/*      */       }
/*      */       catch (NullPointerException npe) {
/*      */       }
/*      */       finally {
/*  873 */         this.closed = true;
/*      */ 
/*  876 */         if (releaseEx != null)
/*      */         {
/*  878 */           if (closeEx != null) {
/*  879 */             releaseEx.setNextException(closeEx);
/*      */           }
/*  881 */           throw releaseEx;
/*      */         }
/*      */ 
/*  884 */         if (closeEx != null)
/*  885 */           throw closeEx;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean getMoreResults() throws SQLException
/*      */   {
/*  892 */     checkOpen();
/*      */ 
/*  894 */     return getMoreResults(3);
/*      */   }
/*      */ 
/*      */   public int[] executeBatch()
/*      */     throws SQLException, BatchUpdateException
/*      */   {
/*  914 */     checkOpen();
/*  915 */     initialize();
/*      */ 
/*  917 */     if ((this.batchValues == null) || (this.batchValues.size() == 0)) {
/*  918 */       return new int[0];
/*      */     }
/*      */ 
/*  921 */     int size = this.batchValues.size();
/*  922 */     int executeSize = this.connection.getBatchSize();
/*  923 */     executeSize = (executeSize == 0) ? 2147483647 : executeSize;
/*      */ 
/*  925 */     ArrayList counts = new ArrayList(size);
/*      */     try
/*      */     {
/*      */       SQLException sqlEx;
/*  931 */       synchronized (this.connection) {
/*  932 */         if ((this.connection.getServerType() == 2) && (this.connection.getTdsVersion() == 2))
/*      */         {
/*  934 */           sqlEx = executeSybaseBatch(size, executeSize, counts);
/*      */         }
/*      */         else sqlEx = executeMSBatch(size, executeSize, counts);
/*      */ 
/*      */       }
/*      */ 
/*  941 */       int[] updateCounts = new int[size];
/*      */ 
/*  943 */       int results = counts.size();
/*  944 */       for (int i = 0; (i < results) && (i < size); ++i) {
/*  945 */         updateCounts[i] = ((Integer)counts.get(i)).intValue();
/*      */       }
/*      */ 
/*  948 */       for (i = results; i < updateCounts.length; ++i) {
/*  949 */         updateCounts[i] = EXECUTE_FAILED.intValue();
/*      */       }
/*      */ 
/*  953 */       if (sqlEx != null) {
/*  954 */         batchEx = new BatchUpdateException(sqlEx.getMessage(), sqlEx.getSQLState(), sqlEx.getErrorCode(), updateCounts);
/*      */ 
/*  960 */         batchEx.setNextException(sqlEx.getNextException());
/*  961 */         throw batchEx;
/*      */       }
/*  963 */       BatchUpdateException batchEx = updateCounts;
/*      */ 
/*  979 */       return batchEx; } catch (BatchUpdateException ex) { } catch (SQLException ex) { } finally { clearBatch();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setFetchDirection(int direction) throws SQLException {
/*  984 */     checkOpen();
/*  985 */     switch (direction)
/*      */     {
/*      */     case 1000:
/*      */     case 1001:
/*      */     case 1002:
/*  989 */       this.fetchDirection = direction;
/*  990 */       break;
/*      */     default:
/*  993 */       throw new SQLException(Messages.get("error.generic.badoption", Integer.toString(direction), "direction"), "24000");
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setFetchSize(int rows)
/*      */     throws SQLException
/*      */   {
/* 1002 */     checkOpen();
/*      */ 
/* 1004 */     if (rows < 0) {
/* 1005 */       throw new SQLException(Messages.get("error.generic.optltzero", "setFetchSize"), "HY092");
/*      */     }
/*      */ 
/* 1008 */     if ((this.maxRows > 0) && (rows > this.maxRows)) {
/* 1009 */       throw new SQLException(Messages.get("error.statement.gtmaxrows"), "HY092");
/*      */     }
/*      */ 
/* 1012 */     if (rows == 0) {
/* 1013 */       rows = getDefaultFetchSize();
/*      */     }
/* 1015 */     this.fetchSize = rows;
/*      */   }
/*      */ 
/*      */   public void setMaxFieldSize(int max) throws SQLException {
/* 1019 */     checkOpen();
/*      */ 
/* 1021 */     if (max < 0) {
/* 1022 */       throw new SQLException(Messages.get("error.generic.optltzero", "setMaxFieldSize"), "HY092");
/*      */     }
/*      */ 
/* 1027 */     this.maxFieldSize = max;
/*      */   }
/*      */ 
/*      */   public void setMaxRows(int max) throws SQLException {
/* 1031 */     checkOpen();
/*      */ 
/* 1033 */     if (max < 0) {
/* 1034 */       throw new SQLException(Messages.get("error.generic.optltzero", "setMaxRows"), "HY092");
/*      */     }
/*      */ 
/* 1038 */     if ((max > 0) && (max < this.fetchSize))
/*      */     {
/* 1040 */       this.fetchSize = max;
/*      */     }
/* 1042 */     this.maxRows = max;
/*      */   }
/*      */ 
/*      */   public void setQueryTimeout(int seconds) throws SQLException {
/* 1046 */     checkOpen();
/*      */ 
/* 1048 */     if (seconds < 0) {
/* 1049 */       throw new SQLException(Messages.get("error.generic.optltzero", "setQueryTimeout"), "HY092");
/*      */     }
/*      */ 
/* 1054 */     this.queryTimeout = seconds;
/*      */   }
/*      */ 
/*      */   public boolean getMoreResults(int current) throws SQLException {
/* 1058 */     checkOpen();
/*      */ 
/* 1060 */     switch (current)
/*      */     {
/*      */     case 3:
/* 1062 */       this.updateCount = -1;
/* 1063 */       closeAllResultSets();
/* 1064 */       break;
/*      */     case 1:
/* 1066 */       this.updateCount = -1;
/* 1067 */       closeCurrentResultSet();
/* 1068 */       break;
/*      */     case 2:
/* 1070 */       this.updateCount = -1;
/*      */ 
/* 1075 */       if (this.openResultSets == null) {
/* 1076 */         this.openResultSets = new ArrayList();
/*      */       }
/* 1078 */       if ((this.currentResult instanceof MSCursorResultSet) || (this.currentResult instanceof CachedResultSet))
/*      */       {
/* 1083 */         this.openResultSets.add(this.currentResult);
/* 1084 */       } else if (this.currentResult != null) {
/* 1085 */         this.currentResult.cacheResultSetRows();
/* 1086 */         this.openResultSets.add(this.currentResult);
/*      */       }
/* 1088 */       this.currentResult = null;
/* 1089 */       break;
/*      */     default:
/* 1091 */       throw new SQLException(Messages.get("error.generic.badoption", Integer.toString(current), "current"), "HY092");
/*      */     }
/*      */ 
/* 1099 */     this.messages.checkErrors();
/*      */ 
/* 1102 */     if ((!(this.resultQueue.isEmpty())) || (processResults(false, false))) {
/* 1103 */       Object nextResult = this.resultQueue.removeFirst();
/*      */ 
/* 1106 */       if (nextResult instanceof Integer) {
/* 1107 */         this.updateCount = ((Integer)nextResult).intValue();
/* 1108 */         return false;
/*      */       }
/*      */ 
/* 1112 */       this.currentResult = ((JtdsResultSet)nextResult);
/* 1113 */       return true;
/*      */     }
/* 1115 */     return false;
/*      */   }
/*      */ 
/*      */   public void setEscapeProcessing(boolean enable) throws SQLException
/*      */   {
/* 1120 */     checkOpen();
/*      */ 
/* 1122 */     this.escapeProcessing = enable;
/*      */   }
/*      */ 
/*      */   public int executeUpdate(String sql) throws SQLException {
/* 1126 */     return executeUpdate(sql, 2);
/*      */   }
/*      */ 
/*      */   public void addBatch(String sql) throws SQLException {
/* 1130 */     checkOpen();
/*      */ 
/* 1132 */     if (sql == null) {
/* 1133 */       throw new NullPointerException();
/*      */     }
/*      */ 
/* 1136 */     if (this.batchValues == null) {
/* 1137 */       this.batchValues = new ArrayList();
/*      */     }
/*      */ 
/* 1140 */     if (this.escapeProcessing) {
/* 1141 */       String[] tmp = SQLParser.parse(sql, null, this.connection, false);
/*      */ 
/* 1143 */       if (tmp[1].length() != 0) {
/* 1144 */         throw new SQLException(Messages.get("error.statement.badsql"), "07000");
/*      */       }
/*      */ 
/* 1148 */       sql = tmp[0];
/*      */     }
/*      */ 
/* 1151 */     this.batchValues.add(sql);
/*      */   }
/*      */ 
/*      */   public void setCursorName(String name) throws SQLException {
/* 1155 */     checkOpen();
/* 1156 */     this.cursorName = name;
/* 1157 */     if (name == null)
/*      */       return;
/* 1159 */     this.resultSetType = 1003;
/* 1160 */     this.fetchSize = 1;
/*      */   }
/*      */ 
/*      */   public boolean execute(String sql) throws SQLException
/*      */   {
/* 1165 */     checkOpen();
/*      */ 
/* 1167 */     return executeImpl(sql, 2, false);
/*      */   }
/*      */ 
/*      */   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
/* 1171 */     checkOpen();
/*      */ 
/* 1173 */     executeImpl(sql, autoGeneratedKeys, true);
/*      */ 
/* 1175 */     int res = getUpdateCount();
/* 1176 */     return ((res == -1) ? 0 : res);
/*      */   }
/*      */ 
/*      */   public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
/* 1180 */     checkOpen();
/*      */ 
/* 1182 */     return executeImpl(sql, autoGeneratedKeys, false);
/*      */   }
/*      */ 
/*      */   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
/* 1186 */     checkOpen();
/*      */ 
/* 1188 */     if (columnIndexes == null) {
/* 1189 */       throw new SQLException(Messages.get("error.generic.nullparam", "executeUpdate"), "HY092");
/*      */     }
/* 1191 */     if (columnIndexes.length != 1) {
/* 1192 */       throw new SQLException(Messages.get("error.generic.needcolindex", "executeUpdate"), "HY092");
/*      */     }
/*      */ 
/* 1196 */     return executeUpdate(sql, 1);
/*      */   }
/*      */ 
/*      */   public boolean execute(String sql, int[] columnIndexes) throws SQLException {
/* 1200 */     checkOpen();
/*      */ 
/* 1202 */     if (columnIndexes == null) {
/* 1203 */       throw new SQLException(Messages.get("error.generic.nullparam", "execute"), "HY092");
/*      */     }
/* 1205 */     if (columnIndexes.length != 1) {
/* 1206 */       throw new SQLException(Messages.get("error.generic.needcolindex", "execute"), "HY092");
/*      */     }
/*      */ 
/* 1210 */     return executeImpl(sql, 1, false);
/*      */   }
/*      */ 
/*      */   public Connection getConnection() throws SQLException {
/* 1214 */     checkOpen();
/*      */ 
/* 1216 */     return this.connection;
/*      */   }
/*      */ 
/*      */   public ResultSet getGeneratedKeys() throws SQLException {
/* 1220 */     checkOpen();
/*      */ 
/* 1222 */     if (this.genKeyResultSet == null) {
/* 1223 */       String[] colNames = { "ID" };
/* 1224 */       int[] colTypes = { 4 };
/*      */ 
/* 1228 */       CachedResultSet rs = new CachedResultSet(this, colNames, colTypes);
/* 1229 */       rs.setConcurrency(1007);
/* 1230 */       this.genKeyResultSet = rs;
/*      */     }
/*      */ 
/* 1233 */     return this.genKeyResultSet;
/*      */   }
/*      */ 
/*      */   public ResultSet getResultSet() throws SQLException {
/* 1237 */     checkOpen();
/*      */ 
/* 1239 */     if ((this.currentResult instanceof MSCursorResultSet) || (this.currentResult instanceof CachedResultSet))
/*      */     {
/* 1241 */       return this.currentResult;
/*      */     }
/*      */ 
/* 1246 */     if ((this.currentResult == null) || ((this.resultSetType == 1003) && (this.resultSetConcurrency == 1007)))
/*      */     {
/* 1249 */       return this.currentResult;
/*      */     }
/*      */ 
/* 1254 */     this.currentResult = new CachedResultSet(this.currentResult, true);
/*      */ 
/* 1256 */     return this.currentResult;
/*      */   }
/*      */ 
/*      */   public SQLWarning getWarnings() throws SQLException {
/* 1260 */     checkOpen();
/*      */ 
/* 1262 */     return this.messages.getWarnings();
/*      */   }
/*      */ 
/*      */   public int executeUpdate(String sql, String[] columnNames) throws SQLException {
/* 1266 */     checkOpen();
/*      */ 
/* 1268 */     if (columnNames == null) {
/* 1269 */       throw new SQLException(Messages.get("error.generic.nullparam", "executeUpdate"), "HY092");
/*      */     }
/* 1271 */     if (columnNames.length != 1) {
/* 1272 */       throw new SQLException(Messages.get("error.generic.needcolname", "executeUpdate"), "HY092");
/*      */     }
/*      */ 
/* 1276 */     return executeUpdate(sql, 1);
/*      */   }
/*      */ 
/*      */   public boolean execute(String sql, String[] columnNames) throws SQLException {
/* 1280 */     checkOpen();
/*      */ 
/* 1282 */     if (columnNames == null) {
/* 1283 */       throw new SQLException(Messages.get("error.generic.nullparam", "execute"), "HY092");
/*      */     }
/* 1285 */     if (columnNames.length != 1) {
/* 1286 */       throw new SQLException(Messages.get("error.generic.needcolname", "execute"), "HY092");
/*      */     }
/*      */ 
/* 1290 */     return executeImpl(sql, 1, false);
/*      */   }
/*      */ 
/*      */   public ResultSet executeQuery(String sql) throws SQLException {
/* 1294 */     checkOpen();
/* 1295 */     initialize();
/*      */ 
/* 1297 */     if ((sql == null) || (sql.length() == 0)) {
/* 1298 */       throw new SQLException(Messages.get("error.generic.nosql"), "HY000");
/*      */     }
/* 1300 */     if (this.escapeProcessing) {
/* 1301 */       String[] tmp = SQLParser.parse(sql, null, this.connection, false);
/*      */ 
/* 1303 */       if (tmp[1].length() != 0) {
/* 1304 */         throw new SQLException(Messages.get("error.statement.badsql"), "07000");
/*      */       }
/*      */ 
/* 1308 */       sql = tmp[0];
/*      */     }
/*      */ 
/* 1311 */     return executeSQLQuery(sql, null, null, useCursor(false, null));
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.JtdsStatement
 * JD-Core Version:    0.5.3
 */