package userModule.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * The `UserLoginSchema` class represents the schema of a user login.
 */
public class UserLoginSchema {

	@JsonProperty("nickname")
	private String nickname;

	@JsonProperty("password")
	private String password;

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname(final String nickname) {
		this.nickname = nickname;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.nickname == null ? 0 : this.nickname.hashCode());
		result = prime * result + (this.password == null ? 0 : this.password.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final UserLoginSchema other = (UserLoginSchema) obj;
		if (this.nickname == null) {
			if (other.nickname != null) {
				return false;
			}
		} else if (!this.nickname.equals(other.nickname)) {
			return false;
		}
		if (this.password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!this.password.equals(other.password)) {
			return false;
		}
		return true;
	}

}
