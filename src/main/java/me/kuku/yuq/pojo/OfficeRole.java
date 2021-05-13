package me.kuku.yuq.pojo;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum OfficeRole {
	USER_ADMIN("fe930be7-5e62-47db-91af-98c3a49a38b1"),
	GLOBAL_ADMIN("62e90394-69f5-4237-9190-012177145e10"),
	PRIVILEGED_ROLE_ADMIN("e8611ab8-c189-46e8-94e1-60213ab1f814");

	private String value;

	OfficeRole(String value){
		this.value = value;
	}
}
