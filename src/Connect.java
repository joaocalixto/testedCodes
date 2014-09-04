import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class Connect {
	private java.sql.Connection con = null;
	private final String url = "jdbc:jtds:sqlserver://";
	private final String serverName = "172.17.9.80";
	private final String portNumber = "1433";
	private final String databaseName = "gateway2";
	private final String userName = "gateway";
	private final String password = "gateway";
	// Informs the driver to use server a side-cursor,
	// which permits more than one active statement
	// on a connection.
	private final String selectMethod = "cursor";

	// Constructor
	public Connect() {
	}

	private void closeConnection() {
		try {
			if (this.con != null) {
				this.con.close();
			}
			this.con = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayDbProperties() {
		java.sql.DatabaseMetaData dm = null;
		java.sql.ResultSet rs = null;
		try {
			this.con = this.getConnection();
			String vkEY = "27382b9c1f5a5c9745f7e184e335b16c27382b9c";

			PreparedStatement vPreparedStatement = null;
			String vSql = "SELECT CL1_KEY, CL1_DATE, CL1_SITE, CL1_DOCUMENT, CL1_MESSAGE, CL1_STATUS, CL1_VARIABLES FROM CACHEL1 WITH(NOLOCK) WHERE CL1_KEY = ?";
			vPreparedStatement = this.con.prepareStatement(vSql);
			vPreparedStatement.setString(1, vkEY);
			// vPreparedStatement.setObject(1, vkEY, Types.CLOB);

			ResultSet vResultSet = null;
			vResultSet = vPreparedStatement.executeQuery();
			if (this.con != null) {
				dm = this.con.getMetaData();
				System.out.println("Driver Information");
				System.out.println("\tDriver Name: " + dm.getDriverName());
				System.out.println("\tDriver Version: " + dm.getDriverVersion());
				System.out.println("\nDatabase Information ");
				System.out.println("\tDatabase Name: " + dm.getDatabaseProductName());
				System.out.println("\tDatabase Version: " + dm.getDatabaseProductVersion());
				System.out.println("Avalilable Catalogs ");
				rs = dm.getCatalogs();
				while (rs.next()) {
					System.out.println("\tcatalog: " + rs.getString(1));

					Timestamp vTimestamp = vResultSet.getTimestamp("CL1_DATE");
					String vSite = vResultSet.getString("CL1_SITE");
					byte[] vDocumentBytes = vResultSet.getBytes("CL1_DOCUMENT");
					String vMessage = vResultSet.getString("CL1_MESSAGE");
					String vStatus = vResultSet.getString("CL1_STATUS");
					byte[] vVariablesBytes = vResultSet.getBytes("CL1_VARIABLES");

					System.out.println(vTimestamp + vSite + vMessage + vStatus);
				}

				rs.close();
				rs = null;
				this.closeConnection();
			} else {
				System.out.println("Error: No active Connection");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		dm = null;
	}

	/*
	 * Display the driver properties, databa se details
	 */

	private java.sql.Connection getConnection() {
		try {
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
			this.con = java.sql.DriverManager.getConnection(this.getConnectionUrl(), this.userName, this.password);
			if (this.con != null) {
				System.out.println("Connection Successful!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error Trace in getConnection() : " + e.getMessage());
		}
		return this.con;
	}

	private String getConnectionUrl() {

		return this.url + this.serverName + ":" + this.portNumber + ";databaseName=" + this.databaseName
				+ ";selectMethod=" + this.selectMethod + ";prepareSQL=0;";

		// + "TDS=4.2;" + "sendStringParametersAsUnicode=false;"
	}

	public static void main(String[] args) throws Exception {
		Connect myDbTest = new Connect();
		myDbTest.displayDbProperties();
	}
}