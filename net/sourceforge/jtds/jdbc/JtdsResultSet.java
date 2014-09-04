/*      */ package net.sourceforge.jtds.jdbc;
/*      */ 
/*      */ import java.io.InputStream;
/*      */ import java.io.InputStreamReader;
/*      */ import java.io.Reader;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.math.BigDecimal;
/*      */ import java.net.MalformedURLException;
/*      */ import java.net.URL;
/*      */ import java.sql.Array;
/*      */ import java.sql.Blob;
/*      */ import java.sql.Clob;
/*      */ import java.sql.Date;
/*      */ import java.sql.Ref;
/*      */ import java.sql.ResultSet;
/*      */ import java.sql.ResultSetMetaData;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.SQLWarning;
/*      */ import java.sql.Statement;
/*      */ import java.sql.Time;
/*      */ import java.sql.Timestamp;
/*      */ import java.text.NumberFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Calendar;
/*      */ import java.util.HashMap;
/*      */ import java.util.Map;
/*      */ import net.sourceforge.jtds.util.BlobBuffer;
/*      */ 
/*      */ public class JtdsResultSet
/*      */   implements ResultSet
/*      */ {
/*      */   static final int HOLD_CURSORS_OVER_COMMIT = 1;
/*      */   static final int CLOSE_CURSORS_AT_COMMIT = 2;
/*      */   protected static final int POS_BEFORE_FIRST = 0;
/*      */   protected static final int POS_AFTER_LAST = -1;
/*      */   protected static final int INITIAL_ROW_COUNT = 1000;
/*   65 */   protected int pos = 0;
/*      */   protected int rowsInResult;
/*   69 */   protected int direction = 1000;
/*      */   protected int resultSetType;
/*      */   protected int concurrency;
/*      */   protected int columnCount;
/*      */   protected ColInfo[] columns;
/*      */   protected Object[] currentRow;
/*      */   protected ArrayList rowData;
/*      */   protected int rowPtr;
/*      */   protected boolean wasNull;
/*      */   protected JtdsStatement statement;
/*      */   protected boolean closed;
/*      */   protected boolean cancelled;
/*   93 */   protected int fetchDirection = 1000;
/*      */   protected int fetchSize;
/*      */   protected String cursorName;
/*      */   private HashMap columnMap;
/*  105 */   private static NumberFormat f = NumberFormat.getInstance();
/*      */ 
/*      */   JtdsResultSet(JtdsStatement statement, int resultSetType, int concurrency, ColInfo[] columns)
/*      */     throws SQLException
/*      */   {
/*  121 */     if (statement == null) {
/*  122 */       throw new IllegalArgumentException("Statement parameter must not be null");
/*      */     }
/*  124 */     this.statement = statement;
/*  125 */     this.resultSetType = resultSetType;
/*  126 */     this.concurrency = concurrency;
/*  127 */     this.columns = columns;
/*  128 */     this.fetchSize = statement.fetchSize;
/*  129 */     this.fetchDirection = statement.fetchDirection;
/*  130 */     this.cursorName = statement.cursorName;
/*      */ 
/*  132 */     if (columns != null) {
/*  133 */       this.columnCount = getColumnCount(columns);
/*  134 */       this.rowsInResult = ((statement.getTds().isDataInResultSet()) ? 1 : 0);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static int getColumnCount(ColInfo[] columns)
/*      */   {
/*  147 */     for (int i = columns.length - 1; (i >= 0) && (columns[i].isHidden); --i);
/*  148 */     return (i + 1);
/*      */   }
/*      */ 
/*      */   protected ColInfo[] getColumns()
/*      */   {
/*  157 */     return this.columns;
/*      */   }
/*      */ 
/*      */   protected void setColName(int colIndex, String name)
/*      */   {
/*  167 */     if ((colIndex < 1) || (colIndex > this.columns.length)) {
/*  168 */       throw new IllegalArgumentException("columnIndex " + colIndex + " invalid");
/*      */     }
/*      */ 
/*  172 */     this.columns[(colIndex - 1)].realName = name;
/*      */   }
/*      */ 
/*      */   protected void setColLabel(int colIndex, String name)
/*      */   {
/*  182 */     if ((colIndex < 1) || (colIndex > this.columns.length)) {
/*  183 */       throw new IllegalArgumentException("columnIndex " + colIndex + " invalid");
/*      */     }
/*      */ 
/*  187 */     this.columns[(colIndex - 1)].name = name;
/*      */   }
/*      */ 
/*      */   protected void setColType(int colIndex, int jdbcType)
/*      */   {
/*  197 */     if ((colIndex < 1) || (colIndex > this.columns.length)) {
/*  198 */       throw new IllegalArgumentException("columnIndex " + colIndex + " invalid");
/*      */     }
/*      */ 
/*  202 */     this.columns[(colIndex - 1)].jdbcType = jdbcType;
/*      */   }
/*      */ 
/*      */   protected Object setColValue(int colIndex, int jdbcType, Object value, int length)
/*      */     throws SQLException
/*      */   {
/*  215 */     checkOpen();
/*  216 */     checkUpdateable();
/*  217 */     if ((colIndex < 1) || (colIndex > this.columnCount)) {
/*  218 */       throw new SQLException(Messages.get("error.resultset.colindex", Integer.toString(colIndex)), "07009");
/*      */     }
/*      */ 
/*  225 */     if (value instanceof Timestamp)
/*  226 */       value = new DateTime((Timestamp)value);
/*  227 */     else if (value instanceof Date)
/*  228 */       value = new DateTime((Date)value);
/*  229 */     else if (value instanceof Time) {
/*  230 */       value = new DateTime((Time)value);
/*      */     }
/*      */ 
/*  233 */     return value;
/*      */   }
/*      */ 
/*      */   protected void setColumnCount(int columnCount)
/*      */   {
/*  242 */     if ((columnCount < 1) || (columnCount > this.columns.length)) {
/*  243 */       throw new IllegalArgumentException("columnCount " + columnCount + " is invalid");
/*      */     }
/*      */ 
/*  247 */     this.columnCount = columnCount;
/*      */   }
/*      */ 
/*      */   protected Object getColumn(int index)
/*      */     throws SQLException
/*      */   {
/*  261 */     checkOpen();
/*      */ 
/*  263 */     if ((index < 1) || (index > this.columnCount)) {
/*  264 */       throw new SQLException(Messages.get("error.resultset.colindex", Integer.toString(index)), "07009");
/*      */     }
/*      */ 
/*  269 */     if (this.currentRow == null) {
/*  270 */       throw new SQLException(Messages.get("error.resultset.norow"), "24000");
/*      */     }
/*      */ 
/*  273 */     Object data = this.currentRow[(index - 1)];
/*      */ 
/*  275 */     this.wasNull = (data == null);
/*      */ 
/*  277 */     return data;
/*      */   }
/*      */ 
/*      */   protected void checkOpen()
/*      */     throws SQLException
/*      */   {
/*  286 */     if (this.closed) {
/*  287 */       throw new SQLException(Messages.get("error.generic.closed", "ResultSet"), "HY010");
/*      */     }
/*      */ 
/*  291 */     if (this.cancelled)
/*  292 */       throw new SQLException(Messages.get("error.generic.cancelled", "ResultSet"), "HY010");
/*      */   }
/*      */ 
/*      */   protected void checkScrollable()
/*      */     throws SQLException
/*      */   {
/*  303 */     if (this.resultSetType == 1003)
/*  304 */       throw new SQLException(Messages.get("error.resultset.fwdonly"), "24000");
/*      */   }
/*      */ 
/*      */   protected void checkUpdateable()
/*      */     throws SQLException
/*      */   {
/*  314 */     if (this.concurrency == 1007)
/*  315 */       throw new SQLException(Messages.get("error.resultset.readonly"), "24000");
/*      */   }
/*      */ 
/*      */   protected static void notImplemented(String method)
/*      */     throws SQLException
/*      */   {
/*  326 */     throw new SQLException(Messages.get("error.generic.notimp", method), "HYC00");
/*      */   }
/*      */ 
/*      */   protected Object[] newRow()
/*      */   {
/*  335 */     Object[] row = new Object[this.columns.length];
/*      */ 
/*  337 */     return row;
/*      */   }
/*      */ 
/*      */   protected Object[] copyRow(Object[] row)
/*      */   {
/*  347 */     Object[] copy = new Object[this.columns.length];
/*      */ 
/*  349 */     System.arraycopy(row, 0, copy, 0, row.length);
/*      */ 
/*  351 */     return copy;
/*      */   }
/*      */ 
/*      */   protected ColInfo[] copyInfo(ColInfo[] info)
/*      */   {
/*  361 */     ColInfo[] copy = new ColInfo[info.length];
/*      */ 
/*  363 */     System.arraycopy(info, 0, copy, 0, info.length);
/*      */ 
/*  365 */     return copy;
/*      */   }
/*      */ 
/*      */   protected Object[] getCurrentRow()
/*      */   {
/*  374 */     return this.currentRow;
/*      */   }
/*      */ 
/*      */   protected void cacheResultSetRows()
/*      */     throws SQLException
/*      */   {
/*  382 */     if (this.rowData == null) {
/*  383 */       this.rowData = new ArrayList(1000);
/*      */     }
/*  385 */     if (this.currentRow != null)
/*      */     {
/*  389 */       this.currentRow = copyRow(this.currentRow);
/*      */     }
/*      */ 
/*  394 */     while (this.statement.getTds().getNextRow()) {
/*  395 */       this.rowData.add(copyRow(this.statement.getTds().getRowData()));
/*      */     }
/*      */ 
/*  398 */     this.statement.cacheResults();
/*      */   }
/*      */ 
/*      */   private ConnectionJDBC2 getConnection()
/*      */     throws SQLException
/*      */   {
/*  409 */     return ((ConnectionJDBC2)this.statement.getConnection());
/*      */   }
/*      */ 
/*      */   public int getConcurrency()
/*      */     throws SQLException
/*      */   {
/*  417 */     checkOpen();
/*      */ 
/*  419 */     return this.concurrency;
/*      */   }
/*      */ 
/*      */   public int getFetchDirection() throws SQLException {
/*  423 */     checkOpen();
/*      */ 
/*  425 */     return this.fetchDirection;
/*      */   }
/*      */ 
/*      */   public int getFetchSize() throws SQLException {
/*  429 */     checkOpen();
/*      */ 
/*  431 */     return this.fetchSize;
/*      */   }
/*      */ 
/*      */   public int getRow() throws SQLException {
/*  435 */     checkOpen();
/*      */ 
/*  437 */     return ((this.pos > 0) ? this.pos : 0);
/*      */   }
/*      */ 
/*      */   public int getType() throws SQLException {
/*  441 */     checkOpen();
/*      */ 
/*  443 */     return this.resultSetType;
/*      */   }
/*      */ 
/*      */   public void afterLast() throws SQLException {
/*  447 */     checkOpen();
/*  448 */     checkScrollable();
/*      */   }
/*      */ 
/*      */   public void beforeFirst() throws SQLException {
/*  452 */     checkOpen();
/*  453 */     checkScrollable();
/*      */   }
/*      */ 
/*      */   public void cancelRowUpdates() throws SQLException {
/*  457 */     checkOpen();
/*  458 */     checkUpdateable();
/*      */   }
/*      */ 
/*      */   public void clearWarnings() throws SQLException {
/*  462 */     checkOpen();
/*      */ 
/*  464 */     this.statement.clearWarnings();
/*      */   }
/*      */ 
/*      */   public void close() throws SQLException {
/*  468 */     if (this.closed)
/*      */       return;
/*      */ 
/*      */     try {
/*  470 */       while ((!(getConnection().isClosed())) && 
/*  474 */         (next()));
/*      */     }
/*      */     finally
/*      */     {
/*  477 */       this.closed = true;
/*  478 */       this.statement = null;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void deleteRow() throws SQLException
/*      */   {
/*  484 */     checkOpen();
/*  485 */     checkUpdateable();
/*      */   }
/*      */ 
/*      */   public void insertRow() throws SQLException {
/*  489 */     checkOpen();
/*  490 */     checkUpdateable();
/*      */   }
/*      */ 
/*      */   public void moveToCurrentRow() throws SQLException {
/*  494 */     checkOpen();
/*  495 */     checkUpdateable();
/*      */   }
/*      */ 
/*      */   public void moveToInsertRow() throws SQLException
/*      */   {
/*  500 */     checkOpen();
/*  501 */     checkUpdateable();
/*      */   }
/*      */ 
/*      */   public void refreshRow() throws SQLException {
/*  505 */     checkOpen();
/*  506 */     checkUpdateable();
/*      */   }
/*      */ 
/*      */   public void updateRow() throws SQLException {
/*  510 */     checkOpen();
/*  511 */     checkUpdateable();
/*      */   }
/*      */ 
/*      */   public boolean first() throws SQLException {
/*  515 */     checkOpen();
/*  516 */     checkScrollable();
/*      */ 
/*  518 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean isAfterLast() throws SQLException {
/*  522 */     checkOpen();
/*      */ 
/*  524 */     return ((this.pos == -1) && (this.rowsInResult != 0));
/*      */   }
/*      */ 
/*      */   public boolean isBeforeFirst() throws SQLException {
/*  528 */     checkOpen();
/*      */ 
/*  530 */     return ((this.pos == 0) && (this.rowsInResult != 0));
/*      */   }
/*      */ 
/*      */   public boolean isFirst() throws SQLException {
/*  534 */     checkOpen();
/*      */ 
/*  536 */     return (this.pos == 1);
/*      */   }
/*      */ 
/*      */   public boolean isLast() throws SQLException {
/*  540 */     checkOpen();
/*      */ 
/*  542 */     if (this.statement.getTds().isDataInResultSet()) {
/*  543 */       this.rowsInResult = (this.pos + 1);
/*      */     }
/*      */ 
/*  546 */     return ((this.pos == this.rowsInResult) && (this.rowsInResult != 0));
/*      */   }
/*      */ 
/*      */   public boolean last() throws SQLException {
/*  550 */     checkOpen();
/*  551 */     checkScrollable();
/*      */ 
/*  553 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean next() throws SQLException {
/*  557 */     checkOpen();
/*      */ 
/*  559 */     if (this.pos == -1)
/*      */     {
/*  561 */       return false;
/*      */     }
/*      */     try
/*      */     {
/*  565 */       if (this.rowData != null)
/*      */       {
/*  568 */         if (this.rowPtr < this.rowData.size()) {
/*  569 */           this.currentRow = ((Object[])this.rowData.get(this.rowPtr));
/*      */ 
/*  573 */           this.rowData.set(this.rowPtr++, null);
/*  574 */           this.pos += 1;
/*  575 */           this.rowsInResult = this.pos;
/*      */         } else {
/*  577 */           this.pos = -1;
/*  578 */           this.currentRow = null;
/*      */         }
/*      */ 
/*      */       }
/*  582 */       else if (!(this.statement.getTds().getNextRow())) {
/*  583 */         this.statement.cacheResults();
/*  584 */         this.pos = -1;
/*  585 */         this.currentRow = null;
/*      */       } else {
/*  587 */         this.currentRow = this.statement.getTds().getRowData();
/*  588 */         this.pos += 1;
/*  589 */         this.rowsInResult = this.pos;
/*      */       }
/*      */ 
/*  593 */       this.statement.getMessages().checkErrors();
/*      */     }
/*      */     catch (NullPointerException npe) {
/*  596 */       throw new SQLException(Messages.get("error.generic.closed", "ResultSet"), "HY010");
/*      */     }
/*      */ 
/*  600 */     return (this.currentRow != null);
/*      */   }
/*      */ 
/*      */   public boolean previous() throws SQLException {
/*  604 */     checkOpen();
/*  605 */     checkScrollable();
/*      */ 
/*  607 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean rowDeleted() throws SQLException {
/*  611 */     checkOpen();
/*  612 */     checkUpdateable();
/*      */ 
/*  614 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean rowInserted() throws SQLException {
/*  618 */     checkOpen();
/*  619 */     checkUpdateable();
/*      */ 
/*  621 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean rowUpdated() throws SQLException {
/*  625 */     checkOpen();
/*  626 */     checkUpdateable();
/*      */ 
/*  628 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean wasNull() throws SQLException {
/*  632 */     checkOpen();
/*      */ 
/*  634 */     return this.wasNull;
/*      */   }
/*      */ 
/*      */   public byte getByte(int columnIndex) throws SQLException {
/*  638 */     return ((Integer)Support.convert(this, getColumn(columnIndex), -6, null)).byteValue();
/*      */   }
/*      */ 
/*      */   public short getShort(int columnIndex) throws SQLException {
/*  642 */     return ((Integer)Support.convert(this, getColumn(columnIndex), 5, null)).shortValue();
/*      */   }
/*      */ 
/*      */   public int getInt(int columnIndex) throws SQLException {
/*  646 */     return ((Integer)Support.convert(this, getColumn(columnIndex), 4, null)).intValue();
/*      */   }
/*      */ 
/*      */   public long getLong(int columnIndex) throws SQLException {
/*  650 */     return ((Long)Support.convert(this, getColumn(columnIndex), -5, null)).longValue();
/*      */   }
/*      */ 
/*      */   public float getFloat(int columnIndex) throws SQLException {
/*  654 */     return ((Float)Support.convert(this, getColumn(columnIndex), 7, null)).floatValue();
/*      */   }
/*      */ 
/*      */   public double getDouble(int columnIndex) throws SQLException {
/*  658 */     return ((Double)Support.convert(this, getColumn(columnIndex), 8, null)).doubleValue();
/*      */   }
/*      */ 
/*      */   public void setFetchDirection(int direction) throws SQLException {
/*  662 */     checkOpen();
/*  663 */     switch (direction)
/*      */     {
/*      */     case 1001:
/*      */     case 1002:
/*  666 */       if (this.resultSetType != 1003) break label57;
/*  667 */       throw new SQLException(Messages.get("error.resultset.fwdonly"), "24000");
/*      */     case 1000:
/*  672 */       this.fetchDirection = direction;
/*  673 */       break;
/*      */     default:
/*  676 */       label57: throw new SQLException(Messages.get("error.generic.badoption", Integer.toString(direction), "direction"), "24000");
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setFetchSize(int rows)
/*      */     throws SQLException
/*      */   {
/*  685 */     checkOpen();
/*      */ 
/*  687 */     if ((rows < 0) || ((this.statement.getMaxRows() > 0) && (rows > this.statement.getMaxRows()))) {
/*  688 */       throw new SQLException(Messages.get("error.generic.badparam", Integer.toString(rows), "rows"), "HY092");
/*      */     }
/*      */ 
/*  694 */     if (rows == 0) {
/*  695 */       rows = this.statement.getDefaultFetchSize();
/*      */     }
/*  697 */     this.fetchSize = rows;
/*      */   }
/*      */ 
/*      */   public void updateNull(int columnIndex) throws SQLException {
/*  701 */     setColValue(columnIndex, 0, null, 0);
/*      */   }
/*      */ 
/*      */   public boolean absolute(int row) throws SQLException {
/*  705 */     checkOpen();
/*  706 */     checkScrollable();
/*  707 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean getBoolean(int columnIndex) throws SQLException {
/*  711 */     return ((Boolean)Support.convert(this, getColumn(columnIndex), 16, null)).booleanValue();
/*      */   }
/*      */ 
/*      */   public boolean relative(int row) throws SQLException {
/*  715 */     checkOpen();
/*  716 */     checkScrollable();
/*  717 */     return false;
/*      */   }
/*      */ 
/*      */   public byte[] getBytes(int columnIndex) throws SQLException {
/*  721 */     checkOpen();
/*  722 */     return ((byte[])Support.convert(this, getColumn(columnIndex), -2, getConnection().getCharset()));
/*      */   }
/*      */ 
/*      */   public void updateByte(int columnIndex, byte x) throws SQLException {
/*  726 */     setColValue(columnIndex, 4, new Integer(x & 0xFF), 0);
/*      */   }
/*      */ 
/*      */   public void updateDouble(int columnIndex, double x) throws SQLException {
/*  730 */     setColValue(columnIndex, 8, new Double(x), 0);
/*      */   }
/*      */ 
/*      */   public void updateFloat(int columnIndex, float x) throws SQLException {
/*  734 */     setColValue(columnIndex, 7, new Float(x), 0);
/*      */   }
/*      */ 
/*      */   public void updateInt(int columnIndex, int x) throws SQLException {
/*  738 */     setColValue(columnIndex, 4, new Integer(x), 0);
/*      */   }
/*      */ 
/*      */   public void updateLong(int columnIndex, long x) throws SQLException {
/*  742 */     setColValue(columnIndex, -5, new Long(x), 0);
/*      */   }
/*      */ 
/*      */   public void updateShort(int columnIndex, short x) throws SQLException {
/*  746 */     setColValue(columnIndex, 4, new Integer(x), 0);
/*      */   }
/*      */ 
/*      */   public void updateBoolean(int columnIndex, boolean x) throws SQLException {
/*  750 */     setColValue(columnIndex, -7, (x) ? Boolean.TRUE : Boolean.FALSE, 0);
/*      */   }
/*      */ 
/*      */   public void updateBytes(int columnIndex, byte[] x) throws SQLException {
/*  754 */     setColValue(columnIndex, -3, x, (x != null) ? x.length : 0);
/*      */   }
/*      */ 
/*      */   public InputStream getAsciiStream(int columnIndex) throws SQLException {
/*  758 */     Clob clob = getClob(columnIndex);
/*      */ 
/*  760 */     if (clob == null) {
/*  761 */       return null;
/*      */     }
/*      */ 
/*  764 */     return clob.getAsciiStream();
/*      */   }
/*      */ 
/*      */   public InputStream getBinaryStream(int columnIndex) throws SQLException {
/*  768 */     Blob blob = getBlob(columnIndex);
/*      */ 
/*  770 */     if (blob == null) {
/*  771 */       return null;
/*      */     }
/*      */ 
/*  774 */     return blob.getBinaryStream();
/*      */   }
/*      */ 
/*      */   public InputStream getUnicodeStream(int columnIndex) throws SQLException {
/*  778 */     ClobImpl clob = (ClobImpl)getClob(columnIndex);
/*      */ 
/*  780 */     if (clob == null) {
/*  781 */       return null;
/*      */     }
/*      */ 
/*  784 */     return clob.getBlobBuffer().getUnicodeStream();
/*      */   }
/*      */ 
/*      */   public void updateAsciiStream(int columnIndex, InputStream inputStream, int length) throws SQLException
/*      */   {
/*  789 */     if ((inputStream == null) || (length < 0))
/*  790 */       updateCharacterStream(columnIndex, null, 0);
/*      */     else
/*      */       try {
/*  793 */         updateCharacterStream(columnIndex, new InputStreamReader(inputStream, "US-ASCII"), length);
/*      */       }
/*      */       catch (UnsupportedEncodingException e)
/*      */       {
/*      */       }
/*      */   }
/*      */ 
/*      */   public void updateBinaryStream(int columnIndex, InputStream inputStream, int length)
/*      */     throws SQLException
/*      */   {
/*  803 */     if ((inputStream == null) || (length < 0)) {
/*  804 */       updateBytes(columnIndex, null);
/*  805 */       return;
/*      */     }
/*      */ 
/*  808 */     setColValue(columnIndex, -3, inputStream, length);
/*      */   }
/*      */ 
/*      */   public Reader getCharacterStream(int columnIndex) throws SQLException {
/*  812 */     Clob clob = getClob(columnIndex);
/*      */ 
/*  814 */     if (clob == null) {
/*  815 */       return null;
/*      */     }
/*      */ 
/*  818 */     return clob.getCharacterStream();
/*      */   }
/*      */ 
/*      */   public void updateCharacterStream(int columnIndex, Reader reader, int length)
/*      */     throws SQLException
/*      */   {
/*  824 */     if ((reader == null) || (length < 0)) {
/*  825 */       updateString(columnIndex, null);
/*  826 */       return;
/*      */     }
/*      */ 
/*  829 */     setColValue(columnIndex, 12, reader, length);
/*      */   }
/*      */ 
/*      */   public Object getObject(int columnIndex) throws SQLException {
/*  833 */     Object value = getColumn(columnIndex);
/*      */ 
/*  837 */     if (value instanceof UniqueIdentifier) {
/*  838 */       return value.toString();
/*      */     }
/*      */ 
/*  842 */     if (value instanceof DateTime) {
/*  843 */       return ((DateTime)value).toObject();
/*      */     }
/*      */ 
/*  846 */     if (!(getConnection().getUseLOBs())) {
/*  847 */       value = Support.convertLOB(value);
/*      */     }
/*      */ 
/*  850 */     return value;
/*      */   }
/*      */ 
/*      */   public void updateObject(int columnIndex, Object x) throws SQLException {
/*  854 */     checkOpen();
/*  855 */     int length = 0;
/*  856 */     int jdbcType = 12;
/*      */ 
/*  858 */     if (x != null)
/*      */     {
/*  860 */       jdbcType = Support.getJdbcType(x);
/*  861 */       if (x instanceof BigDecimal) {
/*  862 */         int prec = getConnection().getMaxPrecision();
/*  863 */         x = Support.normalizeBigDecimal((BigDecimal)x, prec);
/*  864 */       } else if (x instanceof Blob) {
/*  865 */         Blob blob = (Blob)x;
/*  866 */         x = blob.getBinaryStream();
/*  867 */         length = (int)blob.length();
/*  868 */       } else if (x instanceof Clob) {
/*  869 */         Clob clob = (Clob)x;
/*  870 */         x = clob.getCharacterStream();
/*  871 */         length = (int)clob.length();
/*  872 */       } else if (x instanceof String) {
/*  873 */         length = ((String)x).length();
/*  874 */       } else if (x instanceof byte[]) {
/*  875 */         length = ((byte[])x).length;
/*      */       }
/*  877 */       if (jdbcType == 2000)
/*      */       {
/*  879 */         if ((columnIndex < 1) || (columnIndex > this.columnCount)) {
/*  880 */           throw new SQLException(Messages.get("error.resultset.colindex", Integer.toString(columnIndex)), "07009");
/*      */         }
/*      */ 
/*  884 */         ColInfo ci = this.columns[(columnIndex - 1)];
/*  885 */         throw new SQLException(Messages.get("error.convert.badtypes", x.getClass().getName(), Support.getJdbcTypeName(ci.jdbcType)), "22005");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  892 */     setColValue(columnIndex, jdbcType, x, length);
/*      */   }
/*      */ 
/*      */   public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
/*  896 */     checkOpen();
/*  897 */     if ((scale < 0) || (scale > getConnection().getMaxPrecision())) {
/*  898 */       throw new SQLException(Messages.get("error.generic.badscale"), "HY092");
/*      */     }
/*      */ 
/*  901 */     if (x instanceof BigDecimal)
/*  902 */       updateObject(columnIndex, ((BigDecimal)x).setScale(scale, 4));
/*  903 */     else if (x instanceof Number)
/*  904 */       synchronized (f) {
/*  905 */         f.setGroupingUsed(false);
/*  906 */         f.setMaximumFractionDigits(scale);
/*  907 */         updateObject(columnIndex, f.format(x));
/*      */       }
/*      */     else
/*  910 */       updateObject(columnIndex, x);
/*      */   }
/*      */ 
/*      */   public String getCursorName() throws SQLException
/*      */   {
/*  915 */     checkOpen();
/*  916 */     if (this.cursorName != null) {
/*  917 */       return this.cursorName;
/*      */     }
/*  919 */     throw new SQLException(Messages.get("error.resultset.noposupdate"), "24000");
/*      */   }
/*      */ 
/*      */   public String getString(int columnIndex) throws SQLException {
/*  923 */     Object tmp = getColumn(columnIndex);
/*      */ 
/*  925 */     if (tmp instanceof String) {
/*  926 */       return ((String)tmp);
/*      */     }
/*  928 */     return ((String)Support.convert(this, tmp, 12, getConnection().getCharset()));
/*      */   }
/*      */ 
/*      */   public void updateString(int columnIndex, String x) throws SQLException {
/*  932 */     setColValue(columnIndex, 12, x, (x != null) ? x.length() : 0);
/*      */   }
/*      */ 
/*      */   public byte getByte(String columnName) throws SQLException {
/*  936 */     return getByte(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public double getDouble(String columnName) throws SQLException {
/*  940 */     return getDouble(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public float getFloat(String columnName) throws SQLException {
/*  944 */     return getFloat(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public int findColumn(String columnName) throws SQLException {
/*  948 */     checkOpen();
/*      */ 
/*  950 */     if (this.columnMap == null) {
/*  951 */       this.columnMap = new HashMap(this.columnCount);
/*      */     } else {
/*  953 */       Object pos = this.columnMap.get(columnName);
/*  954 */       if (pos != null) {
/*  955 */         return ((Integer)pos).intValue();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  961 */     for (int i = 0; i < this.columnCount; ++i) {
/*  962 */       if (this.columns[i].name.equalsIgnoreCase(columnName)) {
/*  963 */         this.columnMap.put(columnName, new Integer(i + 1));
/*      */ 
/*  965 */         return (i + 1);
/*      */       }
/*      */     }
/*      */ 
/*  969 */     throw new SQLException(Messages.get("error.resultset.colname", columnName), "07009");
/*      */   }
/*      */ 
/*      */   public int getInt(String columnName) throws SQLException {
/*  973 */     return getInt(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public long getLong(String columnName) throws SQLException {
/*  977 */     return getLong(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public short getShort(String columnName) throws SQLException {
/*  981 */     return getShort(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateNull(String columnName) throws SQLException {
/*  985 */     updateNull(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public boolean getBoolean(String columnName) throws SQLException {
/*  989 */     return getBoolean(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public byte[] getBytes(String columnName) throws SQLException {
/*  993 */     return getBytes(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateByte(String columnName, byte x) throws SQLException {
/*  997 */     updateByte(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public void updateDouble(String columnName, double x) throws SQLException {
/* 1001 */     updateDouble(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public void updateFloat(String columnName, float x) throws SQLException {
/* 1005 */     updateFloat(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public void updateInt(String columnName, int x) throws SQLException {
/* 1009 */     updateInt(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public void updateLong(String columnName, long x) throws SQLException {
/* 1013 */     updateLong(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public void updateShort(String columnName, short x) throws SQLException {
/* 1017 */     updateShort(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public void updateBoolean(String columnName, boolean x) throws SQLException {
/* 1021 */     updateBoolean(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public void updateBytes(String columnName, byte[] x) throws SQLException {
/* 1025 */     updateBytes(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
/* 1029 */     return ((BigDecimal)Support.convert(this, getColumn(columnIndex), 3, null));
/*      */   }
/*      */ 
/*      */   public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
/* 1033 */     BigDecimal result = (BigDecimal)Support.convert(this, getColumn(columnIndex), 3, null);
/*      */ 
/* 1035 */     if (result == null) {
/* 1036 */       return null;
/*      */     }
/*      */ 
/* 1039 */     return result.setScale(scale, 4);
/*      */   }
/*      */ 
/*      */   public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
/*      */   {
/* 1044 */     checkOpen();
/* 1045 */     checkUpdateable();
/* 1046 */     if (x != null) {
/* 1047 */       int prec = getConnection().getMaxPrecision();
/* 1048 */       x = Support.normalizeBigDecimal(x, prec);
/*      */     }
/* 1050 */     setColValue(columnIndex, 3, x, 0);
/*      */   }
/*      */ 
/*      */   public URL getURL(int columnIndex) throws SQLException {
/* 1054 */     String url = getString(columnIndex);
/*      */     try
/*      */     {
/* 1057 */       return new URL(url);
/*      */     } catch (MalformedURLException e) {
/* 1059 */       throw new SQLException(Messages.get("error.resultset.badurl", url), "22000");
/*      */     }
/*      */   }
/*      */ 
/*      */   public Array getArray(int columnIndex) throws SQLException {
/* 1064 */     checkOpen();
/* 1065 */     notImplemented("ResultSet.getArray()");
/* 1066 */     return null;
/*      */   }
/*      */ 
/*      */   public void updateArray(int columnIndex, Array x) throws SQLException {
/* 1070 */     checkOpen();
/* 1071 */     checkUpdateable();
/* 1072 */     notImplemented("ResultSet.updateArray()");
/*      */   }
/*      */ 
/*      */   public Blob getBlob(int columnIndex) throws SQLException {
/* 1076 */     return ((Blob)Support.convert(this, getColumn(columnIndex), 2004, null));
/*      */   }
/*      */ 
/*      */   public void updateBlob(int columnIndex, Blob x) throws SQLException {
/* 1080 */     if (x == null)
/* 1081 */       updateBinaryStream(columnIndex, null, 0);
/*      */     else
/* 1083 */       updateBinaryStream(columnIndex, x.getBinaryStream(), (int)x.length());
/*      */   }
/*      */ 
/*      */   public Clob getClob(int columnIndex) throws SQLException
/*      */   {
/* 1088 */     return ((Clob)Support.convert(this, getColumn(columnIndex), 2005, null));
/*      */   }
/*      */ 
/*      */   public void updateClob(int columnIndex, Clob x) throws SQLException {
/* 1092 */     if (x == null)
/* 1093 */       updateCharacterStream(columnIndex, null, 0);
/*      */     else
/* 1095 */       updateCharacterStream(columnIndex, x.getCharacterStream(), (int)x.length());
/*      */   }
/*      */ 
/*      */   public Date getDate(int columnIndex) throws SQLException
/*      */   {
/* 1100 */     return ((Date)Support.convert(this, getColumn(columnIndex), 91, null));
/*      */   }
/*      */ 
/*      */   public void updateDate(int columnIndex, Date x) throws SQLException {
/* 1104 */     setColValue(columnIndex, 91, x, 0);
/*      */   }
/*      */ 
/*      */   public Ref getRef(int columnIndex) throws SQLException {
/* 1108 */     checkOpen();
/* 1109 */     notImplemented("ResultSet.getRef()");
/*      */ 
/* 1111 */     return null;
/*      */   }
/*      */ 
/*      */   public void updateRef(int columnIndex, Ref x) throws SQLException {
/* 1115 */     checkOpen();
/* 1116 */     checkUpdateable();
/* 1117 */     notImplemented("ResultSet.updateRef()");
/*      */   }
/*      */ 
/*      */   public ResultSetMetaData getMetaData() throws SQLException {
/* 1121 */     checkOpen();
/*      */ 
/* 1125 */     boolean useLOBs = ((this instanceof CachedResultSet) && (this.statement.closed)) ? false : getConnection().getUseLOBs();
/*      */ 
/* 1128 */     return new JtdsResultSetMetaData(this.columns, this.columnCount, useLOBs);
/*      */   }
/*      */ 
/*      */   public SQLWarning getWarnings() throws SQLException
/*      */   {
/* 1133 */     checkOpen();
/*      */ 
/* 1135 */     return this.statement.getWarnings();
/*      */   }
/*      */ 
/*      */   public Statement getStatement() throws SQLException {
/* 1139 */     checkOpen();
/*      */ 
/* 1141 */     return this.statement;
/*      */   }
/*      */ 
/*      */   public Time getTime(int columnIndex) throws SQLException {
/* 1145 */     return ((Time)Support.convert(this, getColumn(columnIndex), 92, null));
/*      */   }
/*      */ 
/*      */   public void updateTime(int columnIndex, Time x) throws SQLException {
/* 1149 */     setColValue(columnIndex, 92, x, 0);
/*      */   }
/*      */ 
/*      */   public Timestamp getTimestamp(int columnIndex) throws SQLException {
/* 1153 */     return ((Timestamp)Support.convert(this, getColumn(columnIndex), 93, null));
/*      */   }
/*      */ 
/*      */   public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
/* 1157 */     setColValue(columnIndex, 93, x, 0);
/*      */   }
/*      */ 
/*      */   public InputStream getAsciiStream(String columnName) throws SQLException {
/* 1161 */     return getAsciiStream(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public InputStream getBinaryStream(String columnName) throws SQLException {
/* 1165 */     return getBinaryStream(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public InputStream getUnicodeStream(String columnName) throws SQLException {
/* 1169 */     return getUnicodeStream(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException
/*      */   {
/* 1174 */     updateAsciiStream(findColumn(columnName), x, length);
/*      */   }
/*      */ 
/*      */   public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException
/*      */   {
/* 1179 */     updateBinaryStream(findColumn(columnName), x, length);
/*      */   }
/*      */ 
/*      */   public Reader getCharacterStream(String columnName) throws SQLException {
/* 1183 */     return getCharacterStream(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateCharacterStream(String columnName, Reader x, int length) throws SQLException
/*      */   {
/* 1188 */     updateCharacterStream(findColumn(columnName), x, length);
/*      */   }
/*      */ 
/*      */   public Object getObject(String columnName) throws SQLException {
/* 1192 */     return getObject(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateObject(String columnName, Object x) throws SQLException {
/* 1196 */     updateObject(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public void updateObject(String columnName, Object x, int scale) throws SQLException
/*      */   {
/* 1201 */     updateObject(findColumn(columnName), x, scale);
/*      */   }
/*      */ 
/*      */   public Object getObject(int columnIndex, Map map) throws SQLException {
/* 1205 */     notImplemented("ResultSet.getObject(int, Map)");
/* 1206 */     return null;
/*      */   }
/*      */ 
/*      */   public String getString(String columnName) throws SQLException {
/* 1210 */     return getString(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateString(String columnName, String x) throws SQLException {
/* 1214 */     updateString(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public BigDecimal getBigDecimal(String columnName) throws SQLException {
/* 1218 */     return getBigDecimal(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
/*      */   {
/* 1223 */     return getBigDecimal(findColumn(columnName), scale);
/*      */   }
/*      */ 
/*      */   public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException
/*      */   {
/* 1228 */     updateObject(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public URL getURL(String columnName) throws SQLException {
/* 1232 */     return getURL(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public Array getArray(String columnName) throws SQLException {
/* 1236 */     return getArray(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateArray(String columnName, Array x) throws SQLException {
/* 1240 */     updateArray(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public Blob getBlob(String columnName) throws SQLException {
/* 1244 */     return getBlob(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateBlob(String columnName, Blob x) throws SQLException {
/* 1248 */     updateBlob(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public Clob getClob(String columnName) throws SQLException {
/* 1252 */     return getClob(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateClob(String columnName, Clob x) throws SQLException {
/* 1256 */     updateClob(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public Date getDate(String columnName) throws SQLException {
/* 1260 */     return getDate(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateDate(String columnName, Date x) throws SQLException {
/* 1264 */     updateDate(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public Date getDate(int columnIndex, Calendar cal) throws SQLException {
/* 1268 */     Date date = getDate(columnIndex);
/*      */ 
/* 1270 */     if ((date != null) && (cal != null)) {
/* 1271 */       date = new Date(Support.timeToZone(date, cal));
/*      */     }
/*      */ 
/* 1274 */     return date;
/*      */   }
/*      */ 
/*      */   public Ref getRef(String columnName) throws SQLException {
/* 1278 */     return getRef(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateRef(String columnName, Ref x) throws SQLException {
/* 1282 */     updateRef(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public Time getTime(String columnName) throws SQLException {
/* 1286 */     return getTime(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateTime(String columnName, Time x) throws SQLException {
/* 1290 */     updateTime(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public Time getTime(int columnIndex, Calendar cal) throws SQLException {
/* 1294 */     checkOpen();
/* 1295 */     Time time = getTime(columnIndex);
/*      */ 
/* 1297 */     if ((time != null) && (cal != null)) {
/* 1298 */       return new Time(Support.timeToZone(time, cal));
/*      */     }
/*      */ 
/* 1301 */     return time;
/*      */   }
/*      */ 
/*      */   public Timestamp getTimestamp(String columnName) throws SQLException {
/* 1305 */     return getTimestamp(findColumn(columnName));
/*      */   }
/*      */ 
/*      */   public void updateTimestamp(String columnName, Timestamp x) throws SQLException
/*      */   {
/* 1310 */     updateTimestamp(findColumn(columnName), x);
/*      */   }
/*      */ 
/*      */   public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
/*      */   {
/* 1315 */     checkOpen();
/* 1316 */     Timestamp timestamp = getTimestamp(columnIndex);
/*      */ 
/* 1318 */     if ((timestamp != null) && (cal != null)) {
/* 1319 */       timestamp = new Timestamp(Support.timeToZone(timestamp, cal));
/*      */     }
/*      */ 
/* 1322 */     return timestamp;
/*      */   }
/*      */ 
/*      */   public Object getObject(String columnName, Map map) throws SQLException {
/* 1326 */     return getObject(findColumn(columnName), map);
/*      */   }
/*      */ 
/*      */   public Date getDate(String columnName, Calendar cal) throws SQLException {
/* 1330 */     return getDate(findColumn(columnName), cal);
/*      */   }
/*      */ 
/*      */   public Time getTime(String columnName, Calendar cal) throws SQLException {
/* 1334 */     return getTime(findColumn(columnName), cal);
/*      */   }
/*      */ 
/*      */   public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException
/*      */   {
/* 1339 */     return getTimestamp(findColumn(columnName), cal);
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.JtdsResultSet
 * JD-Core Version:    0.5.3
 */