To Compile:
-----------
- ant clean 
- ant compile

To run:
-------
Make sure the following files are in build/classes
1. Common.cfg  
2. PeerInfo.cfg
3. log4j-1.2.16.jar  
4. log4j.properties  
5. Create folder "peer_1001" and copy the file to be transferred inside this folder

To run, use the following command: 
java -cp log4j-1.2.16.jar:. cnt5106c/torrent/startup/PeerStarter
