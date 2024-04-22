# Beta Testing Setup for Graphma Project

## Overview
This document outlines the necessary steps to set up the Graphma project for beta testing using IntelliJ IDEA. The Graphma project is a multi-module Gradle project composed of several key components:

- **graphma-core**: The core library where the main functionality of the project is implemented.
- **graphma-data**: This subproject contains logic to handle different graph formats and serves as the link to test data. Test data is stored in a separate repository known as `datademorepo`.
- **graphma-benchmark**: Used for performance testing and benchmarking the functionalities developed in `graphma-core` and `graphma-data`.
- **graphma-playground**: A less critical subproject intended for experimental testing of various libraries or code snippets that may eventually be integrated into the core project. **This subproject is not essential for beta testing and should be ignored.**

The main focus for beta testing should be on `graphma-core`, `graphma-data`, and `graphma-benchmark`. These subprojects are crucial for assessing the project's performance, stability, and its capability to process and analyze graph data effectively.

## Prerequisites
Before you start, ensure that you have Java SDK installed on your system, preferably the version that matches the project's requirements (e.g., Java 11 or 17). You can download and install the Java SDK from [AdoptOpenJDK](https://adoptopenjdk.net/) or [Oracle's official Java site](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).

## Installing IntelliJ IDEA
IntelliJ IDEA is an IDE developed by JetBrains that supports Java and a multitude of other programming languages. Follow these steps to download and install IntelliJ IDEA:

1. **Download IntelliJ IDEA**
   - Visit the [IntelliJ IDEA download page](https://www.jetbrains.com/idea/download/).
   - Choose the edition that suits your needs: Community Edition (free and open-source) or Ultimate Edition (free for students, open-source projects, and trial; paid for commercial use).
   - Download the installer for your operating system (Windows, macOS, or Linux).

2. **Install IntelliJ IDEA**
   - Run the downloaded installer and follow the installation instructions.
   - On Windows, you might be prompted to install the JetBrains Runtime. Accept the prompt to ensure better performance and stability of IntelliJ IDEA.

## Loading the Project into IntelliJ IDEA
Once IntelliJ IDEA is installed, you can load the Graphma project into the IDE:

1. **Open IntelliJ IDEA**.
   - Start IntelliJ IDEA.
   - If this is your first time running IntelliJ IDEA, you'll be guided through initial setup including the option to import settings from a previous installation.

2. **Open the Graphma Project**:
   - On the Welcome screen, click `Open`.
   - Navigate to the directory where you have cloned or downloaded the Graphma project.
   - Select the project's root directory (which contains the `build.gradle.kts` or `settings.gradle.kts` file).
   - Click `OK`.

3. **Configure Project Settings**:
   - IntelliJ IDEA will automatically detect that it's a Gradle project. If prompted, select `Use auto-import` option to automatically sync any changes made to the Gradle configuration files.
   - Ensure that the project SDK is correctly set by going to `File > Project Structure > Project` and selecting the appropriate Java SDK from the `Project SDK` dropdown.

4. **Build the Project**:
   - To build the project and verify that everything is set up correctly, go to `View > Tool Windows > Gradle`.
   - In the Gradle tool window, navigate to `Tasks > build > build` and double-click to run the build task.

5. **Run Beta Tests**:
   - Navigate to the test directory in any of the modules.
   - Right-click on any test and select `Run 'TestName'` to execute the test and observe the output.

## Further Steps
After setting up the project, you can start beta testing as per the defined test cases or testing procedures. Ensure to keep track of any issues or bugs in the project's issue tracker or designated documentation.

## Getting Help
If you encounter any issues during setup or testing, consult the [IntelliJ IDEA documentation](https://www.jetbrains.com/idea/documentation/) or seek help on StackOverflow and the JetBrains community forums.
