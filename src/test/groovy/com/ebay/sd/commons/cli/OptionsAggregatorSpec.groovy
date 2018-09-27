/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli

import org.apache.commons.cli.Option
import spock.lang.Specification
import spock.lang.Unroll

class OptionsAggregatorSpec extends Specification {

  private def aggregator = new OptionsAggregator()

  def 'aggregate from simple command with no options'() {
    given:
    def descriptor = commandBuilder().build()

    when:
    def options = aggregator.aggregate(descriptor)

    then:
    options.options.empty
  }

  def 'aggregate from simple command with options'() {
    given:
    def optA = Option.builder('a').longOpt('aaa').desc('aaa opt').required(true)
        .numberOfArgs(0).argName(null).optionalArg(false).type(String).valueSeparator((char) 0).build()
    def optB = Option.builder('b').longOpt('bbb').desc('bbb opt').required(false)
        .numberOfArgs(3).argName('BARG').optionalArg(true).type(Integer).valueSeparator((char) ',').build()
    def descriptor = commandBuilder()
        .addOption(optA)
        .addOption(optB)
        .build()

    when:
    def options = aggregator.aggregate(descriptor)

    then:
    options.options.toSet() == [optA, optB].toSet()
  }

  def 'aggregate from complex routing with various repeating options'() {
    given:
    def optA1 = Option.builder('a').longOpt('aaa').desc('aaa opt1').required(true)
        .numberOfArgs(0).argName(null).optionalArg(false).type(String).valueSeparator((char) 0).build()
    def optA2 = Option.builder('a').longOpt('aaa').desc('aaa opt2').required(true)
        .numberOfArgs(0).argName(null).optionalArg(false).type(String).valueSeparator((char) 0).build()
    def optB1 = Option.builder('b').longOpt('bbb').desc('bbb opt1').required(true)
        .numberOfArgs(3).argName('BARG1').optionalArg(true).type(Integer).valueSeparator((char) ',').build()
    def optB2 = Option.builder('b').longOpt('bbb').desc('bbb opt2').required(false)
        .numberOfArgs(3).argName('BARG2').optionalArg(true).type(Integer).valueSeparator((char) ',').build()
    def optC1 = Option.builder('c').desc('c opt1').build()
    def optC2 = Option.builder('c').desc('c opt2').build()
    def optD1 = Option.builder().longOpt('ddd').desc('ddd opt1').build()
    def optD2 = Option.builder().longOpt('ddd').desc('ddd opt2').build()
    def descriptor = routeBuilder('root')
        .addOption(optA1)
        .addOption(optB1)
        .addSubCommand(
            routeBuilder('sub1')
                .addOption(optA2)
                .addSubCommand(
                    commandBuilder('cmd1a')
                        .addOption(optC1)
                        .addOption(optD1)
                        .build())
                .build())
        .addSubCommand(
            routeBuilder('sub2')
                .addOption(optB2)
                .addSubCommand(
                    commandBuilder('cmd2a')
                        .addOption(optC2)
                        .addOption(optD2)
                        .build())
                .build())
        .build()

    when:
    def options = aggregator.aggregate(descriptor)

    then:
    options.options.size() == 4
    options.options.collect({ it.opt }).toSet() == ['a','b','c',null].toSet()
    options.options.collect({ it.longOpt }).toSet() == ['aaa','bbb','ddd',null].toSet()
  }

  @Unroll
  def 'fail aggregate repeating options with different functional settings: #errorMessage'() {
    given:
    def opt1 = Option.builder('a').longOpt('aaa').desc('aaa opt1').required(true)
        .numberOfArgs(0).argName(null).optionalArg(false).type(String).valueSeparator((char) 0).build()
    def opt2Builder = Option.builder(opt2name).longOpt('aaa').desc('aaa opt2').required(true)
        .numberOfArgs(0).argName(null).optionalArg(false).type(String).valueSeparator((char) 0)
    def opt2 = mutate(opt2Builder).build()
    def cmd = commandBuilder().addOption(opt1).addOption(opt2).build()

    when:
    aggregator.aggregate(cmd)

    then:
    def e = thrown(IllegalStateException)
    e.message.contains(errorMessage)

    where:
    errorMessage      | opt2name | mutate
    'short name'      | 'b'      | { Option.Builder b -> b }
    'short name'      | null     | { Option.Builder b -> b }
    'long name'       | 'a'      | { Option.Builder b -> b.longOpt('bbb') }
    'long name'       | 'a'      | { Option.Builder b -> b.longOpt(null) }
    'optional arg'    | 'a'      | { Option.Builder b -> b.optionalArg(true) }
    'value separator' | 'a'      | { Option.Builder b -> b.valueSeparator((char) ',') }
    'arg type'        | 'a'      | { Option.Builder b -> b.type(File) }
    'args number'     | 'a'      | { Option.Builder b -> b.numberOfArgs(10) }
  }

  private CommandDescriptor.Builder commandBuilder(String name = 'foo') {
    CommandDescriptor.builder(name)
        .description('')
        .factory(Mock(CommandFactory))
  }

  private RouteDescriptor.Builder routeBuilder(String name) {
    RouteDescriptor.builder(name)
        .description('')
  }
}
