package utilities;

public class BrowserContext {

    private static final ThreadLocal<String> browser =
            new ThreadLocal<>();

    public static void setBrowser(String browserName) {
        browser.set(browserName);
    }

    public static String getBrowser() {

        String browserName = browser.get();

        if (browserName == null) {
            throw new IllegalStateException(
                    "Browser is not set for thread: "
                            + Thread.currentThread().threadId()
            );
        }

        return browserName;
    }

    /** Returns the browser name, or {@code "unknown"} if the context was not set / already cleared. */
    public static String getBrowserSafe() {
        String browserName = browser.get();
        return browserName == null ? "unknown" : browserName;
    }

    public static void clear() {
        browser.remove();
    }

}