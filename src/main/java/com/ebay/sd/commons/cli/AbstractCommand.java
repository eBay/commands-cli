/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/
package com.ebay.sd.commons.cli;

import static java.util.Objects.requireNonNull;

import org.apache.commons.cli.ParseException;

/**
 * A base command implementation which shall be extended by command classes in most cases.
 * <p>
 * Accepts the {@link CommandContext} which is provided by the {@link CommandFactory}
 * and adds a validation step which can be implemented to validate the command context
 * includes everything the command execution requires. This validation step shall fail
 * with a {@link ParseException} to distinguish between invalid input and an execution
 * failure.
 * </p>
 *
 * @see CommandFactory
 * @see Command
 */
public abstract class AbstractCommand implements Command {

  private final CommandContext commandContext;

  /**
   * Construct a command
   *
   * @param commandContext the command context
   * @throws ParseException if the the input is invalid
   */
  protected AbstractCommand(CommandContext commandContext) throws ParseException {
    this.commandContext = requireNonNull(commandContext, "commandContext is required");
    validate(commandContext);
  }

  /**
   * Get the command context provided to this command when it was constructed
   *
   * @return the command context
   */
  protected CommandContext getContext() {
    return commandContext;
  }

  /**
   * Validate the command context, check it includes the required input and the validity of the input.
   * Expected to throw an {@link ParseException} in case the input is invalid.
   *
   * @param commandContext the command context to validate
   * @throws ParseException if the the input is invalid
   */
  protected abstract void validate(CommandContext commandContext) throws ParseException;
}
