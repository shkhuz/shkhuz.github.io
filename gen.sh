clean=0
run_server=0
while getopts :cr flag; do
    case $flag in 
        c) clean=1;;
        r) run_server=1;;
    esac
done

build() {
    for md in `find . -name '*.md'`; do
        mdhtml=${md%.*}.html
        mdtitle=`sed -n -e 's/^# //p' $md`

        echo "Building $md..."

        echo "<!DOCTYPE html>
<html lang=\"en\">
<meta charset=\"utf-8\">
<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">

<head>
<title>$mdtitle</title>
<link href=\"https://fonts.googleapis.com/css2?family=Source+Code+Pro&display=swap\" rel=\"stylesheet\"> 
<link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"/assets/apple-touch-icon.png\">
<link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"/assets/favicon-32x32.png\">
<link rel=\"icon\" type=\"image/png\" sizes=\"16x16\" href=\"/assets/favicon-16x16.png\">
<link rel=\"manifest\" href=\"/assets/site.webmanifest\">
<link rel=stylesheet href=\"/style.css\">
</head>

<body>
<article>" > $mdhtml

        if [[ $md != "./index.md" ]]; then
            local tmp=${md#*/}; local tmp2=${tmp#*/}; local tmp3=${tmp2%/*}
            # 2013/07/05 +"%b %d, %Y"
            author="<br><span class='author'>By Huzaifa Shaikh, $(date -d $tmp3 +'%b %d, %Y')</span>"
            cat $md | awk "NR==1{print \$\$1\"$author\"} NR!=1" | markdown -ffencedcode >> $mdhtml
        else
            markdown -ffencedcode $md >> $mdhtml
        fi

        if [[ $md = "./index.md" ]]; then
            echo "<h2>Blog</h2>
    <ul>" >> $mdhtml
            for year in `ls -d blog/*/ | sort -r`; do
                for month in `ls -d $year*/ | sort -r`; do
                    for day in `ls -d $month*/ | sort -r`; do
                        for post in `ls $day*.md`; do
                            postname=`sed -n -e 's/^# //p' $post`
                            echo "<li>`basename $year`/`basename $month`/`basename $day`: <a href=\"${post%.*}.html\">$postname</a>" >> $mdhtml
                        done
                    done
                done
            done
            echo "</ul>" >> $mdhtml
        else
            echo "<a href=\"/\">back to home page</a>" >> $mdhtml
        fi

        echo "</article>
</body>
</html>" >> $mdhtml
    done
    echo "Build successful"
}

clean() {
    rm -rf `find . -type f -name '*.html'`
}

if [[ $clean -eq 1 ]]; then
    clean
else
    build
fi

if [[ $run_server -eq 1 ]]; then
    http-server -p 4000 ./
fi
