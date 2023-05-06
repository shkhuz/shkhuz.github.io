ALL_MD_FILES := $(shell find . -name "*.md")
HTML_FILES := $(addsuffix .html, $(basename $(ALL_MD_FILES)))

all: $(HTML_FILES)

run: $(HTML_FILES)
	http-server -p8080 .

%.html: %.md
	@python3 gen.py $^

clean:
	rm -f $(HTML_FILES)

.PHONY: all run clean
