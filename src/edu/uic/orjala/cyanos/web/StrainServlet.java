//
//  StrainServlet.java
//  Cyanos
//
//  Created by George Chlipala on 5/7/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.forms.AssayForm;
import edu.uic.orjala.cyanos.web.forms.CompoundForm;
import edu.uic.orjala.cyanos.web.forms.CryoForm;
import edu.uic.orjala.cyanos.web.forms.HarvestForm;
import edu.uic.orjala.cyanos.web.forms.SampleForm;
import edu.uic.orjala.cyanos.web.forms.SeparationForm;
import edu.uic.orjala.cyanos.web.forms.StrainForm;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;

/**
 * @author George Chlipala
 * @version 2.61124
 *
 */
public class StrainServlet extends ServletObject {
	
	 /**
	  * 
	  */
	private static final long serialVersionUID = 1L;
	private static final String INOC_DIV_TITLE = "Inoculations";
	private static final String INOC_DIV_ID = "strainInocs";
	
	private static final String COL_DIV_TITLE = "Field Collections";
	private static final String COL_DIV_ID = "fieldCols";
	
	private static final String PHOTO_DIV_ID = "photos";
	private static final String PHOTO_DIV_TITLE = "Photos";
	
	private static final String COMPOUND_DIV_ID = "compounds";
	private static final String COMPOUND_DIV_TITLE = "Compounds";
	
	private static final String HELP_MODULE = "strain";
	

	public void display(CyanosWrapper aWrap) throws Exception {
		PrintWriter out;
		StrainForm strainForm = new StrainForm(aWrap);
		
		
		if ( aWrap.hasFormValue("div") ) {
			aWrap.setContentType("text/html");
			out = aWrap.getWriter();
			Strain thisStrain = new SQLStrain(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
			String divTag = aWrap.getFormValue("div");
			if ( thisStrain.first() ) {
				if ( divTag.equals(AssayForm.DIV_ID) ) {
					AssayForm aForm = new AssayForm(aWrap);
					out.println(aForm.assayListForStrain(thisStrain));
				} else if ( divTag.equals(HarvestForm.DIV_ID) ) {
					HarvestForm aForm = new HarvestForm(aWrap);
					out.println(aForm.harvestList(thisStrain));
				} else if ( divTag.equals(SeparationForm.DIV_ID) ) {
					SeparationForm sepForm = new SeparationForm(aWrap);
					out.println(sepForm.separationList(thisStrain));
				} else if ( divTag.equals(SampleForm.DIV_ID) ) {
					SampleForm aForm = new SampleForm(aWrap);
					out.println(aForm.sampleListContent(thisStrain.getSamples(), true));
				} else if ( divTag.equals(INOC_DIV_ID) ) {
					out.println(strainForm.inoculationList(thisStrain));
				} else if ( divTag.equals(COL_DIV_ID) ) {
					out.println(strainForm.collectionList(thisStrain));
				} else if ( divTag.equals(CryoForm.DIV_ID) ) {
					CryoForm aForm = new CryoForm(aWrap);
					out.println(aForm.cryoList(thisStrain));
				} else if ( divTag.equals(PHOTO_DIV_ID) ) {
					out.println(strainForm.photoAlbum(thisStrain));
				} else if ( divTag.equals(StrainForm.PHOTO_FORM)) {
					out.println(strainForm.photoForm(thisStrain, 3));
				} else if ( divTag.equals(COMPOUND_DIV_ID) ) {
					CompoundForm aForm = new CompoundForm(aWrap);
					out.println(aForm.listCompounds(SQLCompound.compoundsForStrain(aWrap.getSQLDataSource(), thisStrain), true));
				}
			}
//			this.closeSQL();
//			out.close();
			out.flush();
			return;
		}
		
		if ( aWrap.hasFormValue("id") ) {
			StrainForm aForm = new StrainForm(aWrap);
			out = aWrap.startHTMLDoc(String.format("Strain: %s", aWrap.getFormValue("id")));
			Strain thisStrain = new SQLStrain(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
			if ( aWrap.hasFormValue("removeAction") ) {
				if ( thisStrain.isAllowed(Role.DELETE) ) {
					Paragraph head = new Paragraph();
					head.setAlign("CENTER");
					StyledText title = new StyledText("Remove Strain");
					title.setSize("+3");
					head.addItem(title);
					head.addItem("<HR WIDTH=\"85%\"/>");
					StyledText subtitle = new StyledText("<CENTER>");
					subtitle.setSize("+2");
					subtitle.addItalicString("Strain ID: " + thisStrain.getID());
					subtitle.addString("</CENTER>");
					head.addItem(subtitle);
					out.println(head);	
					out.println(aForm.killStrain());
				} else {
					out.println("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ACTION NOT ALLOWED:</FONT> Insufficient permission</B></P>");
				}
			} else {
				if ( thisStrain.first() ) {
					aForm.updateStrain(thisStrain);
					
					Paragraph head = new Paragraph();
					head.setAlign("CENTER");
					StyledText title = new StyledText(String.format("%s <I>%s</I>", thisStrain.getID(), thisStrain.getName()));
					title.setSize("+3");
					head.addItem(title);
					head.addItem("<HR WIDTH=\"85%\"/>");
					out.println(head);

					
					if ( thisStrain.isAllowed(Role.WRITE) ) {
						boolean readOnly = ( thisStrain.wasRemoved() || thisStrain.statusIs(Strain.REMOVED_STATUS) );
						if ( readOnly ) {
							out.println(aForm.showSpeciesText(thisStrain));					
						} else {
							Div strainDiv = aForm.strainViewDiv(thisStrain);
							strainDiv.setClass("main");
							out.println(strainDiv.toString());
							out.println(String.format("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' NAME='addQueue' onClick='queueForm(\"strain\", \"%s\");'>Add to a Work Queue</BUTTON></P>", thisStrain.getID()));
						}

						out.println(aForm.loadableDiv(PHOTO_DIV_ID, PHOTO_DIV_TITLE));
						try {
							String status = thisStrain.getStatus();
							if ( status != null && status.equals(Strain.FIELD_HARVEST_STATUS) ) {
								out.println(aForm.loadableDiv(COL_DIV_ID, COL_DIV_TITLE).toString());
							} else {
								Collection list = thisStrain.getFieldCollections();
								out.println(aForm.loadableDiv(INOC_DIV_ID, INOC_DIV_TITLE).toString());	
								if ( list != null && list.first() ) 
									out.println(aForm.loadableDiv(COL_DIV_ID, COL_DIV_TITLE).toString());									
								out.println(aForm.loadableDiv(CryoForm.DIV_ID, CryoForm.DIV_TITLE));
							}
						} catch (DataException e) {
							out.println(aWrap.handleException(e));
						}
						
						out.println(aForm.loadableDiv(HarvestForm.DIV_ID, HarvestForm.DIV_TITLE));
						out.println(aForm.loadableDiv(SampleForm.DIV_ID, SampleForm.DIV_TITLE));
						out.println(aForm.loadableDiv(SeparationForm.DIV_ID, SeparationForm.DIV_TITLE));
						out.println(aForm.loadableDiv(AssayForm.DIV_ID, AssayForm.DIV_TITLE));
						out.println(aForm.loadableDiv(COMPOUND_DIV_ID, COMPOUND_DIV_TITLE));
												
					} else if ( ! aWrap.hasFormValue("div") ) {
						out.println(aForm.showSpeciesText(thisStrain));
						out.println(aForm.loadableDiv(PHOTO_DIV_ID, PHOTO_DIV_TITLE));
					}
				} else {
					out.println("<P ALIGN='CENTER'><B><FONT SIZE='+1'><FONT COLOR='red'>ERROR:</FONT> Requested strain does not exist in the database.</FONT></B></P>");
					Paragraph head = new Paragraph("<HR WIDTH='85%'/>");
					head.setAlign("CENTER");
					out.println(head);
					out.println(aForm.listSpecies());
				}
				//		thisStrain.close();
			}
		} else if ( "add".equals(aWrap.getFormValue("action")) ) {
			out = aWrap.startHTMLDoc("Add Strain");
			if ( aWrap.getUser().isAllowed(User.CULTURE_ROLE, User.NULL_PROJECT, Role.CREATE) ) {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Add Strain");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head);
				StrainForm aForm = new StrainForm(aWrap);
				out.println(aForm.addStrain());			
			} else {
				aWrap.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
			}
		} else {
			out = aWrap.startHTMLDoc("Strain List");
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Strain List");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head);
			StrainForm myForm = new StrainForm(aWrap);
			out.println(myForm.queryForm());
		}
		aWrap.finishHTMLDoc();
    }


	@Override
	protected String getHelpModule() {
		return HELP_MODULE;
	}
		
	
}
