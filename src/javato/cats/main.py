# main.py
#   1. call ant -f run.xml instr
#      -> runs javato.activetesting.instrumentor.InstrumentorForActiveTesting
#      -> uses VisitorForActiveTesting to insert tags/callbacks

#   2. run the instrumented program once with a CATS analysis class
#      -> e.g. javato.activetesting.GraphAnalysis
#      -> callbacks receive iid + thread id + lock/memory id
#      -> fills LockWaitGraph

#   3. call the fuzzer
#      -> uses the graph / candidate ids
#      -> repeatedly runs the instrumented target
#      -> reports race/deadlock if return code says 4242 / 4243


import subprocess
from fuzzer import run_fuzzer, get_initial_list


def main():
    # Step 1: Instrument the target program
    subprocess.run(["make", "instr"], check=True)
    
    # Step 2: Run the instrumented program with CATS analysis
    subprocess.run(["make", "graph"], check=True)

    # Step 3: Call the fuzzer
    initial_list = get_initial_list()
    run_fuzzer(initial_list)
