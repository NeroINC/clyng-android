This android folder has three important sub folders. 
1. 3rdparty - holds all the 3rd party libraries we use
2. corelib - the cling library for Android
3. demoapp - an example, appleication

There is nothing you need or should do in 3rdparty. you should start by building the corelib. To build corelib start by updating the jdk.home.android_2.2_google_apis in clyngmobile.properties. Then you can safely run
$ ant -f clyngmobile.xml 
The resulting jar should be found in corelib/out/artifacts/ClyngMobile_jar

You can now use this jar in your own application or in the demoapp. Please note that the demo application requires Android SDK >= 11. 
