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
import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.servlet.CollectionServlet;

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
	 * @throws ConfigException 
	 */
	private String collectionMap(Map<String,List<String>> markers) throws ConfigException {
		StringBuffer output = new StringBuffer();
		output.append("<SCRIPT TYPE='text/javascript'>\n//<![CDATA[\n");
/*		
 * 		REPLACED WITH KML INTERFACE
 * 
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
	*/
		output.append(this.buildMapSetup());
		output.append("//]]>\n</SCRIPT>\n");
		return output.toString();
	}
	
	private String buildMapSetup() throws ConfigException {
		StringBuffer output = new StringBuffer();
		output.append("function setupMap(canvas) { var map = setupOLMap(canvas);\n var layer;\n");
		AppConfig myConf = this.myWrapper.getAppConfig();
		Map<String,String> layers = myConf.getMapServerLayers();
		
		if ( myConf.getMapParameter(AppConfig.MAP_OSM_LAYER) != null ) {
			output.append("addOSMLayers(map);\n");
		}

		if ( myConf.getMapParameter(AppConfig.MAP_NASA_LAYER) != null ) {
			output.append("addNASALayers(map);\n");
		}

		Set<String> keys = new TreeSet<String>(layers.keySet());
		Iterator<String> keyIter = keys.iterator();
		while ( keyIter.hasNext() ) {
			String layerName = keyIter.next();
			String layerURL = layers.get(layerName);
			if ( layerURL == null ) continue;
			output.append(String.format(" layer = new OpenLayers.Layer.Mapserver( \"%s\", \"%s\", {}, {gutter: 15});\n", layerName, layerURL));
			output.append(" layer.setIsBaseLayer(true); layer.setVisibility(false);\n");
			output.append(" map.addLayer(layer);\n");
		}	
		String googleMapKey = myConf.getGoogleMapKey();
		if ( googleMapKey != null) {
			output.append(" addGoogleLayers(map);");
		}
		output.append(" addCollectionLayer(map, \"");
		output.append(CollectionServlet.getKMLURL(myWrapper.getRequest()));
		output.append("\");\n");
		
		if ( this.longCenter > -1000.0f && this.latCenter > -1000.0f ) {
			output.append(String.format(" map.setCenter(new OpenLayers.LonLat(%.4f, %.4f), %d);\n", this.longCenter, this.latCenter, this.zoomLevel));			
		} else {
			double maxLong = -200; double minLong = 200;
			double maxLat = -100; double minLat = 100;

			try {
				this.colList.beforeFirst();
				while ( colList.next() ) {
					Float latitude = colList.getLatitudeFloat();
					Float longitude = colList.getLongitudeFloat();
					if ( latitude == null || longitude == null ) continue;
					if ( longitude > maxLong )
						maxLong = longitude.doubleValue();
					if ( longitude < minLong )
						minLong = longitude.doubleValue();
					if ( latitude > maxLat ) 
						maxLat = latitude.doubleValue();
					if ( latitude < minLat ) 
						minLat = latitude.doubleValue();
				}
				this.longCenter = ((maxLong - minLong) / 2) + minLong;
				this.latCenter = ((maxLat - minLat) / 2) + minLat;
				if ( (maxLong - minLong) < (360.0 / 256) ) {
					double half = (360 / 512);
					maxLong = this.longCenter + half;
					minLong = this.longCenter - half;
				}
				
				if ( (maxLat - minLat) < (180.0 / 256) ) {
					double half = (180.0 / 512);
					maxLat = this.latCenter + half;
					minLat = this.latCenter - half;
				}
				
//				output.append(String.format(" map.setCenter(new OpenLayers.LonLat(%.4f, %.4f));\n", this.longCenter, this.latCenter));
//				output.append(String.format(" var defaultBounds = new OpenLayers.Bounds(%.4f, %.4f, %.4f, %.4f);\n", minLong, minLat, maxLong, maxLat));				
//				output.append(" map.setCenter(defaultBounds.getCenterLonLat(), 10);\n");
				output.append(String.format(" setMapBounds(new OpenLayers.Bounds(%.4f, %.4f, %.4f, %.4f));\n", minLong, minLat, maxLong, maxLat));				
			} catch (DataException e) {
				e.printStackTrace();
				output.append(" map.setCenter(new OpenLayers.LonLat(0, 0), 1);\n");
			}
		}

		
		
	//	output.append(" map.render(canvas);\n");
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
