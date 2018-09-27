import java.io.FileReader;

/**
* The SourceModel program implements an application that reads
* corpus files and creates a transition matrix which represents
* a Markov chain for the probability of two letters in sequence.
* The probability models are then used to determine the most likely
* language for a user provided text string based on the corpus files.
*
* @author  Sumit Choudhury
* @version 10.0.02
* @since   2018-09-16
*/
public class SourceModel {

    private String smName;
    private String fileName;
    private int[][] charCounts;
    private double[][] probabilities;

    /**
    *This is the constructor which reads the corpus file and creates
    *the transition matrix which represents a Markov chain for the
    *the probability of two letters in sequence using a given corpus file.
    *
    *@param smName name of language of given corpus file
    *@param fileName name of the corpus file
    *@exception Exception needed for file reading
    */
    public SourceModel(String smName, String fileName) throws Exception {

        this.smName = smName;
        this.fileName = fileName;
        FileReader inStream = new FileReader(fileName);
        charCounts = new int[26][26];
        System.out.print("Training " + smName + " model ... ");
        char temp;
        char temp2 = '\0';
        int totalChars = 0;
        while ((temp = (char) inStream.read()) != (char) 65535) {
            temp = Character.toLowerCase(temp);
            if (temp2 != '\0' && Character.isLetter(temp)
                              && Character.isLetter(temp2)) {
                for (int i = 0; i < charCounts.length; i++) {
                    for (int j = 0; j < charCounts[i].length; j++) {
                        if (i == ((int) temp2 - 97) && j == ((int) temp - 97)) {
                            charCounts[i][j]++;
                            break;
                        }
                    }
                }
            }
            if (Character.isLetter(temp)) {
                temp2 = temp;
            }
        }
        inStream.close();
        probabilities = new double[26][26];
        double rowSum = 0;
        int index = 0;
        for (int[] row: charCounts) {
            for (int i = 0; i < row.length; i++) {
                rowSum += (double) row[i];
            }
            for (int i = 0; i < probabilities[index].length; i++) {
                double probab = (double) charCounts[index][i] / rowSum;
                if (rowSum == 0) {
                    probabilities[index][i] = 0.01;
                } else if (probab != 0.0) {
                    probabilities[index][i] = probab;
                } else {
                    probabilities[index][i] = 0.01;
                }
            }
            index++;
            rowSum = 0;
        }
        System.out.println("done");
    }

    /**
    *
    *@return name of language of given corpus file
    */
    public String getName() {
        return smName;
    }

    /**
    *creates a String representation of the transition matrix
    *
    *@return a String for the transition matrix
    */

    public String toString() {
        String matrix = " ";
        char temp = 'a';
        while (temp <= 'z') {
            matrix += ("    " + temp);
            temp++;
        }
        matrix += "\n";
        temp = 'a';
        for (double[] arr: probabilities) {
            matrix += (temp + " ");
            for (double prob: arr) {
                matrix += (String.format("%.2f", prob) + " ");
            }
            matrix += "\n";
            temp++;
        }
        return matrix;
    }

    /**
    *Probability method takes a sting and calcultaes the probaility that
    *it matches with a certain source model.
    *
    *@param test String that is being tested to math a given source model.
    *@return probability that test String was generated using the given
    *source model
    */
    public Double probability(String test) {
        double probability = 1.0;
        for (int c = 0; c < test.length() - 1; c++) {
            char a = Character.toLowerCase(test.charAt(c));
            char b = Character.toLowerCase(test.charAt(c + 1));
            if (Character.isLetter(a) && Character.isLetter(b)) {
                probability = probability
                              * probabilities[(int) a - 97][(int) b - 97];
            } else if (Character.isLetter(a)) {
                boolean error = false;
                int i = 2;
                do {
                    try {
                        b = Character.toLowerCase(test.charAt(c + i));
                        probability = probability
                                    * probabilities[(int) a - 97][(int) b - 97];
                    } catch (StringIndexOutOfBoundsException ex) {
                        error = true;
                        i++;
                        if ((c + i) > test.length()) {
                            break;
                        }
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        break;
                    }
                } while (error);
            }
        }
        return probability;
    }

    /**
    *Main method that creates a SourceModel object for all given corpus files
    *and then takes finds the probility that a given text string matches a
    *corpus file and finaly displays with corpus the test strin matches most.
    *
    *@param args varargs which contains all corpus files and test string
    *@exception Exception needed for file reading
    */
    public static void main(String ... args) throws Exception {
        SourceModel[] sm = new SourceModel[args.length - 1];
        String text = args[args.length - 1];
        double[] probs = new double[args.length - 1];
        double maxProb = 0.0;
        String likely = "";
        double totalProb = 0.0;
        for (int i = 0; i < sm.length; i++) {
            sm[i] = new SourceModel(args[i].substring(0, args[i].indexOf("."))
                                    , args[i]);
            probs[i] = sm[i].probability(text);
        }
        for (double p: probs) {
            totalProb += p;
        }
        System.out.println("Analyzing: " + text);
        int i = 0;
        for (SourceModel model: sm) {
            double prob = probs[i] / totalProb;
            System.out.printf("Probability that test sring is %8s: %.2f%n",
                                model.getName(), prob);
            if (prob > maxProb) {
                likely = model.getName();
                maxProb = prob;
            }
            i++;
        }
        System.out.printf("Test string is most likely %s.%n", likely);
    }
}