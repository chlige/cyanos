/**
 * 
 */
package edu.uic.orjala.cyanos;

/**
 * @author George Chlipala
 *
 */
public class Role {

	private String myRole = null;
	private int permBits = 0;
	
	public static final int READ = 0;
	public static final int WRITE = 1;
	public static final int CREATE = 2;
	public static final int DELETE = 4;
	
	/**
	 * 
	 */
	public static String labelForBit(int aBit) {
		switch ( aBit ) {
		case 0: return "Read";
		case 1: return "Write";
		case 2: return "Create";
		case 4: return "Delete";
		}
		return "";
	}
	
	public Role(String roleName, int bits) {
		this.myRole = roleName;
		this.permBits = bits;
	}
	
	public boolean hasPermission(int aBit) {
		int myBit = this.permBits & aBit;
		return ( myBit == aBit );
	}
	
	public String roleName() {
		return this.myRole;
	}
	
	public boolean equals(String s) {
		return this.myRole.equals(s);
	}
	
	public int permissions() {
		return this.permBits;
	}

}
