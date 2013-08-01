/**
 * 
 */
package edu.uic.orjala.cyanos.web.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author George Chlipala
 *
 */
public class PopupGroup {

	protected Map<String,String> options = new HashMap<String,String>();
	protected List<Object> keys = new ArrayList<Object>();
	protected String defValue;
	protected String name = "";

	public PopupGroup() {
	}

	public PopupGroup(String aName) {
		name = aName;
	}

	/**
	 * Add an item to the popup (&lt;OPTION&gt;)
	 */
	public void addItem(String newValue) {
		this.options.put(newValue, newValue);
		this.keys.add(newValue);
	}

	/**
	 * Add an array of items to the popup (&lt;OPTION&gt;)
	 */
	public void addItem(String[] newItem) {
		for (int i = 0; i < newItem.length; i++) {
			this.options.put(newItem[i], newItem[i]);
			this.keys.add(newItem[i]);
		}
	}

	/**
	 * Add an item to the popup with a label (&lt;OPTION&gt;)
	 */
	public void addItemWithLabel(String newValue, String newLabel) {
		this.options.put(newValue, newLabel);
		this.keys.add(newValue);
	}

	/**
	 * Add an array of items to the popup with labels (&lt;OPTION&gt;)
	 */
	public void addItemWithLabel(String[] newItem, String[] newLabel) {
		for (int i = 0; i < newItem.length; i++) {
			this.options.put(newItem[i], newLabel[i]);
			this.keys.add(newItem[i]);
		}
	}
	/**
	 * Add an item to the popup (&lt;OPTION&gt;) at specified index.
	 */
	public void addItem(String newValue, int index) {
		this.options.put(newValue, newValue);
		this.keys.add(index, newValue);
	}

	/**
	 * Add an array of items to the popup (&lt;OPTION&gt;) at specified index.
	 */
	public void addItem(String[] newItem, int index) {
		for (int i = newItem.length; i >= 0; i--) {
			this.options.put(newItem[i], newItem[i]);
			this.keys.add(index, newItem[i]);
		}
	}

	/**
	 * Add an item to the popup with a label (&lt;OPTION&gt;) at specified index.
	 */
	public void addItemWithLabel(String newValue, String newLabel, int index) {
		this.options.put(newValue, newLabel);
		this.keys.add(index, newValue);
	}

	/**
	 * Add an array of items to the popup with labels (&lt;OPTION&gt;) at specified index.
	 */
	public void addItemWithLabel(String[] newItem, String[] newLabel, int index) {
		for (int i = newItem.length; i >= 0; i--) {
			this.options.put(newItem[i], newLabel[i]);
			this.keys.add(index, newItem[i]);
		}
	}
	
	public void setDefault(String aValue) {
		this.defValue = aValue;
	}
	
	public void setName(String aName) {
		this.name = aName;
	}

	public String toString() {
		StringBuffer output = new StringBuffer(); 

		output.append(String.format("<OPTGROUP LABEL='%s'>", name));
		for (Iterator<Object> key = this.keys.iterator(); key.hasNext();) {
			Object item = key.next();
			if ( item.getClass().isAssignableFrom(PopupGroup.class)) {
				((PopupGroup) item).setDefault(this.defValue);
				output.append(item.toString());
			} else {
				if ( item.equals(defValue) ) {
					output.append("<OPTION VALUE=\"" + item.toString() + "\" SELECTED>" + 
							this.options.get(item) + "</OPTION>");
				} else {
					output.append("<OPTION VALUE=\"" + item.toString() + "\">" + 
							this.options.get(item) + "</OPTION>");				
				}
			}
		}
		output.append("</OPTGROUP>");
		return output.toString();
	}
}
