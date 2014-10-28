/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.sql.PreparedStatement;
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

public class SQLCryo extends SQLObject implements Cryo {
	
	// Setup the column names here so that changing is easier.	
	private final static String COLLECTION_COLUMN = "collection";
	private final static String SOURCE_COLUMN = "source_id";
	private final static String THAW_COLUMN = "thaw_id";
	private final static String NOTES_COLUMN = "notes";
	private final static String CULTURE_ID_COLUMN = "culture_id";

	protected boolean rowAlpha = true;
	protected boolean colAlpha = false;

	/*
	 * Parameters and SQL for "cleaned-up" database schema.
	 * 	
	private final static String DATE_COLUMN = "added";
	private final static String ROW_COLUMN = "loc_x";
	private final static String COLUMN_COLUMN = "loc_y";

	private static final String INSERT_CRYO_SQL = "INSERT INTO cryo(added) VALUES(CURRENT_DATE)";
	private final static String SELECT_FROM_COLLECTION_SQL = "SELECT * FROM cryo WHERE collection=? AND loc_x=? AND loc_y=?";

	 */

	private final static String DATE_COLUMN = "date";
	private final static String ROW_COLUMN = "row";
	private final static String COLUMN_COLUMN = "col";


	private static final String INSERT_CRYO_SQL = "INSERT INTO cryo(source_id, date) VALUES(?, CURRENT_DATE)";
	private final static String SELECT_FROM_COLLECTION_SQL = "SELECT * FROM cryo WHERE collection=? AND row=? AND col=?";

	private static final String SQL_LOAD = "SELECT cryo.* FROM cryo WHERE cryo_id=?";	
	private final static String SELECT_FOR_STRAIN_SQL = "SELECT * FROM cryo WHERE culture_id=?";
	private static final String SQL_LOAD_FOR_COLLECTION = "SELECT * FROM cryo WHERE collection=?";
	
	public static Cryo loadForCollection(SQLData data, String collectionID) throws DataException, SQLException {
		SQLCryo cryo = new SQLCryo(data);
		PreparedStatement aPsth = data.prepareStatement(SQL_LOAD_FOR_COLLECTION);
		aPsth.setString(1, collectionID);
		cryo.loadUsingPreparedStatement(aPsth);
		return cryo;		
	}
	
	public static Cryo create(SQLData data, String inocID) throws DataException {
		SQLCryo aCryo = new SQLCryo(data);
		PreparedStatement aSth = aCryo.myData.prepareStatement(INSERT_CRYO_SQL);
		aCryo.makeNewWithAutonumber(aSth);
		return aCryo;
	}
	
	public static SQLCryo loadForStrain(SQLData data, String cultureID) throws DataException, SQLException {
		SQLCryo cryo = new SQLCryo(data);
		PreparedStatement aPsth = data.prepareStatement(SELECT_FOR_STRAIN_SQL);
		aPsth.setString(1, cultureID);
		cryo.loadUsingPreparedStatement(aPsth);
		return cryo;
	}
	
	public static Cryo load(SQLData data, String cryoID) throws DataException {
		SQLCryo cryo = new SQLCryo(data, cryoID);
		return cryo;
	}

	
	public SQLCryo(SQLData data) {
		super(data);
		this.initVals();
	}

	public SQLCryo(SQLData data, String anID) throws DataException {
		this(data);
		this.myID = anID;
		this.fetchRecord();
	}
	
	protected void initVals() {
		this.idField = "cryo_id";
		this.myData.setAccessRole(User.CULTURE_ROLE);
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}
	
	public String getLocation() throws DataException {
		int myRow = this.myData.getInt(ROW_COLUMN);
		int myCol = this.myData.getInt(COLUMN_COLUMN);
		if ( myRow > 0 & myCol > 0 ) {
			String row = ( this.rowAlpha ? ALPHABET[myRow] : String.valueOf(myRow) );
			String col = ( this.colAlpha ? ALPHABET[myCol] : String.valueOf(myCol) );
			return row + col;
		} else if ( myRow > 0 ) {
			return String.valueOf(myRow);
		} else if ( myCol > 0 ) {
			return String.valueOf(myCol);
		} else 
			return null;
	}
	
	public String getRow() throws DataException {
		return this.myData.getString(ROW_COLUMN);
	}
	
	public String getColumn() throws DataException {
		return this.myData.getString(COLUMN_COLUMN);
	}
	
	public String getCultureID() throws DataException {
		return this.myData.getString(CULTURE_ID_COLUMN);
	}
	
	public Strain getStrain() throws DataException {
		String cultureID = this.getCultureID();
		if ( cultureID != null ) 
			return SQLStrain.load(myData, cultureID);
		return null;
	}

	public String getCollectionID() throws DataException {
		return this.myData.getString(COLLECTION_COLUMN);
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
	
	public Inoc getSourceInoc() throws DataException {
		String anID = this.myData.getString(SOURCE_COLUMN);
		if ( anID != null ) { 
			Inoc anInoc = SQLInoc.load(this.myData, anID);
			if ( anInoc.first() )
				return anInoc;
		}
		return null;
	}
	
	public Inoc getThawInoc() throws DataException {
		String anID = this.myData.getString(THAW_COLUMN);
		if ( anID != null ) { 
			Inoc anInoc = SQLInoc.load(this.myData, anID);
			if ( anInoc.first() )
				return anInoc;
		}
		return null;
	}
	
	public void setRow(String newValue) throws DataException {
		this.myData.setString(ROW_COLUMN, newValue);
	}
	
	public void setColumn(String newValue) throws DataException {
		this.myData.setString(COLUMN_COLUMN, newValue);
	}
	
	public void setLocation(String aRow, String aCol) throws DataException {
		this.myData.setString(ROW_COLUMN, aRow);
		this.myData.setString(COLUMN_COLUMN, aCol);
	}
	
	public void setLocation(int row, int column) throws DataException {
		this.myData.setInt(ROW_COLUMN, row);
		this.myData.setInt(COLUMN_COLUMN, column);
	}

	public void setCultureID(String newCultureID) throws DataException {
		this.myData.setString(CULTURE_ID_COLUMN, newCultureID);
	}

	public void setNotes(String newNotes) throws DataException {
		this.myData.setString(NOTES_COLUMN, newNotes);
	}
	
	public void setDate(String newValue) throws DataException {
		this.myData.setString(DATE_COLUMN, newValue);
	}
	
	public void setDate(Date newValue) throws DataException {
		this.myData.setDate(DATE_COLUMN, newValue);
	}
	
	public void setCollection(CryoCollection aCol) throws DataException {
		this.myData.setString(COLLECTION_COLUMN, aCol.getID());
	}
	
	public void setCollection(String aColID) throws DataException {
		this.myData.setString(COLLECTION_COLUMN, aColID);
	}
	
	public void setSourceInoc(Inoc anInoc) throws DataException {
		this.myData.setString(SOURCE_COLUMN, anInoc.getID());
		this.myData.setString(CULTURE_ID_COLUMN, anInoc.getStrainID());
	}
	
	/**
	 * 
	 * Will return true if a thaw ID exists.
	 * 
	 * @return boolean
	 * @throws DataException
	 */
	
	public boolean isThawed() throws DataException {
		String anID = this.myData.getString(THAW_COLUMN);
		if ( anID != null ) return true;
		return false;
	}

	public boolean wasRemoved() throws DataException {
		String anID = this.myData.getString(ROW_COLUMN);
		if ( anID == null ) return true;
		return false;
	}

	public boolean isFrozen() throws DataException {
		String anID = this.myData.getString(ROW_COLUMN);
		if ( anID != null ) return true;
		return false;
	}

	public Inoc thaw() throws DataException {
		if ( this.myData == null ) return null;
		Strain myStrain = SQLStrain.load(this.myData, this.getCultureID());
		Inoc anInoc = SQLInoc.createInProject(this.myData, this.getCultureID(), myStrain.getProjectID());
		if ( anInoc.first() ) {
			this.myData.setNull(ROW_COLUMN);
			this.myData.setNull(COLUMN_COLUMN);
			this.myData.setString(THAW_COLUMN, anInoc.getID());
			this.myData.refresh();
			return anInoc;
		}
		return null;
	}
	
	public void remove() throws DataException {
		if ( this.myData != null ) {
			this.myData.setNull(ROW_COLUMN);
			this.myData.setNull(COLUMN_COLUMN);
			this.myData.refresh();
		}
	}
	
	public boolean loadFromCollection(CryoCollection aCol, int row, int column) throws DataException {
		return this.loadFromCollection(aCol.getID(), row, column);
	}
	
	public boolean loadFromCollection(String collectionID, int row, int column) throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SELECT_FROM_COLLECTION_SQL);
			aPsth.setString(1, collectionID);
			aPsth.setInt(2, row);
			aPsth.setInt(3, column);
			this.myData.loadUsingPreparedStatement(aPsth);
			return this.first();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public boolean loadForStrain(Strain aStrain) throws DataException {
		return this.loadForStrainID(aStrain.getID());
	}
	
	public boolean loadForStrainID(String cultureID) throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SELECT_FOR_STRAIN_SQL);
			aPsth.setString(1, cultureID);
			this.myData.loadUsingPreparedStatement(aPsth);
			return this.first();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	

}
