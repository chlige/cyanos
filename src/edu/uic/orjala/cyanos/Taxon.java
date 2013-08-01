package edu.uic.orjala.cyanos;

public interface Taxon extends BasicObject {
	
	public static final String LEVEL_KINGDOM = "kingdom";
	public static final String LEVEL_PHYLUM = "phylum";
	public static final String LEVEL_CLASS = "class";
	public static final String LEVEL_ORDER = "order";
	public static final String LEVEL_FAMILY = "family";
	public static final String LEVEL_GENUS = "genus";
	public static final String LEVEL_SYNONYM = "synonym";
	
	String getLevel() throws DataException;
	
	String getName() throws DataException;
	
	Taxon getParent() throws DataException;
	
	Taxon getLinage() throws DataException;
	
	Taxon getChildren() throws DataException;
	
	Taxon createChild(String name, String level) throws DataException;
	
	String getName(String level) throws DataException;
	
	Strain getStrains() throws DataException;

}
