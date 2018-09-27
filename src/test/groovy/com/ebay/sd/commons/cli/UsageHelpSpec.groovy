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
import spock.lang.Specification

class UsageHelpSpec extends Specification {

  def 'Simple minimal command'() {
    given:
    CommandRoute route = CommandRoute.builder()
        .command(CommandDescriptor.builder("foo")
            .description("The foo command")
            .factory(Mock(CommandFactory))
            .build())
        .build()

    expect:
    printUsage(route, [(UsageHelp.CTX_HELP_OPTION_AUTO_ADD):false]) == normalizedUsage("""usage: foo

The foo command

""")
  }

  def 'Simple command with options and arguments'() {
    given:
    CommandRoute route = CommandRoute.builder()
        .command(CommandDescriptor.builder("foo")
            .description("The foo command")
            .factory(Mock(CommandFactory))
            .addOption(Option.builder('a').desc('a option').build())
            .addOption(Option.builder('b').longOpt('bbb').desc('bbb option').build())
            .addOption(Option.builder('c').longOpt('ccc').hasArg().argName('CCC').desc('ccc option with argument').build())
            .addArgument(Argument.builder('ARG1').required().description('The first argument. This is a very long description to make the line break. The goal is to see line wrapping.').build())
            .addArgument(Argument.builder('ARG2').required().multiplicity(3).description('The second argument, a required argument with exactly 3 values').build())
            .addArgument(Argument.builder('ARGUMENT3').multiplicityUnlimited().description('The third argument, an optional argument with unlimited values').build())
            .build())
        .build()

    expect:
    printUsage(route) == normalizedUsage("""usage: foo [OPTIONS] <ARG1> <ARG2>{3} [<ARGUMENT3>...]

The foo command

Options:
 -a               a option
 -b,--bbb         bbb option
 -c,--ccc <CCC>   ccc option with argument
 -h,--help        Show this help

Arguments:
 ARG1        The first argument. This is a very long description to make
             the line break. The goal is to see line wrapping.
 ARG2        The second argument, a required argument with exactly 3
             values
 ARGUMENT3   The third argument, an optional argument with unlimited
             values
""")
  }

  def 'Level 1 command'() {
    given:
    def cmd = CommandDescriptor.builder("foo")
        .description("The foo command")
        .factory(Mock(CommandFactory))
        .addArgument(Argument.builder('FOO').description('foo argument').build())
        .addOption(Option.builder('f').desc('foo option').build())
        .build()
    def route1 = RouteDescriptor.builder("route1")
        .description('first route')
        .addOption(Option.builder('r').build())
        .addSubCommand(cmd)
        .build()
    CommandRoute commandRoute = CommandRoute.builder()
        .addToPath(route1)
        .command(cmd)
        .build()

    expect:
    printUsage(commandRoute) == normalizedUsage("""usage: route1 foo [OPTIONS] [<FOO>]

The foo command

Options:
 -f          foo option
 -h,--help   Show this help

Arguments:
 FOO   foo argument
""")

    when: 'no command in the command route'
    commandRoute = CommandRoute.builder()
        .addToPath(route1)
        .build()

    then:
    printUsage(commandRoute) == normalizedUsage("""usage: route1 <CMD> [OPTIONS]

first route

Options:
 -h,--help   Show this help
 -r

Commands:
 foo   The foo command
""")
  }

  def 'Level 2 command'() {
    given:
    def cmd = CommandDescriptor.builder("foo")
        .description("The foo command")
        .factory(Mock(CommandFactory))
        .addArgument(Argument.builder('FOO').description('foo argument').build())
        .addOption(Option.builder('f').desc('foo option').build())
        .build()
    def route2 = RouteDescriptor.builder("route2")
        .description('second route')
        .addOption(Option.builder('s').build())
        .addSubCommand(cmd)
        .build()
    def route1 = RouteDescriptor.builder("route1")
        .description('first route')
        .addOption(Option.builder('r').build())
        .addSubCommand(route2)
        .build()
    CommandRoute commandRoute = CommandRoute.builder()
        .addToPath(route1)
        .addToPath(route2)
        .command(cmd)
        .build()

    expect:
    printUsage(commandRoute) == normalizedUsage("""usage: route1 route2 foo [OPTIONS] [<FOO>]

The foo command

Options:
 -f          foo option
 -h,--help   Show this help

Arguments:
 FOO   foo argument
""")

    when: 'no command in the command route'
    commandRoute = CommandRoute.builder()
        .addToPath(route1)
        .addToPath(route2)
        .build()

    then:
    printUsage(commandRoute) == normalizedUsage("""usage: route1 route2 <CMD> [OPTIONS]

second route

Options:
 -h,--help   Show this help
 -s

Commands:
 foo   The foo command
""")
  }

  def 'default help option'() {
    expect:
    UsageHelp.DEFAULT_HELP_OPTION.opt == 'h'
    UsageHelp.DEFAULT_HELP_OPTION.longOpt == 'help'
    UsageHelp.DEFAULT_HELP_OPTION.description == 'Show this help'
    !UsageHelp.DEFAULT_HELP_OPTION.required
    !UsageHelp.DEFAULT_HELP_OPTION.hasArg()
    !UsageHelp.DEFAULT_HELP_OPTION.hasArgs()
    !UsageHelp.DEFAULT_HELP_OPTION.hasOptionalArg()
  }

  static String normalizedUsage(String usage) {
    def newLine = System.getProperty("line.separator")
    if (usage.contains(newLine)) {
      return usage
    }
    usage.replaceAll('\\n', newLine)
  }

  private String printUsage(CommandRoute commandRoute, Map ctxOverride = [:]) {
    StringWriter out = new StringWriter()
    def printWriter = new PrintWriter(out)
    def ctxData = [
        (UsageHelp.CTX_HELP_PRINT_WRITER): printWriter
    ]
    ctxData.putAll(ctxOverride)
    CommandContext ctx = new CommandContext(Mock(CommandLine), commandRoute, ctxData)
    new UsageHelp(ctx).pringUsage()
    printWriter.flush()
    return out.toString()
  }
}
