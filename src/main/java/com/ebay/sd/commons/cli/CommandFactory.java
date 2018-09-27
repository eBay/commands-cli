/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli;

import org.apache.commons.cli.ParseException;

/**
 * A command factory, used for creating {@link Command}s
 */
public interface CommandFactory {

  /**
   * Create a {@link Command} with the given {@link CommandContext}.
   *
   * @param commandContext the command context
   * @return the new {@link Command}
   * @throws ParseException on any parse or preparation error on the command
   */
  Command create(CommandContext commandContext) throws ParseException;
}
