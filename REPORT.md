# Lab Report: Continuous Integration and Continuous Delivery (CI/CD) with Jenkins and Kubernetes Engine (GKE)

---

## 1. Executive Summary & Objectives
The objective of this laboratory is to design, implement, and evaluate an automated Continuous Integration and Continuous Delivery (CI/CD) workflow for a Java-based web application (**Binary Calculator**) using **Jenkins**, **Helm**, and **Google Kubernetes Engine (GKE)**. 

### Key Objectives:
1. Gain hands-on experience with Jenkins automation server and its plugin ecosystem.
2. Compare and implement two distinct Continuous Integration (CI) methodologies:
   - **Technique 1:** GUI-configured Maven project.
   - **Technique 2:** Pipeline-as-Code using a declarative `Jenkinsfile`.
3. Design and implement new functional features into the Binary Calculator core model, web controller, and REST API.
4. Establish an automated Continuous Deployment (CD) pipeline that dynamically provisions containerized build agents in Kubernetes, containerizes the application via Cloud Build, pushes artifacts to Artifact Registry, and deploys the application to a live Kubernetes cluster.

---

## 2. Discussion: Core Jenkins Architecture

### What do Pipeline, Node, Agent, Stage, and Steps mean in the context of Jenkins?

```text
+-----------------------------------------------------------------------------------+
|                                     PIPELINE                                      |
|  (Complete automated CI/CD workflow from source code commit to deployment)       |
|                                                                                   |
|  +--------------------+   +--------------------+   +---------------------------+  |
|  |    STAGE: Test     |   |    STAGE: Build    |   |    STAGE: Containerize    |  |
|  | +----------------+ |   | +----------------+ |   | +-----------------------+ |  |
|  | | Step: mvn test | |   | | Step: mvn pack | |   | | Step: gcloud build... | |  |
|  | +----------------+ |   | +----------------+ |   | +-----------------------+ |  |
|  +--------------------+   +--------------------+   +---------------------------+  |
|                                                                                   |
|  Executed by: AGENT (Dynamic Kubernetes container pod)                            |
|  Running on:  NODE  (GKE Virtual Machine compute instance)                        |
+-----------------------------------------------------------------------------------+
```

#### 1. Pipeline
A **Pipeline** is an automated, user-defined model of a software delivery workflow. It defines every phase required for a software change to go from a code repository to a running deployment. In modern software engineering, pipelines are written as code (Pipeline-as-Code) and stored in version control (`Jenkinsfile`), enabling versioning, code reviews, and reproducibility.

#### 2. Node
A **Node** is any physical computer, virtual machine, or container host that is part of the Jenkins infrastructure and capable of executing jobs. 
* **Controller (Master) Node:** Coordinates scheduling, provides the web dashboard, manages plugins, and monitors workload distribution.
* **Worker (Agent) Node:** Provides compute capacity to execute tasks independently so the controller remains responsive.

#### 3. Agent
An **Agent** is the runtime entity that executes steps assigned by the Jenkins controller. An agent can be a dedicated permanent server or an **ephemeral container agent** spawned on-demand inside Kubernetes. Dynamic agents provide clean, reproducible build environments that scale automatically and terminate immediately upon build completion.

#### 4. Stage
A **Stage** is a distinct logical phase within a pipeline that groups related tasks together (e.g., `Test`, `Build`, `Containerize`, `Deploy`). Stages provide clear visual progress monitoring on the Jenkins dashboard, calculate stage-by-stage timings, and isolate failures.

#### 5. Steps
**Steps** are the fundamental, granular execution commands within a stage. They instruct Jenkins on exact operations to carry out sequentially, such as running a shell script (`sh`), changing working directories (`dir`), or checking out source code (`git`).

---

## 3. Continuous Integration (CI) – The Two Techniques

Continuous Integration ensures that software is validated continuously upon every commit, minimizing integration issues and catching defects early.

### Technique 1: GUI-Configured Maven Project (`binaryCalculate_mvn`)
In this approach, the entire CI lifecycle is configured through the Jenkins web interface:
* **Source Code Management (SCM):** Configured to track the Git repository.
* **Build Triggers:** Enabled *GitHub hook trigger for GITScm polling* to trigger builds on incoming webhook payloads.
* **Build Configuration:** Configured Maven with root POM `BinaryCalculatorWebapp/pom.xml` and build goal `clean package`.
* **Post-Build Feedback:** Integrated *Set GitHub commit status* to report build success or failure directly back to the GitHub commit list as a status checkmark.

### Technique 2: Pipeline-as-Code (`BinaryCalculator_pipeline`)
In this approach, the CI pipeline is declared within a version-controlled `Jenkinsfile`:
* **Advantages over GUI Configuration:**
  - Pipeline logic is versioned alongside the application code.
  - Can be tracked, branched, and reviewed via standard Pull Requests.
  - Portable across Jenkins servers without manual GUI reconfiguration.
* **Pipeline Structure:**
  1. `Init`: Prepares the workspace and logs build information.
  2. `test`: Runs automated unit tests via `mvn clean test`.
  3. `build`: Packages the compiled application into a `.war` archive using `mvn package -DskipTests`.
  4. `Deploy`: A mock delivery validation stage.

---

## 4. Design Section: Binary Calculator Enhancement

### Functional Enhancements
The application was upgraded from supporting basic addition to a comprehensive Binary Arithmetic and Logic engine:

| Operation | Operator | Formula / Bitwise Logic | Example | Result |
| :--- | :---: | :--- | :---: | :---: |
| **Addition** | `+` | Arithmetic binary sum with carry bits | `101 + 11` | `1000` |
| **Multiplication**| `*` | Shift-and-add binary multiplication ($2 \times 3$) | `10 * 11` | `110` ($6$) |
| **Bitwise OR** | `\|` | Logical OR on corresponding binary digits ($1 \lor 0 = 1$) | `1010 \| 1100` | `1110` |
| **Bitwise AND** | `&` | Logical AND on corresponding binary digits ($1 \land 1 = 1$) | `1010 & 1100` | `1000` |

### Architectural Code Changes
1. **Model Layer (`Binary.java`):**
   * Implemented `multiply(Binary num1, Binary num2)` using a binary shift-and-add algorithm.
   * Implemented `or(Binary num1, Binary num2)` for bitwise logical disjunction across operands of equal or variable lengths.
   * Implemented `and(Binary num1, Binary num2)` for bitwise logical conjunction.
2. **Web Controller Layer (`BinaryController.java`):**
   * Updated the `switch(operator)` dispatcher to process `*`, `|`, and `&` requests submitted from the web UI.
3. **REST API Layer (`BinaryAPIController.java`):**
   * Added REST endpoints for plain string responses: `/multiply`, `/or`, `/and`.
   * Added REST endpoints for JSON responses: `/multiply_json`, `/or_json`, `/and_json`.
4. **Verification & Test Suite:**
   * Expanded test coverage across `BinaryTest.java`, `BinaryControllerTest.java`, and `BinaryAPIControllerTest.java`.
   * **Result:** **35 automated test cases executed, 0 failures, 0 errors.**

---

## 5. Continuous Deployment (CD) Pipeline Architecture

The complete Continuous Deployment workflow is governed by `Jenkinsfile_v2` and cloud-native services:

```text
Developer Git Push
       |
       v
GitHub Webhook  --->  Jenkins Controller (on GKE)
                             |
                             v  (Spawns Ephemeral Agent Pod)
                 [Kubernetes Cloud SDK Agent Pod]
                             |
     +-----------------------+-----------------------+
     |                       |                       |
     v                       v                       v
1. Maven Test & Build    2. Google Cloud Build    3. GKE Rollout
(Compiles .war file)     (Pushes Docker image    (Rolling update of
                          to Artifact Registry)   Deployment & Service)
```

### Pipeline Stages in Detail:
1. **`test` Stage:** Executes the full Maven test suite within the ephemeral agent.
2. **`build` Stage:** Generates the production-ready `.war` web archive.
3. **`containerize` Stage:** Authenticates via a GCP Service Account, triggers Google Cloud Build, packages the Docker container, and publishes the image to Google Artifact Registry.
4. **`deployment` Stage:** Issues `kubectl` commands to perform a rolling update of the Kubernetes Deployment on GKE.
5. **`service` Stage:** Verifies the Kubernetes `LoadBalancer` service and outputs the external IP address for public access.

---

## 6. Verification and Testing

### Live Application Verification
The application was deployed and verified over a public Kubernetes LoadBalancer endpoint:

* **Endpoint:** `http://<EXTERNAL-IP>:8080`
* **Test Case 1 (Addition):** `101` + `11` = `1000` *(Verified)*
* **Test Case 2 (Multiplication):** `10` * `11` = `110` *(Verified)*
* **Test Case 3 (Bitwise OR):** `1010` | `1100` = `1110` *(Verified)*
* **Test Case 4 (Bitwise AND):** `1010` & `1100` = `1000` *(Verified)*

### CI/CD Automation Verification
* Any code change pushed to the main repository triggers the GitHub Webhook within seconds.
* The pipeline automatically compiles, tests, containerizes, and rolls out the update to the cluster with zero manual server configuration.

---

## 7. Conclusion
This laboratory demonstrated the full lifecycle of modern DevOps and Cloud-Native software engineering. By uniting Jenkins with Kubernetes and Google Cloud Platform:
* Build environments are fully isolated, reproducible, and scalable.
* Software bugs are detected immediately via automated testing stages.
* Production deployments are fully automated, eliminating human error in release delivery.
