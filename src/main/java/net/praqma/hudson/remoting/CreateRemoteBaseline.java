package net.praqma.hudson.remoting;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.UCMEntity;

import hudson.FilePath.FileCallable;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;

public class CreateRemoteBaseline implements FileCallable<String> {

	private static final long serialVersionUID = -8984877325832486334L;

	private String baseName;
	private String componentName;
	private File view;
	private BuildListener listener;
	private String username;
	
	public CreateRemoteBaseline( String baseName, String componentName, File view, String username, BuildListener listener ) {
		this.baseName = baseName;
		this.componentName = componentName;
		this.view = view;
		this.listener = listener;
		this.username = username;
    }
    
    @Override
    public String invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
        PrintStream out = listener.getLogger();
        
    	Component component = null;
    	
    	try {
			component = UCMEntity.getComponent( componentName, true );
		} catch (UCMException e) {
			throw new IOException( "Unable to get Component:" + e.getMessage() );
		}
    	
    	Baseline bl = null;
    	try {
			bl = Baseline.create( baseName, component, view, true, true );
		} catch (UCMException e) {
			throw new IOException( "Unable to create Baseline:" + e.getMessage() );
		}
    	
    	try {
    		out.println( "Changing ownership of baseline" );
			bl.changeOwnership( username, null );
		} catch (UCMException e) {
			throw new IOException( "Unable to change ownership of " + baseName + ":" + e.getMessage() );
		}
    
        return bl.getFullyQualifiedName();
    }

}
