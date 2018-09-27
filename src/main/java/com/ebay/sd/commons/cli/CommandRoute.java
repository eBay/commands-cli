/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli;

import static com.ebay.sd.commons.cli.Utils.join;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A command route, the resolved path to the command to be executed
 */
public class CommandRoute {

  private final List<RouteDescriptor> path;
  private final CommandDescriptor command;

  private CommandRoute(Builder builder) {
    this.path = Collections.unmodifiableList(requireNonNull(builder.path, "path is required"));
    this.command = builder.command;
  }

  /**
   * Get the resolved path of route to the command
   *
   * @return a list of routes, or an empty list if there is no route to the command
   * @see #getCommand()
   */
  public List<RouteDescriptor> getPath() {
    return path;
  }

  /**
   * Get the resolved command to be executed
   *
   * @return the command
   */
  public CommandDescriptor getCommand() {
    if (command == null) {
      throw new IllegalStateException("command was not set");
    }
    return command;
  }

  boolean hasCommand() {
    return command != null;
  }

  /**
   * Get the full path this command route holds as string
   */
  public String getFullPathAsString() {
    List<String> parts = new ArrayList<>(path.size() + 1);
    for (RouteDescriptor descriptor : path) {
      parts.add(descriptor.getName());
    }
    parts.add(command == null ? "<CMD>" : command.getName());
    return join(parts, ' ');
  }

  @Override
  public String toString() {
    return getFullPathAsString();
  }

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private List<RouteDescriptor> path = new ArrayList<>();
    private CommandDescriptor command;

    private Builder() {
    }

    Builder addToPath(RouteDescriptor route) {
      assertCommandNotSet();
      path.add(requireNonNull(route, "route descriptor is required"));
      return this;
    }

    Builder command(CommandDescriptor command) {
      assertCommandNotSet();
      this.command = command;
      return this;
    }

    private void assertCommandNotSet() {
      if (command != null) {
        throw new IllegalStateException("command already set");
      }
    }

    CommandRoute build() {
      return new CommandRoute(this);
    }
  }
}
