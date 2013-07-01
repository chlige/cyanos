/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.io.CMLReader;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.io.MDLWriter;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.Compound_UV;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.web.forms.CompoundForm;
import edu.uic.orjala.cyanos.web.forms.SampleForm;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;

/**
 * @author George Chlipala
 *
 */
public class CompoundServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3370272128358387178L;
	private static final String DATA_FILE_DIV_ID = "dataFile";
	private static final String DATA_FILE_DIV_TITLE = "Data Files";
	private static final String HELP_MODULE = "compound";

	public void display(CyanosWrapper aWrap) throws Exception {

		String module = aWrap.getRequest().getPathInfo();	
		
		if ( module != null ) {
			if ( module.startsWith("/graphic")) {

				String[] details = module.split("/", 3);
				if ( details.length == 3 ) {
					
					Compound aCompound = new SQLCompound(aWrap.getSQLDataSource(), details[2]);
					if ( ! aCompound.hasMDLData() ) {
						aWrap.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
						return;
					}
					
					aWrap.setContentType("image/png");
					CompoundForm aForm = new CompoundForm(aWrap);
					BufferedImage anImage = aForm.drawMolecule(aCompound);

					ServletOutputStream httpOut = aWrap.getOutputStream();
//					httpOut.wait(1000);
					try {
						if ( ! ImageIO.write(anImage, "PNG", httpOut) )
							this.log("FAILURE TO WRITE IMAGE.");
					} catch ( Exception ex ) {
						this.log(String.format("COMPOUND: %s ERROR: %s", aCompound.getID(), ex.getLocalizedMessage()));
						ex.printStackTrace();
					}
					httpOut.flush();
					httpOut.close();
				}
				return;
			} else if ( module.startsWith("/export") ) {
				ServletOutputStream out = aWrap.getOutputStream();
				String[] details = module.split("/", 3);
				if ( details.length < 3 ) {
					aWrap.setContentType("text/plain");
					out.println("No compound specified.");
					out.close();
					return;
				}
				
				try {
					Pattern cPatt = Pattern.compile("^(.+)\\.(cml|mol)");
					Matcher cMatch = cPatt.matcher(details[2]);
					if ( cMatch.matches() ) {
						Compound aCompound = new SQLCompound(aWrap.getSQLDataSource(), cMatch.group(1));
						if ( aCompound.first() ) {
							if ( aCompound.hasMDLData() ) {
								String exportType = cMatch.group(2);
								if ( exportType != null && exportType.equals("mol") ) {
									aWrap.setContentType("chemical/x-mdl-molfile");
									out.println(aCompound.getMDLData());
								} else {
									// OTHER TYPES
									/*
									MDLReader aReader = new MDLReader(aCompound.getMDLDataStream());
									ChemFile aChemFile = new ChemFile();
									aChemFile = (ChemFile) aMDLeader.read(aChemFile);
									Molecule myMolecule = (Molecule) aChemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);
									MDLWriter chemExport = new MDLWriter(out);
									chemExport.write(myMolecule);
									*/
								}
							} else {
								aWrap.setContentType("text/plain");
								out.println("No MDL data for this compound.");
							}
						} else {
							aWrap.setContentType("text/plain");
							out.println("Compound Not Found.");
						}

					} else {
						aWrap.setContentType("text/plain");
						out.println("Specified compound name not formatted properly.");

					}
					} catch ( DataException ex ) {
					aWrap.setContentType("text/plain");
					out.println("ERROR: " + ex.getMessage());
					ex.printStackTrace();
				} 
	/*			try {
					this.closeSQL();
				} catch ( SQLException ex ) {
					this.log("FAILURE TO CLOSE SQL CONNECTION.");
					ex.printStackTrace();
				}
	*/
	//			out.close();
				return;
			}
		} else if ( aWrap.hasFormValue("div") ) {
			aWrap.setContentType("text/html");
			PrintWriter out = aWrap.getWriter();
			String divTag = aWrap.getFormValue("div");
			Compound aCompound = new SQLCompound(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
			if ( aCompound.first() ) {
				CompoundForm aForm = new CompoundForm(aWrap);
				if ( divTag.equals(DATA_FILE_DIV_ID) ) {
					Div aDiv = new Div(aForm.dataForm(aCompound));
					aDiv.setID(CompoundForm.DATA_FORM);
					out.println(aDiv.toString());
				} else if ( divTag.equals(CompoundForm.DATA_FORM)) {
					out.println(aForm.dataForm(aCompound));
				}
			}
			out.flush();
			return;
		}
		

		PrintWriter out = aWrap.startHTMLDoc("Compound Data", ( ! "/link".equals(module) ));

		Compound_UV aCompound = null;

		if ( aWrap.hasFormValue("id") ) {
			aCompound = new SQLCompound(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
		}
		
		CompoundForm myForm = new CompoundForm(aWrap);
		if ( module == null ) {
			if ( aCompound == null ) {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Compound List");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head.toString());

				if ( aWrap.getUser().isAllowed(User.SAMPLE_ROLE, User.NULL_PROJECT, Role.CREATE));
					out.println("<P ALIGN='CENTER'><A HREF='compound/add'>Add a New Compound</A></P>");

				try {
					Compound compoundList = SQLCompound.compounds(aWrap.getSQLDataSource(), SQLCompound.ID_COLUMN, SQLCompound.ASCENDING_SORT);
					out.println(myForm.listCompounds(compoundList));
				} catch (DataException e) {
					out.println("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
				}
			} else if (aCompound.first()) {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Compound ");
				title.addItalicString(aCompound.getID());
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/><DIV class='content'>");
				out.println(head.toString());
				Div formDiv = new Div(myForm.showCompound(aCompound));
				formDiv.setClass("main");
				out.println(formDiv.toString());
//				if ( aCompound.hasUVData() ) {
//					out.println(myForm.uvDiv(aCompound));
//				}
				SampleForm sampleForm = new SampleForm(aWrap);
				Sample mySamples = aCompound.getSamples();
				Div sampleDiv = sampleForm.sampleDiv(mySamples, true);
				out.println(sampleDiv.toString());
				Div assayDiv = sampleForm.assayDiv(mySamples);
				out.println(assayDiv.toString());
				out.println(myForm.loadableDiv(DATA_FILE_DIV_ID, DATA_FILE_DIV_TITLE));
				out.println("</DIV>");
			} else 
				out.println("<DIV CLASS='messages'><P><B><FONT COLOR='red'>ERROR:</FONT> COMPOUND NOT FOUND!</B></P></DIV>");
		} else if ( module.equals("/add") ) {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Add A Compound");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head);
			if ( aWrap.hasFormValue(CompoundForm.ADD_ACTION)) {
				try {
					Compound_UV newCompound = SQLCompound.create(aWrap.getSQLDataSource(), myForm.newCompoundID());
					out.print("<P ALIGN='CENTER'>Creating a new compound ");
					if ( myForm.isFullAddForm() ) {
						if ( myForm.hasMDLData() ) {
							out.println("using uploaded file...");
							try {
								FileItem anUpload = myForm.getMDLData();
								ChemFile aChemFile = new ChemFile();
								Molecule myMolecule;

								if ( anUpload.getContentType().equals("chemical/x-mdl-molfile") || ( aWrap.getFormValue("format").equals("mdl") && (!anUpload.getContentType().equals("chemical/x-cml") ) )) {
									IChemObjectReader aReader = new MDLReader(anUpload.getInputStream());
									aChemFile = (ChemFile) aReader.read(aChemFile);	
									myMolecule = (Molecule) aChemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);								
									newCompound.setMDLData(anUpload.getString());
								} else {
									IChemObjectReader aCMLReader = new CMLReader(anUpload.getInputStream());
									aChemFile = (ChemFile) aCMLReader.read(aChemFile);									
									StringWriter output = new StringWriter();
									myMolecule = (Molecule) aChemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);
									MDLWriter mdlOut = new MDLWriter(output);
									mdlOut.write(myMolecule);
									mdlOut.close();									
									newCompound.setMDLData(output.toString());
								}
								
								if ( aChemFile.getChemSequenceCount() > 1 ) 
									out.print("<B><FONT COLOR='orange'>WARNING:</FONT> Multiple molecules in this file. Using first molecule</B><BR/>Compound creation...");
								
								myForm.setValuesFromMolecule(newCompound, myMolecule);
							} catch (CDKException e) {
								out.println("<B><FONT COLOR='red'>CDK ERROR:</FONT> " + e.getMessage() + "</B></P>");
								e.printStackTrace();
							} catch (IOException e) {
								out.println("<B><FONT COLOR='red'>IO ERROR:</FONT> " + e.getMessage() + "</B></P>");
								e.printStackTrace();
							} catch (ClassNotFoundException e) {
								out.println("<B><FONT COLOR='red'>Java ERROR:</FONT> " + e.getMessage() + "</B></P>");
								e.printStackTrace();
							} 
						} else {
							out.println("using form values...");
							myForm.setCompoundValues(newCompound);
						}
						out.println("<FONT COLOR='GREEN'><B>SUCCESS</B></FONT></P>");
						out.println("<P ALIGN='CENTER'><A HREF=\"../compound?id=" + newCompound.getID() + "\">View compound</A></P>");
					} else {
						out.println("<FONT COLOR='GREEN'><B>SUCCESS</B></FONT></P>");
						Form aForm = new Form(myForm.compoundForm(newCompound));
						aForm.setAttribute("METHOD", "POST");
						out.println(aForm.toString());
					}
					out.println("</P>");
				} catch (DataException e ) {
					out.println("<B><FONT COLOR='red'>ERROR<BR/>ERROR:</FONT> " + e.getMessage() + "</B></P>");
					out.println(myForm.addCompoundForm());
					e.printStackTrace();
				}
			} else {
				Form aForm = new Form(myForm.addCompoundForm());
				aForm.setAttribute("METHOD", "POST");
				aForm.setAttribute("ENCTYPE", "multipart/form-data");
				out.println(aForm.toString());
			}
		} else if ( module.equals("/link") ) {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("<B>Link to Compound</B>");
			title.setSize("+1");
			head.addItem(title);
			out.println(head);
			Form aForm = new Form();
			aForm.addItem("<INPUT TYPE='HIDDEN' NAME='sample_id' VALUE=\"" + aWrap.getFormValue("sample_id") + "\"/>");
			aForm.addItem("<INPUT TYPE='HIDDEN' NAME='opener' VALUE=\"" + aWrap.getFormValue("opener") + "\"/>");
			
			try {
				if ( aWrap.hasFormValue("linkCompound") ) {
					String cmpdID = myForm.selectedCompoundID();
					if ( cmpdID != null & ( ! cmpdID.equals("")) ) {
						aCompound = new SQLCompound(aWrap.getSQLDataSource(), cmpdID);
						Sample aSample = new SQLSample(aWrap.getSQLDataSource(), aWrap.getFormValue("sample_id"));
						if ( aSample.first() ) {
							if ( aCompound.first() ) {
								aSample.setCompound(aCompound);
								aForm.addItem("<P ALIGN='CENTER'>Linked to Compound.</P>");
								aForm.addItem(myForm.quickView(aCompound));
							} else {
								aCompound = SQLCompound.create(aWrap.getSQLDataSource(), cmpdID);
								aSample.setCompound(aCompound);
								aForm.addItem(myForm.compoundForm(aCompound));
								aForm.addItem("<INPUT TYPE='HIDDEN' NAME='cmpdID' VALUE=\"" + aCompound.getID() + "\"/>");
							}
						} else {
							aForm.addItem("<P ALIGN='CENTER'><FONT COLOR='red'><B>ERROR:</FONT> Sample not found</B></P>");
						}
						aForm.addItem("<P ALIGN='CENTER'><INPUT TYPE='BUTTON' NAME='close' VALUE='Close Window' onClick='closeSmallWindow()' /></P>");
					} else {
						aForm.addItem("<P ALIGN='CENTER'>Please select a compound to link.</P>");
						aForm.addItem(this.compoundList(aWrap));
					}
				} else if ( aWrap.hasFormValue(CompoundForm.UPDATE_ACTION)) {
					aCompound = new SQLCompound(aWrap.getSQLDataSource(), myForm.selectedCompoundID());
					aForm.addItem(myForm.compoundForm(aCompound));
					aForm.addItem("<INPUT TYPE='HIDDEN' NAME='cmpdID' VALUE=\"" + aCompound.getID() + "\"/>");
					aForm.addItem("<INPUT TYPE='BUTTON' NAME='close' VALUE='Close' onClick='closeSmallWindow()' />");
				} else {
					aForm.addItem(this.compoundList(aWrap));
				}
			} catch ( DataException ex ) {
				aForm.addItem("<P ALIGN='CENTER'><FONT COLOR='red'><B>ERROR:</FONT>");
				aForm.addItem(ex.getMessage());
				aForm.addItem("</B></BR><INPUT TYPE='BUTTON' NAME='close' VALUE='Close Window' onClick='closeSmallWindow()' /></P>");
			}
			out.println(aForm.toString());
		}
		
		aWrap.finishHTMLDoc();
	}
	
	private String compoundList(CyanosWrapper aWrap) {
		CompoundForm myForm = new CompoundForm(aWrap);
		try {
			Compound compoundList = SQLCompound.compounds(aWrap.getSQLDataSource(), SQLCompound.ID_COLUMN, SQLCompound.ASCENDING_SORT);
			return "<P ALIGN='CENTER'>Compound ID:" + 
				myForm.linkCompoundMenu(compoundList) + 
				"<BR/><INPUT TYPE='SUBMIT' NAME='linkCompound' VALUE='Select Compound'/></P>";
		} catch (DataException e) {
			e.printStackTrace();
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR: </FONT>" + e.getMessage() + "</B></P>");
		}
	}
	
	@Override
	protected String getHelpModule() {
		return HELP_MODULE;
	}
		

}
