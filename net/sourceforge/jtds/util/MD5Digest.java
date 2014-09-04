/*     */ package net.sourceforge.jtds.util;
/*     */ 
/*     */ public class MD5Digest extends GeneralDigest
/*     */ {
/*     */   private static final int DIGEST_LENGTH = 16;
/*     */   private int H1;
/*     */   private int H2;
/*     */   private int H3;
/*     */   private int H4;
/*  34 */   private int[] X = new int[16];
/*     */   private int xOff;
/*     */   private static final int S11 = 7;
/*     */   private static final int S12 = 12;
/*     */   private static final int S13 = 17;
/*     */   private static final int S14 = 22;
/*     */   private static final int S21 = 5;
/*     */   private static final int S22 = 9;
/*     */   private static final int S23 = 14;
/*     */   private static final int S24 = 20;
/*     */   private static final int S31 = 4;
/*     */   private static final int S32 = 11;
/*     */   private static final int S33 = 16;
/*     */   private static final int S34 = 23;
/*     */   private static final int S41 = 6;
/*     */   private static final int S42 = 10;
/*     */   private static final int S43 = 15;
/*     */   private static final int S44 = 21;
/*     */ 
/*     */   public MD5Digest()
/*     */   {
/*  42 */     reset();
/*     */   }
/*     */ 
/*     */   public MD5Digest(MD5Digest t)
/*     */   {
/*  51 */     super(t);
/*     */ 
/*  53 */     this.H1 = t.H1;
/*  54 */     this.H2 = t.H2;
/*  55 */     this.H3 = t.H3;
/*  56 */     this.H4 = t.H4;
/*     */ 
/*  58 */     System.arraycopy(t.X, 0, this.X, 0, t.X.length);
/*  59 */     this.xOff = t.xOff;
/*     */   }
/*     */ 
/*     */   public String getAlgorithmName()
/*     */   {
/*  64 */     return "MD5";
/*     */   }
/*     */ 
/*     */   public int getDigestSize()
/*     */   {
/*  69 */     return 16;
/*     */   }
/*     */ 
/*     */   protected void processWord(byte[] in, int inOff)
/*     */   {
/*  76 */     this.X[(this.xOff++)] = (in[inOff] & 0xFF | (in[(inOff + 1)] & 0xFF) << 8 | (in[(inOff + 2)] & 0xFF) << 16 | (in[(inOff + 3)] & 0xFF) << 24);
/*     */ 
/*  79 */     if (this.xOff != 16)
/*     */       return;
/*  81 */     processBlock();
/*     */   }
/*     */ 
/*     */   protected void processLength(long bitLength)
/*     */   {
/*  88 */     if (this.xOff > 14)
/*     */     {
/*  90 */       processBlock();
/*     */     }
/*     */ 
/*  93 */     this.X[14] = (int)(bitLength & 0xFFFFFFFF);
/*  94 */     this.X[15] = (int)(bitLength >>> 32);
/*     */   }
/*     */ 
/*     */   private void unpackWord(int word, byte[] out, int outOff)
/*     */   {
/* 102 */     out[outOff] = (byte)word;
/* 103 */     out[(outOff + 1)] = (byte)(word >>> 8);
/* 104 */     out[(outOff + 2)] = (byte)(word >>> 16);
/* 105 */     out[(outOff + 3)] = (byte)(word >>> 24);
/*     */   }
/*     */ 
/*     */   public int doFinal(byte[] out, int outOff)
/*     */   {
/* 112 */     finish();
/*     */ 
/* 114 */     unpackWord(this.H1, out, outOff);
/* 115 */     unpackWord(this.H2, out, outOff + 4);
/* 116 */     unpackWord(this.H3, out, outOff + 8);
/* 117 */     unpackWord(this.H4, out, outOff + 12);
/*     */ 
/* 119 */     reset();
/*     */ 
/* 121 */     return 16;
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 129 */     super.reset();
/*     */ 
/* 131 */     this.H1 = 1732584193;
/* 132 */     this.H2 = -271733879;
/* 133 */     this.H3 = -1732584194;
/* 134 */     this.H4 = 271733878;
/*     */ 
/* 136 */     this.xOff = 0;
/*     */ 
/* 138 */     for (int i = 0; i != this.X.length; ++i)
/*     */     {
/* 140 */       this.X[i] = 0;
/*     */     }
/*     */   }
/*     */ 
/*     */   private int rotateLeft(int x, int n)
/*     */   {
/* 183 */     return (x << n | x >>> 32 - n);
/*     */   }
/*     */ 
/*     */   private int F(int u, int v, int w)
/*     */   {
/* 194 */     return (u & v | (u ^ 0xFFFFFFFF) & w);
/*     */   }
/*     */ 
/*     */   private int G(int u, int v, int w)
/*     */   {
/* 202 */     return (u & w | v & (w ^ 0xFFFFFFFF));
/*     */   }
/*     */ 
/*     */   private int H(int u, int v, int w)
/*     */   {
/* 210 */     return (u ^ v ^ w);
/*     */   }
/*     */ 
/*     */   private int K(int u, int v, int w)
/*     */   {
/* 218 */     return (v ^ (u | w ^ 0xFFFFFFFF));
/*     */   }
/*     */ 
/*     */   protected void processBlock()
/*     */   {
/* 223 */     int a = this.H1;
/* 224 */     int b = this.H2;
/* 225 */     int c = this.H3;
/* 226 */     int d = this.H4;
/*     */ 
/* 231 */     a = rotateLeft(a + F(b, c, d) + this.X[0] + -680876936, 7) + b;
/* 232 */     d = rotateLeft(d + F(a, b, c) + this.X[1] + -389564586, 12) + a;
/* 233 */     c = rotateLeft(c + F(d, a, b) + this.X[2] + 606105819, 17) + d;
/* 234 */     b = rotateLeft(b + F(c, d, a) + this.X[3] + -1044525330, 22) + c;
/* 235 */     a = rotateLeft(a + F(b, c, d) + this.X[4] + -176418897, 7) + b;
/* 236 */     d = rotateLeft(d + F(a, b, c) + this.X[5] + 1200080426, 12) + a;
/* 237 */     c = rotateLeft(c + F(d, a, b) + this.X[6] + -1473231341, 17) + d;
/* 238 */     b = rotateLeft(b + F(c, d, a) + this.X[7] + -45705983, 22) + c;
/* 239 */     a = rotateLeft(a + F(b, c, d) + this.X[8] + 1770035416, 7) + b;
/* 240 */     d = rotateLeft(d + F(a, b, c) + this.X[9] + -1958414417, 12) + a;
/* 241 */     c = rotateLeft(c + F(d, a, b) + this.X[10] + -42063, 17) + d;
/* 242 */     b = rotateLeft(b + F(c, d, a) + this.X[11] + -1990404162, 22) + c;
/* 243 */     a = rotateLeft(a + F(b, c, d) + this.X[12] + 1804603682, 7) + b;
/* 244 */     d = rotateLeft(d + F(a, b, c) + this.X[13] + -40341101, 12) + a;
/* 245 */     c = rotateLeft(c + F(d, a, b) + this.X[14] + -1502002290, 17) + d;
/* 246 */     b = rotateLeft(b + F(c, d, a) + this.X[15] + 1236535329, 22) + c;
/*     */ 
/* 251 */     a = rotateLeft(a + G(b, c, d) + this.X[1] + -165796510, 5) + b;
/* 252 */     d = rotateLeft(d + G(a, b, c) + this.X[6] + -1069501632, 9) + a;
/* 253 */     c = rotateLeft(c + G(d, a, b) + this.X[11] + 643717713, 14) + d;
/* 254 */     b = rotateLeft(b + G(c, d, a) + this.X[0] + -373897302, 20) + c;
/* 255 */     a = rotateLeft(a + G(b, c, d) + this.X[5] + -701558691, 5) + b;
/* 256 */     d = rotateLeft(d + G(a, b, c) + this.X[10] + 38016083, 9) + a;
/* 257 */     c = rotateLeft(c + G(d, a, b) + this.X[15] + -660478335, 14) + d;
/* 258 */     b = rotateLeft(b + G(c, d, a) + this.X[4] + -405537848, 20) + c;
/* 259 */     a = rotateLeft(a + G(b, c, d) + this.X[9] + 568446438, 5) + b;
/* 260 */     d = rotateLeft(d + G(a, b, c) + this.X[14] + -1019803690, 9) + a;
/* 261 */     c = rotateLeft(c + G(d, a, b) + this.X[3] + -187363961, 14) + d;
/* 262 */     b = rotateLeft(b + G(c, d, a) + this.X[8] + 1163531501, 20) + c;
/* 263 */     a = rotateLeft(a + G(b, c, d) + this.X[13] + -1444681467, 5) + b;
/* 264 */     d = rotateLeft(d + G(a, b, c) + this.X[2] + -51403784, 9) + a;
/* 265 */     c = rotateLeft(c + G(d, a, b) + this.X[7] + 1735328473, 14) + d;
/* 266 */     b = rotateLeft(b + G(c, d, a) + this.X[12] + -1926607734, 20) + c;
/*     */ 
/* 271 */     a = rotateLeft(a + H(b, c, d) + this.X[5] + -378558, 4) + b;
/* 272 */     d = rotateLeft(d + H(a, b, c) + this.X[8] + -2022574463, 11) + a;
/* 273 */     c = rotateLeft(c + H(d, a, b) + this.X[11] + 1839030562, 16) + d;
/* 274 */     b = rotateLeft(b + H(c, d, a) + this.X[14] + -35309556, 23) + c;
/* 275 */     a = rotateLeft(a + H(b, c, d) + this.X[1] + -1530992060, 4) + b;
/* 276 */     d = rotateLeft(d + H(a, b, c) + this.X[4] + 1272893353, 11) + a;
/* 277 */     c = rotateLeft(c + H(d, a, b) + this.X[7] + -155497632, 16) + d;
/* 278 */     b = rotateLeft(b + H(c, d, a) + this.X[10] + -1094730640, 23) + c;
/* 279 */     a = rotateLeft(a + H(b, c, d) + this.X[13] + 681279174, 4) + b;
/* 280 */     d = rotateLeft(d + H(a, b, c) + this.X[0] + -358537222, 11) + a;
/* 281 */     c = rotateLeft(c + H(d, a, b) + this.X[3] + -722521979, 16) + d;
/* 282 */     b = rotateLeft(b + H(c, d, a) + this.X[6] + 76029189, 23) + c;
/* 283 */     a = rotateLeft(a + H(b, c, d) + this.X[9] + -640364487, 4) + b;
/* 284 */     d = rotateLeft(d + H(a, b, c) + this.X[12] + -421815835, 11) + a;
/* 285 */     c = rotateLeft(c + H(d, a, b) + this.X[15] + 530742520, 16) + d;
/* 286 */     b = rotateLeft(b + H(c, d, a) + this.X[2] + -995338651, 23) + c;
/*     */ 
/* 291 */     a = rotateLeft(a + K(b, c, d) + this.X[0] + -198630844, 6) + b;
/* 292 */     d = rotateLeft(d + K(a, b, c) + this.X[7] + 1126891415, 10) + a;
/* 293 */     c = rotateLeft(c + K(d, a, b) + this.X[14] + -1416354905, 15) + d;
/* 294 */     b = rotateLeft(b + K(c, d, a) + this.X[5] + -57434055, 21) + c;
/* 295 */     a = rotateLeft(a + K(b, c, d) + this.X[12] + 1700485571, 6) + b;
/* 296 */     d = rotateLeft(d + K(a, b, c) + this.X[3] + -1894986606, 10) + a;
/* 297 */     c = rotateLeft(c + K(d, a, b) + this.X[10] + -1051523, 15) + d;
/* 298 */     b = rotateLeft(b + K(c, d, a) + this.X[1] + -2054922799, 21) + c;
/* 299 */     a = rotateLeft(a + K(b, c, d) + this.X[8] + 1873313359, 6) + b;
/* 300 */     d = rotateLeft(d + K(a, b, c) + this.X[15] + -30611744, 10) + a;
/* 301 */     c = rotateLeft(c + K(d, a, b) + this.X[6] + -1560198380, 15) + d;
/* 302 */     b = rotateLeft(b + K(c, d, a) + this.X[13] + 1309151649, 21) + c;
/* 303 */     a = rotateLeft(a + K(b, c, d) + this.X[4] + -145523070, 6) + b;
/* 304 */     d = rotateLeft(d + K(a, b, c) + this.X[11] + -1120210379, 10) + a;
/* 305 */     c = rotateLeft(c + K(d, a, b) + this.X[2] + 718787259, 15) + d;
/* 306 */     b = rotateLeft(b + K(c, d, a) + this.X[9] + -343485551, 21) + c;
/*     */ 
/* 308 */     this.H1 += a;
/* 309 */     this.H2 += b;
/* 310 */     this.H3 += c;
/* 311 */     this.H4 += d;
/*     */ 
/* 316 */     this.xOff = 0;
/* 317 */     for (int i = 0; i != this.X.length; ++i)
/*     */     {
/* 319 */       this.X[i] = 0;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.util.MD5Digest
 * JD-Core Version:    0.5.3
 */