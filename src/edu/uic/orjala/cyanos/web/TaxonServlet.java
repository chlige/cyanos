/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.uic.orjala.cyanos.web.forms.StrainForm;
import edu.uic.orjala.cyanos.web.html.HtmlList;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.StyledText;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Taxon;
import edu.uic.orjala.cyanos.sql.SQLTaxon;

/**
 * @author George Chlipala
 *
 */
public class TaxonServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3813954687079987698L;
	
	private static final String[] levelMap = { "", "kingdom", "phylum", "class", "ord", "family", "genus"};

	private static final String HELP_MODULE = "strain";
	
	public void display(CyanosWrapper aWrap) throws Exception {
		PrintWriter out;
		String module = aWrap.getRequest().getPathInfo();

		if ( "/export".equals(module) ) {
			out = aWrap.getWriter();
			aWrap.setContentType("text/plain");
			out.print(this.exportTaxa(aWrap));
			out.flush();
			return;

		}
		
		out = aWrap.startHTMLDoc("Taxon Browser");
		
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Taxon Browser");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		out.println(head);
		
		out.println(this.taxaBrowser(aWrap));
		
		aWrap.finishHTMLDoc();
	}
	
	
	private String exportTaxa(CyanosWrapper aWrap) {
		try {
			Taxon aTaxon;
			if ( aWrap.hasFormValue("level") && aWrap.hasFormValue("value") ) {
				int level = Integer.parseInt(aWrap.getFormValue("level"));
				aTaxon = SQLTaxon.allTaxaForLevel(aWrap.getSQLDataSource(), level, aWrap.getFormValue("value"));
			} else {
				aTaxon = SQLTaxon.allTaxa(aWrap.getSQLDataSource());
			}
			aTaxon.beforeFirst();
			List<List> output = new ArrayList<List>();
			List<String> aRow = new ArrayList<String>();
			aRow.add("Kingdom");
			aRow.add("Phylum");
			aRow.add("Class");
			aRow.add("Order");
			aRow.add("Family");
			aRow.add("Genus");
			output.add(aRow);
			while ( aTaxon.next() ) {
				aRow = new ArrayList<String>();
				aRow.add(aTaxon.getKingdom());
				aRow.add(aTaxon.getPhylum());
				aRow.add(aTaxon.getTaxonClass());
				aRow.add(aTaxon.getOrder());
				aRow.add(aTaxon.getGenus());
				output.add(aRow);
			}
			return this.delimOutput(output, ",");
		} catch (DataException e) {
			e.printStackTrace();
			return "ERROR: " + e.getMessage();
		}
	}


	protected String taxaList(CyanosWrapper aWrap) throws DataException {
		StringBuffer output = new StringBuffer();
//		String baseURL = new String();

		String headers[] = {"Kingdom", "Phylum", "Class", "Order", "Family", "Genus"};
		TableCell taxCell = new TableHeader(headers);
		TableRow taxRow = new TableRow(taxCell);
		Table taxTable = new Table(taxRow);

		Taxon aTaxon = new SQLTaxon(aWrap.getSQLDataSource());
		
		int level = Taxon.NO_LEVEL;
		if ( aWrap.hasFormValue("genus") ) {
			aTaxon = new SQLTaxon(aWrap.getSQLDataSource(), aWrap.getFormValue("genus"));
			level = Taxon.GENUS;
		} else {
			String levels[] = {"family", "ord", "class", "phylum", "kingdom"};
			String whereValue = "";
			int taxonLevels[] = { Taxon.FAMILY, Taxon.ORDER, Taxon.CLASS, Taxon.PHYLUM, Taxon.KINGDOM };
			FIND_LEVEL: for ( int i = 0; i < taxonLevels.length; i++ ) {
				if ( aWrap.hasFormValue(levels[i]) ) {
					level = taxonLevels[i];
					whereValue = aWrap.getFormValue(levels[i]);
					break FIND_LEVEL;
				}
			}
			if ( level == Taxon.NO_LEVEL ) {
				aTaxon.loadKingdoms();
			} else {
				aTaxon.loadForTaxonLevel(level, level, whereValue);
			}
		} 
		
		Taxon popTaxon = new SQLTaxon(aWrap.getSQLDataSource());
		popTaxon.loadKingdoms();

		if ( aTaxon.first() ) {

			HtmlList taxaList = new HtmlList();
			taxaList.unordered();
			for ( int i = Taxon.PHYLUM; i <= level; i++ ) {

			}

			output.append("<P ALIGN='CENTER'>" + taxTable.toString() + "</P>");
			if ( "1".equals(aWrap.getFormValue("show")) ) {
//				output.append(this.listSpeciesTable(baseURL, queryString, req.getParameter("sort"), req.getParameter("dir")));
			}
		}
		return output.toString();
	}

	protected HtmlList displayTaxa(CyanosWrapper aWrap, Taxon myTaxon, int thisLevel) throws DataException {
		HtmlList myList = new HtmlList();
		myList.unordered();
		Taxon listTaxon = new SQLTaxon(aWrap.getSQLDataSource());
		int nextLevel = thisLevel + 1;
		String taxonValue = myTaxon.getValueForLevel(thisLevel);
//		String[] levels = new String[Taxon.NUMBER_OF_LEVELS];
		if ( thisLevel < Taxon.GENUS ) {
			if ( listTaxon.loadForTaxonLevel(nextLevel, thisLevel, taxonValue) ) {

			}
		}


		return myList;
	}
	
	protected String taxaBrowser(CyanosWrapper aWrap) 
	throws DataException 
	{
		StringBuffer output = new StringBuffer();
//		String baseURL = new String();

		String headers[] = {"Kingdom", "Phylum", "Class", "Order", "Family", "Genus"};
		TableCell taxCell = new TableHeader(headers);
		TableRow taxRow = new TableRow(taxCell);
		Table taxTable = new Table(taxRow);

		Taxon aTaxon = new SQLTaxon(aWrap.getSQLDataSource());
		
		StringBuffer exportString = new StringBuffer("<P ALIGN='CENTER'><A HREF='taxabrowser/export");
		
		int level = Taxon.NO_LEVEL;
		if ( aWrap.hasFormValue("genus") && (! aWrap.getFormValue("genus").equals("")) ) {
			aTaxon = new SQLTaxon(aWrap.getSQLDataSource(), aWrap.getFormValue("genus"));
			level = Taxon.GENUS;
		} else {
			String levels[] = {"family", "ord", "class", "phylum", "kingdom"};
			String whereValue = "";
			int taxonLevels[] = { Taxon.FAMILY, Taxon.ORDER, Taxon.CLASS, Taxon.PHYLUM, Taxon.KINGDOM };
			FIND_LEVEL: for ( int i = 0; i < taxonLevels.length; i++ ) {
				if ( aWrap.hasFormValue(levels[i]) ) {
					level = taxonLevels[i];
					whereValue = aWrap.getFormValue(levels[i]);
					if ( whereValue.startsWith("<") ) {
						if (level > Taxon.KINGDOM ) {
							level--;
							whereValue = aWrap.getFormValue(levels[i]).substring(1);
						} else {
							level = Taxon.NO_LEVEL;
						}
					}
					break FIND_LEVEL;
				}
			}
			if ( level >= Taxon.KINGDOM ) {
//				aTaxon.loadKingdoms();
//			} else {
				aTaxon.loadForTaxonLevel(level, level, whereValue);
				exportString.append(String.format("?level=%d&value=%s", level, whereValue));
			}
		} 
		
		Taxon popTaxon = new SQLTaxon(aWrap.getSQLDataSource());
		popTaxon.loadKingdoms();
		Popup[] aPop;
		if ( level >= Taxon.KINGDOM ) {
			if ( level == Taxon.GENUS ) {
				aPop = new Popup[level + 1];
			} else {
				aPop = new Popup[level + 2];
			}
		} else {
			aPop = new Popup[Taxon.KINGDOM + 1];
		}
		aPop[Taxon.KINGDOM] = new Popup();
		aPop[Taxon.KINGDOM].addItem("");
		aPop[Taxon.KINGDOM].setName(levelMap[Taxon.KINGDOM]);
		popTaxon.beforeFirst();
		while ( popTaxon.next() ) {
			aPop[Taxon.KINGDOM].addItem(popTaxon.getKingdom());
		}
		aPop[Taxon.KINGDOM].setAttribute("onChange", "this.form.submit()");

		if ( aTaxon.first() ) {
			aPop[Taxon.KINGDOM].setDefault(aTaxon.getKingdom());
			
			for ( int i = Taxon.PHYLUM; i <= level; i++ ) {
				aPop[i] = new Popup();
				aPop[i].addItemWithLabel("<" + aTaxon.getValueForLevel(i - 1), "");
				popTaxon.loadForTaxonLevel(i, i - 1, aTaxon.getValueForLevel(i - 1));
				popTaxon.beforeFirst();
				while (popTaxon.next()) {
					aPop[i].addItem(popTaxon.getValueForLevel(i));
				}
				aPop[i].setDefault(aTaxon.getValueForLevel(i));
				aPop[i].setName(levelMap[i]);
				aPop[i].setAttribute("onChange", "this.form.submit()");
			}
			
			if ( level > Taxon.NO_LEVEL && level < Taxon.GENUS ) {
				aPop[level + 1] = new Popup();
				aPop[level + 1].setName(levelMap[level + 1]);
				aPop[level + 1].setAttribute("onChange", "this.form.submit()");
				popTaxon.loadForTaxonLevel(level + 1, level, aTaxon.getValueForLevel(level));
				aPop[level + 1].addItemWithLabel("<" + aTaxon.getValueForLevel(level), "");
				popTaxon.beforeFirst();
				while (popTaxon.next()) {
					aPop[level + 1].addItem(popTaxon.getValueForLevel(level + 1));
				}
			}
		} else {
			aPop[Taxon.KINGDOM].setDefault("");
		}

		taxCell = new TableCell();
		taxCell.setAttribute("WIDTH", "16%");
		taxCell.setAttribute("ALIGN", "CENTER");

		for ( int i = Taxon.KINGDOM; i <= Taxon.GENUS; i++ ) {
			if (aPop.length > i && aPop[i] != null) {
				taxCell.addItem("<FORM ACTION='taxabrowser'>" + aPop[i].toString() + "</FORM>");				
			} else {
				taxCell.addItem("");
			}
		}

		exportString.append("'>Export Taxa</A></P>");
		taxRow.addItem(taxCell);	
		taxTable.setAttribute("WIDTH", "80%");
		output.append("<P ALIGN='CENTER'>");
		output.append(taxTable.toString());
		output.append(exportString.toString());
		output.append("</P>");
		StrainForm strainForm = new StrainForm(aWrap);
		output.append(strainForm.listSpeciesTable(aTaxon.getStrainsForTaxon()));
		return output.toString();
	}

	@Override
	protected String getHelpModule() {
		return HELP_MODULE;
	}
}
