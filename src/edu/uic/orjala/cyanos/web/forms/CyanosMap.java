package edu.uic.orjala.cyanos.web.forms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;

public abstract class CyanosMap extends BaseForm {

	protected Collection colList;

	public abstract String collectionMap();

	protected int divWidth = 0;
	protected int divHeight = 0;
	protected double latCenter = -1000.0;
	protected double longCenter = -1000.0;
	protected long zoomLevel = -1;

	public CyanosMap(CyanosWrapper callingServlet) {
		super(callingServlet);
	}
	
	public CyanosMap(CyanosWrapper callingServlet, Collection cols) {
		super(callingServlet);
		this.colList = cols;
	}
	
	public CyanosMap(CyanosWrapper callingServlet, Collection cols, int width, int height, double latitude, double longitude, long zoom) {
		super(callingServlet);
		this.colList = cols;
		this.divWidth = width;
		this.divHeight = height;
		this.latCenter = latitude;
		this.longCenter = longitude;
		this.zoomLevel = zoom;
	}

	/** 
	 * Set the dimensions of the map DIV.
	 *
	 * @param width Width of the DIV in pixels
	 * @param height Height of the DIV in pixels
	 */
	public void setDIVSize(int width, int height) {
		this.divWidth = width;
		this.divHeight = height;
	}

	/** 
	 * Set the center of the map.
	 *
	 * @param latitude latitude of the center of the map.
	 * @param longitude longitude of the center of the map.
	 */
	public void setMapCenter(double latitude, double longitude) {
		this.latCenter = latitude;
		this.longCenter = longitude;
	}

	/**
	 * Set the zoom level of the map.
	 *
	 * @param zoom the level of the zoom.
	 */
	public void setMapZoom(long zoom) {
		this.zoomLevel = zoom;
	}

	/**
	 * Generate the HTML DIV for the map content.  This method with utilize the button to display the Loading... dialog.
	 *
	 * @return the map DIV
	 */
	public Div mapDiv() {
		Div mapDiv = new Div(this.collectionMap());
		Div canvasDiv = new Div("<P ALIGN='CENTER'><BUTTON STYLE='margin-top:30%' onClick='this.innerHTML=\"Loading...\"; this.enable=false; window.setTimeout(setupMap, 100, document.getElementById(\"map_canvas\"));' TYPE=BUTTON>View Map</BUTTON></P>");
		canvasDiv.setID("map_canvas");
		canvasDiv.setAttribute("style", String.format("width: %dpx; height: %dpx; margin: 0 auto; border: 1px solid gray; background-color: #FCFCFC;", this.divWidth, this.divHeight));
		mapDiv.addItem(canvasDiv);
		return mapDiv;
	}

	/**
	 * Generate the HTML DIV for the map content.  This method with utilize a hidden DIV to display the loading dialog.
	 *
	 * @param mapContent the map content (HTML and Javascript)
	 * @return the map DIV
	 */
	public Div hiddenDiv() {
		Div mapDiv = new Div(this.collectionMap());
		mapDiv.setAttribute("style", String.format("width: %dpx; margin: 0 auto; border: 1px solid gray; background-color: #FCFCFC;", this.divWidth));
		Div showDiv = new Div("<P ALIGN='CENTER' STYLE='margin-top:30%'><BUTTON onClick='this.innerHTML=\"Loading...\"; this.enabled=false; document.getElementById(\"map_hide\").className = \"showSection\"; document.getElementById(\"map_show\").className = \"hideSection\"; window.setTimeout(setupMap, 100, document.getElementById(\"map_canvas\"));' TYPE=BUTTON>View Map</BUTTON></P>");
		showDiv.setID("map_show");
		showDiv.setAttribute("STYLE", String.format("margin: 0 auto; width: %dpx;", this.divWidth));
		mapDiv.addItem(showDiv);
		
		Div canvasDiv = new Div("<P ALIGN=CENTER STYLE='margin-top:30%'>Loading...</P>");
		canvasDiv.setID("map_canvas");
		canvasDiv.setAttribute("style", String.format("width: %dpx; height: %dpx; margin: 0 auto;", this.divWidth, this.divHeight));
		Div hidingDiv = new Div(canvasDiv);
		hidingDiv.setID("map_hide");
		hidingDiv.setClass("hideSection");
		mapDiv.addItem(hidingDiv);
		return mapDiv;
	}
	
	/**
	 * Build the list of the map markers.
	 *
	 * @param aCol list of collections
	 */
	protected Map<String,List<String>> buildMapMarkers(Collection aCol) throws DataException {
		Map<String,List<String>> retVal = new HashMap<String,List<String>>();
		if ( aCol != null && aCol.first() ) {
			double minLat = 90.0f, maxLat = -90.0f;
			double minLong = 180.0f, maxLong = -180.0f;
			aCol.beforeFirst();
			SimpleDateFormat myFormat = this.dateFormat();
			while ( aCol.next() ) {
				Float latFloat = aCol.getLatitudeFloat();
				Float longFloat = aCol.getLongitudeFloat();
				boolean wasNull = ( (latFloat == null) || (longFloat == null) );
				if ( wasNull ) continue;
				double thisLat = latFloat.doubleValue();
				double thisLong = longFloat.doubleValue();
				String key = String.format("%.4f, %.4f", thisLat, thisLong);
				List<String> aList;
				if ( retVal.containsKey(key) ) {
					aList = retVal.get(key);
				} else {
					aList = new ArrayList<String>();
				}
				String content = String.format("<P><A HREF='%s/collection?col=%s' CLASS='map'>%s</A> - %s By: %s<BR/></P>", 
						this.myWrapper.getContextPath(), aCol.getID(), aCol.getID(), myFormat.format(aCol.getDate()), aCol.getCollector());
				aList.add(content);
				retVal.put(key,aList);
				if ( thisLat > maxLat ) maxLat = thisLat;
				if ( thisLat < minLat ) minLat = thisLat;
				if ( thisLong > maxLong ) maxLong = thisLong;
				if ( thisLong < minLong ) minLong = thisLong;
			}
			double latSize = Math.abs(maxLat - minLat);
			double longSize = Math.abs(maxLong - minLong);
	
			if ( latCenter > -1000 && longCenter > -1000 ) {
			this.latCenter = minLat + ( latSize / 2 );
			this.longCenter = minLong + ( longSize / 2 );
			}
	
			if ( this.zoomLevel > -1 ) {
			double logBase = Math.log(2);
			double latZoom = Math.log(180 / latSize) / logBase;
			double longZoom = Math.log(360 / longSize) / logBase;
			zoomLevel = (latZoom < longZoom ? Math.round(latZoom) : Math.round(longZoom));
			}
		}
		
		return retVal;
	}

}