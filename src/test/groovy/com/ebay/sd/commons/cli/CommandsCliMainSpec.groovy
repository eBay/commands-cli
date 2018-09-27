/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli

import org.apache.commons.cli.Option
import org.apache.commons.cli.ParseException
import spock.lang.Specification
import spock.lang.Unroll

class CommandsCliMainSpec extends Specification {

  def 'Fail to build: rootDescriptor is required'() {
    given:
    def builder = CommandsCliMain.builder()

    when:
    builder.build()

    then:
    def e = thrown(NullPointerException)
    e.message == 'rootDescriptor is required'
  }

  @Unroll
  def 'Fail to build: rootDescriptor can be set only once (#test)'() {
    given:
    def builder = CommandsCliMain.builder()

    when:
    mutator1(builder)
    mutator2(builder)

    then:
    def e = thrown(IllegalStateException)
    e.message == 'rootDescriptor already set'

    where:
    test                | mutator1                                                                | mutator2
    'command > command' | { CommandsCliMain.Builder b -> b.mainCommand(Mock(CommandDescriptor)) } | { CommandsCliMain.Builder b -> b.mainCommand(Mock(CommandDescriptor)) }
    'command > route'   | { CommandsCliMain.Builder b -> b.mainCommand(Mock(CommandDescriptor)) } | { CommandsCliMain.Builder b -> b.mainRoute(Mock(RouteDescriptor)) }
    'route > command'   | { CommandsCliMain.Builder b -> b.mainRoute(Mock(RouteDescriptor)) }     | { CommandsCliMain.Builder b -> b.mainCommand(Mock(CommandDescriptor)) }
    'route > route'     | { CommandsCliMain.Builder b -> b.mainRoute(Mock(RouteDescriptor)) }     | { CommandsCliMain.Builder b -> b.mainRoute(Mock(RouteDescriptor)) }
  }

  @Unroll
  def 'Build full with command and context data #ctxData'() {
    given:
    def command = Mock(CommandDescriptor) {
      getOptions() >> options
      getOptionGroups() >> []
    }
    ctxData = noHelp(ctxData)
    def builder = CommandsCliMain.builder().mainCommand(command).contextData(ctxData)

    when:
    def main = builder.build()

    then:
    main.rootDescriptor == command
    main.options.options.toSet() == options.toSet()
    main.contextData == (ctxData ?: [:])

    where:
    options              | ctxData
    []                   | null
    [opt('a')]           | [a: 'A']
    [opt('a'), opt('b')] | [a: 'A', b: 'B']
  }

  private static Option opt(String opt) {
    Option.builder(opt).build()
  }

  def 'Execute simple command'() {
    given:
    def factory = new DummyCommandFactory()
    def root = CommandDescriptor.builder('foo')
        .description('desc')
        .factory(factory)
        .build()
    def main = CommandsCliMain.builder().mainCommand(root).build()

    when:
    main.execute([] as String[])

    then:
    def command = factory.command
    command.executed
  }

  def 'Execute command with options and arguments'() {
    given:
    def factory = new DummyCommandFactory()
    def root = CommandDescriptor.builder('foo')
        .description('desc')
        .addOption(Option.builder('a').hasArg().build())
        .addOption(Option.builder('b').build())
        .addArgument(Argument.builder('ARG1').description('').required().build())
        .addArgument(Argument.builder('ARG2').description('').required().multiplicity(2).build())
        .addArgument(Argument.builder('ARG3').description('').multiplicityUnlimited().build())
        .factory(factory)
        .build()
    def main = CommandsCliMain.builder().mainCommand(root).build()

    when:
    main.execute(['-a', 'A', '-b', 'arg1', 'arg2a', 'arg2b', 'arg3a', 'arg3b', 'arg3c'] as String[])

    then:
    def command = factory.command
    command.executed
    command.commandContext.commandLine.getOptionValue('a') == 'A'
    command.commandContext.commandLine.hasOption('a')
    command.commandContext.getArgumentValues('ARG1') == ['arg1']
    command.commandContext.getArgumentValues('ARG2') == ['arg2a', 'arg2b']
    command.commandContext.getArgumentValues('ARG3') == ['arg3a', 'arg3b', 'arg3c']
  }

  def 'Route 1 level and execute simple command'() {
    given:
    def factory = new DummyCommandFactory()
    def subCmd = CommandDescriptor.builder('bar')
        .description('desc')
        .factory(factory)
        .build()
    def root = RouteDescriptor.builder('foo')
        .description('')
        .addSubCommand(subCmd)
        .build()
    def main = CommandsCliMain.builder().mainRoute(root).build()

    when:
    main.execute(['bar'] as String[])

    then:
    def command = factory.command
    command.executed
  }

  def 'Route 1 level ad execute command with options and arguments'() {
    given:
    def factory = new DummyCommandFactory()
    def subCmd = CommandDescriptor.builder('bar')
        .description('desc')
        .addOption(Option.builder('a').hasArg().build())
        .addOption(Option.builder('b').build())
        .addArgument(Argument.builder('ARG1').description('').required().build())
        .addArgument(Argument.builder('ARG2').description('').required().multiplicity(2).build())
        .addArgument(Argument.builder('ARG3').description('').multiplicityUnlimited().build())
        .factory(factory)
        .build()
    def root = RouteDescriptor.builder('foo')
        .description('')
        .addSubCommand(subCmd)
        .build()
    def main = CommandsCliMain.builder().mainRoute(root).build()

    when:
    main.execute(['bar', '-a', 'A', '-b', 'arg1', 'arg2a', 'arg2b', 'arg3a', 'arg3b', 'arg3c'] as String[])

    then:
    def command = factory.command
    command.executed
    command.commandContext.commandLine.getOptionValue('a') == 'A'
    command.commandContext.commandLine.hasOption('a')
    command.commandContext.getArgumentValues('ARG1') == ['arg1']
    command.commandContext.getArgumentValues('ARG2') == ['arg2a', 'arg2b']
    command.commandContext.getArgumentValues('ARG3') == ['arg3a', 'arg3b', 'arg3c']
  }

  def 'Route 1 level and execute without command'() {
    given:
    def factory = new DummyCommandFactory()
    def subCmd = CommandDescriptor.builder('bar')
        .description('desc')
        .factory(factory)
        .build()
    def root = RouteDescriptor.builder('foo')
        .description('the foo command')
        .addSubCommand(subCmd)
        .build()
    def out = new StringWriter()
    def main = CommandsCliMain.builder()
        .mainRoute(root)
        .contextData([(UsageHelp.CTX_HELP_PRINT_WRITER):new PrintWriter(out)])
        .build()

    when:
    main.execute([] as String[])

    then:
    def e = thrown(ParseException)
    e.message == 'Command is required for route: foo <CMD>'
    out.toString() == ''

    when:
    main.execute(['-h'] as String[])

    then:
    noExceptionThrown()
    out.toString() == UsageHelpSpec.normalizedUsage("""usage: foo <CMD> [OPTIONS]

the foo command

Options:
 -h,--help   Show this help

Commands:
 bar   desc
""")
  }

  @Unroll
  def 'Two commands with similar options, but different required property'() {
    given:
    def main = createMainWithTwoCommandsWithSimilarOptionsDifferentRequired()
    def argList = [cmd]
    if (withOption) {
      argList << '-a'
    }

    when:
    main.execute(argList as String[])

    then:
    true

    where:
    cmd   | withOption
    'bar' | true
    'baz' | true
    'baz' | false
  }

  def 'Fail on missing option when two commands are defined with similar options, but different required property'() {
    given:
    def main = createMainWithTwoCommandsWithSimilarOptionsDifferentRequired()

    when:
    main.execute(['bar'] as String[])

    then:
    def e = thrown(ParseException)
    e.message == 'Missing required option: a'
  }

  private static CommandsCliMain createMainWithTwoCommandsWithSimilarOptionsDifferentRequired() {
    def barFactory = new DummyCommandFactory()
    def barCmd = CommandDescriptor.builder('bar')
        .description('desc')
        .addOption(Option.builder('a').required(true).build())
        .factory(barFactory)
        .build()
    def bazFactory = new DummyCommandFactory()
    def bazCmd = CommandDescriptor.builder('baz')
        .description('desc')
        .addOption(Option.builder('a').required(false).build())
        .factory(bazFactory)
        .build()
    def root = RouteDescriptor.builder('foo')
        .description('')
        .addSubCommand(barCmd)
        .addSubCommand(bazCmd)
        .build()
    CommandsCliMain.builder().mainRoute(root).build()
  }

  def 'Runtime exception in execute is wrapped with CommandException'() {
    given:
    def factory = Mock(CommandFactory) {
      create(_) >> Mock(Command) {
        execute() >> { throw new RuntimeException('dummy') }
      }
    }
    def descriptor = CommandDescriptor.builder('foo').description('').factory(factory).build()
    def main = CommandsCliMain.builder().mainCommand(descriptor).build()

    when:
    main.execute([] as String[])

    then:
    def e = thrown(CommandException)
    e.message == "UNEXPECTED ERROR: dummy"
  }

  def noHelp(Map data = [:]) {
    data = data ?: [:]
    data << [(UsageHelp.CTX_HELP_OPTION_AUTO_ADD):false]
    data
  }

  private static class DummyCommandFactory implements CommandFactory {

    DummyCommand command

    @Override
    Command create(CommandContext commandContext) throws ParseException {
      command = new DummyCommand(commandContext)
    }
  }

  private static class DummyCommand implements Command {

    CommandContext commandContext
    boolean executed

    DummyCommand(CommandContext commandContext) {
      this.commandContext = commandContext
    }

    @Override
    void execute() throws CommandException {
      executed = true
    }
  }

}
