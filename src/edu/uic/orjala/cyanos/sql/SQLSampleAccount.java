package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import edu.uic.orjala.cyanos.Amount;
import edu.uic.orjala.cyanos.Amount.AmountUnit;
import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.BasicObject;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Notebook;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleAccount;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.User;

/**
 * Class to manage sample transaction data.
 * 
 * <P>This data is stored in the SQL table <CODE>sample_acct</CODE>.</P>
 * 
 * <TABLE BORDER><TR><TH>Field</TH><TH>Type</TH><TH>Null</TH><TH>Key</TH><TH>Default</TH></TR>
 * <TR><TD>acct_id</TD><TD>bigint(20) unsigned</TD><TD>NO</TD><TD>PRI</TD><TD>0</TD></TR>
 * <TR><TD>sample_id</TD><TD>bigint(20) unsigned</TD><TD>NO</TD><TD>PRI</TD><TD>0</TD></TR>
 * <TR><TD>date</TD><TD>date</TD><TD>NO</TD><TD>  </TD><TD>1970-01-01</TD></TR>
 * <TR><TD>ref_table</TD><TD>varchar(45)</TD><TD>YES</TD><TD>MUL</TD><TD>NULL</TD></TR>
 * <TR><TD>ref_id </TD><TD>varchar(45)</TD><TD>YES</TD><TD>MUL</TD><TD>NULL</TD></TR>
 * <TR><TD>void_date</TD><TD>date</TD><TD>YES</TD><TD> </TD><TD>NULL</TD></TR>
 * <TR><TD>void_user</TD><TD>varchar(15)</TD><TD>YES</TD><TD> </TD><TD>NULL</TD></TR>
 * <TR><TD>amount</TD><TD>float</TD><TD>NO</TD><TD> </TD><TD>0</TD></TR>
 * <TR><TD>notes</TD><TD>text</TD><TD>YES</TD><TD> </TD><TD>NULL</TD></TR></TABLE>
 * 
 * @author George Chlipala
 *
 */
public class SQLSampleAccount extends SQLObject implements SampleAccount {

	// Setup the column names here so that changing is easier.	

	private final static String ACCT_ID_COLUMN = "acct_id";
	private final static String ACCT_REF_TABLE_COLUMN = "ref_table";
	private final static String ACCT_REF_ID_COLUMN = "ref_id";
//	private final static String ACCT_AMOUNT_COLUMN = "amount";
	private final static String ACCT_AMOUNT_VALUE_COLUMN = "amount_value";
	private final static String ACCT_AMOUNT_SCALE_COLUMN = "amount_scale";
	private final static String ACCT_NOTES_COLUMN = "notes";
	private final static String ACCT_VOID_COLUMN = "void_date";
	private final static String ACCT_VOID_ADMIN_COLUMN = "void_user";
	
	private final static String NOTEBOOK_COLUMN = "notebook_id";
	private final static String NOTEBOOK_PAGE_COLUMN = "notebook_page";

	private final static String ACCT_DATE_COLUMN = "date";
	private final static String SQL_INSERT_ACCOUNT_LINE = "INSERT INTO sample_acct(sample_id,acct_id,date) VALUES(?,?,CURRENT_DATE)";

	/*
	 * Parameter and SQL for "cleaned-up" database schema.
	 * 	
	private final static String ACCT_DATE_COLUMN = "txn_date";
	private final static String SQL_INSERT_ACCOUNT_LINE = "INSERT INTO sample_acct(sample_id,acct_id,txn_date) VALUES(?,?,CURRENT_DATE)";

	 */
	
	protected final static String SAMPLE_REF = "sample";
	protected final static String SEP_REF = "separation";
	protected final static String HARVEST_REF = "harvest";
	protected final static String ASSAY_REF = "assay";

	private final static String SQL_LOAD_ACCOUNT_DATA = "SELECT * FROM sample_acct WHERE sample_id=? ORDER BY acct_id";
	
//  Old SQL for account balance	
//	private final static String SQL_GET_BALANCE = "SELECT SUM(amount) FROM sample_acct WHERE sample_id=? AND void_date IS NULL";
	private final static String SQL_GET_BALANCE = "SELECT SUM(amount_value * POW(10, amount_scale)), MAX(amount_scale) FROM sample_acct WHERE sample_id=? AND void_date IS NULL";
	
	protected final static String SQL_VOID_REF_TXN = "UPDATE sample_acct SET void_date=CURRENT_DATE,void_user=? WHERE ref_table=? AND ref_id=?";

	/*
	 * Needed for maxAccountId() method.
	private final static String GET_MAX_ACCT_LINE_SQL = "SELECT MAX(acct_id) FROM sample_acct WHERE sample_id=?";
	 */
	
	protected boolean rowAlpha = true;
	protected boolean colAlpha = false;
	
	protected SQLSample mySample = null;

	protected static void voidReferences(SQLData data, String refTable, String refID) throws DataException {
		try {
			PreparedStatement aSth = data.prepareStatement(SQL_VOID_REF_TXN);
			aSth.setString(1, data.getUser().getUserID());
			aSth.setString(2, refTable);
			aSth.setString(3, refID);
			aSth.execute();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	protected SQLSampleAccount(SQLSample sample) throws DataException {
		super(sample.myData);
		this.myID = sample.getID();
		this.idField = "sample_id";
		this.fetchRecord();
		this.myData.setAccessRole(User.SAMPLE_ROLE);
		this.mySample = sample;
	}

	
	protected SQLSampleAccount(SQLData data, String anID) throws DataException {
		this(new SQLSample(data, anID));
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD_ACCOUNT_DATA);
	}
			
	public boolean gotoAccountLine(int aLine) throws DataException {
		boolean retval = false;
		boolean onRow = false;
		int currRow = -1;
		if ( this.myData.getRow() > 0 ) { 
			onRow = true;
			currRow = this.myData.getInt(ACCT_ID_COLUMN);
		}
		this.myData.beforeFirst();
		FIND_LINE: while ( this.myData.next() ) {
			if ( this.myData.getInt(ACCT_ID_COLUMN) == aLine ) {
				retval = true;
				break FIND_LINE;
			}
		}
		
		if ( ! retval && onRow) {
			this.gotoAccountLine(currRow);
		}
		return retval;
	}
	
	public boolean addTransaction() throws DataException {
		if ( this.myID != null && this.isAllowedException(Role.WRITE) ) {
			int aLine = 1;
			if ( this.last() ) 
				aLine = this.myData.getInt(ACCT_ID_COLUMN) + 1;
			try {
				PreparedStatement addTxn = this.myData.prepareStatement(SQL_INSERT_ACCOUNT_LINE);
				addTxn.setString(1, this.myID);
				addTxn.setInt(2, aLine);
				if ( addTxn.executeUpdate() > 0 ) {
					this.fetchRecord();
					return this.gotoAccountLine(aLine);
				}
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return false;
	}
	
	public int getAccountLine() throws DataException {
		return this.myData.getInt(ACCT_ID_COLUMN);
	}
		
	public Date getDate() throws DataException {
		return this.myData.getDate(ACCT_DATE_COLUMN);
	}
	
	public String getDateString() throws DataException {
		return this.myData.getString(ACCT_DATE_COLUMN);
	}
	
	public void setDate(Date newValue) throws DataException {
		this.myData.setDate(ACCT_DATE_COLUMN, newValue);
	}
	
	public void setDate(String newValue) throws DataException {
		this.myData.setString(ACCT_DATE_COLUMN, newValue);
	}
	
	public String getNotes() throws DataException {
		return this.myData.getString(ACCT_NOTES_COLUMN);
	}
	
	public void setNotes(String newValue) throws DataException {
		this.myData.setString(ACCT_NOTES_COLUMN, newValue);
	}
	
	public void addNotes(String newNotes) throws DataException {
		StringBuffer newValue = new StringBuffer(this.getNotes());
		newValue.append(newNotes);
		this.myData.setString(ACCT_NOTES_COLUMN, newValue.toString());
	}
	
	@Override
	public Amount getAmount() throws DataException {
		BigDecimal value = this.myData.getDecimal(ACCT_AMOUNT_VALUE_COLUMN, ACCT_AMOUNT_SCALE_COLUMN);
		BigDecimal conc = this.mySample.getConcentration();
		if ( conc != null && conc.compareTo(BigDecimal.ZERO) == 0 ) {
			value = value.divide(conc).movePointRight(3);
			return new Amount(value, AmountUnit.VOLUME);			
		} else {
			return new Amount(value, AmountUnit.MASS);			
		}
	}
	
	@Override
	public Amount getAmountMass() throws DataException {
		BigDecimal value = this.myData.getDecimal(ACCT_AMOUNT_VALUE_COLUMN, ACCT_AMOUNT_SCALE_COLUMN);
		return new Amount(value, AmountUnit.MASS);			
	}
	
	/**
	 * Set the transaction amount to value given (in grams)
	 * 
	 * @param amount to transact.
	 * @throws DataException
	 */
	private void setAmount(Amount amount) throws DataException {
		boolean liquid = ( amount.getBaseUnit() == AmountUnit.VOLUME );
		
		BigDecimal conc = this.mySample.getConcentration();
		boolean liquidSample = ( conc != null && conc.compareTo(BigDecimal.ZERO) == 0 );
		
		if ( (! liquidSample) && liquid ) {
			throw new DataException("Cannot transact liquid amounts from solid samples.");
		}	
		
		BigDecimal value = amount.getValue();
		if ( liquid && liquidSample ) {
		//  amount (L) * 10^3 mL/L * conc g/mL 
			value = value.movePointRight(3).multiply(conc);
		}
		this.myData.setDecimal(ACCT_AMOUNT_VALUE_COLUMN, ACCT_AMOUNT_VALUE_COLUMN, value);
	}

	@Override
	public void withdrawAmount(String aValue) throws DataException {
		Amount amount = new Amount(aValue);
		this.setAmount(amount.negate());
	}
	
	@Override
	public void withdrawAmount(String amount, String unit) throws DataException {
		Amount newValue = new Amount(amount, unit);
		this.setAmount(newValue.negate());
	}
	
	@Override
	public void depositAmount(String aValue) throws DataException {
		this.setAmount(new Amount(aValue));
	}
	
	@Override
	public void depositAmount(String amount, String unit) throws DataException {
		this.setAmount(new Amount(amount, unit));
	}

	@Override
	public SampleAccount transferAmount(Sample source, String amount, String unit) throws DataException {
		Amount transfer = new Amount(amount, unit);
		BigDecimal value = transfer.getValue();

		boolean liquid = ( transfer.getBaseUnit() == AmountUnit.VOLUME );
		
		BigDecimal conc = this.mySample.getConcentration();
		boolean liquidSample = ( conc != null && conc.compareTo(BigDecimal.ZERO) != 0 );
		
		if ( (! liquidSample) && liquid ) {
			throw new DataException("Cannot transact liquid amounts from solid samples.");
		}	
		
		if ( liquid && liquidSample ) {
		//  amount (L) * 10^3 mL/L * conc g/mL 
			value = value.movePointRight(3).multiply(conc);
		}
		
		this.myData.setDecimal(ACCT_AMOUNT_VALUE_COLUMN, ACCT_AMOUNT_VALUE_COLUMN, value);
		this.setTransactionReference(source);
		
		SQLSampleAccount srcAcct = (SQLSampleAccount) source.getAccount();
		srcAcct.addTransaction();
		srcAcct.setAmount(new Amount(value.negate(), AmountUnit.MASS));
		srcAcct.setTransactionReference(this.mySample);
		return srcAcct;
	}
	
	@Override
	public BasicObject getReference() throws DataException {
		if ( this.myData != null && this.myData.getRow() > 0 ) {
			String refTable = this.myData.getString(ACCT_REF_TABLE_COLUMN);
			if ( this.myData.lastWasNull() ) 
				return null;
			if ( refTable.equals(SAMPLE_REF) ) {
				Sample aSample = new SQLSample(this.myData, this.myData.getString(ACCT_REF_ID_COLUMN));
				return aSample;
			} else if ( refTable.equals(SEP_REF) ) {
				Separation aSep = new SQLSeparation(this.myData, this.myData.getString(ACCT_REF_ID_COLUMN));
				return aSep;
			} else if ( refTable.equals(HARVEST_REF) ) {
				Harvest anObj = SQLHarvest.load(this.myData, this.myData.getString(ACCT_REF_ID_COLUMN));
				return anObj;
			} else if ( refTable.equals(ASSAY_REF) ) {
				Assay anObj = SQLAssay.load(this.myData, this.myData.getString(ACCT_REF_ID_COLUMN));
				return anObj;
			}

		}
		return null;
	}
	
	@Override
	public void setTransactionReference(Sample aRef) throws DataException {
		if ( this.myData != null ) {
			this.myData.setString(ACCT_REF_TABLE_COLUMN, SAMPLE_REF);
			this.myData.setString(ACCT_REF_ID_COLUMN, aRef.getID());
		}
	}

	@Override
	public void setTransactionReference(Separation aRef) throws DataException {
		if ( this.myData != null ) {
			this.myData.setString(ACCT_REF_TABLE_COLUMN, SEP_REF);
			this.myData.setString(ACCT_REF_ID_COLUMN, aRef.getID());
		} 
	}
	
	@Override
	public void setTransactionReference(Harvest aRef) throws DataException {
		if ( this.myData != null ) {
			this.myData.setString(ACCT_REF_TABLE_COLUMN, HARVEST_REF);
			this.myData.setString(ACCT_REF_ID_COLUMN, aRef.getID());
		}
	}
	
	@Override
	public void setTransactionReference(Assay aRef) throws DataException {
		if ( this.myData != null ) {
			this.myData.setString(ACCT_REF_TABLE_COLUMN, ASSAY_REF);
			this.myData.setString(ACCT_REF_ID_COLUMN, aRef.getID());
		}
	}
	
	@Override
	public Class<?> getTransactionReferenceClass() throws DataException {
		if ( this.myData != null && this.myData.getRow() > 0 ) {
			String refTable = this.myData.getString(ACCT_REF_TABLE_COLUMN);
			if ( this.myData.lastWasNull() ) 
				return null;
			if ( refTable.equals(SAMPLE_REF) ) {
				return Sample.class;
			} else if ( refTable.equals(SEP_REF) ) {
				return Separation.class;
			} else if ( refTable.equals(HARVEST_REF) ) {
				return Harvest.class;
			} else if ( refTable.equals(ASSAY_REF) ) {
				return Assay.class;
			}
		}
		return null;
		
	}
	
	@Override
	public String getTransactionReferenceID() throws DataException {
		return this.myData.getString(ACCT_REF_ID_COLUMN);
	}
	
	@Override
	public void updateTransaction() throws DataException {
		this.myData.refresh();
	}
	
	@Override
	public BigDecimal accountBalance() throws DataException {
		return SQLSampleAccount.balance(this.myData, this.myID);
	}

	@Override
	public String getSampleID() {
		return this.myID;
	}
	
	/* 
	 * Apparently this is not necessary but, it may be needed in a future release
	
	private int maxAccountID() throws SQLException {
		PreparedStatement aPsth = this.myData.prepareStatement(GET_MAX_ACCT_LINE_SQL);
		aPsth.setString(1, this.myID);
		ResultSet aResult = aPsth.executeQuery();
		int retval = 0;
		if ( aResult.first() ) {
			retval = aResult.getInt(1);
			aResult.close();
			aPsth.close();
		}
		return retval;
	}
	*/
	
	@Override
	public Notebook getNotebook() throws DataException {
		String notebookID = this.myData.getString(NOTEBOOK_COLUMN);
		if ( notebookID != null ) {
			Notebook aNotebook = new SQLNotebook(this.myData, notebookID);
			return aNotebook;
		}
		return null;
	}

	@Override
	public String getNotebookID() throws DataException {
		return this.myData.getString(NOTEBOOK_COLUMN);
	}

	@Override
	public int getNotebookPage() throws DataException {
		return this.myData.getInt(NOTEBOOK_PAGE_COLUMN);
	}

	@Override
	public void setNotebook(Notebook aNotebook) throws DataException {
		if ( aNotebook != null ) 
			this.myData.setString(NOTEBOOK_COLUMN, aNotebook.getID());
		else 
			this.myData.setNull(NOTEBOOK_COLUMN);
	}

	@Override
	public void setNotebook(Notebook aNotebook, int aPage) throws DataException {
		if ( aNotebook != null ) {
			this.myData.setString(NOTEBOOK_COLUMN, aNotebook.getID());
			this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
		} else {
			this.myData.setNull(NOTEBOOK_COLUMN);
			this.myData.setNull(NOTEBOOK_PAGE_COLUMN);
		}
	}

	@Override
	public void setNotebookID(String anID) throws DataException {
		this.myData.setStringNullBlank(NOTEBOOK_COLUMN, anID);
	}

	@Override
	public void setNotebookID(String anID, int aPage) throws DataException {
		if ( anID.length() > 0 ) {
			this.myData.setString(NOTEBOOK_COLUMN, anID);
			this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
		} else {
			this.myData.setNull(NOTEBOOK_COLUMN);
			this.myData.setNull(NOTEBOOK_PAGE_COLUMN);
		}
	}

	@Override
	public void setNotebookPage(int aPage) throws DataException {
		this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
	}
	
	@Override
	public boolean isValid() throws DataException {
		return this.myData.isNull(ACCT_VOID_COLUMN);
	}
	
	@Override
	public boolean isVoid() throws DataException {
		return (! this.myData.isNull(ACCT_VOID_COLUMN));
	}
	
	@Override
	public Date getVoidDate() throws DataException {
		return this.myData.getDate(ACCT_VOID_COLUMN);
	}
	
	@Override
	public String getVoidUserID() throws DataException {
		return this.myData.getString(ACCT_VOID_ADMIN_COLUMN);
	}
	
	@Override
	public void voidTransaction() throws DataException {
		if ( this.isAllowedException(Role.DELETE) ) {
			Date now = new Date();
			this.myData.setDate(ACCT_VOID_COLUMN, now);
			this.myData.setString(ACCT_VOID_ADMIN_COLUMN, this.myData.getUser().getUserID());
		}
	}
	
	@Override
	public void clearReference() throws DataException {
		this.myData.setNull(ACCT_REF_ID_COLUMN);
		this.myData.setNull(ACCT_REF_TABLE_COLUMN);
	}

	/**
	 * Get the account balance for the specified sample
	 * 
	 * @param data an SQLData object
	 * @param sampleID ID of the specified sample
	 * @return Balance of the sample account.
	 * @throws DataException
	 */
	public static BigDecimal balance(SQLData data, String sampleID) throws DataException {
		try {
			PreparedStatement aBal = data.prepareStatement(SQL_GET_BALANCE);
			aBal.setString(1, sampleID);
			ResultSet aResult = aBal.executeQuery();
			BigDecimal value = null;
			if ( aResult.first() ) {
				value = aResult.getBigDecimal(1);
				if ( ! aResult.wasNull() ) {
					int places = aResult.getInt(2);
					value = value.round(new MathContext(places * (places < 0 ? -1 : 1)));					
				}
			}
			aResult.close();
			aBal.close();
			return ( value != null ? value : BigDecimal.ZERO );
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public static BigDecimal balance(SQLSample sample) throws DataException {
		return balance(sample.myData, sample.getID());
	}

}
