/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.math.BigDecimal;
/*     */ import net.sourceforge.jtds.util.Logger;
/*     */ 
/*     */ public class ResponseStream
/*     */ {
/*     */   private final SharedSocket socket;
/*     */   private byte[] buffer;
/*     */   private int bufferPtr;
/*     */   private int bufferLen;
/*     */   private final int streamId;
/*     */   private boolean isClosed;
/*  53 */   private final byte[] byteBuffer = new byte[255];
/*     */ 
/*  55 */   private final char[] charBuffer = new char[255];
/*     */ 
/*     */   ResponseStream(SharedSocket socket, int streamId, int bufferSize)
/*     */   {
/*  65 */     this.streamId = streamId;
/*  66 */     this.socket = socket;
/*  67 */     this.buffer = new byte[bufferSize];
/*  68 */     this.bufferLen = bufferSize;
/*  69 */     this.bufferPtr = bufferSize;
/*     */   }
/*     */ 
/*     */   int getStreamId()
/*     */   {
/*  78 */     return this.streamId;
/*     */   }
/*     */ 
/*     */   int peek()
/*     */     throws IOException
/*     */   {
/*  88 */     int b = read();
/*     */ 
/*  90 */     this.bufferPtr -= 1;
/*     */ 
/*  92 */     return b;
/*     */   }
/*     */ 
/*     */   int read()
/*     */     throws IOException
/*     */   {
/* 102 */     if (this.bufferPtr >= this.bufferLen) {
/* 103 */       getPacket();
/*     */     }
/*     */ 
/* 106 */     return (this.buffer[(this.bufferPtr++)] & 0xFF);
/*     */   }
/*     */ 
/*     */   int read(byte[] b)
/*     */     throws IOException
/*     */   {
/* 117 */     return read(b, 0, b.length);
/*     */   }
/*     */ 
/*     */   int read(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/* 131 */     int bytesToRead = len;
/*     */ 
/* 133 */     while (bytesToRead > 0) {
/* 134 */       if (this.bufferPtr >= this.bufferLen) {
/* 135 */         getPacket();
/*     */       }
/*     */ 
/* 138 */       int available = this.bufferLen - this.bufferPtr;
/* 139 */       int bc = (available > bytesToRead) ? bytesToRead : available;
/*     */ 
/* 141 */       System.arraycopy(this.buffer, this.bufferPtr, b, off, bc);
/* 142 */       off += bc;
/* 143 */       bytesToRead -= bc;
/* 144 */       this.bufferPtr += bc;
/*     */     }
/*     */ 
/* 147 */     return len;
/*     */   }
/*     */ 
/*     */   int read(char[] c)
/*     */     throws IOException
/*     */   {
/* 158 */     for (int i = 0; i < c.length; ++i) {
/* 159 */       if (this.bufferPtr >= this.bufferLen) {
/* 160 */         getPacket();
/*     */       }
/*     */ 
/* 163 */       int b1 = this.buffer[(this.bufferPtr++)] & 0xFF;
/*     */ 
/* 165 */       if (this.bufferPtr >= this.bufferLen) {
/* 166 */         getPacket();
/*     */       }
/*     */ 
/* 169 */       int b2 = this.buffer[(this.bufferPtr++)] << 8;
/*     */ 
/* 171 */       c[i] = (char)(b2 | b1);
/*     */     }
/*     */ 
/* 174 */     return c.length;
/*     */   }
/*     */ 
/*     */   String readString(int len)
/*     */     throws IOException
/*     */   {
/* 189 */     if (this.socket.getTdsVersion() >= 3) {
/* 190 */       return readUnicodeString(len);
/*     */     }
/*     */ 
/* 193 */     return readNonUnicodeString(len);
/*     */   }
/*     */ 
/*     */   void skipString(int len)
/*     */     throws IOException
/*     */   {
/* 208 */     if (len <= 0) {
/* 209 */       return;
/*     */     }
/*     */ 
/* 212 */     if (this.socket.getTdsVersion() >= 3)
/* 213 */       skip(len * 2);
/*     */     else
/* 215 */       skip(len);
/*     */   }
/*     */ 
/*     */   String readUnicodeString(int len)
/*     */     throws IOException
/*     */   {
/* 228 */     char[] chars = (len > this.charBuffer.length) ? new char[len] : this.charBuffer;
/*     */ 
/* 230 */     for (int i = 0; i < len; ++i) {
/* 231 */       if (this.bufferPtr >= this.bufferLen) {
/* 232 */         getPacket();
/*     */       }
/*     */ 
/* 235 */       int b1 = this.buffer[(this.bufferPtr++)] & 0xFF;
/*     */ 
/* 237 */       if (this.bufferPtr >= this.bufferLen) {
/* 238 */         getPacket();
/*     */       }
/*     */ 
/* 241 */       int b2 = this.buffer[(this.bufferPtr++)] << 8;
/*     */ 
/* 243 */       chars[i] = (char)(b2 | b1);
/*     */     }
/*     */ 
/* 246 */     return new String(chars, 0, len);
/*     */   }
/*     */ 
/*     */   String readNonUnicodeString(int len)
/*     */     throws IOException
/*     */   {
/* 259 */     CharsetInfo info = this.socket.getCharsetInfo();
/*     */ 
/* 261 */     return readString(len, info);
/*     */   }
/*     */ 
/*     */   String readNonUnicodeString(int len, CharsetInfo charsetInfo)
/*     */     throws IOException
/*     */   {
/* 274 */     return readString(len, charsetInfo);
/*     */   }
/*     */ 
/*     */   String readString(int len, CharsetInfo info)
/*     */     throws IOException
/*     */   {
/* 287 */     String charsetName = info.getCharset();
/* 288 */     byte[] bytes = (len > this.byteBuffer.length) ? new byte[len] : this.byteBuffer;
/*     */ 
/* 290 */     read(bytes, 0, len);
/*     */     try
/*     */     {
/* 293 */       return new String(bytes, 0, len, charsetName); } catch (UnsupportedEncodingException e) {
/*     */     }
/* 295 */     return new String(bytes, 0, len);
/*     */   }
/*     */ 
/*     */   short readShort()
/*     */     throws IOException
/*     */   {
/* 306 */     int b1 = read();
/*     */ 
/* 308 */     return (short)(b1 | read() << 8);
/*     */   }
/*     */ 
/*     */   int readInt()
/*     */     throws IOException
/*     */   {
/* 318 */     int b1 = read();
/* 319 */     int b2 = read() << 8;
/* 320 */     int b3 = read() << 16;
/* 321 */     int b4 = read() << 24;
/*     */ 
/* 323 */     return (b4 | b3 | b2 | b1);
/*     */   }
/*     */ 
/*     */   long readLong()
/*     */     throws IOException
/*     */   {
/* 333 */     long b1 = read();
/* 334 */     long b2 = read() << 8;
/* 335 */     long b3 = read() << 16;
/* 336 */     long b4 = read() << 24;
/* 337 */     long b5 = read() << 32;
/* 338 */     long b6 = read() << 40;
/* 339 */     long b7 = read() << 48;
/* 340 */     long b8 = read() << 56;
/*     */ 
/* 342 */     return (b1 | b2 | b3 | b4 | b5 | b6 | b7 | b8);
/*     */   }
/*     */ 
/*     */   BigDecimal readUnsignedLong()
/*     */     throws IOException
/*     */   {
/* 352 */     int b1 = read() & 0xFF;
/* 353 */     long b2 = read();
/* 354 */     long b3 = read() << 8;
/* 355 */     long b4 = read() << 16;
/* 356 */     long b5 = read() << 24;
/* 357 */     long b6 = read() << 32;
/* 358 */     long b7 = read() << 40;
/* 359 */     long b8 = read() << 48;
/*     */ 
/* 362 */     return new BigDecimal(Long.toString(b2 | b3 | b4 | b5 | b6 | b7 | b8)).multiply(new BigDecimal(256.0D)).add(new BigDecimal(b1));
/*     */   }
/*     */ 
/*     */   int skip(int skip)
/*     */     throws IOException
/*     */   {
/* 374 */     int tmp = skip;
/*     */ 
/* 376 */     while (skip > 0) {
/* 377 */       if (this.bufferPtr >= this.bufferLen) {
/* 378 */         getPacket();
/*     */       }
/*     */ 
/* 381 */       int available = this.bufferLen - this.bufferPtr;
/*     */ 
/* 383 */       if (skip > available) {
/* 384 */         skip -= available;
/* 385 */         this.bufferPtr = this.bufferLen;
/*     */       } else {
/* 387 */         this.bufferPtr += skip;
/* 388 */         skip = 0;
/*     */       }
/*     */     }
/*     */ 
/* 392 */     return tmp;
/*     */   }
/*     */ 
/*     */   void skipToEnd()
/*     */   {
/*     */     try
/*     */     {
/* 404 */       this.bufferPtr = this.bufferLen;
/*     */ 
/* 407 */       this.buffer = this.socket.getNetPacket(this.streamId, this.buffer);
/*     */     }
/*     */     catch (IOException ex)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   void close()
/*     */   {
/* 419 */     this.isClosed = true;
/* 420 */     this.socket.closeStream(this.streamId);
/*     */   }
/*     */ 
/*     */   int getTdsVersion()
/*     */   {
/* 429 */     return this.socket.getTdsVersion();
/*     */   }
/*     */ 
/*     */   int getServerType()
/*     */   {
/* 438 */     return this.socket.serverType;
/*     */   }
/*     */ 
/*     */   InputStream getInputStream(int len)
/*     */   {
/* 452 */     return new TdsInputStream(this, len);
/*     */   }
/*     */ 
/*     */   private void getPacket()
/*     */     throws IOException
/*     */   {
/* 461 */     while (this.bufferPtr >= this.bufferLen) {
/*     */       do { if (this.isClosed) {
/* 463 */           throw new IOException("ResponseStream is closed");
/*     */         }
/*     */ 
/* 466 */         this.buffer = this.socket.getNetPacket(this.streamId, this.buffer);
/* 467 */         this.bufferLen = ((this.buffer[2] & 0xFF) << 8 | this.buffer[3] & 0xFF);
/* 468 */         this.bufferPtr = 8;
/*     */       }
/* 470 */       while (!(Logger.isActive()));
/* 471 */       Logger.logPacket(this.streamId, true, this.buffer);
/*     */     }
/*     */   }
/*     */ 
/*     */   private static class TdsInputStream extends InputStream
/*     */   {
/*     */     ResponseStream tds;
/*     */     int maxLen;
/*     */ 
/*     */     public TdsInputStream(ResponseStream tds, int maxLen)
/*     */     {
/* 493 */       this.tds = tds;
/* 494 */       this.maxLen = maxLen;
/*     */     }
/*     */ 
/*     */     public int read() throws IOException {
/* 498 */       return ((this.maxLen-- > 0) ? this.tds.read() : -1);
/*     */     }
/*     */ 
/*     */     public int read(byte[] bytes, int offset, int len) throws IOException {
/* 502 */       if (this.maxLen < 1) {
/* 503 */         return -1;
/*     */       }
/* 505 */       int bc = Math.min(this.maxLen, len);
/* 506 */       if (bc > 0) {
/* 507 */         bc = this.tds.read(bytes, offset, bc);
/* 508 */         this.maxLen -= ((bc == -1) ? 0 : bc);
/*     */       }
/* 510 */       return bc;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.ResponseStream
 * JD-Core Version:    0.5.3
 */