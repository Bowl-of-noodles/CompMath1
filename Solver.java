package edu.math;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Solver {
    public static final int MAX_ITERATION = 10000;
    private double epsilon;
    private int amount;
    private double[][] matrix;
    private double[] solution;
    private double[] solutionErrors;
    boolean fromFile = false;
    private BufferedReader reader;

    public void run() throws IOException {
        init();
        matrix = readMatrix(amount);
        System.out.println("Дана матрица: ");
        printMatrix();
        if (!new transfKuhn().diagDominance()) {
            System.out.println("Нельзя достигнуть диагонального преобладания. Завершение расчетов.");
            return;
        }
        System.out.println("Диагональное преобладание достигнуто. Модифицированная матрица: ");
        printMatrix();
        modifyMatrix();
        solution = getSolution(matrix);
        int iterations = iterate();
        if (iterations == MAX_ITERATION) {
            System.out.println("Не получилось достичь сходимости за допустимое число итераций.");
            return;
        }
        System.out.println("Вектор решения: " + Arrays.toString(solution));
        System.out.println("Количество итераций: " + iterations);
        System.out.println("Вектор погрешностей: " + Arrays.toString(solutionErrors));
    }

    public void init() throws IOException {
        reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Введите имя файла или введите * чтобы считать с клавиатуры: ");
        String input = reader.readLine();
        if (!input.equals("*")) {
            int counter = 0;
            while (counter < 30) {
                try {
                    reader = new BufferedReader(new FileReader(input));
                    fromFile = true;
                    break;
                } catch (FileNotFoundException e) {
                    System.out.println("Файл с указанным именем не найден. Повторите ввод.");
                    counter++;
                    input = reader.readLine();
                }
            }
        }
        printIfConsole("Введите погрешность: ");
        String epsInput = reader.readLine().replace(",", ".");
        epsilon = Double.parseDouble(epsInput);
        while (!((amount > 0) && (amount <= 20))) {
            printIfConsole("Введите количество неизвестных системы (<= 20): ");
            amount = Integer.parseInt(reader.readLine());
        }
    }

    private void printIfConsole(String msg) {
        if (!fromFile) System.out.println(msg);
    }

    private void modifyMatrix() {
        for (int i = 0; i < matrix.length; i++) {

            double denominator = matrix[i][i];
            for(double[] els : matrix){
                for(int j = 0; j<els.length; j++){
                    els[j] = -els[j] / denominator;
                }
                els[amount] *= -1;
            }

            matrix[i][i] = 0;

        }
        System.out.println();

        for (int i = 0; i < matrix.length; i++) {
            for(int j = 0; j < matrix[i].length; j++){
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }


    private int iterate() {
        int iteration = 0;
        double maxEps = Double.MAX_VALUE;
        solutionErrors = new double[solution.length];
        
        while (maxEps >= epsilon && iteration < MAX_ITERATION) {
            double currentEps = 0;
            double[] currentSolution = Arrays.copyOf(solution, solution.length);
            
            for (int i = 0; i < currentSolution.length; i++) {
                double newRoot = 0;
                for (int j = 0; j < currentSolution.length; j++) {
                    double coeff = currentSolution[j];
                    newRoot += matrix[i][j]*coeff;
                }
                newRoot += matrix[i][amount];

                solutionErrors[i] = Math.abs(newRoot - solution[i]);
                if (solutionErrors[i] > currentEps){
                    currentEps = solutionErrors[i];
                }
                solution[i] = newRoot;
            }
            maxEps = currentEps;
            iteration++;
        }
        return iteration;
    }

    private double[] getSolution(double[][] matrix) {
        double[] solution = new double[amount];
        for (int i = 0; i < matrix.length; i++) {
            solution[i] = matrix[i][amount];
        }
        return solution;
    }

    private double[][] readMatrix(int n) throws IOException {
        printIfConsole("Введите матрицу: ");
        double[][] matrix = new double[n][n + 1];
        for (int i = 0; i < matrix.length; i++) {
            String[] line = reader.readLine().split(" ");
            matrix[i] = Arrays.stream(line).
                    mapToDouble(Double::parseDouble).
                    toArray();
        }

        return matrix;
    }

    private void printMatrix() {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    private class transfKuhn {

        List<List<Integer>> g = new ArrayList<>();
        int[] mt = new int[matrix.length];
        boolean[] used = new boolean[matrix.length];

        public boolean diagDominance() {

            for(int i = 0; i < mt.length; i++){
                mt[i] = -1;
            }


            for (int i = 0; i < matrix.length; i++) {
                List<Integer> possibleIndexes = new ArrayList<>();

                double sum = 0;
                for(int j=0; j < matrix.length; j++){
                    sum += Math.abs(matrix[i][j]);
                }

                for (int j = 0; j < matrix.length; j++) {
                    if (sum - 2 * Math.abs(matrix[i][j]) <= 0){
                        possibleIndexes.add(j);
                    }
                }
                g.add(possibleIndexes);
            }

            for (int v = 0; v < matrix.length; v++) {
                for(int i = 0; i < used.length; i++){
                    used[i] = false;
                }
                tryKuhn(v);
            }

            double[][] matrixCopy = Arrays.copyOf(matrix, matrix.length);

            for (int i = 0; i < matrix.length; i++) {
                if (mt[i] == -1) {
                    return false;
                }
                matrix[i] = matrixCopy[mt[i]];
            }
            return true;
        }

        private boolean tryKuhn(int v) {  //нахождению максимального паросочетания в двудольном графе
            if (used[v])  return false;
            used[v] = true;
            for (int i = 0; i < g.get(v).size(); ++i) {
                int to = g.get(v).get(i);
                if (mt[to] == -1 || tryKuhn(mt[to])) {
                    mt[to] = v;
                    return true;
                }
            }
            return false;
        }
    }

}
