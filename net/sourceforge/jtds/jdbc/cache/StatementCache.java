package net.sourceforge.jtds.jdbc.cache;

import java.util.Collection;

public abstract interface StatementCache
{
  public abstract Object get(String paramString);

  public abstract void put(String paramString, Object paramObject);

  public abstract void remove(String paramString);

  public abstract Collection getObsoleteHandles(Collection paramCollection);
}

/* Location:           C:\Tomcat2\webapps\gateway2\WEB-INF\lib\jtds-1.2.6.jar
 * Qualified Name:     net.sourceforge.jtds.jdbc.cache.StatementCache
 * JD-Core Version:    0.5.3
 */