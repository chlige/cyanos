/**
 * 
 */
package edu.uic.orjala.cyanos.xml;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.AssayPlate;
import edu.uic.orjala.cyanos.BasicObject;
import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLAssayPlate;
import edu.uic.orjala.cyanos.sql.SQLCollection;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLHarvest;
import edu.uic.orjala.cyanos.sql.SQLMaterial;
import edu.uic.orjala.cyanos.sql.SQLProject;
import edu.uic.orjala.cyanos.sql.SQLStrain;

/**
 * @author George Chlipala
 *
 */
public class ProjectUpdateXMLHandler extends DefaultHandler {

	public static final String ELEMENT_STRAIN = "strain";
	public static final String ELEMENT_MATERIAL = "material";
	public static final String ELEMENT_HARVEST = "harvest";
	public static final String ELEMENT_ASSAY = "assay";
	public static final String ELEMENT_COLLECTION = "collection";
	public static final String ELEMENT_PROJECT = "project-update";
	public static final String ELEMENT_ASSAY_DATA = "assayData";
	
	public static final String ELEMENT_EXTRACT = "extract";
	public static final String ELEMENT_EXTRACT_METHOD = "extractMethod";

	public static final String ATTR_EXTRACT_SOLVENT = "extractSolvent";
	public static final String ATTR_EXTRACT_TYPE = "extractType";
	public static final String ATTR_ASSAY_DATA_LOCATION = "location";
	
	public static final String ATTR_MATERIAL_AMOUNT = "amount";
	public static final String ATTR_HARVEST_CELL_MASS = "mass";
	public static final String ATTR_HARVEST_MEDIA_VOL = "volume";
	
	public static final String ATTR_HOST_ID = "host_id";
	
	private SQLData myData;
	
	private static final int NO_STATE = 0;
	
	private static final int IN_STRAIN = 1;
	private static final int IN_MATERIAL = 2;
	private static final int IN_ASSAY = 3;
	private static final int IN_COLLECTION = 4;
	private static final int IN_HARVEST = 5;

	private boolean inSubfield = false;
	
	private String subfieldName = null;
	
	private int state = NO_STATE;
	
	private BasicObject myObject = null;
	private Project project = null;
	private String hostID = null;
//	private AssayPlate assayData = null;

	private int strainCount = 0;
	private int materialCount = 0;
	private int assayCount = 0;
	private int harvestCount = 0;
	private int collectionCount = 0;
	
	private final NumberFormat numParser = NumberFormat.getInstance();
	
	private PreparedStatement assayDataSth = null;
	
	private static final String[] STRAIN_ATTRS = {	SQLStrain.SOURCE_COLUMN, SQLStrain.COLLECTION_COLUMN, SQLStrain.ISOLATION_COLUMN, 
		SQLStrain.DATE_COLUMN, SQLStrain.NAME_COLUMN, SQLStrain.GENUS_COLUMN, 
		SQLStrain.STATUS_COLUMN, SQLStrain.DEFAULT_MEDIA_COLUMN, SQLStrain.REMOVE_DATE_COLUMN, 
		SQLStrain.REMOVE_REASON_COLUMN };
	
	private static final String[] HARVEST_ATTRS = { SQLHarvest.CULTURE_ID_COLUMN, SQLHarvest.DATE_COLUMN, SQLHarvest.PREP_DATE_COLUMN,
		SQLHarvest.COLOR_COLUMN, SQLHarvest.TYPE_COLUMN };
	
	private static final String[] MATERIAL_ATTRS = { SQLMaterial.LABEL_COLUMN, SQLMaterial.CULTURE_ID_COLUMN, SQLMaterial.DATE_COLUMN
		// SQLMaterial.REMOVED_DATE_COLUMN, SQLMaterial.REMOVED_USER_COLUMN 
		};
	
	private static final String[] COLLECTION_ATTRS = { SQLCollection.COLLECTOR_COLUMN, SQLCollection.DATE_COLUMN, 
		SQLCollection.LOCATION_COLUMN, SQLCollection.LATITUDE_COLUMN, SQLCollection.LONGITUDE_COLUMN,
		SQLCollection.PRECISION_COLUMN };
	
	private static final String[] ASSAY_ATTRS = { SQLAssay.DATE_COLUMN, SQLAssay.NAME_COLUMN, SQLAssay.TARGET_COLUMN,
		SQLAssay.UNIT_COLUMN, SQLAssay.LENGTH_COLUMN, SQLAssay.WIDTH_COLUMN, SQLAssay.ACTIVE_LEVEL_COLUMN, SQLAssay.SIG_FIGS_COLUMN,
		SQLAssay.ACTIVE_OPERATOR_COLUMN };
	
	private static final String[] ASSAY_DATA_ATTRS = { SQLAssayPlate.CONCENTRATION_COLUMN,
		SQLAssayPlate.CULTURE_ID_COLUMN, // SQLAssayPlate.MATERIAL_ID_COLUMN,  SQLAssayPlate.SAMPLE_ID_COLUMN,
		SQLAssayPlate.NAME_COLUMN, SQLAssayPlate.VALUE_STDEV_COLUMN	};
	
	private static final String[] STRAIN_FIELDS = { SQLStrain.NOTES_COLUMN };
	private static final String[] COLLECTION_FIELDS = { SQLCollection.NOTES_COLUMN };
	private static final String[] MATERIAL_FIELDS = { SQLMaterial.NOTES_COLUMN, ELEMENT_EXTRACT_METHOD };
	private static final String[] ASSAY_FIELDS = { SQLAssay.NOTES_COLUMN };
	private static final String[] HARVEST_FIELDS = { SQLHarvest.NOTES_COLUMN };
	
	private StringBuffer cdata = new StringBuffer();
	
	/**
	 * @throws DataException 
	 * 
	 */
	public ProjectUpdateXMLHandler(SQLData data, String hostID) throws DataException {
		this(data);
	}

	/**
	 * @throws DataException 
	 * 
	 */
	public ProjectUpdateXMLHandler(SQLData data) throws DataException {
		this.myData = data;
		this.assayDataSth = data.prepareStatement(SQLAssayPlate.SQL_INSERT_XML);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {		
		if ( inSubfield && myObject != null ) {
			String value = new String(ch, start, length);
			this.cdata.append(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ( localName == null || localName.length() == 0) 
			localName = qName;
		if ( inSubfield && localName.equals(subfieldName) ) {
			if ( this.cdata.length() > 0 ) {
				try {
					if ( this.inClass(IN_MATERIAL) && this.subfieldName.equals(ELEMENT_EXTRACT_METHOD) ) {
						((Material)myObject).setExtractMethod(this.cdata.toString());
					} else {
						myObject.setAttribute(subfieldName, this.cdata.toString());
					}
				} catch (DataException e) {
					throw new SAXException(e);
				}
				this.cdata.delete(0, this.cdata.length() - 1);
			}
			this.inSubfield = false;
			subfieldName = null;
		} else if ( localName.equalsIgnoreCase(ELEMENT_ASSAY) && this.inClass(IN_ASSAY) ) {
			this.assayCount++;
			this.clearObject();
		} else if ( localName.equalsIgnoreCase(ELEMENT_COLLECTION) && this.inClass(IN_COLLECTION) ) {
			this.collectionCount++;
			this.clearObject();
		} else if ( localName.equalsIgnoreCase(ELEMENT_MATERIAL) && this.inClass(IN_MATERIAL) ) {
			this.materialCount++;
			this.clearObject();
		} else if ( localName.equalsIgnoreCase(ELEMENT_STRAIN) && this.inClass(IN_STRAIN) ) {
			this.strainCount++;
			this.clearObject();
		} else if ( localName.equalsIgnoreCase(ELEMENT_HARVEST) && this.inClass(IN_HARVEST) ) {
			this.harvestCount++;
			this.clearObject();
		} 
	}

	private void clearObject() throws SAXException { 
		try {
			if ( this.myObject != null )
				this.myObject.refresh();
			this.myObject = null;
		} catch (DataException e) {
			throw new SAXException(e);
		}
		this.unsetState();
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		this.myObject = null;
		this.unsetState();
		this.subfieldName = null;
		this.inSubfield = false;
		
		this.strainCount = 0;
		this.materialCount = 0;
		this.assayCount = 0;
		this.collectionCount = 0;
		this.harvestCount = 0;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ( localName == null || localName.length() == 0) 
			localName = qName;

		if ( this.state == NO_STATE ) {
			try {
				this.parseElement(localName, attributes);
			} catch (DataException e) {
				throw new SAXException(e);
			} catch (SQLException e) {
				throw new SAXException(e);
			}
		} else if ( this.inClass(IN_ASSAY) ) {
			this.checkSubField(localName, ASSAY_FIELDS);
			if ( localName.equals(ELEMENT_ASSAY_DATA) ) {
				try {
					this.addAssayData(attributes);
				} catch (ParseException e) {
					throw new SAXException(e);
				}
			}
		} else if ( this.inClass(IN_MATERIAL) ) {
			this.checkSubField(localName, MATERIAL_FIELDS);
			if ( localName.equals(ELEMENT_EXTRACT) && this.myObject instanceof Material) {
				Material material = (Material) this.myObject;
				try {
					material.setExtractType(attributes.getValue(ATTR_EXTRACT_TYPE));
					material.setExtractSolvent(attributes.getValue(ATTR_EXTRACT_SOLVENT));
					Harvest harvest = this.setupHarvest(attributes.getValue(SQLHarvest.ID_COLUMN));
					if ( harvest != null && harvest.first() ) 
						material.setExtractSource(harvest);
				} catch (DataException e) {
					throw new SAXException(e);
				}
			} 
		} else if ( this.inClass(IN_STRAIN) ) {
			this.checkSubField(localName, STRAIN_FIELDS);
		} else if ( this.inClass(IN_COLLECTION) ) {
			this.checkSubField(localName, COLLECTION_FIELDS);
		} else if ( this.inClass(IN_HARVEST) ) {
			this.checkSubField(localName, HARVEST_FIELDS);
		}
	}
	
	private void checkSubField(String localName, String[] possibles ) {
		for ( String name: possibles ) {
			if ( localName.equalsIgnoreCase(name) ) {
				this.subfieldName = name;
				this.inSubfield = true;
				this.cdata = new StringBuffer();
				break;
			}
		}
	}
	
	private void addAssayData(Attributes attributes) throws SAXException, ParseException {
		String activity = attributes.getValue(SQLAssayPlate.VALUE_COLUMN);
		String location = attributes.getValue(ATTR_ASSAY_DATA_LOCATION);
		int[] loc = SQLAssayPlate.parseLocation(location);
		
		int sign = 0;
		
		if ( activity.startsWith(">") ) {
			sign = 1;
			activity = activity.substring(1);
		} else if ( activity.startsWith("<") ) {
			sign = -1;
			activity = activity.substring(1);
		}

		Number actNumber = this.numParser.parse(activity);

		try {
			for ( int i = 1; i < SQLAssayPlate.ALL_COLUMNS.length; i++ ) {
				String column = SQLAssayPlate.ALL_COLUMNS[i];

				if ( column.equals(SQLAssayPlate.ROW_COLUMN) ) {
					this.assayDataSth.setInt(i + 1, loc[0]);
				} else if ( column.equals(SQLAssayPlate.COLUMN_COLUMN) ) {
					this.assayDataSth.setInt(i + 1, loc[1]);
				} else if ( column.equals(SQLAssayPlate.MATERIAL_ID_COLUMN) ) {
					String materialID = attributes.getValue(column);
						try {
							Material material = this.setupMaterial(materialID);
							if ( material != null && material.first() )
								this.assayDataSth.setString(i + 1, material.getID());
						} catch (DataException e) {
							e.printStackTrace();
						}
				} else if ( column.equals(SQLAssayPlate.VALUE_COLUMN) ) {
					this.assayDataSth.setFloat(i + 1, actNumber.floatValue());
				} else if ( column.equals(SQLAssayPlate.VALUE_SIGN_COLUMN) ) {
					this.assayDataSth.setInt(i + 1, sign);
				} else {
					String value = attributes.getValue(column);
					if ( value != null )
						this.assayDataSth.setString(i + 1, value);
					else 
						this.assayDataSth.setNull(i + 1, Types.VARCHAR);
				}

			}
			this.assayDataSth.executeUpdate();
		} catch (SQLException e) {
			throw new SAXException(e);
		}

		/*
		String location = attributes.getValue(ATTR_ASSAY_DATA_LOCATION);
		String strainID = attributes.getValue(SQLAssayPlate.CULTURE_ID_COLUMN);
		try {
			this.assayData.gotoLocation(location);
			this.assayData.addCurrentLocation(strainID);
//			this.assayData.setActivity(attributes.getValue(SQLAssayPlate.VALUE_COLUMN));
			setAttributes(this.assayData, ASSAY_DATA_ATTRS, attributes);
			this.assayData.refresh();
		} catch (DataException e) {
			throw new SAXException(e);
		}
		*/
		
	}

	private void parseElement(String localName, Attributes attributes) throws DataException, SQLException {
		if ( localName.equalsIgnoreCase(ELEMENT_PROJECT) ) {
			this.project = SQLProject.load(myData, attributes.getValue(SQLProject.ID_COLUMN));
			if ( this.hostID == null )
				this.hostID = attributes.getValue(ATTR_HOST_ID);
		} else if ( localName.equalsIgnoreCase(ELEMENT_ASSAY) ) {
			this.setState(IN_ASSAY);
			Assay assay = this.setupAssay(attributes.getValue(SQLAssay.ID_COLUMN));
			this.myObject = assay;
			setAttributes(this.myObject, ASSAY_ATTRS, attributes);
			assay.clearData();
		} else if ( localName.equalsIgnoreCase(ELEMENT_COLLECTION) ) {
			this.setState(IN_COLLECTION);
			this.myObject = this.setupCollection(attributes.getValue(SQLCollection.ID_COLUMN));
			setAttributes(this.myObject, COLLECTION_ATTRS, attributes);
		} else if ( localName.equalsIgnoreCase(ELEMENT_MATERIAL) ) {
			this.setState(IN_MATERIAL);
			Material material = this.setupMaterial(attributes.getValue(SQLMaterial.ID_COLUMN));
			this.myObject = material;
			setAttributes(this.myObject, MATERIAL_ATTRS, attributes);
			try {
				BigDecimal amount = SQLMaterial.parseAmount(attributes.getValue(ATTR_MATERIAL_AMOUNT), "g");
				material.setAmount(amount);
			} catch (NumberFormatException e) {
				System.err.print("ERROR: ProjectUpdateXMLHandler: Unable to parse number string: ");
				System.err.print(attributes.getValue(ATTR_MATERIAL_AMOUNT));
				System.err.print(" for material: ");
				System.err.println(attributes.getValue(SQLMaterial.ID_COLUMN));
			}
		} else if ( localName.equalsIgnoreCase(ELEMENT_STRAIN) ) {
			this.setState(IN_STRAIN);
			this.myObject = this.setupStrain(attributes.getValue(SQLStrain.ID_COLUMN));
			setAttributes(this.myObject, STRAIN_ATTRS, attributes);
		} else if ( localName.equalsIgnoreCase(ELEMENT_HARVEST) ) {
			this.setState(IN_HARVEST);
			this.myObject = this.setupHarvest(attributes.getValue(SQLHarvest.ID_COLUMN));
			setAttributes(this.myObject, HARVEST_ATTRS, attributes);
		}
	}

	private Strain setupStrain(String strainID) throws DataException {
		if ( strainID != null ) {
			Strain object = SQLStrain.load(myData, strainID);
			if ( object.first() ) {
				return object;
			} else {
				return SQLStrain.createInProject(myData, project.getID(), this.hostID, strainID);
			}
		}
		return null;
	}

	private Harvest setupHarvest(String harvestID) throws DataException {
		if ( harvestID != null ) {
			Harvest object = SQLHarvest.load(myData, hostID, harvestID);
			if ( object.first() ) {
				return object;
			} else {
				return SQLHarvest.createInProject(myData, project.getID(), hostID, harvestID);
			}
		}
		return null;
	}

	private Material setupMaterial(String materialID) throws DataException {
		if ( materialID != null ) {
			Material object = SQLMaterial.load(myData, hostID, materialID);
			if ( object.first() ) {
				return object;
			} else {
				return SQLMaterial.createInProject(myData, project.getID(), hostID, materialID);
			}
		}
		return null;
	}

	private Collection setupCollection(String collectionID) throws DataException {
		if ( collectionID != null ) {
			Collection collection = SQLCollection.load(myData, collectionID);
			if ( collection.first() ) {
				return collection;
			} else {
				return SQLCollection.createInProject(myData, project.getID(), hostID, collectionID);
			}
		}
		return null;
	}

	private static void setAttributes(BasicObject object, String[] attrs, Attributes attributes) throws DataException { 
		object.setManualRefresh();
		for ( String attr : attrs) {
			String value = attributes.getValue(attr);
			try {
				object.setAttribute(attr, value);
			} catch (DataException e) {
				throw e;
			}
		}
		object.refresh();
		object.setAutoRefresh();
	}
	
	private Assay setupAssay(String assayID) throws DataException, SQLException {
		if ( assayID != null ) {
			this.assayDataSth.setString(1, assayID);
			Assay assay = SQLAssay.load(myData, assayID);
			if ( assay.first() ) {
				return assay;
			} else {
				return SQLAssay.createInProject(myData, project.getID(), hostID, assayID);
			}
		}
		return null;
	}
	
	private void setState(int aBit) {
		this.state = aBit;
	}

	private boolean inClass(int objectClass) {
		return ( this.state == objectClass );
	}
	
	private void unsetState() {
		this.state = NO_STATE;
	}
	
	public static void writeStrains(Strain strain, XMLStreamWriter writer) throws XMLStreamException, DataException {
		strain.beforeFirst();		
		while ( strain.next() ) {

			writer.writeStartElement(ELEMENT_STRAIN);
			writer.writeAttribute(SQLStrain.ID_COLUMN, strain.getRemoteID());

			writeAttributes(writer, strain, STRAIN_ATTRS);

			String notes = strain.getNotes();
			if ( notes != null ) {
				writer.writeStartElement(SQLStrain.NOTES_COLUMN);
				writer.writeCData(notes);
				writer.writeEndElement();
			}

			writer.writeEndElement();
		}
	}
	
	public static void writeHarvests(Harvest harvests, XMLStreamWriter writer) throws DataException, XMLStreamException {
		harvests.beforeFirst();
		
		while ( harvests.next() ) {
			writer.writeStartElement(ELEMENT_HARVEST);
			writer.writeAttribute(SQLHarvest.ID_COLUMN, harvests.getRemoteID());
			BigDecimal amount = harvests.getCellMass();
			if ( amount != null )
				writer.writeAttribute(ATTR_HARVEST_CELL_MASS, amount.toPlainString());			
			
			amount = harvests.getMediaVolume();
			if ( amount != null )
				writer.writeAttribute(ATTR_HARVEST_MEDIA_VOL, amount.toPlainString());
			
			writeAttributes(writer, harvests, HARVEST_ATTRS);
			
			String notes = harvests.getNotes();
			if ( notes != null ) {
				writer.writeStartElement(SQLHarvest.NOTES_COLUMN);
				writer.writeCData(notes);
				writer.writeEndElement();
			}
			
			writer.writeEndElement();
		}	
	}


	public static void writeMaterials(Material material, XMLStreamWriter writer) throws XMLStreamException, DataException {
		material.beforeFirst();

		while ( material.next() ) {

			writer.writeStartElement(ELEMENT_MATERIAL);
			writer.writeAttribute(SQLMaterial.ID_COLUMN, material.getRemoteID());
			BigDecimal amount = material.getAmount();
			writer.writeAttribute(ATTR_MATERIAL_AMOUNT, SQLMaterial.autoFormatAmount(amount, SQLMaterial.MASS_TYPE, "k", "m", "u"));
			
			writeAttributes(writer, material, MATERIAL_ATTRS);
			

			if ( material.isExtract() ) {
				writer.writeStartElement(ELEMENT_EXTRACT);

				String value = material.getExtractType();
				if ( value != null )
					writer.writeAttribute(ATTR_EXTRACT_TYPE, material.getExtractType());

				writer.writeAttribute(SQLHarvest.ID_COLUMN, material.getExtractSource().getRemoteID());

				value = material.getExtractSolvent();
				if ( value != null )
					writer.writeAttribute(ATTR_EXTRACT_SOLVENT, material.getExtractSolvent());

				value = material.getExtractMethod();
				if ( value != null ) {
					writer.writeStartElement(ELEMENT_EXTRACT_METHOD);
					writer.writeCData(material.getExtractMethod());
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}

			String notes = material.getNotes();
			if ( notes != null ) {
				writer.writeStartElement(SQLMaterial.NOTES_COLUMN);
				writer.writeCData(notes);
				writer.writeEndElement();
			}

			writer.writeEndElement();
		}
	}

	public static void writeCollections(Collection collection, XMLStreamWriter writer) throws XMLStreamException, DataException {
		collection.beforeFirst();

		while ( collection.next() ) {
			writer.writeStartElement(ELEMENT_COLLECTION);
			writer.writeAttribute(SQLCollection.ID_COLUMN, collection.getRemoteID());

			writeAttributes(writer, collection, COLLECTION_ATTRS);

			String notes = collection.getNotes();
			if ( notes != null ) {
				writer.writeStartElement(SQLCollection.NOTES_COLUMN);
				writer.writeCData(notes);
				writer.writeEndElement();
			}

			writer.writeEndElement();
		}
	}

	public static void writeAssays(Assay assay, XMLStreamWriter writer) throws XMLStreamException, DataException {
		assay.beforeFirst();

		while ( assay.next() ) {
			writer.writeStartElement(ELEMENT_ASSAY);
			writer.writeAttribute(SQLAssay.ID_COLUMN, assay.getRemoteID());

			writeAttributes(writer, assay, ASSAY_ATTRS);

			String notes = assay.getNotes();
			if ( notes != null ) {
				writer.writeStartElement(SQLAssay.NOTES_COLUMN);
				writer.writeCData(notes);
				writer.writeEndElement();
			}

			AssayPlate data = assay.getAssayData();
			data.beforeFirst();
			
			while ( data.next() ) {
				writer.writeStartElement(ELEMENT_ASSAY_DATA);
				writer.writeAttribute(SQLAssayPlate.VALUE_COLUMN, data.getActivityString());
				writer.writeAttribute(ATTR_ASSAY_DATA_LOCATION, data.getLocation());
				Material material = data.getMaterial();
				if ( material != null && material.first() ) {
					writer.writeAttribute(SQLAssayPlate.MATERIAL_ID_COLUMN, material.getRemoteID());
				}
				writeAttributes(writer, data, ASSAY_DATA_ATTRS);
				
				writer.writeEndElement();
			}
			
			writer.writeEndElement();
		}
	}

	private static void writeAttributes(XMLStreamWriter writer, BasicObject object, String[] attrs) throws XMLStreamException, DataException {
		for ( String attribute : attrs) {
			String value = object.getAttribute(attribute);
			if ( value != null )
				writer.writeAttribute(attribute, value);
		}
	}
	
	public String getHostID() {
		return this.hostID;
	}
	
	public int getStrainCount() { 
		return this.strainCount;
	}
	
	public int getMaterialCount() {
		return this.materialCount;
	}
	
	public int getAssayCount() {
		return this.assayCount;
	}
	
	public int getCollectionCount() {
		return this.collectionCount;
	}
	
	public int getHarvestCount() { 
		return this.harvestCount;
	}

}
