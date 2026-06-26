JAVATO_HOME := .
WORK_DIR := src/benchmarks
APP_MAIN := benchmarks.testcases.TestRace1
APP_CLASSES := $(WORK_DIR)/classes
TMP_CLASSES := $(WORK_DIR)/tmpclasses

.PHONY: build clean instr graph fuzz cats

build:
	ant build

clean:
	rm -rf $(TMP_CLASSES)
	rm -f $(WORK_DIR)/iidToLine.map $(WORK_DIR)/iidToLine.map.html
	rm -f $(WORK_DIR)/error.list $(WORK_DIR)/error.log $(WORK_DIR)/error.stat

instr: clean build
	java -cp classes:lib/soot.jar \
		javato.activetesting.instrumentor.InstrumentorForActiveTesting \
		-keep-line-number \
		-process-dir $(APP_CLASSES) \
		-no-output-inner-classes-attribute \
		-d $(TMP_CLASSES) \
		-x javato \
		--app $(APP_MAIN)

# run analysis with GraphAnalysis
graph:
	java -ea \
		-Djavato.activetesting.analysis.class=javato.activetesting.GraphAnalysis \
		-cp $(TMP_CLASSES):$(APP_CLASSES):classes \
		$(APP_MAIN)

# run analysis with HybridAnalysis
race:
	java -ea \
		-Djavato.activetesting.analysis.class=javato.activetesting.HybridAnalysis \
		-cp $(TMP_CLASSES):$(APP_CLASSES):classes \
		$(APP_MAIN)

fuzz:
	python3 src/javato/cats/main.py --fuzz-only

cats: instr graph race fuzz