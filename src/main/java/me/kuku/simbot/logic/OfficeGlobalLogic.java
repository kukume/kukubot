package me.kuku.simbot.logic;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.OfficeGlobalEntity;
import me.kuku.simbot.entity.OfficeRole;

import java.io.IOException;

public interface OfficeGlobalLogic {
	Result<?> createUser(OfficeGlobalEntity officePojo, String displayName, String username, String password, Integer index) throws IOException;
	Result<?> userToAdmin(OfficeGlobalEntity officePojo, String mail, OfficeRole officeRole) throws IOException;
}