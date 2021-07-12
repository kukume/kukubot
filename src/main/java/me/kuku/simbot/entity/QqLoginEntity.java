package me.kuku.simbot.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.kuku.utils.QqUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;

@Entity
@Table(name = "qq_login")
@Getter
@Setter
@NoArgsConstructor
public class QqLoginEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@OneToOne
	@JoinColumn(name = "qq")
	private QqEntity qqEntity;
	private String sKey;
	private String psKey;
	private String superKey;
	private String superToken;
	private String pt4Token;

	public QqLoginEntity(QqEntity qqEntity){
		this.qqEntity = qqEntity;
	}

	@JSONField(serialize = false)
	@Transactional
	public String getCookie(){
		return String.format("pt2gguin=o0%s; uin=o0%s; skey=%s; ", qqEntity.getQq(), qqEntity.getQq(), sKey);
	}

	@JSONField(serialize = false)
	@Transactional
	public String getCookie(String psKey){
		return String.format("%sp_skey=%s; p_uin=o0%s;", getCookie(), psKey, qqEntity.getQq());
	}

	@JSONField(serialize = false)
	@Transactional
	public String getCookieWithPs(){
		return String.format("%sp_skey=%s; p_uin=o0%s; ", getCookie(), psKey, qqEntity.getQq());
	}

	@JSONField(serialize = false)
	@Transactional
	public String getCookieWithSuper(){
		return String.format("superuin=o0%s; superkey=%s; supertoken=%s; ", qqEntity.getQq(), superKey, superToken);
	}

	@JSONField(serialize = false)
	public String getGtk(){
		return String.valueOf(QqUtils.getGTK(sKey));
	}

	@JSONField(serialize = false)
	public String getGtk(String psKey){
		return String.valueOf(QqUtils.getGTK(psKey));
	}

	@JSONField(serialize = false)
	public String getGtk2(){
		return QqUtils.getGTK2(sKey);
	}

	@JSONField(serialize = false)
	public String getGtkP(){
		return String.valueOf(QqUtils.getGTK(psKey));
	}

	@JSONField(serialize = false)
	public String getToken(){
		return String.valueOf(QqUtils.getToken(superToken));
	}

	@JSONField(serialize = false)
	public String getToken2(){
		return String.valueOf(QqUtils.getToken2(superToken));
	}

}
