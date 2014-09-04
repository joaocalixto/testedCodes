/*     */ package net.sourceforge.jtds.jdbc.cache;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import net.sourceforge.jtds.jdbc.ProcEntry;
/*     */ 
/*     */ public class ProcedureCache
/*     */   implements StatementCache
/*     */ {
/*     */   private static final int MAX_INITIAL_SIZE = 50;
/*     */   private HashMap cache;
/*     */   int cacheSize;
/*     */   CacheEntry head;
/*     */   CacheEntry tail;
/*     */   ArrayList free;
/*     */ 
/*     */   public ProcedureCache(int cacheSize)
/*     */   {
/*  96 */     this.cacheSize = cacheSize;
/*  97 */     this.cache = new HashMap(Math.min(50, cacheSize) + 1);
/*  98 */     this.head = new CacheEntry(null, null);
/*  99 */     this.tail = new CacheEntry(null, null);
/* 100 */     this.head.next = this.tail;
/* 101 */     this.tail.prior = this.head;
/* 102 */     this.free = new ArrayList();
/*     */   }
/*     */ 
/*     */   public synchronized Object get(String key)
/*     */   {
/* 116 */     CacheEntry ce = (CacheEntry)this.cache.get(key);
/* 117 */     if (ce != null)
/*     */     {
/* 119 */       ce.unlink();
/*     */ 
/* 121 */       ce.link(this.head);
/*     */ 
/* 123 */       ce.value.addRef();
/*     */ 
/* 125 */       return ce.value;
/*     */     }
/* 127 */     return null;
/*     */   }
/*     */ 
/*     */   public synchronized void put(String key, Object handle)
/*     */   {
/* 141 */     ((ProcEntry)handle).addRef();
/*     */ 
/* 144 */     CacheEntry ce = new CacheEntry(key, (ProcEntry)handle);
/* 145 */     this.cache.put(key, ce);
/* 146 */     ce.link(this.head);
/*     */ 
/* 149 */     scavengeCache();
/*     */   }
/*     */ 
/*     */   public synchronized void remove(String key)
/*     */   {
/* 158 */     CacheEntry ce = (CacheEntry)this.cache.get(key);
/* 159 */     if (ce == null)
/*     */       return;
/* 161 */     ce.unlink();
/*     */ 
/* 163 */     this.cache.remove(key);
/*     */   }
/*     */ 
/*     */   public synchronized Collection getObsoleteHandles(Collection handles)
/*     */   {
/*     */     Iterator iterator;
/* 176 */     if (handles != null)
/*     */     {
/* 179 */       for (iterator = handles.iterator(); iterator.hasNext(); ) {
/* 180 */         ProcEntry handle = (ProcEntry)iterator.next();
/* 181 */         handle.release();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 186 */     scavengeCache();
/*     */ 
/* 188 */     if (this.free.size() > 0)
/*     */     {
/* 190 */       Collection list = this.free;
/* 191 */       this.free = new ArrayList();
/* 192 */       return list;
/*     */     }
/*     */ 
/* 195 */     return null;
/*     */   }
/*     */ 
/*     */   private void scavengeCache()
/*     */   {
/* 208 */     CacheEntry ce = this.tail.prior;
/* 209 */     while ((ce != this.head) && (this.cache.size() > this.cacheSize)) {
/* 210 */       if (ce.value.getRefCount() == 0)
/*     */       {
/* 212 */         ce.unlink();
/*     */ 
/* 214 */         this.free.add(ce.value);
/*     */ 
/* 216 */         this.cache.remove(ce.key);
/*     */       }
/* 218 */       ce = ce.prior;
/*     */     }
/*     */   }
/*     */ 
/*     */   private static class CacheEntry
/*     */   {
/*     */     String key;
/*     */     ProcEntry value;
/*     */     CacheEntry next;
/*     */     CacheEntry prior;
/*     */ 
/*     */     CacheEntry(String key, ProcEntry value)
/*     */     {
/*  52 */       this.key = key;
/*  53 */       this.value = value;
/*     */     }
/*     */ 
/*     */     void unlink()
/*     */     {
/*  60 */       this.next.prior = this.prior;
/*  61 */       this.prior.next = this.next;
/*     */     }
/*     */ 
/*     */     void link(CacheEntry ce)
/*     */     {
/*  70 */       this.next = ce.next;
/*  71 */       this.prior = ce;
/*  72 */       this.next.prior = this;
/*  73 */       ce.next = this;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.cache.ProcedureCache
 * JD-Core Version:    0.5.3
 */