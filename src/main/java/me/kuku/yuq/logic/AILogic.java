package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;

@AutoBind
public interface AILogic {
    boolean pornIdentification(String imageUrl) throws IOException;
    String generalOCR(String imageUrl) throws IOException;
    String textChat(String question, String session) throws IOException;
    Result<byte[]> voiceSynthesis(String text) throws IOException;
    String voiceIdentify(String voiceUrl) throws IOException;
}
