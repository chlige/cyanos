package edu.uic.orjala.cyanos;

@Deprecated
public interface TaxonFlat extends BasicObject {
	
	public final static int SYNONYM = 0;
	public final static int KINGDOM = 1;
	public final static int PHYLUM = 2;
	public final static int CLASS = 3;
	public final static int ORDER = 4;
	public final static int FAMILY = 5;
	public final static int GENUS = 6;
	public final static int NO_LEVEL = -1;

	void create(String newGenus) throws DataException;
	
	boolean loadKingdoms() throws DataException;
	
	boolean loadForKingdom(int aLevel, String aKingdom) throws DataException;
	
	boolean loadForPhylum(int aLevel, String aPhylum) throws DataException;
	
	boolean loadForClass(int aLevel, String aClass) throws DataException;
	
	boolean loadForOrder(int aLevel, String anOrder) throws DataException;
	
	boolean loadForFamily(int aLevel, String aFamily) throws DataException;
	
	boolean loadForTaxonLevel(int selectLevel, int whereLevel, String whereValue) throws DataException;
	
	String getValueForLevel(int level) throws DataException;
	
	String getKingdom() throws DataException;
	
	String getPhylum() throws DataException;
	
	String getTaxonClass() throws DataException;
		
	String getOrder() throws DataException;
	
	String getFamily() throws DataException;
	
	String getGenus() throws DataException;
	
	void setKingdom(String newValue) throws DataException;

	void setPhylum(String newValue) throws DataException;

	void setTaxonClass(String newValue) throws DataException;

	void setOrder(String newValue) throws DataException;
	
	void setFamily(String newValue) throws DataException;
	
	boolean loadAllInKingdom() throws DataException;

	boolean loadAllInPhylum() throws DataException;

	boolean loadAllInClass() throws DataException;

	boolean loadAllInOrder() throws DataException;

	boolean loadAllInFamily() throws DataException;

	boolean loadForKingdom(String aValue) throws DataException;
	
	boolean loadForPhylum(String aValue) throws DataException;
	
	boolean loadForOrder(String aValue) throws DataException;
	
	boolean loadForClass(String aValue) throws DataException;
	
	boolean loadForFamily(String aValue) throws DataException;
	
	boolean reloadForLevel(int level, String aValue) throws DataException;
	
	TaxonFlat getSynonym() throws DataException;

	void setSynonym(String newValue) throws DataException;

	void setSynonym(TaxonFlat newSyn) throws DataException;

	boolean hasSynonym() throws DataException;

	boolean isValid() throws DataException;
	
	int getCurrentLevel();
	
	Strain getStrainsForTaxon() throws DataException;

}
