package com.interaxon.muse.museioreceiver;

import java.io.Serializable;

public class FeatureVector implements Serializable {
	
	private static final long serialVersionUID = -5360587050973021953L;
	
	/**
	 * The raw EEG values<br>
	 * For each of the 4 locations (TP9, FP1, FP2, TP10)<br>
	 * Amount of raw EEG values is equivalent to the window size of the FFT
	 */
	private double [][] _rawEEGValues;
	
	/**
	 * The features obtained by applying FFT on the raw EEG values<br>
	 * In the form of log(theta), log(alpha), log(beta), log(gamma)<br>
	 * For each of the 4 locations (TP9, FP1, FP2, TP10)
	 */
	private double [] _features;
	
	/**
	 * The feature class associated with this feature vector<br>
	 * i.e. Relax/Focus
	 */
	private int _featureClass;
	
	/**
	 * Constructor for a Feature Vector
	 * 
	 * @param rawEEGValues		The raw EEG values
	 * @param features			The features obtained by applying FFT on the raw EEG values
	 * @param featureClass		The feature class associated with this feature vector
	 */
	public FeatureVector(double [][] rawEEGValues, double [] features,
			int featureClass) {
		
		_rawEEGValues = rawEEGValues;
		_features = features;
		_featureClass = featureClass;
	}
	
	/**
	 * Getter function for the raw EEG values used to obtain this feature vector
	 * 
	 * @return The raw EEG values
	 */
	public double [][] getRawEEGValues() {
		return _rawEEGValues;
	}
	
	/**
	 * Getter function for the features
	 * 
	 * @return The features obtained by applying FFT on the raw EEG values
	 */
	public double [] getFeatures() {
		return _features;
	}
	
	/**
	 * Getter function for the feature class
	 * 
	 * @return The feature class associated with this feature vector
	 */
	public int getFeatureClass() {
		return _featureClass;
	}
	
	public void setFeatureClass(int newFeatureClass) {
		_featureClass = newFeatureClass;
	}
	
	/**
	 * Calculates the Euclidean distance between this feature vector
	 * and the other feature vector provided
	 * 
	 * @param otherFv The other feature vector
	 * @return The Euclidean distance between the two feature vectors
	 */
	public double calcEuclideanDist(FeatureVector otherFv) {
		
		double distance = 0.0;
		double [] otherFvFeatures = otherFv.getFeatures();
		
		for(int i = 0; i < 16; i ++) {
			double value = (_features[i] - otherFvFeatures[i]);
			distance += value * value;
		}
		
		distance = Math.sqrt(distance);		
		
		return distance;
	}
	
	/**
	 * Calculates the standardized Euclidean distance between this
	 * feature vector and the other feature vector provided
	 * 
	 * @param otherFv The other feature vector
	 * @param stdDevArr The standard deviation of the training feature vectors
	 * 
	 * @return The standardized Euclidean distance between the two feature vectors
	 */
	public double calcStdEuclideanDist(FeatureVector otherFv,
			double [] stdDevArr) {
		
		double distance = 0.0;
		double [] otherFvFeatures = otherFv.getFeatures();
		
		for(int i = 0; i < 16; i ++) {
			double value = (_features[i] - otherFvFeatures[i]) / stdDevArr[i];
			distance += value * value;
		}
		
		distance = Math.sqrt(distance);		
		
		return distance;
	}
	
	/**
	 * Returns a string representation of this feature vector
	 */
	public String toString() {
		
		String s = "";
		
		if(_rawEEGValues != null) {
			s += "Raw EEG Values: ";
			for(int i = 0; i < _rawEEGValues[0].length; i++) {
				s += String.format("%6.3f, ", _rawEEGValues[0][i]);
				s += String.format("%6.3f, ", _rawEEGValues[1][i]);
				s += String.format("%6.3f, ", _rawEEGValues[2][i]);
				s += String.format("%6.3f", _rawEEGValues[3][i]);
				s += "\r\n";
			}
			s += "\r\n";
		}
		
		s += "Features: ";
		for(double feature : _features) {
			s += String.format("%5.3f, ", feature);
		}
		
		s += "Class Label: " + _featureClass + (_featureClass == 1 ? "(Focus)" : "(Relax)");
		
		return s;
	}
}
