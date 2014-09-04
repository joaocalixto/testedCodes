/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Time;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.GregorianCalendar;
/*     */ 
/*     */ public class DateTime
/*     */ {
/*  36 */   private static ThreadLocal calendar = new ThreadLocal() {
/*     */     protected synchronized Object initialValue() {
/*  38 */       return new GregorianCalendar();
/*     */     }
/*  36 */   };
/*     */   static final int DATE_NOT_USED = -2147483648;
/*     */   static final int TIME_NOT_USED = -2147483648;
/*     */   private int date;
/*     */   private int time;
/*     */   private short year;
/*     */   private short month;
/*     */   private short day;
/*     */   private short hour;
/*     */   private short minute;
/*     */   private short second;
/*     */   private short millis;
/*     */   private boolean unpacked;
/*     */   private String stringValue;
/*     */   private Timestamp tsValue;
/*     */   private java.sql.Date dateValue;
/*     */   private Time timeValue;
/*     */ 
/*     */   DateTime(int date, int time)
/*     */   {
/*  82 */     this.date = date;
/*  83 */     this.time = time;
/*     */   }
/*     */ 
/*     */   DateTime(short date, short time)
/*     */   {
/*  94 */     this.date = (date & 0xFFFF);
/*  95 */     this.time = (time * 60 * 300);
/*     */   }
/*     */ 
/*     */   DateTime(Timestamp ts)
/*     */     throws SQLException
/*     */   {
/* 105 */     this.tsValue = ts;
/* 106 */     GregorianCalendar cal = (GregorianCalendar)calendar.get();
/* 107 */     cal.setTime(ts);
/*     */ 
/* 109 */     if (cal.get(0) != 1) {
/* 110 */       cal.set(0, 1);
/* 111 */       throw new SQLException(Messages.get("error.datetime.range.era"), "22007");
/*     */     }
/*     */ 
/* 114 */     if (!(Driver.JDBC3))
/*     */     {
/* 116 */       cal.set(14, ts.getNanos() / 1000000);
/*     */     }
/*     */ 
/* 119 */     this.year = (short)cal.get(1);
/* 120 */     this.month = (short)(cal.get(2) + 1);
/* 121 */     this.day = (short)cal.get(5);
/* 122 */     this.hour = (short)cal.get(11);
/* 123 */     this.minute = (short)cal.get(12);
/* 124 */     this.second = (short)cal.get(13);
/* 125 */     this.millis = (short)cal.get(14);
/* 126 */     packDate();
/* 127 */     packTime();
/* 128 */     this.unpacked = true;
/*     */   }
/*     */ 
/*     */   DateTime(Time t)
/*     */     throws SQLException
/*     */   {
/* 138 */     this.timeValue = t;
/* 139 */     GregorianCalendar cal = (GregorianCalendar)calendar.get();
/* 140 */     cal.setTime(t);
/*     */ 
/* 142 */     if (cal.get(0) != 1) {
/* 143 */       cal.set(0, 1);
/* 144 */       throw new SQLException(Messages.get("error.datetime.range.era"), "22007");
/*     */     }
/*     */ 
/* 147 */     this.date = -2147483648;
/* 148 */     this.year = 1900;
/* 149 */     this.month = 1;
/* 150 */     this.day = 1;
/* 151 */     this.hour = (short)cal.get(11);
/* 152 */     this.minute = (short)cal.get(12);
/* 153 */     this.second = (short)cal.get(13);
/* 154 */     this.millis = (short)cal.get(14);
/* 155 */     packTime();
/* 156 */     this.year = 1970;
/* 157 */     this.month = 1;
/* 158 */     this.day = 1;
/* 159 */     this.unpacked = true;
/*     */   }
/*     */ 
/*     */   DateTime(java.sql.Date d)
/*     */     throws SQLException
/*     */   {
/* 169 */     this.dateValue = d;
/* 170 */     GregorianCalendar cal = (GregorianCalendar)calendar.get();
/* 171 */     cal.setTime(d);
/*     */ 
/* 173 */     if (cal.get(0) != 1) {
/* 174 */       cal.set(0, 1);
/* 175 */       throw new SQLException(Messages.get("error.datetime.range.era"), "22007");
/*     */     }
/*     */ 
/* 178 */     this.year = (short)cal.get(1);
/* 179 */     this.month = (short)(cal.get(2) + 1);
/* 180 */     this.day = (short)cal.get(5);
/* 181 */     this.hour = 0;
/* 182 */     this.minute = 0;
/* 183 */     this.second = 0;
/* 184 */     this.millis = 0;
/* 185 */     packDate();
/* 186 */     this.time = -2147483648;
/* 187 */     this.unpacked = true;
/*     */   }
/*     */ 
/*     */   int getDate()
/*     */   {
/* 196 */     return ((this.date == -2147483648) ? 0 : this.date);
/*     */   }
/*     */ 
/*     */   int getTime()
/*     */   {
/* 205 */     return ((this.time == -2147483648) ? 0 : this.time);
/*     */   }
/*     */ 
/*     */   private void unpackDateTime()
/*     */   {
/* 242 */     if (this.date == -2147483648) {
/* 243 */       this.year = 1970;
/* 244 */       this.month = 1;
/* 245 */       this.day = 1;
/*     */     }
/* 247 */     else if (this.date == 0)
/*     */     {
/* 250 */       this.year = 1900;
/* 251 */       this.month = 1;
/* 252 */       this.day = 1;
/*     */     } else {
/* 254 */       int l = this.date + 68569 + 2415021;
/* 255 */       int n = 4 * l / 146097;
/* 256 */       l -= (146097 * n + 3) / 4;
/* 257 */       int i = 4000 * (l + 1) / 1461001;
/* 258 */       l = l - (1461 * i / 4) + 31;
/* 259 */       int j = 80 * l / 2447;
/* 260 */       int k = l - (2447 * j / 80);
/* 261 */       l = j / 11;
/* 262 */       j = j + 2 - (12 * l);
/* 263 */       i = 100 * (n - 49) + i + l;
/* 264 */       this.year = (short)i;
/* 265 */       this.month = (short)j;
/* 266 */       this.day = (short)k;
/*     */     }
/*     */ 
/* 269 */     if (this.time == -2147483648) {
/* 270 */       this.hour = 0;
/* 271 */       this.minute = 0;
/* 272 */       this.second = 0;
/*     */     } else {
/* 274 */       int hours = this.time / 1080000;
/* 275 */       this.time -= hours * 1080000;
/* 276 */       int minutes = this.time / 18000;
/* 277 */       this.time -= minutes * 18000;
/* 278 */       int seconds = this.time / 300;
/* 279 */       this.time -= seconds * 300;
/* 280 */       this.time = Math.round(this.time * 1000 / 300.0F);
/* 281 */       this.hour = (short)hours;
/* 282 */       this.minute = (short)minutes;
/* 283 */       this.second = (short)seconds;
/* 284 */       this.millis = (short)this.time;
/*     */     }
/* 286 */     this.unpacked = true;
/*     */   }
/*     */ 
/*     */   public void packDate()
/*     */     throws SQLException
/*     */   {
/* 317 */     if ((this.year < 1753) || (this.year > 9999)) {
/* 318 */       throw new SQLException(Messages.get("error.datetime.range"), "22003");
/*     */     }
/* 320 */     this.date = (this.day - 32075 + 1461 * (this.year + 4800 + (this.month - 14) / 12) / 4 + 367 * (this.month - 2 - ((this.month - 14) / 12 * 12)) / 12 - (3 * (this.year + 4900 + (this.month - 14) / 12) / 100 / 4) - 2415021);
/*     */   }
/*     */ 
/*     */   public void packTime()
/*     */   {
/* 329 */     this.time = (this.hour * 1080000);
/* 330 */     this.time += this.minute * 18000;
/* 331 */     this.time += this.second * 300;
/* 332 */     this.time += Math.round(this.millis * 300.0F / 1000.0F);
/* 333 */     if (this.time <= 25919999) {
/*     */       return;
/*     */     }
/* 336 */     this.time = 0;
/* 337 */     this.hour = 0;
/* 338 */     this.minute = 0;
/* 339 */     this.second = 0;
/* 340 */     this.millis = 0;
/* 341 */     if (this.date != -2147483648) {
/* 342 */       GregorianCalendar cal = (GregorianCalendar)calendar.get();
/* 343 */       cal.set(1, this.year);
/* 344 */       cal.set(2, this.month - 1);
/* 345 */       cal.set(5, this.day);
/* 346 */       cal.add(5, 1);
/* 347 */       this.year = (short)cal.get(1);
/* 348 */       this.month = (short)(cal.get(2) + 1);
/* 349 */       this.day = (short)cal.get(5);
/* 350 */       this.date += 1;
/*     */     }
/*     */   }
/*     */ 
/*     */   public Timestamp toTimestamp()
/*     */   {
/* 361 */     if (this.tsValue == null) {
/* 362 */       if (!(this.unpacked)) {
/* 363 */         unpackDateTime();
/*     */       }
/* 365 */       GregorianCalendar cal = (GregorianCalendar)calendar.get();
/* 366 */       cal.set(1, this.year);
/* 367 */       cal.set(2, this.month - 1);
/* 368 */       cal.set(5, this.day);
/* 369 */       cal.set(11, this.hour);
/* 370 */       cal.set(12, this.minute);
/* 371 */       cal.set(13, this.second);
/* 372 */       cal.set(14, this.millis);
/* 373 */       this.tsValue = new Timestamp(cal.getTime().getTime());
/*     */     }
/* 375 */     return this.tsValue;
/*     */   }
/*     */ 
/*     */   public java.sql.Date toDate()
/*     */   {
/* 384 */     if (this.dateValue == null) {
/* 385 */       if (!(this.unpacked)) {
/* 386 */         unpackDateTime();
/*     */       }
/* 388 */       GregorianCalendar cal = (GregorianCalendar)calendar.get();
/* 389 */       cal.set(1, this.year);
/* 390 */       cal.set(2, this.month - 1);
/* 391 */       cal.set(5, this.day);
/* 392 */       cal.set(11, 0);
/* 393 */       cal.set(12, 0);
/* 394 */       cal.set(13, 0);
/* 395 */       cal.set(14, 0);
/* 396 */       this.dateValue = new java.sql.Date(cal.getTime().getTime());
/*     */     }
/* 398 */     return this.dateValue;
/*     */   }
/*     */ 
/*     */   public Time toTime()
/*     */   {
/* 407 */     if (this.timeValue == null) {
/* 408 */       if (!(this.unpacked)) {
/* 409 */         unpackDateTime();
/*     */       }
/* 411 */       GregorianCalendar cal = (GregorianCalendar)calendar.get();
/* 412 */       cal.set(1, 1970);
/* 413 */       cal.set(2, 0);
/* 414 */       cal.set(5, 1);
/* 415 */       cal.set(11, this.hour);
/* 416 */       cal.set(12, this.minute);
/* 417 */       cal.set(13, this.second);
/* 418 */       cal.set(14, this.millis);
/* 419 */       this.timeValue = new Time(cal.getTime().getTime());
/*     */     }
/* 421 */     return this.timeValue;
/*     */   }
/*     */ 
/*     */   public Object toObject()
/*     */   {
/* 430 */     if (this.date == -2147483648) {
/* 431 */       return toTime();
/*     */     }
/* 433 */     if (this.time == -2147483648) {
/* 434 */       return toDate();
/*     */     }
/* 436 */     return toTimestamp();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 445 */     if (this.stringValue == null) {
/* 446 */       if (!(this.unpacked)) {
/* 447 */         unpackDateTime();
/*     */       }
/*     */ 
/* 453 */       int day = this.day;
/* 454 */       int month = this.month;
/* 455 */       int year = this.year;
/* 456 */       int millis = this.millis;
/* 457 */       int second = this.second;
/* 458 */       int minute = this.minute;
/* 459 */       int hour = this.hour;
/* 460 */       char[] buf = new char[23];
/* 461 */       int p = 0;
/* 462 */       if (this.date != -2147483648) {
/* 463 */         p = 10;
/* 464 */         buf[(--p)] = (char)(48 + day % 10);
/* 465 */         day /= 10;
/* 466 */         buf[(--p)] = (char)(48 + day % 10);
/* 467 */         buf[(--p)] = '-';
/* 468 */         buf[(--p)] = (char)(48 + month % 10);
/* 469 */         month /= 10;
/* 470 */         buf[(--p)] = (char)(48 + month % 10);
/* 471 */         buf[(--p)] = '-';
/* 472 */         buf[(--p)] = (char)(48 + year % 10);
/* 473 */         year /= 10;
/* 474 */         buf[(--p)] = (char)(48 + year % 10);
/* 475 */         year /= 10;
/* 476 */         buf[(--p)] = (char)(48 + year % 10);
/* 477 */         year /= 10;
/* 478 */         buf[(--p)] = (char)(48 + year % 10);
/* 479 */         p += 10;
/* 480 */         if (this.time != -2147483648) {
/* 481 */           buf[(p++)] = ' ';
/*     */         }
/*     */       }
/* 484 */       if (this.time != -2147483648) {
/* 485 */         p += 12;
/* 486 */         buf[(--p)] = (char)(48 + millis % 10);
/* 487 */         millis /= 10;
/* 488 */         buf[(--p)] = (char)(48 + millis % 10);
/* 489 */         millis /= 10;
/* 490 */         buf[(--p)] = (char)(48 + millis % 10);
/* 491 */         buf[(--p)] = '.';
/* 492 */         buf[(--p)] = (char)(48 + second % 10);
/* 493 */         second /= 10;
/* 494 */         buf[(--p)] = (char)(48 + second % 10);
/* 495 */         buf[(--p)] = ':';
/* 496 */         buf[(--p)] = (char)(48 + minute % 10);
/* 497 */         minute /= 10;
/* 498 */         buf[(--p)] = (char)(48 + minute % 10);
/* 499 */         buf[(--p)] = ':';
/* 500 */         buf[(--p)] = (char)(48 + hour % 10);
/* 501 */         hour /= 10;
/* 502 */         buf[(--p)] = (char)(48 + hour % 10);
/* 503 */         p += 12;
/* 504 */         if (buf[(p - 1)] == '0') {
/* 505 */           --p;
/*     */         }
/* 507 */         if (buf[(p - 1)] == '0') {
/* 508 */           --p;
/*     */         }
/*     */       }
/* 511 */       this.stringValue = String.valueOf(buf, 0, p);
/*     */     }
/* 513 */     return this.stringValue;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.DateTime
 * JD-Core Version:    0.5.3
 */