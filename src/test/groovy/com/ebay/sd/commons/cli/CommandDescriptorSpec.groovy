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

class CommandDescriptorSpec extends Specification {

  @Unroll
  def 'Fail to build: #expectedMessage'() {
    given:
    def builder = CommandDescriptor.builder(name).description(desc).factory(factory)

    when:
    builder.build()

    then:
    def e = thrown(expectedException)
    e.message == expectedMessage

    where:
    name  | desc  | factory              | expectedException    | expectedMessage
    null  | 'foo' | Mock(CommandFactory) | NullPointerException | 'name is required'
    'foo' | null  | Mock(CommandFactory) | NullPointerException | 'description is required'
    'foo' | 'foo' | null                 | NullPointerException | 'factory is required'
  }

  def 'Build minimal with defaults'() {
    given:
    def builder = minimalBuilder()

    when:
    def descriptor = builder.build()

    then:
    descriptor.name == 'cmd1'
    descriptor.description == 'desc'
    descriptor.arguments.empty
    descriptor.options.empty
    descriptor.optionGroups.empty
  }

  def 'Build full command descriptor'() {
    given:
    def arg1 = Argument.builder('arg1').description('').required().build()
    def arg2 = Argument.builder('arg2').description('').required().multiplicity(2).build()
    def arg3 = Argument.builder('arg3').description('').multiplicityUnlimited().build()
    def opt1 = Option.builder('a').build()
    def opt2 = Option.builder('b').build()
    def optGroup1 = new OptionGroup()
    def builder = minimalBuilder()
        .addArgument(arg1).addArgument(arg2).addArgument(arg3)
        .addOption(opt1).addOption(opt2)
        .addOptionGroup(optGroup1)

    when:
    def descriptor = builder.build()

    then:
    descriptor.name == 'cmd1'
    descriptor.description == 'desc'
    descriptor.arguments == [arg1, arg2, arg3]
    descriptor.options == [opt1, opt2]
    descriptor.optionGroups == [optGroup1]
  }

  def 'Create command'() {
    given:
    def context = Mock(CommandContext)
    def command = Mock(Command)
    def factory = Mock(CommandFactory)
    def descriptor = minimalBuilder(factory).build()

    when:
    def cmd = descriptor.createCommand(context)

    then:
    1 * factory.create(context) >> command
    cmd == command
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

  def 'Fail addArgument(null)'() {
    given:
    def builder = minimalBuilder()

    when:
    builder.addArgument(null)

    then:
    def e = thrown(NullPointerException)
    e.message == 'argument is required'
  }

  def 'Fail addArgument with duplicate name'() {
    given:
    def builder = minimalBuilder().addArgument(Argument.builder('arg1').description('').build())

    when:
    builder.addArgument(Argument.builder('arg1').description('').build())

    then:
    def e = thrown(IllegalArgumentException)
    e.message == 'Argument \'arg1\' already exists for command \'cmd1\''
  }

  def 'Fail addArgument after an optional argument'() {
    given:
    def builder = minimalBuilder().addArgument(Argument.builder('arg1').description('').build())

    when:
    builder.addArgument(Argument.builder('arg2').description('').build())

    then:
    def e = thrown(IllegalArgumentException)
    e.message == 'Argument \'arg2\' cannot come after an optional argument \'arg1\' for command \'cmd1\''
  }

  def 'Fail addArgument after an unlimited argument'() {
    given:
    def builder = minimalBuilder().addArgument(Argument.builder('arg1').required().description('').multiplicityUnlimited().build())

    when:
    builder.addArgument(Argument.builder('arg2').description('').build())

    then:
    def e = thrown(IllegalArgumentException)
    e.message == 'Argument \'arg2\' cannot come after an argument \'arg1\' with unlimited values for command \'cmd1\''
  }

  private CommandDescriptor.Builder minimalBuilder(CommandFactory factory = null) {
    factory = factory ?: Mock(CommandFactory)
    CommandDescriptor.builder('cmd1').description('desc').factory(factory)
  }
}
