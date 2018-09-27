/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli;

/**
 * An executable command. This is the final endpoint for executing the command logic.
 *
 * <p>
 * Usually implemented by extending {@link AbstractCommand},
 * created by a {@link CommandFactory} assigned in a {@link CommandDescriptor}.
 * </p>
 *
 * @see AbstractCommand
 * @see CommandFactory
 * @see CommandDescriptor
 */
public interface Command {

  /**
   * Execute the command logic.
   *
   * @throws CommandException on any execution error
   */
  void execute() throws CommandException;
}
