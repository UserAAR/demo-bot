# DevAAR Telegram Bot 🤖

DevAAR is a multi-functional Telegram bot built with Java and Spring Boot. It is designed to provide interactive services ranging from personal information and education details to fun commands and utility tools. This project aims to offer a flexible solution for both individual users and developers who want to explore or contribute to its functionality.

**Demo Bot:** [@LMSdemobot](https://t.me/LMSdemobot)  
*Try out our demo bot to explore its features. If you notice any missing functionality or have suggestions, future updates will address these improvements.*

---

## Table of Contents 📜
- [Features](#features-)
- [Technology Stack](#technology-stack-)
- [Installation](#installation-)
- [Configuration](#configuration-)
- [Usage](#usage-)
- [Project Structure](#project-structure-)
- [Development](#development-)
- [License](#license-)

---

## Features 🚀

### Core Features
- **Personal Info:** `/personal`, `/skills`, `/coding`  
  *Share and display your personal details, skills, and coding experiences.*

- **Education History:** `/education`  
  *Showcase your educational background.*

- **Contact Info:** `/contact`  
  *Provide easy access to your contact details.*

- **Hobbies:** `/hobbies`  
  *Track and share your hobbies.*

### Entertainment Commands
- Random jokes: `/joke`
- Motivational quotes: `/quote`
- Interesting facts: `/fact`
- Magic 8-Ball: `/magic8`
- Roll a dice: `/dice`
- Flip a coin: `/coinflip`

### Utility Tools
- Math calculations: `/calculate 5+3*2`
- Secure password generator: `/password 12`
- ASCII art generator: `/ascii Hello`
- Todo List management: `/todo add ...`

---

## Technology Stack 💻

- **Core Framework:** Java 21, Spring Boot 3.2
- **Telegram API:** TelegramBots 6.9.7
- **Data Parsing:** Jackson XML 2.15
- **Logging:** SLF4J + Logback
- **Build Tool:** Gradle

---

## Installation ⚙️

1. **Clone the repository:**
   ```bash
   git clone https://github.com/UserAAR/demo-bot.git
   ```

2. **Install dependencies:**
   ```bash
   mvn clean install
   ```

3. **Create the configuration file:**  
   Create a file at `src/main/resources/application.properties` and add the following:
   ```properties
   # src/main/resources/application.properties
   telegram.bot.username=YOUR_BOT_USERNAME
   telegram.bot.token=YOUR_BOT_TOKEN
   ```

---

## Configuration 🔧

**Required Parameters**
```properties
telegram.bot.username=  # Your bot username from BotFather
telegram.bot.token=     # Your bot token from BotFather
```

**Optional Parameters**
```properties
# File paths and limits
user.info.filepath=userdata.txt
cv.filepath=classpath:cv.pdf
```

---

## Usage 📋

**Command List:**

| Command   | Description                        | Example   |
|-----------|------------------------------------|-----------|
| `/start`  | Starts the bot                     | `/start`  |
| `/help`   | Displays the help menu             | `/help`   |
| `/cv`     | Sends a PDF version of the CV      | `/cv`     |
| `/weather`| Shows the weather for Baku         | `/weather`|

For detailed examples of each command, please refer to our demo bot.

---

## Project Structure 🏗️

The project is organized as follows:

```
src/
├── main/
│   ├── java/
│   │   └── com/devaar/
│   │       ├── bot/          # Bot logic and command handlers
│   │       ├── config/       # Spring configuration files
│   │       ├── model/        # Data Transfer Objects (DTOs)
│   │       └── util/         # Utility classes and helpers
│   └── resources/
│       ├── static/           # Static files (e.g., CV)
│       └── application.properties
```

This structure ensures the code is modular, easy to read, and maintainable.

---

## Development 🛠️

**Planned Improvements:**

- Integration with a PostgreSQL database
- A web-based admin panel
- A user statistics dashboard

*Note:* While the current implementation provides core functionality, additional features and improvements are planned for future updates. If you see any areas for enhancement, your contributions are welcome!

**Contribution Guidelines:**

1. **Run Tests:**  
   Before creating a pull request, make sure all tests pass.
   ```bash
   mvn test
   ```

2. **Follow Code Standards:**  
   Write clean and readable code. For example:
   ```java
   // Example of good coding practice
   public class Example {
       public void doSomething(String input) {
           if (input == null) {
               throw new IllegalArgumentException("Input cannot be null");
           }
           // Business logic here
       }
   }
   ```

Your contributions and suggestions will help shape the future of this project.

---

## License 📄

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

**Credits:**

- [TelegramBots library](https://github.com/rubenlagus/TelegramBots)
- Spring Boot Team

---

**Overview:**
- 🚀 A wide range of features and a modular structure
- 📋 Easy setup and configuration instructions
- 🛠️ Planned improvements for future updates
- 💻 A guide for developers to contribute effectively

Simply copy this into your `README.md` file and replace placeholder values (like `YOUR_BOT_TOKEN`). Enjoy exploring and enhancing the DevAAR Telegram Bot!
