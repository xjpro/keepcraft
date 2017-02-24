package keepcraft.data.models;

import java.util.Arrays;

public enum UserPrivilege {

	// NOTE! Critically important that permission ids be in ascending order of access
	ADMIN(400, "Admin"),
	MEMBER_VETERAN(200, "Knight"),
	MEMBER_NORMAL(150, "Squire"),
	MEMBER_START(100, "Soldier"),
	NONMEMBER(50, "Wildling"),
	INIT(0, "Init");

	private final int id;
	private final String name;

	UserPrivilege(int value, String name) {
		this.id = value;
		this.name = name;
	}

	public static UserPrivilege getPrivilege(int id) {
		return Arrays.stream(UserPrivilege.values()).filter(privilege -> privilege.getId() == id).findFirst().orElse(null);
	}

	public UserPrivilege getPrevious() {
		if (this.equals(UserPrivilege.ADMIN)) return UserPrivilege.MEMBER_VETERAN;
		if (this.equals(UserPrivilege.MEMBER_VETERAN)) return UserPrivilege.MEMBER_NORMAL;
		if (this.equals(UserPrivilege.MEMBER_NORMAL)) return UserPrivilege.MEMBER_START;
		if (this.equals(UserPrivilege.MEMBER_START)) return UserPrivilege.NONMEMBER;
		return UserPrivilege.INIT;
	}

	public UserPrivilege getNext() {
		if (this.equals(UserPrivilege.INIT)) return UserPrivilege.NONMEMBER;
		if (this.equals(UserPrivilege.NONMEMBER)) return UserPrivilege.MEMBER_START;
		if (this.equals(UserPrivilege.MEMBER_START)) return UserPrivilege.MEMBER_NORMAL;
		if (this.equals(UserPrivilege.MEMBER_NORMAL)) return UserPrivilege.MEMBER_VETERAN;
		return UserPrivilege.ADMIN;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
