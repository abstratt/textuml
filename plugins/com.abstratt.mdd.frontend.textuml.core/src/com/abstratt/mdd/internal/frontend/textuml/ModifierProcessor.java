package com.abstratt.mdd.internal.frontend.textuml;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

import com.abstratt.mdd.frontend.core.spi.ISourceMiner;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.Token;

/**
 * A node processor that collcets modifiers.
 */
public class ModifierProcessor implements NodeProcessor<Node> {

    private Set<Modifier> modifiers = new LinkedHashSet<Modifier>();

    private ISourceMiner<Node> sourceMiner;

    public ModifierProcessor(ISourceMiner<Node> sourceMiner) {
        this.sourceMiner = sourceMiner;
    }

    @Override
    public void process(Node node) {
        if (node == null)
            return;
        for (Token modifierToken : sourceMiner.findChildren(node, Token.class))
            modifiers.add(Modifier.fromToken(modifierToken.getText()));
    }

    public void collectModifierToken(Node modifierNode) {
        if (modifierNode == null)
            return;
        Token modifierToken = sourceMiner.findChild(modifierNode, Token.class, false);
        modifiers.add(Modifier.fromToken(modifierToken.getText()));
    }

    public Set<Modifier> getModifiers(boolean consume) {
        Assert.isNotNull(modifiers);
        Set<Modifier> result = modifiers;
        if (consume)
            modifiers = new HashSet<Modifier>();
        return result;
    }
}
