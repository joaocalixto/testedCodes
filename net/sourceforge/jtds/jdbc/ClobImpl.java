/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStream;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.Reader;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.io.Writer;
/*     */ import java.sql.Clob;
/*     */ import java.sql.SQLException;
/*     */ import net.sourceforge.jtds.util.BlobBuffer;
/*     */ 
/*     */ public class ClobImpl
/*     */   implements Clob
/*     */ {
/*     */   private static final String EMPTY_CLOB = "";
/*     */   private final BlobBuffer blobBuffer;
/*     */ 
/*     */   ClobImpl(ConnectionJDBC2 connection)
/*     */   {
/*  68 */     this(connection, "");
/*     */   }
/*     */ 
/*     */   ClobImpl(ConnectionJDBC2 connection, String str)
/*     */   {
/*  78 */     if (str == null) {
/*  79 */       throw new IllegalArgumentException("str cannot be null");
/*     */     }
/*  81 */     this.blobBuffer = new BlobBuffer(connection.getBufferDir(), connection.getLobBuffer());
/*     */     try {
/*  83 */       byte[] data = str.getBytes("UTF-16LE");
/*  84 */       this.blobBuffer.setBuffer(data, false);
/*     */     }
/*     */     catch (UnsupportedEncodingException e) {
/*  87 */       throw new IllegalStateException("UTF-16LE encoding is not supported.");
/*     */     }
/*     */   }
/*     */ 
/*     */   BlobBuffer getBlobBuffer()
/*     */   {
/*  97 */     return this.blobBuffer;
/*     */   }
/*     */ 
/*     */   public InputStream getAsciiStream()
/*     */     throws SQLException
/*     */   {
/* 105 */     return this.blobBuffer.getBinaryStream(true);
/*     */   }
/*     */ 
/*     */   public Reader getCharacterStream() throws SQLException {
/*     */     try {
/* 110 */       return new BufferedReader(new InputStreamReader(this.blobBuffer.getBinaryStream(false), "UTF-16LE"));
/*     */     }
/*     */     catch (UnsupportedEncodingException e)
/*     */     {
/* 114 */       throw new IllegalStateException("UTF-16LE encoding is not supported.");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getSubString(long pos, int length) throws SQLException
/*     */   {
/* 120 */     if (length == 0)
/* 121 */       return "";
/*     */     try
/*     */     {
/* 124 */       byte[] data = this.blobBuffer.getBytes((pos - 1L) * 2L + 1L, length * 2);
/* 125 */       return new String(data, "UTF-16LE");
/*     */     } catch (IOException e) {
/* 127 */       throw new SQLException(Messages.get("error.generic.ioerror", e.getMessage()), "HY000");
/*     */     }
/*     */   }
/*     */ 
/*     */   public long length()
/*     */     throws SQLException
/*     */   {
/* 134 */     return (this.blobBuffer.getLength() / 2L);
/*     */   }
/*     */ 
/*     */   public long position(String searchStr, long start) throws SQLException {
/* 138 */     if (searchStr == null) {
/* 139 */       throw new SQLException(Messages.get("error.clob.searchnull"), "HY009");
/*     */     }
/*     */     try
/*     */     {
/* 143 */       byte[] pattern = searchStr.getBytes("UTF-16LE");
/* 144 */       int pos = this.blobBuffer.position(pattern, (start - 1L) * 2L + 1L);
/* 145 */       return ((pos - 1) / 2 + 1);
/*     */     }
/*     */     catch (UnsupportedEncodingException e) {
/* 148 */       throw new IllegalStateException("UTF-16LE encoding is not supported.");
/*     */     }
/*     */   }
/*     */ 
/*     */   public long position(Clob searchStr, long start) throws SQLException
/*     */   {
/* 154 */     if (searchStr == null) {
/* 155 */       throw new SQLException(Messages.get("error.clob.searchnull"), "HY009");
/*     */     }
/*     */ 
/* 158 */     BlobBuffer bbuf = ((ClobImpl)searchStr).getBlobBuffer();
/* 159 */     byte[] pattern = bbuf.getBytes(1L, (int)bbuf.getLength());
/* 160 */     int pos = this.blobBuffer.position(pattern, (start - 1L) * 2L + 1L);
/* 161 */     return ((pos - 1) / 2 + 1);
/*     */   }
/*     */ 
/*     */   public OutputStream setAsciiStream(long pos) throws SQLException {
/* 165 */     return this.blobBuffer.setBinaryStream((pos - 1L) * 2L + 1L, true);
/*     */   }
/*     */ 
/*     */   public Writer setCharacterStream(long pos) throws SQLException {
/*     */     try {
/* 170 */       return new BufferedWriter(new OutputStreamWriter(this.blobBuffer.setBinaryStream((pos - 1L) * 2L + 1L, false), "UTF-16LE"));
/*     */     }
/*     */     catch (UnsupportedEncodingException e)
/*     */     {
/* 175 */       throw new IllegalStateException("UTF-16LE encoding is not supported.");
/*     */     }
/*     */   }
/*     */ 
/*     */   public int setString(long pos, String str) throws SQLException {
/* 180 */     if (str == null) {
/* 181 */       throw new SQLException(Messages.get("error.clob.strnull"), "HY009");
/*     */     }
/*     */ 
/* 184 */     return setString(pos, str, 0, str.length());
/*     */   }
/*     */ 
/*     */   public int setString(long pos, String str, int offset, int len) throws SQLException
/*     */   {
/* 189 */     if ((offset < 0) || (offset > str.length())) {
/* 190 */       throw new SQLException(Messages.get("error.blobclob.badoffset"), "HY090");
/*     */     }
/*     */ 
/* 193 */     if ((len < 0) || (offset + len > str.length())) {
/* 194 */       throw new SQLException(Messages.get("error.blobclob.badlen"), "HY090");
/*     */     }
/*     */     try
/*     */     {
/* 198 */       byte[] data = str.substring(offset, offset + len).getBytes("UTF-16LE");
/*     */ 
/* 202 */       return this.blobBuffer.setBytes((pos - 1L) * 2L + 1L, data, 0, data.length, false);
/*     */     }
/*     */     catch (UnsupportedEncodingException e)
/*     */     {
/* 206 */       throw new IllegalStateException("UTF-16LE encoding is not supported.");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void truncate(long len) throws SQLException
/*     */   {
/* 212 */     this.blobBuffer.truncate(len * 2L);
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.ClobImpl
 * JD-Core Version:    0.5.3
 */