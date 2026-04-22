/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.relsparser;

import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

public final class RelsParser {
  private RelsParser() {
  }

  public static String normalizeRelationshipTarget(final String baseFilePath, final String targetPath, final String targetMode) {
    if (targetMode != null && "external".equalsIgnoreCase(targetMode)) {
      return targetPath;
    }
    return normalizeZipPath(baseFilePath, targetPath);
  }

  public static String normalizeZipPath(final String baseFilePath, final String targetPath) {
    final java.util.List<String> baseDirParts = new java.util.ArrayList<String>();
    final String[] baseParts = baseFilePath.split("/");
    for (int index = 0; index < baseParts.length - 1; index += 1) {
      baseDirParts.add(baseParts[index]);
    }
    final java.util.List<String> parts = targetPath.startsWith("/") ? new java.util.ArrayList<String>() : baseDirParts;
    for (final String part : targetPath.split("/")) {
      if (part.isEmpty() || ".".equals(part)) {
        continue;
      }
      if ("..".equals(part)) {
        if (!parts.isEmpty()) {
          parts.remove(parts.size() - 1);
        }
      } else {
        parts.add(part);
      }
    }
    return String.join("/", parts);
  }

  public static Map<String, RelationshipEntry> parseRelationshipEntries(
      final Map<String, byte[]> files,
      final String relsPath,
      final String sourcePath) {
    final byte[] relBytes = files.get(relsPath);
    final Map<String, RelationshipEntry> relations = new LinkedHashMap<String, RelationshipEntry>();
    if (relBytes == null) {
      return relations;
    }
    final Document doc = XmlUtils.xmlToDocument(XmlUtils.decodeXmlText(relBytes));
    for (final Element node : XmlUtils.getElementsByLocalName(doc, "Relationship")) {
      final String id = node.getAttribute("Id");
      final String target = node.getAttribute("Target");
      if (id == null || id.isEmpty() || target == null || target.isEmpty()) {
        continue;
      }
      final String targetMode = node.getAttribute("TargetMode");
      relations.put(id, new RelationshipEntry(
          normalizeRelationshipTarget(sourcePath, target, targetMode),
          targetMode == null ? "" : targetMode,
          node.getAttribute("Type") == null ? "" : node.getAttribute("Type")));
    }
    return relations;
  }

  public static Map<String, String> parseRelationships(
      final Map<String, byte[]> files,
      final String relsPath,
      final String sourcePath) {
    final Map<String, String> relations = new LinkedHashMap<String, String>();
    for (final Map.Entry<String, RelationshipEntry> entry : parseRelationshipEntries(files, relsPath, sourcePath).entrySet()) {
      relations.put(entry.getKey(), entry.getValue().getTarget());
    }
    return relations;
  }

  public static String buildRelsPath(final String sourcePath) {
    final String[] parts = sourcePath.split("/");
    final String fileName = parts.length == 0 ? "" : parts[parts.length - 1];
    final StringBuilder dir = new StringBuilder();
    for (int index = 0; index < parts.length - 1; index += 1) {
      if (index > 0) {
        dir.append('/');
      }
      dir.append(parts[index]);
    }
    return dir.toString() + "/_rels/" + fileName + ".rels";
  }

  public static final class RelationshipEntry {
    private final String target;
    private final String targetMode;
    private final String type;

    public RelationshipEntry(final String target, final String targetMode, final String type) {
      this.target = target;
      this.targetMode = targetMode;
      this.type = type;
    }

    public String getTarget() {
      return target;
    }

    public String getTargetMode() {
      return targetMode;
    }

    public String getType() {
      return type;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof RelationshipEntry)) {
        return false;
      }
      final RelationshipEntry that = (RelationshipEntry) other;
      return java.util.Objects.equals(target, that.target)
          && java.util.Objects.equals(targetMode, that.targetMode)
          && java.util.Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(target, targetMode, type);
    }
  }
}
