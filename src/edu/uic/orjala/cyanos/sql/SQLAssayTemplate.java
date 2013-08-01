/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.Assay.AssayTemplate;
import edu.uic.orjala.cyanos.DataException;

/**
 * @author George Chlipala
 *
 */
public class SQLAssayTemplate extends SQLProtocol implements AssayTemplate {

	private static final String PROTOCOL_TYPE = "assay protocol";		
	public static final String KEY_TARGET = "trgt";
	public static final String KEY_ACTIVE_OP = "active_op";
	public static final String KEY_ACTIVE_LEVEL = "active"; 
	public static final String KEY_UNIT_FORMAT = "unit";
	public static final String KEY_SIZE = "size";
	public static final String KEY_SIG_FIGS = "sigFigs";
	/**
	 * 
	 */
	public static List<String> listProtocols(SQLData data) throws DataException {
		return SQLProtocol.listProtocols(data, PROTOCOL_TYPE);
	}

	protected SQLAssayTemplate(SQLData data) {
		this.myData = data.duplicate();
		this.protocol = new HashMap<String,String>();
	}
	
	public static SQLAssayTemplate create(SQLData data, String name) {
		SQLAssayTemplate item = new SQLAssayTemplate(data);
		item.myName = name;
		return item;		
	}
	
	public static SQLAssayTemplate load(SQLData data, String name) throws DataException {
		SQLAssayTemplate item = new SQLAssayTemplate(data);
		item.loadProtocol(PROTOCOL_TYPE, name);
		item.myName = name;
		return item;
	}
	
	public static void delete(SQLData data, String name) throws DataException {
		SQLProtocol.deleteProtocol(data, PROTOCOL_TYPE, name);
	}

	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#getTarget()
	 */
	@Override
	public String getTarget() {
		return this.protocol.get(KEY_TARGET);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#getActiveOperator()
	 */
	@Override
	public String getActiveOperator() {
		return this.protocol.get(KEY_ACTIVE_OP);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#getActiveLevel()
	 */
	@Override
	public BigDecimal getActiveLevel() {
		if ( this.protocol.containsKey(KEY_ACTIVE_LEVEL) ) {
			return new BigDecimal(this.protocol.get(KEY_ACTIVE_LEVEL));
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#getUnitFormat()
	 */
	@Override
	public String getUnitFormat() {
		return this.protocol.get(KEY_UNIT_FORMAT);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#getLength()
	 */
	@Override
	public int getLength() {
		String size = this.getSize();
		if ( size != null ) {
			String[] sizes = size.split("x");
			if ( sizes != null && sizes.length == 2 )
				return Integer.parseInt(sizes[1]);
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#getWidth()
	 */
	@Override
	public int getWidth() {
		String size = this.getSize();
		if ( size != null ) {
			String[] sizes = size.split("x");
			if ( sizes != null && sizes.length == 2 )
				return Integer.parseInt(sizes[0]);
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#getSize()
	 */
	@Override
	public String getSize() {
		return this.protocol.get(KEY_SIZE);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#setTarget(java.lang.String)
	 */
	@Override
	public void setTarget(String value) {
		this.protocol.put(KEY_TARGET, value);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#setActiveOperator(java.lang.String)
	 */
	@Override
	public void setActiveOperator(String value) {
		this.protocol.put(KEY_ACTIVE_OP, value);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#setActiveLevel(float)
	 */
	@Override
	public void setActiveLevel(BigDecimal value) {
		this.protocol.put(KEY_ACTIVE_LEVEL, value.toPlainString());
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#setUnitFormat(java.lang.String)
	 */
	@Override
	public void setUnitFormat(String value) {
		this.protocol.put(KEY_UNIT_FORMAT, value);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#setLength(int)
	 */
	@Override
	public void setLength(int value) {
		this.setSize(String.format("%dx%d", this.getWidth(), value));
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#setWidth(int)
	 */
	@Override
	public void setWidth(int value) {
		this.setSize(String.format("%dx%d", value, this.getLength()));
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#setSize(java.lang.String)
	 */
	@Override
	public void setSize(String value) {
		this.protocol.put(KEY_SIZE, value);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#create()
	 */
	@Override
	public Assay create(String newID) throws DataException {
		SQLAssay assay = SQLAssay.create(this.myData, newID);
		return assay;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay.AssayProtocol#createInProject(java.lang.String)
	 */
	@Override
	public Assay createInProject(String newID, String projectID) throws DataException {
		SQLAssay assay = SQLAssay.createInProject(this.myData, newID, projectID);
		return assay;
	}
	
	public void save() throws DataException {
		this.saveProtocol(PROTOCOL_TYPE, this.myName);
	}

	@Override
	public void setSigFigs(int value) {
		this.protocol.put(KEY_SIG_FIGS, Integer.toString(value));
	}

	@Override
	public int getSigFigs() {
		if ( this.protocol.containsKey(KEY_SIG_FIGS) ) {
			return Integer.parseInt(this.protocol.get(KEY_SIG_FIGS));
		}
		return 0;
	}

}
