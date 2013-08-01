/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.Queue;
import edu.uic.orjala.cyanos.web.QueueItem;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Image;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class QueueForm extends BaseForm {

	/**
	 * 
	 */
	
	private static final String[] SPECIES_QUEUES = { "inoculation", "cryo", "assay" };
	private static final String[] INOC_QUEUES = { "harvest", "cryo" };
	private static final String[] HARVEST_QUEUES = { "extract" };
	private static final String[] SAMPLE_QUEUES = { "separation", "assay" };

	public QueueForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}
	
	public String listQueues(Queue queues) {
		
		try {
			TableRow myRow = new TableRow("<TH>Queue Name</TH><TH>Type</TH><TH>Total Items</TH><TH>Completed Items</TH>");
			Table myTable = new Table(myRow);
			myTable.setAttribute("class","dashboard");
			myTable.setAttribute("align","center");
			myTable.setAttribute("width","75%");

			queues.beforeFirst();
			boolean oddRow = true;
			while ( queues.next() ) {
				TableCell aCell = new TableCell();
				aCell.addItem(String.format("<A HREF='%s/queue/%s/%s/'>%s</A>", this.myWrapper.getContextPath(), queues.getQueueType(), queues.getQueueName(), queues.getQueueName()));
				aCell.addItem(String.format("<A HREF='%s/queue/%s'>%s</A>", this.myWrapper.getContextPath(), queues.getQueueType(), queues.getQueueType()));
				aCell.addItem(queues.numberOfItems());
				aCell.addItem(queues.numberComplete());
				myRow = new TableRow(aCell);
				if ( oddRow ) {
					myRow.setClass("odd");
				} else {
					myRow.setClass("even");
				}
				oddRow = (! oddRow);
				myTable.addItem(myRow);
			}
			return myTable.toString();
		} catch (DataException e) {
			return this.handleException(e);
		}
	}

	public String listQueues(String queueType) {
		try {
			Queue queues = Queue.queuesForType(this.getSQLDataSource(), queueType);
			return this.listQueues(queues);
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	public String listAllQueues() {
		try {
			Queue queues = Queue.allQueues(this.getSQLDataSource());
			return this.listQueues(queues);
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	public String listMyQueues() {
		try {
			return this.listQueues(Queue.myQueues(this.getSQLDataSource()));
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	public String queueDetails(String queueType, String queueName) {
		try {
			return this.queueDetails(QueueItem.loadAll(this.getSQLDataSource(), queueType, queueName));
		} catch (DataException e) {
			return this.handleException(e);
		}
	}

	public String queueDetails(QueueItem queueItems) {
	try {
			TableRow myRow = new TableRow("<TD></TD><TH>Item</TH><TH>Requested by:</TH><TH>Request Details</TH><TH>Added to queue</TH><TH>Completed</TH><TH>Completed By:</TH><TH>Notes</TH>");
			Table myTable = new Table(myRow);
			myTable.setAttribute("class","list");
			myTable.setAttribute("align","center");
			myTable.setAttribute("width","75%");

			

			queueItems.beforeFirst();
			boolean oddRow = true;
			String baseUrl = this.myWrapper.getContextPath();
			SimpleDateFormat myFormat = this.myWrapper.dateTimeFormat();
			
			while ( queueItems.next() ) {	
				String itemClass = queueItems.getItemType();
				String itemID = queueItems.getItemID();
				
				String checked = "";
				String id = String.format("%s(%s)", itemClass, itemID);
				boolean selected = ( this.hasFormValue("item") && this.getFormValue("item").equals(id) );
				if ( selected ) { 
					if ( this.hasFormValue("cancelAction") ) {
						selected = false;
					} else if ( this.hasFormValue("updateAction") ) {
						queueItems.complete(this.getFormValue("content"));
						selected = false;
					} else {
						checked = "CHECKED";						
					}
				}

				Date completed = queueItems.getDateCompleted();
				
				TableCell aCell;
				if ( completed == null ) {
					aCell = new TableCell(String.format("<INPUT TYPE='RADIO' NAME='item' VALUE='%s' onClick='this.form.submit()' %s/>", id, checked));
				} else {
					aCell = new TableCell("");
				}
				aCell.addItem(String.format("<A HREF='%s/%s?id=%s'>%s %s</A>", baseUrl, itemClass, itemID, itemClass, itemID));
				aCell.addItem(queueItems.getRequestedUserID());
				aCell.addItem(this.formatStringHTML(queueItems.getRequestDetails()));
				aCell.addItem(myFormat.format(queueItems.getDateAdded()));
				
				if ( selected ) {
					aCell.addItem(String.format("<I>%s</I>", myFormat.format(new Date())));
					aCell.addItem(String.format("<I>%s</I>", this.getUser().getUserID()));
					aCell.addItem("<TEXTAREA NAME='content' ROWS=3 COLS=35></TEXTAREA>");
				} else if ( completed != null ) {
					aCell.addItem(myFormat.format(completed));
					aCell.addItem(queueItems.getCompletedUserID());
					aCell.addItem(this.formatStringHTML(queueItems.getCompletionDetails()));
				} else {
					aCell.addItem("Incomplete");
					aCell.addItem("");
					aCell.addItem("");
				}
				myRow = new TableRow(aCell);
				String rowClass = "";
				if ( completed == null ) rowClass = "danger";
				if ( oddRow ) {
					rowClass = rowClass + "odd";
				} else {
					rowClass = rowClass + "even";
				}
				myRow.setClass(rowClass);
				if ( selected ) myRow.addItem("<TD COLSPAN='8' ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='updateAction' VALUE='Update'/><INPUT TYPE='SUBMIT' NAME='cancelAction' VALUE='Cancel'><INPUT TYPE='reset'></TD>");
				oddRow = (! oddRow);
				myTable.addItem(myRow);
			}
			Form myForm = new Form(myTable);
			myForm.setName("queue");
			myForm.setAttribute("METHOD", "POST");
			return myForm.toString();							
		} catch (DataException e) {
			return this.handleException(e);
		}


	}
		
	public String addToQueue() {
		try {			
			String type = this.getFormValue("queue_type");
			AppConfig myConf = myWrapper.getAppConfig();
			String queueName = this.getFormValue("queue");
			
			if ( myConf.isQueueSingle(type))
				queueName = "default";
			else if ( queueName.equals("") ) 
				queueName = this.getFormValue("newQueue");

			Queue.addItem(this.getSQLDataSource(), type, queueName, this.getFormValue("class"), this.getFormValue("id"), this.getFormValue("notes"));
			return this.message(SUCCESS_TAG, "<B>Adding item to queue.</B></P><P ALIGN='CENTER'><BUTTON TYPE=BUTTON onClick='window.close();'>Close Window</BUTTON></P>");
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	public String addForm() throws ConfigException {
		if ( ! ( this.hasFormValue("id") && this.hasFormValue("class")))
			return "<P ALIGN='CENTER'><B><FONT COLOR='RED'>ERROR:</FONT> Calling link must specifiy an item to add to the queue.</B></P>";
		
		AppConfig myConf = myWrapper.getAppConfig();

		Form myForm = new Form();
		myForm.setAttribute("METHOD", "POST");
		myForm.setAttribute("ACTION", this.myWrapper.getRequestURI());	
		
		myForm.setName("queue");
		myForm.addItem("<INPUT TYPE=HIDDEN NAME='id' VALUE='" + this.getFormValue("id") + "'/>");
		myForm.addItem("<INPUT TYPE=HIDDEN NAME='class' VALUE='" + this.getFormValue("class") + "'/>");
		myForm.addItem(String.format("<P ALIGN='CENTER'>Item: %s(%s)</P>", this.getFormValue("class"), this.getFormValue("id")));

		Map<String,String> queueTypeMap = new HashMap<String,String>();
		queueTypeMap.put(Queue.INOCULATION_QUEUE, "Growth");
		queueTypeMap.put(Queue.HARVEST_QUEUE, "Harvest");
		queueTypeMap.put(Queue.EXTRACTION_QUEUE, "Extract");
		queueTypeMap.put(Queue.CRYO_QUEUE, "Cryopreservation");
		queueTypeMap.put(Queue.FRACTION_QUEUE, "Fractionation");
		queueTypeMap.put(Queue.BIOASSAY_QUEUE, "Assay");
		
		TableCell myCell = new TableCell("Queue Type:");
		Popup aPop = new Popup();
		aPop.addItemWithLabel("user", "User");
		
		String[] extraQueues = {};
		
		String itemClass = this.getFormValue("class");
		if ( itemClass.equals("strain") ) {
			extraQueues = QueueForm.SPECIES_QUEUES;
		} else if ( itemClass.equals("inoc") ) {
			extraQueues = QueueForm.INOC_QUEUES;
		} else if ( itemClass.equals("harvest") ) {
			extraQueues = QueueForm.HARVEST_QUEUES;
		} else if ( itemClass.equals("sample")) {
			extraQueues = QueueForm.SAMPLE_QUEUES;
		}
		
		for ( int i = 0; i < extraQueues.length; i++) {
			aPop.addItemWithLabel(extraQueues[i], queueTypeMap.get(extraQueues[i]));
		}

		aPop.setName("queue_type");
		aPop.setAttribute("onChange", "this.form.submit();");
		if ( this.hasFormValue("queue_type"))
			aPop.setDefault(this.getFormValue("queue_type"));

		myCell.addItem(aPop);		
		TableRow tableRow = new TableRow(myCell);
		
		myCell = new TableCell("Queue:");
		String queueType = this.getFormValue("queue_type");
		if ( myConf.isQueueStatic(queueType) ) {
			List<String> queueList = myConf.queuesForType(queueType);
			aPop = new Popup();
			if ( queueList != null ) {
				ListIterator<String> qIter = queueList.listIterator();
				while ( qIter.hasNext() ) {
					aPop.addItem(qIter.next());
				}
			}
			aPop.setName("queue");
			if ( this.hasFormValue("queue"))
				aPop.setDefault("queue");
			myCell.addItem(aPop);
		} else if ( myConf.isQueueSingle(queueType) ) {
			myCell.addItem("N/A");
		} else {
			myCell.addItem(this.queuePopupWithNew("queue", "newQueue", queueType));				
		}

		tableRow.addItem(myCell);
		
		tableRow.addItem("<TD COLSPAN=2>Request Description</TD>");
		tableRow.addItem(String.format("<TD COLSPAN=2><TEXTAREA NAME='notes' COLS=35 ROWS=5>%s</TEXTAREA></TD>", this.getFormValue("notes")));

		myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='addAction' VALUE='Add Item To Queue'/><INPUT TYPE='RESET'/><BR/><INPUT TYPE='BUTTON' NAME='resetForm' VALUE='Return' onClick='window.close();'/>");			
		myCell.setAttribute("colspan","2");
		myCell.setAttribute("align","center");
		tableRow.addItem(myCell);

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		myForm.addItem(myTable.toString());
		
		return myForm.toString();
	}
	
	public String queuePopupWithNew(String varName, String newVar, String queueType) {
		try {
			Queue allQueues = Queue.allQueues(this.getSQLDataSource());
			Popup aPop = new Popup();
			aPop.setName(varName);
			allQueues.beforeFirst();
			aPop.addItemWithLabel("", "Other ->");
			while ( allQueues.next() ) {
				aPop.addItem(allQueues.getQueueName());
			}
			if ( this.hasFormValue(varName) ) 
				aPop.setDefault(this.getFormValue(varName));
			return String.format("%s <INPUT TYPE='TEXT' NAME='%s' VALUE='%s'/>", aPop.toString(), newVar, this.getFormValue(newVar));
		} catch (DataException e ) {
			return this.handleException(e);
		}
	}

	public String queueModule(String userName) {
		TableRow myRow = new TableRow("<TH>Queue</TH><TH>To Do</TH><TH>Completed</TH><TH>Total</TH>");
		Table myTable = new Table(myRow);
		myTable.setAttribute("align", "center");

		try {
			
			TableCell myCell = new TableCell(String.format("<A HREF='%s/queue/user/%s'>My Queue</A>",myWrapper.getContextPath(), userName));
			Queue myQueues = Queue.userQueue(this.getSQLDataSource());
			if ( myQueues.first() ) {
				int total = myQueues.numberOfItems();
				int complete = myQueues.numberComplete();
				
				myCell.addItem(String.format("%d", total - complete));
				myCell.addItem(complete);
				myCell.addItem(total);
			} else {
				myCell.addItem("-");
				myCell.addItem("-");
				myCell.addItem("-");				
			}
			myRow.addItem(myCell);				

			myQueues = Queue.myQueues(this.getSQLDataSource());
			if ( myQueues.first() ) {

				myRow.addItem("<TD COLSPAN=4' ALIGN='CENTER'><B>Subscribed Queues</B></TD>");

				myQueues.beforeFirst();
				while ( myQueues.next() ) {

					myCell = new TableCell(String.format("<A HREF='%s/queue/%s/%s'>%s</A>",myWrapper.getContextPath(), myQueues.getQueueType(), myQueues.getQueueName(), myQueues.getQueueName()));
					int total = myQueues.numberOfItems();
					int complete = myQueues.numberComplete();
					
					myCell.addItem(String.format("%d", total - complete));
					myCell.addItem(complete);
					myCell.addItem(total);
					myRow.addItem(myCell);
				}
			}
		} catch (DataException e) {
			this.handleException(e);
		}
		
		Div moduleDiv = new Div(myTable);
		moduleDiv.setClass("hideSection");
		moduleDiv.setID("div_queueModule");
		Image anImage = this.getImage("module-twist-closed.png");
		anImage.setAttribute("ID", "twist_queueModule");
		anImage.setAttribute("ALIGN", "absmiddle");
		return "<P CLASS='moduleTitle'><A NAME='queueModule' onClick='twistModule(\"queueModule\")'>" + anImage.toString() +
			"  Work Queues</A></P>" + moduleDiv.toString();


	}
	
}

