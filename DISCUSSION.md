# Lab 3 Part 2: Jenkins Core Concepts Discussion

## Question
> **What do pipeline, node, agent, stage, and steps mean in the context of Jenkins?**

---

### 1. Pipeline
A **Pipeline** is an automated, user-defined model of a software delivery process. It defines the entire continuous integration and continuous delivery (CI/CD) workflow—from pulling source code from a version control system (e.g., GitHub), compiling, testing, and packaging, to deploying the application into production or staging environments.

* **Pipeline as Code:** In modern DevOps, pipelines are declared in a text file called a `Jenkinsfile` and stored alongside source code in version control.
* **In this Lab:** We implemented two pipelines:
  * `BinaryCalculator_pipeline` (using `Jenkinsfile`): A basic CI pipeline for building and testing.
  * `BinaryCalculator_cicd` (using `Jenkinsfile_v2`): A complete CI/CD pipeline that compiles Java code, packages a `.war` file, builds and pushes a Docker image to Google Artifact Registry, and deploys it onto a Google Kubernetes Engine (GKE) cluster.

---

### 2. Node
A **Node** is any physical machine, virtual machine (VM), or container that is part of the Jenkins environment and is capable of executing pipeline jobs.

* **Controller (Master) Node:** The primary Jenkins instance that hosts the web UI, manages configurations, schedules build jobs, and orchestrates workload distribution.
* **Worker (Agent) Node:** Dedicated compute instances that carry out the actual build and execution workloads so the controller is not overloaded.
* **In this Lab:** The GKE cluster worker machines (`e2-standard-2` VMs) act as the underlying nodes hosting the Jenkins controller pod and ephemeral build pods.

---

### 3. Agent
An **Agent** is an execution engine that runs on a node and carries out the tasks dispatched by the Jenkins controller. When a pipeline declares an `agent`, it specifies *where* and *how* the pipeline (or a specific stage) will run.

* **Types of Agents:**
  * Permanent agents (dedicated VMs or servers with static tooling).
  * Ephemeral/Dynamic container agents (spawned on-demand inside Kubernetes or Docker and destroyed immediately after job completion).
* **In this Lab:** We utilized dynamic Kubernetes pod agents in GKE configured with the `google/cloud-sdk:latest` container image (`agent { kubernetes { ... } }`). This allowed Jenkins to run Maven commands, authenticate with GCP using a Service Account, and execute `gcloud` and `kubectl` CLI commands in an isolated, disposable container environment.

---

### 4. Stage
A **Stage** is a distinct, logical phase of the pipeline that groups related steps together. Stages visually divide the CI/CD pipeline on the Jenkins dashboard, making it easy to monitor progress, calculate timing, and pinpoint exactly where a failure occurs.

* **Common Stages:** `Build`, `Test`, `Security Scan`, `Release`, `Deploy`.
* **In this Lab:** Our `BinaryCalculator_cicd` pipeline consists of 5 distinct stages:
  1. `test`: Runs automated unit tests with `mvn clean test`.
  2. `build`: Compiles and packages the web application into a `.war` archive.
  3. `containerize`: Submits the Docker build to Google Cloud Build and pushes the image to Google Artifact Registry.
  4. `deployment`: Applies the updated image to the Kubernetes deployment (`binarycalculator-deployment`).
  5. `service`: Exposes the application using a Kubernetes `LoadBalancer` service and retrieves its public external IP address.

---

### 5. Steps
**Steps** are the individual, granular tasks defined within a stage that instruct Jenkins on *what* action to execute sequentially. They are the fundamental building blocks of a pipeline.

* **Examples of Steps:** Executing shell commands (`sh`), changing directories (`dir`), switching containers (`container`), archiving build artifacts, or publishing test results.
* **In this Lab:** Examples of steps inside our `Jenkinsfile_v2` include:
  * `sh 'mvn clean test -f ./BinaryCalculatorWebapp/pom.xml'`
  * `sh 'gcloud auth activate-service-account --key-file $SERVICE_ACCOUNT'`
  * `sh 'gcloud builds submit -t $IMAGE_TAG --suppress-logs'`
  * `sh 'kubectl create deployment binarycalculator-deployment --image $IMAGE_TAG --port=8080'`

---

## Architectural Hierarchy Summary

```text
Pipeline (The entire CI/CD workflow)
└── Stages (Logical phases: Test, Build, Containerize, Deploy, Service)
    └── Steps (Granular tasks: sh, dir, container, kubectl, gcloud)
        └── Executed by an Agent (Dynamic Kubernetes pod container)
            └── Running on a Node (GKE compute worker VM)
```
