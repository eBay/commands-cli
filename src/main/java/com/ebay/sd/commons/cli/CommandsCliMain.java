/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The main entry point of the Commands CLI.
 * <p>
 * A common usage example:
 * <pre>
 *     public static void main(String[] args) {
 *       CommandsCliMain.builder()
 *           .mainCommand(MyCommand.DESCRIPTOR)
 *           .build()
 *           .main(args)
 *     }
 *   </pre>
 * </p>
 */
public class CommandsCliMain {

  private final Descriptor rootDescriptor;
  private final Options options;
  private final Map<String, Object> contextData = new HashMap<>();

  private CommandsCliMain(Builder builder) {
    rootDescriptor = requireNonNull(builder.rootDescriptor, "rootDescriptor is required");
    if (builder.contextData != null) {
      contextData.putAll(builder.contextData);
    }
    options = new OptionsAggregator().aggregate(rootDescriptor);
    addHelpOptionIfNeeded(options);
  }

  private void addHelpOptionIfNeeded(Options options) {
    Object autoAddHelpOption = this.contextData.get(UsageHelp.CTX_HELP_OPTION_AUTO_ADD);
    if (autoAddHelpOption == null || Boolean.TRUE.equals(autoAddHelpOption)) {
      Option helpOpt = (Option) this.contextData.get(UsageHelp.CTX_HELP_OPTION);
      if (helpOpt == null) {
        helpOpt = UsageHelp.DEFAULT_HELP_OPTION;
      }
      options.addOption(helpOpt);
    }
  }

  /**
   * The main method, should usually be called in the <tt>main(String[] args)</tt> method of the using program.
   * <p>
   * This method uses <tt>System.exit(int)</tt> on any error, hence should not be used for tests.
   * </p>
   *
   * @param args the command line arguments
   * @see #execute(String[])
   */
  public void main(String[] args) {
    try {
      execute(args);
    } catch (ParseException | CommandException e) {
      System.err.println("ERROR: " + e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Execute the commands cli.
   * <p>
   * This method is the driver of this tool.
   * It is responsible for parsing the command line options and arguments,
   * resolving the route to the command and executing it.
   * </p>
   *
   * @param args the command line arguments
   * @throws ParseException on any command line parsing error
   * @throws CommandException on any command execution error
   */
  public void execute(String[] args) throws ParseException, CommandException {
    CommandLine commandLine = parseCommandLine(options, args);
    boolean helpRequested = helpRequested(commandLine, contextData);
    CommandRoute commandRoute = new CommandRouteResolver(rootDescriptor).resolve(commandLine, helpRequested);
    CommandContext context = new CommandContext(commandLine, commandRoute, contextData);
    if (helpRequested) {
      new UsageHelp(context).pringUsage();
      return;
    }
    Command command = createCommand(context);
    execute(command);
  }

  private Command createCommand(CommandContext context) throws ParseException {
    CommandRoute commandRoute = context.getCommandRoute();
    if (!commandRoute.hasCommand()) {
      throw new ParseException("Command is required for route: " + commandRoute);
    }
    CommandDescriptor descriptor = commandRoute.getCommand();
    return descriptor.createCommand(context);
  }

  private void execute(Command command) throws CommandException {
    try {
      command.execute();
    } catch (RuntimeException e) {
      throw new CommandException("UNEXPECTED ERROR: " + e.getMessage(), e);
    }
  }

  private boolean helpRequested(CommandLine commandLine, Map<String, Object> contextData) {
    Option helpOpt = (Option) contextData.get(UsageHelp.CTX_HELP_OPTION);
    helpOpt = helpOpt == null ? UsageHelp.DEFAULT_HELP_OPTION : helpOpt;
    String opt = helpOpt.getLongOpt() != null ? helpOpt.getLongOpt() : helpOpt.getOpt();
    return commandLine.hasOption(opt);
  }

  private CommandLine parseCommandLine(Options options, String[] args) throws ParseException {
    CommandLineParser cliParser = new DefaultParser();
    return cliParser.parse(options, args);
  }

  /**
   * Start building a commands CLI main entry point
   *
   * @return a new {@link Builder}
   * @see Builder#build()
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Commands CLI main entry point builder
   */
  public static class Builder {

    private Descriptor rootDescriptor;
    private Map<String, Object> contextData;

    private Builder() {
    }

    /**
     * Set the main command to be executed.
     * This is for the simple use case where there is a single command.
     *
     * @param descriptor the main command descriptor
     * @return this builder
     * @see #mainRoute(RouteDescriptor)
     */
    public Builder mainCommand(CommandDescriptor descriptor) {
      assertRootDescriptorNotSet();
      this.rootDescriptor = descriptor;
      return this;
    }

    /**
     * Set the main route of the commands.
     * This is for the advanced use case where there are multiple commands with optionally multiple routing levels.
     *
     * @param descriptor the main route descriptor
     * @return this builder
     * @see #mainCommand(CommandDescriptor)
     */
    public Builder mainRoute(RouteDescriptor descriptor) {
      assertRootDescriptorNotSet();
      this.rootDescriptor = descriptor;
      return this;
    }

    private void assertRootDescriptorNotSet() {
      if (rootDescriptor != null) {
        throw new IllegalStateException("rootDescriptor already set");
      }
    }

    /**
     * Set any context data
     *
     * @param contextData the context data map to set
     * @return this builder
     */
    public Builder contextData(Map<String, Object> contextData) {
      this.contextData = contextData;
      return this;
    }

    /**
     * Build the commands CLI main entry point
     *
     * @return the new {@link CommandsCliMain}
     */
    public CommandsCliMain build() {
      return new CommandsCliMain(this);
    }
  }
}
