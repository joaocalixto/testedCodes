/*     */ package net.sourceforge.jtds.ssl;
/*     */ 
/*     */ import com.sun.net.ssl.SSLContext;
/*     */ import com.sun.net.ssl.TrustManager;
/*     */ import com.sun.net.ssl.X509TrustManager;
/*     */ import java.io.IOException;
/*     */ import java.net.InetAddress;
/*     */ import java.net.Socket;
/*     */ import java.net.UnknownHostException;
/*     */ import java.security.GeneralSecurityException;
/*     */ import java.security.KeyManagementException;
/*     */ import java.security.NoSuchAlgorithmException;
/*     */ import javax.net.SocketFactory;
/*     */ import javax.net.ssl.SSLSession;
/*     */ import javax.net.ssl.SSLSocket;
/*     */ import javax.net.ssl.SSLSocketFactory;
/*     */ import net.sourceforge.jtds.util.Logger;
/*     */ 
/*     */ public class SocketFactoriesSUN
/*     */ {
/*     */   public static SocketFactory getSocketFactory(String ssl, Socket socket)
/*     */   {
/*  54 */     return new TdsTlsSocketFactory(ssl, socket);
/*     */   }
/*     */ 
/*     */   private static class TdsTlsSocketFactory extends SocketFactory
/*     */   {
/*     */     private static SSLSocketFactory factorySingleton;
/*     */     private final String ssl;
/*     */     private final Socket socket;
/*     */ 
/*     */     public TdsTlsSocketFactory(String ssl, Socket socket)
/*     */     {
/*  73 */       this.ssl = ssl;
/*  74 */       this.socket = socket;
/*     */     }
/*     */ 
/*     */     public Socket createSocket(String host, int port)
/*     */       throws IOException, UnknownHostException
/*     */     {
/*  86 */       SSLSocket sslSocket = (SSLSocket)getFactory().createSocket(new TdsTlsSocket(this.socket), host, port, true);
/*     */ 
/* 102 */       sslSocket.startHandshake();
/* 103 */       sslSocket.getSession().invalidate();
/*     */ 
/* 107 */       return sslSocket;
/*     */     }
/*     */ 
/*     */     public Socket createSocket(InetAddress host, int port)
/*     */       throws IOException
/*     */     {
/* 117 */       return null;
/*     */     }
/*     */ 
/*     */     public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
/*     */       throws IOException, UnknownHostException
/*     */     {
/* 129 */       return null;
/*     */     }
/*     */ 
/*     */     public Socket createSocket(InetAddress host, int port, InetAddress localHost, int localPort)
/*     */       throws IOException
/*     */     {
/* 140 */       return null;
/*     */     }
/*     */ 
/*     */     private SSLSocketFactory getFactory()
/*     */       throws IOException
/*     */     {
/*     */       try
/*     */       {
/* 151 */         if ("authenticate".equals(this.ssl))
/*     */         {
/* 154 */           return ((SSLSocketFactory)SSLSocketFactory.getDefault());
/*     */         }
/*     */ 
/* 157 */         return factory();
/*     */       }
/*     */       catch (GeneralSecurityException e) {
/* 160 */         Logger.logException(e);
/* 161 */         throw new IOException(e.getMessage());
/*     */       }
/*     */     }
/*     */ 
/*     */     private static SSLSocketFactory factory()
/*     */       throws NoSuchAlgorithmException, KeyManagementException
/*     */     {
/* 173 */       if (factorySingleton == null) {
/* 174 */         SSLContext ctx = SSLContext.getInstance("TLS");
/* 175 */         ctx.init(null, trustManagers(), null);
/* 176 */         factorySingleton = ctx.getSocketFactory();
/*     */       }
/* 178 */       return factorySingleton;
/*     */     }
/*     */ 
/*     */     private static TrustManager[] trustManagers() {
/* 182 */       X509TrustManager tm = new SocketFactoriesSUN.1();
/*     */ 
/* 196 */       return new X509TrustManager[] { tm };
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.ssl.SocketFactoriesSUN
 * JD-Core Version:    0.5.3
 */