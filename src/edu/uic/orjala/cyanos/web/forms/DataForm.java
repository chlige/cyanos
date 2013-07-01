/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.commons.fileupload.FileItem;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.DataFileObject;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.SingleFile;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLExternalFile;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosConfig;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.HtmlList;
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
public class DataForm extends BaseForm {

	/**
	 * 
	 */
	protected Map<String,String> dataTypeMap;
	public static final String DATAFILE = "datafile";

	
	public DataForm(CyanosWrapper aWrapper) {
		super(aWrapper);
		this.dataTypeMap = new HashMap<String,String>();
	}

	public void resetTypeLabels() {
		this.dataTypeMap.clear();
		this.dataTypeMap = new HashMap<String,String>();
	}
	
	public void setTypeLabel(String aType, String aLabel) {
		this.dataTypeMap.put(aType, aLabel);
	}
	
	public void removeTypeLabel(String aType) {
		this.dataTypeMap.remove(aType);
	}
	
	public Div datafileDiv(DataFileObject anObject) {
		return this.collapsableDiv("datafiles", "Data Files", this.datafileContent(anObject));
	}

	public String datafileTable(DataFileObject anObject) {
		return this.datafileContent(anObject);
	}
	
	private String datafileContent(DataFileObject anObject) {
		TableRow myRow = new TableRow();
		Table myTable = new Table("<COL CLASS='datashort'><COL CLASS='datashort'><COL CLASS='datalong'>");
		myTable.addItem(myRow);
		myTable.setClass("list");
		
		String objClass = anObject.getDataFileClass();
		
		try {
			String[] headers = {"File", "Data Type", "Description"};
			TableCell myCell = new TableHeader(headers);
			myCell.setClass("header");
			myRow.addItem(myCell);
			ExternalFile myFiles = anObject.getDataFiles();
			if ( myFiles.first() ) {		
				myFiles.beforeFirst();
				boolean odd = true;
				while ( myFiles.next() ) {
					Image myIcon = this.getIconForMIMEType( myFiles.getMimeType() );
					myIcon.setAttribute("BORDER", "0");
					File aFile = myFiles.getFileObject();
					String fileName = String.format("<A HREF=\"file/get/%s/%s/%s\">%s %s</A>", objClass, anObject.getID(), myFiles.getFilePath(), 
							myIcon.toString(), aFile.getName());
					myCell = new TableCell(fileName);
					String dataType = myFiles.getDataType();
					if ( this.dataTypeMap.containsKey(dataType)) 
						myCell.addItem(this.dataTypeMap.get(dataType));
					else 
						myCell.addItem(dataType);
					myCell.addItem( myFiles.getDescription() );
					myRow = new TableRow(myCell);
					if ( odd ) {
						myRow.setClass("odd");
						odd = false;
					} else {
						myRow.setClass("even");
						odd = true;
					}
					myTable.addItem(myRow);

				}
			} else {
				myRow.addItem("<TD ALIGN='CENTER' COLSPAN=3><B>None</B></TD>");
			}
		} catch ( DataException e ) {
			myRow.addItem(String.format("<TD COLSPAN='3' ALIGN='CENTER'><B><FONT COLOR='red'>SQL ERROR:</FONT> %s</B></TD>", e.getMessage()));
		}
		myTable.setClass("dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");
//		if ( anObject.isAllowed(Role.WRITE) )
//			myTable.addItem(String.format("<TR><TD COLSPAN='3' ALIGN='CENTER'><INPUT TYPE='BUTTON' NAME='datafileForm' VALUE='Add Data Files' onClick=\"dataForm('%s','%s')\"/></TD></TR>", objClass, anObject.getID()));
		return myTable.toString();
	}

	private String linkFile() {
		StringBuffer output = new StringBuffer();

		CyanosConfig myConf = this.myWrapper.getAppConfig();
		String path = myConf.getFilePath(this.getFormValue("class"), this.getFormValue("type"));
		String preFileName = this.getFormValue("aFile");
		String fileName = preFileName.replaceFirst("^/+", "");

		try {
			ExternalFile aFile = SQLExternalFile.load(this.getSQLDataSource(), path, fileName);
			if ( ! aFile.first() ) {
				aFile = new SingleFile(path, fileName);
				try {
					MagicMatch aMatch = Magic.getMagicMatch(aFile.getFileObject(), true);
					aFile.setMimeType(aMatch.getMimeType());
				} catch (MagicParseException e) {
					return this.handleException(e);
				} catch (MagicMatchNotFoundException e) {
					return this.handleException(e);
				} catch (MagicException e) {
					return this.handleException(e);
				}
				aFile.setDescription(this.getFormValue("notes"));
			}
			output.append(this.linkFile(aFile));
		} catch (DataException e) {
			return this.handleException(e);
		}
		output.append("<P ALIGN='CENTER'><INPUT TYPE='BUTTON' NAME='closeMe' VALUE='Close Window' onClick='closeSmallWindow()'/></P>");

		return output.toString();
	}
	
	public String finishUpload() {
		if ( this.hasFormValue("linkExisting") )
			return this.linkFile();
		else
			return this.linkNewFile();
	}
	
	
	private String linkNewFile() {
		StringBuffer output = new StringBuffer();
	
		CyanosConfig myConf = this.myWrapper.getAppConfig();
		String path = myConf.getFilePath(this.getFormValue("class"), this.getFormValue("type"));
		HttpSession aSession = this.myWrapper.getSession();
		FileItem fileItem = (FileItem)aSession.getAttribute(DataForm.DATAFILE);
		String preFileName = this.getFormValue("path") + "/" + fileItem.getName();
		String fileName = preFileName.replaceFirst("^/+", "");
		
		String mimeType = fileItem.getContentType();

		SingleFile aFile = new SingleFile(path, fileName);
		
		try {
			File outputFile = aFile.getFileObject();
			
			if ( outputFile.exists() ) {
				Form myForm = new Form();
				myForm.setAttribute("METHOD", "POST");
				myForm.setAttribute("ACTION", this.myWrapper.getRequestURI());

				myForm.setName("datafile");
				myForm.addItem("<INPUT TYPE=HIDDEN NAME='opener' VALUE='" + this.getFormValue("opener") + "'/>");
				myForm.addItem("<INPUT TYPE=HIDDEN NAME='id' VALUE='" + this.getFormValue("id") + "'/>");
				myForm.addItem("<INPUT TYPE=HIDDEN NAME='class' VALUE='" + this.getFormValue("class") + "'/>");
				myForm.addItem("<INPUT TYPE=HIDDEN NAME='type' VALUE='" + this.getFormValue("type") + "'/>");
				myForm.addItem("<INPUT TYPE=HIDDEN NAME='notes' VALUE=\"" + this.getFormValue("notes") + "\"/>");
				myForm.addItem("<INPUT TYPE=HIDDEN NAME='path' VALUE=\"" + this.getFormValue("notes") + "\"/>");
				myForm.addItem("<P ALIGN='CENTER'><B><FONT COLOR='RED'>Warning!</FONT><BR>File already exists!</P>");
				myForm.addItem("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='forceUpload' VALUE='Overwrite File'/><INPUT TYPE='SUBMIT' NAME='cancel' VALUE='Cancel Upload'/></P>");
				output.append(myForm.toString());
				return output.toString();
			} else if (outputFile.createNewFile() ) {

				InputStream fileData = fileItem.getInputStream();
				FileOutputStream fileOut = new FileOutputStream(outputFile);
				int d = fileData.read();
				
				while ( d != -1 ) {
					fileOut.write(d);
					d = fileData.read();
				}
				fileOut.close();
				fileData.close();
				output.append("<P ALIGN='CENTER'><B><FONT COLOR='GREEN'>Success: </FONT>File saved.</B></P>");
				aFile.setMimeType(mimeType);
				aFile.setDescription(this.getFormValue("notes"));

				TableCell myCell = new TableCell();
				myCell.setAttribute("COLSPAN", "2");
				try {
					output.append(this.linkFile(aFile));
					aSession.removeAttribute(DataForm.DATAFILE);
				} catch (DataException e) {
					output.append("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
					e.printStackTrace();
				}
				output.append("<P ALIGN='CENTER'><INPUT TYPE='BUTTON' NAME='closeMe' VALUE='Close Window' onClick='closeSmallWindow()'/></P>");

			} else {
				output.append("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ERROR!</FONT><BR>Cannot create file.</B></P>");
			}
		} catch ( IOException e ) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
			e.printStackTrace();
		} catch ( DataException e ) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
			e.printStackTrace();
		}
		return output.toString();
	}
	
	private String linkFile(ExternalFile aFile) throws DataException {
		StringBuffer output = new StringBuffer();
		if ( this.getFormValue("class").equals("sample") ) {
			Sample aSample = new SQLSample(this.getSQLDataSource(), this.getFormValue("id"));
			aSample.linkDataFile(aFile, this.getFormValue("type"));
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='GREEN'>Success: </FONT>File linked.</B></P>");
			output.append(this.sampleLink(aSample));
		} else if (this.getFormValue("class").equals("strain") ) {
			Strain aStrain = new SQLStrain(this.getSQLDataSource(), this.getFormValue("id"));
			aStrain.linkDataFile(aFile, this.getFormValue("type"));
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='GREEN'>Success: </FONT>File linked.</B></P>");
			output.append(this.strainLink(aStrain));
		} else if (this.getFormValue("class").equals("separation") ) {
			Separation aSep = new SQLSeparation(this.getSQLDataSource(), this.getFormValue("id"));
			aSep.linkDataFile(aFile, this.getFormValue("type"));
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='GREEN'>Success: </FONT>File linked.</B></P>");
			output.append(String.format("<A HREF='separation?id=%s'>Separation #%s</A>", aSep.getID(), aSep.getID()));
		} else if (this.getFormValue("class").equals("compound") ) {
			Compound aCompound = new SQLCompound(this.getSQLDataSource(), this.getFormValue("id"));
			aCompound.linkDataFile(aFile, this.getFormValue("type"));
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='GREEN'>Success: </FONT>File linked.</B></P>");
			output.append(String.format("<A HREF='compound?id=%s'>%s</A>", aCompound.getID(), aCompound.getName()));						
		} else if ( this.getFormValue("class").equals("assay") ) {
			Assay anObject = new SQLAssay(this.getSQLDataSource(), this.getFormValue("id"));
			anObject.linkDataFile(aFile, this.getFormValue("type"));
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='GREEN'>Success: </FONT>File linked.</B></P>");
			output.append(String.format("<A HREF='assay?id=%s'>%s</A>", anObject.getID(), anObject.getName()));						
		} else {
			return ("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ERROR:</FONT> OBJECT CLASS NOT SPECIFIED</B></P>");
		}
		
		TableCell myCell = new TableCell("File Name:");
		TableRow aRow = new TableRow(myCell);
		myCell.addItem(aFile.getFilePath());
		aRow.addItem(myCell);
		
		myCell = new TableCell("Data Type:");
		myCell.addItem(this.getFormValue("type"));
		aRow.addItem(myCell);
			
		myCell = new TableCell("File Type:");
		myCell.addItem(aFile.getMimeType());
		aRow.addItem(myCell);
			
		myCell = new TableCell("Description:");
		myCell.addItem(aFile.getDescription());
		aRow.addItem(myCell);
			
		Table myTable = new Table(aRow);
		myTable.setClass("species");
		output.append("<P ALIGN='CENTER'>");
		output.append(myTable.toString());
		output.append("</P>");
		return output.toString();
	}
	
	private TableCell typeRow() {
		TableCell myCell = new TableCell("Type:");
		Popup aPop;
		String defaultType = null;
		if ( this.getFormValue("class").equals("sample") ) {
			aPop = DataForm.samplePopup();
			defaultType = Sample.LC_DATA_TYPE;
		} else if (this.getFormValue("class").equals("strain") ) {
			aPop = DataForm.strainTypePopup();
			defaultType = Strain.PHOTO_DATA_TYPE;
		} else if (this.getFormValue("class").equals("separation") ) {
			aPop = DataForm.sepTypePopup();
			defaultType = Separation.LC_DATA_TYPE;
		} else if (this.getFormValue("class").equals("compound") ) {
			aPop = DataForm.compoundPopup();
			defaultType = Compound.NMR_DATA_TYPE;
		} else if (this.getFormValue("class").equals("assay")) {
			aPop = DataForm.assayPopup();
			defaultType = Assay.RAW_DATA_TYPE;
		} else {
			aPop = new Popup();
			aPop.addItemWithLabel("", "OBJECT CLASS NOT SPECIFIED");
		}
		aPop.setName("type");
		if ( this.hasFormValue("type") ) 
			defaultType = this.getFormValue("type");
		aPop.setDefault(defaultType);
		myCell.addItem(aPop.toString());
		return myCell;		
	}
	
	public String fileForm() {
		Form myForm = new Form();
		myForm.setAttribute("METHOD", "POST");
		myForm.setAttribute("ACTION", this.myWrapper.getRequestURI());
		
		myForm.setName("datafile");
		myForm.addItem("<INPUT TYPE=HIDDEN NAME='id' VALUE='" + this.getFormValue("id") + "'/>");
		myForm.addItem("<INPUT TYPE=HIDDEN NAME='class' VALUE='" + this.getFormValue("class") + "'/>");
		myForm.addItem("<INPUT TYPE=HIDDEN NAME='opener' VALUE='" + this.getFormValue("opener") + "'/>");

		TableCell myCell = new TableCell();
		TableRow tableRow = new TableRow(myCell);
		
		HttpSession aSession = this.myWrapper.getSession();
		FileItem anItem = null;
		boolean uploadFile = ( this.hasFormValue("useUpload") ? true : (! this.hasFormValue("linkExisting")) );

		CyanosConfig myConf = this.myWrapper.getAppConfig();
		String rootPath = myConf.getFilePath(this.getFormValue("class"), this.getFormValue("type"));
		
		if ( uploadFile ) {
			myCell.addItem("File:");
			if ( this.hasFormValue("clearFile") ) {
				aSession.removeAttribute(DataForm.DATAFILE);
			} else {
				anItem = (FileItem)aSession.getAttribute(DataForm.DATAFILE);
			}
			if ( anItem == null ) {
				myCell.addItem("<INPUT TYPE='FILE' NAME='datafile' SIZE=25/>");
				myForm.setAttribute("ENCTYPE", "multipart/form-data");
				tableRow.addItem("<TD COLSPAN='2' ALIGN='CENTER'><BUTTON TYPE='SUBMIT' NAME='linkExisting'>Use Existing File</BUTTON></TD>");
			} else {
				myCell.addItem(anItem.getName() + " <BUTTON TYPE='SUBMIT' NAME='clearFile'>Clear File</BUTTON>");
			}
			tableRow.addItem("<TD COLSPAN='2' ALIGN='CENTER'><B>Location</B></TD>");
			if ( this.hasFormValue("mkDir") && this.hasFormValue("path")) {
				String newDir = this.getFormValue("newPath");
				if ( newDir.length() > 0 ) {
					File newDirFile = new File(rootPath + this.getFormValue("path") + "/" + newDir);
					newDirFile.mkdir();
				}
			}

			File outputFile = new File(rootPath);
			String selectedPath = this.getFormValue("path");
			String radioString = "<INPUT TYPE='RADIO' NAME='path' VALUE='/' onClick='this.form.submit()'/>/";
			
			if ( selectedPath == null || selectedPath.equals("/") ) 
				radioString =  "<INPUT TYPE='RADIO' NAME='path' VALUE='/' onClick='this.form.submit()' CHECKED/>/";
			
			myCell = new TableCell(radioString + this.directoryList(rootPath, outputFile, selectedPath));			
			myCell.setAttribute("COLSPAN", "2");
			tableRow.addItem(myCell);
			
			if ( this.hasFormValue("newDir") && this.hasFormValue("path")) {
				myCell = new TableCell("<INPUT TYPE='TEXT' NAME='newPath'/><INPUT TYPE='SUBMIT' NAME='mkDir' VALUE='Make Directory'/><INPUT TYPE='SUBMIT' NAME='noDir' VALUE='Cancel'/>");
			} else {
				myCell = new TableCell("<INPUT TYPE='SUBMIT' NAME='newDir' VALUE='New Subdirectory'/>");
			}	
			myCell.setAttribute("COLSPAN", "2");
			tableRow.addItem(myCell);

		} else {
			myForm.addHiddenValue("linkExisting", "1");
			myCell.addItem("<BUTTON TYPE='SUBMIT' NAME='useUpload'>Upload New File</BUTTON>");
			myCell.setAttribute("COLSPAN", "2");
			myCell.setAttribute("ALIGN", "CENTER");

			File outputFile = new File(rootPath);
			String selectedPath = this.getFormValue("path");
			
			myCell = new TableCell(this.directoryFileList(rootPath, outputFile, selectedPath));			
			myCell.setAttribute("COLSPAN", "2");
			tableRow.addItem(myCell);
		
		}
		
		tableRow.addItem(this.typeRow());
		tableRow.addItem(this.makeFormTextRow("Description:", "notes"));

		if ( uploadFile ) {
			if ( anItem == null ) {
				myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='loadFile' VALUE='Upload Datafile'/><INPUT TYPE='RESET'/><BR/><INPUT TYPE='SUBMIT' NAME='resetForm' VALUE='Return'/>");
			} else {
				myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='addDataAction' VALUE='Add Datafile'/><INPUT TYPE='RESET'/><BR/><INPUT TYPE='SUBMIT' NAME='resetForm' VALUE='Return'/>");			
			}
		} else {
			myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='addDataAction' VALUE='Link Datafile'/><INPUT TYPE='RESET'/><BR/><INPUT TYPE='SUBMIT' NAME='resetForm' VALUE='Return'/>");						
		}
		myCell.setAttribute("colspan","2");
		myCell.setAttribute("align","center");
		tableRow.addItem(myCell);

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		myForm.addItem(myTable.toString());
		
		return myForm.toString();
	}

	private String directoryFileList(String prefix, File somePath, String selectedPath) {
		File[] fileList = DataForm.filesForPath(somePath);
		HtmlList aList = new HtmlList();
		aList.unordered();
		int cut = prefix.length();
		for ( int i = 0; i < fileList.length; i++ ) {
			if ( fileList[i].isDirectory() ) {
				String relPath = fileList[i].getPath().substring(cut);
				if ( selectedPath != null && selectedPath.startsWith(relPath) ) {
					String radioString = String.format("<INPUT TYPE='radio' NAME='path' VALUE='%s' onClick='this.form.submit()'/><B>%s</B>", relPath, fileList[i].getName());
					if ( relPath.equals(selectedPath) )
						radioString = String.format("<INPUT TYPE='radio' NAME='path' VALUE='%s' onClick='this.form.submit()' CHECKED/><B>%s</B>", relPath, fileList[i].getName());
					aList.addItem(radioString + this.directoryFileList(prefix, fileList[i], selectedPath));
				} else
					aList.addItem(String.format("<INPUT TYPE='radio' NAME='path' VALUE='%s' onClick='this.form.submit()'/><B>%s</B>", relPath, fileList[i].getName()));
			} else {
				String relPath = fileList[i].getPath().substring(cut);
				aList.addItem(String.format("<INPUT TYPE='radio' NAME='aFile' VALUE='%s'/>%s", relPath, fileList[i].getName()));				
			}
		}
		return aList.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static File[] filesForPath(File somePath) {
		File[] fileList = somePath.listFiles();
		Arrays.sort(fileList, DataForm.fileCompare());
		return fileList;
	}
	
	public static Comparator fileCompare() {
		return new Comparator()
		    {
		      public int compare(final Object o1, final Object o2) {
		        return ((File) o1).getName().compareToIgnoreCase(((File)o2).getName());
		      }
		    };
	}
	
	public static Comparator directoryFirstCompare() {
		return new Comparator()
		    {
		      public int compare(final Object o1, final Object o2) {
		    	  boolean isDir1 = ((File)o1).isDirectory();
		    	  boolean isDir2 = ((File)o2).isDirectory();
		    	  if ( isDir1 == isDir2 ) {
		    		  return ((File) o1).getName().compareToIgnoreCase(((File)o2).getName());
		    	  } else if ( isDir1 ) {
		    		  return -1;
		    	  } else {
		    		  return 1;
		    	  }
		      }
		    };
	}

	
	private String directoryList(String prefix, File somePath, String selectedPath) {
		File[] fileList = DataForm.filesForPath(somePath);
		HtmlList aList = new HtmlList();
		aList.unordered();
		int cut = prefix.length();
		for ( int i = 0; i < fileList.length; i++ ) {
			if ( fileList[i].isDirectory() ) {
				String relPath = fileList[i].getPath().substring(cut);
				if ( selectedPath != null && selectedPath.startsWith(relPath) ) {
					String radioString = String.format("<INPUT TYPE='radio' NAME='path' VALUE='%s' onClick='this.form.submit()'/><B>%s</B>", relPath, fileList[i].getName());
					if ( relPath.equals(selectedPath) )
						radioString = String.format("<INPUT TYPE='radio' NAME='path' VALUE='%s' onClick='this.form.submit()' CHECKED/><B>%s</B>", relPath, fileList[i].getName());
					aList.addItem(radioString + this.directoryList(prefix, fileList[i], selectedPath));
				} else
					aList.addItem(String.format("<INPUT TYPE='radio' NAME='path' VALUE='%s' onClick='this.form.submit()'/><B>%s</B>", relPath, fileList[i].getName()));
			}
		}
		return aList.toString();
	}

	public static Popup samplePopup() {
		Popup aPop = new Popup();
		aPop.addItemWithLabel(Sample.LC_DATA_TYPE, "LC Chromatogram");		
		aPop.addItemWithLabel(Sample.NMR_DATA_TYPE, "NMR Spectrum");		
		aPop.addItemWithLabel(Sample.MS_DATA_TYPE, "Mass Spectrum");
		return aPop;
	}
	
	public static Popup strainTypePopup() {
		Popup aPop = new Popup();
		aPop.addItemWithLabel(Strain.PHOTO_DATA_TYPE, "Picture");		
		return aPop;
	}
	
	public static Popup sepTypePopup() {
		Popup aPop = new Popup();
		aPop.addItemWithLabel(Separation.LC_DATA_TYPE, "LC Chromatogram");		
		return aPop;
	}
	
	public static Popup compoundPopup() {
		Popup aPop = new Popup();	
		aPop.addItemWithLabel(Compound.NMR_DATA_TYPE, "NMR Spectrum");		
		aPop.addItemWithLabel(Compound.MS_DATA_TYPE, "Mass Spectrum");
		aPop.addItemWithLabel(Compound.IR_DATA_TYPE, "IR Spectrum");
		aPop.addItemWithLabel(Compound.UV_DATA_TYPE, "UV Spectrum");
		return aPop;
	}

	public static Popup assayPopup() {
		Popup aPop = new Popup();	
		aPop.addItemWithLabel(Assay.RAW_DATA_TYPE, "Raw Data");		
		aPop.addItemWithLabel(Assay.REPORT_DATA_TYPE, "Report");
		return aPop;
	}
	
	public static String fileManagerApplet(CyanosWrapper aWrap, String objectClass, String objectID, String dataType, boolean allowPreview) {
		String contextPath = aWrap.getContextPath();
		StringBuffer progressApplet = new StringBuffer("<DIV ALIGN='CENTER'>\n<OBJECT classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\" ALIGN='CENTER' ");
		progressApplet.append("CODEBASE=\"http://java.sun.com/products/plugin/autodl/jinstall-1_5_0-windows-i586.cab#Version=1,5,0,0\" HEIGHT=300 WIDTH='80%' >\n");
		progressApplet.append("<PARAM NAME=\"CODE\" VALUE=\"edu.uic.orjala.cyanos.applet.FileManager\">\n");
		progressApplet.append(String.format("<PARAM NAME=\"ARCHIVE\" VALUE='%s/applets/filemanager.jar'>\n", contextPath));
		progressApplet.append(String.format("<PARAM NAME=\"urlPath\" VALUE=\"%s/file/manager/%s/%s/\">\n", contextPath, objectClass, dataType));
		progressApplet.append(String.format("<PARAM NAME=\"objclass\" VALUE=\"%s\">\n", objectClass));
		progressApplet.append(String.format("<PARAM NAME=\"objID\" VALUE=\"%s\">\n", objectID));
		if ( dataType != null )
		progressApplet.append(String.format("<PARAM NAME=\"dataType\" VALUE=\"%s\">\n", dataType));
		if ( allowPreview )
			progressApplet.append("<PARAM NAME=\"preview\" VALUE=\"true\">\n");
		
		
		progressApplet.append("<COMMENT>\n<EMBED TYPE='application/x-java-applet;version=1.5' CODE='edu.uic.orjala.cyanos.applet.FileManager' ALIGN='CENTER' HEIGHT=300 WIDTH='80%'");
		progressApplet.append(String.format("ARCHIVE='%s/applets/filemanager.jar' ", contextPath));
		progressApplet.append(String.format("urlPath='%s/file/manager/%s/%s/' ", contextPath, objectClass, dataType));
		if ( dataType != null )
			progressApplet.append(String.format(" dataType=\"%s\" ", dataType));
		if ( allowPreview )
			progressApplet.append(" preview=\"true\" ");

		progressApplet.append(String.format("objclass='%s' objID='%s' >", objectClass, objectID));

		progressApplet.append("<NOEMBED>Java not available</NOEMBED>\n</EMBED>\n</COMMENT>\n</OBJECT>\n</DIV>");
		return progressApplet.toString();
	}

	public static String humanReadableSize(Long aSize) {
		double exponent = Math.log10(aSize.doubleValue());
		if ( exponent >= 9.0 ) {
			return String.format("%.1f GB", (aSize.doubleValue() / Math.pow(10, 9)));
		} else if ( exponent >= 6.0 ) {
			return String.format("%.1f MB", (aSize.doubleValue() / Math.pow(10, 6)));			
		} else if ( exponent >= 3.0 ) {
			return String.format("%.1f kB", (aSize.doubleValue() / Math.pow(10, 3)));			
		} else {
			return String.format("%d B", aSize.longValue());						
		}
	}
	
}
