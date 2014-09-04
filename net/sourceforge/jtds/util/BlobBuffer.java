/*      */ package net.sourceforge.jtds.util;
/*      */ 
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.io.RandomAccessFile;
/*      */ import java.sql.SQLException;
/*      */ import net.sourceforge.jtds.jdbc.Messages;
/*      */ 
/*      */ public class BlobBuffer
/*      */ {
/*   64 */   private static final byte[] EMPTY_BUFFER = new byte[0];
/*      */   private static final int PAGE_SIZE = 1024;
/*      */   private static final int PAGE_MASK = -1024;
/*      */   private static final int BYTE_MASK = 1023;
/*      */   private static final int MAX_BUF_INC = 16384;
/*      */   private static final int INVALID_PAGE = -1;
/*      */   private byte[] buffer;
/*      */   private int length;
/*      */   private int currentPage;
/*      */   private File blobFile;
/*      */   private RandomAccessFile raFile;
/*      */   private boolean bufferDirty;
/*      */   private int openCount;
/*      */   private boolean isMemOnly;
/*      */   private final File bufferDir;
/*      */   private final int maxMemSize;
/*      */ 
/*      */   public BlobBuffer(File bufferDir, long maxMemSize)
/*      */   {
/*  134 */     this.bufferDir = bufferDir;
/*  135 */     this.maxMemSize = (int)maxMemSize;
/*  136 */     this.buffer = EMPTY_BUFFER;
/*      */   }
/*      */ 
/*      */   protected void finalize()
/*      */     throws Throwable
/*      */   {
/*      */     try
/*      */     {
/*  144 */       if (this.raFile != null)
/*  145 */         this.raFile.close();
/*      */     }
/*      */     catch (IOException e) {
/*      */     }
/*      */     finally {
/*  150 */       if (this.blobFile != null)
/*  151 */         this.blobFile.delete();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void createBlobFile()
/*      */   {
/*      */     try
/*      */     {
/*  165 */       this.blobFile = File.createTempFile("jtds", ".tmp", this.bufferDir);
/*      */ 
/*  167 */       this.raFile = new RandomAccessFile(this.blobFile, "rw");
/*  168 */       if (this.length > 0) {
/*  169 */         this.raFile.write(this.buffer, 0, this.length);
/*      */       }
/*  171 */       this.buffer = new byte[1024];
/*  172 */       this.currentPage = -1;
/*  173 */       this.openCount = 0;
/*      */     } catch (SecurityException e) {
/*  175 */       this.blobFile = null;
/*  176 */       this.raFile = null;
/*  177 */       this.isMemOnly = true;
/*  178 */       Logger.println("SecurityException creating BLOB file:");
/*  179 */       Logger.logException(e);
/*      */     } catch (IOException ioe) {
/*  181 */       this.blobFile = null;
/*  182 */       this.raFile = null;
/*  183 */       this.isMemOnly = true;
/*  184 */       Logger.println("IOException creating BLOB file:");
/*  185 */       Logger.logException(ioe);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void open()
/*      */     throws IOException
/*      */   {
/*  199 */     if ((this.raFile == null) && (this.blobFile != null))
/*      */     {
/*  201 */       this.raFile = new RandomAccessFile(this.blobFile, "rw");
/*  202 */       this.openCount = 1;
/*  203 */       this.currentPage = -1;
/*  204 */       this.buffer = new byte[1024];
/*  205 */       return;
/*      */     }
/*  207 */     if (this.raFile != null)
/*  208 */       this.openCount += 1;
/*      */   }
/*      */ 
/*      */   public int read(int readPtr)
/*      */     throws IOException
/*      */   {
/*  224 */     if (readPtr >= this.length)
/*      */     {
/*  226 */       return -1;
/*      */     }
/*  228 */     if (this.raFile != null)
/*      */     {
/*  230 */       if (this.currentPage != (readPtr & 0xFFFFFC00))
/*      */       {
/*  232 */         readPage(readPtr);
/*      */       }
/*      */ 
/*  236 */       return (this.buffer[(readPtr & 0x3FF)] & 0xFF);
/*      */     }
/*      */ 
/*  239 */     return (this.buffer[readPtr] & 0xFF);
/*      */   }
/*      */ 
/*      */   public int read(int readPtr, byte[] bytes, int offset, int len)
/*      */     throws IOException
/*      */   {
/*  256 */     if (bytes == null)
/*  257 */       throw new NullPointerException();
/*  258 */     if ((offset < 0) || (offset > bytes.length) || (len < 0) || (offset + len > bytes.length) || (offset + len < 0))
/*      */     {
/*  260 */       throw new IndexOutOfBoundsException(); }
/*  261 */     if (len == 0) {
/*  262 */       return 0;
/*      */     }
/*  264 */     if (readPtr >= this.length)
/*      */     {
/*  266 */       return -1;
/*      */     }
/*      */ 
/*  269 */     if (this.raFile != null)
/*      */     {
/*  271 */       len = Math.min(this.length - readPtr, len);
/*  272 */       if (len >= 1024)
/*      */       {
/*  275 */         if (this.bufferDirty) {
/*  276 */           writePage(this.currentPage);
/*      */         }
/*  278 */         this.currentPage = -1;
/*  279 */         this.raFile.seek(readPtr);
/*  280 */         this.raFile.readFully(bytes, offset, len);
/*      */       }
/*      */       else
/*      */       {
/*  285 */         int count = len;
/*  286 */         while (count > 0) {
/*  287 */           if (this.currentPage != (readPtr & 0xFFFFFC00))
/*      */           {
/*  289 */             readPage(readPtr);
/*      */           }
/*  291 */           int inBuffer = Math.min(1024 - (readPtr & 0x3FF), count);
/*  292 */           System.arraycopy(this.buffer, readPtr & 0x3FF, bytes, offset, inBuffer);
/*  293 */           offset += inBuffer;
/*  294 */           readPtr += inBuffer;
/*  295 */           count -= inBuffer;
/*      */         }
/*      */       }
/*      */     }
/*      */     else {
/*  300 */       len = Math.min(this.length - readPtr, len);
/*  301 */       System.arraycopy(this.buffer, readPtr, bytes, offset, len);
/*      */     }
/*      */ 
/*  304 */     return len;
/*      */   }
/*      */ 
/*      */   public void write(int writePtr, int b)
/*      */     throws IOException
/*      */   {
/*  319 */     if (writePtr >= this.length) {
/*  320 */       if (writePtr > this.length)
/*      */       {
/*  323 */         throw new IOException("BLOB buffer has been truncated");
/*      */       }
/*      */ 
/*  327 */       if (++this.length < 0)
/*      */       {
/*  331 */         throw new IOException("BLOB may not exceed 2GB in size");
/*      */       }
/*      */     }
/*      */ 
/*  335 */     if (this.raFile != null)
/*      */     {
/*  337 */       if (this.currentPage != (writePtr & 0xFFFFFC00))
/*      */       {
/*  339 */         readPage(writePtr);
/*      */       }
/*  341 */       this.buffer[(writePtr & 0x3FF)] = (byte)b;
/*      */ 
/*  343 */       this.bufferDirty = true;
/*      */     }
/*      */     else {
/*  346 */       if (writePtr >= this.buffer.length) {
/*  347 */         growBuffer(writePtr + 1);
/*      */       }
/*  349 */       this.buffer[writePtr] = (byte)b;
/*      */     }
/*      */   }
/*      */ 
/*      */   void write(int writePtr, byte[] bytes, int offset, int len)
/*      */     throws IOException
/*      */   {
/*  365 */     if (bytes == null)
/*  366 */       throw new NullPointerException();
/*  367 */     if ((offset < 0) || (offset > bytes.length) || (len < 0) || (offset + len > bytes.length) || (offset + len < 0))
/*      */     {
/*  369 */       throw new IndexOutOfBoundsException(); }
/*  370 */     if (len == 0) {
/*  371 */       return;
/*      */     }
/*  373 */     if (writePtr + len > 2147483647L) {
/*  374 */       throw new IOException("BLOB may not exceed 2GB in size");
/*      */     }
/*  376 */     if (writePtr > this.length)
/*      */     {
/*  379 */       throw new IOException("BLOB buffer has been truncated");
/*      */     }
/*      */ 
/*  382 */     if (this.raFile != null)
/*      */     {
/*  385 */       if (len >= 1024)
/*      */       {
/*  388 */         if (this.bufferDirty) {
/*  389 */           writePage(this.currentPage);
/*      */         }
/*  391 */         this.currentPage = -1;
/*  392 */         this.raFile.seek(writePtr);
/*  393 */         this.raFile.write(bytes, offset, len);
/*  394 */         writePtr += len;
/*      */       }
/*      */       else
/*      */       {
/*  398 */         int count = len;
/*  399 */         while (count > 0)
/*      */         {
/*  401 */           if (this.currentPage != (writePtr & 0xFFFFFC00))
/*      */           {
/*  403 */             readPage(writePtr);
/*      */           }
/*  405 */           int inBuffer = Math.min(1024 - (writePtr & 0x3FF), count);
/*      */ 
/*  407 */           System.arraycopy(bytes, offset, this.buffer, writePtr & 0x3FF, inBuffer);
/*      */ 
/*  409 */           this.bufferDirty = true;
/*  410 */           offset += inBuffer;
/*  411 */           writePtr += inBuffer;
/*  412 */           count -= inBuffer;
/*      */         }
/*      */       }
/*      */     }
/*      */     else {
/*  417 */       if (writePtr + len > this.buffer.length) {
/*  418 */         growBuffer(writePtr + len);
/*      */       }
/*  420 */       System.arraycopy(bytes, offset, this.buffer, writePtr, len);
/*  421 */       writePtr += len;
/*      */     }
/*  423 */     if (writePtr > this.length)
/*  424 */       this.length = writePtr;
/*      */   }
/*      */ 
/*      */   public void readPage(int page)
/*      */     throws IOException
/*      */   {
/*  437 */     page &= -1024;
/*  438 */     if (this.bufferDirty) {
/*  439 */       writePage(this.currentPage);
/*      */     }
/*  441 */     if (page > this.raFile.length()) {
/*  442 */       throw new IOException("readPage: Invalid page number " + page);
/*      */     }
/*  444 */     this.currentPage = page;
/*      */ 
/*  447 */     this.raFile.seek(this.currentPage);
/*      */ 
/*  449 */     int count = 0;
/*      */     int res;
/*      */     do {
/*  451 */       res = this.raFile.read(this.buffer, count, this.buffer.length - count);
/*  452 */       count += ((res == -1) ? 0 : res); }
/*  453 */     while ((count < 1024) && (res != -1));
/*      */   }
/*      */ 
/*      */   public void writePage(int page)
/*      */     throws IOException
/*      */   {
/*  463 */     page &= -1024;
/*  464 */     if (page > this.raFile.length()) {
/*  465 */       throw new IOException("writePage: Invalid page number " + page);
/*      */     }
/*  467 */     if (this.buffer.length != 1024) {
/*  468 */       throw new IllegalStateException("writePage: buffer size invalid");
/*      */     }
/*  470 */     this.raFile.seek(page);
/*  471 */     this.raFile.write(this.buffer);
/*  472 */     this.bufferDirty = false;
/*      */   }
/*      */ 
/*      */   public void close()
/*      */     throws IOException
/*      */   {
/*  485 */     if (this.openCount > 0)
/*  486 */       if ((--this.openCount == 0) && (this.raFile != null)) {
/*  487 */         if (this.bufferDirty) {
/*  488 */           writePage(this.currentPage);
/*      */         }
/*  490 */         this.raFile.close();
/*  491 */         this.raFile = null;
/*      */ 
/*  493 */         this.buffer = EMPTY_BUFFER;
/*  494 */         this.currentPage = -1;
/*      */       }
/*      */   }
/*      */ 
/*      */   public void growBuffer(int minSize)
/*      */   {
/*  506 */     if (this.buffer.length == 0)
/*      */     {
/*  508 */       this.buffer = new byte[Math.max(1024, minSize)];
/*      */     }
/*      */     else
/*      */     {
/*      */       byte[] tmp;
/*  511 */       if ((this.buffer.length * 2 > minSize) && (this.buffer.length <= 16384))
/*  512 */         tmp = new byte[this.buffer.length * 2];
/*      */       else {
/*  514 */         tmp = new byte[minSize + 16384];
/*      */       }
/*      */ 
/*  517 */       System.arraycopy(this.buffer, 0, tmp, 0, this.buffer.length);
/*  518 */       this.buffer = tmp;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setBuffer(byte[] bytes, boolean copy)
/*      */   {
/*  529 */     if (copy) {
/*  530 */       this.buffer = new byte[bytes.length];
/*  531 */       System.arraycopy(bytes, 0, this.buffer, 0, this.buffer.length);
/*      */     } else {
/*  533 */       this.buffer = bytes;
/*      */     }
/*  535 */     this.length = this.buffer.length;
/*      */   }
/*      */ 
/*      */   public byte[] getBytes(long pos, int len)
/*      */     throws SQLException
/*      */   {
/*  936 */     pos -= 1L;
/*  937 */     if (pos < 0L) {
/*  938 */       throw new SQLException(Messages.get("error.blobclob.badpos"), "HY090");
/*      */     }
/*  940 */     if (pos > this.length) {
/*  941 */       throw new SQLException(Messages.get("error.blobclob.badposlen"), "HY090");
/*      */     }
/*  943 */     if (len < 0) {
/*  944 */       throw new SQLException(Messages.get("error.blobclob.badlen"), "HY090");
/*      */     }
/*  946 */     if (pos + len > this.length)
/*      */     {
/*  948 */       len = (int)(this.length - pos);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  959 */       byte[] data = new byte[len];
/*  960 */       if (this.blobFile == null)
/*      */       {
/*  962 */         System.arraycopy(this.buffer, (int)pos, data, 0, len);
/*      */       }
/*      */       else {
/*  965 */         InputStream is = new BlobInputStream(pos);
/*  966 */         int bc = is.read(data);
/*  967 */         is.close();
/*  968 */         if (bc != data.length) {
/*  969 */           throw new IOException("Unexpected EOF on BLOB data file bc=" + bc + " data.len=" + data.length);
/*      */         }
/*      */       }
/*      */ 
/*  973 */       return data;
/*      */     } catch (IOException e) {
/*  975 */       throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */     }
/*      */   }
/*      */ 
/*      */   public InputStream getBinaryStream(boolean ascii)
/*      */     throws SQLException
/*      */   {
/*      */     try
/*      */     {
/*  989 */       if (ascii) {
/*  990 */         return new AsciiInputStream(0L);
/*      */       }
/*  992 */       return new BlobInputStream(0L);
/*      */     }
/*      */     catch (IOException e) {
/*  995 */       throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */     }
/*      */   }
/*      */ 
/*      */   public InputStream getUnicodeStream()
/*      */     throws SQLException
/*      */   {
/*      */     try
/*      */     {
/* 1010 */       return new UnicodeInputStream(0L);
/*      */     } catch (IOException e) {
/* 1012 */       throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */     }
/*      */   }
/*      */ 
/*      */   public OutputStream setBinaryStream(long pos, boolean ascii)
/*      */     throws SQLException
/*      */   {
/* 1032 */     pos -= 1L;
/* 1033 */     if (pos < 0L) {
/* 1034 */       throw new SQLException(Messages.get("error.blobclob.badpos"), "HY090");
/*      */     }
/*      */ 
/* 1037 */     if (pos > this.length) {
/* 1038 */       throw new SQLException(Messages.get("error.blobclob.badposlen"), "HY090");
/*      */     }
/*      */     try
/*      */     {
/* 1042 */       if ((!(this.isMemOnly)) && (this.blobFile == null)) {
/* 1043 */         createBlobFile();
/*      */       }
/* 1045 */       if (ascii) {
/* 1046 */         return new AsciiOutputStream(pos);
/*      */       }
/* 1048 */       return new BlobOutputStream(pos);
/*      */     }
/*      */     catch (IOException e) {
/* 1051 */       throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */     }
/*      */   }
/*      */ 
/*      */   public int setBytes(long pos, byte[] bytes, int offset, int len, boolean copy)
/*      */     throws SQLException
/*      */   {
/* 1080 */     pos -= 1L;
/* 1081 */     if (pos < 0L) {
/* 1082 */       throw new SQLException(Messages.get("error.blobclob.badpos"), "HY090");
/*      */     }
/*      */ 
/* 1085 */     if (pos > this.length) {
/* 1086 */       throw new SQLException(Messages.get("error.blobclob.badposlen"), "HY090");
/*      */     }
/*      */ 
/* 1089 */     if (bytes == null) {
/* 1090 */       throw new SQLException(Messages.get("error.blob.bytesnull"), "HY009");
/*      */     }
/*      */ 
/* 1093 */     if ((offset < 0) || (offset > bytes.length)) {
/* 1094 */       throw new SQLException(Messages.get("error.blobclob.badoffset"), "HY090");
/*      */     }
/*      */ 
/* 1097 */     if ((len < 0) || (pos + len > 2147483647L) || (offset + len > bytes.length))
/*      */     {
/* 1099 */       throw new SQLException(Messages.get("error.blobclob.badlen"), "HY090");
/*      */     }
/*      */ 
/* 1107 */     if ((this.blobFile == null) && (pos == 0L) && (len >= this.length) && (len <= this.maxMemSize))
/*      */     {
/* 1110 */       if (copy) {
/* 1111 */         this.buffer = new byte[len];
/* 1112 */         System.arraycopy(bytes, offset, this.buffer, 0, len);
/*      */       }
/*      */       else {
/* 1115 */         this.buffer = bytes;
/*      */       }
/* 1117 */       this.length = len;
/* 1118 */       return len;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1125 */       if ((!(this.isMemOnly)) && (this.blobFile == null)) {
/* 1126 */         createBlobFile();
/*      */       }
/*      */ 
/* 1131 */       open();
/* 1132 */       int ptr = (int)pos;
/* 1133 */       write(ptr, bytes, offset, len);
/* 1134 */       close();
/* 1135 */       return len;
/*      */     } catch (IOException e) {
/* 1137 */       throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */     }
/*      */   }
/*      */ 
/*      */   public long getLength()
/*      */   {
/* 1149 */     return this.length;
/*      */   }
/*      */ 
/*      */   public void setLength(long length)
/*      */   {
/* 1158 */     this.length = (int)length;
/*      */   }
/*      */ 
/*      */   public void truncate(long len)
/*      */     throws SQLException
/*      */   {
/* 1168 */     if (len < 0L) {
/* 1169 */       throw new SQLException(Messages.get("error.blobclob.badlen"), "HY090");
/*      */     }
/*      */ 
/* 1172 */     if (len > this.length) {
/* 1173 */       throw new SQLException(Messages.get("error.blobclob.lentoolong"), "HY090");
/*      */     }
/*      */ 
/* 1177 */     this.length = (int)len;
/* 1178 */     if (len != 0L) {
/*      */       return;
/*      */     }
/*      */     try
/*      */     {
/* 1183 */       if (this.blobFile != null) {
/* 1184 */         if (this.raFile != null) {
/* 1185 */           this.raFile.close();
/*      */         }
/* 1187 */         this.blobFile.delete();
/*      */       }
/*      */     } catch (IOException e) {
/* 1190 */       throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */     }
/*      */     finally
/*      */     {
/* 1194 */       this.buffer = EMPTY_BUFFER;
/* 1195 */       this.blobFile = null;
/* 1196 */       this.raFile = null;
/* 1197 */       this.openCount = 0;
/* 1198 */       this.currentPage = -1;
/*      */     }
/*      */   }
/*      */ 
/*      */   public int position(byte[] pattern, long start)
/*      */     throws SQLException
/*      */   {
/*      */     try
/*      */     {
/* 1214 */       start -= 1L;
/* 1215 */       if (start < 0L) {
/* 1216 */         throw new SQLException(Messages.get("error.blobclob.badpos"), "HY090");
/*      */       }
/*      */ 
/* 1219 */       if (start >= this.length) {
/* 1220 */         throw new SQLException(Messages.get("error.blobclob.badposlen"), "HY090");
/*      */       }
/*      */ 
/* 1223 */       if (pattern == null) {
/* 1224 */         throw new SQLException(Messages.get("error.blob.badpattern"), "HY009");
/*      */       }
/*      */ 
/* 1227 */       if ((pattern.length == 0) || (this.length == 0) || (pattern.length > this.length))
/*      */       {
/* 1229 */         return -1;
/*      */       }
/*      */ 
/* 1232 */       int limit = this.length - pattern.length;
/*      */       int i;
/*      */       int p;
/* 1233 */       if (this.blobFile == null) {
/* 1234 */         for (i = (int)start; i <= limit; ++i)
/*      */         {
/* 1236 */           p = 0;
/* 1237 */           while ((p < pattern.length) && (this.buffer[(i + p)] == pattern[p]))
/* 1238 */             ++p;
/* 1239 */           if (p == pattern.length)
/* 1240 */             return (i + 1);
/*      */         }
/*      */       }
/*      */       else {
/* 1244 */         open();
/* 1245 */         for (i = (int)start; i <= limit; ++i)
/*      */         {
/* 1247 */           p = 0;
/* 1248 */           while ((p < pattern.length) && (read(i + p) == (pattern[p] & 0xFF)))
/* 1249 */             ++p;
/* 1250 */           if (p == pattern.length) {
/* 1251 */             close();
/* 1252 */             return (i + 1);
/*      */           }
/*      */         }
/* 1255 */         close();
/*      */       }
/* 1257 */       return -1;
/*      */     } catch (IOException e) {
/* 1259 */       throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*      */     }
/*      */   }
/*      */ 
/*      */   private class AsciiOutputStream extends OutputStream
/*      */   {
/*      */     private int writePtr;
/*      */     private boolean open;
/*      */ 
/*      */     AsciiOutputStream(long pos)
/*      */       throws IOException
/*      */     {
/*  879 */       BlobBuffer.this.open();
/*  880 */       this.open = true;
/*  881 */       this.writePtr = (int)pos;
/*      */     }
/*      */ 
/*      */     protected void finalize()
/*      */       throws Throwable
/*      */     {
/*  889 */       if (!(this.open)) return;
/*      */       try {
/*  891 */         close();
/*      */       } catch (IOException e) {
/*      */       }
/*      */       finally {
/*  895 */         super.finalize();
/*      */       }
/*      */     }
/*      */ 
/*      */     public void write(int b)
/*      */       throws IOException
/*      */     {
/*  907 */       BlobBuffer.this.write(this.writePtr++, b);
/*  908 */       BlobBuffer.this.write(this.writePtr++, 0);
/*      */     }
/*      */ 
/*      */     public void close()
/*      */       throws IOException
/*      */     {
/*  917 */       if (this.open) {
/*  918 */         BlobBuffer.this.close();
/*  919 */         this.open = false;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private class BlobOutputStream extends OutputStream
/*      */   {
/*      */     private int writePtr;
/*      */     private boolean open;
/*      */ 
/*      */     BlobOutputStream(long pos)
/*      */       throws IOException
/*      */     {
/*  807 */       BlobBuffer.this.open();
/*  808 */       this.open = true;
/*  809 */       this.writePtr = (int)pos;
/*      */     }
/*      */ 
/*      */     protected void finalize()
/*      */       throws Throwable
/*      */     {
/*  817 */       if (!(this.open)) return;
/*      */       try {
/*  819 */         close();
/*      */       } catch (IOException e) {
/*      */       }
/*      */       finally {
/*  823 */         super.finalize();
/*      */       }
/*      */     }
/*      */ 
/*      */     public void write(int b)
/*      */       throws IOException
/*      */     {
/*  835 */       BlobBuffer.this.write(this.writePtr++, b);
/*      */     }
/*      */ 
/*      */     public void write(byte[] bytes, int offset, int len)
/*      */       throws IOException
/*      */     {
/*  847 */       BlobBuffer.this.write(this.writePtr, bytes, offset, len);
/*  848 */       this.writePtr += len;
/*      */     }
/*      */ 
/*      */     public void close()
/*      */       throws IOException
/*      */     {
/*  857 */       if (this.open) {
/*  858 */         BlobBuffer.this.close();
/*  859 */         this.open = false;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private class AsciiInputStream extends InputStream
/*      */   {
/*      */     private int readPtr;
/*      */     private boolean open;
/*      */ 
/*      */     public AsciiInputStream(long pos)
/*      */       throws IOException
/*      */     {
/*  725 */       BlobBuffer.this.open();
/*  726 */       this.open = true;
/*  727 */       this.readPtr = (int)pos;
/*      */     }
/*      */ 
/*      */     protected void finalize()
/*      */       throws Throwable
/*      */     {
/*  735 */       if (!(this.open)) return;
/*      */       try {
/*  737 */         close();
/*      */       } catch (IOException e) {
/*      */       }
/*      */       finally {
/*  741 */         super.finalize();
/*      */       }
/*      */     }
/*      */ 
/*      */     public int available()
/*      */       throws IOException
/*      */     {
/*  752 */       return (((int)BlobBuffer.this.getLength() - this.readPtr) / 2);
/*      */     }
/*      */ 
/*      */     public int read()
/*      */       throws IOException
/*      */     {
/*  762 */       int b1 = BlobBuffer.this.read(this.readPtr);
/*  763 */       if (b1 >= 0) {
/*  764 */         this.readPtr += 1;
/*  765 */         int b2 = BlobBuffer.this.read(this.readPtr);
/*  766 */         if (b2 >= 0) {
/*  767 */           this.readPtr += 1;
/*  768 */           if ((b2 != 0) || (b1 > 127))
/*      */           {
/*  772 */             b1 = 63;
/*      */           }
/*  774 */           return b1;
/*      */         }
/*      */       }
/*  777 */       return -1;
/*      */     }
/*      */ 
/*      */     public void close()
/*      */       throws IOException
/*      */     {
/*  786 */       if (this.open) {
/*  787 */         BlobBuffer.this.close();
/*  788 */         this.open = false;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private class UnicodeInputStream extends InputStream
/*      */   {
/*      */     private int readPtr;
/*      */     private boolean open;
/*      */ 
/*      */     public UnicodeInputStream(long pos)
/*      */       throws IOException
/*      */     {
/*  644 */       BlobBuffer.this.open();
/*  645 */       this.open = true;
/*  646 */       this.readPtr = (int)pos;
/*      */     }
/*      */ 
/*      */     protected void finalize()
/*      */       throws Throwable
/*      */     {
/*  654 */       if (!(this.open)) return;
/*      */       try {
/*  656 */         close();
/*      */       } catch (IOException e) {
/*      */       }
/*      */       finally {
/*  660 */         super.finalize();
/*      */       }
/*      */     }
/*      */ 
/*      */     public int available()
/*      */       throws IOException
/*      */     {
/*  671 */       return ((int)BlobBuffer.this.getLength() - this.readPtr);
/*      */     }
/*      */ 
/*      */     public int read()
/*      */       throws IOException
/*      */     {
/*  685 */       int b = BlobBuffer.this.read(this.readPtr ^ 0x1);
/*  686 */       if (b >= 0) {
/*  687 */         this.readPtr += 1;
/*      */       }
/*  689 */       return b;
/*      */     }
/*      */ 
/*      */     public void close()
/*      */       throws IOException
/*      */     {
/*  698 */       if (this.open) {
/*  699 */         BlobBuffer.this.close();
/*  700 */         this.open = false;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private class BlobInputStream extends InputStream
/*      */   {
/*      */     private int readPtr;
/*      */     private boolean open;
/*      */ 
/*      */     public BlobInputStream(long pos)
/*      */       throws IOException
/*      */     {
/*  556 */       BlobBuffer.this.open();
/*  557 */       this.open = true;
/*  558 */       this.readPtr = (int)pos;
/*      */     }
/*      */ 
/*      */     protected void finalize()
/*      */       throws Throwable
/*      */     {
/*  566 */       if (!(this.open)) return;
/*      */       try {
/*  568 */         close();
/*      */       } catch (IOException e) {
/*      */       }
/*      */       finally {
/*  572 */         super.finalize();
/*      */       }
/*      */     }
/*      */ 
/*      */     public int available()
/*      */       throws IOException
/*      */     {
/*  583 */       return ((int)BlobBuffer.this.getLength() - this.readPtr);
/*      */     }
/*      */ 
/*      */     public int read()
/*      */       throws IOException
/*      */     {
/*  593 */       int b = BlobBuffer.this.read(this.readPtr);
/*  594 */       if (b >= 0) {
/*  595 */         this.readPtr += 1;
/*      */       }
/*  597 */       return b;
/*      */     }
/*      */ 
/*      */     public int read(byte[] bytes, int offset, int len)
/*      */       throws IOException
/*      */     {
/*  610 */       int b = BlobBuffer.this.read(this.readPtr, bytes, offset, len);
/*  611 */       if (b > 0) {
/*  612 */         this.readPtr += b;
/*      */       }
/*  614 */       return b;
/*      */     }
/*      */ 
/*      */     public void close()
/*      */       throws IOException
/*      */     {
/*  623 */       if (this.open) {
/*  624 */         BlobBuffer.this.close();
/*  625 */         this.open = false;
/*      */       }
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.util.BlobBuffer
 * JD-Core Version:    0.5.3
 */