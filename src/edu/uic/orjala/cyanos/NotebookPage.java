/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.util.Date;

/**
 * @author George Chlipala
 *
 */
public interface NotebookPage extends BasicObject {
	
	public Notebook getNotebook() throws DataException;
	
	public int getPage() throws DataException;
	
	public String getContent() throws DataException;
	
	public String getTitle() throws DataException;
	
	public Date getCreationDate() throws DataException;
	
	public Date getLastModifiedDate() throws DataException;
	
	public void linkObject(NotebookObject object) throws DataException;
	
	public NotebookObject getLinks() throws DataException;
	
	public Integer[] getVersionNumbers() throws DataException;
	
	ContentVersion[] getVersions() throws DataException;
	
	public ContentVersion getVersion(int version) throws DataException;
	
	public void updateContent(String content) throws DataException;
}
