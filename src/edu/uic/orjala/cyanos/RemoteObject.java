/**
 * 
 */
package edu.uic.orjala.cyanos;

/**
 * @author George Chlipala
 *
 */
public interface RemoteObject {

	String getRemoteID() throws DataException;
	
	String getRemoteHostID() throws DataException;
	
}
