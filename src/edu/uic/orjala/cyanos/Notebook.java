/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.util.Date;

/**
 * @author George Chlipala
 *
 */
public interface Notebook extends BasicObject {
	
	/**
	 * Returns the ID of the current Notebook
	 * 
	 * @return Notebook ID
	 */
	String getID();
	
	/**
	 * Returns the User ID of the owner.
	 * 
	 * @return User ID
	 * @throws DataException
	 */
	String getUserID() throws DataException;
	
	/**
	 * Returns a User object of the owner.
	 * 
	 * @return User object
	 * @throws DataException
	 */
	User getUser() throws DataException;
	
	/**
	 * Get the title of the Notebook.
	 * 
	 * @return Title as a String.
	 * @throws DataException
	 */
	String getTitle() throws DataException;
	
	/**
	 * Get a description of the Notebook.
	 * 
	 * @return Description string.
	 * @throws DataException
	 */
	String getDescription() throws DataException;
	
	/**
	 * Set the title of the Notebook.
	 * 
	 * @param title Name/Label for the notebook.
	 * @throws DataException
	 */
	void setTitle(String title) throws DataException;
	
	/**
	 * Set the description of the Notebook
	 * 
	 * @param text Descriptive text of the notebook.
	 * @throws DataException
	 */
	void setDescription(String text) throws DataException;
	
	/**
	 * Add a page for the associated Object.
	 * 
	 * @param page Page number as an int.
	 * @param anObject an object to associate with the notebook page.
	 * @throws DataException
	 */
	void linkObject(int page, NotebookObject anObject) throws DataException;
	
	/**
	 * Get the project ID of the notebook.  Mainly for database authorization purposes.
	 * 
	 * @return Project ID as a String.
	 * @throws DataException
	 */
	String getProjectID() throws DataException;
	
	/**
	 * Set the project ID of the Notebook. Mainly for database authorization purposes.
	 * 
	 * @param projectID new project ID as a String.
	 * @throws DataException
	 */
	void setProjectID(String projectID) throws DataException;
	
	/**
	 * Set the project of the Notebook. Mainly for database authorization purposes.
	 * 
	 * @param aProject new Project
	 * @throws DataException
	 */
	void setProject(Project aProject) throws DataException;
	
	
	/**
	 * @param page Page number
	 * @param title Title of page
	 * @param content Page content
	 * @return NotebookPage object for page created
	 * @throws DataException
	 */
	NotebookPage addPage(int page, String title, String content) throws DataException;
	
	NotebookPage getPages() throws DataException;
	
	NotebookPage getPage(int page) throws DataException;

	int getPageCount() throws DataException;
	
	Date getFirstUpdate() throws DataException;
	
	Date getRecentUpdate() throws DataException;
	
}
