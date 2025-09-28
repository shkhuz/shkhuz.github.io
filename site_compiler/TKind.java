public enum TKind {
    preblock,
    mathblock,
    equal3,
    lbrack3,
    rbrack3,
    pound,
    minus,
    plus,
    orderedMarker,
    newline,

    text,
    prespan,
    underscore,
    star,
    anchor,

    eof;

    public boolean isInlineTerminator() {
        switch (this) {
            case preblock:
            case mathblock:
            case equal3:
            case lbrack3:
            case rbrack3:
            case pound:
            case minus:
            case plus:
            case orderedMarker:
            case newline:
            case eof:
                return true;

            case text:
            case prespan:
            case underscore:
            case star:
            case anchor:
                return false;
        }
        throw new Error("Add " + this + " to isInlineTerminator()");
    }

    public boolean isListMarker() {
        switch (this) {
            case plus:
            case minus:
            case orderedMarker:
                return true;
        }
        return false;
    }
}
