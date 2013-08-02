/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.util.Iterator;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

/**
 * @author George Chlipala
 *
 */
public class InchiGenerator {

	
	private static final String NAME_INCHI = "inchi";
	private static final String NAME_STEREO = "stereo";
	private static final String NAME_VERSION = "version";
	private static final String NAME_FLAGS = "flags";
	private static final String NAME_FORMAT = "format";
	private static final String NAME_STRING = "str";
	private static final String NAME_FIXED_H = "fixedH";
	
	private static final String CHEMSPIDER_URL = "http://www.chemspider.com/InChi.asmx";
	private static final String CHEMSPIDER_XMLNS = "http://www.chemspider.com/";
	
	private static final String GENERATE_INCHI_CMD = "GenerateInChI";
	private static final String RESPONSE_NAME = "GenerateInChIResponse";

	private static final String GENERATE_INCHI_KEY_CMD = "GenerateInChIKey";
	private static final String RESPONSE_KEY_NAME = "GenerateInChIKeyResponse";
	
	private static final String GENERATE_KEY_FROM_INCHI_CMD = "InChIToInChIKey";
	private static final String RESPONSE_INCHI_KEY_NAME = "InChIToInChIKeyResponse";	

	public static final String STEREO_ABSOLUTE = "Absolute";
	public static final String STEREO_RELATIVE = "Relative";
	public static final String STEREO_RACEMIC = "Racemic";
	public static final String STEREO_NO = "No";

	public static final String VERSION_103 = "v103";
	public static final String VERSION_104 = "v104";
	public static final String VERSION_102b = "v102b";
	public static final String VERSION_102s = "v102s";
	
	public static final String FORMAT_MOL = "MOL";
	public static final String FORMAT_SMILES = "SMILES";
	public static final String FORMAT_SDF = "SDF";
	public static final String FORMAT_InChI = "InChI";
	
	private String format = FORMAT_SMILES;
	private String input = "";
	private String version = VERSION_103;
	private String stereo = STEREO_ABSOLUTE;
	private boolean fixedH = true;
	private SOAPFactory soapFactory;
	
	/**
	 * 
	 */
	public InchiGenerator() {

	}
	
	public InchiGenerator(String format, String data) {
		this.format = format;
		this.input = data;
	}

	public void setMOLData(String molData) {
		format = FORMAT_MOL;
		input = molData;
	}
	
	public void setSMILES(String smilesString) {
		format = FORMAT_SMILES;
		input = smilesString;
	}
	
	public void setStereo(String stereo) {
		this.stereo = stereo;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public void setFixedH() {
		this.fixedH = true;
	}
	
	public void unsetFixedH() {
		this.fixedH = false;
	}
	
	public void setFixedH(boolean fixedH) {
		this.fixedH = fixedH;
	}
	
	public String getString() throws UnsupportedOperationException, SOAPException {
		return this.getStringForCMD(GENERATE_INCHI_CMD, RESPONSE_NAME);
	}

	public String getKey() throws UnsupportedOperationException, SOAPException {
		if ( this.format.equals(FORMAT_InChI)) {
			SOAPMessage request = this.startRequest(GENERATE_KEY_FROM_INCHI_CMD);
			SOAPBody msgBody = request.getSOAPBody();
			Name bodyName = createName(GENERATE_KEY_FROM_INCHI_CMD, "", CHEMSPIDER_XMLNS);
			SOAPBodyElement bodyEl = msgBody.addBodyElement(bodyName);
			addNode(createName(NAME_INCHI), bodyEl, this.input);
			return this.getSOAPString(request, RESPONSE_INCHI_KEY_NAME);
		} else {
			return this.getStringForCMD(GENERATE_INCHI_KEY_CMD, RESPONSE_KEY_NAME);
		}
	}
	
	private SOAPMessage startRequest(String command) throws SOAPException {
		MessageFactory msgFactory = MessageFactory.newInstance();
		SOAPMessage request = msgFactory.createMessage();
		
		SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
		
		envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
		envelope.addNamespaceDeclaration("soap12", "http://www.w3.org/2003/05/soap-envelope");
		
		MimeHeaders mimeHeads = request.getMimeHeaders();
		mimeHeads.addHeader("SOAPAction", String.format("http://www.chemspider.com/%s", command));
		
		return request;
	}
	
	private String getStringForCMD(String command, String responseString) throws UnsupportedOperationException, SOAPException {
		SOAPMessage request = this.startRequest(command);	
		SOAPBody msgBody = request.getSOAPBody();
		Name bodyName = createName(command, "", CHEMSPIDER_XMLNS);
		
		SOAPBodyElement bodyEl = msgBody.addBodyElement(bodyName);

		addNode(createName(NAME_STRING), bodyEl, this.input);
		addNode(createName(NAME_FORMAT), bodyEl, this.format);
				
		Name flagsName = soapFactory.createName(NAME_FLAGS);
		SOAPElement flagsEl = bodyEl.addChildElement(flagsName);
		
		addNode(createName(NAME_VERSION), flagsEl, this.version);
		addNode(createName(NAME_STEREO), flagsEl, this.stereo);
		addNode(createName(NAME_FIXED_H), flagsEl, (this.fixedH ? "true" : "false"));

		return this.getSOAPString(request, responseString);
	}
	
	private String getSOAPString(SOAPMessage request, String responseString) throws UnsupportedOperationException, SOAPException {
		SOAPConnectionFactory soapConnFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection soapConn = soapConnFactory.createConnection();

		SOAPMessage response = soapConn.call(request, CHEMSPIDER_URL);
		
		SOAPBody respBody = response.getSOAPBody();
		
		Iterator<?> anIter = respBody.getChildElements();
		
		String result = null;
		
		while ( anIter.hasNext() ) {
			SOAPBodyElement anElem = (SOAPBodyElement) anIter.next();
//			System.out.format("ELEMENT: %s\n", anElem.getNodeName());
//			System.out.println(anElem.getTextContent());
			if ( anElem.getNodeName().equals(responseString) ) {
				result = anElem.getTextContent();
			} else if ( anElem.hasChildNodes() ) {
				Iterator<?> elIter = anElem.getChildElements(createName(responseString));
				if ( elIter.hasNext() ) {
					SOAPBodyElement resBodyEl = (SOAPBodyElement) elIter.next();
					result = resBodyEl.getTextContent();
				}
			}
		}
		

		/*
		Iterator<?> anIter = respBody.getChildElements(soapFactory.createName(responseString));
		
		SOAPBodyElement resBodyEl = (SOAPBodyElement) anIter.next();
		
		String result = resBodyEl.getTextContent();
		 */
		
		return result;		
	}
	
	private static SOAPElement addNode(Name nodeName, SOAPElement parent, String value) throws SOAPException {
		SOAPElement anElem = parent.addChildElement(nodeName);
		anElem.addTextNode(value);
		return anElem;
	}
 	
	public static String convertSMILES(String smilesString) throws UnsupportedOperationException, SOAPException {
		InchiGenerator iGen = new InchiGenerator(FORMAT_SMILES, smilesString);
		return iGen.getString();
	}
	
	public static String convertMOL(String molData) throws UnsupportedOperationException, SOAPException {
		InchiGenerator iGen = new InchiGenerator(FORMAT_MOL, molData);
		return iGen.getString();
	}
	
	public static String getSMILESKey(String smilesString) throws UnsupportedOperationException, SOAPException {
		InchiGenerator iGen = new InchiGenerator(FORMAT_SMILES, smilesString);
		return iGen.getKey();
	}
	
	public static String getMOLKey(String molData) throws UnsupportedOperationException, SOAPException {
		InchiGenerator iGen = new InchiGenerator(FORMAT_MOL, molData);
		return iGen.getKey();
	}	
	
	public static String getInChiKey(String inchiString) throws UnsupportedOperationException, SOAPException {
		InchiGenerator iGen = new InchiGenerator(FORMAT_InChI, inchiString);
		return iGen.getKey();
	}
	
	private SOAPFactory getSOAPFactory() throws SOAPException {
		if ( this.soapFactory == null ) {
			this.soapFactory = SOAPFactory.newInstance();
		}
		return this.soapFactory;
	}
	
	private Name createName(String localName) throws SOAPException {
		return this.getSOAPFactory().createName(localName);
	}
	
	private Name createName(String localName, String prefix, String uri) throws SOAPException {
		return this.getSOAPFactory().createName(localName, prefix, uri);
	}
}
