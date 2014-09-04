/*     */ package net.sourceforge.jtds.jdbc;
/*     */ 
/*     */ import java.text.MessageFormat;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Map;
/*     */ import java.util.MissingResourceException;
/*     */ import java.util.ResourceBundle;
/*     */ 
/*     */ public final class Messages
/*     */ {
/*     */   private static final String DEFAULT_RESOURCE = "net.sourceforge.jtds.jdbc.Messages";
/*     */   private static ResourceBundle defaultResource;
/*     */ 
/*     */   public static String get(String key)
/*     */   {
/*  67 */     return get(key, null);
/*     */   }
/*     */ 
/*     */   public static String get(String key, Object param1)
/*     */   {
/*  80 */     Object[] args = { param1 };
/*  81 */     return get(key, args);
/*     */   }
/*     */ 
/*     */   static String get(String key, Object param1, Object param2)
/*     */   {
/*  95 */     Object[] args = { param1, param2 };
/*  96 */     return get(key, args);
/*     */   }
/*     */ 
/*     */   private static String get(String key, Object[] arguments)
/*     */   {
/*     */     try
/*     */     {
/* 110 */       ResourceBundle bundle = loadResourceBundle();
/* 111 */       String formatString = bundle.getString(key);
/*     */ 
/* 113 */       if ((arguments == null) || (arguments.length == 0)) {
/* 114 */         return formatString;
/*     */       }
/* 116 */       MessageFormat formatter = new MessageFormat(formatString);
/* 117 */       return formatter.format(arguments);
/*     */     }
/*     */     catch (MissingResourceException mre) {
/* 120 */       throw new RuntimeException("No message resource found for message property " + key);
/*     */     }
/*     */   }
/*     */ 
/*     */   static void loadDriverProperties(Map propertyMap, Map descriptionMap)
/*     */   {
/* 139 */     ResourceBundle bundle = loadResourceBundle();
/* 140 */     Enumeration keys = bundle.getKeys();
/* 141 */     while (keys.hasMoreElements()) {
/* 142 */       String key = (String)keys.nextElement();
/* 143 */       String descriptionPrefix = "prop.desc.";
/* 144 */       String propertyPrefix = "prop.";
/* 145 */       if (key.startsWith("prop.desc.")) {
/* 146 */         descriptionMap.put(key.substring("prop.desc.".length()), bundle.getString(key));
/*     */       }
/* 148 */       else if (key.startsWith("prop."))
/* 149 */         propertyMap.put(key.substring("prop.".length()), bundle.getString(key));
/*     */     }
/*     */   }
/*     */ 
/*     */   private static ResourceBundle loadResourceBundle()
/*     */   {
/* 161 */     if (defaultResource == null) {
/* 162 */       defaultResource = ResourceBundle.getBundle("net.sourceforge.jtds.jdbc.Messages");
/*     */     }
/* 164 */     return defaultResource;
/*     */   }
/*     */ }

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.Messages
 * JD-Core Version:    0.5.3
 */