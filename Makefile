MD_FILES := $(shell find . -type f -name "*.md")
HTML_FILES := $(addsuffix .html, $(basename $(MD_FILES)))

all: $(HTML_FILES)

run: 
	http-server -p 4000 ./

%.html: %.md
	pandoc -s $^ --template pandoc_template.html -o $@

clean: 
	rm -f $(HTML_FILES)

.PHONY: all run clean
