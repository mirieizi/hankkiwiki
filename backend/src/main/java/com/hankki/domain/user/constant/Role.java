package com.hankki.domain.user.constant;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority{
	ROLE_USER("U"), ROLE_ADMIN("A");
	
	private final String code;
	
	Role(String code){
		this.code = code;
	}
	/** DB 저장 코드 값 반환 */
	public String getCode() {
		return code;
	}
	/** DB에서 읽어온 코드로부터 enum 찾기 */
	public static Role fromCode(String dbCode) {
		for (Role r : values()) {
			if (r.code.equals(dbCode)) {
				return r;
			}
		}
		throw new IllegalArgumentException("Unknown Role Code: "+dbCode);
	}

	@Override
	public String getAuthority() {
		return name();
	}
}
