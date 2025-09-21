# Java Performance Profiling

This project is a collection of small, focused Java programs to explore and demonstrate performance characteristics of concurrent systems using:

- Java Flight Recorder (JFR)
- Java Mission Control (JMC)
- Thread pools, blocking, starvation, deadlocks, GC pressure, and more

## 🔬 Demos

### 1. Thread Pool Starvation

File: `StarvationDemo.java`  
Description: Demonstrates a simple thread pool starvation scenario using nested `Future.get()` calls.

To run with JFR enabled:
```bash
java -XX:StartFlightRecording=duration=60s,filename=starvation.jfr,settings=profile \
     -cp target/java-performance-profiling-*.jar \
     org.rks.java.performance.StarvationDemo

📊 Tools Used
	•	Java 21
	•	JFR + JMC
	•	Maven
