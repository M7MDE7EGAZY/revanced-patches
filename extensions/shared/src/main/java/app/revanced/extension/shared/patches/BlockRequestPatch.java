package app.revanced.extension.shared.patches;

import android.net.Uri;

import app.revanced.extension.music.settings.Settings;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.shared.utils.Logger;
import app.revanced.extension.shared.utils.PackageUtils;

@SuppressWarnings("unused")
public class BlockRequestPatch {
    /**
     * Used in YouTube and YouTube Music.
     */
    public static final boolean SPOOF_STREAMING_DATA = BaseSettings.SPOOF_STREAMING_DATA.get();

    /**
     * Used in YouTube Music.
     * Disabled by default.
     */
    public static final boolean SPOOF_CLIENT = Settings.SPOOF_CLIENT.get();

    /**
     * Used in YouTube Music.
     */
    public static final boolean IS_7_17_OR_GREATER = PackageUtils.getAppVersionName().compareTo("7.17.00") >= 0;

    private static final boolean BLOCK_REQUEST = SPOOF_STREAMING_DATA || (SPOOF_CLIENT && IS_7_17_OR_GREATER);

    /**
     * Any unreachable ip address.  Used to intentionally fail requests.
     */
    private static final String UNREACHABLE_HOST_URI_STRING = "https://127.0.0.0";
    private static final Uri UNREACHABLE_HOST_URI = Uri.parse(UNREACHABLE_HOST_URI_STRING);

    /**
     * Injection point.
     * Blocks /get_watch requests by returning an unreachable URI.
     *
     * @param playerRequestUri The URI of the player request.
     * @return An unreachable URI if the request is a /get_watch request, otherwise the original URI.
     */
    public static Uri blockGetWatchRequest(Uri playerRequestUri) {
        if (BLOCK_REQUEST) {
            try {
                String path = playerRequestUri.getPath();

                if (path != null && path.contains("get_watch")) {
                    Logger.printDebug(() -> "Blocking 'get_watch' by returning unreachable uri");

                    return UNREACHABLE_HOST_URI;
                }
            } catch (Exception ex) {
                Logger.printException(() -> "blockGetWatchRequest failure", ex);
            }
        }

        return playerRequestUri;
    }

    /**
     * Injection point.
     * <p>
     * Blocks /initplayback requests.
     */
    public static String blockInitPlaybackRequest(String originalUrlString) {
        if (BLOCK_REQUEST) {
            try {
                var originalUri = Uri.parse(originalUrlString);
                String path = originalUri.getPath();

                if (path != null && path.contains("initplayback")) {
                    Logger.printDebug(() -> "Blocking 'initplayback' by clearing query");

                    return originalUri.buildUpon().clearQuery().build().toString();
                }
            } catch (Exception ex) {
                Logger.printException(() -> "blockInitPlaybackRequest failure", ex);
            }
        }

        return originalUrlString;
    }
}
