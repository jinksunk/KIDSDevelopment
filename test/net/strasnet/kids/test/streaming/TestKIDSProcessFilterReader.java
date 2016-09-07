/**
 * 
 */
package net.strasnet.kids.test.streaming;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.strasnet.kids.streaming.KIDSProcessFilterReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;

/**
 * @author Chris Strasburg
 *
 */
//TODO: Capture and test the log output
public class TestKIDSProcessFilterReader {
	
	private PipedOutputStream pos = null;
	private PipedInputStream pis = null;
	private String grepcommand = "/usr/bin/grep";
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		pos = new PipedOutputStream();
		pis = new PipedInputStream(pos);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	public ProcessBuilder getGrepProc(String filterString) throws IOException{
		List <String> args = new ArrayList<String>();
		//args.add("/bin/sh");
		//args.add("-c");
		//args.add(String.format("%s -a -E '%s'",grepcommand, filterString));
		args.add(grepcommand);
		args.add("-a");
		args.add("-E");
		args.add("filterString");

		ProcessBuilder pb = new ProcessBuilder(args);
		System.out.println(String.format("Executing command: %s",pb.command()));
		return pb;
	}

	/**
	 * Test method for {@link net.strasnet.kids.streaming.KIDSTCPDumpStreamingDetector#handleEvent(net.strasnet.kids.streaming.StreamingEvent)}.
	 */
	@Test
	public final void testFilterStream() {
		
		final String filterExp = "n";
		final String[] testStrings = {"nnn", "klep", "nklap"};
		final List<String> readStrings = new LinkedList<String>();
		final int expectedMatches = 2;
		

		try {
			// Start server:
			ProcessBuilder grepproc = getGrepProc(filterExp);

			final KIDSProcessFilterReader kpfr = new KIDSProcessFilterReader(new BufferedReader(
					new InputStreamReader(this.pis)),
					grepproc);

			Thread reader = new Thread(){
				public void run() {
					String newString = null;
					try {
						while((newString = kpfr.readLine()) != null){
							System.out.println(String.format("reader read: '%s'",newString));
							readStrings.add(newString);
						}
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
				}
			};
				
			Thread writer = new Thread(){
				public void run() {
					PrintWriter out = new PrintWriter(new OutputStreamWriter(pos));
					for (int i = 0; i < testStrings.length; i++){
						System.out.println(String.format("writer writing: '%s'",testStrings[i]));
						for (int j = 0; j < 1; j++){
						    out.println(testStrings[i]);
						}
						out.flush();
					}
					out.close();
				}
			};

			writer.start();
			reader.start();
			
			reader.join(1000);
			writer.join(1000);
			kpfr.close();
			
			int numMatches = readStrings.size();
			
			assertTrue(String.format("Read %d matching strings instead of %d...",numMatches,expectedMatches),
						numMatches == expectedMatches);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		} catch (InterruptedException e){
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	/**
	 * Run the class from the command line:
	 * @param argv
	 */
	public static void main(String[] argv){
		JUnitCore.runClasses(TestKIDSProcessFilterReader.class);
	}

}
