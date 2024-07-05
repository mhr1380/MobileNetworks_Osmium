package com.example.myapplication.utils.CLCalculation;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.ArrayRealVector;

public class CircularLaterationCalculator {

    public static double[] lateration(double[][] positions, double[] distances) {
        int numPoints = positions.length;

        if (numPoints < 3) throw new IllegalArgumentException("At least three reference points are required");

        double[][] A = new double[numPoints-1][2];
        double[] B = new double[numPoints-1];

        for (int i = 0; i < numPoints - 1; i++) {
            A[i][0] = 2 * (positions[i+1][0] - positions[0][0]);
            A[i][1] = 2 * (positions[i+1][1] - positions[0][1]);
            B[i] = distances[0] * distances[0] - distances[i+1] * distances[i+1]
                    - positions[0][0] * positions[0][0] + positions[i+1][0] * positions[i+1][0]
                    - positions[0][1] * positions[0][1] + positions[i+1][1] * positions[i+1][1];
        }

        RealMatrix matrixA = new Array2DRowRealMatrix(A);
        RealVector vectorB = new ArrayRealVector(B);
        DecompositionSolver solver = new LUDecomposition(matrixA).getSolver();
        RealVector solution = solver.solve(vectorB);

        return solution.toArray();
    }

}
