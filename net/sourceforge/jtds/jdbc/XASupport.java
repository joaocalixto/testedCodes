/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.sql.Connection;
/*     */ import java.sql.SQLException;
/*     */ import javax.transaction.xa.XAException;
/*     */ import javax.transaction.xa.Xid;
/*     */ import net.sourceforge.jtds.jdbcx.JtdsXid;
/*     */ import net.sourceforge.jtds.util.Logger;
/*     */ 
/*     */ public class XASupport
/*     */ {
/*     */   private static final int XA_RMID = 1;
/*     */   private static final String TM_ID = "TM=JTDS,RmRecoveryGuid=434CDE1A-F747-4942-9584-04937455CAB4";
/*     */   private static final int XA_OPEN = 1;
/*     */   private static final int XA_CLOSE = 2;
/*     */   private static final int XA_START = 3;
/*     */   private static final int XA_END = 4;
/*     */   private static final int XA_ROLLBACK = 5;
/*     */   private static final int XA_PREPARE = 6;
/*     */   private static final int XA_COMMIT = 7;
/*     */   private static final int XA_RECOVER = 8;
/*     */   private static final int XA_FORGET = 9;
/*     */   private static final int XA_COMPLETE = 10;
/*     */   private static final int XA_TRACE = 0;
/*     */ 
/*     */   public static int xa_open(Connection connection)
/*     */     throws SQLException
/*     */   {
/*  74 */     ConnectionJDBC2 con = (ConnectionJDBC2)connection;
/*  75 */     if (con.isXaEmulation())
/*     */     {
/*  79 */       Logger.println("xa_open: emulating distributed transaction support");
/*  80 */       if (con.getXid() != null) {
/*  81 */         throw new SQLException(Messages.get("error.xasupport.activetran", "xa_open"), "HY000");
/*     */       }
/*     */ 
/*  85 */       con.setXaState(1);
/*  86 */       return 0;
/*     */     }
/*     */ 
/*  93 */     if ((((ConnectionJDBC2)connection).getServerType() != 1) || (((ConnectionJDBC2)connection).getTdsVersion() < 4))
/*     */     {
/*  95 */       throw new SQLException(Messages.get("error.xasupport.nodist"), "HY000");
/*     */     }
/*  97 */     Logger.println("xa_open: Using SQL2000 MSDTC to support distributed transactions");
/*     */ 
/* 101 */     int[] args = new int[5];
/* 102 */     args[1] = 1;
/* 103 */     args[2] = 0;
/* 104 */     args[3] = 1;
/* 105 */     args[4] = 0;
/*     */ 
/* 107 */     byte[][] id = ((ConnectionJDBC2)connection).sendXaPacket(args, "TM=JTDS,RmRecoveryGuid=434CDE1A-F747-4942-9584-04937455CAB4".getBytes());
/* 108 */     if ((args[0] != 0) || (id == null) || (id[0] == null) || (id[0].length != 4))
/*     */     {
/* 112 */       throw new SQLException(Messages.get("error.xasupport.badopen"), "HY000");
/*     */     }
/*     */ 
/* 115 */     return (id[0][0] & 0xFF | (id[0][1] & 0xFF) << 8 | (id[0][2] & 0xFF) << 16 | (id[0][3] & 0xFF) << 24);
/*     */   }
/*     */ 
/*     */   public static void xa_close(Connection connection, int xaConId)
/*     */     throws SQLException
/*     */   {
/* 130 */     ConnectionJDBC2 con = (ConnectionJDBC2)connection;
/* 131 */     if (con.isXaEmulation())
/*     */     {
/* 135 */       con.setXaState(0);
/* 136 */       if (con.getXid() != null) {
/* 137 */         con.setXid(null);
/*     */         try {
/* 139 */           con.rollback();
/*     */         } catch (SQLException e) {
/* 141 */           Logger.println("xa_close: rollback() returned " + e);
/*     */         }
/*     */         try {
/* 144 */           con.setAutoCommit(true);
/*     */         } catch (SQLException e) {
/* 146 */           Logger.println("xa_close: setAutoCommit() returned " + e);
/*     */         }
/* 148 */         throw new SQLException(Messages.get("error.xasupport.activetran", "xa_close"), "HY000");
/*     */       }
/*     */ 
/* 152 */       return;
/*     */     }
/*     */ 
/* 157 */     int[] args = new int[5];
/* 158 */     args[1] = 2;
/* 159 */     args[2] = xaConId;
/* 160 */     args[3] = 1;
/* 161 */     args[4] = 0;
/* 162 */     ((ConnectionJDBC2)connection).sendXaPacket(args, "TM=JTDS,RmRecoveryGuid=434CDE1A-F747-4942-9584-04937455CAB4".getBytes());
/*     */   }
/*     */ 
/*     */   public static void xa_start(Connection connection, int xaConId, Xid xid, int flags)
/*     */     throws XAException
/*     */   {
/* 178 */     ConnectionJDBC2 con = (ConnectionJDBC2)connection;
/* 179 */     if (con.isXaEmulation())
/*     */     {
/* 183 */       JtdsXid lxid = new JtdsXid(xid);
/* 184 */       if (con.getXaState() == 0)
/*     */       {
/* 186 */         raiseXAException(-6);
/*     */       }
/* 188 */       JtdsXid tran = (JtdsXid)con.getXid();
/* 189 */       if (tran != null) {
/* 190 */         if (tran.equals(lxid))
/* 191 */           raiseXAException(-8);
/*     */         else {
/* 193 */           raiseXAException(-6);
/*     */         }
/*     */       }
/* 196 */       if (flags != 0)
/*     */       {
/* 198 */         raiseXAException(-5);
/*     */       }
/*     */       try {
/* 201 */         connection.setAutoCommit(false);
/*     */       } catch (SQLException e) {
/* 203 */         raiseXAException(-3);
/*     */       }
/* 205 */       con.setXid(lxid);
/* 206 */       con.setXaState(3);
/* 207 */       return;
/*     */     }
/*     */ 
/* 212 */     int[] args = new int[5];
/* 213 */     args[1] = 3;
/* 214 */     args[2] = xaConId;
/* 215 */     args[3] = 1;
/* 216 */     args[4] = flags;
/*     */     try
/*     */     {
/* 219 */       byte[][] cookie = ((ConnectionJDBC2)connection).sendXaPacket(args, toBytesXid(xid));
/* 220 */       if ((args[0] == 0) && (cookie != null))
/* 221 */         ((ConnectionJDBC2)connection).enlistConnection(cookie[0]);
/*     */     }
/*     */     catch (SQLException e) {
/* 224 */       raiseXAException(e);
/*     */     }
/* 226 */     if (args[0] != 0)
/* 227 */       raiseXAException(args[0]);
/*     */   }
/*     */ 
/*     */   public static void xa_end(Connection connection, int xaConId, Xid xid, int flags)
/*     */     throws XAException
/*     */   {
/* 244 */     ConnectionJDBC2 con = (ConnectionJDBC2)connection;
/* 245 */     if (con.isXaEmulation())
/*     */     {
/* 249 */       JtdsXid lxid = new JtdsXid(xid);
/* 250 */       if (con.getXaState() != 3)
/*     */       {
/* 252 */         raiseXAException(-6);
/*     */       }
/* 254 */       JtdsXid tran = (JtdsXid)con.getXid();
/* 255 */       if ((tran == null) || (!(tran.equals(lxid)))) {
/* 256 */         raiseXAException(-4);
/*     */       }
/* 258 */       if ((flags != 67108864) && (flags != 536870912))
/*     */       {
/* 261 */         raiseXAException(-5);
/*     */       }
/* 263 */       con.setXaState(4);
/* 264 */       return;
/*     */     }
/*     */ 
/* 269 */     int[] args = new int[5];
/* 270 */     args[1] = 4;
/* 271 */     args[2] = xaConId;
/* 272 */     args[3] = 1;
/* 273 */     args[4] = flags;
/*     */     try {
/* 275 */       ((ConnectionJDBC2)connection).sendXaPacket(args, toBytesXid(xid));
/* 276 */       ((ConnectionJDBC2)connection).enlistConnection(null);
/*     */     } catch (SQLException e) {
/* 278 */       raiseXAException(e);
/*     */     }
/* 280 */     if (args[0] != 0)
/* 281 */       raiseXAException(args[0]);
/*     */   }
/*     */ 
/*     */   public static int xa_prepare(Connection connection, int xaConId, Xid xid)
/*     */     throws XAException
/*     */   {
/* 298 */     ConnectionJDBC2 con = (ConnectionJDBC2)connection;
/* 299 */     if (con.isXaEmulation())
/*     */     {
/* 305 */       JtdsXid lxid = new JtdsXid(xid);
/* 306 */       if (con.getXaState() != 4)
/*     */       {
/* 308 */         raiseXAException(-6);
/*     */       }
/* 310 */       JtdsXid tran = (JtdsXid)con.getXid();
/* 311 */       if ((tran == null) || (!(tran.equals(lxid)))) {
/* 312 */         raiseXAException(-4);
/*     */       }
/* 314 */       con.setXaState(6);
/* 315 */       Logger.println("xa_prepare: Warning: Two phase commit not available in XA emulation mode.");
/* 316 */       return 0;
/*     */     }
/*     */ 
/* 321 */     int[] args = new int[5];
/* 322 */     args[1] = 6;
/* 323 */     args[2] = xaConId;
/* 324 */     args[3] = 1;
/* 325 */     args[4] = 0;
/*     */     try {
/* 327 */       ((ConnectionJDBC2)connection).sendXaPacket(args, toBytesXid(xid));
/*     */     } catch (SQLException e) {
/* 329 */       raiseXAException(e);
/*     */     }
/* 331 */     if ((args[0] != 0) && (args[0] != 3)) {
/* 332 */       raiseXAException(args[0]);
/*     */     }
/* 334 */     return args[0];
/*     */   }
/*     */ 
/*     */   public static void xa_commit(Connection connection, int xaConId, Xid xid, boolean onePhase)
/*     */     throws XAException
/*     */   {
/* 350 */     ConnectionJDBC2 con = (ConnectionJDBC2)connection;
/* 351 */     if (con.isXaEmulation())
/*     */     {
/* 355 */       JtdsXid lxid = new JtdsXid(xid);
/* 356 */       if ((con.getXaState() != 4) && (con.getXaState() != 6))
/*     */       {
/* 359 */         raiseXAException(-6);
/*     */       }
/* 361 */       JtdsXid tran = (JtdsXid)con.getXid();
/* 362 */       if ((tran == null) || (!(tran.equals(lxid)))) {
/* 363 */         raiseXAException(-4);
/*     */       }
/* 365 */       con.setXid(null);
/*     */       try {
/* 367 */         con.commit();
/*     */       } catch (SQLException e) {
/* 369 */         raiseXAException(e);
/*     */       } finally {
/*     */         try {
/* 372 */           con.setAutoCommit(true);
/*     */         } catch (SQLException e) {
/* 374 */           Logger.println("xa_close: setAutoCommit() returned " + e);
/*     */         }
/* 376 */         con.setXaState(1);
/*     */       }
/* 378 */       return;
/*     */     }
/*     */ 
/* 383 */     int[] args = new int[5];
/* 384 */     args[1] = 7;
/* 385 */     args[2] = xaConId;
/* 386 */     args[3] = 1;
/* 387 */     args[4] = ((onePhase) ? 1073741824 : 0);
/*     */     try {
/* 389 */       ((ConnectionJDBC2)connection).sendXaPacket(args, toBytesXid(xid));
/*     */     } catch (SQLException e) {
/* 391 */       raiseXAException(e);
/*     */     }
/* 393 */     if (args[0] != 0)
/* 394 */       raiseXAException(args[0]);
/*     */   }
/*     */ 
/*     */   public static void xa_rollback(Connection connection, int xaConId, Xid xid)
/*     */     throws XAException
/*     */   {
/* 410 */     ConnectionJDBC2 con = (ConnectionJDBC2)connection;
/* 411 */     if (con.isXaEmulation())
/*     */     {
/* 415 */       JtdsXid lxid = new JtdsXid(xid);
/* 416 */       if ((con.getXaState() != 4) && (con.getXaState() != 6))
/*     */       {
/* 418 */         raiseXAException(-6);
/*     */       }
/* 420 */       JtdsXid tran = (JtdsXid)con.getXid();
/* 421 */       if ((tran == null) || (!(tran.equals(lxid)))) {
/* 422 */         raiseXAException(-4);
/*     */       }
/* 424 */       con.setXid(null);
/*     */       try {
/* 426 */         con.rollback();
/*     */       } catch (SQLException e) {
/* 428 */         raiseXAException(e);
/*     */       } finally {
/*     */         try {
/* 431 */           con.setAutoCommit(true);
/*     */         } catch (SQLException e) {
/* 433 */           Logger.println("xa_close: setAutoCommit() returned " + e);
/*     */         }
/* 435 */         con.setXaState(1);
/*     */       }
/* 437 */       return;
/*     */     }
/*     */ 
/* 442 */     int[] args = new int[5];
/* 443 */     args[1] = 5;
/* 444 */     args[2] = xaConId;
/* 445 */     args[3] = 1;
/* 446 */     args[4] = 0;
/*     */     try {
/* 448 */       ((ConnectionJDBC2)connection).sendXaPacket(args, toBytesXid(xid));
/*     */     } catch (SQLException e) {
/* 450 */       raiseXAException(e);
/*     */     }
/* 452 */     if (args[0] != 0)
/* 453 */       raiseXAException(args[0]);
/*     */   }
/*     */ 
/*     */   public static Xid[] xa_recover(Connection connection, int xaConId, int flags)
/*     */     throws XAException
/*     */   {
/* 472 */     ConnectionJDBC2 con = (ConnectionJDBC2)connection;
/* 473 */     if (con.isXaEmulation())
/*     */     {
/* 479 */       if ((flags != 16777216) && (flags != 8388608) && (flags != 0))
/*     */       {
/* 482 */         raiseXAException(-5);
/*     */       }
/* 484 */       return new JtdsXid[0];
/*     */     }
/*     */ 
/* 489 */     int[] args = new int[5];
/* 490 */     args[1] = 8;
/* 491 */     args[2] = xaConId;
/* 492 */     args[3] = 1;
/* 493 */     args[4] = 0;
/* 494 */     Xid[] list = null;
/*     */ 
/* 496 */     if (flags != 16777216) {
/* 497 */       return new JtdsXid[0];
/*     */     }
/*     */     try
/*     */     {
/* 501 */       byte[][] buffer = ((ConnectionJDBC2)connection).sendXaPacket(args, null);
/* 502 */       if (args[0] >= 0) {
/* 503 */         int n = buffer.length;
/* 504 */         list = new JtdsXid[n];
/* 505 */         for (int i = 0; i < n; ++i)
/* 506 */           list[i] = new JtdsXid(buffer[i], 0);
/*     */       }
/*     */     }
/*     */     catch (SQLException e) {
/* 510 */       raiseXAException(e);
/*     */     }
/* 512 */     if (args[0] < 0) {
/* 513 */       raiseXAException(args[0]);
/*     */     }
/* 515 */     if (list == null) {
/* 516 */       list = new JtdsXid[0];
/*     */     }
/* 518 */     return list;
/*     */   }
/*     */ 
/*     */   public static void xa_forget(Connection connection, int xaConId, Xid xid)
/*     */     throws XAException
/*     */   {
/* 533 */     ConnectionJDBC2 con = (ConnectionJDBC2)connection;
/* 534 */     if (con.isXaEmulation())
/*     */     {
/* 538 */       JtdsXid lxid = new JtdsXid(xid);
/* 539 */       JtdsXid tran = (JtdsXid)con.getXid();
/* 540 */       if ((tran == null) || (!(tran.equals(lxid)))) {
/* 541 */         raiseXAException(-4);
/*     */       }
/* 543 */       if ((con.getXaState() != 4) && (con.getXaState() != 6))
/*     */       {
/* 545 */         raiseXAException(-6);
/*     */       }
/* 547 */       con.setXid(null);
/*     */       try {
/* 549 */         con.rollback();
/*     */       } catch (SQLException e) {
/* 551 */         raiseXAException(e);
/*     */       } finally {
/*     */         try {
/* 554 */           con.setAutoCommit(true);
/*     */         } catch (SQLException e) {
/* 556 */           Logger.println("xa_close: setAutoCommit() returned " + e);
/*     */         }
/* 558 */         con.setXaState(1);
/*     */       }
/* 560 */       return;
/*     */     }
/*     */ 
/* 565 */     int[] args = new int[5];
/* 566 */     args[1] = 9;
/* 567 */     args[2] = xaConId;
/* 568 */     args[3] = 1;
/* 569 */     args[4] = 0;
/*     */     try {
/* 571 */       ((ConnectionJDBC2)connection).sendXaPacket(args, toBytesXid(xid));
/*     */     } catch (SQLException e) {
/* 573 */       raiseXAException(e);
/*     */     }
/* 575 */     if (args[0] != 0)
/* 576 */       raiseXAException(args[0]);
/*     */   }
/*     */ 
/*     */   public static void raiseXAException(SQLException sqle)
/*     */     throws XAException
/*     */   {
/* 590 */     XAException e = new XAException(sqle.getMessage());
/* 591 */     e.errorCode = -7;
/* 592 */     Logger.println("XAException: " + e.getMessage());
/* 593 */     throw e;
/*     */   }
/*     */ 
/*     */   public static void raiseXAException(int errorCode)
/*     */     throws XAException
/*     */   {
/* 605 */     String err = "xaerunknown";
/* 606 */     switch (errorCode)
/*     */     {
/*     */     case 100:
/* 608 */       err = "xarbrollback";
/* 609 */       break;
/*     */     case 101:
/* 611 */       err = "xarbcommfail";
/* 612 */       break;
/*     */     case 102:
/* 614 */       err = "xarbdeadlock";
/* 615 */       break;
/*     */     case 103:
/* 617 */       err = "xarbintegrity";
/* 618 */       break;
/*     */     case 104:
/* 620 */       err = "xarbother";
/* 621 */       break;
/*     */     case 105:
/* 623 */       err = "xarbproto";
/* 624 */       break;
/*     */     case 106:
/* 626 */       err = "xarbtimeout";
/* 627 */       break;
/*     */     case 107:
/* 629 */       err = "xarbtransient";
/* 630 */       break;
/*     */     case 9:
/* 632 */       err = "xanomigrate";
/* 633 */       break;
/*     */     case 8:
/* 635 */       err = "xaheurhaz";
/* 636 */       break;
/*     */     case 7:
/* 638 */       err = "xaheurcom";
/* 639 */       break;
/*     */     case 6:
/* 641 */       err = "xaheurrb";
/* 642 */       break;
/*     */     case 5:
/* 644 */       err = "xaheurmix";
/* 645 */       break;
/*     */     case 4:
/* 647 */       err = "xaretry";
/* 648 */       break;
/*     */     case 3:
/* 650 */       err = "xardonly";
/* 651 */       break;
/*     */     case -2:
/* 653 */       err = "xaerasync";
/* 654 */       break;
/*     */     case -4:
/* 656 */       err = "xaernota";
/* 657 */       break;
/*     */     case -5:
/* 659 */       err = "xaerinval";
/* 660 */       break;
/*     */     case -6:
/* 662 */       err = "xaerproto";
/* 663 */       break;
/*     */     case -3:
/* 665 */       err = "xaerrmerr";
/* 666 */       break;
/*     */     case -7:
/* 668 */       err = "xaerrmfail";
/* 669 */       break;
/*     */     case -8:
/* 671 */       err = "xaerdupid";
/* 672 */       break;
/*     */     case -9:
/* 674 */       err = "xaeroutside";
/*     */     }
/*     */ 
/* 677 */     XAException e = new XAException(Messages.get("error.xaexception." + err));
/* 678 */     e.errorCode = errorCode;
/* 679 */     Logger.println("XAException: " + e.getMessage());
/* 680 */     throw e;
/*     */   }
/*     */ 
/*     */   private static byte[] toBytesXid(Xid xid)
/*     */   {
/* 692 */     byte[] buffer = new byte[12 + xid.getGlobalTransactionId().length + xid.getBranchQualifier().length];
/*     */ 
/* 695 */     int fmt = xid.getFormatId();
/* 696 */     buffer[0] = (byte)fmt;
/* 697 */     buffer[1] = (byte)(fmt >> 8);
/* 698 */     buffer[2] = (byte)(fmt >> 16);
/* 699 */     buffer[3] = (byte)(fmt >> 24);
/* 700 */     buffer[4] = (byte)xid.getGlobalTransactionId().length;
/* 701 */     buffer[8] = (byte)xid.getBranchQualifier().length;
/* 702 */     System.arraycopy(xid.getGlobalTransactionId(), 0, buffer, 12, buffer[4]);
/* 703 */     System.arraycopy(xid.getBranchQualifier(), 0, buffer, 12 + buffer[4], buffer[8]);
/* 704 */     return buffer;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.XASupport
 * JD-Core Version:    0.5.3
 */