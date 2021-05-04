
import java.lang.System;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;



//
// This is an implementation of a simplified version of a command
// line dictionary client. The only argument the program takes is
// -d which turns on debugging output. 
//


public class CSdict
{
    static final int MAX_LEN = 255;
    static final int PERMITTED_ARGUMENT_COUNT = 1;
    private static String command;
    private static String[] arguments;
    static Boolean debugOn = false;
    static Socket client;
    static boolean connected;
    static String serverName;
    static String port;
    static String currentdict = "*";
    static List<String> dictlist = new ArrayList<String>();



    public static void main(String [] args)
    {

    System.setProperty("java.net.preferIPv4Stack" , "true");

	byte cmdString[] = new byte[MAX_LEN];
	int len;

	if (args.length == PERMITTED_ARGUMENT_COUNT) {
	    debugOn = args[0].equals("-d");
	    if (debugOn) {
		System.out.println("Debugging output enabled");
	    } else {
		System.out.println("997 Invalid command line option - Only -d is allowed");
		return;
            }
	} else if (args.length > PERMITTED_ARGUMENT_COUNT) {
	    System.out.println("996 Too many command line options - Only -d is allowed");
	    return;
	}

	try {
	    for (len = 1; len > 0;) {
		System.out.print("csdict> ");

		System.in.read(cmdString);
		
		String inputString = new String(cmdString, "ASCII");

			
			String[] inputs = inputString.trim().split(" (|\t)+");
			
			command = inputs[0].toLowerCase().trim();
	    
	    	arguments = Arrays.copyOfRange(inputs, 1, inputs.length);

			String arg1 = arguments[0];
/*
			if (command != "open" && command != "dict" && command != "match" 
				&& command != "set" && command != "define" && command != "prefixmatch" 
				&& command != "close" && command != "quit" ) {
				System.out.println("900 Invalid command.");
				return; 
			}*/

			switch(command){

			case "open":

				
				String arg2 = "2628";

				if (arguments.length == 2){

					arg2 = arguments[1];
					System.out.println(arguments[1]);
				}
				


				open(arg1, arg2);

				break;

			case "dict":
					
				dict();

				break;

			case "set" :
					
				
				setDictionary(arg1);

				break;

			case "define" :
					

				
				define(arg1);

				break;

			case "match" :
					

				
				match(arg1);

				break;

			case "prefixmatch" :
					

				
				prefixmatch(arg1);

				break;

			case "close" :

				close();

				break;

			case "quit" :

				quit();

				break;


			}


	    }
	} catch (IOException exception) {

	    System.err.println("998 Input error while reading commands, terminating.");
	    	System.exit(-1);
	}

    }


    public static void open (String hostname, String portnumber){

    	if ((arguments.length > 2)) {
					System.out.println("901 Incorrect number of arguments.");
				}

    	if (isConnected())
        {
            return;
        }

        int pn = 2628;

    	try {

    	pn = Integer.parseInt(portnumber.trim());

    	}


    	catch (NumberFormatException e){
    		System.out.println("902 Invalid Argument");
    		
    	}




    	if (pn < 0 || pn > 65535){
    		System.out.println("902 Invalid Argument");
    		
    	}



    	

    	try {

    		SocketAddress sockaddr = new InetSocketAddress(hostname, pn);

    		client = new Socket();
    		int timeout = 30000;

    		client.connect(sockaddr, timeout);


    		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(),
                    "UTF-8"));
            String fromServer = in.readLine(); // Server banner

    		 if (fromServer.startsWith("220"))
                 {   // 220 = connect ok
    			 System.out.println("220 "  + client);
    		      connected=true;
    		      return;
                 }
    	}


    	catch (SocketTimeoutException e) {
    		System.out.println("999 Processing error. Timed out while waiting for a response.");

    	}



    	catch (IOException ioe){
    		System.out.println("920 Control connection to " + hostname + " on port " + pn + "failed to open.");

    	}

    	catch (IllegalBlockingModeException e){

    		System.out.println("920 Control connection to " + hostname + " on port " + pn + "failed to open.");

    	}


    	catch (IllegalArgumentException e) {

    		System.out.println("902 Invalid Argument");
    	}

    }

    public static boolean isConnected()
    {
        return connected;
    }


	public static void dict() throws IOException{

		
		if (!CSdict.isConnected()){
			System.out.println("903 Supplied command not expected at this time");
			return;
		}

		if ((arguments.length > 1)) {
					System.out.println("901 Incorrect number of arguments.");
					return;
				}

    	PrintWriter output = null;
    	BufferedReader input = null;
    	String fromserver;




        try {
           output = new PrintWriter(CSdict.client.getOutputStream(), true);
           input = new BufferedReader(new InputStreamReader(CSdict.client.getInputStream(), "UTF-8"));

           output.println("SHOW DB");

           fromserver = input.readLine();

           if (fromserver.startsWith("110")){
           while ((fromserver = input.readLine()) != null && !fromserver.startsWith(".")){

		    		System.out.println(fromserver);
		    		dictlist.add(fromserver);


		    	}
		    }

        }

        catch (IOException e) {
           System.out.println("925 Control connection I/O error, closing control connection");
           close();
        }

    }

	public static void close() {

		
		
		
		String fromserver;
		boolean quit = false;

		if (!CSdict.isConnected()){

			return;
		}
		

		try {

			PrintWriter out = null;
			BufferedReader in;


			 out =
				    new PrintWriter(CSdict.client.getOutputStream(), true);


				in =
				    new BufferedReader(
				        new InputStreamReader(CSdict.client.getInputStream()));


		    out.println("QUIT");


		    while (quit == false && (fromserver = in.readLine()) != null){

		    	if (fromserver.startsWith("221")){
		    		quit = true;
		    	}
		    }

		    out.close();
		    in.close();
		    CSdict.client.close();

		    connected = false;

		    }

		    catch(IOException e) {

		    	e.printStackTrace();

		    }

	}

	public static void define (String word) {

	if (!CSdict.isConnected()){
			System.out.println("903 Supplied command not expected at this time");
			return;
		}

		if ((arguments.length > 1)) {
					System.out.println("901 Incorrect number of arguments.");
					return;
				}		


		String fromserver;
		boolean quit = false;
		PrintWriter output = null;
		BufferedReader input = null;

		

		try {
	           output = new PrintWriter(CSdict.client.getOutputStream(), true);
	           input = new BufferedReader(new InputStreamReader(CSdict.client.getInputStream(), "UTF-8"));

	           output.println("DEFINE " + currentdict + " " + word);

	           fromserver = input.readLine();

	           if (fromserver.startsWith("150")){
	           	System.out.println("150 " + fromserver.length() +" definitions retrieved");

	           while (quit == false && (fromserver = input.readLine()) != null){


	        	   if (fromserver.startsWith("250")){

	        		   quit = true;
	        		   break;

	        	   }
	        	   //if line starts with 151 replace start
	        	   if (fromserver.startsWith("151")){

	        		   int i = word.length();
	        		   String temp = fromserver.substring(5+i);
	        		   String blah = "151" + "@" + temp;
	        		   System.out.println(blah);

	        	   }


	        	   else {

	        	   System.out.println(fromserver);

	        	   }


	           }
	           }
	           else if (fromserver.startsWith("552")){

	        	   System.out.println("No matches found");

	           }
		}


	        catch (IOException e) {
	        	System.out.println("925 Control connection I/O error, closing control connection");
	            close();
	        }

	}

	public static void setDictionary(String name) {

		if (!CSdict.isConnected()){
			System.out.println("903 Supplied command not expected at this time");
			return;
		}

		if ((arguments.length > 1)) {
					System.out.println("901 Incorrect number of arguments.");
					return;
				}
		
		if (name =="*")
		{
			currentdict= "*";
			System.out.println("Default dictionary will be used!");
		}
		else if (name=="!")
		{
			currentdict= "!";
			System.out.println("First match will be set as the dict!");
		}
		else if (name.equals(null)){
			currentdict="*";
			System.out.println("Default dictionary will be used!");
		}

		else{
			if (dictlist == null)
			{
				try {
					dict();
				} catch (IOException e) {
					
					e.printStackTrace();
					System.out.println("Please open a server first!");
				}
			}
			String dictName ;
			String dictLine;

			for (int i=0; i<dictlist.size(); i++)
			{
				dictLine = dictlist.get(i);
				String[] inputs = dictLine.split(" ");
				dictName = inputs[0].trim();

				if (dictName.equals(name))
				{
					currentdict=name;
					System.out.println("The dictionary is set to "+currentdict);
				}

			}
		}

	}


	public static void match(String word){

		if (!CSdict.isConnected()){
			System.out.println("903 Supplied command not expected at this time");
			return;
		}

		if ((arguments.length > 1)) {
					System.out.println("901 Incorrect number of arguments.");
					return;
				}
		

		List<String> matchList = new ArrayList<String>(); 

		boolean quit = false;

		PrintWriter output = null;
		BufferedReader input = null;
		String fromserver;



		try {
			output = new PrintWriter(client.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));

			output.println("MATCH "+currentdict +" exact "+ word );

			fromserver = input.readLine();

			System.out.println(fromserver);

			if (fromserver.startsWith("152")){
				while (quit == false && (fromserver = input.readLine()) != null)
				{
					if (fromserver.startsWith("552"))
					{
						quit=true;
					}
					else if (!fromserver.equals("."))
					{
						
						System.out.println(  fromserver + "/n");
					}

					else if (fromserver.startsWith(".")){

						break;
					}
				}


			}
			else System.out.println("****No matching word(s) found****");
		}





		catch (IOException e) {
			System.out.println("925 Control connection I/O error, closing control connection");
	           close();
		}



	}

	public static void prefixmatch(String word){

		
		if (!CSdict.isConnected()){
			System.out.println("903 Supplied command not expected at this time");
			return;
		}

		if ((arguments.length > 1)) {
					System.out.println("901 Incorrect number of arguments.");
					return;
				}

		

		List<String> matchList = new ArrayList<String>(); // All the dictionaries matching the word

		boolean quit = false;

	

		PrintWriter output = null;
		BufferedReader input = null;
		String fromserver;

		try {
			output = new PrintWriter(client.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));

			output.println("MATCH "+currentdict +" prefix "+ word );

			fromserver = input.readLine();

			System.out.println(fromserver);

			if (fromserver.startsWith("152")){
				while (quit == false && (fromserver = input.readLine()) != null)
				{
					if (fromserver.startsWith("552"))
					{
						quit=true;
					}
					else if (!fromserver.equals("."))
					{
						System.out.println(fromserver+"/n");
					}
					else if (fromserver.startsWith(".")){

						break;
					}
				}


			}
			else System.out.println("****No matching word(s) found****");
		}

		catch (IOException e) {
			System.out.println("925 Control connection I/O error, closing control connection");
	           close();
		}

	}


	public static void quit (){


		try {
			CSdict.client.close();
		} catch (IOException e) {
			
			System.out.println("999 Processing error." + e);
		}
		System.exit(0);

		

	}
}

