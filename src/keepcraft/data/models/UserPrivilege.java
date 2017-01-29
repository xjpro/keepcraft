package keepcraft.data.models;

public abstract class UserPrivilege {

	public final static int ADMIN = 400;
	public final static int SUPER = 300;
	public final static int MEMBER = 200;
	public final static int VISITOR = 100;
	public final static int INIT = 0;

	public static String asString(int privilege) {
		switch (privilege) {
			case ADMIN:
				return "Admin";
			case SUPER:
				return "VIP";
			case MEMBER:
				return "Member";
			case VISITOR:
				return "Visitor";
			case INIT:
				return "Init";
			default:
				return "Unknown";
		}
	}

}
