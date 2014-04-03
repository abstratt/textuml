#!/bin/sh
if [ -z "$ECLIPSE_HOME"]; then
	ECLIPSE_HOME=/opt/eclipse/eclipse
fi

# different ways to get the launcher and Main class
if [ -f $ECLIPSE_HOME/startup.jar ]; then
  cpAndMain="$ECLIPSE_HOME/startup.jar org.eclipse.core.launcher.Main"; # up to M4_33
elif [ -f $ECLIPSE_HOME/plugins/org.eclipse.equinox.launcher.jar ]; then
  cpAndMain="$ECLIPSE_HOME/plugins/org.eclipse.equinox.launcher.jar org.eclipse.equinox.launcher.Main"; # M5_33
else
  cpAndMain=`find $ECLIPSE_HOME/ -name "org.eclipse.equinox.launcher_*.jar" | sort | head -1`" org.eclipse.equinox.launcher.Main";
fi

java -cp $cpAndMain \
   -noupdate -application org.eclipse.ant.core.antRunner -f build.xml compile
