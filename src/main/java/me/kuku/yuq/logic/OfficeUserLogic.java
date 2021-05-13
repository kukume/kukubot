package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.OfficePojo;
import me.kuku.yuq.pojo.OfficeRole;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;

@AutoBind
public interface OfficeUserLogic {
	Result<?> createUser(OfficePojo officePojo, String displayName, String username, String password, Integer index) throws IOException;
	Result<?> userToAdmin(OfficePojo officePojo, String mail, OfficeRole officeRole) throws IOException;
}
