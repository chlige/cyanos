/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Savepoint;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.HydrogenAdder;
import org.openscience.cdk.tools.MFAnalyser;
import org.openscience.cdk.tools.ValencyChecker;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.web.InchiGenerator;
import edu.uic.orjala.cyanos.web.Job;
import edu.uic.orjala.cyanos.web.MultiPartRequest;
import edu.uic.orjala.cyanos.web.html.HtmlList;

/**
 * @author George Chlipala
 *
 */
public class CompoundUpload extends Job {

	private static final String THREAD_LABEL = "compound-upload";
	
	protected final Map<String,String> template = new HashMap<String,String>();
	
	public static final String FORCE_UPLOAD = "forceUpload";
	public static final String COMPOUND_ID = "compoundID";
	public static final String NOTES_PROPERTY = "notes";
	public static final String NAME_PROPERTY = "name";
	public static final String PROJECT_PROPERY = "projectCol";
	public static final String STATIC_PROJECT = "staticProject";
	
	private InputStream sdfData;
	
	public static final String[] templateKeys = { FORCE_UPLOAD, COMPOUND_ID, NOTES_PROPERTY, NAME_PROPERTY, PROJECT_PROPERY, STATIC_PROJECT };
	
	private static final String TITLE = "Compound SDF Upload";
	
	private boolean safeUpload = true;

	public CompoundUpload(SQLData data) {
		super(data);
		this.type = TITLE;
	}
	
	/**
	 * Start paring the upload.
	 * @throws DataException 
	 * @throws IOException 
	 * @throws ServletException 
	 */
	public void startJob(HttpServletRequest req, InputStream sdfStream) throws DataException, ServletException, IOException {
		req = MultiPartRequest.parseRequest(req);
		if ( this.parseThread == null ) {
			this.create();
			this.updateTemplate(req);
			this.safeUpload = req.getParameter(FORCE_UPLOAD) == null;
			this.sdfData = sdfStream;
			this.parseThread = new Thread(this, THREAD_LABEL);
			this.parseThread.start();
		}
	}
	
	void updateTemplate(HttpServletRequest manager) {
		String[] keys = getTemplateKeys();
		for (int i = 0; i < keys.length; i++ ) {
			if ( manager.getParameter(keys[i]) != null ) {
				template.put(keys[i], manager.getParameter(keys[i]));
			}
		}
		
	}

	protected void finishJob() {
		try {
			this.endDate = new Date();
			this.update();
			this.myData.close();
			this.myData.closeDBC();

		} catch (DataException e) {
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();			
		} finally {
			this.working = false;
		}
	}

	/**
	 * Stop parsing the upload.
	 */
	public void stopJob() {
		if ( this.parseThread != null ) {
			this.working = false;
			this.parseThread = null;
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if ( this.working ) return;
		IteratingMDLReader reader = new IteratingMDLReader(this.sdfData, DefaultChemObjectBuilder.getInstance());	
		this.working = true;

		HtmlList resultList = new HtmlList();
		resultList.unordered();
		try {

			String compoundIDProp = this.template.get(COMPOUND_ID);
			boolean useCompoundProp = (compoundIDProp != null && compoundIDProp.length() > 0 );
			
			String notesProp = this.template.get(NOTES_PROPERTY);
			boolean setNotes = (notesProp != null && notesProp.length() > 0);
			
			String nameProp = this.template.get(NAME_PROPERTY);
			boolean setName = ( nameProp != null && nameProp.length() > 0 );
			
			String staticProject = (String)this.template.get(STATIC_PROJECT);
			
			String projectProp = this.template.get(PROJECT_PROPERY);
			boolean useProjectProp = (projectProp != null && projectProp.length() > 0 );

			int row = 1;
			Savepoint savepoint = null;
			
			
			SmilesGenerator smileGen = new SmilesGenerator();
			smileGen.setUseAromaticityFlag(true);

			HydrogenAdder hAdder = new HydrogenAdder(new ValencyChecker());
		
			while ( reader.hasNext() && this.working ) {
				try {

					Molecule molecule = (Molecule)reader.next();
					savepoint = this.myData.setSavepoint();
					
					String compoundID = null;
					
					if ( useCompoundProp ) {
						compoundID = (String)molecule.getProperty(compoundIDProp);
					} else {
						compoundID = (String)molecule.getProperty(CDKConstants.TITLE);
					}
					
					if ( compoundID != null ) {
						HtmlList currResults = new HtmlList();
						currResults.unordered();

						try {
							Compound compound = SQLCompound.load(myData, compoundID);
							compound.setManualRefresh();

							boolean update = true;
							String myProject = staticProject;
							if ( useProjectProp ) myProject = (String)molecule.getProperty(projectProp);
							if ( compound.first() ) {
								currResults.addItem(FOUND_TAG + "Compound found.");
								if ( safeUpload ) {
									currResults.addItem(SKIP_TAG + "Information skipped: SAFE UPLOAD");
									update = false;
								} else {
									compound.setProjectID(myProject);
								}
							} else {
								compound = SQLCompound.create(this.myData, compoundID);
								compound.setProjectID(myProject);
								currResults.addItem(SUCCESS_TAG + "Created new compound record.");
							}

							if ( compound.first() && update ) {
								compound.setManualRefresh();
								
								StringWriter mdlData = new StringWriter();
								MDLWriter mdlWrite = new MDLWriter(mdlData);
								mdlWrite.writeMolecule(molecule);
								
								String mdlString = mdlData.toString();
								
								compound.setMDLData(mdlString);
								
								compound.setSmilesString(smileGen.createSMILES(molecule));									
								String inchiString = InchiGenerator.convertMOL(mdlString);
								compound.setInChiString(inchiString);
								compound.setInChiKey(InchiGenerator.getInChiKey(inchiString));
																
								
						// For CDK v1.0.4
							    hAdder.addExplicitHydrogensToSatisfyValency(molecule);
							    MFAnalyser formulaMaker = new MFAnalyser(molecule);

						//CDK v.1.2.3
						/*		
						  		IMolecularFormula aFormula = MolecularFormulaManipulator.getMolecularFormula(aMolecule);
							    aCompound.setManualRefresh();
								aCompound.setName(this.getFormValue("name"));
						 */
						// for CDK v1.0.4		
							    compound.setFormula(formulaMaker.getMolecularFormula());
					    // TODO should set the proper sig figs.
							    compound.setAverageMass(new BigDecimal(formulaMaker.getNaturalMass()));
							    compound.setMonoisotopicMass(new BigDecimal(formulaMaker.getMass()));
						// CDK v1.2.3
						/*	    aCompound.setFormula(MolecularFormulaManipulator.getHillString(aFormula));
							    aCompound.setAverageMass(MolecularFormulaManipulator.getNaturalExactMass(aFormula));
							    aCompound.setMonoisotopicMass(MolecularFormulaManipulator.getMajorIsotopeMass(aFormula));
						 */
								
								if (setName) {
									String name = (String)molecule.getProperty(nameProp);
									if ( name != null ) {
										compound.setName(name);
									} else {
										compound.setName(compoundID);
									}
								} else {
									compound.setName(compoundID);
								}

								if ( setNotes ) {
									String notes = (String)molecule.getProperty(notesProp);
									if ( notes != null ) {
										compound.setNotes(notes);
									}
								}
								compound.refresh();
								compound.setAutoRefresh();
								this.myData.commit();
								this.myData.releaseSavepoint(savepoint);
								currResults.addItem(SUCCESS_TAG + "Information updated.");
							} else {
								currResults.addItem(FAILED_TAG + "Information update failed.");
							}
						} catch (DataException e) {
							currResults.addItem("<FONT COLOR='red'><B>ERROR:</B></FONT> " + e.getMessage());
							e.printStackTrace();
						}
						resultList.addItem(String.format("Row #:%d %s", row, currResults.toString()));
					} else {
						resultList.addItem(String.format("Row #:%d NO COMPOUND ID FOUND", row));
					}
					row++;
				} catch (Exception e) {
					if ( savepoint != null ) this.myData.rollback(savepoint);
					else this.myData.rollback();
					this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
					e.printStackTrace();
				}
			}
			this.progress = 1.0f;
		} catch (Exception e) {
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
			this.working = false;
		}
		this.messages.append(resultList.toString());
		this.finishJob();
	}

	public String[] getTemplateKeys() {
		return templateKeys;
	}
}
