/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.text;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.JavaTextTools;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProcessor;
import org.eclipse.jdt.internal.ui.text.javadoc.JavaDocCompletionProcessor;


public class ContentAssistPreference {
	
	/** Preference key for content assist auto activation */
	public final static String AUTOACTIVATION=  "content_assist_autoactivation"; //$NON-NLS-1$
	/** Preference key for content assist auto activation delay */
	public final static String AUTOACTIVATION_DELAY=  "content_assist_autoactivation_delay"; //$NON-NLS-1$
	/** Preference key for content assist proposal color */
	public final static String PROPOSALS_FOREGROUND=  "content_assist_proposals_foreground"; //$NON-NLS-1$
	/** Preference key for content assist proposal color */
	public final static String PROPOSALS_BACKGROUND=  "content_assist_proposals_background"; //$NON-NLS-1$
	/** Preference key for content assist parameters color */
	public final static String PARAMETERS_FOREGROUND=  "content_assist_parameters_foreground"; //$NON-NLS-1$
	/** Preference key for content assist parameters color */
	public final static String PARAMETERS_BACKGROUND=  "content_assist_parameters_background"; //$NON-NLS-1$
	/** Preference key for content assist auto insert */
	public final static String AUTOINSERT=  "content_assist_autoinsert"; //$NON-NLS-1$
	
	/** Preference key for java content assist auto activation triggers */
	public final static String AUTOACTIVATION_TRIGGERS_JAVA= "content_assist_autoactivation_triggers_java"; //$NON-NLS-1$
	/** Preference key for javadoc content assist auto activation triggers */
	public final static String AUTOACTIVATION_TRIGGERS_JAVADOC= "content_assist_autoactivation_triggers_javadoc"; //$NON-NLS-1$
	
	/** Preference key for visibility of proposals */
	public final static String SHOW_VISIBLE_PROPOSALS= "content_assist_show_visible_proposals"; //$NON-NLS-1$
	/** Preference key for alphabetic ordering of proposals */
	public final static String ORDER_PROPOSALS= "content_assist_order_proposals"; //$NON-NLS-1$
	/** Preference key for case sensitivity of propsals */
	public final static String CASE_SENSITIVITY= "content_assist_case_sensitivity"; //$NON-NLS-1$
	/** Preference key for adding imports on code assist */
	public final static String ADD_IMPORT= "content_assist_add_import";	 //$NON-NLS-1$
	
	private static Color getColor(IPreferenceStore store, String key, IColorManager manager) {
		RGB rgb= PreferenceConverter.getColor(store, key);
		return manager.getColor(rgb);
	}
	
	private static Color getColor(IPreferenceStore store, String key) {
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
		return getColor(store, key, textTools.getColorManager());
	}
	
	private static JavaCompletionProcessor getJavaProcessor(ContentAssistant assistant) {
		IContentAssistProcessor p= assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
		if (p instanceof JavaCompletionProcessor)
			return  (JavaCompletionProcessor) p;
		return null;
	}
	
	private static JavaDocCompletionProcessor getJavaDocProcessor(ContentAssistant assistant) {
		IContentAssistProcessor p= assistant.getContentAssistProcessor(JavaPartitionScanner.JAVA_DOC);
		if (p instanceof JavaDocCompletionProcessor) 
			return (JavaDocCompletionProcessor) p;
		return null;
	}
	
	private static void configureJavaProcessor(ContentAssistant assistant, IPreferenceStore store) {
		JavaCompletionProcessor jcp= getJavaProcessor(assistant);
		if (jcp == null)
			return;
			
		String triggers= store.getString(AUTOACTIVATION_TRIGGERS_JAVA);
		if (triggers != null)
			jcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
			
		boolean enabled= store.getBoolean(SHOW_VISIBLE_PROPOSALS);
		jcp.restrictProposalsToVisibility(enabled);
		
		enabled= store.getBoolean(CASE_SENSITIVITY);
		jcp.restrictProposalsToMatchingCases(enabled);
		
		enabled= store.getBoolean(ORDER_PROPOSALS);
		jcp.orderProposalsAlphabetically(enabled);
		
		enabled= store.getBoolean(ADD_IMPORT);
		jcp.allowAddingImports(enabled);		
	}
	
	private static void configureJavaDocProcessor(ContentAssistant assistant, IPreferenceStore store) {
		JavaDocCompletionProcessor jdcp= getJavaDocProcessor(assistant);
		if (jdcp == null)
			return;
			
		String triggers= store.getString(AUTOACTIVATION_TRIGGERS_JAVADOC);
		if (triggers != null)
			jdcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
			
		boolean enabled= store.getBoolean(CASE_SENSITIVITY);
		jdcp.restrictProposalsToMatchingCases(enabled);
		
		enabled= store.getBoolean(ORDER_PROPOSALS);
		jdcp.orderProposalsAlphabetically(enabled);
	}
	
	/**
	 * Configure the given content assistant from the given store.
	 */
	public static void configure(ContentAssistant assistant, IPreferenceStore store) {
		
		JavaTextTools textTools= JavaPlugin.getDefault().getJavaTextTools();
		IColorManager manager= textTools.getColorManager();		
		
		
		boolean enabled= store.getBoolean(AUTOACTIVATION);
		assistant.enableAutoActivation(enabled);
		
		int delay= store.getInt(AUTOACTIVATION_DELAY);
		assistant.setAutoActivationDelay(delay);
		
		Color c= getColor(store, PROPOSALS_FOREGROUND, manager);
		assistant.setProposalSelectorForeground(c);
		
		c= getColor(store, PROPOSALS_BACKGROUND, manager);
		assistant.setProposalSelectorBackground(c);
		
		c= getColor(store, PARAMETERS_FOREGROUND, manager);
		assistant.setContextInformationPopupForeground(c);
		assistant.setContextSelectorForeground(c);
		
		c= getColor(store, PARAMETERS_BACKGROUND, manager);
		assistant.setContextInformationPopupBackground(c);
		assistant.setContextSelectorBackground(c);
		
		enabled= store.getBoolean(AUTOINSERT);
		assistant.enableAutoInsert(enabled);

		configureJavaProcessor(assistant, store);
		configureJavaDocProcessor(assistant, store);
	}
	
	
	private static void changeJavaProcessor(ContentAssistant assistant, IPreferenceStore store, String key) {
		JavaCompletionProcessor jcp= getJavaProcessor(assistant);
		if (jcp == null)
			return;
			
		if (AUTOACTIVATION_TRIGGERS_JAVA.equals(key)) {
			String triggers= store.getString(AUTOACTIVATION_TRIGGERS_JAVA);
			if (triggers != null)
				jcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
		} else if (SHOW_VISIBLE_PROPOSALS.equals(key)) {
			boolean enabled= store.getBoolean(SHOW_VISIBLE_PROPOSALS);
			jcp.restrictProposalsToVisibility(enabled);
		} else if (CASE_SENSITIVITY.equals(key)) {
			boolean enabled= store.getBoolean(CASE_SENSITIVITY);
			jcp.restrictProposalsToMatchingCases(enabled);
		} else if (ORDER_PROPOSALS.equals(key)) {
			boolean enable= store.getBoolean(ORDER_PROPOSALS);
			jcp.orderProposalsAlphabetically(enable);
		} else if (ADD_IMPORT.equals(key)) {
			boolean enabled= store.getBoolean(ADD_IMPORT);
			jcp.allowAddingImports(enabled);
		}
	}
	
	private static void changeJavaDocProcessor(ContentAssistant assistant, IPreferenceStore store, String key) {
		JavaDocCompletionProcessor jdcp= getJavaDocProcessor(assistant);
		if (jdcp == null)
			return;
			
		if (AUTOACTIVATION_TRIGGERS_JAVADOC.equals(key)) {
			String triggers= store.getString(AUTOACTIVATION_TRIGGERS_JAVADOC);
			if (triggers != null)
				jdcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());
		} else if (CASE_SENSITIVITY.equals(key)) {
			boolean enabled= store.getBoolean(CASE_SENSITIVITY);
			jdcp.restrictProposalsToMatchingCases(enabled);
		} else if (ORDER_PROPOSALS.equals(key)) {
			boolean enable= store.getBoolean(ORDER_PROPOSALS);
			jdcp.orderProposalsAlphabetically(enable);
		}
	}
	
	/**
	 * Changes the configuration of the given content assistant according to the given property
	 * change event and the given preference store.
	 */
	public static void changeConfiguration(ContentAssistant assistant, IPreferenceStore store, PropertyChangeEvent event) {
		
		String p= event.getProperty();
		
		if (AUTOACTIVATION.equals(p)) {
			boolean enabled= store.getBoolean(AUTOACTIVATION);
			assistant.enableAutoActivation(enabled);
		} else if (AUTOACTIVATION_DELAY.equals(p)) {
			int delay= store.getInt(AUTOACTIVATION_DELAY);
			assistant.setAutoActivationDelay(delay);
		} else if (PROPOSALS_FOREGROUND.equals(p)) {
			Color c= getColor(store, PROPOSALS_FOREGROUND);
			assistant.setProposalSelectorForeground(c);
		} else if (PROPOSALS_BACKGROUND.equals(p)) {
			Color c= getColor(store, PROPOSALS_BACKGROUND);
			assistant.setProposalSelectorBackground(c);
		} else if (PARAMETERS_FOREGROUND.equals(p)) {
			Color c= getColor(store, PARAMETERS_FOREGROUND);
			assistant.setContextInformationPopupForeground(c);
			assistant.setContextSelectorForeground(c);
		} else if (PARAMETERS_BACKGROUND.equals(p)) {
			Color c= getColor(store, PARAMETERS_BACKGROUND);
			assistant.setContextInformationPopupBackground(c);
			assistant.setContextSelectorBackground(c);
		} else if (AUTOINSERT.equals(p)) {
			boolean enabled= store.getBoolean(AUTOINSERT);
			assistant.enableAutoInsert(enabled);
		}
		
		changeJavaProcessor(assistant, store, p);
		changeJavaDocProcessor(assistant, store, p);
	}
}

