package game

import game.utils.Rules._
import game.utils.TextContainer._
import game.utils._

/**
 * The main class. GooseGame contains methods used for game process controlling.
 */
object GooseGame extends App {

  /**
   * Util method that starts an application.
   */
  def start(): Unit = {

    // welcoming message
    println(HELLO_MESSAGE)

    // start receiving commands: play, about, etc.
    CommandProcessor.initialCommandProcessing()
  }

  /**
   * Util method represents a 2 dice roll for some user. It includes command validation too.
   *
   * @param turnOf Current user
   * @return 2 dice roll for a user
   */
  def roll(turnOf: String): Roll = {

    val input = InputMatcher.getInput

    // convenience value for exiting and restarting
    val emptyDice = (0, 0)

    def rollDice: Roll = roll(turnOf)

    def moveUser(): Roll = {
      val dice = RandomUtil.roll()
      println(s"$turnOf rolled dice: " + dice._1 + ", " + dice._2)
      dice
    }

    def wrongUser(): Roll = {
      println(s"""Wrong user, try "move $turnOf"""")
      rollDice
    }

    def badInput(): Roll = {
      println(s"""Unknown command, try "move $turnOf"""")
      rollDice
    }

    def exit(): Roll = {
      println(BYE_MESSAGE)
      System.exit(0)
      emptyDice
    }

    def restart(): Roll = {
      GooseGame.start()
      emptyDice
    }

    // matches on the input and acts accordingly
    InputMatcher.getType(input, Some(turnOf)) match {
      case MoveUser => moveUser()
      case BadUser => wrongUser()
      case Exit => exit()
      case Restart => restart()
      case _ => badInput()
    }
  }

  /**
   * Method that represents a single move. It contains a set of internal methods.
   *
   * @param user    Current user
   * @param diceSum Sum of 2 dice rolled
   * @param users   A collection of all users
   * @return A tuple of (String, Map[String, Int]) type. String represents a message describing a move.
   *         Map[String, Int] represents an updated list of users that will be used during next turn.
   */
  def move(user: String, diceSum: Int, users: Users): (String, Users) = {

    // handle emptiness
    val currentPosition: Int = users(user)

    def positionName: String = if (currentPosition == 0) "the Start" else s"$currentPosition"

    // basic movement message
    def message(position: String): String =
      s"$user moves from $positionName to $position"

    // message for the case when a player is knocked out from their position
    def bouncesMessage(position: Int): String =
      message(s"$END") + s".\n$user bounces! $user returns to ${2 * END - position}"

    // part of a message for the case when user stepped on a goose position,
    // is used in gooseMessage and doubleGooseMessage
    def gooseMessagePart(position: Int): String =
      s", The Goose.\n$user moves again and goes to ${position + diceSum}"

    // compose a message from a basic movement message and a goose message
    def gooseMessage(position: Int): String =
      message(s"$position" + gooseMessagePart(position))

    // compose a message from gooseMessage and another goose message part
    def doubleGooseMessage(position: Int): String =
      gooseMessage(position) + gooseMessagePart(position + diceSum)

    // compose a prank message
    def prankMessage(user: (String, Int), addition: Option[String] = None): String = addition match {
      case Some(a) => a + s".\nOn ${user._2} there is ${user._1}, who returns to $positionName."
      case _ => s".\nOn ${user._2} there is ${user._1}, who returns to $currentPosition"
    }

    // move both players: the knocked out one and the current player
    def prankMove(message: String, pair: (String, Int)): (String, Users) = {
      val newUsers = users + (pair._1 -> currentPosition) + (user -> pair._2)
      (prankMessage(pair, Some(message)), newUsers)
    }

    // check if anyone is standing in the position where current user is supposed to move
    // if someone is - move them back, to where current user was standing and move the current user
    // else just move the current user
    // bonus parameter is used and set when special position, like goose is involved, giving additional move
    def prankOrMove(position: Int, message: String, bonus: Int = 0): (String, Users) =
      users.find(_._2 == position + bonus) match {
        case Some(pair) => prankMove(message, pair)
        case _ => (message, users + (user -> (position + bonus)))
      }

    // match on the position, where user is supposed to land
    // take according action depending on the position type: goose, bridge, simple point, victory, etc.
    diceSum + currentPosition match {
      case a if a > END => (bouncesMessage(a), users + (user -> (2 * END - a)))
      case END => (message(s"$END") + s".\n$user Wins!!", users + (user -> END))
      case BRIDGE_START => prankOrMove(BRIDGE_END, message(s"The Bridge.\n$user jumps to $BRIDGE_END."))
      case a if GEESE.contains(a) && GEESE.contains(a + diceSum) => prankOrMove(a + diceSum, doubleGooseMessage(a), diceSum)
      case a if GEESE.contains(a) => prankOrMove(a, gooseMessage(a), diceSum)
      case a => prankOrMove(a, message(s"$a"))
    }
  }

  /**
   * Util method that starts a game.
   *
   * @param users A collection of players.
   */
  def play(users: Users): Unit = {

    // outputs the current score
    def outputScore(turnOf: String, users: Users): Unit = {
      println("\nScore: ")
      for ((name, position) <- users) println(s"name: $name, position: $position")
      println(s"\n$turnOf make your move!")
    }

    def playRound(turnOf: String, users: Users): Unit =

    // check if any user is at position 63, and leave if true
      if (users.exists(_._2 == END)) println(GAME_OVER_MESSAGE)
      else {

        // print where everyone is standing
        outputScore(turnOf, users)

        // roll the dice
        val rolled: Roll = roll(turnOf)

        // do the moving
        val moved = move(turnOf, rolled._1 + rolled._2, users)

        // output moving message
        println(moved._1)

        // find the user, who's turn to go is next
        val nextUser: String = CommandProcessor.nextUser(users, turnOf)

        // play another round, pass next user and users with updated positions
        playRound(nextUser, moved._2)
      }

    // give a hint about who is the next to go
    println(MOVE_HINT)

    // starts the first round of the game
    playRound(RandomUtil.selectFirst(users), users)
  }

  // starts the game
  start()

}