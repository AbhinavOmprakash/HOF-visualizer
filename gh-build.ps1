cd resources/public
git init .
git add .
git commit -m "update gh pages"
git push --force --quiet https://github.com/AbhinavOmprakash/HOF-visualizer.git master:pages
cd ..
cd ..