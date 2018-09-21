javac -cp .:lib/xstream-1.4.7.jar:lib/commons-net-3.6.jar:lib/xmlpull-1.1.3.1.jar:lib/xpp3_min-1.1.4c.jar src/MultiFTPFetcher/MultiFTPFetcher.java -d build/classes
cd build/classes
jar cmf ../../MANIFEST.MF ../../Ftp.jar MultiFTPFetcher/*
cd ../../
