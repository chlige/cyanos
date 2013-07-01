/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;

/**
 * @author George Chlipala
 *
 */
public class GoogleMap extends CyanosMap {

	public GoogleMap(CyanosWrapper callingServlet) {
		super(callingServlet);
	}
	
	/**
	 * @param cols Collections to map
	 */
	public GoogleMap(CyanosWrapper callingServlet, Collection cols) {
		super(callingServlet, cols);
	}

	public GoogleMap(CyanosWrapper callingServlet, Collection cols, int width, int height, double latitude, double longitude, long zoom) {
		super(callingServlet, cols, width, height, latitude, longitude, zoom);
	}

	/**
	 * Generate the HTML DIV for the map content.  This method with utilize the button to display the Loading... dialog.
	 *
	 * @return the map DIV
	 */
	@Override
	public Div mapDiv() {
		Div mapDiv = new Div(this.collectionMap());
		mapDiv.addItem("<SCRIPT TYPE='text/javascript'>\n//<![CDATA[\n");
		mapDiv.addItem("function setupControls(map) {");
		mapDiv.addItem(" map.addControl(new GMapTypeControl());\n map.addControl(new GSmallMapControl()); \n");
		mapDiv.addItem(" var bottomRight = new GControlPosition(G_ANCHOR_BOTTOM_RIGHT, new GSize(10,25));\n map.addControl(new GScaleControl(), bottomRight);\n } \n");
		mapDiv.addItem("//]]>\n</SCRIPT>\n");
		Div canvasDiv = new Div("<P ALIGN='CENTER'><BUTTON STYLE='margin-top:30%' onClick='this.innerHTML=\"Loading...\"; this.enable=false; window.setTimeout(setupMap, 100, document.getElementById(\"map_canvas\"));' TYPE=BUTTON>View Map</BUTTON></P>");
		canvasDiv.setID("map_canvas");
		canvasDiv.setAttribute("style", String.format("width: %dpx; height: %dpx; margin: 0 auto; border: 1px solid gray; background-color: #FCFCFC;", this.divWidth, this.divHeight));
		mapDiv.addItem(canvasDiv);
		return mapDiv;
	}

	/**
	 * Generate the HTML DIV for the map content.  This method with utilize a hidden DIV to display the loading dialog.
	 *
	 * @return the map DIV
	 */
	@Override
	public Div hiddenDiv() {
		Div mapDiv = new Div(this.collectionMap());
		mapDiv.addItem("<SCRIPT TYPE='text/javascript'>\n//<![CDATA[\n");
		mapDiv.addItem("function setupControls(map) {");
		mapDiv.addItem(" map.addControl(new GMapTypeControl());\n map.addControl(new GSmallMapControl()); \n");
		mapDiv.addItem(" var bottomRight = new GControlPosition(G_ANCHOR_BOTTOM_RIGHT, new GSize(10,25));\n map.addControl(new GScaleControl(), bottomRight);\n } \n");
		mapDiv.addItem("//]]>\n</SCRIPT>\n");
		Div showDiv = new Div("<P ALIGN='CENTER'><BUTTON onClick='this.innerHTML=\"Loading...\"; this.enabled=false; document.getElementById(\"map_hide\").className = \"showSection\"; document.getElementById(\"map_show\").className = \"hideSection\"; window.setTimeout(setupMap, 100, document.getElementById(\"map_canvas\"));' TYPE=BUTTON>View Map</BUTTON></P>");
		showDiv.setID("map_show");
		showDiv.setAttribute("STYLE", String.format("margin: 0 auto; width: %dpx; border: 1px solid gray; background-color: #FCFCFC;", this.divWidth));
		mapDiv.addItem(showDiv);
		
		Div canvasDiv = new Div("<P ALIGN=CENTER STYLE='margin-top:30%'>Loading...</P>");
		canvasDiv.setID("map_canvas");
		canvasDiv.setAttribute("style", String.format("width: %dpx; height: %dpx; margin: 0 auto; border: 1px solid gray; background-color: #FCFCFC;", this.divWidth, this.divHeight));
		Div hidingDiv = new Div(canvasDiv);
		hidingDiv.setID("map_hide");
		hidingDiv.setClass("hideSection");
		mapDiv.addItem(hidingDiv);
		return mapDiv;
	}

	/**
	 * Create the HTML code (including Javascript) for the map.
	 *
	 * @param aCollection collections to map
	 */
	@Override
	public String collectionMap() {		
		this.zoomLevel = ( this.zoomLevel > 19 ? 19 : this.zoomLevel );
		try {
			if ( colList.first() ) {
				return this.collectionMap(this.buildMapMarkers(colList));
			} else {
				return "";
			}
		} catch ( DataException e ) {
			return this.handleException(e);
		}
	}
	
	/**
	 * Create the HTML code (including Javascript) for the map. This will build the code using the Google Map API.
	 *
	 * @param markers makers to map
	 */
	private String collectionMap(Map<String,List<String>> markers) {
		StringBuffer output = new StringBuffer();
		output.append("<SCRIPT TYPE='text/javascript'>\n//<![CDATA[\n");
		StringBuffer markerScript = new StringBuffer();
		markerScript.append("function addCollectionMarkers(map) {\n");
		Iterator<String> keyIter = markers.keySet().iterator();
		while ( keyIter.hasNext() ) {
			String key = keyIter.next();
			markerScript.append(this.createMarker(key, markers.get(key)));
		}
		markerScript.append("}\n");
		output.append(markerScript);
		output.append(this.buildMapSetup());
		output.append("//]]>\n</SCRIPT>\n");
		return output.toString();
	}
	
	private String buildMapSetup() {
		StringBuffer output = new StringBuffer();
		output.append("function setupMap(canvas) { \n var map = new GMap2(canvas);\n");
		output.append(String.format(" map.setCenter(new GLatLng(%.4f, %.4f), %d);\n", this.latCenter, this.longCenter, this.zoomLevel));
		output.append(" map.removeMapType(G_HYBRID_MAP);\n map.addMapType(G_PHYSICAL_MAP);\n map.setMapType(G_PHYSICAL_MAP);\n");
		output.append(" addCollectionMarkers(map);\n");
		output.append(" setupControls(map);\n }\n");
		return output.toString();
	}

	/**
	 * Create the Javascript for the marker.
	 *
	 * @param coordinate coordinate of the marker, as a String.
	 * @param makerList list of maker details.
	 * @return Javascript code for the maker.
	 */
	private String createMarker(String coordinate, List<String> markerList) {
		StringBuffer markerScript = new StringBuffer();
		markerScript.append(String.format(" aCol = new GMarker(new GLatLng(%s), {title: \"%d Collections\"});\n", coordinate, markerList.size()));
		Iterator<String> markerIter = markerList.iterator();
		StringBuffer content = new StringBuffer();
		while ( markerIter.hasNext() ) {
			content.append(markerIter.next());
		}
		markerScript.append(String.format(" aCol.bindInfoWindowHtml(\"%s\");\n", content.toString()));
		markerScript.append(" map.addOverlay(aCol);\n");
		return markerScript.toString();
	}

}
