/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Arrays;

import javax.xml.soap.SOAPException;

import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.openscience.cdk.Atom;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.renderer.Renderer2DModel;
import org.openscience.cdk.renderer.SimpleRenderer2D;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.HydrogenAdder;
import org.openscience.cdk.tools.MFAnalyser;
import org.openscience.cdk.tools.ValencyChecker;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.CompoundObject;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.InchiGenerator;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Image;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
@Deprecated
public class CompoundForm extends BaseForm {

	private static final String FIELD_NEW_COMPOUND_ID = "newCmpdId";
	private static final String FIELD_NOTES = "notes";
	private static final String FIELD_FILE_FORMAT = "format";
	private static final String FIELD_MONO_MASS = "mono_mass";
	private static final String FIELD_AVG_MASS = "avg_mass";
	private static final String FIELD_FORMULA = "formula";
	private static final String FIELD_SMILES_STRING = "smiles_string";
	private static final String FIELD_INCHI_STRING = "inchi_string";
	private static final String FIELD_INCHI_KEY = "inchi_key";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_USE_INCHI_KEY = "use_inchi";
	private static final String MDL_FILE = "mdlFile";

	private static final String FORMAT_CML = "cml";
	private static final String FORMAT_MDL = "mdl";

//	private static final String UV_DIV_TITLE = "UV Data";
//	private static final String UV_DIV_ID = "uvPeaks";

	public final static String ADD_ACTION = "addCompound";
	public final static String UPDATE_ACTION = "updateCompound";
	public static final String DATA_FORM = "dataForm";
	private Image csImg;
	
	private static final String CS_BUTTON_IMG = "icons/cs-icon.png";
//	private static final String CS_BUTTON_IMG = "icons/cs-button-white.png";
//	private static final String CS_BUTTON_IMG = "icons/cs-button-grad.png";
	
	public CompoundForm(CyanosWrapper callingServlet) {
		super(callingServlet);
	}

	public String quickView(Compound aCompound) {
		TableCell myCell = new TableCell("Compound ID:");
		myCell.addItem(aCompound.getID());
		TableRow myRow = new TableRow(myCell);
		
		try {
			myCell = new TableCell("Name:");
			myCell.addItem(aCompound.getName());
			myRow.addItem(myCell);
			
			myCell = new TableCell("Formula:");
			myCell.addItem(aCompound.getFormula());
			myRow.addItem(myCell);
			
			myCell = new TableCell("Formula Weight:");
			myCell.addItem(String.format("%.2f Da", aCompound.getAverageMass()));
			myRow.addItem(myCell);
			
			myCell = new TableCell("Notes:");
			String notes = aCompound.getNotes();
				if ( notes != null ) 
				myCell.addItem(notes.replaceAll("\n", "<BR>"));
			else
				myCell.addItem("");
				
			myRow.addItem(myCell);
			
		} catch ( DataException e ) {
			e.printStackTrace();
			myRow.addItem("<TD COLSPAN='3'><B><FONT COLOR='red'>SQL ERROR: </B>" + e.getMessage() + "</B></TH>");
		}
		
		
		Table myTable = new Table(myRow);
		myTable.setAttribute("class","list");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");
		return myTable.toString();
	}
	
	public String compoundForm(Compound aCompound) {
		TableRow myRow = new TableRow();
		try {
			myRow.addItem(this.makeFormTextRow("Name:", FIELD_NAME, aCompound.getName()));
			boolean hasMDLData = aCompound.hasMDLData();
			if ( hasMDLData ) {
				Popup typePop = new Popup();
				typePop.addItemWithLabel(FORMAT_MDL, "MDL Format");
				typePop.addItemWithLabel(FORMAT_CML, "CML Format");
				typePop.setName(FIELD_FILE_FORMAT);
				myRow.addItem(String.format("<TD>Structure:</TD><TD>%s <INPUT TYPE='FILE' NAME='%s' SIZE=25/></TD>", typePop.toString(), MDL_FILE));
			} else {
				myRow.addItem("<TD COLSPAN=2><BUTTON TYPE='SUBMIT' NAME='clearMDL'>Clear MDL Data</BUTTON></TD>");
			}
			myRow.addItem(this.makeFormTextRow("Formula:", FIELD_FORMULA, aCompound.getFormula()));
			myRow.addItem(this.makeFormTextRow("Formula Weight:", FIELD_AVG_MASS, String.format("%.4f", aCompound.getAverageMass())));
			myRow.addItem(this.makeFormTextRow("Monoisotopic Mass:", FIELD_MONO_MASS, String.format("%.5f", aCompound.getMonoisotopicMass())));
			myRow.addItem(this.makeFormTextAreaRow("SMILES String:", "smiles", aCompound.getSmilesString(), 50, 3));
			if ( hasMDLData ) 
				myRow.addItem("<TD COLSPAN=2><BUTTON TYPE='SUBMIT' NAME='genstring' VALUE='smiles'>Generate SMILES string from MDL data</BUTTON></TD>");
			myRow.addItem(this.makeFormTextAreaRow("InChi Key:", FIELD_INCHI_KEY, aCompound.getInChiKey(), 50, 2));
			myRow.addItem(this.makeFormTextAreaRow("InChi String:", FIELD_INCHI_STRING, aCompound.getInChiString(), 50, 3));
			if ( hasMDLData ) {
				myRow.addItem("<TD COLSPAN=2><BUTTON TYPE='SUBMIT' NAME='genstring' VALUE='inchi'>Generate InChi values from MDL data</BUTTON></TD>");
				myRow.addItem("<TD COLSPAN=2><BUTTON TYPE='SUBMIT' NAME='genstring' VALUE='both'>Generate SMILES & InChi values from MDL data</BUTTON></TD>");
			}
			myRow.addItem(this.makeFormTextAreaRow("Notes:", FIELD_NOTES, aCompound.getNotes(), 50, 7));
			myRow.addItem("<TD COLSPAN=2 ALIGN='CENTER'><BUTTON TYPE='SUBMIT' NAME='updateCompound' tabindex=0/>Update</BUTTON><BUTTON TYPE='RESET'>Reset Form</BUTTON></TD>");
		} catch (DataException e) {
			TableCell aCell = new TableCell(this.handleException(e));
			aCell.setAttribute("COLSPAN", "2");
			myRow.addItem(aCell);
		
		}
		Table myTable = new Table(myRow);
		myTable.setAttribute("class","list");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");

		return myTable.toString();
	} 

	public String showCompound(Compound aCompound) {
		Div compoundDiv = new Div();
		Div formDiv = new Div(compoundDiv);

		if ( aCompound.isAllowed(Role.WRITE) ) {
			if ( this.hasFormValue("updateCompound") ) {
				Div messageDiv = new Div(this.setCompoundValues(aCompound));
				messageDiv.setClass("messages");
				compoundDiv.addItem(messageDiv);
			} else if ( this.hasFormValue("clearMDL") ) {
				Div messageDiv = new Div("<P><B><FONT COLOR='orange'>NOTICE:</FONT> Removing MDL data.</B></P>");
				messageDiv.setClass("messages");
				try {
					aCompound.clearMDLData();
				} catch (DataException e) {
					messageDiv.addItem(this.handleException(e));
				}
				compoundDiv.addItem(messageDiv);
			} else if ( this.hasFormValue("genstring") ) {
				String type = this.getFormValue("genstring");
				boolean genSmiles = ( type.equals("smiles") || type.equals("both") );
				boolean genInChi = ( type.equals("inchi") || type.equals("both") );

				Div messageDiv = new Div("<P><B><FONT COLOR='orange'>NOTICE:</FONT> Updating values.</B></P>");
				messageDiv.setClass("messages");
				compoundDiv.addItem(messageDiv);

				if ( genInChi ) {
					messageDiv.addItem("<P>InChi values: String:");
					try {
						aCompound.setInChiString(InchiGenerator.convertMOL(aCompound.getMDLData()));
						messageDiv.addItem("SUCCESS Key:");
						aCompound.setInChiKey(InchiGenerator.getInChiKey(aCompound.getInChiString()));
						messageDiv.addItem("SUCCESS");
					} catch (UnsupportedOperationException e) {
						messageDiv.addItem(this.handleException(e));
					} catch (DataException e) {
						messageDiv.addItem(this.handleException(e));
					} catch (SOAPException e) {
						messageDiv.addItem(this.handleException(e));
					}
					messageDiv.addItem("</P>");
				}
				
				if ( genSmiles ) {
					messageDiv.addItem("<P>SMILES: ");
					try {
						MDLReader aReader = new MDLReader(aCompound.getMDLDataStream());
						ChemFile aCMLFile = (ChemFile) aReader.read(new ChemFile());									
						if ( aCMLFile.getChemSequenceCount() > 1 ) {
							messageDiv.addItem("<B><FONT COLOR='orange'>WARNING:</FONT> Multiple molecules in this file. Using first molecule</B>");
						}
						Molecule myMolecule = (Molecule) aCMLFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);
						this.setSMILESFromMolecule(aCompound, myMolecule);
						messageDiv.addItem("SUCCESS");						
					} catch (DataException e) {
						messageDiv.addItem(this.handleException(e));
					} catch (CDKException e) {
						messageDiv.addItem(this.handleException(e));
					}
				}
				
			}

			Div editDiv = new Div(this.compoundForm(aCompound));
			editDiv.setID("edit_details");
			editDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"details\")'>Close Form</BUTTON></P>");
			editDiv.setClass("hideSection");
			compoundDiv.addItem(editDiv);

		}
		
		Div textDiv = new Div(this.compoundView(aCompound));
		textDiv.setID("view_details");
		textDiv.setClass("showSection");
		compoundDiv.addItem(textDiv);
		
		if ( aCompound.isAllowed(Role.WRITE) ) {
			textDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"details\")'>Edit Values</BUTTON></P>");
			try {
				if ( aCompound.hasMDLData() ) { 
					String smilesString = aCompound.getSmilesString();
					boolean hasSmiles = ( smilesString != null && smilesString.length() > 0);
					String inchiString = aCompound.getInChiString();
					boolean hasInChI = ( inchiString != null && inchiString.length() > 0 );
					if ( ! hasSmiles )
						textDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='SUBMIT' NAME='genstring' VALUE='smiles'>Generate SMILES string from MDL data</BUTTON></P>");
					if ( ! hasInChI )
						textDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='SUBMIT' NAME='genstring' VALUE='inchi'>Generate InChi values from MDL data</BUTTON></P>");
					if ( ! ( hasSmiles || hasInChI ) )
						textDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='SUBMIT' NAME='genstring' VALUE='both'>Generate SMILES & InChi values from MDL data</BUTTON></P>");
				}
			} catch (DataException e) {
				textDiv.addItem(this.handleException(e));
			}

			
			Form aForm = new Form("<SPAN STYLE='display:none'><BUTTON TYPE='SUBMIT' NAME='updateCompound'/></BUTTON></SPAN>");
			aForm.addItem(formDiv);
			aForm.setName("compoundInfo");
			aForm.setPost();
			aForm.addHiddenValue("id", aCompound.getID());
			try {
				if ( ! aCompound.hasMDLData() ) aForm.setAttribute("ENCTYPE", "multipart/form-data");
			} catch (DataException e) {
				aForm.addItem("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
				e.printStackTrace();
			}
			return aForm.toString();
		} else {
			return compoundDiv.toString();
		}
	}

	public String compoundView(Compound aCompound) {
		try {
			TableCell myCell = new TableCell("Name:");
			myCell.addItem(aCompound.getName());
			TableRow myRow = new TableRow(myCell);

			myCell = new TableCell("Formula:");
			myCell.addItem(aCompound.getHTMLFormula());
			myRow.addItem(myCell);

			myCell = new TableCell("Formula Weight:");
			myCell.addItem(String.format("%.4f", aCompound.getAverageMass()));
			myRow.addItem(myCell);

			myCell = new TableCell("Monoisotopic Mass:");
			myCell.addItem(String.format("%.5f", aCompound.getMonoisotopicMass()));
			myRow.addItem(myCell);

			String inchiKey = aCompound.getInChiKey();
			
			if ( inchiKey != null ) {
				myCell = new TableCell("InChi Key:");
/*
				Image csImg = this.myWrapper.getImage(CS_BUTTON_IMG);
				csImg.setAttribute("VALIGN", "MIDDLE");
				myCell.addItem(String.format("%s<BR><A CLASS='chemspider' HREF=\"http://www.chemspider.com/Search.aspx?q=%s\" target='_blank'>%s</A>", inchiKey, inchiKey, csImg.toString()));
*/
				myCell.addItem(String.format("%s<BR>%s", inchiKey, this.getChemSpiderButton(inchiKey)));
				myRow.addItem(myCell);				
			}
			
			myCell = new TableCell("Notes:");
			String notes = aCompound.getNotes();
			if ( notes != null ) {
				myCell.addItem(notes.replace("\n", "<BR/>"));
			} else {
				myCell.addItem("");
			}
			myRow.addItem(myCell);


			Table myTable = new Table(myRow);
			myTable.setAttribute("class","list");
			myTable.setAttribute("align","center");
			myTable.setAttribute("width","75%");
			Div textDiv = new Div(myTable);

			String smilesString = aCompound.getSmilesString();
			
			if ( smilesString != null ) {
				Div smilesDiv = new Div("<P ALIGN='CENTER'><B>SMILES String</B><BR/><TEXTAREA COLS=50 ROWS=5 WRAP='logical' READONLY>");
				smilesDiv.addItem(smilesString);
				smilesDiv.addItem("</TEXTAREA></P><P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"smiles\")'>Hide SMILES</BUTTON></P>");
				smilesDiv.setClass("hideSection");
				smilesDiv.setID("edit_smiles");

				textDiv.addItem("<DIV ID='view_smiles' CLASS='showSection'><P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"smiles\")'>Show SMILES</BUTTON></P></DIV>");
				textDiv.addItem(smilesDiv);
				textDiv.addItem("</P>");
			}
			
			String inchiString = aCompound.getInChiString();
			
			if ( inchiString != null ) {
				Div inchiDiv = new Div("<P ALIGN='CENTER'><B>InChi String</B><BR/><TEXTAREA COLS=50 ROWS=5 WRAP='logical' READONLY>");
				inchiDiv.addItem(inchiString);
				inchiDiv.addItem("</TEXTAREA></P><P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"inchi\")'>Hide InChi</BUTTON></P>");
				inchiDiv.setClass("hideSection");
				inchiDiv.setID("edit_inchi");

				textDiv.addItem("<DIV ID='view_inchi' CLASS='showSection'><P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"inchi\")'>Show InChi String</BUTTON></P></DIV>");
				textDiv.addItem(inchiDiv);
				textDiv.addItem("</P>");
			}
			

			if ( aCompound.hasMDLData() ) {
				Div structDiv = new Div();
				/*			Popup fileTypePop = new Popup();
			fileTypePop.addItemWithLabel(".cml", "CML File");
			fileTypePop.addItemWithLabel(".mol", "MDL Mol File");
			fileTypePop.setName("fileType");
				 */
				Table structTable = new Table(String.format("<TR><TD COLSPAN=2><IMG SRC=\"%s/compound/graphic/%s\" HEIGHT=300 WIDTH=300></TD></TR>", this.myWrapper.getContextPath(), aCompound.getID()));
				structTable.addItem("<TR><TD>Scale Image:</TD><TD><INPUT TYPE=TEXT SIZE=5 NAME='imgW' VALUE='500'> x <INPUT TYPE=TEXT SIZE=5 NAME='imgH' VALUE='500'/><BUTTON TYPE='BUTTON' onClick='showCompound(this.form.id.value, this.form.imgW.value, this.form.imgH.value)'>Generate</BUTTON></TD></TR>");
				structTable.addItem("<TR><TD COLSPAN='2' ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='exportCompound(this.form.id.value, \".mol\")'>Export Structure</BUTTON></TD></TR>");
//				structTable.addItem("<TR><TD>Structure:</TD><TD>" +
//				fileTypePop.toString() + 
//				"<BUTTON TYPE='BUTTON' onClick='exportCompound(this.form.id.value, this.form.fileType.value)'>Export</BUTTON></TD></TR>");
				structDiv.addItem(structTable);

				Div fullDiv = new Div(textDiv);
				textDiv.setClass("left70");
				fullDiv.addItem(structDiv);
				structDiv.setClass("right30");
				return fullDiv.toString();

			} else {
				return textDiv.toString();
			}
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	
	public String addCompoundForm() {
		TableCell myCell = new TableCell("Compound ID:");
		myCell.addItem("<INPUT TYPE='TEXT' NAME='newCmpdId' SIZE=30> <INPUT TYPE=CHECKBOX NAME='use_inchi' onClick='this.form.newCmpdId.disabled=this.checked'> Use InChI Key");
		TableRow myRow = new TableRow(myCell);
		myRow.addItem(this.makeFormTextRow("Name:", FIELD_NAME));
		myRow.addItem(this.makeFormTextRow("InChI Key:", FIELD_INCHI_KEY));
		myRow.addItem(this.makeFormTextAreaRow("InChI String:", FIELD_INCHI_STRING));
		myRow.addItem(this.makeFormTextAreaRow("SMILES String:", FIELD_SMILES_STRING));
		myRow.addItem(this.makeFormTextRow("Formula:", FIELD_FORMULA));
		myRow.addItem(this.makeFormTextRow("Formula Weight:", FIELD_AVG_MASS, "0.0000"));
		myRow.addItem(this.makeFormTextRow("Monoisotopic Mass:", FIELD_MONO_MASS, "0.00000"));
		Popup typePop = new Popup();
		typePop.addItemWithLabel(FORMAT_MDL, "MDL Format");
		typePop.addItemWithLabel(FORMAT_CML, "CML Format");
		typePop.setName(FIELD_FILE_FORMAT);
		myRow.addItem(String.format("<TD>Structure:</TD><TD>%s <INPUT TYPE='FILE' NAME='%s' SIZE=25/></TD>", typePop.toString(), MDL_FILE));
		myRow.addItem(this.makeFormTextAreaRow("Notes:", FIELD_NOTES));
		myRow.addItem("<TD COLSPAN=2 ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='addCompound' VALUE='Add'/><INPUT TYPE='RESET'/></TD>");

		Table myTable = new Table(myRow);
		myTable.setAttribute("class","list");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");
		return myTable.toString();
	}
	
	public String listCompounds(Compound compounds) {
		return this.listCompounds(compounds, false);
	}
	
	public String listCompounds(Compound compounds, boolean showStructure) {
		try {
			String[] vialHeaders = {"Name", "Formula", "Notes", "Links"};
			TableCell vialHead = new TableHeader(vialHeaders);
			if ( showStructure )
				vialHead.addItem("Structure");

			vialHead.setAttribute("class","header");
			TableRow vialRow = new TableRow(vialHead);
			Table vialTable = new Table(vialRow);
			vialTable.setAttribute("class","dashboard");
			vialTable.setAttribute("align","center");
			vialTable.setAttribute("width","75%");

			if ( compounds != null && compounds.first()) {
				boolean oddRow = true;
				compounds.beforeFirst();
				while ( compounds.next() ) {			
					try {
						TableCell myCell = this.getRow(compounds);
						if ( showStructure ) {
							myCell.addItem(String.format("<IMG SRC=\"%s/compound/graphic/%s\" HEIGHT=150 WIDTH=150></TD></TR>", 
									this.myWrapper.getContextPath(), compounds.getID()));
						}
						TableRow aRow = new TableRow(myCell);
						if ( oddRow ) {
							aRow.setClass("odd");
							oddRow = false;
						} else {
							aRow.setClass("even");
							oddRow = true;
						}
						aRow.setAttribute("align", "center");
						vialTable.addItem(aRow);
					} catch (DataException e) {
						vialTable.addItem(String.format("<TR><TD COLSPAN=%d ALIGN='CENTER'>%s</TD></TR>", (showStructure ? 4 : 5), e.getLocalizedMessage()));
						e.printStackTrace();
					}
				}
			} else {
				vialTable.addItem(String.format("<TR><TD COLSPAN=%d ALIGN='CENTER'><B><I>NONE</B></I></TD></TR>", (showStructure ? 4 : 5)));
			}
			return vialTable.toString();

		} catch ( DataException e ) {
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
		}

	}

	public String listCompounds(CompoundObject source) {
		try {
			String[] vialHeaders = {"Name", "Formula", "Notes", "Links", "Retention Time"};
			TableCell vialHead = new TableHeader(vialHeaders);

			vialHead.setAttribute("class","header");
			TableRow vialRow = new TableRow(vialHead);
			Table vialTable = new Table(vialRow);
			vialTable.setAttribute("class","dashboard");
			vialTable.setAttribute("align","center");
			vialTable.setAttribute("width","75%");
			Compound compounds = source.getCompounds();
			
			if ( compounds != null && compounds.first()) {
				boolean oddRow = true;
				compounds.beforeFirst();
				while ( compounds.next() ) {			
					try {
						TableCell myCell = this.getRow(compounds);
						BigDecimal rt = compounds.getRetentionTime(source);	
						if ( rt != null ) {
							myCell.addItem(String.format("%.2f min", rt.doubleValue()));
						} else {
							myCell.addItem("-");
						}
						
						TableRow aRow = new TableRow(myCell);
						if ( oddRow ) {
							aRow.setClass("odd");
							oddRow = false;
						} else {
							aRow.setClass("even");
							oddRow = true;
						}
						aRow.setAttribute("align", "center");
						vialTable.addItem(aRow);
					} catch (DataException e) {
						vialTable.addItem(String.format("<TR><TD COLSPAN=5 ALIGN='CENTER'>%s</TD></TR>", e.getLocalizedMessage()));
						e.printStackTrace();
					}
				}
			} else {
				vialTable.addItem("<TR><TD COLSPAN=5 ALIGN='CENTER'><B><I>NONE</B></I></TD></TR>");
			}
			return vialTable.toString();

		} catch ( DataException e ) {
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
		}
	}
	
	private Image getChemSpiderImg() {
		if ( csImg == null ) {
			csImg = this.myWrapper.getImage(CS_BUTTON_IMG);
			csImg.setAttribute("VALIGN", "MIDDLE");
		}
		return csImg;
	}
	
	private String getChemSpiderButton(Compound compound) throws DataException {
		return this.getChemSpiderButton(compound.getInChiKey());
	}
	
	private String getChemSpiderButton(String inchiKey) {
		if ( inchiKey != null ) {
			return String.format("<A CLASS='chemspider' HREF=\"http://www.chemspider.com/Search.aspx?q=%s\" target='_blank'>%s Search ChemSpider</A>", 
				inchiKey, getChemSpiderImg().toString());	
		} else {
			return "";
		}
	}
	
	private TableCell getRow(Compound compound) throws DataException {
		String name = compound.getName();
		if ( name == null || name.length() < 1) 
			name = compound.getID();
		TableCell myCell = new TableCell(String.format("<A HREF='%s/compound?id=%s'>%s</A>", 
					this.myWrapper.getContextPath(), compound.getID(), name));							
		myCell.addItem(compound.getHTMLFormula());
		myCell.addItem(shortenString(compound.getNotes(), 15));
		myCell.addItem(this.getChemSpiderButton(compound));
		return myCell;
	}
	
	public String listCompounds(Sample sample) {
		try {
			Compound compounds = sample.getCompounds();
			String[] vialHeaders = {"Name", "Formula", "Retention Time", "Links"};
			TableCell vialHead = new TableHeader(vialHeaders);

			boolean canEdit = this.isAllowed(User.SAMPLE_ROLE, sample.getProjectID(), Role.WRITE);
			
			String[] ids = this.getFormValues("compound");
			
			if ( canEdit ) {
				if ( ids != null && ids.length > 0 )
					Arrays.sort(ids);
				vialHead.addItem("");
			}		
			
			vialHead.setAttribute("class","header");
			TableRow vialRow = new TableRow(vialHead);
			Table vialTable = new Table(vialRow);
			vialTable.setAttribute("class","dashboard");
			vialTable.setAttribute("align","center");
			vialTable.setAttribute("width","75%");

			if ( compounds != null && compounds.first()) {
				boolean oddRow = true;
				compounds.beforeFirst();
				while ( compounds.next() ) {			
					String name = compounds.getName();
					if ( name == null || name.length() < 1) 
						name = compounds.getID();

					TableCell myCell;
					boolean removed = false;
					if ( canEdit && ids != null && ids.length > 0 && Arrays.binarySearch(ids, compounds.getID()) > -1) {
						myCell = new TableCell(name);							
						removed = true;
						myCell.setClass("removed");						
					} else {
						myCell = new TableCell(String.format("<A HREF='%s/compound?id=%s'>%s</A>", 
								this.myWrapper.getContextPath(), compounds.getID(), name));							
					}
					myCell.addItem(compounds.getHTMLFormula());
					BigDecimal rTime = compounds.getRetentionTime(sample);
					if ( rTime != null ) {
						myCell.addItem(String.format("%.2f min", rTime));
					} else {
						myCell.addItem("-");
					}
					myCell.addItem(this.getChemSpiderButton(compounds));
//					myCell.addItem(shortenString(compounds.getNotes(), 20));
					if ( canEdit ) {
						if ( removed ) {
							myCell.addItem("");
							sample.unlinkCompound(compounds);
						} else {
							myCell.addItem(String.format("<INPUT TYPE='CHECKBOX' NAME='compound' VALUE=\"%s\">", compounds.getID()));
						}
					}
					
					TableRow aRow = new TableRow(myCell);
					if ( oddRow ) {
						aRow.setClass("odd");
					} else {
						aRow.setClass("even");
					}
					oddRow = (! oddRow);
					aRow.setAttribute("align", "center");
					vialTable.addItem(aRow);
				}
				if ( canEdit ) {
					vialTable.addItem("<TR><TD COLSPAN=4></TD><TD ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick=\"reloadDiv('compounds',this)\" NAME='delCmpds'>Unlink</BUTTON></TD></TR>");
				}
			} else {
				vialTable.addItem("<TR><TD COLSPAN=3 ALIGN='CENTER'><B><I>NONE</B></I></TD></TR>");
			}
			Form myForm = new Form(vialTable);
			myForm.addHiddenValue("id", sample.getID());
			return myForm.toString();

		} catch ( DataException e ) {
			return this.handleException(e);
		}

	}
	
	public String newCompoundID() {
		if ( this.hasFormValue(FIELD_USE_INCHI_KEY) ) {
			if ( this.hasFormValue(FIELD_INCHI_KEY) && this.getFormValue(FIELD_INCHI_KEY).length() > 0)
				return this.getFormValue(FIELD_INCHI_KEY);
			if ( this.hasFormValue(FIELD_INCHI_STRING) && this.getFormValue(FIELD_INCHI_STRING).length() > 0) {
				try {
					return InchiGenerator.getInChiKey(this.getFormValue(FIELD_INCHI_STRING));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if ( this.hasMDLData() ) {
/*
 				FileUpload mdlUpload = this.myWrapper.getUpload(MDL_FILE);
				try {
					InchiGenerator iGen = new InchiGenerator(InchiGenerator.FORMAT_MOL, Streams.asString(mdlUpload.getStream()));
					return iGen.getKey();
				} catch (Exception e) {
					e.printStackTrace();
				}
*/
			}
			return null;
		} else 
			return this.getFormValue(FIELD_NEW_COMPOUND_ID);
	}
	
	public boolean isFullAddForm() {
		return this.hasFormValue(FIELD_NAME) && this.hasFormValue(FIELD_FORMULA) && this.hasFormValue(FIELD_NOTES);
	}
	
	private void setSMILESFromMolecule(Compound aCompound, Molecule aMolecule) throws DataException {
		SmilesGenerator smileGen = new SmilesGenerator();
		smileGen.setUseAromaticityFlag(true);
		aCompound.setSmilesString(smileGen.createSMILES(aMolecule));		
	}
	
	public void setValuesFromMolecule(Compound aCompound, Molecule aMolecule) throws DataException, IOException, ClassNotFoundException, CDKException {
		this.setSMILESFromMolecule(aCompound, aMolecule);
// For CDK v1.0.4
		HydrogenAdder hAdder = new HydrogenAdder(new ValencyChecker());
	    hAdder.addExplicitHydrogensToSatisfyValency(aMolecule);
	    MFAnalyser formulaMaker = new MFAnalyser(aMolecule);

//CDK v.1.2.3
/*		IMolecularFormula aFormula = MolecularFormulaManipulator.getMolecularFormula(aMolecule);

	    aCompound.setManualRefresh();
		aCompound.setName(this.getFormValue("name"));
*/
// for CDK v1.0.4		
	    aCompound.setFormula(formulaMaker.getMolecularFormula());
	    // TODO should set the proper sig figs.
	    aCompound.setAverageMass(new BigDecimal(formulaMaker.getNaturalMass()));
	    aCompound.setMonoisotopicMass(new BigDecimal(formulaMaker.getMass()));
// CDK v1.2.3
/*	    aCompound.setFormula(MolecularFormulaManipulator.getHillString(aFormula));
	    aCompound.setAverageMass(MolecularFormulaManipulator.getNaturalExactMass(aFormula));
	    aCompound.setMonoisotopicMass(MolecularFormulaManipulator.getMajorIsotopeMass(aFormula));
*/
	    aCompound.setNotes(this.getFormValue(FIELD_NOTES));
		if ( this.hasFormValue(FIELD_NAME) ) 
			aCompound.setName(this.getFormValue(FIELD_NAME));
		aCompound.refresh();
		aCompound.setAutoRefresh();
	}

	public String setCompoundValues(Compound aCompound) {
		return null;
		/*
		StringBuffer output = new StringBuffer();
		if ( aCompound.isAllowed(Role.WRITE) ) {
			try {
				if ( this.myWrapper.hasUpload(MDL_FILE) ) {
					output.append("<P><B><FONT COLOR='green'>Updating Compound Information</FONT> using MDL data.</B></P>");
					FileUpload mdlUpload = this.myWrapper.getUpload(MDL_FILE);
					try {
						ChemFile aCMLFile = new ChemFile();
						if ( mdlUpload.getContentType().equals("chemical/x-mdl-molfile") || ( this.getFormValue(FIELD_FILE_FORMAT).equals(FORMAT_MDL) && (!mdlUpload.getContentType().equals("chemical/x-cml"))) ) {
							MDLReader aReader = new MDLReader(mdlUpload.getStream());
							aCMLFile = (ChemFile) aReader.read(aCMLFile);									
						} else {
							CMLReader aCMLReader = new CMLReader(mdlUpload.getStream());
							aCMLFile = (ChemFile) aCMLReader.read(aCMLFile);									
						}
						if ( aCMLFile.getChemSequenceCount() > 1 ) {
							output.append("<P><B><FONT COLOR='orange'>WARNING:</FONT> Multiple molecules in this file. Using first molecule</B></P>");
						}
						Molecule myMolecule = (Molecule) aCMLFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);
						String mdlData = mdlUpload.getString();
						aCompound.setMDLData(mdlData);
						aCompound.setInChiString(InchiGenerator.getMOLKey(mdlData));
						aCompound.setInChiKey(InchiGenerator.getInChiKey(aCompound.getInChiString()));
						this.setValuesFromMolecule(aCompound, myMolecule);
					} catch (CDKException e) {
						output.append(this.handleException(e));
					} catch (IOException e) {
						output.append(this.handleException(e));
					} catch (ClassNotFoundException e) {
						output.append(this.handleException(e));
					} catch (UnsupportedOperationException e) {
						output.append(this.handleException(e));
					} catch (SOAPException e) {
						output.append(this.handleException(e));
					}
				} else {
					output.append("<P><B><FONT COLOR='green'>Updating Compound Information</FONT> using form data.</B></P>");
					aCompound.setManualRefresh();
					aCompound.setName(this.getFormValue(FIELD_NAME));
					if ( this.hasFormValue(FIELD_FORMULA) && this.getFormValue(FIELD_FORMULA).length() > 0 )
						aCompound.setFormula(this.getFormValue(FIELD_FORMULA));
					if ( this.hasFormValue(FIELD_AVG_MASS) )
						aCompound.setAverageMass(this.getFormValue(FIELD_AVG_MASS));
					if ( this.hasFormValue(FIELD_SMILES_STRING) && this.getFormValue(FIELD_SMILES_STRING).length() > 0 )
						aCompound.setSmilesString(this.getFormValue(FIELD_SMILES_STRING));
					if ( this.hasFormValue(FIELD_MONO_MASS) )
						aCompound.setMonoisotopicMass(this.getFormValue(FIELD_MONO_MASS));
					if ( this.hasFormValue(FIELD_INCHI_KEY) && this.getFormValue(FIELD_INCHI_KEY).length() > 0 )
						aCompound.setInChiKey(this.getFormValue(FIELD_INCHI_KEY));
					if ( this.hasFormValue(FIELD_INCHI_STRING) && this.getFormValue(FIELD_INCHI_STRING).length() > 0 )
						aCompound.setInChiKey(this.getFormValue(FIELD_INCHI_STRING));
				
					aCompound.setNotes(this.getFormValue(FIELD_NOTES));
					aCompound.refresh();
					aCompound.setAutoRefresh();
				}
			} catch (DataException e) {
				output.append(this.handleException(e));
			}
		} else {
			output.append("<P><B><FONT COLOR='red'>FORBIDDEN:</FONT> Insufficient permission to modify compound record.</B></P>");
		}
		return output.toString();
		*/
	}
	
	public boolean hasMDLData() {
		return this.myWrapper.hasUpload(MDL_FILE);
	}
	
	public FileUpload getMDLData() {
		return this.myWrapper.getUpload(MDL_FILE);
	}
	
	public String linkCompoundMenu(String param, String newparam, Compound compoundList) {
		Popup cmpdPop = new Popup();
		cmpdPop.addItemWithLabel("", "New Compound ->");
		try {
			compoundList.beforeFirst();
			while ( compoundList.next() ) {
				String name = compoundList.getName();
				if ( name == null || name.length() < 1) 
					name = compoundList.getID();
				cmpdPop.addItemWithLabel(compoundList.getID(), name);
			}
		} catch (DataException e ) {
			return "<FONT COLOR='RED'><B>SQL ERROR:</FONT>" + e.getMessage() + "</B>";
		}
		cmpdPop.setName(param);
		return cmpdPop.toString() + String.format("<INPUT TYPE='TEXT' NAME='%s'/>", newparam);
		
	}
	
	public String linkCompoundMenu(Compound compoundList) {
		return this.linkCompoundMenu("cmpdID", "newCmpdId", compoundList);
	}

	public String selectedCompoundID() {
		String myID = this.getFormValue("cmpdID");
		if ( myID != null & (! myID.equals("")) ) {
			return myID;
		} else if ( this.hasFormValue(FIELD_NEW_COMPOUND_ID) ) {
			return this.getFormValue(FIELD_NEW_COMPOUND_ID);
		}
		return null;
	}
	
	/*
	public Div uvDiv(Compound_UV aCompound) {
		return this.collapsableDiv(UV_DIV_ID, UV_DIV_TITLE, this.uvData(aCompound));
	}


	private void updateUV(Compound_UV aCompound) {
		if ( this.myServlet.hasUpload("uvFile") ) {
			FileItem uvUpload = this.myServlet.getUpload("uvFile");
			try {
				BufferedReader aReader = new BufferedReader(new InputStreamReader(uvUpload.getInputStream()));
				String aLine = aReader.readLine();
				if ( aLine.startsWith("##") ) {
					aCompound.setUVData(uvUpload.toString());
				} else {
					List<Double> waves = new ArrayList<Double>();
					List<Double> abs = new ArrayList<Double>();
					while ( aLine != null ) {
						String[] cols = aLine.split(",");
						waves.add(Double.valueOf(cols[0]));
						abs.add(Double.valueOf(cols[1]));
						aLine = aReader.readLine();
					}
					double[] xVals = new double[waves.size()];
					double[] yVals = new double[abs.size()];
					for ( int i = 0; i < waves.size(); i++ ) {
						xVals[i] = waves.get(i);
						yVals[i] = abs.get(i);
					}
					OrderedArrayData x = new OrderedArrayData(xVals, UVSpectrum.DEFAULT_XUNIT);
					ArrayData y = new ArrayData(yVals, UVSpectrum.DEFAULT_YUNIT);
					Spectrum uvSpec = new UVSpectrum(x, y);
					JCAMPWriter jcampOut = JCAMPWriter.getInstance();
					try {
						aCompound.setUVData(jcampOut.toJCAMP(uvSpec));
					} catch (JCAMPException e) {
						statusDiv.addItem(this.handleException(e));
					}
				}
			} catch (IOException e) {
				statusDiv.addItem(this.handleException(e));
			}

		}
	}

	
	private String uvData(Compound_UV compound) {
		TableCell headers = new TableHeader("Wavelength (nm)");
		headers.addItem("Rel. Intensity");
		TableRow aRow = new TableRow(headers);
		try {
			Map<Float,Float> uvPeaks = compound.getUVPeaks();
			SortedSet<Float> waves = new TreeSet<Float>(uvPeaks.keySet());
			Iterator<Float> wavIter = waves.iterator();
			while ( wavIter.hasNext() ) {
				Float aWave = wavIter.next();
				TableCell myCell = new TableCell(String.format("%.1f", aWave));
				myCell.addItem(String.format("%.4f", uvPeaks.get(aWave)));
				aRow.addItem(myCell);
			}
		} catch (DataException e) {
			aRow.addItem("<TD COLSPAN=2>" + this.handleException(e) + "</TR>");
		}		
		Table myTable = new Table(aRow);
		myTable.setClass("species");
		return myTable.toString();
	}

	public BufferedImage drawUVSpectrum(Compound_UV aCompound) {
		int width = 400;
		int height = 300;
		
		BufferedImage anImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gf = anImage.createGraphics();
		gf.setBackground(Color.WHITE);
		gf.setColor(Color.BLACK);
		
		try {
			if ( aCompound.hasUVData() ) {
				JCAMPReader aReader = JCAMPReader.getInstance();
				UVSpectrum aSpec = (UVSpectrum)aReader.createSpectrum(aCompound.getUVData());
				Range1D.Double range = aSpec.getXFullViewRange();
				gf.setFont(new Font("SansSerif", Font.PLAIN, 8));
				gf.drawString(String.format("%.0f", range.getXMin()), 10, 380);
				double specWidth = range.getXWidth();
				//TODO need to draw spectrum
			}
		} catch (DataException e) {
			gf.drawString("ERROR", 10, 10);
			this.handleException(e);
		} catch (JCAMPException e) {
			gf.drawString("ERROR", 10, 10);
			this.handleException(e);
		}

		return anImage;
	}
	*/

	public BufferedImage drawMolecule(Compound aCompound) {
		int width = 300;
		int height = 300;
		if ( this.hasFormValue("width"))
			width = Integer.parseInt(this.getFormValue("width"));
		if ( this.hasFormValue("height"))
			height = Integer.parseInt(this.getFormValue("height"));
		BufferedImage anImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gf = anImage.createGraphics();
		gf.setBackground(Color.WHITE);
		gf.setColor(Color.BLACK);
		try {
			if ( aCompound.first() ) {
				if ( aCompound.hasMDLData() ) {
					MDLReader aReader = new MDLReader(aCompound.getMDLDataStream());
					ChemFile aChemFile = new ChemFile();
					try {
						aChemFile = (ChemFile) aReader.read(aChemFile);
//						Molecule myMolecule = (Molecule) aChemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);	

						
						Molecule myMolecule = (Molecule) AtomContainerManipulator.removeHydrogens(aChemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0));
						int atomCount = myMolecule.getAtomCount();
						Atom anAtom;
						HydrogenAdder hAdder = new HydrogenAdder("org.openscience.cdk.tools.ValencyChecker");
						for ( int a = 0; a < atomCount; a++ ) {
							anAtom = (Atom) myMolecule.getAtom(a);
							int aNumber = anAtom.getAtomicNumber();
							switch ( aNumber ) {
							case 7:
							case 8:
							case 16:
								hAdder.addImplicitHydrogensToSatisfyValency(myMolecule, anAtom); break;
							}
						}							
						
						Dimension imgSize = new Dimension(width, height);
						int minSize = ( width < height ? width : height);
						// CDK 1.0.4
						SimpleRenderer2D molRend = new SimpleRenderer2D();
						Renderer2DModel rmdl = molRend.getRenderer2DModel();
						
						// CDK 1.2.1
					/*	Renderer2DModel rmdl = new Renderer2DModel();
						Java2DRenderer molRend = new Java2DRenderer(rmdl);
					*/	
						rmdl.setBackgroundDimension(imgSize);
						rmdl.setColorAtomsByType(true);
						rmdl.setBackColor(Color.WHITE);
						rmdl.setForeColor(Color.BLACK);
						rmdl.setShowImplicitHydrogens(true);
//						rmdl.setShowExplicitHydrogens(true);
						rmdl.setFont(new Font("SansSerif", Font.PLAIN, minSize / 25));
						int bondWidth = ( minSize < 1000 ? 3 : 5);
						bondWidth = ( minSize < 300 ? 1 : 3);
						rmdl.setBondWidth( bondWidth );
						GeometryTools.translateAllPositive(myMolecule, rmdl.getRenderingCoordinates());
						/*
							double bondLength = GeometryTools.getBondLengthAverage(myMolecule, rmdl.getRenderingCoordinates());
							HydrogenPlacer hPlacer = new HydrogenPlacer();
							hPlacer.placeHydrogens2D(myMolecule, bondLength * 0.75, rmdl.getRenderingCoordinates());
						 */
						GeometryTools.scaleMolecule(myMolecule, imgSize, 0.9, rmdl.getRenderingCoordinates());
						GeometryTools.center(myMolecule, imgSize, rmdl.getRenderingCoordinates());

						molRend.paintMolecule(myMolecule, gf);
					} catch (CDKException e) {
						drawText(gf, "CDKException: " + e.getMessage(), width, height);
						gf.dispose();
						e.printStackTrace();
					} catch (Throwable t) {
						drawText(gf, "ERROR " + t.getClass().getCanonicalName() + ": " + t.getMessage(), width, height);
						gf.dispose();
						t.printStackTrace();						
					}
				} else {
					gf.drawString("No structure.", 14, height / 2);
				}
			} else {
				gf.drawString("Compound Not Found.", 14, height / 2);
			}
		} catch ( Exception e ) {
			gf.drawString("ERROR: " + e.getMessage(), 14, height / 2);
			gf.dispose();
			this.handleException(e);
		}
		gf.dispose();
		return anImage;
	}
	
	private void drawText(Graphics2D gf, String text, float width, float height) {
		AttributedString string = new AttributedString(text);
		System.out.println("DRAW TEXT: " + text);
		AttributedCharacterIterator sIter = string.getIterator();
		LineBreakMeasurer lineMeasure = new LineBreakMeasurer(sIter, gf.getFontRenderContext());
		
		lineMeasure.setPosition(sIter.getBeginIndex());
		int end = sIter.getEndIndex();
		
		System.out.format("DRAW TEXT start(%d) end(%d)", sIter.getBeginIndex(), end); 
		
		float drawYPos = 0;
		
		while ( lineMeasure.getPosition() < end ) {
			TextLayout layout = lineMeasure.nextLayout(width);
			System.out.println("LAYOUT: " + layout.toString());
			drawYPos += layout.getAscent();
			float drawXPos = ( layout.isLeftToRight() ? 0 : width - layout.getAdvance() );
			layout.draw(gf, drawXPos, drawYPos);
			drawYPos += layout.getDescent() + layout.getLeading();
		}
	}
	
	public String dataForm(Compound anObject) {
		if ( anObject.isAllowed(Role.WRITE) && this.hasFormValue("showBrowser") ) {
			Form myForm = new Form(DataForm.fileManagerApplet(this.myWrapper, "compound", anObject.getID(), null, false));				
			myForm.addItem(String.format("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick=\"updateForm(this,'%s')\" NAME='cancelBrowser'>Close</BUTTON>", DATA_FORM));
			myForm.setAttribute("NAME", "dataBrowser");
			myForm.addHiddenValue("id", anObject.getID());
			myForm.addHiddenValue("div", DATA_FORM);
			return myForm.toString();
		} else {
			StringBuffer output = new StringBuffer();
			DataForm dataForm = new DataForm(this.myWrapper);
			dataForm.setTypeLabel(Compound.NMR_DATA_TYPE, "NMR Spectrum");
			dataForm.setTypeLabel(Compound.MS_DATA_TYPE, "Mass Spectrum");
			dataForm.setTypeLabel(Compound.IR_DATA_TYPE, "IR Spectrum");			
			dataForm.setTypeLabel(Compound.UV_DATA_TYPE, "UV Spectrum");
			dataForm.setTypeLabel(Compound.CD_DATA_TYPE, "CD Spectrum");
			output.append(dataForm.datafileTable(anObject));
			if ( anObject.isAllowed(Role.WRITE) )
				output.append(String.format("<FORM><P ALIGN='CENTER'><INPUT TYPE=HIDDEN NAME='id' VALUE='%s'/><BUTTON TYPE='BUTTON' NAME='showBrowser' onClick=\"loadForm(this, '%s')\">Manage Data Files</BUTTON></P></FORM>", anObject.getID(), DATA_FORM));
			return output.toString();
		}
	}

	

}
