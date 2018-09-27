/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli

import spock.lang.Specification
import spock.lang.Unroll

class UtilsSpec extends Specification {

  @Unroll
  def 'join : #expected'() {
    expect:
    Utils.join(strings, sep as char) == expected

    where:
    strings         | sep | expected
    []              | ' ' | ''
    ['a']           | ' ' | 'a'
    ['a', 'b']      | ' ' | 'a b'
    ['a', 'b']      | ',' | 'a,b'
    ['a', 'b', 'c'] | '|' | 'a|b|c'
  }

  def 'join fails on null strings'() {
    when:
    Utils.join(null, ',' as char)

    then:
    def e = thrown(NullPointerException)
    e.message == 'strings to join are required'
  }

  @Unroll
  def 'pad : "#expected"'() {
    expect:
    Utils.pad(str, leftPad, length) == expected

    where:
    str           | leftPad | length | expected
    ''            | 0       | 0      | ''
    ''            | 0       | 1      | ' '
    ''            | 0       | 5      | '     '
    ''            | 3       | 5      | '     '
    'a'           | 0       | 1      | 'a'
    'a'           | 0       | 3      | 'a  '
    'a'           | 1       | 3      | ' a '
    'a'           | 2       | 3      | '  a'
    'abcd'        | 0       | 4      | 'abcd'
    'foo bar baz' | 0       | 11     | 'foo bar baz'
    'foo bar baz' | 0       | 14     | 'foo bar baz   '
    'foo bar baz' | 1       | 14     | ' foo bar baz  '
  }

  @Unroll
  def 'fail to pad (str="#str", leftPad=#leftPad, length=#length): #expectedMessage'() {
    when:
    Utils.pad(str, leftPad, length)

    then:
    def e = thrown(expectedException)
    e.message == expectedMessage

    where:
    str    | leftPad | length | expectedException        | expectedMessage
    'a'    | 0       | 0      | IllegalArgumentException | 'String to be padded does not fit in the expected length'
    'a'    | 1       | 1      | IllegalArgumentException | 'String to be padded does not fit in the expected length'
    'abcd' | 0       | 0      | IllegalArgumentException | 'String to be padded does not fit in the expected length'
    'abcd' | 1       | 4      | IllegalArgumentException | 'String to be padded does not fit in the expected length'
    'ab'   | 3       | 4      | IllegalArgumentException | 'String to be padded does not fit in the expected length'
    'ab'   | -1      | 4      | IllegalArgumentException | 'leftPad must be non-negative'
    'ab'   | 0       | -1     | IllegalArgumentException | 'length must be non-negative'
    null   | 0       | 4      | NullPointerException     | 'string to pad is required'
  }
}
