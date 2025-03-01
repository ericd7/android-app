# Android Game

A simple Android game application template built with Kotlin.

## Project Structure

This project follows the standard Android application structure:

- `app/src/main/java/com/example/androidgame/`: Contains the Kotlin source code
  - `MainActivity.kt`: The entry point of the application
  - `GameActivity.kt`: Hosts the game view
  - `GameView.kt`: Renders the game and handles user input
  - `GameThread.kt`: Manages the game loop
- `app/src/main/res/`: Contains resources like layouts, strings, and drawables
  - `layout/`: XML layout files
  - `values/`: String, color, and style definitions

## Development Workflow

### Prerequisites

- Android Studio (for building and running)
- Cursor (for code editing)
- Android SDK 21 or higher
- Kotlin 1.8.10 or newer

### Workflow

1. Edit code in Cursor
2. Open the project in Android Studio to build and run
3. Test on an emulator or physical device through Android Studio

You can use the `open-in-android-studio.bat` script to quickly open the project in Android Studio.

## Game Development

This template provides the basic structure for an Android game:

- A game loop that maintains a consistent frame rate
- Surface view for efficient rendering
- Touch event handling
- Activity lifecycle management

To develop your game:

1. Modify the `GameView.update()` method to update your game state
2. Modify the `GameView.draw()` method to render your game elements
3. Add game objects and logic as needed

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Android Developer Documentation
- Kotlin Programming Language 
