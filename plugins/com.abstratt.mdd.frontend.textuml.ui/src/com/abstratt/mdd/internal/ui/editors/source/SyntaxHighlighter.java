/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *    Massimiliano Federici - bug 2127735 - multi-line comments lose highlighting
 *******************************************************************************/
package com.abstratt.mdd.internal.ui.editors.source;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

public class SyntaxHighlighter extends RuleBasedScanner {

    // colors
    private static final String KEYWORD_COLOR = SyntaxHighlighter.class.getCanonicalName() + ".keyword";
    private static final String STRING_COLOR = SyntaxHighlighter.class.getCanonicalName() + ".string";
    private static final String NUMBER_COLOR = SyntaxHighlighter.class.getCanonicalName() + ".number";
    private static final String COMMENT_COLOR = SyntaxHighlighter.class.getCanonicalName() + ".comment";
    private static final String DEFAULT_COLOR = SyntaxHighlighter.class.getCanonicalName() + ".default";
    private static final String ANNOTATION_COLOR = SyntaxHighlighter.class.getCanonicalName() + ".annotation";

    static {
        // initialize colors
        ColorRegistry registry = JFaceResources.getColorRegistry();
        registry.put(KEYWORD_COLOR, new RGB(128, 0, 128));
        registry.put(STRING_COLOR, new RGB(0, 0, 255));
        registry.put(NUMBER_COLOR, new RGB(215, 107, 0));
        registry.put(COMMENT_COLOR, new RGB(0, 128, 0));
        registry.put(DEFAULT_COLOR, new RGB(0, 0, 0));
        registry.put(ANNOTATION_COLOR, new RGB(0, 150, 150));
    }

    private ITokenScanner commentScanner;

    public SyntaxHighlighter(String[] keywords) {
        super();
        initialize(keywords);
    }

    protected void initialize(String[] keywords) {
        ColorRegistry registry = JFaceResources.getColorRegistry();

        IToken keyword = new Token(new TextAttribute(registry.get(KEYWORD_COLOR), null, SWT.BOLD));
        IToken string = new Token(new TextAttribute(registry.get(STRING_COLOR)));
        IToken number = new Token(new TextAttribute(registry.get(NUMBER_COLOR)));
        IToken annotation = new Token(new TextAttribute(registry.get(ANNOTATION_COLOR)));
        IToken defaultToken = new Token(new TextAttribute(registry.get(DEFAULT_COLOR)));

        List<IRule> rules = new ArrayList<IRule>();

        // strings
        rules.add(new SingleLineRule("\"", "\"", string, '\\'));

        // annotations
        rules.add(new MultiLineRule("[", "]", annotation));

        // numbers
        rules.add(new NumberRule(number));

        // keywords and normal (default) text
        WordRule wordRule = new WordRule(new WordDetector(), defaultToken);
        for (int i = 0; i < keywords.length; i++) {
            wordRule.addWord(keywords[i], keyword);
        }
        rules.add(wordRule);

        setRules(rules.toArray(new IRule[rules.size()]));
    }

    ITokenScanner getCommentScanner() {
        // lazy init
        if (this.commentScanner == null) {
            final Token comment = new Token(new TextAttribute(JFaceResources.getColorRegistry().get(COMMENT_COLOR)));
            // no rules needed, because this will apply to comment partition
            // only
            final RuleBasedScanner ruleBasedScanner = new RuleBasedScanner();
            // this will apply the syntax
            ruleBasedScanner.setDefaultReturnToken(comment);
            this.commentScanner = ruleBasedScanner;
        }
        return commentScanner;
    }
}