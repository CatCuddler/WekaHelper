# Weka Helper

The Weka Helper uses the Weka machine learning framework to automatically train and evaluate models for all sensor subsets that can be constructed from a given set of sensors. It is designed to work in conjunction with the [Body Tracking Framework](https://github.com/riruroman/BodyTracking) and HTC Vive virtual reality hardware. Sensor readings from the HTC Vive HMD, handcontrollers and trackers are recorded through the Body Tracking Framework while a subject performs various exercises. The Weka Helper reads the sensor data, extracts features, and trains and evaluates models for a predefined selection of sensor subsets and classifiers. Models are trained and evaluated using leave-one-out cross validation. Using a previously trained model, the Weka Helper and Body Tracking framework can also communicate through the Java Native Interface in order to predict the name of the currently performed exercise.

# Setup

## IDE
The Weka Helper uses Maven, so simply load the project into your IDE through the pom.xml file. The Weka Helper has been tested with IntelliJ 2018.3.4 and Eclipse 2018-09.

## Java

### Feature extraction, model training and evaluation
For upwards of ~30 minutes of recorded data, the Weka Helper will likely require more memory than a 32-bit JVM can provide, making a 64-bit JVM necessary. The Weka Helper has been tested with Java 11.0.2. For our test data consisting of roughly 4 hours of recordings, using a window spacing of 1 second, more than 8 GB of ram were required. To increase the amount of memory available to the JVM, add the argument "-Xmx11000m" to the VM options, where "1100m" is the memory in megabytes.

### Live classification
In order to communicate with the Body Tracking Framework through the Java Native Interface, the Weka Helper first needs to be packaged (Maven->Install) into a jar file. Since the body Tracking Framework does not currently feature 64-bit support, the jar has to be compiled with a 32-bit version of Java. However, Oracle does not provide 32-bit JDKs for Java 9 and upwards. The Weka Helper has been tested with Zulu 10.3, a 32-bit JDK 10 distribution provided by [Azul](https://www.azul.com/downloads/zulu/).

# Usage

### Feature extraction, model training and evaluation
The Feature Extraction, training and evaluation process is started through the Main class.
The following settings can be changed in the TestBenchSettings class:

- Which sensor subsets to examine
	- Direct selection of subsets
	- Combining predefined selection of subsets with additional trackers
	- Rule-based selection of subsets
- Which classifiers to examine
- Whether to reuse a previously created feature file
- Modifying the various input and output folders
- Window size and spacing
- Whether to create impersonal or personal models
- Whether to exclude various feature types
- Whether to scale all features by a fixed value
- Whether to save all generated models

The TestBenchSettings class contains additional information on these settings. If the folder settings have not been changed, sensor reading files produced by the Body Tracking Framework have to be placed in the "\inputFrameData\currentInput" folder. Results will appear in the "outputResults" folder, with subfolders for each sensor subset, classifier and subject.

### Live classification
If the option for saving models in TestBenchSettings is set, all models will be saved in within their subject result folder. These models can be used in conjunction with a jar (Maven->Install) of the Weka Helper and the Body Tracking Framework to predict exercises while they are being recorded. The settings made in TestBenchSettings need to be the same when training the model and when compiling the jar file. The whole process is described in detail within the documentation for the [Body Tracking Framework](https://github.com/riruroman/BodyTracking).

# Adaptation
The Weka Helper was developed for a specific dataset, containing a predefined selection of exercises and sensors. The machine learning algorithms all work for any exercise and sensor, but they are in some cases referenced directly for specific sanity checks on the produced data, and to improve the result formatting. In order to use the Weka Helper with data for different exercises or sensors, these references have to be changed.

The currently used sensor names are:
head, hip, lFoot, lForearm, lHand, lLeg, rArm, rFoot, rForeArm, rHand, rLeg and spine.
They are referenced in the following classes:
FrameDataSet, ClassificationResult, TestBenchSettings (only for choosing sensor subsets)

The currently used exercises are:
jogging, kick, kickPunch, lateralBounding, lunges, punch, sitting, squats, standing and walking.
They are referenced in the following classes:
ConfusionMatrixSummary, CppDataClassifier (only for live classification)