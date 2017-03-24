package keepcraft.services;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class AnnouncementService {

	private final ChatService chatService;

	public AnnouncementService(ChatService chatService) {
		this.chatService = chatService;
	}

	public void queueAnnoucements() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone(ZoneId.of("America/Chicago")));
		calendar.set(Calendar.HOUR_OF_DAY, 20);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		(new Timer()).schedule(new TimerTask() {
			@Override
			public void run() {
				chatService.sendGlobalAlertMessage("Raiding hours are now open");
			}
		}, calendar.getTime());

		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);

		(new Timer()).schedule(new TimerTask() {
			@Override
			public void run() {
				chatService.sendGlobalAlertMessage("Raiding hours are now closed");
			}
		}, calendar.getTime());
	}
}
