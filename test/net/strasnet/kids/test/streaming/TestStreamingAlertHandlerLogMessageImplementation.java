/**
 * 
 */
package net.strasnet.kids.test.streaming;

import static org.junit.Assert.assertTrue;
import net.strasnet.kids.streaming.StreamingAlertHandlerLogMessageImplementation;
import net.strasnet.kids.streaming.StreamingEvent;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.semanticweb.owlapi.model.IRI;

/**
 * @author Chris Strasburg
 *
 */
//TODO: Capture and test the log output
public class TestStreamingAlertHandlerLogMessageImplementation {
	
	public static StreamingEvent e;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	    //               (IRI eventIRI, Set<IRI> sigSet, String desc, Set<DataInstance> instances){
		e = new StreamingEvent(
				IRI.create("https://www.semantiknit.com/Events/#badThing1"), 
				null, 
				"Test Event - Bad Thing #1", 
				null);
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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.strasnet.kids.streaming.StreamingAlertHandlerLogMessageImplementation#handleEvent(net.strasnet.kids.streaming.StreamingEvent)}.
	 */
	@Test
	public final void testHandleEvent() {
		StreamingAlertHandlerLogMessageImplementation handle = new StreamingAlertHandlerLogMessageImplementation();
		handle.handleEvent(e);
		assertTrue("Event " + e.getEventIRI().getFragment().toString() + " handled.", true);
	}
	
	/**
	 * Run the class from the command line:
	 * @param argv
	 */
	public static void main(String[] argv){
		JUnitCore.runClasses(TestStreamingAlertHandlerLogMessageImplementation.class);
	}

}
