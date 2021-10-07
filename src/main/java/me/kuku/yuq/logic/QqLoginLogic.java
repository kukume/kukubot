package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.QqLoginEntity;
import me.kuku.yuq.pojo.GroupMember;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@AutoBind
public interface QqLoginLogic {
	Result<Map<String, String>> groupUploadImage(QqLoginEntity QqLoginEntity, String url) throws IOException;
	Result<List<GroupMember>> groupMemberInfo(QqLoginEntity QqLoginEntity, Long group) throws IOException;
}
