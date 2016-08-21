package is.ejb.bl.rewardSystems.radius;

import is.ejb.bl.business.RespStatusEnum;
import is.ejb.dl.entities.RadiusConfigurationEntity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RadiusProvider {

	// static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	// static final String DB_URL = "jdbc:mysql://212.48.68.76:3306/radius";

	// Database credentials
	// static final String USER = "radius";
	// static final String PASS = "C0ns3ga";

	private Connection conn = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	private String DB_URL;
	private String USER;
	private String PASS;

	public RadiusProvider(RadiusConfigurationEntity entity) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			DB_URL = "jdbc:mysql://" + entity.getIp() + ":" + entity.getPort()
					+ "/" + entity.getDbname();
			USER = entity.getLogin();
			PASS = entity.getPassword();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public RadiusProvider(String ip, int port, String dbname, String login,
			String password) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			DB_URL = "jdbc:mysql://" + ip + ":" + port + "/" + dbname;
			USER = login;
			PASS = password;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public RespStatusEnum addTime(String user, int time) throws Exception {
		try {
			String expiration = getExpiration(user);
			System.out.println(expiration);
			if (expiration == null) {
				insertExpiration(user, getTimeWithFund(time));
				return RespStatusEnum.SUCCESS;
			} else {
				Date currentExpirationDate = getDateFromString(expiration);
				System.out.println(currentExpirationDate);

				// if currentExpirationDate is after current Date then we need
				// to add time to currentExpirationDate

				if (currentExpirationDate.after(new Date())) {
					updateExpiration(user, addMinutesToDate(currentExpirationDate, time));
					return RespStatusEnum.SUCCESS;
				} else { 
					updateExpiration(user, getTimeWithFund(time));
					return RespStatusEnum.SUCCESS;
				}
			}	
		} catch(Exception exc){
			throw new Exception(exc.toString());
		}
	}

	public Date getTimeWithFund(int time) {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, time);

		return now.getTime();
	}

	public String getExpiration(String user) throws Exception {
		try {
			connect();
			String sql = "SELECT * FROM radcheck WHERE username = '" + user
					+ "'";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {

				if (rs.getString("attribute").equals("Expiration")) {
					String dateResult = rs.getString("value");
					return dateResult;
				}
			}
		} catch (Exception e) {
			throw new Exception(e.toString());
		} finally {
			close();
		}
		return null;
	}

	public void updateExpiration(String user, Date date) throws Exception {
		try {
			connect();
			String sql = "UPDATE radcheck SET value = ? WHERE attribute = 'Expiration' AND username = ?";
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			preparedStatement.setString(1, getFormatedDate(date));
			preparedStatement.setString(2, user);

			System.out.println(preparedStatement.toString());
			preparedStatement.execute();
		} catch (Exception e) {
			throw new Exception(e.toString());
		} finally {
			close();
		}
	}

	public void insertExpiration(String user, Date date) throws Exception {
		try {
			connect();
			String sql = "INSERT INTO radcheck (username,attribute,op,value) VALUES (?,?,?,?)";
			PreparedStatement preparedStatement = conn.prepareStatement(sql);

			preparedStatement.setString(1, user);
			preparedStatement.setString(2, "Expiration");
			preparedStatement.setString(3, ":=");
			preparedStatement.setString(4, getFormatedDate(date));
			preparedStatement.execute();
		} catch (SQLException e) {
			throw new Exception(e.toString());
		} finally {
			close();
		}

	}

	public Date addMinutesToDate(Date date, int minutes) {
		final long ONE_MINUTE_IN_MILLIS = 60000;

		long dateInMilis = date.getTime();
		Date dateAfterAddingTime = new Date(dateInMilis
				+ (minutes * ONE_MINUTE_IN_MILLIS));

		return dateAfterAddingTime;

	}

	private void connect() throws SQLException {
		conn = DriverManager.getConnection(DB_URL, USER, PASS);
		stmt = conn.createStatement();
	}

	public String getFormatedDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss",
				Locale.ENGLISH);
		return format.format(date);
	}

	public Date getDateFromString(String string) {
		try {

			SimpleDateFormat format = new SimpleDateFormat(
					"dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
			return format.parse(string);
		} catch (Exception e) {
			return null;
		}
	}

	private void close() throws SQLException {
		if (!conn.isClosed())
			conn.close();
		if (!stmt.isClosed())
			stmt.close();
		if (rs != null)
			if (!rs.isClosed())
				rs.close();
	}
}
