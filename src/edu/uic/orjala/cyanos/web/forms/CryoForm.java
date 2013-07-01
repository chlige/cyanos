/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import edu.uic.orjala.cyanos.Cryo;
import edu.uic.orjala.cyanos.CryoCollection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLCryo;
import edu.uic.orjala.cyanos.sql.SQLCryoCollection;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLInoc;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Anchor;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Image;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class CryoForm extends BaseForm {

	public static final String DIV_TITLE = "Cryopreservations";
	public static final String DIV_ID = "cryoInfo";

	/**
	 * @param callingServlet
	 */
	public CryoForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}

	public String cryoList(Strain aStrain) {
		StringBuffer output = new StringBuffer();
		
		String[] headerData = {"Collection", "Date", "Locations", "Notes"};
		TableCell header = new TableHeader(headerData);
		header.setAttribute("class","header");
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");

		try {
			SQLCryoCollection cryoBox = new SQLCryoCollection(this.getSQLDataSource());
			if ( cryoBox.loadForStrain(aStrain) ) {
				String curClass = "odd";
				if ( cryoBox.first() ) {
					cryoBox.beforeFirst();
					while ( cryoBox.next() ) {
						TableCell myCell = new TableCell();
						myCell.addItem("<A HREF='cryo?col=" + cryoBox.getID() + "'>" +
								cryoBox.getName() + "</A>");
						Cryo myCryos = cryoBox.getCryosForStrainID(aStrain.getID());
						myCryos.first();
						myCell.addItem(this.formatDate(myCryos.getDate()));
						myCell.addItem(cryoBox.getMinLocationForStrainID(aStrain.getID()) + " - " + cryoBox.getMaxLocationForStrainID(aStrain.getID()));
						myCell.addItem(myCryos.getNotes());
						TableRow aRow = new TableRow(myCell);
						aRow.setClass(curClass);
						aRow.setAttribute("align", "center");
						myTable.addItem(aRow);
						if ( curClass.equals("odd") ) {
							curClass = "even";
						} else {
							curClass = "odd";
						}
					}
				} else {
					myTable.addItem("<TR ALIGN='CENTER'><TH COLSPAN='5'>None</TH></TR>");
				}
			}

		} catch (DataException e) {
			myTable.addItem("<TR ALIGN='CENTER'><TD COLSPAN='5'>");
			myTable.addItem(this.handleException(e));
			myTable.addItem("</TD></TR>");
		}

		output.append(myTable);
		return output.toString();
	}

	public Div cryoDiv(Strain aStrain) {
		return this.collapsableDiv(DIV_ID, DIV_TITLE, this.cryoList(aStrain));
	}
	
	public Div cryoDiv() {
		return this.loadableDiv(DIV_ID, DIV_TITLE);
	}
	
	public String addCryo() {
		StringBuffer output = new StringBuffer();

		try {
			Inoc anInoc = new SQLInoc(this.getSQLDataSource(), this.getFormValue("inoc"));
			CryoCollection colA = new SQLCryoCollection(this.getSQLDataSource(), this.getFormValue("col"));
			CryoCollection colB = null;
			if ( ! this.getFormValue("duplicate").equals("") ) {
				colB = new SQLCryoCollection(this.getSQLDataSource(), this.getFormValue("duplicate"));
			}
			String locs[] = this.getFormValues("location");
			String date = this.getFormValue("date");
			Cryo aCryo;
			String notes = this.getFormValue("notes");
			TableRow aRow = new TableRow("<TH>ID</TH><TH>Strain ID</TH><TH>Inoc ID</TH><TH>Collection</TH><TH>Location</TH>");
			for ( int i = 0; i < locs.length; i++ ) {
				TableCell myCell = new TableCell();
				aCryo = SQLCryo.create(this.getSQLDataSource(), anInoc.getID());
				if ( aCryo.first() ) {
					aCryo.setDate(date);
					aCryo.setNotes(notes);
					colA.addCryoForLocation(aCryo, locs[i]);
					myCell.addItem(aCryo.getID());
					myCell.addItem(anInoc.getStrainID());
					myCell.addItem(anInoc.getID());
					myCell.addItem(colA.getID());
					myCell.addItem(locs[i]);
				} else {
					myCell.addItem(String.format("FAILED FOR INOC #%s", anInoc.getID()));
				}
				aRow.addItem(myCell);
			}
			if ( colB != null ) {
				for ( int i = 0; i < locs.length; i++ ) {
					TableCell myCell = new TableCell();
					aCryo = SQLCryo.create(this.getSQLDataSource(), anInoc.getID());
					if ( aCryo.first() ) {
						aCryo.setDate(date);
						aCryo.setNotes(notes);
						colB.addCryoForLocation(aCryo, locs[i]);
						myCell.addItem(aCryo.getID());
						myCell.addItem(anInoc.getStrainID());
						myCell.addItem(anInoc.getID());
						myCell.addItem(colA.getID());
						myCell.addItem(locs[i]);
					} else {
						myCell.addItem(String.format("FAILED FOR INOC #%s", anInoc.getID()));
					}
					aRow.addItem(myCell);
				}
			}
			anInoc.setFate(Inoc.FATE_CRYO);
			Table myTable = new Table(aRow);
			output.append("<FONT COLOR='green'><B>SUCCESS</B></FONT>");
			output.append(myTable.toString());
		} catch (DataException e) {
			output.append("<FONT COLOR='red'><B>SQL Error:</FONT></B> " + e.getMessage());
		}
		return output.toString();
	}

	public String addCryoForm(CryoCollection aBox) {
		StringBuffer output = new StringBuffer();
		try {
				Form myForm = new Form();
				myForm.setName("cryo");
				myForm.setAttribute("METHOD","POST");		
				myForm.addItem("<HR WIDTH='70%' />");

				TableRow tableRow = new TableRow();
				Table myTable = new Table();
				myTable.addItem(tableRow);
				myTable.setClass("species");
				myTable.setAttribute("width", "80%");
				myTable.setAttribute("align", "center");
				myForm.addItem(myTable);

				TableCell boxHeader = new TableHeader("");
				String[] boxCol = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
				boxHeader.addItem(boxCol);
				String[] boxRow = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
				TableRow rows = new TableRow(boxHeader);

				for (int r = 0; r < boxRow.length; r++ ) {
					TableCell thisRow = new TableCell("<B>" + boxRow[r] + "</B>");
					for (int c = 0; c < boxCol.length; c++ ) {
						String location = new String(boxRow[r] + boxCol[c]);
						if ( aBox.gotoLocation(r + 1, c + 1) ) {
							Cryo aCryo = aBox.getCurrentVial();
							if ( aCryo == null ) {
								thisRow.addItem("<INPUT TYPE=CHECKBOX NAME='location' VALUE='" + location + "'>");
							} else {
								Image anImage = this.getImage("filled.png");
								anImage.setAttribute("BORDER", "0");
								Anchor myLink = new Anchor(anImage);
								myLink.setLink("../cryo?id=" + aCryo.getID());
								thisRow.addItem(myLink);
							}
						}
					}
					rows.addItem(thisRow);
				}
				Table boxTable = new Table(rows);

				TableCell myCell = new TableCell("Strain:");
				Popup strainPop = StrainForm.strainPopup(SQLStrain.strains(this.getSQLDataSource()));
				strainPop.setName("strain");
				strainPop.setAttribute("onChange", "this.form.submit()");
				if ( this.getFormValue("strain") != null ) {
					strainPop.setDefault(this.getFormValue("strain"));
				}
				myCell.addItem(strainPop);
				tableRow.addItem(myCell.toString() + "<TD ROWSPAN=6>" + boxTable.toString() + "</TD>");

				Popup inocPop = new Popup();
				inocPop.setName("inoc");
				myCell = new TableCell("Inoculation: ");
				myCell.addItem(inocPop);
				tableRow.addItem(myCell);

				if ( this.getFormValue("strain") != null ) {
					SQLData aData = this.getSQLDataSource();
					PreparedStatement aSth = aData.prepareStatement("SELECT inoculation_id,date,volume FROM inoculation WHERE culture_id=? AND (fate IS NULL OR fate='cryo') ORDER BY date");
					aSth.setString(1, this.getFormValue("strain"));
					ResultSet results = aSth.executeQuery();
					results.beforeFirst();
					while (results.next()) {
						String myLabel = new String(results.getString("date") + " (" + BaseForm.formatAmount("%.0f %s", results.getFloat("volume"), "mL")
								+ ")");
						inocPop.addItemWithLabel(results.getString("inoculation_id"), myLabel);
					}
					aSth.close();
					if ( this.getFormValue("inoc") != null ) {
						inocPop.setDefault(this.getFormValue("inoc"));
					}
				}

				tableRow.addItem(this.makeFormDateRow("Presevation Date:", "date", "cryo"));
				tableRow.addItem("<TD>Notes:</TD><TD><TEXTAREA NAME='notes' COLS=40 ROWS=5></TEXTAREA></TD>");
				Popup dupPop = new Popup();
				dupPop.setName("duplicate");
				dupPop.addItemWithLabel("", "-- NONE --");
				CryoCollection colList = SQLCryoCollection.collectionsForType(this.getSQLDataSource(), "box");				
				colList.beforeFirst();
				while (colList.next()) {
					dupPop.addItemWithLabel(colList.getID(), colList.getName());
				}
				if ( this.getFormValue("duplicate") != null ) {
					dupPop.setDefault(this.getFormValue("duplicate"));
				}			
				tableRow.addItem("<TD>Duplicate Box:</TD><TD>" + dupPop.toString() + "</TD>");
				tableRow.addItem("<TD COLSPAN=2 ALIGN='CENTER'><INPUT TYPE=SUBMIT NAME=\"addVials\" VALUE=\"Add Vials\"/><INPUT TYPE=\"RESET\"/></TD>");

				output.append(myForm.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			output.append("<FONT COLOR='red'><B>SQL ERROR:</FONT></B> " + e.getMessage());
		} catch (DataException e) {
			e.printStackTrace();
			output.append("<FONT COLOR='red'><B>ERROR:</FONT></B> " + e.getMessage());
		}
		return output.toString();
	}

	public String showCryo(Cryo cryo) {
		try {
			TableCell myCell = new TableCell("Collection:");
			StringBuffer colUrl = new StringBuffer("?col=");
			colUrl.append(cryo.getCollectionID());
			Anchor myLink = new Anchor(cryo.getCollectionID());
			myLink.setLink(colUrl.toString());
			myCell.addItem(myLink);
			TableRow tableRow = new TableRow(myCell);

			myCell = new TableCell("Location:");
			myCell.addItem(cryo.getLocation());
			tableRow.addItem(myCell);
			
			SimpleDateFormat dateFormat = this.dateFormat();
			myCell = new TableCell("Preservation Date:");
			myCell.addItem(dateFormat.format(cryo.getDate()));
			tableRow.addItem(myCell);

			Inoc mySource = cryo.getSourceInoc();
			if ( mySource != null ) {
				myCell = new TableCell(String.format("<A HREF='inoc?id=%s'>Source Inoculation</A>", mySource.getID()));
			} else {
				myCell = new TableCell("NO SOURCE");
			}
			myCell.setAttribute("COLSPAN", "2");
			tableRow.addItem(myCell);

			if (cryo.isFrozen() ) {
				Form myForm = new Form(String.format("<INPUT TYPE='HIDDEN' NAME='id' VALUE='%s'/>",cryo.getID()));
				myForm.addItem("<INPUT TYPE='SUBMIT' NAME='thawVial' VALUE='Thaw Vial'/>");
				myCell = new TableCell(myForm);
				myCell.setAttribute("COLSPAN", "2");
				tableRow.addItem(myCell);				
				
				tableRow.addItem(this.makeFormTextAreaRow("Notes:", "notes", cryo.getNotes()));
			} else {
				Inoc myThaw = cryo.getThawInoc();
				myCell = new TableCell(String.format("<A HREF='inoc?id=%s'>Thaw Result</A>", myThaw.getID()));
				myCell.setAttribute("COLSPAN", "2");
				tableRow.addItem(myCell);
				
				myCell = new TableCell("Notes:");
				myCell.addItem(cryo.getNotes());
				tableRow.addItem(myCell);
			}
			
			myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='updateCryo' VALUE=\"Update\"/><INPUT TYPE=RESET />");
			myCell.setAttribute("colspan","2");
			myCell.setAttribute("align","center");
			tableRow.addItem(myCell);
			
			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			Form myForm = new Form(myTable);
			myForm.setName("cryo");
			return myForm.toString();
			
		} catch (DataException ex) {
			ex.printStackTrace();
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + ex.getMessage() + "</B></P>");		
		}
	}

	public String listCollections() {
		return this.listCollections(false);
	}
	
	public String listCollections(boolean addForm) {
		StringBuffer output = new StringBuffer();
		Table lowerTable = new Table();
		lowerTable.setAttribute("class","dashboard");
		lowerTable.setAttribute("align","center");
		lowerTable.setAttribute("width","75%");

		String[] headerData = {"1. Select Vessel", "2. Select Rack", "3. Select Box"};
		TableCell header = new TableHeader(headerData);
		TableRow tableRow = new TableRow(header);

		TableCell[] topCells = new TableCell[2];
		
		try {
			TableCell firstRow = new TableCell(this.getImage("dewar.png"));
			firstRow.setAttribute("ALIGN", "CENTER");
			firstRow.setAttribute("WIDTH", "33%");

			Popup dewarPop = this.dewarPop();
			Form popupForm = new Form(dewarPop);
			dewarPop.setName("col");
			dewarPop.setAttribute("onChange", "this.form.submit()");
			TableCell popupRow = new TableCell(popupForm);
			popupRow.setAttribute("ALIGN", "CENTER");
			popupRow.setAttribute("WIDTH", "33%");
			tableRow.addItem(firstRow);
			tableRow.addItem(popupRow);

			String content = new String();

			if ( this.hasFormValue("col") && (! this.getFormValue("col").equals("")) ) {
				CryoCollection aCol = new SQLCryoCollection(this.getSQLDataSource(), this.getFormValue("col"));
				CryoCollection dewar = null, rack = null, box = null;
				if ( aCol.first() ) {
					if ( aCol.getFormat().equals("box") ) {
						rack = aCol.getParent();
						dewar = rack.getParent();
						box = aCol;
					} else if ( aCol.getFormat().equals("rack") ) {
						dewar = aCol.getParent();
						rack = aCol;
					} else if ( aCol.getFormat().equals("dewar") ) {
						dewar = aCol;
					}
					if ( dewar != null && dewar.first() ) {
						dewarPop.setDefault(dewar.getID());
						Popup rackPop = this.childCollectionPopup(dewar);
						rackPop.setName("col");
						rackPop.setAttribute("onChange", "this.form.submit()");
						popupForm = new Form(rackPop);
						popupRow.addItem(popupForm);
						firstRow.addItem(this.getImage("all-cols.png"));
						if ( rack != null && rack.first() ) {
							rackPop.setDefault(rack.getID());
							Popup boxPop = this.childCollectionPopup(rack);
							boxPop.setName("col");
							boxPop.setAttribute("onChange", "this.form.submit()");
							firstRow.addItem(this.getImage("box.png"));
							popupForm = new Form(boxPop);
							popupRow.addItem(popupForm);
							if ( box != null && box.first() ) {
								boxPop.setDefault(box.getID());
								if ( addForm ) {
									content = this.addCryoForm(box);
								} else {
									TableCell contentCell = new TableCell(this.showBox(box));
									contentCell.addItem(this.listBox(box));
									contentCell.setAttribute("WIDTH", "50%");
									contentCell.setAttribute("ALIGN", "CENTER");
									TableRow contentRow = new TableRow(contentCell);
									Table contentTable = new Table(contentRow);
									contentTable.setAttribute("WIDTH", "100%");
									content = contentTable.toString();
								}
							} else {
								content = this.listRack(rack);
							}
						} else {
							content = this.listDewar(dewar);
						}
					} else {
						content = this.listAllDewars();
					}
				} else {
					content = "<TD COLSPAN=3 ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Cannot find cryo collection.</TD>";
				}
			} else {
				content = this.listAllDewars();
			}
			TableCell contentCell = new TableCell(content);
			contentCell.setAttribute("ALIGN", "CENTER");
			contentCell.setAttribute("COLSPAN", "3");
			tableRow.addItem(contentCell);

		} catch ( DataException e ) {
			e.printStackTrace();
			tableRow.addItem("<TD COLSPAN=3 ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></TD>");
		}
		
		tableRow.addItem(topCells);
		Table myTable = new Table(tableRow);
		myTable.setAttribute("class","list");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");

		Paragraph myP = new Paragraph(myTable);
		Form myForm = new Form(myP);
		output.append(myForm.toString());
		myP.addItem("<HR WIDTH='85%'/>");
		output.append("<P ALIGN='CENTER'>" + lowerTable + "</P>");
		return output.toString();
	}
	
	private String listRack(CryoCollection rack) {
		String[] headers = {"Rack", "Description", "Size", "Usage"};
		TableCell subHeader = new TableHeader(headers);
		subHeader.setAttribute("class","header");
		TableRow subRow = new TableRow(subHeader);
		Table subTable = new Table(subRow);
		subTable.setAttribute("class","dashboard");
		subTable.setAttribute("align","center");
		subTable.setAttribute("width","75%");
		String curClass = "odd";
		
		try {
			CryoCollection myBoxes = rack.getChildren();
			if ( myBoxes != null ) {
				myBoxes.beforeFirst();
				while ( myBoxes.next() ) {
					TableCell myCell = new TableCell(String.format("<A HREF='?col=%s'>%s</A>",myBoxes.getID(),myBoxes.getName()));
					myCell.addItem(BaseForm.shortenString(myBoxes.getNotes(), 25));
					myCell.addItem(String.format("%d X %d", myBoxes.getLength(), myBoxes.getWidth()));
					Cryo myCryos = myBoxes.getVials();
					if ( myCryos != null && myCryos.last() ) 
						myCell.addItem( myCryos.count() );
					else 
						myCell.addItem("0");
					TableRow aRow = new TableRow(myCell);
					aRow.setClass(curClass);
					aRow.setAttribute("align", "center");
					subTable.addItem(aRow);
					if ( curClass.equals("odd") )
						curClass = "even";
					else
						curClass = "odd";
				}
			}
		} catch (DataException ex) {
           subRow.addItem("<TD ALIGN='CENTER' COLSPAN='4'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + ex.getMessage() + "</B></TD>");
           ex.printStackTrace();
        }			
		return subTable.toString();
	}
	

	private String listDewar(CryoCollection dewar) {
		String[] headers = {"Rack", "Description", "Size", "Usage"};
		TableCell subHeader = new TableHeader(headers);
		subHeader.setAttribute("class","header");
		TableRow subRow = new TableRow(subHeader);
		Table subTable = new Table(subRow);
		subTable.setAttribute("class","dashboard");
		subTable.setAttribute("align","center");
		subTable.setAttribute("width","75%");
		
		try {
			CryoCollection myRacks = dewar.getChildren();
			String curClass = "odd";
			if ( myRacks != null ) {
				myRacks.beforeFirst();
				while ( myRacks.next() ) {
					TableCell myCell = new TableCell(String.format("<A HREF='?col=%s'>%s</A>",myRacks.getID(),myRacks.getName()));
					myCell.addItem(BaseForm.shortenString(myRacks.getNotes(), 25));
					myCell.addItem(String.valueOf(myRacks.getLength()));
					CryoCollection myBoxen = myRacks.getChildren();
					if ( myBoxen != null && myBoxen.last() )
						myCell.addItem(String.valueOf( myBoxen.count() ));
					else 
						myCell.addItem("0");
					TableRow aRow = new TableRow(myCell);
					aRow.setClass(curClass);
					aRow.setAttribute("align", "center");
					subTable.addItem(aRow);
					if ( curClass.equals("odd") )
						curClass = "even";
					else
						curClass = "odd";
				}
			}
		} catch (DataException ex) {
           subRow.addItem("<TD ALIGN='CENTER' COLSPAN='4'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + ex.getMessage() + "</B></TD>");
           ex.printStackTrace();
        }			
		return subTable.toString();
	}


	private String listAllDewars() {
		String[] headers = {"Dewar", "Description", "Size", "Usage"};
		TableCell subHeader = new TableHeader(headers);
		subHeader.setAttribute("class","header");
		TableRow subRow = new TableRow(subHeader);
		Table subTable = new Table(subRow);
		subTable.setAttribute("class","dashboard");
		subTable.setAttribute("align","center");
		subTable.setAttribute("width","75%");

		try {
			CryoCollection dewar = new SQLCryoCollection(this.getSQLDataSource());
			if ( dewar.loadDewars() ) {
				String curClass = "odd";
				dewar.beforeFirst();
				while (dewar.next() ) {
					TableCell myCell = new TableCell(String.format("<A HREF='?col=%s'>%s</A>",dewar.getID(),dewar.getName()));
					myCell.addItem(BaseForm.shortenString(dewar.getNotes(), 25));
					myCell.addItem(String.valueOf(dewar.getLength()));
					CryoCollection myKids = dewar.getChildren();
					if ( myKids != null && myKids.last() )
						myCell.addItem(String.valueOf(myKids.count()));
					else 
						myCell.addItem("0");
					TableRow aRow = new TableRow(myCell);
					aRow.setClass(curClass);
					aRow.setAttribute("align", "center");
					subTable.addItem(aRow);
					if ( curClass.equals("odd") )
						curClass = "even";
					else
						curClass = "odd";
				}
			}
		} catch (DataException ex) {
			subRow.addItem("<TD ALIGN='CENTER' COLSPAN='4'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + ex.getMessage() + "</B></TD>");
			ex.printStackTrace();
        }			
		return subTable.toString();
	}
	
	private String listBox(CryoCollection box) {
		String curClass = "odd";
		String[] vialHeaders = {"Strain", "Date", "Location"};
		TableCell vialHead = new TableHeader(vialHeaders);
		vialHead.setAttribute("class","header");
		TableRow vialRow = new TableRow(vialHead);
		Table vialTable = new Table(vialRow);
		vialTable.setAttribute("class","dashboard");
		vialTable.setAttribute("align","center");
		vialTable.setAttribute("width","75%");
		
		try {
			Inoc sources = box.getSourceInocs();
			if ( sources != null ) {
				sources.beforeFirst();
				SimpleDateFormat dateFormat = this.dateFormat();
				while (sources.next()) {
					Cryo myCryos = box.getCryosForInoculation(sources);
					myCryos.beforeFirst();
					boolean frozen = false;
					FINDFROZEN: while ( myCryos.next() ) {
						if ( myCryos.isFrozen() ) { 
							frozen = true;
							break FINDFROZEN;
						}
					}
					if ( frozen ) {
						TableCell myCell = new TableCell();
						StringBuffer colUrl = new StringBuffer("strain?id=");
						colUrl.append(myCryos.getCultureID());
						Anchor myLink = new Anchor();
						myCell.addItem(myLink.quickLink(colUrl.toString(),myCryos.getCultureID()));
						myCell.addItem(dateFormat.format(myCryos.getDate()));
						String start = myCryos.getLocation();
						myCryos.last();
						String end = myCryos.getLocation();
						myCell.addItem(String.format("%s - %s", start, end));
						TableRow aRow = new TableRow(myCell);
						aRow.setClass(curClass);
						aRow.setAttribute("align", "center");
						vialTable.addItem(aRow);
						if ( curClass.equals("odd") ) {
							curClass = "even";
						} else {
							curClass = "odd";
						}
					}
				}
			}
			vialTable.addItem("<TR><TD COLSPAN=3 ALIGN='CENTER'><A HREF='cryo/add?col=" + 
					box.getID() + "'>Add Vials</A></TD></TR>");
		} catch (DataException e) {
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
		}
		return vialTable.toString();
	}

	private String showBox(CryoCollection myCol) {
		try {
			TableCell boxHeader = new TableHeader("");
			for ( int i = 1; i <= myCol.getWidth(); i++ ) {
				boxHeader.addItem(Integer.toString(i));
			}

			TableRow row = new TableRow(boxHeader);
//			String tdWidth = Float.toString(100.0f / myCol.getWidth()) + "%";
			myCol.beforeFirstRow();
			while ( myCol.nextRow() ) {
				myCol.beforeFirstColumn();
				TableCell thisRow = new TableCell();
			//	thisRow.setAttribute("WIDTH", tdWidth);
				thisRow.setAttribute("ALIGN", "CENTER");
				thisRow.setAttribute("VALIGN", "TOP");
				while ( myCol.nextColumn() ) {
					Cryo aCryo = myCol.getCurrentVial();
					if ( aCryo == null ) {
						Image anImage = this.getImage("empty.png");
						anImage.setAttribute("BORDER", "0");
						thisRow.addItem(anImage);
					} else {
						Image anImage = this.getImage("filled.png");
						anImage.setAttribute("BORDER", "0");
						thisRow.addItem(String.format("<A HREF='cryo?id=%s'>%s</A>", aCryo.getID(), anImage.toString()));
					}
				}					
				row.addItem(String.format("<TH WIDTH=3>%s</TH>%s", myCol.currentRowAlpha(), thisRow.toString()));
			}
			Table myTable = new Table(row);
			return myTable.toString();
		} catch ( DataException e ) {
			e.printStackTrace();
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
		}
	}
	

	private Popup dewarPop() throws DataException {
		Popup aPop = new Popup();
		aPop.addItemWithLabel("","---Select A Dewar---");
		CryoCollection dewars = new SQLCryoCollection(this.getSQLDataSource());
		if ( dewars.loadDewars() ) {
			dewars.beforeFirst();
			while ( dewars.next() ) {
				aPop.addItemWithLabel(dewars.getID(), dewars.getName());
			}
		}
		return aPop;
	}

	private Popup childCollectionPopup(CryoCollection aCol) throws DataException {
		Popup aPop = new Popup();
		String label = "---Select A Collection---";
		if ( aCol.getFormat().equals("dewar") ) 
			label = "---Select A Rack---";
		else if (aCol.getFormat().equals("rack") )
			label = "---Select A Box---";
		aPop.addItemWithLabel("",label);
		CryoCollection myKids = aCol.getChildren();
		if ( myKids != null ) {
			myKids.beforeFirst();
			while ( myKids.next() ) {
				aPop.addItemWithLabel(myKids.getID(), myKids.getName());
			}
		}
		return aPop;
	}

}
