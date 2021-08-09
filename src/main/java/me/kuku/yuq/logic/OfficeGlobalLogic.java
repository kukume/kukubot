package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.OfficeGlobalEntity;
import me.kuku.yuq.entity.OfficeRole;

import java.io.IOException;

@AutoBind
public interface OfficeGlobalLogic {
	Result<?> createUser(OfficeGlobalEntity officePojo, String displayName, String username, String password, Integer index) throws IOException;
	Result<?> userToAdmin(OfficeGlobalEntity officePojo, String mail, OfficeRole officeRole) throws IOException;
}