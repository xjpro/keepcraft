package keepcraft.data.models;


public interface ChatParticipant {
	String getName();
	String getColoredName();
	String getChatTag();
	String getChatTag(UserTeam team);
	boolean getReceiveGlobalMessages();
	void setReceiveGlobalMessages(boolean value);
	boolean canApprove(User target);
}
