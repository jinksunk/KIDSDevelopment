package net.strasnet.kids.test.streaming;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.runner.JUnitCore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;

/**
 * This class just runs through some tests with grep and the system to determine how best to
 * run strings through it continuously.
 * @author Chris Strasburg
 *
 */
public class sillyGrepTest {
	public static final String greploc = "/usr/bin/grep";
	private BlockingQueue<String> ClassLineBuffer = null;
	public static List<String> grepargs = new ArrayList<String>();
	static {
		grepargs.add("-a");
		grepargs.add("-E");
	};
	public static final String shellloc = "/bin/sh";
	public static List<String> shellargs = new ArrayList<String>();
	static {
		shellargs.add("-c");
	};
	
	public static String filterString = "^nn*";
	
	public static List<String> testStrings = new ArrayList<String>();
	static {
		testStrings.add("nnnn");
		testStrings.add("nnn");
		testStrings.add("nn");
		testStrings.add("kraptastic");
		testStrings.add("knnn");
	}
	
	@BeforeClass
	public static void BeforeClass(){
	}

	@AfterClass
	public static void AfterClass(){
		
	}

	@Before
	public void Before(){
		ClassLineBuffer = new ArrayBlockingQueue<String>(1024);
	}

	@After
	public void After(){
		
	}
	
	/**
	 * First test - Run the system grep command without the enclosing shell:
	 * Uses StreamGobbler and StreamFeeder
	 * @param argv
	 */
	@Test
	public void rawGrep() {
		List<String> commandList = new ArrayList<String>();
		commandList.add(greploc);
		commandList.addAll(grepargs);
		commandList.add(filterString);
		
		ProcessBuilder pb = new ProcessBuilder(commandList);
		
		System.out.println(String.format("Running command %s", pb.command()));

		Process grepproc;
		try {
			grepproc = pb.start();
			StreamGobbler stdout = new StreamGobbler(grepproc.getInputStream(), "STDOUT", ClassLineBuffer);
			StreamGobbler stderr = new StreamGobbler(grepproc.getErrorStream(), "ERROR", null);
			StreamFeeder stdin = new StreamFeeder(grepproc.getOutputStream(), sillyGrepTest.testStrings);
			stdout.start();
			stderr.start();
			stdin.start();
			
			int exitval = grepproc.waitFor();
			System.out.println(String.format("Exit value: %d", exitval));
			for (String line : ClassLineBuffer){
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException ie){
			ie.printStackTrace();
		}
		
	}

	public static void main(String[] argv){
		JUnitCore.runClasses(TestKIDSProcessFilterReader.class);
	}
	
	/**
	 * Additional Classes here;
	 */
	class StreamGobbler extends Thread {
		    InputStream is;
		    String type;
		    BlockingQueue<String> thisSink;
		    
		    StreamGobbler(InputStream is, String type, BlockingQueue<String> sink)
		    {
		        this.is = is;
		        this.type = type;
		        thisSink = sink;
		    }
		    
		    public void run()
		    {
		        try
		        {
		            InputStreamReader isr = new InputStreamReader(is);
		            BufferedReader br = new BufferedReader(isr);
		            String line=null;
		            while ( (line = br.readLine()) != null)
		            	if (type == "STDOUT" && thisSink != null){
		            		thisSink.add(line);
		            	} else {
		                    System.out.println(type + ">" + line);    
		            	}
		            } catch (IOException ioe)
		              {
		                ioe.printStackTrace();  
		              }
		    }
	}

	class StreamFeeder extends Thread {
		    OutputStream os;
		    List<String> feedbag = null;
		    
		    StreamFeeder(OutputStream os, List<String> food)
		    {
		        this.os = os;
		        feedbag = food;
		    }
		    
		    public void run()
		    {
		        try
		        {
		            OutputStreamWriter osr = new OutputStreamWriter(os);
		            BufferedWriter br = new BufferedWriter(osr);
		            for (String food : feedbag){
		            	br.write(food, 0, food.length());
		            	br.newLine();
		            	br.flush();
		            }
		            br.close();
		        } catch (IOException ioe){
		        	ioe.printStackTrace();
		        }
		    }
	}

}
