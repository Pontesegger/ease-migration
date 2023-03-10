/**
 */
package org.eclipse.ease.lang.unittest.definition.provider;

import java.util.Collection;
import java.util.List;

import org.eclipse.ease.lang.unittest.definition.IDefinitionPackage;
import org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.ResourceLocator;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.emf.edit.provider.ViewerNotification;

/**
 * This is the item provider adapter for a {@link org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition} object. <!-- begin-user-doc --> <!--
 * end-user-doc -->
 * 
 * @generated
 */
public class TestSuiteDefinitionItemProvider extends ItemProviderAdapter
		implements IEditingDomainItemProvider, IStructuredItemContentProvider, ITreeItemContentProvider, IItemLabelProvider, IItemPropertySource {
	/**
	 * This constructs an instance from a factory and a notifier. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public TestSuiteDefinitionItemProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	/**
	 * This returns the property descriptors for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
		if (itemPropertyDescriptors == null) {
			super.getPropertyDescriptors(object);

			addNamePropertyDescriptor(object);
			addDescriptionPropertyDescriptor(object);
			addIncludeFilterPropertyDescriptor(object);
			addExcludeFilterPropertyDescriptor(object);
			addDisabledResourcesPropertyDescriptor(object);
			addVersionPropertyDescriptor(object);
			addResourcePropertyDescriptor(object);
		}
		return itemPropertyDescriptors;
	}

	/**
	 * This adds a property descriptor for the Name feature. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void addNamePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
				getString("_UI_TestSuiteDefinition_name_feature"),
				getString("_UI_PropertyDescriptor_description", "_UI_TestSuiteDefinition_name_feature", "_UI_TestSuiteDefinition_type"),
				IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__NAME, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Description feature. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void addDescriptionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
				getString("_UI_TestSuiteDefinition_description_feature"),
				getString("_UI_PropertyDescriptor_description", "_UI_TestSuiteDefinition_description_feature", "_UI_TestSuiteDefinition_type"),
				IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__DESCRIPTION, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Include Filter feature. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void addIncludeFilterPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
				getString("_UI_TestSuiteDefinition_includeFilter_feature"),
				getString("_UI_PropertyDescriptor_description", "_UI_TestSuiteDefinition_includeFilter_feature", "_UI_TestSuiteDefinition_type"),
				IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__INCLUDE_FILTER, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Exclude Filter feature. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void addExcludeFilterPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
				getString("_UI_TestSuiteDefinition_excludeFilter_feature"),
				getString("_UI_PropertyDescriptor_description", "_UI_TestSuiteDefinition_excludeFilter_feature", "_UI_TestSuiteDefinition_type"),
				IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__EXCLUDE_FILTER, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Disabled Resources feature. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void addDisabledResourcesPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
				getString("_UI_TestSuiteDefinition_disabledResources_feature"),
				getString("_UI_PropertyDescriptor_description", "_UI_TestSuiteDefinition_disabledResources_feature", "_UI_TestSuiteDefinition_type"),
				IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__DISABLED_RESOURCES, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null,
				null));
	}

	/**
	 * This adds a property descriptor for the Version feature. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void addVersionPropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
				getString("_UI_TestSuiteDefinition_version_feature"),
				getString("_UI_PropertyDescriptor_description", "_UI_TestSuiteDefinition_version_feature", "_UI_TestSuiteDefinition_type"),
				IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__VERSION, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
	}

	/**
	 * This adds a property descriptor for the Resource feature. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void addResourcePropertyDescriptor(Object object) {
		itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory).getRootAdapterFactory(), getResourceLocator(),
				getString("_UI_TestSuiteDefinition_resource_feature"),
				getString("_UI_PropertyDescriptor_description", "_UI_TestSuiteDefinition_resource_feature", "_UI_TestSuiteDefinition_type"),
				IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__RESOURCE, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
	}

	/**
	 * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an {@link org.eclipse.emf.edit.command.AddCommand},
	 * {@link org.eclipse.emf.edit.command.RemoveCommand} or {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
		if (childrenFeatures == null) {
			super.getChildrenFeatures(object);
			childrenFeatures.add(IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__VARIABLES);
			childrenFeatures.add(IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__CUSTOM_CODE);
			childrenFeatures.add(IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__FLAGS);
		}
		return childrenFeatures;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EStructuralFeature getChildFeature(Object object, Object child) {
		// Check the type of the specified child object and return the proper feature to use for
		// adding (see {@link AddCommand}) it as a child.

		return super.getChildFeature(object, child);
	}

	/**
	 * This returns TestSuiteDefinition.gif. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object getImage(Object object) {
		return overlayImage(object, getResourceLocator().getImage("full/obj16/TestSuiteDefinition"));
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected boolean shouldComposeCreationImage() {
		return true;
	}

	/**
	 * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String getText(Object object) {
		String label = ((ITestSuiteDefinition) object).getName();
		return label == null || label.length() == 0 ? getString("_UI_TestSuiteDefinition_type") : getString("_UI_TestSuiteDefinition_type") + " " + label;
	}

	/**
	 * This handles model notifications by calling {@link #updateChildren} to update any cached children and by creating a viewer notification, which it passes
	 * to {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void notifyChanged(Notification notification) {
		updateChildren(notification);

		switch (notification.getFeatureID(ITestSuiteDefinition.class)) {
		case IDefinitionPackage.TEST_SUITE_DEFINITION__NAME:
		case IDefinitionPackage.TEST_SUITE_DEFINITION__DESCRIPTION:
		case IDefinitionPackage.TEST_SUITE_DEFINITION__INCLUDE_FILTER:
		case IDefinitionPackage.TEST_SUITE_DEFINITION__EXCLUDE_FILTER:
		case IDefinitionPackage.TEST_SUITE_DEFINITION__DISABLED_RESOURCES:
		case IDefinitionPackage.TEST_SUITE_DEFINITION__VERSION:
		case IDefinitionPackage.TEST_SUITE_DEFINITION__RESOURCE:
			fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
			return;
		case IDefinitionPackage.TEST_SUITE_DEFINITION__VARIABLES:
		case IDefinitionPackage.TEST_SUITE_DEFINITION__CUSTOM_CODE:
		case IDefinitionPackage.TEST_SUITE_DEFINITION__FLAGS:
			fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
			return;
		}
		super.notifyChanged(notification);
	}

	/**
	 * Return the resource locator for this item provider's resources. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public ResourceLocator getResourceLocator() {
		return TestDefinitionEditPlugin.INSTANCE;
	}

}
