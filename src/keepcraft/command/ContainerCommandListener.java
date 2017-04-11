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
			if (args.length == 0) {
				String containerString = "Container:" +
						String.format("\nOutput type: %s", container.getOutputType()) +
						String.format("\nOutput per hour: %s", container.getOutputPerHour());
				chatService.sendInfoMessage(user, containerString);
				return true;
			} else if (args.length == 1) {
				// Nothing in this arg level
				return false;
			} else if (args.length == 2) {
				if (user.getPrivilege() != UserPrivilege.ADMIN) return false; // only ops

				if (args[0].equalsIgnoreCase("type")) {
					int outputType;
					try {
						outputType = Integer.parseInt(args[1]);
					} catch (Exception e) {
						// invalid input
						return false;
					}
					container.setOutputType(Container.ContainerOutputType.getContainerOutputType(outputType));
					containerService.updateContainer(container);
					chatService.sendSuccessMessage(user, String.format("Container output type to %s", outputType));
					return true;
				} else if (args[0].equalsIgnoreCase("output")) {
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
