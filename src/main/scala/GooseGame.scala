object GooseGame /* extends App*/ {

  // (un)expected behaviour to handle:

  // check if name doesn't yet exist
  // after two users are registered do not accept any more of them. If input is any other than Space, give user a hint
  // check if moving input is correct, else give a hint: "To move a player type "Move name""
  // check if "move $name" is a registered name
  // check if it's the turn of the user from the input, else give user a hint

  // additional features if we still have time tomorrow:

  // provide a way to finish the game anytime - ask if the user wants to restart or just quit the app
  // provide a game description/rules on "about" input
  // provide a way to change names
  // tests
  // randomize who would be the first to roll

  // validate with Eithers?

  // Игорь
  // returns two registered users in the end, until then side-effects with printlns
  def register: (String, String) = ("", "")

  def start(): Unit = {

    println("Welcome to Goose Game!")

    val users: (String, String) = register

    println("Let's start!")

    // val input = scala.io.StdIn.readLine()

    play(users)
  }

  // Вадим
  // returns values from both dice
  def roll: (Int, Int) = (6, 6)

  def play(users: (String, String), positions: (Int, Int) = (0, 0)): Unit = {

    // get input
    // validate user: name exists, their turn

    // represent 0 with "Start"

    val user1: String = users._1
    val user2: String = users._2

    def playRound(turnOf: String, position1: Int = 0, position2: Int = 0): Unit =
      if (position1 != 63 && position2 != 63) {

        // wait for directive to roll

        val dice: (Int, Int) = roll

        def moveThis(oldPosition: Int): (String, Int) = move(turnOf, dice._1 + dice._2, oldPosition)

        def newPosition(oldPosition: Int): Int = {
          val moved = moveThis(oldPosition)
          println(moved._1)
          moved._2
        }

        val isTurnOfUser1 = turnOf == user1

        val nextUser: String = if (isTurnOfUser1) user2 else user1
        val updatePosition: Int = if (isTurnOfUser1) newPosition(position1) else newPosition(position2)

        if (isTurnOfUser1) playRound(nextUser, updatePosition, position2)
        else playRound(nextUser, position1, updatePosition)

      }
      else println("Game over")

    playRound(user1)
  }

  // returns new position and message
  // $user moves from $currentPosition to $newPosition"
  // The Goose message
  // The Bridge message
  // etc

  // here 0 as Start
  def move(user: String, diceSum: Int, currentPosition: Int): (String, Int) = diceSum + currentPosition match {
    case a if a > 63 => (s"$user moves from $currentPosition to 63. user Wins!!", 63)
    case newPosition => (s"$user moves from $currentPosition to $newPosition", newPosition)
  }

  //start()

}

object Test extends App {

  println(GooseGame.play(("John", "Mary")))

}