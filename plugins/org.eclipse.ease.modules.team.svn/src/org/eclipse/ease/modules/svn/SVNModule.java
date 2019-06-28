/*******************************************************************************
 * Copyright (c) 2015 Dominic Pirker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Dominic Pirker - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.svn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.modules.platform.UIModule;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.svn.core.connector.SVNDepth;
import org.eclipse.team.svn.core.operation.CompositeOperation;
import org.eclipse.team.svn.core.operation.IActionOperation;
import org.eclipse.team.svn.core.operation.remote.CheckoutOperation;
import org.eclipse.team.svn.core.operation.remote.LocateProjectsOperation;
import org.eclipse.team.svn.core.operation.remote.management.AddRepositoryLocationOperation;
import org.eclipse.team.svn.core.operation.remote.management.SaveRepositoryLocationsOperation;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.ui.extension.ExtensionsManager;
import org.eclipse.team.svn.ui.utility.UIMonitorUtility;

/**
 * Provides functions to access and operate on SVN repositories.
 */
public class SVNModule extends AbstractScriptModule {

	/**
	 * Creates a new repository location. If the location already exists the existing location gets returned.
	 *
	 * @param rootUrl
	 *            defines the root URL of the repository
	 * @param username
	 *            username to be used
	 * @param password
	 *            password for authentication
	 * @return repository location
	 */
	@WrapToScript
	public IRepositoryLocation createRepositoryLocation(final String rootUrl, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String username,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) final String password) {

		for (final IRepositoryLocation location : SVNRemoteStorage.instance().getRepositoryLocations()) {
			if ((location.getUrlAsIs().equals(rootUrl)) || (location.getUrl().equals(rootUrl)))
				return location;
		}

		final IRepositoryLocation location = SVNRemoteStorage.instance().newRepositoryLocation();

		location.setUrl(rootUrl);
		location.setTrunkLocation("trunk");
		location.setTagsLocation("tags");
		location.setBranchesLocation("branches");
		location.setStructureEnabled(true);

		if (username != null)
			location.setUsername(username);

		if (password != null)
			location.setPassword(password);

		location.setPasswordSaved(true);

		final AddRepositoryLocationOperation operation = new AddRepositoryLocationOperation(location);
		final CompositeOperation compositeOperation = new CompositeOperation(operation.getId(), operation.getMessagesClass());
		compositeOperation.add(operation);
		compositeOperation.add(new SaveRepositoryLocationsOperation());

		executeOperation(compositeOperation);

		return location;
	}

	/**
	 * Imports project from repository location.
	 *
	 * @param rootLocation
	 *            can be a string (to generate RepositoryLocation automatically) or already a RepositoryLocation
	 * @param projectLocations
	 *            array from relative paths to project locations within the repository. When not provided all projects from trunk will be imported (not
	 *            recursive)
	 */
	@WrapToScript
	public void importProjectsFromSVN(Object rootLocation, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String[] projectLocations) {
		if (!(rootLocation instanceof IRepositoryResource))
			rootLocation = createRepositoryLocation(rootLocation.toString(), null, null);

		IRepositoryResource[] checkoutResources;
		if (projectLocations != null) {
			// checkout user defined projects
			final List<IRepositoryResource> checkoutList = new ArrayList<>();

			for (final String location : projectLocations) {
				final IRepositoryResource projectResource = SVNRemoteStorage.instance().asRepositoryResource((IRepositoryLocation) rootLocation,
						((IRepositoryLocation) rootLocation).getUrl() + "/" + location, false);
				checkoutList.add(projectResource);
			}
			checkoutResources = checkoutList.toArray(new IRepositoryResource[checkoutList.size()]);

		} else {
			// checkout all projects
			final IRepositoryResource res = (IRepositoryResource) rootLocation;
			final IRepositoryResource[] doCeckout = new IRepositoryResource[] { res };
			final LocateProjectsOperation locateProjectsOp = new LocateProjectsOperation(doCeckout,
					ExtensionsManager.getInstance().getCurrentCheckoutFactory().getLocateFilter(), 5);

			// Execute the locate project operation first, to get a list of all repository projects
			executeOperation(locateProjectsOp);
			checkoutResources = locateProjectsOp.getRepositoryResources();
		}

		if (UIModule.isHeadless()) {
			final Map<String, IRepositoryResource> operateMap = new HashMap<>();
			for (final IRepositoryResource resource : checkoutResources)
				operateMap.put(resource.getName(), resource);

			final IActionOperation operation = new CheckoutOperation(operateMap, true, null, SVNDepth.INFINITY, false);
			executeOperation(operation);

		} else {
			Display.getDefault().syncExec(() -> {
				final IActionOperation operation = ExtensionsManager.getInstance().getCurrentCheckoutFactory()
						.getCheckoutOperation(Display.getDefault().getActiveShell(), checkoutResources, null, true, null, SVNDepth.INFINITY, false);
				UIMonitorUtility.doTaskNowDefault(operation, false);
			});
		}
	}

	/**
	 * Get the revision for a given resource.
	 *
	 * @param resource
	 *            resource to get revision for
	 * @return revision number
	 */
	@WrapToScript
	public long getRevision(final Object resource) {
		final Object file = ResourceTools.resolve(resource, getScriptEngine().getExecutedFile());
		if (file instanceof IResource) {
			final ILocalResource localResource = SVNRemoteStorage.instance().asLocalResource((IResource) file);
			return localResource.getRevision();
		}

		return -1;
	}

	/**
	 * Execute an internal SVN operation. Uses UI if available.
	 *
	 * @param operation
	 *            operation to execute
	 */
	private void executeOperation(IActionOperation operation) {
		if (UIModule.isHeadless())
			operation.run(new NullProgressMonitor());
		else
			Display.getDefault().syncExec(() -> UIMonitorUtility.doTaskNowDefault(operation, false));
	}
}
