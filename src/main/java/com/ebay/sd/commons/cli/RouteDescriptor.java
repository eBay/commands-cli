/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Descriptor of a route.
 * <p>
 * Defines the properties of a route, including its sub-commands.
 * For example:
 * <pre>
 *     RouteDescriptor route = RouteDescriptor.builder("foo")
 *         .description("foo commands")
 *         .addSubCommand(BarCommand.DESCRIPTOR)
 *         .addSubCommand(RouteDescriptor.builder("baz") ... .build())
 *         .build();
 *   </pre>
 * </p>
 */
public class RouteDescriptor extends Descriptor {

  private final List<Descriptor> subCommands;

  private RouteDescriptor(Builder builder) {
    super(builder);
    this.subCommands = unmodifiableList(new ArrayList<>(requireNonNull(builder.subCommands.values(), "subCommands is required")));
    if (subCommands.isEmpty()) {
      throw new IllegalArgumentException("Route must have at least one sub-command");
    }
  }

  /**
   * Get the sub-commands of this route.
   * A non-empty list is expected (since a route must have at least one sub-command).
   *
   * @return the list of sub-command {@link Descriptor}s
   */
  public List<Descriptor> getSubCommands() {
    return subCommands;
  }

  @Override
  public String toString() {
    return "RouteDescriptor{" +
        "name='" + getName() + '\'' +
        '}';
  }

  /**
   * Start building a new route descriptor
   *
   * @param name the name of the route
   * @return a new {@link Builder}
   */
  public static Builder builder(String name) {
    return new Builder(name);
  }

  /**
   * Route descriptor builder
   *
   * @see Builder#build()
   */
  public static class Builder extends Descriptor.Builder<Builder, RouteDescriptor> {

    private Map<String, Descriptor> subCommands = new LinkedHashMap<>();

    private Builder(String name) {
      super(name);
    }

    /**
     * Add a sub-command to this route.
     * <p>
     * Note that a sub-command name must be unique per route.
     * </p>
     *
     * @param descriptor the command or route descriptor to add
     * @return this builder
     */
    public Builder addSubCommand(Descriptor descriptor) {
      String name = descriptor.getName();
      if (subCommands.containsKey(name)) {
        throw new IllegalStateException("Sub-command '" + name + "' already exists for command '" + name + "'");
      }
      this.subCommands.put(name, descriptor);
      return this;
    }

    /**
     * Build a new route descriptor based on the settings to this builder
     *
     * @return the new route descriptor
     */
    @Override
    public RouteDescriptor build() {
      return new RouteDescriptor(this);
    }
  }
}
