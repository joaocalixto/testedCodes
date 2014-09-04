/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.InterruptedIOException;
/*     */ import java.net.DatagramPacket;
/*     */ import java.net.DatagramSocket;
/*     */ import java.net.InetAddress;
/*     */ import java.sql.SQLException;
/*     */ import net.sourceforge.jtds.util.Logger;
/*     */ 
/*     */ public class MSSqlServerInfo
/*     */ {
/*  54 */   private int numRetries = 3;
/*  55 */   private int timeout = 2000;
/*     */   private String[] serverInfoStrings;
/*     */ 
/*     */   public MSSqlServerInfo(String host)
/*     */     throws SQLException
/*     */   {
/*  59 */     DatagramSocket socket = null;
/*     */     try {
/*  61 */       InetAddress addr = InetAddress.getByName(host);
/*  62 */       socket = new DatagramSocket();
/*  63 */       byte[] msg = { 2 };
/*  64 */       DatagramPacket requestp = new DatagramPacket(msg, msg.length, addr, 1434);
/*     */ 
/*  66 */       byte[] buf = new byte[4096];
/*     */ 
/*  68 */       DatagramPacket responsep = new DatagramPacket(buf, buf.length);
/*  69 */       socket.setSoTimeout(this.timeout);
/*     */ 
/*  71 */       for (int i = 0; i < this.numRetries; ++i)
/*     */         try {
/*  73 */           socket.send(requestp);
/*     */ 
/*  75 */           socket.receive(responsep);
/*  76 */           String infoString = extractString(buf, responsep.getLength());
/*  77 */           this.serverInfoStrings = split(infoString, 59);
/*     */ 
/*  91 */           if (socket != null)
/*  92 */             socket.close(); return;
/*     */         }
/*     */         catch (InterruptedIOException toEx)
/*     */         {
/*  81 */           if (Logger.isActive())
/*  82 */             Logger.logException(toEx);
/*     */         }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  87 */       if (Logger.isActive())
/*  88 */         Logger.logException(e);
/*     */     }
/*     */     finally {
/*  91 */       if (socket != null) {
/*  92 */         socket.close();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  97 */     throw new SQLException(Messages.get("error.msinfo.badinfo", host), "HY000");
/*     */   }
/*     */ 
/*     */   public int getPortForInstance(String instanceName)
/*     */     throws SQLException
/*     */   {
/* 112 */     if (this.serverInfoStrings == null) {
/* 113 */       return -1;
/*     */     }
/*     */ 
/* 117 */     if ((instanceName == null) || (instanceName.length() == 0)) {
/* 118 */       instanceName = "MSSQLSERVER";
/*     */     }
/*     */ 
/* 121 */     String curInstance = null;
/* 122 */     String curPort = null;
/*     */ 
/* 124 */     for (int index = 0; index < this.serverInfoStrings.length; ++index) {
/* 125 */       if (this.serverInfoStrings[index].length() == 0) {
/* 126 */         curInstance = null;
/* 127 */         curPort = null;
/*     */       } else {
/* 129 */         String key = this.serverInfoStrings[index];
/* 130 */         String value = "";
/*     */ 
/* 132 */         ++index;
/*     */ 
/* 134 */         if (index < this.serverInfoStrings.length) {
/* 135 */           value = this.serverInfoStrings[index];
/*     */         }
/*     */ 
/* 138 */         if ("InstanceName".equals(key)) {
/* 139 */           curInstance = value;
/*     */         }
/*     */ 
/* 142 */         if ("tcp".equals(key)) {
/* 143 */           curPort = value;
/*     */         }
/*     */ 
/* 146 */         if ((curInstance == null) || (curPort == null) || (!(curInstance.equalsIgnoreCase(instanceName)))) {
/*     */           continue;
/*     */         }
/*     */         try
/*     */         {
/* 151 */           return Integer.parseInt(curPort);
/*     */         } catch (NumberFormatException e) {
/* 153 */           throw new SQLException(Messages.get("error.msinfo.badport", instanceName), "HY000");
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 162 */     return -1;
/*     */   }
/*     */ 
/*     */   private static final String extractString(byte[] buf, int len)
/*     */   {
/* 167 */     int headerLength = 3;
/*     */ 
/* 169 */     return new String(buf, 3, len - 3);
/*     */   }
/*     */ 
/*     */   public static String[] split(String s, int ch) {
/* 173 */     int size = 0;
/* 174 */     for (int pos = 0; pos != -1; ++size) pos = s.indexOf(ch, pos + 1);
/*     */ 
/* 176 */     String[] res = new String[size];
/* 177 */     int i = 0;
/* 178 */     int p1 = 0;
/* 179 */     int p2 = s.indexOf(ch);
/*     */     do
/*     */     {
/* 182 */       res[(i++)] = s.substring(p1, (p2 == -1) ? s.length() : p2);
/* 183 */       p1 = p2 + 1;
/* 184 */       p2 = s.indexOf(ch, p1); }
/* 185 */     while (p1 != 0);
/*     */ 
/* 187 */     return res;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.MSSqlServerInfo
 * JD-Core Version:    0.5.3
 */