# Java Performance Profiling Demos

This project demonstrates common Java performance issues, visualized using [Java Flight Recorder (JFR)](https://docs.oracle.com/en/java/javase/17/jfapi/java-flight-recorder-api.html) and [Java Mission Control (JMC)](https://www.oracle.com/java/technologies/javacommunity/javamicont.html).

Each demo is a standalone Java class with a `main()` method, launched using Maven profiles and instrumented with JFR.

---

## 🏗️ Build

```bash
mvn compile
```

---

## 🚀 Run Demos with JFR Enabled

### 🔁 Lock Contention Demo

Simulates multiple threads contending for a shared monitor lock.

```bash
mvn compile exec:exec -Plockcontention
```

This will:
- Start 5 threads trying to acquire the same lock
- Record thread contention using JFR
- Write the output to `lockcontention.jfr`

### 🌟 Starvation Demo

Demonstrates thread starvation due to nested task submission in a fixed thread pool.

```bash
mvn compile exec:exec -Pstarvation
```

---

## 📂 Profiles

Defined in `pom.xml`:

| Profile ID       | Description                      | Main Class                                                   | Output                |
|------------------|----------------------------------|---------------------------------------------------------------|------------------------|
| `lockcontention` | Lock contention simulation       | `org.rks.java.performance.LockContentionDemo`                | `lockcontention.jfr`   |
| `starvation`     | Thread starvation via deadlock   | `org.rks.java.performance.StarvationDemo`                    | `starvation.jfr`       |

---

## 📊 Analyze `.jfr` Files with JMC

### 1. Launch JMC

```bash
jmc
```

Or install via Homebrew (macOS):

```bash
brew install --cask jmc
```

### 2. Open `.jfr` File

Choose either `lockcontention.jfr` or `starvation.jfr`.

### 3. Explore in JMC

#### 🔒 Lock Contention

- Go to: `Java Application → Lock Instances`
- Also see: `Event Browser → Java Monitor Blocked`
- Shows which threads were blocked and for how long

#### ⚙️ Thread Behavior

- `Java Application → Threads`: view thread states over time
- `Method Profiling`: identify hot methods related to contention

---

## 🧪 TODOs & Ideas

- Add more demos (false sharing, ReentrantLock contention, GC pressure)
- Automate JFR reporting or convert to CSV
- GitHub Actions to capture `.jfr` as artifacts

---

## 🧠 Author

**Rk** – Built for JVM performance introspection and demoing real-world concurrency bottlenecks.
