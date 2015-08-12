package com.abstratt.mdd.frontend.core.spi;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public interface ISourceAnalyzer {
    public enum ElementKind {
        Groups(true), Classes(true), Components(true), Interfaces(true), Enumerations(false), Signals(true), Receptions(
                false), Associations(true), StateMachines(true), Attributes(false), Compositions(false), References(
                false), Aggregations(false), Operations(false), Ends(false), States(false);
        ElementKind(boolean hasChildren) {
        }
    };

    public class SourceElementComparator implements Comparator<SourceElement> {
        public static Comparator<SourceElement> INSTANCE = new SourceElementComparator();

        @Override
        public int compare(SourceElement o1, SourceElement o2) {
            int byKind = o1.getKind().compareTo(o2.getKind());
            if (byKind != 0)
                return byKind;
            long byLine = o1.getLine() - o2.getLine();
            if (byLine != 0)
                return byLine > 0 ? 1 : -1;
            return o1.getLabel().compareTo(o2.getLabel());
        }

    }

    public class SourceElement {
        private String label;
        private long line;
        private TreeSet<SourceElement> children;
        private ElementKind kind;

        public SourceElement(String representation, long line, ElementKind kind) {
            this.label = representation;
            this.line = line;
            this.children = new TreeSet<SourceElement>(SourceElementComparator.INSTANCE);
            this.kind = kind;
        }

        public SortedSet<SourceElement> getChildren() {
            return children;
        }

        public long getLine() {
            return line;
        }

        public String getLabel() {
            return label;
        }

        public ElementKind getKind() {
            return kind;
        }
    }

    public List<SourceElement> analyze(String source);
}
