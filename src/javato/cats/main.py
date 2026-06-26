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



from fuzzer import run_fuzzer, get_initial_list

def main():
    # Step 3: Call the fuzzer directly, assuming Make already did Steps 1 & 2
    initial_list = get_initial_list()
    run_fuzzer(initial_list)

if __name__ == "__main__":
    main()