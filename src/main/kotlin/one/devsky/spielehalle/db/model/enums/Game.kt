package one.devsky.spielehalle.db.model.enums

import java.awt.Color

enum class Game(val color: Color = Color(0x7289DA)) {
    COINFLIP(Color(0xff9ff3)),
    SLOTS(Color(0x7289DA)),
    SLOTS_HIGH(Color(0x7289DA)),
    BLACKJACK(Color(0x7289DA)),
    BLACKJACK_HIGH(Color(0x7289DA)),
    ROULETTE(Color(0x7289DA)),
    COUNTER(Color(0x1dd1a1)),
    PFERDERENNEN(Color(0x7289DA)),
    HANGMAN, TICTACTOE, CONNECTFOUR, GUESSNUMBER, GUESSWORD,
    TRIVIA, QUIZ, WOULDYOURATHER, GUESSWHO, GUESSMOVIE,

    UNKNOWN
}