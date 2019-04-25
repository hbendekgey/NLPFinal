/*
 * Brad Bain
 * Assigmnent 2
 */

package nlp.lm;

import java.util.*;

public class DiscountLMModel extends LMBase {

    double discount;

    public DiscountLMModel(String filename, double discount)  {
        super(filename);

        this.discount = discount;
        this.bigramProbabilities = calculateBigramProbablities();
    }

    protected double calculateBigramProb(String first, String second) {
        double bigramCount = bigramCounts.get(first).get(second);
        double totalBigramsOfType = totalBigramsStartingWithUnigram(first);

        return (double) (bigramCount - discount) / (totalBigramsOfType);
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

        // Otherwise, calculate alpha and apply it
        double totalbigramClassesForType = possibleKnownBigrams.size();
        double reservedMass = (totalbigramClassesForType * discount) / totalBigramsStartingWithUnigram(firstUnigram);
        double totalSumProbUnigramsInExistingBigram = possibleKnownBigrams.keySet().stream().mapToDouble(x -> this.unigramProbabilities.get(x)).reduce(0, (x,y) -> x+y);
        double alpha = reservedMass / (1 - totalSumProbUnigramsInExistingBigram);
        return alpha * unigramProbabilities.get(secondUnigram);
    }
}