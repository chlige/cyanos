//
//  CalServlet.java
//  Cyanos
//
//  Created by George Chlipala on 5/7/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web;


import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

public class CalServlet extends ServletObject {
	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet ( HttpServletRequest req, HttpServletResponse res ) 
		throws ServletException, IOException
	{
		CyanosWrapper aWrap = new ServletWrapper(this, this.dbh, req, res);
		res.setContentType("text/html");
		PrintWriter out = aWrap.startHTMLDoc("Calendar", false);
		try {
			if ( req.getParameter("month") != null && req.getParameter("year") != null) {
				Integer month = new Integer(req.getParameter("month"));
				Integer year = new Integer(req.getParameter("year"));			
				out.println(this.getMonth(req.getParameter("update_field"), month.intValue(), year.intValue()));
			} else {
				Calendar today = Calendar.getInstance();
				out.println(this.getMonth(req.getParameter("update_field"), today.get(Calendar.MONTH), today.get(Calendar.YEAR)));		
			}
		} catch (Exception e) {
			out.println("<FONT COLOR='red'><B>");
			out.println("Java Exception: " + e.getMessage() + "<BR/>");
			out.println(e.toString() + "<BR/>");
			out.println("</B></FONT>");
			e.printStackTrace();
		}
		aWrap.finishHTMLDoc();
	}
	
	public String getMonth ( String updateField, int month, int year ) 
	{
		StringBuffer output = new StringBuffer();
		Calendar today = Calendar.getInstance();
		Calendar myCal = new GregorianCalendar(year, month, 1);
		boolean thisMonth = (myCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && 
						myCal.get(Calendar.MONTH) == today.get(Calendar.MONTH));

		Popup orderPop = new Popup();
		orderPop.setAttribute("onChange", "document.cal.submit()");
		orderPop.setName("month");
		orderPop.addItemWithLabel("0", "January");
		orderPop.addItemWithLabel("1", "February");
		orderPop.addItemWithLabel("2", "March");
		orderPop.addItemWithLabel("3", "April");
		orderPop.addItemWithLabel("4", "May");
		orderPop.addItemWithLabel("5", "June");
		orderPop.addItemWithLabel("6", "July");
		orderPop.addItemWithLabel("7", "August");
		orderPop.addItemWithLabel("8", "September");
		orderPop.addItemWithLabel("9", "October");
		orderPop.addItemWithLabel("10", "November");
		orderPop.addItemWithLabel("11", "December");
		orderPop.setDefault(String.valueOf(month));
		
		output.append("<FORM NAME='cal'><INPUT TYPE='HIDDEN' NAME='update_field' VALUE='");
		output.append(updateField);
		output.append("'/>");

		Table myTable = new Table("<CAPTION CLASS=\"month\">" + orderPop.toString() + 
			"<INPUT TYPE='TEXT' NAME='year' SIZE='5' onChange='document.cal.submit()' VALUE='" +
			String.valueOf(year) + "'/></CAPTION>");
		myTable.setClass("month");
		myTable.setAttribute("align", "center");		
		myTable.setAttribute("width", "250");		

		String[] mons = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
		TableCell calHeader = new TableHeader(mons);
		TableRow tableRow = new TableRow(calHeader);
		myTable.addItem(tableRow);

		StringBuffer week = new StringBuffer();
		tableRow.addItem(week);
		for (int i = 1; i < myCal.get(Calendar.DAY_OF_WEEK); i++ ) {
			week.append("<TD></TD>");
		}
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		while ( myCal.get(Calendar.MONTH) == month ) {
			if ( thisMonth && myCal.get(Calendar.DATE) == today.get(Calendar.DATE) ) {
				week.append("<TD CLASS='today'><A onClick='setField(\"");	
			} else {
				week.append("<TD CLASS='day'><A onClick='setField(\"");
			}
			week.append(year);
			week.append("-");
			week.append(nf.format(month + 1));
			week.append("-");
			week.append(nf.format(myCal.get(Calendar.DAY_OF_MONTH)));
			week.append("\")'>");
			week.append(myCal.get(Calendar.DAY_OF_MONTH));
			week.append("</A></TD>");
			if ( myCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ) { 
				week = new StringBuffer();
				tableRow.addItem(week);
			}
			myCal.add(Calendar.DATE, 1);
		}
		
		output.append(myTable.toString());
		return output.toString();
	}

}
