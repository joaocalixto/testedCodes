/*     */ package net.sourceforge.jtds.ssl;
/*     */ 
/*     */ import java.io.FilterOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ class TdsTlsOutputStream extends FilterOutputStream
/*     */ {
/*  48 */   private final List bufferedRecords = new ArrayList();
/*     */   private int totalSize;
/*     */ 
/*     */   TdsTlsOutputStream(OutputStream out)
/*     */   {
/*  57 */     super(out);
/*     */   }
/*     */ 
/*     */   private void deferRecord(byte[] record, int len)
/*     */   {
/*  67 */     byte[] tmp = new byte[len];
/*  68 */     System.arraycopy(record, 0, tmp, 0, len);
/*  69 */     this.bufferedRecords.add(tmp);
/*  70 */     this.totalSize += len;
/*     */   }
/*     */ 
/*     */   private void flushBufferedRecords()
/*     */     throws IOException
/*     */   {
/*  77 */     byte[] tmp = new byte[this.totalSize];
/*  78 */     int off = 0;
/*  79 */     for (int i = 0; i < this.bufferedRecords.size(); ++i) {
/*  80 */       byte[] x = (byte[])this.bufferedRecords.get(i);
/*  81 */       System.arraycopy(x, 0, tmp, off, x.length);
/*  82 */       off += x.length;
/*     */     }
/*  84 */     putTdsPacket(tmp, off);
/*  85 */     this.bufferedRecords.clear();
/*  86 */     this.totalSize = 0;
/*     */   }
/*     */ 
/*     */   public void write(byte[] b, int off, int len) throws IOException
/*     */   {
/*  91 */     if ((len < 5) || (off > 0))
/*     */     {
/*  93 */       this.out.write(b, off, len);
/*  94 */       return;
/*     */     }
/*     */ 
/*  99 */     int contentType = b[0] & 0xFF;
/* 100 */     int length = (b[3] & 0xFF) << 8 | b[4] & 0xFF;
/*     */ 
/* 104 */     if ((contentType < 20) || (contentType > 23) || (length != len - 5))
/*     */     {
/* 108 */       putTdsPacket(b, len);
/* 109 */       return;
/*     */     }
/*     */ 
/* 114 */     switch (contentType)
/*     */     {
/*     */     case 23:
/* 118 */       this.out.write(b, off, len);
/* 119 */       break;
/*     */     case 20:
/* 123 */       deferRecord(b, len);
/* 124 */       break;
/*     */     case 21:
/* 128 */       break;
/*     */     case 22:
/* 132 */       if (len < 9)
/*     */         break label235;
/* 134 */       int hsType = b[5];
/* 135 */       int hsLen = (b[6] & 0xFF) << 16 | (b[7] & 0xFF) << 8 | b[8] & 0xFF;
/*     */ 
/* 139 */       if ((hsLen == len - 9) && (hsType == 1))
/*     */       {
/* 142 */         putTdsPacket(b, len);
/* 143 */         return;
/*     */       }
/*     */ 
/* 146 */       deferRecord(b, len);
/*     */ 
/* 151 */       if ((hsLen == len - 9) && (hsType == 16)) {
/*     */         return;
/*     */       }
/* 154 */       flushBufferedRecords(); break;
/*     */     default:
/* 160 */       label235: this.out.write(b, off, len);
/*     */     }
/*     */   }
/*     */ 
/*     */   void putTdsPacket(byte[] b, int len)
/*     */     throws IOException
/*     */   {
/* 172 */     byte[] tdsHdr = new byte[8];
/* 173 */     tdsHdr[0] = 18;
/* 174 */     tdsHdr[1] = 1;
/* 175 */     tdsHdr[2] = (byte)(len + 8 >> 8);
/* 176 */     tdsHdr[3] = (byte)(len + 8);
/* 177 */     this.out.write(tdsHdr, 0, tdsHdr.length);
/* 178 */     this.out.write(b, 0, len);
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */     throws IOException
/*     */   {
/* 187 */     super.flush();
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.ssl.TdsTlsOutputStream
 * JD-Core Version:    0.5.3
 */