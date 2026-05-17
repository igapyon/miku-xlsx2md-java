/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.markdowntableescape;

import jp.igapyon.mikuxlsx2md.markdownnormalize.MarkdownNormalize;

public final class MarkdownTableEscape {
  private MarkdownTableEscape() {
  }

  public static String escapeMarkdownTableCell(final String text) {
    return MarkdownNormalize.normalizeMarkdownTableCell(text);
  }
}
