package com.abstratt.mdd.modelrenderer.uml2dot;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.StructuredActivityNode;

import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.frontend.textuml.renderer.ActivityGenerator;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public class ActivityRenderer implements IElementRenderer<Activity> {
    @Override
    public boolean renderObject(Activity element, IndentedPrintWriter out, IRenderingSession session) {
        generateActivity(element, out);
        return true;
    }

    public static void generateActivity(Activity element, IndentedPrintWriter out) {
        StructuredActivityNode rootAction = ActivityUtils.getRootAction(element);
        List<Action> statements = ActivityUtils.findStatements(rootAction);
        ActivityGenerator activityGenerator = new ActivityGenerator();
        List<String> textumlStatements = statements.stream()
                .map(statement -> activityGenerator.generateAction(statement).toString()).collect(Collectors.toList());
        for (String line : textumlStatements) {
            out.print("                    ");
            out.print(line);
            out.print(';');
            out.print("\\l");
        }
    }
}
