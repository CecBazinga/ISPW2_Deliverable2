package logic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SpreadSubsample;

public class TrainClassifiers {
	
	private static final String RANDOM_FOREST = "RandomForest";
	private static final String NAIVE_BAYES = "NaiveBayes";
	private static final String IBK = "IBk";
	private static final String FEATURE_SELECTION_YES = "Yes";
	private static final String FEATURE_SELECTION_NO = "No";
	private static final String UNDER_SAMPLING = "UnderSampling";
	private static final String OVER_SAMPLING = "OverSampling";
	private static final String SMOTE = "Smote";
	private static  List<WekaDBEntry> wekaDBEntries = new ArrayList<>();
	private static int dataSetDimension;
	
	private TrainClassifiers() {
		
	}

	public static int calculateBuggyClassNumber(Instances training) {
		
		int totInstances = training.size();
		int positiveInstances = 0;
		
		for(int d=0;d<totInstances;d++) {
			if(training.get(d).toString(training.numAttributes()-1).equals("yes")) {
				
				positiveInstances++;
			}
		}
		
		return positiveInstances;
	}
	
	
	public static void registerExceptionOnLog(Exception e, String msg) {
		
		Log.errorLog(msg);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        Log.errorLog(sw.toString());
	}
	
	
	public static WekaDBEntry fillWekaEntry(Instances training , Instances test,String classifierName,
			int numReleaseTraining,String arffName,String fS , Evaluation eval ) {
		
		
		WekaDBEntry wekaEntry = new WekaDBEntry();
		 
		
		int trainingSize = training.size();
		int testSize = test.size();
		int percentageDataTraining = (trainingSize*100)/dataSetDimension;
		
		int positiveInstancesTraining = calculateBuggyClassNumber(training);
		int percentageDefectiveTraining = (positiveInstancesTraining*100)/trainingSize;
		
		int positiveInstancesTest = calculateBuggyClassNumber(test);
		int percentageDefectiveTest = (positiveInstancesTest*100)/testSize;
		
		
	   
	    wekaEntry.setDataSet(arffName);
	    if(eval!=null) {
		    wekaEntry.setNumReleaseTraining(numReleaseTraining-1);
		    wekaEntry.setClassifier(classifierName);
		    wekaEntry.setAuc(eval.areaUnderROC(1));
		    wekaEntry.setKappa(eval.kappa());
		    wekaEntry.setPrecision(eval.precision(1));
		    wekaEntry.setRecall(eval.recall(1));
		    wekaEntry.setBalancing("None");
		    wekaEntry.setPercentageDataTraining(percentageDataTraining);
		    wekaEntry.setPercentageDefectiveTraining(percentageDefectiveTraining);
		    wekaEntry.setPercentageDefectiveTest(percentageDefectiveTest);
		    wekaEntry.settP(eval.numTruePositives(1));
		    wekaEntry.setfP(eval.numFalsePositives(1));
		    wekaEntry.settN(eval.numTrueNegatives(1));
		    wekaEntry.setfN(eval.numFalseNegatives(1));
	    }

	    
	    if(fS.equals(FEATURE_SELECTION_YES)) {
	    	 wekaEntry.setFeatureSelection("BestFirst");
	    }else if(fS.equals(FEATURE_SELECTION_NO)) {
	    	 wekaEntry.setFeatureSelection("None");
	    }
	    
	    return wekaEntry ;
	}
	
	
	
	public static void evaluateClassifier(Instances training , Instances test,String classifierName,
									int numReleaseTraining,String arffName,String fS)    {
		
		Evaluation eval = null;
		AbstractClassifier classifier = null ;
		
		try {
			
			if(classifierName.equals(RANDOM_FOREST)) {
				
				 classifier = new RandomForest();
				 
				
			}else if(classifierName.equals(NAIVE_BAYES)){
				
				 classifier = new NaiveBayes();
				
				
				 
			}else if(classifierName.equals(IBK)) {
				
				 classifier = new IBk();
				 
				
			}else {
				
				Log.errorLog("Inserire una stringa valida come classificatore : NaiveBayes, RandomForest, IBk . \n");
				System.exit(1);
				
			} 
			
			classifier.buildClassifier(training);
			 eval = new Evaluation(test);	
			 eval.evaluateModel(classifier, test);
			 
		}catch(Exception e) {
			
			registerExceptionOnLog( e, "Error creating classifier ! \n" );
	 }
		
		WekaDBEntry wekaEntry = fillWekaEntry(training , test, classifierName, numReleaseTraining, arffName,fS , eval );
	   
	    wekaDBEntries.add(wekaEntry);
		
	}
	
	
	
	
	
	public static double calculatePercentage(int positiveInstances, int totInstances) {
		
		double percentage = 0;
		
		if(positiveInstances > (totInstances-positiveInstances)) {
			
			percentage = (positiveInstances*100)/(double) totInstances;
		}else {
			
			percentage = ((totInstances-positiveInstances)*100)/(double) totInstances;
		}
		
		return percentage;
	}
	
	
	
	
	
	
	public static void applySampling(Instances training , Instances test, String classifier, String balancingMode,String arffName,
										String fS,int numReleaseTraining) {
		
		Resample resample = new Resample();
		FilteredClassifier fc = null ;
		try {
			
			resample.setInputFormat(training);
			
			fc = new FilteredClassifier();
	
			if(classifier.equals(RANDOM_FOREST)) {
				RandomForest classifierForest = new RandomForest();
				fc.setClassifier(classifierForest);
			}else if(classifier.equals(NAIVE_BAYES)){
				NaiveBayes classifierBayes = new NaiveBayes();
				fc.setClassifier(classifierBayes);
			}else if(classifier.equals(IBK)) {
				IBk classifierIBk = new IBk();
				fc.setClassifier(classifierIBk);
			}
			
			if(balancingMode.equals(UNDER_SAMPLING)) {
				SpreadSubsample  spreadSubsample = new SpreadSubsample();
				String[] opts = new String[]{ "-M", "1.0"};
				
				spreadSubsample.setOptions(opts);
				
				fc.setFilter(spreadSubsample);
			}else if(balancingMode.equals(SMOTE)){
				weka.filters.supervised.instance.SMOTE smote = new weka.filters.supervised.instance.SMOTE();
				
				smote.setInputFormat(training);
				
				fc.setFilter(smote);
			}else if(balancingMode.equals(OVER_SAMPLING)){
				
				int totInstances = training.size();
				int positiveInstances = calculateBuggyClassNumber(training);
				
				
				double percentage =  calculatePercentage(positiveInstances, totInstances);
				
				String[] optsOverSampling = new String[]{"-B", "1.0", "-Z", String.valueOf(2*percentage)};
				
				resample.setOptions(optsOverSampling);
				
				fc.setFilter(resample);
			}
	
		
			fc.buildClassifier(training);
			Evaluation eval = new Evaluation(test);
			eval.evaluateModel(fc, test); //sampled
		
		
			WekaDBEntry wekaEntry = fillWekaEntry(training , test, classifier, numReleaseTraining, arffName,fS , eval );
			   
		    wekaDBEntries.add(wekaEntry);
		    
		} catch (Exception e) {
			
			registerExceptionOnLog( e, "Error while sampling ! \n" );
			
		}
	}
	
	
	
	
	public static void createCsvClassifiersMetrics(String projName,String csvName,List<WekaDBEntry> wekaDBEntries) {
		
		String outname =  projName + csvName;
	    
		 try(FileWriter fileWriter = new FileWriter(outname);) {
	            
	            fileWriter.append("Dataset , TrainingReleases , %DataTraining , %DefectiveTraining , %DefectiveTesting ,"
	            		+ " Classifier , Balancing , FeatureSelection , TP , FP , TN , FN , Precision , Recall  , AUC , Kappa ");
	            fileWriter.append("\n");
	            
	            for (WekaDBEntry e : wekaDBEntries) {
	               
	            
	               
	               fileWriter.append(e.getDataSet());
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getNumReleaseTraining()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getPercentageDataTraining()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getPercentageDefectiveTraining()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getPercentageDefectiveTest()));
	               fileWriter.append(",");
	               fileWriter.append(e.getClassifier());
	               fileWriter.append(",");
	               fileWriter.append(e.getBalancing());
	               fileWriter.append(",");
	               fileWriter.append(e.getFeatureSelection());
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.gettP()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getfP()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.gettN()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getfN()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getPrecision()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getRecall()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getAuc()));
	               fileWriter.append(",");
	               fileWriter.append(String.valueOf(e.getKappa()));
	               fileWriter.append("\n");
	               

	            }
	            
	            Log.infoLog("Scrittura del file delle metriche dei classificatori completata con successo \n");
	            Log.infoLog("E' stato creato con successo il relativo file csv : " + outname + "\n");
	               
	         } catch (Exception e) {

	        	 registerExceptionOnLog( e, "Error in translating data in csv form \n" );
	        	 
	         }
	}
	
	
	
	
	public static void train(String projName,int releasesSize) throws IOException    {
		
		Log.infoLog("Trasformazione del dataset da csv in arff... \n");
		
		String csvname =  projName + "Bugginess.csv";
		String arffName =  projName + "Bugginess.arff";
		String csvNotFiltered = "ClassifiersMetrics.csv" ;
		
		
		//laod csv file
		CSVLoader loader = new CSVLoader();
	    loader.setSource(new File(csvname));
	    Instances data = loader.getDataSet();

	    //convert csv to arff
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(data);
	    saver.setFile(new File(arffName));
	    saver.writeBatch();
	
	    Log.infoLog("File arff creato con successo ! \n");
	    
	   wekaDBEntries.clear();
	  
	    
	    //WalkForrward implementation
	    DataSource source1;
	    Instances dataSet = null;
	    
		try {
			source1 = new DataSource(arffName);
			dataSet = source1.getDataSet();
			
		}catch (Exception e) {
			
			 registerExceptionOnLog( e, "Error in csv writer \n" );
     
         }
	    
	    dataSetDimension = dataSet.size();
	  
	  
	    for(int j=2; j <=releasesSize; j++) {
		   
		   Instances training = new Instances(dataSet,0);
		   Instances test = new Instances(dataSet,0);
		   
		   for(int i=0; i<dataSet.size();i++) {
			   
			   int release = Integer.parseInt(dataSet.get(i).toString(0));
			   if(release<j) {
				   
				   training.add(dataSet.get(i));
				   
			   }else if(release == j) {
				   
				   test.add(dataSet.get(i));
			   }
		   }
		   
		  /*  Addestro i solutori senza applicare alcuna tecninca di feature selection
		   *  e senza alcuna tecnica di balancing
		   */
		   
		   int numAttr = training.numAttributes();
		   training.setClassIndex(numAttr - 1);
		   test.setClassIndex(numAttr - 1);
		   int numAttrTrainingNoFilter = training.numAttributes();
		   
	
		   //Naive Bayes training and test without feature selection or balancing
		   evaluateClassifier(training, test, NAIVE_BAYES, j, arffName,FEATURE_SELECTION_NO);
		   
		   
		   //Random Forest training and test without feature selection
		   evaluateClassifier( training, test, RANDOM_FOREST, j, arffName,FEATURE_SELECTION_NO); 
		   
		  
		   //IBk training and test without feature selection
		   evaluateClassifier( training, test, IBK, j, arffName,FEATURE_SELECTION_NO); 
		   
		   
		   
		 /*  Addestro i solutori senza applicare alcuna tecninca di feature selection
		  *  ma applicando le varie tecniche di balancing
		  */  
		   
		   //Naive Bayes with undersampling
		   applySampling(training , test, NAIVE_BAYES, UNDER_SAMPLING, arffName, FEATURE_SELECTION_NO, j);
		   
		   
		   //Random Forest with undersampling
		   applySampling(training , test, RANDOM_FOREST, UNDER_SAMPLING, arffName, FEATURE_SELECTION_NO, j); 
		   
		  
		   //IBk with undersampling
		   applySampling(training , test, IBK, UNDER_SAMPLING, arffName, FEATURE_SELECTION_NO, j);
		   
		   
		   
		   //Naive Bayes with oversampling
		   applySampling(training , test, NAIVE_BAYES, OVER_SAMPLING, arffName, FEATURE_SELECTION_NO, j);
		   
		   
		   //Random Forest with oversampling
		   applySampling(training , test, RANDOM_FOREST, OVER_SAMPLING, arffName, FEATURE_SELECTION_NO, j); 
		   
		  
		   //IBk with oversampling
		   applySampling(training , test, IBK, OVER_SAMPLING, arffName, FEATURE_SELECTION_NO, j);
		   
		   
		   
		   //Naive Bayes with SMOTE
		   applySampling(training , test, NAIVE_BAYES, SMOTE, arffName, FEATURE_SELECTION_NO, j);
		   
		   
		   //Random Forest with SMOTE
		   applySampling(training , test, RANDOM_FOREST, SMOTE, arffName, FEATURE_SELECTION_NO, j); 
		   
		  
		   //IBk with SMOTE
		   applySampling(training , test, IBK, SMOTE, arffName, FEATURE_SELECTION_NO, j);
		   
		   
		   
		   
	
		   
		   /* Addestro i solutori applicando come tecnica di feature selection best first
		    */
		   try {
			   
		   
		    //create AttributeSelection object
			AttributeSelection filter = new AttributeSelection();
			//create evaluator and search algorithm objects
			CfsSubsetEval eval = new CfsSubsetEval();
			GreedyStepwise search = new GreedyStepwise();
			//set the algorithm to search backward
			search.setSearchBackwards(true);
			//set the filter to use the evaluator and search algorithm
			filter.setEvaluator(eval);
			filter.setSearch(search);
			//specify the dataset
			Instances filteredTraining = null;
			Instances testingFiltered = null;
			
			filter.setInputFormat(training);
			//apply
			filteredTraining = Filter.useFilter(training, filter);
			testingFiltered = Filter.useFilter(test, filter);
			

	    
			int numAttrFiltered = filteredTraining.numAttributes();
			filteredTraining.setClassIndex(numAttrFiltered - 1);
			testingFiltered.setClassIndex(numAttrFiltered - 1);
			
			
		   /*  Addestro i solutori senza applicando best first ma
			*  senza alcuna tecnica di balancing
			*/
			
		    //Naive Bayes training and test with feature selection
			evaluateClassifier(filteredTraining, testingFiltered, NAIVE_BAYES, j, arffName,FEATURE_SELECTION_YES);
		   
		    //Random Forest training and test with feature selection
			evaluateClassifier(filteredTraining, testingFiltered, RANDOM_FOREST, j, arffName,FEATURE_SELECTION_YES);
		  
		    //IBk training and test with feature selection
			evaluateClassifier(filteredTraining, testingFiltered, IBK, j, arffName,FEATURE_SELECTION_YES);
			
			
			
		   /*  Addestro i solutori applicando feature selection
			*  e le varie tecniche di balancing
			*/  
			   
		   //Naive Bayes with undersampling
		   applySampling(filteredTraining, testingFiltered, NAIVE_BAYES, UNDER_SAMPLING, arffName, FEATURE_SELECTION_YES, j);
		   
		   
		   //Random Forest with undersampling
		   applySampling(filteredTraining, testingFiltered, RANDOM_FOREST, UNDER_SAMPLING, arffName, FEATURE_SELECTION_YES, j); 
		   
		  
		   //IBk with undersampling
		   applySampling(filteredTraining, testingFiltered, IBK, UNDER_SAMPLING, arffName, FEATURE_SELECTION_YES, j);
		   
		   
		   
		   //Naive Bayes with oversampling
		   applySampling(filteredTraining, testingFiltered, NAIVE_BAYES, OVER_SAMPLING, arffName, FEATURE_SELECTION_YES, j);
		   
		   
		   //Random Forest with oversampling
		   applySampling(filteredTraining, testingFiltered, RANDOM_FOREST, OVER_SAMPLING, arffName, FEATURE_SELECTION_YES, j); 
		   
		  
		   //IBk with oversampling
		   applySampling(filteredTraining, testingFiltered, IBK, OVER_SAMPLING, arffName, FEATURE_SELECTION_YES, j);
		   
		   
		   
		   //Naive Bayes with SMOTE
		   applySampling(filteredTraining, testingFiltered, NAIVE_BAYES, SMOTE, arffName, FEATURE_SELECTION_YES, j);
		   
		   
		   //Random Forest with SMOTE
		   applySampling(filteredTraining, testingFiltered, RANDOM_FOREST, SMOTE, arffName, FEATURE_SELECTION_YES, j); 
		   
		  
		   //IBk with SMOTE
		   applySampling(filteredTraining, testingFiltered, IBK, SMOTE, arffName, FEATURE_SELECTION_YES, j);
			
		   
		   
		    
		    Log.infoLog("NUmero Attributi senza feature selection :" + numAttrTrainingNoFilter + "\n");
		    Log.infoLog("NUmero Attributi con feature selection :" + numAttrFiltered + "\n");
		    for(int v=0;v<numAttrFiltered;v++) {
		    	if(v!=1) {
		    		 Log.infoLog( filteredTraining.attribute(v).toString() + "\n");
		    	}
		    }
		    
		   }catch (Exception e) {

			   registerExceptionOnLog( e, "Error in trainig the damned classifiers \n" );
		        
	         }  
		    
	    }
	    
	    
	    createCsvClassifiersMetrics(projName, csvNotFiltered, wekaDBEntries);
	    

	}
}
