/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.math.BigDecimal;
/*     */ import java.math.BigInteger;
/*     */ import net.sourceforge.jtds.util.Logger;
/*     */ 
/*     */ public class RequestStream
/*     */ {
/*     */   private final SharedSocket socket;
/*     */   private byte[] buffer;
/*     */   private int bufferPtr;
/*     */   private byte pktType;
/*     */   private final int streamId;
/*     */   private boolean isClosed;
/*     */   private int bufferSize;
/*     */   private int maxPrecision;
/*     */ 
/*     */   RequestStream(SharedSocket socket, int streamId, int bufferSize, int maxPrecision)
/*     */   {
/*  69 */     this.streamId = streamId;
/*  70 */     this.socket = socket;
/*  71 */     this.bufferSize = bufferSize;
/*  72 */     this.buffer = new byte[bufferSize];
/*  73 */     this.bufferPtr = 8;
/*  74 */     this.maxPrecision = maxPrecision;
/*     */   }
/*     */ 
/*     */   void setBufferSize(int size)
/*     */   {
/*  83 */     if ((size < this.bufferPtr) || (size == this.bufferSize)) {
/*  84 */       return;
/*     */     }
/*     */ 
/*  87 */     if ((size < 512) || (size > 32768)) {
/*  88 */       throw new IllegalArgumentException("Invalid buffer size parameter " + size);
/*     */     }
/*     */ 
/*  91 */     byte[] tmp = new byte[size];
/*  92 */     System.arraycopy(this.buffer, 0, tmp, 0, this.bufferPtr);
/*  93 */     this.buffer = tmp;
/*     */   }
/*     */ 
/*     */   int getBufferSize()
/*     */   {
/* 102 */     return this.bufferSize;
/*     */   }
/*     */ 
/*     */   int getMaxPrecision()
/*     */   {
/* 111 */     return this.maxPrecision;
/*     */   }
/*     */ 
/*     */   byte getMaxDecimalBytes()
/*     */   {
/* 121 */     return (byte)((this.maxPrecision <= 28) ? 13 : 17);
/*     */   }
/*     */ 
/*     */   int getStreamId()
/*     */   {
/* 130 */     return this.streamId;
/*     */   }
/*     */ 
/*     */   void setPacketType(byte pktType)
/*     */   {
/* 139 */     this.pktType = pktType;
/*     */   }
/*     */ 
/*     */   void write(byte b)
/*     */     throws IOException
/*     */   {
/* 149 */     if (this.bufferPtr == this.buffer.length) {
/* 150 */       putPacket(0);
/*     */     }
/*     */ 
/* 153 */     this.buffer[(this.bufferPtr++)] = b;
/*     */   }
/*     */ 
/*     */   void write(byte[] b)
/*     */     throws IOException
/*     */   {
/* 163 */     int bytesToWrite = b.length;
/* 164 */     int off = 0;
/*     */ 
/* 166 */     while (bytesToWrite > 0) {
/* 167 */       int available = this.buffer.length - this.bufferPtr;
/*     */ 
/* 169 */       if (available == 0) {
/* 170 */         putPacket(0);
/*     */       }
/*     */ 
/* 174 */       int bc = (available > bytesToWrite) ? bytesToWrite : available;
/* 175 */       System.arraycopy(b, off, this.buffer, this.bufferPtr, bc);
/* 176 */       off += bc;
/* 177 */       this.bufferPtr += bc;
/* 178 */       bytesToWrite -= bc;
/*     */     }
/*     */   }
/*     */ 
/*     */   void write(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/* 191 */     int limit = (off + len > b.length) ? b.length : off + len;
/* 192 */     int bytesToWrite = limit - off;
/* 193 */     int i = len - bytesToWrite;
/*     */ 
/* 195 */     while (bytesToWrite > 0) {
/* 196 */       int available = this.buffer.length - this.bufferPtr;
/*     */ 
/* 198 */       if (available == 0) {
/* 199 */         putPacket(0);
/*     */       }
/*     */ 
/* 203 */       int bc = (available > bytesToWrite) ? bytesToWrite : available;
/* 204 */       System.arraycopy(b, off, this.buffer, this.bufferPtr, bc);
/* 205 */       off += bc;
/* 206 */       this.bufferPtr += bc;
/* 207 */       bytesToWrite -= bc;
/*     */     }
/*     */ 
/* 210 */     for (; i > 0; --i)
/* 211 */       write(0);
/*     */   }
/*     */ 
/*     */   void write(int i)
/*     */     throws IOException
/*     */   {
/* 222 */     write((byte)i);
/* 223 */     write((byte)(i >> 8));
/* 224 */     write((byte)(i >> 16));
/* 225 */     write((byte)(i >> 24));
/*     */   }
/*     */ 
/*     */   void write(short s)
/*     */     throws IOException
/*     */   {
/* 235 */     write((byte)s);
/* 236 */     write((byte)(s >> 8));
/*     */   }
/*     */ 
/*     */   void write(long l)
/*     */     throws IOException
/*     */   {
/* 246 */     write((byte)(int)l);
/* 247 */     write((byte)(int)(l >> 8));
/* 248 */     write((byte)(int)(l >> 16));
/* 249 */     write((byte)(int)(l >> 24));
/* 250 */     write((byte)(int)(l >> 32));
/* 251 */     write((byte)(int)(l >> 40));
/* 252 */     write((byte)(int)(l >> 48));
/* 253 */     write((byte)(int)(l >> 56));
/*     */   }
/*     */ 
/*     */   void write(double f)
/*     */     throws IOException
/*     */   {
/* 263 */     long l = Double.doubleToLongBits(f);
/*     */ 
/* 265 */     write((byte)(int)l);
/* 266 */     write((byte)(int)(l >> 8));
/* 267 */     write((byte)(int)(l >> 16));
/* 268 */     write((byte)(int)(l >> 24));
/* 269 */     write((byte)(int)(l >> 32));
/* 270 */     write((byte)(int)(l >> 40));
/* 271 */     write((byte)(int)(l >> 48));
/* 272 */     write((byte)(int)(l >> 56));
/*     */   }
/*     */ 
/*     */   void write(float f)
/*     */     throws IOException
/*     */   {
/* 282 */     int l = Float.floatToIntBits(f);
/*     */ 
/* 284 */     write((byte)l);
/* 285 */     write((byte)(l >> 8));
/* 286 */     write((byte)(l >> 16));
/* 287 */     write((byte)(l >> 24));
/*     */   }
/*     */ 
/*     */   void write(String s)
/*     */     throws IOException
/*     */   {
/* 299 */     if (this.socket.getTdsVersion() >= 3) {
/* 300 */       int len = s.length();
/*     */ 
/* 302 */       for (int i = 0; i < len; ++i) {
/* 303 */         int c = s.charAt(i);
/*     */ 
/* 305 */         if (this.bufferPtr == this.buffer.length) {
/* 306 */           putPacket(0);
/*     */         }
/*     */ 
/* 309 */         this.buffer[(this.bufferPtr++)] = (byte)c;
/*     */ 
/* 311 */         if (this.bufferPtr == this.buffer.length) {
/* 312 */           putPacket(0);
/*     */         }
/*     */ 
/* 315 */         this.buffer[(this.bufferPtr++)] = (byte)(c >> 8);
/*     */       }
/*     */     } else {
/* 318 */       writeAscii(s);
/*     */     }
/*     */   }
/*     */ 
/*     */   void write(char[] s, int off, int len)
/*     */     throws IOException
/*     */   {
/* 329 */     int i = off;
/* 330 */     int limit = (off + len > s.length) ? s.length : off + len;
/*     */ 
/* 332 */     for (; i < limit; ++i) {
/* 333 */       char c = s[i];
/*     */ 
/* 335 */       if (this.bufferPtr == this.buffer.length) {
/* 336 */         putPacket(0);
/*     */       }
/*     */ 
/* 339 */       this.buffer[(this.bufferPtr++)] = (byte)c;
/*     */ 
/* 341 */       if (this.bufferPtr == this.buffer.length) {
/* 342 */         putPacket(0);
/*     */       }
/*     */ 
/* 345 */       this.buffer[(this.bufferPtr++)] = (byte)(c >> '\b');
/*     */     }
/*     */   }
/*     */ 
/*     */   void writeAscii(String s)
/*     */     throws IOException
/*     */   {
/* 356 */     String charsetName = this.socket.getCharset();
/*     */ 
/* 358 */     if (charsetName != null)
/*     */       try {
/* 360 */         write(s.getBytes(charsetName));
/*     */       } catch (UnsupportedEncodingException e) {
/* 362 */         write(s.getBytes());
/*     */       }
/*     */     else
/* 365 */       write(s.getBytes());
/*     */   }
/*     */ 
/*     */   void writeStreamBytes(InputStream in, int length)
/*     */     throws IOException
/*     */   {
/* 377 */     byte[] buffer = new byte[1024];
/*     */ 
/* 379 */     while (length > 0) {
/* 380 */       int res = in.read(buffer);
/*     */ 
/* 382 */       if (res < 0) {
/* 383 */         throw new IOException("Data in stream less than specified by length");
/*     */       }
/*     */ 
/* 387 */       write(buffer, 0, res);
/* 388 */       length -= res;
/*     */     }
/*     */ 
/* 392 */     if ((length < 0) || (in.read() >= 0))
/* 393 */       throw new IOException("More data in stream than specified by length");
/*     */   }
/*     */ 
/*     */   void writeReaderChars(Reader in, int length)
/*     */     throws IOException
/*     */   {
/* 406 */     char[] cbuffer = new char[512];
/* 407 */     byte[] bbuffer = new byte[1024];
/*     */ 
/* 409 */     while (length > 0) {
/* 410 */       int res = in.read(cbuffer);
/*     */ 
/* 412 */       if (res < 0) {
/* 413 */         throw new IOException("Data in stream less than specified by length");
/*     */       }
/*     */ 
/* 417 */       int i = 0; for (int j = -1; i < res; ++i) {
/* 418 */         bbuffer[(++j)] = (byte)cbuffer[i];
/* 419 */         bbuffer[(++j)] = (byte)(cbuffer[i] >> '\b');
/*     */       }
/*     */ 
/* 422 */       write(bbuffer, 0, res * 2);
/* 423 */       length -= res;
/*     */     }
/*     */ 
/* 427 */     if ((length < 0) || (in.read() >= 0))
/* 428 */       throw new IOException("More data in stream than specified by length");
/*     */   }
/*     */ 
/*     */   void writeReaderBytes(Reader in, int length)
/*     */     throws IOException
/*     */   {
/* 443 */     char[] buffer = new char[1024];
/*     */ 
/* 445 */     for (int i = 0; i < length; ) {
/* 446 */       int result = in.read(buffer);
/*     */ 
/* 448 */       if (result == -1) {
/* 449 */         throw new IOException("Data in stream less than specified by length");
/*     */       }
/* 451 */       if (i + result > length) {
/* 452 */         throw new IOException("More data in stream than specified by length");
/*     */       }
/*     */ 
/* 456 */       write(Support.encodeString(this.socket.getCharset(), new String(buffer, 0, result)));
/* 457 */       i += result;
/*     */     }
/*     */   }
/*     */ 
/*     */   void write(BigDecimal value)
/*     */     throws IOException
/*     */   {
/* 469 */     if (value == null) {
/* 470 */       write(0);
/*     */     } else {
/* 472 */       byte signum = (byte)((value.signum() < 0) ? 0 : 1);
/* 473 */       BigInteger bi = value.unscaledValue();
/* 474 */       byte[] mantisse = bi.abs().toByteArray();
/* 475 */       byte len = (byte)(mantisse.length + 1);
/*     */ 
/* 477 */       if (len > getMaxDecimalBytes())
/*     */       {
/* 479 */         throw new IOException("BigDecimal to big to send");
/*     */       }
/*     */       int i;
/* 482 */       if (this.socket.serverType == 2) {
/* 483 */         write(len);
/*     */ 
/* 486 */         write((byte)((signum == 0) ? 1 : 0));
/*     */ 
/* 488 */         for (i = 0; i < mantisse.length; ++i)
/* 489 */           write(mantisse[i]);
/*     */       }
/*     */       else {
/* 492 */         write(len);
/* 493 */         write(signum);
/*     */ 
/* 495 */         for (i = mantisse.length - 1; i >= 0; --i)
/* 496 */           write(mantisse[i]);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   void flush()
/*     */     throws IOException
/*     */   {
/* 508 */     putPacket(1);
/*     */   }
/*     */ 
/*     */   void close()
/*     */   {
/* 515 */     this.isClosed = true;
/*     */   }
/*     */ 
/*     */   int getTdsVersion()
/*     */   {
/* 524 */     return this.socket.getTdsVersion();
/*     */   }
/*     */ 
/*     */   int getServerType()
/*     */   {
/* 533 */     return this.socket.serverType;
/*     */   }
/*     */ 
/*     */   private void putPacket(int last)
/*     */     throws IOException
/*     */   {
/* 543 */     if (this.isClosed) {
/* 544 */       throw new IOException("RequestStream is closed");
/*     */     }
/*     */ 
/* 547 */     this.buffer[0] = this.pktType;
/* 548 */     this.buffer[1] = (byte)last;
/* 549 */     this.buffer[2] = (byte)(this.bufferPtr >> 8);
/* 550 */     this.buffer[3] = (byte)this.bufferPtr;
/* 551 */     this.buffer[4] = 0;
/* 552 */     this.buffer[5] = 0;
/* 553 */     this.buffer[6] = (byte)((this.socket.getTdsVersion() >= 3) ? 1 : 0);
/* 554 */     this.buffer[7] = 0;
/*     */ 
/* 556 */     if (Logger.isActive()) {
/* 557 */       Logger.logPacket(this.streamId, false, this.buffer);
/*     */     }
/*     */ 
/* 560 */     this.buffer = this.socket.sendNetPacket(this.streamId, this.buffer);
/* 561 */     this.bufferPtr = 8;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.RequestStream
 * JD-Core Version:    0.5.3
 */