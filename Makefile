ALL_MD_FILES := $(shell find . -name node_modules -prune -o -name "*.md" -print)
HTML_FILES := $(addsuffix .html, $(basename $(ALL_MD_FILES)))

all: $(HTML_FILES)

run: $(HTML_FILES)
	http-server -c-1 -p8080 .

%.html: %.md
	@python3 gen.py $^

clean:
	rm -f $(HTML_FILES)

.PHONY: all run clean
