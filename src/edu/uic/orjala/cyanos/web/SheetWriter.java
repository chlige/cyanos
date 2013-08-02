/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;

/**
 * @author George Chlipala
 *
 */
public class SheetWriter {

	private PrintWriter out;
	private String delim = ",";
	
	/**
	 * 
	 */
	public SheetWriter(PrintWriter out) {
		this.out = out;
	}
	
	public SheetWriter(PrintWriter out, String delim) {
		this(out);
		this.delim = delim;
	}
	
	public void print(String s) {
		if ( s != null ) {
			s = s.replaceAll("[\n\r]", " ");
			boolean quote = s.contains(delim);
			if (quote)
				out.print("\"");

			out.print(s);

			if ( quote)
				out.print("\"");
		}
		out.print(delim);
	}
	
	public void print(boolean b) {
		out.print(b);
		out.print(delim);
	}
	
	public void print(char c) {
		out.print(c);
		out.print(delim);
	}
	
	public void print(double d) {
		out.print(d);
		out.print(delim);
	}
	
	public void print(float f) {
		out.print(f);
		out.print(delim);
	}
	
	public void print(int i) {
		out.print(i);
		out.print(delim);
	}
	
	public void print(long l) {
		out.print(l);
		out.print(delim);
	}

	public void println(String s) {
		if ( s != null ) {
			s = s.replaceAll("[\n\r]", " ");
			boolean quote = s.contains(delim);
			if (quote)
				out.print("\"");

			out.print(s);

			if ( quote)
				out.print("\"");
		}
		out.println();
	}
	
	public void println(boolean b) {
		out.println(b);
	}
	
	public void println(char c) {
		out.println(c);
	}
	
	public void println(double d) {
		out.println(d);
	}
	
	public void println(float f) {
		out.println(f);
	}
	
	public void println(int i) {
		out.println(i);
	}
	
	public void println(long l) {
		out.println(l);
	}
}
