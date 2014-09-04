/*     */ package net.sourceforge.jtds.util;
/*     */ 
/*     */ public class MD4Digest extends GeneralDigest
/*     */ {
/*     */   private static final int DIGEST_LENGTH = 16;
/*     */   private static final int S11 = 3;
/*     */   private static final int S12 = 7;
/*     */   private static final int S13 = 11;
/*     */   private static final int S14 = 19;
/*     */   private static final int S21 = 3;
/*     */   private static final int S22 = 5;
/*     */   private static final int S23 = 9;
/*     */   private static final int S24 = 13;
/*     */   private static final int S31 = 3;
/*     */   private static final int S32 = 9;
/*     */   private static final int S33 = 11;
/*     */   private static final int S34 = 15;
/*     */   private int H1;
/*     */   private int H2;
/*     */   private int H3;
/*     */   private int H4;
/*  62 */   private int[] X = new int[16];
/*     */   private int xOff;
/*     */ 
/*     */   public MD4Digest()
/*     */   {
/*  69 */     reset();
/*     */   }
/*     */ 
/*     */   public MD4Digest(MD4Digest t)
/*     */   {
/*  77 */     super(t);
/*     */ 
/*  79 */     this.H1 = t.H1;
/*  80 */     this.H2 = t.H2;
/*  81 */     this.H3 = t.H3;
/*  82 */     this.H4 = t.H4;
/*     */ 
/*  84 */     System.arraycopy(t.X, 0, this.X, 0, t.X.length);
/*  85 */     this.xOff = t.xOff;
/*     */   }
/*     */ 
/*     */   public String getAlgorithmName() {
/*  89 */     return "MD4";
/*     */   }
/*     */ 
/*     */   public int getDigestSize() {
/*  93 */     return 16;
/*     */   }
/*     */ 
/*     */   protected void processWord(byte[] in, int inOff) {
/*  97 */     this.X[(this.xOff++)] = (in[inOff] & 0xFF | (in[(inOff + 1)] & 0xFF) << 8 | (in[(inOff + 2)] & 0xFF) << 16 | (in[(inOff + 3)] & 0xFF) << 24);
/*     */ 
/* 100 */     if (this.xOff == 16)
/* 101 */       processBlock();
/*     */   }
/*     */ 
/*     */   protected void processLength(long bitLength)
/*     */   {
/* 106 */     if (this.xOff > 14) {
/* 107 */       processBlock();
/*     */     }
/*     */ 
/* 110 */     this.X[14] = (int)(bitLength & 0xFFFFFFFF);
/* 111 */     this.X[15] = (int)(bitLength >>> 32);
/*     */   }
/*     */ 
/*     */   private void unpackWord(int word, byte[] out, int outOff) {
/* 115 */     out[outOff] = (byte)word;
/* 116 */     out[(outOff + 1)] = (byte)(word >>> 8);
/* 117 */     out[(outOff + 2)] = (byte)(word >>> 16);
/* 118 */     out[(outOff + 3)] = (byte)(word >>> 24);
/*     */   }
/*     */ 
/*     */   public int doFinal(byte[] out, int outOff) {
/* 122 */     finish();
/*     */ 
/* 124 */     unpackWord(this.H1, out, outOff);
/* 125 */     unpackWord(this.H2, out, outOff + 4);
/* 126 */     unpackWord(this.H3, out, outOff + 8);
/* 127 */     unpackWord(this.H4, out, outOff + 12);
/*     */ 
/* 129 */     reset();
/*     */ 
/* 131 */     return 16;
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 138 */     super.reset();
/*     */ 
/* 140 */     this.H1 = 1732584193;
/* 141 */     this.H2 = -271733879;
/* 142 */     this.H3 = -1732584194;
/* 143 */     this.H4 = 271733878;
/*     */ 
/* 145 */     this.xOff = 0;
/*     */ 
/* 147 */     for (int i = 0; i != this.X.length; ++i)
/* 148 */       this.X[i] = 0;
/*     */   }
/*     */ 
/*     */   private int rotateLeft(int x, int n)
/*     */   {
/* 156 */     return (x << n | x >>> 32 - n);
/*     */   }
/*     */ 
/*     */   private int F(int u, int v, int w)
/*     */   {
/* 163 */     return (u & v | (u ^ 0xFFFFFFFF) & w);
/*     */   }
/*     */ 
/*     */   private int G(int u, int v, int w) {
/* 167 */     return (u & v | u & w | v & w);
/*     */   }
/*     */ 
/*     */   private int H(int u, int v, int w) {
/* 171 */     return (u ^ v ^ w);
/*     */   }
/*     */ 
/*     */   protected void processBlock() {
/* 175 */     int a = this.H1;
/* 176 */     int b = this.H2;
/* 177 */     int c = this.H3;
/* 178 */     int d = this.H4;
/*     */ 
/* 183 */     a = rotateLeft(a + F(b, c, d) + this.X[0], 3);
/* 184 */     d = rotateLeft(d + F(a, b, c) + this.X[1], 7);
/* 185 */     c = rotateLeft(c + F(d, a, b) + this.X[2], 11);
/* 186 */     b = rotateLeft(b + F(c, d, a) + this.X[3], 19);
/* 187 */     a = rotateLeft(a + F(b, c, d) + this.X[4], 3);
/* 188 */     d = rotateLeft(d + F(a, b, c) + this.X[5], 7);
/* 189 */     c = rotateLeft(c + F(d, a, b) + this.X[6], 11);
/* 190 */     b = rotateLeft(b + F(c, d, a) + this.X[7], 19);
/* 191 */     a = rotateLeft(a + F(b, c, d) + this.X[8], 3);
/* 192 */     d = rotateLeft(d + F(a, b, c) + this.X[9], 7);
/* 193 */     c = rotateLeft(c + F(d, a, b) + this.X[10], 11);
/* 194 */     b = rotateLeft(b + F(c, d, a) + this.X[11], 19);
/* 195 */     a = rotateLeft(a + F(b, c, d) + this.X[12], 3);
/* 196 */     d = rotateLeft(d + F(a, b, c) + this.X[13], 7);
/* 197 */     c = rotateLeft(c + F(d, a, b) + this.X[14], 11);
/* 198 */     b = rotateLeft(b + F(c, d, a) + this.X[15], 19);
/*     */ 
/* 203 */     a = rotateLeft(a + G(b, c, d) + this.X[0] + 1518500249, 3);
/* 204 */     d = rotateLeft(d + G(a, b, c) + this.X[4] + 1518500249, 5);
/* 205 */     c = rotateLeft(c + G(d, a, b) + this.X[8] + 1518500249, 9);
/* 206 */     b = rotateLeft(b + G(c, d, a) + this.X[12] + 1518500249, 13);
/* 207 */     a = rotateLeft(a + G(b, c, d) + this.X[1] + 1518500249, 3);
/* 208 */     d = rotateLeft(d + G(a, b, c) + this.X[5] + 1518500249, 5);
/* 209 */     c = rotateLeft(c + G(d, a, b) + this.X[9] + 1518500249, 9);
/* 210 */     b = rotateLeft(b + G(c, d, a) + this.X[13] + 1518500249, 13);
/* 211 */     a = rotateLeft(a + G(b, c, d) + this.X[2] + 1518500249, 3);
/* 212 */     d = rotateLeft(d + G(a, b, c) + this.X[6] + 1518500249, 5);
/* 213 */     c = rotateLeft(c + G(d, a, b) + this.X[10] + 1518500249, 9);
/* 214 */     b = rotateLeft(b + G(c, d, a) + this.X[14] + 1518500249, 13);
/* 215 */     a = rotateLeft(a + G(b, c, d) + this.X[3] + 1518500249, 3);
/* 216 */     d = rotateLeft(d + G(a, b, c) + this.X[7] + 1518500249, 5);
/* 217 */     c = rotateLeft(c + G(d, a, b) + this.X[11] + 1518500249, 9);
/* 218 */     b = rotateLeft(b + G(c, d, a) + this.X[15] + 1518500249, 13);
/*     */ 
/* 223 */     a = rotateLeft(a + H(b, c, d) + this.X[0] + 1859775393, 3);
/* 224 */     d = rotateLeft(d + H(a, b, c) + this.X[8] + 1859775393, 9);
/* 225 */     c = rotateLeft(c + H(d, a, b) + this.X[4] + 1859775393, 11);
/* 226 */     b = rotateLeft(b + H(c, d, a) + this.X[12] + 1859775393, 15);
/* 227 */     a = rotateLeft(a + H(b, c, d) + this.X[2] + 1859775393, 3);
/* 228 */     d = rotateLeft(d + H(a, b, c) + this.X[10] + 1859775393, 9);
/* 229 */     c = rotateLeft(c + H(d, a, b) + this.X[6] + 1859775393, 11);
/* 230 */     b = rotateLeft(b + H(c, d, a) + this.X[14] + 1859775393, 15);
/* 231 */     a = rotateLeft(a + H(b, c, d) + this.X[1] + 1859775393, 3);
/* 232 */     d = rotateLeft(d + H(a, b, c) + this.X[9] + 1859775393, 9);
/* 233 */     c = rotateLeft(c + H(d, a, b) + this.X[5] + 1859775393, 11);
/* 234 */     b = rotateLeft(b + H(c, d, a) + this.X[13] + 1859775393, 15);
/* 235 */     a = rotateLeft(a + H(b, c, d) + this.X[3] + 1859775393, 3);
/* 236 */     d = rotateLeft(d + H(a, b, c) + this.X[11] + 1859775393, 9);
/* 237 */     c = rotateLeft(c + H(d, a, b) + this.X[7] + 1859775393, 11);
/* 238 */     b = rotateLeft(b + H(c, d, a) + this.X[15] + 1859775393, 15);
/*     */ 
/* 240 */     this.H1 += a;
/* 241 */     this.H2 += b;
/* 242 */     this.H3 += c;
/* 243 */     this.H4 += d;
/*     */ 
/* 248 */     this.xOff = 0;
/*     */ 
/* 250 */     for (int i = 0; i != this.X.length; ++i)
/* 251 */       this.X[i] = 0;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.util.MD4Digest
 * JD-Core Version:    0.5.3
 */