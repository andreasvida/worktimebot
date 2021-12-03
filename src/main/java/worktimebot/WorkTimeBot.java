package worktimebot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;

import clocking.TimeClock;
import clocking.impl.TimeClockImpl;

public class WorkTimeBot extends TelegramBot {
		
	HashMap<Long, TimeClock> timeClockMap;
	HashMap<Long, UserSettings> userSettingsMap;
	
	private Locale locale = Locale.ENGLISH; 

	private static final String SAVEGAME = "savegame.bin"; //$NON-NLS-1$
	
	public WorkTimeBot(String apitoken) {
		super(apitoken); //$NON-NLS-1$
		load();		
	}
	
	public synchronized void persist() {
		try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(SAVEGAME))) { 			
			o.writeObject(timeClockMap);
			o.writeObject(userSettingsMap);
		} catch (IOException io) { 
			io.printStackTrace();
		}		
	}
	
	public synchronized void load() { 		
		try (ObjectInputStream o = new ObjectInputStream(new FileInputStream(SAVEGAME))) {
			timeClockMap = (HashMap<Long, TimeClock>) o.readObject();			
			userSettingsMap = (HashMap<Long, UserSettings>) o.readObject();			
		} catch (Exception io) { 
			io.printStackTrace();
			timeClockMap = new HashMap<>();
			userSettingsMap = new HashMap<>();
		}		 
	}

	public void start() {
		setUpdatesListener(updates -> {
			for (Update update : updates) {					
				if(update.callbackQuery() != null) {					
					handleCallback(update.callbackQuery());					
				} else {
					try { 
						Long userId = update.message().from().id();
						String msg = handleCommand(update.message().text(), getClockViaUser(userId), update.message().chat().id(), getUserSettings(userId));					
						if(!msg.isEmpty()) { 
							execute(new SendMessage(update.message().chat().id(), msg));
						}		
					} catch (NullPointerException e) {}
				}
			}
			persist();
			return UpdatesListener.CONFIRMED_UPDATES_ALL;
		});
	}
	
	private TimeClock getClockViaUser(Long userId) {		
		if (timeClockMap.get(userId) == null) {
			timeClockMap.put(userId, new TimeClockImpl());
		}
		return timeClockMap.get(userId);
	}
	
	private UserSettings getUserSettings(Long userId) { 
		if(userSettingsMap.get(userId) == null) {			
			// default working hours 8.75, 5.5, 0, 0
			Duration[] hours = new Duration[7];			
			hours[0]=hours[1]=hours[2]=hours[3]= Duration.ofMinutes(8*60+45);
			hours[4]=Duration.ofMinutes(5*60+30);
			hours[5]=hours[6]=Duration.ofMinutes(0);						
			userSettingsMap.put(userId, new UserSettings(hours, 
					Locale.ENGLISH, ZoneId.ofOffset("GMT", 
							ZoneOffset.ofHours(1))));
		}
		return userSettingsMap.get(userId);
	}
	
	private void handleCallback(CallbackQuery query) {
		if (query.data() != null) {
			switch (query.data()) {
			case "/clock": //$NON-NLS-1$
			case "/status": //$NON-NLS-1$
			case "/list": //$NON-NLS-1$
			case "/de": //$NON-NLS-1$
			case "/en": //$NON-NLS-1$
			case "/ru": //$NON-NLS-1$
			case "/jp": //$NON-NLS-1$
				if (query.message() != null) {
					Long chatId = query.message().chat().id();
					TimeClock clock = getClockViaUser(query.from().id());
					UserSettings settings = getUserSettings(query.from().id());
					String response = handleCommand(query.data(), clock, chatId, settings);
					
					execute(new EditMessageText(chatId, query.message().messageId(), response).replyMarkup(new InlineKeyboardMarkup(
							new InlineKeyboardButton(clock.isClockedIn() ? Messages.getString(locale, "WorkTimeBot.7") : Messages.getString(locale, "WorkTimeBot.8")) //$NON-NLS-1$ //$NON-NLS-2$
							.callbackData("/clock"), //$NON-NLS-1$
					new InlineKeyboardButton(Messages.getString(locale, "WorkTimeBot.10")).callbackData("/status"), //$NON-NLS-1$ //$NON-NLS-2$
					new InlineKeyboardButton(Messages.getString(locale, "WorkTimeBot.12")).callbackData("/list")))); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}

	private String handleCommand(String command, TimeClock clock, Long chatId, UserSettings settings) {
		String messageText = ""; //$NON-NLS-1$
		if(command == null) return messageText;				
		
		Instant now = Instant.now();
		
		locale = settings.getLocale();
		
		if("/clock".equals(command)) {  //$NON-NLS-1$
			command = clock.isClockedIn()? "/out" : "/in"; //$NON-NLS-1$ //$NON-NLS-2$
		}
				
		switch (command) {		
							
		case "/in": //$NON-NLS-1$
			if(!clock.isClockedIn()) { 
				clock.clockIn(now);
			}
			messageText = Messages.getString(locale, "WorkTimeBot.19"); //$NON-NLS-1$
			break;

		case "/out": //$NON-NLS-1$
			if(clock.isClockedIn()) { 
				clock.clockOut(now);
			}
			messageText = Messages.getString(locale, "WorkTimeBot.21") + formatDuration(clock.getBalance(now)); //$NON-NLS-1$
			break;
			
		case "/de": //$NON-NLS-1$			
		case "/en": //$NON-NLS-1$
		case "/ru": //$NON-NLS-1$
		case "/jp": //$NON-NLS-1$
			locale = new Locale(command.substring(1), "");
			settings.setLocale(locale);
			persist();
			
		case "/status":					 //$NON-NLS-1$
			Duration balance = clock.getBalance(Instant.now());			
			String formatedBalance = formatDuration(balance);
			
			String message = "";			 //$NON-NLS-1$
			if(clock.isClockedIn()) {				
				message += Messages.getString(locale, "WorkTimeBot.24");				 //$NON-NLS-1$
			} else { 
				message += Messages.getString(locale, "WorkTimeBot.25"); //$NON-NLS-1$
			}
			message += Messages.getString(locale, "WorkTimeBot.26") + formatDuration(clock.getWorkedToday()) + "\n";			 						 //$NON-NLS-1$ //$NON-NLS-2$
			message += Messages.getString(locale, "WorkTimeBot.28") + formatedBalance;         //$NON-NLS-1$
						
			messageText = message;
			break;
		case "/list": //$NON-NLS-1$
			
			messageText = getSummary(clock, locale);
			
			break;
			
		case "/start": //$NON-NLS-1$
			SendMessage msg = new SendMessage(chatId, "Welcome to time clock bot. Please select your language: \n\n"  //$NON-NLS-1$
					 +Messages.getString(new Locale("ru"), "WorkTimeBot.31") + "\n\n"  //$NON-NLS-1$
					 +Messages.getString(Locale.GERMAN, "WorkTimeBot.31") + "\n\n" //$NON-NLS-1$
					 +Messages.getString(new Locale("jp"), "WorkTimeBot.31")   //$NON-NLS-1$
					);									
			msg.replyMarkup(
					new InlineKeyboardMarkup(
							 new InlineKeyboardButton("English").callbackData("/en") //$NON-NLS-1$ //$NON-NLS-2$
							,new InlineKeyboardButton("\u0440\u0443\u0441\u0441\u043A\u0438\u0439").callbackData("/ru") //$NON-NLS-1$ //$NON-NLS-2$
							,new InlineKeyboardButton("Deutsch").callbackData("/de") //$NON-NLS-1$ //$NON-NLS-2$
					        ,new InlineKeyboardButton("\u65e5\u672c").callbackData("/jp") //$NON-NLS-1$ //$NON-NLS-2$
					));		
			execute(msg);			
			break; 
		case "/clear": //$NON-NLS-1$
			Long delete = null;
			
			for(Long l : timeClockMap.keySet()) { 
				if(timeClockMap.get(l) == clock) 
					delete = l;
			}
			if(delete != null) { 
				timeClockMap.remove(delete);
			}			
			delete = null;
			for(Long l : userSettingsMap.keySet()) { 
				if(userSettingsMap.get(l) == clock) 
					delete = l;
			}
			if(delete != null) { 
				userSettingsMap.remove(delete);
			}						
			persist();
			break;			
		case "/help": //$NON-NLS-1$
			messageText = Messages.getString(locale, "WorkTimeBot.37") //$NON-NLS-1$
					+ Messages.getString(locale, "WorkTimeBot.38") //$NON-NLS-1$
					+ Messages.getString(locale, "WorkTimeBot.39") //$NON-NLS-1$
					+ Messages.getString(locale, "WorkTimeBot.40") //$NON-NLS-1$
					+ Messages.getString(locale, "WorkTimeBot.41") //$NON-NLS-1$
					+ Messages.getString(locale, "WorkTimeBot.42"); //$NON-NLS-1$
			break;
		case "/export":	//$NON-NLS-1$			
			String text = getSummary(clock, locale);			
			byte[] document;
			try {
				document = text.getBytes("UTF-8");
				execute(new SendDocument(chatId, document));			
			} catch (UnsupportedEncodingException e) {				
			}			
			break; 	
		}
		
		if(command.startsWith("/holiday")) { //$NON-NLS-1$
			LocalDate day = LocalDate.now();
			try {				
				command = command.substring(8).trim();
				if (!command.isEmpty()) {
					day = LocalDate.parse(command, DateTimeFormatter.ISO_LOCAL_DATE);
				}
				Set<LocalDate> off = clock.getOffDays();
				if (off.contains(day)) {
					off.remove(day);
				} else {
					off.add(day);
				}
				clock.setOffDays(off);				
				messageText = Messages.getString(locale, "WorkTimeBot.47") + off.toString(); //$NON-NLS-1$
			} catch (DateTimeParseException p) {
				messageText = Messages.getString(locale, "WorkTimeBot.48") + p.getParsedString() + "]" //$NON-NLS-1$ //$NON-NLS-2$
						+ Messages.getString(locale, "WorkTimeBot.50"); //$NON-NLS-1$
			}						
		}
		
		if(command.startsWith("/settings")) { //$NON-NLS-1$
			
			String params = command.substring(9).trim();			
			String[] hours = params.split(";");
			Duration[] durations = new Duration[7];
			
			double[] h = new double[7];
			try {
				if (params.isEmpty())
					throw new NumberFormatException();
				if (hours.length == 1) {
					double d = Double.parseDouble(hours[0]);
					if (d < 0.0 || d > 24.0)
						throw new NumberFormatException();
					Arrays.fill(h, d);
				} else if (hours.length == 7) {
					for (int i = 0; i < 7; i++) {

						double d = Double.parseDouble(hours[i]);
						if (d < 0.0 || d > 24.0)
							throw new NumberFormatException();
						h[i] = d;
					}
				} else {
					throw new NumberFormatException();
				}
				
				for (int i = 0; i < 7; i++) {					
					durations[i] = Duration.ofMinutes((int) Math.floor(h[i] * 60.0));
				}				
				clock.setHours(durations);
				settings.setHours(durations);
				persist();
			} catch (NumberFormatException n) {
				messageText = Messages.getString(locale, "WorkTimeBot.66")  //$NON-NLS-1$
						+"\n"
						+formatDuration(settings.hours[0])+"\n"
						+formatDuration(settings.hours[1])+"\n"
							+formatDuration(settings.hours[2])+"\n"
										+formatDuration(settings.hours[3])+"\n"
												+formatDuration(settings.hours[4])+"\n"
														+formatDuration(settings.hours[5])+"\n"
																+formatDuration(settings.hours[6]);
					
			}						
		}
				
		if(!command.startsWith("/")) { 
			clock.addComment(now, command);
			persist();
		}
		
		execute(new SetMyCommands(				
				new BotCommand("/start", Messages.getString(locale, "WorkTimeBot.52")),				 //$NON-NLS-1$ //$NON-NLS-2$
				new BotCommand("/help", Messages.getString(locale, "WorkTimeBot.54")), //$NON-NLS-1$ //$NON-NLS-2$
				new BotCommand("/holiday", Messages.getString(locale, "WorkTimeBot.56")), //$NON-NLS-1$ //$NON-NLS-2$
				new BotCommand("/export", Messages.getString(locale, "WorkTimeBot.58")), //$NON-NLS-1$ //$NON-NLS-2$
				new BotCommand("/clear", Messages.getString(locale, "WorkTimeBot.60")), //$NON-NLS-1$ //$NON-NLS-2$
				new BotCommand("/settings", Messages.getString(locale, "WorkTimeBot.62")) //$NON-NLS-1$ //$NON-NLS-2$
				));
		
		return messageText;
	}

	private String getSummary(TimeClock clock, Locale l) {		
		DateTimeFormatter dateF = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(l);		
		String messageText = clock.printList(l);			
		messageText+= Messages.getString(locale, "WorkTimeBot.63") + formatDuration(clock.getWorkedTime()) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		if (!clock.getOffDays().isEmpty()) {
			messageText += Messages.getString(locale, "WorkTimeBot.65");
			for (LocalDate d : clock.getOffDays()) {
				messageText += d.format(dateF) + ",";
			}
			messageText = messageText.substring(0, messageText.length()-1);
		}
		return messageText;
	}

	private String formatDuration(Duration duration) {
		long seconds = duration.getSeconds();
		long absSeconds = Math.abs(seconds);
		String positive = String.format("%d:%02d:%02d", absSeconds / 3600, (absSeconds % 3600) / 60, absSeconds % 60); //$NON-NLS-1$
		return seconds < 0 ? "-" + positive : positive; //$NON-NLS-1$
	}
	
}
