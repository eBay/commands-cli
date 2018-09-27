/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli.test;

import static org.junit.Assert.assertEquals;

import com.ebay.sd.commons.cli.AbstractCommand;
import com.ebay.sd.commons.cli.Argument;
import com.ebay.sd.commons.cli.Command;
import com.ebay.sd.commons.cli.CommandContext;
import com.ebay.sd.commons.cli.CommandDescriptor;
import com.ebay.sd.commons.cli.CommandException;
import com.ebay.sd.commons.cli.CommandFactory;
import com.ebay.sd.commons.cli.CommandsCliMain;
import com.ebay.sd.commons.cli.Descriptor;
import com.ebay.sd.commons.cli.RouteDescriptor;
import com.ebay.sd.commons.cli.UsageHelp;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class CommandsCliApiTest {

  @Test
  public void testSimpleCommandExecution() throws ParseException, CommandException {
    List<String> executions = new ArrayList<>();
    CommandDescriptor root = CommandDescriptor.builder("foo")
        .description("foo command")
        .factory(newTrackingCommandFactory(executions, "foo"))
        .addArgument(Argument.builder("ARG1").description("").required().build())
        .addArgument(Argument.builder("ARG2").description("").build())
        .build();
    CommandsCliMain main = CommandsCliMain.builder()
        .mainCommand(root)
        .build();
    assertEquals("foo ARG1 [ARG2] \n", toCommandLineString("", root));

    main.execute(new String[] {"arg1", "arg2"});
    assertEquals("foo (ARG1: arg1) (ARG2: arg2)", executions.get(0));
  }

  @Test
  public void testComplexCommandLine() {
    List<String> executions = new ArrayList<>();
    RouteDescriptor root = newComplexRouteDescriptor(executions);
    assertEquals("Possible command lines not as expected",
        "foo bar run --opt -s \n"
            + "foo bar run2 ARG1 ARG2{2} [ARG3...] \n"
            + "foo do \n",
        toCommandLineString("", root));
  }

  @Test
  public void testComplexCommandLineExecutions() throws ParseException, CommandException {
    testComplexExecution(new String[] {"do"}, "foo do");
    testComplexExecution(new String[] {"bar", "run"}, "foo bar run");
    testComplexExecution(new String[] {"bar", "run2", "arg1", "arg2a", "arg2b"}, "foo bar run2 (ARG1: arg1) (ARG2: arg2a arg2b) (ARG3:)");
    testComplexExecution(new String[] {"bar", "run2", "arg1", "arg2a", "arg2b", "arg3a"},
        "foo bar run2 (ARG1: arg1) (ARG2: arg2a arg2b) (ARG3: arg3a)");
    testComplexExecution(new String[] {"bar", "run2", "arg1", "arg2a", "arg2b", "arg3a"},
        "foo bar run2 (ARG1: arg1) (ARG2: arg2a arg2b) (ARG3: arg3a)");
    testComplexExecution(new String[] {"bar", "run2", "arg1", "arg2a", "arg2b", "arg3a", "arg3b", "arg3c"},
        "foo bar run2 (ARG1: arg1) (ARG2: arg2a arg2b) (ARG3: arg3a arg3b arg3c)");
  }

  private static void testComplexExecution(String[] args, String expected) throws ParseException, CommandException {
    List<String> executions = new ArrayList<>();
    RouteDescriptor root = newComplexRouteDescriptor(executions);
    CommandsCliMain main = CommandsCliMain.builder()
        .mainRoute(root)
        .build();
    main.execute(args);
    assertEquals(expected, executions.get(0));
  }

  private static RouteDescriptor newComplexRouteDescriptor(List<String> executions) {
    return RouteDescriptor.builder("foo")
        .description("foo cli")
        .addSubCommand(
            RouteDescriptor.builder("bar")
                .description("foo bar route")
                .addSubCommand(
                    CommandDescriptor.builder("run")
                        .description("foo bar run command")
                        .factory(newTrackingCommandFactory(executions, "foo bar run"))
                        .addOption(Option.builder("o").longOpt("opt").build())
                        .addOption(Option.builder("s").build())
                        .build())
                .addSubCommand(
                    CommandDescriptor.builder("run2")
                        .description("foo bar run2 command")
                        .factory(newTrackingCommandFactory(executions, "foo bar run2"))
                        .addArgument(Argument.builder("ARG1").description("").required().build())
                        .addArgument(Argument.builder("ARG2").description("").required().multiplicity(2).build())
                        .addArgument(Argument.builder("ARG3").description("").multiplicityUnlimited().build())
                        .build())
                .build())
        .addSubCommand(
            CommandDescriptor.builder("do")
                .description("foo do command")
                .factory(newTrackingCommandFactory(executions, "foo do"))
                .build())
        .build();
  }

  @Test
  public void testUsageHelpCustomization() throws ParseException, CommandException {
    //given:
    List<String> executions = new ArrayList<>();
    StringWriter out = new StringWriter();
    Map<String, Object> contextData = new HashMap<>();
    contextData.put(UsageHelp.CTX_HELP_OPTION, Option.builder().longOpt("show-help").desc("Show my-cli help").build());
    contextData.put(UsageHelp.CTX_HELP_PRINT_WRITER, new PrintWriter(out));
    CommandsCliMain main = CommandsCliMain.builder()
        .mainCommand(CommandDescriptor.builder("my-cli")
            .description("My command line tool")
            .factory(newTrackingCommandFactory(executions, "foo"))
            .build())
        .contextData(contextData)
        .build();
    //when:
    main.execute(new String[] {"--show-help"});
    //then:
    assertEquals("Command executions should have been empty (size: 0)", 0, executions.size());
    String newLine = System.lineSeparator();
    String expectedUsageText = "usage: my-cli [OPTIONS]" + newLine
        + newLine
        + "My command line tool" + newLine
        + newLine
        + "Options:" + newLine
        + "    --show-help   Show my-cli help" + newLine;
    assertEquals("", expectedUsageText, out.toString());
  }

  private static CommandFactory newTrackingCommandFactory(final List<String> executions, final String name) {
    return new CommandFactory() {
      @Override
      public Command create(final CommandContext commandContext) throws ParseException {
        return new AbstractCommand(commandContext) {
          @Override
          protected void validate(CommandContext commandContext) throws ParseException {
          }

          @Override
          public void execute() throws CommandException {
            List<Argument> arguments = commandContext.getCommandRoute().getCommand().getArguments();
            StringBuilder sb = new StringBuilder(name);
            for (Argument argument : arguments) {
              sb.append(" (").append(argument.getName()).append(":");
              for (String value : commandContext.getArgumentValues(argument.getName())) {
                sb.append(" ").append(value);
              }
              sb.append(")");
            }
            executions.add(sb.toString());
          }
        };
      }
    };
  }

  private static String toCommandLineString(String prefix, Descriptor descriptor) {
    if (descriptor instanceof RouteDescriptor) {
      return toCommandLineString(prefix, (RouteDescriptor) descriptor);
    } else if (descriptor instanceof CommandDescriptor) {
      return toCommandLineString(prefix, (CommandDescriptor) descriptor);
    }
    throw new IllegalStateException("Unexpected descriptor: " + descriptor);
  }

  private static String toCommandLineString(String prefix, RouteDescriptor descriptor) {
    StringBuilder sb = new StringBuilder();
    String nextPrefix = prefix + descriptor.getName() + " ";
    for (Descriptor subCmd : descriptor.getSubCommands()) {
      sb.append(toCommandLineString(nextPrefix, subCmd));
    }
    return sb.toString();
  }

  private static String toCommandLineString(String prefix, CommandDescriptor descriptor) {
    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append(descriptor.getName()).append(" ");
    for (Option option : descriptor.getOptions()) {
      String longOpt = option.getLongOpt();
      String opt = longOpt == null ? "-" + option.getOpt() : "--" + longOpt;
      sb.append(opt).append(" ");
    }
    for (Argument argument : descriptor.getArguments()) {
      if (!argument.isRequired()) {
        sb.append("[");
      }
      sb.append(argument.getName());
      if (argument.getMultiplicity() == Argument.UNLIMITED_VALUES) {
        sb.append("...");
      } else if (argument.getMultiplicity() > 1) {
        sb.append("{").append(argument.getMultiplicity()).append("}");
      }
      if (!argument.isRequired()) {
        sb.append("]");
      }
      sb.append(" ");
    }
    return sb.append("\n").toString();
  }
}
