/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.sql.DataTruncation;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.SQLWarning;
/*     */ import java.util.HashMap;
/*     */ 
/*     */ class SQLDiagnostic
/*     */ {
/*  39 */   private static final HashMap mssqlStates = new HashMap();
/*     */ 
/*  46 */   private static final HashMap sybStates = new HashMap();
/*     */   private final int serverType;
/*     */   SQLException exceptions;
/*     */   SQLException lastException;
/*     */   SQLWarning warnings;
/*     */   SQLWarning lastWarning;
/*     */ 
/*     */   void addWarning(SQLWarning w)
/*     */   {
/* 336 */     if (this.warnings == null)
/* 337 */       this.warnings = w;
/*     */     else {
/* 339 */       this.lastWarning.setNextWarning(w);
/*     */     }
/* 341 */     this.lastWarning = w;
/*     */   }
/*     */ 
/*     */   void addException(SQLException e) {
/* 345 */     if (this.exceptions == null)
/* 346 */       this.exceptions = e;
/*     */     else {
/* 348 */       this.lastException.setNextException(e);
/*     */     }
/* 350 */     this.lastException = e;
/*     */   }
/*     */ 
/*     */   void addDiagnostic(int number, int state, int serverity, String message, String server, String procName, int line)
/*     */   {
/* 371 */     if (serverity > 10) {
/* 372 */       SQLException e = new SQLException(message, getStateCode(number, this.serverType, "S1000"), number);
/*     */ 
/* 378 */       if (((this.serverType == 1) && (((number == 8152) || (number == 8115) || (number == 220)))) || ((this.serverType == 2) && (((number == 247) || (number == 9502)))))
/*     */       {
/* 385 */         SQLException tmp = e;
/* 386 */         e = new DataTruncation(-1, false, false, -1, -1);
/*     */ 
/* 388 */         e.setNextException(tmp);
/*     */       }
/*     */ 
/* 391 */       addException(e);
/*     */     }
/*     */     else
/*     */     {
/*     */       SQLWarning w;
/* 393 */       if (number == 0)
/*     */       {
/* 395 */         w = new SQLWarning(message, null, 0);
/* 396 */         addWarning(w);
/*     */       } else {
/* 398 */         w = new SQLWarning(message, getStateCode(number, this.serverType, "01000"), number);
/*     */ 
/* 401 */         addWarning(w);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   void clearWarnings()
/*     */   {
/* 410 */     this.warnings = null;
/*     */   }
/*     */ 
/*     */   void checkErrors()
/*     */     throws SQLException
/*     */   {
/* 420 */     if (this.exceptions != null) {
/* 421 */       SQLException tmp = this.exceptions;
/* 422 */       this.exceptions = null;
/* 423 */       throw tmp;
/*     */     }
/*     */   }
/*     */ 
/*     */   SQLWarning getWarnings()
/*     */   {
/* 433 */     return this.warnings;
/*     */   }
/*     */ 
/*     */   SQLDiagnostic(int serverType)
/*     */   {
/* 442 */     this.serverType = serverType;
/*     */   }
/*     */ 
/*     */   private static String getStateCode(int number, int serverType, String defState)
/*     */   {
/* 456 */     HashMap stateTable = (serverType == 2) ? sybStates : mssqlStates;
/* 457 */     String state = (String)stateTable.get(new Integer(number));
/*     */ 
/* 459 */     if (state != null) {
/* 460 */       return state;
/*     */     }
/*     */ 
/* 463 */     return defState;
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*  52 */     mssqlStates.put(new Integer(102), "42000");
/*  53 */     mssqlStates.put(new Integer(105), "37000");
/*  54 */     mssqlStates.put(new Integer(109), "21S01");
/*  55 */     mssqlStates.put(new Integer(110), "21S01");
/*  56 */     mssqlStates.put(new Integer(113), "42000");
/*  57 */     mssqlStates.put(new Integer(131), "37000");
/*  58 */     mssqlStates.put(new Integer(168), "22003");
/*  59 */     mssqlStates.put(new Integer(170), "37000");
/*  60 */     mssqlStates.put(new Integer(174), "37000");
/*  61 */     mssqlStates.put(new Integer(195), "42000");
/*  62 */     mssqlStates.put(new Integer(201), "37000");
/*  63 */     mssqlStates.put(new Integer(206), "22005");
/*  64 */     mssqlStates.put(new Integer(207), "42S22");
/*  65 */     mssqlStates.put(new Integer(208), "S0002");
/*  66 */     mssqlStates.put(new Integer(210), "22007");
/*  67 */     mssqlStates.put(new Integer(211), "22008");
/*  68 */     mssqlStates.put(new Integer(213), "42000");
/*  69 */     mssqlStates.put(new Integer(220), "22003");
/*  70 */     mssqlStates.put(new Integer(229), "42000");
/*  71 */     mssqlStates.put(new Integer(230), "42000");
/*  72 */     mssqlStates.put(new Integer(232), "22003");
/*  73 */     mssqlStates.put(new Integer(233), "23000");
/*  74 */     mssqlStates.put(new Integer(234), "22003");
/*  75 */     mssqlStates.put(new Integer(235), "22005");
/*  76 */     mssqlStates.put(new Integer(236), "22003");
/*  77 */     mssqlStates.put(new Integer(237), "22003");
/*  78 */     mssqlStates.put(new Integer(238), "22003");
/*  79 */     mssqlStates.put(new Integer(241), "22007");
/*  80 */     mssqlStates.put(new Integer(242), "22008");
/*  81 */     mssqlStates.put(new Integer(244), "22003");
/*  82 */     mssqlStates.put(new Integer(245), "22018");
/*  83 */     mssqlStates.put(new Integer(246), "22003");
/*  84 */     mssqlStates.put(new Integer(247), "22005");
/*  85 */     mssqlStates.put(new Integer(248), "22003");
/*  86 */     mssqlStates.put(new Integer(249), "22005");
/*  87 */     mssqlStates.put(new Integer(256), "22005");
/*  88 */     mssqlStates.put(new Integer(257), "22005");
/*  89 */     mssqlStates.put(new Integer(260), "42000");
/*  90 */     mssqlStates.put(new Integer(262), "42000");
/*  91 */     mssqlStates.put(new Integer(266), "25000");
/*  92 */     mssqlStates.put(new Integer(272), "23000");
/*  93 */     mssqlStates.put(new Integer(273), "23000");
/*  94 */     mssqlStates.put(new Integer(277), "25000");
/*  95 */     mssqlStates.put(new Integer(295), "22007");
/*  96 */     mssqlStates.put(new Integer(296), "22008");
/*  97 */     mssqlStates.put(new Integer(298), "22008");
/*  98 */     mssqlStates.put(new Integer(305), "22005");
/*  99 */     mssqlStates.put(new Integer(307), "42S12");
/* 100 */     mssqlStates.put(new Integer(308), "42S12");
/* 101 */     mssqlStates.put(new Integer(310), "22025");
/* 102 */     mssqlStates.put(new Integer(409), "22005");
/* 103 */     mssqlStates.put(new Integer(506), "22019");
/* 104 */     mssqlStates.put(new Integer(512), "21000");
/* 105 */     mssqlStates.put(new Integer(515), "23000");
/* 106 */     mssqlStates.put(new Integer(517), "22008");
/* 107 */     mssqlStates.put(new Integer(518), "22005");
/* 108 */     mssqlStates.put(new Integer(519), "22003");
/* 109 */     mssqlStates.put(new Integer(520), "22003");
/* 110 */     mssqlStates.put(new Integer(521), "22003");
/* 111 */     mssqlStates.put(new Integer(522), "22003");
/* 112 */     mssqlStates.put(new Integer(523), "22003");
/* 113 */     mssqlStates.put(new Integer(524), "22003");
/* 114 */     mssqlStates.put(new Integer(529), "22005");
/* 115 */     mssqlStates.put(new Integer(530), "23000");
/* 116 */     mssqlStates.put(new Integer(532), "01001");
/* 117 */     mssqlStates.put(new Integer(535), "22003");
/* 118 */     mssqlStates.put(new Integer(542), "22008");
/* 119 */     mssqlStates.put(new Integer(544), "23000");
/* 120 */     mssqlStates.put(new Integer(547), "23000");
/* 121 */     mssqlStates.put(new Integer(550), "44000");
/* 122 */     mssqlStates.put(new Integer(611), "25000");
/* 123 */     mssqlStates.put(new Integer(626), "25000");
/* 124 */     mssqlStates.put(new Integer(627), "25000");
/* 125 */     mssqlStates.put(new Integer(628), "25000");
/* 126 */     mssqlStates.put(new Integer(911), "08004");
/* 127 */     mssqlStates.put(new Integer(1007), "22003");
/* 128 */     mssqlStates.put(new Integer(1010), "22019");
/* 129 */     mssqlStates.put(new Integer(1205), "40001");
/* 130 */     mssqlStates.put(new Integer(1211), "40001");
/* 131 */     mssqlStates.put(new Integer(1505), "23000");
/* 132 */     mssqlStates.put(new Integer(1508), "23000");
/* 133 */     mssqlStates.put(new Integer(1774), "21S02");
/* 134 */     mssqlStates.put(new Integer(1911), "42S22");
/* 135 */     mssqlStates.put(new Integer(1913), "42S11");
/* 136 */     mssqlStates.put(new Integer(2526), "37000");
/* 137 */     mssqlStates.put(new Integer(2557), "42000");
/* 138 */     mssqlStates.put(new Integer(2571), "42000");
/* 139 */     mssqlStates.put(new Integer(2601), "23000");
/* 140 */     mssqlStates.put(new Integer(2615), "23000");
/* 141 */     mssqlStates.put(new Integer(2625), "40001");
/* 142 */     mssqlStates.put(new Integer(2626), "23000");
/* 143 */     mssqlStates.put(new Integer(2627), "23000");
/* 144 */     mssqlStates.put(new Integer(2714), "S0001");
/* 145 */     mssqlStates.put(new Integer(2760), "42000");
/* 146 */     mssqlStates.put(new Integer(2812), "37000");
/* 147 */     mssqlStates.put(new Integer(3110), "42000");
/* 148 */     mssqlStates.put(new Integer(3309), "40001");
/* 149 */     mssqlStates.put(new Integer(3604), "23000");
/* 150 */     mssqlStates.put(new Integer(3605), "23000");
/* 151 */     mssqlStates.put(new Integer(3606), "22003");
/* 152 */     mssqlStates.put(new Integer(3607), "22012");
/* 153 */     mssqlStates.put(new Integer(3621), "01000");
/* 154 */     mssqlStates.put(new Integer(3701), "42S02");
/* 155 */     mssqlStates.put(new Integer(3704), "42000");
/* 156 */     mssqlStates.put(new Integer(3725), "23000");
/* 157 */     mssqlStates.put(new Integer(3726), "23000");
/* 158 */     mssqlStates.put(new Integer(3902), "25000");
/* 159 */     mssqlStates.put(new Integer(3903), "25000");
/* 160 */     mssqlStates.put(new Integer(3906), "25000");
/* 161 */     mssqlStates.put(new Integer(3908), "25000");
/* 162 */     mssqlStates.put(new Integer(3915), "25000");
/* 163 */     mssqlStates.put(new Integer(3916), "25000");
/* 164 */     mssqlStates.put(new Integer(3918), "25000");
/* 165 */     mssqlStates.put(new Integer(3919), "25000");
/* 166 */     mssqlStates.put(new Integer(3921), "25000");
/* 167 */     mssqlStates.put(new Integer(3922), "25000");
/* 168 */     mssqlStates.put(new Integer(3926), "25000");
/* 169 */     mssqlStates.put(new Integer(3960), "S0005");
/* 170 */     mssqlStates.put(new Integer(4415), "44000");
/* 171 */     mssqlStates.put(new Integer(4613), "42000");
/* 172 */     mssqlStates.put(new Integer(4618), "42000");
/* 173 */     mssqlStates.put(new Integer(4712), "23000");
/* 174 */     mssqlStates.put(new Integer(4834), "42000");
/* 175 */     mssqlStates.put(new Integer(4924), "42S22");
/* 176 */     mssqlStates.put(new Integer(4925), "42S21");
/* 177 */     mssqlStates.put(new Integer(4926), "42S22");
/* 178 */     mssqlStates.put(new Integer(5011), "42000");
/* 179 */     mssqlStates.put(new Integer(5116), "42000");
/* 180 */     mssqlStates.put(new Integer(5146), "22003");
/* 181 */     mssqlStates.put(new Integer(5812), "42000");
/* 182 */     mssqlStates.put(new Integer(6004), "42000");
/* 183 */     mssqlStates.put(new Integer(6102), "42000");
/* 184 */     mssqlStates.put(new Integer(6104), "37000");
/* 185 */     mssqlStates.put(new Integer(6401), "25000");
/* 186 */     mssqlStates.put(new Integer(7112), "40001");
/* 187 */     mssqlStates.put(new Integer(7956), "42000");
/* 188 */     mssqlStates.put(new Integer(7969), "25000");
/* 189 */     mssqlStates.put(new Integer(8114), "37000");
/* 190 */     mssqlStates.put(new Integer(8115), "22003");
/* 191 */     mssqlStates.put(new Integer(8134), "22012");
/* 192 */     mssqlStates.put(new Integer(8144), "37000");
/* 193 */     mssqlStates.put(new Integer(8152), "22001");
/* 194 */     mssqlStates.put(new Integer(8162), "37000");
/* 195 */     mssqlStates.put(new Integer(8153), "01003");
/* 196 */     mssqlStates.put(new Integer(8506), "25000");
/* 197 */     mssqlStates.put(new Integer(10015), "22003");
/* 198 */     mssqlStates.put(new Integer(10033), "42S12");
/* 199 */     mssqlStates.put(new Integer(10055), "23000");
/* 200 */     mssqlStates.put(new Integer(10065), "23000");
/* 201 */     mssqlStates.put(new Integer(10095), "01001");
/* 202 */     mssqlStates.put(new Integer(11010), "42000");
/* 203 */     mssqlStates.put(new Integer(11011), "23000");
/* 204 */     mssqlStates.put(new Integer(11040), "23000");
/* 205 */     mssqlStates.put(new Integer(11045), "42000");
/* 206 */     mssqlStates.put(new Integer(14126), "42000");
/* 207 */     mssqlStates.put(new Integer(15247), "42000");
/* 208 */     mssqlStates.put(new Integer(15323), "42S12");
/* 209 */     mssqlStates.put(new Integer(15605), "42S11");
/* 210 */     mssqlStates.put(new Integer(15622), "42000");
/* 211 */     mssqlStates.put(new Integer(15626), "25000");
/* 212 */     mssqlStates.put(new Integer(15645), "42S22");
/* 213 */     mssqlStates.put(new Integer(16905), "24000");
/* 214 */     mssqlStates.put(new Integer(16909), "24000");
/* 215 */     mssqlStates.put(new Integer(16911), "24000");
/* 216 */     mssqlStates.put(new Integer(16917), "24000");
/* 217 */     mssqlStates.put(new Integer(16934), "24000");
/* 218 */     mssqlStates.put(new Integer(16946), "24000");
/* 219 */     mssqlStates.put(new Integer(16950), "24000");
/* 220 */     mssqlStates.put(new Integer(16999), "24000");
/* 221 */     mssqlStates.put(new Integer(17308), "42000");
/* 222 */     mssqlStates.put(new Integer(17571), "42000");
/* 223 */     mssqlStates.put(new Integer(18002), "42000");
/* 224 */     mssqlStates.put(new Integer(18452), "28000");
/* 225 */     mssqlStates.put(new Integer(18456), "28000");
/* 226 */     mssqlStates.put(new Integer(18833), "42S12");
/* 227 */     mssqlStates.put(new Integer(20604), "42000");
/* 228 */     mssqlStates.put(new Integer(21049), "42000");
/* 229 */     mssqlStates.put(new Integer(21166), "42S22");
/* 230 */     mssqlStates.put(new Integer(21255), "42S21");
/*     */ 
/* 235 */     sybStates.put(new Integer(102), "37000");
/* 236 */     sybStates.put(new Integer(109), "21S01");
/* 237 */     sybStates.put(new Integer(110), "21S01");
/* 238 */     sybStates.put(new Integer(113), "42000");
/* 239 */     sybStates.put(new Integer(168), "22003");
/* 240 */     sybStates.put(new Integer(201), "37000");
/* 241 */     sybStates.put(new Integer(207), "42S22");
/* 242 */     sybStates.put(new Integer(208), "42S02");
/* 243 */     sybStates.put(new Integer(213), "21S01");
/* 244 */     sybStates.put(new Integer(220), "22003");
/* 245 */     sybStates.put(new Integer(227), "22003");
/* 246 */     sybStates.put(new Integer(229), "42000");
/* 247 */     sybStates.put(new Integer(230), "42000");
/* 248 */     sybStates.put(new Integer(232), "22003");
/* 249 */     sybStates.put(new Integer(233), "23000");
/* 250 */     sybStates.put(new Integer(245), "22018");
/* 251 */     sybStates.put(new Integer(247), "22003");
/* 252 */     sybStates.put(new Integer(257), "37000");
/* 253 */     sybStates.put(new Integer(262), "42000");
/* 254 */     sybStates.put(new Integer(277), "25000");
/* 255 */     sybStates.put(new Integer(307), "42S12");
/* 256 */     sybStates.put(new Integer(512), "21000");
/* 257 */     sybStates.put(new Integer(517), "22008");
/* 258 */     sybStates.put(new Integer(535), "22008");
/* 259 */     sybStates.put(new Integer(542), "22008");
/* 260 */     sybStates.put(new Integer(544), "23000");
/* 261 */     sybStates.put(new Integer(545), "23000");
/* 262 */     sybStates.put(new Integer(546), "23000");
/* 263 */     sybStates.put(new Integer(547), "23000");
/* 264 */     sybStates.put(new Integer(548), "23000");
/* 265 */     sybStates.put(new Integer(549), "23000");
/* 266 */     sybStates.put(new Integer(550), "23000");
/* 267 */     sybStates.put(new Integer(558), "24000");
/* 268 */     sybStates.put(new Integer(559), "24000");
/* 269 */     sybStates.put(new Integer(562), "24000");
/* 270 */     sybStates.put(new Integer(565), "24000");
/* 271 */     sybStates.put(new Integer(583), "24000");
/* 272 */     sybStates.put(new Integer(611), "25000");
/* 273 */     sybStates.put(new Integer(627), "25000");
/* 274 */     sybStates.put(new Integer(628), "25000");
/* 275 */     sybStates.put(new Integer(641), "25000");
/* 276 */     sybStates.put(new Integer(642), "25000");
/* 277 */     sybStates.put(new Integer(911), "08004");
/* 278 */     sybStates.put(new Integer(1276), "25000");
/* 279 */     sybStates.put(new Integer(1505), "23000");
/* 280 */     sybStates.put(new Integer(1508), "23000");
/* 281 */     sybStates.put(new Integer(1715), "21S02");
/* 282 */     sybStates.put(new Integer(1720), "42S22");
/* 283 */     sybStates.put(new Integer(1913), "42S11");
/* 284 */     sybStates.put(new Integer(1921), "42S21");
/* 285 */     sybStates.put(new Integer(2526), "37000");
/* 286 */     sybStates.put(new Integer(2714), "42S01");
/* 287 */     sybStates.put(new Integer(2812), "37000");
/* 288 */     sybStates.put(new Integer(3606), "22003");
/* 289 */     sybStates.put(new Integer(3607), "22012");
/* 290 */     sybStates.put(new Integer(3621), "01000");
/* 291 */     sybStates.put(new Integer(3701), "42S02");
/* 292 */     sybStates.put(new Integer(3902), "25000");
/* 293 */     sybStates.put(new Integer(3903), "25000");
/* 294 */     sybStates.put(new Integer(4602), "42000");
/* 295 */     sybStates.put(new Integer(4603), "42000");
/* 296 */     sybStates.put(new Integer(4608), "42000");
/* 297 */     sybStates.put(new Integer(4934), "42S22");
/* 298 */     sybStates.put(new Integer(6104), "37000");
/* 299 */     sybStates.put(new Integer(6235), "24000");
/* 300 */     sybStates.put(new Integer(6259), "24000");
/* 301 */     sybStates.put(new Integer(6260), "24000");
/* 302 */     sybStates.put(new Integer(7010), "42S12");
/* 303 */     sybStates.put(new Integer(7327), "37000");
/* 304 */     sybStates.put(new Integer(9501), "01003");
/* 305 */     sybStates.put(new Integer(9502), "22001");
/* 306 */     sybStates.put(new Integer(10306), "42000");
/* 307 */     sybStates.put(new Integer(10323), "42000");
/* 308 */     sybStates.put(new Integer(10330), "42000");
/* 309 */     sybStates.put(new Integer(10331), "42000");
/* 310 */     sybStates.put(new Integer(10332), "42000");
/* 311 */     sybStates.put(new Integer(11021), "37000");
/* 312 */     sybStates.put(new Integer(11110), "42000");
/* 313 */     sybStates.put(new Integer(11113), "42000");
/* 314 */     sybStates.put(new Integer(11118), "42000");
/* 315 */     sybStates.put(new Integer(11121), "42000");
/* 316 */     sybStates.put(new Integer(17222), "42000");
/* 317 */     sybStates.put(new Integer(17223), "42000");
/* 318 */     sybStates.put(new Integer(18091), "42S12");
/* 319 */     sybStates.put(new Integer(18117), "42S22");
/* 320 */     sybStates.put(new Integer(18350), "42000");
/* 321 */     sybStates.put(new Integer(18351), "42000");
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.SQLDiagnostic
 * JD-Core Version:    0.5.3
 */