/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.launchers;

import java.util.List;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.util.ListUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * This class is reponsible for creating a launching the Groovy shell.  If an 
 * existing launch configuration exists it will use that, if not it will
 * create a new launch configuration and launch it.
 * 
 * @see ILaunchShortcut
 */
public class GroovyShellLaunchShortcut implements ILaunchShortcut {

	/**
	 * The ID of this groovy launch configuration
	 */
	public static final String GROOVY_SHELL_LAUNCH_CONFIG_ID = "org.codehaus.groovy.eclipse.groovyShellLaunchConfiguration" ; 
	
	/**
	 * Used for dialog presentation if the used needs to choose from
	 * matching Launch configurations
	 */
	public static final String SELECT_CONFIG_DIALOG_TITLE = "Select Groovy Shell Launch" ;
	
	/**
	 * Used for dialog presentation if the used needs to choose from
	 * matching Launch configurations
	 */
	public static final String SELECT_CONFIG_DIALOG_TEXT = "Please select the Groovy Shell run configuration to Launch" ;

	/**
	 * This is the string that will show if the groovy file the user is trying to run 
	 * doesn't meet the criteria to be run.
	 */
	public static final String GROOVY_FILE_NOT_RUNNABLE_MESSAGE = "The groovy shell could not be run.";
	
	/**
	 * Launches from the package explorer.
	 * 
	 * @see ILaunchShortcut#launch
	 */
	public void launch(ISelection selection, String mode)  {
		if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).getFirstElement() instanceof ICompilationUnit) {
			IStructuredSelection structredSelection = (IStructuredSelection) selection;
			IJavaElement elt = (IJavaElement) structredSelection.getFirstElement(); 
			launchGroovy(elt.getJavaProject(), mode);
		}
	}

	/**
	 * Launches from the source file.
	 * 
	 * @see ILaunchShortcut#launch
	 */
	public void launch(IEditorPart editor, String mode)  {
	       // make sure we are saved as we run groovy from the file
        editor.getEditorSite().getPage().saveEditor(editor,false);
        IEditorInput input = editor.getEditorInput();
        IFile file = (IFile) input.getAdapter(IFile.class);
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        if (unit.getJavaProject() != null) {
            launchGroovy(unit.getJavaProject(), mode);
        }

	}
	
	private void launchGroovy(IJavaProject project, String mode) {
        String className = groovy.ui.InteractiveShell.class.getName();
        
        try {
            String launchName = getLaunchManager().generateUniqueLaunchConfigurationNameFrom(project.getProject().getName());
            ILaunchConfigurationWorkingCopy launchConfig = 
                getGroovyLaunchConfigType().newInstance(null, launchName);
            
            launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, className);
            launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getElementName());
            List<String> classpath = ListUtil.newList(JavaRuntime.computeDefaultRuntimeClassPath(project));
            classpath.add(0, GroovyShellLaunchDelegate.getPathTo("jline-*.jar"));
            classpath.add(0, GroovyShellLaunchDelegate.getPathTo("antlr-*.jar"));
            classpath.add(0, GroovyShellLaunchDelegate.getPathTo("commons-cli-*.jar"));
            launchConfig.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
            
            DebugUITools.launch(launchConfig, "run");
            
        } catch (Exception e) {
            GroovyPlugin.getDefault().logException("Exception launching Groovy Console", e);
        }
	}

    /**
     * This is a convenience method for getting the Groovy launch configuration
     * type from the Launch Manager.
     * 
     * @return Returns the ILaunchConfigurationType for running Groovy classes.
     */
    public static ILaunchConfigurationType getGroovyLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(GROOVY_SHELL_LAUNCH_CONFIG_ID) ;
    }

    /**
     * This is a convenince method for getting the Launch Manager from 
     * the Debug plugin.
     * 
     * @return Returns the default Eclipse launch manager.
     */
    public static ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager() ;
    }

}
