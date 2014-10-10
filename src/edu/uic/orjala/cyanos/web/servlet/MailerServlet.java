/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import edu.uic.orjala.cyanos.web.listener.AppConfigListener;


/**
 * @author George Chlipala
 *
 */


public class MailerServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 432363190249517881L;
	Timer myTimer;
	
	public void init(ServletConfig config) throws ServletException {
		try {
			super.init(config);

		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected Connection getDBC() throws SQLException {
		return AppConfigListener.getDBConnection();
	}
}

class QueueMailTask extends TimerTask {

	private MailerServlet myServlet = null;
	private Calendar lastRun;
	private String user;
	private Connection aConn = null;
	private static final String QUEUE_SQL = "SELECT item_type,item_id,req_details,added,added_by FROM queue WHERE queue_type=? AND queue_name=? AND added >= ? ORDER BY added ASC";
	private static final String EMAIL_SQL = "SELECT email FROM users WHERE username=?";
	private static final String QUEUES_FOR_USER_SQL = "SELECT item_id FROM queue WHERE queue_type='user' AND queue_name=? AND item_type='queue' ORDER BY item_id ASC";
	/**
	 * 
	 */
	public QueueMailTask(MailerServlet aServlet, String aUser) {
		this.myServlet = aServlet;
		this.user = aUser;
		this.lastRun = Calendar.getInstance();
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	
	public void run() {
		if ( aConn == null ) {
			aConn = this.getConnection();
		}

		if ( aConn != null ) {
			try {
				List<String> queues = this.getQueues();
				if ( queues.size() > 0 ) {
					Calendar now = Calendar.getInstance();
					Iterator<String> anIter = queues.listIterator();
					StringBuffer output = new StringBuffer();
					PreparedStatement aPsth = aConn.prepareStatement(QUEUE_SQL);
					Timestamp aTs = new Timestamp(this.lastRun.getTimeInMillis());
					aPsth.setTimestamp(3, aTs);
					int totalItems = 0;
					while ( anIter.hasNext() ) {
						String[] queue = anIter.next().split("/", 2);
						aPsth.setString(1, queue[0]);
						aPsth.setString(2, queue[1]);
						ResultSet newItems = aPsth.executeQuery();
						newItems.beforeFirst();
						int items = 0;
						output.append(String.format("%s QUEUE: %s\n", queue[0].toUpperCase(), queue[1]));
						while ( newItems.next() ) {
							items++;
							output.append(String.format("ITEM: %s %s REQUESTED BY: %s \n%s\n", 
									newItems.getString(1), newItems.getString(2), newItems.getString(5), newItems.getString(3)));
						}
						output.append("\n\n");
						totalItems += items;
					}
					output.insert(0, String.format("QUEUE SUMMARY.  %d NEW ITEMS\n\n", totalItems));
					Message aMessage = this.createMailMessage();
					try {
//						aMessage.setFrom(new InternetAddress("CYANOS DATABASE <do-not-reply@cyanos>"));
						InternetAddress to[] = new InternetAddress[1];
						to[0] = new InternetAddress(this.getEmail());
						aMessage.setRecipients(Message.RecipientType.TO, to);
						SimpleDateFormat myFormat = new SimpleDateFormat("MMMMM d, yyyy");
						aMessage.setSubject("Queue Report - " + myFormat.format(now));
						aMessage.setContent(output.toString(), "text/plain");
						Transport.send(aMessage);
					} catch (AddressException e) {
						System.out.println("Cannot create email");
						System.out.println(e.getMessage());
						e.printStackTrace();
					} catch (MessagingException e) {
						System.out.println("Cannot create email");
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			} catch (SQLException e) {

			}

			try {
				aConn.close();
				aConn = null;
			} catch (SQLException e) {
				System.out.println("Cannot close database connection.");
			}
		}
		lastRun = Calendar.getInstance();
	}
	
	private Connection getConnection() {
		try {
			return this.myServlet.getDBC();
		} catch (SQLException e) {
			System.out.println("Cannot get database connection.");
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	private Message createMailMessage() {
		Session aSession = null;
		try {
			aSession = MailerServlet.getMailSession();
		} catch (NamingException e) {
			System.out.println("Cannot start a mail session");
			System.out.println(e.getMessage());
		}
		if (aSession != null )  {
			Message aMessage = new MimeMessage(aSession);
			return aMessage;
		}
		return null;
	}
		
	private String getEmail() throws SQLException {
		PreparedStatement aPsth = aConn.prepareStatement(EMAIL_SQL);
		aPsth.setString(1, this.user);
		ResultSet aResult = aPsth.executeQuery();
		if ( aResult.first() )
			return aResult.getString(1);
		return null;
	}
	
	private List<String> getQueues() throws SQLException {
		PreparedStatement aPsth = aConn.prepareStatement(QUEUES_FOR_USER_SQL);
		aPsth.setString(1, this.user);
		ResultSet aResult = aPsth.executeQuery();
		List<String> queues = new ArrayList<String>();
		aResult.beforeFirst();
		while ( aResult.next() ) {
			queues.add(aResult.getString(1));
		}
		return queues;
	}


}
