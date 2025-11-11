ALL_MD_FILES := $(shell find . -name node_modules -prune -o -name "*.md" -print)
HTML_FILES := $(addsuffix .html, $(basename $(ALL_MD_FILES)))

all: build_sitecompiler $(HTML_FILES)

build_sitecompiler:
	# make -C site_compiler

run: all
	http-server -c-1 -p8080 .

%.html: %.md
	java -classpath site_compiler/classes Use $^

clean:
	rm -f $(HTML_FILES)

.PHONY: all run clean
