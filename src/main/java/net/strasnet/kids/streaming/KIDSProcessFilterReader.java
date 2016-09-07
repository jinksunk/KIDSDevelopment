/**
 * 
 */
package net.strasnet.kids.streaming;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Chris Strasburg
 *
 * This class listens on a local network socket, does some minor pre-processing, and then streams the resulting
 * data to the provided OutputStream. 
 * 
 * This class:
 * <UL>
 *   <LI>Opens a listening network socket on the given host and port</LI>
 *   <LI>Starts a thread to pull data from the network socket and pass it to the preprocessor</LI>
 *   <LI>Starts a second thread to pull data from the preprocessor and store it in the queue</LI>
 *   <LI>Once processed, writes to the given OutputStream</LI>
 * </UL>
 * 
 * TODO: Should make this a multi-threaded server / allow to survive multiple clients
 * TODO: Abstract network socket handling from string processing
 */
public class KIDSProcessFilterReader extends BufferedReader {
	
	private Process ourproc = null;
	//private BufferedReader origin = null;
	private StreamGobbler treader = null;
	private StreamGobbler terror = null;
	private StreamFeeder twriter = null;
	private int linebuflimit = 1024;
	private BlockingQueue<String> ClassLineBuffer = null;

	private static final Logger logme = LogManager.getLogger(KIDSProcessFilterReader.class.getName());

	
	/**
	 * 
	 * @param r
	 * @param grepproc
	 * @throws IOException - If the underlying process throws an IOException.
	 */
	public KIDSProcessFilterReader(BufferedReader origin, ProcessBuilder grepproc) throws IOException{
		super(origin);
		logme.debug(String.format("Starting system process filter command [%s] in directory %s", grepproc.command(), grepproc.directory()));
	    ClassLineBuffer = new ArrayBlockingQueue<String>(linebuflimit);
		ourproc = grepproc.start();
		treader = new StreamGobbler(ourproc.getInputStream(), "STDOUT", ClassLineBuffer);
		terror = new StreamGobbler(ourproc.getErrorStream(), "STDERR", null);
		twriter = new StreamFeeder(ourproc.getOutputStream(), origin);
		treader.start();
		terror.start();
		twriter.start();
    }
	
	@Override
	/**
	 * I don't even know what to do with this - do we need it?
	 */
	public int read(char[] buf, int from, int len) throws IOException {
		logme.debug(String.format("Begin read %d characters from %d in array size %d",len,from, buf.length));
		int charsRead = super.read(buf, 0, len);
		logme.debug(String.format("End read %d characters from parent",charsRead));
		return charsRead;
		
		/*
		char[] tbuf = new char[len]; // temporary buffer
		int charsRead = super.read(tbuf, 0, len);
		logme.debug(String.format("read %d characters from parent",charsRead));
		int writeloc = from;
		
		// pull out lines from buf; pass lines to grep
		
		// Write characters to the grep process?
		for (int i = 0; i < len; i++){
			// Check for a newline:
			boolean nlmatch = false;
			for (int j = 0 ; nlmatch != true && j < nlarray.length; j++){
				nlmatch = (nlarray[j] == tbuf[i+j]);
			}
			if (nlmatch){
				String tosubmit = String.format("%s%n",sb.toString());
				logme.debug(String.format("Submitting [%s] to grep...",tosubmit));
				gout.write(tosubmit,0,tosubmit.length());
				logme.debug("Reading back from grep...");
				StringBuffer returnVal = new StringBuffer();
				while(gin.ready()){
					returnVal.append(gin.read());
				}
				if (!returnVal.equals("")){
					logme.debug(String.format("Grep matched %s!", returnVal));
					String finalout = String.format("%s%n", tosubmit);
					char[] finalcharry = finalout.toCharArray();
					// Write to buf, track using writeloc
					int k = 0;
					for (k = 0; k < finalout.length(); k++){
						buf[k+writeloc] = finalcharry[k];
					}
					writeloc += k;
				} else {
					logme.debug(String.format("Grep did not match (got [%s])", returnVal));
				}
			} else {
				sb.append(tbuf[i]);
			}
		}
		
		// Return the total number of characters written
        logme.debug(String.format("End read of %d chars from %d in buf size %d; read %d characters.",len, from, buf.length, (writeloc - from)));
		return (writeloc - from);
		*/
		
	}
	
	@Override
	/**
	 * This method will continuously read lines from the origin stream, passing them to the 
	 * underlying filter process until the filter process returns a line to return.
	 */
	public String readLine() throws IOException {
		logme.debug("Begin readLine");

		/*
		String line = null;
		int bufsize = 1024;
		char[] readback = new char[bufsize];
		StringBuilder toReturn = new StringBuilder();
		int chars = 0;
		*/

		// First, read a line from the origin stream:

		// We assume the filter process will only return one line:
		// If the read state is not ready, the line didn't match - return null
		logme.debug(String.format("Blocking queue size is: %d", ClassLineBuffer.size()));
		
		// TODO: At some point we may want to empty the stream.
		if (treader.isInterrupted()){
			logme.info(String.format("treader has been interrupted; exiting", ClassLineBuffer.size()));
			twriter.interrupt();
			// If we still have elements in our buffer, we can continue to service requests
		}
		if (twriter.isInterrupted()){
			logme.info(String.format("treader has been interrupted; queue size is: %d", ClassLineBuffer.size()));
			treader.interrupt();
			// If we still have elements in our buffer, we can continue to service requests
		}
		
		// Buffered Version
		String toReturn;
		try {
			logme.debug("Reading next line from queue buffer:");
			toReturn = ClassLineBuffer.take();
		} catch (InterruptedException e) {
			logme.info(String.format("LineBuffer thread interrupted; exiting"));
			close();
			throw new IOException();
		}
		logme.debug(String.format("Read line %s from queue", toReturn));
		
		/*// -- Unbuffered Version
		gin.mark(bufsize);
		char[] previous = new char[bufsize];
		while((chars = gin.read(readback)) == bufsize){
			// While there could be more to read - check for a newline:
			logme.debug(String.format("Read %d characters from stream",chars));
			char[] candidate = this.getFirstLine(readback, previous);
			toReturn.append(candidate);
			if (candidate.length < bufsize){
				// We found a newline!
				gin.reset();
				gin.skip(candidate.length + System.lineSeparator().length());
				logme.debug(String.format("End readLine (returning %s)", toReturn));
				return toReturn.toString();
			}
			gin.mark(bufsize);
		}
		*/
		logme.debug(String.format("End readLine (returning %s)", toReturn));
		return toReturn;
	}

	@Override
	public void close() throws IOException {
		super.close();
		logme.debug("Begin close");
		//logme.debug("Interrupting Threads...");
		
		/* TODO: Close threads gracefully:
		treader.close();
		twriter.close();
		terror.close();
		*/
		logme.debug("Joining Threads...");
		try{
			twriter.close();
			twriter.join(1000);
			treader.close();
			treader.join(1000);
			terror.close();
			terror.join(1000);
		} catch (InterruptedException e){
			logme.info("Interrupted joining threads",e);
		}

		logme.debug("Waiting for the process:");
		int exitval;
		try {
			exitval = ourproc.waitFor();
		} catch (InterruptedException e1) {
			logme.error("Interrupted waiting for filter process to terminate: ", e1);
			ourproc.destroy();
			exitval = ourproc.exitValue();
		}

		logme.debug(String.format("Process exited with code: %d",exitval));
		logme.debug("End close");
	}
	
	@Override
	public boolean markSupported(){
		boolean val = super.markSupported();
		logme.debug("Begin markSupported");
		logme.debug(String.format("End markSupported (returning %b)", val));
		return val;
	}
	
	@Override
	public int read() throws IOException{
		int retVal = super.read();
		logme.debug("Begin read");
		logme.debug(String.format("End read (%d)", retVal));
		return retVal;
	}
	
	@Override
	public boolean ready() throws IOException{
		boolean retVal = super.ready();
		logme.debug("Begin ready");
		logme.debug(String.format("End ready (%b)", retVal));
		return retVal;
	}
	
	@Override
	public void reset() throws IOException {
		super.reset();
		logme.debug("Begin reset");
		logme.debug("End reset");
	}
	
	@Override
	public long skip(long n) throws IOException{
		long retVal = super.skip(n);
		logme.debug(String.format("Begin skip (%d)", n));
		logme.debug("End skip (%d); returning %d", n, retVal);
		return retVal;
	}
	
	/**
	 * This method takes as input a line in form:
	 * LogEntryID,EpochTimestamp,EventID,EventCode,KVPair1[,...,KVPairN]
	 * 
	 * checks it for validity, and simply returns it with a newline attached.
	 * 
	 * @param input
	 * @return the transformed input
	 */
	private String processLine(String input){
		return String.format("%s%n",input);
	}
	
	/**
	 * Return an array of characters up to, but not including, the first newline seen.
	 * 
	 * @param c The character array to search.
	 * @param p The previously searched array, if any - to check for split newline sequences
	 */
	private char[] getFirstLine(char[] c, char[] p){
		char[] nlarray = System.lineSeparator().toCharArray();
		logme.debug(String.format("Newline array is %d characters",nlarray.length));
		char[] toReturn = new char[c.length];
		
		boolean found = false;
		int i = 0; // start nlarray.length characters - 1 back.
		int beginVal = 0;
		if (p != null){
			beginVal = 0 - nlarray.length +1;
		}
		for (i = beginVal; i < (c.length - nlarray.length + 1); i++){
			for (int j = 0; j < nlarray.length; j++){
				if (j+i < 0){
					if (p[p.length + i] == nlarray[j]){
						found = true;
					}
				} else {
				    if (c[i] == nlarray[j]){
				    	found = true;
				    }
				}
			}
			if (found){
				logme.debug(String.format("Found newline at position %d; returning %s", i, String.copyValueOf(toReturn)));
				return toReturn;
			} else {
				if (i > 0){
					toReturn[i] = c[i];
				}
			}
		}
		logme.debug(String.format("No newline found; returning %s",String.copyValueOf(toReturn)));
		return toReturn;
	}
	
	class StreamGobbler extends Thread{
		private BufferedReader gin;
		private String type;
		private BlockingQueue<String> store;
		
		public StreamGobbler(InputStream in, String ourtype, BlockingQueue<String> buf){
			gin = new BufferedReader(new InputStreamReader(in));
			type = ourtype;
			store = buf;
		}

		public void run(){
			String line;
			try {
				while ((line = gin.readLine()) != null){
		        	logme.debug(String.format("%s > StreamGobbler read line: %s", type, line));
		        	if (store != null){
		        		// Std out:
		        	    store.put(line);
		        	} else {
		        		// Std err:
		        		logme.error(line);
		        	}
		        	if (this.isInterrupted()){
		        		logme.info(String.format("%s > StreamGobbler interrupted; exiting", type));
		        		gin.close();
		        		return;
		        	}
				}
				logme.debug(String.format("Exited readline loop with null, clsoing stream",type));
				gin.close();
			} catch (IOException e){
				logme.error(String.format("%s > StreamGobbler caught IOException, terminating.", type),e);
				try {
					gin.close();
				} catch (IOException e1) {
					logme.error(String.format("%s > StreamGobbler IOException closing read stream from process.",type),e1);
				}
				return;
			} catch (InterruptedException e) {
				logme.error(String.format("%s > StreamGobbler Caught InterruptedException, terminating.", type),e);
				try {
					gin.close();
				} catch (IOException e1) {
					logme.error(String.format("%s > StreamGobbler IOException closing read stream from process.",type),e1);
				}
				return;
			}
		}
		public void close(){
			try {
				logme.debug(String.format("%s > StreamGobbler closing gin", type));
				gin.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	class StreamFeeder extends Thread {
		private BufferedWriter gout;
		private BufferedReader origin;
		private boolean endflag;
		
		public StreamFeeder(OutputStream out, BufferedReader source){
			gout = new BufferedWriter(new OutputStreamWriter(out));
			origin = source;
			endflag = false;
		}
		public void run(){
			String line;
			try {
				while ((line = origin.readLine()) != null){
		        	logme.debug(String.format("StreamFeeder read line: %s", line));

		        	String outline = String.format("%s%n",line);
		        	int outlen = outline.length();
		        	// Write the line to the filter process:
		        	logme.debug(String.format("StreamFeeder writing line of length %d: %s", outlen, outline));
		        	gout.write(outline,0,outlen);
		        	gout.newLine();
		        	gout.flush();
		        	if (this.isInterrupted()){
		        		logme.info(String.format("StreamFeeder interrupted; exiting", line));
		        		gout.close();
		        		return;
		        	}
				}
				close();
			} catch (IOException e){
				logme.error("Caught IOException, terminating.",e);
				try {
					logme.debug("StreamFeeder closing gout");
					gout.close();
				} catch (IOException e1) {
					logme.error("IOException closing write stream to process.",e1);
				}
				return;
			}
		}
		public void close(){
			try {
				origin.close();
				gout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

}
