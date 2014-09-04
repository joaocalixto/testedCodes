/*     */ package net.sourceforge.jtds.jdbc.cache;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.LinkedList;
/*     */ 
/*     */ public class SimpleLRUCache extends HashMap
/*     */ {
/*     */   private final int maxCacheSize;
/*     */   private final LinkedList list;
/*     */ 
/*     */   public SimpleLRUCache(int maxCacheSize)
/*     */   {
/*  44 */     super(maxCacheSize);
/*  45 */     this.maxCacheSize = Math.max(0, maxCacheSize);
/*  46 */     this.list = new LinkedList();
/*     */   }
/*     */ 
/*     */   public synchronized void clear()
/*     */   {
/*  53 */     super.clear();
/*  54 */     this.list.clear();
/*     */   }
/*     */ 
/*     */   public synchronized Object put(Object key, Object value)
/*     */   {
/*  69 */     if (this.maxCacheSize == 0) {
/*  70 */       return null;
/*     */     }
/*     */ 
/*  74 */     if ((!(super.containsKey(key))) && (!(this.list.isEmpty())) && (this.list.size() + 1 > this.maxCacheSize)) {
/*  75 */       Object deadKey = this.list.removeLast();
/*  76 */       super.remove(deadKey);
/*     */     }
/*     */ 
/*  79 */     freshenKey(key);
/*  80 */     return super.put(key, value);
/*     */   }
/*     */ 
/*     */   public synchronized Object get(Object key)
/*     */   {
/*  91 */     Object value = super.get(key);
/*  92 */     if (value != null) {
/*  93 */       freshenKey(key);
/*     */     }
/*  95 */     return value;
/*     */   }
/*     */ 
/*     */   public synchronized Object remove(Object key)
/*     */   {
/* 102 */     this.list.remove(key);
/* 103 */     return super.remove(key);
/*     */   }
/*     */ 
/*     */   private void freshenKey(Object key)
/*     */   {
/* 113 */     this.list.remove(key);
/* 114 */     this.list.addFirst(key);
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.cache.SimpleLRUCache
 * JD-Core Version:    0.5.3
 */