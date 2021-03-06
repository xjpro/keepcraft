package keepcraft.services;

import org.bukkit.Bukkit;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class AnnouncementService {

	private final ChatService chatService;
	private final Timer timer;

	public AnnouncementService(ChatService chatService) {
		this.chatService = chatService;
		timer = new Timer();
	}

	public void queueTimedAnnouncements() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone(ZoneId.of("America/Chicago")));
		calendar.set(Calendar.HOUR_OF_DAY, 20);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Bukkit.getServer().setWhitelist(false); // Whitelist off at 8
				chatService.sendGlobalAlertMessage("Raiding hours are now open");
			}
		}, calendar.getTime());

		calendar.set(Calendar.HOUR_OF_DAY, 21);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				chatService.sendGlobalAlertMessage("Raiding hours are now closed");
			}
		}, calendar.getTime());
	}

	public void cancelQueuedAnnouncements() {
		timer.cancel();
	}
}
