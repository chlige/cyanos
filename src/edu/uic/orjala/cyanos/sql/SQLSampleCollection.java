package edu.uic.orjala.cyanos.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.uic.orjala.cyanos.AccessException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleCollection;
import edu.uic.orjala.cyanos.User;

public class SQLSampleCollection extends SQLBoxObject implements SampleCollection {
	
	//	private ResultSet mySamples = null;
	public final static String ID_COLUMN = "collection";
	public final static String LIBRARY_COLUMN = "library";
	public final static String NAME_COLUMN = "name";
	public final static String NOTES_COLUMN = "notes";
	public final static String LENGTH_COLUMN = "length";
	public final static String WIDTH_COLUMN = "width";

	private static final String SQL_LOAD_SAMPLES = "SELECT sample.* FROM sample WHERE collection = ? ORDER BY row, col ASC";

	
	/*
	 * SQL for "cleaned-up" database schema.
	 * 	
	private static final String SQL_LOAD_SAMPLES = "SELECT sample.* FROM sample WHERE collection = ? ORDER BY loc_x, loc_y ASC";

	 */
	
	private static final String SQL_FIND_ORPHANS = "SELECT DISTINCT collection FROM sample WHERE collection NOT IN (SELECT collection FROM sample_library)";
	
	private final static String SQL_INSERT_COLLECTION = "INSERT INTO sample_library(collection) VALUES(?)";
	private final static String SQL_LOAD_FOR_LIBRARY = "SELECT sample_library.* FROM sample_library WHERE library=? ORDER BY collection";
	private final static String SQL_LOAD_ALL = "SELECT sample_library.* FROM sample_library ORDER BY library,collection";
	private final static String SQL_LOAD = "SELECT sample_library.* FROM sample_library WHERE collection=?";
	private static final String SQL_INSERT_WITH_PROJECT = "INSERT INTO sample_library(collection,project_id) VALUES(?,?)";
	
	private static final String SQL_LOAD_SORTABLE = "SELECT sample_library.* FROM sample_library ORDER BY %s %s";
	private static final String SQL_LOAD_LIBRARIES = "SELECT DISTINCT library FROM sample_library ORDER BY library ASC";
	
	private static final String SQL_TRASH_COLLECTION = "INSERT INTO %s.sample_library(collection,name,library,notes,length) (SELECT collection,name,library,notes,length FROM sample_library WHERE collection=?)";
	private static final String SQL_DELETE_COLLECTION = "DELETE FROM sample_library WHERE collection=?";
	private static final String SQL_GET_SIZE = "SELECT COUNT(sample_id) FROM sample WHERE collection=?";
	
	public static void delete(SQLData data, String collectionID) throws DataException {
		try {
			PreparedStatement aSth = data.prepareStatement(SQL_GET_SIZE);
			aSth.setString(1, collectionID);
			ResultSet aResult = aSth.executeQuery();
			if ( aResult.first() ) {
				int size = aResult.getInt(1);
				aSth.close();
				if ( size == 0 && data.getUser().isAllowed(User.SAMPLE_ROLE, null, Role.DELETE) ) {
					boolean deleteObj = false;
					if ( data.hasTrash() ) {
						aSth = data.prepareStatement(String.format(SQL_TRASH_COLLECTION, data.getTrashCatalog()));
						aSth.setString(1, collectionID);
						deleteObj= (aSth.executeUpdate() > 0);
					} else {
						deleteObj = true;
					}
					aSth.close();
					if ( deleteObj ) {
						aSth = data.prepareStatement(SQL_DELETE_COLLECTION);
						aSth.setString(1, collectionID);
						aSth.execute();
						aSth.close();
					}
				} else {
					throw new AccessException(data.getUser(), User.SAMPLE_ROLE, Role.DELETE);
				}
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public static List<String> orphanedCollections(SQLData data) throws DataException {
		SQLData dataCopy = data.duplicate();
		dataCopy.loadUsingSQL(SQL_FIND_ORPHANS);
		List<String> retval = new ArrayList<String>();
		dataCopy.beforeFirst();
		while ( dataCopy.next() ) {
			retval.add(dataCopy.getString(ID_COLUMN));
		}
		return retval;
	}
	
	/**
	 * Retrieve all sample collections sorted by specified column
	 * 
	 * @param data SQLData object
	 * @param column Column to sort by (use statics)
	 * @param sortDirection Direction for sort @see SQLObject.ASCENDING_SORT and SQLObject.DESCENDING_SORT
	 * @return SampleCollection object with all sample collections.
	 * @throws DataException
	 */
	public static SQLSampleCollection sampleCollections(SQLData data, String column, String sortDirection) throws DataException {
		SQLSampleCollection myCols = new SQLSampleCollection(data);
		String sqlString = String.format(SQL_LOAD_SORTABLE, column, sortDirection);
		myCols.myData.loadUsingSQL(sqlString);
		return myCols;
	}
	
	/**
	 * Retrieve all sample collections sorted by library and collection ID.
	 * 
	 * @param data SQLData object
	 * @return
	 * @throws DataException
	 */
	public static SQLSampleCollection sampleCollections(SQLData data) throws DataException {
		SQLSampleCollection myCols = new SQLSampleCollection(data);
		myCols.myData.loadUsingSQL(SQL_LOAD_ALL);
		return myCols;
	}
	
	public static List<String> libraries(SQLData data) throws DataException {
		SQLData dataCopy = data.duplicate();
		dataCopy.loadUsingSQL(SQL_LOAD_LIBRARIES);
		List<String> retval = new ArrayList<String>();
		dataCopy.beforeFirst();
		while ( dataCopy.next() ) {
			retval.add(dataCopy.getString(LIBRARY_COLUMN));
		}
		return retval;
	}
	
	public static SQLSampleCollection create(SQLData data, String newID) throws DataException {
		SQLSampleCollection aCol = new SQLSampleCollection(data);	
		aCol.makeNewWithValue(SQL_INSERT_COLLECTION, newID);
		return aCol;
	}
	
	/**
	 * Creates a new sample collection record in the specified project. 
	 * This method should be used to ensure data security of new objects.
	 * 
	 * @param data SQLData object.
	 * @param newID ID for the new sample collection.
	 * @param projectID ID of the project.
	 * @return SampleCollection object.
	 * @throws DataException
	 */
	public static SQLSampleCollection createInProject(SQLData data, String newID, String projectID) throws DataException {
		SQLSampleCollection aCol = new SQLSampleCollection(data);	
		aCol.makeNewInProject(SQL_INSERT_WITH_PROJECT, newID, projectID);
		return aCol;
	}
	
	public static SQLSampleCollection loadForLibrary(SQLData data, String aLibrary) throws DataException {
		try {
			SQLSampleCollection aCol = new SQLSampleCollection(data);
			PreparedStatement aSth = aCol.myData.prepareStatement(SQL_LOAD_FOR_LIBRARY);
			aSth.setString(1, aLibrary);
			aCol.myData.loadUsingPreparedStatement(aSth);
			if ( aCol.first() ) 
				return aCol;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public static SQLSampleCollection load(SQLData data, String anID) throws DataException {
		SQLSampleCollection aCol = new SQLSampleCollection(data);
		aCol.myID = anID;
		aCol.fetchRecord();
		return aCol;
	}
	
	protected SQLSampleCollection(SQLData data) {
		super(data);
		this.initVals();
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD, LENGTH_COLUMN, WIDTH_COLUMN);
	}
	
	public Sample getCurrentSample() throws DataException {	
		return this.getSampleForLocation(this.currentRowIndex(), this.currentColumnIndex());
	}
	
	public Sample getSampleForLocation(int row, int column) throws DataException {
		if ( this.inBox() ) {
			Sample aSample = SQLSample.loadFromCollection(this.myData, this.myID, row, column);
			return aSample;
		}
		return null;
	}
	
	protected void initVals() {
		this.idField = "collection";
		this.myData.setAccessRole(User.SAMPLE_ROLE);
	}
		
	public String getLibrary() throws DataException {
		return this.myData.getString(LIBRARY_COLUMN);
	}
	
	public void setLibrary(String newValue) throws DataException {
		this.myData.setString(LIBRARY_COLUMN, newValue);
	}
	
	public String getName() throws DataException {
		return this.myData.getQUIETString(NAME_COLUMN);
	}

	public int getLength() throws DataException {
		return this.myData.getInt(LENGTH_COLUMN);
	}
	
	public int getWidth() throws DataException {
		return this.myData.getInt(WIDTH_COLUMN);
	}
	
	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}
		
	public void setName(String newValue) throws DataException {
		this.myData.setString(NAME_COLUMN, newValue);
	}
	
	public void setLength(int newValue) throws DataException {
		this.maxLength = newValue;
		this.myData.setInt(LENGTH_COLUMN, newValue);
	}
	
	public void setWidth(int newValue) throws DataException {
		this.maxWidth = newValue;
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
	
	public Sample getSamples() throws DataException {
		SQLSample mySamples = new SQLSample(this.myData);
		try {
			PreparedStatement aSth = this.myData.prepareStatement(SQL_LOAD_SAMPLES);
			aSth.setString(1, this.myID);
			mySamples.loadUsingPreparedStatement(aSth);
			return mySamples;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public void addSampleToCurrentLocation(Sample aSample) throws DataException {
		if ( this.myID != null ) {
			aSample.setCollectionID(this.myID);
			aSample.setLocation(this.currRow, this.currCol);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#setupRecord()
	 */
	@Override
	protected void setupRecord() throws DataException {
		super.setupRecord();
		this.maxLength = this.getLength();
		this.maxWidth = this.getWidth();
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#unsetRecord()
	 */
	@Override
	protected void unsetRecord() {
		super.unsetRecord();
		this.maxLength = 0;
		this.maxWidth = 0;
	}
}
