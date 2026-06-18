package javato.activetesting;

import javato.activetesting.analysis.CheckerAnalysisImpl;
import javato.activetesting.common.Parameters;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Collection;

import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.HybridRaceTracker;
import javato.activetesting.analysis.Observer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileWriter;

/**
 * Copyright (c) 2007-2008,
 * Koushik Sen    <ksen@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class RaceFuzzerAnalysis extends CheckerAnalysisImpl {
    private CommutativePair racePair;

    public void initialize() {
        if (Parameters.errorId >= 0) {
            LinkedHashSet<CommutativePair> seenRaces = HybridRaceTracker.getRacesFromFile();
            racePair = (CommutativePair) (seenRaces.toArray())[Parameters.errorId - 1];
        }
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) {
//  ignore this
    }

    public void methodEnterBefore(Integer iid) {
//  ignore this
    }

    public void methodExitAfter(Integer iid) {
//  ignore this
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
//  ignore this
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
//  ignore this
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
//  ignore this
    }

    public void readBefore(Integer iid, Integer thread, Long memory) {
        if (racePair != null && racePair.contains(iid)) {
            ActiveChecker.fuzzDelay(iid);
            RaceOracle.checkCollision(memory, iid);
        }
    }

    public void writeBefore(Integer iid, Integer thread, Long memory) {
        if (racePair != null && racePair.contains(iid)) {
            ActiveChecker.fuzzDelay(iid);
            RaceOracle.checkCollision(memory, iid);    
        }
    }

    public void finish() {
//  ignore this
    }
}


public class RaceOracle{
    //How many threads are in a specific area right now
    static ConcurrentHashMap<Long, Set<Long>> activeAccesses = new ConcurrentHashMap<>();

    //The last time each address was touched
    static ConcurrentHashMap<Long, Long> lastAccessTime = new ConcurrentHashMap<>();

    public static void checkCollision(Long memoryAddress, Integer iid){
        long currentTime = System.nanoTime();

        activeAccesses.putIfAbsent(memoryAddress, ConcurrentHashMap.newKeySet());
        Set<Long> threadsPresent = activeAccesses.get(memoryAddress);

        long myThreadId = Thread.currentThread().getId(); 
        threadsPresent.add(myThreadId);

        if(threadsPresent.size() > 1){
            //Win! Found a data race
            System.exit(4242); //Code for our fuzzer to know that it exited from a date race
        }

        //Calculate how far apart were the thread accesses to a specific memory address in order to guide the mutations
        Long previousTime = lastAccessTime.put(memoryAddress, currentTime); //Returns the previous value
        if (previousTime != null){
            long distance = Math.abs(currentTime - previousTime);
            writeFeedback(distance);
        }

        threadsPresent.remove(myThreadId);
    }

    public static synchronized void writeFeedback(long distance){
        FileWriter fw = new FileWriter("/tmp/fuzzer_feedback.txt");
        fw.write(String.valueOf(distance));
    }
}