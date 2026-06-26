import json
import random
import subprocess

TAPE = "/tmp/tape.txt"
FEEDBACK = "/tmp/fuzzer_feedback.txt"
TARGET = ["java", "-cp", "file.jar", "javato.activetesting.Main"]
#* Tape is a dictionary: {iid : delay}. iid is the results of the static analysis, delay is the ammount of sleep that the thread will sleep 
#* initial_list is the result of the static analysis

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

def get_distance():
    with open (FEEDBACK, "r") as f:
        return int(f.read().strip())


def save_tape(tape):
    with open(TAPE, "w") as f:
        json.dump(tape, f)

def run_fuzzer(initial_list):
    best_tape = {iid : 0 for iid in initial_list}
    best_distance = float('inf')

    execs = 0
    while(1):
        execs += 1
        current_tape = mutate_tape(best_tape)
        save_tape(current_tape)

        try:
            result = subprocess.run(TARGET, capture_output=True, text=True, timeout=5.0)
        except subprocess.TimeoutExpired:
            #Caused a hang
            continue
        if result.returncode == 4242:
            #Got a race condition 
            #Save it somewhere
            #either break or continue to some depth
            while(0):       # put only so no errors exist
                pass
        if result.returncode == 4243 || result.returncode == 4244:
            #Got a deadlock
            #Save it somewhere
            #either break or continue to some depth
            while(0):       # put only so no errors exist
                pass


        current_distance = get_distance()

        if current_distance < best_distance:
            best_distance = current_distance
            best_tape = current_tape
        else:
            pass

    
    #initial_list = [] #the result of the racerD analysis
    #run_fuzzer(initial_list)

def get_initial_list():
    deadlock_iids = read_file("graph_iids.txt")  # from GraphAnalysis
    race_iids = read_file("race_iids.txt")       # from HybridAnalysis
    return list(set(deadlock_iids + race_iids))  # merge and deduplicate

def read_file (filename):
    # Read
    with open(filename, "r") as f:
        return [line.strip() for line in f.readlines()]
