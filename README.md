swanboxlibgdx
=============

swanbox is an awesome library/platform/collection of apps that lets you experience the fun and intimacy of a LAN game on your mobile phone. Current available demos include a pattern memory game and poker. 

first time setup
============
To begin: Install the [gradle plugin for eclipse](https://github.com/spring-projects/eclipse-integration-gradle/). Then install maven. You will need to install the socketio jar into your local .m2 repostory also (cd into swanlib/libs and run `mvn install:install-file -Dfile=socketio.jar -DgroupId=org.gottox -DartifactId=socketio -Dversion=1.0 -Dpackaging=jar`. Then in eclipse, import existing GRADLE PROJECTS. Import the android/core/desktop versions. Then install swanlib into your local repository (you will have to do this every time you make a change to swanlib) by running `./gradlew install` in the swanlib dir. 


running
==========
The desktop versions will run from eclipse in the usual way of run->java application because there are no special build instructions (if we add anything, we'll need to make this command line based). To run android, you will need to type `./gradlew android:installDebug` which will install it on the phone (but will NOT run it - you'll have to do that manually). To run the swan server, cd into the server directory and run `python swan_server.py`. Remember to have [gevent-socketio](https://github.com/abourget/gevent-socketio) installed.
