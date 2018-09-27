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
import java.util.Collection;
import java.util.List;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;

/**
 * A base command / route descriptor
 */
public abstract class Descriptor implements NameDescriptionSupport {

  private final String name;
  private final String description;
  private final List<Option> options;
  private final List<OptionGroup> optionGroups;

  Descriptor(Builder<?, ?> builder) {
    this.name = requireNonNull(builder.name, "name is required");
    this.description = requireNonNull(builder.description, "description is required");
    this.options = unmodifiableList(new ArrayList<>(requireNonNull(builder.options, "options is required")));
    this.optionGroups = unmodifiableList(new ArrayList<>(requireNonNull(builder.optionGroups, "optionGroups is required")));
  }

  /**
   * Get the name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the options assigned to this descriptor
   *
   * @return the collection of options, or an empty collection
   */
  public Collection<Option> getOptions() {
    return options;
  }

  /**
   * Get the option groups assigned to this descriptor
   *
   * @return the collection of option groups, or an empty collection
   */
  public Collection<OptionGroup> getOptionGroups() {
    return optionGroups;
  }

  @Override
  public String toString() {
    return "Descriptor{" +
        "name='" + name + '\'' +
        '}';
  }

  static abstract class Builder<B extends Builder, T extends Descriptor> {

    private final String name;
    private String description;
    private List<Option> options = new ArrayList<>();
    private List<OptionGroup> optionGroups = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private B self() {
      return (B) this;
    }

    Builder(String name) {
      this.name = name;
    }

    String getName() {
      return name;
    }

    /**
     * Set the description
     *
     * @param description the description
     * @return this builder
     */
    public B description(String description) {
      this.description = description;
      return self();
    }

    /**
     * Add an option
     *
     * @param option the option
     * @return this builder
     */
    public B addOption(Option option) {
      this.options.add(requireNonNull(option, "option is required"));
      return self();
    }

    /**
     * Add an option group
     *
     * @param group the option group
     * @return this builder
     */
    public B addOptionGroup(OptionGroup group) {
      this.optionGroups.add(requireNonNull(group, "group is required"));
      return self();
    }

    /**
     * Build the new descriptor
     *
     * @return a new descriptor instance, based on this builder
     */
    public abstract T build();
  }
}
