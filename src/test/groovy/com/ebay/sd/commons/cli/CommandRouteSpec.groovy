/* *********************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an Apache-2.0-style
license that can be found in the LICENSE.txt file or at
http://www.apache.org/licenses/LICENSE-2.0.
************************************************************/

package com.ebay.sd.commons.cli

import spock.lang.Specification

class CommandRouteSpec extends Specification {

  def 'fail get command if not set'() {
    given:
    def route = CommandRoute.builder().build()

    expect:
    !route.hasCommand()

    when:
    route.getCommand()
    then:
    def e = thrown(IllegalStateException)
    e.message == 'command was not set'
  }

  def 'Build minimal command route'() {
    given:
    def command = mockCommandDescriptor('foo')
    def route = CommandRoute.builder().command(command).build()

    expect:
    route.hasCommand()
    route.command == command
    route.path.empty
    route.fullPathAsString == 'foo'
  }

  def 'Build full command route'() {
    given:
    def route1 = mockRouteDescriptor('foo')
    def route2 = mockRouteDescriptor('bar')
    def route3 = mockRouteDescriptor('baz')
    def command = mockCommandDescriptor('coo')
    def route = CommandRoute.builder().addToPath(route1).addToPath(route2).addToPath(route3).command(command).build()

    expect:
    route.command == command
    route.path == [route1, route2, route3]
    route.fullPathAsString == 'foo bar baz coo'
  }

  def 'Fail to add to path after a command was already set'() {
    given:
    def builder = CommandRoute.builder().command(Mock(CommandDescriptor))

    when:
    builder.addToPath(Mock(RouteDescriptor))
    then:
    def e = thrown(IllegalStateException)
    e.message == 'command already set'
  }

  def 'Fail to set command after a command was already set'() {
    given:
    def builder = CommandRoute.builder().command(Mock(CommandDescriptor))

    when:
    builder.command(Mock(CommandDescriptor))
    then:
    def e = thrown(IllegalStateException)
    e.message == 'command already set'
  }

  private CommandDescriptor mockCommandDescriptor(String name = null) {
    Mock(CommandDescriptor) {
      getName() >> name
    }
  }

  private RouteDescriptor mockRouteDescriptor(String name = null) {
    Mock(RouteDescriptor) {
      getName() >> name
    }
  }
}
