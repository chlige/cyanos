/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.util.HashMap;
import java.util.List;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.Separation.SeparationTemplate;

/**
 * @author George Chlipala
 *
 */
public class SQLSeparationTemplate extends SQLProtocol implements SeparationTemplate {

	public final static String METHOD_KEY = "method";
	public final static String STATIONARY_PHASE_KEY = "sphase";
	public final static String MOBILE_PHASE_KEY = "mphase";
	private static final String PROTOCOL_TYPE = "fraction protocol";

	/**
	 * 
	 */
	public static List<String> listProtocols(SQLData data) throws DataException {
		return SQLProtocol.listProtocols(data, PROTOCOL_TYPE);
	}

	protected SQLSeparationTemplate(SQLData data) {
		this.myData = data.duplicate();
		this.protocol = new HashMap<String,String>();
	}
	
	public static SQLSeparationTemplate create(SQLData data, String name) {
		SQLSeparationTemplate item = new SQLSeparationTemplate(data);
		item.myName = name;
		return item;		
	}

	public static SQLSeparationTemplate load(SQLData data, String name) throws DataException {
		SQLSeparationTemplate item = new SQLSeparationTemplate(data);
		item.loadProtocol(PROTOCOL_TYPE, name);
		item.myName = name;
		return item;
	}
	
	public static void delete(SQLData data, String name) throws DataException {
		SQLProtocol.deleteProtocol(data, PROTOCOL_TYPE, name);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Separation.SeparationProtocol#getMethod()
	 */
	public String getMethod() {
		return this.protocol.get(METHOD_KEY);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Separation.SeparationProtocol#getMobilePhase()
	 */
	public String getMobilePhase() {
		return this.protocol.get(MOBILE_PHASE_KEY);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Separation.SeparationProtocol#getStationaryPhase()
	 */
	public String getStationaryPhase() {
		return this.protocol.get(STATIONARY_PHASE_KEY);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Separation.SeparationProtocol#setMethod(java.lang.String)
	 */
	public void setMethod(String newValue) {
		this.protocol.put(METHOD_KEY, newValue);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Separation.SeparationProtocol#setMobilePhase(java.lang.String)
	 */
	public void setMobilePhase(String newValue) {
		this.protocol.put(MOBILE_PHASE_KEY, newValue);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Separation.SeparationProtocol#setStationaryPhase(java.lang.String)
	 */
	public void setStationaryPhase(String newValue) {
		this.protocol.put(STATIONARY_PHASE_KEY, newValue);
	}

	public Separation create() throws DataException {
		Separation aSep = SQLSeparation.create(this.myData);
		aSep.setTemplate(this);
		return aSep;
	}

	public Separation createInProject(String projectID) throws DataException {
		Separation aSep = SQLSeparation.createInProject(this.myData, projectID);
		aSep.setTemplate(this);
		return aSep;
	}

	public String getName() {
		return this.myName;
	}

	public void save() throws DataException {
		this.saveProtocol(PROTOCOL_TYPE, this.myName);
	}

	public void setName(String aName) {
		this.myName = aName;
	}

}

