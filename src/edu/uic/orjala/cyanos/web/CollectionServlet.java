package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.sql.SQLCollection;
import edu.uic.orjala.cyanos.sql.SQLIsolation;
import edu.uic.orjala.cyanos.web.forms.CollectionForm;
import edu.uic.orjala.cyanos.web.forms.HarvestForm;
import edu.uic.orjala.cyanos.web.forms.IsolationForm;
import edu.uic.orjala.cyanos.web.forms.StrainForm;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.StyledText;

public class CollectionServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4993281813710300178L;

	private static final String HELP_MODULE = "collection";
	
	private final String ISOLATION_DIV_ID = "isolations";
	private final String STRAIN_LIST_DIV_ID = "strains";

	public void display(CyanosWrapper aWrap) throws Exception {

		PrintWriter out = aWrap.getWriter();
		
		String module = aWrap.getRequest().getPathInfo();

		if ( "/export".equals(module) ) {
			aWrap.setContentType("text/plain");
			if ( aWrap.hasFormValue("col") )
				out.println(this.exportIsolations(aWrap, aWrap.getFormValue("col")));
			else 
				out.println(this.exportCollections(aWrap));
			out.flush();
//			out.close();
//			this.closeSQL();
			return;
		} else if ( aWrap.hasFormValue("div") ) {
			aWrap.setContentType("text/html");
			String divTag = aWrap.getFormValue("div");
			if ( aWrap.hasFormValue("col") ) {
				Collection aCol = new SQLCollection(aWrap.getSQLDataSource(), aWrap.getFormValue("col"));
				if ( aCol.isLoaded() ) {
					if ( aCol.isAllowed(Role.READ) ) {
						if ( divTag.equals(STRAIN_LIST_DIV_ID) ) {
							StrainForm aForm = new StrainForm(aWrap);
							out.println(aForm.listSpeciesTable(aCol.getStrains()));
						} else if ( divTag.equals(HarvestForm.DIV_ID) ) {
							HarvestForm aForm = new HarvestForm(aWrap);
							out.println(aForm.harvestList(aCol.getHarvest()));
							out.println(String.format("<P ALIGN='CENTER'><A HREF=\"%s/inoc/harvest?col=%s\"/>Add a New Harvest</A></P>", aWrap.getContextPath(), aCol.getID()));
						} else if ( divTag.equals(ISOLATION_DIV_ID)) {
							IsolationForm aForm = new IsolationForm(aWrap);
							out.println(aForm.listIsolations(aCol.getIsolations()));
						} else if ( divTag.equals(STRAIN_LIST_DIV_ID) ) {
							StrainForm aForm = new StrainForm(aWrap);
							out.println(aForm.listSpeciesTable(aCol.getStrains()));
						}
					} else {
						out.print("ACCESS DENIED");
					}
				}
			} else if (aWrap.hasFormValue("id") ) {
				Isolation anIso = new SQLIsolation(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
				if ( anIso.isLoaded() ) {
					if ( anIso.isAllowed(Role.READ) ) {
						if ( divTag.equals(STRAIN_LIST_DIV_ID) ) {
							StrainForm aForm = new StrainForm(aWrap);
							out.println(aForm.listSpeciesTable(anIso.getStrains()));
							out.println(String.format("<P ALIGN='CENTER'><A HREF=\"strain?action=add&culture_source=%s\"/>Add a New Strain</A></P>", anIso.getID()));
						} else if ( divTag.equals(ISOLATION_DIV_ID) ) {
							IsolationForm aForm = new IsolationForm(aWrap);
							out.println(aForm.listIsolations(anIso.getChildren()));
						}
					} else {
						out.print("ACCESS DENIED");
					}
				}
			}
			out.flush();
			return;
		} else if ( aWrap.hasFormValue("kml") ) {
			aWrap.setContentType("application/vnd.google-earth.kml+xml");
			Collection colList = null;
			if ( aWrap.hasFormValue("col") ) {
				Float maxLat = null , minLat = null, maxLong = null, minLong = null;
				if ( aWrap.hasFormValue("minLat") && aWrap.hasFormValue("maxLat") && aWrap.hasFormValue("minLong") && aWrap.hasFormValue("maxLong") ) {
					minLat = Float.parseFloat(aWrap.getFormValue("minLat"));
					maxLat = Float.parseFloat(aWrap.getFormValue("maxLat"));
					minLong = Float.parseFloat(aWrap.getFormValue("minLong"));
					maxLong = Float.parseFloat(aWrap.getFormValue("maxLong"));
				} else {
					Collection center = new SQLCollection(aWrap.getSQLDataSource(), aWrap.getFormValue("col"));
					Float latFloat = center.getLatitudeFloat();
					Float longFloat = center.getLongitudeFloat();
					boolean hasLocation = ( (latFloat != null) && (longFloat != null) );
					if ( hasLocation ) {
						float thisLat = latFloat.floatValue();
						float thisLong = longFloat.floatValue();
						float latRange = 180.0f / 1024.0f;
						float longRange = 360.0f / 1024.0f;
						maxLat = thisLat + latRange; 
						minLat = thisLat - latRange;
						maxLong = thisLong + longRange;
						minLong = thisLong - longRange;
					}
				}
				if ( maxLat != null )
					colList = SQLCollection.collectionsLoacted(aWrap.getSQLDataSource(), minLat, maxLat, minLong, maxLong);
			} else {
				if ( aWrap.hasFormValue("query") && ( ! aWrap.getFormValue("query").equals("") ) ) {
					String query = aWrap.getFormValue("query");
					if ( query.matches("\\\\*") ) query = query.replaceAll("\\\\*", "%");
					else query = "%" + query + "%";
					String[] columns = { aWrap.getFormValue("field") };
					String[] values = { query };
					colList = SQLCollection.collectionsLike(aWrap.getSQLDataSource(), columns, values, SQLCollection.ID_COLUMN, SQLCollection.ASCENDING_SORT);
				} else {
					colList = SQLCollection.collections(aWrap.getSQLDataSource());
				}
			}
			if ( colList != null ) {
				Source kmlSource = new DOMSource(this.getKML(aWrap, colList));
				Result aResult = new StreamResult(out);
				System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
				Transformer xmlTrn = TransformerFactory.newInstance().newTransformer();
				xmlTrn.setOutputProperty("indent", "yes");
				xmlTrn.transform(kmlSource, aResult);
			}
			out.flush();
			return;
		}
		
		aWrap.setContentType("text/html");

		if ( aWrap.hasFormValue("id") )
			out = aWrap.startHTMLDoc("Isolatin Data");
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
			
			} else if ( aWrap.hasFormValue("col") ) {
				Collection aCol = new SQLCollection(aWrap.getSQLDataSource(), aWrap.getFormValue("col"));
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Collection #" + aCol.getID());
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head);

				CollectionForm colForm = new CollectionForm(aWrap);
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
				*/


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
						out.println(String.format("<P ALIGN='CENTER'><A HREF='collection/export?%s'>Export Collection Data</A></P>", aWrap.getRequest().getQueryString()));
						CollectionForm myForm = new CollectionForm(aWrap);
						String query = aWrap.getFormValue("query");
						if ( query.indexOf("*") > -1 ) query = query.replaceAll("\\*", "%");
						else query = "%" + query + "%";
						String[] columns = { aWrap.getFormValue("field") };
						String[] values = { query };
						Collection colList = SQLCollection.collectionsLike(aWrap.getSQLDataSource(), columns, values, SQLCollection.ID_COLUMN, SQLCollection.ASCENDING_SORT);
						CyanosConfig myConf = aWrap.getAppConfig();
						if ( myConf.canMap() ) {
							out.println(myForm.collectionMap(colList, 700, 500));
						}
						out.println(myForm.collectionList(colList));
					}
/*					if ( myConf.canMap() && aWrap.hasFormValue("showMap")) {
						out.println(this.collectionMap(colList));
						out.println("<SCRIPT TYPE='text/javascript'>\n//<![CDATA[\n");
						out.println("function setupControls(map) {");
						out.println(" map.addControl(new GMapTypeControl());\n map.addControl(new GLargeMapControl());\n var bottomRight = new GControlPosition(G_ANCHOR_BOTTOM_RIGHT, new GSize(10,25));\n map.addControl(new GScaleControl(), bottomRight);\n } \n");
						out.println("//]]>\n</SCRIPT>\n");
						out.println(String.format(this.mapCanvas, 700, 500));
					}
					
*/				
				} catch (DataException e) {
					out.println("<P ALIGN='CENTER'><FONT COLOR='red'><B>ERROR:</FONT> " + e.getMessage() + "</B></P>");
				}
			}
		} else if ( module.equals("/add") ) {
			CollectionForm myForm = new CollectionForm(aWrap);
			if ( aWrap.hasFormValue("makeCollection")) {
				out.println(myForm.addCollection());
			} else {
				out.println(myForm.addCollectionForm());
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
*/
		}
		
		aWrap.finishHTMLDoc();
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
	
	private String exportCollections(CyanosWrapper aWrap) {
		try {
			Collection thisCol = null;
			if ( aWrap.hasFormValue("query") && ( ! aWrap.getFormValue("query").equals("")) ) {
				String query = aWrap.getFormValue("query");
				if ( query.matches("\\*") ) query = query.replaceAll("\\*", "%");
				else query = "%" + query + "%";
				String[] likeColumns = { aWrap.getFormValue("field") };
				String[] likeValues = { query };
				thisCol = SQLCollection.collectionsLike(aWrap.getSQLDataSource(), likeColumns, likeValues, SQLCollection.ID_COLUMN, SQLCollection.ASCENDING_SORT);
			} 
			if ( thisCol != null ) {
				List<List> output = new ArrayList<List>();
				List<String> aRow = new ArrayList<String>();
				aRow.add("Collection ID");
				aRow.add("Date");
				aRow.add("Collected by");
				aRow.add("Location Name");
				aRow.add("Latitude");
				aRow.add("Longitude");
				aRow.add("Coordinate Precision");
				aRow.add("Project Code");
				aRow.add("Notes");
				output.add(aRow);
				thisCol.beforeFirst();
				while ( thisCol.next() ) {
					aRow = new ArrayList<String>();
					aRow.add(thisCol.getID());
					aRow.add(thisCol.getDateString());
					aRow.add(thisCol.getCollector());
					aRow.add(thisCol.getLocationName());
					aRow.add(thisCol.getLatitudeDM());
					aRow.add(thisCol.getLongitudeDM());
					aRow.add(String.format("%d m", thisCol.getPrecision()));
					aRow.add(thisCol.getProjectID());
					aRow.add(thisCol.getNotes());
					output.add(aRow);
				}
				return this.delimOutput(output, ",");
			} else {
				return "ERROR: COLLECTIONS NOT FOUND.";
			}
		} catch (DataException e) {
			e.printStackTrace();
			return "ERROR: " + e.getMessage();
		}
	}

	private String exportIsolations(CyanosWrapper aWrap, String collectionID) {
		try {
			Collection thisCol = new SQLCollection(aWrap.getSQLDataSource(), collectionID);
			if ( thisCol.first() ) {
				return "";
			} else {
				return "ERROR: COLLECTION NOT FOUND.";
			}
		} catch (DataException e) {
			e.printStackTrace();
			return "ERROR: " + e.getMessage();
		}
	}
	
	private Map<String,StringBuffer> buildMapMarkers(CyanosWrapper aWrap, Collection aCol) throws DataException {
		Map<String,StringBuffer> retVal = new HashMap<String,StringBuffer>();
		if ( aCol != null && aCol.first() ) {
			aCol.beforeFirst();
			SimpleDateFormat myFormat = aWrap.dateFormat();
			while ( aCol.next() ) {
				Float latFloat = aCol.getLatitudeFloat();
				Float longFloat = aCol.getLongitudeFloat();
				boolean wasNull = ( (latFloat == null) && (longFloat == null) );
				if ( wasNull ) continue;
				double thisLat = latFloat.doubleValue();
				double thisLong = aCol.getLongitudeFloat();
				String key = String.format("%.4f, %.4f", thisLat, thisLong);
				StringBuffer content;
				if ( ! retVal.containsKey(key) ) 
					retVal.put(key, new StringBuffer());
				content = retVal.get(key);
				content.append(String.format("<P><A HREF='?col=%s&showMap=true' CLASS='map'>%s</A> - %s By: %s<BR/></P>", 
						aCol.getID(), aCol.getID(), myFormat.format(aCol.getDate()), aCol.getCollector()));
			}
		}
		return retVal;
	}
	
	private Document getKML(CyanosWrapper aWrap, Collection aCol) throws DataException, ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.newDocument();

		Element kmlRoot = dom.createElement("kml");
		dom.appendChild(kmlRoot);
		kmlRoot.setAttribute("xmlns", "http://earth.google.com/kml/2.2");
		
		Map<String,StringBuffer> markers = this.buildMapMarkers(aWrap, aCol);
		Iterator<String> keyIter = markers.keySet().iterator();
		while ( keyIter.hasNext() ) {
			String key = keyIter.next();
			Element aMarker = dom.createElement("Placemark");
			Element desc = dom.createElement("description");
			aMarker.appendChild(desc);
			desc.setTextContent(markers.get(key).toString());

			Element point = dom.createElement("Point");
			aMarker.appendChild(point);
			Element coord = dom.createElement("coordinate");
			coord.setTextContent(key);
			point.appendChild(coord);
			
			kmlRoot.appendChild(aMarker);
		}
		
		return dom;
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
	
	@Override
	protected String getHelpModule() {
		return HELP_MODULE;
	}
		

}
