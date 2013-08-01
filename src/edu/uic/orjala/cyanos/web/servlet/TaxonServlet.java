/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.Taxon;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.sql.SQLTaxon;

/**
 * @author George Chlipala
 *
 */
public class TaxonServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	/**
	 * 
	 */

	public final static String PARAM_LEVEL = "taxa_level";
	public final static String PARAM_NAME = "taxa_name";

	public static final String HELP_MODULE = "strain";
	public static final String DELIM = ",";
	
	public static final String TAXON_OBJECT = "taxonObj";
	
	public static final String PARAM_EXPORT = "export";

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doGet(req, res);
		this.handleRequest(req, res);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doPost(req, res);
		this.handleRequest(req, res);
	}

	public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		if ( req.getParameter(PARAM_EXPORT) != null ) {
			this.exportTaxa(req, res);
		}
		

		try {
			Taxon taxon = this.getTaxon(req);
			if ( req.getParameter(PARAM_NAME) != null && taxon.first() )
				req.setAttribute(StrainServlet.SEARCHRESULTS_ATTR, this.getStrains(req, taxon));

			if ( req.getParameter("div") != null ) {
				res.setContentType("text/html; charset=UTF-8");
				String divTag = req.getParameter("div");
				if ( divTag.equals(StrainServlet.SEARCH_DIV_ID) ) {
					RequestDispatcher disp = getServletContext().getRequestDispatcher("/includes/taxon-strain-list.jsp");
					disp.forward(req, res);
				}
			} else {
				req.setAttribute(TAXON_OBJECT, taxon);

				RequestDispatcher disp = this.getServletContext().getRequestDispatcher("/taxon.jsp");
				disp.forward(req, res);
			}
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}

	}
	
	private Strain getStrains(HttpServletRequest req, Taxon taxon) throws DataException, SQLException {
		String sortString = req.getParameter(StrainServlet.PARAM_SORT_FIELD);
		
		if ( sortString == null )
			sortString = SQLStrain.SORT_ID;
		else if ( sortString.equals(SQLStrain.ID_COLUMN) )
			sortString = SQLStrain.SORT_ID;
			
		String sortDir = req.getParameter(StrainServlet.PARAM_SORT_DIR);

		if ( sortDir == null ) 
			sortDir = SQLStrain.ASCENDING_SORT;

		return SQLTaxon.getStrains(this.getSQLData(req), taxon.getName(), sortString, sortDir);
	}

	private Taxon getTaxon(HttpServletRequest req) throws DataException, SQLException {
		String name = req.getParameter(PARAM_NAME);
		
		if ( name != null ) {
			return SQLTaxon.load(this.getSQLData(req), name);
		} else {
			return SQLTaxon.taxaForLevel(this.getSQLData(req), Taxon.LEVEL_KINGDOM);
		}
	}

	private void exportTaxa(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		PrintWriter out = res.getWriter();
		res.setContentType("text/plain");

		try {
			PreparedStatement sth;
			if ( req.getParameter(PARAM_NAME) != null) {
				sth = this.getSQLData(req).prepareStatement(SQLTaxon.SQL_LOAD_ALL_LEVEL);
				sth.setString(1, req.getParameter(PARAM_NAME));
			} else {
				sth = this.getSQLData(req).prepareStatement(SQLTaxon.SQL_LOAD_ALL);
			}

			ResultSet results = sth.executeQuery();
			
			out.print("Level");
			out.print(DELIM);
			out.print("Name");
			out.print(DELIM);
			out.println("Parent");
			results.beforeFirst();
			
			while ( results.next() ) {
				out.print(quoteString(results.getString(2),DELIM));
				out.print(DELIM);
				out.print(quoteString(results.getString(1),DELIM));
				out.print(DELIM);
				out.println(quoteString(results.getString(3),DELIM));
			}
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		out.flush();
	}
	
	public static void printTaxonLink(JspWriter out, Taxon taxon) throws IOException, DataException {
		out.print("<b>");
		String level = taxon.getLevel();
		level = level.substring(0, 1).toUpperCase()
				+ level.substring(1).toLowerCase();
		out.print(level);
		out.print("</b>: <i><a href=\"taxabrowser?");
		out.print(TaxonServlet.PARAM_NAME);
		out.print("=");
		String name = taxon.getName();
		out.print(name);
		out.print("\">");
		out.print(name);
		out.println("</a></i>");
	}

	public static void printTaxon(JspWriter out, Taxon taxon) throws IOException, DataException {
		out.print("<b>");
		String level = taxon.getLevel();
		level = level.substring(0, 1).toUpperCase()
				+ level.substring(1).toLowerCase();
		out.print(level);
		out.print(": <i>");
		out.print(taxon.getName());
		out.println("</i></b>");
	}

	

/*
	protected String taxaList(CyanosWrapper aWrap) throws DataException {
		StringBuffer output = new StringBuffer();
		//		String baseURL = new String();

		String headers[] = {"Kingdom", "Phylum", "Class", "Order", "Family", "Genus"};
		TableCell taxCell = new TableHeader(headers);
		TableRow taxRow = new TableRow(taxCell);
		Table taxTable = new Table(taxRow);

		Taxon aTaxon = new SQLTaxon(aWrap.getSQLDataSource());

		int level = TaxonFlat.NO_LEVEL;
		if ( aWrap.hasFormValue("genus") ) {
			aTaxon = new SQLTaxonFlat(aWrap.getSQLDataSource(), aWrap.getFormValue("genus"));
			level = TaxonFlat.GENUS;
		} else {
			String levels[] = {"family", "ord", "class", "phylum", "kingdom"};
			String whereValue = "";
			int taxonLevels[] = { TaxonFlat.FAMILY, TaxonFlat.ORDER, TaxonFlat.CLASS, TaxonFlat.PHYLUM, TaxonFlat.KINGDOM };
			FIND_LEVEL: for ( int i = 0; i < taxonLevels.length; i++ ) {
				if ( aWrap.hasFormValue(levels[i]) ) {
					level = taxonLevels[i];
					whereValue = aWrap.getFormValue(levels[i]);
					break FIND_LEVEL;
				}
			}
			if ( level == TaxonFlat.NO_LEVEL ) {
				aTaxon.loadKingdoms();
			} else {
				aTaxon.loadForTaxonLevel(level, level, whereValue);
			}
		} 

		TaxonFlat popTaxon = new SQLTaxonFlat(aWrap.getSQLDataSource());
		popTaxon.loadKingdoms();

		if ( aTaxon.first() ) {

			HtmlList taxaList = new HtmlList();
			taxaList.unordered();
			for ( int i = TaxonFlat.PHYLUM; i <= level; i++ ) {

			}

			output.append("<P ALIGN='CENTER'>" + taxTable.toString() + "</P>");
			if ( "1".equals(aWrap.getFormValue("show")) ) {
				//				output.append(this.listSpeciesTable(baseURL, queryString, req.getParameter("sort"), req.getParameter("dir")));
			}
		}
		return output.toString();
	}

*/
	
	/*
	protected HtmlList displayTaxa(CyanosWrapper aWrap, TaxonFlat myTaxon, int thisLevel) throws DataException {
		HtmlList myList = new HtmlList();
		myList.unordered();
		TaxonFlat listTaxon = new SQLTaxonFlat(aWrap.getSQLDataSource());
		int nextLevel = thisLevel + 1;
		String taxonValue = myTaxon.getValueForLevel(thisLevel);
		//		String[] levels = new String[Taxon.NUMBER_OF_LEVELS];
		if ( thisLevel < TaxonFlat.GENUS ) {
			if ( listTaxon.loadForTaxonLevel(nextLevel, thisLevel, taxonValue) ) {

			}
		}


		return myList;
	}
	*/
	
/*
	protected String taxaBrowser(CyanosWrapper aWrap) 
			throws DataException 
			{
		StringBuffer output = new StringBuffer();
		//	String baseURL = new String();

		String headers[] = {"Kingdom", "Phylum", "Class", "Order", "Family", "Genus"};
		TableCell taxCell = new TableHeader(headers);
		TableRow taxRow = new TableRow(taxCell);
		Table taxTable = new Table(taxRow);

		TaxonFlat aTaxon = new SQLTaxonFlat(aWrap.getSQLDataSource());

		StringBuffer exportString = new StringBuffer("<P ALIGN='CENTER'><A HREF='taxabrowser/export");

		int level = TaxonFlat.NO_LEVEL;
		if ( aWrap.hasFormValue("genus") && (! aWrap.getFormValue("genus").equals("")) ) {
			String genus = aWrap.getFormValue("genus");
			if ( genus.startsWith("<") ) {
				aTaxon.loadForTaxonLevel(TaxonFlat.FAMILY, TaxonFlat.FAMILY, genus.substring(1));
				level = TaxonFlat.FAMILY;
			} else {
				aTaxon = new SQLTaxonFlat(aWrap.getSQLDataSource(), aWrap.getFormValue("genus"));
				level = TaxonFlat.GENUS;
			}
		} else {
			String levels[] = {"family", "ord", "class", "phylum", "kingdom"};
			String whereValue = "";
			int taxonLevels[] = { TaxonFlat.FAMILY, TaxonFlat.ORDER, TaxonFlat.CLASS, TaxonFlat.PHYLUM, TaxonFlat.KINGDOM };
			FIND_LEVEL: for ( int i = 0; i < taxonLevels.length; i++ ) {
				if ( aWrap.hasFormValue(levels[i]) ) {
					level = taxonLevels[i];
					whereValue = aWrap.getFormValue(levels[i]);
					if ( whereValue.startsWith("<") ) {
						if (level > TaxonFlat.KINGDOM ) {
							level--;
							whereValue = aWrap.getFormValue(levels[i]).substring(1);
						} else {
							level = TaxonFlat.NO_LEVEL;
						}
					}
					break FIND_LEVEL;
				}
			}
			if ( level >= TaxonFlat.KINGDOM ) {
				//	aTaxon.loadKingdoms();
				//	} else {
				aTaxon.loadForTaxonLevel(level, level, whereValue);
				exportString.append(String.format("?level=%d&value=%s", level, whereValue));
			}
		} 

		TaxonFlat popTaxon = new SQLTaxonFlat(aWrap.getSQLDataSource());
		popTaxon.loadKingdoms();
		Popup[] aPop;
		if ( level >= TaxonFlat.KINGDOM ) {
			if ( level == TaxonFlat.GENUS ) {
				aPop = new Popup[level + 1];
			} else {
				aPop = new Popup[level + 2];
			}
		} else {
			aPop = new Popup[TaxonFlat.KINGDOM + 1];
		}
		aPop[TaxonFlat.KINGDOM] = new Popup();
		aPop[TaxonFlat.KINGDOM].addItem("");
		aPop[TaxonFlat.KINGDOM].setName(levelMap[TaxonFlat.KINGDOM]);
		popTaxon.beforeFirst();
		while ( popTaxon.next() ) {
			aPop[TaxonFlat.KINGDOM].addItem(popTaxon.getKingdom());
		}
		aPop[TaxonFlat.KINGDOM].setAttribute("onChange", "this.form.submit()");

		if ( aTaxon.first() ) {
			aPop[TaxonFlat.KINGDOM].setDefault(aTaxon.getKingdom());

			for ( int i = TaxonFlat.PHYLUM; i <= level; i++ ) {
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

			if ( level > TaxonFlat.NO_LEVEL && level < TaxonFlat.GENUS ) {
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
			aPop[TaxonFlat.KINGDOM].setDefault("");
		}

		taxCell = new TableCell();
		taxCell.setAttribute("WIDTH", "16%");
		taxCell.setAttribute("ALIGN", "CENTER");

		for ( int i = TaxonFlat.KINGDOM; i <= TaxonFlat.GENUS; i++ ) {
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
*/
	
	public String getHelpModule() {
		return HELP_MODULE;
	}
}
