package worktimebot;

public class Main {
	public static void main(String args[]) {		
		if(args.length>0) {							
			WorkTimeBot bot = new WorkTimeBot(args[0]);
			bot.start();
		} else {
			System.out.println("Argument missing: please provide Telegram BOT API Token");
		}
	}
}
