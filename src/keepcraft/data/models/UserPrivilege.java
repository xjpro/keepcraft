package keepcraft.data.models;

public abstract class UserPrivilege {

	public final static int ADMIN = 400;
	public final static int SUPER = 300;
	public final static int MEMBER_VETERAN = 200;
	public final static int MEMBER_NORMAL = 150;
	public final static int MEMBER_START = 100;
	public final static int NONMEMBER = 50;
	public final static int INIT = 0;

	public static String asString(int privilege) {
		switch (privilege) {
			case ADMIN:
				return "Admin";
			case SUPER:
				return "VIP";
			case MEMBER_VETERAN:
				return "Knight";
			case MEMBER_NORMAL:
				return "Squire";
			case MEMBER_START:
				return "Soldier";
			case NONMEMBER:
				return "Wildling";
			case INIT:
				return "Init";
			default:
				return "Unknown";
		}
	}

}
