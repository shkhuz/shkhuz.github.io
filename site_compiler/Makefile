JAVA_FILES := $(shell find src -name "*.java")
STAMP := classes/.compiled.stamp

$(STAMP): $(JAVA_FILES)
	@mkdir -p classes
	javac -g --release 11 -Xlint:unchecked -d classes $^
	touch $(STAMP)

run: $(STAMP) test.md
	java -classpath classes Use test.md

debug: $(STAMP) test.md
	jdb -classpath classes -sourcepath . Use test.md

clean: 
	rm -rf classes

.PHONY: run debug clean
