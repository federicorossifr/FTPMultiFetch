cls
@echo off
@echo Building sources...
javac -classpath ".;lib\*" src\MultiFTPFetcher\MultiFTPFetcher.java -d build\classes
@echo OK
@echo Building jar...
cd build\classes
jar cmf ..\..\MANIFEST.MF ..\..\Ftp.jar MultiFTPFetcher\*
cd ..\..\
@echo DONE
pause
cls
