run: all
	java -classpath classes Use index.md

debug: all
	jdb -classpath classes -sourcepath . Use index.md

all:
	javac -g --release 11 -Xlint:unchecked -d classes $(shell find . -name "*.java")

