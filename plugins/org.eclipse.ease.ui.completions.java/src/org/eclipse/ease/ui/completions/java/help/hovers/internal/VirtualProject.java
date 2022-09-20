/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completions.java.help.hovers.internal;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class VirtualProject implements IProject {

	@Override
	public boolean exists(final IPath path) {
		throw new UnsupportedOperationException("exists not implemented");
	}

	@Override
	public IResource findMember(final String path) {
		throw new UnsupportedOperationException("findMember not implemented");
	}

	@Override
	public IResource findMember(final String path, final boolean includePhantoms) {
		throw new UnsupportedOperationException("findMember not implemented");
	}

	@Override
	public IResource findMember(final IPath path) {
		throw new UnsupportedOperationException("findMember not implemented");
	}

	@Override
	public IResource findMember(final IPath path, final boolean includePhantoms) {
		throw new UnsupportedOperationException("findMember not implemented");
	}

	@Override
	public String getDefaultCharset() throws CoreException {
		// FIXME needs preferences lookup for default settings
		return Charset.defaultCharset().name();
	}

	@Override
	public String getDefaultCharset(final boolean checkImplicit) throws CoreException {
		throw new UnsupportedOperationException("getDefaultCharset not implemented");
	}

	@Override
	public IFile getFile(final IPath path) {
		throw new UnsupportedOperationException("getFile not implemented");
	}

	@Override
	public IFolder getFolder(final IPath path) {
		throw new UnsupportedOperationException("getFolder not implemented");
	}

	@Override
	public IResource[] members() throws CoreException {
		throw new UnsupportedOperationException("members not implemented");
	}

	@Override
	public IResource[] members(final boolean includePhantoms) throws CoreException {
		throw new UnsupportedOperationException("members not implemented");
	}

	@Override
	public IResource[] members(final int memberFlags) throws CoreException {
		throw new UnsupportedOperationException("members not implemented");
	}

	@Override
	public IFile[] findDeletedMembersWithHistory(final int depth, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("findDeletedMembersWithHistory not implemented");
	}

	@Override
	public void setDefaultCharset(final String charset) throws CoreException {
		throw new UnsupportedOperationException("setDefaultCharset not implemented");
	}

	@Override
	public void setDefaultCharset(final String charset, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("setDefaultCharset not implemented");
	}

	@Override
	public IResourceFilterDescription createFilter(final int type, final FileInfoMatcherDescription matcherDescription, final int updateFlags,
			final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("createFilter not implemented");
	}

	@Override
	public IResourceFilterDescription[] getFilters() throws CoreException {
		throw new UnsupportedOperationException("getFilters not implemented");
	}

	@Override
	public void accept(final IResourceProxyVisitor visitor, final int memberFlags) throws CoreException {
		throw new UnsupportedOperationException("accept not implemented");
	}

	@Override
	public void accept(final IResourceProxyVisitor visitor, final int depth, final int memberFlags) throws CoreException {
		throw new UnsupportedOperationException("accept not implemented");
	}

	@Override
	public void accept(final IResourceVisitor visitor) throws CoreException {
		throw new UnsupportedOperationException("accept not implemented");
	}

	@Override
	public void accept(final IResourceVisitor visitor, final int depth, final boolean includePhantoms) throws CoreException {
		throw new UnsupportedOperationException("accept not implemented");
	}

	@Override
	public void accept(final IResourceVisitor visitor, final int depth, final int memberFlags) throws CoreException {
		throw new UnsupportedOperationException("accept not implemented");
	}

	@Override
	public void clearHistory(final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("clearHistory not implemented");
	}

	@Override
	public void copy(final IPath destination, final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("copy not implemented");
	}

	@Override
	public void copy(final IPath destination, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("copy not implemented");
	}

	@Override
	public void copy(final IProjectDescription description, final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("copy not implemented");
	}

	@Override
	public void copy(final IProjectDescription description, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("copy not implemented");
	}

	@Override
	public IMarker createMarker(final String type) throws CoreException {
		throw new UnsupportedOperationException("createMarker not implemented");
	}

	@Override
	public IResourceProxy createProxy() {
		throw new UnsupportedOperationException("createProxy not implemented");
	}

	@Override
	public void delete(final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("delete not implemented");
	}

	@Override
	public void delete(final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("delete not implemented");
	}

	@Override
	public void deleteMarkers(final String type, final boolean includeSubtypes, final int depth) throws CoreException {
		throw new UnsupportedOperationException("deleteMarkers not implemented");
	}

	@Override
	public boolean exists() {
		throw new UnsupportedOperationException("exists not implemented");
	}

	@Override
	public IMarker findMarker(final long id) throws CoreException {
		throw new UnsupportedOperationException("findMarker not implemented");
	}

	@Override
	public IMarker[] findMarkers(final String type, final boolean includeSubtypes, final int depth) throws CoreException {
		return new IMarker[0];
	}

	@Override
	public int findMaxProblemSeverity(final String type, final boolean includeSubtypes, final int depth) throws CoreException {
		throw new UnsupportedOperationException("findMaxProblemSeverity not implemented");
	}

	@Override
	public String getFileExtension() {
		throw new UnsupportedOperationException("getFileExtension not implemented");
	}

	@Override
	public IPath getFullPath() {
		return ResourcesPlugin.getWorkspace().getRoot().getFullPath();
	}

	@Override
	public long getLocalTimeStamp() {
		throw new UnsupportedOperationException("getLocalTimeStamp not implemented");
	}

	@Override
	public IPath getLocation() {
		return null;
	}

	@Override
	public URI getLocationURI() {
		throw new UnsupportedOperationException("getLocationURI not implemented");
	}

	@Override
	public IMarker getMarker(final long id) {
		throw new UnsupportedOperationException("getMarker not implemented");
	}

	@Override
	public long getModificationStamp() {
		throw new UnsupportedOperationException("getModificationStamp not implemented");
	}

	@Override
	public String getName() {
		return "EASE Java Help Project (virtual)";
	}

	@Override
	public IPathVariableManager getPathVariableManager() {
		throw new UnsupportedOperationException("getPathVariableManager not implemented");
	}

	@Override
	public IContainer getParent() {
		throw new UnsupportedOperationException("getParent not implemented");
	}

	@Override
	public Map<QualifiedName, String> getPersistentProperties() throws CoreException {
		throw new UnsupportedOperationException("getPersistentProperties not implemented");
	}

	@Override
	public String getPersistentProperty(final QualifiedName key) throws CoreException {
		throw new UnsupportedOperationException("getPersistentProperty not implemented");
	}

	@Override
	public IProject getProject() {
		throw new UnsupportedOperationException("getProject not implemented");
	}

	@Override
	public IPath getProjectRelativePath() {
		throw new UnsupportedOperationException("getProjectRelativePath not implemented");
	}

	@Override
	public IPath getRawLocation() {
		throw new UnsupportedOperationException("getRawLocation not implemented");
	}

	@Override
	public URI getRawLocationURI() {
		throw new UnsupportedOperationException("getRawLocationURI not implemented");
	}

	@Override
	public ResourceAttributes getResourceAttributes() {
		throw new UnsupportedOperationException("getResourceAttributes not implemented");
	}

	@Override
	public Map<QualifiedName, Object> getSessionProperties() throws CoreException {
		throw new UnsupportedOperationException("getSessionProperties not implemented");
	}

	@Override
	public Object getSessionProperty(final QualifiedName key) throws CoreException {
		throw new UnsupportedOperationException("getSessionProperty not implemented");
	}

	@Override
	public int getType() {
		return PROJECT;
	}

	@Override
	public IWorkspace getWorkspace() {
		throw new UnsupportedOperationException("getWorkspace not implemented");
	}

	@Override
	public boolean isAccessible() {
		return true;
	}

	@Override
	public boolean isDerived() {
		throw new UnsupportedOperationException("isDerived not implemented");
	}

	@Override
	public boolean isDerived(final int options) {
		throw new UnsupportedOperationException("isDerived not implemented");
	}

	@Override
	public boolean isHidden() {
		throw new UnsupportedOperationException("isHidden not implemented");
	}

	@Override
	public boolean isHidden(final int options) {
		throw new UnsupportedOperationException("isHidden not implemented");
	}

	@Override
	public boolean isLinked() {
		throw new UnsupportedOperationException("isLinked not implemented");
	}

	@Override
	public boolean isVirtual() {
		throw new UnsupportedOperationException("isVirtual not implemented");
	}

	@Override
	public boolean isLinked(final int options) {
		throw new UnsupportedOperationException("isLinked not implemented");
	}

	@Override
	public boolean isLocal(final int depth) {
		throw new UnsupportedOperationException("isLocal not implemented");
	}

	@Override
	public boolean isPhantom() {
		throw new UnsupportedOperationException("isPhantom not implemented");
	}

	@Override
	public boolean isReadOnly() {
		throw new UnsupportedOperationException("isReadOnly not implemented");
	}

	@Override
	public boolean isSynchronized(final int depth) {
		throw new UnsupportedOperationException("isSynchronized not implemented");
	}

	@Override
	public boolean isTeamPrivateMember() {
		throw new UnsupportedOperationException("isTeamPrivateMember not implemented");
	}

	@Override
	public boolean isTeamPrivateMember(final int options) {
		throw new UnsupportedOperationException("isTeamPrivateMember not implemented");
	}

	@Override
	public void move(final IPath destination, final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("move not implemented");
	}

	@Override
	public void move(final IPath destination, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("move not implemented");
	}

	@Override
	public void move(final IProjectDescription description, final boolean force, final boolean keepHistory, final IProgressMonitor monitor)
			throws CoreException {
		throw new UnsupportedOperationException("move not implemented");
	}

	@Override
	public void move(final IProjectDescription description, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("move not implemented");
	}

	@Override
	public void refreshLocal(final int depth, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("refreshLocal not implemented");
	}

	@Override
	public void revertModificationStamp(final long value) throws CoreException {
		throw new UnsupportedOperationException("revertModificationStamp not implemented");
	}

	@Override
	public void setDerived(final boolean isDerived) throws CoreException {
		throw new UnsupportedOperationException("setDerived not implemented");
	}

	@Override
	public void setDerived(final boolean isDerived, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("setDerived not implemented");
	}

	@Override
	public void setHidden(final boolean isHidden) throws CoreException {
		throw new UnsupportedOperationException("setHidden not implemented");
	}

	@Override
	public void setLocal(final boolean flag, final int depth, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("setLocal not implemented");
	}

	@Override
	public long setLocalTimeStamp(final long value) throws CoreException {
		throw new UnsupportedOperationException("setLocalTimeStamp not implemented");
	}

	@Override
	public void setPersistentProperty(final QualifiedName key, final String value) throws CoreException {
		throw new UnsupportedOperationException("setPersistentProperty not implemented");
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		throw new UnsupportedOperationException("setReadOnly not implemented");
	}

	@Override
	public void setResourceAttributes(final ResourceAttributes attributes) throws CoreException {
		throw new UnsupportedOperationException("setResourceAttributes not implemented");
	}

	@Override
	public void setSessionProperty(final QualifiedName key, final Object value) throws CoreException {
		throw new UnsupportedOperationException("setSessionProperty not implemented");
	}

	@Override
	public void setTeamPrivateMember(final boolean isTeamPrivate) throws CoreException {
		throw new UnsupportedOperationException("setTeamPrivateMember not implemented");
	}

	@Override
	public void touch(final IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public Object getAdapter(final Class adapter) {
		throw new UnsupportedOperationException("getAdapter not implemented");
	}

	@Override
	public boolean contains(final ISchedulingRule rule) {
		throw new UnsupportedOperationException("contains not implemented");
	}

	@Override
	public boolean isConflicting(final ISchedulingRule rule) {
		throw new UnsupportedOperationException("isConflicting not implemented");
	}

	@Override
	public void build(final int kind, final String builderName, final Map<String, String> args, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("build not implemented");
	}

	@Override
	public void build(final int kind, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("build not implemented");
	}

	@Override
	public void build(final IBuildConfiguration config, final int kind, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("build not implemented");
	}

	@Override
	public void close(final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("close not implemented");
	}

	@Override
	public void create(final IProjectDescription description, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("create not implemented");
	}

	@Override
	public void create(final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("create not implemented");
	}

	@Override
	public void create(final IProjectDescription description, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("create not implemented");
	}

	@Override
	public void delete(final boolean deleteContent, final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("delete not implemented");
	}

	@Override
	public IBuildConfiguration getActiveBuildConfig() throws CoreException {
		throw new UnsupportedOperationException("getActiveBuildConfig not implemented");
	}

	@Override
	public IBuildConfiguration getBuildConfig(final String configName) throws CoreException {
		throw new UnsupportedOperationException("getBuildConfig not implemented");
	}

	@Override
	public IBuildConfiguration[] getBuildConfigs() throws CoreException {
		throw new UnsupportedOperationException("getBuildConfigs not implemented");
	}

	@Override
	public IContentTypeMatcher getContentTypeMatcher() throws CoreException {
		throw new UnsupportedOperationException("getContentTypeMatcher not implemented");
	}

	@Override
	public IProjectDescription getDescription() throws CoreException {
		return new VirtualProjectDescription(this);
	}

	@Override
	public IFile getFile(final String name) {
		if (".classpath".equals(name))
			return new VirtualClasspathFile();

		throw new UnsupportedOperationException("getFile not implemented");
	}

	@Override
	public IFolder getFolder(final String name) {
		throw new UnsupportedOperationException("getFolder not implemented");
	}

	@Override
	public IProjectNature getNature(final String natureId) throws CoreException {
		throw new UnsupportedOperationException("getNature not implemented");
	}

	@Override
	public IPath getWorkingLocation(final String id) {
		return null;
	}

	@Override
	public IProject[] getReferencedProjects() throws CoreException {
		throw new UnsupportedOperationException("getReferencedProjects not implemented");
	}

	@Override
	public IProject[] getReferencingProjects() {
		throw new UnsupportedOperationException("getReferencingProjects not implemented");
	}

	@Override
	public IBuildConfiguration[] getReferencedBuildConfigs(final String configName, final boolean includeMissing) throws CoreException {
		throw new UnsupportedOperationException("getReferencedBuildConfigs not implemented");
	}

	@Override
	public boolean hasBuildConfig(final String configName) throws CoreException {
		throw new UnsupportedOperationException("hasBuildConfig not implemented");
	}

	@Override
	public boolean hasNature(final String natureId) throws CoreException {
		return ("org.eclipse.jdt.core.javanature".equals(natureId));
	}

	@Override
	public boolean isNatureEnabled(final String natureId) throws CoreException {
		throw new UnsupportedOperationException("isNatureEnabled not implemented");
	}

	@Override
	public boolean isOpen() {
		throw new UnsupportedOperationException("isOpen not implemented");
	}

	@Override
	public void loadSnapshot(final int options, final URI snapshotLocation, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("loadSnapshot not implemented");
	}

	@Override
	public void move(final IProjectDescription description, final boolean force, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("move not implemented");
	}

	@Override
	public void open(final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("open not implemented");
	}

	@Override
	public void open(final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("open not implemented");
	}

	@Override
	public void saveSnapshot(final int options, final URI snapshotLocation, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("saveSnapshot not implemented");
	}

	@Override
	public void setDescription(final IProjectDescription description, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("setDescription not implemented");
	}

	@Override
	public void setDescription(final IProjectDescription description, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException("setDescription not implemented");
	}

	@Override
	public void clearCachedDynamicReferences() {
		throw new UnsupportedOperationException("clearCachedDynamicReferences not implemented");
	}

	@Override
	public String getDefaultLineSeparator() throws CoreException {
		throw new UnsupportedOperationException("getDefaultLineSeparator not implemented");
	}
}
