package net.praqma.hudson.test.integration.self;

import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.model.Slave;
import hudson.scm.PollingResult;
import hudson.triggers.SCMTrigger;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.hudson.test.BaseTestClass;
import net.praqma.hudson.test.SystemValidator;
import net.praqma.util.test.junit.DescriptionRule;
import net.praqma.util.test.junit.TestDescription;
import net.praqma.util.debug.Logger;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import net.praqma.hudson.scm.pollingmode.PollChildMode;
import net.praqma.hudson.scm.pollingmode.PollSelfMode;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 * User: cwolfgang
 * Date: 08-11-12
 * Time: 22:12
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AnyIT extends BaseTestClass {

    @Rule
    public ClearCaseRule ccenv = new ClearCaseRule( "ccucm" );

    @Rule
    public DescriptionRule desc = new DescriptionRule();

    private static final Logger logger = Logger.getLogger();

    public AbstractBuild<?, ?> initiateBuild( String projectName, boolean recommend, boolean tag, boolean description, boolean fail ) throws Exception {
        PollSelfMode mode = new PollSelfMode("ANY");
        return jenkins.initiateBuild( projectName, mode, "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob(), recommend, tag, description, fail, false, "");
    }

    @Test
    @ClearCaseUniqueVobName( name = "self-any" )
    @TestDescription( title = "Self polling", text = "baselines available, find the newest" )
    public void test() throws Exception {
        AbstractBuild<?, ?> build = initiateBuild( ccenv.getUniqueName(), false, false, false, false );

        Baseline baseline = ccenv.context.baselines.get( "client-3" );
        SystemValidator validator = new SystemValidator( build )
                .validateBuild( Result.SUCCESS )
                .validateBuiltBaseline( Project.PromotionLevel.INITIAL, baseline, false )
                .validate();
    }

    @Test
    @ClearCaseUniqueVobName( name = "self-any-recommend" )
    @TestDescription( title = "Self polling", text = "baselines available, find the newest" )
    public void testRecommend() throws Exception {
        AbstractBuild<?, ?> build = initiateBuild( ccenv.getUniqueName(), true, false, false, false );

        Baseline baseline = ccenv.context.baselines.get( "client-3" );
        SystemValidator validator = new SystemValidator( build )
                .validateBuild( Result.SUCCESS )
                .validateBuiltBaseline( Project.PromotionLevel.INITIAL, baseline, true )
                .validate();
    }

    @Test
    @ClearCaseUniqueVobName( name = "self-any-poll" )
    @TestDescription( title = "Self polling", text = "baselines available, find the newest, poll" )
    public void testPoll() throws Exception {
        PollSelfMode mode = new PollSelfMode("ANY");
        FreeStyleProject project = jenkins.setupProjectWithASlave( "polling-test-with-baselines-" + ccenv.getUniqueName(), mode, "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob(), false, false, false, false, "");
                
        /* BUILD 1 */
        AbstractBuild<?, ?> build = null;
        try {
            build = project.scheduleBuild2( 0, new SCMTrigger.SCMTriggerCause("Triggered for testing") ).get();
        } catch( Exception e ) {
            logger.info( "Build failed: " + e.getMessage() );
        }

        Baseline baseline = ccenv.context.baselines.get( "client-3" );
        SystemValidator validator = new SystemValidator( build )
                .validateBuild( Result.SUCCESS )
                .validateBuiltBaseline( Project.PromotionLevel.INITIAL, baseline, false )
                .validate();

        PollingResult result = project.poll( jenkins.createTaskListener() );
        assertThat( result, is( PollingResult.NO_CHANGES ) );
    }

    @Test
    @ClearCaseUniqueVobName( name = "self-any-poll2" )
    @TestDescription( title = "Self polling", text = "baselines available, find the newest, add baselines, poll" )
    public void testPollThree() throws Exception {
        PollSelfMode mode = new PollSelfMode("ANY");
        FreeStyleProject project = jenkins.setupProjectWithASlave( "polling-test-with-baselines-" + ccenv.getUniqueName(), mode, "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob(), false, false, false, false, "");

        /* BUILD 1 */
        AbstractBuild<?, ?> build = null;
        try {
            build = project.scheduleBuild2( 0, new SCMTrigger.SCMTriggerCause("Triggered for testing") ).get();
        } catch( Exception e ) {
            logger.info( "Build failed: " + e.getMessage() );
        }

        Baseline baseline = ccenv.context.baselines.get( "client-3" );
        new SystemValidator( build )
                .validateBuild( Result.SUCCESS )
                .validateBuiltBaseline( Project.PromotionLevel.INITIAL, baseline, false )
                .validate();

        PollingResult result = project.poll( jenkins.createTaskListener() );
        assertThat( result, is( PollingResult.NO_CHANGES ) );

        Stream stream = ccenv.context.streams.get( "one_int" );

        String viewtag = ccenv.getUniqueName() + "_one_int";
        File path = ccenv.setDynamicActivity( stream, viewtag, "any-act" );
        Baseline b1 = getNewBaseline( path, "1.txt", "ANY-1" );
        Baseline b2 = getNewBaseline( path, "2.txt", "ANY-2" );

        result = project.poll( jenkins.createTaskListener() );
        assertThat( result, is( PollingResult.BUILD_NOW ) );

        /* BUILD 2 */
        AbstractBuild<?, ?> build2 = null;
        try {
            build2 = project.scheduleBuild2( 0, new SCMTrigger.SCMTriggerCause("Triggered for testing") ).get();
        } catch( Exception e ) {
            logger.info( "Build failed: " + e.getMessage() );
        }

        new SystemValidator( build2 )
                .validateBuild( Result.SUCCESS )
                .validateBuiltBaseline( Project.PromotionLevel.INITIAL, b2, false )
                .validate();
    }




    protected Baseline getNewBaseline( File path, String filename, String bname ) throws ClearCaseException {

        try {
            ccenv.addNewElement( ccenv.context.components.get( "Model" ), path, filename );
        } catch( ClearCaseException e ) {
            ExceptionUtils.print( e, System.out, true );
        }
        return Baseline.create( bname, ccenv.context.components.get( "_System" ), path, Baseline.LabelBehaviour.FULL, false );
    }
}