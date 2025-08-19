package com.powergem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TableBuilder {
  private final List<String> header;
  private final List<List<String>> rows;

  public TableBuilder(String... header) {
    this.header = List.of(header);
    this.rows = new ArrayList<>();
  }

  public TableBuilder addRow(List<String> row) {
    if (row.isEmpty()) {
      throw new IllegalArgumentException("Specified row is empty.");
    }

    if (!this.rows.isEmpty() && this.rows.getFirst().size() != row.size()) {
      throw new IllegalArgumentException("Row size mismatch. Expected " + this.rows.getFirst().size() + ", got " + row.size());
    }

    this.rows.add(row);
    return this;
  }

  public TableBuilder addRow(String... cols) {
    List<String> list = Arrays.stream(cols).map(s -> s == null ? "" : s).toList();
    return addRow(list);
  }

  public void printTable() {
    if (this.rows.isEmpty()) {
      return;
    }

    int columns = this.rows.getFirst().size();
    int[] maxColumnWidths = this.header.isEmpty() ? new int[columns] : this.header.stream().mapToInt(String::length).toArray();

    for (List<String> row : this.rows) {
      for (int col = 0; col < columns; col++) {
        maxColumnWidths[col] = Math.max(maxColumnWidths[col], String.valueOf(row.get(col)).length());
      }
    }

    // Print header row centered
    if (!this.header.isEmpty()) {
      for (int col = 0; col < columns; col++) {
        String header = this.header.get(col);
        int padding = maxColumnWidths[col] - header.length();
        int leftPad = padding / 2;
        int rightPad = padding - leftPad;
        System.out.print(" ".repeat(leftPad));
        System.out.print(header);
        System.out.print(" ".repeat(rightPad));
        System.out.print(" | ");
      }
      System.out.println();

      // Print separator line
      for (int col = 0; col < columns; col++) {
        System.out.print("-".repeat(maxColumnWidths[col]));
        System.out.print("-|-");
      }
      System.out.println();
    }

    for (List<String> row : rows) {
      for (int col = 0; col < columns; col++) {
        String columnValue = row.get(col);
        System.out.print(columnValue);
        System.out.print(" ".repeat(maxColumnWidths[col] - columnValue.length()));
        System.out.print(" | ");
      }
      System.out.println();
    }
  }
}
