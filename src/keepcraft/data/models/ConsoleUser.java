package keepcraft.data.models;

public class ConsoleUser implements ChatParticipant {
	@Override
	public String getName() {
		return "CONSOLE";
	}

	@Override
	public String getColoredName() {
		return "CONSOLE";
	}

	@Override
	public String getChatTag() {
		return "CONSOLE";
	}

	@Override
	public String getChatTag(UserTeam team) {
		return "CONSOLE";
	}

	@Override
	public boolean getReceiveGlobalMessages() {
		return true;
	}

	@Override
	public void setReceiveGlobalMessages(boolean value) {
	}

	@Override
	public boolean canApprove(User target) {
		return target.getPrivilege() == UserPrivilege.MEMBER_START;
	}
}
