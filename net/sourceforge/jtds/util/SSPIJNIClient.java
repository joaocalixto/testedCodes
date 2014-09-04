/*     */ package net.sourceforge.jtds.util;
/*     */ 
/*     */ public class SSPIJNIClient
/*     */ {
/*     */   private static SSPIJNIClient thisInstance;
/*     */   private static boolean libraryLoaded;
/*     */   private boolean initialized;
/*     */ 
/*     */   private native void initialize();
/*     */ 
/*     */   private native void unInitialize();
/*     */ 
/*     */   private native byte[] prepareSSORequest();
/*     */ 
/*     */   private native byte[] prepareSSOSubmit(byte[] paramArrayOfByte, long paramLong);
/*     */ 
/*     */   public static synchronized SSPIJNIClient getInstance()
/*     */     throws Exception
/*     */   {
/*  87 */     if (thisInstance == null) {
/*  88 */       if (!(libraryLoaded)) {
/*  89 */         throw new Exception("Native SSPI library not loaded. Check the java.library.path system property.");
/*     */       }
/*     */ 
/*  92 */       thisInstance = new SSPIJNIClient();
/*  93 */       thisInstance.invokeInitialize();
/*     */     }
/*  95 */     return thisInstance;
/*     */   }
/*     */ 
/*     */   public void invokeInitialize()
/*     */   {
/* 102 */     if (!(this.initialized)) {
/* 103 */       initialize();
/* 104 */       this.initialized = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void invokeUnInitialize()
/*     */   {
/* 112 */     if (this.initialized) {
/* 113 */       unInitialize();
/* 114 */       this.initialized = false;
/*     */     }
/*     */   }
/*     */ 
/*     */   public byte[] invokePrepareSSORequest()
/*     */     throws Exception
/*     */   {
/* 125 */     if (!(this.initialized)) {
/* 126 */       throw new Exception("SSPI Not Initialized");
/*     */     }
/* 128 */     return prepareSSORequest();
/*     */   }
/*     */ 
/*     */   public byte[] invokePrepareSSOSubmit(byte[] buf)
/*     */     throws Exception
/*     */   {
/* 139 */     if (!(this.initialized)) {
/* 140 */       throw new Exception("SSPI Not Initialized");
/*     */     }
/* 142 */     return prepareSSOSubmit(buf, buf.length);
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*     */     try
/*     */     {
/*  66 */       System.loadLibrary("ntlmauth");
/*  67 */       libraryLoaded = true;
/*     */     } catch (UnsatisfiedLinkError err) {
/*  69 */       Logger.println("Unable to load library: " + err);
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.util.SSPIJNIClient
 * JD-Core Version:    0.5.3
 */