/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPException;

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

import edu.uic.orjala.cyanos.AssayData;
import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLAssayData;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLMaterial;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.web.InchiGenerator;
import edu.uic.orjala.cyanos.web.MultiPartRequest;
import edu.uic.orjala.cyanos.web.listener.CyanosRequestListener;
import edu.uic.orjala.cyanos.web.listener.UploadManager.FileUpload;

/**
 * @author George Chlipala
 *
 */
public class CompoundServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3370272128358387178L;
	
	public static final String DATAFILE_DIV_ID = "dataFile";
	public static final String DATAFILE_DIV_TITLE = "Data Files";
	public static final String MATERIAL_LIST_DIV_ID = "materialinfo";
	public static final String SEP_LIST_DIV_ID = "sepInfo";
	public static final String INFO_FORM_DIV_ID = "infoForm";
	public static final String ASSAY_DIV_ID = "assays";
	
	public static final String HELP_MODULE = "compound";

	public static final String COMPOUND_RESULTS = "compoundResult";
	public static final String COMPOUND_PARENT = "compoundParent";
	public static final String COMPOUND_LIST = "compoundList";
	
	public static final String COMPOUND_OBJ = "compoundObj";
	
	public static final String ATTR_UPDATE_MAP = "updateMap";
	
	public static final String FIELD_NEW_COMPOUND_ID = "newCmpdId";
	public static final String FIELD_NOTES = "notes";
	public static final String FIELD_FILE_FORMAT = "format";
	public static final String FIELD_MONO_MASS = "mono_mass";
	public static final String FIELD_AVG_MASS = "avg_mass";
	public static final String FIELD_FORMULA = "formula";
	public static final String FIELD_SMILES_STRING = "smiles_string";
	public static final String FIELD_INCHI_STRING = "inchi_string";
	public static final String FIELD_INCHI_KEY = "inchi_key";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_USE_INCHI_KEY = "use_inchi";
	public static final String FIELD_PROJECT = "project";
	public static final String MDL_FILE = "mdlFile";

	public static final String FORMAT_CML = "cml";
	public static final String FORMAT_MDL = "mdl";

//	public static final String UV_DIV_TITLE = "UV Data";
//	public static final String UV_DIV_ID = "uvPeaks";

	public final static String ADD_ACTION = "addCompound";
	public final static String UPDATE_ACTION = "updateCompound";
	public final static String CLEAR_ACTION = "clearMDL";
	public static final String DATA_FORM = "dataForm";
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, res);
		this.handleRequest(req, res);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req = MultiPartRequest.parseRequest(req);
		super.doPost(req, res);
		this.handleRequest(req, res);
	}

	public static Compound compoundsLike(HttpServletRequest request, String query) throws DataException, SQLException {
		return SQLCompound.loadLike(getSQLData(request), query);
	}
	
	protected void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		String module = req.getPathInfo();	
		
		if ( module != null ) {
			if ( module.startsWith("/graphic")) {
//				this.drawMolecule(SQLCompound.load(getSQLData(req), req), req, res);
				return;
			} else if ( module.startsWith("/export") ) {
				exportMolecule(req, res);
				return;
			}
		} else if ( req.getParameter("div") != null ) {
			try {
				this.handleDivRequest(req, res);
				return;
			} catch (DataException e) {
				throw new ServletException(e);
			} catch (SQLException e) {
				throw new ServletException(e);
			}
		}


		try {
		Compound aCompound = null;
		if ( req.getParameter("id") != null )
			aCompound = SQLCompound.load(getSQLData(req), req.getParameter("id"));
		
		if ( module == null ) {
			 if ( req.getParameter("form") != null ) {
					if ( req.getParameter("form").equals("add") ) {
						if ( req.getParameter(UPDATE_ACTION) != null ) {
							aCompound = SQLCompound.create(getSQLData(req), req.getParameter("newID"));
							updateCompound(req, aCompound);
							req.setAttribute(COMPOUND_OBJ, aCompound);
							this.forwardRequest(req, res, "/compound.jsp");
						} else {
							this.forwardRequest(req, res, "/compound/add-compound.jsp");
						}
					}
			 } else if ( aCompound == null ) {
				if ( req.getParameter("query") != null ) {
					SQLCompound compoundList = SQLCompound.loadLike(getSQLData(req), req.getParameter("query"));
					if ( "sdf".equals(req.getParameter("export")) ) {
						exportSDF(compoundList, res);
						return;
					} else {
						req.setAttribute(COMPOUND_RESULTS, compoundList);
					}
				}
				this.forwardRequest(req, res, "/compound.jsp");
			} else if (aCompound.first()) {
				String exportType = req.getParameter("exportType");
				if ( exportType != null ) {
					if ( exportType.equals("graph") )
						exportGraph(aCompound.getID(), getSQLData(req), res);						
					else 
						exportMolecule(aCompound, exportType, res);
					return;
				}		

				if ( req.getParameter("graphic") != null ) {
					drawMolecule(aCompound, req, res);
				}
				
				if ( req.getParameter(UPDATE_ACTION) != null ) 
					updateCompound(req, aCompound);
					
				req.setAttribute(COMPOUND_OBJ, aCompound);
				this.forwardRequest(req, res, "/compound.jsp");
				
/*				Paragraph head = new Paragraph();
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
				SeparationForm sepForm = new SeparationForm(aWrap);
				Separation seps = SQLSeparation.separationsForCompound(aWrap.getSQLDataSource(), aCompound);
				Div sepDiv = sepForm.separationDiv(seps);
				out.println(sepDiv.toString());
				out.println(myForm.loadableDiv(DATAFILE_DIV_ID, DATAFILE_DIV_TITLE));
				out.println("</DIV>");
*/
			} 
/*		} else if ( module.equals("/add") ) {
			PrintWriter out = aWrap.startHTMLDoc("Compound Data", ( ! "/link".equals(module) ));

			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Add A Compound");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head);
			if ( aWrap.hasFormValue(CompoundForm.ADD_ACTION)) {
				try {
					String newID = myForm.newCompoundID();
					if ( newID == null || newID.length() < 1 ) {
						out.print("<P ALIGN='CENTER'>Need to specify a compound ID");
						Form aForm = new Form(myForm.addCompoundForm());
						aForm.setAttribute("METHOD", "POST");
						aForm.setAttribute("ENCTYPE", "multipart/form-data");
						out.println(aForm.toString());
					} else {
						Compound newCompound = SQLCompound.create(aWrap.getSQLDataSource(), newID);
						out.print("<P ALIGN='CENTER'>Creating a new compound ");
						if ( myForm.isFullAddForm() ) {
							if ( myForm.hasMDLData() ) {
								out.println("using uploaded file...");
								try {
									FileUpload anUpload = myForm.getMDLData();
									ChemFile aChemFile = new ChemFile();
									Molecule myMolecule;
									String mdlData;
									
									if ( anUpload.getContentType().equals("chemical/x-mdl-molfile") || ( aWrap.getFormValue("format").equals("mdl") && (! anUpload.getContentType().equals("chemical/x-cml") ) ) ) {
										IChemObjectReader aReader = new MDLReader(anUpload.getStream());
										aChemFile = (ChemFile) aReader.read(aChemFile);	
										myMolecule = (Molecule) aChemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);
										if ( aChemFile.getChemSequenceCount() > 1 ) {
											StringWriter output = new StringWriter();
											MDLWriter mdlOut = new MDLWriter(output);
											mdlOut.write(myMolecule);
											mdlOut.close();									
											mdlData = output.toString();										
										} else {
											mdlData = anUpload.getString();
										}
									} else {
										IChemObjectReader aCMLReader = new CMLReader(anUpload.getStream());
										aChemFile = (ChemFile) aCMLReader.read(aChemFile);									
										StringWriter output = new StringWriter();
										myMolecule = (Molecule) aChemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);
										MDLWriter mdlOut = new MDLWriter(output);
										mdlOut.write(myMolecule);
										mdlOut.close();									
										mdlData = output.toString();
									}

									if ( aChemFile.getChemSequenceCount() > 1 ) {
										out.print("<B><FONT COLOR='orange'>WARNING:</FONT> Multiple molecules in this file. Using first molecule</B><BR/>Compound creation...");
									}
									
									newCompound.setMDLData(mdlData);
									String inchiString = InchiGenerator.convertMOL(mdlData);
									newCompound.setInChiString(inchiString);
									newCompound.setInChiKey(InchiGenerator.getInChiKey(inchiString));
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
					}
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
 */
		}

		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		} catch (UnsupportedOperationException e) {
			throw new ServletException(e);
		} catch (SOAPException e) {
			throw new ServletException(e);
		} catch (CDKException e) {
			throw new ServletException(e);
		} catch (ClassNotFoundException e) {
			throw new ServletException(e);
		}


		//		aWrap.finishHTMLDoc();
	}

	public static Compound getCompound(HttpServletRequest request) throws DataException, SQLException {
		return SQLCompound.load(getSQLData(request), request.getParameter("id"));
	}
	
	private static void exportSDF(SQLCompound compoundList, HttpServletResponse res) throws IOException {
		ServletOutputStream out = res.getOutputStream();
		try {
			StringBuffer output = new StringBuffer();
			while ( compoundList.next() ) {
				if ( compoundList.hasMDLData() ) {
					output.append(compoundList.getMDLData());
					output.append("> <COMPOUND_ID>\n");
					output.append(compoundList.getID());
					output.append("\n\n> <NAME>\n");
					output.append(compoundList.getName());
					output.append("\n\n> <PROJECT>\n");
					output.append(compoundList.getProjectID());
					output.append("\n\n> <NOTES>\n");
					String notes = compoundList.getNotes();
					output.append(notes.replaceAll("\n+", "\n"));
					output.append("\n\n$$$$\n");
				}
			}
			res.setHeader("Content-Disposition", "inline; filename=\"compounds.sdf\"");
			res.setContentType("chemical/x-mdl-sdfile");
			out.print(output.toString());
		} catch ( DataException ex ) {
			res.setContentType("text/plain");
			out.println("ERROR: " + ex.getMessage());
			ex.printStackTrace();
		} 
		out.flush();
		return;
	}

	private static void exportMolecule(Compound compound, String exportType, HttpServletResponse res) throws IOException {
		ServletOutputStream out = res.getOutputStream();
		if ( exportType == null ) exportType = "mol";

		if ( compound == null ) {
			res.setContentType("text/plain");
			out.println("No compound specified.");
			out.close();
			return;
		}


		try {
			if ( compound.first() ) {
				if ( compound.hasMDLData() ) {					
					if ( exportType.equals("mol") ) {
						res.setHeader("Content-Disposition", String.format("inline; filename=\"%s.%s\"", compound.getID(), exportType)); 
						res.setContentType("chemical/x-mdl-molfile");
						out.println(compound.getMDLData());
					} else {
						res.setContentType("text/plain");
						out.println("OTHER TYPES CURRENTLY NOT SUPPORTED");
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
					res.setContentType("text/plain");
					out.println("No molecular data for this compound.");
				}
			} else {
				res.setContentType("text/plain");
				out.println("Compound Not Found.");
			}
		} catch ( DataException ex ) {
			res.setContentType("text/plain");
			out.println("ERROR: " + ex.getMessage());
			ex.printStackTrace();
		} 
		out.flush();
		return;
	}
	
	
	private static void exportMolecule(HttpServletRequest req, HttpServletResponse res) throws IOException {
		ServletOutputStream out = res.getOutputStream();
		String compoundID = req.getParameter("id");
		String exportType = req.getParameter("type");
		if ( exportType == null ) exportType = "mol";
		
		if ( compoundID == null ) {
			res.setContentType("text/plain");
			out.println("No compound specified.");
			out.close();
			return;
		}

		try {
			exportMolecule(SQLCompound.load(getSQLData(req), compoundID), exportType, res);
		} catch (DataException e) {
			res.setContentType("text/plain");
			out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			res.setContentType("text/plain");
			out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void drawMolecule(Compound aCompound, HttpServletRequest req, HttpServletResponse res) throws IOException {
		ServletOutputStream httpOut = res.getOutputStream();
		res.setContentType("image/png");
		res.setHeader("Content-Disposition", String.format("inline; filename=\"%s.mol\"", aCompound.getID())); 

		try {

			if ( ! aCompound.hasMDLData() ) {
				res.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}


			int width = 300;
			int height = 300;
			if ( req.getParameter("width") != null )
				width = Integer.parseInt(req.getParameter("width"));
			if ( req.getParameter("height") != null )
				height = Integer.parseInt(req.getParameter("height"));

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
							//							Molecule myMolecule = (Molecule) aChemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);	


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
							//							rmdl.setShowExplicitHydrogens(true);
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
				e.printStackTrace();
			}
			gf.dispose();


			if ( ! ImageIO.write(anImage, "PNG", httpOut) )
				this.log("FAILURE TO WRITE IMAGE.");
		} catch ( Exception ex ) {
			this.log(String.format("COMPOUND: %s ERROR: %s", aCompound.getID(), ex.getLocalizedMessage()));
			ex.printStackTrace();
		} finally {
			httpOut.flush();
			httpOut.close();
		}
		return;
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


	private void handleDivRequest(HttpServletRequest req, HttpServletResponse res) throws DataException, SQLException, ServletException, IOException {
		res.setContentType("text/html; charset=UTF-8");
		String divTag = req.getParameter("div");
		Compound aCompound = SQLCompound.load(getSQLData(req), req.getParameter("id"));
		if ( aCompound.first() ) {
//			CompoundForm aForm = new CompoundForm(aWrap);
			if ( divTag.equals(DATAFILE_DIV_ID) ) {
				RequestDispatcher disp = DataFileServlet.dataFileDiv(req, getServletContext(), aCompound, Compound.DATA_FILE_CLASS);
				disp.forward(req, res);			

/*					Div aDiv = new Div(aForm.dataForm(aCompound));
				aDiv.setID(CompoundForm.DATA_FORM);
				out.println(aDiv.toString());
			} else if ( divTag.equals(CompoundForm.DATA_FORM)) {
				out.println(aForm.dataForm(aCompound));
*/
			} else if ( divTag.equals(SEP_LIST_DIV_ID) ) {
				req.setAttribute(SeparationServlet.SEARCHRESULTS_ATTR, SQLSeparation.separationsForCompound(getSQLData(req), aCompound));
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/separation/separation-list.jsp");
				disp.forward(req, res);		
			} else if ( divTag.equals(MATERIAL_LIST_DIV_ID) ) {
				req.setAttribute(MaterialServlet.SEARCHRESULTS_ATTR, SQLMaterial.materialsForCompound(getSQLData(req), aCompound.getID()));
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/material/material-list.jsp");
				disp.forward(req, res);				
			} else if ( divTag.equals(ASSAY_DIV_ID) ) {
				AssayData data;
				if ( req.getParameter("target") != null && req.getParameter("target").length() > 0 )
					data = SQLAssayData.dataForCompoundID(getSQLData(req), req.getParameter("id"), req.getParameter("target"));
				else
					data = SQLAssayData.dataForCompoundID(getSQLData(req), req.getParameter("id"));
				req.setAttribute(AssayServlet.SEARCHRESULTS_ATTR, data);
				req.setAttribute(AssayServlet.TARGET_LIST, SQLAssay.targets(getSQLData(req)));
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/assay/assay-data-list.jsp");
				disp.forward(req, res);
			} else {
				PrintWriter out = res.getWriter();
				out.println("DIV NOT SETUP");					
				out.flush();
			}
		}
		return;
	}

	private static void updateCompound(HttpServletRequest req, Compound compound) throws DataException, UnsupportedOperationException, SOAPException, CDKException, IOException, ClassNotFoundException, ServletException {
		Set<String> updateValues = new HashSet<String>();
		if ( req.getParameter(CLEAR_ACTION) != null ) {
			compound.clearMDLData();
		} else {
			setAttribute(req, FIELD_NAME, compound, SQLCompound.NAME_COLUMN, updateValues);
			setAttribute(req, FIELD_NOTES, compound, SQLCompound.NOTES_COLUMN, updateValues);
			setAttribute(req, FIELD_PROJECT, compound, SQLCompound.PROJECT_COLUMN, updateValues);

			if ( CyanosRequestListener.getUploadCount(req, MDL_FILE) > 0 ) {
				updateCompoundUpload(req, compound);
			} else {	
				setAttribute(req, FIELD_FORMULA, compound, SQLCompound.FORMULA_COLUMN, updateValues);

				boolean hasMDLData = compound.hasMDLData();
				boolean genInchi = false;
				boolean genSMILES = false;
				
				String[] gens = req.getParameterValues("genString");

				if ( hasMDLData && gens != null && gens.length > 0 ) {
					for ( String gen : gens ) {
						if ( gen.equals("inchi") ) genInchi = true;
						if ( gen.equals("smiles") ) genSMILES = true;
					}
				}
				
				if ( genInchi ) {
					String inchiString = InchiGenerator.convertMOL(compound.getMDLData());
					compound.setInChiString(inchiString);
					compound.setInChiKey(InchiGenerator.getInChiKey(inchiString));
				} else {
					setAttribute(req, FIELD_INCHI_STRING, compound, SQLCompound.INCHI_STRING_COLUMN, updateValues);
					setAttribute(req, FIELD_INCHI_KEY, compound, SQLCompound.INCHI_KEY_COLUMN, updateValues);
				}

				if ( genSMILES ) {
					SmilesGenerator smileGen = new SmilesGenerator();
					smileGen.setUseAromaticityFlag(true);
					compound.setSmilesString(smileGen.createSMILES(getMolecule(compound)));			
				} else {
					setAttribute(req, FIELD_SMILES_STRING, compound, SQLCompound.SMILES_COLUMN, updateValues);
				}
			}
		}
		
		req.setAttribute(ATTR_UPDATE_MAP, updateValues);
	}
	
	private static void updateCompoundUpload(HttpServletRequest req, Compound compound) throws IOException, CDKException, DataException, UnsupportedOperationException, SOAPException, ClassNotFoundException, ServletException {
		FileUpload mdlUpload = getUpload(req, MDL_FILE);
		if ( mdlUpload != null ) {
			ChemFile aCMLFile = new ChemFile();
			String reqFormat = req.getParameter(FIELD_FILE_FORMAT);
			if ( mdlUpload.getContentType().equals("chemical/x-mdl-molfile") || 
					( reqFormat != null && reqFormat.equals(FORMAT_MDL) && (! mdlUpload.getContentType().equals("chemical/x-cml"))) ) {
				MDLReader aReader = new MDLReader(mdlUpload.getStream());
				aCMLFile = (ChemFile) aReader.read(aCMLFile);									
			} else {
				CMLReader aCMLReader = new CMLReader(mdlUpload.getStream());
				aCMLFile = (ChemFile) aCMLReader.read(aCMLFile);									
			}
			// TODO should communicate this to the end user.
			//			if ( aCMLFile.getChemSequenceCount() > 1 ) {
			//				output.append("<P><B><FONT COLOR='orange'>WARNING:</FONT> Multiple molecules in this file. Using first molecule</B></P>");
			//			}
			Molecule molecule = (Molecule) aCMLFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);
			compound.setMDLData(mdlUpload.toString());
			compound.setInChiString(InchiGenerator.getMOLKey(mdlUpload.toString()));
			compound.setInChiKey(InchiGenerator.getInChiKey(compound.getInChiString()));

			SmilesGenerator smileGen = new SmilesGenerator();
			smileGen.setUseAromaticityFlag(true);
			compound.setSmilesString(smileGen.createSMILES(molecule));

			// For CDK v1.0.4
			HydrogenAdder hAdder = new HydrogenAdder(new ValencyChecker());
			hAdder.addExplicitHydrogensToSatisfyValency(molecule);
			MFAnalyser formulaMaker = new MFAnalyser(molecule);

			//CDK v.1.2.3
			/*		
 				IMolecularFormula aFormula = MolecularFormulaManipulator.getMolecularFormula(aMolecule);
			 */
			// for CDK v1.0.4		
			compound.setFormula(formulaMaker.getMolecularFormula());
			BigDecimal mass = new BigDecimal(formulaMaker.getNaturalMass());
			mass = mass.setScale(4, BigDecimal.ROUND_HALF_UP);
			compound.setAverageMass(mass);

			mass = new BigDecimal(formulaMaker.getMass());
			mass = mass.setScale(5, BigDecimal.ROUND_HALF_UP);
			compound.setMonoisotopicMass(mass);
			// CDK v1.2.3
			/*	   
		  		compound.setFormula(MolecularFormulaManipulator.getHillString(aFormula));
			    compound.setAverageMass(MolecularFormulaManipulator.getNaturalExactMass(aFormula));
			    compound.setMonoisotopicMass(MolecularFormulaManipulator.getMajorIsotopeMass(aFormula));
			 */
		}
	}
	
	private static Molecule getMolecule(Compound compound) throws DataException, CDKException {
		if ( compound.hasMDLData() ) {
			MDLReader aReader = new MDLReader(compound.getMDLDataStream());
			ChemFile aChemFile = new ChemFile();
			aChemFile = (ChemFile) aReader.read(aChemFile);
			return (Molecule) aChemFile.getChemSequence(0).getChemModel(0).getMoleculeSet().getMolecule(0);
		}
		return null;
	}
	
	private static void setAttribute(HttpServletRequest req, String fieldName, Compound compound, String attrName, Set<String> updateValues) throws DataException {
		String newValue = req.getParameter(fieldName);
		if ( newValue != null && (! newValue.equals(compound.getAttribute(attrName)) ) ) {
			compound.setAttribute(attrName, newValue);
			updateValues.add(fieldName);
		}
	}
		
	public String getHelpModule() {
		return HELP_MODULE;
	}
		
	private static void setSMILESFromMolecule(Compound aCompound, Molecule aMolecule) throws DataException {
		SmilesGenerator smileGen = new SmilesGenerator();
		smileGen.setUseAromaticityFlag(true);
		aCompound.setSmilesString(smileGen.createSMILES(aMolecule));		
	}
	
	public static void setValuesFromMolecule(Compound aCompound, Molecule aMolecule) throws DataException, IOException, ClassNotFoundException, CDKException {
		setSMILESFromMolecule(aCompound, aMolecule);
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
		aCompound.refresh();
		aCompound.setAutoRefresh();
	}
	
	public final static String SQL_GRAPH_COUNTS = "SELECT COUNT(DISTINCT atom_number), COUNT(DISTINCT bond_id) FROM compound_atoms JOIN compound_bonds USING(compound_id) WHERE compound_id=?";
	
	public final static String SQL_SELECT_ATOM_GRAPH = "SELECT atom_number,element,coord_x,coord_y,coord_z,charge FROM compound_atoms WHERE compound_id=? ORDER BY atom_number ASC";
	public final static String SQL_SELECT_BOND_GRAPH = "SELECT atomA.atom_number,atomB.atom_number,ROUND(bond_order),stereo FROM compound_bonds "
			+ "JOIN compound_bond_atoms atomA ON (compound_bonds.compound_id = atomA.compound_id AND compound_bonds.bond_id = atomA.bond_id )"
			+ "JOIN compound_bond_atoms atomB ON (compound_bonds.compound_id = atomB.compound_id AND compound_bonds.bond_id = atomB.bond_id AND atomA.atom_number < atomB.atom_number)"
			+ "WHERE compound_bonds.compound_id=? ORDER BY atomA.atom_number, atomB.atom_number ASC";

	public static void exportGraph(String compoundID, SQLData data, HttpServletResponse res) throws IOException, DataException, SQLException {
		ServletOutputStream out = res.getOutputStream();

		res.setContentType("chemical/x-mdl-molfile");
		res.setHeader("Content-Disposition", String.format("inline; filename=\"%s.mol\"", compoundID)); 

		PreparedStatement sth = data.prepareStatement(SQL_GRAPH_COUNTS);
		sth.setString(1, compoundID);
		ResultSet results = sth.executeQuery();
		
		if ( results.first() && results.getInt(1) > 0 ) {
			int atomCount = results.getInt(1);
			int bondCount = results.getInt(2);
			
			results.close();
			sth.close();

			ArrayList<String> atomList = new ArrayList<String>(atomCount);
			ArrayList<String> bondList = new ArrayList<String>(bondCount);
			ArrayList<String> propList = new ArrayList<String>();
			ArrayList<String> chargeList = new ArrayList<String>();
			
			sth = data.prepareStatement(SQL_SELECT_ATOM_GRAPH);
			sth.setString(1, compoundID);
			results = sth.executeQuery();
			while ( results.next() ) {
				atomList.add(String.format("% 10.4f% 10.04f% 10.4f %-3s 0  0  0  0  0  0  0  0  0  0  0  0", 
						results.getFloat(3), results.getFloat(4), results.getFloat(5), results.getString(2)));
				int charge = results.getInt(6);
				if ( charge != 0 ) {
					chargeList.add(String.format(" %3d %3d",results.getInt(1), charge));
				}
			}
			
			results.close();
			sth.close();
			sth = data.prepareStatement(SQL_SELECT_BOND_GRAPH);
			sth.setString(1, compoundID);
			results = sth.executeQuery();
			while ( results.next() ) {
				bondList.add(String.format("% 3d% 3d% 3d% 3d  0  0  0", results.getInt(1), results.getInt(2), results.getInt(3), results.getInt(4)));
			}
			results.close();
			sth.close();
			
			if ( chargeList.size() > 0 ) {
				int chargeSize = chargeList.size();
				int lineNumber = 1;
				int lineSize = ( chargeSize >= 8 ? 8 : chargeSize );
				int item = 1;
				StringBuffer line = new StringBuffer(String.format("M CHG%3d", lineSize));
				for ( String charge : chargeList ) {
					line.append(charge);
					if ( item == 8 ) {
						item = 1;
						propList.add(line.toString());
						lineNumber++;
						lineSize = ( chargeSize >= ( lineNumber * 8 ) ? 8 : chargeSize - ((lineNumber - 1) * 8) );
						line = new StringBuffer(String.format("M CHG%3d", lineSize));
					} else {
						item++;
					}
				}
				
			}
			
			out.println(compoundID);
			out.println(String.format("  %8s%10s2D","cyanos","0307761300"));
			out.println();
			out.println(String.format("% 3d% 3d% 3d% 3d% 3d% 3d% 3d% 3d% 3d% 3d% 3d%6s", atomCount, bondCount, 0, 0, 0, 0, 0, 0, 0, 0, propList.size() + 1, "V2000"));
	
			for (String atom: atomList ) {
				out.println(atom);
			}
			
			for (String bond: bondList ) {
				out.println(bond);
			}
			
			for ( String prop: propList ) {
				out.println(prop);
			}
			
			out.println("M  END");				
		} 
	}
}
