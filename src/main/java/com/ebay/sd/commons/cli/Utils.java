/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;

/**
 * Simple internal utilities
 */
abstract class Utils {

  private Utils() {
    //Utility class
  }

  /**
   * Join a collection of strings using a given separator
   * @param strings the strings to join (must be not null)
   * @param separator the separator to use
   * @return the resulted joined string, or an empty string if the strings collection is empty
   */
  static String join(Collection<String> strings, char separator) {
    requireNonNull(strings, "strings to join are required");
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (String str : strings) {
      if (first) {
        first = false;
      } else {
        sb.append(separator);
      }
      sb.append(str);
    }
    return sb.toString();
  }

  /**
   * Add padding to a given string.
   * <p>
   *   Adds left padding and fills right padding to the required length.
   * </p>
   * @param str the string to pad
   * @param padLeft the number of padding characters to add on the left had side
   * @param length the expected total length of the resulted string
   * @return the padded string
   */
  static String pad(String str, int padLeft, int length) {
    requireNonNull(str, "string to pad is required");
    requireNonNegative(padLeft, "leftPad must be non-negative");
    requireNonNegative(length, "length must be non-negative");
    if (str.length() + padLeft > length) {
      throw new IllegalArgumentException("String to be padded does not fit in the expected length");
    }
    char[] chars = new char[length];
    Arrays.fill(chars, 0, padLeft, ' ');
    System.arraycopy(str.toCharArray(), 0, chars, padLeft, str.length());
    Arrays.fill(chars, str.length() + padLeft, length, ' ');
    return String.valueOf(chars);
  }

  static int requireNonNegative(int value, String message) {
    if (value < 0) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

}
