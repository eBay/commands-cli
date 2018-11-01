/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/
package com.ebay.sd.commons.cli;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.ParseException;

/**
 * Descriptor of a command.
 * <p>
 * Defines the properties of a command, including the {@link CommandFactory} to use for creating the {@link Command} instance.
 * For example:
 * <pre>
 *     CommandDescriptor fooCmd = CommandDescriptor.builder("foo")
 *         .description("Do foo")
 *         .addOption(MyOptions.VERBOSE)
 *         .addArgument(fileArg)
 *         .factory(fooCmdFactory)
 *         .build();
 * </pre>
 */
public class CommandDescriptor extends Descriptor {

  private final List<Argument> arguments;
  private final CommandFactory factory;

  private CommandDescriptor(Builder builder) {
    super(builder);
    this.arguments = unmodifiableList(new ArrayList<>(requireNonNull(builder.args, "arguments is required")));
    this.factory = requireNonNull(builder.factory, "factory is required");
  }

  /**
   * Get the arguments of the command
   *
   * @return an ordered list of arguments (based on the order of addition), or an empty list
   */
  public List<Argument> getArguments() {
    return arguments;
  }

  /**
   * Create tne command
   *
   * @param commandContext the command context
   * @return the new {@link Command} instance
   * @throws ParseException on any parsing or command preparation error
   */
  public Command createCommand(CommandContext commandContext) throws ParseException {
    return factory.create(commandContext);
  }

  @Override
  public String toString() {
    return "CommandDescriptor{" +
        "name='" + getName() + '\'' +
        '}';
  }

  /**
   * Start building a new command descriptor
   *
   * @param name the name of the command
   * @return a new {@link Builder}
   * @see Builder#build()
   */
  public static Builder builder(String name) {
    return new Builder(name);
  }

  /**
   * Command descriptor builder
   *
   * @see #builder(String)
   */
  public static class Builder extends Descriptor.Builder<Builder, CommandDescriptor> {

    private CommandFactory factory;
    private List<Argument> args = new ArrayList<>();

    private Builder(String name) {
      super(name);
    }

    /**
     * Add an argument for the command.
     * <p>
     * The order of addition is the expected order of arguments when parsed.
     * There are some constraints on the order of arguments:
     * <ul>
     * <li>Argument name must be unique per command</li>
     * <li>Argument with unlimited values must be last</li>
     * <li>An optional argument (non-required) must be last</li>
     * </ul>
     *
     * @param argument the argument to add
     * @return this builder
     */
    public Builder addArgument(Argument argument) {
      validateArgument(argument);
      this.args.add(argument);
      return this;
    }

    private void validateArgument(Argument argument) {
      requireNonNull(argument, "argument is required");
      if (args.isEmpty()) {
        return;
      }
      if (argumentNameExists(argument.getName())) {
        throw new IllegalArgumentException(format("Argument '%s' already exists for command '%s'", argument.getName(), getName()));
      }
      Argument lastArgument = args.get(args.size() - 1);
      if (!lastArgument.isRequired()) {
        throw new IllegalArgumentException(format("Argument '%s' cannot come after an optional argument '%s' for command '%s'",
            argument.getName(), lastArgument.getName(), getName()));
      }
      if (lastArgument.getMultiplicity() == Argument.UNLIMITED_VALUES) {
        throw new IllegalArgumentException(format("Argument '%s' cannot come after an argument '%s' with unlimited values for command '%s'",
            argument.getName(), lastArgument.getName(), getName()));
      }
    }

    private boolean argumentNameExists(String name) {
      for (Argument argument : args) {
        if (argument.getName().equals(name)) {
          return true;
        }
      }
      return false;
    }

    /**
     * Set the command factory to use for creating the {@link Command} instance
     *
     * @param factory the command factory
     * @return this builder
     */
    public Builder factory(CommandFactory factory) {
      this.factory = factory;
      return this;
    }

    /**
     * Build a new command descriptor based on the settings to this builder
     *
     * @return the new command descriptor
     */
    @Override
    public CommandDescriptor build() {
      return new CommandDescriptor(this);
    }
  }
}
