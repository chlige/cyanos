package edu.uic.orjala.cyanos;

import java.util.Date;

public interface Isolation extends BasicObject, NotebookObject, RemoteObject {

	/**
	 * Returns the ID of the current Isolation record
	 * 
	 * @return Isolation ID
	 */
	String getID();
	
	String getProjectID() throws DataException;

	Project getProject() throws DataException;
	
	String getNotes() throws DataException;
	
	Date getDate() throws DataException;
	
	String getDateString() throws DataException;
	
	String getMedia() throws DataException;
	
	String getType() throws DataException;
	
	String getParentID() throws DataException;
	
	Isolation getParent() throws DataException;
	
	Isolation getChildren() throws DataException;
	
	String getCollectionID() throws DataException;
	
	Collection getCollection() throws DataException;

	void setMedia(String newValue) throws DataException;
	
	void setType(String newValue) throws DataException;
	
	void setParentID(String newValue) throws DataException;
	
	void setParent(Isolation anIsolation) throws DataException;
	
	void setCollectionID(String newValue) throws DataException;
	
	void setCollection(Collection aCollection) throws DataException;
	
	void setDate(Date newValue) throws DataException;
	
	void setDate(String newValue) throws DataException;
	
	void setNotes(String newNotes) throws DataException;
	
	void addNotes(String newNotes) throws DataException;
	
	Strain getStrains() throws DataException;

	void setProjectID(String newValue) throws DataException;
	
	void setProject(Project aProject) throws DataException;
	
	Isolation possibleParents() throws DataException;

}
