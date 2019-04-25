/*
 * Brad Bain
 * Assigmnent 2
 */

package nlp.lm;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;

public abstract class LMBase implements LMModel {

    /* Track total unigrams */
    private int totalUnigrams;

    /*
     * Track unigram counts
     * E.g. C(a) = unigramCount[a]
     */
    protected HashMap<String, Integer> unigramCounts;

    /*
     * Track unigram probabilities
     * E.g. C(a) = unigramCount[a]
     */
    protected HashMap<String, Double> unigramProbabilities;

    /*
     * Track bigram counts
     * E.g. C(ab) = bigramCount[a][b]
     */
    protected HashMap<String, HashMap<String, Integer>> bigramCounts;

    /* 
     * Track bigram probabilities
     * E.g. P(b | a) = bigrams[a][b]
     */ 
    protected HashMap<String, HashMap<String, Double>> bigramProbabilities;

    public LMBase(String filename) {
        this.unigramCounts = calculateUnigramCounts(filename);
        this.bigramCounts = calculateBigramCounts(filename);
        this.unigramProbabilities = calculateUnigramProbabilities();
    }


    private HashMap<String, Integer> numberOfBigramsStartingWithUnigram = new HashMap<String, Integer>();

    /**
     * Utility function for counting how many bigrams start with the given unigram,
     * with built in cacheing to avoid having to recaulcuate the number for the 
     * same unigram in the future
     * 
     * @param first - the unigram to count how many occurrences of a bigram it starts
     * @return result - the number of bigrams starting with the given unigram
     */
    protected int totalBigramsStartingWithUnigram(String first) {
        Integer result = numberOfBigramsStartingWithUnigram.get(first);
        if(result == null) {
            HashMap<String, Integer> knownBigrams = this.bigramCounts.get(first);
            if(knownBigrams == null) {
                return 0;
            }

            result = this.bigramCounts.get(first).values().stream().reduce(0, (x,y) -> x+y);
            numberOfBigramsStartingWithUnigram.put(first, result);
        }

        return result;
    }

    /**
     * Utility function for tokenizing a given sentence according to the following
     * criteria: - Each line is treated as a sentence - <s> and </s> are added to
     * the beginning and end of a sentence - tokens are split on whitespace
     * 
     * @param sentence - the string to tokenize
     * @returns String[] result - the tokenized sentence
     */
    private String[] tokenize(String sentence) {
        // Add <s> and </s> to the end of each sentence
        sentence = MessageFormat.format("<s> {0} </s>", sentence);
        
        // Split words/tokens on whitespace
        String[] tokens = sentence.split("\\s|\\t|,|;|\\.|\\?|!|-|:|@|\\[|\\]|\\(|\\)|\\{|\\}|_|\\*|/");

        return tokens;
    }

    /**
     * For the given file name, read and store each unigram occurrence inside a one-layer HashMap,
     * also accounting for <s> and </s> symbols and replacing the first occurrence
     * of each unigram with the <UNK> symbol.
     * 
     * @param filename - The file to calculate unigramCounts from 
     * @return HashMap<String, Integer> unigramCounts - Counts of existing unigrams, keyed by unigram. Each value is guaranteed to be > 0.
     */
    private HashMap<String, Integer> calculateUnigramCounts(String filename) {
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            HashMap<String, Integer> unigramCounts = new HashMap<String, Integer>();

            // Each line is treated as a sentence
            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] words = tokenize(line);
                for (String w : words) {

                    // Increase unigram count if it already exists
                    if(unigramCounts.containsKey(w)) {
                        unigramCounts.put(w, unigramCounts.get(w) + 1);
                    }
                    
                    // If it doesn't, and it's a special start/end token, also add it
                    else if(w.equals("<s>") || w.equals("</s>")) {
                        unigramCounts.put(w, 1);
                    }
                    
                    // Otherwise, we mark we've seen this word but also replace first occurrence with <UNK>
                    else {
                        unigramCounts.put(w, 0);
                        unigramCounts.put("<UNK>", unigramCounts.getOrDefault("<UNK>", 0) + 1);
                    }

                    this.totalUnigrams++;
                }
            }
            // When we're done, we remove all occurrences with 0 counts (these were replaced with <UNK>)
            unigramCounts.values().removeAll(Collections.singleton(0));
            return unigramCounts;
        }
        catch(FileNotFoundException e) {
            System.out.println("File not found - " + e.getMessage());
            return null;
        }
        catch(IOException e) {
            System.out.println("Unable to read file - " + e.getMessage());
            return null;
        }
    }

    /**
     * For the given file name, read and store each bigram occurrence inside a two-layer HashMap,
     * also accounting for <s> and </s> symbols and replacing the first occurrence
     * of each unigram with the <UNK> symbol.
     * 
     * @param filename
     * @return HashMap<String, HashMap<String, Integer>> bigramCounts - a two-layer map of bigram counts
     */
    private HashMap<String, HashMap<String, Integer>> calculateBigramCounts(String filename) {
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {

            HashMap<String, HashMap<String, Integer>> bigramCounts = new HashMap<String, HashMap<String, Integer>>();
            HashSet<String> known = new HashSet<String>(Arrays.asList("<s>", "</s>")); // tracks which words we've seen in previous sentences

            // Each line is treated as a sentence
            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] words = tokenize(line);
                for (int i = 0; i < words.length - 1; i++) {
                    String first  = words[i];
                    String second = words[i+1];

                    // Determine whether the first word is known to start an existing bigram
                    HashMap<String, Integer> possibleKnownBigrams = bigramCounts.get(first);

                    // If there are no occurrences of this word at the start of a bigram, 
                    // init empty child HashMap or treat as <UNK>, depending on if we've seen this word before
                    if(possibleKnownBigrams == null) {
                        // If we've seen this word before (e.g. last word in sentence / not as start of bigram),
                        // then we can add it to the bigram list, as we don't treat it as <UNK>
                        if(known.contains(first)) {
                            bigramCounts.put(first, new HashMap<String, Integer>());
                        }
                        // Otherwise, add it to known for the future and treat it as <UNK> for now 
                        else {
                            known.add(first);

                            first = "<UNK>";
                            words[i] = "<UNK>";
                            bigramCounts.put(first, new HashMap<String, Integer>());
                        }

                        possibleKnownBigrams = bigramCounts.get(first);
                        assert(possibleKnownBigrams != null);
                    }
                
                    // Increase bigram count if it already exists
                    if(possibleKnownBigrams.containsKey(second)) {
                        bigramCounts.get(first).put(second, bigramCounts.get(first).get(second) + 1);
                    }
                    // If it doesn't exist following this bigram, but it has been seen >=1 times as a unigram,
                    // track it's occurrence
                    else if(known.contains(second)) {
                        bigramCounts.get(first).put(second, 1);
                    }
                    // Otherwise, we mark we've seen this word but also treat it as <UNK>
                    else {
                        known.add(second);

                        second = "<UNK>";
                        words[i+1] = "<UNK>";
                        bigramCounts.get(first).put("<UNK>", bigramCounts.get(first).getOrDefault("<UNK>", 0) + 1);
                    }
                }
            }
            return bigramCounts;
        }
        catch(FileNotFoundException e) {
            System.out.println("File not found - " + e.getMessage());
            return null;
        }
        catch(IOException e) {
            System.out.println("Unable to read file - " + e.getMessage());
            return null;
        }
    }

    /**
     * Read and store the probability of each unigram occurrence inside a one-layer HashMap,.
     * 
     * @param filename
     * @return HashMap<String, Double> unigramProbabilities - a one-layer map of unigram probabilities
     */
    private HashMap<String, Double> calculateUnigramProbabilities() {
        HashMap<String, Double> unigramProbabilities = new HashMap<String, Double>();
        for(String unigram : unigramCounts.keySet()) {
            unigramProbabilities.put(unigram, (double) unigramCounts.get(unigram) / totalUnigrams);
        }
        return unigramProbabilities;
    }

    /**
     * Read and store the probability of each bigram occurrence inside a two-layer HashMap,.
     * 
     * @param filename
     * @return HashMap<String, HashMap<String, Double>> bigramProbabilities - a two-layer map of bigram probabilities
     */
    protected HashMap<String, HashMap<String, Double>> calculateBigramProbablities() {
        HashMap<String, HashMap<String, Double>> bigramProbs = new HashMap<String, HashMap<String, Double>>();

        for (String firstWord : bigramCounts.keySet()) {
            for (String secondWord : bigramCounts.get(firstWord).keySet()) {
                double bigramProbability = calculateBigramProb(firstWord, secondWord); 

                if(bigramProbs.get(firstWord) == null) {
                    bigramProbs.put(firstWord, new HashMap<String, Double>());
                }
                bigramProbs.get(firstWord).put(secondWord, bigramProbability);
                
            }
        }

        return bigramProbs;
    }


	/**
     * Given a sentence, return the log of the probability of the sentence based on
     * the LM.
     * 
     * @param sentWords the words in the sentence. sentWords should NOT contain <s>
     *                  or </s>.
     * @return the log probability
     */
	public double logProb(ArrayList<String> sentWords) {
        ArrayList<String> sentence = new ArrayList<String>(sentWords);

        sentence.add(0, "<s>");
        sentence.add("</s>");

        double logProb = 0;
        for(int i = 0; i < sentence.size() - 1; i++) {
            String first = sentence.get(i);
            String second = sentence.get(i+1);

            logProb += Math.log10(getBigramProb(first, second));
        }

        return logProb;
    }
	
	/**
	 * Given a text file, calculate the perplexity of the text file, that is the negative average per word log
	 * probability
	 * 
	 * @param filename a text file.  The file will contain sentences WITHOUT <s> or </s>.
	 * @return the perplexity of the text in file based on the LM
	 */
	public double getPerplexity(String filename) {
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            double totalPerplexitySum = 0;
            double totalSentences = 0;

            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] sentence = line.split("\\s"); // sentWords should NOT contains <s> or </s>
                ArrayList<String> sentWords = new ArrayList<String>(Arrays.asList(sentence));
                
                double logProb = logProb(sentWords);
                totalPerplexitySum += Math.pow(10, -logProb/(sentWords.size() + 1));
                totalSentences++;
            }
            return totalPerplexitySum / totalSentences;
        }
        catch(FileNotFoundException e) {
            System.out.println("File not found - " + e.getMessage());
            return -1;
        }
        catch(IOException e) {
            System.out.println("Unable to read file - " + e.getMessage());
            return -1;
        }
    }

	/**
	 * Returns p(second | first), previously calculated. If either first or second is not
     * in the existing vocabulary, they are treated as <UNK>.
	 * 
	 * @param first
	 * @param second
	 * @return the probability of the second word given the first word (as a probability)
	 */
    abstract public double getBigramProb(String first, String second);
    
    /**
     * Calculates p(second | first) in addition to any model-specifc smoothing.
     * Precondition: first and second are previously seen unigrams.
     * 
     * @param first - the first word in the bigram
     * @param second - the second word in the bigram
     * @return - double representing
     */
    abstract protected double calculateBigramProb(String first, String second);
}