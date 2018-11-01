/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/
package com.ebay.sd.commons.cli;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.cli.Option;

/**
 * A command argument definition.
 *
 * <p>
 * For example, to create a required argument named "FILE" which expects exactly one value, use:
 * <pre>
 *   Argument fileArg = Argument.builder("FILE")
 *       .description("The input file")
 *       .required()
 *       .build()
 * </pre>
 * It is then can be added to the command descriptor using:
 * <pre>
 *     .addCommand(fileArg)
 * </pre>
 * To get the argument value during command execution, use the {@link CommandContext}. For example:
 * <pre>
 *   String file = commandContext.getArgumentValue("FILE");
 * </pre>
 *
 * @see CommandDescriptor.Builder#addArgument(Argument)
 * @see CommandContext#getArgumentValue(String)
 */
public class Argument implements NameDescriptionSupport {

  /**
   * Constant to define an argument with unlimited multiplicity
   *
   * @see Builder#multiplicity(int)
   * @see #getMultiplicity()
   */
  public static final int UNLIMITED_VALUES = Option.UNLIMITED_VALUES;
  private final String name;
  private final String description;
  private final boolean required;
  private final int multiplicity;
  private final List<String> values = new ArrayList<>();

  private Argument(Builder builder) {
    this.name = requireNonNull(builder.name, "name is required");
    this.description = requireNonNull(builder.description, "description is required");
    this.required = builder.required;
    this.multiplicity = requireMultiplicity(builder.multiplicity);
  }

  private int requireMultiplicity(int multiplicity) {
    if (multiplicity <= 0 && multiplicity != UNLIMITED_VALUES) {
      throw new IllegalArgumentException("Illegal multiplicity, must be positive: " + multiplicity);
    }
    return multiplicity;
  }

  /**
   * Get the name of the argument
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Get the description of the argument
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Get whether this argument is defined as required
   *
   * @return <tt>true</tt> if this argument is required, <tt>false</tt> otherwise
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * Get the multiplicity of this argument
   *
   * <p>
   * This is either a positive value (<tt>&gt;0</tt>) or equals to {@link #UNLIMITED_VALUES}.
   * </p>
   *
   * @return the multiplicity of this argument
   */
  public int getMultiplicity() {
    return multiplicity;
  }

  List<String> getValues() {
    return Collections.unmodifiableList(values);
  }

  String getValue() {
    return hasValues() ? values.get(0) : null;
  }

  String getValue(String defaultValue) {
    String value = getValue();
    return value != null ? value : defaultValue;
  }

  boolean hasValues() {
    return !values.isEmpty();
  }

  void addValue(String value) {
    values.add(requireNonNull(value, "value is required"));
  }

  @Override
  public String toString() {
    String multiplicityStr = multiplicity == UNLIMITED_VALUES ? "UNLIMITED" : String.valueOf(multiplicity);
    return "Argument{" +
        "name='" + name + '\'' +
        ", required=" + required +
        ", multiplicity=" + multiplicityStr +
        '}';
  }

  /**
   * Start building a new argument
   *
   * @param name the name of the argument.
   * This name is used to identify the argument and needs to be unique per command
   * @return a new {@link Builder}
   * @see Builder#build()
   */
  public static Builder builder(String name) {
    return new Builder(name);
  }

  /**
   * Argument builder
   *
   * @see #builder(String)
   */
  public static class Builder {

    private final String name;
    private String description;
    private boolean required = false;
    private int multiplicity = 1;

    private Builder(String name) {
      this.name = name;
    }

    /**
     * Set the argument description
     *
     * @param description the description
     * @return this builder
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Set this argument as required
     *
     * @return this builder
     * @see #required(boolean)
     */
    public Builder required() {
      return required(true);
    }

    /**
     * Set whether this argument is required or not (default: <tt>false</tt>)
     *
     * @param required whether the argument is required
     * @return this builder
     */
    public Builder required(boolean required) {
      this.required = required;
      return this;
    }

    /**
     * Set the multiplicity of this argument as unlimited (i.e. <tt>[0..*]</tt>, or <tt>[1..*]</tt> if required)
     *
     * @return this builder
     * @see #multiplicity(int)
     * @see #required(boolean)
     */
    public Builder multiplicityUnlimited() {
      return multiplicity(UNLIMITED_VALUES);
    }

    /**
     * Set the multiplicity of this argument, i.e. how many values this argument can have (default: <tt>1</tt>)
     * <p>
     * Must me a positive number (<tt>&gt;0</tt>) or set to {@link #UNLIMITED_VALUES}
     * </p>
     *
     * @param multiplicity the multiplicity to set
     * @return this builder
     */
    public Builder multiplicity(int multiplicity) {
      this.multiplicity = multiplicity;
      return this;
    }

    /**
     * Build a new argument instance based on the settings to this builder
     *
     * @return the new argument
     */
    public Argument build() {
      return new Argument(this);
    }
  }
}
