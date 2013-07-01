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
	var request = "cal.cgi?update_field=" + escape(date_field.form.name + "." + date_field.name);
	calWin = window.open(request, "calendar", "height=200,width=350");  
	calWin.updateField = date_field;
}

function selectLoc(loc_field) {
	var request = "plate.cgi?update_field=" + escape(loc_field.form.name + "." + loc_field.name);
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
