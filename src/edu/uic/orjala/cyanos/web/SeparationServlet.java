package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.Separation.SeparationProtocol;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.sql.SQLSeparationProtocol;
import edu.uic.orjala.cyanos.web.forms.AssayForm;
import edu.uic.orjala.cyanos.web.forms.ProtocolForm;
import edu.uic.orjala.cyanos.web.forms.SeparationForm;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.StyledText;

public class SeparationServlet extends ServletObject {


	/**
	 * 
	 */
	private static final long serialVersionUID = 5897244100880061390L;

	private static final String DATA_FILE_DIV_ID = "dataFiles";
	private static final String DATA_FILE_DIV_TITLE = "Data Files";

	public void display(CyanosWrapper aWrap) throws Exception 
	{

		PrintWriter out;
		String module = aWrap.getRequest().getPathInfo();

		if ( aWrap.hasFormValue("div") ) {
			aWrap.setContentType("text/html");
			out = aWrap.getWriter();
			String divTag = aWrap.getFormValue("div");
			if ( "/protocol".equals(module) && divTag.equals(ProtocolForm.DIV_ID) ) {
				ProtocolForm aForm = new ProtocolForm(aWrap, AssayForm.ASSAY_PROTOCOL, AssayForm.PROTOCOL_KEYS);
				Form retForm = aForm.protocolForm();
				out.print(retForm.toString());
			} else if ( aWrap.hasFormValue("id") ) {
				Separation thisSep = new SQLSeparation(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
				if ( thisSep.first() ) {
					SeparationForm myForm = new SeparationForm(aWrap);
					if ( divTag.equals(DATA_FILE_DIV_ID) ) {
						Div aDiv = new Div(myForm.dataForm(thisSep));
						aDiv.setID(SeparationForm.DATA_FORM);
						out.println(aDiv.toString());
					} else if ( divTag.equals(SeparationForm.DATA_FORM) ) {
						out.println(myForm.dataForm(thisSep));
					}
				}
			}
			out.flush();
			return;

		}
		
		if ( "/export".equals(module) ) {
			out = aWrap.getWriter();
			aWrap.setContentType("text/plain");
			if ( aWrap.hasFormValue("id") )
				out.println(this.exportSeparation(aWrap));
			else 
				out.println(this.exportSepList(aWrap));
			out.flush();
			return;
		}
		
		out = aWrap.startHTMLDoc("Separation Data");
				
		if ( module == null ) {
			if ( aWrap.hasFormValue("id") ) {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Separation Record");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head.toString());

				SeparationForm myForm = new SeparationForm(aWrap);
				Separation thisSep = new SQLSeparation(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
				out.println(myForm.showSeparation(thisSep));
				out.println(myForm.sourceDiv(thisSep));
				out.println(myForm.fractionDiv(thisSep));
				out.println(myForm.loadableDiv(DATA_FILE_DIV_ID, DATA_FILE_DIV_TITLE));


			} else {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Separation Data");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head.toString());
				out.println("<P ALIGN='CENTER'><A HREF='separation/export'>Export Separation Data</A></P>");
				Form myForm = new Form();
				myForm.addItem("<P ALIGN='CENTER'>Separation ID:<INPUT TYPE='TEXT' NAME='id'><INPUT TYPE='SUBMIT' VALUE='Load'/></P>");
				out.println(myForm.toString());
			}
		} else if ( module.equals("/protocol") ) {
			out.println(this.protocolModule(aWrap));
		}
		
		aWrap.finishHTMLDoc();
	}

	private String exportSepList(CyanosWrapper aWrap) {
		try {
			Separation thisSep = SQLSeparation.separations(aWrap.getSQLDataSource());
			if ( thisSep != null && thisSep.first() ) {
				List<List> output = new ArrayList<List>();
				List<String> aRow = new ArrayList<String>();
				aRow.add("Separation ID");
				aRow.add("Culture ID");
				aRow.add("Stationary Phase");
				aRow.add("Mobile Phase");
				aRow.add("Method");
				aRow.add("Date");
				aRow.add("Notes");
				output.add(aRow);
				while ( thisSep.next() ) {
					aRow = new ArrayList<String>();
					aRow.add(thisSep.getID());
					Sample parents = thisSep.getSources();
					if ( parents != null && parents.first() ) 
						aRow.add(parents.getCultureID());
					else
						aRow.add("");
					aRow.add(thisSep.getStationaryPhase());
					aRow.add(thisSep.getMobilePhase());
					aRow.add(thisSep.getMethod());
					aRow.add(thisSep.getDateString());
					aRow.add(thisSep.getNotes());
					output.add(aRow);
				}
				return this.delimOutput(output, ",");
			} else {
				return "ERROR: SEPARATION NOT FOUND.";
			}
		} catch (DataException e) {
			e.printStackTrace();
			return "ERROR: " + e.getMessage();
		}
	}

	private String protocolModule(CyanosWrapper aWrap) {
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Separation Protocols");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		Form uploadForm = new Form(head);
		uploadForm.setAttribute("METHOD", "POST");
		uploadForm.addItem("<CENTER>");
		
		SeparationProtocol myProtocol = null;
		try {
			myProtocol = this.buildProtocol(aWrap);
		} catch (DataException e) {
			return aWrap.handleException(e);
		}
		
		if ( aWrap.hasFormValue("loadform") ) {
			uploadForm.addItem(this.loadProtocolTemplate(aWrap, myProtocol));
		} else if ( aWrap.hasFormValue("saveform") ) {
			uploadForm.addItem(this.saveProtocolTemplate(aWrap, myProtocol));
		} else {
			uploadForm.addItem("<INPUT TYPE='SUBMIT' NAME='loadform' VALUE='Load a protocol template'/>");
			uploadForm.addItem("<INPUT TYPE='SUBMIT' NAME='saveform' VALUE='Save as a protocol template'/><BR/>");
			uploadForm.addItem(this.protocolForm(aWrap, myProtocol));
		}
		uploadForm.addItem("</CENTER>");
		return uploadForm.toString();
	}
	
	private SeparationProtocol buildProtocol(CyanosWrapper aWrap) throws DataException {
		SeparationProtocol myProtocol = new SQLSeparationProtocol(aWrap.getSQLDataSource());
		myProtocol.setMethod(aWrap.getFormValue("method"));
		myProtocol.setMobilePhase(aWrap.getFormValue("mphase"));
		myProtocol.setStationaryPhase(aWrap.getFormValue("sphase"));
		return myProtocol;
	}

	private String loadProtocolTemplate(CyanosWrapper aWrap, SeparationProtocol myProtocol) {
		StringBuffer output = new StringBuffer();
		if ( aWrap.hasFormValue("loadAction") ) {
				try {
					myProtocol = new SQLSeparationProtocol(aWrap.getSQLDataSource(), aWrap.getFormValue("template"));
					if ( myProtocol != null )
						output.append("<P ALIGN='CENTER'><FONT COLOR='green'><B>Data template loaded.</B></FONT></P>");		
					else
						output.append("<P ALIGN='CENTER'><FONT COLOR='red'><B>Could not retrieve data template!</B></FONT></P>");
				} catch (DataException e) {
					output.append(aWrap.handleException(e));
				}
				output.append("<INPUT TYPE='SUBMIT' NAME='loadform' VALUE='Load a protocol template'/>");
				output.append("<INPUT TYPE='SUBMIT' NAME='saveform' VALUE='Save as a protocol template'/><BR/>");
		} else if ( aWrap.hasFormValue("cancelAction")) {
			output.append("<INPUT TYPE='SUBMIT' NAME='loadform' VALUE='Load a protocol template'/>");
			output.append("<INPUT TYPE='SUBMIT' NAME='saveform' VALUE='Save as a protocol template'/><BR/>");
		} else {
			output.append(this.loadDataTemplateForm(aWrap));
		}
		output.append(this.protocolForm(aWrap, myProtocol));
		return output.toString();
	}

	private String saveProtocolTemplate(CyanosWrapper aWrap, SeparationProtocol aProtocol) {
		StringBuffer output = new StringBuffer();
		if ( aWrap.hasFormValue("action") ) {
			if ( aWrap.getFormValue("action").equals("Save protocol template") ) {
				String templateName = null;
				if ( aWrap.hasFormValue("template") ) {
					if ( aWrap.getFormValue("template").equals("") ) {
						templateName = aWrap.getFormValue("newName");
					} else {
						templateName = aWrap.getFormValue("template");
					}
				}
				if ( templateName != null ) {
					aProtocol.setName(templateName);
					try {
						aProtocol.save();
						output.append("Protocol saved!");
					} catch (DataException e) {
						output.append(aWrap.handleException(e));
					}
					output.append("<INPUT TYPE='SUBMIT' NAME='loadform' VALUE='Load a protocol template'/>");
					output.append("<INPUT TYPE='SUBMIT' NAME='saveform' VALUE='Save as a protocol template'/><BR/>");
				} else {
					output.append("<FONT COLOR='red'><B>Specify a name for the template</B></FONT>");
					output.append(this.saveProtocolTemplateForm(aWrap));
				}
			}
			output.append(this.protocolForm(aWrap, aProtocol));
		} else {
			output.append(this.saveProtocolTemplateForm(aWrap));
			output.append(this.protocolForm(aWrap, aProtocol));
		}
		return output.toString();
	}

	private String saveProtocolTemplateForm(CyanosWrapper aWrap) {
		StringBuffer output = new StringBuffer();
		try {			
			List<String> protocols = SQLSeparationProtocol.protocols(aWrap.getSQLDataSource());
			ListIterator<String> anIter = protocols.listIterator();
			Popup aPop = new Popup();
			aPop.addItemWithLabel("", "A New File ->");
			aPop.setName("template");
			while ( anIter.hasNext() ) {
				aPop.addItem(anIter.next());
			}
			output.append("<INPUT TYPE='HIDDEN' NAME='saveform' VALUE='inform'/>");
			output.append("<P><B>Save template to:</B>");
			output.append(aPop.toString());
			output.append("<INPUT TYPE='TEXT' NAME='newName'/>");
			output.append("</P><P><INPUT TYPE='SUBMIT' NAME='action' VALUE='Save protocol template'/><INPUT TYPE='SUBMIT' NAME='action' VALUE='Cancel'/></P>");
		} catch (DataException e) {
			return aWrap.handleException(e);
		}

		return output.toString();
	}

	private String protocolForm(CyanosWrapper aWrap, SeparationProtocol myProtocol) {
		SeparationForm aForm = new SeparationForm(aWrap);
		return aForm.protocolForm(myProtocol);
	}

	private String loadDataTemplateForm(CyanosWrapper aWrap) {
		StringBuffer output = new StringBuffer();
		try {
			List<String> protocols = SQLSeparationProtocol.protocols(aWrap.getSQLDataSource());
			ListIterator<String> anIter = protocols.listIterator();
			
			Popup aPop = new Popup();
			aPop.addItemWithLabel("", "---SELECT A TEMPLATE---");
			aPop.setName("template");
			while ( anIter.hasNext() ) {
				aPop.addItem(anIter.toString());
			}
			output.append("<INPUT TYPE='HIDDEN' NAME='loadform' VALUE='inForm'/>");
			output.append("<P><B>Select a template:</B>");
			output.append(aPop.toString());
			output.append("</P><P><INPUT TYPE='SUBMIT' NAME='loadAction' VALUE='Load a protocol template'/><INPUT TYPE='SUBMIT' NAME='cancelAction' VALUE='Cancel'/></P>");
		} catch (DataException e) {
			return aWrap.handleException(e);
		}
		return output.toString();
	}
	

	private String exportSeparation(CyanosWrapper aWrap) {
		try {
			Separation thisSep = new SQLSeparation(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
			if ( thisSep.first() ) {
				List<List> output = new ArrayList<List>();
				List<String> aRow = new ArrayList<String>();
				aRow.add("Separation ID");
				aRow.add("FR Number");
				aRow.add("Sample ID");
				aRow.add("Culture ID");
				aRow.add("Sample Label");
				aRow.add("Amount");
				output.add(aRow);
				String myID = thisSep.getID();
				Sample parents = thisSep.getSources();
				if ( parents != null ) {
					parents.beforeFirst();
					while ( parents.next() ) {
						aRow = new ArrayList<String>();
						aRow.add(myID);
						aRow.add("SOURCE");
						aRow.add(parents.getID());
						aRow.add(parents.getCultureID());
						aRow.add(parents.getName());
						aRow.add(BaseForm.formatAmount("%.2f %s", parents.getAmountForSeparation(myID), parents.getBaseUnit()));
						output.add(aRow);
					}
				}
				thisSep.beforeFirstFraction();
				while ( thisSep.nextFraction() ) {
					Sample aFrac = thisSep.getCurrentFraction();
					aRow = new ArrayList<String>();
					aRow.add(myID);
					aRow.add(String.valueOf(thisSep.getCurrentFractionNumber()));
					aRow.add(aFrac.getID());
					aRow.add(aFrac.getCultureID());
					aRow.add(aFrac.getName());
					aRow.add(BaseForm.formatAmount("%.2f %s", aFrac.getAmountForSeparation(myID), aFrac.getBaseUnit()));
					output.add(aRow);
				}
				return this.delimOutput(output, ",");
			} else {
				return "ERROR: SEPARATION NOT FOUND.";
			}
		} catch (DataException e) {
			e.printStackTrace();
			return "ERROR: " + e.getMessage();
		}
	}

	
}
