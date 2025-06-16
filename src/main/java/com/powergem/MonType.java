package com.powergem;

import java.util.Optional;

public enum MonType {
  LINE(1),
  TRANSFORMER(2);

  private final int monType;

  MonType(int monType) {
    this.monType = monType;
  }

  public int getMonType() {
    return monType;
  }

  public static Optional<MonType> getMonType(int monType) {
    return switch (monType) {
      case 1 -> Optional.of(LINE);
      case 2 -> Optional.of(TRANSFORMER);
      default -> Optional.empty();
    };
  }
}
