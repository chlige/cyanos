/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.commons.fileupload.FileItem;
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
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class CompoundForm extends BaseForm {

	private static final String MDL_FILE = "mdlFile";
//	private static final String UV_DIV_TITLE = "UV Data";
//	private static final String UV_DIV_ID = "uvPeaks";

	public final static String ADD_ACTION = "addCompound";
	public final static String UPDATE_ACTION = "updateCompound";
	public static final String DATA_FORM = "dataForm";
	
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
			myRow.addItem(this.makeFormTextRow("Name:", "name", aCompound.getName()));
			if ( ! aCompound.hasMDLData() ) {
				Popup typePop = new Popup();
				typePop.addItemWithLabel("mdl", "MDL Format");
				typePop.addItemWithLabel("cml", "CML Format");
				typePop.setName("format");
				myRow.addItem(String.format("<TD>Structure:</TD><TD>%s <INPUT TYPE='FILE' NAME='%s' SIZE=25/></TD>", typePop.toString(), MDL_FILE));
			} else {
				myRow.addItem("<TD COLSPAN=2><BUTTON TYPE='SUBMIT' NAME='clearMDL'>Clear MDL Data</BUTTON></TD>");
			}
			myRow.addItem(this.makeFormTextRow("Formula:", "formula", aCompound.getFormula()));
			myRow.addItem(this.makeFormTextRow("Formula Weight:", "avg_mass", String.format("%.4f", aCompound.getAverageMass())));
			myRow.addItem(this.makeFormTextRow("Monoisotopic Mass:", "mono_mass", String.format("%.5f", aCompound.getMonoisotopicMass())));
			myRow.addItem(this.makeFormTextAreaRow("SMILES String:", "smiles", aCompound.getSmilesString(), 50, 3));
			myRow.addItem(this.makeFormTextAreaRow("Notes:", "notes", aCompound.getNotes(), 50, 7));
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
				Div smilesDiv = new Div("<P ALIGN='CENTER'><B>SMILES String</B><BR/><TEXTAREA NAME='nothing' COLS=50 ROWS=5 WRAP='logical' READONLY>");
				smilesDiv.addItem(smilesString);
				smilesDiv.addItem("</TEXTAREA></P><P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"smiles\")'>Hide SMILES</BUTTON></P>");
				smilesDiv.setClass("hideSection");
				smilesDiv.setID("edit_smiles");

				textDiv.addItem("<DIV ID='view_smiles' CLASS='showSection'><P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"smiles\")'>Show SMILES</BUTTON></P></DIV>");
				textDiv.addItem(smilesDiv);
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
		TableRow myRow = new TableRow(this.makeFormTextRow("Compound ID:", "newCmpdId"));
		myRow.addItem(this.makeFormTextRow("Name:", "name"));
		myRow.addItem(this.makeFormTextRow("Formula:", "formula"));
		myRow.addItem(this.makeFormTextRow("Formula Weight:", "avg_mass", "0.0000"));
		myRow.addItem(this.makeFormTextRow("Monoisotopic Mass:", "mono_mass", "0.00000"));
		Popup typePop = new Popup();
		typePop.addItemWithLabel("mdl", "MDL Format");
		typePop.addItemWithLabel("cml", "CML Format");
		typePop.setName("format");
		myRow.addItem(String.format("<TD>Structure:</TD><TD>%s <INPUT TYPE='FILE' NAME='%s' SIZE=25/></TD>", typePop.toString(), MDL_FILE));
		myRow.addItem(this.makeFormTextAreaRow("Notes:", "notes"));
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
			String[] vialHeaders = {"Name", "Formula", "Notes"};
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
					String name = compounds.getName();
					if ( name == null || name.length() < 1) 
						name = compounds.getID();
					TableCell myCell = new TableCell(String.format("<A HREF='%s/compound?id=%s'>%s</A>", this.myWrapper.getContextPath(), compounds.getID(), name));							
					myCell.addItem(compounds.getHTMLFormula());
					myCell.addItem(shortenString(compounds.getNotes(), 15));
					if ( showStructure )
						myCell.addItem(String.format("<IMG SRC=\"%s/compound/graphic/%s\" HEIGHT=150 WIDTH=150></TD></TR>", this.myWrapper.getContextPath(), compounds.getID()));

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
				}
			} else {
				vialTable.addItem("<TR><TD COLSPAN=3 ALIGN='CENTER'><B><I>NONE</B></I></TD></TR>");
			}
			return vialTable.toString();

		} catch ( DataException e ) {
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
		}

	}
	
	public String newCompoundID() {
		return this.getFormValue("newCmpdId");
	}
	
	public boolean isFullAddForm() {
		return this.hasFormValue("name") && this.hasFormValue("formula") && this.hasFormValue("notes");
	}
	
	public void setValuesFromMolecule(Compound aCompound, Molecule aMolecule) throws DataException, IOException, ClassNotFoundException, CDKException {
		SmilesGenerator smileGen = new SmilesGenerator();
		smileGen.setUseAromaticityFlag(true);
		aCompound.setSmilesString(smileGen.createSMILES(aMolecule));
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
	    aCompound.setAverageMass(formulaMaker.getNaturalMass());
	    aCompound.setMonoisotopicMass(formulaMaker.getMass());
// CDK v1.2.3
/*	    aCompound.setFormula(MolecularFormulaManipulator.getHillString(aFormula));
	    aCompound.setAverageMass(MolecularFormulaManipulator.getNaturalExactMass(aFormula));
	    aCompound.setMonoisotopicMass(MolecularFormulaManipulator.getMajorIsotopeMass(aFormula));
*/
	    aCompound.setNotes(this.getFormValue("notes"));
		aCompound.refresh();
		aCompound.setAutoRefresh();
	}

	public String setCompoundValues(Compound aCompound) {
		StringBuffer output = new StringBuffer();
		if ( aCompound.isAllowed(Role.WRITE) ) {
			try {
				if ( this.myWrapper.hasUpload(MDL_FILE) ) {
					output.append("<P><B><FONT COLOR='green'>Updating Compound Information</FONT> using MDL data.</B></P>");
					FileItem mdlUpload = this.myWrapper.getUpload(MDL_FILE);
					try {
						ChemFile aCMLFile = new ChemFile();
						if ( mdlUpload.getContentType().equals("chemical/x-mdl-molfile") || ( this.getFormValue("format").equals("mdl") && (!mdlUpload.getContentType().equals("chemical/x-cml"))) ) {
							MDLReader aReader = new MDLReader(mdlUpload.getInputStream());
							aCMLFile = (ChemFile) aReader.read(aCMLFile);									
						} else {
							CMLReader aCMLReader = new CMLReader(mdlUpload.getInputStream());
							aCMLFile = (ChemFile) aCMLReader.read(aCMLFile);									
						}
						if ( aCMLFile.getChemSequenceCount() > 1 ) {
							output.append("<P><B><FONT COLOR='orange'>WARNING:</FONT> Multiple molecules in this file. Using first molecule</B></P>");
						}
						Molecule myMolecule = (Molecule) aCMLFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);
						aCompound.setMDLData(mdlUpload.getString());
						this.setValuesFromMolecule(aCompound, myMolecule);
					} catch (CDKException e) {
						output.append(this.handleException(e));
					} catch (IOException e) {
						output.append(this.handleException(e));
					} catch (ClassNotFoundException e) {
						output.append(this.handleException(e));
					}
				} else {
					output.append("<P><B><FONT COLOR='green'>Updating Compound Information</FONT> using form data.</B></P>");
					aCompound.setManualRefresh();
					aCompound.setName(this.getFormValue("name"));
					if ( this.hasFormValue("formula") )
						aCompound.setFormula(this.getFormValue("formula"));
					if ( this.hasFormValue("avg_mass") )
						aCompound.setAverageMass(this.getFormValue("avg_mass"));
					if ( this.hasFormValue("smiles") )
						aCompound.setSmilesString(this.getFormValue("smiles"));
					if ( this.hasFormValue("mono_mass") )
						aCompound.setMonoisotopicMass(this.getFormValue("mono_mass"));
					aCompound.setNotes(this.getFormValue("notes"));
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
	}
	
	public boolean hasMDLData() {
		return this.myWrapper.hasUpload(MDL_FILE);
	}
	
	public FileItem getMDLData() {
		return this.myWrapper.getUpload(MDL_FILE);
	}
	
	public String linkCompoundMenu(Compound compoundList) {
		Popup cmpdPop = new Popup();
		cmpdPop.addItemWithLabel("", "New Compound ->");
		try {
			compoundList.beforeFirst();
			while ( compoundList.next() ) {
				cmpdPop.addItemWithLabel(compoundList.getID(), compoundList.getName());
			}
		} catch (DataException e ) {
			return "<FONT COLOR='RED'><B>SQL ERROR:</FONT>" + e.getMessage() + "</B>";
		}
		cmpdPop.setName("cmpdID");
		return cmpdPop.toString() + "<INPUT TYPE='TEXT' NAME='newCmpdId'/>";
	}

	public String selectedCompoundID() {
		String myID = this.getFormValue("cmpdID");
		if ( myID != null & (! myID.equals("")) ) {
			return myID;
		} else if ( this.hasFormValue("newCmpdId") ) {
			return this.getFormValue("newCmpdId");
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
						gf.drawString("ERROR: " + e.getMessage(), 14, height / 2);
						gf.dispose();
						e.printStackTrace();
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
