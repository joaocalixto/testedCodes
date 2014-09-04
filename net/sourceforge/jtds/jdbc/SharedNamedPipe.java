/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ import jcifs.Config;
/*     */ import jcifs.smb.NtlmPasswordAuthentication;
/*     */ import jcifs.smb.SmbNamedPipe;
/*     */ 
/*     */ public class SharedNamedPipe extends SharedSocket
/*     */ {
/*     */   private SmbNamedPipe pipe;
/*     */ 
/*     */   public SharedNamedPipe(ConnectionJDBC2 connection)
/*     */     throws IOException
/*     */   {
/*  55 */     super(connection.getBufferDir(), connection.getTdsVersion(), connection.getServerType());
/*     */ 
/*  58 */     int timeout = connection.getSocketTimeout() * 1000;
/*  59 */     String val = String.valueOf((timeout > 0) ? timeout : 2147483647);
/*  60 */     Config.setProperty("jcifs.smb.client.responseTimeout", val);
/*  61 */     Config.setProperty("jcifs.smb.client.soTimeout", val);
/*     */ 
/*  63 */     NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(connection.getDomainName(), connection.getUser(), connection.getPassword());
/*     */ 
/*  66 */     StringBuffer url = new StringBuffer(32);
/*     */ 
/*  68 */     url.append("smb://");
/*  69 */     url.append(connection.getServerName());
/*  70 */     url.append("/IPC$");
/*     */ 
/*  72 */     String instanceName = connection.getInstanceName();
/*  73 */     if ((instanceName != null) && (instanceName.length() != 0)) {
/*  74 */       url.append("/MSSQL$");
/*  75 */       url.append(instanceName);
/*     */     }
/*     */ 
/*  78 */     String namedPipePath = DefaultProperties.getNamedPipePath(connection.getServerType());
/*  79 */     url.append(namedPipePath);
/*     */ 
/*  81 */     setPipe(new SmbNamedPipe(url.toString(), 3, auth));
/*     */ 
/*  83 */     setOut(new DataOutputStream(getPipe().getNamedPipeOutputStream()));
/*     */ 
/*  85 */     int bufferSize = Support.calculateNamedPipeBufferSize(connection.getTdsVersion(), connection.getPacketSize());
/*     */ 
/*  87 */     setIn(new DataInputStream(new BufferedInputStream(getPipe().getNamedPipeInputStream(), bufferSize)));
/*     */   }
/*     */ 
/*     */   boolean isConnected()
/*     */   {
/*  98 */     return (getPipe() != null);
/*     */   }
/*     */ 
/*     */   void close()
/*     */     throws IOException
/*     */   {
/* 105 */     super.close();
/* 106 */     getOut().close();
/* 107 */     getIn().close();
/*     */   }
/*     */ 
/*     */   void forceClose()
/*     */   {
/*     */     try
/*     */     {
/* 118 */       getOut().close();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */     finally {
/* 124 */       setOut(null);
/*     */     }
/*     */     try
/*     */     {
/* 128 */       getIn().close();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */     finally {
/* 134 */       setIn(null);
/*     */     }
/*     */ 
/* 137 */     setPipe(null);
/*     */   }
/*     */ 
/*     */   private SmbNamedPipe getPipe()
/*     */   {
/* 147 */     return this.pipe;
/*     */   }
/*     */ 
/*     */   private void setPipe(SmbNamedPipe pipe)
/*     */   {
/* 157 */     this.pipe = pipe;
/*     */   }
/*     */ 
/*     */   protected void setTimeout(int timeout)
/*     */   {
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.SharedNamedPipe
 * JD-Core Version:    0.5.3
 */