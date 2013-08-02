Demonstrates an issue with Blueprint...

BUILD:
ant build

RUN with FELIX:
java -cp org.apache.felix.framework-4.2.1.jar:biz.aQute.bndlib-2.1.0.jar:bin org.example.launch.Launcher

RUN with EQUINOX:
java -cp org.eclipse.osgi-3.8.2.jar:biz.aQute.bndlib-2.1.0.jar:bin org.example.launch.Launcher