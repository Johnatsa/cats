JAVATO_HOME := .
WORK_DIR := src/benchmarks
APP_MAIN := benchmarks.testcases.TestRace1
APP_CLASSES := $(WORK_DIR)/classes
TMP_CLASSES := $(WORK_DIR)/tmpclasses

# --- UPDATED FOR JAVA 7 ---
JAVA_HOME := /usr/lib/jvm/zulu7-ca-amd64
JAVA := $(JAVA_HOME)/bin/java
JAVAC := $(JAVA_HOME)/bin/javac
SOURCE_LEVEL := 1.7
TARGET_LEVEL := 1.7
# --------------------------

JAVA7_RT := $(JAVA_HOME)/jre/lib/rt.jar
JAVA7_JCE := $(JAVA_HOME)/jre/lib/jce.jar
JAVA_BOOT_ARG := $(if $(wildcard $(JAVA7_RT)),-Dsun.boot.class.path=$(JAVA7_RT):$(JAVA7_JCE),)

empty :=
space := $(empty) $(empty)
JARS := $(wildcard lib/*.jar)
JAR_CP := $(subst $(space),:,$(JARS))
TOOL_CP := classes:$(APP_CLASSES):$(JAR_CP)
RUNTIME_CP := $(TMP_CLASSES):$(APP_CLASSES):classes:$(JAR_CP)

.PHONY: build clean instr graph fuzz cats

build: build-tools build-app

build-tools:
	mkdir -p classes
	find src/javato src/lockWaitGraph -name '*.java' > /tmp/cats_tool_sources.txt
	$(JAVAC) -source $(SOURCE_LEVEL) -target $(TARGET_LEVEL) -cp "$(TOOL_CP)" -d classes @/tmp/cats_tool_sources.txt

build-app:
	mkdir -p $(APP_CLASSES)
	$(JAVAC) -source $(SOURCE_LEVEL) -target $(TARGET_LEVEL) -cp "classes:$(APP_CLASSES)" -sourcepath src -d $(APP_CLASSES) src/$(subst .,/,$(APP_MAIN)).java

clean:
	rm -rf $(TMP_CLASSES)
	rm -f $(WORK_DIR)/iidToLine.map $(WORK_DIR)/iidToLine.map.html
	rm -f $(WORK_DIR)/error.list $(WORK_DIR)/error.log $(WORK_DIR)/error.stat
	rm -f graph_cycles.json graph_iids.txt race_iids.txt

instr: clean build
	$(JAVA) $(JAVA_BOOT_ARG) -cp "$(TOOL_CP)" \
		javato.activetesting.instrumentor.InstrumentorForActiveTesting \
		-keep-line-number \
		-process-dir $(APP_CLASSES) \
		-no-output-inner-classes-attribute \
		-d $(TMP_CLASSES) \
		-x javato \
		--app $(APP_MAIN)

# run analysis with GraphAnalysis
graph:
	$(JAVA) -ea \
		-Djavato.activetesting.analysis.class=javato.activetesting.GraphAnalysis \
		-cp "$(RUNTIME_CP)" \
		$(APP_MAIN)

# run analysis with HybridAnalysis
race:
	$(JAVA) -ea \
		-Djavato.activetesting.analysis.class=javato.activetesting.HybridAnalysis \
		-cp "$(RUNTIME_CP)" \
		$(APP_MAIN)

fuzz:
	python3 src/javato/cats/main.py

cats: instr graph race fuzz
