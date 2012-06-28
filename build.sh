rm *.class
javac -cp RXTXcomm.jar SerialMonitor.java
javac ArduinoThermometer.java
java -cp .:RXTXcomm.jar ArduinoThermometer

