package pkleczek.profiwan.utils;

public final class TextUtils {
	public static final CharSequence CUSTOM_ACCENT_MARKER = "\\"; //$NON-NLS-1$

	private TextUtils() {}
	
	public static String getAccentedString(String str) {
		final CharSequence UNICODE_ACCENT_MARKER = "\u0301"; //$NON-NLS-1$
		return str.replace(CUSTOM_ACCENT_MARKER, UNICODE_ACCENT_MARKER);
	}
}
