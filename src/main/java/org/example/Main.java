package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            // 1. Первый проход: фильтрация строк с хотя бы одним значимым значением
            int n = 0;
            int maxColumns = 0;
            List<String> validLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getClassLoader().getResourceAsStream("lng.txt"), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] values = line.split(";");
                    boolean hasValue = false;
                    for (String v : values) {
                        if (!v.trim().isEmpty() && !v.trim().equals("\"\"")) {
                            hasValue = true;
                            break;
                        }
                    }
                    if (!hasValue) continue;
                    validLines.add(line);
                    int columns = values.length;
                    if (columns > maxColumns) maxColumns = columns;
                }
            }
            n = validLines.size();
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("temp.txt"), StandardCharsets.UTF_8))) {
                for (String line : validLines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            int[] parent = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;

            // 2. Итерации до сходимости
            int prevGroupCount = -1;
            while (true) {
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
                    // Объединяем все уникальные корни между собой
                    for (List<Integer> indices : valueToRows.values()) {
                        Set<Integer> roots = new HashSet<>();
                        for (int idx : indices) {
                            roots.add(find(parent, idx));
                        }
                        Integer first = null;
                        for (Integer root : roots) {
                            if (first == null) first = root;
                            else union(parent, first, root);
                        }
                    }
                    valueToRows = null;
                    System.gc();
                }
                // Path compression для всех parent[i]
                for (int i = 0; i < n; i++) {
                    parent[i] = find(parent, i);
                }
                // Подсчёт групп
                Set<Integer> roots = new HashSet<>();
                for (int i = 0; i < n; i++) roots.add(parent[i]);
                int groupCount = roots.size();
                if (groupCount == prevGroupCount) break;
                prevGroupCount = groupCount;
            }

            // 3. Группировка по корню
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

            // 4. Составляем список групп для вывода
            List<List<String>> outputGroups = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("temp.txt"), StandardCharsets.UTF_8))) {
                String line;
                int idx = 0;
                while ((line = reader.readLine()) != null) {
                    int root = parent[idx];
                    if (!groups.containsKey(root)) {
                        idx++;
                        continue;
                    }
                    groups.get(root).add(idx);
                    idx++;
                }
            }
            for (Iterator<Map.Entry<Integer, List<Integer>>> it = groups.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Integer, List<Integer>> entry = it.next();
                if (entry.getValue().size() <= 1) it.remove();
            }
            int groupCount = groups.size();
            System.out.println("Количество групп (более одного элемента): " + groupCount);
            System.out.println();

            try (PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
                 BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("temp.txt"), StandardCharsets.UTF_8))) {
                writer.println("Количество групп (более одного элемента): " + groupCount);
                writer.println();
                // Считываем все строки в список для быстрого доступа по индексу
                List<String> allLines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) allLines.add(line);
                int groupNum = 1;
                for (List<Integer> group : groups.values()) {
                    writer.println("Группа " + groupNum++);
                    for (int idx : group) {
                        writer.println(allLines.get(idx));
                    }
                    writer.println();
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Время выполнения: " + (endTime - startTime) + " мс");

            // 6. Удаляем временный файл
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