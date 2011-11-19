rm -rf build
mkdir build
mkdir build/classes
find . -type f | grep "java" | grep -v .svn | xargs javac -cp junit.jar:log4j-1.2.16.jar -d build/classes 
