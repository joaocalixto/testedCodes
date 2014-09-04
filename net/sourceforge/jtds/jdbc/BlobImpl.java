/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.sql.Blob;
/*     */ import java.sql.SQLException;
/*     */ import net.sourceforge.jtds.util.BlobBuffer;
/*     */ 
/*     */ public class BlobImpl
/*     */   implements Blob
/*     */ {
/*  39 */   private static final byte[] EMPTY_BLOB = new byte[0];
/*     */   private final BlobBuffer blobBuffer;
/*     */ 
/*     */   BlobImpl(ConnectionJDBC2 connection)
/*     */   {
/*  50 */     this(connection, EMPTY_BLOB);
/*     */   }
/*     */ 
/*     */   BlobImpl(ConnectionJDBC2 connection, byte[] bytes)
/*     */   {
/*  60 */     if (bytes == null) {
/*  61 */       throw new IllegalArgumentException("bytes cannot be null");
/*     */     }
/*     */ 
/*  64 */     this.blobBuffer = new BlobBuffer(connection.getBufferDir(), connection.getLobBuffer());
/*  65 */     this.blobBuffer.setBuffer(bytes, false);
/*     */   }
/*     */ 
/*     */   public InputStream getBinaryStream()
/*     */     throws SQLException
/*     */   {
/*  73 */     return this.blobBuffer.getBinaryStream(false);
/*     */   }
/*     */ 
/*     */   public byte[] getBytes(long pos, int length) throws SQLException {
/*  77 */     return this.blobBuffer.getBytes(pos, length);
/*     */   }
/*     */ 
/*     */   public long length() throws SQLException {
/*  81 */     return this.blobBuffer.getLength();
/*     */   }
/*     */ 
/*     */   public long position(byte[] pattern, long start) throws SQLException {
/*  85 */     return this.blobBuffer.position(pattern, start);
/*     */   }
/*     */ 
/*     */   public long position(Blob pattern, long start) throws SQLException {
/*  89 */     if (pattern == null) {
/*  90 */       throw new SQLException(Messages.get("error.blob.badpattern"), "HY009");
/*     */     }
/*  92 */     return this.blobBuffer.position(pattern.getBytes(1L, (int)pattern.length()), start);
/*     */   }
/*     */ 
/*     */   public OutputStream setBinaryStream(long pos) throws SQLException {
/*  96 */     return this.blobBuffer.setBinaryStream(pos, false);
/*     */   }
/*     */ 
/*     */   public int setBytes(long pos, byte[] bytes) throws SQLException {
/* 100 */     if (bytes == null) {
/* 101 */       throw new SQLException(Messages.get("error.blob.bytesnull"), "HY009");
/*     */     }
/* 103 */     return setBytes(pos, bytes, 0, bytes.length);
/*     */   }
/*     */ 
/*     */   public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException
/*     */   {
/* 108 */     if (bytes == null) {
/* 109 */       throw new SQLException(Messages.get("error.blob.bytesnull"), "HY009");
/*     */     }
/*     */ 
/* 114 */     return this.blobBuffer.setBytes(pos, bytes, offset, len, true);
/*     */   }
/*     */ 
/*     */   public void truncate(long len) throws SQLException {
/* 118 */     this.blobBuffer.truncate(len);
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.BlobImpl
 * JD-Core Version:    0.5.3
 */