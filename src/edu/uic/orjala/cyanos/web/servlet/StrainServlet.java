//
//  StrainServlet.java
//  Cyanos
//
//  Created by George Chlipala on 5/7/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uic.orjala.cyanos.AssayData;
import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.Taxon;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLAssayData;
import edu.uic.orjala.cyanos.sql.SQLCollection;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLHarvest;
import edu.uic.orjala.cyanos.sql.SQLInoc;
import edu.uic.orjala.cyanos.sql.SQLIsolation;
import edu.uic.orjala.cyanos.sql.SQLMaterial;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.sql.SQLTaxon;
import edu.uic.orjala.cyanos.web.AppConfig;

/**
 * @author George Chlipala
 * @version 2.61124
 *
 */
public class StrainServlet extends ServletObject {

	private static final long serialVersionUID = -4078009429206886640L;

	public static final String INFO_FORM_DIV_ID = "infoForm";

	public static final String INOC_DIV_TITLE = "Inoculations";
	public static final String INOC_DIV_ID = "strainInocs";

	public static final String CRYO_DIV_TITLE = "Cryopreservations";
	public static final String CRYO_DIV_ID = "cryoInfo";

	public static final String HARVEST_DIV_TITLE = "Harvests";
	public static final String HARVEST_DIV_ID = "harvestInfo";

	public static final String COL_DIV_TITLE = "Field Collections";
	public static final String COL_DIV_ID = "fieldCols";

	public static final String PHOTO_DIV_ID = "photos";
	public static final String PHOTO_DIV_TITLE = "Photos";

	public static final String COMPOUND_DIV_ID = "compounds";
	public static final String COMPOUND_DIV_TITLE = "Compounds";

	public static final String SEPARATION_DIV_TITLE = "Separations";
	public static final String SEPARATION_DIV_ID = "sepInfo";

	public static final String ASSAY_DIV_TITLE = "Assays";
	public static final String ASSAY_DIV_ID = "assayInfo";


	public static final String EXTRACT_LIST_DIV_ID = "extracts";

	public static final String HELP_MODULE = "strain";

	public static final String STRAIN_OBJECT = "strain";

	public static final String SEARCHRESULTS_ATTR = "strains";
	public static final String FIELD_QUERY = "query";

	public static final String SEARCH_DIV_ID = "strains";

	public static final String PARAM_SORT_FIELD = "sortField";
	public static final String PARAM_SORT_DIR = "sortDir";
	public static final String PARAM_LIVESEARCH = "livesearch";

	public static final String PARAM_GENUS = "genus";
	public static final String LS_PARAM_COLLECTION = "collection";
	public static final String LS_PARAM_ISOLATION = "isolation";
	
	public static final String LIVESEARCH_QUERY = "query";

	public static final String DIV_VALID_GENUS = "validgenus";
	
	public static final String PHOTO_FORM = "photoForm";

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doGet(req, res);
		this.handleRequest(req, res);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doPost(req, res);
		this.handleRequest(req, res);
	}



	/*
	public void display(CyanosWrapper aWrap) throws Exception {
		PrintWriter out;
		StrainForm strainForm = new StrainForm(aWrap);

		if ( aWrap.getSession().getAttribute("dateFormatter") == null )
			aWrap.getSession().setAttribute("dateFormatter", aWrap.dateFormat());



		if ( aWrap.hasFormValue("div") ) {
			aWrap.setContentType("text/html; charset=UTF-8");
			String divTag = aWrap.getFormValue("div");
			out = aWrap.getWriter();
			if ( aWrap.hasFormValue("id") ) {
				Strain thisStrain = SQLStrain.load(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
				if ( thisStrain.first() ) {
					if ( aWrap.hasFormValue(PARAM_LIVESEARCH) ) {
						String searchTag = aWrap.getFormValue(PARAM_LIVESEARCH);
						if ( searchTag.equals(StrainForm.LIVESEARCH_GENUS) ) {
							out.println(strainForm.livesearchGenus());
						} else if ( searchTag.equals(LIVESEARCH_QUERY) ) {
							this.livesearchQuery(aWrap.getRequest(), aWrap.getResponse());
						}
					} else if ( divTag.equals(ASSAY_DIV_ID) ) {
						AssayData data;
						if ( aWrap.hasFormValue("target") && aWrap.getFormValue("target").length() > 0 )
							data = SQLAssayData.dataForStrainID(aWrap.getSQLDataSource(), thisStrain.getID(), aWrap.getFormValue("target"));
						else
							data = SQLAssayData.dataForStrain(aWrap.getSQLDataSource(), thisStrain);
						aWrap.getRequest().setAttribute(AssayServlet.SEARCHRESULTS_ATTR, data);
						aWrap.getRequest().setAttribute(AssayServlet.TARGET_LIST, SQLAssay.targets(aWrap.getSQLDataSource()));
						RequestDispatcher disp = getServletContext().getRequestDispatcher("/assay/assay-data-list.jsp");
						disp.forward(aWrap.getRequest(), aWrap.getResponse());
						//					AssayForm aForm = new AssayForm(aWrap);
						//					out.println(aForm.assayListForStrain(thisStrain));
					} else if ( divTag.equals(HARVEST_DIV_ID) ) {
						HarvestForm aForm = new HarvestForm(aWrap);
						out.println(aForm.harvestList(thisStrain));
					} else if ( divTag.equals(SEPARATION_DIV_ID) ) {
						Separation seps = SQLSeparation.findForStrain(aWrap.getSQLDataSource(), thisStrain.getID());
						aWrap.getRequest().setAttribute(SeparationServlet.SEARCHRESULTS_ATTR, seps);
						RequestDispatcher disp = getServletContext().getRequestDispatcher("/separation/separation-list.jsp");
						disp.forward(aWrap.getRequest(), aWrap.getResponse());
						//					SeparationForm sepForm = new SeparationForm(aWrap);
						//					out.println(sepForm.separationList(thisStrain));
					} else if ( divTag.equals(EXTRACT_LIST_DIV_ID) ) {			
						aWrap.getRequest().setAttribute(MaterialServlet.SEARCHRESULTS_ATTR, SQLMaterial.extractsForStrain(aWrap.getSQLDataSource(), thisStrain.getID()));
						RequestDispatcher disp = getServletContext().getRequestDispatcher("/material/extract-list.jsp");
						disp.forward(aWrap.getRequest(), aWrap.getResponse());				
						//				} else if ( divTag.equals(SampleForm.DIV_ID) ) {
						//					SampleForm aForm = new SampleForm(aWrap);
						//					out.println(aForm.sampleListContent(thisStrain.getSamples(), true));
					} else if ( divTag.equals(INOC_DIV_ID) ) {
						out.println(strainForm.inoculationList(thisStrain));
					} else if ( divTag.equals(COL_DIV_ID) ) {
						out.println(strainForm.collectionList(thisStrain));
					} else if ( divTag.equals(CRYO_DIV_ID) ) {
						CryoForm aForm = new CryoForm(aWrap);
						out.println(aForm.cryoList(thisStrain));
					} else if ( divTag.equals(PHOTO_DIV_ID) ) {

						out.println(strainForm.photoAlbum(thisStrain));
					} else if ( divTag.equals(StrainForm.PHOTO_FORM)) {
						out.println(strainForm.photoForm(thisStrain, 3));
					} else if ( divTag.equals(COMPOUND_DIV_ID) ) {
						aWrap.getRequest().setAttribute(CompoundServlet.COMPOUND_RESULTS, SQLCompound.compoundsForStrain(aWrap.getSQLDataSource(), thisStrain));
						RequestDispatcher disp = getServletContext().getRequestDispatcher("/compund/compound-list.jsp");
						disp.forward(aWrap.getRequest(), aWrap.getResponse());									
						//					CompoundForm aForm = new CompoundForm(aWrap);
						//					out.println(aForm.listCompounds(SQLCompound.compoundsForStrain(aWrap.getSQLDataSource(), thisStrain), true));
					} else if ( divTag.equals(StrainForm.DIV_VALID_GENUS) ) {
						this.validateGenus(aWrap.getRequest(), aWrap.getResponse());
					}
				}
			} else if ( divTag.equals(SEARCH_DIV_ID) ) {
				if ( aWrap.hasFormValue(FIELD_QUERY) ) {
					aWrap.getRequest().setAttribute(SEARCHRESULTS_ATTR, getStrains(aWrap.getRequest()));
				}
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/strain-list.jsp");
				disp.forward(aWrap.getRequest(), aWrap.getResponse());
			}
			//			this.closeSQL();
//			out.close();
			out.flush();
			return;
		}

		/*	if ( aWrap.hasFormValue("id") ) {
			StrainForm aForm = new StrainForm(aWrap);
			out = aWrap.startHTMLDoc(String.format("Strain: %s", aWrap.getFormValue("id")));
			Strain thisStrain = SQLStrain.load(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
			if ( aWrap.hasFormValue("removeAction") ) {
				if ( thisStrain.isAllowed(Role.DELETE) ) {
					Paragraph head = new Paragraph();
					head.setAlign("CENTER");
					StyledText title = new StyledText("Remove Strain");
					title.setSize("+3");
					head.addItem(title);
					head.addItem("<HR WIDTH=\"85%\"/>");
					StyledText subtitle = new StyledText("<CENTER>");
					subtitle.setSize("+2");
					subtitle.addItalicString("Strain ID: " + thisStrain.getID());
					subtitle.addString("</CENTER>");
					head.addItem(subtitle);
					out.println(head);	
					out.println(aForm.killStrain());
				} else {
					out.println("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ACTION NOT ALLOWED:</FONT> Insufficient permission</B></P>");
				}
			} else {

				/*					aForm.updateStrain(thisStrain);

					Paragraph head = new Paragraph();
					head.setAlign("CENTER");
					StyledText title = new StyledText(String.format("%s <I>%s</I>", thisStrain.getID(), thisStrain.getName()));
					title.setSize("+3");
					head.addItem(title);
					head.addItem("<HR WIDTH=\"85%\"/>");
					out.println(head);


					if ( thisStrain.isAllowed(Role.WRITE) ) {
						boolean readOnly = ( thisStrain.wasRemoved() || thisStrain.statusIs(Strain.REMOVED_STATUS) );
						if ( readOnly ) {
							out.println(aForm.showSpeciesText(thisStrain));					
						} else {
							Div strainDiv = aForm.strainViewDiv(thisStrain);
							strainDiv.setClass("main");
							out.println(strainDiv.toString());
							out.println(String.format("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' NAME='addQueue' onClick='queueForm(\"strain\", \"%s\");'>Add to a Work Queue</BUTTON></P>", thisStrain.getID()));
						}

						out.println(aForm.loadableDiv(PHOTO_DIV_ID, PHOTO_DIV_TITLE));
						try {
							String status = thisStrain.getStatus();
							if ( status != null && status.equals(Strain.FIELD_HARVEST_STATUS) ) {
								out.println(aForm.loadableDiv(COL_DIV_ID, COL_DIV_TITLE).toString());
							} else {
								Collection list = thisStrain.getFieldCollections();
								out.println(aForm.loadableDiv(INOC_DIV_ID, INOC_DIV_TITLE).toString());	
								if ( list != null && list.first() ) 
									out.println(aForm.loadableDiv(COL_DIV_ID, COL_DIV_TITLE).toString());									
								out.println(aForm.loadableDiv(CryoForm.DIV_ID, CryoForm.DIV_TITLE));
							}
						} catch (DataException e) {
							out.println(aWrap.handleException(e));
						}

						out.println(aForm.loadableDiv(HarvestForm.DIV_ID, HarvestForm.DIV_TITLE));
//						out.println(aForm.loadableDiv(SampleForm.DIV_ID, SampleForm.DIV_TITLE));
						out.println(aForm.loadableDiv(EXTRACT_LIST_DIV_ID, "Extracts"));
						out.println(aForm.loadableDiv(SeparationForm.DIV_ID, SeparationForm.DIV_TITLE));
						out.println(aForm.loadableDiv(AssayForm.DIV_ID, AssayForm.DIV_TITLE));
						out.println(aForm.loadableDiv(COMPOUND_DIV_ID, COMPOUND_DIV_TITLE));

					} else if ( ! aWrap.hasFormValue("div") ) {
						out.println(aForm.showSpeciesText(thisStrain));
						out.println(aForm.loadableDiv(PHOTO_DIV_ID, PHOTO_DIV_TITLE));
					}
				} else {
					out.println("<P ALIGN='CENTER'><B><FONT SIZE='+1'><FONT COLOR='red'>ERROR:</FONT> Requested strain does not exist in the database.</FONT></B></P>");
					Paragraph head = new Paragraph("<HR WIDTH='85%'/>");
					head.setAlign("CENTER");
					out.println(head);
					out.println(aForm.listSpecies());
				}
				//		thisStrain.close();
			}
				if ( "add".equals(aWrap.getFormValue("action")) ) {
					out = aWrap.startHTMLDoc("Add Strain");
					if ( aWrap.getUser().isAllowed(User.CULTURE_ROLE, User.NULL_PROJECT, Role.CREATE) ) {
						Paragraph head = new Paragraph();
						head.setAlign("CENTER");
						StyledText title = new StyledText("Add Strain");
						title.setSize("+3");
						head.addItem(title);
						head.addItem("<HR WIDTH='85%'/>");
						out.println(head);
						aForm = new StrainForm(aWrap);
						out.println(aForm.addStrain());			
					} else {
						aWrap.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
					}
				}
			}
		} else {

		if ( aWrap.hasFormValue("id") ) {
			aWrap.getRequest().setAttribute(STRAIN_OBJECT, SQLStrain.load(aWrap.getSQLDataSource(), aWrap.getFormValue("id")));
		} else if ( aWrap.hasFormValue(FIELD_QUERY) ) {
			aWrap.getRequest().setAttribute(SEARCHRESULTS_ATTR, getStrains(aWrap.getRequest()));
		}

		RequestDispatcher disp = getServletContext().getRequestDispatcher("/strain.jsp");
		disp.forward(aWrap.getRequest(), aWrap.getResponse());
		return;

			out = aWrap.startHTMLDoc("Strain List");
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Strain List");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head);
			StrainForm myForm = new StrainForm(aWrap);
			out.println(myForm.queryForm());
		//			aWrap.finishHTMLDoc();
	}
	 */

	public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {

		if ( req.getParameter("div") != null ) {
			try {
				this.handleAJAX(req, res);
				return;
			} catch (DataException e) {
				throw new ServletException(e);
			} catch (SQLException e) {
				throw new ServletException(e);
			}
		}

		try {

			if ( "add".equals(req.getParameter("action")) ) {
				if ( this.getUser(req).isAllowed(User.CULTURE_ROLE, User.NULL_PROJECT, Role.CREATE) ) {
					if ( req.getParameter("updateStrain") != null ) {
						try {
							Strain strainObj = SQLStrain.create(this.getSQLData(req), req.getParameter("newID"));
							strainObj.setName(req.getParameter("sci_name"));
							strainObj.setDate(req.getParameter("addDate"));
							req.setAttribute(STRAIN_OBJECT, strainObj);	
							this.forwardRequest(req, res, "/strain.jsp");
						} catch (DataException e) {
							req.setAttribute("error_msg", e.getLocalizedMessage());
							this.forwardRequest(req, res, "/strain/strain-add.jsp");							
						}
					} else 
						this.forwardRequest(req, res, "/strain/strain-add.jsp");
				} else {
					res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
				}
				return;
			}

			if ( req.getParameter("id") != null ) {
				req.setAttribute(STRAIN_OBJECT, SQLStrain.load(this.getSQLData(req), req.getParameter("id")));	
			} else if ( req.getParameter(FIELD_QUERY) != null ) {
				req.setAttribute(SEARCHRESULTS_ATTR, getStrains(req));
			} else if ( req.getParameter("photoList") != null ) {
				req.setAttribute(STRAIN_OBJECT, SQLStrain.loadWithPhotos(this.getSQLData(req)));
				this.forwardRequest(req, res, "/strain-photos.jsp");
				return;
			}

			this.forwardRequest(req, res, "/strain.jsp");
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}

	}
	
	private void handleAJAX(HttpServletRequest req, HttpServletResponse res) throws DataException, SQLException, IOException, ServletException {
		res.setContentType("text/html; charset=UTF-8");
		String divTag = req.getParameter("div");
		PrintWriter out = res.getWriter();
		if ( req.getParameter(PARAM_LIVESEARCH) != null ) {
			String searchTag = req.getParameter(PARAM_LIVESEARCH);
			if ( searchTag.equals(PARAM_GENUS) ) {
				this.livesearchGenus(req, res);
			} else if ( searchTag.equals(LIVESEARCH_QUERY) ) {
				this.livesearchQuery(req, res);
			} else if ( searchTag.equals(LS_PARAM_COLLECTION) ) {
				this.livesearchCollection(req, res);
			} else if ( searchTag.equals(LS_PARAM_ISOLATION) ) {
				this.livesearchIsolation(req, res);
			}
		} else if ( req.getParameter("id") != null ) {
			Strain thisStrain = SQLStrain.load(this.getSQLData(req), req.getParameter("id"));
			if ( thisStrain.first() ) {
				if ( divTag.equals(INFO_FORM_DIV_ID) ) {
					req.setAttribute(STRAIN_OBJECT, thisStrain);
					this.forwardRequest(req, res, "/strain/strain-form.jsp");
				} else if ( divTag.equals(ASSAY_DIV_ID) ) {
					AssayData data;
					if ( req.getParameter("target") != null && req.getParameter("target").length() > 0 )
						data = SQLAssayData.dataForStrainID(this.getSQLData(req), thisStrain.getID(), req.getParameter("target"));
					else
						data = SQLAssayData.dataForStrain(this.getSQLData(req), thisStrain);
					req.setAttribute(AssayServlet.SEARCHRESULTS_ATTR, data);
					req.setAttribute(AssayServlet.TARGET_LIST, SQLAssay.targets(this.getSQLData(req)));
					this.forwardRequest(req, res, "/assay/assay-data-list.jsp");
				} else if ( divTag.equals(HARVEST_DIV_ID) ) {
					req.setAttribute(HarvestServlet.SEARCHRESULTS_ATTR, SQLHarvest.harvestsForStrain(this.getSQLData(req), thisStrain));					
					this.forwardRequest(req, res, "/harvest/harvest-list.jsp");
				} else if ( divTag.equals(SEPARATION_DIV_ID) ) {
					req.setAttribute(SeparationServlet.SEARCHRESULTS_ATTR, SQLSeparation.findForStrain(this.getSQLData(req), thisStrain.getID()));
					this.forwardRequest(req, res, "/separation/separation-list.jsp");
				} else if ( divTag.equals(EXTRACT_LIST_DIV_ID) ) {			
					req.setAttribute(MaterialServlet.SEARCHRESULTS_ATTR, SQLMaterial.extractsForStrain(this.getSQLData(req), thisStrain.getID()));
					this.forwardRequest(req, res, "/material/extract-list.jsp");
				} else if ( divTag.equals(INOC_DIV_ID) ) {
					req.setAttribute(InocServlet.SEARCHRESULTS_ATTR, ( req.getParameter("allInocs") != null ? thisStrain.getInoculations() :  SQLInoc.viableInocsForStrain(this.getSQLData(req), thisStrain.getID()) ));
					this.forwardRequest(req, res, "/strain/inoc-list.jsp");
				} else if ( divTag.equals(COL_DIV_ID) ) {
					req.setAttribute(CollectionServlet.SEARCHRESULTS_ATTR, thisStrain.getFieldCollections());
					this.forwardRequest(req, res, "/collection/collection-list.jsp");
				} else if ( divTag.equals(CRYO_DIV_ID) ) {
					//						CryoForm aForm = new CryoForm(aWrap);
					//						out.println(aForm.cryoList(thisStrain));
				} else if ( divTag.equals(PHOTO_DIV_ID) ) {
					req.setAttribute(STRAIN_OBJECT, thisStrain);
					if ( req.getParameter(DataFileServlet.ACTION_SHOW_BROWSER) != null ) {
						AppConfig myConf = this.getAppConfig();
						String path = myConf.getFilePath(Strain.DATA_FILE_CLASS, Strain.PHOTO_DATA_TYPE);
						req.setAttribute(DataFileServlet.ATTR_ROOT_DIR, path);
						if ( req.getParameter("path") != null ) {
							req.setAttribute(DataFileServlet.ATTR_CURRENT_DIR, new File(path, req.getParameter("path")));					
						} else {
							req.setAttribute(DataFileServlet.ATTR_CURRENT_DIR, new File(path));						
						}
					}
					this.forwardRequest(req, res, "/strain/strain-photos.jsp");
				} else if ( divTag.equals(PHOTO_FORM)) {
					//						out.println(strainForm.photoForm(thisStrain, 3));
				} else if ( divTag.equals(COMPOUND_DIV_ID) ) {
					req.setAttribute(CompoundServlet.COMPOUND_RESULTS, SQLCompound.compoundsForStrain(this.getSQLData(req), thisStrain));
					this.forwardRequest(req, res, "/compund/compound-list.jsp");
				} else if ( divTag.equals(DIV_VALID_GENUS) ) {
					this.validateGenus(req, res);
				}
			}
		} else if ( divTag.equals(SEARCH_DIV_ID) ) {
			if ( req.getParameter(FIELD_QUERY) != null ) {
				req.setAttribute(SEARCHRESULTS_ATTR, getStrains(req));
			}
			this.forwardRequest(req, res, "/strain/strain-list.jsp");
		}
		out.flush();
		return;
	}

	private Strain getStrains(HttpServletRequest req) throws DataException, SQLException {
		String queryString = req.getParameter(FIELD_QUERY);
		if (queryString.matches("\\*") ) {
			queryString.replaceAll("\\*", "%");
		} else {
			queryString = "%" + queryString + "%";
		}

		String sortString = req.getParameter(PARAM_SORT_FIELD);

		if ( sortString == null )
			sortString = SQLStrain.SORT_ID;
		else if ( sortString.equals(SQLStrain.ID_COLUMN) )
			sortString = SQLStrain.SORT_ID;

		String sortDir = req.getParameter(PARAM_SORT_DIR);

		if ( sortDir == null ) 
			sortDir = SQLStrain.ASCENDING_SORT;

		String[] columns = {SQLStrain.NAME_COLUMN, SQLStrain.ID_COLUMN};
		String[] queries = {queryString, queryString};

		if ( sortString.equals(Taxon.LEVEL_ORDER) ) 
			return SQLStrain.strainsLikeByTaxa(this.getSQLData(req), columns, queries, sortString, sortDir);
		else 
			return SQLStrain.strainsLike(this.getSQLData(req), columns, queries, sortString, sortDir);
	}

	public String getHelpModule() {
		return HELP_MODULE;
	}

	/*
	private String livesearchQuery(SQLData data, String divID, String parameter, String value) throws DataException, SQLException {
		String lval = String.format("%s%%", value);
		String lvalSpace = String.format("%% %s%%", value);
		StringBuffer output = new StringBuffer();

		String[] likeColumns = { SQLStrain.ID_COLUMN };
		String[] likeValues = { lval };
		Strain strains = SQLStrain.strainsLike(data, likeColumns, likeValues, SQLStrain.ID_COLUMN, SQLStrain.ASCENDING_SORT);
		strains.beforeFirst();
		while ( strains.next() ) {
			output.append(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", parameter, strains.getID(), divID, strains.getID()));			
		}

		PreparedStatement psth = data.prepareStatement("SELECT DISTINCT name FROM species WHERE name LIKE ? OR name LIKE ? ORDER BY name");
		psth.setString(1, lval);
		psth.setString(2, lvalSpace);
		ResultSet results = psth.executeQuery();

		results.beforeFirst();
		while ( results.next() ) {
			String name = results.getString(1);
			output.append(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", parameter, name, divID, name));			
		}

		String[] like = { lval, lvalSpace };
		TaxonFlat taxa = SQLTaxonFlat.taxaLike(data, TaxonFlat.GENUS, like);
		taxa.beforeFirst();
		while ( taxa.next() ) {
			output.append(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", parameter, taxa.getGenus(), divID, taxa.getGenus()));
		}


		if ( output.length() > 0 ) {
			return output.toString();
		} else {
			return "No suggestions.";
		}
	}
	 */

	private void livesearchQuery(HttpServletRequest req, HttpServletResponse res) throws DataException, SQLException, IOException {
		String searchTag = req.getParameter(PARAM_LIVESEARCH);
		String divID = req.getParameter("div");
		String value = req.getParameter(searchTag);

		boolean hasOutput = false;

		PrintWriter out = res.getWriter();

		String lval = String.format("%s%%", value);
		String lvalSpace = String.format("%% %s%%", value);

		String[] likeColumns = { SQLStrain.ID_COLUMN };
		String[] likeValues = { lval };
		Strain strains = SQLStrain.strainsLike(this.getSQLData(req), likeColumns, likeValues, SQLStrain.ID_COLUMN, SQLStrain.ASCENDING_SORT);
		strains.beforeFirst();
		while ( strains.next() ) {
			hasOutput = true;
			out.println(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", searchTag, strains.getID(), divID, strains.getID()));			
		}

		PreparedStatement psth = this.getSQLData(req).prepareStatement("(SELECT DISTINCT name AS label FROM species WHERE name LIKE ? OR name LIKE ?) " +
				"UNION DISTINCT (SELECT DISTINCT genus AS label FROM species WHERE genus LIKE ? OR genus LIKE ?) ORDER BY label ASC");
		psth.setString(1, lval);
		psth.setString(2, lvalSpace);
		psth.setString(3, lval);
		psth.setString(4, lvalSpace);
		ResultSet results = psth.executeQuery();

		results.beforeFirst();
		while ( results.next() ) {
			hasOutput = true;
			String name = results.getString(1);
			out.println(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", searchTag, name, divID, name));			
		}		

		/*
		Taxon taxa = SQLTaxon.taxaLike(this.getSQLData(req), lval, Taxon.LEVEL_GENUS);
		taxa.beforeFirst();
		while ( taxa.next() ) {
			hasOutput = true;
			out.println(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", searchTag, taxa.getName(), divID, taxa.getName()));
		}
		 */

		if ( ! hasOutput ) 
			out.println("No suggestions.");
	}

	public static void printCultureIDs(SQLData data, HttpServletRequest req, HttpServletResponse res) throws DataException, IOException {
		PrintWriter out = res.getWriter();
		res.setContentType("text/html; charset=UTF-8");

		String field = req.getParameter(PARAM_LIVESEARCH);
		String[] likeColumns = { SQLStrain.ID_COLUMN };
		String[] likeValues = { String.format("%s%%", req.getParameter(field)) };
		Strain strains = SQLStrain.strainsLike(data, likeColumns, likeValues, SQLStrain.ID_COLUMN, SQLStrain.ASCENDING_SORT);

		if ( strains.first() ) {
			strains.beforeFirst();
			while ( strains.next() ) {
				out.println(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", field, strains.getID(), 
						req.getParameter("div"), strains.getID()));
			}
		} else {
			out.println("No suggestions.");
		}
	}

	private void livesearchGenus(HttpServletRequest req, HttpServletResponse res) throws IOException {
		PrintWriter out = res.getWriter();
		try {
			String searchParam = req.getParameter(PARAM_GENUS).concat("%");
			Taxon taxa = SQLTaxon.taxaLike(this.getSQLData(req), searchParam, Taxon.LEVEL_GENUS);
			if ( taxa.first() ) {
				taxa.beforeFirst();
				while ( taxa.next() ) {
					String name = taxa.getName();
					out.println(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", PARAM_GENUS, name, DIV_VALID_GENUS, name));
				}
			} else {
				out.println("No suggestions.");
			}
		} catch (DataException e) {
			out.print("ERROR: ");
			out.print(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			out.print("ERROR: ");
			out.print(e.getMessage());
			e.printStackTrace();
		}

	}


	private void livesearchCollection(HttpServletRequest req, HttpServletResponse res) throws IOException {
		PrintWriter out = res.getWriter();
		try {
			String searchParam = "%".concat(req.getParameter(LS_PARAM_COLLECTION)).concat("%");
			Collection collections = SQLCollection.collectionsLike(getSQLData(req), new String[]{ SQLCollection.ID_COLUMN }, 
					new String[]{ searchParam }, SQLCollection.ID_COLUMN, SQLCollection.ASCENDING_SORT);
			if ( collections.first() ) {
				collections.beforeFirst();
				while ( collections.next() ) {
					String name = collections.getID();
					out.println(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", LS_PARAM_COLLECTION, name, req.getParameter("div"), name));
				}
			} else {
				out.println("No suggestions.");
			}
		} catch (DataException e) {
			out.print("ERROR: ");
			out.print(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			out.print("ERROR: ");
			out.print(e.getMessage());
			e.printStackTrace();
		}

	}

	private void livesearchIsolation(HttpServletRequest req, HttpServletResponse res) throws IOException {
		PrintWriter out = res.getWriter();
		try {
			String searchParam = "%".concat(req.getParameter(LS_PARAM_ISOLATION)).concat("%");
			Isolation isolations = SQLIsolation.isolationsLike(this.getSQLData(req), 
					new String[]{SQLIsolation.ID_COLUMN}, new String[]{searchParam}, 
					SQLIsolation.ID_COLUMN, SQLIsolation.ASCENDING_SORT);
			if ( isolations.first() ) {
				isolations.beforeFirst();
				while ( isolations.next() ) {
					String name = isolations.getID();
					out.println(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", LS_PARAM_ISOLATION, name, req.getParameter("div"), name));
				}
			} else {
				out.println("No suggestions.");
			}
		} catch (DataException e) {
			out.print("ERROR: ");
			out.print(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			out.print("ERROR: ");
			out.print(e.getMessage());
			e.printStackTrace();
		}

	}




	private void validateGenus(HttpServletRequest req, HttpServletResponse res) throws DataException, IOException, SQLException {
		PrintWriter out = res.getWriter();

		if ( SQLTaxon.load(this.getSQLData(req), req.getParameter("genus")).first() ) {
			out.println("<b>Valid name</b>");
		}
		Taxon possibles = SQLTaxon.soundsLike(this.getSQLData(req), req.getParameter("genus"));
		possibles.beforeFirst();
		out.print("<ul style=\"list-style-type: none;\">");
		while ( possibles.next() ) {
			out.print("<li>");
			out.print("<BUTTON TYPE='BUTTON' onClick=\"this.form.genus.value='" +
					possibles.getName() + "'\">Select</BUTTON> <b>");
			String level = possibles.getLevel();
			out.print(level.substring(0, 1).toUpperCase().concat(level.substring(1).toLowerCase()));
			out.print("</b> <i>");
			out.print(possibles.getName());
			out.print("</i>");
			Taxon lineage = possibles.getLinage();
			lineage.first();
			if ( ! level.equals(lineage.getLevel() ) ) {
				out.print(" (");
				out.print( lineage.getName() );
				while ( lineage.next() ) {
					if ( ! level.equals(lineage.getLevel()) ) {
						out.print(", ");
						out.print(lineage.getName());
					}
				}
				out.print(")");
			}

			out.println(")</li>");
		}
		out.print("</ul>");
	}
}
