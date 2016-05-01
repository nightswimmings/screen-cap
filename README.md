

### ScreenCap Test App

This micro-app is intended to work as a real-time video recorder and graphic processor. The app runs in background and waits for certain keyboard inputs (hook) to start screen recording/capture. It outputs those recordings by default into a "Recordings" folder created expressly and sitting in the directory from which the app is executed.

 - For capturing screen, press PrtScrn key
 - For starting/stopping video recording, press Scroll Lock key
 - For exiting the app from background, press F12
 
The library is provided as a Java Jar File, which needs Java8 installed on the system. In order to start the app, we can either double click the jar file (which should equal "Open With..." Java VM), or either start it from the console with "java -jar library.jar". The former outputs logs to a "log.out" file created on-the-fly in the very same folder, while the later writes logs directly to console.  

The scope of this app is still not met. Aside from screen/video capture, there is a whole architecture for enabling graphic processing. Even though it is yet not possible to test the real-time processing, some foundations have been already developed, so we can test and play with a couple of filters by means of manual execution of the concrete FilterShow class within the Jar. To access it, just run the system console with its base in the library folder and execute the following:
 
>java -cp "ScreenCap-0.0.1-SNAPSHOT-bin.jar" org.nightswimming.cv.FiltersShow 

(it will ask for a mandatory parameter: name of the filter; and an optional filename: to use a file instead of a default current screenshot)
 
## Dev Inf

The whole project and bundle of sources, test, and resources is found
within the jar itself. Just unzip it, and import the project bundle into your favourite IDE as a Maven project.

The app is not yet finished nor polished, 
and is currently aimed only at internal use. 
IT HAS ONLY BEEN TESTED ON WINDOWS. 

- Log file and output folder names/paths are hardcoded.
- The configuration for output video is currently h.264, and it can only be configured through the code (though that is quite complete for customization).
- Tests (integration) are not polished yet, they require manual interaction for acknowledging everything is ok (a GUI is shown in each case and waits for user close event).

It's main purpose has been to serve as a convenient util, as well as a playground for working with following Java features:

- Java8 lambdas and functional interfaces (CVFilter.java class)

- JavaCV (OpenCV) graphics transformation ported 3rd party library (quite unstable and unrefined yet, almost a one-man convenient effort)

- State-of-the-art java.util.concurrency (multithreading) package uses in Java 8 (Futures/Promises, Executors/Queues, Locks...), including a custom-made CancellableCompletionService which is really missed in Java. This part has been deliberatedly over-engineered for a small app, with the hope to make the app more robust as soon as more features are added.
  
- Third party Keyboard hook libraries in Java, something not provided out-of-the-box with the core

- Available Tuple libraries in Java are pretty poor. A custom one-size-fits-all generic scalable tuple library has been designed, but due to some limitations of the language and thus the library, it has not been publicly exposed. Indeed, as long as Java lacks features like subgenerics or Self types I hardly believe, an ultimate complete generic tuple library will ever be possible. Even if the performance would probably suffer, that feature would suppose a paradigm shifter in some scenarios, as it would overcome the current single return type limitation of the language, allowing to specify several concrete return types in methods, which would allow also to make contracts (interfaces) much more safe.
  
## Thanks 
 This project plays with features from the projects
jNativeHook (https://github.com/kwhat/jnativehook)
 and JavaCV (https://github.com/bytedeco/javacv)

 
 
 