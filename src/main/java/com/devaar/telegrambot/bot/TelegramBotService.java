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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
                log.error("CV faylı tapılmadı: {}", CV_PATH);
            }
        } catch (IOException e) {
            log.error("CV yüklənərkən xəta: {}", e.getMessage());
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


            responseText = switch (receivedText.split(" ")[0]) {
                case "/start" -> "✨ Xoş gəlmişsiniz! Mən Java ilə yazılmış demo botam. Əmrlər üçün /help yazın.";
                case "/help" -> """
                    📜 Əlçatan Əmrlər:
                    /start - Başlanğıc mesajı
                    /help - Kömək menyusu
                    /info - Bot haqqında məlumat
                    /resume - CV'mə bax :)
                    /personal → Şəxsi məlumat
                    /skills → Əsas bacarıqlar
                    /coding → Kodlaşdırma bacarıqları
                    /education → Təhsil
                    /contact → Əlaqə məlumatı
                    /time - Cari vaxt
                    /date - Bugünün tarixi
                    /weather - Hava proqnozu
                    /joke - Zarafat maşını
                    /quote - Motivasiya sözü
                    /fact - Maraqlı fakt
                    /compliment - Təsadüfi kompliment
                    /roast - Zarafatlı tənqid
                    /dice - Zər at
                    /coinflip - Pul çevir
                    /magic8 - Sehrli 8-top
                    /calculate <ifadə> - Riyazi hesab
                    /password <uzunluq> - Təhlükəsiz şifrə
                    /ascii <mətn> - ASCII sənəti
                    /todo - Tapşırıqlar siyahısı
                    /timer <dəqiqə> - Geri sayım
                    /advice - Həyat məsləhəti
                    /contact - Əlaqə məlumatı
                    /settings - Parametrlər
                    /feedback - Geri bildirim
                    /about - Bot versiyası
                    /cv - CV'mi yüklə
                    /donate - Bana para ver,bana para verğğğğ...
                    """;
                case "/info" -> "🤖 Adım: DevAAR Bot\nYaradılıb: 2024\nDil: Java + Spring Boot";
                case "/time" -> "⏰ Cari vaxt: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                case "/date" -> "📅 Tarix: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

                case "/weather" -> randomBakuWeather();
                case "/joke" -> randomJoke();
                case "/quote" -> randomQuote();
                case "/fact" -> randomFact();
                case "/compliment" -> randomCompliment();
                case "/roast" -> randomRoast();
                case "/dice" -> "🎲 Zər nəticəsi: " + (new Random().nextInt(6) + 1);
                case "/coinflip" -> new Random().nextBoolean() ? "🪙 Nəticə: Xəritə" : "🪙 Nəticə: YAZI";
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
                case "/settings" -> "⚙️ Parametrlər: (Gəlişəcək)";
                case "/feedback" -> "💡 İstəyiniz var? Sadəcə yazın!";
                case "/about" -> "ℹ️ Versiya: 2.0\nYeniləmə: 31.01.2025";
                case "/cv", "/resume" -> {
                    sendCV(chatId);
                    yield null;
                }
                case "/donate" -> "😉 Təşəkkürlər! Dəstək üçün: BTC adresi: 1BvB...";
                default -> "🤷‍♂️ Əmr tanınmadı. /help yazın.";
            };

            sendResponse(chatId, responseText);
        }
    }

    private String randomBakuWeather() {
        String[] weathers = {
                "☀️ Bakı: 25°C, Günəşli",
                "⛅ Bakı: 18°C, Buludlu",
                "🌧️ Bakı: 12°C, Yağışlı",
                "🌪️ Bakı: 32°C, Külekli"
        };
        return weathers[new Random().nextInt(weathers.length)];
    }

    private String randomJoke() {
        String[] jokes = {
                "Niyə kompüter yorulmur? Çünki həmişə 'byte' yeyir!",
                "Proqramçı niyə qaranlıqda işləyir? Çünki 'bug'lardan qorxur! 😂"
        };
        return jokes[new Random().nextInt(jokes.length)];
    }

    private String getPersonalInfo() {
        return """
        👤 **Şəxsi Məlumat:**
        Ad: Amil Abdullazada
        Təsvir: Həvəsli birinci kurs kompüter elmləri tələbəsi. 
        Web proqramlaşdırmaya güclü maraq. 2 ildən çox təcrübə.
        """;
    }

    private String getKeySkills() {
        return """
        💼 **Əsas Bacarıqlar:**
        → Web Development (Django, Spring)
        → Data Science (Python, NumPy, Pandas)
        → Verilənlər Bazaları (MySQL, PostgreSQL)
        → Git & GitHub
        → AWS ilə tanışlıq
        → Komanda işi & Kommunikasiya
        """;
    }

    private String getCodingSkills() {
        return """
        💻 **Proqramlaşdırma:**
        → Python (Django, Data Science)
        → Java (Spring Framework)
        → PHP, HTML/CSS
        → SQL (MySQL, PostgreSQL)
        """;
    }

    private String getEducation() {
        return """
        🎓 **Təhsil:**
        → 2023-2027: Azərbaycan Dövlət İqtisad Universiteti (Kompüter Elmləri Bakalavr)
        → 2018-2023: FRITL Liseyi (Fizika-Riyaziyyat)
        """;
    }

    private String getContactInfo() {
        return """
        📞 **Əlaqə:**
        → Telefon: +994 50 774 65 85
        → Email: amil.abdullazada@gmail.com
        → LinkedIn: linkedin.com/in/aar-amil
        → Ünvan: 📍 Bakı, Azərbaycan
        """;
    }

    private String randomQuote() {
        String[] quotes = {
                "«Uğur kiçik səylərin təkrarıdır.» – Robert Kolier",
                "«Kod yazmaq şeir yazmaq kimidir.» – Alim Qasimov"
        };
        return "📜 " + quotes[new Random().nextInt(quotes.length)];
    }

    private String manageTodoList(Message message) {
        String chatId = message.getChatId().toString();
        String[] parts = message.getText().split(" ", 3); // Format: /todo <command> [parametr]
        String command = (parts.length > 1) ? parts[1].toLowerCase() : "";
        String task = (parts.length > 2) ? parts[2] : "";

        return switch (command) {
            case "add" -> addTodo(chatId, task);
            case "list" -> listTodos(chatId);
            case "remove" -> removeTodo(chatId, task);
            default -> """
            📋 Todo Əmrləri:
            - /todo add <tapşırıq> → Tapşırıq əlavə et
            - /todo list → Tapşırıqları göstər
            - /todo remove <nömrə> → Tapşırığı sil""";
        };
    }


    private String addTodo(String chatId, String task) {
        if (task.isEmpty()) return "❌ Tapşırıq yazın!";
        todoLists.computeIfAbsent(chatId, k -> new ArrayList<>()).add(task);
        return "✅ Əlavə edildi: " + task;
    }

    private String listTodos(String chatId) {
        List<String> tasks = todoLists.getOrDefault(chatId, List.of());
        if (tasks.isEmpty()) return "📭 Tapşırıq yoxdur!";
        StringBuilder sb = new StringBuilder("📋 Tapşırıqlar:\n");
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
                return "❌ Keçərsiz nömrə!";
            }
            String removedTask = tasks.remove(index);
            return "🗑 Silindi: " + removedTask;
        } catch (NumberFormatException e) {
            return "❌ Rəqəm daxil edin!";
        }
    }

    // CV göndərən metod
    private void sendCV(String chatId) {
        try {
            File cvFile = new ClassPathResource(CV_PATH).getFile();
            SendDocument document = new SendDocument(chatId, new InputFile(cvFile, "my_cv.pdf"));
            execute(document);
        } catch (IOException e) {
            sendMessage(chatId, "❌ CV faylı tapılmadı!");
            log.error("CV göndərilmədi: {}", e.getMessage());
        } catch (TelegramApiException e) {
            sendMessage(chatId, "❌ Göndərilmədi. Yenidən cəhd edin.");
            log.error("Telegram API xətası: {}", e.getMessage());
        }
    }

    private String randomFact() {
        String[] facts = {
                "Azərbaycan dünyada ən çox vulkanı olan ölkədir!",
                "Bakı dəniz səviyyəsindən 28 metr aşağıdadır."
        };
        return "🧠 " + facts[new Random().nextInt(facts.length)];
    }

    private String randomCompliment() {
        String[] compliments = {
                "Sən əsl bir kod sehrbazısan! 💻",
                "Gözəl gülüşün bütün botları əridər! 😊"
        };
        return "🌸 " + compliments[new Random().nextInt(compliments.length)];
    }

    private String randomRoast() {
        String[] roasts = {
                "Kodun elədir ki, sanki StackOverflow-dan kopyalayıb yapışdırmısan! 😜",
                "Yemək bişirmək bacarığın kimi kod yazırsan: hər şey yanmış! 🔥"
        };
        return "🔥 " + roasts[new Random().nextInt(roasts.length)];
    }

    private String magic8Ball() {
        String[] answers = {
                "Bəli, əmin ol! ✅",
                "Xeyr, heç vaxt! ❌",
                "Çətin sual... 🤔",
                "Serverdə problem var, sonra soruş! 🖥️"
        };
        return "🎱 " + answers[new Random().nextInt(answers.length)];
    }

    private String calculateExpression(String text) {
        try {
            String expr = text.substring("/calculate".length()).trim();
            Object result = new ScriptEngineManager().getEngineByName("JavaScript").eval(expr);

            if (result instanceof Number) {
                double numericResult = ((Number) result).doubleValue();
                return "🧮 Nəticə: " + numericResult;
            } else {
                return "❌ Riyazi ifadə yoxdur!";
            }
        } catch (ScriptException | NullPointerException e) {
            return "❌ Xətalı ifadə! Misal: /calculate 5+3*2";
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
            return "🔐 Təhlükəsiz şifrə: " + password;
        } catch (Exception e) {
            return "❌ Xəta! Misal: /password 12";
        }
    }

    private String generateAsciiArt(String text) {
        String input = text.substring("/ascii".length()).trim();
        return "🎨 ASCII Art:\n" + input.toUpperCase().chars()
                .mapToObj(c -> (char) c + " ")
                .collect(Collectors.joining("\n"));
    }
    private void sendMessage(String chatId,String responseText){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(responseText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message to Chat ID: {}. Error message: {}", chatId, e.getMessage());
        }
    }


    private String startTimer(String text, String chatId) {
        try {
            int minutes = Integer.parseInt(text.split(" ")[1]);

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(() -> {
                sendMessage(chatId, "⏰ " + minutes + " dəqiqə tamam oldu!");
            }, minutes, TimeUnit.MINUTES);

            return "⏳ Zamanlayıcı " + minutes + " dəqiqə üçün quruldu!";
        } catch (Exception e) {
            return "❌ Xəta! Misal: /timer 5";
        }
    }

    private String randomAdvice() {
        String[] advices = {
                "Hər gün 8 stəkan su iç! 💧",
                "Kod yazmazdan əvvəl planla! 📝"
        };
        return "💡 Məsləhət: " + advices[new Random().nextInt(advices.length)];
    }

    private void sendResponse(String chatId, String text) {
        if (text == null) return;
        SendMessage message = new SendMessage(chatId, text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Mesaj göndərilmədi: {}", e.getMessage());
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
