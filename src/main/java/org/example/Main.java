package org.example;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Set<List<String>> uniqueRows = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/lng.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> row = Arrays.asList(line.split(";"));
                uniqueRows.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<List<String>> rows = new ArrayList<>(uniqueRows);
        int n = rows.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        // Карта: (столбец, значение) -> индексы строк
        Map<String, List<Integer>> valueToRows = new HashMap<>();
        for (int i = 0; i < n; i++) {
            List<String> row = rows.get(i);
            for (int col = 0; col < row.size(); col++) {
                String val = row.get(col).trim();
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

        // Группировка по корню
        Map<Integer, List<List<String>>> groups = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = find(parent, i);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(rows.get(i));
        }

        // Оставляем только группы с более чем одним элементом
        List<List<List<String>>> filteredGroups = new ArrayList<>();
        for (List<List<String>> group : groups.values()) {
            if (group.size() > 1) {
                filteredGroups.add(group);
            }
        }
        // Сортируем по убыванию размера
        filteredGroups.sort((a, b) -> Integer.compare(b.size(), a.size()));

        // Вывод количества групп
        System.out.println("Количество групп (более одного элемента): " + filteredGroups.size());
        System.out.println();

        // Запись в файл
        try (PrintWriter writer = new PrintWriter("output.txt", "UTF-8")) {
            writer.println("Количество групп (более одного элемента): " + filteredGroups.size());
            writer.println();
            int fileGroupNum = 1;
            for (List<List<String>> group : filteredGroups) {
                writer.println("Группа " + fileGroupNum++);
                for (List<String> row : group) {
                    writer.println(String.join(";", row));
                }
                writer.println();
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