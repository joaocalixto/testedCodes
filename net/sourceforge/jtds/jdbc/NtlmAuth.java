/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.Arrays;
/*     */ import net.sourceforge.jtds.util.DESEngine;
/*     */ import net.sourceforge.jtds.util.MD4Digest;
/*     */ import net.sourceforge.jtds.util.MD5Digest;
/*     */ 
/*     */ public class NtlmAuth
/*     */ {
/*     */   public static byte[] answerNtChallenge(String password, byte[] nonce)
/*     */     throws UnsupportedEncodingException
/*     */   {
/*  45 */     return encryptNonce(ntHash(password), nonce);
/*     */   }
/*     */ 
/*     */   public static byte[] answerLmChallenge(String pwd, byte[] nonce) throws UnsupportedEncodingException
/*     */   {
/*  50 */     byte[] password = convertPassword(pwd);
/*     */ 
/*  52 */     DESEngine d1 = new DESEngine(true, makeDESkey(password, 0));
/*  53 */     DESEngine d2 = new DESEngine(true, makeDESkey(password, 7));
/*  54 */     byte[] encrypted = new byte[21];
/*  55 */     Arrays.fill(encrypted, 0);
/*     */ 
/*  57 */     d1.processBlock(nonce, 0, encrypted, 0);
/*  58 */     d2.processBlock(nonce, 0, encrypted, 8);
/*     */ 
/*  60 */     return encryptNonce(encrypted, nonce);
/*     */   }
/*     */ 
/*     */   public static byte[] answerNtlmv2Challenge(String domain, String user, String password, byte[] nonce, byte[] targetInfo, byte[] clientNonce)
/*     */     throws UnsupportedEncodingException
/*     */   {
/*  71 */     return answerNtlmv2Challenge(domain, user, password, nonce, targetInfo, clientNonce, System.currentTimeMillis());
/*     */   }
/*     */ 
/*     */   public static byte[] answerNtlmv2Challenge(String domain, String user, String password, byte[] nonce, byte[] targetInfo, byte[] clientNonce, byte[] timestamp)
/*     */     throws UnsupportedEncodingException
/*     */   {
/*  82 */     byte[] hash = ntv2Hash(domain, user, password);
/*  83 */     byte[] blob = createBlob(targetInfo, clientNonce, timestamp);
/*  84 */     return lmv2Response(hash, blob, nonce);
/*     */   }
/*     */ 
/*     */   public static byte[] answerNtlmv2Challenge(String domain, String user, String password, byte[] nonce, byte[] targetInfo, byte[] clientNonce, long now)
/*     */     throws UnsupportedEncodingException
/*     */   {
/*  93 */     return answerNtlmv2Challenge(domain, user, password, nonce, targetInfo, clientNonce, createTimestamp(now));
/*     */   }
/*     */ 
/*     */   public static byte[] answerLmv2Challenge(String domain, String user, String password, byte[] nonce, byte[] clientNonce)
/*     */     throws UnsupportedEncodingException
/*     */   {
/* 102 */     byte[] hash = ntv2Hash(domain, user, password);
/* 103 */     return lmv2Response(hash, clientNonce, nonce);
/*     */   }
/*     */ 
/*     */   private static byte[] ntv2Hash(String domain, String user, String password)
/*     */     throws UnsupportedEncodingException
/*     */   {
/* 113 */     byte[] hash = ntHash(password);
/* 114 */     String identity = user.toUpperCase() + domain.toUpperCase();
/* 115 */     byte[] identityBytes = identity.getBytes("UnicodeLittleUnmarked");
/*     */ 
/* 117 */     return hmacMD5(identityBytes, hash);
/*     */   }
/*     */ 
/*     */   private static byte[] lmv2Response(byte[] hash, byte[] clientData, byte[] challenge)
/*     */   {
/* 136 */     byte[] data = new byte[challenge.length + clientData.length];
/* 137 */     System.arraycopy(challenge, 0, data, 0, challenge.length);
/* 138 */     System.arraycopy(clientData, 0, data, challenge.length, clientData.length);
/*     */ 
/* 140 */     byte[] mac = hmacMD5(data, hash);
/* 141 */     byte[] lmv2Response = new byte[mac.length + clientData.length];
/* 142 */     System.arraycopy(mac, 0, lmv2Response, 0, mac.length);
/* 143 */     System.arraycopy(clientData, 0, lmv2Response, mac.length, clientData.length);
/* 144 */     return lmv2Response;
/*     */   }
/*     */ 
/*     */   private static byte[] hmacMD5(byte[] data, byte[] key)
/*     */   {
/* 159 */     byte[] ipad = new byte[64];
/* 160 */     byte[] opad = new byte[64];
/* 161 */     for (int i = 0; i < 64; ++i) {
/* 162 */       ipad[i] = 54;
/* 163 */       opad[i] = 92;
/*     */     }
/* 165 */     for (i = key.length - 1; i >= 0; --i)
/*     */     {
/*     */       int tmp52_50 = i;
/*     */       byte[] tmp52_49 = ipad; tmp52_49[tmp52_50] = (byte)(tmp52_49[tmp52_50] ^ key[i]);
/*     */       int tmp64_62 = i;
/*     */       byte[] tmp64_61 = opad; tmp64_61[tmp64_62] = (byte)(tmp64_61[tmp64_62] ^ key[i]);
/*     */     }
/* 169 */     byte[] content = new byte[data.length + 64];
/* 170 */     System.arraycopy(ipad, 0, content, 0, 64);
/* 171 */     System.arraycopy(data, 0, content, 64, data.length);
/* 172 */     data = md5(content);
/* 173 */     content = new byte[data.length + 64];
/* 174 */     System.arraycopy(opad, 0, content, 0, 64);
/* 175 */     System.arraycopy(data, 0, content, 64, data.length);
/* 176 */     return md5(content);
/*     */   }
/*     */ 
/*     */   private static byte[] md5(byte[] data)
/*     */   {
/* 181 */     MD5Digest md5 = new MD5Digest();
/* 182 */     md5.update(data, 0, data.length);
/* 183 */     byte[] hash = new byte[16];
/* 184 */     md5.doFinal(hash, 0);
/* 185 */     return hash;
/*     */   }
/*     */ 
/*     */   public static byte[] createTimestamp(long time)
/*     */   {
/* 213 */     time += 11644473600000L;
/* 214 */     time *= 10000L;
/*     */ 
/* 217 */     byte[] timestamp = new byte[8];
/* 218 */     for (int i = 0; i < 8; ++i) {
/* 219 */       timestamp[i] = (byte)(int)time;
/* 220 */       time >>>= 8;
/*     */     }
/*     */ 
/* 223 */     return timestamp;
/*     */   }
/*     */ 
/*     */   private static byte[] createBlob(byte[] targetInformation, byte[] clientChallenge, byte[] timestamp)
/*     */   {
/* 239 */     byte[] blobSignature = { 1, 1, 0, 0 };
/*     */ 
/* 242 */     byte[] reserved = { 0, 0, 0, 0 };
/*     */ 
/* 245 */     byte[] unknown1 = { 0, 0, 0, 0 };
/*     */ 
/* 248 */     byte[] unknown2 = { 0, 0, 0, 0 };
/*     */ 
/* 252 */     byte[] blob = new byte[blobSignature.length + reserved.length + timestamp.length + clientChallenge.length + unknown1.length + targetInformation.length + unknown2.length];
/*     */ 
/* 256 */     int offset = 0;
/* 257 */     System.arraycopy(blobSignature, 0, blob, offset, blobSignature.length);
/* 258 */     offset += blobSignature.length;
/* 259 */     System.arraycopy(reserved, 0, blob, offset, reserved.length);
/* 260 */     offset += reserved.length;
/* 261 */     System.arraycopy(timestamp, 0, blob, offset, timestamp.length);
/* 262 */     offset += timestamp.length;
/* 263 */     System.arraycopy(clientChallenge, 0, blob, offset, clientChallenge.length);
/*     */ 
/* 265 */     offset += clientChallenge.length;
/* 266 */     System.arraycopy(unknown1, 0, blob, offset, unknown1.length);
/* 267 */     offset += unknown1.length;
/* 268 */     System.arraycopy(targetInformation, 0, blob, offset, targetInformation.length);
/*     */ 
/* 270 */     offset += targetInformation.length;
/* 271 */     System.arraycopy(unknown2, 0, blob, offset, unknown2.length);
/* 272 */     return blob;
/*     */   }
/*     */ 
/*     */   private static byte[] encryptNonce(byte[] key, byte[] nonce)
/*     */   {
/* 280 */     byte[] out = new byte[24];
/*     */ 
/* 282 */     DESEngine d1 = new DESEngine(true, makeDESkey(key, 0));
/* 283 */     DESEngine d2 = new DESEngine(true, makeDESkey(key, 7));
/* 284 */     DESEngine d3 = new DESEngine(true, makeDESkey(key, 14));
/*     */ 
/* 286 */     d1.processBlock(nonce, 0, out, 0);
/* 287 */     d2.processBlock(nonce, 0, out, 8);
/* 288 */     d3.processBlock(nonce, 0, out, 16);
/*     */ 
/* 290 */     return out;
/*     */   }
/*     */ 
/*     */   private static byte[] ntHash(String password)
/*     */     throws UnsupportedEncodingException
/*     */   {
/* 299 */     byte[] key = new byte[21];
/* 300 */     Arrays.fill(key, 0);
/* 301 */     byte[] pwd = password.getBytes("UnicodeLittleUnmarked");
/*     */ 
/* 304 */     MD4Digest md4 = new MD4Digest();
/* 305 */     md4.update(pwd, 0, pwd.length);
/* 306 */     md4.doFinal(key, 0);
/* 307 */     return key;
/*     */   }
/*     */ 
/*     */   private static byte[] convertPassword(String password)
/*     */     throws UnsupportedEncodingException
/*     */   {
/* 316 */     byte[] pwd = password.toUpperCase().getBytes("UTF8");
/*     */ 
/* 318 */     byte[] rtn = new byte[14];
/* 319 */     Arrays.fill(rtn, 0);
/* 320 */     System.arraycopy(pwd, 0, rtn, 0, (pwd.length > 14) ? 14 : pwd.length);
/*     */ 
/* 325 */     return rtn;
/*     */   }
/*     */ 
/*     */   private static byte[] makeDESkey(byte[] buf, int off)
/*     */   {
/* 333 */     byte[] ret = new byte[8];
/*     */ 
/* 335 */     ret[0] = (byte)(buf[(off + 0)] >> 1 & 0xFF);
/* 336 */     ret[1] = (byte)(((buf[(off + 0)] & 0x1) << 6 | (buf[(off + 1)] & 0xFF) >> 2 & 0xFF) & 0xFF);
/* 337 */     ret[2] = (byte)(((buf[(off + 1)] & 0x3) << 5 | (buf[(off + 2)] & 0xFF) >> 3 & 0xFF) & 0xFF);
/* 338 */     ret[3] = (byte)(((buf[(off + 2)] & 0x7) << 4 | (buf[(off + 3)] & 0xFF) >> 4 & 0xFF) & 0xFF);
/* 339 */     ret[4] = (byte)(((buf[(off + 3)] & 0xF) << 3 | (buf[(off + 4)] & 0xFF) >> 5 & 0xFF) & 0xFF);
/* 340 */     ret[5] = (byte)(((buf[(off + 4)] & 0x1F) << 2 | (buf[(off + 5)] & 0xFF) >> 6 & 0xFF) & 0xFF);
/* 341 */     ret[6] = (byte)(((buf[(off + 5)] & 0x3F) << 1 | (buf[(off + 6)] & 0xFF) >> 7 & 0xFF) & 0xFF);
/* 342 */     ret[7] = (byte)(buf[(off + 6)] & 0x7F);
/*     */ 
/* 344 */     for (int i = 0; i < 8; ++i) {
/* 345 */       ret[i] = (byte)(ret[i] << 1);
/*     */     }
/*     */ 
/* 348 */     return ret;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.NtlmAuth
 * JD-Core Version:    0.5.3
 */