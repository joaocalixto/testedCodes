
import java.awt.GraphicsEnvironment;
import java.io.PrintStream;
import java.util.prefs.Preferences;

public class StartBurp2
{
  public static boolean a;
  public static int b;
  private static final String[] z;

  public static void main(String[] paramArrayOfString)
  {
    int j = StartBurp2.b;
    try
    {
      try
      {
        StartBurp2.a = GraphicsEnvironment.isHeadless();
      }
      catch (Exception localException1)
      {
      }
      int i = 0;
      do
      {
        if (i >= paramArrayOfString.length) {
			break;
		}
        if (StartBurp2.z[4].equals(paramArrayOfString[i])) {
			System.out.println(feb.a());
		}
        ++i;
      }
      while (j == 0);
      jeb.a();
      Preferences localPreferences = Preferences.userNodeForPackage(StartBurp.class);
      boolean bool = StartBurp2.z[5].equals(localPreferences.get(StartBurp2.z[1], null));
      we localwe = new we(paramArrayOfString, bool);
      rfb localrfb = qdb.a(bool);
      localwe.a(localrfb);
      localrfb = ldb.a();
      localwe.a(localrfb);
      localrfb = kdb.a();
      localwe.a(localrfb);
      localrfb = mdb.a();
      localwe.a(localrfb);
      if (!(bool))
      {
        localrfb = pdb.a();
        localwe.a(localrfb);
        localrfb = ndb.a();
        localwe.a(localrfb);
        localrfb = odb.a();
        localwe.a(localrfb);
        localrfb = rdb.a();
        localwe.a(localrfb);
        localrfb = jdb.a();
        localwe.a(localrfb);
      }
      localwe.a();
      new Thread(new lhb(localwe, StartBurp2.z[0], StartBurp2.z[2], StartBurp2.z[3])).start();
    }
    catch (Exception localException2)
    {
    }
  }

  static
  {
    String[] tmp5_2 = new String[6];
    jsr 50;
    tmp5_2[0] = "!|Wl$f'Ssl={Tuy.mQ2p,|\fj{;'Piw=mU-0}&\19-0?mQ";
    String[] tmp13_5 = tmp5_2;
    jsr 42;
    tmp13_5[1] = "x&\232m<aWy0%mBrS&lF";
    String[] tmp21_13 = tmp13_5;
    jsr 34;
    tmp21_13[2] = ":}Jh{?9\r(0y9";
    String[] tmp29_21 = tmp21_13;
    jsr 26;
    tmp29_21[3] = "+}Ql>:}Jh{";
    String[] tmp37_29 = tmp29_21;
    jsr 18;
    tmp37_29[4] = "-aB{p&{Wu}:";
    String[] tmp45_37 = tmp37_29;
    jsr 10;
    tmp45_37[5] = "=zVy";
    z = tmp45_37;
    break label171:
    label171: localObject = returnAddress;
  }
}