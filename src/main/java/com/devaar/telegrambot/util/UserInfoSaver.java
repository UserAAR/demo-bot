package com.devaar.telegrambot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;

@Component
@Slf4j
public class UserInfoSaver {

    public void saveInfos(String userInformation){
        File file = new File("/home/aar/IdeaProjects/telegram-bot/src/main/resources/ID.txt");
        try(FileWriter fw = new FileWriter(file,true)){
            fw.write(userInformation);
            fw.write("\n");
            log.info("User information successfully saved: {}", userInformation);
        } catch (Exception e) {
            log.error("Failed to save user information to file '{}'. Error: {}", file, e.getMessage(), e);
        }
    }
}
