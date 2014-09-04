/*     */ package net.sourceforge.jtds.util;
/*     */ 
/*     */ import java.util.LinkedList;
/*     */ import java.util.ListIterator;
/*     */ 
/*     */ public class TimerThread extends Thread
/*     */ {
/*     */   private static TimerThread instance;
/*  79 */   private final LinkedList timerList = new LinkedList();
/*     */   private long nextTimeout;
/*     */ 
/*     */   public static synchronized TimerThread getInstance()
/*     */   {
/*  87 */     if (instance == null) {
/*  88 */       instance = new TimerThread();
/*  89 */       instance.start();
/*     */     }
/*  91 */     return instance;
/*     */   }
/*     */ 
/*     */   public TimerThread()
/*     */   {
/*  99 */     super("jTDS TimerThread");
/*     */ 
/* 101 */     setDaemon(true);
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 108 */     synchronized (this.timerList) {
/* 109 */       boolean run = true;
/* 110 */       while (run)
/*     */         try
/*     */         {
/* 113 */           while (((ms = this.nextTimeout - System.currentTimeMillis()) > 0L) || (this.nextTimeout == 0L))
/*     */           {
/*     */             long ms;
/* 115 */             this.timerList.wait((this.nextTimeout == 0L) ? 0L : ms);
/*     */           }
/*     */ 
/* 119 */           long time = System.currentTimeMillis();
/* 120 */           while (!(this.timerList.isEmpty()))
/*     */           {
/* 123 */             TimerRequest t = (TimerRequest)this.timerList.getFirst();
/* 124 */             if (t.time > time) {
/*     */               break;
/*     */             }
/*     */ 
/* 128 */             t.target.timerExpired();
/*     */ 
/* 130 */             this.timerList.removeFirst();
/*     */           }
/*     */ 
/* 134 */           updateNextTimeout();
/*     */         }
/*     */         catch (InterruptedException e) {
/* 137 */           run = false;
/* 138 */           this.timerList.clear();
/*     */         }
/*     */     }
/*     */   }
/*     */ 
/*     */   public Object setTimer(int timeout, TimerListener l)
/*     */   {
/* 158 */     TimerRequest t = new TimerRequest(timeout, l);
/*     */ 
/* 160 */     synchronized (this.timerList) {
/* 161 */       if (this.timerList.isEmpty())
/*     */       {
/* 163 */         this.timerList.add(t);
/*     */       }
/*     */       else {
/* 166 */         TimerRequest crt = (TimerRequest)this.timerList.getLast();
/* 167 */         if (t.time >= crt.time) {
/* 168 */           this.timerList.addLast(t);
/*     */         }
/*     */         else {
/* 171 */           ListIterator li = this.timerList.listIterator();
/*     */           do { if (!(li.hasNext())) break label139;
/* 172 */             crt = (TimerRequest)li.next(); }
/* 173 */           while (t.time >= crt.time);
/* 174 */           li.previous();
/* 175 */           li.add(t);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 183 */       if (this.timerList.getFirst() == t) {
/* 184 */         label139: this.nextTimeout = t.time;
/* 185 */         this.timerList.notifyAll();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 190 */     return t;
/*     */   }
/*     */ 
/*     */   public boolean cancelTimer(Object handle)
/*     */   {
/* 201 */     TimerRequest t = (TimerRequest)handle;
/*     */ 
/* 203 */     synchronized (this.timerList) {
/* 204 */       boolean result = this.timerList.remove(t);
/* 205 */       if (this.nextTimeout == t.time) {
/* 206 */         updateNextTimeout();
/*     */       }
/* 208 */       return result;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static synchronized void stopTimer()
/*     */   {
/* 218 */     if (instance != null) {
/* 219 */       instance.interrupt();
/* 220 */       instance = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean hasExpired(Object handle)
/*     */   {
/* 232 */     TimerRequest t = (TimerRequest)handle;
/*     */ 
/* 234 */     synchronized (this.timerList) {
/* 235 */       return (!(this.timerList.contains(t)));
/*     */     }
/*     */   }
/*     */ 
/*     */   private void updateNextTimeout()
/*     */   {
/* 241 */     this.nextTimeout = ((this.timerList.isEmpty()) ? 0L : ((TimerRequest)this.timerList.getFirst()).time);
/*     */   }
/*     */ 
/*     */   private static class TimerRequest
/*     */   {
/*     */     final long time;
/*     */     final TimerThread.TimerListener target;
/*     */ 
/*     */     TimerRequest(int timeout, TimerThread.TimerListener target)
/*     */     {
/*  66 */       if (timeout <= 0) {
/*  67 */         throw new IllegalArgumentException("Invalid timeout parameter " + timeout);
/*     */       }
/*     */ 
/*  70 */       this.time = (System.currentTimeMillis() + timeout);
/*  71 */       this.target = target;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static abstract interface TimerListener
/*     */   {
/*     */     public abstract void timerExpired();
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.util.TimerThread
 * JD-Core Version:    0.5.3
 */