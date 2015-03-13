var select, map;

function setupOLMap(canvas) {
	canvas.innerHTML = ''; 
	map = new OpenLayers.Map({div: canvas,
//		projection: "EPSG:900913"
		projection: "EPSG:4326"
//	    numZoomLevels: 24,
		// , allOverlays: true
	});

	
	/*
	var TILEMATRIXSET_GEO_250m = "EPSG4326_250m";
	var TILEMATRIXSET_GEO_500m = "EPSG4326_500m";
	var TILEMATRIXSET_GEO_1km = "EPSG4326_1km";
	var TILEMATRIXSET_GEO_2km = "EPSG4326_2km";
	var GIBS_WMTS_GEO_ENDPOINT = "http://map1.vis.earthdata.nasa.gov/wmts-geo/wmts.cgi";
	
	var layerModisTerraTrueColor = new OpenLayers.Layer.WMTS({
		name: "Terra / MODIS Corrected Reflectance (True Color), 2012-06-08",
		url: GIBS_WMTS_GEO_ENDPOINT,
		layer: "MODIS_Terra_CorrectedReflectance_TrueColor",
		matrixSet: TILEMATRIXSET_GEO_250m,
		format: "image/jpeg",
		style: "",
		transitionEffect: "resize",
		projection: "EPSG:4326",
		numZoomLevels: 9,
		maxResolution: 0.5625,
		'tileSize': new OpenLayers.Size(512, 512),
		isBaseLayer: true
	});
	*/

/*
	// Create overlays
	var layerModisAerosolOpticalDepth = new OpenLayers.Layer.WMTS({
		name: "Terra / MODIS Aerosol Optical Depth, 2012-06-08",
		url: GIBS_WMTS_GEO_ENDPOINT,
		layer: "MODIS_Terra_Aerosol",
		matrixSet: TILEMATRIXSET_GEO_2km,
		format: "image/png",
		style: "",
		transitionEffect: "resize",
		projection: "EPSG:4326",
		numZoomLevels: 9,
		maxResolution: 0.5625,
		'tileSize': new OpenLayers.Size(512, 512),
		isBaseLayer: false,
		visibility: false
	});
*/
	// The "time" parameter isn't being included in tile requests to the server
	// in the current version of OpenLayers (2.12);  need to use this hack
	// to force the inclusion of the time parameter.
	//
	// If the time parameter is omitted, the current (UTC) day is retrieved
	// from the server
//	layerModisTerraTrueColor.mergeNewParams({time:"2012-06-08"});
//	layerModisTerra721.mergeNewParams({time:"2012-06-08"});
//	layerModisAerosolOpticalDepth.mergeNewParams({time:"2012-06-08"});
//	layerAirsDustScore.mergeNewParams({time:"2012-06-08"});
	
//	map.addLayers([layerModisTerraTrueColor, layerModisTerra721,
//	           	layerModisAerosolOpticalDepth, layerAirsDustScore]);

//	map.addLayers([layerModisTerraTrueColor]);

	map.addControl( new OpenLayers.Control.LayerSwitcher() );
	
	
//	var layer = new OpenLayers.Layer.Google("Google Physical", 
//			{type: google.maps.MapTypeId.TERRAIN}); 
//	map.addLayer(layer);
//	layer.setIsBaseLayer(true); 
	
//	layer = OpenLayers.Layer.WMS("NASA Global Mosaic", "http://wms.jpl.nasa.gov/wms.cgi"
//	layer = OpenLayers.Layer.WMS("NASA Global Mosaic", "http://map1.vis.earthdata.nasa.gov/wmts-geo/wmts.cgi");
//		, {layers: "modis,global_mosaic"}
	 
//	map.addLayer(layer);
//	layer.setIsBaseLayer(true); 

	return map;
}

function addNOAALayers(map) {
	map.addLayers([
	               new OpenLayers.Layer.ArcGIS93Rest("National Geographic", 
	            		   "http://services.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/export", 
	            		   {layers: "show:0"}, {projection: "EPSG:3857"}),
	    	       new OpenLayers.Layer.ArcGIS93Rest("NGS Topographic", 
	    	    		   "http://services.arcgisonline.com/ArcGIS/rest/services/NGS_Topo_US_2D/MapServer/export",
	    	            	{layers: "show:0"}, {projection: "EPSG:3857"}),
//	    	       new OpenLayers.Layer.ArcGIS93Rest("World Physical Map", 
//	    	    		   "http://services.arcgisonline.com/ArcGIS/rest/services/World_Physical_Map/MapServer/export",	
//	   	            	 	{layers: "show:0"}, {projection: "EPSG:3857"}),
//	   	           new OpenLayers.Layer.ArcGIS93Rest("World Image", 
//	   		 	   	           "http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/export",
//	   		   	            	 	{}, {projection: "EPSG:3857"}),
	    	       	   
	               ]);
	// , {projection: "EPSG:4326"}
}

function addGoogleLayers(map) {
	map.addLayers([
	        new OpenLayers.Layer.Google("Google Physical", 
	        		{type: google.maps.MapTypeId.TERRAIN}), 
			new OpenLayers.Layer.Google("Google Streetmap", 
					{numZoomLevels: 20}),
			new OpenLayers.Layer.Google("Google Hybrid", 
					{type: google.maps.MapTypeId.HYBRID, numZoomLevels: 20}),
			new OpenLayers.Layer.Google("Google Satellite", 
					{type: google.maps.MapTypeId.SATELLITE, numZoomLevels: 22})	
	]);
}

function addNASALayers(map) {
	var gm_layer = new OpenLayers.Layer.WMS("NASA Blue Marble",
			"http://maps.opengeo.org/geowebcache/service/wms",
			{layers : "bluemarble"}, {projection: "EPSG:900913"});
	map.addLayer(gm_layer);
	
	addNOAALayers(map);
	
	/*
	var layer = new OpenLayers.Layer.WMS("Blue Marble","http://maps.opengeo.org/geowebcache/service/wms?TILED=true&",
			{layers : "bluemarble", format: "image/png"});
	map.addLayers([
	     layer
/*	     new OpenLayers.Layer.WMS("NASA BlueMarble", "http://wms.jpl.nasa.gov/wms.cgi", 
	    		 {transitionEffect: "resize",	numZoomLevels: 9, layers: "BMNG", 
	        		format: "image/jpeg", projection: "EPSG:4326", style: ""})

	]);
*/
}

function addOSMLayers(map) {
//	var layer = new OpenLayers.Layer.OSM('Open Street Maps'); 
//	map.addLayer(layer);
//	layer.setIsBaseLayer(true); 
	map.addLayers([new OpenLayers.Layer.OSM("OpenStreet Map"),
	               new OpenLayers.Layer.OSM('Open Cycle Terrain', 
	            		   [ "http://a.tile3.opencyclemap.org/landscape/${z}/${x}/${y}.png", 
	            		     "http://b.tile3.opencyclemap.org/landscape/${z}/${x}/${y}.png", 
	            		     "http://c.tile3.opencyclemap.org/landscape/${z}/${x}/${y}.png" ], 
	            		   {projection: "EPSG:900913"})]);
//	layer.setIsBaseLayer(true); 		
}

function addMapQuestLayers(map) {
//	var layer = new OpenLayers.Layer.OSM('Open Street Maps'); 
//	map.addLayer(layer);
//	layer.setIsBaseLayer(true); 

	var mqOSM = ["http://otile1.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.jpg",
                    "http://otile2.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.jpg",
                    "http://otile3.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.jpg",
                    "http://otile4.mqcdn.com/tiles/1.0.0/map/${z}/${x}/${y}.jpg"];
    var mqAerial = ["http://otile1.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.jpg",
                        "http://otile2.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.jpg",
                        "http://otile3.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.jpg",
                        "http://otile4.mqcdn.com/tiles/1.0.0/sat/${z}/${x}/${y}.jpg"];
	
	map.addLayers([new OpenLayers.Layer.OSM('MapQuest Standard',mqOSM),
	            	new OpenLayers.Layer.OSM('MapQuest Satellite',mqAerial)]);
//	layer.setIsBaseLayer(true); 		
}

function getColor(feature, focusColor, otherColor){
	var color = otherColor;
	var attr = feature.attributes;
	if ( attr.styleUrl && attr.styleUrl == "#focusMark" ) {
		color = focusColor;
	} else if(feature.cluster) {
		for (var i = 0; i < feature.cluster.length; i++) {
			var attr = feature.cluster[i].attributes;
			if ( attr.styleUrl && attr.styleUrl == "#focusMark" ) {
				color = focusColor;
			}
		}
	}
	return color;
}

function addCollectionLayer(map, url) {
	var context = {
			getStrokeColor: function(feature){
				return getColor(feature, '#FF0000', '#a00000');
			},
			getFillColor: function(feature){
				return getColor(feature, '#FFA000', '#FF0000');
			}
	};

	var styles = new OpenLayers.StyleMap({
		'default': new OpenLayers.Style({
			pointRadius: 5,
			fillColor: "${getFillColor}",
			fillOpacity: 0.8,
			strokeColor: "${getStrokeColor}",
            strokeWidth: 1,
			strokeOpacity: 1.0,
			graphicZIndex: 1
		}, { context: context}),
        'select' : new OpenLayers.Style({
        	pointRadius: 5,
        	fillColor: "#FFFF00",
            fillOpacity: 1,
            strokeColor: "#A0A000",
            strokeWidth: 1,
            strokeOpacity: 1,
            graphicZIndex: 2
        })

	});
	
	var layer = new OpenLayers.Layer.Vector('Collections', {  
			styleMap: styles,
			strategies: [new OpenLayers.Strategy.BBOX(), new OpenLayers.Strategy.Cluster()], 
			projection: "EPSG:4326",
			protocol: new OpenLayers.Protocol.HTTP({ url: url, 
				format: new OpenLayers.Format.KML({
                    extractStyles: true, 
                    extractAttributes: true, 
                    maxDepth: 2
                   	})
			}) 
	});
	map.addLayer(layer);
	select = new OpenLayers.Control.SelectFeature(layer);
    
    layer.events.on({
        "featureselected": onKMLSelect,
        "featureunselected": onKMLUnselect
    });
    map.addControl(select);
    select.activate();    
}

function setMapBounds(bounds) {
	var toProjection = map.getProjectionObject();
	if ( toProjection.getCode() === "EPSG:4326") {
		map.zoomToExtent(bounds);		
	} else {
		var fromProjection = new OpenLayers.Projection("EPSG:4326");
		map.zoomToExtent(bounds.transform(fromProjection, toProjection));		
	}
}

function onKMLSelect(event) {
    var feature = event.feature;
    // Since KML is user-generated, do naive protection against
    // Javascript.
    var content = "";
    if ( feature.cluster ) {
    	var list = feature.cluster;
    	if ( list.length == 1 ) {
            content = "<h2>"+ list[0].attributes.name + "</h2>" + list[0].attributes.description;    		
    	} else {
        	content = "<H2>" + list.length.toString() + " Collections</H2>";
        	for ( var i = 0; i < list.length; i++ ) {
        		content = content + list[i].attributes.description + "<BR>";
        	}    		
    	}
    } else {
        content = "<h2>"+ feature.attributes.name + "</h2>" + feature.attributes.description;
    }
    if (content.search("<script") != -1) {
        content = "Content contained Javascript! Escaped content below.<br>" + content.replace(/</g, "&lt;");
    }
    popup = new OpenLayers.Popup.FramedCloud("collection", 
                             feature.geometry.getBounds().getCenterLonLat(),
                             new OpenLayers.Size(100,100),
                             content,
                             null, true, onMapPopupClose);
    feature.popup = popup;
    map.addPopup(popup);
}

function onMapPopupClose(evt) {
    select.unselectAll();
}

function onKMLUnselect(event) {
    var feature = event.feature;
    if(feature.popup) {
        map.removePopup(feature.popup);
        feature.popup.destroy();
        delete feature.popup;
    }
}