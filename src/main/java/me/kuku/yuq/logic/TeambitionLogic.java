package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.TeambitionPojo;

import java.io.IOException;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
@AutoBind
public interface TeambitionLogic {
	Result<TeambitionPojo> login(String phone, String password) throws IOException;
	Result<String> uploadToProject(TeambitionPojo teambitionPojo, String projectName, byte[] bytes, String...path) throws IOException;
	Result<String> fileDownloadUrl(TeambitionPojo teambitionPojo, String projectName, String...path) throws IOException;
}
