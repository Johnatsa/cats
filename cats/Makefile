JAVATO_HOME := .
# Your new test directory
WORK_DIR := tests
# Assuming TestRace1.java has "package tests;" at the top
APP_MAIN := race_tests.TestRace1
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

# Tools only need the libraries (like Jackson) to compile
TOOL_CP := classes:$(JAR_CP)
# Runtime needs everything
RUNTIME_CP := $(TMP_CLASSES):$(APP_CLASSES):classes:$(JAR_CP)

.PHONY: build clean instr graph race fuzz cats

build: build-tools build-app

build-tools:
	@echo "--- Compiling Fuzzer & Instrumentor Tools ---"
	mkdir -p classes
	# This single command grabs ALL your fuzzer, instrumentors, and lockWaitGraph files natively
	find src -name '*.java' > /tmp/cats_tool_sources.txt
	$(JAVAC) -source $(SOURCE_LEVEL) -target $(TARGET_LEVEL) -cp "$(TOOL_CP)" -d classes @/tmp/cats_tool_sources.txt

build-app:
	@echo "--- Compiling Test Applications ---"
	mkdir -p $(APP_CLASSES)
	# Grabs all java files in your tests directory
	find tests -name '*.java' > /tmp/cats_test_sources.txt
	$(JAVAC) -source $(SOURCE_LEVEL) -target $(TARGET_LEVEL) -cp "classes:$(APP_CLASSES)" -d $(APP_CLASSES) @/tmp/cats_test_sources.txt

clean:
	@echo "--- Wiping all compiled artifacts ---"
	rm -rf classes
	rm -rf $(APP_CLASSES)
	rm -rf $(TMP_CLASSES)
	rm -rf generated/*
	rm -f /tmp/cats_tool_sources.txt /tmp/cats_test_sources.txt

instr: clean build
	@echo "--- Running Soot Instrumentor ---"
	$(JAVA) $(JAVA_BOOT_ARG) -cp "$(TOOL_CP):$(APP_CLASSES)" \
		inside_instrumentor.InstrumentorForActiveTesting \
		-keep-line-number \
		-process-dir $(APP_CLASSES) \
		-no-output-inner-classes-attribute \
		-d $(TMP_CLASSES) \
		-x fuzzer -x outside_insturmentor -x outside_instrumentor -x inside_instrumentor -x lockWaitGraph \
		--app $(APP_MAIN)
graph:
	@echo "--- Running Graph Analysis ---"
	-$(JAVA) -ea \
		-DCATS_BRAIN=fuzzer.GraphAnalysis \
		-cp "$(RUNTIME_CP)" \
		$(APP_MAIN)

race:
	@echo "--- Running Race/Hybrid Analysis ---"
	$(JAVA) -ea \
		-DCATS_BRAIN=fuzzer.HybridAnalysis \
		-cp "$(RUNTIME_CP)" \
		$(APP_MAIN)

fuzz:
	@echo "--- Starting Fuzzer Python Script ---"
	# Updated to point directly to main.py at the root
	python3 main.py --app $(APP_MAIN) $(FUZZ_MODE)

cats: instr
	@echo "--- Routing Analysis Phase based on FUZZ_MODE ---"
	@if [ "$(FUZZ_MODE)" = "--deadlock" ]; then \
		$(MAKE) graph; \
	elif [ "$(FUZZ_MODE)" = "--race" ]; then \
		$(MAKE) race; \
	else \
		$(MAKE) graph; \
		$(MAKE) race; \
	fi
	@echo "--- Starting Fuzzer ---"
	$(MAKE) fuzz