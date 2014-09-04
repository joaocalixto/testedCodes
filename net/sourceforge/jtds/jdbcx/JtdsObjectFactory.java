/*     */ package net.sourceforge.jtds.jdbcx;
/*     */ 
/*     */ import java.util.Hashtable;
/*     */ import javax.naming.Context;
/*     */ import javax.naming.Name;
/*     */ import javax.naming.RefAddr;
/*     */ import javax.naming.Reference;
/*     */ import javax.naming.spi.ObjectFactory;
/*     */ import net.sourceforge.jtds.jdbc.Messages;
/*     */ 
/*     */ public class JtdsObjectFactory
/*     */   implements ObjectFactory
/*     */ {
/*     */   public Object getObjectInstance(Object refObj, Name name, Context nameCtx, Hashtable env)
/*     */     throws Exception
/*     */   {
/*  42 */     Reference ref = (Reference)refObj;
/*     */ 
/*  44 */     if (ref.getClassName().equals(JtdsDataSource.class.getName())) {
/*  45 */       JtdsDataSource ds = new JtdsDataSource();
/*     */ 
/*  47 */       ds.setServerName((String)ref.get(Messages.get("prop.servername")).getContent());
/*  48 */       Object portNumber = ref.get(Messages.get("prop.portnumber")).getContent();
/*  49 */       if (portNumber != null) {
/*  50 */         ds.setPortNumber(Integer.parseInt((String)portNumber));
/*     */       }
/*  52 */       ds.setDatabaseName((String)ref.get(Messages.get("prop.databasename")).getContent());
/*  53 */       ds.setUser((String)ref.get(Messages.get("prop.user")).getContent());
/*  54 */       ds.setPassword((String)ref.get(Messages.get("prop.password")).getContent());
/*  55 */       ds.setCharset((String)ref.get(Messages.get("prop.charset")).getContent());
/*  56 */       ds.setLanguage((String)ref.get(Messages.get("prop.language")).getContent());
/*  57 */       ds.setTds((String)ref.get(Messages.get("prop.tds")).getContent());
/*  58 */       ds.setBindAddress((String)ref.get(Messages.get("prop.bindaddress")).getContent());
/*  59 */       Object serverType = ref.get(Messages.get("prop.servertype")).getContent();
/*  60 */       if (serverType != null) {
/*  61 */         ds.setServerType(Integer.parseInt((String)serverType));
/*     */       }
/*  63 */       ds.setDomain((String)ref.get(Messages.get("prop.domain")).getContent());
/*  64 */       ds.setUseNTLMV2((String)ref.get(Messages.get("prop.usentlmv2")).getContent());
/*  65 */       ds.setInstance((String)ref.get(Messages.get("prop.instance")).getContent());
/*  66 */       Object lastUpdateCount = ref.get(Messages.get("prop.lastupdatecount")).getContent();
/*  67 */       if (lastUpdateCount != null) {
/*  68 */         ds.setLastUpdateCount("true".equals(lastUpdateCount));
/*     */       }
/*  70 */       Object sendStringParametersAsUnicode = ref.get(Messages.get("prop.useunicode")).getContent();
/*     */ 
/*  72 */       if (sendStringParametersAsUnicode != null) {
/*  73 */         ds.setSendStringParametersAsUnicode("true".equals(sendStringParametersAsUnicode));
/*     */       }
/*  75 */       Object namedPipe = ref.get(Messages.get("prop.namedpipe")).getContent();
/*  76 */       if (namedPipe != null) {
/*  77 */         ds.setNamedPipe("true".equals(namedPipe));
/*     */       }
/*  79 */       ds.setMacAddress((String)ref.get(Messages.get("prop.macaddress")).getContent());
/*  80 */       Object maxStatements = ref.get(Messages.get("prop.maxstatements")).getContent();
/*  81 */       if (maxStatements != null) {
/*  82 */         ds.setMaxStatements(Integer.parseInt((String)maxStatements));
/*     */       }
/*  84 */       Object packetSize = ref.get(Messages.get("prop.packetsize")).getContent();
/*  85 */       if (packetSize != null) {
/*  86 */         ds.setPacketSize(Integer.parseInt((String)packetSize));
/*     */       }
/*  88 */       Object prepareSql = ref.get(Messages.get("prop.preparesql")).getContent();
/*  89 */       if (prepareSql != null) {
/*  90 */         ds.setPrepareSql(Integer.parseInt((String)prepareSql));
/*     */       }
/*  92 */       Object lobBuffer = ref.get(Messages.get("prop.lobbuffer")).getContent();
/*  93 */       if (lobBuffer != null) {
/*  94 */         ds.setLobBuffer(Long.parseLong((String)lobBuffer));
/*     */       }
/*  96 */       Object loginTimeout = ref.get(Messages.get("prop.logintimeout")).getContent();
/*  97 */       if (loginTimeout != null) {
/*  98 */         ds.setLoginTimeout(Integer.parseInt((String)loginTimeout));
/*     */       }
/* 100 */       Object socketTimeout = ref.get(Messages.get("prop.sotimeout")).getContent();
/* 101 */       if (socketTimeout != null) {
/* 102 */         ds.setSocketTimeout(Integer.parseInt((String)socketTimeout));
/*     */       }
/* 104 */       Object socketKeepAlive = ref.get(Messages.get("prop.sokeepalive")).getContent();
/* 105 */       if (socketKeepAlive != null) {
/* 106 */         ds.setSocketKeepAlive("true".equalsIgnoreCase((String)socketKeepAlive));
/*     */       }
/* 108 */       Object processId = ref.get(Messages.get("prop.processid")).getContent();
/* 109 */       if (processId != null) {
/* 110 */         ds.setProcessId((String)processId);
/*     */       }
/* 112 */       ds.setAppName((String)ref.get(Messages.get("prop.appname")).getContent());
/* 113 */       ds.setProgName((String)ref.get(Messages.get("prop.progname")).getContent());
/* 114 */       ds.setWsid((String)ref.get(Messages.get("prop.wsid")).getContent());
/* 115 */       Object tcpNoDelay = ref.get(Messages.get("prop.tcpnodelay")).getContent();
/* 116 */       if (tcpNoDelay != null) {
/* 117 */         ds.setTcpNoDelay("true".equals(tcpNoDelay));
/*     */       }
/* 119 */       Object xaEmulation = ref.get(Messages.get("prop.xaemulation")).getContent();
/* 120 */       if (xaEmulation != null) {
/* 121 */         ds.setXaEmulation("true".equals(xaEmulation));
/*     */       }
/* 123 */       ds.setLogFile((String)ref.get(Messages.get("prop.logfile")).getContent());
/* 124 */       ds.setSsl((String)ref.get(Messages.get("prop.ssl")).getContent());
/* 125 */       Object batchSize = ref.get(Messages.get("prop.batchsize")).getContent();
/* 126 */       if (batchSize != null) {
/* 127 */         ds.setBatchSize(Integer.parseInt((String)batchSize));
/*     */       }
/* 129 */       Object bufferDir = ref.get(Messages.get("prop.bufferdir")).getContent();
/* 130 */       if (bufferDir != null) {
/* 131 */         ds.setBufferDir((String)bufferDir);
/*     */       }
/* 133 */       Object bufferMaxMemory = ref.get(Messages.get("prop.buffermaxmemory")).getContent();
/* 134 */       if (bufferMaxMemory != null) {
/* 135 */         ds.setBufferMaxMemory(Integer.parseInt((String)bufferMaxMemory));
/*     */       }
/* 137 */       Object bufferMinPackets = ref.get(Messages.get("prop.bufferminpackets")).getContent();
/* 138 */       if (bufferMinPackets != null) {
/* 139 */         ds.setBufferMinPackets(Integer.parseInt((String)bufferMinPackets));
/*     */       }
/* 141 */       Object cacheMetaData = ref.get(Messages.get("prop.cachemetadata")).getContent();
/* 142 */       if (cacheMetaData != null) {
/* 143 */         ds.setCacheMetaData("true".equals(cacheMetaData));
/*     */       }
/* 145 */       Object useCursors = ref.get(Messages.get("prop.usecursors")).getContent();
/* 146 */       if (useCursors != null) {
/* 147 */         ds.setUseCursors("true".equals(useCursors));
/*     */       }
/* 149 */       Object useJCIFS = ref.get(Messages.get("prop.usejcifs")).getContent();
/* 150 */       if (useJCIFS != null) {
/* 151 */         ds.setUseJCIFS("true".equals(useJCIFS));
/*     */       }
/* 153 */       Object useLOBs = ref.get(Messages.get("prop.uselobs")).getContent();
/* 154 */       if (useLOBs != null) {
/* 155 */         ds.setUseLOBs("true".equals(useLOBs));
/*     */       }
/*     */ 
/* 158 */       ds.setDescription((String)ref.get("description").getContent());
/*     */ 
/* 160 */       return ds;
/*     */     }
/*     */ 
/* 163 */     return null;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbcx.JtdsObjectFactory
 * JD-Core Version:    0.5.3
 */