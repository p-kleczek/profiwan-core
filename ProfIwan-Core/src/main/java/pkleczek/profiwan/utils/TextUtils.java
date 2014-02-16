package pkleczek.profiwan.utils;

public final class TextUtils {
	public static final CharSequence CUSTOM_ACCENT_MARKER = "\\"; //$NON-NLS-1$
	public static final String UNICODE_ACCENT_MARKER = "\u0301"; //$NON-NLS-1$

	private TextUtils() {}
	
	public static String getAccentedString(String str) {
		return str.replace(CUSTOM_ACCENT_MARKER, UNICODE_ACCENT_MARKER);
	}

	public static String getUnaccentedString(String str) {
		return str.replace(UNICODE_ACCENT_MARKER, "");
	}
}
