/*      */ package net.sourceforge.jtds.jdbc;
/*      */ 
/*      */ import java.sql.SQLException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import net.sourceforge.jtds.jdbc.cache.SQLCacheKey;
/*      */ import net.sourceforge.jtds.jdbc.cache.SimpleLRUCache;
/*      */ 
/*      */ class SQLParser
/*      */ {
/*      */   private static SimpleLRUCache cache;
/*      */   private final String sql;
/*      */   private final char[] in;
/*      */   private int s;
/*      */   private final int len;
/*      */   private final char[] out;
/*      */   private int d;
/*      */   private final ArrayList params;
/*      */   private char terminator;
/*      */   private String procName;
/*      */   private String keyWord;
/*      */   private String tableName;
/*      */   private final ConnectionJDBC2 connection;
/*  193 */   private static boolean[] identifierChar = { false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false };
/*      */ 
/*  625 */   private static final byte[] timeMask = { 35, 35, 58, 35, 35, 58, 35, 35 };
/*      */ 
/*  630 */   private static final byte[] dateMask = { 35, 35, 35, 35, 45, 35, 35, 45, 35, 35 };
/*      */ 
/*  635 */   static final byte[] timestampMask = { 35, 35, 35, 35, 45, 35, 35, 45, 35, 35, 32, 35, 35, 58, 35, 35, 58, 35, 35 };
/*      */ 
/*  672 */   private static HashMap fnMap = new HashMap();
/*      */ 
/*  674 */   private static HashMap msFnMap = new HashMap();
/*      */ 
/*  676 */   private static HashMap cvMap = new HashMap();
/*      */ 
/*      */   static String[] parse(String sql, ArrayList paramList, ConnectionJDBC2 connection, boolean extractTable)
/*      */     throws SQLException
/*      */   {
/*  139 */     if (extractTable) {
/*  140 */       SQLParser parser = new SQLParser(sql, paramList, connection);
/*  141 */       return parser.parse(extractTable);
/*      */     }
/*      */ 
/*  144 */     SimpleLRUCache cache = getCache(connection);
/*      */ 
/*  146 */     SQLCacheKey cacheKey = new SQLCacheKey(sql, connection);
/*      */ 
/*  152 */     CachedSQLQuery cachedQuery = (CachedSQLQuery)cache.get(cacheKey);
/*  153 */     if (cachedQuery == null)
/*      */     {
/*  155 */       SQLParser parser = new SQLParser(sql, paramList, connection);
/*  156 */       cachedQuery = new CachedSQLQuery(parser.parse(extractTable), paramList);
/*      */ 
/*  158 */       cache.put(cacheKey, cachedQuery);
/*      */     }
/*      */     else {
/*  161 */       int length = (cachedQuery.paramNames == null) ? 0 : cachedQuery.paramNames.length;
/*      */ 
/*  163 */       for (int i = 0; i < length; ++i) {
/*  164 */         ParamInfo paramInfo = new ParamInfo(cachedQuery.paramNames[i], cachedQuery.paramMarkerPos[i], cachedQuery.paramIsRetVal[i], cachedQuery.paramIsUnicode[i]);
/*      */ 
/*  168 */         paramList.add(paramInfo);
/*      */       }
/*      */     }
/*  171 */     return cachedQuery.parsedSql;
/*      */   }
/*      */ 
/*      */   private static synchronized SimpleLRUCache getCache(ConnectionJDBC2 connection)
/*      */   {
/*  182 */     if (cache == null) {
/*  183 */       int maxStatements = connection.getMaxStatements();
/*  184 */       maxStatements = Math.max(0, maxStatements);
/*  185 */       maxStatements = Math.min(1000, maxStatements);
/*  186 */       cache = new SimpleLRUCache(maxStatements);
/*      */     }
/*  188 */     return cache;
/*      */   }
/*      */ 
/*      */   private static boolean isIdentifier(int ch)
/*      */   {
/*  221 */     return ((ch > 127) || (identifierChar[ch] != 0));
/*      */   }
/*      */ 
/*      */   private SQLParser(String sqlIn, ArrayList paramList, ConnectionJDBC2 connection)
/*      */   {
/*  233 */     this.sql = sqlIn;
/*  234 */     this.in = this.sql.toCharArray();
/*  235 */     this.len = this.in.length;
/*  236 */     this.out = new char[this.len + 256];
/*  237 */     this.params = paramList;
/*  238 */     this.procName = "";
/*      */ 
/*  240 */     this.connection = connection;
/*      */   }
/*      */ 
/*      */   private void copyLiteral(String txt)
/*      */     throws SQLException
/*      */   {
/*  249 */     int len = txt.length();
/*      */ 
/*  251 */     for (int i = 0; i < len; ++i) {
/*  252 */       char c = txt.charAt(i);
/*      */ 
/*  254 */       if (c == '?') {
/*  255 */         if (this.params == null) {
/*  256 */           throw new SQLException(Messages.get("error.parsesql.unexpectedparam", String.valueOf(this.s)), "2A000");
/*      */         }
/*      */ 
/*  262 */         ParamInfo pi = new ParamInfo(this.d, this.connection.getUseUnicode());
/*  263 */         this.params.add(pi);
/*      */       }
/*      */ 
/*  266 */       this.out[(this.d++)] = c;
/*      */     }
/*      */   }
/*      */ 
/*      */   private void copyString()
/*      */   {
/*  274 */     char saveTc = this.terminator;
/*  275 */     char tc = this.in[this.s];
/*      */ 
/*  277 */     if (tc == '[') {
/*  278 */       tc = ']';
/*      */     }
/*      */ 
/*  281 */     this.terminator = tc;
/*      */ 
/*  283 */     this.out[(this.d++)] = this.in[(this.s++)];
/*      */ 
/*  285 */     while (this.in[this.s] != tc) {
/*  286 */       this.out[(this.d++)] = this.in[(this.s++)];
/*      */     }
/*      */ 
/*  289 */     this.out[(this.d++)] = this.in[(this.s++)];
/*      */ 
/*  291 */     this.terminator = saveTc;
/*      */   }
/*      */ 
/*      */   private String copyKeyWord()
/*      */   {
/*  298 */     int start = this.d;
/*      */ 
/*  300 */     while ((this.s < this.len) && (isIdentifier(this.in[this.s]))) {
/*  301 */       this.out[(this.d++)] = this.in[(this.s++)];
/*      */     }
/*      */ 
/*  304 */     return String.valueOf(this.out, start, this.d - start).toLowerCase();
/*      */   }
/*      */ 
/*      */   private void copyParam(String name, int pos)
/*      */     throws SQLException
/*      */   {
/*  314 */     if (this.params == null) {
/*  315 */       throw new SQLException(Messages.get("error.parsesql.unexpectedparam", String.valueOf(this.s)), "2A000");
/*      */     }
/*      */ 
/*  321 */     ParamInfo pi = new ParamInfo(pos, this.connection.getUseUnicode());
/*  322 */     pi.name = name;
/*      */ 
/*  324 */     if (pos >= 0) {
/*  325 */       this.out[(this.d++)] = this.in[(this.s++)];
/*      */     } else {
/*  327 */       pi.isRetVal = true;
/*  328 */       this.s += 1;
/*      */     }
/*      */ 
/*  331 */     this.params.add(pi);
/*      */   }
/*      */ 
/*      */   private String copyProcName()
/*      */     throws SQLException
/*      */   {
/*  340 */     int start = this.d;
/*      */ 
/*  343 */     if ((this.in[this.s] == '"') || (this.in[this.s] == '[')) {
/*  344 */       copyString();
/*      */     } else {
/*  346 */       char c = this.in[(this.s++)];
/*      */ 
/*  348 */       while ((isIdentifier(c)) || (c == ';')) {
/*  349 */         this.out[(this.d++)] = c;
/*  350 */         c = this.in[(this.s++)];
/*      */       }
/*      */ 
/*  353 */       this.s -= 1;
/*      */     }
/*      */ 
/*  356 */     if (this.in[this.s] == '.') while (true) {
/*  357 */         if (this.in[this.s] == '.');
/*  358 */         this.out[(this.d++)] = this.in[(this.s++)];
/*      */       }
/*      */ 
/*      */ 
/*  365 */     if (this.d == start)
/*      */     {
/*  367 */       throw new SQLException(Messages.get("error.parsesql.syntax", "call", String.valueOf(this.s)), "22025");
/*      */     }
/*      */ 
/*  374 */     return new String(this.out, start, this.d - start);
/*      */   }
/*      */ 
/*      */   private String copyParamName()
/*      */   {
/*  383 */     int start = this.d;
/*  384 */     char c = this.in[(this.s++)];
/*      */ 
/*  386 */     while (isIdentifier(c)) {
/*  387 */       this.out[(this.d++)] = c;
/*  388 */       c = this.in[(this.s++)];
/*      */     }
/*      */ 
/*  391 */     this.s -= 1;
/*      */ 
/*  393 */     return new String(this.out, start, this.d - start);
/*      */   }
/*      */ 
/*      */   private void copyWhiteSpace()
/*      */   {
/*  400 */     while ((this.s < this.in.length) && (Character.isWhitespace(this.in[this.s])))
/*  401 */       this.out[(this.d++)] = this.in[(this.s++)];
/*      */   }
/*      */ 
/*      */   private void mustbe(char c, boolean copy)
/*      */     throws SQLException
/*      */   {
/*  414 */     if (this.in[this.s] != c) {
/*  415 */       throw new SQLException(Messages.get("error.parsesql.mustbe", String.valueOf(this.s), String.valueOf(c)), "22019");
/*      */     }
/*      */ 
/*  422 */     if (copy)
/*  423 */       this.out[(this.d++)] = this.in[(this.s++)];
/*      */     else
/*  425 */       this.s += 1;
/*      */   }
/*      */ 
/*      */   private void skipWhiteSpace()
/*      */   {
/*  433 */     while (Character.isWhitespace(this.in[this.s]))
/*  434 */       this.s += 1;
/*      */   }
/*      */ 
/*      */   private void skipSingleComments()
/*      */   {
/*  442 */     while ((this.s < this.len) && (this.in[this.s] != '\n') && (this.in[this.s] != '\r'))
/*      */     {
/*  444 */       this.out[(this.d++)] = this.in[(this.s++)];
/*      */     }
/*      */   }
/*      */ 
/*      */   private void skipMultiComments()
/*      */     throws SQLException
/*      */   {
/*  452 */     int block = 0;
/*      */     do
/*      */     {
/*  455 */       if (this.s < this.len - 1) {
/*  456 */         if ((this.in[this.s] == '/') && (this.in[(this.s + 1)] == '*'))
/*  457 */           ++block;
/*  458 */         else if ((this.in[this.s] == '*') && (this.in[(this.s + 1)] == '/')) {
/*  459 */           --block;
/*      */         }
/*      */ 
/*  462 */         this.out[(this.d++)] = this.in[(this.s++)];
/*      */       } else {
/*  464 */         throw new SQLException(Messages.get("error.parsesql.missing", "*/"), "22025");
/*      */       }
/*      */     }
/*      */ 
/*  468 */     while (block > 0);
/*  469 */     this.out[(this.d++)] = this.in[(this.s++)];
/*      */   }
/*      */ 
/*      */   private void callEscape()
/*      */     throws SQLException
/*      */   {
/*  479 */     copyLiteral("EXECUTE ");
/*  480 */     this.keyWord = "execute";
/*      */ 
/*  482 */     this.procName = copyProcName();
/*  483 */     skipWhiteSpace();
/*      */ 
/*  485 */     if (this.in[this.s] == '(') {
/*  486 */       this.s += 1;
/*  487 */       this.terminator = ')';
/*  488 */       skipWhiteSpace();
/*      */     } else {
/*  490 */       this.terminator = '}';
/*      */     }
/*      */ 
/*  493 */     this.out[(this.d++)] = ' ';
/*      */ 
/*  496 */     while (this.in[this.s] != this.terminator) {
/*  497 */       String name = null;
/*      */ 
/*  499 */       if (this.in[this.s] == '@')
/*      */       {
/*  501 */         name = copyParamName();
/*  502 */         skipWhiteSpace();
/*  503 */         mustbe('=', true);
/*  504 */         skipWhiteSpace();
/*      */ 
/*  506 */         if (this.in[this.s] == '?') {
/*  507 */           copyParam(name, this.d);
/*      */         }
/*      */         else
/*  510 */           this.procName = "";
/*      */       }
/*  512 */       else if (this.in[this.s] == '?') {
/*  513 */         copyParam(name, this.d);
/*      */       }
/*      */       else {
/*  516 */         this.procName = "";
/*      */       }
/*      */ 
/*  520 */       while ((this.in[this.s] != this.terminator) && (this.in[this.s] != ',')) {
/*  521 */         if (this.in[this.s] == '{')
/*  522 */           escape();
/*  523 */         if ((this.in[this.s] == '\'') || (this.in[this.s] == '[') || (this.in[this.s] == '"')) {
/*  524 */           copyString();
/*      */         }
/*  526 */         this.out[(this.d++)] = this.in[(this.s++)];
/*      */       }
/*      */ 
/*  530 */       if (this.in[this.s] == ',') {
/*  531 */         this.out[(this.d++)] = this.in[(this.s++)];
/*      */       }
/*      */ 
/*  534 */       skipWhiteSpace();
/*      */     }
/*      */ 
/*  537 */     if (this.terminator == ')') {
/*  538 */       this.s += 1;
/*      */     }
/*      */ 
/*  541 */     this.terminator = '}';
/*  542 */     skipWhiteSpace();
/*      */   }
/*      */ 
/*      */   private boolean getDateTimeField(byte[] mask)
/*      */     throws SQLException
/*      */   {
/*  552 */     skipWhiteSpace();
/*  553 */     if (this.in[this.s] == '?')
/*      */     {
/*  555 */       copyParam(null, this.d);
/*  556 */       skipWhiteSpace();
/*  557 */       return (this.in[this.s] == this.terminator);
/*      */     }
/*  559 */     this.out[(this.d++)] = '\'';
/*  560 */     if ((this.in[this.s] == '\'') || (this.in[this.s] == '"'));
/*  560 */     this.terminator = ((tmp105_104 = this) ? this.in[(tmp105_104.s++)] : '}');
/*  561 */     skipWhiteSpace();
/*  562 */     int ptr = 0;
/*      */ 
/*  564 */     while (ptr < mask.length) {
/*  565 */       char c = this.in[(this.s++)];
/*  566 */       if ((c == ' ') && (this.out[(this.d - 1)] == ' ')) {
/*      */         continue;
/*      */       }
/*      */ 
/*  570 */       if (mask[ptr] == 35) {
/*  571 */         if (!(Character.isDigit(c)))
/*  572 */           return false;
/*      */       }
/*  574 */       else if (mask[ptr] != c) {
/*  575 */         return false;
/*      */       }
/*      */ 
/*  578 */       if (c != '-') {
/*  579 */         this.out[(this.d++)] = c;
/*      */       }
/*      */ 
/*  582 */       ++ptr;
/*      */     }
/*      */ 
/*  585 */     if (mask.length == 19) {
/*  586 */       int digits = 0;
/*      */ 
/*  588 */       if (this.in[this.s] == '.') {
/*  589 */         this.out[(this.d++)] = this.in[(this.s++)];
/*      */         while (true) {
/*  591 */           if (!(Character.isDigit(this.in[this.s]))) break label377;
/*  592 */           if (digits < 3) {
/*  593 */             this.out[(this.d++)] = this.in[(this.s++)];
/*  594 */             ++digits;
/*      */           }
/*  596 */           this.s += 1;
/*      */         }
/*      */       }
/*      */ 
/*  600 */       this.out[(this.d++)] = '.';
/*      */ 
/*  603 */       for (; digits < 3; ++digits) {
/*  604 */         label377: this.out[(this.d++)] = '0';
/*      */       }
/*      */     }
/*      */ 
/*  608 */     skipWhiteSpace();
/*      */ 
/*  610 */     if (this.in[this.s] != this.terminator) {
/*  611 */       return false;
/*      */     }
/*      */ 
/*  614 */     if (this.terminator != '}') {
/*  615 */       this.s += 1;
/*      */     }
/*      */ 
/*  618 */     skipWhiteSpace();
/*  619 */     this.out[(this.d++)] = '\'';
/*      */ 
/*  621 */     return true;
/*      */   }
/*      */ 
/*      */   private void outerJoinEscape()
/*      */     throws SQLException
/*      */   {
/*  647 */     while (this.in[this.s] != '}') {
/*  648 */       char c = this.in[this.s];
/*      */ 
/*  650 */       switch (c)
/*      */       {
/*      */       case '"':
/*      */       case '\'':
/*      */       case '[':
/*  654 */         copyString();
/*  655 */         break;
/*      */       case '{':
/*  658 */         escape();
/*  659 */         break;
/*      */       case '?':
/*  661 */         copyParam(null, this.d);
/*  662 */         break;
/*      */       default:
/*  664 */         this.out[(this.d++)] = c;
/*  665 */         this.s += 1;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   private void functionEscape()
/*      */     throws SQLException
/*      */   {
/*  729 */     char tc = this.terminator;
/*  730 */     skipWhiteSpace();
/*  731 */     StringBuffer nameBuf = new StringBuffer();
/*      */ 
/*  735 */     while (isIdentifier(this.in[this.s])) {
/*  736 */       nameBuf.append(this.in[(this.s++)]);
/*      */     }
/*      */ 
/*  739 */     String name = nameBuf.toString().toLowerCase();
/*      */ 
/*  743 */     skipWhiteSpace();
/*  744 */     mustbe('(', false);
/*  745 */     int parenCnt = 1;
/*  746 */     int argStart = this.d;
/*  747 */     int arg2Start = 0;
/*  748 */     this.terminator = ')';
/*  749 */     while ((this.in[this.s] != ')') || (parenCnt > 1)) {
/*  750 */       char c = this.in[this.s];
/*      */ 
/*  752 */       switch (c)
/*      */       {
/*      */       case '"':
/*      */       case '\'':
/*      */       case '[':
/*  756 */         copyString();
/*  757 */         break;
/*      */       case '{':
/*  760 */         escape();
/*  761 */         break;
/*      */       case ',':
/*  763 */         if (arg2Start == 0) {
/*  764 */           arg2Start = this.d - argStart;
/*      */         }
/*  766 */         if ("concat".equals(name)) {
/*  767 */           this.out[(this.d++)] = '+'; this.s += 1;
/*  768 */         } else if ("mod".equals(name)) {
/*  769 */           this.out[(this.d++)] = '%'; this.s += 1;
/*      */         } else {
/*  771 */           this.out[(this.d++)] = c; this.s += 1;
/*      */         }
/*  773 */         break;
/*      */       case '(':
/*  775 */         ++parenCnt;
/*  776 */         this.out[(this.d++)] = c; this.s += 1;
/*  777 */         break;
/*      */       case ')':
/*  779 */         --parenCnt;
/*  780 */         this.out[(this.d++)] = c; this.s += 1;
/*  781 */         break;
/*      */       default:
/*  783 */         this.out[(this.d++)] = c; this.s += 1;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  788 */     String args = String.valueOf(this.out, argStart, this.d - argStart).trim();
/*      */ 
/*  790 */     this.d = argStart;
/*  791 */     mustbe(')', false);
/*  792 */     this.terminator = tc;
/*  793 */     skipWhiteSpace();
/*      */ 
/*  800 */     if (("convert".equals(name)) && (arg2Start < args.length() - 1)) {
/*  801 */       String arg2 = args.substring(arg2Start + 1).trim().toLowerCase();
/*  802 */       String dataType = (String)cvMap.get(arg2);
/*      */ 
/*  804 */       if (dataType == null)
/*      */       {
/*  806 */         dataType = arg2;
/*      */       }
/*      */ 
/*  809 */       copyLiteral("convert(");
/*  810 */       copyLiteral(dataType);
/*  811 */       this.out[(this.d++)] = ',';
/*  812 */       copyLiteral(args.substring(0, arg2Start));
/*  813 */       this.out[(this.d++)] = ')';
/*      */ 
/*  815 */       return;
/*      */     }
/*      */     String fn;
/*  822 */     if (this.connection.getServerType() == 1) {
/*  823 */       fn = (String)msFnMap.get(name);
/*  824 */       if (fn == null)
/*  825 */         fn = (String)fnMap.get(name);
/*      */     }
/*      */     else {
/*  828 */       fn = (String)fnMap.get(name);
/*      */     }
/*  830 */     if (fn == null)
/*      */     {
/*  832 */       copyLiteral(name);
/*  833 */       this.out[(this.d++)] = '(';
/*  834 */       copyLiteral(args);
/*  835 */       this.out[(this.d++)] = ')';
/*  836 */       return;
/*      */     }
/*      */ 
/*  841 */     if ((args.length() > 8) && (args.substring(0, 8).equalsIgnoreCase("sql_tsi_")))
/*      */     {
/*  843 */       args = args.substring(8);
/*  844 */       if ((args.length() > 11) && (args.substring(0, 11).equalsIgnoreCase("frac_second")))
/*      */       {
/*  846 */         args = "millisecond" + args.substring(11);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  852 */     int len = fn.length();
/*  853 */     for (int i = 0; i < len; ++i) {
/*  854 */       char c = fn.charAt(i);
/*  855 */       if (c == '$')
/*      */       {
/*  857 */         copyLiteral(args);
/*      */       }
/*      */       else this.out[(this.d++)] = c;
/*      */     }
/*      */   }
/*      */ 
/*      */   private void likeEscape()
/*      */     throws SQLException
/*      */   {
/*  870 */     copyLiteral("escape ");
/*  871 */     skipWhiteSpace();
/*      */ 
/*  873 */     if ((this.in[this.s] == '\'') || (this.in[this.s] == '"'))
/*  874 */       copyString();
/*      */     else {
/*  876 */       mustbe('\'', true);
/*      */     }
/*      */ 
/*  879 */     skipWhiteSpace();
/*      */   }
/*      */ 
/*      */   private void escape()
/*      */     throws SQLException
/*      */   {
/*  888 */     char tc = this.terminator;
/*  889 */     this.terminator = '}';
/*  890 */     StringBuffer escBuf = new StringBuffer();
/*  891 */     this.s += 1;
/*  892 */     skipWhiteSpace();
/*      */     String esc;
/*  894 */     if (this.in[this.s] == '?') {
/*  895 */       copyParam("@return_status", -1);
/*  896 */       skipWhiteSpace();
/*  897 */       mustbe('=', false);
/*  898 */       skipWhiteSpace();
/*      */ 
/*  900 */       while (Character.isLetter(this.in[this.s])) {
/*  901 */         escBuf.append(Character.toLowerCase(this.in[(this.s++)]));
/*      */       }
/*      */ 
/*  904 */       skipWhiteSpace();
/*  905 */       esc = escBuf.toString();
/*      */ 
/*  907 */       if ("call".equals(esc))
/*  908 */         callEscape();
/*      */       else {
/*  910 */         throw new SQLException(Messages.get("error.parsesql.syntax", "call", String.valueOf(this.s)), "22019");
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  917 */       while (Character.isLetter(this.in[this.s])) {
/*  918 */         escBuf.append(Character.toLowerCase(this.in[(this.s++)]));
/*      */       }
/*      */ 
/*  921 */       skipWhiteSpace();
/*  922 */       esc = escBuf.toString();
/*      */ 
/*  924 */       if ("call".equals(esc))
/*  925 */         callEscape();
/*  926 */       else if ("t".equals(esc)) {
/*  927 */         if (!(getDateTimeField(timeMask))) {
/*  928 */           throw new SQLException(Messages.get("error.parsesql.syntax", "time", String.valueOf(this.s)), "22019");
/*      */         }
/*      */ 
/*      */       }
/*  934 */       else if ("d".equals(esc)) {
/*  935 */         if (!(getDateTimeField(dateMask))) {
/*  936 */           throw new SQLException(Messages.get("error.parsesql.syntax", "date", String.valueOf(this.s)), "22019");
/*      */         }
/*      */ 
/*      */       }
/*  942 */       else if ("ts".equals(esc)) {
/*  943 */         if (!(getDateTimeField(timestampMask))) {
/*  944 */           throw new SQLException(Messages.get("error.parsesql.syntax", "timestamp", String.valueOf(this.s)), "22019");
/*      */         }
/*      */ 
/*      */       }
/*  950 */       else if ("oj".equals(esc))
/*  951 */         outerJoinEscape();
/*  952 */       else if ("fn".equals(esc))
/*  953 */         functionEscape();
/*  954 */       else if ("escape".equals(esc))
/*  955 */         likeEscape();
/*      */       else {
/*  957 */         throw new SQLException(Messages.get("error.parsesql.badesc", esc, String.valueOf(this.s)), "22019");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  965 */     mustbe('}', false);
/*  966 */     this.terminator = tc;
/*      */   }
/*      */ 
/*      */   private String getTableName()
/*      */     throws SQLException
/*      */   {
/*  975 */     StringBuffer name = new StringBuffer(128);
/*  976 */     copyWhiteSpace();
/*  977 */     char c = (this.s < this.len) ? this.in[this.s] : ' ';
/*  978 */     if (c == '{')
/*      */     {
/*  982 */       return "";
/*      */     }
/*      */ 
/*  987 */     while ((c == '/') || ((c == '-') && (this.s + 1 < this.len))) {
/*  988 */       if (c == '/') {
/*  989 */         if (this.in[(this.s + 1)] != '*') break;
/*  990 */         skipMultiComments();
/*      */       }
/*      */       else
/*      */       {
/*  995 */         if (this.in[(this.s + 1)] != '-') break;
/*  996 */         skipSingleComments();
/*      */       }
/*      */ 
/* 1001 */       copyWhiteSpace();
/* 1002 */       c = (this.s < this.len) ? this.in[this.s] : ' ';
/*      */     }
/*      */ 
/* 1005 */     if (c == '{')
/*      */     {
/* 1007 */       return "";
/*      */     }
/*      */ 
/* 1012 */     while (this.s < this.len)
/*      */     {
/*      */       int start;
/* 1013 */       if ((c == '[') || (c == '"')) {
/* 1014 */         start = this.d;
/* 1015 */         copyString();
/* 1016 */         name.append(String.valueOf(this.out, start, this.d - start));
/* 1017 */         copyWhiteSpace();
/* 1018 */         c = (this.s < this.len) ? this.in[this.s] : ' ';
/*      */       } else {
/* 1020 */         start = this.d;
/* 1021 */         if (this.s < this.len);
/* 1021 */         c = (tmp271_270 = this) ? this.in[(tmp271_270.s++)] : ' ';
/*      */ 
/* 1024 */         while ((isIdentifier(c)) && (c != '.') && (c != ',')) {
/* 1025 */           this.out[(this.d++)] = c;
/* 1026 */           if (this.s < this.len);
/* 1026 */           c = (tmp340_339 = this) ? this.in[(tmp340_339.s++)] : ' ';
/*      */         }
/* 1028 */         name.append(String.valueOf(this.out, start, this.d - start));
/* 1029 */         this.s -= 1;
/* 1030 */         copyWhiteSpace();
/* 1031 */         c = (this.s < this.len) ? this.in[this.s] : ' ';
/*      */       }
/* 1033 */       if (c != '.') {
/*      */         break;
/*      */       }
/* 1036 */       name.append(c);
/* 1037 */       this.out[(this.d++)] = c; this.s += 1;
/* 1038 */       copyWhiteSpace();
/* 1039 */       c = (this.s < this.len) ? this.in[this.s] : ' ';
/*      */     }
/* 1041 */     return name.toString();
/*      */   }
/*      */ 
/*      */   String[] parse(boolean extractTable)
/*      */     throws SQLException
/*      */   {
/* 1054 */     boolean isSelect = false;
/* 1055 */     boolean isModified = false;
/* 1056 */     boolean isSlowScan = true;
/*      */     try {
/* 1058 */       while (this.s < this.len) {
/* 1059 */         char c = this.in[this.s];
/*      */ 
/* 1061 */         switch (c)
/*      */         {
/*      */         case '{':
/* 1063 */           escape();
/* 1064 */           isModified = true;
/* 1065 */           break;
/*      */         case '"':
/*      */         case '\'':
/*      */         case '[':
/* 1069 */           copyString();
/* 1070 */           break;
/*      */         case '?':
/* 1072 */           copyParam(null, this.d);
/* 1073 */           break;
/*      */         case '/':
/* 1075 */           if ((this.s + 1 < this.len) && (this.in[(this.s + 1)] == '*')) {
/* 1076 */             skipMultiComments();
/*      */           } else {
/* 1078 */             this.out[(this.d++)] = c; this.s += 1;
/*      */           }
/* 1080 */           break;
/*      */         case '-':
/* 1082 */           if ((this.s + 1 < this.len) && (this.in[(this.s + 1)] == '-')) {
/* 1083 */             skipSingleComments();
/*      */           } else {
/* 1085 */             this.out[(this.d++)] = c; this.s += 1;
/*      */           }
/* 1087 */           label384: break;
/*      */         default:
/* 1089 */           if ((isSlowScan) && (Character.isLetter(c))) {
/* 1090 */             if (this.keyWord == null) {
/* 1091 */               this.keyWord = copyKeyWord();
/* 1092 */               if ("select".equals(this.keyWord)) {
/* 1093 */                 isSelect = true;
/*      */               }
/* 1095 */               isSlowScan = (extractTable) && (isSelect);
/*      */             }
/* 1098 */             else if ((extractTable) && (isSelect)) {
/* 1099 */               String sqlWord = copyKeyWord();
/* 1100 */               if (!("from".equals(sqlWord)))
/*      */                 break label384;
/* 1102 */               isSlowScan = false;
/* 1103 */               this.tableName = getTableName();
/*      */             }
/*      */ 
/*      */           }
/*      */           else
/*      */           {
/* 1109 */             this.out[(this.d++)] = c; this.s += 1;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1119 */       if ((this.params != null) && (this.params.size() > 255) && (this.connection.getPrepareSql() != 0) && (this.procName != null))
/*      */       {
/* 1122 */         int limit = 255;
/* 1123 */         if (this.connection.getServerType() == 2) {
/* 1124 */           if ((this.connection.getDatabaseMajorVersion() > 12) || ((this.connection.getDatabaseMajorVersion() == 12) && (this.connection.getDatabaseMinorVersion() >= 50)))
/*      */           {
/* 1127 */             limit = 2000;
/*      */           }
/*      */         }
/* 1130 */         else if (this.connection.getDatabaseMajorVersion() == 7) {
/* 1131 */           limit = 1000;
/*      */         }
/* 1133 */         else if (this.connection.getDatabaseMajorVersion() > 7) {
/* 1134 */           limit = 2000;
/*      */         }
/*      */ 
/* 1138 */         if (this.params.size() > limit) {
/* 1139 */           throw new SQLException(Messages.get("error.parsesql.toomanyparams", Integer.toString(limit)), "22025");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1145 */       String[] result = new String[4];
/*      */ 
/* 1148 */       result[0] = ((isModified) ? new String(this.out, 0, this.d) : this.sql);
/* 1149 */       result[1] = this.procName;
/* 1150 */       result[2] = ((this.keyWord == null) ? "" : this.keyWord);
/* 1151 */       result[3] = this.tableName;
/* 1152 */       return result;
/*      */     }
/*      */     catch (IndexOutOfBoundsException e) {
/* 1155 */       throw new SQLException(Messages.get("error.parsesql.missing", String.valueOf(this.terminator)), "22025");
/*      */     }
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*  680 */     msFnMap.put("length", "len($)");
/*  681 */     msFnMap.put("truncate", "round($, 1)");
/*      */ 
/*  683 */     fnMap.put("user", "user_name($)");
/*  684 */     fnMap.put("database", "db_name($)");
/*  685 */     fnMap.put("ifnull", "isnull($)");
/*  686 */     fnMap.put("now", "getdate($)");
/*  687 */     fnMap.put("atan2", "atn2($)");
/*  688 */     fnMap.put("mod", "($)");
/*  689 */     fnMap.put("length", "char_length($)");
/*  690 */     fnMap.put("locate", "charindex($)");
/*  691 */     fnMap.put("repeat", "replicate($)");
/*  692 */     fnMap.put("insert", "stuff($)");
/*  693 */     fnMap.put("lcase", "lower($)");
/*  694 */     fnMap.put("ucase", "upper($)");
/*  695 */     fnMap.put("concat", "($)");
/*  696 */     fnMap.put("curdate", "convert(datetime, convert(varchar, getdate(), 112))");
/*  697 */     fnMap.put("curtime", "convert(datetime, convert(varchar, getdate(), 108))");
/*  698 */     fnMap.put("dayname", "datename(weekday,$)");
/*  699 */     fnMap.put("dayofmonth", "datepart(day,$)");
/*  700 */     fnMap.put("dayofweek", "((datepart(weekday,$)+@@DATEFIRST-1)%7+1)");
/*  701 */     fnMap.put("dayofyear", "datepart(dayofyear,$)");
/*  702 */     fnMap.put("hour", "datepart(hour,$)");
/*  703 */     fnMap.put("minute", "datepart(minute,$)");
/*  704 */     fnMap.put("second", "datepart(second,$)");
/*  705 */     fnMap.put("year", "datepart(year,$)");
/*  706 */     fnMap.put("quarter", "datepart(quarter,$)");
/*  707 */     fnMap.put("month", "datepart(month,$)");
/*  708 */     fnMap.put("week", "datepart(week,$)");
/*  709 */     fnMap.put("monthname", "datename(month,$)");
/*  710 */     fnMap.put("timestampadd", "dateadd($)");
/*  711 */     fnMap.put("timestampdiff", "datediff($)");
/*      */ 
/*  713 */     cvMap.put("binary", "varbinary");
/*  714 */     cvMap.put("char", "varchar");
/*  715 */     cvMap.put("date", "datetime");
/*  716 */     cvMap.put("double", "float");
/*  717 */     cvMap.put("longvarbinary", "image");
/*  718 */     cvMap.put("longvarchar", "text");
/*  719 */     cvMap.put("time", "datetime");
/*  720 */     cvMap.put("timestamp", "timestamp");
/*      */   }
/*      */ 
/*      */   private static class CachedSQLQuery
/*      */   {
/*      */     final String[] parsedSql;
/*      */     final String[] paramNames;
/*      */     final int[] paramMarkerPos;
/*      */     final boolean[] paramIsRetVal;
/*      */     final boolean[] paramIsUnicode;
/*      */ 
/*      */     CachedSQLQuery(String[] parsedSql, ArrayList params)
/*      */     {
/*   69 */       this.parsedSql = parsedSql;
/*      */ 
/*   71 */       if (params != null) {
/*   72 */         int size = params.size();
/*   73 */         this.paramNames = new String[size];
/*   74 */         this.paramMarkerPos = new int[size];
/*   75 */         this.paramIsRetVal = new boolean[size];
/*   76 */         this.paramIsUnicode = new boolean[size];
/*      */ 
/*   78 */         for (int i = 0; i < size; ++i) {
/*   79 */           ParamInfo paramInfo = (ParamInfo)params.get(i);
/*   80 */           this.paramNames[i] = paramInfo.name;
/*   81 */           this.paramMarkerPos[i] = paramInfo.markerPos;
/*   82 */           this.paramIsRetVal[i] = paramInfo.isRetVal;
/*   83 */           this.paramIsUnicode[i] = paramInfo.isUnicode;
/*      */         }
/*      */       } else {
/*   86 */         this.paramNames = null;
/*   87 */         this.paramMarkerPos = null;
/*   88 */         this.paramIsRetVal = null;
/*   89 */         this.paramIsUnicode = null;
/*      */       }
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.SQLParser
 * JD-Core Version:    0.5.3
 */