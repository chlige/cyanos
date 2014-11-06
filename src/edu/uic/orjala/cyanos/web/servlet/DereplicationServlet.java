/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.DereplicationForm;
import edu.uic.orjala.cyanos.web.forms.CompoundForm;
import edu.uic.orjala.cyanos.web.forms.MSDereplication;
import edu.uic.orjala.cyanos.web.forms.NMRDereplication;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Image;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;
import edu.uic.orjala.cyanos.web.listener.AppConfigListener;

/**
 * @author George Chlipala
 *
 */
public class DereplicationServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 25005917302237743L;
	private static final String HELP_MODULE = "dereplication";
//	private static final String QUERY_RESULTS = "dereplication_results";	
	private List<Class> derepForms = null;
	
	public static final String QUERY_ATTRIBUTE = "query";
	public static final String SEARCH_ACTION = "searchAction";


	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if ( ! AppConfigListener.isNewInstall() ) {
			this.derepForms = new ArrayList<Class>();
			this.derepForms.add(MSDereplication.class);
			if ( this.checkNMRDerep() ) 
				this.derepForms.add(NMRDereplication.class);
			AppConfig myConfig = this.getAppConfig();
			List<String> addOns = myConfig.classesForDereplicationModule();
			if ( addOns != null ) {
				this.addModules(addOns);
			}
		}
	}

	public void display(CyanosWrapper aWrap) throws Exception {
		
		PrintWriter out = aWrap.startHTMLDoc("Dereplication");
		
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Compound Dereplication");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		out.println(head.toString());

/*		Form aForm = new Form("<P ALIGN='CENTER'><B>Search type:</B> ");
		aForm.setAttribute("METHOD", "POST");
		String formType = "ms";
		if ( this.hasFormValue("dataType")) 
			formType = this.getFormValue("dataType");
		Popup typePopup = new Popup();
		typePopup.addItemWithLabel("ms", "MS");
//		typePopup.addItemWithLabel("uv", "UV");
		if ( this.nmrDerep ) typePopup.addItemWithLabel("nmr", "NMR");
		typePopup.setName("dataType");
		typePopup.setDefault(formType);
		typePopup.setAttribute("onChange", "this.form.submit()");
		aForm.addItem(typePopup.toString());
		aForm.setAttribute("ACTION", "dereplication");
		aForm.addItem("</P>");
		searchDiv.addItem(aForm.toString());
*/		
		Div contentDiv = new Div();
		contentDiv.setID("mainContent");
		
		List<DereplicationForm> myForms = this.dereplicationForms(aWrap);
		
		if ( aWrap.hasFormValue("searchAction") ) {			
			String sqlWhere = this.sqlWhere(aWrap);		
			if ( sqlWhere != null ) {
			
				Compound compounds = SQLCompound.compoundsWhere(aWrap.getSQLDataSource(), sqlWhere, SQLCompound.MONOISOTOPIC_MASS_COLUMN, SQLCompound.ASCENDING_SORT);
				CompoundForm aForm = new CompoundForm(aWrap);
				
				Div hiddenDiv = new Div();
				hiddenDiv.setID("hideSQL");
				hiddenDiv.addItem("<P ALIGN='CENTER'><CODE>");
				hiddenDiv.addItem(sqlWhere);
				hiddenDiv.addItem("</CODE></P>");
				hiddenDiv.addItem("<P ALIGN='CENTER'><A onClick=\"showHide('showSQL','hideSQL')\">Hide SQL WHERE Statement</A></P>");
				hiddenDiv.setClass("hideSection");
				
				Div showDiv = new Div();
				showDiv.setID("showSQL");
				showDiv.setClass("showSection");
				showDiv.addItem("<P ALIGN='CENTER'><A onClick=\"showHide('hideSQL','showSQL')\">Show SQL WHERE Statement</A></P>");
				
				Div sqlDiv = new Div(showDiv);
				sqlDiv.addItem(hiddenDiv);
	//			sqlDiv.setClass("searchNav");
				contentDiv.addItem(sqlDiv);

				contentDiv.addItem(aForm.listCompounds(compounds, true));

				// TODO NEED TO SETUP FOR EXTERNAL COMPOUNDS.
				/*
				if ( outside.size() > 0 ) {
					anIter = outside.listIterator();
					while ( anIter.hasNext() ) {
					}
				}
				*/
			}
		} else if ( aWrap.hasFormValue("printSQL") ) {
			String sqlWhere = this.sqlWhere(aWrap);
			contentDiv.addItem("<CODE>");
			contentDiv.addItem(sqlWhere);
			contentDiv.addItem("</CODE>");	
		}
		
		
		out.println(this.searchDiv(aWrap, myForms));
		out.println(contentDiv.toString());
		
		aWrap.finishHTMLDoc();
	}
	
	private String sqlWhere(CyanosWrapper aWrap) {
		List<String> whereList = this.whereList(aWrap);
		ListIterator<String> whereIter = whereList.listIterator();
		if ( whereIter.hasNext() ) {
			StringBuffer sqlString = new StringBuffer( whereIter.next() );

			while ( whereIter.hasNext() ) {
				sqlString.append(" AND ");
				sqlString.append(whereIter.next());
			}
			return sqlString.toString();
		}
		return null;
	}
	
	private List<String> whereList(CyanosWrapper aWrap) {
		ListIterator<DereplicationForm> anIter = this.dereplicationForms(aWrap).listIterator();
		List<String> whereList = new ArrayList<String>();
			
		while ( anIter.hasNext() ) {
			DereplicationForm aForm = anIter.next();
			String aWhere = aForm.sqlWhere("");
			if ( aWhere != null )
				whereList.add(aWhere);
		}
		return whereList;
	}
	
	private String searchDiv(CyanosWrapper aWrap, List<DereplicationForm> myForms) {
		Div mainDiv = new Div();
		mainDiv.setClass("searchNav");
		
		Image twist;
		if ( aWrap.hasFormValue("searchAction") ) {
			twist = aWrap.getImage("twist-closed.png");
		} else {
			twist = aWrap.getImage("twist-open.png");
		}

		twist.setAttribute("ID", "twist_search");
		twist.setAttribute("ALIGN", "ABSMIDDLE");
		mainDiv.addItem(String.format("<A NAME='search' CLASS='twist' onClick='loadDiv(\"search\")' CLASS='divTitle'>%s Search Form</A>", twist.toString()));
		
		Form aForm = new Form();
		
		ListIterator<DereplicationForm> anIter = myForms.listIterator();
	
		while ( anIter.hasNext() ) {
			DereplicationForm derepForm = anIter.next();
			aForm.addItem(derepForm.form());
		}
		
		aForm.addItem("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='searchAction' VALUE='Search'/><INPUT TYPE='RESET'/></P>");		
		Div searchDiv = new Div(aForm);
		
		aForm = new Form("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' VALUE='Clear Form'/>");
		aForm.setAttribute("ACTION", aWrap.getRequestURI());
		
		searchDiv.addItem(aForm);
		
		searchDiv.setID("div_search");
		if ( aWrap.hasFormValue("searchAction") )
			searchDiv.setClass("hideSection");			
		else
			searchDiv.setClass("showSection");
		mainDiv.addItem(searchDiv);
		
		return mainDiv.toString();

	}
	
	private List<DereplicationForm> dereplicationForms(CyanosWrapper aWrap) {
		List<DereplicationForm> myForms = new ArrayList<DereplicationForm>();
		ListIterator<Class> anIter = this.derepForms.listIterator();
		Class[] classList = { CyanosWrapper.class };
		Object[] args = { aWrap };
		while ( anIter.hasNext() ) {
			Class aClass = anIter.next();
			try {
				Constructor aCons = aClass.getConstructor(classList);
				myForms.add((DereplicationForm) aCons.newInstance(args));
			} catch (Exception e) {
				this.log("Cannot add DereplicationForm", e);
				e.printStackTrace();
			}
		}
		
		return myForms;
	}
/*		
	private String uvResults() {
		String[] resultHeaders = {"Name", "Formula", "Notes", "UV"};
		TableCell resultHead = new TableHeader(resultHeaders);
		resultHead.setAttribute("class","header");
		TableRow aRow = new TableRow(resultHead);
		Table myTable = new Table(aRow);
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");

		float relDiff = 0.05f;
		if ( ! this.getFormValue("relintDiff").equals("") ) {
			relDiff = Float.parseFloat(this.getFormValue("relintDiff"));
		}
		float waveDiff = Float.parseFloat(this.getFormValue("waveDiff"));

		Integer rowNumber = new Integer(this.getFormValue("rows"));
		int rows = rowNumber.intValue();
		List<Float> waves = new ArrayList<Float>();
		Map<Float,Float> relInts = new HashMap<Float,Float>();
		SortedSet<String> compoundList = new TreeSet<String>();
		Compound_UV compounds;
		
		for ( int i = 1; i <= rows; i++) {
			String waveLabel = String.format("%2d_wave", i);
			String intLabel = String.format("%2d_relint", i);
			if ( ! this.getFormValue(waveLabel).equals("") ) {
				Float aWave = new Float(Float.parseFloat(this.getFormValue(waveLabel)));
				waves.add(aWave);
				Float anInt = new Float(Float.parseFloat(this.getFormValue(intLabel)));
				relInts.put(aWave, anInt);
				try {
					compounds = new SQLCompound(this.getSQLDataSource());
					if ( compounds.findCompoundsByUVMax(aWave.floatValue(), waveDiff, 
							anInt.floatValue(), relDiff) ) {
						compounds.beforeFirst();
						while ( compounds.next() ) {
							compoundList.add(compounds.getID());
						}
					}
				} catch (DataException e) {
					return this.handleException(e);
				}
			}
		}
		
		Iterator<String> idIter = compoundList.iterator();
		boolean oddRow = true;
		while ( idIter.hasNext() ) {
			try {
				compounds = new SQLCompound(this.getSQLDataSource(), idIter.next());
				if ( compounds.first() ) {
					TableCell myCell = new TableCell();
					myCell.addItem("<A HREF='?id=" + compounds.getID() + "'>" + compounds.getName() + "</A>");	
					myCell.addItem(compounds.getFormula());
					myCell.addItem(this.shortenString(compounds.getNotes(), 15));
					Map<Float,Float> uvPeaks = compounds.getUVPeaks();

					SortedSet<Float> dataWaves = new TreeSet<Float>(uvPeaks.keySet());
					Iterator<Float> dataIter = dataWaves.iterator();
					
					aRow = new TableRow(myCell);
					if ( oddRow ) 
						aRow.setClass("odd");
					else
						aRow.setClass("even");
					aRow.setAttribute("align", "center");
					myTable.addItem(aRow);
					oddRow = (! oddRow);

				}
			} catch (DataException e) {
				return this.handleException(e);
			}
			
		}
		
		
		return "";
	}

	private String uvForm() {
		
		int rows;
		Popup numberPop = new Popup();
		for ( int i = 1; i <= 15; i++ ) {
			numberPop.addItem(String.valueOf(i));
		}
		numberPop.setName("rows");
		numberPop.setAttribute("onChange", "this.form.submit()");
		if ( this.hasFormValue("rows") ) {
			numberPop.setDefault(this.getFormValue("rows"));
			Integer rowNumber = new Integer(this.getFormValue("rows"));
			rows = rowNumber.intValue();
		} else {
			numberPop.setDefault("5");
			Integer rowNumber = new Integer("5");
			rows = rowNumber.intValue();
		}
		TableRow aRow = new TableRow("<TH COLSPAN='2' ALIGN='CENTER'>Search Lines: " + numberPop.toString() + "</TH>");
		
		TableCell myCell = new TableCell("Wavelength<BR/>(nm)");
		myCell.addItem("Relative<BR/>Intensity");
		aRow.addItem(myCell);
		
		for ( int i = 1; i <= rows; i++) {
			String waveLabel = String.format("%2d_wave", i);
			String intLabel = String.format("%2d_relint", i);
			if ( this.hasFormValue(waveLabel)) {
				myCell = new TableCell(String.format("<INPUT TYPE='TEXT' NAME='%s' VALUE='%s' SIZE='5'/>", waveLabel, this.getFormValue(waveLabel)));
			} else {
				myCell = new TableCell(String.format("<INPUT TYPE='TEXT' NAME='%s' SIZE='5'/>", waveLabel));
			}
			if ( this.hasFormValue(intLabel)) {
				myCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='%s' VALUE='%s' SIZE='5'/>", intLabel, this.getFormValue(intLabel)));
			} else {
				myCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='%s' SIZE='5'/>", intLabel));
			}
			aRow.addItem(myCell);
		}
		
		aRow.addItem("<TH COLSPAN=2>Tolerance</TH>");
		
		if ( this.hasFormValue("waveDiff")) {
			myCell = new TableCell(String.format("<INPUT TYPE='TEXT' NAME='waveDiff' VALUE='%s' SIZE='5'/>", this.getFormValue("waveDiff")));
		} else {
			myCell = new TableCell("<INPUT TYPE='TEXT' NAME='waveDiff' VALUE='0.5' SIZE='5'/>");
		}
		
		if ( this.hasFormValue("relintDiff")) {
			myCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='relintDiff' VALUE='%s' SIZE='5'/>", this.getFormValue("relintDiff")));
		} else {
			myCell.addItem("<INPUT TYPE='TEXT' NAME='relintDiff' VALUE='0.05' SIZE='5'/>");
		}
		aRow.addItem(myCell);
		
		Table myTable = new Table(aRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		
		return myTable.toString();
	}
	*/
	private boolean checkNMRDerep() {
		Connection aDBC = null;
		boolean retVal = false;
		try {
			aDBC = AppConfigListener.getDBConnection();
			Statement aSth = aDBC.createStatement();
			ResultSet aResult = aSth.executeQuery("SELECT MATCH_SUBSTRUCT('C=O','C(N)C(=O)NC(C(C)C)C(=O)O')");
			if ( aResult.first() ) {
				retVal = ( aResult.getInt(1) == 1);
			}
			aSth.close();
		} catch (SQLException e) {
			this.log("SQL Error in NMR dereplication check.", e);
			retVal = false;
		}
		try {
			if ( aDBC != null && (! aDBC.isClosed()) ) 
				aDBC.close();
		} catch (SQLException e) {
			this.log("Failed to close DBC connection during NMR dereplication check.", e);
		}
		return retVal;
	}
	
/*	public String results(Compound compounds) {
		try {
			String[] vialHeaders = {"Name", "Formula", "Mass", "Structure"};
			TableCell vialHead = new TableHeader(vialHeaders);

			vialHead.setAttribute("class","header");
			TableRow vialRow = new TableRow(vialHead);
			Table vialTable = new Table(vialRow);
			vialTable.setAttribute("class","dashboard");
			vialTable.setAttribute("align","center");
			vialTable.setAttribute("width","75%");

			if ( compounds != null && compounds.first()) {
				String curClass = "odd";
				compounds.beforeFirst();
				while ( compounds.next() ) {			
					TableCell myCell = new TableCell();
					myCell.addItem(String.format("<A HREF='%s/compound?id=%s'>%s</A>",this.getContextPath(), compounds.getID(), compounds.getName()));	
					myCell.addItem(compounds.getHTMLFormula());
					myCell.addItem(String.format("%.5f", compounds.getMonoisotopicMass()));
					myCell.addItem(String.format("<IMG SRC=\"%s/compound/graphic/%s\" HEIGHT=150 WIDTH=150></TD></TR>", this.getContextPath(), compounds.getID()));
					TableRow aRow = new TableRow(myCell);
					aRow.setClass(curClass);
					aRow.setAttribute("align", "center");
					vialTable.addItem(aRow);
					if ( curClass.equals("odd") ) {
						curClass = "even";
					} else {
						curClass = "odd";
					}
				}
			} else {
				vialTable.addItem("<TR><TD COLSPAN=4 ALIGN='CENTER'><B><I>NONE</B></I></TD></TR>");
			}
			return vialTable.toString();

		} catch ( DataException e ) {
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
		}
	}
*/	
	private void addModules(List<String> newModules) {
		ListIterator<String> anIter = newModules.listIterator();
		while ( anIter.hasNext() ) {
			String className = anIter.next();
			try {
				Class aClass = Class.forName(className);
				if ( aClass != null ) {
					List<Class> interfaces = Arrays.asList(aClass.getInterfaces());
					if ( interfaces.contains(DereplicationForm.class)) {
						this.derepForms.add(aClass);
						this.log(String.format("LOADED dereplication module: %s", className));
					} else {
						this.log(String.format("Will NOT load module: %s. Does NOT implement DereplicationForm interface!", className));
					}
				}
			} catch (ClassNotFoundException e) {
				this.log(String.format("Could not load dereplication module: %s", className));
			}
		}
	}
	
	
	public String getHelpModule() {
		return HELP_MODULE;
	}
	
	public static StringBuffer getQuery(HttpServletRequest request) {
		Object query = request.getAttribute(DereplicationServlet.QUERY_ATTRIBUTE);
		if ( query == null ) {
			query = new StringBuffer();
			request.setAttribute(QUERY_ATTRIBUTE, query);
		}
		if ( query instanceof StringBuffer ) {
			return (StringBuffer)query;
		}
		return null;
	}
		

}
