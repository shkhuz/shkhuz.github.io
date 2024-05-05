ALL_MD_FILES := $(shell find . -name node_modules -prune -o -name "*.md" -print)
HTML_FILES := $(addsuffix .html, $(basename $(ALL_MD_FILES)))

all: $(HTML_FILES) mental_math

run: $(HTML_FILES) mental_math
	http-server -c-1 -p8080 .

mental_math:
	$(MAKE) -C mental_math/project_files

%.html: %.md
	@python3 gen.py $^

clean:
	rm -f $(HTML_FILES)

.PHONY: all run clean mental_math
