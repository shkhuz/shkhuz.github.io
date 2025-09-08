run: all
	java -classpath classes Use test.md

debug: all
	jdb -classpath classes -sourcepath . Use test.md

all:
	javac -g --release 11 -Xlint:unchecked -d classes $(shell find . -name "*.java")

