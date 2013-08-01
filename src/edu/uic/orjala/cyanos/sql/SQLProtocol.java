/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uic.orjala.cyanos.DataException;

/**
 * @author George Chlipala
 *
 */
public class SQLProtocol {
	
	protected SQLData myData = null;
	protected String myID = null;
	protected Map<String,String> protocol = null;
	protected String myName = null;
	
	private static final String SQL_LIST_PROTOCOL = "SELECT name FROM data_templates WHERE data=?";
	private static final String SQL_LOAD_PROTOCOL = "SELECT template,name,data FROM data_templates WHERE name=? AND data=?";
	protected static final String SQL_LOAD_ALL = "SELECT template,name,data FROM data_templates WHERE data=?";
	private static final String SQL_DELETE_PROTOCOL = "DELETE FROM data_templates WHERE name=? AND data=?";
	private static final String SQL_SAVE_PROTOCOL = "REPLACE INTO data_templates(data,name,template) VALUES(?,?,?)";

	
	@SuppressWarnings("unchecked")
	public static Map<String,String> loadProtocol(SQLData data, String dataType, String name) throws DataException {
		Map<String,String> retValue = null;
		try {
			PreparedStatement psth = data.prepareStatement(SQL_LOAD_PROTOCOL);
			psth.setString(1, name);
			psth.setString(2, dataType);
			ResultSet results = psth.executeQuery();
			if ( results.first() ) {
				ObjectInputStream ioData = new ObjectInputStream(results.getBinaryStream(1));
				retValue = (HashMap<String,String>) ioData.readObject();
			}
			results.close();
			psth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		} catch (IOException e) {
			throw new DataException(e);
		} catch (ClassNotFoundException e) {
			throw new DataException(e);
		}
		return retValue;
	}

	
	protected void loadProtocol(String dataType, String name) throws DataException {
		if ( this.protocol != null )
			this.protocol.clear();
		this.protocol = SQLProtocol.loadProtocol(this.myData, dataType, name);
	}
	
	public static void saveProtocol(SQLData data, Map<String,String> aProtocol, String dataType, String name) throws DataException {
		if ( name != null ) {
			try {
				PreparedStatement psth = data.prepareStatement(SQL_SAVE_PROTOCOL);
				psth.setString(1, dataType);
				psth.setString(2, name);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream outData = new ObjectOutputStream(out);	
				outData.writeObject(aProtocol);
				psth.setBytes(3, out.toByteArray());
				psth.executeUpdate();
				psth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			} catch (IOException e) {
				throw new DataException(e);
			}
		}
	}
	
	public static void deleteProtocol(SQLData data, String dataType, String name) throws DataException {
		if ( name != null ) {
			try {
				PreparedStatement psth = data.prepareStatement(SQL_DELETE_PROTOCOL);
				psth.setString(1, name);
				psth.setString(2, dataType);
				psth.executeUpdate();
				psth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}
	
	protected void saveProtocol(String dataType, String name) throws DataException {
		SQLProtocol.saveProtocol(this.myData, this.protocol, dataType, name);
	}

	
	public static List<String> listProtocols(SQLData data, String dataType) throws DataException {
		try {
			PreparedStatement psth = data.prepareStatement(SQL_LIST_PROTOCOL);
			psth.setString(1, dataType);
			ResultSet results = psth.executeQuery();
			results.beforeFirst();
			List<String> aList = new ArrayList<String>();
			while (results.next()) {
				aList.add(results.getString(1));
			}
			results.close();
			psth.close();
			return aList;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public String getName() {
		return this.myName;
	}

	public void setName(String aName) {
		this.myName = aName;
	}


	
}
