# Telegram Bot Service

## Description

This project implements a Telegram bot service using Java and Spring Boot. The bot provides various functionalities, including message sending, responding to specific commands, and interacting with users in a dynamic manner. The bot is capable of handling messages, sending media, and responding to specific commands such as `/start`, `/help`, `/info`, and more. This bot can be used as part of a larger system or independently to interact with users on Telegram.

## Features

- **Command-Based Responses:** The bot responds to commands such as:
  - `/start`: Initializes the bot and sends a greeting message.
  - `/help`: Provides instructions on how to interact with the bot.
  - `/info`: Returns information about the bot and its capabilities.
  - `/time`: Displays the current server time.
  - `/date`: Displays the current date.
  - `/joke`: Sends a random joke.
  - `/quote`: Sends a motivational quote.
  - `/fact`: Sends a random fun fact.

- **Message Handling:** The bot is capable of handling various types of messages including:
  - Text messages
  - Media files (photos, documents, etc.)
  - Locations

- **Logging:** The bot includes logging for incoming messages, errors, and important events using **SLF4J**.

## Technologies Used

- **Java** - The programming language used for implementing the bot.
- **Spring Boot** - Framework used to structure the application and create the necessary components (controllers, services).
- **Telegram Bot API** - The Telegram Bot API that provides methods to interact with the Telegram platform.
- **SLF4J** - Used for logging messages.
- **Telegram Bots Java Library** - A library used to simplify the interaction with the Telegram Bot API.

## Requirements

Before running the project, make sure you have the following:

- **Java 11** or higher installed on your machine.
- **Spring Boot** for creating and managing the application.
- **Telegram Bot Token** from [BotFather](https://core.telegram.org/bots#botfather) to authenticate your bot with the Telegram platform.

## Setup and Installation

### 1. Clone the repository

First, clone this repository to your local machine:

```bash
git clone https://github.com/UserAAR/telegram-bot-service.git
cd telegram-bot-service

### 2. Configure application properties

Inside the `src/main/resources/application.properties` file, configure your bot credentials:

```properties
telegram.bot.username=YourBotUsername
telegram.bot.token=YourBotToken
