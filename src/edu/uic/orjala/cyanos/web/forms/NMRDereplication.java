/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.DereplicationForm;
import edu.uic.orjala.cyanos.web.html.Image;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableRow;


/**
 * @author George Chlipala
 *
 */
public class NMRDereplication extends BaseForm implements DereplicationForm {
	
	private static final String C_METHYL = "[CH0X4][CH3X4]";
	private static final String CH_METHYL = "[CHX4][CH3X4]";
	private static final String CH2_METHYL = "[CH2X4][CH3X4]";
	private static final String OME = "[O][CH3X4]";
	private static final String NME = "[N][CH3X4]";

	private static final String EXO_METHYLENE = "[CH2X3]=C";
	private static final String ENE_DISUB = "[CHX3]=[CHX3]";
	private static final String EXO_VINYL = "[CH2X3]=[CHX3]";
	
	private static final String ALDEHYDE = "[CH]=O";
	private static final String AROMATIC_RING = "[cH]";
	private static final String MONO_SUB_AROMATIC = "[aH0]1[cH1][cH1][cH1][cH1][cH1]1";

	/*
	private static final String ENE = "C=C";
	private static final String SP2_CARBON = "[$([cX3]),$([CX3]=*)]";
	*/
	// Substituted aromatics a(-A)a(-A)aa(-A)aa(-A)


	/**
	 * @param callingServlet
	 */
	public NMRDereplication(CyanosWrapper aWrapper) {
		super(aWrapper);
	}
	
	public String form() {
		StringBuffer output = new StringBuffer();
		
		output.append(this.methylTable());
		output.append(this.sp2Table());
		output.append(this.aromTable());
		output.append(this.specialTable());
		return this.selectedDiv("nmrdata", "NMR Data", output.toString()).toString();
	}
	
	private String methylTable() {
		String[] titles = {"Triplet<BR/>(-CH<sub>2</sub>-<B>CH<sub>3</sub></B>)", "Doublet<BR/>(-CH-<B>CH<sub>3</sub></B>)", "Singlet<BR/>(-C-<B>CH<sub>3</sub></B>)",
				"-O-<B>CH<sub>3</sub></B>", "-N-<B>CH<sub>3</sub></B>" };
		String[] names = { "ch2_methyl", "ch_methyl", "c_methyl", "ome", "nme" };
		return "<B>Methyl</B></BR>" + this.derepTable(titles, names);
	}

	private String sp2Table() {
		String[] titles = {"-C=<B>CH<sub>2</sub></B>", "-CH=<B>CH</B>", "-CH=<B>CH<sub>2</sub></B>" };
		String[] names = { "exo_me", "ene_disub", "exo_vinyl" };
		return "<B>sp2 Carbons</B></BR>" +this.derepTable(titles, names);
	}
	
	private String aromTable() {
		Image subImage = this.getImage("arom_sub.png");
		Table aTable = new Table("<TR ALIGN='CENTER'><TD>Aromatic Protons</TD><TD>Substitutions</TD><TD ROWSPAN=2>");
		aTable.addItem(subImage.toString());
		aTable.addItem("</TD></TR>");

		TableCell myCell = new TableCell(this.inputField("arom"));
		
		StringBuffer subChecks = new StringBuffer();
		List<String> aromSubs;
		if ( this.hasFormValue("arom_sub")) {
			aromSubs = Arrays.asList( this.getFormValues("arom_sub") );
		} else {
			aromSubs = new ArrayList<String>();
		}
		
		if ( aromSubs.contains("0")) {
			subChecks.append("<INPUT TYPE='CHECKBOX' NAME='arom_sub' VALUE='0' CHECKED />unsubstituted<BR/>");
		} else {
			subChecks.append("<INPUT TYPE='CHECKBOX' NAME='arom_sub' VALUE='0' />unsubstituted<BR/>");			
		}
		
		for ( int i = 2; i < 7; i++ ) {
			if ( aromSubs.contains(String.format("%d", i)) ) {
				subChecks.append(String.format("<INPUT TYPE='CHECKBOX' NAME='arom_sub' VALUE='%d' CHECKED />%d ", i, i));
			} else {
				subChecks.append(String.format("<INPUT TYPE='CHECKBOX' NAME='arom_sub' VALUE='%d' />%d ", i, i));
			}
		}
		myCell.addItem(subChecks.toString());
		TableRow myRow = new TableRow(myCell);
		myRow.setAttribute("ALIGN", "CENTER");
		
		aTable.addItem(myRow);
		aTable.setClass("species");
		return "<B>Aromatic Rings</B></BR>" + aTable.toString();
	}	
	
	private String specialTable() {
		String[] titles = {"-C-<B>CHO</B>" };
		String[] names = { "aldehyde" };
		return "<B>Special</B></BR>" + this.derepTable(titles, names);
	}	
	
	private String inputField(String name) {
		if ( this.hasFormValue(name)) {
			 return String.format("<INPUT TYPE='TEXT' SIZE='5' NAME='%s' VALUE='%s'/>", 
					name, this.getFormValue(name));				
		} else {
			return String.format("<INPUT TYPE='TEXT' SIZE='5' NAME='%s'/>", name);
		}
	}
	
	private String derepTable(String[] titles, String[] names) {
		TableCell myCell = new TableCell(titles);
		TableRow myRow = new TableRow(myCell);
		myRow.setAttribute("ALIGN", "CENTER");
		myCell = new TableCell();
		for ( int i = 0; i < names.length; i++) {
			myCell.addItem(this.inputField(names[i]));
		}
		myRow.addItem(myCell);
		Table aTable = new Table(myRow);
		aTable.setClass("species");
		return aTable.toString();
	}
	
	public Compound compounds() throws DataException {
		Compound compoundList = SQLCompound.compoundsWhere(this.getSQLDataSource(), this.sqlWhere(""), SQLCompound.MONOISOTOPIC_MASS_COLUMN, SQLCompound.ASCENDING_SORT);
		return compoundList;
	}

	public String sqlWhere(String tableAlias) {
		if ( ! this.hasFormValue("nmrdata") ) return null;
		String colName = SQLCompound.SMILES_COLUMN;
		if ( tableAlias != null && tableAlias.length() > 0 )
			colName = String.format("%s.%s", tableAlias, colName);
		
		StringBuffer output = new StringBuffer(String.format("%s IS NOT NULL AND (", colName));
		List<String> whereList = new ArrayList<String>();
		
		String[] names = { "ch2_methyl", "ch_methyl", "c_methyl", "ome", "nme", "exo_me", "ene_disub", "exo_vinyl", "aldehyde", "arom" };
		String[] smarts = { CH2_METHYL, CH_METHYL, C_METHYL, OME, NME, EXO_METHYLENE, ENE_DISUB, EXO_VINYL, ALDEHYDE,  AROMATIC_RING };
		
		for (int i = 0; i < names.length; i++) {
			if ( this.hasFormValue(names[i])) {
				String value = this.getFormValue(names[i]);
				if ( value.endsWith("+") ) {
					whereList.add(String.format("SUBSTRUCT_COUNT('%s',%s) >= %d", smarts[i], colName, Integer.parseInt(value.substring(0, value.length() - 1))));
				} else if ( value.matches("([0-9]+)\\-([0-9]+)") ) {
					String[] vals = value.split("\\-");
					whereList.add(String.format("(SUBSTRUCT_COUNT('%s',%s) >= %d AND SUBSTRUCT_COUNT('%s',%s) <= %d)", smarts[i], colName, Integer.parseInt(vals[0]), 
							smarts[i], colName, Integer.parseInt(vals[1])));
				} else if ( value.length() > 0 ) {
					whereList.add(String.format("SUBSTRUCT_COUNT('%s',%s) = %d", smarts[i], colName, Integer.parseInt(value)));
				}
			}
		}
		
		if ( this.hasFormValue("arom_sub") ) {
			// Substituted aromatics a(-A)a(-A)aa(-A)aa(-A)
			List<String> aromSubs = Arrays.asList( this.getFormValues("arom_sub") );
			StringBuffer ring = new StringBuffer("[aH0]1");
			for ( int i = 2; i < 7; i++ ) {
				if ( aromSubs.contains(String.format("%d", i)) ) {
					ring.append("[aH0]");
				} else {
					ring.append("[cH1]");
				}
			}
			ring.append("1");
			whereList.add(String.format("MATCH_SUBSTRUCT('%s',%s) = 1", ring.toString(), colName));
			if ( aromSubs.contains("0") ) {
				whereList.add(String.format("MATCH_SUBSTRUCT('%s',%s) = 1", MONO_SUB_AROMATIC, colName));
			}
		}
		
		ListIterator<String> anIter = whereList.listIterator();
		if ( anIter.hasNext() ) {
			output.append( anIter.next() );

			while ( anIter.hasNext() ) {
				output.append(" AND ");
				output.append(anIter.next());
			}

			output.append(")");

			return output.toString();
		}
		return null;
	}
	
	public String formID() {
		return "nmr";
	}

	public String formTitle() {
		return "NMR Data";
	}
	
}
