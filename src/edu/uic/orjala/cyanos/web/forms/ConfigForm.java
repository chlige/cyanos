/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.HtmlList;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.StyledText;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class ConfigForm extends BaseForm {

	/**
	 * @author George Chlipala
	 *
	 */
	public class CustomJarFilter implements FileFilter {

		long lastModified;
		
		/**
		 * 
		 */
		public CustomJarFilter(File parentFile) {
			lastModified = parentFile.lastModified();
		}

		/* (non-Javadoc)
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File arg0) {
			System.out.format("PARENT: %d %s: %d\n", lastModified, arg0.getName(), arg0.lastModified());
			return arg0.lastModified() > lastModified;
		}

	}

	public static final String DOWNLOAD_CONFIG_ACTION = "downloadConfig";
	public static final String REVERT_CONFIG_ACTION = "revertConfig";
	public static final String APP_CONFIG_ATTR = "app-config";
	private final AppConfig myConfig;
	private File jarDir;
	/**
	 * 
	 */
	public ConfigForm(CyanosWrapper callingServlet) {
		super(callingServlet);
		myConfig = (AppConfig) callingServlet.getSession().getAttribute(APP_CONFIG_ATTR);
	}

	public void setJarDir(File aFile) {
		jarDir = aFile;
	}
	
	private String basicForm() {
		StringBuffer output = new StringBuffer();
		output.append("<P ALIGN='CENTER'><FONT SIZE='+1'><B>Configuration</B></FONT></P>");

		if ( this.myWrapper.hasUpload("xmlFile") ) {
/*
			FileUpload file = this.myWrapper.getUpload("xmlFile");
			try {
				myConfig.loadXML(file.getStream());
			} catch (ConfigException e) {
				output.append(this.handleException(e));
			} catch (IOException e) {
				output.append(this.handleException(e));
			}
			this.myWrapper.getSession().setAttribute(APP_CONFIG_ATTR, myConfig);
*/
		} 	
		
		Form myForm = new Form();
		myForm.setAttribute("METHOD", "POST");		
		myForm.addItem("<P ALIGN='CENTER'><BUTTON TYPE='SUBMIT' NAME='downloadConfig'>Download configuration</BUTTON></P>");
		myForm.addItem("<P ALIGN='CENTER'><BUTTON TYPE='SUBMIT' NAME='revertConfig'>Revert to saved configuration</BUTTON></P>");
		output.append(myForm.toString());

		myForm = new Form();
		myForm.setAttribute("METHOD", "POST");
		myForm.setAttribute("ACTION", this.myWrapper.getRequestURI());
		myForm.setAttribute("ENCTYPE", "multipart/form-data");
		if ( this.hasFormValue("form") )
			myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='form' VALUE='%s'/>", this.getFormValue("form")));	

		myForm.addItem("<P ALIGN='CENTER'><BR/><B>File to upload (XML configuration):</B><INPUT TYPE='FILE' NAME='xmlFile' SIZE=25/><BUTTON TYPE='SUBMIT'>Upload XML File</BUTTON></P>");
		output.append(myForm.toString());
		
		
		return output.toString();
	}

	/*
	public String datasourceForm() {
		Form myForm = new Form("<P ALIGN='CENTER'><FONT SIZE='+1'><B>Datasources</B></FONT></P>");
		myForm.setAttribute("METHOD", "POST");
		if ( this.hasFormValue("form") )
			myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='form' VALUE='%s'/>", this.getFormValue("form")));

		if ( this.hasFormValue("updateAction") ) {
			myConfig.setDataSource(this.getFormValue("dataJdbc"));
			myConfig.setUserDB(this.getFormValue("userJdbc"));
		}
		
		TableCell myCell = new TableCell("Application data:");
		Popup jdbcPop = new Popup();
		jdbcPop.setName("dataJdbc");
		try {
			Context initCtx = new InitialContext();
			NamingEnumeration<NameClassPair> jdbcList = initCtx.list("java:comp/env/jdbc");
			while ( jdbcList.hasMoreElements() ) {
				NameClassPair aName = jdbcList.nextElement();
				jdbcPop.addItem(aName.getName());
			}
		} catch (NamingException ex) {
			myForm.addItem("<P><B><FONT COLOR='red'>ERROR:</FONT> " + ex.getMessage() + "</B></P>");
			ex.printStackTrace();
		}
		
		jdbcPop.setDefault(myConfig.getDataSource());
		myCell.addItem(jdbcPop.toString());
		myCell.addItem("<INPUT TYPE='SUBMIT' NAME='checkDataJDBC' VALUE='Check JDBC'/>");
		TableRow aRow = new TableRow(myCell);
		
		if ( this.hasFormValue("checkDataJDBC") ) {
			aRow.addItem("<TD COLSPAN=4>" + this.testDB(this.getFormValue("dataJdbc")) + "</TD>");
		} 
	
		myCell = new TableCell("User accounts:");
//		jdbcPop.setDefault(myConf.getUserDB());
		jdbcPop.setName("userJdbc");
		myCell.addItem(jdbcPop.toString());
		myCell.addItem("<INPUT TYPE='SUBMIT' NAME='checkUserJDBC' VALUE='Check JDBC'/>");
		aRow.addItem(myCell);

		if ( this.hasFormValue("checkUserJDBC") ) {
			myForm.addItem("<TD COLSPAN=4>" + this.testDB(this.getFormValue("userJdbc")) + "</TD>");
		} 
		
		Table myTable = new Table(aRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		myForm.addItem(myTable);

		myForm.addItem("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='updateAction' VALUE='Update'/><INPUT TYPE='RESET'/></P>");
		return myForm.toString();

	}
	*/
	
	public String urlTemplateForm() {
		Form myForm = new Form("<P ALIGN='CENTER'><FONT SIZE='+1'><B>URL Templates</B></FONT></P>");

		myForm.addItem("<P ALIGN='CENTER'>Allowed field codes<BR><UL><LI>!FORMULA! - Molecular Formula</LI><LI>!SMILES! - SMILES String</LI>" +
				"<LI>!AVG_MASS! - Average Mass</LI><LI>!MONO_MASS! - Monoisotopic Mass</LI><LI>!CAS_ID! - CAS ID</LI></UL>" +
				"<BR> Example: <CODE>http://chem.state.edu/display?cas_id=!CAS_ID!</CODE> - Hypothetical display of a substance (index by CAS ID).<BR/>" +
				"<CODE>http://chem.state.edu/search?mass=!AVG_MASS!&diff=0.5</CODE> - Hyptothetical search based on mass.</P>");

		if ( this.hasFormValue("updateAction") ) {
			
		}
		
		Popup classPop = new Popup();
		classPop.addItemWithLabel("compound", "Compound");
		
		TableRow aRow = new TableRow("<TH>Object Class</TH><TH>Label</TH><TH>Template</TH>");
		Map<String,String> templateMap = myConfig.getURLTemplates("compound");
		if ( templateMap != null ) {
			Iterator<String> templateIter = templateMap.keySet().iterator();

			while ( templateIter.hasNext() ) {
				String aLabel = templateIter.next();
				classPop.setName(String.format("template.class.compound.%s",aLabel));
				TableCell myCell = new TableCell(classPop.toString());
				myCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='template.label.compound.%s' VALUE=\"%s\"/>", aLabel, aLabel));
				myCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='template.content.compound.%s' VALUE=\"%s\"/>", aLabel, templateMap.get(aLabel)));
				aRow.addItem(myCell);
			}
		}
		classPop.setName("new.class");
		aRow.addItem("<TD COLSPAN=3 ALIGN='CENTER'><B>New Template</B></TD>");
		aRow.addItem("<TD>" + classPop.toString() + "</TD><TD><INPUT TYPE='TEXT' NAME='new.label'/></TD><TD><INPUT TYPE='TEXT' NAME='new.content'/><TD>");

		
		Table myTable = new Table(aRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		myForm.addItem(myTable);

		myForm.addItem("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='updateAction' VALUE='Update'/><INPUT TYPE='RESET'/></P>");
		myForm.setAttribute("METHOD", "POST");
		return myForm.toString();
	}

	public String moduleForm() {
		Form myForm = new Form("<P ALIGN='CENTER'><FONT SIZE='+1'><B>Custom Modules</B></FONT></P>");

		myForm.addItem("<P ALIGN='CENTER'>Classes specified should be fully qualified, e.g. <CODE>java.lang.String</CODE> not <CODE>String</CODE>, and implement the appropriate interface.<BR/>In addition, the compiled class files should be located in the CLASSPATH of the Tomcat Application Server.</P>");

		if ( this.hasFormValue("updateAction") ) {
			if ( this.hasFormValue("new:class") ) {
				String[] classes = this.getFormValues("new:class");
				for ( int i = 0; i < classes.length; i++ ) {					
					String fieldName = String.format("class:%s", classes[i]);
					String classType = this.getFormValue(fieldName);
					if ( classType.length() > 0 ) {
						myConfig.addClassForModuleType(classType, classes[i]);
						myConfig.addClassForJar(classes[i], this.getFormValue("lib-jar"));
					}
				}
			} 
			
			if ( this.hasFormValue("del:class")  ) {
				String[] classes = this.getFormValues("del:class");
				for ( int i = 0; i < classes.length; i++ ) {					
					String fieldName = String.format("class:%s", classes[i]);
					if ( this.getFormValue(fieldName).length() > 0 ) {
						myConfig.removeClassForModuleType(this.getFormValue(fieldName), classes[i]);
					}
				}
			}
		}
		
		TableRow aRow = new TableRow("<TH>Module Type</TH><TH>Java Class</TH><TH>Remove</TH>");

		String[] types = { AppConfig.DEREPLICATION_MODULE, AppConfig.UPLOAD_MODULE };
		String[] typeLabels = { "Dereplication", "Upload" };
		
		boolean haveItems = false;
		
		for ( int i = 0; i < types.length; i++ ) {
			List<String> aList = myConfig.classesForModuleType(types[i]);
			if ( aList != null ) {
				ListIterator<String> anIter = aList.listIterator();
				haveItems = aList.size() > 0;
				while ( anIter.hasNext() ) {
					String aClass = anIter.next();
					TableCell myCell = new TableCell(typeLabels[i]);
					myCell.addItem(aClass);
					myCell.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='class:%s' VALUE='%s'><INPUT TYPE='CHECKBOX' NAME='del:class' VALUE=\"%s\">", aClass, types[i], aClass));
					aRow.addItem(myCell);
				}
			}
		}
		if ( haveItems ) {
			Table myTable = new Table(aRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			myForm.addItem(myTable);			
		}
		myForm.addItem("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='updateAction' VALUE='Update'/><INPUT TYPE='RESET'/></P>");
		myForm.setAttribute("METHOD", "POST");

		if ( this.hasFormValue("jar") ) {
			Popup typePop = new Popup();
			typePop.addItem("");
			typePop.addItemWithLabel(AppConfig.DEREPLICATION_MODULE, "Dereplication");
			typePop.addItemWithLabel(AppConfig.UPLOAD_MODULE, "Upload");

			myForm.addItem("<P><H3>New Modules</H3>");
			
			myForm.addItem("Select modules to add to CYANOS.  You only need to select the Java class for the module and not any of the supporting classes that may be present in the JAR file.");
			myForm.addItem("The supporting classes will load automatically with the module class.");
			myForm.addHiddenValue("lib-jar", this.getFormValue("jar"));
			aRow = new TableRow("<TH>Module Type</TH><TH>Java Class</TH>");
			
			try {				
				JarFile jar = new JarFile(new File(jarDir, this.getFormValue("jar")));
				for ( Enumeration<JarEntry> anEnum = jar.entries(); anEnum.hasMoreElements(); ) {
					JarEntry entry = anEnum.nextElement();
					if ( entry.getName().endsWith(".class") ) {
						String name = entry.getName().replaceAll("/", "\\.");
						name = name.substring(0, name.length() - 6);

						StringBuffer cell = new StringBuffer("<TD>");
						typePop.setName(String.format("class:%s", name));
						cell.append(typePop.toString());
						cell.append(String.format("</TD><TD><INPUT TYPE='HIDDEN' NAME='new:class' VALUE='%s'>%s</TD>", name, name));
						aRow.addItem(cell.toString());
					}
				}
			} catch (IOException e) {
				myForm.addItem(this.handleException(e));
			}	
			
			Table myTable = new Table(aRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			myForm.addItem(myTable);			
			myForm.addItem("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='updateAction' VALUE='Update'/><INPUT TYPE='RESET'/></P>");

		}
		
		if ( this.hasFormValue("uploadJar") ) {
		/*
			FileUpload fileItem = this.myWrapper.getUpload("jarDir");
			if ( this.jarDir.exists() && (! this.hasFormValue("overwrite")) ) {
				myForm.setAttribute("ENCTYPE", "multipart/form-data");
				myForm.addItem("File already exists.  Upload again and select overwrite to continue.<BR/><B>JAR file to upload:</B> ");
				myForm.addItem("<INPUT TYPE='FILE' NAME='jarDir' SIZE=25/>");
				myForm.addHiddenValue("form", "modules");
				myForm.addItem("<BUTTON TYPE='SUBMIT' NAME='uploadJar'>Upload </BUTTON><BR>Overwrite existing file: <INPUT TYPE='CHECKBOX' NAME='overwrite'></P>");				
			} else {
				try {
					FileOutputStream jarOut = new FileOutputStream(jarDir);
					int c = -1;
					InputStream fileIn = fileItem.getStream();
					while ( (c = fileIn.read()) > 0 ) {
						jarOut.write(c);
					}
					fileIn.close();
					jarOut.close();
				} catch (IOException e) {
					myForm.addItem(this.handleException(e));
				}
			}
			*/
		}

		File parentFile = new File(this.jarDir, "cyanos.jar");
		myForm.addItem("<P><H3>JAR Files</H3>");
		myForm.addItem("JAR files with custom Java classes should be placed in the folder</P><P style='margin-left: 10px'><CODE>");
		myForm.addItem(this.jarDir.getAbsolutePath());
		myForm.addItem("</CODE></P><P>on the Web Application Server.<UL>");
		File[] files = this.jarDir.listFiles(new CustomJarFilter(parentFile));
		
		for ( int i = 0; i < files.length; i++ ) {
			String name = files[i].getName();
			myForm.addItem(String.format("<LI><A HREF='?form=modules&jar=%s'>%s</A></LI>", name, name));
		}
		
		myForm.addItem("</UL></P>");

		return myForm.toString();
	}

	public String mappingForm() {
		Form myForm = new Form("<P ALIGN='CENTER'><FONT SIZE='+1'><B>Mapping Configuration</B></FONT></P>");

		if ( this.hasFormValue("updateAction") ) {
			if ( this.hasFormValue("delLayer") ) {
				String[] layers = this.getFormValues("delLayer");
				for ( int i = 0; i <  layers.length; i++ ) {
					myConfig.deleteMapServerLayer(layers[i]);
				}
			}
			if ( ! this.getFormValue("mapName").equals("") ) {
				myConfig.addMapServerLayer(this.getFormValue("mapName"), this.getFormValue("mapURL"));
			}
			if ( this.hasFormValue("enableOSM") ) {
				myConfig.setMapParameter(AppConfig.MAP_OSM_LAYER, "1");
			} else {
				myConfig.removeMapParameter(AppConfig.MAP_OSM_LAYER);
			}
			
			if ( this.hasFormValue("enableNASA") ) {
				myConfig.setMapParameter(AppConfig.MAP_NASA_LAYER, "1");
			} else {
				myConfig.removeMapParameter(AppConfig.MAP_NASA_LAYER);
			}
			
			String googleMapKey = this.getFormValue("googleMapKey");
			if ( googleMapKey != null && googleMapKey.length() > 0 ) {
				myConfig.setGoogleMapKey(googleMapKey);
			} else {
				myConfig.removeMapParameter(AppConfig.PARAM_GOOGLE_MAP_KEY);
			}
			this.myWrapper.getSession().setAttribute(APP_CONFIG_ATTR, myConfig);
		}
		
		Map<String,String> layers = myConfig.getMapServerLayers();
		Set<String> keys = new TreeSet<String>(layers.keySet());
		Iterator<String> keyIter = keys.iterator();
		TableHeader header = new TableHeader("Layer Name");
		header.addItem("URL");
		header.addItem("Delete");
		TableRow aRow = new TableRow(header);
		
		boolean enableOSM = ("1".equals(myConfig.getMapParameter(AppConfig.MAP_OSM_LAYER)));
		boolean enableNASA = ("1".equals(myConfig.getMapParameter(AppConfig.MAP_NASA_LAYER)));
		
		while ( keyIter.hasNext() ) {
			String aKey = keyIter.next();
			TableCell aCell = new TableCell(aKey);
			aCell.addItem(layers.get(aKey));
			aCell.addItem(String.format("<INPUT TYPE='CHECKBOX' NAME='delLayer' VALUE='%s'>", aKey));
			aRow.addItem(aCell);
		}
		Table aTable = new Table(aRow);
		aTable.setClass("species");
		aTable.setAttribute("ALIGN","CENTER");
		
		myForm.addItem("<HR WIDTH='70%'><P ALIGN='CENTER'><B>OpenLayers Mapping</B>");
		
		myForm.addItem("<P>CYANOS utilizes the OpenLayers mapping system to display maps (<A HREF='http://openlayers.org'>openlayers.org</A>).  This mapping system allows one to customize the maps that can be displayed, also called layers. By default, CYANOS will enable the OpenStree maps terrain layer.</P>");
		
		try {
			if ( ! myConfig.configExists() ) 
				enableOSM = true;			
		} catch (ConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		myForm.addItem(String.format("<P><INPUT TYPE='CHECKBOX' NAME='enableOSM' %s/>Enable OpenStreet Terrain map.</P>", (enableOSM ? "CHECKED" : "")));
		myForm.addItem(String.format("<P><INPUT TYPE='CHECKBOX' NAME='enableNASA' %s/>Enable NASA BlueMarble map.</P>", (enableNASA ? "CHECKED" : "")));

		String googleMapKey = myConfig.getGoogleMapKey();

		myForm.addItem(String.format("<P>Google Maps API Key: <INPUT TYPE='TEXT' NAME='googleMapKey' VALUE='%s' SIZE=100 />", 
				( googleMapKey != null ? googleMapKey : "") ));
		myForm.addItem("<BR>You can acquire a Map API key at <A HREF='http://code.google.com/apis/maps/signup.html'>http://code.google.com/apis/maps/signup.html</A></P>");

		myForm.addItem("<BR>Link to an additional MapServer services.  MapServer source code/binaries can be downloaded at <A HREF='http://mapserver.org/'>http://mapserver.org/</A>");
		myForm.addItem("<BR>New Layer: (Name)<INPUT TYPE='TEXT' NAME='mapName' SIZE=25 /> (URL)<INPUT TYPE='TEXT' NAME='mapURL' SIZE=75 />");
	
		if ( layers.size() > 0 ) {
			myForm.addItem("<BR><B>Existing layers</B>");
			myForm.addItem(aTable);			
		}

		myForm.addItem("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='updateAction' VALUE='Update'/><INPUT TYPE='RESET'/></P>");

		myForm.setAttribute("METHOD", "POST");
		return myForm.toString();

	}
	
	public String filepathsForm() {
		Form myForm = new Form("<P ALIGN='CENTER'><FONT SIZE='+1'><B>File Paths</B></FONT></P>");
		
		TableRow aRow = new TableRow("<TH>Object Class</TH><TH>File Type</TH><TH>Directory</TH>");
		
		if ( this.hasFormValue("updateAction")) {
			myConfig.clearFilePaths();
			if ( this.hasFormValue("new_class") && (this.getFormValue("new_class").length() > 0) ) {
				myConfig.setFilePath(this.getFormValue("new_class"), this.getFormValue("new_type"), this.getFormValue("new_path"));
			}
			if ( this.hasFormValue("row") ) {
				String[] rows = this.getFormValues("row");
				for ( int i = 0; i < rows.length; i++ ) {
					if ( this.getFormValue(String.format("%s_class", rows[i])).length() > 0 ) {
						myConfig.setFilePath(this.getFormValue(String.format("%s_class", rows[i])), 
								this.getFormValue(String.format("%s_type", rows[i])), 
								this.getFormValue(String.format("%s_path", rows[i])));
					}
				}
			}
			this.myWrapper.getSession().setAttribute(APP_CONFIG_ATTR, myConfig);
		}

		Map<String, Map<String, String>> fileTypeMap = myConfig.getFilePathMap();
		Iterator<String> classIter = fileTypeMap.keySet().iterator();
		int row = 1;
		while ( classIter.hasNext() ) {
			String aClass = classIter.next();
			Map<String, String> classMap =  fileTypeMap.get(aClass);
			Iterator<String> typeIter = classMap.keySet().iterator();
			while ( typeIter.hasNext() ) {
				String aType = typeIter.next();
				TableCell myCell = new TableCell(String.format("<INPUT TYPE='HIDDEN' NAME='row' VALUE='%d'/><INPUT TYPE='TEXT' NAME='%d_class' VALUE=\"%s\"/>", row, row, aClass));
				myCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='%d_type' VALUE=\"%s\"/>", row, aType));
				myCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='%d_path' VALUE=\"%s\"/>", row, (String)classMap.get(aType)));
				aRow.addItem(myCell);
				row++;
			}
			aRow.addItem("<TD COLSPAN=3><HR></TD>");
		}
		aRow.addItem("<TD COLSPAN=3 ALIGN='CENTER'><B>New Mapping</B></TD>");
		aRow.addItem("<TD><INPUT TYPE='TEXT' NAME='new_class'/></TD><TD><INPUT TYPE='TEXT' NAME='new_type'/></TD><TD><INPUT TYPE='TEXT' NAME='new_path'/><TD>");
	
		Table myTable = new Table(aRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		myForm.addItem(myTable);
		myForm.setAttribute("METHOD", "POST");
		
		myForm.addItem("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='updateAction' VALUE='Update'/><INPUT TYPE='RESET'/></P>");
		return myForm.toString();
	}
	
	public String queueForm() {
		Form myForm = new Form("<P ALIGN='CENTER'><FONT SIZE='+1'><B>Work Queues</B></FONT></P>");
		myForm.setAttribute("METHOD", "POST");
		TableRow aRow = new TableRow("<TH>Type</TH><TH>Source</TH><TD></TD>");

		if ( this.hasFormValue("updateAction") ) {
			for ( int i = 0; i < AppConfig.QUEUE_TYPES.length; i++ ) {
				String mySource = this.getFormValue(String.format("queuetype-%02d", i));
				myConfig.setQueueSource(AppConfig.QUEUE_TYPES[i], mySource);
				if ( mySource.equals("static") ) {
					String queueNames = this.getFormValue(String.format("queues-%02d", i));
					if ( queueNames.length() > 0 ) {
						String[] queues = queueNames.split(" *, *");
						myConfig.setQueues(AppConfig.QUEUE_TYPES[i], queues);
					}
				}
			}
			this.myWrapper.getSession().setAttribute(APP_CONFIG_ATTR, myConfig);
		}


		Map<String,String> queueTypeMap = new HashMap<String,String>();
		queueTypeMap.put("user", "User");
		queueTypeMap.put("inoculation", "Growth");
		queueTypeMap.put("harvest", "Harvest");
		queueTypeMap.put("extract", "Extract");
		queueTypeMap.put("cryo", "Cryopreservation");
		queueTypeMap.put("separation", "Fractionation");
		queueTypeMap.put("assay", "Assay");
		
		Popup sourcePop = new Popup();
		sourcePop.addItemWithLabel("static", "Static");
		sourcePop.addItemWithLabel("none", "Single Queue");
		sourcePop.addItemWithLabel("jdbc", "Database");

		for ( int i = 0; i < AppConfig.QUEUE_TYPES.length; i++ ) {
			TableCell myCell = new TableCell(queueTypeMap.get(AppConfig.QUEUE_TYPES[i]));
			String mySource = myConfig.queueSource(AppConfig.QUEUE_TYPES[i]);
			if ( mySource == null ) {
				if ( AppConfig.QUEUE_TYPES[i].equals("user") ) 
					mySource = "jdbc";
				else 
					mySource = "static";
			}
			sourcePop.setDefault(mySource);
			sourcePop.setName(String.format("queuetype-%02d", i));
			myCell.addItem(sourcePop.toString());
			if ( mySource.equals("static") ) {
				List<String> queueNames = myConfig.queuesForType(AppConfig.QUEUE_TYPES[i]);
				StringBuffer nameVal = new StringBuffer();
				if ( queueNames != null && (queueNames.size() > 0 )) {
					ListIterator<String> nameIter = queueNames.listIterator();
						nameVal.append(nameIter.next());
						while ( nameIter.hasNext() ) {
							nameVal.append(" ,");
							nameVal.append(nameIter.next());
						}
				}
				myCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='queues-%02d' VALUE='%s' SIZE=40 />", i, nameVal.toString()));
			}
			aRow.addItem(myCell);
		}
		
		Table myTable = new Table(aRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		myForm.addItem(myTable);
				
		myForm.addItem("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='updateAction' VALUE='Update'/><INPUT TYPE='RESET'/></P>");
		return myForm.toString();
	}
	
	private String testDB(String jndiName) {
		StringBuffer output = new StringBuffer();
		output.append("<P ALIGN='CENTER'><B>Testing datasource</B> " + jndiName + "...");

		try { 
			
			InitialContext initCtx = new InitialContext();
			DataSource aDS = (DataSource)initCtx.lookup("java:comp/env/jdbc/" + jndiName);

			Connection aConn = aDS.getConnection();
			output.append("<FONT COLOR='GREEN'><B>Success</FONT></B></P>");
			output.append("<P CLASS='mainContent'><B>Checking Schema...</B>");
			int statusCols = 3;
			TableCell myCell = new TableHeader();

			for ( int i = 0; i < statusCols; i++ ) {
				myCell.addItem("Table");
				myCell.addItem("Status");
			}
			boolean schemaOK = true;
			TableRow aRow = new TableRow(myCell);
 
			DatabaseMetaData dbMeta = aConn.getMetaData();
			String[] types = {"TABLE"};
			ResultSet aResult = dbMeta.getTables(null, null, null, types);
			List<String> foundTables = new ArrayList<String>();
			if ( aResult.getType() != ResultSet.TYPE_FORWARD_ONLY ) aResult.beforeFirst();
			while ( aResult.next() ) {
				foundTables.add(aResult.getString("TABLE_NAME").toLowerCase());
			}

			String[] tables = myConfig.tableList();
			myCell = new TableCell();
			for ( int i=0; i < tables.length; i++ ) {
				myCell.addItem(tables[i]);
				if ( foundTables.contains(tables[i]) )
					myCell.addItem("<FONT COLOR='GREEN'><B>OK</B></FONT>");
				else {
					myCell.addItem("<FONT COLOR='RED'><B>NOT FOUND</B></FONT>");
					schemaOK = false;
				}
				int count = (i + 1) % statusCols;
				if ( count == 0 ) {
					aRow.addItem(myCell);
					myCell = new TableCell();
				}
			}

			if ( (tables.length % statusCols) > 0  ) {

			}

			Table myTable = new Table(aRow);
			myTable.setClass("status");
			output.append(myTable.toString());
			if ( ! schemaOK ) {
				output.append("Schema not up to date.");
			}
			output.append("</P>");
			aConn.close();
		} catch (SQLException e) {
			output.append("<BR/><FONT COLOR='RED'>ERROR:</FONT>" + e.getMessage() + "</B>");
			e.printStackTrace();
		} catch (NamingException e) {
			output.append("<BR/><FONT COLOR='RED'>ERROR:</FONT>" + e.getMessage() + "</B>");
			e.printStackTrace();
		}
		output.append("</P>");
		return output.toString();
	}

	public String manageConfig() throws NamingException {
		StringBuffer output = new StringBuffer();
		
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Application Configuration");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		output.append(head);
		
		if ( this.hasFormValue("saveFile") ) {
			try {
				myConfig.writeConfig();
				output.append("<DIV CLASS='messages'><P><B><FONT COLOR='green'>Configuration saved!</FONT><BR>Restart application to ensure changes take effect.</B></P></DIV>");
			} catch (Exception e) {
				output.append(this.handleException(e));
			}
		} else if ( this.hasFormValue("reloadFile")) {
			try {
				myConfig.loadConfig();
				output.append("<DIV CLASS='messages'><P><B><FONT COLOR='green'>Configuration reloaded!</FONT></B></P></DIV>");
			} catch (Exception e) {
				output.append(this.handleException(e));
			}
 		} 
				
//		String[] forms = { "jdbc", "filepaths", "queues", "maps", "modules" };
//		String[] labels = { "Datasources", "File Paths", "Work Queues", "Maps", "Modules" };
		String[] forms = { "savereload", "filepaths", "queues", "maps", "modules" };
		String[] labels = { "Basic", "File Paths", "Work Queues", "Maps", "Modules" };
		String myContent = "";

		HtmlList aList = new HtmlList();
		aList.unordered();
		
		String thisForm = this.getFormValue("form");;
		
		for ( int i = 0; i < forms.length; i++ ) {
			if ( forms[i].equals(thisForm) ) {
				switch (i) {
				case 0: myContent = this.basicForm(); break;
				case 1: myContent = this.filepathsForm(); break;
				case 2: myContent = this.queueForm(); break;
				case 3: myContent = this.mappingForm(); break;
				case 4: myContent = this.moduleForm(); break;
				}
				aList.addItem(String.format("<B>%s</B>", labels[i]));
			} else {
				aList.addItem(String.format("<A HREF='?form=%s'>%s</A>", forms[i], labels[i]));
			}
		}		
		
		if ( myConfig.isUnsaved() ) {
 			output.append("<DIV CLASS='messages'><P><FORM METHOD='POST'><B>Changes not saved to configuration file!</B> <BUTTON TYPE='SUBMIT' NAME='saveFile'>Save</BUTTON><BUTTON TYPE='SUBMIT' NAME='reloadFile'>Revert</BUTTON></FORM></P></DIV>");
		} else if (	myConfig.getDate() != null && myConfig.getDate().compareTo(((AppConfig)this.myWrapper.getAppConfig()).getDate()) != 0 ) {
			output.append("<DIV CLASS='messages'><P><B>Saved configuration different than running configuration.<BR>Restart application to ensure changes take effect.</B></P></DIV>"); 			
 		}
		
		output.append("<DIV ID='setupPanel'>");
		
		output.append("<DIV ID='sideNav'>");
		output.append(aList.toString());
		output.append("</DIV>");
		
		output.append("<DIV ID='mainPanel'>");
		output.append(myContent);
		output.append("</DIV></DIV>");
		
		return output.toString();
	}

	

}
