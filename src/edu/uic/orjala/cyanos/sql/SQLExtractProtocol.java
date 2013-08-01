/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Material.ExtractProtocol;
import edu.uic.orjala.cyanos.Separation;

/**
 * @author George Chlipala
 *
 */
public class SQLExtractProtocol extends SQLProtocol implements ExtractProtocol {

	public final static String METHOD_KEY = "method";
	public final static String SOLVENT_KEY = "solvent";
	public final static String TYPE_KEY = "type";
	private static final String PROTOCOL_NAME = "extract protocol";

	
	public static List<ExtractProtocol> protocols(SQLData data) throws DataException {
		List<ExtractProtocol> retVal = new ArrayList<ExtractProtocol>();
		try {
			PreparedStatement psth = data.prepareStatement(SQL_LOAD_ALL);
			psth.setString(1, PROTOCOL_NAME);
			ResultSet results = psth.executeQuery();
			results.beforeFirst();
			while ( results.next() ) {
				ObjectInputStream ioData = new ObjectInputStream(results.getBinaryStream(1));
				if ( ioData instanceof Map<?,?> )
					retVal.add(new SQLExtractProtocol(data, results.getString(2), (Map<String,String>) ioData.readObject()));
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
		return retVal;
	}
	
	public static List<String> protocolNames(SQLData data) throws DataException {
		return SQLProtocol.listProtocols(data, PROTOCOL_NAME);
	}

	protected SQLExtractProtocol(SQLData data, String name, Map<String,String> template) {
		this.myData = data.duplicate();
		this.protocol = template;
		this.myName = name;
	}
	
	public SQLExtractProtocol(SQLData data) {
		this.myData = data.duplicate();
		this.protocol = new HashMap<String,String>();
	}

	public SQLExtractProtocol(SQLData data, String aName) throws DataException {
		this.myData = data.duplicate();
		this.protocol = new HashMap<String,String>();
		this.myName = aName;
		this.loadProtocol(PROTOCOL_NAME, this.myName);
	}

	public static SQLExtractProtocol load(SQLData data, String aName) throws DataException {
		SQLExtractProtocol proto = new SQLExtractProtocol(data);
		proto.myName = aName;
		proto.loadProtocol(PROTOCOL_NAME, aName);
		return proto;
	}

	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material.ExtractProtocol#getExtractSolvent()
	 */
	
	public String getExtractSolvent() {
		return this.protocol.get(SOLVENT_KEY);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material.ExtractProtocol#getExtractType()
	 */
	
	public String getExtractType() {
		return this.protocol.get(TYPE_KEY);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material.ExtractProtocol#getExtractMethod()
	 */
	
	public String getExtractMethod() {
		return this.protocol.get(METHOD_KEY);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material.ExtractProtocol#setExtractSolvent(java.lang.String)
	 */
	
	public void setExtractSolvent(String newSolvent) {
		this.protocol.put(SOLVENT_KEY, newSolvent);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material.ExtractProtocol#setExtractType(java.lang.String)
	 */
	
	public void setExtractType(String newType) {
		this.protocol.put(TYPE_KEY, newType);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material.ExtractProtocol#setExtractMethod(java.lang.String)
	 */
	
	public void setExtractMethod(String newType) {
		this.protocol.put(METHOD_KEY, newType);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material.ExtractProtocol#create(java.lang.String)
	 */
	
	public Material create(String harvestID) throws DataException {

		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material.ExtractProtocol#createInProject(java.lang.String, java.lang.String)
	 */
	
	public Separation createInProject(String harvestID, String projectID)
			throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return this.myName;
	}

	public void save() throws DataException {
		this.saveProtocol(PROTOCOL_NAME, this.myName);
	}

	public void setName(String aName) {
		this.myName = aName;
	}

}
