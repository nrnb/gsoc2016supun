package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadLocalRandom;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;



public class MatrixtSNE implements CyMatrix {
	Random rnd = new Random();
	static DecimalFormat mydecimalFormat = new DecimalFormat("00.###E0");
	private static ForkJoinPool pool = new ForkJoinPool();
	private static String DEFAULT_TITLE  = "Vector";
	public static int noDigits = 4;

	public static String arrToStr(int [] arr, String title) {
		String res = "";
		res += title + "[" +  arr.length + "]:";
		for (int j = 0; j < arr.length; j++) {
			res += arr[j] + ", ";
		}
		res += "\n";
		return res;
	}

	public static String arrToStr(double [] arr) {
		return arrToStr(arr, DEFAULT_TITLE, Integer.MAX_VALUE);
	}

	public static String arrToStr(double [] arr, int maxLen) {
		return arrToStr(arr, DEFAULT_TITLE, maxLen);
	}
	
	public static String arrToStr(double [] arr, String title) {
		return arrToStr(arr, title, Integer.MAX_VALUE);
	}

	public static String arrToStr(double [] arr, String title, int maxLen) {
		String res = "";
		res += title + "[" +  arr.length + "]:";
		for (int j = 0; j < arr.length && j < maxLen; j++) {
			res += formatDouble(arr[j]) + ", ";
		}
		return res;
	}

	public static String doubleArrayToPrintString(double[][] m) {
		return doubleArrayToPrintString(m, ", ", Integer.MAX_VALUE, m.length, Integer.MAX_VALUE, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, int maxRows) {
		return doubleArrayToPrintString(m, ", ", maxRows, maxRows, Integer.MAX_VALUE, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, String colDelimiter) {
		return doubleArrayToPrintString(m, colDelimiter, Integer.MAX_VALUE, -1, Integer.MAX_VALUE, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, String colDelimiter, int toprowlim) {
		return doubleArrayToPrintString(m, colDelimiter, toprowlim, -1, Integer.MAX_VALUE, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, int toprowlim, int btmrowlim) {
		return doubleArrayToPrintString(m, ", ", toprowlim, btmrowlim, Integer.MAX_VALUE, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, int toprowlim, int btmrowlim, int collim) {
		return doubleArrayToPrintString(m, ", ", toprowlim, btmrowlim, collim, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, String colDelimiter, int toprowlim, int btmrowlim) {
		return doubleArrayToPrintString(m, colDelimiter, toprowlim, btmrowlim, Integer.MAX_VALUE, "\n");
	}

	public static String doubleArrayToPrintString(double[][] m, String colDelimiter, int toprowlim, int btmrowlim, int collim) {
		return doubleArrayToPrintString(m, colDelimiter, toprowlim, btmrowlim, collim, "\n");
	}
	
	public static String doubleArrayToPrintString(double[][] m, String colDelimiter, int toprowlim, int btmrowlim, int collim, String sentenceDelimiter) {
		StringBuffer str = new StringBuffer(m.length * m[0].length);

		str.append("Dim:" + m.length + " x " + m[0].length + "\n");

		int i = 0;
		for (; i < m.length && i < toprowlim; i++) {
			String rowPref = i < 1000 ? String.format("%03d", i) : String.format("%04d", i);
			str.append(rowPref+": [");
			for (int j = 0; j < m[i].length - 1 && j < collim; j++) {
				String formatted = formatDouble(m[i][j]);
				str = str.append(formatted);
				str = str.append(colDelimiter);
			}
			str = str.append(formatDouble(m[i][m[i].length - 1]));

			if( collim == Integer.MAX_VALUE) { 
				str.append("]");
			} else {
				str.append("...]");
			}
			if (i < m.length - 1) {
				str = str.append(sentenceDelimiter);
			}
		}
		if(btmrowlim<0) return str.toString();
		while(i<(m.length-btmrowlim)) i++;
		if( i < m.length) str.append("\t.\n\t.\n\t.\n");
		for (; i < m.length; i++) {
			String rowPref = i < 1000 ? String.format("%03d", i) : String.format("%04d", i);
			str.append(rowPref+": [");
			for (int j = 0; j < m[i].length - 1 && j < collim; j++) {
				str = str.append(formatDouble(m[i][j]));
				str = str.append(colDelimiter);
			}
			str = str.append(formatDouble(m[i][m[i].length - 1]));

			if( collim > m[i].length ) { 
				str.append("]");
			} else {
				str.append(", ...]");
			}
			if (i < m.length - 1) {
				str = str.append(sentenceDelimiter);
			}
		}
		return str.toString();
	}

	public static String formatDouble(double d) {
		if ( d == 0.0 ) return "<0.0>";
		if ( d<0.0001 && d>0 || d > -0.0001 && d < 0) {
			return mydecimalFormat.format(d);
		} else {
			String formatString = "%." + noDigits + "f";
			return String.format(formatString, d);
		}
	}

	public static String doubleArrayToString(double[][] m) {
		return doubleArrayToString(m, ",");
	}

	public static String doubleArrayToString(double[][] m, String colDelimiter) {
		StringBuffer str = new StringBuffer(m.length * m[0].length);
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length - 1; j++) {
				str = str.append(Double.toString(m[i][j]));
				str = str.append(colDelimiter);
			}
			str = str.append(Double.toString(m[i][m[i].length - 1]));
			str = str.append("\n");
		}
		return str.toString();
	}

	public static double [] rep(double val, int times) {
		double [] res = new double[times];
		for (int i = 0; i < res.length; i++) {
			res[i] = val;
		}
		return res;
	}

	public static double [] asVector(double [][] matrix) {
		boolean isCol = matrix.length != 1;
		int n = matrix.length == 1 ? matrix[0].length : matrix.length;
		if(matrix.length != 1 && matrix[0].length!=1) {
			throw new IllegalArgumentException("Cannot convert non-row or col matrix to vactor! Matrix dim: " 
					+ matrix.length + "x" + matrix[0].length);
		}

		double [] res = new double[n];

		if(isCol) {
			for (int j = 0; j < matrix.length; j++) {				
				res[j] = matrix[j][0]; 
			}
		} else {
			for (int j = 0; j < matrix[0].length; j++) {				
				res[j] = matrix[0][j]; 
			}			
		}

		return res;
	}

	
	public static double [][] centerAndScaleGlobal(double [][] matrix) {
		double [][] res = new double[matrix.length][matrix[0].length]; 
		double mean = mean(matrix);
		double std = stdev(matrix);
		for (int i = 0; i < res.length; i++) {
			for (int j = 0; j < res[i].length; j++) {
				res[i][j] = (matrix[i][j]-mean) / std; 
			}
		}
		
		return res;
	}
	
	
	public static double [][] centerAndScale(double [][] matrix) {
		double [][] res = new double[matrix.length][matrix[0].length]; 
		double [] means = colMeans(matrix);
		for (int i = 0; i < res.length; i++) {
			for (int j = 0; j < res[i].length; j++) {
				res[i][j] = (matrix[i][j]-means[j]); 
			}
		}
		
		double [] std = colStddev(res);
		for (int i = 0; i < res.length; i++) {
			for (int j = 0; j < res[i].length; j++) {
				res[i][j] = res[i][j] / (std[j] == 0 ? 1 : std[j]); 
			}
		}

		return res;
	}
	
	public static double [][] centerAndScaleSametime(double [][] matrix) {
		double [][] res = new double[matrix.length][matrix[0].length]; 
		double [] means = colMeans(matrix);
		double [] std = colStddev(matrix);
		for (int i = 0; i < res.length; i++) {
			for (int j = 0; j < res[i].length; j++) {
				res[i][j] = (matrix[i][j]-means[j]) / (std[j] == 0 ? 1 : std[j]); 
			}
		}

		return res;
	}
	
	
	public static double [][] addNoise(double [][] matrix) {
		double [][] res = new double[matrix.length][matrix[0].length]; 
		double [] std = colStddev(matrix);
		for (int i = 0; i < res.length; i++) {
			for (int j = 0; j < res[i].length; j++) {
				double noise = rnorm(0, std[j] == 0.0 ? 0.00001 : std[j]/5);
				res[i][j] = matrix[i][j] + noise; 
			}
		}

		return res;
	}

	
	public static double[][] transposeSerial(double[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		double[][] transpose = new double[cols][rows];
		for (int col = 0; col < cols; col++)
			for (int row = 0; row < rows; row++)
				transpose[col][row] = matrix[row][col];
		return transpose;
	}

	
	public double[][] transpose(double[][] matrix) {
		return transpose(matrix, 1000);
	}
	
	
	
	public double[][] transpose(double[][] matrix, int ll) {
		int cols = matrix[0].length;
		int rows = matrix.length;
		double[][] transpose = new double[cols][rows];
		if(rows < 100 ) {
			for (int i = 0; i < cols; i++)
				for (int j = 0; j < rows; j++)
					transpose[i][j] = matrix[j][i];
		} else {
			MatrixTransposer process = new MatrixTransposer(matrix, transpose,0,rows,ll);                
			pool.invoke(process);
		}
		return transpose;
	}

	class MatrixTransposer extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		double [][] orig;
		double [][] transpose;
		int startRow = -1;
		int endRow = -1;
		int limit = 1000;

		public MatrixTransposer(double [][] orig, double [][] transpose, int startRow, int endRow, int ll) {
			this.limit = ll;
			this.orig = orig;
			this.transpose = transpose;
			this.startRow = startRow;
			this.endRow = endRow;
		}

		public MatrixTransposer(double [][] orig, double [][] transpose, int startRow, int endRow) {
			this.orig = orig;
			this.transpose = transpose;
			this.startRow = startRow;
			this.endRow = endRow;
		}

		@Override
		protected void compute() {
			try {
				if ( (endRow-startRow) <= limit ) {
					int cols = orig[0].length;
					for (int i = 0; i < cols; i++) {
						for (int j = startRow; j < endRow; j++) {
							transpose[i][j] = orig[j][i];
						}
					}
				}
				else {
					int range = (endRow-startRow);
					int startRow1 = startRow;
					int endRow1 = startRow + (range / 2);
					int startRow2 = endRow1;
					int endRow2 = endRow;
					invokeAll(new MatrixTransposer(orig, transpose, startRow1, endRow1, limit),
							new MatrixTransposer(orig, transpose, startRow2, endRow2, limit));
				}
			}
			catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}

	
	public static double [][] exp(double [][] m1) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = Math.exp(m1[i][j]);
			}
		}
		return matrix;
	}

	public static double [] sqrt(double [] v1) {
		double [] vector = new double[v1.length];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = Math.sqrt(v1[i]);
		}
		return vector;
	}

	
	public static double mean(double [] vector) {
		double sum = 0.0;
		for (int i = 0; i < vector.length; i++) {
			sum +=vector[i];
		}
		return sum/vector.length;
	}

	
	public static double [][] log(double [][] m1) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = Math.log(m1[i][j]);
			}
		}
		return matrix;
	}

	
	public static double [][] pow(double [][] m1, double power) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = Math.pow(m1[i][j], power);
			}
		}
		return matrix;
	}

	
	public static double [] pow(double [] m1, double power) {
		double[] matrix = new double[m1.length];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = Math.pow(m1[i], power);
		}
		return matrix;
	}

	
	public static double [][] log(double [][] m1, boolean infAsZero) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = Math.log(m1[i][j]);
				if(infAsZero && Double.isInfinite(matrix[i][j]))
					matrix[i][j] = 0.0;
			}
		}
		return matrix;
	}

	
	public static double [][] scalarInverse(double [][] m1) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] = 1/m1[i][j];
			}
		}
		return matrix;
	}

	
	public static double [] scalarInverse(double [] v1) {
		double [] vector = new double[v1.length];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = 1/v1[i];
		}
		return vector;
	}

	
	public static double[][] rnorm(int m, int n) {
		double[][] array = new double[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < array[i].length; j++) {				
				array[i][j] = rnorm(0.0,1.0);
			}
		}
		return array;
	}
	
	public static double [] rnorm(int n, double [] mus, double [] sigmas) {
		double [] res = new double[n];
		for (int i = 0; i < res.length; i++) {
			res[i] = mus[i] + (ThreadLocalRandom.current().nextGaussian() * sigmas[i]);
		}
		return res; 
	}

	public static double [] rnorm(int n, double mu, double [] sigmas) {
		double [] res = new double[n];
		for (int i = 0; i < res.length; i++) {
			res[i] = mu + (ThreadLocalRandom.current().nextGaussian() * sigmas[i]);
		}
		return res; 
	}

	public static double rnorm() {
		return ThreadLocalRandom.current().nextGaussian();
	}

	/**
	 * Generate random draw from Normal with mean mu and std. dev sigma
	 * @param mu
	 * @param sigma
	 * @return random sample
	 */
	public static double rnorm(double mu, double sigma) {
		return mu + (ThreadLocalRandom.current().nextGaussian() * sigma);
	}

	
	public static boolean [][] equal(double [][] matrix1, double [][] matrix2) {
		boolean [][] equals = new boolean[matrix1.length][matrix1[0].length];
		if( matrix1.length != matrix2.length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		if( matrix1[0].length != matrix2[0].length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		for (int i = 0; i < matrix1.length; i++) {
			for (int j = 0; j < matrix1[0].length; j++) {
				equals[i][j] = Double.compare(matrix1[i][j], matrix2[i][j]) == 0;
			}
		}
		return equals;
	}

	
	public static boolean [][] equal(boolean [][] matrix1, boolean [][] matrix2) {
		boolean [][] equals = new boolean[matrix1.length][matrix1[0].length];
		if( matrix1.length != matrix2.length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		if( matrix1[0].length != matrix2[0].length) {
			throw new IllegalArgumentException("Dimensions does not match");
		}
		for (int i = 0; i < matrix1.length; i++) {
			for (int j = 0; j < matrix1[0].length; j++) {
				equals[i][j] = (matrix1[i][j] == matrix2[i][j]);
			}
		}
		return equals;
	}

	
	public static boolean [][] biggerThan(double [][] matrix, double value) {
		boolean [][] equals = new boolean[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				equals[i][j] = Double.compare(matrix[i][j], value) == 1;
			}
		}
		return equals;
	}

	
	public static boolean [][] negate(boolean [][] booleans) {
		boolean [][] negates = new boolean[booleans.length][booleans[0].length];
		for (int i = 0; i < booleans.length; i++) {
			for (int j = 0; j < booleans[0].length; j++) {
				negates[i][j] = !booleans[i][j];
			}
		}
		return negates;
	}

	
	public static double [][] abs(boolean [][] booleans) {
		double [][] absolutes = new double[booleans.length][booleans[0].length];
		for (int i = 0; i < booleans.length; i++) {
			for (int j = 0; j < booleans[0].length; j++) {
				absolutes[i][j] = booleans[i][j] ? 1 : 0;
			}
		}
		return absolutes;
	}

	
	public static double [][] abs(double [][] vals) {
		double [][] absolutes = new double[vals.length][vals[0].length];
		for (int i = 0; i < vals.length; i++) {
			for (int j = 0; j < vals[0].length; j++) {
				absolutes[i][j] = Math.abs(vals[i][j]);
			}
		}
		return absolutes;
	}

	public static double [] abs(double [] vals) {
		double [] absolutes = new double[vals.length];
		for (int i = 0; i < vals.length; i++) {
			absolutes[i] = Math.abs(vals[i]);
		}
		return absolutes;
	}

	public static double [][] sign(double [][] matrix) {
		double [][] signs = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				signs[i][j] = matrix[i][j] >= 0 ? 1 : -1;
			}
		}
		return signs;
	}

	public static double mean(double [][] matrix) {
		return mean(matrix,2)[0][0];
	}
	
	
	public static double [][] mean(double [][] matrix, int axis) {
	
		double [][] result;
		if( axis == 0) {
			result = new double[1][matrix[0].length];
			for (int j = 0; j < matrix[0].length; j++) {
				double colsum = 0.0;
				for (int i = 0; i < matrix.length; i++) {
					colsum += matrix[i][j];
				}
				result[0][j] = colsum / matrix.length;
			}
		}   else if (axis == 1) {
			result = new double[matrix.length][1];
			for (int i = 0; i < matrix.length; i++) {
				double rowsum = 0.0;
				for (int j = 0; j < matrix[0].length; j++) {
					rowsum += matrix[i][j];
				}
				result[i][0] = rowsum / matrix[0].length;
			}
		}   else if (axis == 2) {
			result = new double[1][1];
			for (int j = 0; j < matrix[0].length; j++) {
				for (int i = 0; i < matrix.length; i++) {
					result[0][0] += matrix[i][j];
				}
			}
			result[0][0] /=  (matrix[0].length *  matrix.length);
		}else {
			throw  new IllegalArgumentException("Axes other than 0,1,2 is unsupported");
		}
		return result;
	}

	
	public static double [][] sum(double [][] matrix, int axis) {
		// Axis = 0 => sum columns
		// Axis = 1 => sum rows
		double [][] result;
		if( axis == 0) {
			result = new double[1][matrix[0].length];
			for (int j = 0; j < matrix[0].length; j++) {
				double rowsum = 0.0;
				for (int i = 0; i < matrix.length; i++) {
					rowsum += matrix[i][j];
				}
				result[0][j] = rowsum;
			}
		}   else if (axis == 1) {
			result = new double[matrix.length][1];
			for (int i = 0; i < matrix.length; i++) {
				double colsum = 0.0;
				for (int j = 0; j < matrix[0].length; j++) {
					colsum += matrix[i][j];
				}
				result[i][0] = colsum;
			}
		}   else {
			throw  new IllegalArgumentException("Axes other than 0,1 is unsupported");
		}
		return result;
	}

	
	public double sumPar(double[][] matrix) {
		int ll = 100;
		int cols = matrix[0].length;
		int rows = matrix.length;
		double [] sums = new double[rows];
		if(rows < ll ) {
			for (int row = 0; row < rows; row++)
				for (int col = 0; col < cols; col++)
					sums[row] += matrix[row][col];
		} else {
			MatrixSummer process = new MatrixSummer(matrix, sums, 0, rows, ll);                
			pool.invoke(process);
		}
		double sum = 0.0;
		for (int i = 0; i < sums.length; i++) {
			sum += sums[i];
		}
		return sum;
	}

	class MatrixSummer extends RecursiveAction {
		private static final long serialVersionUID = 1L;
		double [][] orig;
		double [] sums;
		int startRow = -1;
		int endRow = -1;
		int limit = 1000;

		public MatrixSummer(double [][] orig, double [] sums, int startRow, int endRow, int ll) {
			this.limit = ll;
			this.orig = orig;
			this.sums = sums;
			this.startRow = startRow;
			this.endRow = endRow;
		}

		public MatrixSummer(double [][] orig, double [] transpose, int startRow, int endRow) {
			this.orig = orig;
			this.sums = transpose;
			this.startRow = startRow;
			this.endRow = endRow;
		}

		@Override
		protected void compute() {
			try {
				if ( (endRow-startRow) <= limit ) {
					int cols = orig[0].length;
					for (int row = startRow; row < endRow; row++) {
						for (int i = 0; i < cols; i++) {
							sums[row] += orig[row][i];
						}
					}
				}
				else {
					int range = (endRow-startRow);
					int startRow1 = startRow;
					int endRow1 = startRow + (range / 2);
					int startRow2 = endRow1;
					int endRow2 = endRow;
					invokeAll(new MatrixSummer(orig, sums, startRow1, endRow1, limit),
							new MatrixSummer(orig, sums, startRow2, endRow2, limit));
				}
			}
			catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}


	
	public static double sum(double [][] matrix) {
		double sum = 0.0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				sum+=matrix[i][j];
			}
		}
		return sum;
	}

	public static double sum(double [] vector) {
		double res = 0.0;
		for (int i = 0; i < vector.length; i++) {
			res += vector[i];
		}
		return res;
	}

	
	public static double [][] maximum(double [][] matrix, double maxval) {
		double [][] maxed = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				maxed[i][j] = matrix[i][j] > maxval ? matrix[i][j] : maxval;
			}
		}
		return maxed;
	}

	
	public static void assignAllLessThan(double[][] matrix, double lessthan, double assign) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if( matrix[i][j] < lessthan) {
					matrix[i][j] = assign;
				}
			}
		}
	}

	
	public static double [][] square(double [][] matrix) {
		return scalarPow(matrix,2);
	}

	
	public static double [][] replaceNaN(double [][] matrix, double repl) {
		double [][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if(Double.isNaN(matrix[i][j])) {
					result[i][j] = repl;
				} else {
					result[i][j] = matrix[i][j];
				}
			}
		}
		return result;
	}

	
	public static double [][] replaceInf(double [][] matrix, double repl) {
		double [][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if(Double.isInfinite(matrix[i][j])) {
					result[i][j] = repl;
				} else {
					result[i][j] = matrix[i][j];
				}
			}
		}
		return result;
	}

	
	public static double [][] scalarPow(double [][] matrix, double power) {
		double [][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] += Math.pow(matrix[i][j],power);
			}
		}
		return result;
	}

	public static double [][] addColumnVector(double [][] matrix, double [][] colvector) {
		double [][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] = matrix[i][j] + colvector[i][0];
			}
		}
		return result;
	}

	public static double [][] addRowVector(double [][] matrix, double [][] rowvector) {
		double [][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] = matrix[i][j] + rowvector[0][j];
			}
		}
		return result;
	}

	public static double [][] fillWithRowOld(double [][] matrix, int row) {
		double [][] result = new double[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				result[i][j] = matrix[row][j];
			}
		}
		return result;
	}

	public static double [][] fillWithRow(double [][] matrix, int row) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		double [][] result = new double[rows][cols];
		for (int i = 0; i < rows; i++) {
			System.arraycopy(matrix[row], 0, result[i], 0, cols);
		}
		return result;
	}


	public static double [][] tile(double [][] matrix, int rowtimes, int coltimes) {
		double [][] result = new double[matrix.length*rowtimes][matrix[0].length*coltimes];
		for (int i = 0, resultrow = 0; i < rowtimes; i++) {
			for (int j = 0; j < matrix.length; j++) {
				for (int k = 0, resultcol = 0; k < coltimes; k++) {
					for (int l = 0; l < matrix[0].length; l++) {
						result[resultrow][resultcol++] = matrix[j][l];
					}
				}
				resultrow++;
			}
		}

		return result;
	}

	public static double[][] normalize(double[][] x, double[] meanX, double[] stdevX) {
		double[][] y = new double[x.length][x[0].length];
		for (int i = 0; i < y.length; i++)
			for (int j = 0; j < y[i].length; j++)
				y[i][j] = (x[i][j] - meanX[j]) / stdevX[j];
		return y;
	}
	
	public static int [] range(int n) {
		int [] result = new int[n];
		for (int i = 0; i < n; i++) {
			result[i] = i;
		}
		return result;
	}

	public static int [] range(int a, int b) {
		if( b < a ) {
			throw new IllegalArgumentException("b has to be larger than a");
		}
		int val = a;
		int [] result = new int[b-a];
		for (int i = 0; i < (b-a); i++) {
			result[i] = val++;
		}
		return result;
	}

	
	public static int [] concatenate(int [] v1,int [] v2) {
		int [] result = new int[v1.length+v2.length];
		int index = 0;
		for (int i = 0; i < v1.length; i++, index++) {
			result[index] = v1[index];
		}
		for (int i = 0; i < v2.length; i++, index++) {
			result[index] = v2[i];
		}
		return result;
	}
	
	
		public static double [] concatenate(double [] v1,double [] v2) {
			double [] result = new double[v1.length+v2.length];
			int index = 0;
			for (int i = 0; i < v1.length; i++, index++) {
				result[index] = v1[index];
			}
			for (int i = 0; i < v2.length; i++, index++) {
				result[index] = v2[i];
			}
			return result;
		}

	
	public static double [][] concatenate(double [][] m1,double[][] m2) {
		if(m1.length!=m2.length) throw new IllegalArgumentException("m1 and m2 must have the same number of rows:" + m1.length + " != " + m2.length);
		double [][] result = new double[m1.length][m1[0].length+m2[0].length];
		int resCol = 0;
		for (int i = 0; i < m1.length; i++) {
			resCol = 0;
			for (int j = 0; j < m1[i].length; j++) {
				result[i][resCol++] = m1[i][j];
			}
			for (int j = 0; j < m2[i].length; j++) {
				result[i][resCol++] = m2[i][j];
			}
		}	
		return result;
	}

	
	public static double [][] concatenate(double [][] m1,double[] v2) {
		if(m1.length!=v2.length) throw new IllegalArgumentException("m1 and v2 must have the same number of rows:" + m1.length + " != " + v2.length);
		double [][] result = new double[m1.length][m1[0].length+1];
		int resCol = 0;
		for (int i = 0; i < m1.length; i++) {
			resCol = 0;
			for (int j = 0; j < m1[i].length; j++) {
				result[i][resCol++] = m1[i][j];
			}
			result[i][resCol++] = v2[i];
		}	
		return result;
	}

	public double [][] scalarMultiply(double [][] m1,double [][] m2) {
		return parScalarMultiply(m1, m2);
	}
	
	
	public static double [][] sMultiply(double [][] v1,double [][] v2) {
		if( v1.length != v2.length || v1[0].length != v2[0].length ) {
			throw new IllegalArgumentException("a and b has to be of equal dimensions");
		}
		double [][] result = new double[v1.length][v1[0].length];
		for (int i = 0; i < v1.length; i++) {
			for (int j = 0; j < v1[0].length; j++) {
				result[i][j] = v1[i][j] * v2[i][j];
			}
		}
		return result;
	}
	
	public double[][] parScalarMultiply(double [][] m1,double [][] m2) {
		int ll = 600;
		double [][] result = new double[m1.length][m1[0].length];

		MatrixOperator process = new MatrixOperator(m1,m2,result, multiplyop, 0, m1.length,ll);                
		pool.invoke(process);
		return result;
	}

	public double[][] parScalarMinus(double [][] m1,double [][] m2) {
		int ll = 600;
		double [][] result = new double[m1.length][m1[0].length];
		
		MatrixOperator process = new MatrixOperator(m1,m2,result, minusop, 0, m1.length,ll);                
		pool.invoke(process);
		return result;
	}

	public interface MatrixOp {
		double compute(double op1, double op2);
	}

	MatrixOp multiplyop = new MatrixOp() {
		public double compute(double f1, double f2) {
			return f1 * f2;
		}
	};

	MatrixOp minusop = new MatrixOp() {
		public double compute(double f1, double f2) {
			return f1 - f2;
		}
	};

	class MatrixOperator extends RecursiveAction {
		final static long serialVersionUID = 1L;
		double [][] matrix1;
		double [][] matrix2;
		double [][] resultMatrix;
		int startRow = -1;
		int endRow = -1;
		int limit = 1000;
		MatrixOp op;

		public MatrixOperator(double [][] matrix1, double [][] matrix2, double [][] resultMatrix, 
				MatrixOp op, int startRow, int endRow, int ll) {
			this.op = op;
			this.limit = ll;
			this.matrix1 = matrix1;
			this.matrix2 = matrix2;
			this.resultMatrix = resultMatrix;
			this.startRow = startRow;
			this.endRow = endRow;
		}

		@Override
		protected void compute() {
			try {
				if ( (endRow-startRow) <= limit ) {
					int cols = matrix1[0].length;
					for (int i = startRow; i < endRow; i++) {
						for (int j = 0; j < cols; j++) {
							resultMatrix[i][j] = op.compute(matrix1[i][j], matrix2[i][j]);
						}
					}
				}
				else {
					int range = (endRow-startRow);
					int startRow1 = startRow;
					int endRow1 = startRow + (range / 2);
					int startRow2 = endRow1;
					int endRow2 = endRow;
					invokeAll(new MatrixOperator(matrix1, matrix2, resultMatrix, op, startRow1, endRow1, limit),
							new MatrixOperator(matrix1, matrix2, resultMatrix, op, startRow2, endRow2, limit));
				}
			}
			catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}


	public static void assignAtIndex(double[][] num, int[] range, int[] range1, double value) {
		for (int j = 0; j < range.length; j++) {
			num[range[j]][range1[j]] = value;
		}
	}

	public static double [][] getValuesFromRow(double[][] matrix, int row, int[] indicies) {
		double [][] values = new double[1][indicies.length];
		for (int j = 0; j < indicies.length; j++) {
			values[0][j] = matrix[row][indicies[j]];
		}
		return values;
	}

	public static void assignValuesToRow(double[][] matrix, int row, int[] indicies, double [] values) {
		if( indicies.length != values.length ) {
			throw new IllegalArgumentException("Length of indicies and values have to be equal");
		}
		for (int j = 0; j < indicies.length; j++) {
			matrix[row][indicies[j]] = values[j];
		}
	}

	public static double stdev(double [][] matrix) {
		double m = mean(matrix);

        double total = 0;

        final int N = matrix.length * matrix[0].length;

        for( int i = 0; i < matrix.length; i++ ) {
        	for (int j = 0; j < matrix[i].length; j++) {				
        		double x = matrix[i][j];
        		total += (x - m)*(x - m);
			}
        }

        return Math.sqrt(total / (N-1));	
	}
	
	public static double[] colStddev(double[][] v) {
		double[] var = variance(v);
		for (int i = 0; i < var.length; i++)
			var[i] = Math.sqrt(var[i]);
		return var;
	}

	public static double[] variance(double[][] v) {
		int m = v.length;
		int n = v[0].length;
		double[] var = new double[n];
		int degrees = (m - 1);
		double c;
		double s;
		for (int j = 0; j < n; j++) {
			c = 0;
			s = 0;
			for (int k = 0; k < m; k++)
				s += v[k][j];
			s = s / m;
			for (int k = 0; k < m; k++)
				c += (v[k][j] - s) * (v[k][j] - s);
			var[j] = c / degrees;
		}
		return var;
	}

	
	public static double[] colMeans(double[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;
		double[] mean = new double[cols];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				mean[j] += matrix[i][j];
		for (int j = 0; j < cols; j++)
			mean[j] /= (double) rows;
		return mean;
	}

	public static double[][] copyRows(double[][] input, int... indices) {
		double[][] matrix = new double[indices.length][input[0].length];
		for (int i = 0; i < indices.length; i++)
			System.arraycopy(input[indices[i]], 0, matrix[i], 0, input[indices[i]].length);
		return matrix;
	}

	public static double[][] copyCols(double[][] input, int... indices) {
		double[][] matrix = new double[indices.length][input.length];
		for (int i = 0; i < indices.length; i++)
			for (int j = 0; j < input.length; j++) {
				matrix[i][j] = input[j][indices[i]];
			}
		return matrix;
	}

	public static double[][] fillMatrix(int rows, int cols, double fillvalue) {
		double[][] matrix = new double[rows][cols];
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[i].length; j++)
				matrix[i][j] = fillvalue;
		return matrix;
	}

	public static double[][] plus(double[][] m1, double[][] m2) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				matrix[i][j] = m1[i][j] + m2[i][j];
		return matrix;
	}


	public static double[][] scalarPlus(double[][] m1, double m2) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				matrix[i][j] = m1[i][j] + m2;
		return matrix;
	}


	public static double [] scalarPlus(double[] m1, double m2) {
		double[] matrix = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			matrix[i] = m1[i] + m2;
		return matrix;
	}

	public double[][] minus(double[][] m1, double[][] m2) {
		return parScalarMinus(m1, m2);
	}


	public static double[][] sMinus(double[][] m1, double[][] m2) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[0].length; j++)
				matrix[i][j] = m1[i][j] - m2[i][j];
		return matrix;
	}

	
	public static double[][] scalarDivide(double[][] numerator, double denom) {
		double[][] matrix = new double[numerator.length][numerator[0].length];
		for (int i = 0; i < numerator.length; i++)
			for (int j = 0; j < numerator[i].length; j++)
				matrix[i][j] = numerator[i][j] / denom;
		return matrix;
	}

	
	public static double [] scalarDivide(double numerator, double[] denom) {
		double[] vector = new double[denom.length];
		for (int i = 0; i < denom.length; i++)
			vector[i] = numerator / denom[i] ;
		return vector;
	}

	
	public static double [] scalarDivide(double[] numerator, double denom) {
		double[] vector = new double[numerator.length];
		for (int i = 0; i < numerator.length; i++)
			vector[i] = numerator[i] / denom;
		return vector;
	}

	
	public static double [] scalarDivide(double [] numerator, double[] denom) {
		double[] vector = new double[denom.length];
		for (int i = 0; i < denom.length; i++)
			vector[i] = numerator[i] / denom[i] ;
		return vector;
	}

	
	public static double[][] scalarDivide(double[][] numerator, double[][] denom) {
		double[][] matrix = new double[numerator.length][numerator[0].length];
		for (int i = 0; i < numerator.length; i++)
			for (int j = 0; j < numerator[i].length; j++)
				matrix[i][j] = numerator[i][j] / denom[i][j];
		return matrix;
	}
	
	
	public static double[][] scalarMult(double[][] m1, double mul) {
		double[][] matrix = new double[m1.length][m1[0].length];
		for (int i = 0; i < m1.length; i++)
			for (int j = 0; j < m1[i].length; j++)
				matrix[i][j] = m1[i][j] * mul;
		return matrix;
	}
	
	
	public static double[][] times(double[][] m1, double[][] m2) {
		
		 int m1ColLength = m1[0].length; 
	        int m2RowLength = m2.length;    
	        if(m1ColLength != m2RowLength) return null; 
	        int mRRowLength = m1.length;    
	        int mRColLength = m2[0].length; 
	        double[][] mResult = new double[mRRowLength][mRColLength];
	        for(int i = 0; i < mRRowLength; i++) {         
	            for(int j = 0; j < mRColLength; j++) {     
	                for(int k = 0; k < m1ColLength; k++) { 
	                    mResult[i][j] += m1[i][k] * m2[k][j];
	                }
	            }
	        }
	        return mResult;
	}

	public static double [] scalarMultiply(double[] m1, double mul) {
		double[] matrix = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			matrix[i] = m1[i] * mul;
		return matrix;
	}

	public static double [] scalarMultiply(double[] m1, double [] m2) {
		double[] matrix = new double[m1.length];
		for (int i = 0; i < m1.length; i++)
			matrix[i] = m1[i] * m2[i];
		return matrix;
	}

	public static double[][] diag(double[][] ds) {
		boolean isLong = ds.length > ds[0].length;
		int dim = Math.max(ds.length,ds[0].length);
		double [][] result = new double [dim][dim];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result.length; j++) {
				if(i==j) {
					if(isLong)
						result[i][j] = ds[i][0];
					else
						result[i][j] = ds[0][i];
				}
			}
		}

		return result;
	}

	public static double [][] dot(double [][] a, double [][] b) {
		if(a[0].length!=b.length) throw new IllegalArgumentException("Dims does not match: " + a[0].length  + "!=" + b.length);
		double [][] res = new double[a.length][b[0].length];
		for (int row = 0; row < a.length; row++) {
			for (int col = 0; col < b[row].length; col++) {
				for (int i = 0; i < a[0].length; i++) {
					res[row][col] = a[row][i] * b[i][col];
				}
			}
		}
		return res;
	}

	public static double dot(double [] a, double [] b) {
		if(a.length!=b.length) {
			throw new IllegalArgumentException("Vectors are not of equal length");
		}
		double res = 0.0;
		for (int i = 0; i < b.length; i++) {
			res += a[i] * b[i];
		}
		return res;
	}
	
	public static double dot2P1(double [] a1, double [] a2, double [] b) {
		if((a1.length+a2.length)!=b.length) {
			throw new IllegalArgumentException("Vectors are not of equal length");
		}
		double res = 0.0;
		int bidx = 0;
		for (int i = 0; i < a1.length; i++, bidx++) {
			res += a1[i] * b[bidx];
		}
		for (int i = 0; i < a2.length; i++, bidx++) {
			res += a2[i] * b[bidx];
		}
		return res;
	}

	public static int maxIdx(double[] probs) {
		int maxIdx = 0;
		double max = probs[maxIdx];
		for (int i = 0; i < probs.length; i++) {
			if(probs[i]>max) {
				max = probs[i];
				maxIdx = i;
			}
		}
		return maxIdx;
	}

	public static double[][] extractCol(int col, double[][] matrix) {
		double [][] res = new double[matrix.length][1];
		for (int row = 0; row < matrix.length; row++) {
			res[row][0] = matrix[row][col];
		}
		return res;
	}

	public static double[] extractColVector(int col, double[][] matrix) {
		double [] res = new double[matrix.length];
		for (int row = 0; row < matrix.length; row++) {
			res[row] = matrix[row][col];
		}
		return res;
	}

	

	public static double[][] extractRowCols(int col, double[][] zs2, int[] cJIdxs) {
		double [][] res = new double[cJIdxs.length][1];
		for (int row = 0; row < cJIdxs.length; row++) {
			res[row][0] = zs2[cJIdxs[row]][col];
		}
		return res;
	}

	public static Integer [] indicesOf(int classIdx, int [] ys) {
		List<Integer> indices = new ArrayList<Integer>();
		for (int row = 0; row < ys.length; row++) {
			if(ys[row]==classIdx) {
				indices.add(row);
			}
		}
		return indices.toArray(new Integer[0]);
	}

	public static double[][] makeDesignMatrix(double[][] xstmp) {
		double [][] xs = new double[xstmp.length][xstmp[0].length+1];
		for (int row = 0; row < xs.length; row++) {
			for (int col = 0; col < xs[0].length; col++) {
				if(col==0) {
					xs[row][col] = 1.0;	
				} else {
					xs[row][col] = xstmp[row][col-1];
				}
			}
		}
		return xs;
	}
	
	public static Matrix arrayToCyMatrix(double[][] array){
		Matrix matrix=new ColtMatrix(array.length, array[0].length);
		
		for (int row = 0; row < array.length; row++) {
			for (int col = 0; col < array[0].length; col++) {
			matrix.setValue(row, col, array[row][col]);
			}}
		return matrix;
	}

	public static double[][] addIntercept(double[][] xs) {
		double [][] result = new double [xs.length][xs[0].length+1];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[0].length; j++) {
				if(j==0) {
					result[i][j] = 1.0;
				} else {
					result[i][j] = xs[i][j-1];
				}
			}
		}
		return result;
	}

	public static double[] toPrimitive(Double[] ds) {
		double [] result = new double[ds.length];
		for (int i = 0; i < ds.length; i++) {
			result[i] = ds[i];
		}
		return result;
	}

	@Override
	public void initialize(int rows, int columns, double[][] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(int rows, int columns, Double[][] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int nRows() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int nColumns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Double getValue(int row, int column) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double doubleValue(int row, int column) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setValue(int row, int column, double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValue(int row, int column, Double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasValue(int row, int column) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getColumnLabels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnLabel(int col) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setColumnLabel(int col, String label) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColumnLabels(List<String> labelList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String[] getRowLabels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRowLabel(int row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRowLabel(int row, String label) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRowLabels(List<String> labelList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[][] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getMaxValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isTransposed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTransposed(boolean transposed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSymmetrical() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSymmetrical(boolean symmetrical) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setMissingToZero() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void adjustDiagonals() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[] getRank(int row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void index() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Matrix submatrix(int[] index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix submatrix(int row, int col, int rows, int cols) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void invertMatrix() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void normalize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void normalizeMatrix() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void normalizeRow(int row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void normalizeColumn(int column) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void standardizeRow(int row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void standardizeColumn(int column) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void centralizeRows() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void centralizeColumns() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double rowSum(int row) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double columnSum(int row) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double rowVariance(int row) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double columnVariance(int row) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double rowMean(int row) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double columnMean(int column) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int cardinality() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Matrix multiplyMatrix(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix covariance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Matrix correlation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void threshold() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void threshold(double thresh) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[] eigenValues(boolean nonZero) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[][] eigenVectors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String printMatrixInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String printMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeMatrix(String filename) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DoubleMatrix2D getColtMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CyNetwork getNetwork() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRowNodes(CyNode[] rowNodes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRowNodes(List<CyNode> rowNodes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRowNode(int row, CyNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CyNode getRowNode(int row) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CyNode> getRowNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setColumnNodes(CyNode[] columnNodes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColumnNodes(List<CyNode> columnNodes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColumnNode(int column, CyNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CyNode getColumnNode(int column) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CyNode> getColumnNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAssymetricalEdge() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAssymetricalEdge(boolean assymetricalEdge) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CyMatrix getDistanceMatrix(DistanceMetric metric) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CyMatrix copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CyMatrix copy(Matrix matrix) {
		// TODO Auto-generated method stub
		return null;
	}

}
