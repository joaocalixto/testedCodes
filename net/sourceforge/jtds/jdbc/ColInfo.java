package net.sourceforge.jtds.jdbc;

public class ColInfo
{
  int tdsType;
  int jdbcType;
  String realName;
  String name;
  String tableName;
  String catalog;
  String schema;
  int nullable;
  boolean isCaseSensitive;
  boolean isWriteable;
  boolean isIdentity;
  boolean isKey;
  boolean isHidden;
  int userType;
  byte[] collation;
  CharsetInfo charsetInfo;
  int displaySize;
  int bufferSize;
  int precision;
  int scale;
  String sqlType;
}

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.ColInfo
 * JD-Core Version:    0.5.3
 */