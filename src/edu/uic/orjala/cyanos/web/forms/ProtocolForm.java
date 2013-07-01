/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLProtocol;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Popup;

/**
 * @author George Chlipala
 *
 */
public class ProtocolForm extends BaseForm {

	public static final String DIV_ID = "templateForm";
	
	private String myDataType = null;
	private String[] protocolKeys = null;
	
	private Map<String,String> currentTemplate = null;
	
	private static final String LOAD_FORM_BUTTON = "<BUTTON NAME='loadProtoForm' onClick=\"destForm = this.form; loadForm(this, 'templateForm');\" TYPE=BUTTON>%s</BUTTON>";
	private static final String SAVE_FORM_BUTTON = "<BUTTON NAME='saveProtoForm' onClick=\"destForm = this.form; loadForm(this, 'templateForm');\" TYPE=BUTTON>%s</BUTTON><BR/>";
	private static final String LOAD_FORM = "loadProtoForm";
	private static final String LOAD_ACTION = "loadAction";
	private static final String SAVE_FORM = "saveProtoForm";
	private static final String SAVE_ACTION = "saveAction";
	
	private static final String LOAD_FORM_HIDDEN = String.format("<INPUT TYPE='HIDDEN' NAME='%s' VALUE='inForm'/><INPUT TYPE='HIDDEN' NAME='div' VALUE='%s'/>", LOAD_FORM, DIV_ID);
	private static final String SAVE_FORM_HIDDEN = String.format("<INPUT TYPE='HIDDEN' NAME='%s' VALUE='inForm'/><INPUT TYPE='HIDDEN' NAME='div' VALUE='%s'/>", SAVE_FORM, DIV_ID);
	
	private static final String CANCEL_BUTTON = String.format("<BUTTON onClick=\"closeForm('%s');\" TYPE=BUTTON>Cancel</BUTTON>", DIV_ID);
	private static final String CLOSE_BUTTON = String.format("<BUTTON onClick=\"closeForm('%s');\" TYPE=BUTTON>Close</BUTTON>", DIV_ID);
	private static final String LOAD_BUTTON = String.format("<BUTTON NAME='%s' onClick=\"updateForm(this, '%s');\" TYPE=BUTTON>Load a protocol template</BUTTON>", LOAD_ACTION, DIV_ID);
	private static final String SAVE_BUTTON = String.format("<BUTTON NAME='%s' onClick=\"updateForm(this, '%s');\" TYPE=BUTTON>Save as aprotocol template</BUTTON>", SAVE_ACTION, DIV_ID);
	
	public ProtocolForm(CyanosWrapper aWrapper, String dataType, String[] templateKeys) {
		super(aWrapper);
		this.myDataType = dataType;
		this.protocolKeys = templateKeys;
	}
	
	public static String loadButton(String label) {
		return String.format(LOAD_FORM_BUTTON, label);
	}
	
	public static String saveButton(String label) {
		return String.format(SAVE_FORM_BUTTON, label);
	}
	
	public boolean isSaveForm() {
		return this.hasFormValue(SAVE_FORM);
	}
	
	public static Div formDiv(String loadText, String saveText) {
		Div myDiv = new Div("<P ALIGN='CENTER'>");
		myDiv.setID(DIV_ID);
		if ( loadText != null )
			myDiv.addItem(ProtocolForm.loadButton(loadText));
		if ( saveText != null )
			myDiv.addItem(ProtocolForm.saveButton(saveText));
		myDiv.addItem("</P>");
		return myDiv;
	}
	
	public Form saveForm() {
		Form aForm = new Form();
		aForm.addItem("<CENTER>");
		if ( this.hasFormValue(SAVE_ACTION) ) {
			String templateName = null;
			if ( this.hasFormValue("template") ) {
				if ( this.getFormValue("template").equals("") ) {
					templateName = this.getFormValue("newName");
				} else {
					templateName = this.getFormValue("template");
				}
			}
			if ( templateName != null ) {
				try {
					this.saveDataTemplate(this.myDataType, templateName, this.currentTemplate );
					aForm.addItem(this.message(SUCCESS_TAG, "Template saved<BR/>"));
				} catch (DataException e) {
					aForm.addItem(this.handleException(e));
				}
				aForm.addItem(CLOSE_BUTTON);
			} else {
				aForm.addItem("<FONT COLOR='red'><B>Specify a name for the template</B></FONT>");
				aForm.addItem(this.saveProtocolTemplateForm(this.myDataType));
			}
		} else {
			aForm.addItem(this.saveProtocolTemplateForm(this.myDataType));
		}
		aForm.addItem("</CENTER>");
		return aForm;
	}
	
	public Map<String, String> getTemplate() {
		return this.currentTemplate;
	}
	
	public Form loadForm(boolean reloadForm) {
		Form aForm = new Form();
		aForm.addItem("<CENTER>");
		if ( this.hasFormValue(LOAD_ACTION) ) {
			try {
				this.currentTemplate = this.loadDataTemplate(this.myDataType, this.getFormValue("template"));
				if ( this.currentTemplate != null ) {
					aForm.addItem(this.message(SUCCESS_TAG, "</B>Data template loaded.</B>"));		
					aForm.addItem(this.setButton(this.currentTemplate, reloadForm));
				} else {
					aForm.addItem(this.message(ERROR_TAG, "<B>Could not retrieve data template!</B>"));
				}
			} catch (DataException e) {
				aForm.addItem(this.handleException(e));
			}
		} else {
			aForm.addItem(this.loadDataTemplateForm(this.myDataType));
		}
		aForm.addItem("</CENTER>");
		return aForm;
	}
	
	public Form protocolForm() {
		return this.protocolForm(false);
	}
	
	public Form protocolForm(boolean reloadForm) {
		this.currentTemplate = this.buildTemplate(this.protocolKeys);		
		if ( this.hasFormValue(LOAD_FORM) ) {
			return this.loadForm(reloadForm);
		} else if ( this.hasFormValue(SAVE_FORM) ) {
			return this.saveForm();
		}
		return new Form();
	}

	protected String loadDataTemplateForm(String dataType) {
		StringBuffer output = new StringBuffer();
		try {
			List<String> protocols = SQLProtocol.listProtocols(this.getSQLDataSource(), dataType);
			ListIterator<String> anIter = protocols.listIterator();			
			Popup aPop = new Popup();
			aPop.setName("template");
			while (anIter.hasNext()) {
				aPop.addItem(anIter.next());
			}
			output.append(LOAD_FORM_HIDDEN);
			output.append("<P ALIGN='CENTER'><B>Select a template:</B>");
			output.append(aPop.toString());
			output.append("</P><P ALIGN='CENTER'>");
			output.append(LOAD_BUTTON);
			output.append(CANCEL_BUTTON);
			output.append("</P>");
		} catch (DataException e) {
			output.append(this.handleException(e));
		}
		return output.toString();
	}
	
	protected String saveProtocolTemplateForm(String dataType) {
		StringBuffer output = new StringBuffer();
		try {			
			List<String> protocols = SQLProtocol.listProtocols(this.getSQLDataSource(), dataType);
			ListIterator<String> anIter = protocols.listIterator();			
			Popup aPop = new Popup();
			aPop.addItemWithLabel("", "A New Protocol ->");
			aPop.setName("template");
			while (anIter.hasNext()) {
				aPop.addItem(anIter.next());
			}
			output.append(SAVE_FORM_HIDDEN);
			output.append("<P><B>Save template to:</B>");
			output.append(aPop.toString());
			output.append("<INPUT TYPE='TEXT' NAME='newName'/>");
			output.append("</P><P ALIGN='CENTER'>");
			output.append(SAVE_BUTTON);
			output.append(CANCEL_BUTTON);
			output.append("</P>");
		} catch (DataException e) {
			output.append(this.handleException(e));
		}
		return output.toString();
	}


	protected Map<String,String> buildTemplate(String[] tempKeys) {
		Map<String, String> newTemplate = new HashMap<String, String>();
		for (int i = 0; i < tempKeys.length; i++ ) {
			if ( this.hasFormValue(tempKeys[i])) {
				newTemplate.put(tempKeys[i], this.getFormValue(tempKeys[i]));
			}
		}
		return newTemplate;
	}
	
	private String setButton(Map<String, String> aTemplate, boolean reloadForm) {
		StringBuffer output = new StringBuffer("<BUTTON onClick=\" var elements = destForm.elements;\n");
		Set<String> keys = aTemplate.keySet();
		Iterator<String> anIter = keys.iterator();
		while ( anIter.hasNext() ) {
			String aKey = anIter.next();
			String aValue = aTemplate.get(aKey);
			if ( aValue != null ) {
				aValue = aValue.replaceAll("\\\"", "&quot;");
				aValue = aValue.replaceAll("\\'", "\\\\'");
			} else {
				aValue = "";
			}
			output.append(String.format(" setValue( elements['%s'], '%s');\n", aKey, aValue));
		}
		if ( reloadForm )
			output.append(" destForm.submit();\n");
		else 
			output.append(String.format(" closeForm('%s');", DIV_ID));
		output.append("\" TYPE=BUTTON>Use template</BUTTON>");
		return output.toString();
	}
	
}
