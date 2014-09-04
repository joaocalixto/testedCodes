package net.sourceforge.jtds.ssl;

public abstract interface Ssl
{
  public static final String SSL_OFF = "off";
  public static final String SSL_REQUEST = "request";
  public static final String SSL_REQUIRE = "require";
  public static final String SSL_AUTHENTICATE = "authenticate";
  public static final int TLS_HEADER_SIZE = 5;
  public static final byte TYPE_CHANGECIPHERSPEC = 20;
  public static final byte TYPE_ALERT = 21;
  public static final byte TYPE_HANDSHAKE = 22;
  public static final byte TYPE_APPLICATIONDATA = 23;
  public static final int HS_HEADER_SIZE = 4;
  public static final int TYPE_CLIENTKEYEXCHANGE = 16;
  public static final int TYPE_CLIENTHELLO = 1;
}

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.ssl.Ssl
 * JD-Core Version:    0.5.3
 */