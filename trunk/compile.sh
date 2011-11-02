rm -rf build
mkdir build
mkdir build/classes
find . -type f | grep "java" | grep -v .svn | xargs javac -cp junit.jar -d build/classes 
