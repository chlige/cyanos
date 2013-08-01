/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.Taxon;
import edu.uic.orjala.cyanos.User;


/**
 * Class to retrieve taxonomic data.
 * 
 * <P>This data is stored in the SQL table <CODE>taxon</CODE>.</P>
 * 
 * <TABLE BORDER=1>
 * <TR><TD><B>SQL Column</B></TD><TD><B>SQL Data Type</B></TD></TR>
 * <TR><TD>name</TD><TD>VARCHAR(64)</TD></TR>
 * <TR><TD>level</TD><TD>VARCHAR(64)</TD></TR>
 * </TABLE>
 * 
 * @author George Chlipala
 * @version 1.0
 *
 */
public class SQLTaxon extends SQLObject implements Taxon {

	public static final String NAME_COLUMN = "name";
	public static final String LEVEL_COLUMN = "level";
	
	public static final String PARENT_COLUMN = "parent";
	public static final String CHILD_COLUMN = "child";
	public static final String DEPTH_COLUMN = "depth";
	
	public static final String SQL_INSERT_TAXON = "INSERT INTO taxon(name,level) VALUES(?,?)";
	public static final String SQL_UPDATE_NODES = "REPLACE INTO taxon_paths(parent,child,depth) SELECT parent,?,depth+1 FROM taxon_paths WHERE child=? UNION ALL SELECT ?,?,0";	
	
	private static final String SQL_SELECT_STRAIN = "SELECT DISTINCT species.* FROM species JOIN taxon t ON(t.name = species.genus) JOIN taxon_paths tp ON (tp.child = t.name) WHERE tp.parent=?";

	private static final String SQL_LOAD = "SELECT taxon.* FROM taxon WHERE name = ?";

	private static final String SQL_LOAD_ALL_FOR = "SELECT taxon.* FROM taxon WHERE level = ? ORDER BY name ASC";
	private static final String SQL_LOAD_LIKE = "SELECT taxon.* FROM taxon WHERE name LIKE ? ORDER BY name ASC";
	private static final String SQL_LOAD_LIKE_LEVEL = "SELECT taxon.* FROM taxon WHERE name LIKE ? AND level = ? ORDER BY name ASC";
	public static final String SQL_LOAD_ALL = "SELECT taxon.name,taxon.level,par.parent FROM taxon JOIN taxon_paths tp ON (tp.parent = taxon.name) " +
			"LEFT OUTER JOIN taxon_paths par ON (par.child = taxon.name AND par.depth = 1) GROUP BY taxon.name ORDER BY MAX(tp.depth) DESC, taxon.name ASC";
	public static final String SQL_LOAD_ALL_LEVEL = "SELECT taxon.name,taxon.level,par.parent FROM taxon JOIN taxon_paths tp ON (tp.parent = taxon.name) " +
			"JOIN taxon_paths tpsel ON (tpsel.child = taxon.name) " +
			"LEFT OUTER JOIN taxon_paths par ON (par.child = taxon.name AND par.depth = 1) WHERE tpsel.parent = ? GROUP BY taxon.name ORDER BY MAX(tp.depth) DESC, taxon.name ASC";	
	
	private static final String SQL_LOAD_SOUNDEX = "SELECT taxon.* FROM taxon WHERE name SOUNDS LIKE ?";
	private static final String SQL_LOAD_SOUNDEX_LEVEL = "SELECT taxon.* FROM taxon WHERE name SOUNDS LIKE ? AND level = ?";
	
	private static final String SQL_LOAD_CHILDREN_LEVEL = "SELECT DISTINCT taxon.* FROM taxon JOIN taxon_paths ON taxon.name = taxon_paths.child WHERE taxon_paths.parent = ? AND taxon_paths.depth > 0 AND taxon.level = ? ORDER BY taxon.name ASC";
	private static final String SQL_LOAD_CHILDREN_NAMES = "SELECT DISTINCT taxon.name FROM taxon JOIN taxon_paths ON taxon.name = taxon_paths.child WHERE taxon_paths.parent = ? AND taxon_paths.depth > 0 AND taxon.level = ? ORDER BY taxon.name ASC";
	private static final String SQL_LOAD_CHILDREN = "SELECT DISTINCT taxon.* FROM taxon JOIN taxon_paths ON taxon.name = taxon_paths.child WHERE taxon_paths.parent = ? AND taxon_paths.depth = 1 ORDER BY taxon.name ASC";
	
	private static final String SQL_LOAD_PARENT = "SELECT taxon.* FROM taxon JOIN taxon_paths ON taxon.name = taxon_paths.parent WHERE taxon_paths.child = ? AND taxon_paths.depth = 1";

	private static final String SQL_PARENT_NAME = "SELECT taxon.name FROM taxon JOIN taxon_paths ON taxon.name = taxon_paths.parent WHERE taxon_paths.child = ? AND taxon.level = ?";

	private static final String SQL_LOAD_LINEAGE = "SELECT taxon.* FROM taxon JOIN taxon_paths ON taxon.name = taxon_paths.parent WHERE taxon_paths.child = ? ORDER BY taxon_paths.depth DESC";

	public static String nameForLevel(SQLData data, String name, String level) throws DataException {
		try {
			PreparedStatement psth = data.prepareStatement(SQL_PARENT_NAME);
			psth.setString(1, name);
			psth.setString(2, level);
			ResultSet results = psth.executeQuery();
			if ( results.first() ) {
				String thisName = results.getString(1);
				results.close();
				psth.close();
				return thisName;
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public static List<String> namesForLevel(SQLData data, String parent, String level) throws SQLException, DataException {
		List<String> names = new ArrayList<String>();
		
		PreparedStatement psth = data.prepareStatement(SQL_LOAD_CHILDREN_NAMES);
		psth.setString(1, parent);
		psth.setString(2, level);
		ResultSet results = psth.executeQuery();
		results.beforeFirst();
		
		while ( results.next() ) {
			names.add(results.getString(1));
		}
		results.close();
		psth.close();
		return names;
	}
	
	private static final String SQL_LOAD_LEAF = "SELECT taxon.* FROM taxon WHERE taxon.name NOT IN (SELECT DISTINCT parent FROM taxon_paths WHERE depth > 0)";
	
	/**
	 * Returns a Taxon object with all leaf taxa (taxonomic names that are not a parent of any other names)
	 * 
	 * @param data
	 * @return
	 * @throws DataException
	 */
	public static SQLTaxon leafTaxa(SQLData data) throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(data);
		myTaxon.loadSQL(SQL_LOAD_LEAF);
		return myTaxon;
	}
	
	private static final String SQL_LOAD_ROOT = "SELECT taxon.* FROM taxon WHERE taxon.name NOT IN (SELECT DISTINCT child FROM taxon_paths WHERE depth > 0)";
	
	/**
	 * Returns a Taxon object with all root taxa (taxonomic names that do not have a parent)
	 * 
	 * @param data
	 * @return
	 * @throws DataException
	 */
	public static SQLTaxon rootTaxa(SQLData data) throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(data);
		myTaxon.loadSQL(SQL_LOAD_ROOT);
		return myTaxon;
	}
	
	public static SQLTaxon taxaForLevel(SQLData data, String level) throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(data);
		try {
			PreparedStatement psth = data.prepareStatement(SQL_LOAD_ALL_FOR);
			psth.setString(1, level);
			myTaxon.loadUsingPreparedStatement(psth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return myTaxon;
	}

	public static SQLTaxon load(SQLData data, String name) throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(data);
		try {
			PreparedStatement psth = data.prepareStatement(SQL_LOAD);
			psth.setString(1, name);
			myTaxon.loadUsingPreparedStatement(psth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return myTaxon;
	}
	
	public static SQLTaxon createChild(SQLData data, String parent, String name, String level) throws DataException {
		try {
			PreparedStatement psth = data.prepareStatement(SQL_INSERT_TAXON);
			psth.setString(1, name);
			psth.setString(2, level);
			if ( psth.executeUpdate() > 0 ) {
				psth.close();
				psth = data.prepareStatement(SQL_UPDATE_NODES);
				psth.setString(1, name);
				psth.setString(2, parent);
				psth.setString(3, name);
				psth.setString(4, name);
				if ( psth.executeUpdate() > 0 ) {
					psth.close();
					return SQLTaxon.load(data, name);
				}
			}	
			psth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public static SQLTaxon createTaxon(SQLData data, String name, String level) throws DataException {
		return SQLTaxon.createChild(data, null, name, level);
	}
	
	public static Taxon taxaForLevel(SQLData data, String parent, String level) throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(data);
		try {
			PreparedStatement psth = data.prepareStatement(SQL_LOAD_CHILDREN_LEVEL);
			psth.setString(1, parent);
			psth.setString(2, level);
			myTaxon.loadUsingPreparedStatement(psth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return myTaxon;
	}
	
	public static Taxon taxaLike(SQLData data, String like) throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(data);		
		try {
			PreparedStatement aSth = myTaxon.myData.prepareStatement(SQL_LOAD_LIKE);
			aSth.setString(1, like);
			myTaxon.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return myTaxon;
	}
	
	public static Taxon taxaLike(SQLData data, String like, String level) throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(data);		
		try {
			PreparedStatement aSth = myTaxon.myData.prepareStatement(SQL_LOAD_LIKE_LEVEL);
			aSth.setString(1, like);
			aSth.setString(2, level);
			myTaxon.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return myTaxon;
	}
	
	public static Taxon soundsLike(SQLData data, String value) throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(data);
		try {
			PreparedStatement psth = data.prepareStatement(SQL_LOAD_SOUNDEX);
			psth.setString(1, value);
			myTaxon.loadUsingPreparedStatement(psth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return myTaxon;
	}

	public static Taxon soundsLike(SQLData data, String level, String value) throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(data);
		try {
			PreparedStatement psth = data.prepareStatement(SQL_LOAD_SOUNDEX_LEVEL);
			psth.setString(1, value);
			psth.setString(2, level);
			myTaxon.loadUsingPreparedStatement(psth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return myTaxon;
	}


	protected SQLTaxon(SQLData data) {
		super(data);
		this.initVals();
	}
	
	protected void initVals() {
		this.idField = NAME_COLUMN;
		this.myData.setAccessRole(User.CULTURE_ROLE);
	}
	
	public Strain getStrains() throws DataException {
		return SQLTaxon.getStrains(myData, this.getName());
	}

	public static Strain getStrains(SQLData data, String name) throws DataException {
		return SQLTaxon.getStrains(data, name, SQLStrain.SORT_ID, SQLStrain.ASCENDING_SORT);
	}

	public static Strain getStrains(SQLData data, String name, String sortColumn, String sortDir) throws DataException {
		SQLStrain aStrain = new SQLStrain(data);
		try {
			PreparedStatement aPsth = aStrain.myData.prepareStatement(SQL_SELECT_STRAIN + " ORDER BY " + sortColumn + " " + sortDir);
			aPsth.setString(1, name);
			aStrain.loadUsingPreparedStatement(aPsth);
			if ( aStrain.first() ) 
				return aStrain;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aStrain;
	}

	@Override
	public String getLevel() throws DataException {
		return this.myData.getString(LEVEL_COLUMN);
	}

	@Override
	public String getName() throws DataException {
		return this.myData.getString(NAME_COLUMN);
	}

	@Override
	public Taxon getParent() throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(this.myData);
		try {
			PreparedStatement psth = myTaxon.myData.prepareStatement(SQL_LOAD_PARENT);
			psth.setString(1, this.getName());
			myTaxon.loadUsingPreparedStatement(psth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return myTaxon;		
	}

	@Override
	public Taxon getChildren() throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(myData);
		try {
			PreparedStatement psth = myData.prepareStatement(SQL_LOAD_CHILDREN);
			psth.setString(1, this.getName());
			myTaxon.loadUsingPreparedStatement(psth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return myTaxon;
	}

	@Override
	public Taxon createChild(String name, String level) throws DataException {
		return SQLTaxon.createChild(myData, this.getName(), name, level);
	}

	@Override
	protected void fetchRecord() throws DataException {

	}

	@Override
	public Taxon getLinage() throws DataException {
		return SQLTaxon.getLinage(myData, this.getName());
	}

	public static Taxon getLinage(SQLData data, String name) throws DataException {
		SQLTaxon myTaxon = new SQLTaxon(data);
		try {
			PreparedStatement psth = myTaxon.myData.prepareStatement(SQL_LOAD_LINEAGE);
			psth.setString(1, name);
			myTaxon.loadUsingPreparedStatement(psth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return myTaxon;		
	}

	@Override
	public String getName(String level) throws DataException {
		return SQLTaxon.nameForLevel(myData, this.getName(), level);
	}
}
