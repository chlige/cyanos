/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLProject;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.sql.SQLTaxonFlat;
import edu.uic.orjala.cyanos.web.forms.ProjectForm;
import edu.uic.orjala.cyanos.web.task.ProjectUpdateTask;

/**
 * @author George Chlipala
 *
 */
public class ProjectServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5740025290717824706L;
	private static final String COL_DIV = "collection";
	private static final String STRAIN_DIV = "strain";
	private static final String MATERIAL_DIV = "material";
	private static final String ASSAY_DIV = "assay";
	
	public static final String PROJECT_OBJECT = "project";
	public static final String INFO_FORM_DIV_ID = "infoForm";
	public static final String SEARCHRESULTS_ATTR = "projects";
	
	public static final String UPDATE_CLASS_STRAIN = "strain";
	public static final String UPDATE_CLASS_MATERIAL = "material";
	public static final String UPDATE_CLASS_COLLECTION = "collection";
	public static final String UPDATE_CLASS_ASSAY = "assay";
	public static final String UPDATE_CLASS_COMPOUND = "compound";
	
	public static final String ACTION_GEN_XML = "updateXML";
	public static final String SESS_ATTR_UPDATE_JOB = "projectUpdateJob";

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doGet(req, res);
		this.handleBasicRequest(req, res);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doPost(req, res);
		this.handleBasicRequest(req, res);
	}

	protected void handleBasicRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		if ( req.getParameter("div") != null ) {
			this.handleDivContent(req, res);
			return;
		}
		
		String projectID = req.getParameter("id");	
		
		try {
			if ( projectID != null ) {
				Project aProject = SQLProject.load(this.getSQLData(req), projectID);

				if ( req.getParameter(ACTION_GEN_XML) != null ) {
					res.setContentType("application/xml");
					ProjectUpdateTask.writeXML(this.getSQLData(req), aProject, this.getAppConfig().getHostUUID(), res.getOutputStream());
					return;
				} else {
					req.setAttribute(PROJECT_OBJECT, aProject);				
				}

			} else if ( req.getParameter("addProject") != null ) {
				Project aProject = SQLProject.create(this.getSQLData(req), req.getParameter("newID"));
				aProject.setName(req.getParameter("label"));
				aProject.setNotes(req.getParameter("notes"));		
				
				String masterURL = req.getParameter("masterURL");
				if ( masterURL != null && masterURL.length() > 0 ) {
					aProject.setMasterURL(masterURL);
					aProject.setUpdateCert(req.getParameter("masterKey"));
					
					String[] params = {"pref_collection", "pref_strain", "pref_material", "pref_assay" };
					String[] classes = {ProjectServlet.UPDATE_CLASS_COLLECTION, ProjectServlet.UPDATE_CLASS_STRAIN, ProjectServlet.UPDATE_CLASS_MATERIAL, ProjectServlet.UPDATE_CLASS_ASSAY}; 
					
					for ( int i = 0; i < params.length; i++ ) {
						String[] values = req.getParameterValues(params[i]);
						if ( values != null && values.length > 0 ) {
							int bits= 0;
							try {
								for ( String bit : values ) {
									bits = bits + Integer.parseInt(bit);
								}
								if ( aProject.getUpdatePrefs(classes[i]) != bits ) {
									aProject.setUpdatePrefs(classes[i], bits); 
								}
							} catch (NumberFormatException e) {
								// Do nothing right now.
							}
						}
					}
				} else { 
					String newHostID = req.getParameter("new_hostid");
					if ( newHostID != null && newHostID.length() > 0 ) {
						aProject.addUpdateHost(newHostID, req.getParameter("new_hostname"), req.getParameter("new_hostkey"));	
					}
				}
				req.setAttribute(PROJECT_OBJECT, aProject);				
			} else {
				req.setAttribute(SEARCHRESULTS_ATTR, SQLProject.projects(this.getSQLData(req), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT));
			}
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} catch (XMLStreamException e) {
			throw new ServletException(e);
		}

		RequestDispatcher disp;
		if ( "/update".equals(req.getPathInfo()) ) {
			disp = getServletContext().getRequestDispatcher("/project-upload.jsp");			
		} else {
			disp = getServletContext().getRequestDispatcher("/project.jsp");
		}
		disp.forward(req, res);
		return;
	}
	
	private void handleDivContent(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		String divTag = req.getParameter("div");
		
		if ( divTag != null ) {

			if ( divTag.equals(ProjectForm.ADD_DIV_ID) ) {
//				ProjectForm projectForm = new ProjectForm(aWrap);
//				out.print(projectForm.addProject());
			} else {
				try {
					Project aProject = SQLProject.load(this.getSQLData(req), req.getParameter("id"));
					if ( aProject.first() ) {
						if ( divTag.equals(COL_DIV) ) {
							req.setAttribute(CollectionServlet.SEARCHRESULTS_ATTR, aProject.collections());
							RequestDispatcher disp = getServletContext().getRequestDispatcher("/collection/collection-list.jsp");
							disp.forward(req, res);												
						} else if ( divTag.equals(STRAIN_DIV) ) {
							req.setAttribute(StrainServlet.SEARCHRESULTS_ATTR, getStrains(req));
							RequestDispatcher disp = getServletContext().getRequestDispatcher("/strain-list.jsp");
							disp.forward(req, res);												
						} else if ( divTag.equals(MATERIAL_DIV) ) {
							req.setAttribute(MaterialServlet.SEARCHRESULTS_ATTR, aProject.materials());
							RequestDispatcher disp = getServletContext().getRequestDispatcher("/material/material-list.jsp");
							disp.forward(req, res);						
						} else if ( divTag.equals(ASSAY_DIV) ) {
							Assay data = aProject.assays();
							req.setAttribute(AssayServlet.SEARCHRESULTS_ATTR, data);
							RequestDispatcher disp = getServletContext().getRequestDispatcher("/assay/assay-list.jsp");
							disp.forward(req, res);
							//					} else if ( divTag.equals(INFO_FORM_DIV_ID) ) {
							//						req.setAttribute(PROJECT_OBJECT, aProject);				
							//						RequestDispatcher disp = getServletContext().getRequestDispatcher("/project-form.jsp");
							//						disp.forward(req, res);
						}
					}
				} catch (DataException e) {
					throw new ServletException(e);
				} catch (SQLException e) {
					throw new ServletException(e);
				} 
			}
			return;
		}

	}

	/*
	public void display(CyanosWrapper aWrap) throws Exception {

		PrintWriter out;
		ProjectForm projectForm = new ProjectForm(aWrap);

		if ( aWrap.getSession().getAttribute("dateFormatter") == null )
			aWrap.getSession().setAttribute("dateFormatter", aWrap.dateFormat());

		if ( aWrap.hasFormValue("div") ) {
			this.handleDivContent(aWrap.getRequest(), aWrap.getResponse());
			return;
		}
		
//		out = aWrap.startHTMLDoc("Project Manager");
		
		
		if ( aWrap.hasFormValue("id")) {

			Project aProject = SQLProject.load(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));

			if ( aWrap.hasFormValue(ACTION_GEN_XML) ) {
				aWrap.setContentType("application/xml");
				ProjectUpdateTask.writeXML(aWrap.getSQLDataSource(), aProject, aWrap.getAppConfig().getHostUUID(), aWrap.getOutputStream());
				return;
			} else {
				aWrap.getRequest().setAttribute(PROJECT_OBJECT, aProject);				
			}
			
			if ("/update".equals(aWrap.getRequest().getPathInfo()) && aWrap.hasFormValue("runJob") && aWrap.hasUpload("xmlFile") ) {
				
				File tempFile = File.createTempFile("cyanos-update-", ".xml");
				FileUpload upload = aWrap.getUpload("xmlFile");
				InputStream in = upload.getStream();
				FileOutputStream fileOut = new FileOutputStream(tempFile);
				
				byte[] buffer = new byte[1024];
				int read = 0;
				
				while ( (read = in.read(buffer)) != -1 ) {
					fileOut.write(buffer, 0, read);
				}

				in.close();
				fileOut.flush();
				fileOut.close();		
				
				RecvUpdateJob job = new RecvUpdateJob(aWrap.getSQLDataSource(true), tempFile);
				aWrap.getSession().setAttribute(SESS_ATTR_UPDATE_JOB, job);
				job.startParse();
				
			}
		} else if ( aWrap.hasFormValue("addProject") ) {
			HttpServletRequest req = aWrap.getRequest();
			Project aProject = SQLProject.create(aWrap.getSQLDataSource(), aWrap.getFormValue("newID"));
			aProject.setName(aWrap.getFormValue("label"));
			aProject.setNotes(aWrap.getFormValue("notes"));		
			
			String masterURL = aWrap.getFormValue("masterURL");
			if ( masterURL != null && masterURL.length() > 0 ) {
				aProject.setMasterURL(masterURL);
				aProject.setUpdateCert(aWrap.getFormValue("masterKey"));
				
				String[] params = {"pref_collection", "pref_strain", "pref_material", "pref_assay" };
				String[] classes = {ProjectServlet.UPDATE_CLASS_COLLECTION, ProjectServlet.UPDATE_CLASS_STRAIN, ProjectServlet.UPDATE_CLASS_MATERIAL, ProjectServlet.UPDATE_CLASS_ASSAY}; 
				
				for ( int i = 0; i < params.length; i++ ) {
					String[] values = req.getParameterValues(params[i]);
					if ( values != null && values.length > 0 ) {
						int bits= 0;
						try {
							for ( String bit : values ) {
								bits = bits + Integer.parseInt(bit);
							}
							if ( aProject.getUpdatePrefs(classes[i]) != bits ) {
								aProject.setUpdatePrefs(classes[i], bits); 
//								out.print("<tr class='updated'><td colspan='2'>Updated update preferences (");
//								out.print(classes[i]);
//								out.println(")</td></tr>");
							}
						} catch (NumberFormatException e) {
//							out.print("<tr class='updated'><td colspan='2'>ERROR updating preferences (");
//							out.print(classes[i]);
//							out.print(") ");
//							out.print(e.getLocalizedMessage());
//							out.println("</td></tr>");
						}
					}
				}

			} else { 
				String newHostID = req.getParameter("new_hostid");
				if ( newHostID != null && newHostID.length() > 0 ) {
					aProject.addUpdateHost(newHostID, req.getParameter("new_hostname"), req.getParameter("new_hostkey"));	
				}
			}
			aWrap.getRequest().setAttribute(PROJECT_OBJECT, aProject);				
		} else {
//			out.println(projectForm.listProjects());
			aWrap.getRequest().setAttribute(SEARCHRESULTS_ATTR, SQLProject.projects(aWrap.getSQLDataSource(), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT));
		}
		
		RequestDispatcher disp;
		if ( "/update".equals(aWrap.getRequest().getPathInfo()) ) {
			disp = getServletContext().getRequestDispatcher("/project-upload.jsp");			
		} else {
			disp = getServletContext().getRequestDispatcher("/project.jsp");
		}
		disp.forward(aWrap.getRequest(), aWrap.getResponse());
		return;
//		aWrap.finishHTMLDoc();
	}
	*/
	
	public Project getProjects(HttpServletRequest request) throws DataException, SQLException {
		return this.getProjects(this.getSQLData(request));
	}
	
	public Project getProjects(SQLData data) throws DataException {
		return SQLProject.projects(data, SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
	}
	
	private Strain getStrains(HttpServletRequest request) throws DataException, SQLException {
		String sortString = request.getParameter(StrainServlet.PARAM_SORT_FIELD);

		if ( sortString == null || sortString.equals(SQLStrain.ID_COLUMN) )
				sortString = "CAST(culture_id AS UNSIGNED)";
		
		String sortDir = request.getParameter(StrainServlet.PARAM_SORT_DIR);
		
		if (sortDir == null ) 
			sortDir = SQLStrain.ASCENDING_SORT;
		
		
		String[] columns = {SQLStrain.PROJECT_COLUMN};
		String[] queries = {request.getParameter("id")};
		
		if ( sortString.equals(SQLTaxonFlat.ORDER_COLUMN) ) 
			return SQLStrain.strainsLikeByTaxa(this.getSQLData(request), columns, queries, sortString, sortDir);
		else 
			return SQLStrain.strainsLike(this.getSQLData(request), columns, queries, sortString, sortDir);
	}

}
