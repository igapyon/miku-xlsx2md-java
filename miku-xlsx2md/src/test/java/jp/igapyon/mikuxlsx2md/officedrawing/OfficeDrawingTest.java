/*
 * Copyright 2026 Toshiki Iga
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.igapyon.mikuxlsx2md.officedrawing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.igapyon.mikuxlsx2md.xmlutils.XmlUtils;

class OfficeDrawingTest {
  @Test
  void rendersTextboxShapesAsSvgAssetsWithSanitizedSheetDirectories() {
    final Document doc = XmlUtils.xmlToDocument(
        "<root xmlns:xdr=\"xdr\" xmlns:a=\"a\">"
            + "<xdr:twoCellAnchor>"
            + "<xdr:ext cx=\"1905000\" cy=\"476250\"/>"
            + "<xdr:sp>"
            + "<xdr:nvSpPr><xdr:cNvSpPr txBox=\"1\"/></xdr:nvSpPr>"
            + "<xdr:spPr>"
            + "<a:solidFill><a:srgbClr val=\"FF0000\"/></a:solidFill>"
            + "<a:ln w=\"9525\"><a:solidFill><a:srgbClr val=\"00FF00\"/></a:solidFill></a:ln>"
            + "</xdr:spPr>"
            + "<xdr:txBody><a:p><a:r><a:t>Hello</a:t></a:r></a:p></xdr:txBody>"
            + "</xdr:sp>"
            + "</xdr:twoCellAnchor>"
            + "</root>");
    final Element anchor = XmlUtils.getElementsByLocalName(doc, "twoCellAnchor").get(0);
    final Element shape = XmlUtils.getElementsByLocalName(doc, "sp").get(0);

    final OfficeDrawing.SvgRenderResult result = OfficeDrawing.renderShapeSvg(shape, anchor, "A/B:Sheet", 1);
    final String svgText = new String(result.getData(), StandardCharsets.UTF_8);

    assertEquals("shape_001.svg", result.getFilename());
    assertEquals("assets/A_B_Sheet/shape_001.svg", result.getPath());
    assertTrue(svgText.contains("<svg"));
    assertTrue(svgText.contains("Hello"));
    assertTrue(svgText.contains("#FF0000"));
    assertTrue(svgText.contains("#00FF00"));
  }

  @Test
  void rendersConnectorShapesWithArrowMarkers() {
    final Document doc = XmlUtils.xmlToDocument(
        "<root xmlns:xdr=\"xdr\" xmlns:a=\"a\">"
            + "<xdr:twoCellAnchor>"
            + "<xdr:ext cx=\"2857500\" cy=\"190500\"/>"
            + "<xdr:cxnSp>"
            + "<xdr:spPr><a:ln w=\"19050\"><a:solidFill><a:srgbClr val=\"123456\"/></a:solidFill></a:ln></xdr:spPr>"
            + "</xdr:cxnSp>"
            + "</xdr:twoCellAnchor>"
            + "</root>");
    final Element anchor = XmlUtils.getElementsByLocalName(doc, "twoCellAnchor").get(0);
    final Element shape = XmlUtils.getElementsByLocalName(doc, "cxnSp").get(0);

    final OfficeDrawing.SvgRenderResult result = OfficeDrawing.renderShapeSvg(shape, anchor, "Sheet1", 2);
    final String svgText = new String(result.getData(), StandardCharsets.UTF_8);

    assertEquals("assets/Sheet1/shape_002.svg", result.getPath());
    assertTrue(svgText.contains("<marker"));
    assertTrue(svgText.contains("marker-end=\"url(#arrow)\""));
    assertTrue(svgText.contains("#123456"));
  }

  @Test
  void returnsNullForUnsupportedShapeKinds() {
    final Document doc = XmlUtils.xmlToDocument(
        "<root xmlns:xdr=\"xdr\" xmlns:a=\"a\">"
            + "<xdr:twoCellAnchor>"
            + "<xdr:sp><xdr:spPr><a:prstGeom prst=\"ellipse\"/></xdr:spPr></xdr:sp>"
            + "</xdr:twoCellAnchor>"
            + "</root>");
    final Element anchor = XmlUtils.getElementsByLocalName(doc, "twoCellAnchor").get(0);
    final Element shape = XmlUtils.getElementsByLocalName(doc, "sp").get(0);

    assertNull(OfficeDrawing.renderShapeSvg(shape, anchor, "Sheet1", 3));
  }
}
