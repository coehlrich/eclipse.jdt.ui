/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.refactoring.reorg2;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.Assert;
import org.eclipse.jdt.internal.corext.SourceRange;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.WorkingCopyUtil;


public class ReorgUtils2 {

	//workaround for bug 18311
	private static final ISourceRange fgUnknownRange= new SourceRange(-1, 0);

	private ReorgUtils2() {
	}

	public static boolean containsOnlyProjects(List elements){
		if (elements.isEmpty())
			return false;
		for(Iterator iter= elements.iterator(); iter.hasNext(); ) {
			if (! isProject(iter.next()))
				return false;
		}
		return true;
	}
	
	public static boolean isProject(Object element){
		return (element instanceof IJavaProject) || (element instanceof IProject);
	}

	public static boolean isInsideCompilationUnit(IJavaElement element) {
		return 	!(element instanceof ICompilationUnit) && 
				element.getAncestor(IJavaElement.COMPILATION_UNIT) != null;
	}
	
	/**
	 * May be <code>null</code>.
	 */
	public static ICompilationUnit getCompilationUnit(IJavaElement javaElement){
		if (javaElement instanceof ICompilationUnit)
			return (ICompilationUnit) javaElement;
		return (ICompilationUnit) javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
	}

	/**
	 * some of the returned elements may be <code>null</code>.
	 */
	public static ICompilationUnit[] getCompilationUnits(IJavaElement[] javaElements){
		ICompilationUnit[] result= new ICompilationUnit[javaElements.length];
		for (int i= 0; i < javaElements.length; i++) {
			result[i]= getCompilationUnit(javaElements[i]);
		}
		return result;
	}
		
	public static IResource getResource(IJavaElement element){
		if (element instanceof ICompilationUnit)
			return JavaModelUtil.toOriginal((ICompilationUnit)element).getResource();
		else
			return element.getResource();
	}
	
	public static IResource[] getResources(IJavaElement[] elements) {
		IResource[] result= new IResource[elements.length];
		for (int i= 0; i < elements.length; i++) {
			result[i]= ReorgUtils2.getResource(elements[i]);
		}
		return result;
	}
	
	public static String getName(IResource resource) {
		String pattern= createNamePattern(resource);
		String[] args= createNameArguments(resource);
		return MessageFormat.format(pattern, args);
	}
	
	private static String createNamePattern(IResource resource) {
		switch(resource.getType()){
			case IResource.FILE:
				return "file ''{0}''";
			case IResource.FOLDER:
				return "folder ''{0}''";
			case IResource.PROJECT:
				return "project ''{0}''";
			default:
				Assert.isTrue(false);
				return null;
		}
	}

	private static String[] createNameArguments(IResource resource) {
		return new String[]{resource.getName()};
	}

	public static String getName(IJavaElement element) {
		String pattern= createNamePattern(element);
		String[] args= createNameArguments(element);
		return MessageFormat.format(pattern, args);
	}

	private static String[] createNameArguments(IJavaElement element) {
		switch(element.getElementType()){
			case IJavaElement.CLASS_FILE:
				return new String[]{element.getElementName()};
			case IJavaElement.COMPILATION_UNIT:
				return new String[]{element.getElementName()};
			case IJavaElement.FIELD:
				return new String[]{element.getElementName()};
			case IJavaElement.IMPORT_CONTAINER:
				return new String[0];
			case IJavaElement.IMPORT_DECLARATION:
				return new String[]{element.getElementName()};
			case IJavaElement.INITIALIZER:
				return new String[0];
			case IJavaElement.JAVA_PROJECT:
				return new String[]{element.getElementName()};
			case IJavaElement.METHOD:
				return new String[]{element.getElementName()};
			case IJavaElement.PACKAGE_DECLARATION:
				if (JavaElementUtil.isDefaultPackage(element))
					return new String[0];
				else
					return new String[]{element.getElementName()};
			case IJavaElement.PACKAGE_FRAGMENT:
				return new String[]{element.getElementName()};
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				return new String[]{element.getElementName()};
			case IJavaElement.TYPE:
				return new String[]{element.getElementName()};
			default:
				Assert.isTrue(false);
				return null;
		}
	}

	private static String createNamePattern(IJavaElement element) {
		switch(element.getElementType()){
			case IJavaElement.CLASS_FILE:
				return "class file ''{0}''";
			case IJavaElement.COMPILATION_UNIT:
				return "compilation unit ''{0}''";
			case IJavaElement.FIELD:
				return "field ''{0}''";
			case IJavaElement.IMPORT_CONTAINER:
				return "the import container";
			case IJavaElement.IMPORT_DECLARATION:
				return "import declaration ''{0}''";
			case IJavaElement.INITIALIZER:
				return "the initializer";
			case IJavaElement.JAVA_PROJECT:
				return "Java project ''{0}''";
			case IJavaElement.METHOD:
				return "method ''{0}''";
			case IJavaElement.PACKAGE_DECLARATION:
				return "package declaration ''{0}''";
			case IJavaElement.PACKAGE_FRAGMENT:
				if (JavaElementUtil.isDefaultPackage(element))
					return "the default package";
				else
					return "package fragment ''{0}''";
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				return "package fragment root ''{0}''";
			case IJavaElement.TYPE:
				return "type ''{0}''";
			default:
				Assert.isTrue(false);
				return null;
		}
	}

	public static IJavaElement toWorkingCopy(IJavaElement element){
		if (element instanceof ICompilationUnit)
			return JavaModelUtil.toWorkingCopy((ICompilationUnit)element);
		if (element instanceof IMember)
			return JavaModelUtil.toWorkingCopy((IMember)element);
		if (element instanceof IPackageDeclaration)
			return JavaModelUtil.toWorkingCopy((IPackageDeclaration)element);
		if (element instanceof IImportContainer)
			return JavaModelUtil.toWorkingCopy((IImportContainer)element);			
		if (element instanceof IImportDeclaration)
			return JavaModelUtil.toWorkingCopy((IImportDeclaration)element);	
		return element;
	}
	
	public static IJavaElement[] toWorkingCopies(IJavaElement[] javaElements){
		IJavaElement[] result= new IJavaElement[javaElements.length];
		for (int i= 0; i < javaElements.length; i++) {
			result[i]= ReorgUtils2.toWorkingCopy(javaElements[i]);
		}
		return result;
	}
	
	public static IResource[] getResources(List elements) {
		List resources= new ArrayList(elements.size());
		for (Iterator iter= elements.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof IResource)
				resources.add(element);
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}

	public static IJavaElement[] getJavaElements(List elements) {
		List resources= new ArrayList(elements.size());
		for (Iterator iter= elements.iterator(); iter.hasNext();) {
			Object element= iter.next();
			if (element instanceof IJavaElement)
				resources.add(element);
		}
		return (IJavaElement[]) resources.toArray(new IJavaElement[resources.size()]);
	}
	
	public static boolean isDeletedFromEditor(IJavaElement elem) throws JavaModelException{
		if (! isInsideCompilationUnit(elem))
			return false;
		if (elem instanceof IMember && ((IMember)elem).isBinary())
			return false;
		ICompilationUnit cu= ReorgUtils2.getCompilationUnit(elem);
		if (cu == null)
			return false;
		ICompilationUnit wc= WorkingCopyUtil.getWorkingCopyIfExists(cu);
		if (cu.equals(wc))
			return false;
		IJavaElement element= (IJavaElement)elem;
		IJavaElement wcElement= JavaModelUtil.findInCompilationUnit(wc, element);
		return wcElement == null || ! wcElement.exists();
	}
	
	public static boolean hasSourceAvailable(IMember member) throws JavaModelException{
		return ! member.isBinary() || 
				(member.getSourceRange() != null && ! fgUnknownRange.equals(member.getSourceRange()));
	}
	
	public static IResource[] setMinus(IResource[] setToRemoveFrom, IResource[] elementsToRemove) {
		Set setMinus= new HashSet(setToRemoveFrom.length - setToRemoveFrom.length);
		setMinus.addAll(Arrays.asList(setToRemoveFrom));
		setMinus.removeAll(Arrays.asList(elementsToRemove));
		return (IResource[]) setMinus.toArray(new IResource[setMinus.size()]);		
	}

	public static IJavaElement[] setMinus(IJavaElement[] setToRemoveFrom, IJavaElement[] elementsToRemove) {
		Set setMinus= new HashSet(setToRemoveFrom.length - setToRemoveFrom.length);
		setMinus.addAll(Arrays.asList(setToRemoveFrom));
		setMinus.removeAll(Arrays.asList(elementsToRemove));
		return (IJavaElement[]) setMinus.toArray(new IJavaElement[setMinus.size()]);		
	}
	
	public static IJavaElement[] union(IJavaElement[] set1, IJavaElement[] set2) {
		Set union= new HashSet(set1.length + set2.length);
		union.addAll(Arrays.asList(set1));
		union.addAll(Arrays.asList(set2));
		return (IJavaElement[]) union.toArray(new IJavaElement[union.size()]);
	}	

	public static IResource[] union(IResource[] set1, IResource[] set2) {
		Set union= new HashSet(set1.length + set2.length);
		union.addAll(Arrays.asList(set1));
		union.addAll(Arrays.asList(set2));
		return (IResource[]) union.toArray(new IResource[union.size()]);
	}	

	public static Set union(Set set1, Set set2){
		Set union= new HashSet(set1.size() + set2.size());
		union.addAll(set1);
		union.addAll(set2);
		return union;
	}

	public static IType[] getMainTypes(IJavaElement[] javaElements) throws JavaModelException {
		List result= new ArrayList();
		for (int i= 0; i < javaElements.length; i++) {
			IJavaElement element= javaElements[i];
			if (element instanceof IType && JavaElementUtil.isMainType((IType)element))
				result.add(element);
		}
		return (IType[]) result.toArray(new IType[result.size()]);
	}
	
	public static IFolder[] getFolders(IResource[] resources) {
		List result= getResourcesOfType(resources, IResource.FOLDER);
		return (IFolder[]) result.toArray(new IFolder[result.size()]);
	}

	public static IFile[] getFiles(IResource[] resources) {
		List result= getResourcesOfType(resources, IResource.FILE);
		return (IFile[]) result.toArray(new IFile[result.size()]);
	}
		
	//the result can be cast down to the requested type array
	public static List getResourcesOfType(IResource[] resources, int typeMask){
		List result= new ArrayList(resources.length);
		for (int i= 0; i < resources.length; i++) {
			if (isOfType(resources[i], typeMask))
				result.add(resources[i]);
		}
		return result;
	}
	
	//the result can be cast down to the requested type array
	//type is _not_ a mask	
	public static List getElementsOfType(IJavaElement[] javaElements, int type){
		List result= new ArrayList(javaElements.length);
		for (int i= 0; i < javaElements.length; i++) {
			if (isOfType(javaElements[i], type))
				result.add(javaElements[i]);
		}
		return result;
	}

	public static boolean hasElementsNotOfType(IResource[] resources, int typeMask) {
		for (int i= 0; i < resources.length; i++) {
			IResource resource= resources[i];
			if (resource != null && ! isOfType(resource, typeMask))
				return true;
		}
		return false;
	}

	//type is _not_ a mask	
	public static boolean hasElementsNotOfType(IJavaElement[] javaElements, int type) {
		for (int i= 0; i < javaElements.length; i++) {
			IJavaElement element= javaElements[i];
			if (element != null && ! isOfType(element, type))
				return true;
		}
		return false;
	}
	
	//type is _not_ a mask	
	public static boolean hasElementsOfType(IJavaElement[] javaElements, int type) {
		for (int i= 0; i < javaElements.length; i++) {
			IJavaElement element= javaElements[i];
			if (element != null && isOfType(element, type))
				return true;
		}
		return false;
	}

	public static boolean hasElementsOfType(IResource[] resources, int typeMask) {
		for (int i= 0; i < resources.length; i++) {
			IResource resource= resources[i];
			if (resource != null && isOfType(resource, typeMask))
				return true;
		}
		return false;
	}

	private static boolean isOfType(IJavaElement element, int type) {
		return element.getElementType() == type;//this is _not_ a mask
	}
		
	private static boolean isOfType(IResource resource, int type) {
		return isFlagSet(resource.getType(), type);
	}
		
	private static boolean isFlagSet(int flags, int flag){
		return (flags & flag) != 0;
	}

	public static boolean isSourceFolder(IJavaElement javaElement) throws JavaModelException {
		return (javaElement instanceof IPackageFragmentRoot) &&
				((IPackageFragmentRoot)javaElement).getKind() == IPackageFragmentRoot.K_SOURCE;
	}
	
	public static boolean isPackageFragmentRoot(IJavaProject javaProject) throws JavaModelException{
		return getCorrespondingPackageFragmentRoot(javaProject) != null;
	}
	
	private static final boolean isPackageFragmentRootCorrespondingToProject(IPackageFragmentRoot root) throws JavaModelException {
		return root.getResource() instanceof IProject;
	}

	public static final IPackageFragmentRoot getCorrespondingPackageFragmentRoot(IJavaProject p) throws JavaModelException {
		IPackageFragmentRoot[] roots= p.getPackageFragmentRoots();
		for (int i= 0; i < roots.length; i++) {
			if (isPackageFragmentRootCorrespondingToProject(roots[i]))
				return roots[i];
		}
		return null;
	}

}