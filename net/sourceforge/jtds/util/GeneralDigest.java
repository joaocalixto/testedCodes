/*     */ package net.sourceforge.jtds.util;
/*     */ 
/*     */ public abstract class GeneralDigest
/*     */ {
/*     */   private byte[] xBuf;
/*     */   private int xBufOff;
/*     */   private long byteCount;
/*     */ 
/*     */   protected GeneralDigest()
/*     */   {
/*  43 */     this.xBuf = new byte[4];
/*  44 */     this.xBufOff = 0;
/*     */   }
/*     */ 
/*     */   protected GeneralDigest(GeneralDigest t)
/*     */   {
/*  54 */     this.xBuf = new byte[t.xBuf.length];
/*  55 */     System.arraycopy(t.xBuf, 0, this.xBuf, 0, t.xBuf.length);
/*     */ 
/*  57 */     this.xBufOff = t.xBufOff;
/*  58 */     this.byteCount = t.byteCount;
/*     */   }
/*     */ 
/*     */   public void update(byte in)
/*     */   {
/*  64 */     this.xBuf[(this.xBufOff++)] = in;
/*     */ 
/*  66 */     if (this.xBufOff == this.xBuf.length)
/*     */     {
/*  68 */       processWord(this.xBuf, 0);
/*  69 */       this.xBufOff = 0;
/*     */     }
/*     */ 
/*  72 */     this.byteCount += 1L;
/*     */   }
/*     */ 
/*     */   public void update(byte[] in, int inOff, int len)
/*     */   {
/*  83 */     while ((this.xBufOff != 0) && (len > 0))
/*     */     {
/*  85 */       update(in[inOff]);
/*     */ 
/*  87 */       ++inOff;
/*  88 */       --len;
/*     */     }
/*     */ 
/*  94 */     while (len > this.xBuf.length)
/*     */     {
/*  96 */       processWord(in, inOff);
/*     */ 
/*  98 */       inOff += this.xBuf.length;
/*  99 */       len -= this.xBuf.length;
/* 100 */       this.byteCount += this.xBuf.length;
/*     */     }
/*     */ 
/* 106 */     while (len > 0)
/*     */     {
/* 108 */       update(in[inOff]);
/*     */ 
/* 110 */       ++inOff;
/* 111 */       --len;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void finish()
/*     */   {
/* 117 */     long bitLength = this.byteCount << 3;
/*     */ 
/* 122 */     update(-128);
/*     */ 
/* 124 */     while (this.xBufOff != 0)
/*     */     {
/* 126 */       update(0);
/*     */     }
/*     */ 
/* 129 */     processLength(bitLength);
/*     */ 
/* 131 */     processBlock();
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 136 */     this.byteCount = 0L;
/*     */ 
/* 138 */     this.xBufOff = 0;
/* 139 */     for (int i = 0; i < this.xBuf.length; ++i)
/* 140 */       this.xBuf[i] = 0;
/*     */   }
/*     */ 
/*     */   protected abstract void processWord(byte[] paramArrayOfByte, int paramInt);
/*     */ 
/*     */   protected abstract void processLength(long paramLong);
/*     */ 
/*     */   protected abstract void processBlock();
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.util.GeneralDigest
 * JD-Core Version:    0.5.3
 */