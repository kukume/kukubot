package me.kuku.yuq.logic;

import me.kuku.yuq.pojo.Result;

import java.io.IOException;

public interface QQAILogic {
    boolean pornIdentification(String imageUrl) throws IOException;
    String generalOCR(String imageUrl) throws IOException;
    String textChat(String question, String session) throws IOException;
    Result<byte[]> voiceSynthesis(String text) throws IOException;
}
