/*
 * Brad Bain
 * Assigmnent 2
 */

package nlp.lm;

import java.io.File;
import java.util.*;

public class LambdaLMModel extends LMBase {

    double lambda;

    /* 
     * Track bigram probabilities
     * E.g. P(b | a) = bigrams[a][b]
     */ 
    //protected HashMap<String, HashMap<String, Double>> bigramProbabilities;

    public LambdaLMModel(String filename, double lambda)  {
        super(filename);

        this.lambda = lambda;
        this.bigramProbabilities = calculateBigramProbablities();
    }
	
	/**
	 * Returns p(second | first)
	 * 
	 * @param first
	 * @param second
	 * @return the probability of the second word given the first word (as a probability)
	 */
	public double getBigramProb(String first, String second) {
        // If the first unigram isn't in the known corpus, treat it as <UNK>
        String firstUnigram = first;
        if(!unigramCounts.containsKey(firstUnigram)) {
            firstUnigram = "<UNK>";
        }

        if(!bigramProbabilities.containsKey(firstUnigram)) { // Edge case: avoid bigrams starting with </s>. Because that's impossible!
            return 0;
        }

        // If the second unigram isn't seen in the known corpus,
        // then treat it as <UNK>
        String secondUnigram = second;
        if(!unigramCounts.containsKey(secondUnigram)) {
            secondUnigram = "<UNK>";
        }

        // If the bigram existis within the corpus, 
        // return the calculated probability of the bigram
        HashMap<String, Double> possibleKnownBigrams = bigramProbabilities.get(firstUnigram);
        if(possibleKnownBigrams != null && possibleKnownBigrams.containsKey(secondUnigram)) {
            return possibleKnownBigrams.get(secondUnigram);
        }

        // Otherwise, calculate the lambda probability of a bigram not seen before.
        // numerator = lambda 
        // denominator = existing denominator (occurrences of bigrams starting with firstUnigram) plus the number of unigram classes
        return lambda / (lambda*unigramCounts.size() + totalBigramsStartingWithUnigram(firstUnigram));
    }

    protected double calculateBigramProb(String first, String second) {
        // numerator = lambda + # given bigram class 
        // denominator = occurrences of bigrams starting with firstWord plus the number of unigram classes
        double bigramCount = bigramCounts.get(first).get(second);

        return (double) (bigramCount + lambda) / (lambda*unigramCounts.size() + totalBigramsStartingWithUnigram(first));
    }

    public static void main(String[] args) {
    	// Arg parsing
    	if(args.length != 4) {
            System.out.println("Invalid number of arguments: <episode corpus> <episode directory> <model = LM | DI> <lambda>");
            System.exit(0);
        }

        String corpusPath = args[0];
        String episodeDirPath = args[1];
        String modelType = args[2];
        double lambda = Double.parseDouble(args[3]);
        
        LMModel lm; 
        if(modelType.equals("LM")) {
        	lm = new LambdaLMModel(corpusPath, lambda);
        } else {
        	lm = new DiscountLMModel(corpusPath, lambda);
        }
        
        File episodeDir = new File(episodeDirPath);
        for(File episode : episodeDir.listFiles()) {
        	Double perplexity = lm.getPerplexity(episode.getPath());
        	System.out.println(String.format("%s: %10.2f", episode.getName(), perplexity));
        }
    }
}