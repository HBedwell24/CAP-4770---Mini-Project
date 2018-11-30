// URL: https://github.com/kinejohnsrud/naive-bayesian-spam-filter/tree/master/spam-filter/src/bayes
import java.math.*;

public class Word {
	private String word;	// the word itself
	private int spamCount;	// number of this words appearances in spam messages
	private int hamCount;	// number of this words appearances in ham messages
	private float probWordGivenSpam;	// spamCount divided by total spam count
	private float probWordGivenHam;	// hamCount divided by total ham count
	private float probOfSpam;	// probability of word being spam
	
	public Word(String word) {
		this.word = word;
		spamCount = 0;
		hamCount = 0;
		probWordGivenSpam = 0.0f;
		probWordGivenHam = 0.0f;
		probOfSpam = 0.0f;	
	}
	
	public void countSpam() {
		spamCount++;
	}
	
	public void countHam() {
		hamCount++;
	}
	
	// calculates the probability of spam, using Laplace smoothing
	public float calculateWordSpamProbability(int totSpam, int numOfDistinctWords) {
		// log(P(word[i]|spam)) 
		probWordGivenSpam = (float) Math.log((spamCount + 1)/(totSpam + numOfDistinctWords));
		return probWordGivenSpam;
	}
	
	// calculates the probability of spam, using Laplace smoothing
	public float calculateWordHamProbability(int totHam, int numOfDistinctWords) {
		// log(P(word[i]|ham))
		probWordGivenHam = (float) Math.log((hamCount + 1)/(totHam + numOfDistinctWords));
		return probWordGivenHam;
	}

	public String getWord() {
		return word;
	}

	public float getSpamRate() {
		return probWordGivenSpam;
	}

	public float getHamRate() {
		return probWordGivenHam;
	}

	public void setHamRate(float probWordGivenHam) {
		this.probWordGivenHam = probWordGivenHam;
	}

	public float getProbOfSpam() {
		return probOfSpam;
	}

	public void setProbOfSpam(float probOfSpam) {
		this.probOfSpam = probOfSpam;
	}
}
