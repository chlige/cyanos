/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author George Chlipala
 *
 */
public class NotebookServlet extends ServletObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static File tempDir;

	@Override
	public void init() throws ServletException {
		super.init();
		tempDir = (File)getServletContext().getAttribute(ServletContext.TEMPDIR);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getPathInfo();
		if ( path != null ) {
			File userDir = getUserDir(req);
			File thisFile = new File(userDir, path);
			if ( thisFile.exists() ) {
				OutputStream out = res.getOutputStream();
				InputStream fileIn = new FileInputStream(thisFile);
				Long thisSize = new Long(thisFile.length());
				res.setContentLength(thisSize.intValue());
				byte[] buffer = new byte[DataFileServlet.BUFFER_SIZE];
				int count;
				while ( (count = fileIn.read(buffer)) > 0  ) {
					out.write(buffer, 0, count);
				}
				fileIn.close();
				out.flush();
				out.close();
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getPathInfo();
		UUID uuid = UUID.randomUUID();		
		if ( path != null ) {
			File userDir = getUserDir(req);
			File outputFile = new File(userDir, path);
			if ( (! outputFile.exists()) && outputFile.createNewFile() ) {
				BufferedInputStream fileData = new BufferedInputStream(req.getInputStream());
				FileOutputStream fileOut = new FileOutputStream(outputFile);
				byte[] buffer = new byte[DataFileServlet.BUFFER_SIZE];
				int count;
				while ( (count = fileData.read(buffer)) > 0  ) {
					fileOut.write(buffer, 0, count);
				}
				fileOut.close();
				fileData.close();
				resp.setStatus(HttpServletResponse.SC_CREATED);
			} else {
				resp.setStatus(HttpServletResponse.SC_OK);
			}
		}
	}

	private static File getUserDir(HttpServletRequest req) {
		String userID = req.getRemoteUser();
		if ( userID != null ) {
			File userDir = new File(tempDir, userID);
			if ( ! userDir.exists() )
				userDir.mkdir();
			return userDir;
		} else {
			return null;
		}
	}
	
}
