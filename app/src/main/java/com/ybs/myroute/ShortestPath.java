package com.ybs.myroute;

import android.util.Log;

import java.util.Stack;

public class ShortestPath {
    private int numberOfNodes;
    private Stack<Integer> stack;

    public ShortestPath() {
        stack = new Stack<Integer>();
    }

    public String tsp(double adjacencyMatrix[][]) {
        numberOfNodes = adjacencyMatrix[1].length - 1;
        int[] visited = new int[numberOfNodes + 1];
        visited[1] = 1;
        stack.push(1);
        int element, dst = 0, i;
        double min = Double.MAX_VALUE;
        boolean minFlag = false;
        String route = "";
        route += "1 ";

        while (!stack.isEmpty()) {
            element = stack.peek();
            i = 1;
            min = Double.MAX_VALUE;
            while (i <= numberOfNodes) {
                if (adjacencyMatrix[element][i] > 1 && visited[i] == 0) {
                    if (min > adjacencyMatrix[element][i]) {
                        min = adjacencyMatrix[element][i];
                        dst = i;
                        minFlag = true;
                    }
                }
                i++;
            }
            if (minFlag) {
                visited[dst] = 1;
                stack.push(dst);
                route += dst + " ";
                minFlag = false;
                continue;
            }
            stack.pop();
        }
        return route;
    }
}