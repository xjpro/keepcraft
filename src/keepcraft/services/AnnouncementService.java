package keepcraft.services;

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

	public void queueAnnoucements() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone(ZoneId.of("America/Chicago")));
		calendar.set(Calendar.HOUR_OF_DAY, 20);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				chatService.sendGlobalAlertMessage("Raiding hours are now open");
			}
		}, calendar.getTime());

		calendar.set(Calendar.HOUR_OF_DAY, 23);
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
