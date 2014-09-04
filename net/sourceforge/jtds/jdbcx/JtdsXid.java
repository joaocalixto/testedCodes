/*     */ package net.sourceforge.jtds.jdbcx;
/*     */ 
/*     */ import javax.transaction.xa.Xid;
/*     */ import net.sourceforge.jtds.jdbc.Support;
/*     */ 
/*     */ public class JtdsXid
/*     */   implements Xid
/*     */ {
/*     */   public static final int XID_SIZE = 140;
/*     */   private final byte[] gtran;
/*     */   private final byte[] bqual;
/*     */   public final int fmtId;
/*     */   public int hash;
/*     */ 
/*     */   public JtdsXid(byte[] buf, int pos)
/*     */   {
/*  49 */     this.fmtId = (buf[pos] & 0xFF | (buf[(pos + 1)] & 0xFF) << 8 | (buf[(pos + 2)] & 0xFF) << 16 | (buf[(pos + 3)] & 0xFF) << 24);
/*     */ 
/*  53 */     int t = buf[(pos + 4)];
/*  54 */     int b = buf[(pos + 8)];
/*  55 */     this.gtran = new byte[t];
/*  56 */     this.bqual = new byte[b];
/*  57 */     System.arraycopy(buf, 12 + pos, this.gtran, 0, t);
/*  58 */     System.arraycopy(buf, 12 + t + pos, this.bqual, 0, b);
/*  59 */     calculateHash();
/*     */   }
/*     */ 
/*     */   public JtdsXid(byte[] global, byte[] branch)
/*     */   {
/*  69 */     this.fmtId = 0;
/*  70 */     this.gtran = global;
/*  71 */     this.bqual = branch;
/*  72 */     calculateHash();
/*     */   }
/*     */ 
/*     */   public JtdsXid(Xid xid)
/*     */   {
/*  79 */     this.fmtId = xid.getFormatId();
/*  80 */     this.gtran = new byte[xid.getGlobalTransactionId().length];
/*  81 */     System.arraycopy(xid.getGlobalTransactionId(), 0, this.gtran, 0, this.gtran.length);
/*  82 */     this.bqual = new byte[xid.getBranchQualifier().length];
/*  83 */     System.arraycopy(xid.getBranchQualifier(), 0, this.bqual, 0, this.bqual.length);
/*  84 */     calculateHash();
/*     */   }
/*     */ 
/*     */   private void calculateHash() {
/*  88 */     String x = Integer.toString(this.fmtId) + new String(this.gtran) + new String(this.bqual);
/*  89 */     this.hash = x.hashCode();
/*     */   }
/*     */ 
/*     */   public int hashCode()
/*     */   {
/*  98 */     return this.hash;
/*     */   }
/*     */ 
/*     */   public boolean equals(Object obj)
/*     */   {
/* 108 */     if (obj == this) {
/* 109 */       return true;
/*     */     }
/* 111 */     if (obj instanceof JtdsXid) {
/* 112 */       JtdsXid xobj = (JtdsXid)obj;
/*     */ 
/* 114 */       if ((this.gtran.length + this.bqual.length == xobj.gtran.length + xobj.bqual.length) && (this.fmtId == xobj.fmtId))
/*     */       {
/* 116 */         for (int i = 0; i < this.gtran.length; ++i) {
/* 117 */           if (this.gtran[i] != xobj.gtran[i]) {
/* 118 */             return false;
/*     */           }
/*     */         }
/*     */ 
/* 122 */         for (i = 0; i < this.bqual.length; ++i) {
/* 123 */           if (this.bqual[i] != xobj.bqual[i]) {
/* 124 */             return false;
/*     */           }
/*     */         }
/*     */ 
/* 128 */         return true;
/*     */       }
/*     */     }
/* 131 */     return false;
/*     */   }
/*     */ 
/*     */   public int getFormatId()
/*     */   {
/* 139 */     return this.fmtId;
/*     */   }
/*     */ 
/*     */   public byte[] getBranchQualifier() {
/* 143 */     return this.bqual;
/*     */   }
/*     */ 
/*     */   public byte[] getGlobalTransactionId() {
/* 147 */     return this.gtran;
/*     */   }
/*     */ 
/*     */   public String toString() {
/* 151 */     StringBuffer txt = new StringBuffer(256);
/* 152 */     txt.append("XID[Format=").append(this.fmtId).append(", Global=0x");
/* 153 */     txt.append(Support.toHex(this.gtran)).append(", Branch=0x");
/* 154 */     txt.append(Support.toHex(this.bqual)).append(']');
/* 155 */     return txt.toString();
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbcx.JtdsXid
 * JD-Core Version:    0.5.3
 */