// Copyright (C) 2015 Scott Hoelsema
// Licensed under GPL v3.0; see LICENSE for full text

package utils;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWriteMode;

/**
 * A collection of static methods that perform a variety of useful functions
 * 
 * @author Scott Hoelsema
 */
public class Utilities {
	
	static DbxClient dbxConnection = null;
	
	public static void setIcon(JFrame jFrame) {
		if(Configuration.APP_LOGO_PATH.equals("default"))
			jFrame.setIconImage(new ImageIcon(jFrame.getClass().getResource("/resources/icon/logo.png")).getImage());
		else
			jFrame.setIconImage(new ImageIcon(Configuration.APP_LOGO_PATH).getImage());
	}
	
	/**
	 * Center a JFrame on the screen
	 * 
	 * @param frame The JFrame to center
	 */
	public static void centerOnScreen(JFrame frame) {
		Toolkit toolkit = frame.getToolkit();
		Dimension size = toolkit.getScreenSize();
		frame.setLocation(size.width/2 - frame.getWidth()/2, size.height/2 - frame.getHeight()/2);
	}
	
	/**
	 * Translates MySQL Timestamp to a more readable date, e.g. "January 1, 2012"
	 * 
	 * @param timestamp The SQL timestamp to be converted
	 * @param parseTime Boolean indicating whether or not to parse the time portion of the timestamp (assumes it exists; if the original SQL type was DATE, there is no time portion)
	 * @return A String that is the same date or date and time, but more easily readable
	 */
	public static String translateToReadableDate(Timestamp timestamp, boolean parseTime) {
		String date = timestamp.toString();
		
		String translatedDate = "";
		if(date.substring(5,7).equals("01")) {
			translatedDate = "January ";
		} else if(date.substring(5,7).equals("02")) {
			translatedDate = "February ";
		} else if(date.substring(5,7).equals("03")) {
			translatedDate = "March ";
		} else if(date.substring(5,7).equals("04")) {
			translatedDate = "April ";
		} else if(date.substring(5,7).equals("05")) {
			translatedDate = "May ";
		} else if(date.substring(5,7).equals("06")) {
			translatedDate = "June ";
		} else if(date.substring(5,7).equals("07")) {
			translatedDate = "July ";
		} else if(date.substring(5,7).equals("08")) {
			translatedDate = "August ";
		} else if(date.substring(5,7).equals("09")) {
			translatedDate = "September ";
		} else if(date.substring(5,7).equals("10")) {
			translatedDate = "October ";
		} else if(date.substring(5,7).equals("11")) {
			translatedDate = "November ";
		} else if(date.substring(5,7).equals("12")) {
			translatedDate = "December ";
		}
		
		if(date.charAt(8)=='0') {
			translatedDate = translatedDate + date.substring(9,10) + ", ";
		} else {
			translatedDate = translatedDate + date.substring(8,10) + ", ";
		}
		
		translatedDate = translatedDate + date.substring(0,4);
		
		// Return if only handling date rather than datetime
		if(parseTime==false){return translatedDate;}
		
		translatedDate = translatedDate + " ";
		
		if(Integer.parseInt(date.substring(11,13))>12) { // PM past 12:59
			translatedDate = translatedDate + (Integer.parseInt(date.substring(11,13))-12) + ":" + date.substring(14,16) + " PM";
		} else if(Integer.parseInt(date.substring(11,13))==12) { // PM Between 12:00 and 12:59
			translatedDate = translatedDate + (Integer.parseInt(date.substring(11,13))) + ":" + date.substring(14,16) + " PM";
		} else { // AM
			translatedDate = translatedDate + (Integer.parseInt(date.substring(11,13))) + ":" + date.substring(14,16) + " AM";
		}
		
		return translatedDate;
	}
	
	/**
	 * Translates MySQL Date to a more readable date, e.g. "January 1, 2012"
	 * 
	 * @param SQLDate The SQL Date to be converted
	 * @return A String that is the same date, but more easily readable
	 */
	public static String translateToReadableDate(Date SQLDate) {
		String date = SQLDate.toString();
		
		String translatedDate = "";
		if(date.substring(5,7).equals("01")) {
			translatedDate = "January ";
		} else if(date.substring(5,7).equals("02")) {
			translatedDate = "February ";
		} else if(date.substring(5,7).equals("03")) {
			translatedDate = "March ";
		} else if(date.substring(5,7).equals("04")) {
			translatedDate = "April ";
		} else if(date.substring(5,7).equals("05")) {
			translatedDate = "May ";
		} else if(date.substring(5,7).equals("06")) {
			translatedDate = "June ";
		} else if(date.substring(5,7).equals("07")) {
			translatedDate = "July ";
		} else if(date.substring(5,7).equals("08")) {
			translatedDate = "August ";
		} else if(date.substring(5,7).equals("09")) {
			translatedDate = "September ";
		} else if(date.substring(5,7).equals("10")) {
			translatedDate = "October ";
		} else if(date.substring(5,7).equals("11")) {
			translatedDate = "November ";
		} else if(date.substring(5,7).equals("12")) {
			translatedDate = "December ";
		}
		
		if(date.charAt(8)=='0') {
			translatedDate = translatedDate + date.substring(9,10) + ", ";
		} else {
			translatedDate = translatedDate + date.substring(8,10) + ", ";
		}
		
		translatedDate = translatedDate + date.substring(0,4);	
		
		return translatedDate;
	}
	
	/**
	 * Converts from military time to 12 hour time
	 * 
	 * @param time String of the time in format HH:MM
	 * @return The time in 12 hour format
	 */
	public static String twelveHourClockFormat(String time) {
		if(Integer.parseInt(time.substring(0,2)) < 13) { // Already correct; less than 1 o'clock
			return time;
		} else {
			int hours = Integer.parseInt(time.substring(0,2));
			return Integer.toString(hours - 12) + time.substring(2);
		}
	}
	
	/**
	 * Upload a file to dropbox
	 * 
	 * @param file
	 *            The file to upload
	 * @return boolean indicating success
	 */
	public static boolean uploadToDropbox(File file) {
		if(dbxConnection == null) { // If currently no connection to Dropbox, set one up
			setUpDropboxConnection();
		}
		try {
			FileInputStream inputStream = new FileInputStream(file);
			dbxConnection.uploadFile("/" + file.getName(), DbxWriteMode.add(), file.length(), inputStream);
			inputStream.close();
			return true;
		} catch (DbxException | IOException e) {
			Logger.logThrowable(e);
			JOptionPane.showMessageDialog(null, "Problem uploading. Check your internet connection.", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Helper method for uploadToDropbox that sets up the connection if it isn't already
	 */
	private static void setUpDropboxConnection() {
		DbxRequestConfig config = new DbxRequestConfig("/", Locale.getDefault().toString());
        
        // Use to reset authentication code for second paramater of DbxClient constructor
		/*final String APP_KEY = "wqq9grqrfjvew0w";
		final String APP_SECRET = "uklzz70x9ian3kt";
        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        String authorizeUrl = webAuth.start();
        System.out.println("1. Go to: " + authorizeUrl);
        System.out.println("2. Click \"Allow\" (you might have to log in first)");
        System.out.println("3. Copy the authorization code.");
		try {
			String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			DbxAuthFinish authFinish = webAuth.finish(code);
			DbxClient client = new DbxClient(config, code);
		} catch (DbxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
        
       dbxConnection = new DbxClient(config, Configuration.dropBoxApiKey);
	}
	
	public static void main(String[] args) {
		setUpDropboxConnection();
	}
}
