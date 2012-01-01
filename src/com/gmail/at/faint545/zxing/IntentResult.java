/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.at.faint545.zxing;

/**
 * <p>Encapsulates the result of a barcode scan invoked through  {@link IntentIntegrator} .</p>
 * @author  Sean Owen
 */
public final class IntentResult {

  /**
 * @uml.property  name="contents"
 */
private final String contents;
  /**
 * @uml.property  name="formatName"
 */
private final String formatName;
  /**
 * @uml.property  name="rawBytes"
 */
private final byte[] rawBytes;
  /**
 * @uml.property  name="orientation"
 */
private final Integer orientation;
  /**
 * @uml.property  name="errorCorrectionLevel"
 */
private final String errorCorrectionLevel;

  IntentResult() {
    this(null, null, null, null, null);
  }

  IntentResult(String contents,
               String formatName,
               byte[] rawBytes,
               Integer orientation,
               String errorCorrectionLevel) {
    this.contents = contents;
    this.formatName = formatName;
    this.rawBytes = rawBytes;
    this.orientation = orientation;
    this.errorCorrectionLevel = errorCorrectionLevel;
  }

  /**
 * @return  raw content of barcode
 * @uml.property  name="contents"
 */
  public String getContents() {
    return contents;
  }

  /**
 * @return  name of format, like "QR_CODE", "UPC_A". See  {@code  BarcodeFormat}  for more format names.
 * @uml.property  name="formatName"
 */
  public String getFormatName() {
    return formatName;
  }

  /**
 * @return  raw bytes of the barcode content, if applicable, or null otherwise
 * @uml.property  name="rawBytes"
 */
  public byte[] getRawBytes() {
    return rawBytes;
  }

  /**
 * @return  rotation of the image, in degrees, which resulted in a successful scan. May be null.
 * @uml.property  name="orientation"
 */
  public Integer getOrientation() {
    return orientation;
  }

  /**
 * @return  name of the error correction level used in the barcode, if applicable
 * @uml.property  name="errorCorrectionLevel"
 */
  public String getErrorCorrectionLevel() {
    return errorCorrectionLevel;
  }
  
  @Override
  public String toString() {
    StringBuilder dialogText = new StringBuilder(100);
    dialogText.append("Format: ").append(formatName).append('\n');
    dialogText.append("Contents: ").append(contents).append('\n');
    int rawBytesLength = rawBytes == null ? 0 : rawBytes.length;
    dialogText.append("Raw bytes: (").append(rawBytesLength).append(" bytes)\n");
    dialogText.append("Orientation: ").append(orientation).append('\n');
    dialogText.append("EC level: ").append(errorCorrectionLevel).append('\n');
    return dialogText.toString();
  }

}