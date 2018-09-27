/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli;

import static com.ebay.sd.commons.cli.Utils.pad;
import static java.util.Objects.requireNonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Utility for printing program usage help pages
 * <p>
 * Help pages can be generated for both routes and commands.
 * The main difference is that routes have sub commands and commands have arguments.
 * For example, a common command help page can be:
 * <pre>
 *   usage: my-cli foo [OPTIONS] &lt;ARG1&gt; [&lt;ARG2&gt;...]
 *
 *   Command for doing something
 *
 *   Options:
 *    -h,--help      Show this help
 *    -v,--verbose   Show verbose output
 *
 *   Arguments:
 *    ARG1   The first argument, this is a required argument with
 *           single value
 *    ARG2   The second argument, this is an optional argument with
 *           unlimited values
 * </pre>
 * A common route help page can be:
 * <pre>
 *   usage: my-cli foo &lt;CMD&gt; [OPTIONS]
 *
 *   Foo related commands
 *
 *   Options:
 *    -h,--help   Show this help
 *
 *   Commands:
 *    bar   The bar command
 *    baz   The baz command
 * </pre>
 * </p>
 */
public class UsageHelp {

  /**
   * Command context key for setting a custom help option instead of the default: <tt>-h,--help</tt>
   * <p>
   * Expected value type: {@link Option}
   * </p>
   *
   * @see CommandContext#putValue(String, Object)
   */
  public static final String CTX_HELP_OPTION = "help.option";
  /**
   * Command context key for setting whether the help option should be added automatically to every command and route
   * <p>
   * Expected value type: {@link Boolean}
   * </p>
   *
   * @see CommandContext#putValue(String, Object)
   */
  public static final String CTX_HELP_OPTION_AUTO_ADD = "help.option.auto.add";
  /**
   * Command context key for setting the print writer, instead of the default one writing to stdout.
   * <p>
   * Expected value type: {@link PrintWriter}
   * </p>
   *
   * @see CommandContext#putValue(String, Object)
   */
  public static final String CTX_HELP_PRINT_WRITER = "help.printwriter";
  /**
   * Command context key for setting a custom help formatter to customize help pages layout
   * <p>
   * Expected value type: {@link HelpFormatter}
   * </p>
   *
   * @see CommandContext#putValue(String, Object)
   */
  public static final String CTX_HELP_FORMATTER = "help.formatter";
  static final Option DEFAULT_HELP_OPTION = Option.builder("h").longOpt("help").desc("Show this help").build();
  private final CommandContext commandContext;

  UsageHelp(CommandContext commandContext) {
    this.commandContext = requireNonNull(commandContext, "commandContext is required");
  }

  void pringUsage() {
    HelpFormatter formatter = (HelpFormatter) commandContext.getValue(CTX_HELP_FORMATTER, new HelpFormatter());
    Descriptor descriptor = getDescriptor();
    Options options = getOptions(descriptor);
    int width = formatter.getWidth();
    int leftPadding = formatter.getLeftPadding();
    int descPadding = formatter.getDescPadding();
    String cmdLineSyntax = getCmdLineSyntax(options);
    String header = getHeader(descriptor, formatter);
    String footer = getFooter(descriptor, formatter);
    if (!options.getOptions().isEmpty()) {
      header += formatter.getNewLine() + "Options:";
    }
    PrintWriter out = (PrintWriter) commandContext.getValue(CTX_HELP_PRINT_WRITER, new PrintWriter(System.out));
    try {
      formatter.printHelp(out, width, cmdLineSyntax, header, options, leftPadding, descPadding, footer);
    } finally {
      out.flush();
    }
  }

  private String getHeader(Descriptor descriptor, HelpFormatter formatter) {
    StringBuilder header = new StringBuilder()
        .append(formatter.getNewLine())
        .append(descriptor.getDescription())
        .append(formatter.getNewLine());
    return header.toString();
  }

  private String getFooter(Descriptor descriptor, HelpFormatter formatter) {
    if (descriptor instanceof CommandDescriptor) {
      return getCommandFooter(((CommandDescriptor) descriptor), formatter);
    } else if (descriptor instanceof RouteDescriptor) {
      return getRouteFooter(((RouteDescriptor) descriptor), formatter);
    }
    throw new IllegalStateException("Unexpected descriptor type: " + descriptor);
  }

  private String getRouteFooter(RouteDescriptor descriptor, HelpFormatter formatter) {
    return getFooter(formatter, "Commands:", descriptor.getSubCommands());
  }

  private String getCommandFooter(CommandDescriptor descriptor, HelpFormatter formatter) {
    return getFooter(formatter, "Arguments:", descriptor.getArguments());
  }

  private String getFooter(HelpFormatter formatter, String title, List<? extends NameDescriptionSupport> namedObjects) {
    if (namedObjects.isEmpty()) {
      return "";
    }
    int maxCmdLength = findMaxNameLength(namedObjects);
    int leftPadding = formatter.getLeftPadding();
    int totalLength = leftPadding + maxCmdLength + 3;
    StringWriter out = new StringWriter();
    try (PrintWriter pw = new PrintWriter(out)) {
      out.append(formatter.getNewLine()).append(title).append(formatter.getNewLine());
      for (NameDescriptionSupport namedObject : namedObjects) {
        String text = pad(namedObject.getName(), leftPadding, totalLength) + namedObject.getDescription();
        formatter.printWrapped(pw, formatter.getWidth(), totalLength, text);
      }
    }
    return out.toString();
  }

  private int findMaxNameLength(List<? extends NameDescriptionSupport> namedObjects) {
    int max = 0;
    for (NameDescriptionSupport namedObject : namedObjects) {
      max = Math.max(max, namedObject.getName().length());
    }
    return max;
  }

  private String getCmdLineSyntax(Options options) {
    CommandRoute commandRoute = commandContext.getCommandRoute();
    StringBuilder syntax = new StringBuilder(commandRoute.getFullPathAsString());
    if (!options.getOptions().isEmpty()) {
      syntax.append(" [OPTIONS]");
    }
    if (commandRoute.hasCommand()) {
      for (Argument argument : commandRoute.getCommand().getArguments()) {
        syntax.append(" ").append(toString(argument));
      }
    }
    return syntax.toString();
  }

  static String toString(Argument argument) {
    StringBuilder syntax = new StringBuilder();
    if (!argument.isRequired()) {
      syntax.append("[");
    }
    syntax.append("<").append(argument.getName()).append(">");
    if (argument.getMultiplicity() == Argument.UNLIMITED_VALUES) {
      syntax.append("...");
    } else if (argument.getMultiplicity() > 1) {
      syntax.append("{").append(argument.getMultiplicity()).append("}");
    }
    if (!argument.isRequired()) {
      syntax.append("]");
    }
    return syntax.toString();
  }

  private Options getOptions(Descriptor descriptor) {
    Options options = new Options();
    for (Option option : descriptor.getOptions()) {
      options.addOption(option);
    }
    if (Boolean.TRUE.equals(commandContext.getValue(CTX_HELP_OPTION_AUTO_ADD, true))) {
      options.addOption((Option) commandContext.getValue(CTX_HELP_OPTION, DEFAULT_HELP_OPTION));
    }
    return options;
  }

  private Descriptor getDescriptor() {
    CommandRoute commandRoute = commandContext.getCommandRoute();
    if (commandRoute.hasCommand()) {
      return commandRoute.getCommand();
    }
    return commandRoute.getPath().get(commandRoute.getPath().size() - 1);
  }
}
