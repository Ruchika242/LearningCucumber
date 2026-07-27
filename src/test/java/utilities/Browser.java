package utilities;

/**
 * Minimal Browser enum with only getter and setter for a selected browser string,
 * as requested ("till setter and getter").
 */
public enum Browser {
    CHROME,
    FIREFOX,
    EDGE,
    SAFARI,
    CHROMIUM;

    // Mutable selected browser string
    private static String selectedBrowser = null;

    public static void setSelectedBrowser(String browserName) {
        selectedBrowser = browserName;
    }

    public static String getSelectedBrowser() {
        return selectedBrowser;
    }
}




