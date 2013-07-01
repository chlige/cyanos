/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.web.CyanosConfig;
import edu.uic.orjala.cyanos.web.CyanosWrapper;

/**
 * @author George Chlipala
 *
 */
public class OpenLayersMap extends CyanosMap {

	public OpenLayersMap(CyanosWrapper callingServlet) {
		super(callingServlet);
	}
	
	/**
	 * @param cols Collections to map
	 */
	public OpenLayersMap(CyanosWrapper callingServlet, Collection cols) {
		super(callingServlet, cols);
	}

	public OpenLayersMap(CyanosWrapper callingServlet, Collection cols, int width, int height, double latitude, double longitude, long zoom) {
		super(callingServlet, cols, width, height, latitude, longitude, zoom);
	}

	/**
	 * Create the HTML code (including Javascript) for the map.
	 *
	 * @param aCollection collections to map
	 */
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
		markerScript.append(" markers = new OpenLayers.Layer.Markers(\"Collections\");\n");
		markerScript.append(" map.addLayer(markers);\n");
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
		output.append("function setupMap(canvas) { \n var map = new OpenLayers.Map(canvas);\n");
		CyanosConfig myConf = this.myWrapper.getAppConfig();
		Map<String,String> layers = myConf.getMapServerLayers();
		Set<String> keys = new TreeSet<String>(layers.keySet());
		Iterator<String> keyIter = keys.iterator();
		while ( keyIter.hasNext() ) {
			String layerName = keyIter.next();
			String layerURL = layers.get(layerName);
			output.append(String.format(" layer = new OpenLayers.Layer.Mapserver( \"%s\", \"%s\", {}, {gutter: 15});\n", layerName, layerURL));
			output.append(" map.addLayer(layer);\n");
		}	
		output.append(" addCollectionMarkers(map);\n");
		output.append(String.format(" map.setCenter(new OpenLayers.LonLat(%.4f, %.4f), %d);\n", this.longCenter, this.latCenter, this.zoomLevel));
		output.append(" map.addControl( new OpenLayers.Control.LayerSwitcher() );\n");
		output.append(" canvas.innerHTML = '';\n");
		output.append(" map.render(canvas);\n");
		output.append("}\n");
		
		
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
		Iterator<String> markerIter = markerList.iterator();
		StringBuffer content = new StringBuffer();
		while ( markerIter.hasNext() ) {
			content.append(markerIter.next());
		}
		markerScript.append(String.format(" aCol = makeOLMarker(OLLatLong(%s), \"%s\");\n", coordinate, content.toString()));
		markerScript.append(" markers.addMarker(aCol);\n");
		return markerScript.toString();
	}
}
