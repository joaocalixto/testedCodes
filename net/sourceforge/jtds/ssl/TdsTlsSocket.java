/*     */ package net.sourceforge.jtds.ssl;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.net.Socket;
/*     */ import java.net.SocketException;
/*     */ 
/*     */ class TdsTlsSocket extends Socket
/*     */ {
/*     */   private final Socket delegate;
/*     */   private final InputStream istm;
/*     */   private final OutputStream ostm;
/*     */ 
/*     */   TdsTlsSocket(Socket delegate)
/*     */     throws IOException
/*     */   {
/*  44 */     this.delegate = delegate;
/*  45 */     this.istm = new TdsTlsInputStream(delegate.getInputStream());
/*  46 */     this.ostm = new TdsTlsOutputStream(delegate.getOutputStream());
/*     */   }
/*     */ 
/*     */   public synchronized void close()
/*     */     throws IOException
/*     */   {
/*     */   }
/*     */ 
/*     */   public InputStream getInputStream()
/*     */     throws IOException
/*     */   {
/*  64 */     return this.istm;
/*     */   }
/*     */ 
/*     */   public OutputStream getOutputStream()
/*     */     throws IOException
/*     */   {
/*  73 */     return this.ostm;
/*     */   }
/*     */ 
/*     */   public boolean isConnected()
/*     */   {
/*  82 */     return true;
/*     */   }
/*     */ 
/*     */   public synchronized void setSoTimeout(int timeout)
/*     */     throws SocketException
/*     */   {
/*  91 */     this.delegate.setSoTimeout(timeout);
/*     */   }
/*     */ 
/*     */   public synchronized void setKeepAlive(boolean keepAlive)
/*     */     throws SocketException
/*     */   {
/* 100 */     this.delegate.setKeepAlive(keepAlive);
/*     */   }
/*     */ 
/*     */   public void setTcpNoDelay(boolean on)
/*     */     throws SocketException
/*     */   {
/* 109 */     this.delegate.setTcpNoDelay(on);
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.ssl.TdsTlsSocket
 * JD-Core Version:    0.5.3
 */