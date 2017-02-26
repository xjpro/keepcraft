package keepcraft.command;

import keepcraft.data.models.Container;
import keepcraft.data.models.Plot;
import keepcraft.data.models.UserPrivilege;
import keepcraft.services.ContainerService;
import keepcraft.services.PlotService;
import keepcraft.services.UserService;
import org.bukkit.command.CommandSender;
import keepcraft.services.ChatService;
import keepcraft.data.models.User;

public class ContainerCommandListener extends CommandListener {

	private final UserService userService;
	private final PlotService plotService;
	private final ContainerService containerService;
	private final ChatService chatService;

	public ContainerCommandListener(UserService userService, PlotService plotService, ContainerService containerService, ChatService chatService) {
		this.userService = userService;
		this.plotService = plotService;
		this.containerService = containerService;
		this.chatService = chatService;
	}

	@Override
	protected boolean handle(String commandName, CommandSender commandSender, String[] args) {
		User user = userService.getOnlineUser(commandSender.getName());
		Container container = user.getTargetContainer();

		if (container == null) {
			chatService.sendFailureMessage(user, "No target — open a container to target it");
			return true;
		}

		if (commandName.equalsIgnoreCase("chest")) {

			if (args.length == 1) {
				// Set permissions level

				// Check that container is not in enemy plot
				if (user.getPrivilege() != UserPrivilege.ADMIN) {
					Plot plot = plotService.getIntersectedPlot(container.getBlock().getLocation());
					if (plot != null && !plot.isFactionProtected(user.getFaction())) {
						chatService.sendFailureMessage(user, "You do not have permission to modify this");
						return true;
					}
				}

				// Check that user has high enough privilege to modify
				if (!container.canAccess(user)) {
					chatService.sendFailureMessage(user, "You do not have permission to modify this");
					return true;
				}

				int typeId = -1;
				try {
					typeId = Integer.parseInt(args[0]);
				} catch (Exception ignored) {
				}

				if (typeId < 0 || typeId > 2) {
					chatService.sendFailureMessage(user, "Enter a valid permission level: 0 (public), 1 (all team members), or 2 (veteran team members)");
					return true;
				}
				if (typeId == 2 && user.getPrivilege().getId() < UserPrivilege.MEMBER_VETERAN.getId()) {
					chatService.sendFailureMessage(user, String.format("You must be %s rank to set that permission level", UserPrivilege.MEMBER_VETERAN.getName()));
					return true;
				}
				if (typeId == 1 && user.getPrivilege().getId() < UserPrivilege.MEMBER_NORMAL.getId()) {
					chatService.sendFailureMessage(user, String.format("You must be %s rank to set that permission level", UserPrivilege.MEMBER_NORMAL.getName()));
					return true;
				}

				container.setPermission(Container.ContainerPermission.getContainerPermission(typeId));
				containerService.updateContainer(container);
				chatService.sendSuccessMessage(user, String.format("Permissions updated to %s", container.getOutputType()));
				return true;

			} else if (args.length == 2) {
				if (user.getPrivilege() != UserPrivilege.ADMIN) return false; // only ops

				if (args[0].equalsIgnoreCase("output")) {
					int output;
					try {
						output = Integer.parseInt(args[1]);
					} catch (Exception e) {
						// invalid input
						return false;
					}
					container.setOutputPerHour(output);
					containerService.updateContainer(container);
					chatService.sendSuccessMessage(user, String.format("Container output set to %s per hour", output));
					return true;
				}
			}
		}

		return false;
	}

}
