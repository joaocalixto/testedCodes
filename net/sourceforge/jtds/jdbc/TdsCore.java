/*      */ package net.sourceforge.jtds.jdbc;
/*      */ 
/*      */ import java.io.IOException;
/*      */ import java.net.InetAddress;
/*      */ import java.net.UnknownHostException;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.SQLWarning;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.HashMap;
/*      */ import java.util.Random;
/*      */ import net.sourceforge.jtds.util.Logger;
/*      */ import net.sourceforge.jtds.util.SSPIJNIClient;
/*      */ import net.sourceforge.jtds.util.TimerThread;
/*      */ import net.sourceforge.jtds.util.TimerThread.TimerListener;
/*      */ 
/*      */ public class TdsCore
/*      */ {
/*      */   public static final int MIN_PKT_SIZE = 512;
/*      */   public static final int DEFAULT_MIN_PKT_SIZE_TDS70 = 4096;
/*      */   public static final int MAX_PKT_SIZE = 32768;
/*      */   public static final int PKT_HDR_LEN = 8;
/*      */   public static final byte QUERY_PKT = 1;
/*      */   public static final byte LOGIN_PKT = 2;
/*      */   public static final byte RPC_PKT = 3;
/*      */   public static final byte REPLY_PKT = 4;
/*      */   public static final byte CANCEL_PKT = 6;
/*      */   public static final byte MSDTC_PKT = 14;
/*      */   public static final byte SYBQUERY_PKT = 15;
/*      */   public static final byte MSLOGIN_PKT = 16;
/*      */   public static final byte NTLMAUTH_PKT = 17;
/*      */   public static final byte PRELOGIN_PKT = 18;
/*      */   public static final int SSL_ENCRYPT_LOGIN = 0;
/*      */   public static final int SSL_CLIENT_FORCE_ENCRYPT = 1;
/*      */   public static final int SSL_NO_ENCRYPT = 2;
/*      */   public static final int SSL_SERVER_FORCE_ENCRYPT = 3;
/*      */   private static final byte TDS5_PARAMFMT2_TOKEN = 32;
/*      */   private static final byte TDS_LANG_TOKEN = 33;
/*      */   private static final byte TDS5_WIDE_RESULT = 97;
/*      */   private static final byte TDS_CLOSE_TOKEN = 113;
/*      */   private static final byte TDS_OFFSETS_TOKEN = 120;
/*      */   private static final byte TDS_RETURNSTATUS_TOKEN = 121;
/*      */   private static final byte TDS_PROCID = 124;
/*      */   private static final byte TDS7_RESULT_TOKEN = -127;
/*      */   private static final byte TDS7_COMP_RESULT_TOKEN = -120;
/*      */   private static final byte TDS_COLNAME_TOKEN = -96;
/*      */   private static final byte TDS_COLFMT_TOKEN = -95;
/*      */   private static final byte TDS_TABNAME_TOKEN = -92;
/*      */   private static final byte TDS_COLINFO_TOKEN = -91;
/*      */   private static final byte TDS_OPTIONCMD_TOKEN = -90;
/*      */   private static final byte TDS_COMP_NAMES_TOKEN = -89;
/*      */   private static final byte TDS_COMP_RESULT_TOKEN = -88;
/*      */   private static final byte TDS_ORDER_TOKEN = -87;
/*      */   private static final byte TDS_ERROR_TOKEN = -86;
/*      */   private static final byte TDS_INFO_TOKEN = -85;
/*      */   private static final byte TDS_PARAM_TOKEN = -84;
/*      */   private static final byte TDS_LOGINACK_TOKEN = -83;
/*      */   private static final byte TDS_CONTROL_TOKEN = -82;
/*      */   private static final byte TDS_ROW_TOKEN = -47;
/*      */   private static final byte TDS_ALTROW = -45;
/*      */   private static final byte TDS5_PARAMS_TOKEN = -41;
/*      */   private static final byte TDS_CAP_TOKEN = -30;
/*      */   private static final byte TDS_ENVCHANGE_TOKEN = -29;
/*      */   private static final byte TDS_MSG50_TOKEN = -27;
/*      */   private static final byte TDS_DBRPC_TOKEN = -26;
/*      */   private static final byte TDS5_DYNAMIC_TOKEN = -25;
/*      */   private static final byte TDS5_PARAMFMT_TOKEN = -20;
/*      */   private static final byte TDS_AUTH_TOKEN = -19;
/*      */   private static final byte TDS_RESULT_TOKEN = -18;
/*      */   private static final byte TDS_DONE_TOKEN = -3;
/*      */   private static final byte TDS_DONEPROC_TOKEN = -2;
/*      */   private static final byte TDS_DONEINPROC_TOKEN = -1;
/*      */   private static final byte TDS_ENV_DATABASE = 1;
/*      */   private static final byte TDS_ENV_LANG = 2;
/*      */   private static final byte TDS_ENV_CHARSET = 3;
/*      */   private static final byte TDS_ENV_PACKSIZE = 4;
/*      */   private static final byte TDS_ENV_LCID = 5;
/*      */   private static final byte TDS_ENV_SQLCOLLATION = 7;
/*  292 */   private static final ParamInfo[] EMPTY_PARAMETER_INFO = new ParamInfo[0];
/*      */   private static final byte DONE_MORE_RESULTS = 1;
/*      */   private static final byte DONE_ERROR = 2;
/*      */   private static final byte DONE_ROW_COUNT = 16;
/*      */   static final byte DONE_CANCEL = 32;
/*      */   private static final byte DONE_END_OF_RESPONSE = -128;
/*      */   public static final int UNPREPARED = 0;
/*      */   public static final int TEMPORARY_STORED_PROCEDURES = 1;
/*      */   public static final int EXECUTE_SQL = 2;
/*      */   public static final int PREPARE = 3;
/*      */   static final int SYB_LONGDATA = 1;
/*      */   static final int SYB_DATETIME = 2;
/*      */   static final int SYB_BITNULL = 4;
/*      */   static final int SYB_EXTCOLINFO = 8;
/*      */   static final int SYB_UNICODE = 16;
/*      */   static final int SYB_UNITEXT = 32;
/*      */   static final int SYB_BIGINT = 64;
/*      */   private static final int ASYNC_CANCEL = 0;
/*      */   private static final int TIMEOUT_CANCEL = 1;
/*  347 */   private static HashMap tds8SpNames = new HashMap();
/*      */   private static String hostName;
/*      */   private static SSPIJNIClient sspiJNIClient;
/*      */   private final ConnectionJDBC2 connection;
/*      */   private int tdsVersion;
/*      */   private final int serverType;
/*      */   private final SharedSocket socket;
/*      */   private final RequestStream out;
/*      */   private final ResponseStream in;
/*  390 */   private boolean endOfResponse = true;
/*      */ 
/*  392 */   private boolean endOfResults = true;
/*      */   private ColInfo[] columns;
/*      */   private Object[] rowData;
/*      */   private TableMetaData[] tables;
/*  400 */   private TdsToken currentToken = new TdsToken(null);
/*      */   private Integer returnStatus;
/*      */   private ParamInfo returnParam;
/*      */   private ParamInfo[] parameters;
/*  408 */   private int nextParam = -1;
/*      */   private final SQLDiagnostic messages;
/*      */   private boolean isClosed;
/*      */   private boolean ntlmAuthSSO;
/*      */   private boolean fatalError;
/*      */   private Semaphore connectionLock;
/*      */   private boolean inBatch;
/*  422 */   private int sslMode = 2;
/*      */   private boolean cancelPending;
/*  426 */   private final int[] cancelMonitor = new int[1];
/*      */ 
/*      */   TdsCore(ConnectionJDBC2 connection, SQLDiagnostic messages)
/*      */   {
/*  435 */     this.connection = connection;
/*  436 */     this.socket = connection.getSocket();
/*  437 */     this.messages = messages;
/*  438 */     this.serverType = connection.getServerType();
/*  439 */     this.tdsVersion = this.socket.getTdsVersion();
/*  440 */     this.out = this.socket.getRequestStream(connection.getNetPacketSize(), connection.getMaxPrecision());
/*  441 */     this.in = this.socket.getResponseStream(this.out, connection.getNetPacketSize());
/*      */   }
/*      */ 
/*      */   private void checkOpen()
/*      */     throws SQLException
/*      */   {
/*  451 */     if (this.connection.isClosed())
/*  452 */       throw new SQLException(Messages.get("error.generic.closed", "Connection"), "HY010");
/*      */   }
/*      */ 
/*      */   int getTdsVersion()
/*      */   {
/*  464 */     return this.tdsVersion;
/*      */   }
/*      */ 
/*      */   ColInfo[] getColumns()
/*      */   {
/*  473 */     return this.columns;
/*      */   }
/*      */ 
/*      */   void setColumns(ColInfo[] columns)
/*      */   {
/*  482 */     this.columns = columns;
/*  483 */     this.rowData = new Object[columns.length];
/*  484 */     this.tables = null;
/*      */   }
/*      */ 
/*      */   ParamInfo[] getParameters()
/*      */   {
/*  493 */     if (this.currentToken.dynamParamInfo != null) {
/*  494 */       ParamInfo[] params = new ParamInfo[this.currentToken.dynamParamInfo.length];
/*      */ 
/*  496 */       for (int i = 0; i < params.length; ++i) {
/*  497 */         ColInfo ci = this.currentToken.dynamParamInfo[i];
/*  498 */         params[i] = new ParamInfo(ci, ci.realName, null, 0);
/*      */       }
/*      */ 
/*  501 */       return params;
/*      */     }
/*      */ 
/*  504 */     return EMPTY_PARAMETER_INFO;
/*      */   }
/*      */ 
/*      */   Object[] getRowData()
/*      */   {
/*  513 */     return this.rowData;
/*      */   }
/*      */ 
/*      */   void negotiateSSL(String instance, String ssl)
/*      */     throws IOException, SQLException
/*      */   {
/*  532 */     if (!(ssl.equalsIgnoreCase("off"))) {
/*  533 */       if ((ssl.equalsIgnoreCase("require")) || (ssl.equalsIgnoreCase("authenticate")))
/*      */       {
/*  535 */         sendPreLoginPacket(instance, true);
/*  536 */         this.sslMode = readPreLoginPacket();
/*  537 */         if ((this.sslMode != 1) && (this.sslMode != 3))
/*      */         {
/*  539 */           throw new SQLException(Messages.get("error.ssl.encryptionoff"), "08S01");
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  544 */         sendPreLoginPacket(instance, false);
/*  545 */         this.sslMode = readPreLoginPacket();
/*      */       }
/*  547 */       if (this.sslMode != 2)
/*  548 */         this.socket.enableEncryption(ssl);
/*      */     }
/*      */   }
/*      */ 
/*      */   void login(String serverName, String database, String user, String password, String domain, String charset, String appName, String progName, String wsid, String language, String macAddress, int packetSize)
/*      */     throws SQLException
/*      */   {
/*      */     try
/*      */     {
/*  584 */       if (wsid.length() == 0) {
/*  585 */         wsid = getHostName();
/*      */       }
/*  587 */       if (this.tdsVersion >= 3) {
/*  588 */         sendMSLoginPkt(serverName, database, user, password, domain, appName, progName, wsid, language, macAddress, packetSize);
/*      */       }
/*  591 */       else if (this.tdsVersion == 2) {
/*  592 */         send50LoginPkt(serverName, user, password, charset, appName, progName, wsid, language, packetSize);
/*      */       }
/*      */       else
/*      */       {
/*  596 */         send42LoginPkt(serverName, user, password, charset, appName, progName, wsid, language, packetSize);
/*      */       }
/*      */ 
/*  600 */       if (this.sslMode == 0) {
/*  601 */         this.socket.disableEncryption();
/*      */       }
/*  603 */       nextToken();
/*      */ 
/*  605 */       while (!(this.endOfResponse)) {
/*  606 */         if (this.currentToken.isAuthToken()) {
/*  607 */           sendNtlmChallengeResponse(this.currentToken.nonce, user, password, domain);
/*      */         }
/*      */ 
/*  610 */         nextToken();
/*      */       }
/*      */ 
/*  613 */       this.messages.checkErrors();
/*      */     } catch (IOException ioe) {
/*  615 */       throw Support.linkException(new SQLException(Messages.get("error.generic.ioerror", ioe.getMessage()), "08S01"), ioe);
/*      */     }
/*      */   }
/*      */ 
/*      */   boolean getMoreResults()
/*      */     throws SQLException
/*      */   {
/*  631 */     checkOpen();
/*  632 */     nextToken();
/*      */ 
/*  636 */     while ((!(this.endOfResponse)) && (!(this.currentToken.isUpdateCount())) && (!(this.currentToken.isResultSet()))) {
/*  637 */       nextToken();
/*      */     }
/*      */ 
/*  646 */     if (this.currentToken.isResultSet()) {
/*  647 */       byte saveToken = this.currentToken.token;
/*      */       try {
/*  649 */         byte x = (byte)this.in.peek();
/*      */ 
/*  653 */         while ((x == -92) || (x == -91) || (x == -82)) {
/*  654 */           nextToken();
/*  655 */           x = (byte)this.in.peek();
/*      */         }
/*      */       } catch (IOException e) {
/*  658 */         this.connection.setClosed();
/*      */ 
/*  660 */         throw Support.linkException(new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "08S01"), e);
/*      */       }
/*      */ 
/*  666 */       this.currentToken.token = saveToken;
/*      */     }
/*      */ 
/*  669 */     return this.currentToken.isResultSet();
/*      */   }
/*      */ 
/*      */   boolean isResultSet()
/*      */   {
/*  678 */     return this.currentToken.isResultSet();
/*      */   }
/*      */ 
/*      */   boolean isRowData()
/*      */   {
/*  687 */     return this.currentToken.isRowData();
/*      */   }
/*      */ 
/*      */   boolean isUpdateCount()
/*      */   {
/*  696 */     return this.currentToken.isUpdateCount();
/*      */   }
/*      */ 
/*      */   int getUpdateCount()
/*      */   {
/*  705 */     if (this.currentToken.isEndToken()) {
/*  706 */       return this.currentToken.updateCount;
/*      */     }
/*      */ 
/*  709 */     return -1;
/*      */   }
/*      */ 
/*      */   boolean isEndOfResponse()
/*      */   {
/*  718 */     return this.endOfResponse;
/*      */   }
/*      */ 
/*      */   void clearResponseQueue()
/*      */     throws SQLException
/*      */   {
/*  727 */     checkOpen();
/*  728 */     while (!(this.endOfResponse))
/*  729 */       nextToken();
/*      */   }
/*      */ 
/*      */   void consumeOneResponse()
/*      */     throws SQLException
/*      */   {
/*  741 */     checkOpen();
/*  742 */     while (!(this.endOfResponse)) {
/*  743 */       nextToken();
/*      */ 
/*  745 */       if ((this.currentToken.isEndToken()) && ((this.currentToken.status & 0xFFFFFF80) != 0))
/*      */       {
/*  747 */         return;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   boolean getNextRow()
/*      */     throws SQLException
/*      */   {
/*  761 */     if ((this.endOfResponse) || (this.endOfResults)) {
/*  762 */       return false;
/*      */     }
/*  764 */     checkOpen();
/*  765 */     nextToken();
/*      */ 
/*  768 */     while ((!(this.currentToken.isRowData())) && (!(this.currentToken.isEndToken()))) {
/*  769 */       nextToken();
/*      */     }
/*      */ 
/*  772 */     return this.currentToken.isRowData();
/*      */   }
/*      */ 
/*      */   boolean isDataInResultSet() throws SQLException
/*      */   {
/*  787 */     checkOpen();
/*      */     byte x;
/*      */     try
/*      */     {
/*  790 */       x = (this.endOfResponse) ? -3 : (byte)this.in.peek();
/*      */ 
/*  795 */       while ((x != -47) && (x != -3) && (x != -1) && (x != -2)) {
/*  796 */         nextToken();
/*  797 */         x = (byte)this.in.peek();
/*      */       }
/*      */ 
/*  800 */       this.messages.checkErrors();
/*      */     } catch (IOException e) {
/*  802 */       this.connection.setClosed();
/*  803 */       throw Support.linkException(new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "08S01"), e);
/*      */     }
/*      */ 
/*  810 */     return (x == -47);
/*      */   }
/*      */ 
/*      */   Integer getReturnStatus()
/*      */   {
/*  819 */     return this.returnStatus;
/*      */   }
/*      */ 
/*      */   synchronized void closeConnection()
/*      */   {
/*      */     try
/*      */     {
/*  829 */       if (this.tdsVersion == 2) {
/*  830 */         this.socket.setTimeout(1000);
/*  831 */         this.out.setPacketType(15);
/*  832 */         this.out.write(113);
/*  833 */         this.out.write(0);
/*  834 */         this.out.flush();
/*  835 */         this.endOfResponse = false;
/*  836 */         clearResponseQueue();
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */   }
/*      */ 
/*      */   void close()
/*      */     throws SQLException
/*      */   {
/*  848 */     if (this.isClosed) return;
/*      */     try {
/*  850 */       clearResponseQueue();
/*  851 */       this.out.close();
/*  852 */       this.in.close();
/*      */     } finally {
/*  854 */       this.isClosed = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   void cancel(boolean timeout)
/*      */   {
/*  865 */     Semaphore mutex = null;
/*      */     try {
/*  867 */       mutex = this.connection.getMutex();
/*  868 */       synchronized (this.cancelMonitor) {
/*  869 */         if ((!(this.cancelPending)) && (!(this.endOfResponse))) {
/*  870 */           this.cancelPending = this.socket.cancel(this.out.getStreamId());
/*      */         }
/*      */ 
/*  873 */         if (this.cancelPending) {
/*  874 */           this.cancelMonitor[0] = ((timeout) ? 1 : 0);
/*  875 */           this.endOfResponse = false;
/*      */         }
/*      */       }
/*      */     } finally {
/*  879 */       if (mutex != null)
/*  880 */         mutex.release();
/*      */     }
/*      */   }
/*      */ 
/*      */   void submitSQL(String sql)
/*      */     throws SQLException
/*      */   {
/*  892 */     checkOpen();
/*  893 */     this.messages.clearWarnings();
/*      */ 
/*  895 */     if (sql.length() == 0) {
/*  896 */       throw new IllegalArgumentException("submitSQL() called with empty SQL String");
/*      */     }
/*      */ 
/*  899 */     executeSQL(sql, null, null, false, 0, -1, -1, true);
/*  900 */     clearResponseQueue();
/*  901 */     this.messages.checkErrors();
/*      */   }
/*      */ 
/*      */   void startBatch()
/*      */   {
/*  913 */     this.inBatch = true;
/*      */   }
/*      */ 
/*      */   synchronized void executeSQL(String sql, String procName, ParamInfo[] parameters, boolean noMetaData, int timeOut, int maxRows, int maxFieldSize, boolean sendNow)
/*      */     throws SQLException
/*      */   {
/*  940 */     boolean sendFailed = true;
/*      */     try
/*      */     {
/*  947 */       if (this.connectionLock == null) {
/*  948 */         this.connectionLock = this.connection.getMutex();
/*      */       }
/*      */ 
/*  951 */       clearResponseQueue();
/*  952 */       this.messages.exceptions = null;
/*      */ 
/*  961 */       setRowCountAndTextSize(maxRows, maxFieldSize);
/*      */ 
/*  963 */       this.messages.clearWarnings();
/*  964 */       this.returnStatus = null;
/*      */ 
/*  968 */       if ((parameters != null) && (parameters.length == 0)) {
/*  969 */         parameters = null;
/*      */       }
/*  971 */       this.parameters = parameters;
/*      */ 
/*  975 */       if ((procName != null) && (procName.length() == 0)) {
/*  976 */         procName = null;
/*      */       }
/*      */ 
/*  979 */       if ((parameters != null) && (parameters[0].isRetVal)) {
/*  980 */         this.returnParam = parameters[0];
/*  981 */         this.nextParam = 0;
/*      */       } else {
/*  983 */         this.returnParam = null;
/*  984 */         this.nextParam = -1;
/*      */       }
/*      */ 
/*  987 */       if (parameters != null)
/*      */       {
/*      */         int i;
/*  988 */         if ((procName == null) && (sql.startsWith("EXECUTE ")))
/*      */         {
/*  994 */           for (i = 0; i < parameters.length; ++i)
/*      */           {
/*  996 */             if ((!(parameters[i].isRetVal)) && (parameters[i].isOutput)) {
/*  997 */               throw new SQLException(Messages.get("error.prepare.nooutparam", Integer.toString(i + 1)), "07000");
/*      */             }
/*      */           }
/*      */ 
/* 1001 */           sql = Support.substituteParameters(sql, parameters, this.connection);
/* 1002 */           parameters = null;
/*      */         }
/*      */         else
/*      */         {
/* 1007 */           for (i = 0; i < parameters.length; ++i) {
/* 1008 */             if ((!(parameters[i].isSet)) && (!(parameters[i].isOutput))) {
/* 1009 */               throw new SQLException(Messages.get("error.prepare.paramnotset", Integer.toString(i + 1)), "07000");
/*      */             }
/*      */ 
/* 1012 */             parameters[i].clearOutValue();
/*      */ 
/* 1017 */             TdsData.getNativeType(this.connection, parameters[i]);
/*      */           }
/*      */         }
/*      */       }
/*      */       try
/*      */       {
/* 1023 */         switch (this.tdsVersion)
/*      */         {
/*      */         case 1:
/* 1025 */           executeSQL42(sql, procName, parameters, noMetaData, sendNow);
/* 1026 */           break;
/*      */         case 2:
/* 1028 */           executeSQL50(sql, procName, parameters);
/* 1029 */           break;
/*      */         case 3:
/*      */         case 4:
/*      */         case 5:
/* 1033 */           executeSQL70(sql, procName, parameters, noMetaData, sendNow);
/* 1034 */           break;
/*      */         default:
/* 1036 */           throw new IllegalStateException("Unknown TDS version " + this.tdsVersion);
/*      */         }
/*      */ 
/* 1039 */         if (sendNow) {
/* 1040 */           this.out.flush();
/* 1041 */           this.connectionLock.release();
/* 1042 */           this.connectionLock = null;
/* 1043 */           sendFailed = false;
/* 1044 */           this.endOfResponse = false;
/* 1045 */           this.endOfResults = true;
/* 1046 */           wait(timeOut);
/*      */         } else {
/* 1048 */           sendFailed = false;
/*      */         }
/*      */       } catch (IOException ioe) {
/* 1051 */         this.connection.setClosed();
/*      */ 
/* 1053 */         throw Support.linkException(new SQLException(Messages.get("error.generic.ioerror", ioe.getMessage()), "08S01"), ioe);
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/* 1060 */       if ((((sendNow) || (sendFailed))) && (this.connectionLock != null)) {
/* 1061 */         this.connectionLock.release();
/* 1062 */         this.connectionLock = null;
/*      */       }
/*      */ 
/* 1065 */       if (sendNow)
/* 1066 */         this.inBatch = false;
/*      */     }
/*      */   }
/*      */ 
/*      */   String microsoftPrepare(String sql, ParamInfo[] params, boolean needCursor, int resultSetType, int resultSetConcurrency)
/*      */     throws SQLException
/*      */   {
/* 1091 */     checkOpen();
/* 1092 */     this.messages.clearWarnings();
/*      */ 
/* 1094 */     int prepareSql = this.connection.getPrepareSql();
/*      */     StringBuffer spSql;
/*      */     String procName;
/* 1096 */     if (prepareSql == 1) {
/* 1097 */       spSql = new StringBuffer(sql.length() + 32 + params.length * 15);
/* 1098 */       procName = this.connection.getProcName();
/*      */ 
/* 1100 */       spSql.append("create proc ");
/* 1101 */       spSql.append(procName);
/* 1102 */       spSql.append(' ');
/*      */ 
/* 1104 */       for (int i = 0; i < params.length; ++i) {
/* 1105 */         spSql.append("@P");
/* 1106 */         spSql.append(i);
/* 1107 */         spSql.append(' ');
/* 1108 */         spSql.append(params[i].sqlType);
/*      */ 
/* 1110 */         if (i + 1 < params.length) {
/* 1111 */           spSql.append(',');
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1116 */       spSql.append(" as ");
/* 1117 */       spSql.append(Support.substituteParamMarkers(sql, params));
/*      */     }
/*      */     try {
/* 1120 */       submitSQL(spSql.toString());
/* 1121 */       return procName;
/*      */     } catch (SQLException prepParam) {
/* 1123 */       if ("08S01".equals(e.getSQLState()))
/*      */       {
/* 1125 */         throw e;
/*      */       }
/*      */ 
/* 1130 */       this.messages.addWarning(Support.linkException(new SQLWarning(Messages.get("error.prepare.prepfailed", e.getMessage()), e.getSQLState(), e.getErrorCode()), e));
/*      */ 
/* 1136 */       break label554:
/*      */ 
/* 1138 */       if (prepareSql == 3)
/*      */       {
/* 1141 */         ParamInfo[] prepParam = new ParamInfo[(needCursor) ? 6 : 4];
/*      */ 
/* 1144 */         prepParam[0] = new ParamInfo(4, null, 1);
/*      */ 
/* 1147 */         prepParam[1] = new ParamInfo(-1, Support.getParameterDefinitions(params), 4);
/*      */ 
/* 1152 */         prepParam[2] = new ParamInfo(-1, Support.substituteParamMarkers(sql, params), 4);
/*      */ 
/* 1157 */         prepParam[3] = new ParamInfo(4, new Integer(1), 0);
/*      */ 
/* 1159 */         if (needCursor)
/*      */         {
/* 1162 */           int scrollOpt = MSCursorResultSet.getCursorScrollOpt(resultSetType, resultSetConcurrency, true);
/*      */ 
/* 1164 */           int ccOpt = MSCursorResultSet.getCursorConcurrencyOpt(resultSetConcurrency);
/*      */ 
/* 1167 */           prepParam[4] = new ParamInfo(4, new Integer(scrollOpt), 1);
/*      */ 
/* 1172 */           prepParam[5] = new ParamInfo(4, new Integer(ccOpt), 1);
/*      */         }
/*      */ 
/* 1177 */         this.columns = null;
/*      */         try {
/* 1179 */           executeSQL(null, (needCursor) ? "sp_cursorprepare" : "sp_prepare", prepParam, false, 0, -1, -1, true);
/*      */ 
/* 1182 */           int resultCount = 0;
/* 1183 */           while (!(this.endOfResponse)) {
/* 1184 */             nextToken();
/* 1185 */             if (isResultSet());
/* 1186 */             ++resultCount;
/*      */           }
/*      */ 
/* 1190 */           if (resultCount != 1)
/*      */           {
/* 1193 */             this.columns = null;
/*      */           }
/* 1195 */           Integer prepareHandle = (Integer)prepParam[0].getOutValue();
/* 1196 */           if (prepareHandle != null) {
/* 1197 */             return prepareHandle.toString();
/*      */           }
/*      */ 
/* 1200 */           this.messages.checkErrors();
/*      */         } catch (SQLException e) {
/* 1202 */           if ("08S01".equals(e.getSQLState()))
/*      */           {
/* 1204 */             throw e;
/*      */           }
/*      */ 
/* 1208 */           this.messages.addWarning(Support.linkException(new SQLWarning(Messages.get("error.prepare.prepfailed", e.getMessage()), e.getSQLState(), e.getErrorCode()), e));
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1217 */     label554: return null;
/*      */   }
/*      */ 
/*      */   synchronized String sybasePrepare(String sql, ParamInfo[] params)
/*      */     throws SQLException
/*      */   {
/* 1230 */     checkOpen();
/* 1231 */     this.messages.clearWarnings();
/* 1232 */     if ((sql == null) || (sql.length() == 0)) {
/* 1233 */       throw new IllegalArgumentException("sql parameter must be at least 1 character long.");
/*      */     }
/*      */ 
/* 1237 */     String procName = this.connection.getProcName();
/*      */ 
/* 1239 */     if ((procName == null) || (procName.length() != 11)) {
/* 1240 */       throw new IllegalArgumentException("procName parameter must be 11 characters long.");
/*      */     }
/*      */ 
/* 1246 */     for (int i = 0; i < params.length; ++i) {
/* 1247 */       if (("text".equals(params[i].sqlType)) || ("unitext".equals(params[i].sqlType)) || ("image".equals(params[i].sqlType)))
/*      */       {
/* 1250 */         return null;
/*      */       }
/*      */     }
/*      */ 
/* 1254 */     Semaphore mutex = null;
/*      */     String str1;
/*      */     try {
/* 1257 */       mutex = this.connection.getMutex();
/*      */ 
/* 1259 */       this.out.setPacketType(15);
/* 1260 */       this.out.write(-25);
/*      */ 
/* 1262 */       byte[] buf = Support.encodeString(this.connection.getCharset(), sql);
/*      */ 
/* 1264 */       this.out.write((short)(buf.length + 41));
/* 1265 */       this.out.write(1);
/* 1266 */       this.out.write(0);
/* 1267 */       this.out.write(10);
/* 1268 */       this.out.writeAscii(procName.substring(1));
/* 1269 */       this.out.write((short)(buf.length + 26));
/* 1270 */       this.out.writeAscii("create proc ");
/* 1271 */       this.out.writeAscii(procName.substring(1));
/* 1272 */       this.out.writeAscii(" as ");
/* 1273 */       this.out.write(buf);
/* 1274 */       this.out.flush();
/* 1275 */       this.endOfResponse = false;
/* 1276 */       clearResponseQueue();
/* 1277 */       this.messages.checkErrors();
/* 1278 */       str1 = procName;
/*      */ 
/* 1297 */       return str1;
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/* 1287 */       if ("08S01".equals(e.getSQLState()))
/*      */       {
/* 1289 */         throw e;
/*      */       }
/*      */ 
/* 1294 */       str1 = null;
/*      */ 
/* 1297 */       return str1;
/*      */     }
/*      */     finally
/*      */     {
/* 1296 */       if (mutex != null)
/* 1297 */         mutex.release();
/*      */     }
/*      */   }
/*      */ 
/*      */   synchronized void sybaseUnPrepare(String procName)
/*      */     throws SQLException
/*      */   {
/* 1310 */     checkOpen();
/* 1311 */     this.messages.clearWarnings();
/*      */ 
/* 1313 */     if ((procName == null) || (procName.length() != 11)) {
/* 1314 */       throw new IllegalArgumentException("procName parameter must be 11 characters long.");
/*      */     }
/*      */ 
/* 1318 */     Semaphore mutex = null;
/*      */     try {
/* 1320 */       mutex = this.connection.getMutex();
/*      */ 
/* 1322 */       this.out.setPacketType(15);
/* 1323 */       this.out.write(-25);
/* 1324 */       this.out.write(15);
/* 1325 */       this.out.write(4);
/* 1326 */       this.out.write(0);
/* 1327 */       this.out.write(10);
/* 1328 */       this.out.writeAscii(procName.substring(1));
/* 1329 */       this.out.write(0);
/* 1330 */       this.out.flush();
/* 1331 */       this.endOfResponse = false;
/* 1332 */       clearResponseQueue();
/* 1333 */       this.messages.checkErrors();
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/* 1342 */       if ("08S01".equals(e.getSQLState()))
/*      */       {
/* 1344 */         throw e;
/*      */       }
/*      */     }
/*      */     finally {
/* 1348 */       if (mutex != null)
/* 1349 */         mutex.release();
/*      */     }
/*      */   }
/*      */ 
/*      */   synchronized byte[] enlistConnection(int type, byte[] oleTranID)
/*      */     throws SQLException
/*      */   {
/* 1364 */     Semaphore mutex = null;
/*      */     try {
/* 1366 */       mutex = this.connection.getMutex();
/*      */ 
/* 1368 */       this.out.setPacketType(14);
/* 1369 */       this.out.write((short)type);
/* 1370 */       switch (type)
/*      */       {
/*      */       case 0:
/* 1372 */         this.out.write(0);
/* 1373 */         break;
/*      */       case 1:
/* 1375 */         if (oleTranID != null) {
/* 1376 */           this.out.write((short)oleTranID.length);
/* 1377 */           this.out.write(oleTranID);
/*      */         }
/*      */         else {
/* 1380 */           this.out.write(0);
/*      */         }
/*      */       }
/*      */ 
/* 1384 */       this.out.flush();
/* 1385 */       this.endOfResponse = false;
/* 1386 */       this.endOfResults = true;
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/* 1396 */       if (mutex != null) {
/* 1397 */         mutex.release();
/*      */       }
/*      */     }
/*      */ 
/* 1401 */     byte[] tmAddress = null;
/* 1402 */     if ((getMoreResults()) && (getNextRow()) && 
/* 1403 */       (this.rowData.length == 1)) {
/* 1404 */       Object x = this.rowData[0];
/* 1405 */       if (x instanceof byte[]) {
/* 1406 */         tmAddress = (byte[])x;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1411 */     clearResponseQueue();
/* 1412 */     this.messages.checkErrors();
/* 1413 */     return tmAddress;
/*      */   }
/*      */ 
/*      */   SQLException getBatchCounts(ArrayList counts, SQLException sqlEx)
/*      */     throws SQLException
/*      */   {
/* 1434 */     Integer lastCount = JtdsStatement.SUCCESS_NO_INFO;
/*      */     try
/*      */     {
/* 1437 */       checkOpen();
/* 1438 */       while (!(this.endOfResponse)) {
/* 1439 */         nextToken();
/* 1440 */         if (this.currentToken.isResultSet())
/*      */         {
/* 1442 */           throw new SQLException(Messages.get("error.statement.batchnocount"), "07000");
/*      */         }
/*      */ 
/* 1450 */         switch (this.currentToken.token)
/*      */         {
/*      */         case -3:
/* 1452 */           if (((this.currentToken.status & 0x2) != 0) || (lastCount == JtdsStatement.EXECUTE_FAILED))
/*      */           {
/* 1454 */             counts.add(JtdsStatement.EXECUTE_FAILED);
/*      */           }
/* 1456 */           else if (this.currentToken.isUpdateCount())
/* 1457 */             counts.add(new Integer(this.currentToken.updateCount));
/*      */           else {
/* 1459 */             counts.add(lastCount);
/*      */           }
/*      */ 
/* 1462 */           lastCount = JtdsStatement.SUCCESS_NO_INFO;
/* 1463 */           break;
/*      */         case -1:
/* 1465 */           if ((this.currentToken.status & 0x2) != 0)
/* 1466 */             lastCount = JtdsStatement.EXECUTE_FAILED;
/* 1467 */           else if (this.currentToken.isUpdateCount())
/* 1468 */             lastCount = new Integer(this.currentToken.updateCount); break;
/*      */         case -2:
/* 1472 */           if (((this.currentToken.status & 0x2) != 0) || (lastCount == JtdsStatement.EXECUTE_FAILED))
/*      */           {
/* 1474 */             counts.add(JtdsStatement.EXECUTE_FAILED);
/*      */           }
/*      */           else counts.add(lastCount);
/*      */ 
/* 1478 */           lastCount = JtdsStatement.SUCCESS_NO_INFO;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1485 */       this.messages.checkErrors();
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/* 1491 */       if (sqlEx != null)
/* 1492 */         sqlEx.setNextException(e);
/*      */       else
/* 1494 */         sqlEx = e;
/*      */     }
/*      */     finally {
/* 1497 */       while (!(this.endOfResponse)) {
/*      */         try
/*      */         {
/* 1500 */           nextToken();
/*      */         } catch (SQLException ex) {
/* 1502 */           checkOpen();
/*      */ 
/* 1504 */           if (sqlEx != null)
/* 1505 */             sqlEx.setNextException(ex);
/*      */           else {
/* 1507 */             sqlEx = ex;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1513 */     return sqlEx;
/*      */   }
/*      */ 
/*      */   private void putLoginString(String txt, int len)
/*      */     throws IOException
/*      */   {
/* 1524 */     byte[] tmp = Support.encodeString(this.connection.getCharset(), txt);
/* 1525 */     this.out.write(tmp, 0, len);
/* 1526 */     this.out.write((byte)((tmp.length < len) ? tmp.length : len));
/*      */   }
/*      */ 
/*      */   private void sendPreLoginPacket(String instance, boolean forceEncryption)
/*      */     throws IOException
/*      */   {
/* 1539 */     this.out.setPacketType(18);
/*      */ 
/* 1541 */     this.out.write(0);
/* 1542 */     this.out.write(21);
/* 1543 */     this.out.write(6);
/*      */ 
/* 1545 */     this.out.write(1);
/* 1546 */     this.out.write(27);
/* 1547 */     this.out.write(1);
/*      */ 
/* 1549 */     this.out.write(2);
/* 1550 */     this.out.write(28);
/* 1551 */     this.out.write((byte)(instance.length() + 1));
/*      */ 
/* 1553 */     this.out.write(3);
/* 1554 */     this.out.write((short)(28 + instance.length() + 1));
/* 1555 */     this.out.write(4);
/*      */ 
/* 1557 */     this.out.write(-1);
/*      */ 
/* 1559 */     this.out.write(new byte[] { 8, 0, 1, 85, 0, 0 });
/*      */ 
/* 1561 */     this.out.write((byte)((forceEncryption) ? 1 : 0));
/*      */ 
/* 1563 */     this.out.writeAscii(instance);
/* 1564 */     this.out.write(0);
/*      */ 
/* 1566 */     this.out.write(new byte[] { 1, 2, 0, 0 });
/*      */ 
/* 1568 */     this.out.flush();
/*      */   }
/*      */ 
/*      */   private int readPreLoginPacket()
/*      */     throws IOException
/*      */   {
/* 1586 */     byte[][] list = new byte[8][];
/* 1587 */     byte[][] data = new byte[8][];
/* 1588 */     int recordCount = 0;
/*      */ 
/* 1590 */     byte[] record = new byte[5];
/*      */ 
/* 1592 */     record[0] = (byte)this.in.read();
/* 1593 */     while ((record[0] & 0xFF) != 255) {
/* 1594 */       if (recordCount == list.length) {
/* 1595 */         throw new IOException("Pre Login packet has more than 8 entries");
/*      */       }
/*      */ 
/* 1598 */       this.in.read(record, 1, 4);
/* 1599 */       list[(recordCount++)] = record;
/* 1600 */       record = new byte[5];
/* 1601 */       record[0] = (byte)this.in.read();
/*      */     }
/*      */ 
/* 1604 */     for (int i = 0; i < recordCount; ++i) {
/* 1605 */       byte[] value = new byte[list[i][4]];
/* 1606 */       this.in.read(value);
/* 1607 */       data[i] = value;
/*      */     }
/* 1609 */     if (Logger.isActive())
/*      */     {
/* 1611 */       Logger.println("PreLogin server response");
/* 1612 */       for (i = 0; i < recordCount; ++i) {
/* 1613 */         Logger.println("Record " + i + " = " + Support.toHex(data[i]));
/*      */       }
/*      */     }
/*      */ 
/* 1617 */     if (recordCount > 1) {
/* 1618 */       return data[1][0];
/*      */     }
/*      */ 
/* 1621 */     return 2;
/*      */   }
/*      */ 
/*      */   private void send42LoginPkt(String serverName, String user, String password, String charset, String appName, String progName, String wsid, String language, int packetSize)
/*      */     throws IOException
/*      */   {
/* 1649 */     byte[] empty = new byte[0];
/*      */ 
/* 1651 */     this.out.setPacketType(2);
/* 1652 */     putLoginString(wsid, 30);
/* 1653 */     putLoginString(user, 30);
/* 1654 */     putLoginString(password, 30);
/* 1655 */     putLoginString(String.valueOf(this.connection.getProcessId()), 30);
/*      */ 
/* 1657 */     this.out.write(3);
/* 1658 */     this.out.write(1);
/* 1659 */     this.out.write(6);
/* 1660 */     this.out.write(10);
/* 1661 */     this.out.write(9);
/* 1662 */     this.out.write(1);
/* 1663 */     this.out.write(1);
/* 1664 */     this.out.write(0);
/* 1665 */     this.out.write(0);
/*      */ 
/* 1667 */     this.out.write(empty, 0, 7);
/*      */ 
/* 1669 */     putLoginString(appName, 30);
/* 1670 */     putLoginString(serverName, 30);
/*      */ 
/* 1672 */     this.out.write(0);
/* 1673 */     this.out.write((byte)password.length());
/* 1674 */     byte[] tmp = Support.encodeString(this.connection.getCharset(), password);
/* 1675 */     this.out.write(tmp, 0, 253);
/* 1676 */     this.out.write((byte)(tmp.length + 2));
/*      */ 
/* 1678 */     this.out.write(4);
/* 1679 */     this.out.write(2);
/*      */ 
/* 1681 */     this.out.write(0);
/* 1682 */     this.out.write(0);
/* 1683 */     putLoginString(progName, 10);
/*      */ 
/* 1685 */     this.out.write(6);
/* 1686 */     this.out.write(0);
/* 1687 */     this.out.write(0);
/* 1688 */     this.out.write(0);
/*      */ 
/* 1690 */     this.out.write(0);
/* 1691 */     this.out.write(13);
/* 1692 */     this.out.write(17);
/*      */ 
/* 1694 */     putLoginString(language, 30);
/*      */ 
/* 1696 */     this.out.write(1);
/* 1697 */     this.out.write(0);
/* 1698 */     this.out.write(0);
/* 1699 */     this.out.write(empty, 0, 8);
/* 1700 */     this.out.write(0);
/*      */ 
/* 1702 */     putLoginString(charset, 30);
/*      */ 
/* 1704 */     this.out.write(1);
/* 1705 */     putLoginString(String.valueOf(packetSize), 6);
/*      */ 
/* 1707 */     this.out.write(empty, 0, 8);
/*      */ 
/* 1709 */     this.out.flush();
/* 1710 */     this.endOfResponse = false;
/*      */   }
/*      */ 
/*      */   private void send50LoginPkt(String serverName, String user, String password, String charset, String appName, String progName, String wsid, String language, int packetSize)
/*      */     throws IOException
/*      */   {
/* 1737 */     byte[] empty = new byte[0];
/*      */ 
/* 1739 */     this.out.setPacketType(2);
/* 1740 */     putLoginString(wsid, 30);
/* 1741 */     putLoginString(user, 30);
/* 1742 */     putLoginString(password, 30);
/* 1743 */     putLoginString(String.valueOf(this.connection.getProcessId()), 30);
/*      */ 
/* 1745 */     this.out.write(3);
/* 1746 */     this.out.write(1);
/* 1747 */     this.out.write(6);
/* 1748 */     this.out.write(10);
/* 1749 */     this.out.write(9);
/* 1750 */     this.out.write(1);
/* 1751 */     this.out.write(1);
/* 1752 */     this.out.write(0);
/* 1753 */     this.out.write(0);
/*      */ 
/* 1755 */     this.out.write(empty, 0, 7);
/*      */ 
/* 1757 */     putLoginString(appName, 30);
/* 1758 */     putLoginString(serverName, 30);
/* 1759 */     this.out.write(0);
/* 1760 */     this.out.write((byte)password.length());
/* 1761 */     byte[] tmp = Support.encodeString(this.connection.getCharset(), password);
/* 1762 */     this.out.write(tmp, 0, 253);
/* 1763 */     this.out.write((byte)(tmp.length + 2));
/*      */ 
/* 1765 */     this.out.write(5);
/* 1766 */     this.out.write(0);
/*      */ 
/* 1768 */     this.out.write(0);
/* 1769 */     this.out.write(0);
/* 1770 */     putLoginString(progName, 10);
/*      */ 
/* 1772 */     this.out.write(5);
/* 1773 */     this.out.write(0);
/* 1774 */     this.out.write(0);
/* 1775 */     this.out.write(0);
/*      */ 
/* 1777 */     this.out.write(0);
/* 1778 */     this.out.write(13);
/* 1779 */     this.out.write(17);
/*      */ 
/* 1781 */     putLoginString(language, 30);
/*      */ 
/* 1783 */     this.out.write(1);
/* 1784 */     this.out.write(0);
/* 1785 */     this.out.write(0);
/* 1786 */     this.out.write(empty, 0, 8);
/* 1787 */     this.out.write(0);
/*      */ 
/* 1789 */     putLoginString(charset, 30);
/*      */ 
/* 1791 */     this.out.write(1);
/* 1792 */     if (packetSize > 0)
/* 1793 */       putLoginString(String.valueOf(packetSize), 6);
/*      */     else {
/* 1795 */       putLoginString(String.valueOf(512), 6);
/*      */     }
/* 1797 */     this.out.write(empty, 0, 4);
/*      */ 
/* 1813 */     byte[] capString = { 1, 11, 79, -1, -123, -18, -17, 101, 127, -1, -1, -1, -42, 2, 10, 0, 2, 4, 6, -128, 6, 72, 0, 0, 12 };
/*      */ 
/* 1822 */     if (packetSize == 0)
/*      */     {
/* 1824 */       capString[17] = 0;
/*      */     }
/* 1826 */     this.out.write(-30);
/* 1827 */     this.out.write((short)capString.length);
/* 1828 */     this.out.write(capString);
/*      */ 
/* 1830 */     this.out.flush();
/* 1831 */     this.endOfResponse = false;
/*      */   }
/*      */ 
/*      */   private void sendMSLoginPkt(String serverName, String database, String user, String password, String domain, String appName, String progName, String wsid, String language, String macAddress, int netPacketSize)
/*      */     throws IOException, SQLException
/*      */   {
/* 1867 */     byte[] empty = new byte[0];
/* 1868 */     boolean ntlmAuth = false;
/* 1869 */     byte[] ntlmMessage = null;
/*      */ 
/* 1871 */     if ((user == null) || (user.length() == 0))
/*      */     {
/* 1874 */       if (Support.isWindowsOS()) {
/* 1875 */         this.ntlmAuthSSO = true;
/* 1876 */         ntlmAuth = true; break label70:
/*      */       }
/* 1878 */       throw new SQLException(Messages.get("error.connection.sso"), "08001");
/*      */     }
/*      */ 
/* 1881 */     if ((domain != null) && (domain.length() > 0))
/*      */     {
/* 1884 */       ntlmAuth = true;
/*      */     }
/*      */ 
/* 1887 */     if (this.ntlmAuthSSO) {
/*      */       try
/*      */       {
/* 1890 */         label70: sspiJNIClient = SSPIJNIClient.getInstance();
/* 1891 */         ntlmMessage = sspiJNIClient.invokePrepareSSORequest();
/*      */       } catch (Exception e) {
/* 1893 */         throw new IOException("SSO Failed: " + e.getMessage());
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1898 */     short packSize = (short)(86 + 2 * (wsid.length() + appName.length() + serverName.length() + progName.length() + database.length() + language.length()));
/*      */     short authLen;
/* 1907 */     if (ntlmAuth) {
/* 1908 */       if ((this.ntlmAuthSSO) && (ntlmMessage != null))
/* 1909 */         authLen = (short)ntlmMessage.length;
/*      */       else {
/* 1911 */         authLen = (short)(32 + domain.length());
/*      */       }
/* 1913 */       packSize = (short)(packSize + authLen);
/*      */     } else {
/* 1915 */       authLen = 0;
/* 1916 */       packSize = (short)(packSize + 2 * (user.length() + password.length()));
/*      */     }
/*      */ 
/* 1920 */     this.out.setPacketType(16);
/* 1921 */     this.out.write(packSize);
/*      */ 
/* 1923 */     if (this.tdsVersion == 3)
/* 1924 */       this.out.write(1879048192);
/*      */     else {
/* 1926 */       this.out.write(1895825409);
/*      */     }
/*      */ 
/* 1929 */     this.out.write(netPacketSize);
/*      */ 
/* 1931 */     this.out.write(7);
/*      */ 
/* 1933 */     this.out.write(this.connection.getProcessId());
/*      */ 
/* 1935 */     this.out.write(0);
/*      */ 
/* 1939 */     byte flags = -32;
/* 1940 */     this.out.write(flags);
/*      */ 
/* 1943 */     flags = 3;
/* 1944 */     if (ntlmAuth)
/* 1945 */       flags = (byte)(flags | 0x80);
/* 1946 */     this.out.write(flags);
/*      */ 
/* 1948 */     this.out.write(0);
/* 1949 */     this.out.write(0);
/*      */ 
/* 1951 */     this.out.write(empty, 0, 4);
/* 1952 */     this.out.write(empty, 0, 4);
/*      */ 
/* 1955 */     short curPos = 86;
/*      */ 
/* 1958 */     this.out.write(curPos);
/* 1959 */     this.out.write((short)wsid.length());
/* 1960 */     curPos = (short)(curPos + wsid.length() * 2);
/*      */ 
/* 1963 */     if (!(ntlmAuth))
/*      */     {
/* 1965 */       this.out.write(curPos);
/* 1966 */       this.out.write((short)user.length());
/* 1967 */       curPos = (short)(curPos + user.length() * 2);
/*      */ 
/* 1970 */       this.out.write(curPos);
/* 1971 */       this.out.write((short)password.length());
/* 1972 */       curPos = (short)(curPos + password.length() * 2);
/*      */     } else {
/* 1974 */       this.out.write(curPos);
/* 1975 */       this.out.write(0);
/*      */ 
/* 1977 */       this.out.write(curPos);
/* 1978 */       this.out.write(0);
/*      */     }
/*      */ 
/* 1982 */     this.out.write(curPos);
/* 1983 */     this.out.write((short)appName.length());
/* 1984 */     curPos = (short)(curPos + appName.length() * 2);
/*      */ 
/* 1987 */     this.out.write(curPos);
/* 1988 */     this.out.write((short)serverName.length());
/* 1989 */     curPos = (short)(curPos + serverName.length() * 2);
/*      */ 
/* 1992 */     this.out.write(0);
/* 1993 */     this.out.write(0);
/*      */ 
/* 1996 */     this.out.write(curPos);
/* 1997 */     this.out.write((short)progName.length());
/* 1998 */     curPos = (short)(curPos + progName.length() * 2);
/*      */ 
/* 2001 */     this.out.write(curPos);
/* 2002 */     this.out.write((short)language.length());
/* 2003 */     curPos = (short)(curPos + language.length() * 2);
/*      */ 
/* 2006 */     this.out.write(curPos);
/* 2007 */     this.out.write((short)database.length());
/* 2008 */     curPos = (short)(curPos + database.length() * 2);
/*      */ 
/* 2011 */     this.out.write(getMACAddress(macAddress));
/*      */ 
/* 2014 */     this.out.write(curPos);
/* 2015 */     this.out.write(authLen);
/*      */ 
/* 2018 */     this.out.write(packSize);
/*      */ 
/* 2020 */     this.out.write(wsid);
/*      */ 
/* 2024 */     if (!(ntlmAuth)) {
/* 2025 */       String scrambledPw = tds7CryptPass(password);
/* 2026 */       this.out.write(user);
/* 2027 */       this.out.write(scrambledPw);
/*      */     }
/*      */ 
/* 2030 */     this.out.write(appName);
/* 2031 */     this.out.write(serverName);
/* 2032 */     this.out.write(progName);
/* 2033 */     this.out.write(language);
/* 2034 */     this.out.write(database);
/*      */ 
/* 2037 */     if (ntlmAuth) {
/* 2038 */       if (this.ntlmAuthSSO)
/*      */       {
/* 2040 */         this.out.write(ntlmMessage);
/*      */       }
/*      */       else {
/* 2043 */         byte[] domainBytes = domain.getBytes("UTF8");
/*      */ 
/* 2046 */         byte[] header = { 78, 84, 76, 77, 83, 83, 80, 0 };
/* 2047 */         this.out.write(header);
/* 2048 */         this.out.write(1);
/* 2049 */         if (this.connection.getUseNTLMv2())
/* 2050 */           this.out.write(569861);
/*      */         else {
/* 2052 */           this.out.write(45569);
/*      */         }
/*      */ 
/* 2064 */         this.out.write((short)domainBytes.length);
/* 2065 */         this.out.write((short)domainBytes.length);
/* 2066 */         this.out.write(32);
/*      */ 
/* 2070 */         this.out.write(0);
/* 2071 */         this.out.write(0);
/* 2072 */         this.out.write(32);
/*      */ 
/* 2075 */         this.out.write(domainBytes);
/*      */       }
/*      */     }
/* 2078 */     this.out.flush();
/* 2079 */     this.endOfResponse = false;
/*      */   }
/*      */ 
/*      */   private void sendNtlmChallengeResponse(byte[] nonce, String user, String password, String domain)
/*      */     throws IOException
/*      */   {
/* 2095 */     this.out.setPacketType(17);
/*      */ 
/* 2099 */     if (this.ntlmAuthSSO) {
/* 2100 */       byte[] ntlmMessage = this.currentToken.ntlmMessage;
/*      */       try
/*      */       {
/* 2103 */         ntlmMessage = sspiJNIClient.invokePrepareSSOSubmit(ntlmMessage);
/*      */       } catch (Exception e) {
/* 2105 */         throw new IOException("SSO Failed: " + e.getMessage());
/*      */       }
/* 2107 */       this.out.write(ntlmMessage);
/*      */     }
/*      */     else
/*      */     {
/*      */       byte[] lmAnswer;
/*      */       byte[] ntAnswer;
/* 2117 */       if (this.connection.getUseNTLMv2())
/*      */       {
/* 2121 */         byte[] clientNonce = new byte[8];
/* 2122 */         new Random().nextBytes(clientNonce);
/*      */ 
/* 2124 */         lmAnswer = NtlmAuth.answerLmv2Challenge(domain, user, password, nonce, clientNonce);
/* 2125 */         ntAnswer = NtlmAuth.answerNtlmv2Challenge(domain, user, password, nonce, this.currentToken.ntlmTarget, clientNonce);
/*      */       }
/*      */       else
/*      */       {
/* 2131 */         lmAnswer = NtlmAuth.answerLmChallenge(password, nonce);
/* 2132 */         ntAnswer = NtlmAuth.answerNtChallenge(password, nonce);
/*      */       }
/*      */ 
/* 2135 */       byte[] header = { 78, 84, 76, 77, 83, 83, 80, 0 };
/* 2136 */       this.out.write(header);
/* 2137 */       this.out.write(3);
/* 2138 */       int domainLenInBytes = domain.length() * 2;
/* 2139 */       int userLenInBytes = user.length() * 2;
/*      */ 
/* 2141 */       int hostLenInBytes = 0;
/* 2142 */       int pos = 64 + domainLenInBytes + userLenInBytes + 0;
/*      */ 
/* 2144 */       this.out.write((short)lmAnswer.length);
/* 2145 */       this.out.write((short)lmAnswer.length);
/* 2146 */       this.out.write(pos);
/* 2147 */       pos += lmAnswer.length;
/*      */ 
/* 2149 */       this.out.write((short)ntAnswer.length);
/* 2150 */       this.out.write((short)ntAnswer.length);
/* 2151 */       this.out.write(pos);
/* 2152 */       pos = 64;
/*      */ 
/* 2154 */       this.out.write((short)domainLenInBytes);
/* 2155 */       this.out.write((short)domainLenInBytes);
/* 2156 */       this.out.write(pos);
/* 2157 */       pos += domainLenInBytes;
/*      */ 
/* 2160 */       this.out.write((short)userLenInBytes);
/* 2161 */       this.out.write((short)userLenInBytes);
/* 2162 */       this.out.write(pos);
/* 2163 */       pos += userLenInBytes;
/*      */ 
/* 2165 */       this.out.write(0);
/* 2166 */       this.out.write(0);
/* 2167 */       this.out.write(pos);
/* 2168 */       pos += 0;
/*      */ 
/* 2170 */       this.out.write(0);
/* 2171 */       this.out.write(0);
/* 2172 */       this.out.write(pos);
/*      */ 
/* 2174 */       if (this.connection.getUseNTLMv2())
/* 2175 */         this.out.write(557569);
/*      */       else {
/* 2177 */         this.out.write(33281);
/*      */       }
/* 2179 */       this.out.write(domain);
/* 2180 */       this.out.write(user);
/*      */ 
/* 2185 */       this.out.write(lmAnswer);
/* 2186 */       this.out.write(ntAnswer);
/*      */     }
/* 2188 */     this.out.flush();
/*      */   }
/*      */ 
/*      */   private void nextToken()
/*      */     throws SQLException
/*      */   {
/* 2199 */     checkOpen();
/* 2200 */     if (this.endOfResponse) {
/* 2201 */       this.currentToken.token = -3;
/* 2202 */       this.currentToken.status = 0;
/* 2203 */       return;
/*      */     }
/*      */     try {
/* 2206 */       this.currentToken.token = (byte)this.in.read();
/* 2207 */       switch (this.currentToken.token)
/*      */       {
/*      */       case 32:
/* 2209 */         tds5ParamFmt2Token();
/* 2210 */         break;
/*      */       case 33:
/* 2212 */         tdsInvalidToken();
/* 2213 */         break;
/*      */       case 97:
/* 2215 */         tds5WideResultToken();
/* 2216 */         break;
/*      */       case 113:
/* 2218 */         tdsInvalidToken();
/* 2219 */         break;
/*      */       case 121:
/* 2221 */         tdsReturnStatusToken();
/* 2222 */         break;
/*      */       case 124:
/* 2224 */         tdsProcIdToken();
/* 2225 */         break;
/*      */       case 120:
/* 2227 */         tdsOffsetsToken();
/* 2228 */         break;
/*      */       case -127:
/* 2230 */         tds7ResultToken();
/* 2231 */         break;
/*      */       case -120:
/* 2233 */         tdsInvalidToken();
/* 2234 */         break;
/*      */       case -96:
/* 2236 */         tds4ColNamesToken();
/* 2237 */         break;
/*      */       case -95:
/* 2239 */         tds4ColFormatToken();
/* 2240 */         break;
/*      */       case -92:
/* 2242 */         tdsTableNameToken();
/* 2243 */         break;
/*      */       case -91:
/* 2245 */         tdsColumnInfoToken();
/* 2246 */         break;
/*      */       case -89:
/* 2248 */         tdsInvalidToken();
/* 2249 */         break;
/*      */       case -88:
/* 2251 */         tdsInvalidToken();
/* 2252 */         break;
/*      */       case -87:
/* 2254 */         tdsOrderByToken();
/* 2255 */         break;
/*      */       case -86:
/*      */       case -85:
/* 2258 */         tdsErrorToken();
/* 2259 */         break;
/*      */       case -84:
/* 2261 */         tdsOutputParamToken();
/* 2262 */         break;
/*      */       case -83:
/* 2264 */         tdsLoginAckToken();
/* 2265 */         break;
/*      */       case -82:
/* 2267 */         tdsControlToken();
/* 2268 */         break;
/*      */       case -47:
/* 2270 */         tdsRowToken();
/* 2271 */         break;
/*      */       case -45:
/* 2273 */         tdsInvalidToken();
/* 2274 */         break;
/*      */       case -41:
/* 2276 */         tds5ParamsToken();
/* 2277 */         break;
/*      */       case -30:
/* 2279 */         tdsCapabilityToken();
/* 2280 */         break;
/*      */       case -29:
/* 2282 */         tdsEnvChangeToken();
/* 2283 */         break;
/*      */       case -27:
/* 2285 */         tds5ErrorToken();
/* 2286 */         break;
/*      */       case -25:
/* 2288 */         tds5DynamicToken();
/* 2289 */         break;
/*      */       case -20:
/* 2291 */         tds5ParamFmtToken();
/* 2292 */         break;
/*      */       case -19:
/* 2294 */         tdsNtlmAuthToken();
/* 2295 */         break;
/*      */       case -18:
/* 2297 */         tds5ResultToken();
/* 2298 */         break;
/*      */       case -3:
/*      */       case -2:
/*      */       case -1:
/* 2302 */         tdsDoneToken();
/* 2303 */         break;
/*      */       default:
/* 2305 */         throw new ProtocolException("Invalid packet type 0x" + Integer.toHexString(this.currentToken.token & 0xFF));
/*      */       }
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/* 2310 */       this.connection.setClosed();
/* 2311 */       throw Support.linkException(new SQLException(Messages.get("error.generic.ioerror", ioe.getMessage()), "08S01"), ioe);
/*      */     }
/*      */     catch (ProtocolException pe)
/*      */     {
/* 2317 */       this.connection.setClosed();
/* 2318 */       throw Support.linkException(new SQLException(Messages.get("error.generic.tdserror", pe.getMessage()), "08S01"), pe);
/*      */     }
/*      */     catch (OutOfMemoryError err)
/*      */     {
/* 2325 */       this.in.skipToEnd();
/* 2326 */       this.endOfResponse = true;
/* 2327 */       this.endOfResults = true;
/* 2328 */       this.cancelPending = false;
/* 2329 */       throw err;
/*      */     }
/*      */   }
/*      */ 
/*      */   private void tdsInvalidToken()
/*      */     throws IOException, ProtocolException
/*      */   {
/* 2341 */     this.in.skip(this.in.readShort());
/* 2342 */     throw new ProtocolException("Unsupported TDS token: 0x" + Integer.toHexString(this.currentToken.token & 0xFF));
/*      */   }
/*      */ 
/*      */   private void tds5ParamFmt2Token()
/*      */     throws IOException, ProtocolException
/*      */   {
/* 2354 */     this.in.readInt();
/* 2355 */     int paramCnt = this.in.readShort();
/* 2356 */     ColInfo[] params = new ColInfo[paramCnt];
/* 2357 */     for (int i = 0; i < paramCnt; ++i)
/*      */     {
/* 2362 */       ColInfo col = new ColInfo();
/* 2363 */       int colNameLen = this.in.read();
/* 2364 */       col.realName = this.in.readNonUnicodeString(colNameLen);
/* 2365 */       int column_flags = this.in.readInt();
/* 2366 */       col.isCaseSensitive = false;
/* 2367 */       col.nullable = (((column_flags & 0x20) != 0) ? 1 : 0);
/*      */ 
/* 2370 */       col.isWriteable = ((column_flags & 0x10) != 0);
/* 2371 */       col.isIdentity = ((column_flags & 0x40) != 0);
/* 2372 */       col.isKey = ((column_flags & 0x2) != 0);
/* 2373 */       col.isHidden = ((column_flags & 0x1) != 0);
/*      */ 
/* 2375 */       col.userType = this.in.readInt();
/* 2376 */       TdsData.readType(this.in, col);
/*      */ 
/* 2378 */       this.in.skip(1);
/* 2379 */       params[i] = col;
/*      */     }
/* 2381 */     this.currentToken.dynamParamInfo = params;
/* 2382 */     this.currentToken.dynamParamData = new Object[paramCnt];
/*      */   }
/*      */ 
/*      */   private void tds5WideResultToken()
/*      */     throws IOException, ProtocolException
/*      */   {
/* 2394 */     this.in.readInt();
/* 2395 */     int colCnt = this.in.readShort();
/* 2396 */     this.columns = new ColInfo[colCnt];
/* 2397 */     this.rowData = new Object[colCnt];
/* 2398 */     this.tables = null;
/*      */ 
/* 2400 */     for (int colNum = 0; colNum < colCnt; ++colNum) {
/* 2401 */       ColInfo col = new ColInfo();
/*      */ 
/* 2405 */       int nameLen = this.in.read();
/* 2406 */       col.name = this.in.readNonUnicodeString(nameLen);
/*      */ 
/* 2410 */       nameLen = this.in.read();
/* 2411 */       col.catalog = this.in.readNonUnicodeString(nameLen);
/*      */ 
/* 2415 */       nameLen = this.in.read();
/* 2416 */       col.schema = this.in.readNonUnicodeString(nameLen);
/*      */ 
/* 2420 */       nameLen = this.in.read();
/* 2421 */       col.tableName = this.in.readNonUnicodeString(nameLen);
/*      */ 
/* 2425 */       nameLen = this.in.read();
/* 2426 */       col.realName = this.in.readNonUnicodeString(nameLen);
/* 2427 */       if ((col.name == null) || (col.name.length() == 0)) {
/* 2428 */         col.name = col.realName;
/*      */       }
/* 2430 */       int column_flags = this.in.readInt();
/* 2431 */       col.isCaseSensitive = false;
/* 2432 */       col.nullable = (((column_flags & 0x20) != 0) ? 1 : 0);
/*      */ 
/* 2435 */       col.isWriteable = ((column_flags & 0x10) != 0);
/* 2436 */       col.isIdentity = ((column_flags & 0x40) != 0);
/* 2437 */       col.isKey = ((column_flags & 0x2) != 0);
/* 2438 */       col.isHidden = ((column_flags & 0x1) != 0);
/*      */ 
/* 2440 */       col.userType = this.in.readInt();
/* 2441 */       TdsData.readType(this.in, col);
/*      */ 
/* 2443 */       this.in.skip(1);
/* 2444 */       this.columns[colNum] = col;
/*      */     }
/* 2446 */     this.endOfResults = false;
/*      */   }
/*      */ 
/*      */   private void tdsReturnStatusToken()
/*      */     throws IOException, SQLException
/*      */   {
/* 2455 */     this.returnStatus = new Integer(this.in.readInt());
/* 2456 */     if (this.returnParam != null)
/* 2457 */       this.returnParam.setOutValue(Support.convert(this.connection, this.returnStatus, this.returnParam.jdbcType, this.connection.getCharset()));
/*      */   }
/*      */ 
/*      */   private void tdsProcIdToken()
/*      */     throws IOException
/*      */   {
/* 2470 */     this.in.skip(8);
/*      */   }
/*      */ 
/*      */   private void tdsOffsetsToken()
/*      */     throws IOException
/*      */   {
/* 2481 */     this.in.read();
/* 2482 */     this.in.read();
/* 2483 */     this.in.readShort();
/*      */   }
/*      */ 
/*      */   private void tds7ResultToken()
/*      */     throws IOException, ProtocolException, SQLException
/*      */   {
/* 2494 */     this.endOfResults = false;
/*      */ 
/* 2496 */     int colCnt = this.in.readShort();
/*      */ 
/* 2498 */     if (colCnt < 0)
/*      */     {
/* 2502 */       return;
/*      */     }
/*      */ 
/* 2505 */     this.columns = new ColInfo[colCnt];
/* 2506 */     this.rowData = new Object[colCnt];
/* 2507 */     this.tables = null;
/*      */ 
/* 2509 */     for (int i = 0; i < colCnt; ++i) {
/* 2510 */       ColInfo col = new ColInfo();
/*      */ 
/* 2512 */       col.userType = this.in.readShort();
/*      */ 
/* 2514 */       int flags = this.in.readShort();
/*      */ 
/* 2516 */       col.nullable = (((flags & 0x1) != 0) ? 1 : 0);
/*      */ 
/* 2519 */       col.isCaseSensitive = ((flags & 0x2) != 0);
/* 2520 */       col.isIdentity = ((flags & 0x10) != 0);
/* 2521 */       col.isWriteable = ((flags & 0xC) != 0);
/* 2522 */       TdsData.readType(this.in, col);
/*      */ 
/* 2524 */       if ((this.tdsVersion >= 4) && (col.collation != null)) {
/* 2525 */         TdsData.setColumnCharset(col, this.connection);
/*      */       }
/*      */ 
/* 2528 */       int clen = this.in.read();
/*      */ 
/* 2530 */       col.realName = this.in.readUnicodeString(clen);
/* 2531 */       col.name = col.realName;
/*      */ 
/* 2533 */       this.columns[i] = col;
/*      */     }
/*      */   }
/*      */ 
/*      */   private void tds4ColNamesToken()
/*      */     throws IOException
/*      */   {
/* 2545 */     ArrayList colList = new ArrayList();
/*      */ 
/* 2547 */     int pktLen = this.in.readShort();
/* 2548 */     this.tables = null;
/* 2549 */     int bytesRead = 0;
/*      */ 
/* 2551 */     while (bytesRead < pktLen) {
/* 2552 */       ColInfo col = new ColInfo();
/* 2553 */       int nameLen = this.in.read();
/* 2554 */       String name = this.in.readNonUnicodeString(nameLen);
/*      */ 
/* 2556 */       bytesRead = bytesRead + 1 + nameLen;
/* 2557 */       col.realName = name;
/* 2558 */       col.name = name;
/*      */ 
/* 2560 */       colList.add(col);
/*      */     }
/*      */ 
/* 2563 */     int colCnt = colList.size();
/* 2564 */     this.columns = ((ColInfo[])colList.toArray(new ColInfo[colCnt]));
/* 2565 */     this.rowData = new Object[colCnt];
/*      */   }
/*      */ 
/*      */   private void tds4ColFormatToken()
/*      */     throws IOException, ProtocolException
/*      */   {
/* 2577 */     int pktLen = this.in.readShort();
/*      */ 
/* 2579 */     int bytesRead = 0;
/* 2580 */     int numColumns = 0;
/* 2581 */     while (bytesRead < pktLen) {
/* 2582 */       if (numColumns > this.columns.length) {
/* 2583 */         throw new ProtocolException("Too many columns in TDS_COL_FMT packet");
/*      */       }
/* 2585 */       ColInfo col = this.columns[numColumns];
/*      */ 
/* 2587 */       if (this.serverType == 1) {
/* 2588 */         col.userType = this.in.readShort();
/*      */ 
/* 2590 */         int flags = this.in.readShort();
/*      */ 
/* 2592 */         col.nullable = (((flags & 0x1) != 0) ? 1 : 0);
/*      */ 
/* 2595 */         col.isCaseSensitive = ((flags & 0x2) != 0);
/* 2596 */         col.isWriteable = ((flags & 0xC) != 0);
/* 2597 */         col.isIdentity = ((flags & 0x10) != 0);
/*      */       }
/*      */       else {
/* 2600 */         col.isCaseSensitive = false;
/* 2601 */         col.isWriteable = true;
/*      */ 
/* 2603 */         if (col.nullable == 0) {
/* 2604 */           col.nullable = 2;
/*      */         }
/*      */ 
/* 2607 */         col.userType = this.in.readInt();
/*      */       }
/* 2609 */       bytesRead += 4;
/*      */ 
/* 2611 */       bytesRead += TdsData.readType(this.in, col);
/*      */ 
/* 2613 */       ++numColumns;
/*      */     }
/*      */ 
/* 2616 */     if (numColumns != this.columns.length) {
/* 2617 */       throw new ProtocolException("Too few columns in TDS_COL_FMT packet");
/*      */     }
/*      */ 
/* 2620 */     this.endOfResults = false;
/*      */   }
/*      */ 
/*      */   private void tdsTableNameToken()
/*      */     throws IOException, ProtocolException
/*      */   {
/* 2630 */     int pktLen = this.in.readShort();
/* 2631 */     int bytesRead = 0;
/* 2632 */     ArrayList tableList = new ArrayList();
/*      */ 
/* 2634 */     while (bytesRead < pktLen)
/*      */     {
/*      */       TableMetaData table;
/*      */       int nameLen;
/* 2638 */       if (this.tdsVersion >= 5)
/*      */       {
/* 2642 */         table = new TableMetaData(null);
/* 2643 */         ++bytesRead;
/* 2644 */         int tableNameToken = this.in.read();
/* 2645 */         switch (tableNameToken)
/*      */         {
/*      */         case 4:
/* 2646 */           nameLen = this.in.readShort();
/* 2647 */           bytesRead += nameLen * 2 + 2;
/*      */ 
/* 2649 */           this.in.readUnicodeString(nameLen);
/*      */         case 3:
/* 2650 */           nameLen = this.in.readShort();
/* 2651 */           bytesRead += nameLen * 2 + 2;
/* 2652 */           table.catalog = this.in.readUnicodeString(nameLen);
/*      */         case 2:
/* 2653 */           nameLen = this.in.readShort();
/* 2654 */           bytesRead += nameLen * 2 + 2;
/* 2655 */           table.schema = this.in.readUnicodeString(nameLen);
/*      */         case 1:
/* 2656 */           nameLen = this.in.readShort();
/* 2657 */           bytesRead += nameLen * 2 + 2;
/* 2658 */           table.name = this.in.readUnicodeString(nameLen);
/*      */         case 0:
/* 2659 */           break;
/*      */         default:
/* 2661 */           throw new ProtocolException("Invalid table TAB_NAME_TOKEN: " + tableNameToken);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*      */         String tabName;
/* 2665 */         if (this.tdsVersion >= 3) {
/* 2666 */           nameLen = this.in.readShort();
/* 2667 */           bytesRead += nameLen * 2 + 2;
/* 2668 */           tabName = this.in.readUnicodeString(nameLen);
/*      */         }
/*      */         else {
/* 2671 */           nameLen = this.in.read();
/* 2672 */           ++bytesRead;
/* 2673 */           if (nameLen == 0) {
/*      */             continue;
/*      */           }
/* 2676 */           tabName = this.in.readNonUnicodeString(nameLen);
/* 2677 */           bytesRead += nameLen;
/*      */         }
/* 2679 */         table = new TableMetaData(null);
/*      */ 
/* 2681 */         int dotPos = tabName.lastIndexOf(46);
/* 2682 */         if (dotPos > 0) {
/* 2683 */           table.name = tabName.substring(dotPos + 1);
/*      */ 
/* 2685 */           int nextPos = tabName.lastIndexOf(46, dotPos - 1);
/* 2686 */           if (nextPos + 1 < dotPos) {
/* 2687 */             table.schema = tabName.substring(nextPos + 1, dotPos);
/*      */           }
/* 2689 */           dotPos = nextPos;
/* 2690 */           nextPos = tabName.lastIndexOf(46, dotPos - 1);
/* 2691 */           if (nextPos + 1 < dotPos)
/* 2692 */             table.catalog = tabName.substring(nextPos + 1, dotPos);
/*      */         }
/*      */         else {
/* 2695 */           table.name = tabName;
/*      */         }
/*      */       }
/* 2698 */       tableList.add(table);
/*      */     }
/* 2700 */     if (tableList.size() > 0)
/* 2701 */       this.tables = ((TableMetaData[])tableList.toArray(new TableMetaData[tableList.size()]));
/*      */   }
/*      */ 
/*      */   private void tdsColumnInfoToken()
/*      */     throws IOException, ProtocolException
/*      */   {
/* 2714 */     int pktLen = this.in.readShort();
/* 2715 */     int bytesRead = 0;
/* 2716 */     int columnIndex = 0;
/*      */ 
/* 2718 */     while (bytesRead < pktLen)
/*      */     {
/* 2723 */       this.in.read();
/* 2724 */       if (columnIndex >= this.columns.length) {
/* 2725 */         throw new ProtocolException("Column index " + (columnIndex + 1) + " invalid in TDS_COLINFO packet");
/*      */       }
/*      */ 
/* 2728 */       ColInfo col = this.columns[(columnIndex++)];
/* 2729 */       int tableIndex = this.in.read();
/*      */ 
/* 2734 */       if ((this.tables != null) && (tableIndex > this.tables.length)) {
/* 2735 */         throw new ProtocolException("Table index " + tableIndex + " invalid in TDS_COLINFO packet");
/*      */       }
/*      */ 
/* 2738 */       byte flags = (byte)this.in.read();
/* 2739 */       bytesRead += 3;
/*      */ 
/* 2741 */       if ((tableIndex != 0) && (this.tables != null)) {
/* 2742 */         TableMetaData table = this.tables[(tableIndex - 1)];
/* 2743 */         col.catalog = table.catalog;
/* 2744 */         col.schema = table.schema;
/* 2745 */         col.tableName = table.name;
/*      */       }
/*      */ 
/* 2748 */       col.isKey = ((flags & 0x8) != 0);
/* 2749 */       col.isHidden = ((flags & 0x10) != 0);
/*      */ 
/* 2752 */       if ((flags & 0x20) != 0) {
/* 2753 */         int nameLen = this.in.read();
/* 2754 */         ++bytesRead;
/* 2755 */         String colName = this.in.readString(nameLen);
/* 2756 */         bytesRead += ((this.tdsVersion >= 3) ? nameLen * 2 : nameLen);
/* 2757 */         col.realName = colName;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void tdsOrderByToken()
/*      */     throws IOException
/*      */   {
/* 2771 */     int pktLen = this.in.readShort();
/* 2772 */     this.in.skip(pktLen);
/*      */   }
/*      */ 
/*      */   private void tdsErrorToken()
/*      */     throws IOException
/*      */   {
/* 2783 */     int pktLen = this.in.readShort();
/* 2784 */     int sizeSoFar = 6;
/* 2785 */     int number = this.in.readInt();
/* 2786 */     int state = this.in.read();
/* 2787 */     int severity = this.in.read();
/* 2788 */     int msgLen = this.in.readShort();
/* 2789 */     String message = this.in.readString(msgLen);
/* 2790 */     sizeSoFar += 2 + ((this.tdsVersion >= 3) ? msgLen * 2 : msgLen);
/* 2791 */     int srvNameLen = this.in.read();
/* 2792 */     String server = this.in.readString(srvNameLen);
/* 2793 */     sizeSoFar += 1 + ((this.tdsVersion >= 3) ? srvNameLen * 2 : srvNameLen);
/*      */ 
/* 2795 */     int procNameLen = this.in.read();
/* 2796 */     String procName = this.in.readString(procNameLen);
/* 2797 */     sizeSoFar += 1 + ((this.tdsVersion >= 3) ? procNameLen * 2 : procNameLen);
/*      */ 
/* 2799 */     int line = this.in.readShort();
/* 2800 */     sizeSoFar += 2;
/*      */ 
/* 2802 */     if (pktLen - sizeSoFar > 0) {
/* 2803 */       this.in.skip(pktLen - sizeSoFar);
/*      */     }
/* 2805 */     if (this.currentToken.token == -86)
/*      */     {
/* 2807 */       if (severity < 10) {
/* 2808 */         severity = 11;
/*      */       }
/* 2810 */       if (severity >= 20)
/*      */       {
/* 2813 */         this.fatalError = true;
/*      */       }
/*      */     }
/* 2816 */     else if (severity > 9) {
/* 2817 */       severity = 9;
/*      */     }
/*      */ 
/* 2820 */     this.messages.addDiagnostic(number, state, severity, message, server, procName, line);
/*      */   }
/*      */ 
/*      */   private void tdsOutputParamToken()
/*      */     throws IOException, ProtocolException, SQLException
/*      */   {
/* 2846 */     this.in.readShort();
/* 2847 */     String name = this.in.readString(this.in.read());
/*      */ 
/* 2850 */     boolean funcReturnVal = this.in.read() == 2;
/*      */ 
/* 2853 */     this.in.read();
/*      */ 
/* 2855 */     this.in.skip(3);
/*      */ 
/* 2857 */     ColInfo col = new ColInfo();
/* 2858 */     TdsData.readType(this.in, col);
/*      */ 
/* 2860 */     if ((this.tdsVersion >= 4) && (col.collation != null)) {
/* 2861 */       TdsData.setColumnCharset(col, this.connection);
/*      */     }
/* 2863 */     Object value = TdsData.readData(this.connection, this.in, col);
/*      */ 
/* 2870 */     if ((this.parameters == null) || ((name.length() != 0) && (!(name.startsWith("@")))))
/*      */       return;
/* 2872 */     if ((this.tdsVersion >= 4) && (funcReturnVal))
/*      */     {
/* 2875 */       if (this.returnParam != null)
/* 2876 */         if (value != null) {
/* 2877 */           this.returnParam.setOutValue(Support.convert(this.connection, value, this.returnParam.jdbcType, this.connection.getCharset()));
/*      */ 
/* 2881 */           this.returnParam.collation = col.collation;
/* 2882 */           this.returnParam.charsetInfo = col.charsetInfo;
/*      */         } else {
/* 2884 */           this.returnParam.setOutValue(null);
/*      */         }
/*      */     }
/*      */     else {
/*      */       do
/* 2889 */         if (++this.nextParam >= this.parameters.length) return;
/* 2890 */       while (!(this.parameters[this.nextParam].isOutput));
/* 2891 */       if (value != null) {
/* 2892 */         this.parameters[this.nextParam].setOutValue(Support.convert(this.connection, value, this.parameters[this.nextParam].jdbcType, this.connection.getCharset()));
/*      */ 
/* 2896 */         this.parameters[this.nextParam].collation = col.collation;
/* 2897 */         this.parameters[this.nextParam].charsetInfo = col.charsetInfo;
/*      */       } else {
/* 2899 */         this.parameters[this.nextParam].setOutValue(null);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void tdsLoginAckToken()
/*      */     throws IOException
/*      */   {
/* 2915 */     int build = 0;
/* 2916 */     this.in.readShort();
/*      */ 
/* 2918 */     int ack = this.in.read();
/*      */ 
/* 2924 */     this.tdsVersion = TdsData.getTdsVersion(this.in.read() << 24 | this.in.read() << 16 | this.in.read() << 8 | this.in.read());
/*      */ 
/* 2926 */     this.socket.setTdsVersion(this.tdsVersion);
/*      */ 
/* 2928 */     String product = this.in.readString(this.in.read());
/*      */     int major;
/*      */     int minor;
/* 2930 */     if (this.tdsVersion >= 3) {
/* 2931 */       major = this.in.read();
/* 2932 */       minor = this.in.read();
/* 2933 */       build = this.in.read() << 8;
/* 2934 */       build += this.in.read();
/*      */     } else {
/* 2936 */       if (product.toLowerCase().startsWith("microsoft")) {
/* 2937 */         this.in.skip(1);
/* 2938 */         major = this.in.read();
/* 2939 */         minor = this.in.read();
/*      */       } else {
/* 2941 */         major = this.in.read();
/* 2942 */         minor = this.in.read() * 10;
/* 2943 */         minor += this.in.read();
/*      */       }
/* 2945 */       this.in.skip(1);
/*      */     }
/*      */ 
/* 2948 */     if ((product.length() > 1) && (-1 != product.indexOf(0))) {
/* 2949 */       product = product.substring(0, product.indexOf(0));
/*      */     }
/*      */ 
/* 2952 */     this.connection.setDBServerInfo(product, major, minor, build);
/*      */ 
/* 2954 */     if ((this.tdsVersion == 2) && (ack != 5))
/*      */     {
/* 2956 */       this.messages.addDiagnostic(4002, 0, 14, "Login failed", "", "", 0);
/*      */ 
/* 2958 */       this.currentToken.token = -86;
/*      */     }
/*      */     else
/*      */     {
/* 2971 */       SQLException ex = this.messages.exceptions;
/*      */ 
/* 2974 */       this.messages.clearWarnings();
/*      */ 
/* 2978 */       while (ex != null) {
/* 2979 */         this.messages.addWarning(new SQLWarning(ex.getMessage(), ex.getSQLState(), ex.getErrorCode()));
/*      */ 
/* 2982 */         ex = ex.getNextException();
/*      */       }
/* 2984 */       this.messages.exceptions = null;
/*      */     }
/*      */   }
/*      */ 
/*      */   private void tdsControlToken()
/*      */     throws IOException
/*      */   {
/* 2994 */     int pktLen = this.in.readShort();
/*      */ 
/* 2996 */     this.in.skip(pktLen);
/*      */   }
/*      */ 
/*      */   private void tdsRowToken()
/*      */     throws IOException, ProtocolException
/*      */   {
/* 3006 */     for (int i = 0; i < this.columns.length; ++i) {
/* 3007 */       this.rowData[i] = TdsData.readData(this.connection, this.in, this.columns[i]);
/*      */     }
/*      */ 
/* 3010 */     this.endOfResults = false;
/*      */   }
/*      */ 
/*      */   private void tds5ParamsToken()
/*      */     throws IOException, ProtocolException, SQLException
/*      */   {
/* 3026 */     if (this.currentToken.dynamParamInfo == null) {
/* 3027 */       throw new ProtocolException("TDS 5 Param results token (0xD7) not preceded by param format (0xEC or 0X20).");
/*      */     }
/*      */ 
/* 3031 */     for (int i = 0; i < this.currentToken.dynamParamData.length; ++i) {
/* 3032 */       this.currentToken.dynamParamData[i] = TdsData.readData(this.connection, this.in, this.currentToken.dynamParamInfo[i]);
/*      */ 
/* 3034 */       String name = this.currentToken.dynamParamInfo[i].realName;
/*      */ 
/* 3040 */       if ((this.parameters == null) || ((name.length() != 0) && (!(name.startsWith("@")))))
/*      */         continue;
/*      */       do
/* 3043 */         if (++this.nextParam >= this.parameters.length) break label207;
/* 3044 */       while (!(this.parameters[this.nextParam].isOutput));
/* 3045 */       Object value = this.currentToken.dynamParamData[i];
/* 3046 */       label207: if (value != null) {
/* 3047 */         this.parameters[this.nextParam].setOutValue(Support.convert(this.connection, value, this.parameters[this.nextParam].jdbcType, this.connection.getCharset()));
/*      */       }
/*      */       else
/*      */       {
/* 3052 */         this.parameters[this.nextParam].setOutValue(null);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void tdsCapabilityToken()
/*      */     throws IOException, ProtocolException
/*      */   {
/* 3069 */     this.in.readShort();
/* 3070 */     if (this.in.read() != 1) {
/* 3071 */       throw new ProtocolException("TDS_CAPABILITY: expected request string");
/*      */     }
/* 3073 */     int capLen = this.in.read();
/* 3074 */     if ((capLen != 11) && (capLen != 0)) {
/* 3075 */       throw new ProtocolException("TDS_CAPABILITY: byte count not 11");
/*      */     }
/* 3077 */     byte[] capRequest = new byte[11];
/* 3078 */     if (capLen == 0)
/* 3079 */       Logger.println("TDS_CAPABILITY: Invalid request length");
/*      */     else {
/* 3081 */       this.in.read(capRequest);
/*      */     }
/* 3083 */     if (this.in.read() != 2) {
/* 3084 */       throw new ProtocolException("TDS_CAPABILITY: expected response string");
/*      */     }
/* 3086 */     capLen = this.in.read();
/* 3087 */     if ((capLen != 10) && (capLen != 0)) {
/* 3088 */       throw new ProtocolException("TDS_CAPABILITY: byte count not 10");
/*      */     }
/* 3090 */     byte[] capResponse = new byte[10];
/* 3091 */     if (capLen == 0)
/* 3092 */       Logger.println("TDS_CAPABILITY: Invalid response length");
/*      */     else {
/* 3094 */       this.in.read(capResponse);
/*      */     }
/*      */ 
/* 3115 */     int capMask = 0;
/* 3116 */     if ((capRequest[0] & 0x2) == 2) {
/* 3117 */       capMask |= 32;
/*      */     }
/* 3119 */     if ((capRequest[1] & 0x3) == 3) {
/* 3120 */       capMask |= 2;
/*      */     }
/* 3122 */     if ((capRequest[2] & 0x80) == 128) {
/* 3123 */       capMask |= 16;
/*      */     }
/* 3125 */     if ((capRequest[3] & 0x2) == 2) {
/* 3126 */       capMask |= 8;
/*      */     }
/* 3128 */     if ((capRequest[2] & 0x1) == 1) {
/* 3129 */       capMask |= 64;
/*      */     }
/* 3131 */     if ((capRequest[4] & 0x4) == 4) {
/* 3132 */       capMask |= 4;
/*      */     }
/* 3134 */     if ((capRequest[7] & 0x30) == 48) {
/* 3135 */       capMask |= 1;
/*      */     }
/* 3137 */     this.connection.setSybaseInfo(capMask);
/*      */   }
/*      */ 
/*      */   private void tdsEnvChangeToken()
/*      */     throws IOException, SQLException
/*      */   {
/* 3149 */     int len = this.in.readShort();
/* 3150 */     int type = this.in.read();
/*      */     int clen;
/* 3152 */     switch (type)
/*      */     {
/*      */     case 1:
/* 3155 */       clen = this.in.read();
/* 3156 */       String newDb = this.in.readString(clen);
/* 3157 */       clen = this.in.read();
/* 3158 */       String oldDb = this.in.readString(clen);
/* 3159 */       this.connection.setDatabase(newDb, oldDb);
/* 3160 */       break;
/*      */     case 2:
/* 3165 */       clen = this.in.read();
/* 3166 */       String language = this.in.readString(clen);
/* 3167 */       clen = this.in.read();
/* 3168 */       String oldLang = this.in.readString(clen);
/* 3169 */       if (!(Logger.isActive())) return;
/* 3170 */       Logger.println("Language changed from " + oldLang + " to " + language); break;
/*      */     case 3:
/* 3177 */       clen = this.in.read();
/* 3178 */       String charset = this.in.readString(clen);
/* 3179 */       if (this.tdsVersion >= 3)
/* 3180 */         this.in.skip(len - 2 - (clen * 2));
/*      */       else {
/* 3182 */         this.in.skip(len - 2 - clen);
/*      */       }
/* 3184 */       this.connection.setServerCharset(charset);
/* 3185 */       break;
/*      */     case 4:
/* 3191 */       int clen = this.in.read();
/* 3192 */       int blocksize = Integer.parseInt(this.in.readString(clen));
/* 3193 */       if (this.tdsVersion >= 3)
/* 3194 */         this.in.skip(len - 2 - (clen * 2));
/*      */       else {
/* 3196 */         this.in.skip(len - 2 - clen);
/*      */       }
/* 3198 */       this.connection.setNetPacketSize(blocksize);
/* 3199 */       this.out.setBufferSize(blocksize);
/* 3200 */       if (!(Logger.isActive())) break label367;
/* 3201 */       Logger.println("Changed blocksize to " + blocksize);
/*      */ 
/* 3204 */       break;
/*      */     case 5:
/* 3210 */       this.in.skip(len - 1);
/* 3211 */       break;
/*      */     case 7:
/* 3215 */       clen = this.in.read();
/* 3216 */       byte[] collation = new byte[5];
/* 3217 */       if (clen == 5) {
/* 3218 */         this.in.read(collation);
/* 3219 */         this.connection.setCollation(collation);
/*      */       } else {
/* 3221 */         this.in.skip(clen);
/*      */       }
/* 3223 */       clen = this.in.read();
/* 3224 */       this.in.skip(clen);
/* 3225 */       break;
/*      */     case 6:
/*      */     default:
/* 3230 */       if (Logger.isActive()) {
/* 3231 */         label367: Logger.println("Unknown environment change type 0x" + Integer.toHexString(type));
/*      */       }
/*      */ 
/* 3234 */       this.in.skip(len - 1);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void tds5ErrorToken()
/*      */     throws IOException
/*      */   {
/* 3246 */     int pktLen = this.in.readShort();
/* 3247 */     int sizeSoFar = 6;
/* 3248 */     int number = this.in.readInt();
/* 3249 */     int state = this.in.read();
/* 3250 */     int severity = this.in.read();
/*      */ 
/* 3252 */     int stateLen = this.in.read();
/* 3253 */     this.in.readNonUnicodeString(stateLen);
/* 3254 */     this.in.read();
/*      */ 
/* 3256 */     this.in.readShort();
/* 3257 */     sizeSoFar += 4 + stateLen;
/*      */ 
/* 3259 */     int msgLen = this.in.readShort();
/* 3260 */     String message = this.in.readNonUnicodeString(msgLen);
/* 3261 */     sizeSoFar += 2 + msgLen;
/* 3262 */     int srvNameLen = this.in.read();
/* 3263 */     String server = this.in.readNonUnicodeString(srvNameLen);
/* 3264 */     sizeSoFar += 1 + srvNameLen;
/*      */ 
/* 3266 */     int procNameLen = this.in.read();
/* 3267 */     String procName = this.in.readNonUnicodeString(procNameLen);
/* 3268 */     sizeSoFar += 1 + procNameLen;
/*      */ 
/* 3270 */     int line = this.in.readShort();
/* 3271 */     sizeSoFar += 2;
/*      */ 
/* 3273 */     if (pktLen - sizeSoFar > 0) {
/* 3274 */       this.in.skip(pktLen - sizeSoFar);
/*      */     }
/* 3276 */     if (severity > 10)
/*      */     {
/* 3278 */       this.messages.addDiagnostic(number, state, severity, message, server, procName, line);
/*      */     }
/*      */     else
/* 3281 */       this.messages.addDiagnostic(number, state, severity, message, server, procName, line);
/*      */   }
/*      */ 
/*      */   private void tds5DynamicToken()
/*      */     throws IOException
/*      */   {
/* 3294 */     int pktLen = this.in.readShort();
/* 3295 */     byte type = (byte)this.in.read();
/* 3296 */     this.in.read();
/* 3297 */     pktLen -= 2;
/* 3298 */     if (type == 32)
/*      */     {
/* 3300 */       int len = this.in.read();
/* 3301 */       this.in.skip(len);
/* 3302 */       pktLen -= len + 1;
/*      */     }
/* 3304 */     this.in.skip(pktLen);
/*      */   }
/*      */ 
/*      */   private void tds5ParamFmtToken()
/*      */     throws IOException, ProtocolException
/*      */   {
/* 3317 */     this.in.readShort();
/* 3318 */     int paramCnt = this.in.readShort();
/* 3319 */     ColInfo[] params = new ColInfo[paramCnt];
/* 3320 */     for (int i = 0; i < paramCnt; ++i)
/*      */     {
/* 3325 */       ColInfo col = new ColInfo();
/* 3326 */       int colNameLen = this.in.read();
/* 3327 */       col.realName = this.in.readNonUnicodeString(colNameLen);
/* 3328 */       int column_flags = this.in.read();
/* 3329 */       col.isCaseSensitive = false;
/* 3330 */       col.nullable = (((column_flags & 0x20) != 0) ? 1 : 0);
/*      */ 
/* 3333 */       col.isWriteable = ((column_flags & 0x10) != 0);
/* 3334 */       col.isIdentity = ((column_flags & 0x40) != 0);
/* 3335 */       col.isKey = ((column_flags & 0x2) != 0);
/* 3336 */       col.isHidden = ((column_flags & 0x1) != 0);
/*      */ 
/* 3338 */       col.userType = this.in.readInt();
/* 3339 */       if ((byte)this.in.peek() == -3)
/*      */       {
/* 3341 */         this.currentToken.dynamParamInfo = null;
/* 3342 */         this.currentToken.dynamParamData = null;
/*      */ 
/* 3344 */         this.messages.addDiagnostic(9999, 0, 16, "Prepare failed", "", "", 0);
/*      */ 
/* 3347 */         return;
/*      */       }
/* 3349 */       TdsData.readType(this.in, col);
/*      */ 
/* 3351 */       this.in.skip(1);
/* 3352 */       params[i] = col;
/*      */     }
/* 3354 */     this.currentToken.dynamParamInfo = params;
/* 3355 */     this.currentToken.dynamParamData = new Object[paramCnt];
/*      */   }
/*      */ 
/*      */   private void tdsNtlmAuthToken()
/*      */     throws IOException, ProtocolException
/*      */   {
/* 3367 */     int pktLen = this.in.readShort();
/*      */ 
/* 3369 */     int hdrLen = 40;
/*      */ 
/* 3371 */     if (pktLen < hdrLen) {
/* 3372 */       throw new ProtocolException("NTLM challenge: packet is too small:" + pktLen);
/*      */     }
/* 3374 */     byte[] ntlmMessage = new byte[pktLen];
/* 3375 */     this.in.read(ntlmMessage);
/*      */ 
/* 3377 */     int seq = getIntFromBuffer(ntlmMessage, 8);
/* 3378 */     if (seq != 2) {
/* 3379 */       throw new ProtocolException("NTLM challenge: got unexpected sequence number:" + seq);
/*      */     }
/* 3381 */     int flags = getIntFromBuffer(ntlmMessage, 20);
/*      */ 
/* 3390 */     int headerOffset = 40;
/*      */ 
/* 3392 */     int size = getShortFromBuffer(ntlmMessage, 40);
/* 3393 */     int offset = getIntFromBuffer(ntlmMessage, 44);
/* 3394 */     this.currentToken.ntlmTarget = new byte[size];
/* 3395 */     System.arraycopy(ntlmMessage, offset, this.currentToken.ntlmTarget, 0, size);
/*      */ 
/* 3397 */     this.currentToken.nonce = new byte[8];
/* 3398 */     this.currentToken.ntlmMessage = ntlmMessage;
/* 3399 */     System.arraycopy(ntlmMessage, 24, this.currentToken.nonce, 0, 8);
/*      */   }
/*      */ 
/*      */   private static int getIntFromBuffer(byte[] buf, int offset)
/*      */   {
/* 3404 */     int b1 = buf[offset] & 0xFF;
/* 3405 */     int b2 = (buf[(offset + 1)] & 0xFF) << 8;
/* 3406 */     int b3 = (buf[(offset + 2)] & 0xFF) << 16;
/* 3407 */     int b4 = (buf[(offset + 3)] & 0xFF) << 24;
/* 3408 */     return (b4 | b3 | b2 | b1);
/*      */   }
/*      */ 
/*      */   private static int getShortFromBuffer(byte[] buf, int offset)
/*      */   {
/* 3413 */     int b1 = buf[offset] & 0xFF;
/* 3414 */     int b2 = (buf[(offset + 1)] & 0xFF) << 8;
/* 3415 */     return (b2 | b1);
/*      */   }
/*      */ 
/*      */   private void tds5ResultToken()
/*      */     throws IOException, ProtocolException
/*      */   {
/* 3424 */     this.in.readShort();
/* 3425 */     int colCnt = this.in.readShort();
/* 3426 */     this.columns = new ColInfo[colCnt];
/* 3427 */     this.rowData = new Object[colCnt];
/* 3428 */     this.tables = null;
/*      */ 
/* 3430 */     for (int colNum = 0; colNum < colCnt; ++colNum)
/*      */     {
/* 3434 */       ColInfo col = new ColInfo();
/* 3435 */       int colNameLen = this.in.read();
/* 3436 */       col.realName = this.in.readNonUnicodeString(colNameLen);
/* 3437 */       col.name = col.realName;
/* 3438 */       int column_flags = this.in.read();
/* 3439 */       col.isCaseSensitive = false;
/* 3440 */       col.nullable = (((column_flags & 0x20) != 0) ? 1 : 0);
/*      */ 
/* 3443 */       col.isWriteable = ((column_flags & 0x10) != 0);
/* 3444 */       col.isIdentity = ((column_flags & 0x40) != 0);
/* 3445 */       col.isKey = ((column_flags & 0x2) != 0);
/* 3446 */       col.isHidden = ((column_flags & 0x1) != 0);
/*      */ 
/* 3448 */       col.userType = this.in.readInt();
/* 3449 */       TdsData.readType(this.in, col);
/*      */ 
/* 3451 */       this.in.skip(1);
/* 3452 */       this.columns[colNum] = col;
/*      */     }
/* 3454 */     this.endOfResults = false;
/*      */   }
/*      */ 
/*      */   private void tdsDoneToken()
/*      */     throws IOException
/*      */   {
/* 3463 */     this.currentToken.status = (byte)this.in.read();
/* 3464 */     this.in.skip(1);
/* 3465 */     this.currentToken.operation = (byte)this.in.read();
/* 3466 */     this.in.skip(1);
/* 3467 */     this.currentToken.updateCount = this.in.readInt();
/*      */ 
/* 3469 */     if (!(this.endOfResults))
/*      */     {
/*      */       TdsToken tmp73_70 = this.currentToken; tmp73_70.status = (byte)(tmp73_70.status & 0xFFFFFFEF);
/* 3472 */       this.endOfResults = true;
/*      */     }
/*      */ 
/* 3478 */     if ((this.currentToken.status & 0x20) != 0)
/*      */     {
/* 3481 */       synchronized (this.cancelMonitor) {
/* 3482 */         this.cancelPending = false;
/*      */ 
/* 3484 */         if (this.cancelMonitor[0] == 0) {
/* 3485 */           this.messages.addException(new SQLException(Messages.get("error.generic.cancelled", "Statement"), "HY008"));
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3493 */     if ((this.currentToken.status & 0x1) == 0)
/*      */     {
/* 3498 */       this.endOfResponse = (!(this.cancelPending));
/*      */ 
/* 3500 */       if (this.fatalError)
/*      */       {
/* 3503 */         this.connection.setClosed();
/*      */       }
/*      */     }
/*      */ 
/* 3507 */     if ((this.serverType != 1) || 
/* 3512 */       (this.currentToken.operation != -63))
/*      */       return;
/*      */     TdsToken tmp225_222 = this.currentToken; tmp225_222.status = (byte)(tmp225_222.status & 0xFFFFFFEF);
/*      */   }
/*      */ 
/*      */   private void executeSQL42(String sql, String procName, ParamInfo[] parameters, boolean noMetaData, boolean sendNow)
/*      */     throws IOException, SQLException
/*      */   {
/* 3533 */     if (procName != null)
/*      */     {
/* 3535 */       this.out.setPacketType(3);
/* 3536 */       byte[] buf = Support.encodeString(this.connection.getCharset(), procName);
/*      */ 
/* 3538 */       this.out.write((byte)buf.length);
/* 3539 */       this.out.write(buf);
/* 3540 */       this.out.write((short)((noMetaData) ? 2 : 0));
/*      */ 
/* 3542 */       if (parameters != null) {
/* 3543 */         for (int i = this.nextParam + 1; i < parameters.length; ++i) {
/* 3544 */           if (parameters[i].name != null) {
/* 3545 */             buf = Support.encodeString(this.connection.getCharset(), parameters[i].name);
/*      */ 
/* 3547 */             this.out.write((byte)buf.length);
/* 3548 */             this.out.write(buf);
/*      */           } else {
/* 3550 */             this.out.write(0);
/*      */           }
/*      */ 
/* 3553 */           this.out.write((byte)((parameters[i].isOutput) ? 1 : 0));
/* 3554 */           TdsData.writeParam(this.out, this.connection.getCharsetInfo(), null, parameters[i]);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 3560 */       if (!(sendNow))
/*      */       {
/* 3562 */         this.out.write(-128);
/*      */       }
/* 3564 */     } else if (sql.length() > 0) {
/* 3565 */       if (parameters != null) {
/* 3566 */         sql = Support.substituteParameters(sql, parameters, this.connection);
/*      */       }
/*      */ 
/* 3569 */       this.out.setPacketType(1);
/* 3570 */       this.out.write(sql);
/* 3571 */       if (sendNow)
/*      */         return;
/* 3573 */       this.out.write(" ");
/*      */     }
/*      */   }
/*      */ 
/*      */   private void executeSQL50(String sql, String procName, ParamInfo[] parameters)
/*      */     throws IOException, SQLException
/*      */   {
/* 3590 */     boolean haveParams = parameters != null;
/* 3591 */     boolean useParamNames = false;
/* 3592 */     this.currentToken.dynamParamInfo = null;
/* 3593 */     this.currentToken.dynamParamData = null;
/*      */ 
/* 3603 */     for (int i = 0; (haveParams) && (i < parameters.length); ++i) {
/* 3604 */       if ((!("text".equals(parameters[i].sqlType))) && (!("image".equals(parameters[i].sqlType))) && (!("unitext".equals(parameters[i].sqlType)))) {
/*      */         continue;
/*      */       }
/* 3607 */       if ((procName != null) && (procName.length() > 0))
/*      */       {
/* 3609 */         if (("text".equals(parameters[i].sqlType)) || ("unitext".equals(parameters[i].sqlType)))
/*      */         {
/* 3611 */           throw new SQLException(Messages.get("error.chartoolong"), "HY000");
/*      */         }
/*      */ 
/* 3615 */         throw new SQLException(Messages.get("error.bintoolong"), "HY000");
/*      */       }
/*      */ 
/* 3618 */       if (parameters[i].tdsType == 36)
/*      */         continue;
/* 3620 */       sql = Support.substituteParameters(sql, parameters, this.connection);
/* 3621 */       haveParams = false;
/* 3622 */       procName = null;
/* 3623 */       break;
/*      */     }
/*      */ 
/* 3628 */     this.out.setPacketType(15);
/*      */     byte[] buf;
/* 3630 */     if (procName == null)
/*      */     {
/* 3632 */       this.out.write(33);
/*      */ 
/* 3634 */       if (haveParams) {
/* 3635 */         sql = Support.substituteParamMarkers(sql, parameters);
/*      */       }
/*      */ 
/* 3638 */       if (this.connection.isWideChar())
/*      */       {
/* 3640 */         buf = Support.encodeString(this.connection.getCharset(), sql);
/*      */ 
/* 3642 */         this.out.write(buf.length + 1);
/* 3643 */         this.out.write((byte)((haveParams) ? 1 : 0));
/* 3644 */         this.out.write(buf);
/*      */       } else {
/* 3646 */         this.out.write(sql.length() + 1);
/* 3647 */         this.out.write((byte)((haveParams) ? 1 : 0));
/* 3648 */         this.out.write(sql);
/*      */       }
/* 3650 */     } else if (procName.startsWith("#jtds"))
/*      */     {
/* 3652 */       this.out.write(-25);
/* 3653 */       this.out.write((short)(procName.length() + 4));
/* 3654 */       this.out.write(2);
/* 3655 */       this.out.write((byte)((haveParams) ? 1 : 0));
/* 3656 */       this.out.write((byte)(procName.length() - 1));
/* 3657 */       this.out.write(procName.substring(1));
/* 3658 */       this.out.write(0);
/*      */     } else {
/* 3660 */       buf = Support.encodeString(this.connection.getCharset(), procName);
/*      */ 
/* 3663 */       this.out.write(-26);
/* 3664 */       this.out.write((short)(buf.length + 3));
/* 3665 */       this.out.write((byte)buf.length);
/* 3666 */       this.out.write(buf);
/* 3667 */       this.out.write((short)((haveParams) ? 2 : 0));
/* 3668 */       useParamNames = true;
/*      */     }
/*      */ 
/* 3674 */     if (!(haveParams))
/*      */       return;
/* 3676 */     this.out.write(-20);
/*      */ 
/* 3678 */     int len = 2;
/*      */ 
/* 3680 */     for (int i = this.nextParam + 1; i < parameters.length; ++i) {
/* 3681 */       len += TdsData.getTds5ParamSize(this.connection.getCharset(), this.connection.isWideChar(), parameters[i], useParamNames);
/*      */     }
/*      */ 
/* 3687 */     this.out.write((short)len);
/* 3688 */     this.out.write((short)((this.nextParam < 0) ? parameters.length : parameters.length - 1));
/*      */ 
/* 3690 */     for (i = this.nextParam + 1; i < parameters.length; ++i) {
/* 3691 */       TdsData.writeTds5ParamFmt(this.out, this.connection.getCharset(), this.connection.isWideChar(), parameters[i], useParamNames);
/*      */     }
/*      */ 
/* 3699 */     this.out.write(-41);
/*      */ 
/* 3701 */     for (i = this.nextParam + 1; i < parameters.length; ++i)
/* 3702 */       TdsData.writeTds5Param(this.out, this.connection.getCharsetInfo(), parameters[i]);
/*      */   }
/*      */ 
/*      */   public static boolean isPreparedProcedureName(String procName)
/*      */   {
/* 3720 */     return ((procName != null) && (procName.length() > 0) && (Character.isDigit(procName.charAt(0))));
/*      */   }
/*      */ 
/*      */   private void executeSQL70(String sql, String procName, ParamInfo[] parameters, boolean noMetaData, boolean sendNow)
/*      */     throws IOException, SQLException
/*      */   {
/* 3739 */     int prepareSql = this.connection.getPrepareSql();
/*      */ 
/* 3741 */     if ((parameters == null) && (prepareSql == 2))
/*      */     {
/* 3748 */       prepareSql = 0;
/*      */     }
/*      */ 
/* 3751 */     if (this.inBatch)
/*      */     {
/* 3755 */       prepareSql = 2;
/*      */     }
/*      */     ParamInfo[] params;
/* 3758 */     if (procName == null)
/*      */     {
/* 3761 */       if (parameters != null) {
/* 3762 */         if (prepareSql == 0)
/*      */         {
/* 3765 */           sql = Support.substituteParameters(sql, parameters, this.connection);
/*      */         }
/*      */         else
/*      */         {
/* 3771 */           params = new ParamInfo[2 + parameters.length];
/* 3772 */           System.arraycopy(parameters, 0, params, 2, parameters.length);
/*      */ 
/* 3774 */           params[0] = new ParamInfo(-1, Support.substituteParamMarkers(sql, parameters), 4);
/*      */ 
/* 3777 */           TdsData.getNativeType(this.connection, params[0]);
/*      */ 
/* 3779 */           params[1] = new ParamInfo(-1, Support.getParameterDefinitions(parameters), 4);
/*      */ 
/* 3782 */           TdsData.getNativeType(this.connection, params[1]);
/*      */ 
/* 3784 */           parameters = params;
/*      */ 
/* 3787 */           procName = "sp_executesql";
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/* 3793 */     else if (isPreparedProcedureName(procName))
/*      */     {
/* 3798 */       if (parameters != null) {
/* 3799 */         params = new ParamInfo[1 + parameters.length];
/* 3800 */         System.arraycopy(parameters, 0, params, 1, parameters.length);
/*      */       } else {
/* 3802 */         params = new ParamInfo[1];
/*      */       }
/*      */ 
/* 3805 */       params[0] = new ParamInfo(4, new Integer(procName), 0);
/*      */ 
/* 3807 */       TdsData.getNativeType(this.connection, params[0]);
/*      */ 
/* 3809 */       parameters = params;
/*      */ 
/* 3812 */       procName = "sp_execute";
/*      */     }
/*      */ 
/* 3816 */     if (procName != null)
/*      */     {
/* 3818 */       this.out.setPacketType(3);
/*      */       Integer shortcut;
/* 3821 */       if ((this.tdsVersion >= 4) && ((shortcut = (Integer)tds8SpNames.get(procName)) != null))
/*      */       {
/* 3824 */         this.out.write(-1);
/* 3825 */         this.out.write(shortcut.shortValue());
/*      */       } else {
/* 3827 */         this.out.write((short)procName.length());
/* 3828 */         this.out.write(procName);
/*      */       }
/*      */ 
/* 3835 */       this.out.write((short)((noMetaData) ? 2 : 0));
/*      */ 
/* 3837 */       if (parameters != null)
/*      */       {
/* 3839 */         for (int i = this.nextParam + 1; i < parameters.length; ++i) {
/* 3840 */           if (parameters[i].name != null) {
/* 3841 */             this.out.write((byte)parameters[i].name.length());
/* 3842 */             this.out.write(parameters[i].name);
/*      */           } else {
/* 3844 */             this.out.write(0);
/*      */           }
/*      */ 
/* 3847 */           this.out.write((byte)((parameters[i].isOutput) ? 1 : 0));
/*      */ 
/* 3849 */           TdsData.writeParam(this.out, this.connection.getCharsetInfo(), this.connection.getCollation(), parameters[i]);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 3855 */       if (sendNow)
/*      */         return;
/* 3857 */       this.out.write(-128);
/*      */     } else {
/* 3859 */       if (sql.length() <= 0)
/*      */         return;
/* 3861 */       this.out.setPacketType(1);
/* 3862 */       this.out.write(sql);
/* 3863 */       if (sendNow)
/*      */         return;
/* 3865 */       this.out.write(" ");
/*      */     }
/*      */   }
/*      */ 
/*      */   private void setRowCountAndTextSize(int rowCount, int textSize)
/*      */     throws SQLException
/*      */   {
/* 3882 */     boolean newRowCount = (rowCount >= 0) && (rowCount != this.connection.getRowCount());
/*      */ 
/* 3884 */     boolean newTextSize = (textSize >= 0) && (textSize != this.connection.getTextSize());
/*      */ 
/* 3886 */     if ((!(newRowCount)) && (!(newTextSize))) return;
/*      */     try {
/* 3888 */       StringBuffer query = new StringBuffer(64);
/* 3889 */       if (newRowCount) {
/* 3890 */         query.append("SET ROWCOUNT ").append(rowCount);
/*      */       }
/* 3892 */       if (newTextSize) {
/* 3893 */         query.append(" SET TEXTSIZE ").append((textSize == 0) ? 2147483647 : textSize);
/*      */       }
/*      */ 
/* 3896 */       this.out.setPacketType(1);
/* 3897 */       this.out.write(query.toString());
/* 3898 */       this.out.flush();
/* 3899 */       this.endOfResponse = false;
/* 3900 */       this.endOfResults = true;
/* 3901 */       wait(0);
/* 3902 */       clearResponseQueue();
/* 3903 */       this.messages.checkErrors();
/*      */ 
/* 3905 */       this.connection.setRowCount(rowCount);
/* 3906 */       this.connection.setTextSize(textSize);
/*      */     } catch (IOException ioe) {
/* 3908 */       throw new SQLException(Messages.get("error.generic.ioerror", ioe.getMessage()), "08S01");
/*      */     }
/*      */   }
/*      */ 
/*      */   private void wait(int timeOut)
/*      */     throws IOException, SQLException
/*      */   {
/* 3921 */     Object timer = null;
/*      */     try {
/* 3923 */       if (timeOut > 0)
/*      */       {
/* 3925 */         timer = TimerThread.getInstance().setTimer(timeOut * 1000, new TimerThread.TimerListener()
/*      */         {
/*      */           public void timerExpired() {
/* 3928 */             TdsCore.this.cancel(true);
/*      */           }
/*      */         });
/*      */       }
/* 3932 */       this.in.peek();
/*      */     } finally {
/* 3934 */       if ((timer != null) && 
/* 3935 */         (!(TimerThread.getInstance().cancelTimer(timer))))
/* 3936 */         throw new SQLException(Messages.get("error.generic.timeout"), "HYT00");
/*      */     }
/*      */   }
/*      */ 
/*      */   public void cleanUp()
/*      */   {
/* 3949 */     if (!(this.endOfResponse))
/*      */       return;
/* 3951 */     this.returnParam = null;
/* 3952 */     this.parameters = null;
/*      */ 
/* 3954 */     this.columns = null;
/* 3955 */     this.rowData = null;
/* 3956 */     this.tables = null;
/*      */ 
/* 3958 */     this.messages.clearWarnings();
/*      */   }
/*      */ 
/*      */   public SQLDiagnostic getMessages()
/*      */   {
/* 3966 */     return this.messages;
/*      */   }
/*      */ 
/*      */   private static byte[] getMACAddress(String macString)
/*      */   {
/* 3976 */     byte[] mac = new byte[6];
/* 3977 */     boolean ok = false;
/*      */ 
/* 3979 */     if ((macString != null) && (macString.length() == 12)) {
/*      */       try {
/* 3981 */         int i = 0; for (int j = 0; i < 6; j += 2) {
/* 3982 */           mac[i] = (byte)Integer.parseInt(macString.substring(j, j + 2), 16);
/*      */ 
/* 3981 */           ++i;
/*      */         }
/*      */ 
/* 3986 */         ok = true;
/*      */       }
/*      */       catch (Exception ex)
/*      */       {
/*      */       }
/*      */     }
/* 3992 */     if (!(ok)) {
/* 3993 */       Arrays.fill(mac, 0);
/*      */     }
/*      */ 
/* 3996 */     return mac;
/*      */   }
/*      */ 
/*      */   private static String getHostName()
/*      */   {
/* 4006 */     if (hostName != null) {
/* 4007 */       return hostName;
/*      */     }
/*      */ 
/*      */     String name;
/*      */     try
/*      */     {
/* 4013 */       name = InetAddress.getLocalHost().getHostName().toUpperCase();
/*      */     } catch (UnknownHostException e) {
/* 4015 */       hostName = "UNKNOWN";
/* 4016 */       return hostName;
/*      */     }
/*      */ 
/* 4019 */     int pos = name.indexOf(46);
/*      */ 
/* 4021 */     if (pos >= 0) {
/* 4022 */       name = name.substring(0, pos);
/*      */     }
/*      */ 
/* 4025 */     if (name.length() == 0) {
/* 4026 */       hostName = "UNKNOWN";
/* 4027 */       return hostName;
/*      */     }
/*      */     try
/*      */     {
/* 4031 */       Integer.parseInt(name);
/*      */ 
/* 4033 */       hostName = "UNKNOWN";
/* 4034 */       return hostName;
/*      */     }
/*      */     catch (NumberFormatException e)
/*      */     {
/* 4039 */       hostName = name; }
/* 4040 */     return name;
/*      */   }
/*      */ 
/*      */   private static String tds7CryptPass(String pw)
/*      */   {
/* 4050 */     int xormask = 23130;
/* 4051 */     int len = pw.length();
/* 4052 */     char[] chars = new char[len];
/*      */ 
/* 4054 */     for (int i = 0; i < len; ++i) {
/* 4055 */       int c = pw.charAt(i) ^ 0x5A5A;
/* 4056 */       int m1 = c >> 4 & 0xF0F;
/* 4057 */       int m2 = c << 4 & 0xF0F0;
/*      */ 
/* 4059 */       chars[i] = (char)(m1 | m2);
/*      */     }
/*      */ 
/* 4062 */     return new String(chars);
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*  349 */     tds8SpNames.put("sp_cursor", new Integer(1));
/*  350 */     tds8SpNames.put("sp_cursoropen", new Integer(2));
/*  351 */     tds8SpNames.put("sp_cursorprepare", new Integer(3));
/*  352 */     tds8SpNames.put("sp_cursorexecute", new Integer(4));
/*  353 */     tds8SpNames.put("sp_cursorprepexec", new Integer(5));
/*  354 */     tds8SpNames.put("sp_cursorunprepare", new Integer(6));
/*  355 */     tds8SpNames.put("sp_cursorfetch", new Integer(7));
/*  356 */     tds8SpNames.put("sp_cursoroption", new Integer(8));
/*  357 */     tds8SpNames.put("sp_cursorclose", new Integer(9));
/*  358 */     tds8SpNames.put("sp_executesql", new Integer(10));
/*  359 */     tds8SpNames.put("sp_prepare", new Integer(11));
/*  360 */     tds8SpNames.put("sp_execute", new Integer(12));
/*  361 */     tds8SpNames.put("sp_prepexec", new Integer(13));
/*  362 */     tds8SpNames.put("sp_prepexecrpc", new Integer(14));
/*  363 */     tds8SpNames.put("sp_unprepare", new Integer(15));
/*      */   }
/*      */ 
/*      */   private static class TableMetaData
/*      */   {
/*      */     String catalog;
/*      */     String schema;
/*      */     String name;
/*      */ 
/*      */     private TableMetaData()
/*      */     {
/*      */     }
/*      */ 
/*      */     TableMetaData(TdsCore.1 x0)
/*      */     {
/*      */     }
/*      */   }
/*      */ 
/*      */   private static class TdsToken
/*      */   {
/*      */     byte token;
/*      */     byte status;
/*      */     byte operation;
/*      */     int updateCount;
/*      */     byte[] nonce;
/*      */     byte[] ntlmMessage;
/*      */     byte[] ntlmTarget;
/*      */     ColInfo[] dynamParamInfo;
/*      */     Object[] dynamParamData;
/*      */ 
/*      */     private TdsToken()
/*      */     {
/*      */     }
/*      */ 
/*      */     boolean isUpdateCount()
/*      */     {
/*   87 */       return ((((this.token == -3) || (this.token == -1))) && ((this.status & 0x10) != 0));
/*      */     }
/*      */ 
/*      */     boolean isEndToken()
/*      */     {
/*   97 */       return ((this.token == -3) || (this.token == -1) || (this.token == -2));
/*      */     }
/*      */ 
/*      */     boolean isAuthToken()
/*      */     {
/*  108 */       return (this.token == -19);
/*      */     }
/*      */ 
/*      */     boolean resultsPending()
/*      */     {
/*  117 */       return ((!(isEndToken())) || ((this.status & 0x1) != 0));
/*      */     }
/*      */ 
/*      */     boolean isResultSet()
/*      */     {
/*  126 */       return ((this.token == -95) || (this.token == -127) || (this.token == -18) || (this.token == 97) || (this.token == -91) || (this.token == -47));
/*      */     }
/*      */ 
/*      */     public boolean isRowData()
/*      */     {
/*  140 */       return (this.token == -47);
/*      */     }
/*      */ 
/*      */     TdsToken(TdsCore.1 x0)
/*      */     {
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.TdsCore
 * JD-Core Version:    0.5.3
 */