/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli

import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionGroup
import spock.lang.Specification
import spock.lang.Unroll

class RouteDescriptorSpec extends Specification {

  @Unroll
  def 'Fail to build: #expectedMessage'() {
    given:
    def builder = RouteDescriptor.builder(name).description(desc)

    when:
    builder.build()

    then:
    def e = thrown(expectedException)
    e.message == expectedMessage

    where:
    name  | desc  | expectedException        | expectedMessage
    null  | 'foo' | NullPointerException     | 'name is required'
    'foo' | null  | NullPointerException     | 'description is required'
    'foo' | 'foo' | IllegalArgumentException | 'Route must have at least one sub-command'
  }

  def 'Build minimal with defaults'() {
    given:
    def subCmd = Mock(Descriptor)
    def builder = minimalBuilder(subCmd)

    when:
    def descriptor = builder.build()

    then:
    descriptor.name == 'cmd1'
    descriptor.description == 'desc'
    descriptor.options.empty
    descriptor.optionGroups.empty
    descriptor.subCommands == [subCmd]
  }

  def 'Build full command descriptor'() {
    given:
    def opt1 = Option.builder('a').build()
    def opt2 = Option.builder('b').build()
    def optGroup1 = new OptionGroup()
    def subCmd1 = mockDescriptorWithName('sub-cmd1')
    def subCmd2 = mockDescriptorWithName('sub-cmd2')
    def subCmd3 = mockDescriptorWithName('sub-cmd3')
    def builder = minimalBuilder(subCmd1, subCmd2, subCmd3)
        .addOption(opt1).addOption(opt2)
        .addOptionGroup(optGroup1)

    when:
    def descriptor = builder.build()

    then:
    descriptor.name == 'cmd1'
    descriptor.description == 'desc'
    descriptor.options == [opt1, opt2]
    descriptor.optionGroups == [optGroup1]
    descriptor.subCommands == [subCmd1, subCmd2, subCmd3]
  }

  def 'Fail addOption(null)'() {
    given:
    def builder = minimalBuilder()

    when:
    builder.addOption(null)

    then:
    def e = thrown(NullPointerException)
    e.message == 'option is required'
  }

  def 'Fail addOptionGroup(null)'() {
    given:
    def builder = minimalBuilder()

    when:
    builder.addOptionGroup(null)

    then:
    def e = thrown(NullPointerException)
    e.message == 'group is required'
  }

  private Descriptor mockDescriptorWithName(String name) {
    Mock(Descriptor) {
      getName() >> name
    }
  }

  private static RouteDescriptor.Builder minimalBuilder(Descriptor... subCommands) {
    def builder = RouteDescriptor.builder('cmd1').description('desc')
    for (Descriptor descriptor in subCommands) {
      builder.addSubCommand(descriptor)
    }
    builder
  }
}
