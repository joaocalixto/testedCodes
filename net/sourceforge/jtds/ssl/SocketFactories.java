/*     */ package net.sourceforge.jtds.ssl;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.net.InetAddress;
/*     */ import java.net.Socket;
/*     */ import java.net.UnknownHostException;
/*     */ import java.security.GeneralSecurityException;
/*     */ import java.security.KeyManagementException;
/*     */ import java.security.NoSuchAlgorithmException;
/*     */ import javax.net.SocketFactory;
/*     */ import javax.net.ssl.SSLContext;
/*     */ import javax.net.ssl.SSLSession;
/*     */ import javax.net.ssl.SSLSocket;
/*     */ import javax.net.ssl.SSLSocketFactory;
/*     */ import javax.net.ssl.TrustManager;
/*     */ import javax.net.ssl.X509TrustManager;
/*     */ import net.sourceforge.jtds.util.Logger;
/*     */ 
/*     */ public class SocketFactories
/*     */ {
/*     */   public static SocketFactory getSocketFactory(String ssl, Socket socket)
/*     */   {
/*  53 */     return new TdsTlsSocketFactory(ssl, socket);
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
/*  72 */       this.ssl = ssl;
/*  73 */       this.socket = socket;
/*     */     }
/*     */ 
/*     */     public Socket createSocket(String host, int port)
/*     */       throws IOException, UnknownHostException
/*     */     {
/*  85 */       SSLSocket sslSocket = (SSLSocket)getFactory().createSocket(new TdsTlsSocket(this.socket), host, port, true);
/*     */ 
/* 101 */       sslSocket.startHandshake();
/* 102 */       sslSocket.getSession().invalidate();
/*     */ 
/* 106 */       return sslSocket;
/*     */     }
/*     */ 
/*     */     public Socket createSocket(InetAddress host, int port)
/*     */       throws IOException
/*     */     {
/* 116 */       return null;
/*     */     }
/*     */ 
/*     */     public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
/*     */       throws IOException, UnknownHostException
/*     */     {
/* 128 */       return null;
/*     */     }
/*     */ 
/*     */     public Socket createSocket(InetAddress host, int port, InetAddress localHost, int localPort)
/*     */       throws IOException
/*     */     {
/* 139 */       return null;
/*     */     }
/*     */ 
/*     */     private SSLSocketFactory getFactory()
/*     */       throws IOException
/*     */     {
/*     */       try
/*     */       {
/* 150 */         if ("authenticate".equals(this.ssl))
/*     */         {
/* 153 */           return ((SSLSocketFactory)SSLSocketFactory.getDefault());
/*     */         }
/*     */ 
/* 156 */         return factory();
/*     */       }
/*     */       catch (GeneralSecurityException e) {
/* 159 */         Logger.logException(e);
/* 160 */         throw new IOException(e.getMessage());
/*     */       }
/*     */     }
/*     */ 
/*     */     private static SSLSocketFactory factory()
/*     */       throws NoSuchAlgorithmException, KeyManagementException
/*     */     {
/* 172 */       if (factorySingleton == null) {
/* 173 */         SSLContext ctx = SSLContext.getInstance("TLS");
/* 174 */         ctx.init(null, trustManagers(), null);
/* 175 */         factorySingleton = ctx.getSocketFactory();
/*     */       }
/* 177 */       return factorySingleton;
/*     */     }
/*     */ 
/*     */     private static TrustManager[] trustManagers() {
/* 181 */       X509TrustManager tm = new SocketFactories.1();
/*     */ 
/* 196 */       return new X509TrustManager[] { tm };
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.ssl.SocketFactories
 * JD-Core Version:    0.5.3
 */