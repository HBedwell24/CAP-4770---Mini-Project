// URL: https://github.com/kinejohnsrud/naive-bayesian-spam-filter/tree/master/spam-filter/src/bayes

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NaiveBayes {
	HashMap<String, Word> words = new HashMap<String, Word>();
	BufferedWriter out;

	// NaiveBayes Constructor
	public NaiveBayes() {
		
	}
	
	public PrintWriter transformTrainData(String path) throws IOException {
		final ZipFile zipFile = new ZipFile(path);
		PrintWriter printWriter = new PrintWriter("train.txt");
		final Enumeration<? extends ZipEntry> entries = zipFile.entries();
	    while (entries.hasMoreElements()) {
	        final ZipEntry entry = entries.nextElement();
	        if (entry.getName().startsWith("spmsg")) {
	        	printWriter.print("spam \t");
	        }
	        else {
	        	printWriter.print("ham \t");
	        }
	        InputStream stream = zipFile.getInputStream(entry);
	        BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
	        String line;
			while ((line = in.readLine()) != null) {
				String replaceSpecial = line.replaceAll("[^A-Za-z0-9 ]", "");
				String replaceWhitespace = replaceSpecial.replaceAll("\\s+", " ");
				printWriter.print(replaceWhitespace);
			}
			printWriter.println(System.lineSeparator());
			in.close();
	    }
	    printWriter.close();
	    zipFile.close();
		return printWriter;
	}
	
	public PrintWriter transformTestData(String path) throws IOException {
		final ZipFile zipFile = new ZipFile(path);
		PrintWriter printWriter = new PrintWriter("test.txt");
		final Enumeration<? extends ZipEntry> entries = zipFile.entries();
	    while (entries.hasMoreElements()) {
	        final ZipEntry entry = entries.nextElement();
	        InputStream stream = zipFile.getInputStream(entry);
	        BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
	        String line;
			while ((line = in.readLine()) != null) {
				String replaceSpecial = line.replaceAll("[^A-Za-z ]", "");
				String replaceWhitespace = replaceSpecial.replaceAll("\\s+", " ");
				printWriter.print(replaceWhitespace);
			}
			printWriter.println(System.lineSeparator());
			in.close();
	    }
	    printWriter.close();
	    zipFile.close();
		return printWriter;
	}

	// uses a train-file to make a hashmap containing all words, and their probability of being spam
	public int train(String path) throws IOException {
		
		int totalSpamCount = 0;
		int totalHamCount = 0;
		
		transformTrainData(path);
		BufferedReader in = new BufferedReader(new FileReader("train.txt"));
		String line = in.readLine();
		while (line != null){
			if (!line.equals("")){
				String type = line.split("\t")[0];
				String sms = line.split("\t")[1];
				
				for (String word : sms.split(" ")) {
					word = word.replaceAll("\\W", "");
					word = word.toLowerCase();
					Word w = null;
					if(words.containsKey(word)) {
						w = (Word) words.get(word);
					}
					else {
						w = new Word(word);
						words.put(word,w);
					}
					if(type.equals("ham")) {
						w.countHam();
						totalHamCount++;
					}
					else if(type.equals("spam")) {
						w.countSpam();
						totalSpamCount++;
					}		
				}
			}	
			line = in.readLine();	
		}
		in.close();
		for (String key : words.keySet()) {
			words.get(key).calculateProbability(totalSpamCount, totalHamCount);
		}
		return totalSpamCount;
	}
	
	// takes the text to be analyzed as input, and produces predictions by form of 'spam' or 'ham'
	public int filter(String path) throws IOException {
		transformTestData(path);
		int spamCount = 0;
		BufferedReader in = new BufferedReader(new FileReader("train.txt"));
		PrintWriter printWriter = new PrintWriter("predictions.txt");
		String line;
		while ((line = in.readLine()) != null) {
			if (!line.equals("")) {
				ArrayList<Word> sms = makeWordList(line);
				boolean isSpam = calculateBayes(sms);
				if(isSpam == true) {
					printWriter.print("spam");
				}
				else if (isSpam == false) {
					printWriter.print("ham");
				}
			}
			printWriter.println(System.lineSeparator());
			line = in.readLine();
		}
		printWriter.close();
		in.close();
		return spamCount;
	}

	// make an arraylist of all words in an sms, set probability of spam to 0.4 if word is not known
	public ArrayList<Word> makeWordList(String sms) {
		
		ArrayList<Word> wordList = new ArrayList<Word>();
		
		for (String word : sms.split(" ")) {
			word = word.replaceAll("\\W", "");
			word = word.toLowerCase();
			Word w = null;
			if(words.containsKey(word)){
				w = (Word) words.get(word);
			}
			else {
				w = new Word(word);
				w.setProbOfSpam(0.40f);
			}
			wordList.add(w);
		}
		return wordList;
	}
	
	// applying Bayes rule and calculating probability of ham or spam. Return true if spam, false if ham
	public boolean calculateBayes(ArrayList<Word> sms) {
		
		float probabilityOfPositiveProduct = 1.0f;
		float probabilityOfNegativeProduct = 1.0f;
		
		for (int i = 0; i < sms.size(); i++) {
			Word word = (Word) sms.get(i);
			probabilityOfPositiveProduct *= word.getProbOfSpam();
			probabilityOfNegativeProduct *= (1.0f - word.getProbOfSpam());
		}
		
		float probOfSpam = probabilityOfPositiveProduct / (probabilityOfPositiveProduct + probabilityOfNegativeProduct);
		
		if(probOfSpam > 0.9f) {
			return true;
		}
		
		else {
			return false;
		}
	}
	
	public void accuracy(int spamCount, int spamTotal) {
		
		System.out.println(spamCount);
		System.out.println(spamTotal);
		//double accuracy = spamCount/spamTotal;
		//System.out.println("The Naive Bayes algorithm successfully predicted " + accuracy + "% of the spam emails found in the test set!");
	}
}