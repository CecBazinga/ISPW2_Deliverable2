package entity;

public class WekaDBEntry {
	
	public String getDataSet() {
		return dataSet;
	}
	public void setDataSet(String dataSet) {
		this.dataSet = dataSet;
	}

	public int getNumReleaseTraining() {
		return numReleaseTraining;
	}

	public void setNumReleaseTraining(int numReleaseTraining) {
		this.numReleaseTraining = numReleaseTraining;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getAuc() {
		return auc;
	}

	public void setAuc(double auc) {
		this.auc = auc;
	}

	public double getKappa() {
		return kappa;
	}

	public void setKappa(double kappa) {
		this.kappa = kappa;
	}

	public String getBalancing() {
		return balancing;
	}
	public void setBalancing(String balancing) {
		this.balancing = balancing;
	}

	public String getFeatureSelection() {
		return featureSelection;
	}
	public void setFeatureSelection(String featureSelection) {
		this.featureSelection = featureSelection;
	}

	public int getPercentageDataTraining() {
		return percentageDataTraining;
	}
	public void setPercentageDataTraining(int percentageDataTraining) {
		this.percentageDataTraining = percentageDataTraining;
	}

	public int getPercentageDefectiveTraining() {
		return percentageDefectiveTraining;
	}
	public void setPercentageDefectiveTraining(int percentageDefectiveTraining) {
		this.percentageDefectiveTraining = percentageDefectiveTraining;
	}

	public int getPercentageDefectiveTest() {
		return percentageDefectiveTest;
	}
	public void setPercentageDefectiveTest(int percentageDefectiveTest) {
		this.percentageDefectiveTest = percentageDefectiveTest;
	}

	public double gettP() {
		return tP;
	}
	public void settP(double tP) {
		this.tP = tP;
	}

	public double getfP() {
		return fP;
	}
	public void setfP(double fP) {
		this.fP = fP;
	}

	public double gettN() {
		return tN;
	}
	public void settN(double tN) {
		this.tN = tN;
	}

	public double getfN() {
		return fN;
	}
	public void setfN(double fN) {
		this.fN = fN;
	}

	private String dataSet;
	private int numReleaseTraining;
	private int percentageDataTraining;
	private int percentageDefectiveTraining;
	private int percentageDefectiveTest;
	private String classifier;
	private String balancing;
	private String featureSelection;
	private double tP;
	private double fP;
	private double tN;
	private double fN;
	private double precision;
	private double recall;
	private double auc;
	private double kappa;
	
	
}
