/*******************************************************************************
 * Copyright (c) 2009 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Attila Bak - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.internal.ui;

 import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage.Literals;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.internal.ui.editors.source.SourceEditor;
import com.abstratt.mdd.ui.UIUtils;

/**
 * Content assist processor for TextUML.
 * 
 * FIXME This implementation has a few caveats that we need to address:
 * - it allows access to all elements in the same repository
 * - it ignores profile applications - all stereotypes in any profiles in the repository are offered
 * - ... 
 */
public class TextUMLCompletionProcessor implements IContentAssistProcessor {

	private static final String EMPTY_STRING = "";
	private static final String UML = "uml";
	private static final String TEXTUML_DEPENDENCY = "dependency";
	private static final String TEXTUML_CLASS = "class";
	private static final String TEXTUML_INTERFACE = "interface";
	private static final String TEXTUML_ASSOCIATION = "association";
	private static final String TEXTUML_OPERATION = "operation";
	private static final String TEXTUML_ROLE = "role";
	private static final String TEXTUML_ATTRIBUTE = "attribute";
	private static final String TEXTUML_CONSTANT = "constant";
	private static final String TEXTUML_DATATYPE= "datatype";
	private static final String TEXTUML_PRIMITIVE= "primitive";
	// maps TextUML keywords to the corresponding Ecore classes
	private static final Object[][] TARGET_METACLASSES = {
			{ TEXTUML_DEPENDENCY, Literals.DEPENDENCY },
			{ TEXTUML_CLASS, Literals.CLASS },
			{ TEXTUML_ASSOCIATION, Literals.ASSOCIATION},
			{ TEXTUML_INTERFACE, Literals.INTERFACE },
			{ TEXTUML_PRIMITIVE, Literals.PRIMITIVE_TYPE},
			{ TEXTUML_DATATYPE, Literals.DATA_TYPE},
			{ TEXTUML_OPERATION, Literals.OPERATION },
			{ TEXTUML_ROLE, Literals.PROPERTY },
			{ TEXTUML_ATTRIBUTE, Literals.PROPERTY },
			{ TEXTUML_CONSTANT, Literals.PROPERTY } };
	
	private static final String BASE_PROPERTY = "base_";
	private static final String ECORE = "Ecore";
	private static final String STANDARD = "Standard";
	private SourceEditor editor = null;
	private String patternStr = "([a-zA-Z0-9])+[:][:]([a-zA-Z0-9])+";
	private Pattern pattern = null;
	protected final static String[] defaultProposals = com.abstratt.mdd.frontend.textuml.core.TextUMLConstants.KEYWORDS;

	public TextUMLCompletionProcessor(SourceEditor editor) {
		super();
		this.editor = editor;
		pattern = Pattern.compile(patternStr);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
	
		List<ICompletionProposal> finalProposals = new ArrayList<ICompletionProposal>();
		List<String> proposals = new ArrayList<String>();
		ICompletionProposal[] toReturn = null;
		try {
			toReturn = doComputeProposal(viewer, offset, finalProposals,
					proposals);
		} catch (Exception ex) {
			toReturn = new ICompletionProposal[proposals.size()];
			return proposals.toArray(toReturn);
		}
		return toReturn;
	}

	private ICompletionProposal[] doComputeProposal(ITextViewer viewer,
			int offset, List<ICompletionProposal> finalProposals,
			List<String> proposals) {
		IRepository repository = getRepository();
		// Safety check
		if (repository == null) {
			ICompletionProposal[] dummyRet = new ICompletionProposal[proposals
					.size()];
			return proposals.toArray(dummyRet);
		}
		IDocument doc = viewer.getDocument();

		int lineNumber = -1;
		try {
			lineNumber = doc.getLineOfOffset(offset);
		} catch (BadLocationException e) {
			lineNumber = -1;
		}
		// Safety check
		if (lineNumber == -1) {
			ICompletionProposal[] dummyRet = new ICompletionProposal[proposals
					.size()];
			return proposals.toArray(dummyRet);
		}
		//FIXME this relies on specific formatting. Should use new API in TextUMLCompiler (and enhance it if necessary) 
		// in order to find out the context
		
		// Getting the current and the next line
		String currentLine = getLineText(viewer, lineNumber);
		String nextLine = getLineText(viewer, lineNumber + 1);

		// Getting the last word tipped for the code completion to enable
		// incremental search (only for keywords)
		String lastWord = lastWord(doc, offset);
		
		// If we are in an attribute, operation or role we look up the types
		// that are available and return
		if (currentLine.indexOf(TEXTUML_ATTRIBUTE) > -1
				|| currentLine.indexOf(TEXTUML_ROLE) > -1
				|| currentLine.indexOf(TEXTUML_OPERATION) > -1
				|| currentLine.indexOf(TEXTUML_DEPENDENCY) > -1) {
			findTypes(proposals, lastWord);
			for (Iterator<String> iterator = proposals.iterator(); iterator
					.hasNext();) {
				String string = iterator.next();
				if(checkifNotEmpty(lastWord)) {
					finalProposals.add(new CompletionProposal(string, offset-lastWord.length(), lastWord.length(),
							string.length()));
					
				} else {
					finalProposals.add(new CompletionProposal(string, offset, 0,
							string.length()));
				}
			}
			return createProposalList(finalProposals);
		}
		
		// If we are on an "empty" line or before the end in somewhere we
		// deliver the default keyword proposals
		if (nextLine.trim().length() == 0 || nextLine.indexOf("end") > -1) {
			for (int i = 0; i < defaultProposals.length; i++) {
				if (defaultProposals[i].startsWith(lastWord)) {
					finalProposals.add(new CompletionProposal(
							defaultProposals[i], offset - lastWord.length(),
							lastWord.length(), defaultProposals[i].length()));
				}
			}
			return createProposalList(finalProposals);
		}

		int currentLineOffset = -1;
		String currentLineToInspect = EMPTY_STRING;
		try {
			currentLineOffset = doc.getLineOffset(lineNumber);
			currentLineToInspect = doc.get(currentLineOffset, offset
					- currentLineOffset);
		} catch (BadLocationException e) {
			currentLineToInspect = EMPTY_STRING;
		}
		Matcher matcher = pattern.matcher(currentLineToInspect);
		boolean matchFound = false;
		int lastEnd = 0;
		String profileWithStereotype = EMPTY_STRING;
		// Try to get the stereotype where we are currently working on the
		// current line
		while (matcher.find()) {
			matchFound = true;
			profileWithStereotype = matcher.group();
			lastEnd = matcher.end();
		}
		if (matchFound) {
			// We have found stereotypes already applied
			// Get the remaining of the line where we are currently
			String remaining = currentLineToInspect.substring(lastEnd);
			if (remaining.indexOf("(") > -1) {
				// We might be working in the tagged values section
				if (remaining.indexOf(")") > -1 && remaining.indexOf(",") > -1
						&& remaining.indexOf(",") > remaining.indexOf(")")) {
					// In this case we are outside of the tagged values section
					// because it look like
					// Stereotype(taggedvaluename="something"),
					findStereotypesForNextLine(proposals, nextLine);
					removeUnneededBecauseOfCurrentLine(proposals, currentLine);

				} else {
					// We are in a tagged value section for sure the pattern is:
					// Stereotype(taggedvaluename
					String[] stringArr = profileWithStereotype.substring(0,
							profileWithStereotype.length()).split("::");
					findTaggedValuesForCurrentLineButNotApplied(proposals,
							stringArr[0], stringArr[1], currentLine);
				}
			} else {
				// Normal stereotype application we are not editing tagged
				// values
				findStereotypesForNextLine(proposals, nextLine);
				removeUnneededBecauseOfCurrentLine(proposals, currentLine);
			}
		} else {
			// We have nothing applied on the current line try to get some
			// values for the next line
			findStereotypesForNextLine(proposals, nextLine);
		}

		for (Iterator<String> iterator = proposals.iterator(); iterator
				.hasNext();) {
			String string = iterator.next();
			finalProposals.add(new CompletionProposal(string, offset, 0, string
					.length()));
		}

		return createProposalList(finalProposals);
	}

	private ICompletionProposal[] createProposalList(
			List<ICompletionProposal> finalProposals) {
		ICompletionProposal[] a = new ICompletionProposal[finalProposals.size()];
		return finalProposals.toArray(a);
	}
	
	private boolean checkifNotEmpty(String word) {
		return word != null && !word.equals(EMPTY_STRING);
	}

	private void removeUnneededBecauseOfCurrentLine(List<String> proposals,
			String currentLine) {
		List<String> result = new ArrayList<String>();
		for (Iterator<String> iterator = proposals.iterator(); iterator
				.hasNext();) {
			String string = iterator.next();
			String replaced = string.replaceAll("\\[", EMPTY_STRING)
					.replaceAll("\\]", EMPTY_STRING);
			if (currentLine.indexOf(replaced) == -1) {
				result.add(replaced);
			}
		}
		proposals.clear();
		proposals.addAll(result);
	}

	private void findTypes(List<String> proposals, String lastWord) {
		Boolean lastWordEmpty = true;
		if(checkifNotEmpty(lastWord)) {
			lastWord = lastWord.toLowerCase();
			lastWordEmpty = false;
		}
		Package[] packages = getValidPackages();
		for (int i = 0; i < packages.length; i++) {
			for (TreeIterator<EObject> iterator = packages[i].eAllContents(); iterator
					.hasNext();) {
				EObject object = iterator.next();
				if (object instanceof Class) {
					Class clazz = (Class) object;
					if(lastWordEmpty || clazz.getQualifiedName().toLowerCase().contains(lastWord)) {
						proposals.add(clazz.getQualifiedName());
					}
					
				}
				if (object instanceof PrimitiveType) {
					PrimitiveType primo = (PrimitiveType) object;
					if(lastWordEmpty || primo.getQualifiedName().toLowerCase().contains(lastWord)) {
						proposals.add(primo.getQualifiedName());
					}
				}
				if (object instanceof Enumeration) {
					Enumeration enume = (Enumeration) object;
					if(lastWordEmpty || enume.getQualifiedName().toLowerCase().contains(lastWord)) {
						proposals.add(enume.getQualifiedName());
					}
				}
			}
		}
	}

	private void findTaggedValuesForCurrentLineButNotApplied(
			List<String> proposals, String profileName, String stereotypeName,
			String currentLine) {
		List<String> result = findTaggedAttributesForStereotype(findStereotypeByName(
				profileName, stereotypeName));
		for (Iterator<String> iterator = result.iterator(); iterator.hasNext();) {
			String string = iterator.next();
			if (string.indexOf(BASE_PROPERTY) == -1
					&& currentLine.indexOf(string) == -1) {
				proposals.add(string + "=\"\"");
			}
		}
	}

	private IRepository getRepository() {
		IProject currentProject = editor.getWorkingCopy().getFile()
				.getProject();
		try {
			return UIUtils.getCachedRepository(currentProject);
		} catch (CoreException e) {
		}
		return null;
	}

	private void findStereotypesForNextLine(List<String> proposals, String nextLine) {
		NamedElement target = null;
		for (Object[] targets : TARGET_METACLASSES) {
			final String targetKeyword = (String) targets[0];
			if (nextLine.indexOf(targetKeyword) >= 0) {
				final EClass targetMetaclass = (EClass) targets[1];
				selectProposalsFromStereotypes(proposals,
						findStereotypesForExtendedClass(getStereotypes(), targetMetaclass));
				return;
			}
		}
	}

	private void selectProposalsFromStereotypes(List<String> proposals,
			List<Stereotype> stereos) {
		for (Iterator<Stereotype> iterator = stereos.iterator(); iterator
				.hasNext();) {
			Stereotype stereotype = iterator.next();
			proposals.add("[" + stereotype.getProfile().getName() + "::"
					+ stereotype.getName() + "]");
		}
	}

	private List<String> findTaggedAttributesForStereotype(Stereotype stereotype) {
		List<String> result = new ArrayList<String>();
		for (Iterator<Property> iterator = stereotype.getAllAttributes()
				.iterator(); iterator.hasNext();) {
			Property property = iterator.next();
			result.add(property.getName());
		}
		return result;
	}

	private Stereotype findStereotypeByName(String profileName, String name) {
		Profile profile = findProfileByName(profileName);
		if (profile == null)
			return null;
		EList<Element> elements = profile.getOwnedElements();
		for (Iterator<Element> iterator = elements.iterator(); iterator
				.hasNext();) {
			Element element = iterator.next();
			if (element instanceof Stereotype)
				if (((Stereotype) element).getName().equals(name))
					return (Stereotype) element;
		}
		return null;
	}

	private String lastWord(IDocument doc, int offset) {
		String result = EMPTY_STRING;
		try {
			for (int n = offset - 1; n >= 0; n--) {
				char c = doc.getChar(n);
				if (!Character.isJavaIdentifierPart(c))
					return doc.get(n + 1, offset - n - 1);
			}
		} catch (BadLocationException e) {
			result = EMPTY_STRING;
		}
		return result;
	}

	private Profile findProfileByName(String name) {
		Package[] packages = getRepository()
				.getTopLevelPackages(null);
		Profile[] validProfiles = getValidProfiles(packages);
		for (int i = 0; i < validProfiles.length; i++)
			if (validProfiles[i].getName().equals(name))
				return validProfiles[i];
		return null;
	}

	private List<Stereotype> findStereotypesForExtendedClass(
			List<Stereotype> stereos, EClass targetMetaclass) {
		List<Stereotype> result = new ArrayList<Stereotype>();
		for (Iterator<Stereotype> iterator = stereos.iterator(); iterator
				.hasNext();) {
			Stereotype stereotype = iterator.next();
			for (Iterator<Class> clazzIter = stereotype
					.getAllExtendedMetaclasses().iterator(); clazzIter
					.hasNext();) {
				Class clazz = clazzIter.next();
				// FIXME this does not handle inheritance (a stereotype for a base metaclass should apply to any sub metaclasses)
				if (clazz.getName().equals(targetMetaclass.getName()))
					result.add(stereotype);
			}
		}
		return result;
	}

	private String getLineText(ITextViewer viewer, int lineNumber) {
		IDocument doc = viewer.getDocument();
		String result;
		try {
			IRegion lineRegion = doc.getLineInformation(lineNumber);
			result = doc.get(lineRegion.getOffset(), lineRegion.getLength());
		} catch (BadLocationException e) {
			result = EMPTY_STRING;
		}
		return result;
	}

	private Package[] getValidPackages() {
		Package[] packages = getRepository().getTopLevelPackages(null);
		List<Package> validPackages = new ArrayList<Package>();
		for (int i = 0; i < packages.length; i++) {
			Package p = (Package) packages[i];
			if (!(p instanceof Profile) && !p.getName().equals(STANDARD)
					&& !p.getName().equals(UML)
					&& !p.getName().equalsIgnoreCase(ECORE))
				validPackages.add((Package) p);
		}
		return validPackages.toArray(new Package[validPackages.size()]);
	}

	private Profile[] getValidProfiles(Package[] packages) {
		List<Profile> validPackages = new ArrayList<Profile>();
		for (int i = 0; i < packages.length; i++) {
			Package p = (Package) packages[i];
			if (p instanceof Profile && !p.getName().equals(STANDARD)
					&& !p.getName().equals(ECORE)) {
				validPackages.add((Profile) p);
			}
		}
		return validPackages.toArray(new Profile[validPackages.size()]);
	}

	private List<Stereotype> getStereotypes() {
		List<Stereotype> result = new ArrayList<Stereotype>();
		Package[] packages = getRepository()
				.getTopLevelPackages(null);
		Profile[] validProfiles = getValidProfiles(packages);
		for (int i = 0; i < validProfiles.length; i++) {
			EList<Element> elements = validProfiles[i].getOwnedElements();
			for (Iterator<Element> iterator = elements.iterator(); iterator
					.hasNext();) {
				Element element = iterator.next();
				if (element instanceof Stereotype)
					result.add((Stereotype) element);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '[' };
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return "Error in code completion";
	}

}
