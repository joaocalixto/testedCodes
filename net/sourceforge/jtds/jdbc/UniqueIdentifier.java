/*    */ package net.sourceforge.jtds.jdbc;
/*    */ 
/*    */ public class UniqueIdentifier
/*    */ {
/*    */   private final byte[] bytes;
/*    */ 
/*    */   public UniqueIdentifier(byte[] id)
/*    */   {
/* 33 */     this.bytes = id;
/*    */   }
/*    */ 
/*    */   public byte[] getBytes()
/*    */   {
/* 42 */     return ((byte[])this.bytes.clone());
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 52 */     byte[] tmp = this.bytes;
/*    */ 
/* 54 */     if (this.bytes.length == 16)
/*    */     {
/* 56 */       tmp = new byte[this.bytes.length];
/* 57 */       System.arraycopy(this.bytes, 0, tmp, 0, this.bytes.length);
/* 58 */       tmp[0] = this.bytes[3];
/* 59 */       tmp[1] = this.bytes[2];
/* 60 */       tmp[2] = this.bytes[1];
/* 61 */       tmp[3] = this.bytes[0];
/* 62 */       tmp[4] = this.bytes[5];
/* 63 */       tmp[5] = this.bytes[4];
/* 64 */       tmp[6] = this.bytes[7];
/* 65 */       tmp[7] = this.bytes[6];
/*    */     }
/*    */ 
/* 68 */     byte[] bb = new byte[1];
/*    */ 
/* 70 */     StringBuffer buf = new StringBuffer(36);
/*    */ 
/* 72 */     for (int i = 0; i < this.bytes.length; ++i) {
/* 73 */       bb[0] = tmp[i];
/* 74 */       buf.append(Support.toHex(bb));
/*    */ 
/* 76 */       if ((i == 3) || (i == 5) || (i == 7) || (i == 9)) {
/* 77 */         buf.append('-');
/*    */       }
/*    */     }
/*    */ 
/* 81 */     return buf.toString();
/*    */   }
/*    */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.UniqueIdentifier
 * JD-Core Version:    0.5.3
 */