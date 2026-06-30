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
import argparse


from fuzzer import run_deadlock_phase, run_fuzzer, get_initial_list

def main():
    parser = argparse.ArgumentParser(description="CATS Concurrency Fuzzer")
    parser.add_argument("--app", required=True, help="Target Java class (passed from Makefile)")
    parser.add_argument("--deadlock", action="store_true", help="Run Deadlock phase (1 execution)")
    parser.add_argument("--race", action="store_true", help="Run Race condition phase (mutations)")
    
    args = parser.parse_args()

    run_both = not args.deadlock and not args.race
    
    if args.deadlock or run_both:
        run_deadlock_phase(args.app)
        
    if args.race or run_both:
        initial_list = get_initial_list()
        run_fuzzer(args.app, initial_list)


if __name__ == "__main__":
    main()