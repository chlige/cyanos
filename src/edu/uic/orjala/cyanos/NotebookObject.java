/**
 * 
 */
package edu.uic.orjala.cyanos;

/**
 * @author George Chlipala
 *
 */
public interface NotebookObject {

	Notebook getNotebook() throws DataException;
	
	void setNotebook(Notebook aNotebook) throws DataException;
	
	void setNotebook(Notebook aNotebook, int aPage) throws DataException;

	String getNotebookID() throws DataException;
	
	void setNotebookID(String anID) throws DataException;
	
	void setNotebookID(String anID, int aPage) throws DataException;

	int getNotebookPage() throws DataException;
	
	void setNotebookPage(int aPage) throws DataException;
	
}
