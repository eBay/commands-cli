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
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;

/**
 * A context for carrying data for command execution
 */
public class CommandContext {

  private final CommandLine commandLine;
  private final CommandRoute commandRoute;
  private final Map<String, Object> data;

  CommandContext(CommandLine commandLine, CommandRoute commandRoute, Map<String, Object> data) {
    this.commandLine = requireNonNull(commandLine, "commandLine is required");
    this.commandRoute = requireNonNull(commandRoute, "commandRoute is required");
    this.data = new HashMap<>();
    if (data != null) {
      this.data.putAll(data);
    }
  }

  /**
   * Get the parsed command line
   *
   * @return the command line
   */
  public CommandLine getCommandLine() {
    return commandLine;
  }

  /**
   * Get the resolved command route
   *
   * @return the command route
   */
  public CommandRoute getCommandRoute() {
    return commandRoute;
  }

  /**
   * Put a context value
   *
   * @param key the key to identify the value
   * @param value the value
   */
  public void putValue(String key, Object value) {
    data.put(key, value);
  }

  /**
   * Get a context value
   *
   * @param key the key that identifies the value to get
   * @return the value, or <tt>null</tt> if does not exist
   */
  public Object getValue(String key) {
    return data.get(key);
  }

  /**
   * Get a context value, or default value if was not set
   *
   * @param key the key that identifies the value to get
   * @param defaultValue the default value to return in case a value is missing for the given <tt>key</tt>
   * @return the value, or default value
   */
  public Object getValue(String key, Object defaultValue) {
    Object value = getValue(key);
    return value != null ? value : defaultValue;
  }

  /**
   * Get a required context value. Throws an exception if the value does not exist (i.e. <tt>null</tt>)
   *
   * @param key the key that identifies the value to get
   * @return the value
   */
  public Object getRequiredValue(String key) {
    return requireNonNull(getValue(key), key + " is required");
  }

  /**
   * Get the values of a given argument
   *
   * @param name the name of the argument
   * @return the list of values, or an empty list
   */
  public List<String> getArgumentValues(String name) {
    return findArgument(name).getValues();
  }

  /**
   * Get the value of a given argument. If the argument has multiple values, the first value is returned.
   *
   * @param name the name of the argument
   * @return the value
   */
  public String getArgumentValue(String name) {
    return findArgument(name).getValue();
  }

  /**
   * Get the value of a given argument or default value if the argument has no values.
   * If the argument has multiple values, the first value is returned.
   *
   * @param name the name of the argument
   * @param defaultValue the default value to return in case a value is missing for the given <tt>name</tt>
   * @return the value, or default
   */
  public String getArgumentValue(String name, String defaultValue) {
    return findArgument(name).getValue(defaultValue);
  }

  /**
   * Get the value of a given argument.
   * If the argument has multiple values, the first value is returned.
   * Throws an exception if the argument has no value.
   *
   * @param name the name of the argument
   * @return the value
   */
  public String getRequiredArgumentValue(String name) {
    return requireNonNull(getArgumentValue(name), name + " is required");
  }

  private Argument findArgument(String name) {
    for (Argument argument : commandRoute.getCommand().getArguments()) {
      if (argument.getName().equals(name)) {
        return argument;
      }
    }
    throw new IllegalStateException("Argument not found: " + name);
  }
}
