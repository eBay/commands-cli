# commands-cli

[![Build Status](https://travis-ci.org/eBay/commands-cli.svg?branch=master)](https://travis-ci.org/eBay/commands-cli)
[![Code Quality](https://api.codacy.com/project/badge/Grade/1b1f6836a8b74f56b212f53b281215ee)](https://www.codacy.com/app/eBay/commands-cli?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=eBay/commands-cli&amp;utm_campaign=Badge_Grade)
[![Code Coverage](https://api.codacy.com/project/badge/Coverage/1b1f6836a8b74f56b212f53b281215ee)](https://www.codacy.com/app/eBay/commands-cli?utm_source=github.com&utm_medium=referral&utm_content=eBay/commands-cli&utm_campaign=Badge_Coverage)
[![GitHub](https://img.shields.io/github/license/ebay/commands-cli.svg)](LICENSE.txt)

An opinionated extension to the [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) library which adds support for commands.

## Main Features

Extends the Apache Commons CLI, adding the following features:

* Command arguments parsing and validation
* Simple command execution pattern:
  ```
  my-cli [OPTIONS] [ARGS]
  ```
* Complex command routing patterns, e.g. git-flow style:
  ```
  git flow feature start <NAME>
  git flow feature finish <NAME> 
  git flow release start <VERSION>
  git flow release finish <VERSION>
  git flow support start <VERSION> <TAG>
  ```  
* Built-in usage help option for every route and command:
  ```
  $ my-cli --help
  usage: my-cli [OPTIONS]

  My command line tool

  Options:
   -h,--help   Show this help
  ```

## Usage

### Maven Dependency

```xml
<dependency>
  <groupId>com.ebay.sd.commons</groupId>
  <artifactId>commands-cli</artifactId>
  <version>${commands-cli.version}</version>
</dependency>
```

### Simple Example

Define the executable command:

```java
public class MyCommand extends AbstractCommand {
  
  public static final CommandDescriptor DESCRIPTOR = CommandDescriptor.builder("my-cli")
      .description("This is my command line tool")
      .addOption(Option.builder("v").longOpt("verbose").build())
      .addArgument(Argument.builder("FILE").description("The input file").required().build())
      .factory(new CommandFactory() {
        @Override
        Command create(CommandContext commandContext) throws ParseException {
          return new MyCommand(commandContext);
        }
      })
      .build();
  
  public MyCommand(CommandContext commandContext) {
    super(commandContext);
  }
  
  @Override
  protected void validate(CommandContext commandContext) throws ParseException {
    //Validate the command input if needed
  }
  
  @Override
  public void execute() throws CommandException {
    //Do what ever you want to do, use getCommandContext() to check options, get arguments, etc.
  }
}
```

In the `main` method - define and run the program:

```java
public class Main {
  public static void main(String[] args) {
    CommandsCliMain.builder().mainCommand(MyCommand.DESCRIPTOR).build().main(args);
  }
}
```

The example above defines a simple command line interface which has a single option (flag) `-v, --verbose` 
(defined and implemented by the program) and a single required argument `FILE`. 
The actual execution is done by the command class `MyCommand`.  
To invoke it from the command line (assuming `my-cli` is an executable/script which invokes `main`), 
a user shall use the following usage pattern:
```
my-cli [OPTIONS] FILE
```
For example, with verbosity flag turned on: 
```
my-cli --verbose path/to/file
```

### Advanced Example

Continuing the git-flow example, define the commands, e.g. the `git flow feature start <NAME>` command 
with an optional `--showcommands` option: 

```java
public class GitFlowFeatureStartCommand extends AbstractCommand {
  public static final CommandDescriptor DESCRIPTOR = CommandDescriptor.builder("start")
    .description("Start a feature branch")
    .addOption(Option.builder().longOpt("showcommands").desc("Show git commands while executing them").required(false).build())
    .addArgument(Argument.builder("NAME").description("Feature name").required().build())
    .factory(new CommandFactory() {
      @Override
      public Command create(CommandContext commandContext) throws ParseException {
        return new GitFlowFeatureStartCommand(commandContext);
      }
    })
    .build();
  
  //etc...
}
```

In the main program, build routing to the commands and the run it, passing the command line arguments.

```java
public class Main {
  public static void main(String[] args){
    //Define the routes to the commands
    RouteDescriptor git = RouteDescriptor.builder("git")
        .description("The git command line tool")
        .addSubCommand(
            RouteDescriptor.builder("flow")
                .description("git-flow extensions")
                .addSubCommand(
                    RouteDescriptor.builder("feature")
                        .description("git-flow feature branch related operations")
                        .addSubCommand(GitFlowFeatureStartCommand.DESCRIPTOR)
                        .addSubCommand(GitFlowFeatureFinishCommand.DESCRIPTOR)
                        .build())
                .addSubCommand(
                    RouteDescriptor.builder("release")
                        .description("git-flow release branch related operations")
                        .addSubCommand(GitFlowReleaseStartCommand.DESCRIPTOR)
                        .addSubCommand(GitFlowReleaseFinishCommand.DESCRIPTOR)
                        .build())
                .addSubCommand(
                    RouteDescriptor.builder("support")
                        .description("git-flow support branch related operations")
                        .addSubCommand(GitFlowSupportStartCommand.DESCRIPTOR)
                        .build())
                .build())
        .build();
    
    //Build and run the main program
    CommandsCliMain.builder().mainRoute(git).build().main(args);
  }
}
```

### Usage Help

By default, a help option is added to each route and command:
```
 -h,--help   Show this help
```

This can be customized using the context data. For example:
```java
public class Main {
  public static void main(String[] args){
    HelpFormatter myHelpFormatter = createMyHelpFormatter();
    Option showHelpOpt = Option.builder()
        .longOpt("show-help")
        .desc("Show my-cli help")
        .build();
    
    Map<String, Object> ctxData = new HashMap<>();
    ctxData.put(UsageHelp.CTX_HELP_OPTION, showHelpOpt);
    ctxData.put(UsageHelp.CTX_HELP_FORMATTER, myHelpFormatter); 
    ctxData.put(UsageHelp.CTX_HELP_OPTION_AUTO_ADD, false);
    
    CommandsCliMain cli = CommandsCliMain.builder()
        .contextData(ctxData)
        .mainRoute(root)
        .build();
    cli.main(args); 
  }
  //...
}
```

  
----
  
## License  

Copyright 2018 eBay Inc.  
Developer: [Yinon Avraham](https://github.com/yinonavraham)

Use of this source code is governed by an Apache-2.0-style  
license that can be found in the [LICENSE](LICENSE.txt) file or at  
http://www.apache.org/licenses/LICENSE-2.0.
