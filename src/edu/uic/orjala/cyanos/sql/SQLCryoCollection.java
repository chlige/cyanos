/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import edu.uic.orjala.cyanos.Cryo;
import edu.uic.orjala.cyanos.CryoCollection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;

/**
 * @author George Chlipala
 *
 */
public class SQLCryoCollection extends SQLBoxObject implements CryoCollection {

	// Setup the column names here so that changing is easier.	
	private final String FORMAT_COLUMN = "format";
	private final String PARENT_COLUMN = "parent";
	private final String NAME_COLUMN = "name";
	private final String LENGTH_COLUMN = "length";
	private final String WIDTH_COLUMN = "width";
	private final String NOTES_COLUMN = "notes";
	
	private final String DATE_COLUMN = "date";

	private static final String SQL_GET_CRYOS_FOR_INOC = "SELECT cryo.* FROM cryo WHERE source_id=? AND collection=? ORDER BY row,col";
	private static final String SQL_LOAD_SOURCE_INOCS = "SELECT inoculation.* FROM inoculation JOIN cryo c ON ( inoculation.inoculation_id=c.source_id ) WHERE c.collection=? ORDER BY c.row,c.col";
	private static final String SQL_GET_CRYOS = "SELECT cryo.* FROM cryo WHERE collection=? ORDER BY row,col";

	/*
	 * Parameters and SQL for "cleaned-up" database schema.
	 *  
	private final String DATE_COLUMN = "added";
	private static final String SQL_GET_CRYOS_FOR_INOC = "SELECT cryo.* FROM cryo WHERE source_id=? AND collection=? ORDER BY loc_x,loc_y";
	private static final String SQL_LOAD_SOURCE_INOCS = "SELECT inoculation.* FROM inoculation JOIN cryo c ON ( inoculation.inoculation_id=c.source_id ) WHERE c.collection=? ORDER BY c.loc_x,c.loc_y";
	private static final String SQL_GET_CRYOS = "SELECT cryo.* FROM cryo WHERE collection=? ORDER BY loc_x,loc_y";

	private final static String INSERT_COLLECTION_SQL = "INSERT INTO cryo_library(collection) VALUES(?)";
	private final static String SELECT_DEWARS_SQL = "SELECT * from cryo_library WHERE format='dewar' ORDER BY collection";
	private final static String SELECT_KIDS_SQL = "SELECT * from cryo_library WHERE parent=> ORDER BY collection";
	private final static String SELECT_FOR_CULTURE_SQL = "SELECT cryo_library.* FROM cryo_library l JOIN cryo ON(cryo.collection = l.collection) WHERE cryo.culture_id=?";
	private static final String SQL_LOAD = "SELECT cryo_library.* FROM cryo_library WHERE collection=?";
	 */
	
	private final static String INSERT_COLLECTION_SQL = "INSERT INTO cryo_library(collection) VALUES(?)";
	private final static String SELECT_DEWARS_SQL = "SELECT * from cryo_library WHERE format='dewar' ORDER BY collection";
	private final static String SELECT_KIDS_SQL = "SELECT * from cryo_library WHERE parent=? ORDER BY collection";
	private final static String SELECT_FOR_CULTURE_SQL = "SELECT DISTINCT cryo_library.* FROM cryo_library JOIN cryo ON(cryo.collection = cryo_library.collection) WHERE cryo.culture_id=?";
	private static final String SQL_LOAD = "SELECT cryo_library.* FROM cryo_library WHERE collection=?";
	
	private final static String GET_MAX_LOCATION_SQL = "SELECT max(row),max(col) FROM cryo WHERE collection=? AND culture_id=?";
	private final static String GET_MIN_LOCATION_SQL = "SELECT min(row),min(col) FROM cryo WHERE collection=? AND culture_id=?";
	private static final String SQL_GET_CRYOS_FOR_STRAIN = "SELECT cryo.* FROM cryo WHERE culture_id=? AND collection=?";
	
	private static final String SQL_LOAD_FOR_TYPE = "SELECT cryo_library.* FROM cryo_library WHERE format=?";
	
	public static CryoCollection collectionsForType(SQLData data, String aType) throws DataException {
		SQLCryoCollection aCol = new SQLCryoCollection(data);
		try {
			PreparedStatement aSth = data.prepareStatement(SQL_LOAD_FOR_TYPE);
			aSth.setString(1, aType);
			aCol.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aCol;
	}
	
	public SQLCryoCollection(SQLData data) {
		super(data);
		this.initVals();
	}

	public SQLCryoCollection(SQLData data, String anID) throws DataException {
		this(data);
		this.myID = anID;
		this.fetchRecord();
	}
	
	public void create(String newID) throws DataException {
		this.makeNewWithValue(INSERT_COLLECTION_SQL, newID);
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}
	
	public boolean loadDewars() throws DataException {
		this.myData.loadUsingSQL(SELECT_DEWARS_SQL);
		return this.first();
	}
	
	public boolean loadFromParent(String aLibrary) throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SELECT_KIDS_SQL);
			aPsth.setString(1, aLibrary);
			this.myData.loadUsingPreparedStatement(aPsth);
			return this.first();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public boolean loadForStrain(Strain aStrain) throws DataException {
		return this.loadForStrainID(aStrain.getID());
	}
	
	public boolean loadForStrainID(String strainID) throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SELECT_FOR_CULTURE_SQL);
			aPsth.setString(1, strainID);
			this.myData.loadUsingPreparedStatement(aPsth);
			return this.first();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
		
	protected void initVals() {
		this.idField = "collection";
		this.myData.setAccessRole(User.CULTURE_ROLE);
	}
		
	public CryoCollection getChildren() throws DataException {
		String myFormat = this.myData.getString(this.FORMAT_COLUMN);
		if ( myFormat != null && (! myFormat.equals(BOX)) ) {
			SQLCryoCollection aCol = new SQLCryoCollection(this.myData);
			if ( aCol.loadFromParent(this.myID) ) 
				return aCol;
		}
		return null;
	}
	
	public CryoCollection getParent() throws DataException {
		String myParent = this.myData.getString(PARENT_COLUMN);
		if ( myParent != null ) {
			CryoCollection aCol = new SQLCryoCollection(this.myData, myParent);
			if ( aCol.first() ) return aCol;
		}
		return null;		
	}
	
	public String getFormat() throws DataException {
		return this.myData.getString(FORMAT_COLUMN);
	}
	
	public String getName() throws DataException {
		return this.myData.getString(NAME_COLUMN);
	}
	
	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}
	
	public String getDateString() throws DataException {
		return this.myData.getString(DATE_COLUMN);
	}
	
	public Date getDate() throws DataException {
		return this.myData.getDate(DATE_COLUMN);
	}
	
	public int getLength() throws DataException {
		return this.myData.getInt(LENGTH_COLUMN);
	}
	
	public int getWidth() throws DataException {
		return this.myData.getInt(WIDTH_COLUMN);
	}
	public void setName(String newValue) throws DataException {
		this.myData.setString(NAME_COLUMN, newValue);
	}
	
	public void setLength(int newValue) throws DataException {
		this.myData.setInt(LENGTH_COLUMN, newValue);
	}
	
	public void setWidth(int newValue) throws DataException {
		this.myData.setInt(WIDTH_COLUMN, newValue);
	}
	
	public void setNotes(String newValue) throws DataException {
		this.myData.setString(NOTES_COLUMN, newValue);
	}

	public void addNotes(String newNotes) throws DataException {
		StringBuffer curNotes = new StringBuffer(this.myData.getString(NOTES_COLUMN));
		curNotes.append(" ");
		curNotes.append(newNotes);
		this.myData.setString(NOTES_COLUMN, curNotes.toString());
	}
	
	public Cryo getVials() throws DataException {
		SQLCryo myVials = new SQLCryo(this.myData);
		try {
			PreparedStatement aSth = this.myData.prepareStatement(SQL_GET_CRYOS);
			aSth.setString(1, this.myID);
			myVials.loadUsingPreparedStatement(aSth);
			return myVials;
		} catch ( SQLException e ) {
			throw new DataException(e);
		}
	}

	public Cryo getVial(int row, int col) throws DataException {
		Cryo myVial = new SQLCryo(this.myData);
		if ( myVial.loadFromCollection(this.myID, row, col) ) return myVial;
		return null;
	}
	
	public String getMaxLocationForStrainID(String strainID) throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(GET_MAX_LOCATION_SQL);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, strainID);
				ResultSet aResult = aPsth.executeQuery();
				if ( aResult.first() ) {
					String myloc = this.stringLocation(aResult.getInt(1), aResult.getInt(2));
					aResult.close();
					aPsth.close();
					return myloc;
				}
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}
	
	public String getMinLocationForStrainID(String strainID) throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(GET_MIN_LOCATION_SQL);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, strainID);
				ResultSet aResult = aPsth.executeQuery();
				if ( aResult.first() ) {
					String myloc = this.stringLocation(aResult.getInt(1), aResult.getInt(2));
					aResult.close();
					aPsth.close();
					return myloc;
				}
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	public Cryo getCryosForStrainID(String strainID) throws DataException {
		if ( this.myID != null ) {
			SQLCryo aCryo = new SQLCryo(this.myData);
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_CRYOS_FOR_STRAIN);
				aPsth.setString(1, strainID);
				aPsth.setString(2, this.myID);
				aCryo.loadUsingPreparedStatement(aPsth);
				return aCryo;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}
	
	public Cryo getCryosForInoculation(Inoc anInoc) throws DataException {
		return this.getCryosForInoculationID(anInoc.getID());
	}
	
	public Inoc getSourceInocs() throws DataException {
		if ( this.myID != null ) {
			SQLInoc anInoc = new SQLInoc(this.myData);
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_SOURCE_INOCS);
				aPsth.setString(1, this.myID);
				anInoc.loadUsingPreparedStatement(aPsth);
				return anInoc;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}
	
	public Cryo getCryosForInoculationID(String inocID) throws DataException {
		if ( this.myID != null ) {
			SQLCryo aCryo = new SQLCryo(this.myData);
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_CRYOS_FOR_INOC);
				aPsth.setString(1, inocID);
				aPsth.setString(2, this.myID);
				aCryo.loadUsingPreparedStatement(aPsth);
				return aCryo;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	public Cryo getCurrentVial() throws DataException {	
		return this.getVialForLocation(this.currentRowIndex(), this.currentColumnIndex());
	}
	
	public Cryo getVialForLocation(int row, int column) throws DataException {
		if ( this.inBox() ) {
			Cryo aCryo = new SQLCryo(this.myData);
			aCryo.loadFromCollection(this.myID, row, column);
			if ( aCryo.first() ) return aCryo;
		}
		return null;
	}
	
	public void addCryoForLocation(Cryo aCryo, int row, int column) throws DataException {
		if ( this.myID != null ) {
			aCryo.setCollection(this.myID);
			aCryo.setLocation(row, column);
		}
	}
	
	public void addCryoForLocation(Cryo aCryo, String location) throws DataException {
		if ( this.myID != null ) {
			int loc[] = this.parseLocation(location);
			this.addCryoForLocation(aCryo, loc[0], loc[1]);
		}
	}
	
	public void addCryoForCurrentLocation(Cryo aCryo) throws DataException {
		this.addCryoForLocation(aCryo, this.currRow, this.currCol);
	}
}
