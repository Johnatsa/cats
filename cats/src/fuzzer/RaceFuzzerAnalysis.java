package fuzzer;

import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileReader;

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
public class RaceFuzzerAnalysis implements Analysis {
    
    private HashSet<Integer> targetRaceIids = new HashSet<>();
    
    public void initialize() {
        System.out.println("yoi");
        try{
            BufferedReader br = new BufferedReader(new FileReader("generated/race_iids.txt"));
            String line;
            while((line = br.readLine()) != null){
                if(!line.trim().isEmpty()){
                    targetRaceIids.add(Integer.parseInt(line.trim()));
                }
            } 
            br.close();
            System.out.println("RaceFuzzer loaded targets: " + targetRaceIids);
        } catch(Exception e){
            System.out.println("Couldn't read race_iids.txt" + e);
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
        System.out.println("Read hook triggered");
        if (targetRaceIids.contains(iid)) {
            ActiveChecker.fuzzDelay(iid);
            RaceOracle.checkCollision(memory, iid);
        }
    }

    public void writeBefore(Integer iid, Integer thread, Long memory) {
        System.out.println("Write hook triggered");
        if (targetRaceIids.contains(iid)) {
            ActiveChecker.fuzzDelay(iid);
            RaceOracle.checkCollision(memory, iid);
        }
    }

    public void finish() {
//  ignore this
    }
}
