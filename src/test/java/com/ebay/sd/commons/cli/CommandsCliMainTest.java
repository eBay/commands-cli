package com.ebay.sd.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.Assertion;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class CommandsCliMainTest {

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();
  @Rule
  public final SystemOutRule systemOut = new SystemOutRule().enableLog().muteForSuccessfulTests();
  @Rule
  public final SystemErrRule systemErr = new SystemErrRule().enableLog().muteForSuccessfulTests();

  @Test
  public void testMyCliExecuteEmpty() {
    exit.expectSystemExitWithStatus(1);
    exit.checkAssertionAfterwards(new Assertion() {
      @Override
      public void checkAssertion() throws Exception {
        assertEquals("ERROR: Command is required for route: my-cli <CMD>\n", systemErr.getLogWithNormalizedLineSeparator());
      }
    });
    createCli().main(new String[] {});
    fail("Should not get here - exit(1) was expected");
  }

  @Test
  public void testMyCliUsage() {
    createCli().main(new String[] {"-h"});
    assertEquals(readExpectedResourceFile("usage-my-cli.txt"), systemOut.getLogWithNormalizedLineSeparator());
  }

  @Test
  public void testCmd1Usage() {
    createCli().main(new String[] {"-h", "1-cmd"});
    assertEquals(readExpectedResourceFile("usage-my-cli-1-cmd.txt"), systemOut.getLogWithNormalizedLineSeparator());
  }

  @Test
  public void testCmd1Execute() {
    createCli().main(new String[] {"1-cmd"});
    assertEquals("my-cli 1-cmd\n", systemOut.getLogWithNormalizedLineSeparator());
  }

  @Test
  public void testRoute2Cmd1Usage() {
    createCli().main(new String[] {"-h", "2-route", "2-1-cmd"});
    assertEquals(readExpectedResourceFile("usage-my-cli-2-route-1-cmd.txt"), systemOut.getLogWithNormalizedLineSeparator());
  }

  @Test
  public void testRoute2Cmd1Execute() {
    createCli().main(new String[] {"2-route", "2-1-cmd"});
    assertEquals("my-cli 2-route 2-1-cmd opt1=null ARG1=[]\n", systemOut.getLogWithNormalizedLineSeparator());

    systemOut.clearLog();
    createCli().main(new String[] {"2-route", "2-1-cmd", "--opt1", "opt1-value"});
    assertEquals("my-cli 2-route 2-1-cmd opt1=[opt1-value] ARG1=[]\n", systemOut.getLogWithNormalizedLineSeparator());

    systemOut.clearLog();
    createCli().main(new String[] {"2-route", "2-1-cmd", "--opt1", "opt1-value", "VALUE1"});
    assertEquals("my-cli 2-route 2-1-cmd opt1=[opt1-value] ARG1=[VALUE1]\n", systemOut.getLogWithNormalizedLineSeparator());

    systemOut.clearLog();
    createCli().main(new String[] {"2-route", "2-1-cmd", "VALUE1", "--opt1", "opt1-value", "VALUE2"});
    assertEquals("my-cli 2-route 2-1-cmd opt1=[opt1-value] ARG1=[VALUE1, VALUE2]\n", systemOut.getLogWithNormalizedLineSeparator());
  }

  private CommandsCliMain createCli() {
    return CommandsCliMain.builder()
        .mainRoute(RouteDescriptor.builder("my-cli")
            .description("My Command Line Dummy Tool")
            .addSubCommand(CommandDescriptor.builder("1-cmd")
                .description("First level command 1")
                .factory(echoCommandFactory(Collections.<String>emptyList(), Collections.<String>emptyList()))
                .build())
            .addSubCommand(RouteDescriptor.builder("2-route")
                .description("First level route 2")
                .addSubCommand(CommandDescriptor.builder("2-1-cmd")
                    .description("Command 1 of route 2")
                    .factory(echoCommandFactory(Arrays.asList("opt1"), Arrays.asList("ARG1")))
                    .addOption(Option.builder().longOpt("opt1").hasArg().build())
                    .addArgument(Argument.builder("ARG1").multiplicityUnlimited().description("Argument 1").build())
                    .build())
                .build())
            .build())
        .build();
  }

  private CommandFactory echoCommandFactory(final List<String> options, final List<String> arguments) {
    return new CommandFactory() {
      @Override
      public Command create(final CommandContext commandContext) throws ParseException {
        return new Command() {
          @Override
          public void execute() throws CommandException {
            StringBuilder echo = new StringBuilder(commandContext.getCommandRoute().toString());
            for (String opt : options) {
              echo.append(" ").append(opt).append("=").append(Arrays.toString(commandContext.getCommandLine().getOptionValues(opt)));
            }
            for (String arg : arguments) {
              echo.append(" ").append(arg).append("=").append(commandContext.getArgumentValues(arg));
            }
            System.out.println(echo.toString());
          }
        };
      }
    };
  }

  private String readExpectedResourceFile(String resourceName) {
    String path = "/" + this.getClass().getName().replaceAll("\\.", "/") + "/" + resourceName;
    try (InputStream inputStream = this.getClass().getResourceAsStream(path)) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      StringWriter writer = new StringWriter();
      BufferedWriter bufferedWriter = new BufferedWriter(writer);
      String line;
      while ((line = reader.readLine()) != null) {
        bufferedWriter.write(line);
        bufferedWriter.newLine();
      }
      bufferedWriter.flush();
      return writer.toString().replaceAll("\r\n", "\n");
    } catch (IOException e) {
      throw new RuntimeException(e.toString(), e);
    }
  }
}