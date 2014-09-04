/*      */ package net.sourceforge.jtds.jdbc;
/*      */ 
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.lang.ref.WeakReference;
/*      */ import java.net.UnknownHostException;
/*      */ import java.sql.CallableStatement;
/*      */ import java.sql.Connection;
/*      */ import java.sql.DatabaseMetaData;
/*      */ import java.sql.PreparedStatement;
/*      */ import java.sql.ResultSet;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.SQLWarning;
/*      */ import java.sql.Savepoint;
/*      */ import java.sql.Statement;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collection;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Random;
/*      */ import net.sourceforge.jtds.jdbc.cache.ProcedureCache;
/*      */ import net.sourceforge.jtds.jdbc.cache.StatementCache;
/*      */ import net.sourceforge.jtds.util.Logger;
/*      */ import net.sourceforge.jtds.util.TimerThread;
/*      */ import net.sourceforge.jtds.util.TimerThread.TimerListener;
/*      */ 
/*      */ public class ConnectionJDBC2
/*      */   implements Connection
/*      */ {
/*      */   private static final String SYBASE_SERVER_CHARSET_QUERY = "select name from master.dbo.syscharsets where id = (select value from master.dbo.sysconfigures where config=131)";
/*      */   private static final String SQL_SERVER_65_CHARSET_QUERY = "select name from master.dbo.syscharsets where id = (select csid from master.dbo.syscharsets, master.dbo.sysconfigures where config=1123 and id = value)";
/*      */   private static final String SYBASE_INITIAL_SQL = "SET TRANSACTION ISOLATION LEVEL 1\r\nSET CHAINED OFF\r\nSET QUOTED_IDENTIFIER ON\r\nSET TEXTSIZE 2147483647";
/*      */   private static final String SQL_SERVER_INITIAL_SQL = "SELECT @@MAX_PRECISION\r\nSET TRANSACTION ISOLATION LEVEL READ COMMITTED\r\nSET IMPLICIT_TRANSACTIONS OFF\r\nSET QUOTED_IDENTIFIER ON\r\nSET TEXTSIZE 2147483647";
/*      */   public static final int TRANSACTION_SNAPSHOT = 4096;
/*      */   private final String url;
/*      */   private String serverName;
/*      */   private int portNumber;
/*      */   private int serverType;
/*      */   private String instanceName;
/*      */   private String databaseName;
/*      */   private String currentDatabase;
/*      */   private String domainName;
/*      */   private String user;
/*      */   private String password;
/*      */   private String serverCharset;
/*      */   private String appName;
/*      */   private String progName;
/*      */   private String wsid;
/*      */   private String language;
/*      */   private String macAddress;
/*      */   private int tdsVersion;
/*      */   private final SharedSocket socket;
/*      */   private final TdsCore baseTds;
/*  137 */   private int netPacketSize = 512;
/*      */   private int packetSize;
/*      */   private byte[] collation;
/*      */   private boolean charsetSpecified;
/*      */   private String databaseProductName;
/*      */   private String databaseProductVersion;
/*      */   private int databaseMajorVersion;
/*      */   private int databaseMinorVersion;
/*      */   private boolean closed;
/*      */   private boolean readOnly;
/*  157 */   private final ArrayList statements = new ArrayList();
/*      */ 
/*  159 */   private int transactionIsolation = 2;
/*      */ 
/*  161 */   private boolean autoCommit = true;
/*      */   private final SQLDiagnostic messages;
/*      */   private int rowCount;
/*      */   private int textSize;
/*  169 */   private int maxPrecision = 38;
/*      */ 
/*  171 */   private int spSequenceNo = 1;
/*      */ 
/*  173 */   private int cursorSequenceNo = 1;
/*      */ 
/*  175 */   private final ArrayList procInTran = new ArrayList();
/*      */   private CharsetInfo charsetInfo;
/*      */   private int prepareSql;
/*      */   private long lobBuffer;
/*      */   private int maxStatements;
/*      */   private StatementCache statementCache;
/*  187 */   private boolean useUnicode = true;
/*      */   private boolean namedPipe;
/*      */   private boolean lastUpdateCount;
/*  193 */   private boolean tcpNoDelay = true;
/*      */   private int loginTimeout;
/*      */   private int sybaseInfo;
/*      */   private boolean xaTransaction;
/*      */   private int xaState;
/*      */   private Object xid;
/*  205 */   private boolean xaEmulation = true;
/*      */ 
/*  207 */   private final Semaphore mutex = new Semaphore(1L);
/*      */   private int socketTimeout;
/*      */   private boolean socketKeepAlive;
/*      */   private static Integer processId;
/*      */   private String ssl;
/*      */   private int batchSize;
/*      */   private boolean useMetadataCache;
/*      */   private boolean useCursors;
/*      */   private File bufferDir;
/*      */   private int bufferMaxMemory;
/*      */   private int bufferMinPackets;
/*      */   private boolean useLOBs;
/*      */   private TdsCore cachedTds;
/*      */   private String bindAddress;
/*      */   private boolean useJCIFS;
/*  237 */   private boolean useNTLMv2 = false;
/*      */   private static int connections;
/*      */ 
/*      */   private ConnectionJDBC2()
/*      */   {
/*  248 */     connections += 1;
/*  249 */     this.url = null;
/*  250 */     this.socket = null;
/*  251 */     this.baseTds = null;
/*  252 */     this.messages = null;
/*      */   }
/*      */ 
/*      */   ConnectionJDBC2(String url, Properties info)
/*      */     throws SQLException
/*      */   {
/*  264 */     connections += 1;
/*  265 */     this.url = url;
/*      */ 
/*  269 */     unpackProperties(info);
/*  270 */     this.messages = new SQLDiagnostic(this.serverType);
/*      */ 
/*  275 */     if ((this.instanceName.length() > 0) && (!(this.namedPipe))) {
/*      */       try {
/*  277 */         MSSqlServerInfo msInfo = new MSSqlServerInfo(this.serverName);
/*      */ 
/*  279 */         this.portNumber = msInfo.getPortForInstance(this.instanceName);
/*      */       }
/*      */       catch (SQLException e) {
/*  282 */         if (this.portNumber <= 0) {
/*  283 */           throw e;
/*      */         }
/*      */       }
/*      */ 
/*  287 */       if (this.portNumber == -1) {
/*  288 */         throw new SQLException(Messages.get("error.msinfo.badinst", this.serverName, this.instanceName), "08003");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  294 */     SharedSocket.setMemoryBudget(this.bufferMaxMemory * 1024);
/*  295 */     SharedSocket.setMinMemPkts(this.bufferMinPackets);
/*      */ 
/*  298 */     Object timer = null;
/*  299 */     boolean loginError = false;
/*      */     SQLWarning warn;
/*      */     try
/*      */     {
/*  301 */       if (this.loginTimeout > 0)
/*      */       {
/*  303 */         timer = TimerThread.getInstance().setTimer(this.loginTimeout * 1000, new TimerThread.TimerListener()
/*      */         {
/*      */           public void timerExpired() {
/*  306 */             if (ConnectionJDBC2.this.socket != null) {
/*  307 */               ConnectionJDBC2.this.socket.forceClose();
/*      */             }
/*      */           }
/*      */         });
/*      */       }
/*      */ 
/*  313 */       if (this.namedPipe)
/*      */       {
/*  315 */         this.socket = createNamedPipe(this);
/*      */       }
/*      */       else {
/*  318 */         this.socket = new SharedSocket(this);
/*      */       }
/*      */ 
/*  321 */       if ((timer != null) && (TimerThread.getInstance().hasExpired(timer)))
/*      */       {
/*  324 */         this.socket.forceClose();
/*  325 */         throw new IOException("Login timed out");
/*      */       }
/*      */ 
/*  328 */       if (this.charsetSpecified) {
/*  329 */         loadCharset(this.serverCharset);
/*      */       }
/*      */       else
/*      */       {
/*  333 */         loadCharset("iso_1");
/*  334 */         this.serverCharset = "";
/*      */       }
/*      */ 
/*  340 */       this.baseTds = new TdsCore(this, this.messages);
/*      */ 
/*  345 */       if ((this.tdsVersion >= 4) && (!(this.namedPipe))) {
/*  346 */         this.baseTds.negotiateSSL(this.instanceName, this.ssl);
/*      */       }
/*      */ 
/*  352 */       this.baseTds.login(this.serverName, this.databaseName, this.user, this.password, this.domainName, this.serverCharset, this.appName, this.progName, this.wsid, this.language, this.macAddress, this.packetSize);
/*      */ 
/*  369 */       warn = this.messages.warnings;
/*      */ 
/*  374 */       this.tdsVersion = this.baseTds.getTdsVersion();
/*  375 */       if ((this.tdsVersion < 3) && (this.databaseName.length() > 0))
/*      */       {
/*  377 */         setCatalog(this.databaseName);
/*      */       }
/*      */ 
/*  383 */       if ((((this.serverCharset == null) || (this.serverCharset.length() == 0))) && (this.collation == null))
/*      */       {
/*  385 */         loadCharset(determineServerCharset());
/*      */       }
/*      */ 
/*  391 */       if (this.serverType == 2) {
/*  392 */         this.baseTds.submitSQL("SET TRANSACTION ISOLATION LEVEL 1\r\nSET CHAINED OFF\r\nSET QUOTED_IDENTIFIER ON\r\nSET TEXTSIZE 2147483647");
/*      */       }
/*      */       else
/*      */       {
/*  396 */         Statement stmt = createStatement();
/*  397 */         ResultSet rs = stmt.executeQuery("SELECT @@MAX_PRECISION\r\nSET TRANSACTION ISOLATION LEVEL READ COMMITTED\r\nSET IMPLICIT_TRANSACTIONS OFF\r\nSET QUOTED_IDENTIFIER ON\r\nSET TEXTSIZE 2147483647");
/*      */ 
/*  399 */         if (rs.next()) {
/*  400 */           this.maxPrecision = rs.getByte(1);
/*      */         }
/*      */ 
/*  403 */         rs.close();
/*  404 */         stmt.close();
/*      */       }
/*      */     } catch (UnknownHostException e) {
/*  407 */       loginError = true;
/*  408 */       throw Support.linkException(new SQLException(Messages.get("error.connection.badhost", e.getMessage()), "08S03"), e);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  412 */       loginError = true;
/*  413 */       if ((this.loginTimeout > 0) && (e.getMessage().indexOf("timed out") >= 0)) {
/*  414 */         throw Support.linkException(new SQLException(Messages.get("error.connection.timeout"), "HYT01"), e);
/*      */       }
/*      */ 
/*  417 */       throw Support.linkException(new SQLException(Messages.get("error.connection.ioerror", e.getMessage()), "08S01"), e);
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*  421 */       loginError = true;
/*  422 */       if ((this.loginTimeout > 0) && (e.getMessage().indexOf("socket closed") >= 0)) {
/*  423 */         throw Support.linkException(new SQLException(Messages.get("error.connection.timeout"), "HYT01"), e);
/*      */       }
/*      */ 
/*  426 */       throw e;
/*      */     } catch (RuntimeException e) {
/*  428 */       loginError = true;
/*  429 */       throw e;
/*      */     }
/*      */     finally
/*      */     {
/*  433 */       if (loginError)
/*  434 */         close();
/*  435 */       else if (timer != null)
/*      */       {
/*  437 */         TimerThread.getInstance().cancelTimer(timer);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  445 */     this.messages.warnings = warn;
/*      */   }
/*      */ 
/*      */   protected void finalize()
/*      */     throws Throwable
/*      */   {
/*      */     try
/*      */     {
/*  453 */       close();
/*      */     } catch (Exception e) {
/*      */     }
/*      */     finally {
/*  457 */       super.finalize();
/*      */     }
/*      */   }
/*      */ 
/*      */   private SharedSocket createNamedPipe(ConnectionJDBC2 connection)
/*      */     throws IOException
/*      */   {
/*  483 */     long loginTimeout = connection.getLoginTimeout();
/*  484 */     long retryTimeout = ((loginTimeout > 0L) ? loginTimeout : 20L) * 1000L;
/*  485 */     long startLoginTimeout = System.currentTimeMillis();
/*  486 */     Random random = new Random(startLoginTimeout);
/*  487 */     boolean isWindowsOS = Support.isWindowsOS();
/*      */ 
/*  489 */     SharedSocket socket = null;
/*  490 */     IOException lastIOException = null;
/*  491 */     int exceptionCount = 0;
/*      */     do
/*      */       try
/*      */       {
/*  495 */         if ((isWindowsOS) && (!(connection.getUseJCIFS()))) {
/*  496 */           socket = new SharedLocalNamedPipe(connection);
/*      */         }
/*      */         else
/*  499 */           socket = new SharedNamedPipe(connection);
/*      */       }
/*      */       catch (IOException ioe)
/*      */       {
/*  503 */         ++exceptionCount;
/*  504 */         lastIOException = ioe;
/*  505 */         if (ioe.getMessage().toLowerCase().indexOf("all pipe instances are busy") >= 0)
/*      */         {
/*  509 */           int randomWait = random.nextInt(800) + 200;
/*  510 */           if (Logger.isActive()) {
/*  511 */             Logger.println("Retry #" + exceptionCount + " Wait " + randomWait + " ms: " + ioe.getMessage());
/*      */           }
/*      */           try
/*      */           {
/*  515 */             Thread.sleep(randomWait);
/*      */           }
/*      */           catch (InterruptedException ie)
/*      */           {
/*      */           }
/*      */         }
/*      */         else {
/*  522 */           throw ioe;
/*      */         }
/*      */       }
/*  525 */     while ((socket == null) && (System.currentTimeMillis() - startLoginTimeout < retryTimeout));
/*      */ 
/*  527 */     if (socket == null) {
/*  528 */       IOException ioException = new IOException("Connection timed out to named pipe");
/*  529 */       Support.linkException(ioException, lastIOException);
/*  530 */       throw ioException;
/*      */     }
/*      */ 
/*  533 */     return socket;
/*      */   }
/*      */ 
/*      */   SharedSocket getSocket()
/*      */   {
/*  543 */     return this.socket;
/*      */   }
/*      */ 
/*      */   int getTdsVersion()
/*      */   {
/*  552 */     return this.tdsVersion;
/*      */   }
/*      */ 
/*      */   String getProcName()
/*      */   {
/*  572 */     String seq = "000000" + Integer.toHexString(this.spSequenceNo++).toUpperCase();
/*      */ 
/*  574 */     return "#jtds" + seq.substring(seq.length() - 6, seq.length());
/*      */   }
/*      */ 
/*      */   synchronized String getCursorName()
/*      */   {
/*  583 */     String seq = "000000" + Integer.toHexString(this.cursorSequenceNo++).toUpperCase();
/*      */ 
/*  585 */     return "_jtds" + seq.substring(seq.length() - 6, seq.length());
/*      */   }
/*      */ 
/*      */   synchronized String prepareSQL(JtdsPreparedStatement pstmt, String sql, ParamInfo[] params, boolean returnKeys, boolean cursorNeeded)
/*      */     throws SQLException
/*      */   {
/*  613 */     if ((this.prepareSql == 0) || (this.prepareSql == 2))
/*      */     {
/*  615 */       return null;
/*      */     }
/*      */ 
/*  618 */     if (this.serverType == 2) {
/*  619 */       if (this.tdsVersion != 2) {
/*  620 */         return null;
/*      */       }
/*      */ 
/*  623 */       if (returnKeys) {
/*  624 */         return null;
/*      */       }
/*      */ 
/*  627 */       if (cursorNeeded)
/*      */       {
/*  633 */         return null;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  640 */     for (int i = 0; i < params.length; ++i) {
/*  641 */       if (!(params[i].isSet)) {
/*  642 */         throw new SQLException(Messages.get("error.prepare.paramnotset", Integer.toString(i + 1)), "07000");
/*      */       }
/*      */ 
/*  647 */       TdsData.getNativeType(this, params[i]);
/*      */ 
/*  649 */       if ((this.serverType == 2) && ((
/*  650 */         ("text".equals(params[i].sqlType)) || ("image".equals(params[i].sqlType)))))
/*      */       {
/*  652 */         return null;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  657 */     String key = Support.getStatementKey(sql, params, this.serverType, getCatalog(), this.autoCommit, cursorNeeded);
/*      */ 
/*  663 */     ProcEntry proc = (ProcEntry)this.statementCache.get(key);
/*      */ 
/*  665 */     if (proc != null)
/*      */     {
/*  671 */       if ((pstmt.handles != null) && (pstmt.handles.contains(proc))) {
/*  672 */         proc.release();
/*      */       }
/*      */ 
/*  675 */       pstmt.setColMetaData(proc.getColMetaData());
/*  676 */       if (this.serverType == 2) {
/*  677 */         pstmt.setParamMetaData(proc.getParamMetaData());
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  683 */       proc = new ProcEntry();
/*      */ 
/*  685 */       if (this.serverType == 1) {
/*  686 */         proc.setName(this.baseTds.microsoftPrepare(sql, params, cursorNeeded, pstmt.getResultSetType(), pstmt.getResultSetConcurrency()));
/*      */ 
/*  692 */         if (proc.toString() == null) {
/*  693 */           proc.setType(4);
/*  694 */         } else if (this.prepareSql == 1) {
/*  695 */           proc.setType(1);
/*      */         } else {
/*  697 */           proc.setType((cursorNeeded) ? 3 : 2);
/*      */ 
/*  699 */           proc.setColMetaData(this.baseTds.getColumns());
/*  700 */           pstmt.setColMetaData(proc.getColMetaData());
/*      */         }
/*      */       }
/*      */       else {
/*  704 */         proc.setName(this.baseTds.sybasePrepare(sql, params));
/*      */ 
/*  706 */         if (proc.toString() == null)
/*  707 */           proc.setType(4);
/*      */         else {
/*  709 */           proc.setType(1);
/*      */         }
/*      */ 
/*  712 */         proc.setColMetaData(this.baseTds.getColumns());
/*  713 */         proc.setParamMetaData(this.baseTds.getParameters());
/*  714 */         pstmt.setColMetaData(proc.getColMetaData());
/*  715 */         pstmt.setParamMetaData(proc.getParamMetaData());
/*      */       }
/*      */ 
/*  718 */       addCachedProcedure(key, proc);
/*      */     }
/*      */ 
/*  723 */     if (pstmt.handles == null) {
/*  724 */       pstmt.handles = new HashSet(10);
/*      */     }
/*      */ 
/*  727 */     pstmt.handles.add(proc);
/*      */ 
/*  730 */     return proc.toString();
/*      */   }
/*      */ 
/*      */   void addCachedProcedure(String key, ProcEntry proc)
/*      */   {
/*  743 */     this.statementCache.put(key, proc);
/*      */ 
/*  745 */     if ((this.autoCommit) || (proc.getType() != 1) || (this.serverType != 1)) {
/*      */       return;
/*      */     }
/*  748 */     this.procInTran.add(key);
/*      */   }
/*      */ 
/*      */   void removeCachedProcedure(String key)
/*      */   {
/*  761 */     this.statementCache.remove(key);
/*      */ 
/*  763 */     if (!(this.autoCommit))
/*  764 */       this.procInTran.remove(key);
/*      */   }
/*      */ 
/*      */   int getMaxStatements()
/*      */   {
/*  774 */     return this.maxStatements;
/*      */   }
/*      */ 
/*      */   public int getServerType()
/*      */   {
/*  784 */     return this.serverType;
/*      */   }
/*      */ 
/*      */   void setNetPacketSize(int size)
/*      */   {
/*  793 */     this.netPacketSize = size;
/*      */   }
/*      */ 
/*      */   int getNetPacketSize()
/*      */   {
/*  802 */     return this.netPacketSize;
/*      */   }
/*      */ 
/*      */   int getRowCount()
/*      */   {
/*  811 */     return this.rowCount;
/*      */   }
/*      */ 
/*      */   void setRowCount(int count)
/*      */   {
/*  820 */     this.rowCount = count;
/*      */   }
/*      */ 
/*      */   public int getTextSize()
/*      */   {
/*  829 */     return this.textSize;
/*      */   }
/*      */ 
/*      */   public void setTextSize(int textSize)
/*      */   {
/*  838 */     this.textSize = textSize;
/*      */   }
/*      */ 
/*      */   boolean getLastUpdateCount()
/*      */   {
/*  847 */     return this.lastUpdateCount;
/*      */   }
/*      */ 
/*      */   int getMaxPrecision()
/*      */   {
/*  856 */     return this.maxPrecision;
/*      */   }
/*      */ 
/*      */   long getLobBuffer()
/*      */   {
/*  865 */     return this.lobBuffer;
/*      */   }
/*      */ 
/*      */   int getPrepareSql()
/*      */   {
/*  874 */     return this.prepareSql;
/*      */   }
/*      */ 
/*      */   int getBatchSize()
/*      */   {
/*  883 */     return this.batchSize;
/*      */   }
/*      */ 
/*      */   boolean getUseMetadataCache()
/*      */   {
/*  894 */     return this.useMetadataCache;
/*      */   }
/*      */ 
/*      */   boolean getUseCursors()
/*      */   {
/*  904 */     return this.useCursors;
/*      */   }
/*      */ 
/*      */   boolean getUseLOBs()
/*      */   {
/*  916 */     return this.useLOBs;
/*      */   }
/*      */ 
/*      */   boolean getUseNTLMv2()
/*      */   {
/*  927 */     return this.useNTLMv2;
/*      */   }
/*      */ 
/*      */   String getAppName()
/*      */   {
/*  936 */     return this.appName;
/*      */   }
/*      */ 
/*      */   String getBindAddress()
/*      */   {
/*  945 */     return this.bindAddress;
/*      */   }
/*      */ 
/*      */   File getBufferDir()
/*      */   {
/*  954 */     return this.bufferDir;
/*      */   }
/*      */ 
/*      */   int getBufferMaxMemory()
/*      */   {
/*  963 */     return this.bufferMaxMemory;
/*      */   }
/*      */ 
/*      */   int getBufferMinPackets()
/*      */   {
/*  972 */     return this.bufferMinPackets;
/*      */   }
/*      */ 
/*      */   String getDatabaseName()
/*      */   {
/*  981 */     return this.databaseName;
/*      */   }
/*      */ 
/*      */   String getDomainName()
/*      */   {
/*  990 */     return this.domainName;
/*      */   }
/*      */ 
/*      */   String getInstanceName()
/*      */   {
/*  999 */     return this.instanceName;
/*      */   }
/*      */ 
/*      */   int getLoginTimeout()
/*      */   {
/* 1008 */     return this.loginTimeout;
/*      */   }
/*      */ 
/*      */   int getSocketTimeout()
/*      */   {
/* 1017 */     return this.socketTimeout;
/*      */   }
/*      */ 
/*      */   boolean getSocketKeepAlive()
/*      */   {
/* 1026 */     return this.socketKeepAlive;
/*      */   }
/*      */ 
/*      */   int getProcessId()
/*      */   {
/* 1036 */     return processId.intValue();
/*      */   }
/*      */ 
/*      */   String getMacAddress()
/*      */   {
/* 1045 */     return this.macAddress;
/*      */   }
/*      */ 
/*      */   boolean getNamedPipe()
/*      */   {
/* 1054 */     return this.namedPipe;
/*      */   }
/*      */ 
/*      */   int getPacketSize()
/*      */   {
/* 1063 */     return this.packetSize;
/*      */   }
/*      */ 
/*      */   String getPassword()
/*      */   {
/* 1072 */     return this.password;
/*      */   }
/*      */ 
/*      */   int getPortNumber()
/*      */   {
/* 1081 */     return this.portNumber;
/*      */   }
/*      */ 
/*      */   String getProgName()
/*      */   {
/* 1090 */     return this.progName;
/*      */   }
/*      */ 
/*      */   String getServerName()
/*      */   {
/* 1099 */     return this.serverName;
/*      */   }
/*      */ 
/*      */   boolean getTcpNoDelay()
/*      */   {
/* 1108 */     return this.tcpNoDelay;
/*      */   }
/*      */ 
/*      */   boolean getUseJCIFS()
/*      */   {
/* 1117 */     return this.useJCIFS;
/*      */   }
/*      */ 
/*      */   String getUser()
/*      */   {
/* 1126 */     return this.user;
/*      */   }
/*      */ 
/*      */   String getWsid()
/*      */   {
/* 1135 */     return this.wsid;
/*      */   }
/*      */ 
/*      */   protected void unpackProperties(Properties info)
/*      */     throws SQLException
/*      */   {
/* 1147 */     this.serverName = info.getProperty(Messages.get("prop.servername"));
/* 1148 */     this.portNumber = parseIntegerProperty(info, "prop.portnumber");
/* 1149 */     this.serverType = parseIntegerProperty(info, "prop.servertype");
/* 1150 */     this.databaseName = info.getProperty(Messages.get("prop.databasename"));
/* 1151 */     this.instanceName = info.getProperty(Messages.get("prop.instance"));
/* 1152 */     this.domainName = info.getProperty(Messages.get("prop.domain"));
/* 1153 */     this.user = info.getProperty(Messages.get("prop.user"));
/* 1154 */     this.password = info.getProperty(Messages.get("prop.password"));
/* 1155 */     this.macAddress = info.getProperty(Messages.get("prop.macaddress"));
/* 1156 */     this.appName = info.getProperty(Messages.get("prop.appname"));
/* 1157 */     this.progName = info.getProperty(Messages.get("prop.progname"));
/* 1158 */     this.wsid = info.getProperty(Messages.get("prop.wsid"));
/* 1159 */     this.serverCharset = info.getProperty(Messages.get("prop.charset"));
/* 1160 */     this.language = info.getProperty(Messages.get("prop.language"));
/* 1161 */     this.bindAddress = info.getProperty(Messages.get("prop.bindaddress"));
/* 1162 */     this.lastUpdateCount = parseBooleanProperty(info, "prop.lastupdatecount");
/* 1163 */     this.useUnicode = parseBooleanProperty(info, "prop.useunicode");
/* 1164 */     this.namedPipe = parseBooleanProperty(info, "prop.namedpipe");
/* 1165 */     this.tcpNoDelay = parseBooleanProperty(info, "prop.tcpnodelay");
/* 1166 */     this.useCursors = ((this.serverType == 1) && (parseBooleanProperty(info, "prop.usecursors")));
/* 1167 */     this.useLOBs = parseBooleanProperty(info, "prop.uselobs");
/* 1168 */     this.useMetadataCache = parseBooleanProperty(info, "prop.cachemetadata");
/* 1169 */     this.xaEmulation = parseBooleanProperty(info, "prop.xaemulation");
/* 1170 */     this.useJCIFS = parseBooleanProperty(info, "prop.usejcifs");
/* 1171 */     this.charsetSpecified = (this.serverCharset.length() > 0);
/* 1172 */     this.useNTLMv2 = parseBooleanProperty(info, "prop.usentlmv2");
/*      */ 
/* 1176 */     if (this.domainName != null) {
/* 1177 */       this.domainName = this.domainName.toUpperCase();
/*      */     }
/* 1179 */     Integer parsedTdsVersion = DefaultProperties.getTdsVersion(info.getProperty(Messages.get("prop.tds")));
/*      */ 
/* 1181 */     if (parsedTdsVersion == null) {
/* 1182 */       throw new SQLException(Messages.get("error.connection.badprop", Messages.get("prop.tds")), "08001");
/*      */     }
/*      */ 
/* 1185 */     this.tdsVersion = parsedTdsVersion.intValue();
/*      */ 
/* 1187 */     this.packetSize = parseIntegerProperty(info, "prop.packetsize");
/* 1188 */     if (this.packetSize < 512) {
/* 1189 */       if (this.tdsVersion >= 3)
/*      */       {
/* 1191 */         this.packetSize = ((this.packetSize == 0) ? 0 : 4096);
/* 1192 */       } else if (this.tdsVersion == 1)
/*      */       {
/* 1194 */         this.packetSize = 512;
/*      */       }
/*      */     }
/* 1197 */     if (this.packetSize > 32768) {
/* 1198 */       this.packetSize = 32768;
/*      */     }
/* 1200 */     this.packetSize = (this.packetSize / 512 * 512);
/*      */ 
/* 1202 */     this.loginTimeout = parseIntegerProperty(info, "prop.logintimeout");
/* 1203 */     this.socketTimeout = parseIntegerProperty(info, "prop.sotimeout");
/* 1204 */     this.socketKeepAlive = parseBooleanProperty(info, "prop.sokeepalive");
/* 1205 */     this.autoCommit = parseBooleanProperty(info, "prop.autocommit");
/*      */ 
/* 1207 */     String pid = info.getProperty(Messages.get("prop.processid"));
/* 1208 */     if ("compute".equals(pid))
/*      */     {
/* 1210 */       if (processId == null)
/*      */       {
/* 1212 */         processId = new Integer(new Random(System.currentTimeMillis()).nextInt(32768)); }
/*      */     }
/* 1214 */     else if (pid.length() > 0) {
/* 1215 */       processId = new Integer(parseIntegerProperty(info, "prop.processid"));
/*      */     }
/*      */ 
/* 1218 */     this.lobBuffer = parseLongProperty(info, "prop.lobbuffer");
/*      */ 
/* 1220 */     this.maxStatements = parseIntegerProperty(info, "prop.maxstatements");
/*      */ 
/* 1222 */     this.statementCache = new ProcedureCache(this.maxStatements);
/* 1223 */     this.prepareSql = parseIntegerProperty(info, "prop.preparesql");
/* 1224 */     if (this.prepareSql < 0)
/* 1225 */       this.prepareSql = 0;
/* 1226 */     else if (this.prepareSql > 3) {
/* 1227 */       this.prepareSql = 3;
/*      */     }
/*      */ 
/* 1230 */     if ((this.tdsVersion < 3) && (this.prepareSql == 3)) {
/* 1231 */       this.prepareSql = 2;
/*      */     }
/*      */ 
/* 1234 */     if ((this.tdsVersion < 2) && (this.prepareSql == 2)) {
/* 1235 */       this.prepareSql = 1;
/*      */     }
/*      */ 
/* 1238 */     this.ssl = info.getProperty(Messages.get("prop.ssl"));
/*      */ 
/* 1240 */     this.batchSize = parseIntegerProperty(info, "prop.batchsize");
/* 1241 */     if (this.batchSize < 0) {
/* 1242 */       throw new SQLException(Messages.get("error.connection.badprop", Messages.get("prop.batchsize")), "08001");
/*      */     }
/*      */ 
/* 1246 */     this.bufferDir = new File(info.getProperty(Messages.get("prop.bufferdir")));
/* 1247 */     if ((!(this.bufferDir.isDirectory())) && 
/* 1248 */       (!(this.bufferDir.mkdirs()))) {
/* 1249 */       throw new SQLException(Messages.get("error.connection.badprop", Messages.get("prop.bufferdir")), "08001");
/*      */     }
/*      */ 
/* 1254 */     this.bufferMaxMemory = parseIntegerProperty(info, "prop.buffermaxmemory");
/* 1255 */     if (this.bufferMaxMemory < 0) {
/* 1256 */       throw new SQLException(Messages.get("error.connection.badprop", Messages.get("prop.buffermaxmemory")), "08001");
/*      */     }
/*      */ 
/* 1260 */     this.bufferMinPackets = parseIntegerProperty(info, "prop.bufferminpackets");
/* 1261 */     if (this.bufferMinPackets < 1)
/* 1262 */       throw new SQLException(Messages.get("error.connection.badprop", Messages.get("prop.bufferminpackets")), "08001");
/*      */   }
/*      */ 
/*      */   private static boolean parseBooleanProperty(Properties info, String key)
/*      */     throws SQLException
/*      */   {
/* 1277 */     String propertyName = Messages.get(key);
/* 1278 */     String prop = info.getProperty(propertyName);
/* 1279 */     if ((prop != null) && (!("true".equalsIgnoreCase(prop))) && (!("false".equalsIgnoreCase(prop)))) {
/* 1280 */       throw new SQLException(Messages.get("error.connection.badprop", propertyName), "08001");
/*      */     }
/* 1282 */     return "true".equalsIgnoreCase(prop);
/*      */   }
/*      */ 
/*      */   private static int parseIntegerProperty(Properties info, String key)
/*      */     throws SQLException
/*      */   {
/* 1296 */     String propertyName = Messages.get(key);
/*      */     try {
/* 1298 */       return Integer.parseInt(info.getProperty(propertyName));
/*      */     } catch (NumberFormatException e) {
/* 1300 */       throw new SQLException(Messages.get("error.connection.badprop", propertyName), "08001");
/*      */     }
/*      */   }
/*      */ 
/*      */   private static long parseLongProperty(Properties info, String key)
/*      */     throws SQLException
/*      */   {
/* 1316 */     String propertyName = Messages.get(key);
/*      */     try {
/* 1318 */       return Long.parseLong(info.getProperty(propertyName));
/*      */     } catch (NumberFormatException e) {
/* 1320 */       throw new SQLException(Messages.get("error.connection.badprop", propertyName), "08001");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String getCharset()
/*      */   {
/* 1331 */     return this.charsetInfo.getCharset();
/*      */   }
/*      */ 
/*      */   protected boolean isWideChar()
/*      */   {
/* 1340 */     return this.charsetInfo.isWideChars();
/*      */   }
/*      */ 
/*      */   protected CharsetInfo getCharsetInfo()
/*      */   {
/* 1349 */     return this.charsetInfo;
/*      */   }
/*      */ 
/*      */   protected boolean getUseUnicode()
/*      */   {
/* 1358 */     return this.useUnicode;
/*      */   }
/*      */ 
/*      */   protected boolean getSybaseInfo(int flag)
/*      */   {
/* 1367 */     return ((this.sybaseInfo & flag) != 0);
/*      */   }
/*      */ 
/*      */   protected void setSybaseInfo(int mask)
/*      */   {
/* 1376 */     this.sybaseInfo = mask;
/*      */   }
/*      */ 
/*      */   protected void setServerCharset(String charset)
/*      */     throws SQLException
/*      */   {
/* 1386 */     if (this.charsetSpecified) {
/* 1387 */       Logger.println("Server charset " + charset + ". Ignoring as user requested " + this.serverCharset + '.');
/*      */ 
/* 1389 */       return;
/*      */     }
/*      */ 
/* 1392 */     if (!(charset.equals(this.serverCharset))) {
/* 1393 */       loadCharset(charset);
/*      */ 
/* 1395 */       if (Logger.isActive())
/* 1396 */         Logger.println("Set charset to " + this.serverCharset + '/' + this.charsetInfo);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void loadCharset(String charset)
/*      */     throws SQLException
/*      */   {
/* 1409 */     if ((getServerType() == 1) && (charset.equalsIgnoreCase("iso_1")))
/*      */     {
/* 1411 */       charset = "Cp1252";
/*      */     }
/*      */ 
/* 1416 */     CharsetInfo tmp = CharsetInfo.getCharset(charset);
/*      */ 
/* 1418 */     if (tmp == null) {
/* 1419 */       throw new SQLException(Messages.get("error.charset.nomapping", charset), "2C000");
/*      */     }
/*      */ 
/* 1423 */     loadCharset(tmp, charset);
/* 1424 */     this.serverCharset = charset;
/*      */   }
/*      */ 
/*      */   private void loadCharset(CharsetInfo ci, String ref)
/*      */     throws SQLException
/*      */   {
/*      */     try
/*      */     {
/* 1434 */       "This is a test".getBytes(ci.getCharset());
/*      */ 
/* 1436 */       this.charsetInfo = ci;
/*      */     } catch (UnsupportedEncodingException ex) {
/* 1438 */       throw new SQLException(Messages.get("error.charset.invalid", ref, ci.getCharset()), "2C000");
/*      */     }
/*      */ 
/* 1444 */     this.socket.setCharsetInfo(this.charsetInfo);
/*      */   }
/*      */ 
/*      */   private String determineServerCharset()
/*      */     throws SQLException
/*      */   {
/* 1462 */     String queryStr = null;
/*      */ 
/* 1464 */     switch (this.serverType)
/*      */     {
/*      */     case 1:
/* 1466 */       if (this.databaseProductVersion.indexOf("6.5") >= 0) {
/* 1467 */         queryStr = "select name from master.dbo.syscharsets where id = (select csid from master.dbo.syscharsets, master.dbo.sysconfigures where config=1123 and id = value)";
/*      */       }
/*      */       else
/*      */       {
/* 1471 */         throw new SQLException("Please use TDS protocol version 7.0 or higher");
/*      */       }
/*      */     case 2:
/* 1477 */       queryStr = "select name from master.dbo.syscharsets where id = (select value from master.dbo.sysconfigures where config=131)";
/*      */     }
/*      */ 
/* 1481 */     Statement stmt = createStatement();
/* 1482 */     ResultSet rs = stmt.executeQuery(queryStr);
/* 1483 */     rs.next();
/* 1484 */     String charset = rs.getString(1);
/* 1485 */     rs.close();
/* 1486 */     stmt.close();
/*      */ 
/* 1488 */     return charset;
/*      */   }
/*      */ 
/*      */   void setCollation(byte[] collation)
/*      */     throws SQLException
/*      */   {
/* 1508 */     String strCollation = "0x" + Support.toHex(collation);
/*      */ 
/* 1510 */     if (this.charsetSpecified) {
/* 1511 */       Logger.println("Server collation " + strCollation + ". Ignoring as user requested " + this.serverCharset + '.');
/*      */ 
/* 1513 */       return;
/*      */     }
/*      */ 
/* 1516 */     CharsetInfo tmp = CharsetInfo.getCharset(collation);
/*      */ 
/* 1518 */     loadCharset(tmp, strCollation);
/* 1519 */     this.collation = collation;
/*      */ 
/* 1521 */     if (Logger.isActive())
/* 1522 */       Logger.println("Set collation to " + strCollation + '/' + this.charsetInfo);
/*      */   }
/*      */ 
/*      */   byte[] getCollation()
/*      */   {
/* 1533 */     return this.collation;
/*      */   }
/*      */ 
/*      */   boolean isCharsetSpecified()
/*      */   {
/* 1542 */     return this.charsetSpecified;
/*      */   }
/*      */ 
/*      */   protected void setDatabase(String newDb, String oldDb)
/*      */     throws SQLException
/*      */   {
/* 1554 */     if ((this.currentDatabase != null) && (!(oldDb.equalsIgnoreCase(this.currentDatabase)))) {
/* 1555 */       throw new SQLException(Messages.get("error.connection.dbmismatch", oldDb, this.databaseName), "HY096");
/*      */     }
/*      */ 
/* 1560 */     this.currentDatabase = newDb;
/*      */ 
/* 1562 */     if (Logger.isActive())
/* 1563 */       Logger.println("Changed database from " + oldDb + " to " + newDb);
/*      */   }
/*      */ 
/*      */   protected void setDBServerInfo(String databaseProductName, int databaseMajorVersion, int databaseMinorVersion, int buildNumber)
/*      */   {
/* 1579 */     this.databaseProductName = databaseProductName;
/* 1580 */     this.databaseMajorVersion = databaseMajorVersion;
/* 1581 */     this.databaseMinorVersion = databaseMinorVersion;
/*      */ 
/* 1583 */     if (this.tdsVersion >= 3) {
/* 1584 */       StringBuffer buf = new StringBuffer(10);
/*      */ 
/* 1586 */       if (databaseMajorVersion < 10) {
/* 1587 */         buf.append('0');
/*      */       }
/*      */ 
/* 1590 */       buf.append(databaseMajorVersion).append('.');
/*      */ 
/* 1592 */       if (databaseMinorVersion < 10) {
/* 1593 */         buf.append('0');
/*      */       }
/*      */ 
/* 1596 */       buf.append(databaseMinorVersion).append('.');
/* 1597 */       buf.append(buildNumber);
/*      */ 
/* 1599 */       while (buf.length() < 10) {
/* 1600 */         buf.insert(6, '0');
/*      */       }
/*      */ 
/* 1603 */       this.databaseProductVersion = buf.toString();
/*      */     } else {
/* 1605 */       this.databaseProductVersion = databaseMajorVersion + "." + databaseMinorVersion;
/*      */     }
/*      */   }
/*      */ 
/*      */   synchronized void removeStatement(JtdsStatement statement)
/*      */     throws SQLException
/*      */   {
/* 1622 */     synchronized (this.statements) {
/* 1623 */       for (int i = 0; i < this.statements.size(); ++i) {
/* 1624 */         WeakReference wr = (WeakReference)this.statements.get(i);
/*      */ 
/* 1626 */         if (wr != null) {
/* 1627 */           Statement stmt = (Statement)wr.get();
/*      */ 
/* 1631 */           if ((stmt == null) || (stmt == statement)) {
/* 1632 */             this.statements.set(i, null);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1638 */     if (!(statement instanceof JtdsPreparedStatement)) {
/*      */       return;
/*      */     }
/* 1641 */     Collection handles = this.statementCache.getObsoleteHandles(((JtdsPreparedStatement)statement).handles);
/*      */     Iterator iterator;
/* 1644 */     if (handles != null)
/* 1645 */       if (this.serverType == 1)
/*      */       {
/* 1647 */         StringBuffer cleanupSql = new StringBuffer(handles.size() * 32);
/* 1648 */         for (Iterator iterator = handles.iterator(); iterator.hasNext(); ) {
/* 1649 */           ProcEntry pe = (ProcEntry)iterator.next();
/*      */ 
/* 1652 */           pe.appendDropSQL(cleanupSql);
/*      */         }
/* 1654 */         if (cleanupSql.length() > 0) {
/* 1655 */           this.baseTds.executeSQL(cleanupSql.toString(), null, null, true, 0, -1, -1, true);
/*      */ 
/* 1657 */           this.baseTds.clearResponseQueue();
/*      */         }
/*      */       }
/*      */       else {
/* 1661 */         for (iterator = handles.iterator(); iterator.hasNext(); ) {
/* 1662 */           ProcEntry pe = (ProcEntry)iterator.next();
/* 1663 */           if (pe.toString() != null)
/*      */           {
/* 1665 */             this.baseTds.sybaseUnPrepare(pe.toString());
/*      */           }
/*      */         }
/*      */       }
/*      */   }
/*      */ 
/*      */   void addStatement(JtdsStatement statement)
/*      */   {
/* 1682 */     synchronized (this.statements) {
/* 1683 */       for (int i = 0; i < this.statements.size(); ++i) {
/* 1684 */         WeakReference wr = (WeakReference)this.statements.get(i);
/*      */ 
/* 1690 */         if ((wr == null) || (wr.get() == null)) {
/* 1691 */           this.statements.set(i, new WeakReference(statement));
/* 1692 */           return;
/*      */         }
/*      */       }
/*      */ 
/* 1696 */       this.statements.add(new WeakReference(statement));
/*      */     }
/*      */   }
/*      */ 
/*      */   void checkOpen()
/*      */     throws SQLException
/*      */   {
/* 1706 */     if (this.closed)
/* 1707 */       throw new SQLException(Messages.get("error.generic.closed", "Connection"), "HY010");
/*      */   }
/*      */ 
/*      */   void checkLocal(String method)
/*      */     throws SQLException
/*      */   {
/* 1719 */     if (this.xaTransaction)
/* 1720 */       throw new SQLException(Messages.get("error.connection.badxaop", method), "HY010");
/*      */   }
/*      */ 
/*      */   static void notImplemented(String method)
/*      */     throws SQLException
/*      */   {
/* 1732 */     throw new SQLException(Messages.get("error.generic.notimp", method), "HYC00");
/*      */   }
/*      */ 
/*      */   public int getDatabaseMajorVersion()
/*      */   {
/* 1742 */     return this.databaseMajorVersion;
/*      */   }
/*      */ 
/*      */   public int getDatabaseMinorVersion()
/*      */   {
/* 1751 */     return this.databaseMinorVersion;
/*      */   }
/*      */ 
/*      */   String getDatabaseProductName()
/*      */   {
/* 1760 */     return this.databaseProductName;
/*      */   }
/*      */ 
/*      */   String getDatabaseProductVersion()
/*      */   {
/* 1769 */     return this.databaseProductVersion;
/*      */   }
/*      */ 
/*      */   String getURL()
/*      */   {
/* 1778 */     return this.url;
/*      */   }
/*      */ 
/*      */   public String getRmHost()
/*      */   {
/* 1789 */     return this.serverName + ':' + this.portNumber;
/*      */   }
/*      */ 
/*      */   void setClosed()
/*      */   {
/* 1796 */     if (!(this.closed)) {
/* 1797 */       this.closed = true;
/*      */       try
/*      */       {
/* 1802 */         this.socket.close();
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   synchronized byte[][] sendXaPacket(int[] args, byte[] data)
/*      */     throws SQLException
/*      */   {
/* 1822 */     ParamInfo[] params = new ParamInfo[6];
/* 1823 */     params[0] = new ParamInfo(4, null, 2);
/* 1824 */     params[1] = new ParamInfo(4, new Integer(args[1]), 0);
/* 1825 */     params[2] = new ParamInfo(4, new Integer(args[2]), 0);
/* 1826 */     params[3] = new ParamInfo(4, new Integer(args[3]), 0);
/* 1827 */     params[4] = new ParamInfo(4, new Integer(args[4]), 0);
/* 1828 */     params[5] = new ParamInfo(-3, data, 1);
/*      */ 
/* 1832 */     this.baseTds.executeSQL(null, "master..xp_jtdsxa", params, false, 0, -1, -1, true);
/*      */ 
/* 1837 */     ArrayList xids = new ArrayList();
/* 1838 */     if (!(this.baseTds.isEndOfResponse())) while (true) {
/* 1839 */         if ((this.baseTds.getMoreResults()) && 
/* 1841 */           (this.baseTds.getNextRow()));
/* 1842 */         Object[] row = this.baseTds.getRowData();
/* 1843 */         if ((row.length == 1) && (row[0] instanceof byte[])) {
/* 1844 */           xids.add(row[0]);
/*      */         }
/*      */       }
/*      */ 
/*      */ 
/* 1849 */     this.messages.checkErrors();
/* 1850 */     if (params[0].getOutValue() instanceof Integer)
/*      */     {
/* 1852 */       args[0] = ((Integer)params[0].getOutValue()).intValue();
/*      */     }
/*      */     else args[0] = -7;
/*      */ 
/* 1856 */     if (xids.size() > 0)
/*      */     {
/* 1858 */       byte[][] list = new byte[xids.size()][];
/* 1859 */       for (int i = 0; i < xids.size(); ++i) {
/* 1860 */         list[i] = ((byte[])xids.get(i));
/*      */       }
/* 1862 */       return list;
/*      */     }
/* 1864 */     if (params[5].getOutValue() instanceof byte[])
/*      */     {
/* 1867 */       byte[][] cookie = new byte[1][];
/* 1868 */       cookie[0] = ((byte[])params[5].getOutValue());
/* 1869 */       return cookie;
/*      */     }
/*      */ 
/* 1872 */     return ((byte[][])null);
/*      */   }
/*      */ 
/*      */   synchronized void enlistConnection(byte[] oleTranID)
/*      */     throws SQLException
/*      */   {
/* 1884 */     if (oleTranID != null)
/*      */     {
/* 1886 */       this.prepareSql = 2;
/* 1887 */       this.baseTds.enlistConnection(1, oleTranID);
/* 1888 */       this.xaTransaction = true;
/*      */     } else {
/* 1890 */       this.baseTds.enlistConnection(1, null);
/* 1891 */       this.xaTransaction = false;
/*      */     }
/*      */   }
/*      */ 
/*      */   void setXid(Object xid)
/*      */   {
/* 1901 */     this.xid = xid;
/* 1902 */     this.xaTransaction = (xid != null);
/*      */   }
/*      */ 
/*      */   Object getXid()
/*      */   {
/* 1911 */     return this.xid;
/*      */   }
/*      */ 
/*      */   void setXaState(int value)
/*      */   {
/* 1920 */     this.xaState = value;
/*      */   }
/*      */ 
/*      */   int getXaState()
/*      */   {
/* 1929 */     return this.xaState;
/*      */   }
/*      */ 
/*      */   boolean isXaEmulation()
/*      */   {
/* 1937 */     return this.xaEmulation;
/*      */   }
/*      */ 
/*      */   Semaphore getMutex()
/*      */   {
/* 1948 */     boolean interrupted = Thread.interrupted();
/*      */     try
/*      */     {
/* 1951 */       this.mutex.acquire();
/*      */     } catch (InterruptedException e) {
/* 1953 */       throw new IllegalStateException("Thread execution interrupted");
/*      */     }
/*      */ 
/* 1956 */     if (interrupted)
/*      */     {
/* 1958 */       Thread.currentThread().interrupt();
/*      */     }
/*      */ 
/* 1961 */     return this.mutex;
/*      */   }
/*      */ 
/*      */   synchronized void releaseTds(TdsCore tds)
/*      */     throws SQLException
/*      */   {
/* 1973 */     if (this.cachedTds != null)
/*      */     {
/* 1975 */       tds.close();
/*      */     }
/*      */     else {
/* 1978 */       tds.clearResponseQueue();
/* 1979 */       tds.cleanUp();
/* 1980 */       this.cachedTds = tds;
/*      */     }
/*      */   }
/*      */ 
/*      */   synchronized TdsCore getCachedTds()
/*      */   {
/* 1992 */     TdsCore result = this.cachedTds;
/* 1993 */     this.cachedTds = null;
/* 1994 */     return result;
/*      */   }
/*      */ 
/*      */   public int getHoldability()
/*      */     throws SQLException
/*      */   {
/* 2002 */     checkOpen();
/*      */ 
/* 2004 */     return 1;
/*      */   }
/*      */ 
/*      */   public synchronized int getTransactionIsolation() throws SQLException {
/* 2008 */     checkOpen();
/*      */ 
/* 2010 */     return this.transactionIsolation;
/*      */   }
/*      */ 
/*      */   public synchronized void clearWarnings() throws SQLException {
/* 2014 */     checkOpen();
/* 2015 */     this.messages.clearWarnings();
/*      */   }
/*      */ 
/*      */   public synchronized void close()
/*      */     throws SQLException
/*      */   {
/* 2036 */     if (this.closed)
/*      */       return;
/*      */     try
/*      */     {
/*      */       ArrayList tmpList;
/* 2043 */       synchronized (this.statements) {
/* 2044 */         tmpList = new ArrayList(this.statements);
/* 2045 */         this.statements.clear();
/*      */       }
/*      */ 
/* 2048 */       for (int i = 0; i < tmpList.size(); ++i) {
/* 2049 */         WeakReference wr = (WeakReference)tmpList.get(i);
/*      */ 
/* 2051 */         if (wr != null) {
/* 2052 */           Statement stmt = (Statement)wr.get();
/* 2053 */           if (stmt == null) continue;
/*      */           try {
/* 2055 */             stmt.close();
/*      */           }
/*      */           catch (SQLException ex)
/*      */           {
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/* 2065 */         if (this.baseTds != null) {
/* 2066 */           this.baseTds.closeConnection();
/* 2067 */           this.baseTds.close();
/*      */         }
/*      */ 
/* 2070 */         if (this.cachedTds != null) {
/* 2071 */           this.cachedTds.close();
/* 2072 */           this.cachedTds = null;
/*      */         }
/*      */       }
/*      */       catch (SQLException ex)
/*      */       {
/*      */       }
/* 2078 */       if (this.socket != null)
/* 2079 */         this.socket.close();
/*      */     }
/*      */     catch (IOException e) {
/*      */     }
/*      */     finally {
/* 2084 */       this.closed = true;
/* 2085 */       if (--connections == 0)
/* 2086 */         TimerThread.stopTimer();
/*      */     }
/*      */   }
/*      */ 
/*      */   public synchronized void commit()
/*      */     throws SQLException
/*      */   {
/* 2093 */     checkOpen();
/* 2094 */     checkLocal("commit");
/*      */ 
/* 2096 */     if (getAutoCommit()) {
/* 2097 */       throw new SQLException(Messages.get("error.connection.autocommit", "commit"), "25000");
/*      */     }
/*      */ 
/* 2102 */     this.baseTds.submitSQL("IF @@TRANCOUNT > 0 COMMIT TRAN");
/* 2103 */     this.procInTran.clear();
/* 2104 */     clearSavepoints();
/*      */   }
/*      */ 
/*      */   public synchronized void rollback() throws SQLException {
/* 2108 */     checkOpen();
/* 2109 */     checkLocal("rollback");
/*      */ 
/* 2111 */     if (getAutoCommit()) {
/* 2112 */       throw new SQLException(Messages.get("error.connection.autocommit", "rollback"), "25000");
/*      */     }
/*      */ 
/* 2117 */     this.baseTds.submitSQL("IF @@TRANCOUNT > 0 ROLLBACK TRAN");
/*      */ 
/* 2119 */     for (int i = 0; i < this.procInTran.size(); ++i) {
/* 2120 */       String key = (String)this.procInTran.get(i);
/* 2121 */       if (key != null) {
/* 2122 */         this.statementCache.remove(key);
/*      */       }
/*      */     }
/* 2125 */     this.procInTran.clear();
/*      */ 
/* 2127 */     clearSavepoints();
/*      */   }
/*      */ 
/*      */   public synchronized boolean getAutoCommit() throws SQLException {
/* 2131 */     checkOpen();
/*      */ 
/* 2133 */     return this.autoCommit;
/*      */   }
/*      */ 
/*      */   public boolean isClosed() throws SQLException {
/* 2137 */     return this.closed;
/*      */   }
/*      */ 
/*      */   public boolean isReadOnly() throws SQLException {
/* 2141 */     checkOpen();
/*      */ 
/* 2143 */     return this.readOnly;
/*      */   }
/*      */ 
/*      */   public void setHoldability(int holdability) throws SQLException {
/* 2147 */     checkOpen();
/* 2148 */     switch (holdability)
/*      */     {
/*      */     case 1:
/* 2150 */       break;
/*      */     case 2:
/* 2152 */       throw new SQLException(Messages.get("error.generic.optvalue", "CLOSE_CURSORS_AT_COMMIT", "setHoldability"), "HY092");
/*      */     default:
/* 2158 */       throw new SQLException(Messages.get("error.generic.badoption", Integer.toString(holdability), "holdability"), "HY092");
/*      */     }
/*      */   }
/*      */ 
/*      */   public synchronized void setTransactionIsolation(int level)
/*      */     throws SQLException
/*      */   {
/* 2167 */     checkOpen();
/*      */ 
/* 2169 */     if (this.transactionIsolation == level)
/*      */     {
/* 2171 */       return;
/*      */     }
/*      */ 
/* 2174 */     String sql = "SET TRANSACTION ISOLATION LEVEL ";
/* 2175 */     boolean sybase = this.serverType == 2;
/*      */ 
/* 2177 */     switch (level)
/*      */     {
/*      */     case 1:
/* 2179 */       sql = sql + ((sybase) ? "0" : "READ UNCOMMITTED");
/* 2180 */       break;
/*      */     case 2:
/* 2182 */       sql = sql + ((sybase) ? "1" : "READ COMMITTED");
/* 2183 */       break;
/*      */     case 4:
/* 2185 */       sql = sql + ((sybase) ? "2" : "REPEATABLE READ");
/* 2186 */       break;
/*      */     case 8:
/* 2188 */       sql = sql + ((sybase) ? "3" : "SERIALIZABLE");
/* 2189 */       break;
/*      */     case 4096:
/* 2191 */       if (sybase) {
/* 2192 */         throw new SQLException(Messages.get("error.generic.optvalue", "TRANSACTION_SNAPSHOT", "setTransactionIsolation"), "HY024");
/*      */       }
/*      */ 
/* 2198 */       sql = sql + "SNAPSHOT";
/*      */ 
/* 2200 */       break;
/*      */     case 0:
/* 2202 */       throw new SQLException(Messages.get("error.generic.optvalue", "TRANSACTION_NONE", "setTransactionIsolation"), "HY024");
/*      */     default:
/* 2208 */       throw new SQLException(Messages.get("error.generic.badoption", Integer.toString(level), "level"), "HY092");
/*      */     }
/*      */ 
/* 2215 */     this.transactionIsolation = level;
/* 2216 */     this.baseTds.submitSQL(sql);
/*      */   }
/*      */ 
/*      */   public synchronized void setAutoCommit(boolean autoCommit) throws SQLException {
/* 2220 */     checkOpen();
/* 2221 */     checkLocal("setAutoCommit");
/*      */ 
/* 2223 */     if (this.autoCommit == autoCommit)
/*      */     {
/* 2231 */       return;
/*      */     }
/*      */ 
/* 2234 */     StringBuffer sql = new StringBuffer(70);
/*      */ 
/* 2236 */     if (!(this.autoCommit))
/*      */     {
/* 2239 */       sql.append("IF @@TRANCOUNT > 0 COMMIT TRAN\r\n");
/*      */     }
/*      */ 
/* 2242 */     if (this.serverType == 2) {
/* 2243 */       if (autoCommit)
/* 2244 */         sql.append("SET CHAINED OFF");
/*      */       else {
/* 2246 */         sql.append("SET CHAINED ON");
/*      */       }
/*      */     }
/* 2249 */     else if (autoCommit)
/* 2250 */       sql.append("SET IMPLICIT_TRANSACTIONS OFF");
/*      */     else {
/* 2252 */       sql.append("SET IMPLICIT_TRANSACTIONS ON");
/*      */     }
/*      */ 
/* 2256 */     this.baseTds.submitSQL(sql.toString());
/* 2257 */     this.autoCommit = autoCommit;
/*      */   }
/*      */ 
/*      */   public void setReadOnly(boolean readOnly) throws SQLException {
/* 2261 */     checkOpen();
/* 2262 */     this.readOnly = readOnly;
/*      */   }
/*      */ 
/*      */   public synchronized String getCatalog() throws SQLException {
/* 2266 */     checkOpen();
/*      */ 
/* 2268 */     return this.currentDatabase;
/*      */   }
/*      */ 
/*      */   public synchronized void setCatalog(String catalog) throws SQLException {
/* 2272 */     checkOpen();
/*      */ 
/* 2274 */     if ((this.currentDatabase != null) && (this.currentDatabase.equals(catalog))) {
/* 2275 */       return;
/*      */     }
/*      */ 
/* 2278 */     int maxlength = (this.tdsVersion >= 3) ? 128 : 30;
/*      */ 
/* 2280 */     if ((catalog.length() > maxlength) || (catalog.length() < 1)) {
/* 2281 */       throw new SQLException(Messages.get("error.generic.badparam", catalog, "catalog"), "3D000");
/*      */     }
/*      */ 
/* 2288 */     String sql = "use " + catalog;
/*      */ 
/* 2290 */     this.baseTds.submitSQL(sql);
/*      */   }
/*      */ 
/*      */   public DatabaseMetaData getMetaData() throws SQLException {
/* 2294 */     checkOpen();
/*      */ 
/* 2296 */     return new JtdsDatabaseMetaData(this);
/*      */   }
/*      */ 
/*      */   public SQLWarning getWarnings() throws SQLException {
/* 2300 */     checkOpen();
/*      */ 
/* 2302 */     return this.messages.getWarnings();
/*      */   }
/*      */ 
/*      */   public Savepoint setSavepoint() throws SQLException {
/* 2306 */     checkOpen();
/* 2307 */     notImplemented("Connection.setSavepoint()");
/*      */ 
/* 2309 */     return null;
/*      */   }
/*      */ 
/*      */   public void releaseSavepoint(Savepoint savepoint) throws SQLException {
/* 2313 */     checkOpen();
/* 2314 */     notImplemented("Connection.releaseSavepoint(Savepoint)");
/*      */   }
/*      */ 
/*      */   public void rollback(Savepoint savepoint) throws SQLException {
/* 2318 */     checkOpen();
/* 2319 */     notImplemented("Connection.rollback(Savepoint)");
/*      */   }
/*      */ 
/*      */   public Statement createStatement() throws SQLException {
/* 2323 */     checkOpen();
/*      */ 
/* 2325 */     return createStatement(1003, 1007);
/*      */   }
/*      */ 
/*      */   public synchronized Statement createStatement(int type, int concurrency)
/*      */     throws SQLException
/*      */   {
/* 2331 */     checkOpen();
/*      */ 
/* 2333 */     JtdsStatement stmt = new JtdsStatement(this, type, concurrency);
/* 2334 */     addStatement(stmt);
/*      */ 
/* 2336 */     return stmt;
/*      */   }
/*      */ 
/*      */   public Statement createStatement(int type, int concurrency, int holdability) throws SQLException
/*      */   {
/* 2341 */     checkOpen();
/* 2342 */     setHoldability(holdability);
/*      */ 
/* 2344 */     return createStatement(type, concurrency);
/*      */   }
/*      */ 
/*      */   public Map getTypeMap() throws SQLException {
/* 2348 */     checkOpen();
/*      */ 
/* 2350 */     return new HashMap();
/*      */   }
/*      */ 
/*      */   public void setTypeMap(Map map) throws SQLException {
/* 2354 */     checkOpen();
/* 2355 */     notImplemented("Connection.setTypeMap(Map)");
/*      */   }
/*      */ 
/*      */   public String nativeSQL(String sql) throws SQLException {
/* 2359 */     checkOpen();
/*      */ 
/* 2361 */     if ((sql == null) || (sql.length() == 0)) {
/* 2362 */       throw new SQLException(Messages.get("error.generic.nosql"), "HY000");
/*      */     }
/*      */ 
/* 2365 */     String[] result = SQLParser.parse(sql, new ArrayList(), this, false);
/*      */ 
/* 2367 */     return result[0];
/*      */   }
/*      */ 
/*      */   public CallableStatement prepareCall(String sql) throws SQLException {
/* 2371 */     checkOpen();
/*      */ 
/* 2373 */     return prepareCall(sql, 1003, 1007);
/*      */   }
/*      */ 
/*      */   public synchronized CallableStatement prepareCall(String sql, int type, int concurrency)
/*      */     throws SQLException
/*      */   {
/* 2381 */     checkOpen();
/*      */ 
/* 2383 */     if ((sql == null) || (sql.length() == 0)) {
/* 2384 */       throw new SQLException(Messages.get("error.generic.nosql"), "HY000");
/*      */     }
/*      */ 
/* 2387 */     JtdsCallableStatement stmt = new JtdsCallableStatement(this, sql, type, concurrency);
/*      */ 
/* 2391 */     addStatement(stmt);
/*      */ 
/* 2393 */     return stmt;
/*      */   }
/*      */ 
/*      */   public CallableStatement prepareCall(String sql, int type, int concurrency, int holdability)
/*      */     throws SQLException
/*      */   {
/* 2402 */     checkOpen();
/* 2403 */     setHoldability(holdability);
/* 2404 */     return prepareCall(sql, type, concurrency);
/*      */   }
/*      */ 
/*      */   public PreparedStatement prepareStatement(String sql) throws SQLException
/*      */   {
/* 2409 */     checkOpen();
/*      */ 
/* 2411 */     return prepareStatement(sql, 1003, 1007);
/*      */   }
/*      */ 
/*      */   public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
/*      */     throws SQLException
/*      */   {
/* 2418 */     checkOpen();
/*      */ 
/* 2420 */     if ((sql == null) || (sql.length() == 0)) {
/* 2421 */       throw new SQLException(Messages.get("error.generic.nosql"), "HY000");
/*      */     }
/*      */ 
/* 2424 */     if ((autoGeneratedKeys != 1) && (autoGeneratedKeys != 2))
/*      */     {
/* 2426 */       throw new SQLException(Messages.get("error.generic.badoption", Integer.toString(autoGeneratedKeys), "autoGeneratedKeys"), "HY092");
/*      */     }
/*      */ 
/* 2433 */     JtdsPreparedStatement stmt = new JtdsPreparedStatement(this, sql, 1003, 1007, autoGeneratedKeys == 1);
/*      */ 
/* 2438 */     addStatement(stmt);
/*      */ 
/* 2440 */     return stmt;
/*      */   }
/*      */ 
/*      */   public synchronized PreparedStatement prepareStatement(String sql, int type, int concurrency)
/*      */     throws SQLException
/*      */   {
/* 2447 */     checkOpen();
/*      */ 
/* 2449 */     if ((sql == null) || (sql.length() == 0)) {
/* 2450 */       throw new SQLException(Messages.get("error.generic.nosql"), "HY000");
/*      */     }
/*      */ 
/* 2453 */     JtdsPreparedStatement stmt = new JtdsPreparedStatement(this, sql, type, concurrency, false);
/*      */ 
/* 2458 */     addStatement(stmt);
/*      */ 
/* 2460 */     return stmt;
/*      */   }
/*      */ 
/*      */   public PreparedStatement prepareStatement(String sql, int type, int concurrency, int holdability)
/*      */     throws SQLException
/*      */   {
/* 2469 */     checkOpen();
/* 2470 */     setHoldability(holdability);
/*      */ 
/* 2472 */     return prepareStatement(sql, type, concurrency);
/*      */   }
/*      */ 
/*      */   public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
/*      */   {
/* 2477 */     if (columnIndexes == null) {
/* 2478 */       throw new SQLException(Messages.get("error.generic.nullparam", "prepareStatement"), "HY092");
/*      */     }
/* 2480 */     if (columnIndexes.length != 1) {
/* 2481 */       throw new SQLException(Messages.get("error.generic.needcolindex", "prepareStatement"), "HY092");
/*      */     }
/*      */ 
/* 2485 */     return prepareStatement(sql, 1);
/*      */   }
/*      */ 
/*      */   public Savepoint setSavepoint(String name) throws SQLException {
/* 2489 */     checkOpen();
/* 2490 */     notImplemented("Connection.setSavepoint(String)");
/*      */ 
/* 2492 */     return null;
/*      */   }
/*      */ 
/*      */   public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
/*      */   {
/* 2497 */     if (columnNames == null) {
/* 2498 */       throw new SQLException(Messages.get("error.generic.nullparam", "prepareStatement"), "HY092");
/*      */     }
/* 2500 */     if (columnNames.length != 1) {
/* 2501 */       throw new SQLException(Messages.get("error.generic.needcolname", "prepareStatement"), "HY092");
/*      */     }
/*      */ 
/* 2505 */     return prepareStatement(sql, 1);
/*      */   }
/*      */ 
/*      */   void clearSavepoints()
/*      */   {
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.ConnectionJDBC2
 * JD-Core Version:    0.5.3
 */