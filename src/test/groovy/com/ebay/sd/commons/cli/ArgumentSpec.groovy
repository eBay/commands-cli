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

class ArgumentSpec extends Specification {

  @Unroll
  def 'Fail to build: #expectedMessage'() {
    given:
    def builder = Argument.builder(name).description(desc).multiplicity(multiplicity)

    when:
    builder.build()

    then:
    def e = thrown(expectedException)
    e.message == expectedMessage

    where:
    name  | desc  | multiplicity | expectedException        | expectedMessage
    null  | 'foo' | 1            | NullPointerException     | 'name is required'
    'foo' | null  | 1            | NullPointerException     | 'description is required'
    'foo' | 'foo' | 0            | IllegalArgumentException | 'Illegal multiplicity, must be positive: 0'
    'foo' | 'foo' | -1           | IllegalArgumentException | 'Illegal multiplicity, must be positive: -1'
    'foo' | 'foo' | -3           | IllegalArgumentException | 'Illegal multiplicity, must be positive: -3'
  }

  def 'Build minimal with defaults'() {
    when:
    def argument = minimalBuilder().build()

    then:
    argument.name == 'arg1'
    argument.description == 'desc1'
    argument.required == false
    argument.multiplicity == 1
  }

  @Unroll
  def 'Builder full with: .required(#required).multiplicity(#multiplicity)'() {
    given:
    def builder = minimalBuilder().required(required).multiplicity(multiplicity)

    when:
    def argument = builder.build()

    then:
    argument.name == 'arg1'
    argument.description == 'desc1'
    argument.required == required
    argument.multiplicity == multiplicity

    where:
    required | multiplicity
    true     | 1
    true     | 10
    false    | 10
    false    | 1
  }

  def 'Builder with: .required()'() {
    when:
    def argument = minimalBuilder().required().build()

    then:
    argument.required
  }

  def 'Builder with: .multiplicityUnlimited()'() {
    when:
    def argument = minimalBuilder().multiplicityUnlimited().build()

    then:
    argument.multiplicity == Argument.UNLIMITED_VALUES
  }

  def 'Values'() {
    given:
    def argument = minimalBuilder().build();

    expect:
    argument.getValue() == null
    argument.getValues() == []
    argument.getValue('def') == 'def'

    when:
    argument.addValue('a')

    then:
    argument.getValue() == 'a'
    argument.getValues() == ['a']
    argument.getValue('def') == 'a'

    when:
    argument.addValue('b')

    then:
    argument.getValue() == 'a'
    argument.getValues() == ['a', 'b']
    argument.getValue('def') == 'a'
  }

  private static Argument.Builder minimalBuilder() {
    Argument.builder('arg1').description('desc1')
  }
}
