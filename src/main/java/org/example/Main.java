package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            //Пишем строки во временный файл, считаем количество строк и максимальное число столбцов
            int n = 0;
            int maxColumns = 0;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getClassLoader().getResourceAsStream("lng.txt"), StandardCharsets.UTF_8));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("temp.txt"), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    writer.write(line);
                    writer.newLine();
                    n++;
                    int columns = line.split(";").length;
                    if (columns > maxColumns) maxColumns = columns;
                }
            }

            int[] parent = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;

            //строим valueToRows и делаем Union-Find
            for (int col = 0; col < maxColumns; col++) {
                Map<String, List<Integer>> valueToRows = new HashMap<>();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("temp.txt"), StandardCharsets.UTF_8))) {
                    String line;
                    int idx = 0;
                    while ((line = reader.readLine()) != null) {
                        String[] row = line.split(";");
                        if (col < row.length) {
                            String val = row[col].trim();
                            if (!val.isEmpty() && !val.equals("\"\"")) {
                                valueToRows.computeIfAbsent(val, k -> new ArrayList<>()).add(idx);
                            }
                        }
                        idx++;
                    }
                }
                for (List<Integer> indices : valueToRows.values()) {
                    for (int i = 1; i < indices.size(); i++) {
                        union(parent, indices.get(0), indices.get(i));
                    }
                }
                valueToRows = null;
                System.gc();
            }

            //Path compression для всех parent[i]
            for (int i = 0; i < n; i++) {
                parent[i] = find(parent, i);
            }

            //Группировка по корню
            Map<Integer, List<Integer>> groups = new HashMap<>();
            Map<Integer, Integer> rootToGroupNum = new HashMap<>();
            int fileGroupNum = 1;
            for (int i = 0; i < n; i++) {
                int root = parent[i];
                if (!groups.containsKey(root)) {
                    groups.put(root, new ArrayList<>());
                    rootToGroupNum.put(root, fileGroupNum++);
                }
                groups.get(root).add(i);
            }

            //Считаем количество групп с более чем одним элементом
            int groupCount = 0;
            for (List<Integer> group : groups.values()) {
                if (group.size() > 1) groupCount++;
            }
            System.out.println("Количество групп (более одного элемента): " + groupCount);
            System.out.println();

            //Читаем Temp
            try (PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("temp.txt"), StandardCharsets.UTF_8))) {
                writer.println("Количество групп (более одного элемента): " + groupCount);
                writer.println();
                Map<Integer, List<String>> groupLines = new HashMap<>();
                for (Map.Entry<Integer, List<Integer>> entry : groups.entrySet()) {
                    if (entry.getValue().size() > 1) {
                        groupLines.put(rootToGroupNum.get(entry.getKey()), new ArrayList<>());
                    }
                }
                String line;
                int idx = 0;
                while ((line = reader.readLine()) != null) {
                    int root = parent[idx];
                    Integer groupNum = rootToGroupNum.get(root);
                    if (groupNum != null && groupLines.containsKey(groupNum)) {
                        groupLines.get(groupNum).add(line);
                    }
                    idx++;
                }
                //Вывод
                for (int groupNum = 1; groupNum <= groupLines.size(); groupNum++) {
                    List<String> lines = groupLines.get(groupNum);
                    if (lines != null && lines.size() > 1) {
                        writer.println("Группа " + groupNum);
                        for (String l : lines) {
                            writer.println(l);
                        }
                        writer.println();
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Время выполнения: " + (endTime - startTime) + " мс");

            //Удаляем временный файл
            new File("temp.txt").delete();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static int find(int[] parent, int x) {
        if (parent[x] != x) parent[x] = find(parent, parent[x]);
        return parent[x];
    }

    static void union(int[] parent, int x, int y) {
        int px = find(parent, x);
        int py = find(parent, y);
        if (px != py) parent[py] = px;
    }
}