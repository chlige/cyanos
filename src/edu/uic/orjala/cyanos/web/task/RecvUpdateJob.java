package edu.uic.orjala.cyanos.web.task;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Savepoint;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.xml.ProjectUpdateXMLHandler;

/**
 * @author George Chlipala
 *
 */
public class RecvUpdateJob implements Runnable {

	private static final String THREAD_LABEL = "UPDATE_JOB";
	private Thread updateThread = null;
	private SQLData myData;
	private ProjectUpdateXMLHandler xmlHandler;
	private Exception excep = null;
	private File xmlFile;
	private boolean running = false;
	private Savepoint savepoint = null;
	private SAXParser saxParser;

	/**
	 * 
	 */
	public RecvUpdateJob(SQLData data, File file) {
		myData = data;
		xmlFile = file;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			this.running = true;
			SAXParserFactory factory = SAXParserFactory.newInstance();
			saxParser = factory.newSAXParser();
			xmlHandler = new ProjectUpdateXMLHandler(myData);
			saxParser.parse(xmlFile, xmlHandler);	
			xmlFile.delete();
		} catch (ParserConfigurationException e) {
			this.excep = e;
		} catch (SAXException e) {
			this.excep = e;
		} catch (IOException e) {
			this.excep = e;
		} catch (DataException e) {
			this.excep = e;
		} finally { 
			this.running = false;		
			try {
				if ( this.excep != null )
					myData.rollback(savepoint);
				else
					myData.commit();
				this.savepoint = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean hasException() {
		return (this.excep != null);
	}

	public Exception getException() {
		return this.excep;
	}

	public ProjectUpdateXMLHandler getXMLHandler() {
		return this.xmlHandler;
	}

	public boolean isRunning() {
		return this.running;
	}

	public void startParse() {
		if ( this.updateThread == null ) {
			try {
				this.savepoint = this.myData.setSavepoint();
				this.updateThread = new Thread(this, THREAD_LABEL);
				this.updateThread.start();
			} catch (SQLException e) {
				this.excep = e;
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#stopParse()
	 */
	public void stopParse() {
		if ( this.updateThread != null ) {
			this.updateThread = null;
			this.running = false;
		}
	}

}
