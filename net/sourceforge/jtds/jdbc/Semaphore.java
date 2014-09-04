/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ public class Semaphore
/*     */ {
/*     */   protected long permits;
/*     */ 
/*     */   public Semaphore(long initialPermits)
/*     */   {
/*  44 */     this.permits = initialPermits;
/*     */   }
/*     */ 
/*     */   public void acquire()
/*     */     throws InterruptedException
/*     */   {
/*  51 */     if (Thread.interrupted()) {
/*  52 */       throw new InterruptedException();
/*     */     }
/*     */ 
/*  55 */     synchronized (this) {
/*     */       try {
/*  57 */         while (this.permits <= 0L) {
/*  58 */           super.wait();
/*     */         }
/*  60 */         this.permits -= 1L;
/*     */       } catch (InterruptedException ex) {
/*  62 */         super.notify();
/*  63 */         throw ex;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean attempt(long msecs)
/*     */     throws InterruptedException
/*     */   {
/*  72 */     if (Thread.interrupted()) {
/*  73 */       throw new InterruptedException();
/*     */     }
/*     */ 
/*  76 */     synchronized (this) {
/*  77 */       if (this.permits > 0L) {
/*  78 */         this.permits -= 1L;
/*  79 */         return true; }
/*  80 */       if (msecs <= 0L)
/*  81 */         return false;
/*     */       try
/*     */       {
/*  84 */         long startTime = System.currentTimeMillis();
/*  85 */         long waitTime = msecs;
/*     */         do
/*     */         {
/*  88 */           super.wait(waitTime);
/*     */ 
/*  90 */           if (this.permits > 0L) {
/*  91 */             this.permits -= 1L;
/*  92 */             monitorexit; return true;
/*     */           }
/*  94 */           waitTime = msecs - (System.currentTimeMillis() - startTime); }
/*  95 */         while (waitTime > 0L);
/*  96 */         return false;
/*     */       }
/*     */       catch (InterruptedException ex)
/*     */       {
/* 101 */         super.notify();
/* 102 */         throw ex;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void release()
/*     */   {
/* 112 */     this.permits += 1L;
/* 113 */     super.notify();
/*     */   }
/*     */ 
/*     */   public synchronized void release(long n)
/*     */   {
/* 127 */     if (n < 0L) {
/* 128 */       throw new IllegalArgumentException("Negative argument");
/*     */     }
/*     */ 
/* 131 */     this.permits += n;
/* 132 */     for (long i = 0L; i < n; i += 1L)
/* 133 */       super.notify();
/*     */   }
/*     */ 
/*     */   public synchronized long permits()
/*     */   {
/* 142 */     return this.permits;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.Semaphore
 * JD-Core Version:    0.5.3
 */