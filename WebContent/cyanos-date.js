function showDate(divTag, dateFieldID, showTime) {
	var cals = document.getElementsByClassName("calendar");
	for ( var i = 0; i < cals.length; i++ ) {
		if ( cals[i].id == divTag ) {
			if ( cals[i].style.display == "block") {
				cals[i].style.border = "0px";
				cals[i].style.display = "none";
			} else {
				var dateString = window.document.getElementById(dateFieldID).value;
				var timestamp = Date.parse(dateString);
				var selDate;
				if ( isNaN(timestamp) == false ) {
					selDate = new Date(timestamp);
				} else {
					selDate = new Date();
				}
				buildCalendar(dateFieldID, divTag, selDate.getMonth(), selDate.getFullYear(), showTime);
				cals[i].style.border = "1px solid #A5ACB2";
				cals[i].style.display = "block";
			}
		} else if ( cals[i].style.display == "block") {
			cals[i].style.border = "0px";
			cals[i].style.display = "none";
		}
	}	
}

function setDateDiv(fieldTag, fieldValue, divTag, close) {
	if ( close ) {
		window.document.getElementById(fieldTag).value = fieldValue; 
		closeDateDiv(divTag);
	} else {
		document.getElementById('temp_'.concat(fieldTag)).value = fieldValue; 
		var elem = document.getElementsByClassName("selDay");
		var today = new Date();
		var todayString = today.toISOString().substring(0,10);
		if ( elem.length > 0 ) {
			if ( elem[0].id == todayString ) {
				elem[0].className = "today";
			} else {
				elem[0].className = "day";
			}
		}
		document.getElementById(fieldValue).className = "selDay";
	}
}

function closeDateDiv(divTag) {
	var div = window.document.getElementById(divTag);
	div.style.border = "0px";
	div.style.display = "none";
}

function resetDateDiv(action, showTime) {
	var div = getParent(action, "div.calendar")
	var divTag = div.id;
	var fieldTag = divTag.substring(4);

	var dateString = window.document.getElementById(fieldTag).value;
	var timestamp = Date.parse(dateString);
	var selDate;
	if ( isNaN(timestamp) == false ) {
		selDate = new Date(timestamp);
	} else {
		selDate = new Date();
	}
	buildCalendar(fieldTag, divTag, selDate.getMonth(), selDate.getFullYear(), showTime);
}

function buildCalendar(fieldTag, divTag, month, year, showTime) {
	var div = window.document.getElementById(divTag);
	var dateString = window.document.getElementById(fieldTag).value;
	var timestamp = Date.parse(dateString);
	dateString = dateString.substring(0,10);
	var selDateString;
	var selDate = new Date();
	if ( isNaN(timestamp) == false ) {
		selDate = new Date(timestamp);
		selDateString = selDate.toISOString().substring(0,10);
	}
	var today = new Date();
	
	var table = div.getElementsByTagName("table")[0];
	
	var rowIndex = 1;
	
	var calDate = new Date(year, month, 1, 0, 0, 0, 0);
	
	for ( var i = 0; i < calDate.getDay(); i++  ) {
		var cell = table.rows[1].cells[i];
		cell.innerHTML = "";
		cell.className = "";
	}
	
	var todayString = today.toISOString().substring(0,10);
	
	while ( calDate.getMonth() == month ) {
		while ( table.rows.length <= rowIndex ) {
			var newRow = table.insertRow(-1);
			for ( var i = 0; i < 7; i++ ) {
				newRow.insertCell(-1);
			}
		}
		

		var thisDay = calDate.getDay();
		var cell = table.rows[rowIndex].cells[thisDay];
		cell.innerHTML = "";

		var anchor = document.createElement("a");
		anchor.innerHTML = calDate.getDate();
		var dateString = calDate.toISOString().substring(0,10);
		cell.id = dateString;
		anchor.onclick = function(thisDate) { return function () { setDateDiv(fieldTag, thisDate, divTag, ! showTime); }; }(dateString);
		cell.appendChild(anchor);
		if ( dateString == selDateString ) {
			cell.className = "selDay";
		} else if ( dateString == todayString ) {
			cell.className = "today";
		} else {
			cell.className = "day";
		}
		
		if ( thisDay == 6 ) {
			rowIndex++;
		}
		calDate.setDate(calDate.getDate() + 1);
	}
	
	if ( calDate.getDay() > 1 ) {
		for ( var i = calDate.getDay(); i < 7; i++ ) {
			table.rows[rowIndex].cells[i].innerHTML = "";
			table.rows[rowIndex].cells[i].className = "";
		}
	} else {
		rowIndex--;
	}
	
	while ( rowIndex + 1 < table.rows.length ) {
		table.deleteRow(-1);
	}
		
	div.querySelector("#year").value = year;
	div.querySelector("#month").value = month;
	if ( showTime ) {
		div.querySelector("#hour").value = ( selDate.getHours() + 11 ) % 12 + 1;
		div.querySelector("#minute").value = selDate.getMinutes();
		div.querySelector("#meridian").selectedIndex = ( selDate.getHours() > 11 ? 1 : 0 );
	}
}

function updateDateTime(action) {
	var div = getParent(action, "div.calendar")
	var divTag = div.id;
	var fieldTag = divTag.substring(4);

	var hour = div.querySelector("#hour").value;
	if ( hour < 10 ) hour = "0".concat(hour);
	var minute = div.querySelector("#minute").value;
	if ( minute < 10 ) minute = "0".concat(minute);
	
	var meridian = div.querySelector("#meridian").value;
	
	var newValue = document.getElementById('temp_'.concat(fieldTag)).value.concat(" ").concat(hour).concat(":").concat(minute).concat(" ").concat(meridian);
	setDateDiv(fieldTag, newValue, divTag, true);
}

function updateDateDiv(action, showTime) {
	var div = getParent(action, "div.calendar");
	var divTag = div.id;
	var fieldTag = divTag.substring(4);
	buildCalendar(fieldTag, divTag, div.querySelector("#month").value, div.querySelector("#year").value, showTime);	
}

function getParent(elem, selector) {
	var byClass = selector.indexOf(".");
	var byID = selector.indexOf("#");
	var tagName = "";
	var className = "";
	var id = "";
	if ( byClass < 0 && byID < 0 ) {
		tagName = selector.toUpperCase();
	} else {
		if ( byClass > byID ) {
			var sels = selector.split(".", 2);	
			if ( sels.length > 1 ) {
				tagName = sels[0].toUpperCase();
			}
			className = sels[sels.length - 1];
		} else {
			var sels = selector.split("#", 2);	
			if ( sels.length > 1 ) {
				tagName = sels[0].toUpperCase();
			}
			id = sels[sels.length - 1];			
		}
	}
	
	for ( ; elem && elem != document; elem = elem.parentNode ) {
		var valid = 0;
		if ( tagName.length > 0 ) {
			valid += (elem.tagName === tagName ? 1 : -1);
		}
		if ( className.length > 0 ) {
			valid += (className === elem.className ? 1 : -1);
		} 
		if ( id.length > 0 ) {
			valid += (id === elem.id ? 1 : -1);
		}
		if ( valid > 0 )
			return elem;
	}
	
}

function gotoToday(button, showTime) {
	var div = getParent(button, "div.calendar");
	var today = new Date();
	var divTag = div.id;
	var fieldTag = divTag.substring(4);
	buildCalendar(fieldTag, divTag, today.getMonth(), today.getFullYear(), showTime);
	setDateDiv(fieldTag, today.toISOString().substring(0,10), divTag, ! showTime );
}