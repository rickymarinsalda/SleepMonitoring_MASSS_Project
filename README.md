# SleepMonitoring_MASSS_Project

Evaluation of power consumption when adopting different architectures, like moving part of the computation onto external devices (a smartphone) in Android sleep monitoring applications


## Device Connection and Energy Consumption Experimentation

To monitor and analyze the energy consumption of algorithms on your smartwatch and smartphone, follow these steps:

1. **Check Connected Devices:**
   To see the list of connected devices, use the following command:
   ```sh
   adb devices
   ```
2. **Start the Energy Consumption Experiment:**
   To reset battery statistics and begin the experiment, execute:
   ```sh
   adb shell dumpsys batterystats --reset
   ```
3. **End the Experiment and Save the Results:**
   Once the algorithms have been tested with the sample data, save the results with this command:
   ```sh
   adb bugreport bugreport.zip
   ```
4. **Analyze the Results:**
   Use the generated `bugreport.zip` file with [Battery Historian](https://developer.android.com/studio/profile/battery-historian) to analyze the energy consumption measurements.

By following these steps, you can analyze the energy consumption of your algorithms both on the smartwatch and the smartphone, providing valuable insights into their performance and efficiency.
