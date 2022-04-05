package bni;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeSet;

/**
 * @author Oto Stanko
 */
public class newInitialStatesGenerator {

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

        TreeSet ExaminingStates = generateInitialStates((long)noStates, numNodes, dirs);
        boolean[][] _states = mod.jmut.core.Util.convertInitialStatesToBoolean(ExaminingStates, numNodes);
        String stateSet = numNodes + "Nodes_" + noStates + " states";
        mod.jmut.core.Calc.states.put(stateSet, new mod.jmut.core.comp.InitialStates(_states));
        return stateSet;
    }


    public static TreeSet generateInitialStates(long NumOfRandomStates, int numNodes, String[] dirs) throws IOException {
        TreeSet ExaminingStates = new TreeSet();
        int counter = 0;

        do {
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < numNodes; ++i) {
                sb.append(Math.random() < 0.5D ? "0" : "1");
            }
            counter += 1;

            ExaminingStates.add(sb.toString());
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
}
