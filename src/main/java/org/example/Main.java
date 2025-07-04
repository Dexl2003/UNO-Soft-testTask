package org.example;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        // Используем LinkedHashMap для уникальных строк и их индексов
        Map<String, Integer> rowToIndex = new LinkedHashMap<>();
        List<String[]> rows = new ArrayList<>();
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("lng.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!rowToIndex.containsKey(line)) {
                    String[] rowArr = line.split(";");
                    rowToIndex.put(line, rows.size());
                    rows.add(rowArr);
                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        int n = rows.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        // Карта: (столбец, значение) -> индексы строк
        Map<String, List<Integer>> valueToRows = new HashMap<>();
        for (int i = 0; i < n; i++) {
            String[] row = rows.get(i);
            for (int col = 0; col < row.length; col++) {
                String val = row[col].trim();
                if (!val.isEmpty() && !val.equals("\"\"")) {
                    String key = col + ":" + val;
                    valueToRows.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
                }
            }
        }

        // Union-Find объединение
        for (List<Integer> indices : valueToRows.values()) {
            for (int i = 1; i < indices.size(); i++) {
                union(parent, indices.get(0), indices.get(i));
            }
        }

        // Группировка по корню (индексы, не строки)
        Map<Integer, List<Integer>> groups = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = find(parent, i);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(i);
        }

        // Считаем количество групп с более чем одним элементом
        int groupCount = 0;
        for (List<Integer> group : groups.values()) {
            if (group.size() > 1) groupCount++;
        }
        System.out.println("Количество групп (более одного элемента): " + groupCount);
        System.out.println();

        // Запись в файл сразу, не накапливая группы в памяти
        try (PrintWriter writer = new PrintWriter("output.txt", "UTF-8")) {
            writer.println("Количество групп (более одного элемента): " + groupCount);
            writer.println();
            int fileGroupNum = 1;
            for (List<Integer> group : groups.values()) {
                if (group.size() > 1) {
                    writer.println("Группа " + fileGroupNum++);
                    for (int idx : group) {
                        writer.println(String.join(";", rows.get(idx)));
                    }
                    writer.println();
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Время выполнения: " + (endTime - startTime) + " мс");
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