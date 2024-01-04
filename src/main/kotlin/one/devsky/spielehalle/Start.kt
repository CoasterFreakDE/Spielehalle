package one.devsky.spielehalle

/**
 * The entry point of the program.
 *
 * This method is responsible for starting the execution of the program. It instantiates an instance of the `Neo` class
 * and catches any exception that might occur during the execution. If an exception occurs, it prints the stack trace.
 *
 * @see Neo
 */
fun main() {
    // Prevents the bot from creating windows while generating images
    System.setProperty("java.awt.headless", "true")

    Spielehalle()
}