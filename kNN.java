import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class kNN {

	private List<String[]> trainfeatures = new ArrayList<>();
	private List<String> trainlabel = new ArrayList<>();

	private List<String[]> testfeatures = new ArrayList<>();
	private List<String> testlabel = new ArrayList<>();

	Scanner sc = new Scanner(System.in);
	int knn_value = 1;
	int DistanceMetricsSelction = 0;
	int totalNumberOfLabel = 0;
	String file_path;
	 
	public kNN (int k) {
		knn_value = k;	
	}

	void loadtrainData(String loadfilename) throws NumberFormatException, IOException {

		final ZipFile file = new ZipFile(loadfilename);

		try {
			final Enumeration<? extends ZipEntry> entries = file.entries();
		    while (entries.hasMoreElements()) {
		        final ZipEntry entry = entries.nextElement();
		        InputStream stream = file.getInputStream(entry);
		        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		        System.out.println(entry);
			
				String line;
				TrainRecord[] records;
				int index = 0;
				while ((line = br.readLine()) != null) {
					String[] split = line.split(",");
					String[] feature = new String[split.length - 1];
					for (int i = 0; i < split.length - 1; i++)
						feature[i] = split[i];
					trainfeatures.add(feature);
					trainlabel.add(split[feature.length]);
					
					records[index] = new TrainRecord(trainfeatures, trainlabel);
					index ++;
				}
		    }
		    file.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	void loadtestData(String testfilename) throws NumberFormatException, IOException {

		final ZipFile file = new ZipFile(testfilename);

		try {
			final Enumeration<? extends ZipEntry> entries = file.entries();
		    while (entries.hasMoreElements()) {
		        final ZipEntry entry = entries.nextElement();
		        InputStream stream = file.getInputStream(entry);
		        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		        System.out.println(entry);
			
		        PrintWriter pw = new PrintWriter("RealLabel.txt");
			
				String testline;
				while ((testline = br.readLine()) != null) {
	
					String[] split = testline.split(",");
					String[] feature = new String[split.length - 1];
					for (int i = 0; i < split.length - 1; i++)
						feature[i] = split[i];
					testfeatures.add(feature);
					testlabel.add(split[feature.length]);
					// writing original label for test data to file and counting number of label.
					pw.println(split[feature.length]);
					totalNumberOfLabel++;
				}
				pw.close();
		    }
		    file.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	void cosineSimilarity() throws IOException {
		
		try {
			//read trainingSet and testingSet
			TrainRecord[] trainingSet =  FileManager.readTrainFile(path);
			TestRecord[] testingSet =  FileManager.readTestFile(path );
			Metric metric = new CosineSimilarity();
			
			int numOfTestingRecord = testingSet.length;
			for(int i = 0; i < numOfTestingRecord; i ++){
				TrainRecord[] neighbors = findKNearestNeighbors(trainingSet, testingSet[i], K, metric);
				int classLabel = classify(neighbors);
				testingSet[i].predictedLabel = classLabel; //assign the predicted label to TestRecord
			}
			
			int correctPrediction = 0;
			for(int j = 0; j < numOfTestingRecord; j ++){
				if(testingSet[j].predictedLabel == testingSet[j].classLabel)
					correctPrediction ++;
			}
			
			String predictPath = FileManager.outputFile(testingSet, trainingFile);
			System.out.println("The prediction file is stored in "+predictPath);
			System.out.println("The accuracy is "+((double)correctPrediction / numOfTestingRecord)*100+"%");
			
			final long endTime = System.currentTimeMillis();
			System.out.println("Total excution time: "+(endTime - startTime) / (double)1000 +" seconds.");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	static TrainRecord[] findKNearestNeighbors(TrainRecord[] trainingSet, TestRecord testRecord, int K, Metric metric){
		int NumOfTrainingSet = trainingSet.length;
		assert K <= NumOfTrainingSet : "K is lager than the length of trainingSet!";
		
		TrainRecord[] neighbors = new TrainRecord[K];
		
		int index;
		for(index = 0; index < K; index++){
			trainingSet[index].distance = metric.getDistance(trainingSet[index], testRecord);
			neighbors[index] = trainingSet[index];
		}
		
		//go through the remaining records in the trainingSet to find K nearest neighbors
		for(index = K; index < NumOfTrainingSet; index ++){
			trainingSet[index].distance = metric.getDistance(trainingSet[index], testRecord);
			
			//get the index of the neighbor with the largest distance to testRecord
			int maxIndex = 0;
			for(int i = 1; i < K; i ++){
				if(neighbors[i].distance > neighbors[maxIndex].distance)
					maxIndex = i;
			}
			
			//add the current trainingSet[index] into neighbors if applicable
			if(neighbors[maxIndex].distance > trainingSet[index].distance)
				neighbors[maxIndex] = trainingSet[index];
		}
		
		return neighbors;
	}
	
	// Get the class label by using neighbors
	static int classify(TrainRecord[] neighbors){
		//construct a HashMap to store <classLabel, weight>
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		int num = neighbors.length;
		
		for(int index = 0;index < num; index ++){
			TrainRecord temp = neighbors[index];
			int key = temp.classLabel;
		
			//if this classLabel does not exist in the HashMap, put <key, 1/(temp.distance)> into the HashMap
			if(!map.containsKey(key))
				map.put(key, 1 / temp.distance);
			
			//else, update the HashMap by adding the weight associating with that key
			else{
				double value = map.get(key);
				value += 1 / temp.distance;
				map.put(key, value);
			}
		}	
		
		//Find the most likely label
		double maxSimilarity = 0;
		int returnLabel = -1;
		Set<Integer> labelSet = map.keySet();
		Iterator<Integer> it = labelSet.iterator();
		
		//go through the HashMap by using keys 
		//and find the key with the highest weights 
		while(it.hasNext()){
			int label = it.next();
			double value = map.get(label);
			if(value > maxSimilarity){
				maxSimilarity = value;
				returnLabel = label;
			}
		}
		return returnLabel;
	}
	
	static String extractGroupName(String filePath){
		StringBuilder groupName = new StringBuilder();
		for(int i = 15; i < filePath.length(); i ++){
			if(filePath.charAt(i) != '_')
				groupName.append(filePath.charAt(i));
			else
				break;
		}
		return groupName.toString();
	}
}