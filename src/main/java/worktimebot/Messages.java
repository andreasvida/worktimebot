package worktimebot;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "worktimebot.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static String getString(Locale locale, String key) {
		try {
			String suffix = "_" + locale.getLanguage();
			if("en".equals(locale.getLanguage())) {
				suffix = "";
			}					
			return RESOURCE_BUNDLE.getString(key + suffix);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
