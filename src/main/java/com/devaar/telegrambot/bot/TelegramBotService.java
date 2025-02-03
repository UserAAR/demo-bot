package com.devaar.telegrambot.bot;

import com.devaar.telegrambot.util.UserInfoSaver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    private final UserInfoSaver userInfoSaver;
    // Todo List (ChatID → List<Task>)
    private final Map<String, List<String>> todoLists = new ConcurrentHashMap<>();

    public TelegramBotService(UserInfoSaver userInfoSaver) {
        this.userInfoSaver = userInfoSaver;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private static final String CV_PATH = "resources/cv.pdf";

    @PostConstruct
    public void init() {
        try {
            File cvFile = new ClassPathResource(CV_PATH).getFile();
            if (!cvFile.exists()) {
                log.error("CV couldn't found: {}", CV_PATH);
            }
        } catch (IOException e) {
            log.error("Error during the downloading CV: {}", e.getMessage());
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String receivedText = update.getMessage().getText().toLowerCase();
            Long userId = update.getMessage().getFrom().getId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            String username = update.getMessage().getFrom().getUserName();
            String responseText;

            actionLogger(chatId, receivedText, userId, firstName, lastName, username);
            userInfoSaver.saveInfos((username == null || "null".equals(username.trim()) ? "Unknown" : username) + " | " +
                    firstName + " " + lastName + " : " + userId + " | " + chatId);


            String command = receivedText.split(" ")[0].toLowerCase();

            responseText = switch (command) {
                case "/start" -> "✨ Welcome! I am a demo bot written in Java. Type /help for available commands.";
                case "/help" -> """
                📜 Available Commands:
                /start - Welcome message
                /help - Show this help menu
                /info - Bot info
                /resume - View my CV
                /personal - Personal information
                /skills - Key skills
                /coding - Coding skills
                /education - Education details
                /contact - Contact info
                /time - Current time
                /date - Today’s date
                convert <amount> <from_currency> <to_currency>
                💱 Convert currencies (e.g., `/convert 100 USD EUR`).
                /encrypt <shift> <text>
                🔐 Encrypt text using a Caesar cipher (e.g., `/encrypt 3 Hello` → "Khoor").
                /decrypt <shift> <text>
                🔓 Decrypt text encrypted with a Caesar cipher (e.g., `/decrypt 3 Khoor` → "Hello").
                /analyze <text>
                📊 Perform word frequency analysis on text (e.g., `/analyze This is a test. This is fun!`).
                /base64encode <text>
                🔡 Encode text in Base64 format (e.g., `/base64encode Hello` → "SGVsbG8=").
                /base64decode <encoded_text>** \s
                🔢 Decode Base64-encoded text (e.g., `/base64decode SGVsbG8=` → "Hello").
                /weather - Weather forecast
                /joke - Joke generator
                /quote - Motivational quote
                /fact - Interesting fact
                /compliment - Random compliment
                /roast - Playful roast
                /dice - Roll a dice
                /coinflip - Flip a coin
                /magic8 - Magic 8-ball
                /calculate <expression> - Math calculation
                /password <length> - Generate secure password
                /ascii <text> - Create ASCII art
                /todo - To-do list management
                /timer <minutes> - Start a countdown timer
                /advice - Random advice
                /settings - Settings (coming soon)
                /feedback - Leave feedback
                /about - Bot version info
                /cv - Send my CV
                /donate - Support me
                -------------------------------------
                /reminder <minutes> <message> - Set a reminder
                /news - Latest news headline
                /meme - Random meme text
                /horoscope <zodiac_sign> - Daily horoscope
                /wordoftheday - Get the word of the day
                /trivia - Random trivia fact
                /riddle - A riddle with its answer
                /reverse <text> - Reverse your text
                /upper <text> - Convert text to UPPERCASE
                /lower <text> - Convert text to lowercase
                /palindrome <text> - Check if text is a palindrome
                /wordcount <text> - Count the number of words
                /morse <text> - Convert text to Morse code
                /binary <text> - Convert text to binary
                /hex <text> - Convert text to hexadecimal
                /music - Music recommendation
                /movie - Movie recommendation
                /story - A short random story
                /guessgame - Play a guessing game
                /translate <target_language> <text> - Simulated translation
                """;
                case "/info" -> "🤖 Name: DevAAR Bot\nCreated: 2024\nTechnology: Java + Spring Boot";
                case "/time" -> "⏰ Current Time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                case "/date" -> "📅 Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                case "/convert" -> convertCurrency(receivedText);
                case "/encrypt" -> encryptText(receivedText);
                case "/decrypt" -> decryptText(receivedText);
                case "/analyze" -> analyzeText(receivedText);
                case "/base64encode" -> base64Encode(receivedText);
                case "/base64decode" -> base64Decode(receivedText);
                case "/weather" -> randomBakuWeather();
                case "/joke" -> randomJoke();
                case "/quote" -> randomQuote();
                case "/fact" -> randomFact();
                case "/compliment" -> randomCompliment();
                case "/roast" -> randomRoast();
                case "/dice" -> "🎲 Dice result: " + (new Random().nextInt(6) + 1);
                case "/coinflip" -> new Random().nextBoolean() ? "🪙 Result: Heads" : "🪙 Result: Tails";
                case "/magic8" -> magic8Ball();
                case "/calculate" -> calculateExpression(receivedText);
                case "/password" -> generatePassword(receivedText);
                case "/ascii" -> generateAsciiArt(receivedText);
                case "/todo" -> manageTodoList(update.getMessage());
                case "/timer" -> startTimer(receivedText, chatId);
                case "/advice" -> randomAdvice();
                case "/personal" -> getPersonalInfo();
                case "/skills" -> getKeySkills();
                case "/coding" -> getCodingSkills();
                case "/education" -> getEducation();
                case "/contact" -> getContactInfo();
                case "/reminder" -> setReminder(receivedText);
                case "/news" -> randomNews();
                case "/meme" -> randomMeme();
                case "/horoscope" -> horoscope(receivedText);
                case "/wordoftheday" -> wordOfTheDay();
                case "/trivia" -> randomTrivia();
                case "/riddle" -> randomRiddle();
                case "/reverse" -> reverseText(receivedText);
                case "/upper" -> toUpperCase(receivedText);
                case "/lower" -> toLowerCase(receivedText);
                case "/palindrome" -> checkPalindrome(receivedText);
                case "/wordcount" -> countWords(receivedText);
                case "/morse" -> toMorse(receivedText);
                case "/binary" -> toBinary(receivedText);
                case "/hex" -> toHex(receivedText);
                case "/music" -> randomMusicRecommendation();
                case "/movie" -> randomMovieRecommendation();
                case "/story" -> randomShortStory();
                case "/guessgame" -> guessGame(receivedText);
                case "/translate" -> translateText(receivedText);
                case "/settings" -> "⚙️ Settings: (Coming soon)";
                case "/feedback" -> "💡 Got suggestions? Just send them over!";
                case "/about" -> "ℹ️ Version: 2.0\nLast Updated: 31.01.2025";
                case "/cv", "/resume" -> {
                    sendCV(chatId);
                    yield null;
                }
                case "/donate" -> "😉 Thanks for your support! ";

                default -> "🤷‍♂️ Command not recognized. Type /help for available commands.";
            };


            if (responseText != null) {
                sendResponse(chatId, responseText);
            }
        }
    }
    private String randomBakuWeather() {
        String[] weathers = {
                "☀️ Baku: 25°C, Sunny",
                "⛅ Baku: 18°C, Cloudy",
                "🌧️ Baku: 12°C, Rainy",
                "🌪️ Baku: 32°C, Windy"
        };
        return weathers[new Random().nextInt(weathers.length)];
    }

    private String randomJoke() {
        String[] jokes = {
                "Why doesn't a computer get tired? Because it always 'bytes'! 😄",
                "Why does a programmer work in the dark? Because they're afraid of bugs! 😂"
        };
        return jokes[new Random().nextInt(jokes.length)];
    }

    private String getPersonalInfo() {
        return """
    👤 Personal Info:
    Name: Amil Abdullazada
    Description: Enthusiastic first-year computer science student.
    Passionate about web development with over 2 years of experience.
    """;
    }

    private String getKeySkills() {
        return """
    💼 Key Skills:
    → Web Development (Django, Spring)
    → Data Science (Python, NumPy, Pandas)
    → Databases (MySQL, PostgreSQL)
    → Git & GitHub
    → Familiarity with AWS
    → Teamwork & Communication
    """;
    }

    private String getCodingSkills() {
        return """
    💻 Programming Languages:
    → Python (Django, Data Science)
    → Java (Spring Framework)
    → PHP, HTML/CSS
    → SQL (MySQL, PostgreSQL)
    """;
    }

    private String getEducation() {
        return """
    🎓 Education:
    → 2023-2027: Azerbaijan State Economic University (B.Sc. in Computer Science)
    → 2018-2023: FRITL High School (Physics-Math)
    """;
    }

    private String getContactInfo() {
        return """
    📞 Contact Info:
    → Phone: +994 50 774 65 85
    → Email: amil.abdullazada@gmail.com
    → LinkedIn: linkedin.com/in/aar-amil
    → Address: 📍 Baku, Azerbaijan
    """;
    }

    private String randomQuote() {
        String[] quotes = {
                "“Success is the sum of small efforts, repeated day in and day out.” – Robert Collier",
                "“Coding is like writing poetry.” – Unknown"
        };
        return "📜 " + quotes[new Random().nextInt(quotes.length)];
    }

    private String manageTodoList(Message message) {
        String chatId = message.getChatId().toString();
        String[] parts = message.getText().split(" ", 3);
        String command = (parts.length > 1) ? parts[1].toLowerCase() : "";
        String task = (parts.length > 2) ? parts[2] : "";

        return switch (command) {
            case "add" -> addTodo(chatId, task);
            case "list" -> listTodos(chatId);
            case "remove" -> removeTodo(chatId, task);
            default -> """
        📋 Todo Commands:
        - /todo add <task> → Add a task
        - /todo list → Show tasks
        - /todo remove <number> → Remove a task
        """;
        };
    }

    private String addTodo(String chatId, String task) {
        if (task.isEmpty()) return "❌ Please enter a task!";
        todoLists.computeIfAbsent(chatId, k -> new ArrayList<>()).add(task);
        return "✅ Added: " + task;
    }

    private String listTodos(String chatId) {
        List<String> tasks = todoLists.getOrDefault(chatId, List.of());
        if (tasks.isEmpty()) return "📭 No tasks found!";
        StringBuilder sb = new StringBuilder("📋 Tasks:\n");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append(i + 1).append(". ").append(tasks.get(i)).append("\n");
        }
        return sb.toString();
    }

    private String removeTodo(String chatId, String indexStr) {
        try {
            int index = Integer.parseInt(indexStr) - 1;
            List<String> tasks = todoLists.get(chatId);
            if (tasks == null || index < 0 || index >= tasks.size()) {
                return "❌ Invalid number!";
            }
            String removedTask = tasks.remove(index);
            return "🗑 Deleted: " + removedTask;
        } catch (NumberFormatException e) {
            return "❌ Please enter a valid number!";
        }
    }

    private void sendCV(String chatId) {
        try {
            File cvFile = new ClassPathResource(CV_PATH).getFile();
            SendDocument document = new SendDocument(chatId, new InputFile(cvFile, "my_cv.pdf"));
            execute(document);
        } catch (IOException e) {
            sendMessage(chatId, "❌ CV file not found!");
            log.error("Failed to send CV: {}", e.getMessage());
        } catch (TelegramApiException e) {
            sendMessage(chatId, "❌ Failed to send. Please try again.");
            log.error("Telegram API error: {}", e.getMessage());
        }
    }

    private String randomFact() {
        String[] facts = {
                "Azerbaijan has the most volcanoes in the world! 🌋",
                "Baku is 28 meters below sea level! 🌊"
        };
        return "🧠 " + facts[new Random().nextInt(facts.length)];
    }

    private String randomCompliment() {
        String[] compliments = {
                "You're a true coding wizard! 💻✨",
                "Your smile could light up any room! 😊"
        };
        return "🌸 " + compliments[new Random().nextInt(compliments.length)];
    }

    private String randomRoast() {
        String[] roasts = {
                "Your code looks like you copied it from StackOverflow! 😜",
                "Your cooking skills are as burnt as your code! 🔥"
        };
        return "🔥 " + roasts[new Random().nextInt(roasts.length)];
    }

    private String magic8Ball() {
        String[] answers = {
                "Yes, definitely! ✅",
                "No, never! ❌",
                "That's a tough one... 🤔",
                "Server error, try again later! 🖥️"
        };
        return "🎱 " + answers[new Random().nextInt(answers.length)];
    }

    private String calculateExpression(String text) {
        try {
            String expr = text.substring("/calculate".length()).trim();
            Object result = new ScriptEngineManager().getEngineByName("JavaScript").eval(expr);
            if (result instanceof Number) {
                double numericResult = ((Number) result).doubleValue();
                return "🧮 Result: " + numericResult;
            } else {
                return "❌ No valid mathematical expression found!";
            }
        } catch (ScriptException | NullPointerException e) {
            return "❌ Invalid expression! Example: /calculate 5+3*2";
        }
    }

    private String generatePassword(String text) {
        try {
            int length = Integer.parseInt(text.split(" ")[1]);
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
            StringBuilder password = new StringBuilder();
            for (int i = 0; i < length; i++) {
                password.append(chars.charAt(new Random().nextInt(chars.length())));
            }
            return "🔐 Secure Password: " + password;
        } catch (Exception e) {
            return "❌ Error! Example: /password 12";
        }
    }

    private String generateAsciiArt(String text) {
        String input = text.substring("/ascii".length()).trim();
        return "🎨 ASCII Art:\n" + input.toUpperCase().chars()
                .mapToObj(c -> (char) c + " ")
                .collect(Collectors.joining("\n"));
    }

    private void sendMessage(String chatId, String responseText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(responseText);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Message not sent to Chat ID: {}. Error: {}", chatId, e.getMessage());
        }
    }

    private String startTimer(String text, String chatId) {
        try {
            int minutes = Integer.parseInt(text.split(" ")[1]);
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(() -> sendMessage(chatId, "⏰ " + minutes + " minute(s) are up!"), minutes, TimeUnit.MINUTES);
            return "⏳ Timer set for " + minutes + " minute(s)!";
        } catch (Exception e) {
            return "❌ Error! Example: /timer 5";
        }
    }

    private String setReminder(String commandText) {
        String[] parts = commandText.split(" ", 3);
        if (parts.length < 3) return "Usage: /reminder <minutes> <message>";
        try {
            int minutes = Integer.parseInt(parts[1]);
            String message = parts[2];
            return "⏰ Reminder set for " + minutes + " minute(s): " + message;
        } catch (NumberFormatException e) {
            return "❌ Invalid time. Please specify the number of minutes.";
        }
    }

    private String randomNews() {
        String[] news = {
                "📰 Breaking News: New Java update released!",
                "💻 Tech News: Spring Boot 3.2 is now available.",
                "🌍 World News: Community event celebrates innovation!",
                "🎬 Entertainment: A blockbuster movie just hit theaters!"
        };
        return news[new Random().nextInt(news.length)];
    }

    private String randomMeme() {
        String[] memes = {
                "When you code all night and it finally works... 🎉",
                "That moment when your code runs perfectly on the first try!",
                "Debugging: Where you become both the detective and the suspect."
        };
        return memes[new Random().nextInt(memes.length)];
    }

    private String horoscope(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2) return "Usage: /horoscope <zodiac_sign>";
        String sign = parts[1].trim();
        return "It's Haram Akhi,SubhanAllah";
    }

    private String wordOfTheDay() {
        String[][] words = {
                {"Serendipity", "The occurrence of events by chance in a happy or beneficial way."},
                {"Ethereal", "Extremely delicate and light—almost too perfect for this world."},
                {"Quintessential", "Representing the most perfect example of a quality or class."}
        };
        String[] word = words[new Random().nextInt(words.length)];
        return "📚 Word of the Day: " + word[0] + "\nDefinition: " + word[1];
    }

    private String randomTrivia() {
        String[] trivia = {
                "Did you know? The first computer bug was an actual moth! 🐛",
                "Trivia: Ada Lovelace is considered the first programmer! 👩‍💻",
                "Fun Fact: The QWERTY keyboard layout was designed in the 19th century! ⌨️"
        };
        return trivia[new Random().nextInt(trivia.length)];
    }

    private String randomRiddle() {
        String[][] riddles = {
                {"What has keys but can't open locks?", "A piano. 🎹"},
                {"What runs but never walks?", "A river. 🌊"},
                {"What has a face and two hands but no arms or legs?", "A clock. ⏰"}
        };
        String[] riddle = riddles[new Random().nextInt(riddles.length)];
        return "❓ Riddle: " + riddle[0] + "\n💡 Answer: " + riddle[1];
    }

    private String reverseText(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2) return "Usage: /reverse <text>";
        String text = parts[1];
        return new StringBuilder(text).reverse().toString();
    }

    private String toUpperCase(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2) return "Usage: /upper <text>";
        return parts[1].toUpperCase();
    }

    private String toLowerCase(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2) return "Usage: /lower <text>";
        return parts[1].toLowerCase();
    }

    private String checkPalindrome(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2) return "Usage: /palindrome <text>";
        String text = parts[1].replaceAll("\\s+", "").toLowerCase();
        String reversed = new StringBuilder(text).reverse().toString();
        return text.equals(reversed) ? "✅ The text is a palindrome." : "❌ The text is not a palindrome.";
    }

    private String countWords(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2) return "Usage: /wordcount <text>";
        String text = parts[1].trim();
        int count = text.isEmpty() ? 0 : text.split("\\s+").length;
        return "🔢 Word count: " + count;
    }

    private String toMorse(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2) return "Usage: /morse <text>";
        String text = parts[1].toUpperCase();
        Map<Character, String> morseMap = new HashMap<>();
        morseMap.put('A', ".-");    morseMap.put('B', "-...");  morseMap.put('C', "-.-.");
        morseMap.put('D', "-..");   morseMap.put('E', ".");     morseMap.put('F', "..-.");
        morseMap.put('G', "--.");   morseMap.put('H', "....");  morseMap.put('I', "..");
        morseMap.put('J', ".---");  morseMap.put('K', "-.-");   morseMap.put('L', ".-..");
        morseMap.put('M', "--");    morseMap.put('N', "-.");    morseMap.put('O', "---");
        morseMap.put('P', ".--.");  morseMap.put('Q', "--.-");  morseMap.put('R', ".-.");
        morseMap.put('S', "...");   morseMap.put('T', "-");     morseMap.put('U', "..-");
        morseMap.put('V', "...-");  morseMap.put('W', ".--");   morseMap.put('X', "-..-");
        morseMap.put('Y', "-.--");  morseMap.put('Z', "--..");
        morseMap.put('0', "-----"); morseMap.put('1', ".----"); morseMap.put('2', "..---");
        morseMap.put('3', "...--"); morseMap.put('4', "....-"); morseMap.put('5', ".....");
        morseMap.put('6', "-...."); morseMap.put('7', "--..."); morseMap.put('8', "---..");
        morseMap.put('9', "----.");
        StringBuilder morse = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (ch == ' ') {
                morse.append(" / ");
            } else {
                String code = morseMap.get(ch);
                morse.append(code != null ? code : "").append(" ");
            }
        }
        return morse.toString().trim();
    }

    private String toBinary(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2) return "Usage: /binary <text>";
        StringBuilder binary = new StringBuilder();
        for (char ch : parts[1].toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(ch)).replace(' ', '0')).append(" ");
        }
        return binary.toString().trim();
    }

    private String toHex(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2) return "Usage: /hex <text>";
        StringBuilder hex = new StringBuilder();
        for (char ch : parts[1].toCharArray()) {
            hex.append(String.format("%02X", (int) ch)).append(" ");
        }
        return hex.toString().trim();
    }

    private String randomMusicRecommendation() {
        String[] songs = {
                "🎵 'Bohemian Rhapsody' by Queen",
                "🎶 'Shape of You' by Ed Sheeran",
                "🎼 'Hotel California' by Eagles",
                "🎸 'Imagine' by John Lennon"
        };
        return songs[new Random().nextInt(songs.length)];
    }

    private String randomMovieRecommendation() {
        String[] movies = {
                "🎬 'Inception' directed by Christopher Nolan",
                "🎥 'The Matrix' directed by the Wachowskis",
                "🍿 'The Shawshank Redemption' directed by Frank Darabont",
                "🎞 'Interstellar' directed by Christopher Nolan"
        };
        return movies[new Random().nextInt(movies.length)];
    }

    private String convertCurrency(String commandText) {
        String[] parts = commandText.split(" ");
        if (parts.length < 4)
            return "Usage: /convert <amount> <from_currency> <to_currency> (e.g., /convert 100 USD EUR)";
        try {
            double amount = Double.parseDouble(parts[1]);
            String from = parts[2].toUpperCase();
            String to = parts[3].toUpperCase();

            Map<String, Double> ratesToUSD = Map.of(
                    "USD", 1.0,
                    "EUR", 1.1,
                    "GBP", 1.3,
                    "AZN", 0.59
            );

            if (!ratesToUSD.containsKey(from) || !ratesToUSD.containsKey(to))
                return "❌ Supported currencies: USD, EUR, GBP, AZN";

            double amountInUSD = amount / ratesToUSD.get(from);
            double converted = amountInUSD * ratesToUSD.get(to);
            return String.format("💱 %.2f %s = %.2f %s", amount, from, converted, to);
        } catch (NumberFormatException e) {
            return "❌ Invalid amount. Please enter a numeric value.";
        }
    }

    private String encryptText(String commandText) {
        String[] parts = commandText.split(" ", 3);
        if (parts.length < 3)
            return "Usage: /encrypt <shift> <text>";
        try {
            int shift = Integer.parseInt(parts[1]);
            String text = parts[2];
            StringBuilder result = new StringBuilder();
            for (char ch : text.toCharArray()) {
                if (Character.isLetter(ch)) {
                    char base = Character.isUpperCase(ch) ? 'A' : 'a';
                    ch = (char) ((ch - base + shift) % 26 + base);
                }
                result.append(ch);
            }
            return "🔒 Encrypted: " + result.toString();
        } catch (NumberFormatException e) {
            return "❌ Shift must be a number. Example: /encrypt 3 Hello";
        }
    }

    private String decryptText(String commandText) {
        String[] parts = commandText.split(" ", 3);
        if (parts.length < 3)
            return "Usage: /decrypt <shift> <text>";
        try {
            int shift = Integer.parseInt(parts[1]);
            String text = parts[2];
            return "🔓 Decrypted: " + encryptText("/encrypt " + (26 - (shift % 26)) + " " + text)
                    .replace("🔒 Encrypted: ", "");
        } catch (NumberFormatException e) {
            return "❌ Shift must be a number. Example: /decrypt 3 Khoor";
        }
    }

    private String analyzeText(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2)
            return "Usage: /analyze <text>";
        String text = parts[1].toLowerCase().replaceAll("[^a-z\\s]", "");
        String[] words = text.split("\\s+");
        Map<String, Integer> freq = new HashMap<>();
        for (String word : words) {
            if (!word.isEmpty())
                freq.put(word, freq.getOrDefault(word, 0) + 1);
        }
        StringBuilder sb = new StringBuilder("📊 Word Frequency Analysis:\n");
        freq.forEach((word, count) -> sb.append(word).append(": ").append(count).append("\n"));
        return sb.toString();
    }

    private String base64Encode(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2)
            return "Usage: /base64encode <text>";
        String encoded = Base64.getEncoder().encodeToString(parts[1].getBytes(StandardCharsets.UTF_8));
        return "🔐 Base64 Encoded: " + encoded;
    }

    private String base64Decode(String commandText) {
        String[] parts = commandText.split(" ", 2);
        if (parts.length < 2)
            return "Usage: /base64decode <encoded_text>";
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(parts[1]);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            return "🔓 Base64 Decoded: " + decoded;
        } catch (IllegalArgumentException e) {
            return "❌ Invalid Base64 encoded text.";
        }
    }


    private String randomShortStory() {
        String[] stories = {
                "Once upon a time in a world of code, bugs were the dragons and developers the knights. 🏰",
                "In a land where every error was a mystery, a brave programmer set out to conquer them all. 🗺️",
                "Amidst lines of code, a tiny bug sparked a grand adventure. 🚀"
        };
        return stories[new Random().nextInt(stories.length)];
    }

    private String guessGame(String commandText) {
        int number = new Random().nextInt(10) + 1;
        return "🤔 Guess a number between 1 and 10. My number is: " + number;
    }

    private String translateText(String commandText) {
        String[] parts = commandText.split(" ", 3);
        if (parts.length < 3) return "Usage: /translate <target_language> <text>";
        String targetLang = parts[1];
        String text = parts[2];
        return "🌐 Translated (" + targetLang + "): " + text;
    }

    private String randomAdvice() {
        String[] advices = {
                "Drink 8 glasses of water every day! 💧",
                "Plan before you code! 📝"
        };
        return "💡 Advice: " + advices[new Random().nextInt(advices.length)];
    }

    private void sendResponse(String chatId, String text) {
        if (text == null) return;
        SendMessage message = new SendMessage(chatId, text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Message not sent: {}", e.getMessage());
        }
    }

    private void actionLogger(String chatId, String receivedText, Long userId, String firstName, String lastName, String username) {
        log.info("User [{}] [{} {}] (Chat ID: {}) sent message: '{}'",
                username != null ? username : "Unknown", firstName, lastName, chatId, receivedText);
    }


    //    @Override
    public void onUpdateReceivedV0(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long userId = update.getMessage().getFrom().getId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            String username = update.getMessage().getFrom().getUserName();
            String chatId = update.getMessage().getChatId().toString();
            String receivedText = update.getMessage().getText();

            actionLogger(chatId, receivedText, userId, firstName, lastName, username);


            String responseText = switch (receivedText.toLowerCase()) {
                case "/start" ->
                        "Hello! I am a Telegram bot created by Amil (suspicious). 🤖 Use /help to see all features.";
                case "/help" -> """
                        📌 Command List:
                        - /start → Welcome message
                        - /info → Information about the bot
                        - /time → Current time
                        - /date → Today's date
                        - /weather <city> → Weather information
                        - /joke → Random joke
                        - /quote → Motivational quote
                        - /news → Daily news
                        - /fact → Interesting fact
                        - /calculate <operation> → Math operation
                        - /poll <question>? <answer1> <answer2> ... → Start a poll
                        """;
                case "/info" ->
                        "My name is DevAAR Bot. I am a Telegram bot built with Java and Spring Boot!";
                case "/time" -> "Current time: " + java.time.LocalTime.now();
                case "/date" -> "Today's date: " + java.time.LocalDate.now();
                case "/joke" ->
                        "😂 Random joke: 'Why do computers never get hungry? Because they always byte!' ";
                case "/quote" -> "📜 Motivational Quote: 'Success is the sum of small efforts, repeated day in and day out.'";
                case "/fact" ->
                        "🧠 Interesting Fact: 'The total weight of all ants on Earth is roughly equal to the total weight of all humans!'";
                default -> "Sorry, I don't recognize that command. 🤖 Type /help to see all commands.";
            };

            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(responseText);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Error sending message to Chat ID: {}. Error message: {}", chatId, e.getMessage());
            }
        }
    }
}
