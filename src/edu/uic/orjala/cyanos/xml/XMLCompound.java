/**
 * 
 */
package edu.uic.orjala.cyanos.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.CompoundObject;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.sql.SQLCompound;

/**
 * @author George Chlipala
 *
 */
public class XMLCompound extends DefaultHandler implements Compound  {


	static final String COMPOUND_ID = "compound_id";
	static final String NAME = "name";
	static final String FORMULA = "formula";
	static final String INCHI_KEY = "inchi_key";
	static final String INCHI_STRING = "inchi_string";
	static final String SMILES_STRING = "smiles_string";
	static final String PROJECT_ID = "project_id";
	static final String NOTES = "notes";
	
	private boolean inNotes = true;
	private StringBuffer cdata;
	
	/**
	 * @author George Chlipala
	 *
	 */
	public final class CompoundRecord {

		protected String id;
		protected String name;
		protected String formula;
		protected String inchiKey;
		protected String inchiString;
		protected String smilesString;
		protected String notes;
		protected String projectID;
		
		/**
		 * 
		 */
		protected CompoundRecord() {
			
		}
	}

	private final ArrayList<CompoundRecord> compoudList = new ArrayList<CompoundRecord>();
	private CompoundRecord currentCompound = null;
	private ListIterator<CompoundRecord> compoundIter;
	
	
	public static void generateXML(Compound compounds, XMLStreamWriter xtw) throws XMLStreamException, DataException {		
		compounds.beforeFirst();
		while ( compounds.next() ) {
			xtw.writeStartElement("compound");
			xtw.writeAttribute(COMPOUND_ID, compounds.getID());
			xtw.writeAttribute(NAME, compounds.getName());
			xtw.writeAttribute(FORMULA, compounds.getFormula());
			if ( compounds.getInChiKey() != null )
				xtw.writeAttribute(INCHI_KEY, compounds.getInChiKey());
			xtw.writeStartElement(NOTES);
			xtw.writeCData(compounds.getNotes());
			xtw.writeEndElement();
			xtw.writeEndElement();				
		}
		compounds.beforeFirst();
	}
	
	public static XMLCompound load(Reader in) throws ParserConfigurationException, SAXException, IOException {
		XMLCompound compoundList = new XMLCompound();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		InputSource is = new InputSource(in);
		is.setEncoding("UTF-8");
		saxParser.parse(is, compoundList);
		return compoundList;
	}
	
	/**
	 * 
	 */
	XMLCompound() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.DataFileObject#getDataFileClass()
	 */
	@Override
	public String getDataFileClass() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.DataFileObject#linkDataFile(edu.uic.orjala.cyanos.ExternalFile, java.lang.String)
	 */
	@Override
	public void linkDataFile(ExternalFile aFile, String dataType)
			throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.DataFileObject#linkDataFile(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void linkDataFile(String path, String dataType, String description,
			String mimeType) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.DataFileObject#updateDataFile(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void updateDataFile(String path, String dataType,
			String description, String mimeType) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.DataFileObject#getDataFiles()
	 */
	@Override
	public ExternalFile getDataFiles() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.DataFileObject#getDataFilesForType(java.lang.String)
	 */
	@Override
	public ExternalFile getDataFilesForType(String dataType)
			throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.DataFileObject#unlinkDataFile(edu.uic.orjala.cyanos.ExternalFile)
	 */
	@Override
	public void unlinkDataFile(ExternalFile aFile) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.DataFileObject#unlinkDataFile(java.lang.String)
	 */
	@Override
	public void unlinkDataFile(String path) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#next()
	 */
	@Override
	public boolean next() throws DataException {
		if ( this.compoundIter.hasNext() ) {
			this.currentCompound = this.compoundIter.next();
			return true; 
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#previous()
	 */
	@Override
	public boolean previous() throws DataException {
		if ( this.compoundIter.hasPrevious() ) {
			this.currentCompound = this.compoundIter.previous();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#first()
	 */
	@Override
	public boolean first() throws DataException {
		this.beforeFirst();
		return this.next();
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#last()
	 */
	@Override
	public boolean last() throws DataException {
		if ( this.compoudList.size() > 0 ) {
			this.compoundIter = this.compoudList.listIterator(this.compoudList.size() - 1);
			return this.next();
		}
		return false;
		
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#beforeFirst()
	 */
	@Override
	public void beforeFirst() throws DataException {
		this.currentCompound = null;
		this.compoundIter = this.compoudList.listIterator();
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#afterLast()
	 */
	@Override
	public void afterLast() throws DataException {
		this.currentCompound = null;
		
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#setManualRefresh()
	 */
	@Override
	public void setManualRefresh() {
		// DOES NOTHING.
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#setAutoRefresh()
	 */
	@Override
	public void setAutoRefresh() {
		// DOES NOTHING
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#refresh()
	 */
	@Override
	public void refresh() throws DataException {
		// DOES NOTHING
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#count()
	 */
	@Override
	public int count() throws DataException {
		return this.compoudList.size();
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#isAllowed(int)
	 */
	@Override
	public boolean isAllowed(int permission) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#isLoaded()
	 */
	@Override
	public boolean isLoaded() throws DataException {
		return ( this.compoudList.size() > 0 );
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#gotoRow(int)
	 */
	@Override
	public boolean gotoRow(int row) throws DataException {
		if ( this.compoudList.size() >= ( row + 1 )) {
			this.compoundIter = this.compoudList.listIterator(row);
			return this.next();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#setSavepoint()
	 */
	@Override
	public Savepoint setSavepoint() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#setSavepoint(java.lang.String)
	 */
	@Override
	public Savepoint setSavepoint(String name) throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#setAutoCommit(boolean)
	 */
	@Override
	public void setAutoCommit(boolean value) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#commit()
	 */
	@Override
	public void commit() throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#rollback()
	 */
	@Override
	public void rollback() throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#rollback(java.sql.Savepoint)
	 */
	@Override
	public void rollback(Savepoint savepoint) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#getAttribute(java.lang.String)
	 */
	@Override
	public String getAttribute(String attribute) throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#setAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void setAttribute(String attribute, String value)
			throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#isLast()
	 */
	@Override
	public boolean isLast() throws DataException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.RemoteObject#getRemoteID()
	 */
	@Override
	public String getRemoteID() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.RemoteObject#getRemoteHostID()
	 */
	@Override
	public String getRemoteHostID() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getID()
	 */
	@Override
	public String getID() {
		if ( this.currentCompound != null ) {
			return this.currentCompound.id;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getName()
	 */
	@Override
	public String getName() throws DataException {
		if ( this.currentCompound != null ) {
			return this.currentCompound.name;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setName(java.lang.String)
	 */
	@Override
	public void setName(String newValue) throws DataException {
		if ( this.currentCompound != null ) {
			this.currentCompound.name = newValue;
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getFormula()
	 */
	@Override
	public String getFormula() throws DataException {
		if ( this.currentCompound != null ) {
			return this.currentCompound.formula;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getHTMLFormula()
	 */
	@Override
	public String getHTMLFormula() throws DataException {
		return SQLCompound.getHTMLFormula(this.currentCompound.formula);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setFormula(java.lang.String)
	 */
	@Override
	public void setFormula(String newValue) throws DataException {
		if ( this.currentCompound != null )
			this.currentCompound.formula = newValue;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getSmilesString()
	 */
	@Override
	public String getSmilesString() throws DataException {
		if ( this.currentCompound != null ) {
			return this.currentCompound.smilesString;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setSmilesString(java.lang.String)
	 */
	@Override
	public void setSmilesString(String newValue) throws DataException {
		if ( this.currentCompound != null )
			this.currentCompound.smilesString = newValue;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getInChiString()
	 */
	@Override
	public String getInChiString() throws DataException {
		if ( this.currentCompound != null ) {
			return this.currentCompound.inchiString;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setInChiString(java.lang.String)
	 */
	@Override
	public void setInChiString(String newValue) throws DataException {
		if ( this.currentCompound != null )
			this.currentCompound.inchiString = newValue;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getInChiKey()
	 */
	@Override
	public String getInChiKey() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setInChiKey(java.lang.String)
	 */
	@Override
	public void setInChiKey(String newValue) throws DataException {
		if ( this.currentCompound != null )
			this.currentCompound.inchiKey = newValue;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getAverageMass()
	 */
	@Override
	public BigDecimal getAverageMass() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setAverageMass(java.lang.String)
	 */
	@Override
	public void setAverageMass(String newValue) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setAverageMass(java.math.BigDecimal)
	 */
	@Override
	public void setAverageMass(BigDecimal newValue) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getMonoisotopicMass()
	 */
	@Override
	public BigDecimal getMonoisotopicMass() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setMonoisotopicMass(java.math.BigDecimal)
	 */
	@Override
	public void setMonoisotopicMass(BigDecimal newValue) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setMonoisotopicMass(java.lang.String)
	 */
	@Override
	public void setMonoisotopicMass(String newValue) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getNotes()
	 */
	@Override
	public String getNotes() throws DataException {
		if ( this.currentCompound != null )
			return this.currentCompound.notes;
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setNotes(java.lang.String)
	 */
	@Override
	public void setNotes(String newNotes) throws DataException {
		if ( this.currentCompound != null )
			this.currentCompound.notes = newNotes;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#addNotes(java.lang.String)
	 */
	@Override
	public void addNotes(String newNotes) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getMDLData()
	 */
	@Override
	public String getMDLData() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getMDLDataStream()
	 */
	@Override
	public InputStream getMDLDataStream() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setMDLData(java.lang.String)
	 */
	@Override
	public void setMDLData(String newValue) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#hasMDLData()
	 */
	@Override
	public boolean hasMDLData() throws DataException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#clearMDLData()
	 */
	@Override
	public void clearMDLData() throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getThumbnail()
	 */
	@Override
	public byte[] getThumbnail() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getThumbnailStream()
	 */
	@Override
	public InputStream getThumbnailStream() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#hasThumbnail()
	 */
	@Override
	public boolean hasThumbnail() throws DataException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#clearThumbnail()
	 */
	@Override
	public void clearThumbnail() throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setTumbnail(byte[])
	 */
	@Override
	public void setTumbnail(byte[] data) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#addNMRDatafile(edu.uic.orjala.cyanos.ExternalFile)
	 */
	@Override
	public void addNMRDatafile(ExternalFile aFile) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#addMSDatafile(edu.uic.orjala.cyanos.ExternalFile)
	 */
	@Override
	public void addMSDatafile(ExternalFile aFile) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#addIRDatafile(edu.uic.orjala.cyanos.ExternalFile)
	 */
	@Override
	public void addIRDatafile(ExternalFile aFile) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#addUVDatafile(edu.uic.orjala.cyanos.ExternalFile)
	 */
	@Override
	public void addUVDatafile(ExternalFile aFile) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#dataTypes()
	 */
	@Override
	public String[] dataTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getSamples()
	 */
	@Override
	public Sample getSamples() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getMaterials()
	 */
	@Override
	public Material getMaterials() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getRetentionTime(edu.uic.orjala.cyanos.CompoundObject)
	 */
	@Override
	public BigDecimal getRetentionTime(CompoundObject aSample)
			throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getRetentionTimeForSample(java.lang.String)
	 */
	@Override
	public BigDecimal getRetentionTimeForSample(String sampleID)
			throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getRetentionTimeForMaterial(java.lang.String)
	 */
	@Override
	public BigDecimal getRetentionTimeForMaterial(String materialID)
			throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getRetentionTimeForSeparation(java.lang.String)
	 */
	@Override
	public BigDecimal getRetentionTimeForSeparation(String separationID)
			throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getProjectID()
	 */
	@Override
	public String getProjectID() throws DataException {
		if ( this.currentCompound != null )
			return this.currentCompound.projectID;
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#getProject()
	 */
	@Override
	public Project getProject() throws DataException {
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setProjectID(java.lang.String)
	 */
	@Override
	public void setProjectID(String newValue) throws DataException {
		
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Compound#setProject(edu.uic.orjala.cyanos.Project)
	 */
	@Override
	public void setProject(Project aProject) throws DataException {
		this.setProjectID(aProject.getID());
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		this.compoudList.clear();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ( localName == null || localName.length() == 0) 
			localName = qName;

		if ( localName.equalsIgnoreCase("compound") ) {
			this.currentCompound = new CompoundRecord();
			this.currentCompound.id = attributes.getValue(COMPOUND_ID);
			this.currentCompound.formula = attributes.getValue(FORMULA);
			this.currentCompound.name = attributes.getValue(NAME);
			this.currentCompound.inchiKey = attributes.getValue(INCHI_KEY);
			this.currentCompound.inchiString = attributes.getValue(INCHI_STRING);
			this.currentCompound.smilesString = attributes.getValue(SMILES_STRING);
			this.currentCompound.projectID = attributes.getValue(PROJECT_ID);
			this.compoudList.add(currentCompound);
		} else if ( localName.equalsIgnoreCase(NOTES) ) {
			this.inNotes = true;
			this.cdata = new StringBuffer();
		}
	}

	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {		
		if ( this.inNotes && this.currentCompound != null ) {
			String value = new String(ch, start, length);
			this.cdata.append(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ( localName == null || localName.length() == 0) 
			localName = qName;

		if ( this.inNotes && localName.equalsIgnoreCase(NOTES) ) {
			this.inNotes = false;
			this.currentCompound.notes = this.cdata.toString();
		} else if ( localName.equalsIgnoreCase("compound") ) {
			this.currentCompound = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		this.currentCompound = null;
		this.compoundIter = this.compoudList.listIterator();
	}

	@Override
	public boolean hasDataFile(String path) throws DataException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ExternalFile getDataFile(String path) throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	
}
