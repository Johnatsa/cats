import json
import random
import os
import subprocess


TAPE = "/tmp/tape.txt"
FEEDBACK = "/tmp/fuzzer_feedback.txt"

def build_target(app_main, analysis_class):
    # The -D flag MUST come before the -cp
    return [
        "java", 
        "-ea",
        f"-Djavato.activetesting.analysis.class={analysis_class}",
        "-cp", "src/benchmarks/tmpclasses:src/benchmarks/classes:classes:lib/soot.jar:lib/ant-contrib.jar:lib/asm-3.1.jar:lib/jackson-annotations-2.8.11.jar:lib/jackson-core-2.8.11.jar:lib/jackson-databind-2.8.11.jar:lib/servlet.jar",
        app_main
    ]

def mutate_tape(current_tape):
    new_tape = current_tape.copy()
    target_iid = random.choice(list(new_tape.keys()))
    strategy = random.random()

    #! Can we make it not fully random?
    if(strategy < 0.60):
        #Add or substract 10ms of sleep
        adjustment = random.randint(-10,10)
        new_tape[target_iid] = max(0, new_tape[target_iid] + adjustment)
    elif (strategy < 0.90):
        # no sleep or force yield
        new_tape[target_iid] = random.choice([0,500])
    else:
        #Completely random new delay
        new_tape[target_iid] = random.randint(0,100)
    return new_tape

def get_distance():
    if not os.path.exists(FEEDBACK):
        return float('inf')

    with open (FEEDBACK, "r") as f:
        return int(f.read().strip())


def save_tape(tape):
    with open(TAPE, "w") as f:
        json.dump(tape, f)


def run_deadlock_phase(app_main):
    print(f"Deadlock search for: {app_main}")
    target = build_target(app_main, "javato.activetesting.DeadlockFuzzerAnalysis")
    
    result = subprocess.run(target, capture_output=True, text=True, timeout=10.0)
    if result.returncode == 4243:
        print("Deadlock found")
    elif result.returncode == 4244:
        print("WaitOnly thread found")



def run_fuzzer(app_main, initial_list):
    print(f"Race condition search for: {app_main}")
    target = build_target(app_main, "javato.activetesting.RaceFuzzerAnalysis")

    best_tape = {iid : 0 for iid in initial_list}
    best_distance = float('inf')

    execs = 0
    while(execs < 1000):
        if(execs % 100 ==0):
            print("Execution " + str(execs))
        execs += 1
        current_tape = mutate_tape(best_tape)
        save_tape(current_tape)

        try:
            #print(f"DEBUG: Running command: {' '.join(target)}")
            result = subprocess.run(target, capture_output=True, text=True, timeout=5.0)
        except subprocess.TimeoutExpired:
            continue

        #debug
        if result.returncode != 0 and result.returncode not in [4242, 4243, 4244]:
                print(f"Java crashed! Return code: {result.returncode}")
                print(f"Error output:\n{result.stderr}")
                break 

        if result.returncode == 4242:
            print("Race condition found")
            
        current_distance = get_distance()

        if current_distance < best_distance:
            best_distance = current_distance
            best_tape = current_tape
        else:
            pass
    print("Executions finished, check the /src/javato/activetesting/cats/bugs.txt file")
    
    #initial_list = [] #the result of the racerD analysis
    #run_fuzzer(initial_list)

def get_initial_list():
    try:
        with open("race_iids.txt", "r") as f:
            return list(set([line.strip() for line in f.readlines() if line.strip()]))
    except FileNotFoundError:
        return []

def read_file (filename):
    # Read
    with open(filename, "r") as f:
        return [line.strip() for line in f.readlines()]
