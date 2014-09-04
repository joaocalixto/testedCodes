/*     */ package net.sourceforge.jtds.jdbcx;
/*     */ 
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.io.Serializable;
/*     */ import java.sql.Connection;
/*     */ import java.sql.SQLException;
/*     */ import java.util.Properties;
/*     */ import javax.naming.NamingException;
/*     */ import javax.naming.Reference;
/*     */ import javax.naming.Referenceable;
/*     */ import javax.naming.StringRefAddr;
/*     */ import javax.sql.ConnectionPoolDataSource;
/*     */ import javax.sql.DataSource;
/*     */ import javax.sql.XAConnection;
/*     */ import javax.sql.XADataSource;
/*     */ import net.sourceforge.jtds.jdbc.DefaultProperties;
/*     */ import net.sourceforge.jtds.jdbc.Driver;
/*     */ import net.sourceforge.jtds.jdbc.Messages;
/*     */ import net.sourceforge.jtds.jdbc.Support;
/*     */ import net.sourceforge.jtds.util.Logger;
/*     */ 
/*     */ public class JtdsDataSource
/*     */   implements DataSource, ConnectionPoolDataSource, XADataSource, Referenceable, Serializable
/*     */ {
/*     */   static final long serialVersionUID = 266240L;
/*     */   protected String serverName;
/*     */   protected String serverType;
/*     */   protected String portNumber;
/*     */   protected String databaseName;
/*     */   protected String tdsVersion;
/*     */   protected String charset;
/*     */   protected String language;
/*     */   protected String domain;
/*     */   protected String useNTLMV2;
/*     */   protected String instance;
/*     */   protected String lastUpdateCount;
/*     */   protected String sendStringParametersAsUnicode;
/*     */   protected String namedPipe;
/*     */   protected String macAddress;
/*     */   protected String prepareSql;
/*     */   protected String packetSize;
/*     */   protected String tcpNoDelay;
/*     */   protected String user;
/*     */   protected String password;
/*     */   protected String loginTimeout;
/*     */   protected String lobBuffer;
/*     */   protected String maxStatements;
/*     */   protected String appName;
/*     */   protected String progName;
/*     */   protected String wsid;
/*     */   protected String xaEmulation;
/*     */   protected String logFile;
/*     */   protected String socketTimeout;
/*     */   protected String socketKeepAlive;
/*     */   protected String processId;
/*     */   protected String ssl;
/*     */   protected String batchSize;
/*     */   protected String bufferDir;
/*     */   protected String bufferMaxMemory;
/*     */   protected String bufferMinPackets;
/*     */   protected String cacheMetaData;
/*     */   protected String useCursors;
/*     */   protected String useLOBs;
/*     */   protected String bindAddress;
/*     */   protected String useJCIFS;
/*     */   protected String description;
/*  98 */   private static final Driver driver = new Driver();
/*     */ 
/*     */   public XAConnection getXAConnection()
/*     */     throws SQLException
/*     */   {
/* 116 */     return new JtdsXAConnection(this, getConnection(this.user, this.password));
/*     */   }
/*     */ 
/*     */   public XAConnection getXAConnection(String user, String password)
/*     */     throws SQLException
/*     */   {
/* 127 */     return new JtdsXAConnection(this, getConnection(user, password));
/*     */   }
/*     */ 
/*     */   public Connection getConnection()
/*     */     throws SQLException
/*     */   {
/* 137 */     return getConnection(this.user, this.password);
/*     */   }
/*     */ 
/*     */   public Connection getConnection(String user, String password)
/*     */     throws SQLException
/*     */   {
/* 151 */     if (this.serverName == null) {
/* 152 */       throw new SQLException(Messages.get("error.connection.nohost"), "08001");
/*     */     }
/*     */ 
/* 159 */     if ((getLogWriter() == null) && (this.logFile != null) && (this.logFile.length() > 0)) {
/*     */       try
/*     */       {
/* 162 */         setLogWriter(new PrintWriter(new FileOutputStream(this.logFile), true));
/*     */       } catch (IOException e) {
/* 164 */         System.err.println("jTDS: Failed to set log file " + e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 168 */     Properties props = new Properties();
/* 169 */     addNonNullProperties(props, user, password);
/*     */     String url;
/*     */     try
/*     */     {
/* 174 */       int serverTypeDef = (this.serverType == null) ? 0 : Integer.parseInt(this.serverType);
/*     */ 
/* 176 */       url = "jdbc:jtds:" + DefaultProperties.getServerTypeWithDefault(serverTypeDef) + ':';
/*     */     }
/*     */     catch (RuntimeException ex)
/*     */     {
/* 180 */       SQLException sqlException = new SQLException(Messages.get("error.connection.servertype", ex.toString()), "08001");
/*     */ 
/* 182 */       Support.linkException(sqlException, ex);
/* 183 */       throw sqlException;
/*     */     }
/*     */ 
/* 188 */     return driver.connect(url, props);
/*     */   }
/*     */ 
/*     */   public Reference getReference() throws NamingException {
/* 192 */     Reference ref = new Reference(super.getClass().getName(), JtdsObjectFactory.class.getName(), null);
/*     */ 
/* 196 */     ref.add(new StringRefAddr(Messages.get("prop.servername"), this.serverName));
/* 197 */     ref.add(new StringRefAddr(Messages.get("prop.servertype"), this.serverType));
/* 198 */     ref.add(new StringRefAddr(Messages.get("prop.portnumber"), this.portNumber));
/* 199 */     ref.add(new StringRefAddr(Messages.get("prop.databasename"), this.databaseName));
/* 200 */     ref.add(new StringRefAddr(Messages.get("prop.tds"), this.tdsVersion));
/* 201 */     ref.add(new StringRefAddr(Messages.get("prop.charset"), this.charset));
/* 202 */     ref.add(new StringRefAddr(Messages.get("prop.language"), this.language));
/* 203 */     ref.add(new StringRefAddr(Messages.get("prop.domain"), this.domain));
/* 204 */     ref.add(new StringRefAddr(Messages.get("prop.usentlmv2"), this.useNTLMV2));
/* 205 */     ref.add(new StringRefAddr(Messages.get("prop.instance"), this.instance));
/* 206 */     ref.add(new StringRefAddr(Messages.get("prop.lastupdatecount"), this.lastUpdateCount));
/* 207 */     ref.add(new StringRefAddr(Messages.get("prop.useunicode"), this.sendStringParametersAsUnicode));
/* 208 */     ref.add(new StringRefAddr(Messages.get("prop.namedpipe"), this.namedPipe));
/* 209 */     ref.add(new StringRefAddr(Messages.get("prop.macaddress"), this.macAddress));
/* 210 */     ref.add(new StringRefAddr(Messages.get("prop.preparesql"), this.prepareSql));
/* 211 */     ref.add(new StringRefAddr(Messages.get("prop.packetsize"), this.packetSize));
/* 212 */     ref.add(new StringRefAddr(Messages.get("prop.tcpnodelay"), this.tcpNoDelay));
/* 213 */     ref.add(new StringRefAddr(Messages.get("prop.xaemulation"), this.xaEmulation));
/* 214 */     ref.add(new StringRefAddr(Messages.get("prop.user"), this.user));
/* 215 */     ref.add(new StringRefAddr(Messages.get("prop.password"), this.password));
/* 216 */     ref.add(new StringRefAddr(Messages.get("prop.logintimeout"), this.loginTimeout));
/* 217 */     ref.add(new StringRefAddr(Messages.get("prop.sotimeout"), this.socketTimeout));
/* 218 */     ref.add(new StringRefAddr(Messages.get("prop.sokeepalive"), this.socketKeepAlive));
/* 219 */     ref.add(new StringRefAddr(Messages.get("prop.processid"), this.processId));
/* 220 */     ref.add(new StringRefAddr(Messages.get("prop.lobbuffer"), this.lobBuffer));
/* 221 */     ref.add(new StringRefAddr(Messages.get("prop.maxstatements"), this.maxStatements));
/* 222 */     ref.add(new StringRefAddr(Messages.get("prop.appname"), this.appName));
/* 223 */     ref.add(new StringRefAddr(Messages.get("prop.progname"), this.progName));
/* 224 */     ref.add(new StringRefAddr(Messages.get("prop.wsid"), this.wsid));
/* 225 */     ref.add(new StringRefAddr(Messages.get("prop.logfile"), this.logFile));
/* 226 */     ref.add(new StringRefAddr(Messages.get("prop.ssl"), this.ssl));
/* 227 */     ref.add(new StringRefAddr(Messages.get("prop.batchsize"), this.batchSize));
/* 228 */     ref.add(new StringRefAddr(Messages.get("prop.bufferdir"), this.bufferDir));
/* 229 */     ref.add(new StringRefAddr(Messages.get("prop.buffermaxmemory"), this.bufferMaxMemory));
/* 230 */     ref.add(new StringRefAddr(Messages.get("prop.bufferminpackets"), this.bufferMinPackets));
/* 231 */     ref.add(new StringRefAddr(Messages.get("prop.cachemetadata"), this.cacheMetaData));
/* 232 */     ref.add(new StringRefAddr(Messages.get("prop.usecursors"), this.useCursors));
/* 233 */     ref.add(new StringRefAddr(Messages.get("prop.uselobs"), this.useLOBs));
/* 234 */     ref.add(new StringRefAddr(Messages.get("prop.bindaddress"), this.bindAddress));
/* 235 */     ref.add(new StringRefAddr(Messages.get("prop.usejcifs"), this.useJCIFS));
/*     */ 
/* 237 */     ref.add(new StringRefAddr("description", this.description));
/*     */ 
/* 239 */     return ref;
/*     */   }
/*     */ 
/*     */   public javax.sql.PooledConnection getPooledConnection()
/*     */     throws SQLException
/*     */   {
/* 254 */     return getPooledConnection(this.user, this.password);
/*     */   }
/*     */ 
/*     */   public synchronized javax.sql.PooledConnection getPooledConnection(String user, String password)
/*     */     throws SQLException
/*     */   {
/* 268 */     return new PooledConnection(getConnection(user, password));
/*     */   }
/*     */ 
/*     */   public PrintWriter getLogWriter()
/*     */     throws SQLException
/*     */   {
/* 276 */     return Logger.getLogWriter();
/*     */   }
/*     */ 
/*     */   public void setLogWriter(PrintWriter out) throws SQLException {
/* 280 */     Logger.setLogWriter(out);
/*     */   }
/*     */ 
/*     */   public void setLoginTimeout(int loginTimeout) throws SQLException {
/* 284 */     this.loginTimeout = String.valueOf(loginTimeout);
/*     */   }
/*     */ 
/*     */   public int getLoginTimeout() throws SQLException {
/* 288 */     if (this.loginTimeout == null) {
/* 289 */       return 0;
/*     */     }
/* 291 */     return Integer.parseInt(this.loginTimeout);
/*     */   }
/*     */ 
/*     */   public void setSocketTimeout(int socketTimeout) throws SQLException {
/* 295 */     this.socketTimeout = String.valueOf(socketTimeout);
/*     */   }
/*     */ 
/*     */   public void setSocketKeepAlive(boolean socketKeepAlive) throws SQLException {
/* 299 */     this.socketKeepAlive = String.valueOf(socketKeepAlive); }
/*     */ 
/*     */   public void setProcessId(String processId) throws SQLException {
/* 302 */     this.processId = String.valueOf(processId);
/*     */   }
/*     */ 
/*     */   public int getSocketTimeout() throws SQLException {
/* 306 */     if (this.socketTimeout == null) {
/* 307 */       return 0;
/*     */     }
/* 309 */     return Integer.parseInt(this.socketTimeout);
/*     */   }
/*     */ 
/*     */   public boolean getSocketKeepAlive() throws SQLException {
/* 313 */     return Boolean.valueOf(this.socketKeepAlive).booleanValue();
/*     */   }
/*     */ 
/*     */   public String getProcessId() throws SQLException {
/* 317 */     return this.processId;
/*     */   }
/*     */ 
/*     */   public void setDatabaseName(String databaseName) {
/* 321 */     this.databaseName = databaseName;
/*     */   }
/*     */ 
/*     */   public String getDatabaseName() {
/* 325 */     return this.databaseName;
/*     */   }
/*     */ 
/*     */   public void setDescription(String description) {
/* 329 */     this.description = description;
/*     */   }
/*     */ 
/*     */   public String getDescription() {
/* 333 */     return this.description;
/*     */   }
/*     */ 
/*     */   public void setPassword(String password) {
/* 337 */     this.password = password;
/*     */   }
/*     */ 
/*     */   public String getPassword() {
/* 341 */     return this.password;
/*     */   }
/*     */ 
/*     */   public void setPortNumber(int portNumber) {
/* 345 */     this.portNumber = String.valueOf(portNumber);
/*     */   }
/*     */ 
/*     */   public int getPortNumber() {
/* 349 */     if (this.portNumber == null) {
/* 350 */       return 0;
/*     */     }
/* 352 */     return Integer.parseInt(this.portNumber);
/*     */   }
/*     */ 
/*     */   public void setServerName(String serverName) {
/* 356 */     this.serverName = serverName;
/*     */   }
/*     */ 
/*     */   public String getServerName() {
/* 360 */     return this.serverName;
/*     */   }
/*     */ 
/*     */   public void setUser(String user) {
/* 364 */     this.user = user;
/*     */   }
/*     */ 
/*     */   public String getUser() {
/* 368 */     return this.user;
/*     */   }
/*     */ 
/*     */   public void setTds(String tds) {
/* 372 */     this.tdsVersion = tds;
/*     */   }
/*     */ 
/*     */   public String getTds() {
/* 376 */     return this.tdsVersion;
/*     */   }
/*     */ 
/*     */   public void setServerType(int serverType)
/*     */   {
/* 381 */     this.serverType = String.valueOf(serverType);
/*     */   }
/*     */ 
/*     */   public int getServerType() {
/* 385 */     if (this.serverType == null) {
/* 386 */       return 0;
/*     */     }
/* 388 */     return Integer.parseInt(this.serverType);
/*     */   }
/*     */ 
/*     */   public String getDomain() {
/* 392 */     return this.domain;
/*     */   }
/*     */ 
/*     */   public void setDomain(String domain) {
/* 396 */     this.domain = domain;
/*     */   }
/*     */ 
/*     */   public String getUseNTLMV2() {
/* 400 */     return this.useNTLMV2;
/*     */   }
/*     */ 
/*     */   public void setUseNTLMV2(String usentlmv2) {
/* 404 */     this.useNTLMV2 = usentlmv2; }
/*     */ 
/*     */   public String getInstance() {
/* 407 */     return this.instance;
/*     */   }
/*     */ 
/*     */   public void setInstance(String instance) {
/* 411 */     this.instance = instance;
/*     */   }
/*     */ 
/*     */   public boolean getSendStringParametersAsUnicode() {
/* 415 */     return Boolean.valueOf(this.sendStringParametersAsUnicode).booleanValue();
/*     */   }
/*     */ 
/*     */   public void setSendStringParametersAsUnicode(boolean sendStringParametersAsUnicode) {
/* 419 */     this.sendStringParametersAsUnicode = String.valueOf(sendStringParametersAsUnicode);
/*     */   }
/*     */ 
/*     */   public boolean getNamedPipe() {
/* 423 */     return Boolean.valueOf(this.namedPipe).booleanValue();
/*     */   }
/*     */ 
/*     */   public void setNamedPipe(boolean namedPipe) {
/* 427 */     this.namedPipe = String.valueOf(namedPipe);
/*     */   }
/*     */ 
/*     */   public boolean getLastUpdateCount() {
/* 431 */     return Boolean.valueOf(this.lastUpdateCount).booleanValue();
/*     */   }
/*     */ 
/*     */   public void setLastUpdateCount(boolean lastUpdateCount) {
/* 435 */     this.lastUpdateCount = String.valueOf(lastUpdateCount);
/*     */   }
/*     */ 
/*     */   public boolean getXaEmulation() {
/* 439 */     return Boolean.valueOf(this.xaEmulation).booleanValue();
/*     */   }
/*     */ 
/*     */   public void setXaEmulation(boolean xaEmulation) {
/* 443 */     this.xaEmulation = String.valueOf(xaEmulation);
/*     */   }
/*     */ 
/*     */   public String getCharset() {
/* 447 */     return this.charset;
/*     */   }
/*     */ 
/*     */   public void setCharset(String charset) {
/* 451 */     this.charset = charset;
/*     */   }
/*     */ 
/*     */   public String getLanguage() {
/* 455 */     return this.language;
/*     */   }
/*     */ 
/*     */   public void setLanguage(String language) {
/* 459 */     this.language = language;
/*     */   }
/*     */ 
/*     */   public String getMacAddress() {
/* 463 */     return this.macAddress;
/*     */   }
/*     */ 
/*     */   public void setMacAddress(String macAddress) {
/* 467 */     this.macAddress = macAddress;
/*     */   }
/*     */ 
/*     */   public void setPacketSize(int packetSize) {
/* 471 */     this.packetSize = String.valueOf(packetSize);
/*     */   }
/*     */ 
/*     */   public int getPacketSize() {
/* 475 */     if (this.packetSize == null) {
/* 476 */       return 0;
/*     */     }
/* 478 */     return Integer.parseInt(this.packetSize);
/*     */   }
/*     */ 
/*     */   public boolean getTcpNoDelay() {
/* 482 */     return Boolean.valueOf(this.tcpNoDelay).booleanValue();
/*     */   }
/*     */ 
/*     */   public void setTcpNoDelay(boolean tcpNoDelay) {
/* 486 */     this.tcpNoDelay = String.valueOf(tcpNoDelay);
/*     */   }
/*     */ 
/*     */   public void setPrepareSql(int prepareSql) {
/* 490 */     this.prepareSql = String.valueOf(prepareSql);
/*     */   }
/*     */ 
/*     */   public int getPrepareSql() {
/* 494 */     if (this.prepareSql == null) {
/* 495 */       return 0;
/*     */     }
/* 497 */     return Integer.parseInt(this.prepareSql);
/*     */   }
/*     */ 
/*     */   public void setLobBuffer(long lobBuffer) {
/* 501 */     this.lobBuffer = String.valueOf(lobBuffer);
/*     */   }
/*     */ 
/*     */   public long getLobBuffer() {
/* 505 */     if (this.lobBuffer == null) {
/* 506 */       return 0L;
/*     */     }
/* 508 */     return Long.parseLong(this.lobBuffer);
/*     */   }
/*     */ 
/*     */   public void setMaxStatements(int maxStatements) {
/* 512 */     this.maxStatements = String.valueOf(maxStatements);
/*     */   }
/*     */ 
/*     */   public int getMaxStatements() {
/* 516 */     if (this.maxStatements == null) {
/* 517 */       return 0;
/*     */     }
/* 519 */     return Integer.parseInt(this.maxStatements);
/*     */   }
/*     */ 
/*     */   public void setAppName(String appName) {
/* 523 */     this.appName = appName;
/*     */   }
/*     */ 
/*     */   public String getAppName() {
/* 527 */     return this.appName;
/*     */   }
/*     */ 
/*     */   public void setProgName(String progName) {
/* 531 */     this.progName = progName;
/*     */   }
/*     */ 
/*     */   public String getProgName() {
/* 535 */     return this.progName;
/*     */   }
/*     */ 
/*     */   public void setWsid(String wsid) {
/* 539 */     this.wsid = wsid;
/*     */   }
/*     */ 
/*     */   public String getWsid() {
/* 543 */     return this.wsid;
/*     */   }
/*     */ 
/*     */   public void setLogFile(String logFile) {
/* 547 */     this.logFile = logFile;
/*     */   }
/*     */ 
/*     */   public String getLogFile() {
/* 551 */     return this.logFile;
/*     */   }
/*     */ 
/*     */   public void setSsl(String ssl) {
/* 555 */     this.ssl = ssl;
/*     */   }
/*     */ 
/*     */   public String getSsl() {
/* 559 */     return this.ssl;
/*     */   }
/*     */ 
/*     */   public void setBatchSize(int batchSize) {
/* 563 */     this.batchSize = String.valueOf(batchSize);
/*     */   }
/*     */ 
/*     */   public int getBatchSize() {
/* 567 */     if (this.batchSize == null) {
/* 568 */       return 0;
/*     */     }
/* 570 */     return Integer.parseInt(this.batchSize);
/*     */   }
/*     */ 
/*     */   public String getBufferDir() {
/* 574 */     if (this.bufferDir == null) {
/* 575 */       return System.getProperty("java.io.tmpdir");
/*     */     }
/*     */ 
/* 578 */     return this.bufferDir;
/*     */   }
/*     */ 
/*     */   public void setBufferDir(String bufferDir) {
/* 582 */     this.bufferDir = bufferDir;
/*     */   }
/*     */ 
/*     */   public int getBufferMaxMemory() {
/* 586 */     if (this.bufferMaxMemory == null) {
/* 587 */       return 0;
/*     */     }
/* 589 */     return Integer.parseInt(this.bufferMaxMemory);
/*     */   }
/*     */ 
/*     */   public void setBufferMaxMemory(int bufferMaxMemory) {
/* 593 */     this.bufferMaxMemory = String.valueOf(bufferMaxMemory);
/*     */   }
/*     */ 
/*     */   public int getBufferMinPackets() {
/* 597 */     if (this.bufferMinPackets == null) {
/* 598 */       return 0;
/*     */     }
/* 600 */     return Integer.parseInt(this.bufferMinPackets);
/*     */   }
/*     */ 
/*     */   public void setBufferMinPackets(int bufferMinPackets) {
/* 604 */     this.bufferMinPackets = String.valueOf(bufferMinPackets);
/*     */   }
/*     */ 
/*     */   public boolean getCacheMetaData() {
/* 608 */     return Boolean.valueOf(this.cacheMetaData).booleanValue();
/*     */   }
/*     */ 
/*     */   public void setCacheMetaData(boolean cacheMetaData) {
/* 612 */     this.cacheMetaData = String.valueOf(cacheMetaData);
/*     */   }
/*     */ 
/*     */   public boolean getUseCursors() {
/* 616 */     return Boolean.valueOf(this.useCursors).booleanValue();
/*     */   }
/*     */ 
/*     */   public void setUseCursors(boolean useCursors) {
/* 620 */     this.useCursors = String.valueOf(useCursors);
/*     */   }
/*     */ 
/*     */   public boolean getUseLOBs() {
/* 624 */     return Boolean.valueOf(this.useLOBs).booleanValue();
/*     */   }
/*     */ 
/*     */   public void setUseLOBs(boolean useLOBs) {
/* 628 */     this.useLOBs = String.valueOf(useLOBs);
/*     */   }
/*     */ 
/*     */   public String getBindAddress() {
/* 632 */     return this.bindAddress;
/*     */   }
/*     */ 
/*     */   public void setBindAddress(String bindAddress) {
/* 636 */     this.bindAddress = bindAddress;
/*     */   }
/*     */ 
/*     */   public boolean getUseJCIFS() {
/* 640 */     return Boolean.valueOf(this.useJCIFS).booleanValue();
/*     */   }
/*     */ 
/*     */   public void setUseJCIFS(boolean useJCIFS) {
/* 644 */     this.useJCIFS = String.valueOf(useJCIFS);
/*     */   }
/*     */ 
/*     */   private void addNonNullProperties(Properties props, String user, String password) {
/* 648 */     props.setProperty(Messages.get("prop.servername"), this.serverName);
/* 649 */     if (this.serverType != null) {
/* 650 */       props.setProperty(Messages.get("prop.servertype"), this.serverType);
/*     */     }
/* 652 */     if (this.portNumber != null) {
/* 653 */       props.setProperty(Messages.get("prop.portnumber"), this.portNumber);
/*     */     }
/* 655 */     if (this.databaseName != null) {
/* 656 */       props.setProperty(Messages.get("prop.databasename"), this.databaseName);
/*     */     }
/* 658 */     if (this.tdsVersion != null) {
/* 659 */       props.setProperty(Messages.get("prop.tds"), this.tdsVersion);
/*     */     }
/* 661 */     if (this.charset != null) {
/* 662 */       props.setProperty(Messages.get("prop.charset"), this.charset);
/*     */     }
/* 664 */     if (this.language != null) {
/* 665 */       props.setProperty(Messages.get("prop.language"), this.language);
/*     */     }
/* 667 */     if (this.domain != null) {
/* 668 */       props.setProperty(Messages.get("prop.domain"), this.domain);
/*     */     }
/* 670 */     if (this.useNTLMV2 != null) {
/* 671 */       props.setProperty(Messages.get("prop.usentlmv2"), this.useNTLMV2);
/*     */     }
/* 673 */     if (this.instance != null) {
/* 674 */       props.setProperty(Messages.get("prop.instance"), this.instance);
/*     */     }
/* 676 */     if (this.lastUpdateCount != null) {
/* 677 */       props.setProperty(Messages.get("prop.lastupdatecount"), this.lastUpdateCount);
/*     */     }
/* 679 */     if (this.sendStringParametersAsUnicode != null) {
/* 680 */       props.setProperty(Messages.get("prop.useunicode"), this.sendStringParametersAsUnicode);
/*     */     }
/* 682 */     if (this.namedPipe != null) {
/* 683 */       props.setProperty(Messages.get("prop.namedpipe"), this.namedPipe);
/*     */     }
/* 685 */     if (this.macAddress != null) {
/* 686 */       props.setProperty(Messages.get("prop.macaddress"), this.macAddress);
/*     */     }
/* 688 */     if (this.prepareSql != null) {
/* 689 */       props.setProperty(Messages.get("prop.preparesql"), this.prepareSql);
/*     */     }
/* 691 */     if (this.packetSize != null) {
/* 692 */       props.setProperty(Messages.get("prop.packetsize"), this.packetSize);
/*     */     }
/* 694 */     if (this.tcpNoDelay != null) {
/* 695 */       props.setProperty(Messages.get("prop.tcpnodelay"), this.tcpNoDelay);
/*     */     }
/* 697 */     if (this.xaEmulation != null) {
/* 698 */       props.setProperty(Messages.get("prop.xaemulation"), this.xaEmulation);
/*     */     }
/* 700 */     if (user != null) {
/* 701 */       props.setProperty(Messages.get("prop.user"), user);
/*     */     }
/* 703 */     if (password != null) {
/* 704 */       props.setProperty(Messages.get("prop.password"), password);
/*     */     }
/* 706 */     if (this.loginTimeout != null) {
/* 707 */       props.setProperty(Messages.get("prop.logintimeout"), this.loginTimeout);
/*     */     }
/* 709 */     if (this.socketTimeout != null) {
/* 710 */       props.setProperty(Messages.get("prop.sotimeout"), this.socketTimeout);
/*     */     }
/* 712 */     if (this.socketKeepAlive != null) {
/* 713 */       props.setProperty(Messages.get("prop.sokeepalive"), this.socketKeepAlive);
/*     */     }
/* 715 */     if (this.processId != null) {
/* 716 */       props.setProperty(Messages.get("prop.processid"), this.processId);
/*     */     }
/* 718 */     if (this.lobBuffer != null) {
/* 719 */       props.setProperty(Messages.get("prop.lobbuffer"), this.lobBuffer);
/*     */     }
/* 721 */     if (this.maxStatements != null) {
/* 722 */       props.setProperty(Messages.get("prop.maxstatements"), this.maxStatements);
/*     */     }
/* 724 */     if (this.appName != null) {
/* 725 */       props.setProperty(Messages.get("prop.appname"), this.appName);
/*     */     }
/* 727 */     if (this.progName != null) {
/* 728 */       props.setProperty(Messages.get("prop.progname"), this.progName);
/*     */     }
/* 730 */     if (this.wsid != null) {
/* 731 */       props.setProperty(Messages.get("prop.wsid"), this.wsid);
/*     */     }
/* 733 */     if (this.ssl != null) {
/* 734 */       props.setProperty(Messages.get("prop.ssl"), this.ssl);
/*     */     }
/* 736 */     if (this.batchSize != null) {
/* 737 */       props.setProperty(Messages.get("prop.batchsize"), this.batchSize);
/*     */     }
/* 739 */     if (this.bufferDir != null) {
/* 740 */       props.setProperty(Messages.get("prop.bufferdir"), this.bufferDir);
/*     */     }
/* 742 */     if (this.bufferMaxMemory != null) {
/* 743 */       props.setProperty(Messages.get("prop.buffermaxmemory"), this.bufferMaxMemory);
/*     */     }
/* 745 */     if (this.bufferMinPackets != null) {
/* 746 */       props.setProperty(Messages.get("prop.bufferminpackets"), this.bufferMinPackets);
/*     */     }
/* 748 */     if (this.cacheMetaData != null) {
/* 749 */       props.setProperty(Messages.get("prop.cachemetadata"), this.cacheMetaData);
/*     */     }
/* 751 */     if (this.useCursors != null) {
/* 752 */       props.setProperty(Messages.get("prop.usecursors"), this.useCursors);
/*     */     }
/* 754 */     if (this.useLOBs != null) {
/* 755 */       props.setProperty(Messages.get("prop.uselobs"), this.useLOBs);
/*     */     }
/* 757 */     if (this.bindAddress != null) {
/* 758 */       props.setProperty(Messages.get("prop.bindaddress"), this.bindAddress);
/*     */     }
/* 760 */     if (this.useJCIFS != null)
/* 761 */       props.setProperty(Messages.get("prop.usejcifs"), this.useJCIFS);
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbcx.JtdsDataSource
 * JD-Core Version:    0.5.3
 */