run: all
	java -classpath classes Use inline_test.md

debug: all
	jdb -classpath classes -sourcepath . Use inline_test.md

all:
	javac -g --release 11 -Xlint:unchecked -d classes $(shell find . -name "*.java")

