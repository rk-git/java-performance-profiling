# Java Performance Profiling

This repository contains self-contained Java programs that simulate **real-world concurrency bottlenecks**. Each program is designed to be profiled using [Java Flight Recorder (JFR)](https://docs.oracle.com/en/java/javase/17/jfapi/java-flight-recorder-api.html) and analyzed in [Java Mission Control (JMC)](https://www.oracle.com/java/technologies/javamicot.html).

The goal is to provide educational, measurable, and visual feedback on JVM performance topics such as thread contention, starvation, and lock contention in I/O-heavy environments.

---

## 🧱 Build

To compile all demos:

```bash
mvn compile
```

---

## 🚀 Run Demos with JFR Enabled

Each demo is run via a Maven profile. JFR recording starts automatically and writes a `.jfr` file for later inspection.

---

### 🔁 Lock Contention

Simulates multiple threads repeatedly acquiring a shared lock via `synchronized`.

```bash
mvn compile exec:exec -Plockcontention
```

- Threads: 20
- Fixed thread pool size: 4
- Expected: monitor contention on a single shared object

🗂️ Output: `lockcontention.jfr`

---

### 🌟 Thread Starvation

Illustrates how a fixed thread pool can deadlock itself when tasks submit nested tasks that block.

```bash
mvn compile exec:exec -Pstarvation
```

- Fixed thread pool size: 2
- Each task submits a nested task and calls `.get()`
- Expected: outer threads wait forever, executor deadlock

🗂️ Output: `starvation.jfr`

---

### 📣 System.out Contention

Demonstrates performance bottlenecks caused by multiple threads writing to `System.out`, which is internally synchronized.

```bash
mvn compile exec:exec -Psystemoutcontention
```

- Threads: 20
- Only 8 threads processing at a time
- Expected: heavy contention on internal `PrintStream` lock

🗂️ Output: `systemoutcontention.jfr`

---

## 📂 Maven Profiles

The `pom.xml` defines the following runnable profiles:

| Profile ID            | Description                             | Main Class                                            | JFR Output File           |
|------------------------|-----------------------------------------|--------------------------------------------------------|---------------------------|
| `lockcontention`       | Lock contention via synchronized block  | `org.rks.java.performance.LockContentionDemo`          | `lockcontention.jfr`      |
| `starvation`           | Thread starvation via nested futures    | `org.rks.java.performance.StarvationDemo`              | `starvation.jfr`          |
| `systemoutcontention`  | Contention on System.out lock           | `org.rks.java.performance.SystemOutContention`         | `systemoutcontention.jfr` |

---

## 📊 Analyzing `.jfr` Files

Use [Java Mission Control (JMC)](https://www.oracle.com/java/technologies/javamicot.html) to inspect `.jfr` recordings.

### 🔧 Install JMC (macOS)

```bash
brew install --cask jmc
```

### 📈 Recommended JMC Views

#### 🔒 Lock Contention

- **Java Application → Lock Instances**
- **Event Browser → Java Monitor Blocked**

Use these to:
- See which threads were blocked
- Identify which locks caused contention
- Measure time spent waiting

#### ⚙️ Thread Activity

- **Threads timeline** to see blocked/running states
- **Method profiling** to detect hotspots

---

## 🧪 Ideas for Expansion

- ReentrantLock vs `synchronized` contention
- Logging frameworks with async vs sync writers
- GC pressure demos (allocation rate vs pause time)
- False sharing and memory layout effects
- Integration with CI to archive `.jfr` files

---

## 🧠 Author

**Rk**  
Email: `rks.0728@gmail.com`  
Focus: JVM concurrency profiling, performance visualization, and system behavior under load.
