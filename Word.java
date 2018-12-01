// URL: https://github.com/kinejohnsrud/naive-bayesian-spam-filter/tree/master/spam-filter/src/bayes

import java.math.*;

public class Word {
	private String word;	// the word itself
	private int spamCount;	// number of this words appearances in spam messages
	private int hamCount;	// number of this words appearances in ham messages
	private float probWordGivenSpam;	// log(P(word[i]|spam)) 
	private float probWordGivenHam;	// log(P(word[i]|ham))
	
	public Word(String word) {
		this.word = word;
		spamCount = 0;
		hamCount = 0;
		probWordGivenSpam = 0.0f;
		probWordGivenHam = 0.0f;
	}
	
	public void countSpam() {
		spamCount++;
	}
	
	public void countHam() {
		hamCount++;
	}
	
	// calculates the probability of spam, using Laplace smoothing
	public void calculateWordSpamProbability(int totSpam, int numOfDistinctWords) {
		probWordGivenSpam = (float) Math.log((spamCount + 1)/(totSpam + numOfDistinctWords));
	}
	
	// calculates the probability of spam, using Laplace smoothing
	public void calculateWordHamProbability(int totHam, int numOfDistinctWords) {
		probWordGivenHam = (float) Math.log((hamCount + 1)/(totHam + numOfDistinctWords));
	}

	public String getWord() {
		return word;
	}

	public float getProbWordGivenSpam() {
		return probWordGivenSpam;
	}

	public float getProbWordGivenHam() {
		return probWordGivenHam;
	}
	
	public void setProbWordGivenSpam(float probWordGivenSpam) {
		this.probWordGivenSpam = probWordGivenSpam;
	}
	
	public void setProbWordGivenHam(float probWordGivenHam) {
		this.probWordGivenHam = probWordGivenHam;
	}
}
