/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.core;

import jp.igapyon.mikuxlsx2md.workbookloader.WorkbookLoader;

public final class Core {
  private Core() {
  }

  public static WorkbookLoader.ParsedWorkbook parseWorkbook(final byte[] workbookBytes, final String workbookName) {
    return WorkbookLoader.parseWorkbook(workbookBytes, workbookName);
  }
}
