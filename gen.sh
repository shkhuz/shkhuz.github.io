clean=0
while getopts :c flag; do
    case $flag in 
        c) clean=1;;
    esac
done

build() {
    for md in `find . -name '*.md'`; do
        mdhtml=${md%.*}.html
        mdtitle=`sed -n -e 's/^# //p' $md`

        echo "<!DOCTYPE html>
    <html lang=\"en\">
    <meta charset=\"utf-8\">
    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">

    <head>
    <title>$mdtitle</title>
	<link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"/assets/apple-touch-icon.png\">
	<link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"/assets/favicon-32x32.png\">
	<link rel=\"icon\" type=\"image/png\" sizes=\"16x16\" href=\"/assets/favicon-16x16.png\">
	<link rel=\"manifest\" href=\"/assets/site.webmanifest\">
    <link rel=stylesheet href=\"/style.css\">
    </head>

    <body>
    <article>" > $mdhtml
        markdown -ffencedcode $md >> $mdhtml

        if [[ $md = "./index.md" ]]; then
            echo "<h2>Blog</h2>
    <ul>" >> $mdhtml
            for year in `ls -d blog/*/`; do
                for month in `ls -d $year*/`; do
                    for day in `ls -d $month*/`; do
                        for post in `ls $day*.md`; do
                            postname=`sed -n -e 's/^# //p' $post`
                            echo "<li>`basename $year`/`basename $month`/`basename $day`: <a href=\"${post%.*}.html\">$postname</a>" >> $mdhtml
                        done
                    done
                done
            done
            echo "</ul>" >> $mdhtml
        fi

        echo "</article>
    </body>
    </html>" >> $mdhtml
    done
}

clean() {
    rm -rf `find . -type f -name '*.html'`
}

if [[ $clean -eq 1 ]]; then
    clean
else
    build
fi
