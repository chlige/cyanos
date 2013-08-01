/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.uic.orjala.cyanos.AccessException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.TaxonFlat;
import edu.uic.orjala.cyanos.User;


/**
 * Class to retrieve taxonomic data.
 * 
 * <P>This data is stored in the SQL table <CODE>taxonomic</CODE>.</P>
 * 
 * <TABLE BORDER=1><TR><TD><B>SQL Column</B></TD><TD><B>SQL Data Type</B></TD><TD><B>Java Constant</B></TD></TR>
 * <TR><TD>kingdom</TD><TD>VARCHAR(64)</TD><TD>KINGDOM</TD></TR>
 * <TR><TD>phylum</TD><TD>VARCHAR(64)</TD><TD>PHYLUM</TD></TR>
 * <TR><TD>class</TD><TD>VARCHAR(64)</TD><TD>CLASS</TD></TR>
 * <TR><TD>ord</TD><TD>VARCHAR(64)</TD><TD>ORDER</TD></TR>
 * <TR><TD>family</TD><TD>VARCHAR(64)</TD><TD>FAMILY</TD></TR>
 * <TR><TD>genus</TD><TD>VARCHAR(64)</TD><TD>GENUS</TD></TR>
 * <TR><TD>syn</TD><TD>VARCHAR(64)</TD><TD>SYNONYM</TD></TR>
 * </TABLE>
 * 
 * <P>Records in this table are indexed by <CODE>genus</CODE>, thus creation of new taxon must start from the genus.
 * Currently, one cannot manipulate data from the web interface.  Instead, updates are done via the SQL CLI and scripts.</P>
 * 
 * @author George Chlipala
 * @version 1.0
 *
 */
@Deprecated
public class SQLTaxonFlat extends SQLObject implements TaxonFlat {

	/*
	 * CREATE TABLE `taxonomic` (
  		`kingdom` varchar(45) NOT NULL default '',
  		`phylum` varchar(45) NOT NULL default '',
  		`class` varchar(45) NOT NULL default '',
  		`ord` varchar(45) NOT NULL default '',
  		`family` varchar(45) NOT NULL default '',
  		`genus` varchar(45) NOT NULL default '',
  		`synonym` varchar(45) default NULL,
  		PRIMARY KEY  (`genus`),
  		KEY `family` (`family`),
  		KEY `class` (`class`),
  		KEY `phylum` (`phylum`),
  		KEY `order` (`ord`)
		) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Taxonomic map';
	 */
	
	public final static int NUMBER_OF_LEVELS = 7;
	
	public static final String KINGDOM_COLUMN = "kingdom";
	public static final String PHYLUM_COLUMN = "phylum";
	public static final String CLASS_COLUMN = "class";
	public static final String ORDER_COLUMN = "ord";
	public static final String FAMILY_COLUMN = "family";
	public static final String GENUS_COLUMN = "genus";
	public static final String SYNONYM_COLUMN = "synonym";
	
	public static final String[] SQL_COLUMN= { SYNONYM_COLUMN, KINGDOM_COLUMN, PHYLUM_COLUMN, CLASS_COLUMN, ORDER_COLUMN, FAMILY_COLUMN, GENUS_COLUMN };
	private int currLevel = 6;
	
	private final static String INSERT_TAXON_SQL = "INSERT INTO taxonomic(genus) VALUES(?)";
	private static final String SQL_LOAD_KINGDOMS = "SELECT DISTINCT kingdom FROM taxonomic";
	private static final String SQL_UPDATE_SYNOMYMS = "UPDATE taxonomic SET %s=? WHERE syn=?";
	private static final String SQL_SELECT_STAIN_TEMPLATE = "SELECT species.* FROM species JOIN taxonomic t ON(t.genus = species.genus) WHERE t.%s=?";
	private static final String SQL_LOAD_ALL_FOR = "SELECT taxonomic.* FROM taxonomic WHERE %s='%s'";
	private static final String SQL_LOAD_ALL = "SELECT taxonomic.* FROM taxonomic";
	
	public final static String SQL_ADD_TAXON = "REPLACE taxonomic(kingdom,phylum,class,ord,family,genus) VALUES(?,?,?,?,?,?)";
	
	private static final String SQL_LOAD_SOUNDEX = "SELECT DISTINCT %s FROM taxonomic WHERE %s SOUNDS LIKE ?";
		
	public static TaxonFlat allTaxaForLevel(SQLData data, int level, String value) throws DataException {
		SQLTaxonFlat myTaxon = new SQLTaxonFlat(data);
		String sqlString = String.format(SQL_LOAD_ALL_FOR, SQL_COLUMN[level], value);
		myTaxon.myData.loadUsingSQL(sqlString);
		myTaxon.currLevel = GENUS;
		return myTaxon;
	}
	
	public static TaxonFlat allTaxa(SQLData data) throws DataException {
		SQLTaxonFlat myTaxon = new SQLTaxonFlat(data);
		myTaxon.myData.loadUsingSQL(SQL_LOAD_ALL);
		myTaxon.currLevel = GENUS;
		return myTaxon;		
	}
	
	public static TaxonFlat taxaLike(SQLData data, int level, String like) throws DataException {
		SQLTaxonFlat myTaxon = new SQLTaxonFlat(data);		
		String columns = SQL_COLUMN[KINGDOM];
		for (int i = PHYLUM; i <= level; i++) {
			columns = String.format("%s,%s", columns, SQL_COLUMN[i]);
		}
		if ( level == GENUS )
			columns = String.format("%s,%s", columns, SQL_COLUMN[SYNONYM]);
	
		String sqlString = String.format("SELECT DISTINCT %s FROM taxonomic WHERE %s LIKE ? ORDER BY %s", 
				columns, SQL_COLUMN[level], SQL_COLUMN[level]);
		try {
			PreparedStatement aSth = myTaxon.myData.prepareStatement(sqlString);
			aSth.setString(1, like);
			myTaxon.myData.loadUsingPreparedStatement(aSth);
			
		} catch (SQLException e) {
			throw new DataException(e);
		}
		myTaxon.currLevel = level;
		return myTaxon;
	}
	
	public static TaxonFlat taxaLike(SQLData data, int level, String[] like) throws DataException {
		SQLTaxonFlat myTaxon = new SQLTaxonFlat(data);		
		String columns = SQL_COLUMN[KINGDOM];
		for (int i = PHYLUM; i <= level; i++) {
			columns = String.format("%s,%s", columns, SQL_COLUMN[i]);
		}
		if ( level == GENUS )
			columns = String.format("%s,%s", columns, SQL_COLUMN[SYNONYM]);

		String likeStm = String.format("%s LIKE ?", SQL_COLUMN[level]);
		String where = new String(likeStm);
		for (int i = 1; i < like.length; i++) {
			where = where.concat(" OR ");
			where = where.concat(likeStm);
		}
		String sqlString = String.format("SELECT DISTINCT %s FROM taxonomic WHERE (%s) ORDER BY %s", 
				columns, where, SQL_COLUMN[level], SQL_COLUMN[level]);
		try {
			PreparedStatement aSth = myTaxon.myData.prepareStatement(sqlString);
			for (int i = 0; i < like.length; i++ ) {	
				aSth.setString(i + 1, like[i]);
			}
			myTaxon.myData.loadUsingPreparedStatement(aSth);
			
		} catch (SQLException e) {
			throw new DataException(e);
		}
		myTaxon.currLevel = level;
		return myTaxon;
	}

	
	public SQLTaxonFlat(SQLData data) {
		super(data);
		this.initVals();
	}
	
	public SQLTaxonFlat(SQLData data, String anID) throws DataException {
		this(data);
		this.myID = anID;
		this.fetchRecord();
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(this.buildSQLForLevel(this.currLevel));
	}
	
	protected void initVals() {
		this.idField = SQL_COLUMN[this.currLevel];
		this.myData.setAccessRole(User.CULTURE_ROLE);
//		this.buildSQLForCurrentLevel();
	}
	
	public void create(String newGenus) throws DataException {
		this.makeNewWithValue(INSERT_TAXON_SQL, newGenus);
	}
	
	protected boolean newGenus(String newValue) throws DataException {
		if ( this.myData != null && this.myData.getRow() > 0 ) {
			String kingdom = this.myData.getString(SQL_COLUMN[KINGDOM]);
			String phylum = this.myData.getString(SQL_COLUMN[PHYLUM]);
			String classis = this.myData.getString(SQL_COLUMN[CLASS]);
			String ordo = this.myData.getString(SQL_COLUMN[ORDER]);
			String family = this.myData.getString(SQL_COLUMN[FAMILY]);
			
			this.create(newValue);
			boolean autoRefresh = this.myData.autoRefresh();
			this.setManualRefresh();
			this.myData.setString(SQL_COLUMN[FAMILY], family);
			this.myData.setString(SQL_COLUMN[ORDER], ordo);
			this.myData.setString(SQL_COLUMN[CLASS], classis);
			this.myData.setString(SQL_COLUMN[PHYLUM], phylum);
			this.myData.setString(SQL_COLUMN[KINGDOM], kingdom);
			this.myData.refresh();
			this.myID = newValue;
			if ( autoRefresh ) this.setAutoRefresh();
			
			return this.first();
		}
		return false;
	}
	
//	private void buildSQLForCurrentLevel() {
//		this.baseSQL = this.buildSQLForLevel(this.currLevel, this.currLevel);
//	}
	
	private String buildSQLForLevel(int level) {
		return buildSQLForLevel(level, level);
	}
	
	private static String buildSQLForLevel(int selectLevel, int whereLevel) {
		String columns = SQL_COLUMN[KINGDOM];
		for (int i = PHYLUM; i <= selectLevel; i++) {
			columns = String.format("%s,%s", columns, SQL_COLUMN[i]);
		}
		if ( selectLevel == GENUS )
			columns = String.format("%s,%s", columns, SQL_COLUMN[SYNONYM]);
		return String.format("SELECT DISTINCT %s FROM taxonomic WHERE %s=? ORDER BY %s", columns, SQL_COLUMN[whereLevel], SQL_COLUMN[selectLevel]);
	}
	
	public boolean loadKingdoms() throws DataException {
		if ( this.myData != null ) {
			this.myData.loadUsingSQL(SQL_LOAD_KINGDOMS);
			this.currLevel = KINGDOM;
			return this.myData.first();
		}
		return false;
	}
	
	public boolean loadForKingdom(int aLevel, String aKingdom) throws DataException {
		return this.loadForTaxonLevel(aLevel, KINGDOM, aKingdom);
	}
	
	public boolean loadForPhylum(int aLevel, String aPhylum) throws DataException {
		return this.loadForTaxonLevel(aLevel, PHYLUM, aPhylum);
	}
	
	public boolean loadForClass(int aLevel, String aClass) throws DataException {
		return this.loadForTaxonLevel(aLevel, CLASS, aClass);
	}
	
	public boolean loadForOrder(int aLevel, String anOrder) throws DataException {
		return this.loadForTaxonLevel(aLevel, ORDER, anOrder);
	}
	
	public boolean loadForFamily(int aLevel, String aFamily) throws DataException {
		return this.loadForTaxonLevel(aLevel, FAMILY, aFamily);
	}
	
	public boolean loadForTaxonLevel(int selectLevel, int whereLevel, String whereValue) throws DataException {
		if ( selectLevel < KINGDOM ) return false;
		this.currLevel = selectLevel;
		String sqlString = buildSQLForLevel(selectLevel, whereLevel);
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(sqlString);
			aPsth.setString(1, whereValue);
			this.myData.loadUsingPreparedStatement(aPsth);
			return this.myData.first();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public String getValueForLevel(int level) throws DataException {
		if ( level >= KINGDOM && this.currLevel >= level ) 
			return this.myData.getString(SQL_COLUMN[level]);
		return null;
	}
	
	public String getKingdom() throws DataException {
		return this.myData.getString(SQL_COLUMN[KINGDOM]);
	}
	
	public String getPhylum() throws DataException {
		return this.myData.getString(SQL_COLUMN[PHYLUM]);
	}
	
	public String getTaxonClass() throws DataException {
		return this.myData.getString(SQL_COLUMN[CLASS]);
	}
		
	public String getOrder() throws DataException {
		return this.myData.getString(SQL_COLUMN[ORDER]);
	}
	
	public String getFamily() throws DataException {
		return this.myData.getString(SQL_COLUMN[FAMILY]);
	}
	
	public String getGenus() throws DataException {
		return this.myData.getString(SQL_COLUMN[GENUS]);
	}
	
	public void setKingdom(String newValue) throws DataException {
		if ( this.currLevel == GENUS ) {
			this.myData.setString(SQL_COLUMN[KINGDOM], newValue);
			this.updateSynonyms(KINGDOM, newValue);
		} 
	}

	public void setPhylum(String newValue) throws DataException {
		if ( this.currLevel == GENUS ) {
			this.myData.setString(SQL_COLUMN[PHYLUM], newValue);
			this.updateSynonyms(PHYLUM, newValue);
		}
	}

	public void setTaxonClass(String newValue) throws DataException {
		if ( this.currLevel == GENUS ) {
			this.myData.setString(SQL_COLUMN[CLASS], newValue);
			this.updateSynonyms(CLASS, newValue);
		}
	}

	public void setOrder(String newValue) throws DataException {
		if ( this.currLevel == GENUS ) {
			this.myData.setString(SQL_COLUMN[ORDER], newValue);
			this.updateSynonyms(ORDER, newValue);
		}
	}
	
	public void setFamily(String newValue) throws DataException {
		if ( this.currLevel == GENUS ) {
			this.myData.setString(SQL_COLUMN[FAMILY], newValue);
			this.updateSynonyms(FAMILY, newValue);
		}
	}
	
	public boolean loadAllInKingdom() throws DataException {
		if ( this.currLevel >= KINGDOM )
			return this.loadForKingdom(this.getKingdom());
		return false;
	}

	public boolean loadAllInPhylum() throws DataException {
		if ( this.currLevel >= PHYLUM )
			return this.loadForPhylum(this.getPhylum());
		return false;
	}

	public boolean loadAllInClass() throws DataException {
		if ( this.currLevel >= CLASS )
			return this.loadForClass(this.getTaxonClass());
		return false;
	}

	public boolean loadAllInOrder() throws DataException {
		if ( this.currLevel >= ORDER )
			return this.loadForOrder(this.getOrder());
		return false;
	}

	public boolean loadAllInFamily() throws DataException {
		if ( this.currLevel >= FAMILY )
			return this.loadForFamily(this.getFamily());
		return false;
	}

	public boolean loadForKingdom(String aValue) throws DataException {
		return this.reloadForLevel(KINGDOM, aValue);
	}
	
	public boolean loadForPhylum(String aValue) throws DataException {
		return this.reloadForLevel(PHYLUM, aValue);
	}
	
	public boolean loadForOrder(String aValue) throws DataException {
		return this.reloadForLevel(ORDER, aValue);
	}
	
	public boolean loadForClass(String aValue) throws DataException {
		return this.reloadForLevel(CLASS, aValue);
	}
	
	public boolean loadForFamily(String aValue) throws DataException {
		return this.reloadForLevel(FAMILY, aValue);
	}
	
	public boolean reloadForLevel(int level, String aValue) throws DataException {
		String sqlString = buildSQLForLevel(this.currLevel, level);
		try {
			PreparedStatement aSth = this.myData.prepareStatement(sqlString);
			aSth.setString(1, aValue);
			this.myData.loadUsingPreparedStatement(aSth);
			return this.first();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public TaxonFlat getSynonym() throws DataException {
		if ( this.currLevel == GENUS ) {
			String mySyn = this.myData.getString(SQL_COLUMN[SYNONYM]);
			if ( mySyn != null ) {
				TaxonFlat synTax = new SQLTaxonFlat(this.myData, mySyn);
				return synTax;
			}
		}
		return null;
	}

	public void setSynonym(String newValue) throws DataException {
		if ( this.currLevel == GENUS )
			this.myData.setString(SQL_COLUMN[SYNONYM], newValue);
	}

	public void setSynonym(TaxonFlat newSyn) throws DataException {
		this.setSynonym(newSyn.getGenus());
	}

	public boolean hasSynonym() throws DataException {
		if ( this.currLevel == GENUS ) {
			String aSyn = this.myData.getString(SQL_COLUMN[SYNONYM]);
			if ( aSyn != null ) return true;
		}
		return false;
	}

	public boolean isValid() throws DataException {
		if ( this.currLevel == GENUS ) {
			return this.myData.isNull(SQL_COLUMN[SYNONYM]);
		}
		return false;
	}

	private void updateSynonyms(int column, String newValue) throws DataException {
		if ( this.currLevel == GENUS && this.isAllowed(Role.WRITE) ) {
			String genus = this.myData.getString(SQL_COLUMN[GENUS]);
			String sql = String.format(SQL_UPDATE_SYNOMYMS, SQL_COLUMN[column]);
			try {
				PreparedStatement aSth = this.myData.prepareStatement(sql);
				aSth.setString(1, newValue);
				aSth.setString(2, genus);
				aSth.executeUpdate();
				aSth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		} else if ( this.myData.willThrowAccessExeceptions() ) {
			throw new AccessException(this.myData.getUser(), User.CULTURE_ROLE, Role.WRITE);
		}
	}
	
	public int getCurrentLevel() {
		return this.currLevel;
	}
	
	public Strain getStrainsForTaxon() throws DataException {
		SQLStrain aStrain = new SQLStrain(this.myData);
		String sqlString = String.format(SQL_SELECT_STAIN_TEMPLATE, SQL_COLUMN[this.currLevel]);
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(sqlString);
			aPsth.setString(1, this.myData.getString(SQL_COLUMN[this.currLevel]));
			aStrain.loadUsingPreparedStatement(aPsth);
			if ( aStrain.first() ) 
				return aStrain;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}

	/*
	@Deprecated
	public boolean loadTaxonWhere(String sqlWhere) throws DataException {
		if (this.baseSQL == null )
			return false;
		String sqlQuery = this.baseSQL + " " + sqlWhere;
		this.myData.loadUsingSQL(sqlQuery);
		return this.first();
	}
	*/
	
	public boolean next() throws DataException {
		boolean retval = false;
		if ( this.myData != null ) 
			retval = this.myData.next();
		if ( retval && this.currLevel == GENUS) this.myID = this.myData.getString(this.idField);
		return retval;
	}

	public boolean previous() throws DataException {
		boolean retval = false;
		if ( this.myData != null ) 
			retval = this.myData.previous();
		if ( retval && this.currLevel == GENUS) this.myID = this.myData.getString(this.idField);
		return retval;
	}
	
	public boolean first() throws DataException {
		boolean retval = false;
		if ( this.myData != null ) 
			retval = this.myData.first();
		if ( retval && this.currLevel == GENUS) this.myID = this.myData.getString(this.idField);
		return retval;
	}
	
	public boolean last() throws DataException {
		boolean retval = false;
		if ( this.myData != null ) 
			return this.myData.last();
		if ( retval && this.currLevel == GENUS) this.myID = this.myData.getString(this.idField);
		return retval;
	}

	public void beforeFirst() throws DataException {
		this.myID = null;
		if ( this.myData != null ) 
			this.myData.beforeFirst();
	}

	public void afterLast() throws DataException {
		this.myID = null;
		if ( this.myData != null ) 
			this.myData.afterLast();
	}
	
	public static boolean isValid(SQLData data, int level, String value) throws DataException {
		String sqlString = buildSQLForLevel(level, level);
		try {
			PreparedStatement aSth = data.prepareStatement(sqlString);
			aSth.setString(1, value);
			ResultSet results = aSth.executeQuery();
			boolean retval = results.first();
			results.close();
			aSth.close();
			return retval;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public static TaxonFlat soundsLike(SQLData data, int level, String value) throws DataException {
		SQLTaxonFlat myTaxon = new SQLTaxonFlat(data);
		String columns = SQL_COLUMN[KINGDOM];
		for (int i = PHYLUM; i <= level; i++) {
			columns = String.format("%s,%s", columns, SQL_COLUMN[i]);
		}
		if ( level == GENUS )
			columns = String.format("%s,%s", columns, SQL_COLUMN[SYNONYM]);
		String sqlString = String.format(SQL_LOAD_SOUNDEX, columns, SQL_COLUMN[level], SQL_COLUMN[level]);
		try {
			PreparedStatement aSth = data.prepareStatement(sqlString);
			aSth.setString(1, value);
			myTaxon.myData.loadUsingPreparedStatement(aSth);
			myTaxon.currLevel = level;
			return myTaxon;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

}
