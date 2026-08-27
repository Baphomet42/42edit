package baphomethlabs.fortytwoedit.gui.screen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import com.google.common.collect.Lists;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import baphomethlabs.fortytwoedit.gui.widget.SmartButton;
import baphomethlabs.fortytwoedit.gui.widget.SmartEditBox;

public class LogScreen extends GenericScreen {

    private MultiLineEditBox box;
    private static File logFile;
    private static int fullLogLines = 0;
    private static int fullLogStart = 0;
    private static int modLogStart = 0;
    private static final List<LogMessage> FULL_LOG = Lists.newArrayList();
    private static final List<LogMessage> MOD_LOG = Lists.newArrayList();
    private static final List<LogMessage> MOD_LOG_QUEUE = Lists.newArrayList();

    private SmartButton btnPause = null;
    private SmartButton btnSource = null;
    private SmartButton btnSearchMode = null;

    private static boolean clearFullLogCache = false;
    private static boolean paused = false;
    private static boolean onlyMod = false;

    protected SmartEditBox txtRegex;
    private static String regexInput = "";
    private static boolean useRegex = false;
    private static final String REGEX_STRING_ENDS_DOLLAR = ".*[^\\\\](\\\\\\\\)*\\$$";

    private long lastCheck = 0;
    private long lastUpdate = 0;
    private static final int UPDATE_WAIT_MS = 1000;
    private static final String ss = GenericScreen.UNICODE_SECTION_SIGN;

    public LogScreen() {
        super("Log");
    }

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = LogScreen::new;
        this.addBackButton(SecretScreen::new);

        logFile = new File(minecraft.gameDirectory.getAbsolutePath() + "\\logs\\latest.log");

        this.addRenderableWidget(
            WIDGET_UTIL.newButton("", btn -> {
                    paused = !paused;
                    updateBox();
                    updatePauseButton();
                }).setSize(40).setPosition(x + GUI_SPACE + 40 + WID_SPACE, y + GUI_SPACE).setTooltip("")
                .runWithSelf(w -> this.btnPause = w).build()
        );
        updatePauseButton();

        this.addRenderableWidget(
            WIDGET_UTIL.newButton(Component.nullToEmpty("Clear"),
                (button, inputWithModifiers) -> btnClearLog(inputWithModifiers)).setSize(40).setPosition(x + backgroundWidth - GUI_SPACE - 50 - 40 - WID_SPACE,
                y + GUI_SPACE).setTooltip("Clear all logged messages\n\nShift click to restore all cleared messages").build()
        );

        this.addRenderableWidget(
            WIDGET_UTIL.newButton("", btn -> {
                    onlyMod = !onlyMod;
                    updateBox();
                    updateSourceButton();
                }).setSize(50).setPosition(x + backgroundWidth - GUI_SPACE - 50, y + GUI_SPACE)
                .runWithSelf(w -> this.btnSource = w)
                .setTooltip("Toggle log source between all logs or only 42edit logs").build()
        );
        updateSourceButton();

        box = this.addRenderableWidget(
            MultiLineEditBox.builder().setX(x + 15 - 3).setY(y + 35).build(minecraft.font, 240 - 24, ROW_HEIGHT * 6, Component.nullToEmpty(""))
        );

        this.addRenderableWidget(
            WIDGET_UTIL.newButton("", btn -> {
                    useRegex = !useRegex;
                    updateBox();
                    updateSearchButton();
                }).setSize(50).setPosition(x + backgroundWidth - 5 - 50 - 7, y + 35 + ROW_HEIGHT * 6 + 1)
                .runWithSelf(w -> this.btnSearchMode = w)
                .setTooltip("Toggle search mode between classic and regex").build()
        );
        updateSearchButton();

        this.addRenderableWidget(
            WIDGET_UTIL.newEditBox().setSize(160).setPosition(x + 15 - 3, y + 35 + ROW_HEIGHT * 6 + 1).runWithSelf(w -> this.txtRegex = w)
                .setValue("" + regexInput).setResponder(this::editTxtRegex).build()
        );

        updateBox();
    }

    protected void updatePauseButton() {
        if (this.btnPause != null) {
            this.btnPause.setMessage(Component.nullToEmpty(paused ? "Resume" : "Pause"));
            this.btnPause.setTooltip(Tooltip.create(Component.nullToEmpty(paused ? "Unpause log and show new messages" : "Temporarily freeze new messages from appearing")));
        }
    }

    protected void updateSourceButton() {
        if (this.btnSource != null) {
            this.btnSource.setMessage(Component.nullToEmpty(onlyMod ? "[42edit]" : "[All]"));
        }
    }

    protected void updateSearchButton() {
        if (this.btnSearchMode != null) {
            this.btnSearchMode.setMessage(Component.nullToEmpty(useRegex ? "[Regex]" : "[Search]"));
        }
    }

    protected void updateBox() {
        StringBuilder sb = new StringBuilder();
        List<LogMessage> logList = onlyMod ? MOD_LOG : FULL_LOG;
        int logStart = onlyMod ? modLogStart : fullLogStart;

        boolean firstLog = true;
        String regexError = null;
        for (int i = logStart; i < logList.size(); i++) {

            if (regexInput.length() > 0) {
                boolean matchRegex = false;
                if (useRegex) {
                    try {
                        String regexMod = regexInput + "";
                        "".matches(regexMod); // used to get errors on original input regex

                        if (!regexMod.startsWith("^"))
                            regexMod = ".*" + regexMod;
                        if (!regexMod.matches(REGEX_STRING_ENDS_DOLLAR))
                            regexMod = regexMod + ".*";

                        if (logList.get(i).searchLine().matches(regexMod))
                            matchRegex = true;
                    }
                    catch (Exception ex) {
                        regexError = ex.getMessage();
                    }
                }
                else if (logList.get(i).searchLine().toLowerCase().contains(regexInput.toLowerCase()))
                    matchRegex = true;

                if (!matchRegex)
                    continue;
            }

            if (!firstLog)
                sb.append("\n\n");
            else
                firstLog = false;

            sb.append(logList.get(i).formattedLine());
        }
        // end log box with empty line unless no lines were added
        if (!firstLog)
            sb.append("\n");

        if (regexError != null) {
            if (regexError.length() > 1 && regexError.endsWith("^"))
                regexError = regexError.substring(0, regexError.length() - 1).trim();
            sb = new StringBuilder(ss + "cInvalid Regex\n\n" + regexError.replace("\r", ""));
        }

        box.setValue(sb.toString());
    }

    protected void editTxtRegex(String text) {
        if (!text.equals(regexInput)) {
            regexInput = text;
            updateBox();
        }
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss"); // from CommandBlockExecutor.class

    /**
     * Get the current timestamp in the pattern `[HH:mm:ss]`
     * 
     * @return
     */
    public static String getTimestamp() {
        return "[" + DATE_FORMAT.format(new Date()) + "]";
    }

    private record LogMessage(String timestamp, LogType type, String message, String formattedLine, String searchLine) {

        public static LogMessage build(String inpLine) {
            String line = inpLine + "";
            String timestamp = null;
            LogType logType = null;

            if (line.matches("^\\[[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\] .+")) {
                timestamp = line.substring(0, line.indexOf("]") + 1);
                line = line.replaceFirst("^\\[[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\] ", "");

                if (line.matches("^\\[[^/\\]]+/[A-Z]+\\]:? .+")) {
                    logType = LogType.build(line.substring(line.indexOf("/") + 1, line.indexOf("]")));
                    line = line.replaceFirst("^\\[[^/\\]]+/[A-Z]+\\]:? ", "");
                }
            }

            return build(timestamp, logType, line);
        }
        public static LogMessage build(LogType type, String message) {
            return build(getTimestamp(), type, message);
        }
        public static LogMessage build(String timestamp, LogType type, String message) {
            StringBuilder formattedLine = new StringBuilder();
            StringBuilder searchLine = new StringBuilder();

            if (timestamp != null) {
                formattedLine.append(ss).append("9").append(timestamp).append(ss).append("r ");
                searchLine.append(timestamp).append(" ");
            }

            if (type != null) {
                formattedLine.append(ss).append(type.formatCode()).append(type.text()).append(ss).append("r ");
                searchLine.append(type.text()).append(" ");
            }

            String formatMessage = message.replace("\t", "  ");
            if (timestamp != null && type != null && formatMessage.matches("^\\([^)]+\\) .+")) {
                formattedLine.append(ss).append("3").append(formatMessage.substring(0, formatMessage.indexOf(")") + 1)).append(ss).append("r ");
                searchLine.append(formatMessage.substring(0, formatMessage.indexOf(")") + 1)).append(" ");

                formatMessage = formatMessage.replaceFirst("^\\([^)]+\\) ", "");
            }
            formattedLine.append(formatMessage);
            searchLine.append(formatMessage);

            return new LogMessage(timestamp, type, message, formattedLine.toString(), searchLine.toString().replace("\n", " "));
        }
    }

    public record LogType(String text, String formatCode) {
        public static final LogType INFO = new LogType("[INFO]", "2");
        public static final LogType WARN = new LogType("[WARN]", "6");
        public static final LogType ERROR = new LogType("[ERROR]", "c");
        public static final LogType DEBUG = new LogType("[DEBUG]", "a");
        public static final LogType FATAL = new LogType("[FATAL]", "4");
        public static final LogType UNKNOWN = new LogType("[UNKNOWN]", "f");

        public static LogType build(String text) {
            if (text != null && text.length() > 0) {
                switch (text) {
                    case "INFO": return LogType.INFO;
                    case "WARN": return LogType.WARN;
                    case "ERROR": return LogType.ERROR;
                    case "DEBUG": return LogType.DEBUG;
                    case "FATAL": return LogType.FATAL;
                    default: return new LogType("[" + text + "]", LogType.UNKNOWN.formatCode());
                }
            }
            return LogType.UNKNOWN;
        }
    }

    public static void logModLog(LogType type, String message) {
        MOD_LOG_QUEUE.add(LogMessage.build(type, message));
    }

    public static void debugTryRefreshVarious() {
        clearFullLogCache = true;
        regexInput = "";
        useRegex = false;
    }

    private void refreshVariousOnTick() {
        lastCheck = 0;
        lastUpdate = 0;
        FULL_LOG.clear();
        fullLogLines = 0;
        unhideAllLogged();
    }

    private void btnClearLog(InputWithModifiers inputWithModifiers) {
        if (inputWithModifiers.hasShiftDown()) {
            unhideAllLogged();
        }
        else {
            hideCurrentlyLogged();
        }
        updateBox();
    }

    private void hideCurrentlyLogged() {
        fullLogStart = FULL_LOG.size();
        modLogStart = MOD_LOG.size();
    }

    private void unhideAllLogged() {
        fullLogStart = 0;
        modLogStart = 0;
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return !(txtRegex.canConsumeInput() || box.isFocused());
    }

    @Override
    public void tick() {
        if (!paused) {

            if (!MOD_LOG_QUEUE.isEmpty()) {
                for (int i = 0; i < MOD_LOG_QUEUE.size(); i++) {
                    MOD_LOG.add(MOD_LOG_QUEUE.get(i));
                }
                MOD_LOG_QUEUE.clear();
                if (onlyMod)
                    updateBox();
            }

            if (System.currentTimeMillis() - lastCheck >= UPDATE_WAIT_MS) {
                if (clearFullLogCache) {
                    clearFullLogCache = false;
                    refreshVariousOnTick();
                    updateBox();
                }

                lastCheck = System.currentTimeMillis();
                if (logFile.lastModified() > lastUpdate) {
                    lastUpdate = logFile.lastModified();

                    try(BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                        String line;
                        int i = 0;
                        while ((line = reader.readLine()) != null) {
                            if (i >= fullLogLines) {
                                LogMessage temp = LogMessage.build(line);

                                if (temp.timestamp() != null || FULL_LOG.isEmpty())
                                    FULL_LOG.add(temp);
                                else {
                                    LogMessage lastLog = FULL_LOG.getLast();
                                    FULL_LOG.set(FULL_LOG.size() - 1, LogMessage.build(lastLog.timestamp(), lastLog.type(), lastLog.message() + "\n" + line));
                                }

                                fullLogLines++;
                            }
                            i++;
                        }
                    }
                    catch (Exception ex) {
                        logModLog(LogType.ERROR, "Failed to read vanilla log file: " + (logFile == null ? "null" : logFile.getPath()));
                    }

                    if (!onlyMod)
                        updateBox();
                }
            }

        }

        super.tick();
    }

}
