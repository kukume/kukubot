package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.TeambitionPojo;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
@AutoBind
public interface TeambitionLogic {
	Result<TeambitionPojo> login(String phone, String password) throws IOException;
	Result<TeambitionPojo> getAuth(TeambitionPojo teambitionPojo) throws IOException;
	Result<TeambitionPojo> project(TeambitionPojo teambitionPojo, String name) throws IOException;
	Result<String> uploadToProject(TeambitionPojo teambitionPojo, byte[] bytes, String...path) throws IOException;
	Result<String> fileDownloadUrl(TeambitionPojo teambitionPojo, String...path) throws IOException;
}
