JAVA_FILES := $(shell find src -name "*.java")
STAMP := classes/.compiled.stamp

$(STAMP): $(JAVA_FILES)
	@mkdir -p classes
	javac -g --release 11 -Xlint:unchecked -d classes $^
	touch $(STAMP)

run: $(STAMP) index.md
	java -classpath classes Use index.md

debug: $(STAMP)
	jdb -classpath classes -sourcepath . Use index.md

clean: 
	rm -rf classes

.PHONY: run debug clean
