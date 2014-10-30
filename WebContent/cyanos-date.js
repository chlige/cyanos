function showDate(divTag, dateFieldID) {
	var div = window.document.getElementById(divTag);
	if ( div.style.display == "block") {
		div.style.border = "0px";
		div.style.display = "none";
	} else {
		resetDateDiv(dateFieldID, divTag);
		div.style.border = "1px solid #A5ACB2";
		div.style.display = "block";
	}
}

function setDateDiv(fieldTag, fieldValue, divTag) {
	var div = window.document.getElementById(divTag);
	window.document.getElementById(fieldTag).value= fieldValue; 
	div.style.border = "0px";
	div.style.display = "none";
}

function resetDateDiv(fieldTag, divTag) {
	var dateString = window.document.getElementById(fieldTag).value;
	var timestamp = Date.parse(dateString);
	var selDate;
	if ( isNaN(timestamp) == false ) {
		selDate = new Date(timestamp);
	} else {
		selDate = new Date();
	}
	buildCalendar(fieldTag, divTag, selDate.getMonth(), selDate.getFullYear());
}

function buildCalendar(fieldTag, divTag, month, year) {
	var div = window.document.getElementById(divTag);
	var dateString = window.document.getElementById(fieldTag).value;
	var timestamp = Date.parse(dateString);
	var selDateString;
	if ( isNaN(timestamp) == false ) {
		var selDate = new Date(timestamp);
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
		anchor.onclick = function(thisDate) { return function () { setDateDiv(fieldTag, thisDate, divTag); }; }(dateString);
		cell.appendChild(anchor);
		if ( dateString == selDateString ) {
			cell.className = "selDate";
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
		
	var inputs = div.getElementsByTagName("input");
	for ( i in inputs ) {
		if ( inputs[i].name == "year" ) {
			inputs[i].value = year;
		}
	}

	inputs = div.getElementsByTagName("select");
	for ( i in inputs ) {
		if ( inputs[i].name == "month" ) {
			inputs[i].selectedIndex = month;
		}
	}
	

}

function gotoToday(fieldTag, divTag) {
	var today = new Date();
	buildCalendar(fieldTag, divTag, today.getMonth(), today.getFullYear());
}

function updateDateDiv(action, fieldTag, divTag) {
	var myForm = action.form;
	
	buildCalendar(fieldTag, divTag, myForm.elements["month"].value, myForm.elements["year"].value);	
}
