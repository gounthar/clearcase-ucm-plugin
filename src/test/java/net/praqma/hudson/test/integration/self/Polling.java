package net.praqma.hudson.test.integration.self;

import org.mockito.Mockito;

import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.scm.PollingResult;
import net.praqma.clearcase.test.junit.CoolTestCase;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.hudson.test.CCUCMTestCase;
import net.praqma.util.debug.Logger;

public class Polling extends CCUCMTestCase {

	private static Logger logger = Logger.getLogger();

	public void testPollingSelfWithBaselines() throws Exception {
		String un = setupCC( "testPollingSelfWithBaselines", false );
		FreeStyleProject project = setupProject( "polling-test-with-baselines-" + un, "self", "one_int@" + coolTest.getPVob(), false, false, false, false );
		
		TaskListener tasklistener = Mockito.mock( TaskListener.class );
		
		/* Behaviour */
		Mockito.when( tasklistener.getLogger() ).thenReturn( System.out );
		
		PollingResult result = project.poll( tasklistener );
		
		assertTrue( result.hasChanges() );
		testCCUCMPolling( project );
	}
	
	public void testPollingSelfWithNoBaselines() throws Exception {
		String un = setupCC( "testPollingSelfWithNoBaselines", false );
		FreeStyleProject project = setupProject( "polling-test-with-baselines-" + un, "self", "one_dev@" + coolTest.getPVob(), false, false, false, false );
		
		TaskListener tasklistener = Mockito.mock( TaskListener.class );
		
		/* Behaviour */
		Mockito.when( tasklistener.getLogger() ).thenReturn( System.out );
		
		PollingResult result = project.poll( tasklistener );
		
		assertFalse( result.hasChanges() );
	}
	
}
