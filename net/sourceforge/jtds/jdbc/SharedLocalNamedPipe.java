/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedOutputStream;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.RandomAccessFile;
/*     */ 
/*     */ public class SharedLocalNamedPipe extends SharedSocket
/*     */ {
/*     */   RandomAccessFile pipe;
/*     */ 
/*     */   public SharedLocalNamedPipe(ConnectionJDBC2 connection)
/*     */     throws IOException
/*     */   {
/*  42 */     super(connection.getBufferDir(), connection.getTdsVersion(), connection.getServerType());
/*     */ 
/*  44 */     String serverName = connection.getServerName();
/*  45 */     String instanceName = connection.getInstanceName();
/*     */ 
/*  47 */     StringBuffer pipeName = new StringBuffer(64);
/*  48 */     pipeName.append("\\\\");
/*  49 */     if ((serverName == null) || (serverName.length() == 0))
/*  50 */       pipeName.append('.');
/*     */     else {
/*  52 */       pipeName.append(serverName);
/*     */     }
/*  54 */     pipeName.append("\\pipe");
/*  55 */     if ((instanceName != null) && (instanceName.length() != 0)) {
/*  56 */       pipeName.append("\\MSSQL$").append(instanceName);
/*     */     }
/*  58 */     String namedPipePath = DefaultProperties.getNamedPipePath(connection.getServerType());
/*  59 */     pipeName.append(namedPipePath.replace('/', '\\'));
/*     */ 
/*  61 */     this.pipe = new RandomAccessFile(pipeName.toString(), "rw");
/*     */ 
/*  63 */     int bufferSize = Support.calculateNamedPipeBufferSize(connection.getTdsVersion(), connection.getPacketSize());
/*     */ 
/*  65 */     setOut(new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.pipe.getFD()), bufferSize)));
/*     */ 
/*  68 */     setIn(new DataInputStream(new BufferedInputStream(new FileInputStream(this.pipe.getFD()), bufferSize)));
/*     */   }
/*     */ 
/*     */   boolean isConnected()
/*     */   {
/*  79 */     return (this.pipe != null);
/*     */   }
/*     */ 
/*     */   byte[] sendNetPacket(int streamId, byte[] buffer)
/*     */     throws IOException
/*     */   {
/*  92 */     byte[] ret = super.sendNetPacket(streamId, buffer);
/*  93 */     getOut().flush();
/*  94 */     return ret;
/*     */   }
/*     */ 
/*     */   void close()
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/* 103 */       super.close();
/*     */ 
/* 105 */       getOut().close();
/* 106 */       setOut(null);
/* 107 */       getIn().close();
/* 108 */       setIn(null);
/*     */ 
/* 110 */       if (this.pipe != null)
/* 111 */         this.pipe.close();
/*     */     }
/*     */     finally {
/* 114 */       this.pipe = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   void forceClose()
/*     */   {
/*     */     try
/*     */     {
/* 125 */       getOut().close();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally {
/* 131 */       setOut(null);
/*     */     }
/*     */     try
/*     */     {
/* 135 */       getIn().close();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally {
/* 141 */       setIn(null);
/*     */     }
/*     */     try
/*     */     {
/* 145 */       if (this.pipe != null)
/* 146 */         this.pipe.close();
/*     */     } catch (IOException ex) {
/*     */     }
/*     */     finally {
/* 150 */       this.pipe = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void setTimeout(int timeout)
/*     */   {
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.SharedLocalNamedPipe
 * JD-Core Version:    0.5.3
 */