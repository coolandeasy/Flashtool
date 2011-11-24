package org.system;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

import gui.FlasherGUI;

import org.adb.AdbUtility;
import org.logger.MyLogger;

public class PhoneThread extends Thread {
	
	String currentStatus="none";
	boolean done=false;

	private ProcessBuilder builder;
	private Process adb;
	private static String fsep = OS.getFileSeparator();
	private InputStream processInput;
	private Scanner sc;

	public void done() {
		done=true;
	}
	
	public void run() {
		String status = "none";
		if (OS.getName().equals("windows")) {
		try {
			if (OS.getName().equals("linux"))
				builder = new ProcessBuilder(new File(System.getProperty("user.dir")+fsep+"x10flasher_lib"+fsep+"adb").getAbsolutePath(), "status-window");
			else
				builder = new ProcessBuilder(new File(System.getProperty("user.dir")+fsep+"x10flasher_lib"+fsep+"adb.exe").getAbsolutePath(), "status-window");
			adb = builder.start();
		    Thread t = new Thread() {
		    	  public void run() {
				      processInput = adb.getInputStream();
				      sc = new Scanner(processInput);
			    	  while (sc.hasNextLine()) {
			    		  String line = sc.nextLine();
			    		  if (OS.getName().equals("linux")) {
			    			  if (line.contains("State: device")) {
			    				  if (!AdbUtility.isConnected()) { 
			    					  while (!AdbUtility.isConnected()) {
			    						  try {
			    							  sleep(1000);
			    						  }
			    						  catch (Exception e) {
			    						  }
			    					  }
			    				  }
			    				  MyLogger.getLogger().debug("Device connected, continuing with identification");
			    				  FlasherGUI.doIdent();
			    			  }
			    			  else
			    				  if (line.contains("State: unknown")) FlasherGUI.doDisableIdent();
			    		  }
			    	  }    		  
		    	  }
		    };
		    t.start();
			while (!done) {
				int count=1;
				while (count<2001 && !done) {
					sleep(1);
					count++;
				}
				if (!done) {
				currentStatus=Device.getStatus(Device.getConnectedDevice());
				if (!status.equals(currentStatus)) {
					status = currentStatus;
					if (currentStatus.startsWith("Err")) {
						MyLogger.getLogger().error("Device connected in "+currentStatus.replace("Err","")+" mode.");
						MyLogger.getLogger().error("Drivers need to be installed for connected device.");
					}
					if (currentStatus.equals("adb")) {
		    		  if (!AdbUtility.isConnected()) {
			    		  while (!AdbUtility.isConnected()) {
			    			  sleep(500);
			    		  }
		    		  }
		    		  MyLogger.getLogger().info("Device connected with USB debugging on");
		    		  MyLogger.getLogger().debug("Device connected, continuing with identification");
		    		  FlasherGUI.doIdent();
					}
					if (currentStatus.equals("none")) {
						MyLogger.getLogger().info("Device disconnected");
						FlasherGUI.doDisableIdent();
					}
					if (currentStatus.equals("flash")) {
						MyLogger.getLogger().info("Device connected in flash mode");
						FlasherGUI.doDisableIdent();
					}
					if (currentStatus.equals("fastboot")) {
						MyLogger.getLogger().info("Device connected in fastboot mode");
						FlasherGUI.doDisableIdent();
					}
					if (currentStatus.equals("normal")) {
						MyLogger.getLogger().info("Device connected with USB debugging off");
						FlasherGUI.doDisableIdent();
					}
					if (currentStatus.equals("mtp")) {
						MyLogger.getLogger().info("Device connected with USB debugging on and MTP mode on. Switch your device to MSC mode");
						FlasherGUI.doDisableIdent();
					}
				}
				}
			}
			adb.destroy();
			try {
				AdbUtility.killServer();
			}
			catch (Exception e) {
			}
		}
		catch (Exception e) {
		}
		}
	}

}