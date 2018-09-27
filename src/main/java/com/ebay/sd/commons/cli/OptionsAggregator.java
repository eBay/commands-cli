/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli;

import static java.util.Objects.requireNonNull;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * Options aggregator.
 * <p>
 * Responsible for aggregating and validating all options and option groups from the given descriptor recursively.
 * </p>
 */
class OptionsAggregator {

  /**
   * Aggregate the options from the given descriptor recursively and validate them
   *
   * @param rootDescriptor the root descriptor from which to start aggregating
   * @return options with all aggregated options and option groups
   */
  Options aggregate(Descriptor rootDescriptor) {
    Options options = new Options();
    aggregate(options, requireNonNull(rootDescriptor, "root descriptor is required"));
    return options;
  }

  private void aggregate(Options options, Descriptor descriptor) {
    for (Option option : descriptor.getOptions()) {
      Option normalizedOption = normalizeOption(option);
      assertNoConflict(options, normalizedOption);
      options.addOption(normalizedOption);
    }
    for (OptionGroup group : descriptor.getOptionGroups()) {
      options.addOptionGroup(group);
    }
    if (descriptor instanceof RouteDescriptor) {
      for (Descriptor subCmd : ((RouteDescriptor) descriptor).getSubCommands()) {
        aggregate(options, subCmd);
      }
    }
  }

  private Option normalizeOption(Option option) {
    Option copy = (Option) option.clone();
    copy.setRequired(false);
    return copy;
  }

  private void assertNoConflict(Options options, Option option) {
    assertSimilarAsExisting(options, option, option.getOpt());
    assertSimilarAsExisting(options, option, option.getLongOpt());
  }

  /**
   * Check that if there is already an existing option as the one provided, they are similar enough and do no cause a conflict.
   * A conflict can be caused if a functional property of the options is not the same, including:
   * <ul>
   * <li>short name</li>
   * <li>long name</li>
   * <li>whether the option is required</li>
   * <li>number of arguments the option can take</li>
   * <li>argument type</li>
   * <li>whether the option arguments are optional</li>
   * <li>value separator</li>
   * </ul>
   *
   * @param options the already aggregated options
   * @param option the option to add
   * @param name the name by which to find the existing option from the already aggregated options
   */
  private void assertSimilarAsExisting(Options options, Option option, String name) {
    Option existing = findOption(options, name);
    if (existing != null) {
      assertEquals("short name", option, existing, SHORT_NAME);
      assertEquals("long name", option, existing, LONG_NAME);
      assertEquals("required setting", option, existing, REQUIRED);
      assertEquals("args number", option, existing, ARGS);
      assertEquals("arg type", option, existing, ARG_TYPE);
      assertEquals("optional arg setting", option, existing, OPTIONAL_ARG);
      assertEquals("value separator", option, existing, VALUE_SEPARATOR);
    }
  }

  /**
   * Assert that a field value is the same in both new option and existing option
   *
   * @param field the description of the field (for the error message, if needed)
   * @param option the new option
   * @param existing the existing option
   * @param extractor a function for extracting the field value from the given options
   * @param <T> the type of the field value
   */
  private <T> void assertEquals(String field, Option option, Option existing, FieldValueExtractor<T> extractor) {
    T newValue = extractor.getValue(option);
    T existingValue = extractor.getValue(existing);
    if (newValue == null && existingValue == null || newValue != null && newValue.equals(existingValue)) {
      return;
    }
    throw new IllegalStateException("There is already an existing matching option with different " + field + ".\n"
        + "New: " + option + "\n"
        + "Existing: " + existing + "\n");
  }

  private Option findOption(Options options, String name) {
    if (name != null) {
      return options.getOption(name);
    }
    return null;
  }

  private interface FieldValueExtractor<T> {
    T getValue(Option option);
  }

  private static final FieldValueExtractor<String> SHORT_NAME = new FieldValueExtractor<String>() {
    @Override
    public String getValue(Option option) {
      return option.getOpt();
    }
  };

  private static final FieldValueExtractor<String> LONG_NAME = new FieldValueExtractor<String>() {
    @Override
    public String getValue(Option option) {
      return option.getLongOpt();
    }
  };

  private static final FieldValueExtractor<Boolean> REQUIRED = new FieldValueExtractor<Boolean>() {
    @Override
    public Boolean getValue(Option option) {
      return option.isRequired();
    }
  };

  private static final FieldValueExtractor<Integer> ARGS = new FieldValueExtractor<Integer>() {
    @Override
    public Integer getValue(Option option) {
      return option.getArgs();
    }
  };

  private static final FieldValueExtractor<Object> ARG_TYPE = new FieldValueExtractor<Object>() {
    @Override
    public Object getValue(Option option) {
      return option.getType();
    }
  };

  private static final FieldValueExtractor<Boolean> OPTIONAL_ARG = new FieldValueExtractor<Boolean>() {
    @Override
    public Boolean getValue(Option option) {
      return option.hasOptionalArg();
    }
  };

  private static final FieldValueExtractor<Character> VALUE_SEPARATOR = new FieldValueExtractor<Character>() {
    @Override
    public Character getValue(Option option) {
      return option.getValueSeparator();
    }
  };
}
