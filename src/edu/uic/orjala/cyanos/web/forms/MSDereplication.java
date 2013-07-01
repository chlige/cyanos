/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.tools.MFAnalyser;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.DereplicationForm;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class MSDereplication extends BaseForm implements DereplicationForm {

	private static final double H_ADDUCT = 1.007825;
	private static final double Na_ADDUCT = 22.989769;
	private static final double NH4_ADDUCT = 18.0344;
	private static final double K_ADDUCT = 38.9637;
	
	private static final String WHERE_FORMAT = "ABS(%s - (%s %s %.5f)) < %.5f";
	
	/**
	 * @param callingServlet
	 */
	public MSDereplication(CyanosWrapper aWrapper) {
		super(aWrapper);
	}
	
	public String form() {
		StringBuffer myForm = new StringBuffer();
		
		Popup modePop = new Popup();
		modePop.addItemWithLabel("pos", "Positive Mode");
		modePop.addItemWithLabel("neg", "Negative Mode");
		modePop.setName("msMode");
		
		myForm.append("<P ALIGN='CENTER'>");
		myForm.append(modePop);
		if ( this.hasFormValue("mass")) {
			myForm.append(String.format(" <I>m/z</I> <INPUT TYPE='TEXT' NAME='mass' VALUE='%s'/>",this.getFormValue("mass")));
		} else {
			myForm.append("<I>m/z</I> <INPUT TYPE='TEXT' NAME='mass'/>");
		}
		
		if ( this.hasFormValue("diff")) {
			myForm.append(String.format(" &#x00B1 <INPUT TYPE='TEXT' SIZE='5' NAME='diff' VALUE='%s'/>",this.getFormValue("diff")));
		} else {
			myForm.append(" &#x00B1 <INPUT TYPE='TEXT' SIZE='5' NAME='diff' VALUE='10' />");
		}

		Popup diffPop = new Popup();
		diffPop.addItem("ppm");
		diffPop.addItem("Da");
		diffPop.setName("diffUnit");
		if ( this.hasFormValue("diffUnit"))
			diffPop.setDefault(this.getFormValue("diffUnit"));
		myForm.append(diffPop);
		
		myForm.append("<BR><B>Adducts</B><BR/>");
		myForm.append(this.adductItems());
		return this.selectedDiv("msdata", "Mass Spec", myForm.toString()).toString();
	}
	
	public Compound compounds() throws DataException {
		Compound compoundList = SQLCompound.compoundsWhere(this.getSQLDataSource(), this.sqlWhere(""), SQLCompound.MONOISOTOPIC_MASS_COLUMN, SQLCompound.ASCENDING_SORT);
		return compoundList;
	}
	
	public String results() {
		try {
			Compound compounds = SQLCompound.compoundsWhere(this.getSQLDataSource(), this.sqlWhere(""), SQLCompound.MONOISOTOPIC_MASS_COLUMN, SQLCompound.ASCENDING_SORT);
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
					myCell.addItem(String.format("<A HREF='%s/compound?id=%s'>%s</A>",this.myWrapper.getContextPath(), compounds.getID(), compounds.getName()));	
					myCell.addItem(compounds.getHTMLFormula());
					myCell.addItem(String.format("%.5f", compounds.getMonoisotopicMass()));
					myCell.addItem(String.format("<IMG SRC=\"%s/compound/graphic/%s?width=150&height=150\" HEIGHT=150 WIDTH=150></TD></TR>", this.myWrapper.getContextPath(), compounds.getID()));
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
	
	public String sqlWhere(String tableAlias) {
		if ( ! this.hasFormValue("msdata") ) return null;
		String colName = SQLCompound.MONOISOTOPIC_MASS_COLUMN;
		if ( tableAlias != null && tableAlias.length() > 0 )
			colName = String.format("%s.%s", tableAlias, colName);
		String sign = "+";
		if ( this.getFormValue("msMode").equals("neg") ) {
			sign = "-";
		}
		
		if ( this.getFormValue("mass").equals(""))
			return null;
		
		double mass = Double.parseDouble(this.getFormValue("mass"));
		
		double diff = 0.0f;
		
		if ( this.getFormValue("diffUnit").equals("Da") ) {
			diff = Double.parseDouble(this.getFormValue("diff"));
		} else if ( this.hasFormValue("diff") ) {
			diff = ( Double.parseDouble(this.getFormValue("diff")) * mass ) / 1000000;
		} else {
			diff = ( 10 * mass ) / 1000000;
		}
				
		StringBuffer output = new StringBuffer(String.format("%s IS NOT NULL AND (", colName));
		List<String> whereList = new ArrayList<String>();
		
		if ( this.hasFormValue("adduct") ) {
			List<String> aList = Arrays.asList( this.getFormValues("adduct") );
			String[] names = { "M", "H", "Na", "NH4", "K"};
			double[] adducts = { 0.0f, H_ADDUCT, Na_ADDUCT, NH4_ADDUCT, K_ADDUCT };
			for (int i=0; i < names.length; i++) {
				if ( aList.contains(names[i]) )
					whereList.add(String.format(WHERE_FORMAT, mass, colName, sign, adducts[i], diff));
			}
		} else {
			whereList.add(String.format(WHERE_FORMAT, mass, colName, sign, 0.0f, diff));
		}
		
		if ( this.hasFormValue("customAdduct") && this.getFormValue("customAdduct").length() > 0 ) {
			Molecule aMolecule = new Molecule();
			MFAnalyser anAnalyzer = new MFAnalyser(this.getFormValue("customAdduct"), aMolecule);
			if ( anAnalyzer != null ) {
				whereList.add(String.format(WHERE_FORMAT, mass, colName, sign, anAnalyzer.getMass(), diff));
			}
		}
		
		ListIterator<String> anIter = whereList.listIterator();
		if ( anIter.hasNext() ) {
			output.append( anIter.next() );

			while ( anIter.hasNext() ) {
				output.append(" OR ");
				output.append(anIter.next());
			}

			output.append(")");

			return output.toString();
		}
		return null;
	}
	
	private String adductItems() {
		String[] labels = {"M","H", "Na", "NH<SUB>4</SUB>", "K"};
		String[] names = { "M","H", "Na", "NH4", "K"};
		TableCell myCell = new TableCell();
		List aList;
		if ( this.hasFormValue("adduct") ) {
			aList = Arrays.asList( this.getFormValues("adduct") );
		} else {
			aList = new ArrayList();
		}
		
		for (int i=0; i < names.length; i++) {
			if ( aList.contains(names[i]) ) {
				myCell.addItem(String.format("%s<INPUT TYPE='CHECKBOX' NAME='adduct' VALUE='%s' CHECKED/>", labels[i], names[i]));
			} else {
				myCell.addItem(String.format("%s<INPUT TYPE='CHECKBOX' NAME='adduct' VALUE='%s'/>", labels[i], names[i]));
			}
		}
		if ( this.hasFormValue("customAdduct")) {
			myCell.addItem(String.format("Custom: <INPUT TYPE='TEXT' NAME='customAdduct' VALUE='%s'/>",this.getFormValue("customAdduct")));
		} else {
			myCell.addItem("Custom: <INPUT TYPE='TEXT' NAME='customAdduct'/>");
		}
		TableRow myRow = new TableRow(myCell);
		myRow.setAttribute("ALIGN", "CENTER");
		Table aTable = new Table(myRow);
		aTable.setClass("species");
		aTable.setAttribute("ALIGN", "CENTER");
		return aTable.toString();
	}

	public String formID() {
		return "ms";
	}

	public String formTitle() {
		return "Mass Spec";
	}
}
