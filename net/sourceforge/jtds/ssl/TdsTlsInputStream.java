/*     */ package net.sourceforge.jtds.ssl;
/*     */ 
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.FilterInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ class TdsTlsInputStream extends FilterInputStream
/*     */ {
/*     */   int bytesOutstanding;
/*  44 */   final byte[] readBuffer = new byte[6144];
/*     */   InputStream bufferStream;
/*     */   boolean pureSSL;
/*     */ 
/*     */   public TdsTlsInputStream(InputStream in)
/*     */   {
/*  57 */     super(in);
/*     */   }
/*     */ 
/*     */   public int read(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/*  71 */     if ((this.pureSSL) && (this.bufferStream == null)) {
/*  72 */       return this.in.read(b, off, len);
/*     */     }
/*     */ 
/*  77 */     if ((!(this.pureSSL)) && (this.bufferStream == null)) {
/*  78 */       primeBuffer();
/*     */     }
/*     */ 
/*  82 */     int ret = this.bufferStream.read(b, off, len);
/*  83 */     this.bytesOutstanding -= ((ret < 0) ? 0 : ret);
/*  84 */     if (this.bytesOutstanding == 0)
/*     */     {
/*  87 */       this.bufferStream = null;
/*     */     }
/*     */ 
/*  90 */     return ret;
/*     */   }
/*     */ 
/*     */   private void primeBuffer()
/*     */     throws IOException
/*     */   {
/* 100 */     readFully(this.readBuffer, 0, 5);
/*     */     int len;
/* 102 */     if ((this.readBuffer[0] == 4) || (this.readBuffer[0] == 18))
/*     */     {
/* 104 */       len = (this.readBuffer[2] & 0xFF) << 8 | this.readBuffer[3] & 0xFF;
/*     */ 
/* 106 */       readFully(this.readBuffer, 5, 3);
/* 107 */       len -= 8;
/* 108 */       readFully(this.readBuffer, 0, len);
/*     */     } else {
/* 110 */       len = (this.readBuffer[3] & 0xFF) << 8 | this.readBuffer[4] & 0xFF;
/* 111 */       readFully(this.readBuffer, 5, len - 5);
/* 112 */       this.pureSSL = true;
/*     */     }
/*     */ 
/* 115 */     this.bufferStream = new ByteArrayInputStream(this.readBuffer, 0, len);
/* 116 */     this.bytesOutstanding = len;
/*     */   }
/*     */ 
/*     */   private void readFully(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/* 130 */     int res = 0;
/* 131 */     while ((len > 0) && ((res = this.in.read(b, off, len)) >= 0)) {
/* 132 */       off += res;
/* 133 */       len -= res;
/*     */     }
/*     */ 
/* 136 */     if (res < 0)
/* 137 */       throw new IOException();
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.ssl.TdsTlsInputStream
 * JD-Core Version:    0.5.3
 */