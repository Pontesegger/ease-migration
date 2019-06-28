/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.CompositeCommand;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.commands.ICreationCommand;
import org.eclipse.papyrus.commands.OpenDiagramCommand;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.gmfdiag.navigation.CreatedNavigableElement;
import org.eclipse.papyrus.infra.gmfdiag.navigation.ExistingNavigableElement;
import org.eclipse.papyrus.infra.gmfdiag.navigation.NavigableElement;
import org.eclipse.papyrus.infra.gmfdiag.navigation.NavigationHelper;
import org.eclipse.papyrus.infra.services.controlmode.ControlModeManager;
import org.eclipse.papyrus.infra.services.controlmode.ControlModeRequest;
import org.eclipse.papyrus.infra.services.controlmode.IControlModeManager;
import org.eclipse.papyrus.uml.diagram.clazz.ClassDiagramCreationCondition;
import org.eclipse.papyrus.uml.diagram.clazz.CreateClassDiagramCommand;
import org.eclipse.uml2.uml.Element;

/**
 * Module used to interact with Papyrus Editor.
 */
public class PapyrusModule extends AbstractScriptModule {

	private EcoreModule getEcoreModule() {
		return getEnvironment().getModule(EcoreModule.class);
	}

	private UMLModule getUMLModule() {
		return getEnvironment().getModule(UMLModule.class);
	}

	private NotationModule getNotationModule() {
		return getEnvironment().getModule(NotationModule.class);
	}

	/**
	 * Return the model set (ResourceSet) of the current model open in Papyrus.
	 *
	 * @return current model set
	 */
	@WrapToScript
	public ModelSet getModelSet() {
		final EditingDomain editingDomain = TransactionUtil.getEditingDomain(getUMLModule().getModel());
		if (editingDomain == null) {
			Logger.error(Activator.PLUGIN_ID, "Unable to get the editing domain");
			return null;
		}
		final ResourceSet resourceSet = editingDomain.getResourceSet();
		if (resourceSet instanceof ModelSet) {
			return (ModelSet) resourceSet;

		}
		Logger.error(Activator.PLUGIN_ID, "The resource set is not a model set");
		return null;
	}

	@Override
	public void initialize(final IScriptEngine engine, final IEnvironment environment) {
		super.initialize(engine, environment);
		getNotationModule().initialize(engine, environment);
	}

	/**
	 * Return the select view element (Notation metamodel).
	 *
	 * @return selection view or <code>null</code>
	 */
	@WrapToScript
	public View getSelectionView() {
		final EObject v = getNotationModule().getSelection();
		if (v instanceof View) {
			return (View) v;
		}

		return null;
	}

	/**
	 * Return the UML element from the selection.
	 *
	 * @return UML element or <code>null</code>
	 */
	@WrapToScript
	public Element getSelectionElement() {
		final EObject elem = getEcoreModule().getSelection();
		if (elem instanceof Element) {
			return (Element) elem;
		}

		return null;
	}

	/**
	 * Create a new empty diagram. WARNING: For now only Class diagrams are implemented.
	 *
	 * @param semanticElement
	 *            UML or Sysml element of the diagram
	 * @param diagramType
	 *            currently only <i>Class</i> is supported
	 * @param diagramName
	 *            The name of the diagram (Optional set the name to newDiagram)
	 * @param open
	 *            <code>true</code> if the diagram shall be opened
	 */
	@WrapToScript
	public void createDiagram(final EObject semanticElement, @ScriptParameter(defaultValue = "Class") final String diagramType,
			@ScriptParameter(defaultValue = "NewDiagram") final String diagramName, @ScriptParameter(defaultValue = "false") final boolean open) {
		if ("Class".equals(diagramType)) {
			createDiagram(getModelSet(), new CreateClassDiagramCommand(), new ClassDiagramCreationCondition(), semanticElement, diagramName, open);
		}
	}

	/**
	 * Use the control function of papyrus. That is to say that all contained element diagrams will be stored in a different resource.
	 *
	 * @param semanticElement
	 *            The semantic element to control (That is to say an UML element)
	 * @param fileName
	 *            The name of the new file
	 */
	@WrapToScript
	public void control(final EObject semanticElement, String fileName) {
		if (fileName == null) {
			fileName = semanticElement.eResource().getURIFragment(semanticElement);
		}
		final URI baseURI = semanticElement.eResource().getURI();
		final URI createURI = baseURI.trimSegments(1).appendSegment(fileName + ".uml");
		final ControlModeRequest controlRequest = ControlModeRequest.createUIControlModelRequest(getEditingDomain(), semanticElement, createURI);
		controlRequest.setIsUIAction(false);
		final IControlModeManager controlMng = ControlModeManager.getInstance();
		final ICommand controlCommand = controlMng.getControlCommand(controlRequest);
		getEditingDomain().getCommandStack().execute(new GMFtoEMFCommandWrapper(controlCommand));
	}

	private void createDiagram(final ModelSet modelSet, final ICreationCommand creationCommand, final ClassDiagramCreationCondition creationCondition,
			final EObject target, final String diagramName, final boolean openDiagram) {
		final NavigableElement navElement = getNavigableElementWhereToCreateDiagram(creationCondition, target);
		if ((navElement != null) && (modelSet != null)) {
			final CompositeCommand command = getLinkCreateAndOpenNavigableDiagramCommand(navElement, creationCommand, diagramName, modelSet, openDiagram);
			// modelSet.getTransactionalEditingDomain().getCommandStack().execute(new GMFtoEMFCommandWrapper(command));
			try {
				command.execute(new NullProgressMonitor(), null);
			} catch (final ExecutionException e) {
				e.printStackTrace();
			}

		}
	}

	private NavigableElement getNavigableElementWhereToCreateDiagram(final ClassDiagramCreationCondition creationCondition, final EObject selectedElement) {

		if (selectedElement != null) {
			// First check if the current element can host the requested diagram
			if (creationCondition.create(selectedElement)) {
				return new ExistingNavigableElement(selectedElement, null);
			} else {
				final List<NavigableElement> navElements = NavigationHelper.getInstance().getAllNavigableElements(selectedElement);
				// this will sort elements by navigation depth
				Collections.sort(navElements);

				for (final NavigableElement navElement : navElements) {
					// ignore existing elements because we want a hierarchy to
					// be created if it is not on the current element
					if ((navElement instanceof CreatedNavigableElement) && creationCondition.create(navElement.getElement())) {
						return navElement;
					}
				}
			}
		}
		return null;
	}

	protected TransactionalEditingDomain getEditingDomain() {
		return (TransactionalEditingDomain) getEcoreModule().getEditingDomain();
	}

	public static CompositeCommand getLinkCreateAndOpenNavigableDiagramCommand(final NavigableElement navElement,
			final ICreationCommand creationCommandInterface, final String diagramName, final ModelSet modelSet, final boolean openDiagram) {
		final CompositeCommand compositeCommand = new CompositeCommand("Create diagram");

		if (navElement instanceof CreatedNavigableElement) {
			compositeCommand.add(new AbstractTransactionalCommand(modelSet.getTransactionalEditingDomain(), "Create hierarchy", null) {

				@Override
				protected CommandResult doExecuteWithResult(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
					NavigationHelper.linkToModel((CreatedNavigableElement) navElement);
					NavigationHelper.setBaseName((CreatedNavigableElement) navElement, "");
					return CommandResult.newOKCommandResult();
				}
			});
		}

		final ICommand createDiagCommand = creationCommandInterface.getCreateDiagramCommand(modelSet, navElement.getElement(), diagramName);
		compositeCommand.add(createDiagCommand);
		if (openDiagram) {
			compositeCommand.add(new OpenDiagramCommand(modelSet.getTransactionalEditingDomain(), createDiagCommand));
		}

		return compositeCommand;
	}

	/**
	 * Return if the current instance is a instance of an EClass define by its name. Will look into UML and Notation metamodel.
	 *
	 * @param eObject
	 *            The {@link EObject} you want to test.
	 * @param type
	 *            The name of the EClass defined in the metamodel
	 * @return <code>true</code> if the {@link EObject} is instance of typeName
	 */
	@WrapToScript
	public boolean eInstanceOf(final EObject eObject, final String type) {
		EClassifier classifier = getEcoreModule().getEPackage().getEClassifier(type);
		if (classifier == null) {
			classifier = getEcoreModule().getEPackage().getEClassifier(type);
		}
		return classifier.isInstance(eObject);
	}
}
