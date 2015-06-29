var closeLSdiv = true;

function setLoc(label) {
	document.plate.location.value = label;
}

function addSample(col_id, col_type) {
	var request = "add-sample.cgi?col_id=" + escape(col_id) + "&col_type=" + escape(col_type);
	var sampleWin = window.open(request, "add-sample", "height=450,width=400"); 
	sampleWin.mainWindow = self; 
}

function printPlate(col_id) {
	var request = "cols.cgi?plate=" + escape(col_id) + "&action=print";
	var sampleWin = window.open(request, "printPlate", "height=500,width=1000"); 
	sampleWin.mainWindow = self; 
}

function closeSmallWindow() {
	window.opener.location.reload();
	window.close();
}

function selectDate(date_field) {
	var contextPath = "/cyanos/";
	// if ( contextPath === undefined ) { var contextPath = ""; }
	var request = contextPath + "cal?update_field=" + escape(date_field.form.name + "." + date_field.name);
	calWin = window.open(request, "calendar", "height=200,width=350");  
	calWin.updateField = date_field;
}

function dataForm(table, id) {
	var request = "/cyanos/file?id=" + id + "&class=" + table;
	fileWin = window.open(request, "file", "height=500,width=400,scrollbars=yes");
	fileWin.opener = window;
}

function dataFormType(table, id, dataType) {
	var request = "/cyanos/file?id=" + id + "&class=" + table + "&type=" + dataType;
	fileWin = window.open(request, "file", "height=500,width=400,scrollbars=yes");
	fileWin.opener = window;
}

function queueForm(table, id) {
	var request = "/cyanos/queue/add?id=" + id + "&class=" + table;
	fileWin = window.open(request, "queue", "height=500,width=400,scrollbars=yes");
	fileWin.opener = window;
}

function subscribeQueue(type, name, user) {
	var request = "/cyanos/queue/add?addAction=1&queue_type=user&class=queue&id=" + type + "/" + name + "&queue=" + user;
	fileWin = window.open(request, "queue", "height=500,width=400");
	fileWin.opener = window;
}
	
function compoundForm(id) {
	var request = "/cyanos/compound/link?sample_id=" + id;
	fileWin = window.open(request, "compound", "height=500,width=600");
	fileWin.opener = window;
}

function setLink(table,id) {
	var request = "link?table=" + table + "&id=" + id;
	calWin = window.open(request, "link", "height=150,width=350");
	fileWin.opener = window;
}

function selectLoc(loc_field) {
	var request = "plate?update_field=" + escape(loc_field.form.name + "." + loc_field.name);
	plateWin = window.open(request, "plate", "height=275,width=390");  
	plateWin.updateField = loc_field;
}

function setField(new_value) {
	var updateField = eval('window.opener.document.' + window.document.forms[0].update_field.value);
	updateField.value = new_value;
	window.close();
}

function debug() {
		var debugOutput = document.location + "\n";
		for ( var i = 0; i < document.forms.length; i++ ) {
			debugOutput += i + ". " + document.forms[i].name + "\n";
		}
		alert(debugOutput);
}

function showCompound(cmpdID, width, height) {
	var request = "/cyanos/compound?graphic=png&id="+ cmpdID + "&width=" + width + "&height=" + height;
	window.open(request, "compound", "height=" + height + ",width=" + width);
}

function exportCompound(cmpdID, type) {
	var request = "/cyanos/compound/export/"+ cmpdID + type;
	window.open(request, "compound", "height=350,width=250");
}

function setVals(anArray, src, dst) {
	var aKey = src.options[src.selectedIndex].value;
	var i = 0;
	for ( i=0; i < dst.options.length; i++ ) {
		dst.options[i] = null;
	}
	for ( i = 0; i < anArray[aKey].length; i++ ) {
		dst.options[i] = new Option(anArray[aKey][i], anArray[aKey][i]);
	}
}
/*
function setupMenu(aList) {
	var items = aList.childNodes;
	for ( var i = 0; i < items.length; i++ ) {
		if ( items[i].addEventListener ) {
			items[i].addEventListener("click", toggleMenu, false);
//			items[i].addEventListener("mouseover", showHide, false);
		} else if ( items[i].attachEvent ) {
			items[i].attachEvent("click", toggleMenu);
		}
		items[i].shown = false;
	}
}

function toggleMenu(evObj) {
	var anItem = evObj.currentTarget;
	var isShown = anItem.shown;
	hideItems( anItem.parentNode );
	if ( ! isShown ) {
		var subList = anItem.getElementsByTagName("ul");
		for ( var x = 0; x < subList.length; x++ ) {
			subList[x].className='menushow';
		}
		anItem.shown = true;
	}
	anItem.blur();
}

function showHide(evObj) {
	var anItem = evObj.currentTarget;
	hideItems( anItem.parentNode );
	var subList = anItem.getElementsByTagName("ul");
	for ( var x = 0; x < subList.length; x++ ) {
		subList[x].className='menushow';
	}
	anItem.shown = true;
}

function hideItems(aList) {
	var items = aList.childNodes;
	for ( var i = 0; i < items.length; i++ ) {
		if ( items[i].shown ) {
			items[i].shown = false;
			var subList = items[i].getElementsByTagName("ul");
			for ( var x = 0; x < subList.length; x++ ) {
				subList[x].className='menuhide';
			}
		}
	}
}
*/

function toggleMenu(id) {
	var thisMenu = document.getElementById(id);
	var thisSubMenu = thisMenu.getElementsByTagName("DIV")[0];
	var menubar = thisMenu.parentNode;

	if ( thisSubMenu.className == 'submenu' ) {
		var subList = menubar.getElementsByTagName("DIV");
		for ( var x = 0; x < subList.length; x++ ) {
			subList[x].className='submenu';
		}
		thisSubMenu.className = "menuShow";
	} else {
		thisSubMenu.className = 'submenu';
	}
	thisMenu.blur();
}

function toggleNavMenu(id) {
	var thisMenu = document.getElementById(id);
	var menubar = document.getElementById("menu")

	if ( thisMenu.style.display == 'none' ) {
		var subList = menubar.getElementsByTagName("DIV");
		for ( var x = 0; x < subList.length; x++ ) {
			subList[x].style.display="none";
		}
		thisMenu.style.display="block"
	} else {
		thisMenu.style.display="none";
	}
	thisMenu.blur();
}


function selectDiv(checkbox) {
	var divID = "div_" + checkbox.name;
	var div = window.document.getElementById(divID);	
	if ( div.className == "unloaded" ) {
		div.className = "showSection";
		div.innerHTML = "<P ALIGN='center'><B>Loading...</B></P>";
		window.setTimeout( loadContent, 100, tag, div);
	} else if ( div.className == "hideSection" ) {
		div.className = "showSection";
	} else {
		div.className = "hideSection";
	}
}

function loadForm(button, tag) {
	var div = window.document.getElementById(tag);
	divHTML = div.innerHTML;
	var query = "div=" + escape(tag) + "&" + escape(button.name) + "=" + escape(button.value) + valuesForForm(button.form);
	setLoading(div);
	window.setTimeout( loadQuery, 100, query, tag);
}

function setLoading(div) {
	var divs = div.getElementsByTagName("div"); 
	var hasDiv = false;
	for (var i = 0; i < divs.length; i++) { 
		if ( divs[i].id == "spinner" ) {
			startSpin(divs[i]);
			hasDiv = true;
		}
	}
	div.className = "showSection";
	if ( ! hasDiv ) {
		div.innerHTML = "<p align=\"center\"><b><i>Loading...</i></b></p>";
	}
}

function unsetLoading(div) {
	spin = 0;
	var divs = div.getElementsByTagName("div"); 
	for (var i = 0; i < divs.length; i++) { 
		if ( divs[i].id == "loading" ) {
			divs[i].className = "hideSection";
		}
	}
}

function closeForm(tag) {
	var div = window.document.getElementById(tag);
	div.innerHTML = divHTML;
}

function updateForm(action, divID) {
	var query = "div=" + escape(divID) + "&" + ( action.type == "button" ? escape(action.name) + "=" + escape(action.value) : "" ) + valuesForForm(action.form);
	action.disabled = true;
	var div = window.document.getElementById(divID);
	if ( div == null ) { 
		divID = "div_" + divID;
		div = window.document.getElementById(divID);
	}
	setLoading(div);
	window.setTimeout( loadQuery, 100, query, divID);	
}

function updateDiv(action, divID) {
	var query = "div=" + escape(divID) + "&" + ( action.type == "button" ? escape(action.name) + "=" + escape(action.value) : "" )  + valuesForForm(action.form);
	action.disabled = true;
	var div = window.document.getElementById(divID);
	setLoading(div);
	window.setTimeout( loadAJAXDiv, 100, query, divID);	
}

function updateFileDiv(divID, directory, dataClass, dataType) {
	var query = "showBrowser&div=" + escape(divID) + "&path=" + directory  + "&dataClass=" + dataClass;
	if ( dataType != null && dataType != '' ) { query = query + "&dataType=" + dataType; }
	var div = window.document.getElementById(divID);
	setLoading(div);
	window.setTimeout(loadAJAXDiv, 100, query, divID);	
}

function refreshDiv(action, divID) {
	var query = "div=" + escape(divID) + "&" + ( action.type == "button" ? escape(action.name) + "=" + escape(action.value) : "" )  + valuesForForm(action.form);
	action.disabled = true;
	var div = window.document.getElementById("div_" + divID);
	setLoading(div);
	window.setTimeout( loadAJAXDiv, 100, query, "div_" + divID);	
}

function sortTable(divID, sortField, sortDir) {
	var query = "div=" + escape(divID) + "&sortField=" + escape(sortField) + "&sortDir=" + escape(sortDir);
	var div = window.document.getElementById(divID);
	setLoading(div);
	window.setTimeout( loadQuery, 100, query, divID);		
}

function refreshForm(divID, field, value, form) {
	var query = "div=" + escape(divID) + "&" + escape(field) + "=" + escape(value) + valuesForForm(form);
	var div = window.document.getElementById(divID);
	setLoading(div);
	window.setTimeout( loadQuery, 100, query, divID);	
}

function loadDiv(tag) {
	var divID = "div_" + tag;
	var imgID = "twist_" + tag;
	var div = window.document.getElementById(divID);
	var img = window.document.getElementById(imgID);
	var imgPath = img.src.split("/");
	if ( div.className == "unloaded" ) {
		imgPath[imgPath.length - 1] = "twist-open.png";
		img.src = imgPath.join("/");
		setLoading(div);
		window.setTimeout( loadContent, 250, tag, div);
	} else if ( div.className == "hideSection" ) {
		div.className = "showSection";
		imgPath[imgPath.length - 1] = "twist-open.png";
	} else {
		div.className = "hideSection";
		imgPath[imgPath.length - 1] = "twist-closed.png";
	}
	img.src = imgPath.join("/");
}

function twistModule(tag) {
	var divID = "div_" + tag;
	var imgID = "twist_" + tag;
	var div = window.document.getElementById(divID);
	var img = window.document.getElementById(imgID);
	var imgPath = img.src.split("/");
	if ( div.className == "hideSection" ) {
		div.className = "showSection";
		imgPath[imgPath.length - 1] = "module-twist-open.png";
	} else {
		div.className = "hideSection";
		imgPath[imgPath.length - 1] = "module-twist-closed.png";
	}
	img.src = imgPath.join("/");
}
	
function flipDiv(tag) {
	var editID = "edit_" + tag;
	var viewID = "view_" + tag;
	var editDiv = window.document.getElementById(editID);
	var viewDiv = window.document.getElementById(viewID);
	if ( editDiv.className == "hideSection" ) {
		editDiv.className = "showEdit";
		viewDiv.className = "hideSection";
	} else {
		editDiv.className = "hideSection";
		viewDiv.className = "showSection";
	}
}

function toggleDiv(tag, show) {
	var aDiv = window.document.getElementById(tag);
	if ( show ) {
		aDiv.className = "showSection";
	} else {
		aDiv.className = "hideSection";
	}
}	

function showHideButton(button, divID) {
	var show = (button.innerHTML == 'Show'); 
	toggleDiv(divID, show); 
	if ( show ) { button.innerHTML = 'Hide'; 
	} else { button.innerHTML = 'Show';}
}

function showHide(show, hide) {
	var showDiv = window.document.getElementById(show);
	var hideDiv = window.document.getElementById(hide);
	showDiv.className="showSection";
	hideDiv.className="hideSection";
}

function hideDiv(tag) {
	var editDiv = window.document.getElementById(tag);
	editDiv.className = "hideSection";
}

function showDiv(tag) {
	var aDiv = window.document.getElementById(tag);
	aDiv.className = "showSection";
}
		
function hideTabs(aList) {
	var items = aList.childNodes;
	for ( var i = 0; i < items.length; i++ ) {
		if ( items[i].shown ) {
			items[i].shown = false;
			var subList = items[i].getElementsByTagName("div");
			for ( var x = 0; x < subList.length; x++ ) {
				subList[x].className='hide';
			}
		}
	}
}

function setupTabs(aList) {
	var items = aList.childNodes;
	for ( var i = 0; i < items.length; i++ ) {
		items[i].addEventListener("click", toggleTab, false);
		items[i].shown = false;
	}
}

function toggleTab(evObj) {
	var anItem = evObj.currentTarget;
	var isShown = anItem.shown;
	hideItems( anItem.parentNode );
	if ( ! isShown ) {
		var subList = anItem.getElementsByTagName("div");
		for ( var x = 0; x < subList.length; x++ ) {
			subList[x].className='menushow';
		}
		anItem.shown = true;
	}
	anItem.blur();
}

function loadTable(anUrl, showCheck) {
	var xmlHttp = null;
	var myLoc = window.location;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
	
	if ( showCheck != null && showCheck.checked ) {
		anUrl = anUrl + "&" + showCheck.name;
	}
  	console.log(anUrl);
  	console.log(showCheck);
	if (xmlHttp != null) {
  		xmlHttp.open("GET", myLoc.protocol + "//" + myLoc.hostname + ":" + myLoc.port + anUrl, true);
 		xmlHttp.onreadystatechange = function() {
 			if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
 				var docDiv = document.getElementById("spreadsheet");
				setDivContent(docDiv, xmlHttp);
 			}
 		}
 		xmlHttp.send(null);
  	} 
}

function showSpreadSheet(anUrl, worksheet, length, showCheck, header) {
	var xmlHttp = null;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
	
	var myLoc = document.createElement('a');
	myLoc.href = anUrl;
	
	if ( myLoc.search.length > 0 ) {
		myLoc.search = myLoc.search + "&worksheet=" + worksheet;
	} else {
		myLoc.search = "worksheet=" + worksheet;		
	}
	
	myLoc.search = myLoc.search + "&length=" + length;
	
	if ( showCheck != null && showCheck.checked ) {
		myLoc.search = myLoc.search + "&" + showCheck.name;
	}
	
	if ( header != null && header ) {
		myLoc.search = myLoc.search + "&header";
	} 
	
  	console.log(anUrl);
  	console.log(showCheck);
	if (xmlHttp != null) {
  		xmlHttp.open("GET", myLoc.href, true);
 		xmlHttp.onreadystatechange = function() {
 			if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
 				var docDiv = document.getElementById("spreadsheet");
 				setDivContent(docDiv, xmlHttp);
 			}
 		}
 		xmlHttp.send(null);
  	} 
}

function loadContent(tag, div) {
	var xmlHttp = null;
	var myLoc = window.location;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
  	
  	var prefix = "?div=";
  	if ( myLoc.search.length > 0 ) { 
  		prefix = "&div=";
  	}
  
 	if (xmlHttp != null) {
 		xmlHttp.open("GET", myLoc.toString() + prefix + escape(tag), true);
 		xmlHttp.onreadystatechange = function() {
 	 		if (this.readyState == 4 && this.status == 200) {
 				var docDiv = document.getElementById(div.id);
 				docDiv.className = "showSection";
 				unsetLoading(docDiv);
 				setDivContent(docDiv, this);
 			} 
 		}
 		xmlHttp.send(null);
  	} 
}

function loadQuery(query, divID) {
	var xmlHttp = null;
	var myLoc = window.location;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
  	
 	if (xmlHttp != null) {
 		if ( myLoc.search && myLoc.search.length > 0 ) 
	 		xmlHttp.open("GET", myLoc.toString() + "&" + query, true);
		else 
 			xmlHttp.open("GET", myLoc.toString() + "?" + query, true);
 		xmlHttp.onreadystatechange = function() {
 	 		if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
 				var docDiv = document.getElementById(divID);
 				docDiv.className = "showSection";
 				unsetLoading(docDiv);
 				setDivContent(docDiv, xmlHttp);
 			} 
 		}
 		
 		xmlHttp.send(null);
  	} 
}

function loadAJAXDiv(query, divID) {
	var xmlHttp = null;
	var myLoc = window.location;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
  	
 	if (xmlHttp != null) {
 		if ( myLoc.search && myLoc.search.length > 0 ) 
	 		xmlHttp.open("GET", myLoc.toString() + "&" + query, true);
		else 
 			xmlHttp.open("GET", myLoc.toString() + "?" + query, true);
 		xmlHttp.onreadystatechange = function() {
 	 		if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
 				var docDiv = document.getElementById(divID);
 				unsetLoading(docDiv);
 				setDivContent(docDiv, xmlHttp);
 			} 
 		}
 		xmlHttp.send(null);
 		
  	} 
}

function livesearch(field, searchTag, divTag) {
	var div = window.document.getElementById(divTag);
	if ( field.value.length == 0 ) {
		div.style.border = "0px";
		div.innerHTML = "";
		div.style.display = "none";
	} else {	
		div.innerHTML = "<FONT COLOR='#505050'>Loading...</FONT>";
		div.style.border = "1px solid #A5ACB2";
		div.style.display = "block";
		var query = "div=" + escape(divTag) + "&" + escape(field.name) + "=" + escape(field.value) + "&livesearch=" + escape(searchTag);
		window.setTimeout(loadLS, 100, query, divTag);
	}
}

/*
function showDate(divTag) {
	var div = window.document.getElementById(divTag);
	div.style.border = "1px solid #A5ACB2";
	if ( div.style.display == "block") {
		div.style.display = "none";
	} else {
		div.style.display = "block";
		
	}
}
*/
/*
function livesearch(field, module, searchTag, divTag) {
	var div = window.document.getElementById(divTag);
	if ( field.value.length == 0 ) {
		div.style.border = "0px";
		div.innerHTML = "";
		div.style.display = "none";
	} else {	
		div.innerHTML = "<FONT COLOR='#505050'>Loading...</FONT>";
		div.style.border = "1px solid #A5ACB2";
		div.style.display = "block";
		var query = "div=" + escape(divTag) + "&" + escape(field.name) + "=" + escape(field.value) + "&livesearch=" + escape(searchTag);
		window.setTimeout(loadLS, 100, query, divTag);
	}
}

*/
	
function setLS(fieldTag, fieldValue, divTag) {
	var field = window.document.getElementById(fieldTag);
	field.value= fieldValue; 
	closeLSdiv = true;
	closeLS(divTag);
	field.onchange();
}

function closeLS(divTag) {
	if ( closeLSdiv ) {
		var div = window.document.getElementById(divTag);
		div.style.border = "0px";
		div.innerHTML = "";
		div.style.display = "none";
	}
}

function setDateDiv(fieldTag, fieldValue, divTag) {
	var div = window.document.getElementById(divTag);
	window.document.getElementById(fieldTag).value= fieldValue; 
	div.style.border = "0px";
	div.style.display = "none";
}

function loadLS(query, divID) {
	var xmlHttp = null;
	var myLoc = window.location;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
	
 	if (xmlHttp != null) {
 		if ( myLoc.search && myLoc.search.length > 0 ) 
	 		xmlHttp.open("GET", myLoc.toString() + "&" + query, false);
		else 
 			xmlHttp.open("GET", myLoc.toString() + "?" + query, false);
 		xmlHttp.send(null);
 		if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
			var docDiv = document.getElementById(divID);
			setDivContent(docDiv, xmlHttp);
		} 
  	} 
}

function setDivContent(div, xmlHttp) {
	if ( document.getElementById("loginCover") == null ) {
		div.innerHTML = xmlHttp.responseText;
		var cover = div.childNodes["loginCover"];
		if ( cover != null ) {
//			document.body.appendChild(cover); 
			document.body.appendChild(cover);
			var loginBox = div.childNodes['loginBox'];
			document.body.appendChild(loginBox);

			document.getElementById("loginForm").style.display = "none";
			document.getElementById('loginNote').style.visibility = "visible";
			cover.style.height = window.screen.height; 
			cover.style.visibility = "visible";
			div.innerHTML = "";
		}
	}
}

function reloadDiv(divID, formObject) {
	var xmlHttp = null;
	var myLoc = window.location;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
  	
  	var prefix = "?div=";
  	if ( myLoc.search.length > 0 ) { 
  		prefix = "&div=";
  	}
  	
	if (xmlHttp != null) {
		var query;
		if ( formObject.type == "button" ) {
			query = "&" + formObject.name + "=" + escape(formObject.value) + valuesForForm(formObject.form);
		} else {
			query = valuesForForm(formObject.form);
		}
 		xmlHttp.open("GET", myLoc.toString() + prefix + escape(divID) + query, false);
 		xmlHttp.send(null);
 		if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
			var docDiv = document.getElementById("div_" + divID);
			setDivContent(docDiv, xmlHttp);
			unsetLoading(docDiv);
		} 
  	} 
}

function valuesForForm(aForm) {
	var elems = aForm.elements;
	var query = "";
	for ( var i = 0; i < elems.length; i++ ) {
		var anElem = elems[i];
		if ( ! (anElem.type == "button" || anElem.type == "reset" || anElem.type == "submit") ) {
			if ( (anElem.type == "radio" || anElem.type == "checkbox") ) {
				if ( anElem.checked ) {
					query = query + "&" + anElem.name + "=" + escape(anElem.value);
				}
			} else {
				query = query + "&" + anElem.name + "=" + escape(anElem.value);
			}
		}
	}
	return query;
}

function setValue(anElem, aValue) {
	if ( anElem == null ) {
		return;
	}

	if ( (anElem.type == "radio" || anElem.type == "checkbox") ) {
		anElem.checked = aValue;
	} else if ( anElem.type == "select" ) {
		var opts = anElem.options;
		for ( var i = 0; i < opts.length; i++ ) {
			if ( opts[i].value == aValue ) {
				anElem.selectedIndex = i;
				break;
			}
		}
	} else {
		anElem.value = aValue;
	}
}

function makeOLMarker(ll, content) {
	var feature = new OpenLayers.Feature(markers, ll); 
	feature.closeBox = true;
	feature.popupClass = OpenLayers.Popup.FramedCloud;
	feature.data.popupContentHTML = content;
	feature.data.overflow = "auto";
	
	var marker = feature.createMarker();

    var markerClick = function (evt) {
    	if (this.popup == null) {
    		this.popup = this.createPopup(this.closeBox);
            map.addPopup(this.popup);
            this.popup.show();
        } else {
            this.popup.toggle();
        }
        currentPopup = this.popup;
        OpenLayers.Event.stop(evt);
    };
    marker.events.register("mousedown", feature, markerClick);
	return marker;
}

function OLLatLong(lat, long) {
	return new OpenLayers.LonLat(long, lat);
}

function setupCalendar(divTag, fieldTag) {
	var div = window.document.getElementById(divTag);
	var field = window.document.getElementById(fieldTag); 	
	var form = new String("<table class='month' width='250'><tr><th width='14%'>Sun</th><th width='14%'>Mon</th><th width='14%'>Tues</th><th width='14%'>Wed</th><th width='14%'>Thurs</th><th width='14%'>Fri</th><th width='14%'>Sat</th></tr>");
	form = form.concat(value);
	var match = field.value.match(/^(\d{4})-(\d{2})-(\d{2})$/);
	if ( match != null ) {
		var year = match[0];
	}
}

function getCurrentFMPath(fmObTag, fmETag, formObj) {
	var objectFM = top.document.getElementById(fmObTag);
	var embedFM = top.document.getElementById(fmETag);
	if ( objectFM != null && objectFM.getCurrentPath != null ) {
		formObj.value = objectFM.getCurrentPath();
	} else if ( embedFM != null && embedFM.getCurrentPath != null ) {
		formObj.value = embedFM.getCurrentPath();
	}
}

function refreshFM(fmObjTag, fmETag) {
	var objectFM = top.document.getElementById(fmObjTag);
	var embedFM = top.document.getElementById(fmETag);
	if ( objectFM != null && objectFM.refreshFileList != null ) {
		objectFM.refreshFileList();
	} else if ( embedFM != null && embedFM.refreshFileList != null ) {
		embedFM.refreshFileList();
	}	
}

function validatePassword(pass1, pass2, button, divID) {
	if ( pass1.length > 0 && pass2.length > 0 ) {
		var div = document.getElementById(divID);
		if ( pass1 == pass2 ) {
			if ( div != null ) {
				div.innerHTML = "<font color='green'>Password confirmed</font>";
			}
			if ( button != null ) {
				button.disabled = false;
			}
		} else {
			if ( div != null ) {
				div.innerHTML = "<font color='red'>Password mismatch</font>";
			}			
			if ( button != null ) {
				button.disabled = true;
			}
		}
	}
}



//simple script to rotate all spinners 45 degrees on each tick
//this works differently from the css transforms, which is smooth

var count = 0;
var spin = 0;

/*
function spinner() {
	if ( spin == 1 ) {
		var elem = document.getElementById('spinner');
		elem.style.MozTransform = 'scale(0.5) rotate('+count+'deg)';
		elem.style.WebkitTransform = 'scale(0.5) rotate('+count+'deg)';
		if (count==360) { count = 0; }
		count+=45;
		window.setTimeout(spinner, 100);
	}
}
*/

function runSpinner(spinnerDiv) {
	if ( spin == 1 ) {
		spinnerDiv.style.MozTransform = 'scale(0.5) rotate('+count+'deg)';
		spinnerDiv.style.WebkitTransform = 'scale(0.5) rotate('+count+'deg)';
		if (count==360) { count = 0; }
		count+=45;
		window.setTimeout(runSpinner, 100, spinnerDiv);
	}
}

function startSpin(spinnerDiv) {
	spin = 1;
	count = 0;
	runSpinner(spinnerDiv);
}

var progressLen = 0;

function progress() {
	if ( spin == 1 ) {
		var elem = document.getElementById('progressBar');
		elem.style.opacity = (0.3 *  Math.sin((count / 180) * Math.PI)) + 0.7 ;
		elem.style.width = progressLen + "%";
		var elem = document.getElementById('progressText');
		if ( elem != null ) {
			elem.innerHTML =  progressLen.toFixed(0) + "%";
		}
		if (count==360) { count = 0; }
		count += 15;
		window.setTimeout(progress, 100);		
	}
}

function setProgress(length) {
	var elem = document.getElementById('progressBar');
	elem.style.opacity = (0.3 *  Math.sin((count / 180) * Math.PI)) + 0.7 ;
	elem.style.width = length + "%";
	var elem = document.getElementById('progressText');
	if ( elem != null ) {
		elem.innerHTML =  length.toFixed(0) + "%";
	}
	if (count==360) { count = 0; }
	count += 15;
}


function endProgress(length, button) {
	var elem = document.getElementById('progressBar');
	elem.style.opacity = 1.0 ;
	elem.style.width = length + "%";
	spin = 0;
	if ( button != null ) {
		button.disabled = false;
	}
}

function testProgress() {
	if ( progressLen < 1 ) {
		progressLen += .05;
		window.setTimeout(testProgress, 1000);
	} else {
		endProgress(progressLen);
		var elem = document.getElementById('progressText');
		if ( elem != null ) {
			elem.innerHTML =  "Complete";
		}
	}
}

function enableButton(button) {
	if ( button != null ) {
		button.disabled = false;
	}
}

function uploadStatus(updatePath, resultButton) {
	var xmlHttp = null;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
  	
	if (xmlHttp != null) {
 		xmlHttp.open("GET", updatePath, false);
 		xmlHttp.send(null);
 		if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
 			var progText = document.getElementById('progressText');
			var response = xmlHttp.responseText;
			if ( response === "DONE" ) {
				endProgress(100, resultButton);
				if ( progText != null ) {
					progText.innerHTML = "Complete";
				}
			} else if ( response === "ERROR" ) { 
				endProgress(progressLen, resultButton);
				if ( progText != null ) {
					progText.innerHTML = "<font color='red'>ERROR!</font>";
				}						
			} else if ( response === "STOP" ) {
				endProgress(progressLen, resultButton);
				if ( progText != null ) {
					progText.innerHTML = "Stopped";
				}				
			} else {
				progressLen = Number(response);
				setProgress(progressLen);
				window.setTimeout(uploadStatus, 50, updatePath, resultButton);
			}
		} 
  	} 
}

function toggleRows(className, show) {
        var elem = document.getElementsByClassName(className);
        for ( var i = 0; i < elem.length; i++ ) {
                elem[i].style.display = (show ? "table-row" : "none");
        }
}
