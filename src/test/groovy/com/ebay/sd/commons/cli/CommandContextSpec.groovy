/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli

import org.apache.commons.cli.CommandLine
import spock.lang.Specification
import spock.lang.Unroll

class CommandContextSpec extends Specification {

  @Unroll
  def 'Constructor required params: #expectedMessage'() {
    when:
    new CommandContext(commandLine, route, data)

    then:
    def e = thrown(expectedException)
    e.message == expectedMessage

    where:
    commandLine       | route              | data                   | expectedException    | expectedMessage
    null              | Mock(CommandRoute) | Collections.emptyMap() | NullPointerException | 'commandLine is required'
    Mock(CommandLine) | null               | Collections.emptyMap() | NullPointerException | 'commandRoute is required'
  }

  def 'Constructor with null data'() {
    given:
    def cmdLine = Mock(CommandLine)
    def route = Mock(CommandRoute)

    when:
    def ctx = new CommandContext(cmdLine, route, null)

    then:
    ctx.commandLine == cmdLine
    ctx.commandRoute == route
    ctx.data.isEmpty()
  }

  def 'Constructor with provided data'() {
    given:
    def cmdLine = Mock(CommandLine)
    def route = Mock(CommandRoute)
    def data = [a: 'A', b: 2, c: false]

    when:
    def ctx = new CommandContext(cmdLine, route, data)

    then:
    ctx.commandLine == cmdLine
    ctx.commandRoute == route
    ctx.data == data
    ctx.getValue('a') == 'A'
    ctx.getValue('b') == 2
    ctx.getValue('c') == false

    when:
    data.d = 'ignored'

    then:
    ctx.getValue('d') == null

    when:
    ctx.putValue('d', 'foo')

    then:
    data.d == 'ignored'
  }

  def 'getRequiredValue()'() {
    given:
    def ctx = new CommandContext(Mock(CommandLine), Mock(CommandRoute), [a: 'A'])

    expect:
    ctx.getRequiredValue('a') == 'A'

    when:
    ctx.getRequiredValue('b')

    then:
    def e = thrown(NullPointerException)
    e.message == 'b is required'
  }

  def 'getValue(key, defaultValue)'() {
    given:
    def ctx = new CommandContext(Mock(CommandLine), Mock(CommandRoute), [a: 'A'])

    expect:
    ctx.getValue('a', 'B') == 'A'
    ctx.getValue('b', 'B') == 'B'
  }

  def 'getArgumentValue()'() {
    given:
    def arg1 = Argument.builder('ARG1').description('').build()
    arg1.addValue('arg1')
    def arg2 = Argument.builder('ARG2').description('').build()
    arg2.addValue('arg2a')
    arg2.addValue('arg2b')
    def arg3 = Argument.builder('ARG3').description('').build()
    def descriptor = Mock(CommandDescriptor) {
      getArguments() >> [arg1, arg2, arg3]
    }
    def route = CommandRoute.builder().command(descriptor).build()
    def ctx = new CommandContext(Mock(CommandLine), route, [:])

    expect:
    ctx.getArgumentValue('ARG1') == 'arg1'
    ctx.getArgumentValue('ARG2') == 'arg2a'
    ctx.getArgumentValue('ARG3') == null
    ctx.getArgumentValue('ARG3', 'foo') == 'foo'

    when:
    ctx.getArgumentValue('MISSING')
    then:
    def e = thrown(IllegalStateException)
    e.message == 'Argument not found: MISSING'

    when:
    ctx.getArgumentValue('MISSING', 'default')
    then:
    e = thrown(IllegalStateException)
    e.message == 'Argument not found: MISSING'
  }

  def 'getArgumentValues()'() {
    given:
    def arg1 = Argument.builder('ARG1').description('').build()
    arg1.addValue('arg1')
    def arg2 = Argument.builder('ARG2').description('').build()
    arg2.addValue('arg2a')
    arg2.addValue('arg2b')
    def arg3 = Argument.builder('ARG3').description('').build()
    def descriptor = Mock(CommandDescriptor) {
      getArguments() >> [arg1, arg2, arg3]
    }
    def route = CommandRoute.builder().command(descriptor).build()
    def ctx = new CommandContext(Mock(CommandLine), route, [:])

    expect:
    ctx.getArgumentValues('ARG1') == ['arg1']
    ctx.getArgumentValues('ARG2') == ['arg2a', 'arg2b']
    ctx.getArgumentValues('ARG3') == []

    when:
    ctx.getArgumentValues('MISSING')
    then:
    def e = thrown(IllegalStateException)
    e.message == 'Argument not found: MISSING'
  }

  def 'getRequiredArgumentValue()'() {
    given:
    def arg1 = Argument.builder('ARG1').description('').build()
    arg1.addValue('arg1')
    def arg2 = Argument.builder('ARG2').description('').build()
    arg2.addValue('arg2a')
    arg2.addValue('arg2b')
    def arg3 = Argument.builder('ARG3').description('').build()
    def descriptor = Mock(CommandDescriptor) {
      getArguments() >> [arg1, arg2, arg3]
    }
    def route = CommandRoute.builder().command(descriptor).build()
    def ctx = new CommandContext(Mock(CommandLine), route, [:])

    expect:
    ctx.getRequiredArgumentValue('ARG1') == 'arg1'
    ctx.getRequiredArgumentValue('ARG2') == 'arg2a'

    when:
    ctx.getRequiredArgumentValue('ARG3')
    then:
    def e = thrown(NullPointerException)
    e.message == 'ARG3 is required'

    when:
    ctx.getRequiredArgumentValue('MISSING')
    then:
    e = thrown(IllegalStateException)
    e.message == 'Argument not found: MISSING'
  }
}
