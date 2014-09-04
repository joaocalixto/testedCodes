/*     */ package net.sourceforge.jtds.util;
/*     */ 
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintWriter;
/*     */ import java.sql.DriverManager;
/*     */ 
/*     */ public class Logger
/*     */ {
/*     */   private static PrintWriter log;
/*  82 */   private static final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
/*     */ 
/*     */   public static void setLogWriter(PrintWriter out)
/*     */   {
/*  48 */     log = out;
/*     */   }
/*     */ 
/*     */   public static PrintWriter getLogWriter()
/*     */   {
/*  57 */     return log;
/*     */   }
/*     */ 
/*     */   public static boolean isActive()
/*     */   {
/*  66 */     return ((log != null) || (DriverManager.getLogWriter() != null));
/*     */   }
/*     */ 
/*     */   public static void println(String message)
/*     */   {
/*  76 */     if (log != null)
/*  77 */       log.println(message);
/*     */     else
/*  79 */       DriverManager.println(message);
/*     */   }
/*     */ 
/*     */   public static void logPacket(int streamId, boolean in, byte[] pkt)
/*     */   {
/*  93 */     int len = (pkt[2] & 0xFF) << 8 | pkt[3] & 0xFF;
/*     */ 
/*  95 */     StringBuffer line = new StringBuffer(80);
/*     */ 
/*  97 */     line.append("----- Stream #");
/*  98 */     line.append(streamId);
/*  99 */     line.append((in) ? " read" : " send");
/* 100 */     line.append((pkt[1] != 0) ? " last " : " ");
/*     */ 
/* 102 */     switch (pkt[0])
/*     */     {
/*     */     case 1:
/* 104 */       line.append("Request packet ");
/* 105 */       break;
/*     */     case 2:
/* 107 */       line.append("Login packet ");
/* 108 */       break;
/*     */     case 3:
/* 110 */       line.append("RPC packet ");
/* 111 */       break;
/*     */     case 4:
/* 113 */       line.append("Reply packet ");
/* 114 */       break;
/*     */     case 6:
/* 116 */       line.append("Cancel packet ");
/* 117 */       break;
/*     */     case 14:
/* 119 */       line.append("XA control packet ");
/* 120 */       break;
/*     */     case 15:
/* 122 */       line.append("TDS5 Request packet ");
/* 123 */       break;
/*     */     case 16:
/* 125 */       line.append("MS Login packet ");
/* 126 */       break;
/*     */     case 17:
/* 128 */       line.append("NTLM Authentication packet ");
/* 129 */       break;
/*     */     case 18:
/* 131 */       line.append("MS Prelogin packet ");
/* 132 */       break;
/*     */     case 5:
/*     */     case 7:
/*     */     case 8:
/*     */     case 9:
/*     */     case 10:
/*     */     case 11:
/*     */     case 12:
/*     */     case 13:
/*     */     default:
/* 134 */       line.append("Invalid packet ");
/*     */     }
/*     */ 
/* 138 */     println(line.toString());
/* 139 */     println("");
/* 140 */     line.setLength(0);
/*     */ 
/* 142 */     for (int i = 0; i < len; i += 16) {
/* 143 */       if (i < 1000) {
/* 144 */         line.append(' ');
/*     */       }
/*     */ 
/* 147 */       if (i < 100) {
/* 148 */         line.append(' ');
/*     */       }
/*     */ 
/* 151 */       if (i < 10) {
/* 152 */         line.append(' ');
/*     */       }
/*     */ 
/* 155 */       line.append(i);
/* 156 */       line.append(':').append(' ');
/*     */ 
/* 158 */       int j = 0;
/*     */       int val;
/* 160 */       for (; (j < 16) && (i + j < len); ++j) {
/* 161 */         val = pkt[(i + j)] & 0xFF;
/*     */ 
/* 163 */         line.append(hex[(val >> 4)]);
/* 164 */         line.append(hex[(val & 0xF)]);
/* 165 */         line.append(' ');
/*     */       }
/*     */ 
/* 168 */       for (; j < 16; ++j) {
/* 169 */         line.append("   ");
/*     */       }
/*     */ 
/* 172 */       line.append('|');
/*     */ 
/* 174 */       for (j = 0; (j < 16) && (i + j < len); ++j) {
/* 175 */         val = pkt[(i + j)] & 0xFF;
/*     */ 
/* 177 */         if ((val > 31) && (val < 127))
/* 178 */           line.append((char)val);
/*     */         else {
/* 180 */           line.append(' ');
/*     */         }
/*     */       }
/*     */ 
/* 184 */       line.append('|');
/* 185 */       println(line.toString());
/* 186 */       line.setLength(0);
/*     */     }
/*     */ 
/* 189 */     println("");
/*     */   }
/*     */ 
/*     */   public static void logException(Exception e)
/*     */   {
/* 198 */     if (log != null)
/* 199 */       e.printStackTrace(log);
/* 200 */     else if (DriverManager.getLogWriter() != null)
/* 201 */       e.printStackTrace(DriverManager.getLogWriter());
/*     */   }
/*     */ 
/*     */   /** @deprecated */
/*     */   public static void setActive(boolean value)
/*     */   {
/* 215 */     if ((!(value)) || (log != null)) return;
/*     */     try {
/* 217 */       log = new PrintWriter(new FileOutputStream("log.out"), true);
/*     */     } catch (IOException e) {
/* 219 */       log = null;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.util.Logger
 * JD-Core Version:    0.5.3
 */