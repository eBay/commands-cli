/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

/**
 * Command route resolver.
 * <p>
 * Responsible for resolving the command route and the command for execution,
 * based on the command line arguments and the given descriptor.
 * Also, parses the left over command line arguments and assigns them to the resolved command.
 * </p>
 */
class CommandRouteResolver {

  private final Descriptor root;

  CommandRouteResolver(Descriptor root) {
    this.root = requireNonNull(root, "root descriptor is required");
  }

  /**
   * Resolve the command route from the command line and parse the command arguments
   *
   * @param commandLine the command line from which to resolve the command
   * @param skipParseArguments whether to skip the command arguments parsing step.
   * This is usually useful when usage help was requested, to avoid parse errors on missing or wrong arguments.
   * @return the resolved command route
   * @throws ParseException on any resolving or parsing error
   */
  CommandRoute resolve(CommandLine commandLine, boolean skipParseArguments) throws ParseException {
    requireNonNull(commandLine, "commandLine is required");
    CommandRoute route = doResolve(commandLine);
    if (!skipParseArguments) {
      validateOptions(commandLine, route);
      parseCommandArguments(commandLine, route);
    }
    return route;
  }

  private CommandRoute doResolve(CommandLine commandLine) throws ParseException {
    CommandRoute.Builder route = CommandRoute.builder();
    doResolve(route, commandLine, root, -1);
    return route.build();
  }

  private void doResolve(CommandRoute.Builder route, CommandLine commandLine, Descriptor descriptor, int cmdIndex) throws ParseException {
    if (descriptor instanceof RouteDescriptor) {
      route.addToPath((RouteDescriptor) descriptor);
      Descriptor subCommand = findSubCommand(commandLine, (RouteDescriptor) descriptor, cmdIndex + 1);
      if (subCommand != null) {
        doResolve(route, commandLine, subCommand, cmdIndex + 1);
      }
      return;
    } else if (descriptor instanceof CommandDescriptor) {
      route.command((CommandDescriptor) descriptor);
      return;
    }
    throw new IllegalStateException("Unexpected descriptor type: " + descriptor.getClass());
  }

  private Descriptor findSubCommand(CommandLine commandLine, RouteDescriptor descriptor, int cmdIndex) throws ParseException {
    Descriptor found = null;
    if (commandLine.getArgList().size() > cmdIndex) {
      String cmd = commandLine.getArgList().get(cmdIndex);
      for (Descriptor subCmd : descriptor.getSubCommands()) {
        if (subCmd.getName().equals(cmd)) {
          found = subCmd;
          break;
        }
      }
    } else {
      //all command line args were used to resolve the route path
      return null;
    }
    if (found == null) {
      throw new ParseException("Unknown command: " + commandLine.getArgList().get(cmdIndex));
    }
    return found;
  }

  private void validateOptions(CommandLine commandLine, CommandRoute route) throws ParseException {
    Descriptor descriptor;
    if (route.hasCommand()) {
      descriptor = route.getCommand();
    } else {
      descriptor = route.getPath().get(route.getPath().size() - 1);
    }
    validateNoMissingOptions(commandLine, descriptor);
  }

  private void validateNoMissingOptions(CommandLine commandLine, Descriptor descriptor) throws MissingOptionException {
    List<String> missingOptions = new ArrayList<>();
    for (Option option : descriptor.getOptions()) {
      String opt = option.getOpt() != null ? option.getOpt() : option.getLongOpt();
      if (option.isRequired() && !commandLine.hasOption(opt)) {
        missingOptions.add(opt);
      }
    }
    if (!missingOptions.isEmpty()) {
      throw new MissingOptionException(missingOptions);
    }
  }

  private void parseCommandArguments(CommandLine commandLine, CommandRoute commandRoute) throws ParseException {
    if (!commandRoute.hasCommand()) {
      return;
    }
    int cmdIndex = commandRoute.getPath().size() - 1;
    List<String> cmdArgs = commandLine.getArgList().subList(cmdIndex + 1, commandLine.getArgList().size());
    List<Argument> arguments = commandRoute.getCommand().getArguments();
    Iterator<String> cmdArgsIter = cmdArgs.iterator();
    for (Argument argument : arguments) {
      boolean unlimited = argument.getMultiplicity() == Argument.UNLIMITED_VALUES;
      int count = unlimited ? Integer.MAX_VALUE : argument.getMultiplicity();
      for (int i = 0; i < count && cmdArgsIter.hasNext(); i++) {
        argument.addValue(cmdArgsIter.next());
      }
      validateParsedArgument(argument);
    }
    if (cmdArgsIter.hasNext()) {
      throw new ParseException("There is at least one unhandled argument: " + cmdArgsIter.next());
    }
  }

  private void validateParsedArgument(Argument argument) throws ParseException {
    if (argument.isRequired() && argument.getValues().isEmpty()) {
      throw new ParseException("Argument is required: " + argument.getName());
    }
    if (argument.getMultiplicity() > 0) {
      if (argument.getValues().size() == 0 && !argument.isRequired()) {
        return;
      }
      if (argument.getValues().size() < argument.getMultiplicity()) {
        throw new ParseException(format("Argument has too few values: %s (expected: %d)", argument.getName(), argument.getMultiplicity()));
      }
      if (argument.getValues().size() > argument.getMultiplicity()) {
        //This is unexpected - should never get here, but still...
        throw new IllegalStateException(
            format("Argument has too many values: %s (expected: %d)", argument.getName(), argument.getMultiplicity()));
      }
    }
  }
}
