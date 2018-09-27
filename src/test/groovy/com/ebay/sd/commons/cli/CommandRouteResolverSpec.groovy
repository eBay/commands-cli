/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.ParseException
import spock.lang.Specification
import spock.lang.Unroll

class CommandRouteResolverSpec extends Specification {

  @Unroll
  def 'resolve main simple command, argList: #argList'() {
    given:
    def root = CommandDescriptor.builder('foo')
        .description('')
        .factory(Mock(CommandFactory))
        .addArgument(arg('A1', false))
        .build()
    def commandLine = Mock(CommandLine) {
      getArgList() >> argList
    }
    def resolver = new CommandRouteResolver(root)

    when:
    def route = resolver.resolve(commandLine, false)

    then:
    route.path.empty
    route.command == root
    route.fullPathAsString == 'foo'

    where:
    argList << [
        [],
        ['a'],
        ['a', 'b', 'c']
    ]
  }

  @Unroll
  def 'resolve 1 level routed simple command, argList: #argList'() {
    given:
    def cmd = CommandDescriptor.builder('bar')
        .description('')
        .factory(Mock(CommandFactory))
        .addArgument(arg('A1', false))
        .build()
    def root = RouteDescriptor.builder('foo').description('').addSubCommand(cmd).build()
    def commandLine = Mock(CommandLine) {
      getArgList() >> argList
    }
    def resolver = new CommandRouteResolver(root)

    when:
    def route = resolver.resolve(commandLine, false)

    then:
    route.path == [root]
    route.command == cmd
    route.fullPathAsString == 'foo bar'

    where:
    argList << [
        ['bar'],
        ['bar', 'a'],
        ['bar', 'a', 'b', 'c']
    ]
  }

  @Unroll
  def 'resolve 2 level routed simple command, argList: #argList'() {
    given:
    def cmd = CommandDescriptor.builder('baz')
        .description('')
        .factory(Mock(CommandFactory))
        .addArgument(arg('A1', false))
        .build()
    def route1 = RouteDescriptor.builder('bar').description('').addSubCommand(cmd).build()
    def root = RouteDescriptor.builder('foo').description('').addSubCommand(route1).build()
    def commandLine = Mock(CommandLine) {
      getArgList() >> argList
    }
    def resolver = new CommandRouteResolver(root)

    when:
    def route = resolver.resolve(commandLine, false)

    then:
    route.path == [root, route1]
    route.command == cmd
    route.fullPathAsString == 'foo bar baz'

    where:
    argList << [
        ['bar', 'baz'],
        ['bar', 'baz', 'a'],
        ['bar', 'baz', 'a', 'b', 'c']
    ]
  }

  def 'fail to resolve 1 level routed simple command - unknown command'() {
    given:
    def cmd = CommandDescriptor.builder('bar').description('').factory(Mock(CommandFactory)).build()
    def root = RouteDescriptor.builder('foo').description('').addSubCommand(cmd).build()
    def commandLine = Mock(CommandLine) {
      getArgList() >> ['baz']
    }
    def resolver = new CommandRouteResolver(root)

    when:
    resolver.resolve(commandLine, false)

    then:
    def e = thrown(ParseException)
    e.message == 'Unknown command: baz'
  }

  @Unroll
  def 'fail on missing required option (args: #args)'() {
    given:
    def barCmd = CommandDescriptor.builder('bar').description('')
        .addOption(Option.builder('o').required(true).build())
        .factory(Mock(CommandFactory))
        .build()
    def bazCmd = CommandDescriptor.builder('baz').description('')
        .addOption(Option.builder('o').required(false).build())
        .factory(Mock(CommandFactory))
        .build()
    def root = RouteDescriptor.builder('foo').description('')
        .addOption(Option.builder('o').required(true).build())
        .addSubCommand(barCmd)
        .addSubCommand(bazCmd)
        .build()

    when: 'command line has the option required option, then it is OK'
    def commandLineBuilder = new CommandLine.Builder().addOption(Option.builder('o').build())
    args.each { commandLineBuilder.addArg(it) }
    def commandLine = commandLineBuilder.build()
    def resolver = new CommandRouteResolver(root)
    resolver.resolve(commandLine, false)

    then:
    noExceptionThrown()

    when: 'command line does not have the required option, there is a parse exception on missing required option'
    commandLineBuilder = new CommandLine.Builder()
    args.each { commandLineBuilder.addArg(it) }
    commandLine = commandLineBuilder.build()
    resolver = new CommandRouteResolver(root)
    resolver.resolve(commandLine, false)

    then:
    def e = thrown(ParseException)
    e.message == 'Missing required option: o'

    where:
    args << [
        [],
        ['bar']
    ]
  }

  def 'resolve 1 level routed simple command - command missing'() {
    given:
    def cmd = CommandDescriptor.builder('bar').description('').factory(Mock(CommandFactory)).build()
    def root = RouteDescriptor.builder('foo').description('').addSubCommand(cmd).build()
    def commandLine = Mock(CommandLine) {
      getArgList() >> []
    }
    def resolver = new CommandRouteResolver(root)

    when:
    def route = resolver.resolve(commandLine, false)

    then:
    !route.hasCommand()
    route.path == [root]
  }

  @Unroll
  def 'fail to resolve 2 level routed simple command - unknown command, argList: #argList'() {
    given:
    def cmd = CommandDescriptor.builder('baz').description('').factory(Mock(CommandFactory)).build()
    def route1 = RouteDescriptor.builder('bar').description('').addSubCommand(cmd).build()
    def root = RouteDescriptor.builder('foo').description('').addSubCommand(route1).build()
    def commandLine = Mock(CommandLine) {
      getArgList() >> argList
    }
    def resolver = new CommandRouteResolver(root)

    when:
    resolver.resolve(commandLine, false)

    then:
    def e = thrown(ParseException)
    e.message == 'Unknown command: coo'

    where:
    argList        | _
    ['coo']        | _
    ['bar', 'coo'] | _
  }

  @Unroll
  def 'resolve 2 level routed simple command - command missing, argList: #argList'() {
    given:
    def cmd = CommandDescriptor.builder('baz').description('').factory(Mock(CommandFactory)).build()
    def route1 = RouteDescriptor.builder('bar').description('').addSubCommand(cmd).build()
    def root = RouteDescriptor.builder('foo').description('').addSubCommand(route1).build()
    def commandLine = Mock(CommandLine) {
      getArgList() >> argList
    }
    def resolver = new CommandRouteResolver(root)

    when:
    def commandRoute = resolver.resolve(commandLine, false)

    then:
    !commandRoute.hasCommand()
    commandRoute.path == (argList.empty ? [root] : [root, route1])

    where:
    argList | route
    []      | 'foo'
    ['bar'] | 'bar'
  }

  @Unroll
  def 'resolve main command with arguments, args: #args'() {
    given:
    def cmdBuilder = CommandDescriptor.builder('foo')
        .description('')
        .factory(Mock(CommandFactory))
    def argList = []
    for (arg in args) {
      cmdBuilder.addArgument(arg[0])
      argList.addAll(arg[1])
    }
    def root = cmdBuilder.build()
    def commandLine = Mock(CommandLine) {
      getArgList() >> argList
    }
    def resolver = new CommandRouteResolver(root)

    when:
    def route = resolver.resolve(commandLine, false)

    then:
    route.path.empty
    route.command == root
    route.fullPathAsString == 'foo'
    assertResolvedCommandArguments(route, args)

    where:
    args << [
        [],
        [[arg('A1', true, 1), ['a']]],
        [[arg('A1', true, 1), ['a']], [arg('A2', true, 2), ['b', 'c']]],
        [[arg('A1', true, 1), ['a']], [arg('A2', true), ['b']]],
        [[arg('A1', true, 1), ['a']], [arg('A2', true), ['b', 'c']]],
        [[arg('A1', false, 1), ['a']]],
        [[arg('A1', false, 1), []]],
        [[arg('A1', false), []]]
    ]
  }

  private static boolean assertResolvedCommandArguments(CommandRoute route, List args) {
    route.command.arguments.size() == args.size()
    for (int i = 0; i < route.command.arguments.size(); i++) {
      def arg = route.command.arguments[i]
      def expectedArg = args[i][0]
      def expectedValues = args[i][1]
      assert arg == expectedArg
      assert arg.values == expectedValues
    }
    true
  }

  @Unroll
  def 'fail resolve main command with arguments, args: #args, expectedMessage: #expectedMessage'() {
    given:
    def cmdBuilder = CommandDescriptor.builder('foo')
        .description('')
        .factory(Mock(CommandFactory))
    def argList = []
    for (arg in args) {
      cmdBuilder.addArgument(arg[0])
      argList.addAll(arg[1])
    }
    def root = cmdBuilder.build()
    def commandLine = Mock(CommandLine) {
      getArgList() >> argList
    }
    def resolver = new CommandRouteResolver(root)

    when:
    resolver.resolve(commandLine, false)

    then:
    def e = thrown(ParseException)
    e.message == expectedMessage

    where:
    expectedMessage                                 | args
    'Argument is required: A1'                      | [[arg('A1', true, 1), []]]
    'Argument is required: A1'                      | [[arg('A1', true, 3), []]]
    'Argument is required: A1'                      | [[arg('A1', true), []]]
    'There is at least one unhandled argument: b'   | [[arg('A1', true, 1), ['a', 'b']]]
    'There is at least one unhandled argument: c'   | [[arg('A1', true, 2), ['a', 'b', 'c']]]
    'Argument has too few values: A1 (expected: 2)' | [[arg('A1', true, 2), ['a']]]
  }

  def 'don\'t fail when argument is missing and skip argument parsing is on'() {
    given:
    def root = CommandDescriptor.builder('foo')
        .description('')
        .factory(Mock(CommandFactory))
        .addArgument(arg('A1', true, 1))
        .build()
    def commandLine = Mock(CommandLine) {
      getArgList() >> []
    }
    def resolver = new CommandRouteResolver(root)

    when:
    def route = resolver.resolve(commandLine, true)

    then:
    route.command == root

    when:
    resolver.resolve(commandLine, false)

    then:
    def e = thrown(ParseException)
    e.message == 'Argument is required: A1'
  }

  def 'fail to resolve with unexpected descriptor type'() {
    given:
    def resolver = new CommandRouteResolver(Mock(Descriptor))

    when:
    resolver.resolve(Mock(CommandLine), false)

    then:
    def e = thrown(IllegalStateException)
    e.message.startsWith('Unexpected descriptor type: ')
  }

  def 'fail construct resolver with null root descriptor'() {
    when:
    new CommandRouteResolver(null)

    then:
    def e = thrown(NullPointerException)
    e.message == 'root descriptor is required'
  }

  def 'fail to resolve with null command line'() {
    given:
    def resolver = new CommandRouteResolver(Mock(Descriptor))

    when:
    resolver.resolve(null, false)

    then:
    def e = thrown(NullPointerException)
    e.message == 'commandLine is required'
  }

  private static Argument arg(String name, boolean required, int multiplicity = Argument.UNLIMITED_VALUES) {
    Argument.builder(name).description('').required(required).multiplicity(multiplicity).build()
  }
}
