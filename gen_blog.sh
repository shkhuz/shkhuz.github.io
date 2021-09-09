# A super simple blog directory contents lister
# Dumps an .md file with listings
# Author: Huzaifa Shaikh
echo "---" > blog/index.md
echo "pagetitle: Blog" >> blog/index.md
echo "---" >> blog/index.md
echo "" >> blog/index.md

for year in `ls -d blog/*/`; do
    for month in `ls -d $year*/`; do
        for day in `ls -d $month*/`; do
            for post in `ls $day*.md`; do
                post_without_first_dir=${post#*/}
                postname=`sed -n -e 's/^pagetitle: //p' $post`
                echo "- `basename $year`/`basename $month`/`basename $day`: [$postname](${post_without_first_dir%.*}.html)" >> blog/index.md
                # echo "$year, $month, $day, $post"
            done
        done
    done
done
