/*package bni;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Oto Stanko
 */
/*public class newInitialStatesGenerator {

    public static String generateInitialStates(String network, String numStates, String[] dirs) throws IOException {
        mod.jmut.core.Config.out("generateInitialStates", String.valueOf(numStates));

        int numNodes;
        try {
            numNodes = Integer.parseInt(network);
        } catch (Exception var8) {
            mod.jmut.core.comp.NetData netD = mod.jmut.core.Calc.datas.get(network);
            if (netD == null) {
                return null;
            }

            numNodes = netD.nodes.size();
        }

        int NumOfAllPossibleStates = (int)Math.pow(2.0D, numNodes);
        int noStates;
        if (numStates.equalsIgnoreCase("all")) {
            noStates = NumOfAllPossibleStates;
        } else {
            noStates = Integer.parseInt(numStates);
            if (noStates > NumOfAllPossibleStates) {
                noStates = NumOfAllPossibleStates;
            }
        }

        HashSet<boolean[]> ExaminingStates = generateInitialStates(noStates, numNodes, dirs);
        //boolean[][] _states = mod.jmut.core.Util.convertInitialStatesToBoolean(ExaminingStates, numNodes);
        boolean[][] _states = statesToBoolean(ExaminingStates, numNodes);
        String stateSet = numNodes + "Nodes_" + noStates + " states";
        mod.jmut.core.Calc.states.put(stateSet, new mod.jmut.core.comp.InitialStates(_states));
        return stateSet;
    }


    public static boolean[][] statesToBoolean(HashSet<boolean[]> ExaminingStates, int numNodes) {
        int numStates = ExaminingStates.size();
        boolean[][] states = new boolean[numStates][numNodes];
        int index = 0;

        for (boolean[] examiningState : ExaminingStates) {
            states[index] = examiningState;
        }
        return states;
    }


    public static HashSet<boolean[]> generateInitialStates(long NumOfRandomStates,
                                                           int numNodes,
                                                           String[] dirs) throws IOException {
        HashSet<boolean[]> ExaminingStates = new HashSet<>();
        mod.jmut.core.Config.out("generated", "Have to generate " + NumOfRandomStates + " states");
        //boolean[][] ExaminingStates = new boolean[(int)NumOfRandomStates][numNodes];
        int counter = 0;

        do {
            counter += 1;
            boolean[] new_state = new boolean[numNodes];
            for(int i=0; i<numNodes; i++) {
                new_state[i] = Math.random() < 0.50;
            }
            //mod.jmut.core.Config.out("generated", Arrays.toString(new_state));
            ExaminingStates.add(new_state);
        } while((long)ExaminingStates.size() != NumOfRandomStates);

        mod.jmut.core.Config.out("generated", counter + " states until I got " + NumOfRandomStates + " states");
        File file = new File(dirs[dirs.length-1] + "generatedStates.txt");
        file.createNewFile();
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        writer.println(counter);
        writer.flush();
        writer.close();
        return ExaminingStates;
    }
}*/



package bni;

import java.io.IOException;
import java.util.TreeSet;

/**
 * @author Oto Stanko
 */
public class newInitialStatesGenerator {

    private static int NUMGENERATEDSTATES = 0;

    public static String generateInitialStates(String network, String numStates) throws IOException {
        mod.jmut.core.Config.out("generateInitialStates", String.valueOf(numStates));

        NUMGENERATEDSTATES = 0;
        int numNodes;
        try {
            numNodes = Integer.parseInt(network);
        } catch (Exception var8) {
            mod.jmut.core.comp.NetData netD = mod.jmut.core.Calc.datas.get(network);
            if (netD == null) {
                return null;
            }

            numNodes = netD.nodes.size();
        }

        int NumOfAllPossibleStates = (int)Math.pow(2.0D, numNodes);
        int noStates;
        if (numStates.equalsIgnoreCase("all")) {
            noStates = NumOfAllPossibleStates;
        } else {
            noStates = Integer.parseInt(numStates);
            if (noStates > NumOfAllPossibleStates) {
                noStates = NumOfAllPossibleStates;
            }
        }


        TreeSet<String> ExaminingStates;
        int method = 1;
        // if noStates is more than 1.4621*2^(numNodes-1), call getAllCombinations()
        if(noStates > (1.4621 * ((int)Math.pow(2.0D, numNodes-1)) )) {
            // method number 1
            // deterministically generate all states
            ExaminingStates = getAllCombinations(numNodes);
            if(NumOfAllPossibleStates-noStates != 0) {
                TreeSet<String> statesToRemove = generateInitialStates(NumOfAllPossibleStates-noStates,
                        numNodes);
                // remove some portion of random states
                ExaminingStates.removeAll(statesToRemove);
            }
            NUMGENERATEDSTATES += NumOfAllPossibleStates;
        }
        else {
            // get all required states by randomly generating them, method number 0
            method = 0;
            ExaminingStates = generateInitialStates(noStates, numNodes);
        }

        mod.jmut.core.Config.out("States",
                "actual_" + NUMGENERATEDSTATES +
                ",required_" + noStates +
                ",method_" + method);
        // print and save statistics about how many states were generated and which method was used
        mod.jmut.core.Config.out("generated", NUMGENERATEDSTATES + " states until I got " + noStates + " states");


        // After required number of initial states is generated, save them in boolean[][]
        boolean[][] _states = mod.jmut.core.Util.convertInitialStatesToBoolean(ExaminingStates, numNodes);
        String stateSet = numNodes + "Nodes_" + noStates + " states";
        mod.jmut.core.Calc.states.put(stateSet, new mod.jmut.core.comp.InitialStates(_states));
        return stateSet;
    }


    public static TreeSet<String> generateInitialStates(long NumOfRandomStates, int numNodes) {
        TreeSet<String> ExaminingStates = new TreeSet<>();
        int counter = 0;

        do {
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < numNodes; ++i) {
                sb.append(Math.random() < 0.5D ? "0" : "1");
            }
            counter += 1;

            ExaminingStates.add(sb.toString());
        } while((long)ExaminingStates.size() != NumOfRandomStates);

        NUMGENERATEDSTATES = counter;
        return ExaminingStates;
    }


    static public TreeSet<String> getAllCombinations(int length) {
        int numOptions = (int)Math.pow(2.0D, length);
        TreeSet<String> ExaminingStates = new TreeSet<>();
        for(int o=0; o<numOptions; o++) {
            StringBuilder sb = new StringBuilder();
            for(int l=0; l<length; l++) {
                int val = ( (int)Math.pow(2.0D, l) ) & o;
                sb.append(val > 0 ? "1" : "0");
            }
            ExaminingStates.add(sb.toString());
        }
        return ExaminingStates;
    }
}
