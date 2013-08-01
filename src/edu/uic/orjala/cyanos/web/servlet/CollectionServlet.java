package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.sql.SQLCollection;
import edu.uic.orjala.cyanos.sql.SQLIsolation;
import edu.uic.orjala.cyanos.web.SheetWriter;

public class CollectionServlet extends ServletObject {
	
	private static float LAT_RANGE = 180.0f / 256.0f;
	private static float LONG_RANGE = 360.0f / 256.0f;


	/**
	 * @author George Chlipala
	 *
	 */
	public class MapBounds {

		protected float minLat = -90.0f;
		protected float maxLat = 90.0f;
		protected float minLong = -180.0f;
		protected float maxLong = 180.0f;
		
		/**
		 * 
		 */
		public MapBounds() {
			// TODO Auto-generated constructor stub
		}
		
		public float getMinLongitude() {
			return minLong;
		}
		
		public float getMinLatitude() { 
			return minLat;
		}
		
		public float getMaxLongitude() {
			return maxLong;
		}
		
		public float getMaxLatitude() { 
			return maxLat;
		}

	}


	public static final String PARAM_COLLECTION_ID = "col";

	public static final String PARAM_BBOX = "bbox";
	public static final String PARAM_MAX_LONG = "maxLong";
	public static final String PARAM_MIN_LONG = "minLong";
	public static final String PARAM_MAX_LAT = "maxLat";
	public static final String PARAM_MIN_LAT = "minLat";
	
	public static final String ATTR_COLLECTION = "collection";
	public static final String ATTR_ISOLATION = "isolation";
	public static final String ATTR_MAP_BOUNDS = "map bounds";

	/**
	 * 
	 */
	private static final long serialVersionUID = -4993281813710300178L;

	public static final String HELP_MODULE = "collection";
	public static final String SEARCHRESULTS_ATTR = "searchresults";
	public static final String ISOLATION_DIV_ID = "isolations";
	public static final String STRAIN_LIST_DIV_ID = "strains";
	public static final String HARVEST_DIV_ID = "harvests";
	public static final String INFO_FORM_DIV_ID = "infoForm";

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


	private void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {

		try {
			if ( req.getParameter("div") != null ) {
				this.handleAJAX(req, res);
				return;
			} else if ( req.getRequestURI().endsWith(".kml") ) {
				this.printKML(req, res);
				return;
			} else if ( req.getRequestURI().endsWith(".csv") ) {
				if ( req.getParameter(PARAM_COLLECTION_ID) != null )
					this.exportIsolations(req, res);
				else 
					this.exportCollections(req, res);
				return;
			}
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} catch (XMLStreamException e) {
			throw new ServletException(e);
		}

		try {
			
			String form = req.getParameter("form");
			if ( form != null ) {
				if ( form.equals("add") ) {
					this.forwardRequest(req, res, "/collection/add-collection.jsp");					
					return;
				}
			} 

			if ( req.getParameter("col") != null ) {
				req.setAttribute(ATTR_COLLECTION, SQLCollection.load(this.getSQLData(req), req.getParameter("col")));	
				req.setAttribute(ATTR_MAP_BOUNDS, this.getMapBounds(req));
			} else if ( req.getParameter("id") != null ) {
				req.setAttribute(ATTR_ISOLATION, SQLIsolation.load(this.getSQLData(req), req.getParameter("id")));
				this.forwardRequest(req, res, "/isolation.jsp");
				return;
			} else if ( req.getParameter("query") != null ) {
				req.setAttribute(SEARCHRESULTS_ATTR, this.getCollectionList(req));
				req.setAttribute(ATTR_MAP_BOUNDS, this.getMapBounds(req));
			}
			this.forwardRequest(req, res, "/collection.jsp");
			return;
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}

	}
		
/*
	public void display(CyanosWrapper aWrap) throws Exception {

		PrintWriter out = aWrap.getWriter();
		
		String module = aWrap.getRequest().getPathInfo();
		CollectionForm colForm = new CollectionForm(aWrap);

		if ( aWrap.getRequestURI().endsWith(".csv") ) {
			aWrap.setContentType("text/plain");
			if ( aWrap.hasFormValue(PARAM_COLLECTION_ID) )
				out.println(this.exportIsolations(aWrap, aWrap.getFormValue(PARAM_COLLECTION_ID)));
			else 
				out.println(this.exportCollections(colForm));
			out.flush();
//			out.close();
//			this.closeSQL();
			return;
		} else if ( aWrap.hasFormValue("div") ) {
			aWrap.setContentType("text/html; charset=UTF-8");
			String divTag = aWrap.getFormValue("div");

		} else if ( aWrap.getRequestURI().endsWith(".kml") ) {
			this.printKML(aWrap.getRequest(), aWrap.getResponse());
			out.flush();
			return;
		}
		
		aWrap.setContentType("text/html; charset=UTF-8");

		if ( aWrap.hasFormValue("id") )
			out = aWrap.startHTMLDoc("Isolation Data");
		else
			out = aWrap.startHTMLDoc("Collection Data", true, true);
					
		if ( module == null ) {
			if ( aWrap.hasFormValue("id") ) {
				Isolation anIso = new SQLIsolation(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Isolation " + anIso.getID());
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head);
				
				IsolationForm aForm = new IsolationForm(aWrap);

				out.println(aForm.showIsolation(anIso));
				out.println(aForm.loadableDiv(ISOLATION_DIV_ID, "Child Isolations"));
				out.println(aForm.loadableDiv(STRAIN_LIST_DIV_ID, "Strain List"));
			
			} else if ( aWrap.hasFormValue(PARAM_COLLECTION_ID) ) {
				Collection aCol = SQLCollection.load(aWrap.getSQLDataSource(), aWrap.getFormValue(PARAM_COLLECTION_ID));
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Collection #" + aCol.getID());
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head);

				out.println(colForm.showCollection(aCol));
				
				out.println(colForm.loadableDiv(ISOLATION_DIV_ID, "Isolations"));
				out.println(colForm.loadableDiv(STRAIN_LIST_DIV_ID, "Strain List"));
				out.println(colForm.loadableDiv(HarvestForm.DIV_ID, "Harvests"));	

				/*
				if ( aWrap.hasFormValue("isolationForm") ) {
					out.println("<HR WIDTH='75%' ALIGN='CENTER'/><P ALIGN='CENTER'><FONT SIZE='+1'><B>Add an Isolation</B></FONT>");
					out.println(this.addIsolationForm(aCol));
				} else {
					if ( aWrap.hasFormValue("addIsolation") )
						out.println(this.addIsolation());

					out.println("<HR WIDTH='75%' ALIGN='CENTER'/><P ALIGN='CENTER'><FONT SIZE='+1'><B>Isolation List</B></FONT>");
					out.println(isoForm.(aCol.getIsolations()));
					Form myForm = new Form("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='isolationForm' VALUE='Add an Isolation'/></P>");
					myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='col' VALUE='%s'/>", aWrap.getFormValue("col")));
					myForm.setAttribute("METHOD", "POST");
					out.println(myForm.toString());
				}
				*/
				
				/*
				if ( aWrap.hasFormValue("harvestForm")) {
					output.append("<HR WIDTH='75%' ALIGN='CENTER'/><P ALIGN='CENTER'><FONT SIZE='+1'><B>Add a Field Harvest</B></FONT>");
					output.append(this.harvestForm());
				} else {
					if ( aWrap.hasFormValue("addHarvest")) {
						output.append(this.addHarvest());
					} 
					output.append("<HR WIDTH='75%' ALIGN='CENTER'/><P ALIGN='CENTER'><FONT SIZE='+1'><B>Harvest List</B></FONT>");
					output.append(this.harvestList(aCollection.getHarvest()));
					myForm = new Form("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='harvestForm' VALUE='Add a Harvest'/></P>");
					myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='col' VALUE='%s'/>", aWrap.getFormValue("col")));
					myForm.setAttribute("METHOD", "POST");
					output.append(myForm.toString());
				}
*//*
			} else {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Collection Data");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head.toString());
				Popup fieldPop = new Popup();
				fieldPop.addItemWithLabel(SQLCollection.ID_COLUMN, "ID");
				fieldPop.addItemWithLabel(SQLCollection.DATE_COLUMN, "Date");
				fieldPop.addItemWithLabel(SQLCollection.COLLECTOR_COLUMN, "Collector");
				fieldPop.addItemWithLabel(SQLCollection.LOCATION_COLUMN, "Location");
				fieldPop.setName("field");
				if ( aWrap.hasFormValue("field")) fieldPop.setDefault(aWrap.getFormValue("field"));
				Form aForm = new Form("<P ALIGN='CENTER'>");
				aForm.addItem(fieldPop);
				if ( aWrap.hasFormValue("query"))
					aForm.addItem(String.format("<INPUT TYPE='TEXT' NAME='query' VALUE='%s' SIZE='20'/><INPUT TYPE='SUBMIT' VALUE='Search'/><BR/>", aWrap.getFormValue("query")));
				else
					aForm.addItem("<INPUT TYPE='TEXT' NAME='query' SIZE='20'/><INPUT TYPE='SUBMIT' VALUE='Search'/><BR/>");
				out.println(aForm.toString());
							
				try {
					if ( aWrap.hasFormValue("query") && ( ! aWrap.getFormValue("query").equals("") ) ) {
						out.println(String.format("<P ALIGN='CENTER'>Export Collection Data [<A HREF='collection.csv?%s'>CSV</A>] [<A HREF='%s'>KML</A>]</P>", aWrap.getRequest().getQueryString(), getKMLURL(aWrap)));
						String query = aWrap.getFormValue("query");
						if ( query.indexOf("*") > -1 ) query = query.replaceAll("\\*", "%");
						else query = "%" + query + "%";
						String[] columns = { aWrap.getFormValue("field") };
						String[] values = { query };
						Collection colList = SQLCollection.collectionsLike(aWrap.getSQLDataSource(), columns, values, SQLCollection.ID_COLUMN, SQLCollection.ASCENDING_SORT);
						CyanosConfig myConf = aWrap.getAppConfig();
						if ( myConf.canMap() ) {
							out.println(colForm.collectionMap(colList, 700, 500));
						}
						out.println(colForm.collectionList(colList));
					}
/*					if ( myConf.canMap() && aWrap.hasFormValue("showMap")) {
						out.println(this.collectionMap(colList));
						out.println("<SCRIPT TYPE='text/javascript'>\n//<![CDATA[\n");
						out.println("function setupControls(map) {");
						out.println(" map.addControl(new GMapTypeControl());\n map.addControl(new GLargeMapControl());\n var bottomRight = new GControlPosition(G_ANCHOR_BOTTOM_RIGHT, new GSize(10,25));\n map.addControl(new GScaleControl(), bottomRight);\n } \n");
						out.println("//]]>\n</SCRIPT>\n");
						out.println(String.format(this.mapCanvas, 700, 500));
					}
					
*//*
				} catch (DataException e) {
					out.println("<P ALIGN='CENTER'><FONT COLOR='red'><B>ERROR:</FONT> " + e.getMessage() + "</B></P>");
				}
			}
		} else if ( module.equals("/add") ) {
			if ( aWrap.hasFormValue("makeCollection")) {
				out.println(colForm.addCollection());
			} else {
				out.println(colForm.addCollectionForm());
			}
		} else if ( module.equals("/report") ) {
			
			// Collection report?
/*
			try {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Collection Data Report");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head.toString());
				
				String sqlString = "SELECT s.culture_id,c.latitude,c.longitude,s.name " +
					"FROM collection c LEFT OUTER JOIN isolation i ON(i.collection_id=c.collection_id) " +
					"LEFT OUTER JOIN species s ON (i.isolation_id = s.culture_source) " +
					"WHERE c.latitude IS NOT NULL AND c.longitude IS NOT NULL";
				Statement aSth = this.dbc.createStatement();
				ResultSet aResult = aSth.executeQuery(sqlString);
				Map<String,List<String>> markers = this.strainMarkers(aResult);
				out.println(this.onOffMap(markers, "Strains", "No Strains"));
				out.println("<SCRIPT TYPE='text/javascript'>\n//<![CDATA[\n");
				out.println("function setupControls(map) {");
				out.println(" map.addControl(new GMapTypeControl());\n map.addControl(new GLargeMapControl());\n var bottomRight = new GControlPosition(G_ANCHOR_BOTTOM_RIGHT, new GSize(10,25));\n map.addControl(new GScaleControl(), bottomRight);\n } \n");
				out.println("//]]>\n</SCRIPT>\n");
				out.println("<DIV id='main_content' style='margin: 0 auto; width: 75%; height: 550 px;'><div id='map_canvas' style='width: 700px; height: 500px; float: left; margin-right:10 px'></div>");
				out.println(this.strainMapControl());
				out.println("</DIV>");
			} catch (SQLException e) {
				out.println("<P ALIGN='CENTER'><FONT COLOR='red'><B>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
			}
*//*
		}
		
		aWrap.finishHTMLDoc();
	}
*/
	
	private MapBounds getMapBounds(HttpServletRequest req) throws DataException, SQLException {
		MapBounds bounds = new MapBounds();
		
		if ( req.getAttribute(ATTR_COLLECTION) != null ) {
			Collection center = (Collection) req.getAttribute(ATTR_COLLECTION);
			Float latFloat = center.getLatitudeFloat();
			Float longFloat = center.getLongitudeFloat();
			if ( (latFloat != null) && (longFloat != null) ) {
				float thisLat = latFloat.floatValue();
				float thisLong = longFloat.floatValue();
				bounds.maxLat = thisLat + LAT_RANGE; 
				bounds.minLat = thisLat - LAT_RANGE;
				bounds.maxLong = thisLong + LONG_RANGE;
				bounds.minLong = thisLong - LONG_RANGE;
			}
		} else if ( req.getParameter("query") != null ) {
			String query = req.getParameter("query");
			if ( ( ! query.equals("") ) ) {
				if ( query.indexOf("*") > -1 ) query = query.replaceAll("\\\\*", "%");
				else query = "%" + query + "%";
				String[] columns = { req.getParameter("field") };
				String[] values = { query };
				float[] b = SQLCollection.boundsLike(this.getSQLData(req), columns, values, SQLCollection.ID_COLUMN, SQLCollection.ASCENDING_SORT);
				bounds.minLong = b[0];
				bounds.minLat = b[1];
				bounds.maxLong = b[2];
				bounds.maxLat = b[3];
			}
		}
		return bounds;
	}

	private void handleAJAX(HttpServletRequest req, HttpServletResponse res) throws DataException, SQLException, IOException, ServletException {
		res.setContentType("text/html; charset=UTF-8");
		String divID = req.getParameter("div");
		PrintWriter out = res.getWriter();

		if ( req.getParameter(PARAM_COLLECTION_ID) != null) {
			Collection aCol = SQLCollection.load(this.getSQLData(req), req.getParameter(PARAM_COLLECTION_ID));
			if ( aCol.isLoaded() ) {
				if ( aCol.isAllowed(Role.READ) ) {
					if ( divID.equals(STRAIN_LIST_DIV_ID) ) {
						req.setAttribute(StrainServlet.SEARCHRESULTS_ATTR, aCol.getStrains());
						this.forwardRequest(req, res, "/strain/strain-list.jsp");
					} else if ( divID.equals(HARVEST_DIV_ID) ) {
						req.setAttribute(HarvestServlet.SEARCHRESULTS_ATTR, aCol.getHarvest());
						this.includeRequest(req, res, "/harvest/harvest-list.jsp");
						out.println(String.format("<P ALIGN='CENTER'><A HREF=\"%s/inoc?form=harvest&col=%s\"/>Add a New Harvest</A></P>", req.getContextPath(), aCol.getID()));
					} else if ( divID.equals(ISOLATION_DIV_ID)) {
						req.setAttribute(SEARCHRESULTS_ATTR, aCol.getIsolations());
						this.forwardRequest(req, res, "/isolation/isolation-list.jsp");
					} else if ( divID.equals(INFO_FORM_DIV_ID) ) {
						req.setAttribute(ATTR_COLLECTION, aCol);
						req.setAttribute(ATTR_MAP_BOUNDS, this.getMapBounds(req));
						req.setAttribute("canMap", new Boolean(this.getAppConfig().canMap()));
						this.forwardRequest(req, res, "/collection/collection-form.jsp");
					}
				} else {
					out.print("ACCESS DENIED");
				}
			}
		} else if (req.getParameter("id") != null ) {
			Isolation anIso = SQLIsolation.load(this.getSQLData(req), req.getParameter("id"));
			if ( anIso.isLoaded() ) {
				if ( anIso.isAllowed(Role.READ) ) {
					if ( divID.equals(STRAIN_LIST_DIV_ID) ) {
						req.setAttribute(StrainServlet.SEARCHRESULTS_ATTR, anIso.getStrains());
						this.includeRequest(req, res, "/strain/strain-list.jsp");
						out.println(String.format("<P ALIGN='CENTER'><A HREF=\"strain?action=add&culture_source=%s\"/>Add a New Strain</A></P>", anIso.getID()));
					} else if ( divID.equals(ISOLATION_DIV_ID) ) {
						req.setAttribute(SEARCHRESULTS_ATTR, anIso.getChildren());
						this.forwardRequest(req, res, "/isolation/isolation-list.jsp");
					}
				} else {
					out.print("ACCESS DENIED");
				}
			}
		}
		out.flush();
		return;
	}
	
	/*
	private String strainMapControl() {
		Popup aPop = new Popup();
		aPop.addItemWithLabel("simple", "Simple");
		aPop.addItemWithLabel("color", "Coded by Order");
		aPop.setName("type");
		
		Form myForm = new Form("Map Type:");
		myForm.addItem(aPop);
		
		return myForm.toString();
	}
	*/
	
	private void exportCollections(HttpServletRequest req, HttpServletResponse res) throws IOException {
		PrintWriter out = res.getWriter();
		res.setContentType("text/plain");
		
		try {
			
			Collection thisCol = this.getCollectionList(req);

			if ( thisCol != null && thisCol.first() ) {
				SheetWriter sheetOut = new SheetWriter(out);
				sheetOut.print("Collection ID");
				sheetOut.print("Date");
				sheetOut.print("Collected by");
				sheetOut.print("Location Name");
				sheetOut.print("Latitude");
				sheetOut.print("Longitude");
				sheetOut.print("Coordinate Precision");
				sheetOut.print("Project Code");
				sheetOut.println("Notes");
				thisCol.beforeFirst();
				while ( thisCol.next() ) {
					sheetOut.print(thisCol.getID());
					sheetOut.print(thisCol.getDateString());
					sheetOut.print(thisCol.getCollector());
					sheetOut.print(thisCol.getLocationName());
					sheetOut.print(thisCol.getLatitudeDM());
					sheetOut.print(thisCol.getLongitudeDM());
					sheetOut.print(String.format("%d m", thisCol.getPrecision()));
					sheetOut.print(thisCol.getProjectID());
					sheetOut.println(thisCol.getNotes());
				}
			} else {
				out.println("ERROR: COLLECTIONS NOT FOUND.");
			}
		} catch (DataException e) {
			e.printStackTrace();
			out.print("ERROR: ");
			out.println(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			out.print("ERROR: ");
			out.println(e.getMessage());
		}
	}

	private void exportIsolations(HttpServletRequest req, HttpServletResponse res) throws IOException {
		PrintWriter out = res.getWriter();
		res.setContentType("text/plain");
		try {
			Isolation isos = SQLIsolation.isolationsForCollection(this.getSQLData(req), req.getParameter(PARAM_COLLECTION_ID));
			if ( isos != null && isos.first() ) {
				SheetWriter sheetOut = new SheetWriter(out);
				sheetOut.print("Isolation ID");
				sheetOut.print("Date");
				sheetOut.print("Source Collection");
				sheetOut.print("Type");
				sheetOut.print("Media");
				sheetOut.print("Parent");
				sheetOut.print("Project Code");
				sheetOut.println("Notes");
				isos.beforeFirst();
				while ( isos.next() ) {
					sheetOut.print(isos.getID());
					sheetOut.print(isos.getDateString());
					sheetOut.print(isos.getCollectionID());
					sheetOut.print(isos.getType());
					sheetOut.print(isos.getMedia());
					sheetOut.print(isos.getParentID());
					sheetOut.print(isos.getProjectID());
					sheetOut.println(isos.getNotes());
				}
			} else {
				out.println("ERROR: NO RECORDS FOUND.");
			}
		} catch (DataException e) {
			e.printStackTrace();
			out.print("ERROR: ");
			out.println(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			out.print("ERROR: ");
			out.println(e.getMessage());
		}
	}
	
	public void printKML(HttpServletRequest req, HttpServletResponse res) throws XMLStreamException, IOException, DataException, SQLException {
		res.setContentType("application/vnd.google-earth.kml+xml");
		XMLOutputFactory xof = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = xof.createXMLStreamWriter(res.getWriter());
		writer.writeStartDocument("UTF-8", "1.0");
		writer.writeStartElement("kml");
		writer.writeAttribute("xmlns", "http://www.opengis.net/kml/2.2");
		writer.writeStartElement("Document");
		
		writer.writeStartElement("Style");
		writer.writeAttribute("id", "collectionMark");
		writer.writeStartElement("IconStyle");
		writer.writeStartElement("Icon");
		writer.writeStartElement("href");
		writer.writeCharacters(String.format("%s://%s:%d%s/images/map_icons/A.png", req.getScheme(), req.getServerName(), 
				req.getServerPort(), req.getContextPath()));
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
			
		this.writeCollectionsKML(req, writer, "#collectionMark");

		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		writer.close();
	}

	public Collection getCollectionList(HttpServletRequest req) throws DataException, SQLException {
		Collection colList = null;
		if ( req.getParameter(PARAM_COLLECTION_ID) != null ) {
			Float maxLat = null , minLat = null, maxLong = null, minLong = null;
			if ( req.getParameter(PARAM_MIN_LAT) != null && req.getParameter(PARAM_MAX_LAT) != null 
					&& req.getParameter(PARAM_MIN_LONG) != null && req.getParameter(PARAM_MAX_LONG) != null) {
				minLat = Float.parseFloat(req.getParameter(PARAM_MIN_LAT));
				maxLat = Float.parseFloat(req.getParameter(PARAM_MAX_LAT));
				minLong = Float.parseFloat(req.getParameter(PARAM_MIN_LONG));
				maxLong = Float.parseFloat(req.getParameter(PARAM_MAX_LONG));
			} else if ( req.getParameter(PARAM_BBOX) != null ) {
				String[] box = req.getParameter(PARAM_BBOX).split(",");
				minLong = Float.parseFloat(box[0]);
				minLat = Float.parseFloat(box[1]);
				maxLong = Float.parseFloat(box[2]);
				maxLat = Float.parseFloat(box[3]);
			} else {
				Collection center = SQLCollection.load(this.getSQLData(req), req.getParameter(PARAM_COLLECTION_ID));
				Float latFloat = center.getLatitudeFloat();
				Float longFloat = center.getLongitudeFloat();
				boolean hasLocation = ( (latFloat != null) && (longFloat != null) );
				if ( hasLocation ) {
					float thisLat = latFloat.floatValue();
					float thisLong = longFloat.floatValue();
					float latRange = 180.0f / 256.0f;
					float longRange = 360.0f / 256.0f;
					maxLat = thisLat + latRange; 
					minLat = thisLat - latRange;
					maxLong = thisLong + longRange;
					minLong = thisLong - longRange;
				}
			}
			if ( maxLat != null )
				colList = SQLCollection.collectionsLoacted(this.getSQLData(req), minLat, maxLat, minLong, maxLong);
		} else {
			String query = req.getParameter("query");
			if ( query != null && ( ! query.equals("") ) ) {
				if ( query.indexOf("*") > -1 ) query = query.replaceAll("\\\\*", "%");
				else query = "%" + query + "%";
				String[] columns = { req.getParameter("field") };
				String[] values = { query };
				colList = SQLCollection.collectionsLike(this.getSQLData(req), columns, values, SQLCollection.ID_COLUMN, SQLCollection.ASCENDING_SORT);
			} else {
				colList = SQLCollection.collections(this.getSQLData(req));
			}
		}
		return colList;
	}

	private void writeCollectionsKML(HttpServletRequest req, XMLStreamWriter writer, String styleURL) throws DataException, XMLStreamException, SQLException {
		Collection cols = this.getCollectionList(req);
		String selectedCol = req.getParameter(PARAM_COLLECTION_ID);
		if ( cols != null && cols.first() ) {
			String url = req.getRequestURL().toString();
			url = url.replace("collection.kml", "collection");
			cols.beforeFirst();
			while ( cols.next() ) {
				Float latFloat = cols.getLatitudeFloat();
				Float longFloat = cols.getLongitudeFloat();
				if ( (latFloat == null) || (longFloat == null) ) continue;
				double thisLat = latFloat.doubleValue();
				double thisLong = cols.getLongitudeFloat();

				writer.writeStartElement("Placemark");

				writer.writeStartElement("name");
				writer.writeCharacters(cols.getID());
				writer.writeEndElement();

				writer.writeStartElement("description");
				writer.writeCharacters(getKMLDescription(cols, url, DATE_FORMAT));
				writer.writeEndElement();

				writer.writeStartElement("styleUrl");
				if ( cols.getID().equals(selectedCol))		
					writer.writeCharacters("#focusMark");
				else 
					writer.writeCharacters(styleURL);
				writer.writeEndElement();			
				

				writer.writeStartElement("Point");
				writer.writeStartElement("coordinates");
				writer.writeCharacters(String.format("%.4f, %.4f", thisLong, thisLat));
				writer.writeEndElement();
				writer.writeEndElement();

				writer.writeEndElement();
				
			}
		}
	}
	
	protected static String getKMLDescription(Collection col, String url, DateFormat dateFormat) throws DataException {
		return String.format("<P><A HREF='%s?col=%s&showMap=true' CLASS='map'>%s</A> - %s By: %s<BR/></P>", 
				url, col.getID(), col.getID(), dateFormat.format(col.getDate()), col.getCollector());
	}


/*
 * This method was used to create tabbed dialogs for the Google map.  
 * However, it was not aestitically pleasing and thus replaced by buildMapMakers() method.
 * 
	private Map<String,List<Map<String,String>>> buildTabbedMapMarkers(Collection aCol) throws SQLException {
		Map<String,List<Map<String,String>>> retVal = new HashMap<String,List<Map<String,String>>>();
		if ( aCol != null && aCol.first() ) {
			double minLat = 90.0f, maxLat = -90.0f;
			double minLong = 180.0f, maxLong = -180.0f;
			aCol.beforeFirst();
			SimpleDateFormat myFormat = this.dateFormat();
			while ( aCol.next() ) {
				double thisLat = aCol.getLatitudeFloat();
				boolean wasNull = aCol.lastWasNull();
				double thisLong = aCol.getLongitudeFloat();
				wasNull = ( aCol.lastWasNull() ? aCol.lastWasNull() : wasNull );
				if ( wasNull ) continue;
				String key = String.format("%.4f, %.4f", thisLat, thisLong);
				List<Map<String,String>> aList;
				if ( retVal.containsKey(key) ) {
					aList = retVal.get(key);
				} else {
					aList = new ArrayList<Map<String,String>>();
					retVal.put(key, aList);
				}
				Map<String,String> aPos = new HashMap<String,String>();
				aPos.put("label", aCol.getID());
				String notes = aCol.getNotes().replaceAll("\n", "<BR/>");
				notes = notes.replaceAll("\"", "&quot;");
				if ( notes.length() > 100 ) notes = notes.substring(0, 100);
				String content = String.format("\"<A HREF='?col=%s&showMap=true' CLASS='map'>%s</A><BR/>%s<BR/>By: %s<BR/><P>%s</P>\"", 
						aCol.getID(), aCol.getID(), myFormat.format(aCol.getDate()), aCol.getCollector(),notes);
				aPos.put("content", content);
				aList.add(aPos);
				if ( thisLat > maxLat ) maxLat = thisLat;
				if ( thisLat < minLat ) minLat = thisLat;
				if ( thisLong > maxLong ) maxLong = thisLong;
				if ( thisLong < minLong ) minLong = thisLong;
			}
			Map<String,String> extras = new HashMap<String,String>();
			extras.put("max_lat", String.format("%.4f", maxLat));
			extras.put("min_lat", String.format("%.4f", minLat));
			extras.put("max_long", String.format("%.4f", maxLong));
			extras.put("min_long", String.format("%.4f", minLong));
			double latSize = Math.abs(maxLat - minLat);
			double longSize = Math.abs(maxLong - minLong);
			double latCenter = minLat + ( latSize / 2 );
			double longCenter = minLong + ( longSize / 2 );
			double logBase = Math.log(2);
			double latZoom = Math.log(180 / latSize) / logBase;
			double longZoom = Math.log(360 / longSize) / logBase;
			double zoom = ( latZoom < longZoom ? latZoom : longZoom);
			zoom = ( zoom > 19 ? 19 : zoom );
			extras.put("center", String.format("%.4f, %.4f", latCenter, longCenter));
			extras.put("zoom", String.format("%.0f", zoom));
			
			List<Map<String,String>> extVal = new ArrayList<Map<String,String>>();
			extVal.add(extras);
			retVal.put("_extras_", extVal);		
			
		}
		return retVal;
	}
*/

	/*
	private Map<String,List<String>> strainMarkers(ResultSet aResult) throws SQLException{		
		Map<String,List<String>> retVal = new HashMap<String,List<String>>();
		if ( aResult.first() ) {
			double minLat = 90.0f, maxLat = -90.0f;
			double minLong = 180.0f, maxLong = -180.0f;
			aResult.beforeFirst();
			String contextPath = this.req.getContextPath();
			while ( aResult.next() ) {
				double thisLat = aResult.getDouble(2);
				boolean wasNull = aResult.wasNull();
				double thisLong = aResult.getDouble(3);
				wasNull = ( aResult.wasNull() ? true : wasNull );
				if ( wasNull ) continue;
				String key = String.format("%.4f, %.4f", thisLat, thisLong);
				List<String> aList;
				if ( retVal.containsKey(key) ) {
					aList = retVal.get(key);
				} else {
					aList = new ArrayList<String>();
				}
				String strainID = aResult.getString(1);
				if ( strainID != null ) {
					String content = String.format("<A HREF='%s/strain?id=%s' CLASS='map'>%s <I>%s</I></A><BR/>", 
							contextPath, strainID, strainID, aResult.getString(4));
					aList.add(content);
				}
				retVal.put(key,aList);
				if ( thisLat > maxLat ) maxLat = thisLat;
				if ( thisLat < minLat ) minLat = thisLat;
				if ( thisLong > maxLong ) maxLong = thisLong;
				if ( thisLong < minLong ) minLong = thisLong;
			}
			List<String> extraList = new ArrayList<String>();
			double latSize = Math.abs(maxLat - minLat);
			double longSize = Math.abs(maxLong - minLong);
			double latCenter = minLat + ( latSize / 2 );
			double longCenter = minLong + ( longSize / 2 );
			double logBase = Math.log(2);
			double latZoom = Math.log(180 / latSize) / logBase;
			double longZoom = Math.log(360 / longSize) / logBase;
			double zoom = ( latZoom < longZoom ? latZoom : longZoom);
			zoom = ( zoom > 19 ? 19 : zoom );
			extraList.add(String.format("%.4f, %.4f", latCenter, longCenter));
			extraList.add(String.format("%.0f", zoom));
			retVal.put("_extras_", extraList);
		}
		return retVal;
	}
	*/
	
	/*
	private String onOffMap(Map<String,List<String>> markers, String onTitle, String offTitle) {
		StringBuffer output = new StringBuffer();
		output.append("<SCRIPT TYPE='text/javascript'>\n//<![CDATA[\n");
		String center = "0, 0";
		String zoom = "0";
			StringBuffer markerScript = new StringBuffer();
			markerScript.append("function addCollectionMarkers(map) {\n var onIcon = new GIcon(G_DEFAULT_ICON);\n onIcon.iconSize = new GSize(32, 32);\n onIcon.image=\"");
			markerScript.append(this.getImagePath("map_icons/C.png"));
			markerScript.append("\";\n var offIcon = new GIcon(G_DEFAULT_ICON);\n offIcon.iconSize = new GSize(32, 32);\n offIcon.image=\"");
			markerScript.append(this.getImagePath("map_icons/0.png"));
			markerScript.append("\";\n");
			Iterator<String> keyIter = markers.keySet().iterator();
			while ( keyIter.hasNext() ) {
				String key = keyIter.next();
				if ( key.equals("_extras_") ) continue;
				List<String> markerList = markers.get(key);
				if ( markerList.size() > 0 ) { 
					markerScript.append(String.format(" aCol = new GMarker(new GLatLng(%s), {title: \"%d %s\", icon:onIcon});\n", key, markerList.size(), onTitle));
					Iterator<String> markerIter = markerList.iterator();
					StringBuffer content = new StringBuffer();
					while ( markerIter.hasNext() ) {
						content.append(markerIter.next());
					}
					markerScript.append(String.format(" aCol.bindInfoWindowHtml(\"%s\");\n", content.toString()));
				} else {
					markerScript.append(String.format(" aCol = new GMarker(new GLatLng(%s), {title: \"%s\", icon:offIcon});\n", key, offTitle));
				}
				markerScript.append(" map.addOverlay(aCol);\n");
			}
			center = markers.get("_extras_").get(0);
			zoom = markers.get("_extras_").get(1);
			markerScript.append("}\n");
			output.append(markerScript);
		output.append(this.buildMapSetup(center, zoom));
		output.append("//]]>\n</SCRIPT>\n");
		return output.toString();

	}
	*/
	
	/*
	private String collectionMap(Collection aCol) {
		StringBuffer output = new StringBuffer();
		output.append("<SCRIPT TYPE='text/javascript'>\n//<![CDATA[\n");
		String center = "0, 0";
		String zoom = "0";
		try {
			StringBuffer markerScript = new StringBuffer();
			markerScript.append("function addCollectionMarkers(map) {\n");
			if ( aCol.first() ) {
				Map<String,List<String>> markers = this.buildMapMarkers(aCol);
				Iterator<String> keyIter = markers.keySet().iterator();
				while ( keyIter.hasNext() ) {
					String key = keyIter.next();
					if ( key.equals("_extras_") ) continue;
					List<String> markerList = markers.get(key);
					markerScript.append(String.format(" aCol = new GMarker(new GLatLng(%s), {title: \"%d Collections\"});\n", key, markerList.size()));
					Iterator<String> markerIter = markerList.iterator();
					StringBuffer content = new StringBuffer();
					while ( markerIter.hasNext() ) {
						content.append(markerIter.next());
					}
					markerScript.append(String.format(" aCol.bindInfoWindowHtml(\"%s\");\n", content.toString()));
					markerScript.append(" map.addOverlay(aCol);\n");
				}
				center = markers.get("_extras_").get(0);
				zoom = markers.get("_extras_").get(1);
			} 
			markerScript.append("}\n");
			output.append(markerScript);
		} catch ( DataException e ) {
			output.append("function addCollectionMarkers(map) {\n");
			output.append(String.format("map.openInfoWindow(map.getCenter(),document.createTextNode('ERROR: %s'));}\n", e.getMessage()));
		}
		output.append(this.buildMapSetup(center, zoom));
		output.append("//]]>\n</SCRIPT>\n");
		return output.toString();
	}
*/
	
/*	
	private String collectionMap(Collection aCol, double latCenter, double longCenter, int zoomLevel) {
		StringBuffer output = new StringBuffer();
		zoomLevel = ( zoomLevel > 19 ? 19 : zoomLevel );
		output.append("<SCRIPT TYPE='text/javascript'>\n//<![CDATA[\n");
		try {
			StringBuffer markerScript = new StringBuffer();
			markerScript.append("function addCollectionMarkers(map) {\n");
			if ( aCol.first() ) {
				Map<String,List<String>> markers = this.buildMapMarkers(aCol);
				Iterator<String> keyIter = markers.keySet().iterator();
				while ( keyIter.hasNext() ) {
					String key = keyIter.next();
					if ( key.equals("_extras_") ) continue;
					List<String> markerList = markers.get(key);
					markerScript.append(String.format(" aCol = new GMarker(new GLatLng(%s), {title: \"%d Collections\"});\n", key, markerList.size()));
					Iterator<String> markerIter = markerList.iterator();
					StringBuffer content = new StringBuffer();
					while ( markerIter.hasNext() ) {
						content.append(markerIter.next());
					}
					markerScript.append(String.format(" aCol.bindInfoWindowHtml(\"%s\");\n", content.toString()));
					markerScript.append(" map.addOverlay(aCol);\n");
				}
			} 
			markerScript.append("}\n");
			output.append(markerScript);
		} catch ( DataException e ) {
			output.append("function addCollectionMarkers(map) {\n");
			output.append(String.format("map.openInfoWindow(map.getCenter(),document.createTextNode('ERROR: %s'));}\n", e.getMessage()));
		}
		output.append(this.buildMapSetup(String.format("%.4f, %.4f", latCenter, longCenter), String.format("%d", zoomLevel)));
		output.append("//]]>\n</SCRIPT>\n");
		return output.toString();
	}
	*/
	/*
	private String buildMapSetup(String center, String zoom) {
		StringBuffer output = new StringBuffer();
		output.append("function setupMap(canvas) { \n var map = new GMap2(canvas);\n");
		output.append(String.format(" map.setCenter(new GLatLng(%s), %s);\n", center, zoom));
		output.append(" map.removeMapType(G_HYBRID_MAP);\n map.addMapType(G_PHYSICAL_MAP);\n map.setMapType(G_PHYSICAL_MAP);\n");
		output.append(" addCollectionMarkers(map);\n");
		output.append(" setupControls(map);\n }\n");
		return output.toString();
	}

	*/
	
	
	public String getHelpModule() {
		return HELP_MODULE;
	}
	
	
//	public static String getKMLURL(CyanosWrapper wrap) {
//		return getKMLURL(wrap.getRequest());
//	}

	public static String getKMLURL(HttpServletRequest req) {
		StringBuffer output = new StringBuffer(req.getContextPath());
		output.append("/collection.kml?");
		if ( req.getQueryString() == null ) {
			if ( req.getMethod().equalsIgnoreCase("POST") ) {
				Object col = req.getAttribute(ATTR_COLLECTION);
				if ( col != null && col instanceof Collection) {
					output.append("&col=");
					output.append(((Collection)col).getID());
				} else if ( req.getParameter("query") != null ) { 
					output.append("&field=");
					output.append(req.getParameter("field"));
					output.append("&query=");
					output.append(req.getParameter("query"));
				}
			}
		} else {
			output.append(req.getQueryString());
		}
		return output.toString();
	}

}
