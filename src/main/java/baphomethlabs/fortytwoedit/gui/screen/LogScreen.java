package baphomethlabs.fortytwoedit.gui.screen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import com.google.common.collect.Lists;
import baphomethlabs.fortytwoedit.FortytwoEdit;

public class LogScreen extends GenericScreen {

    private MultiLineEditBox box;
    private AbstractWidget pauseBtn;
    private static final Tooltip PAUSE_TOOLTIP = Tooltip.create(Component.nullToEmpty("Temporarily freeze new messages from appearing"));
    private static final Tooltip RESUME_TOOLTIP = Tooltip.create(Component.nullToEmpty("Unpause log and show new messages"));
    private static File logFile;
    private static int fullLogLines = 0;
    private static int fullLogStart = 0;
    private static int modLogStart = 0;
    private static final List<LogMessage> FULL_LOG = Lists.newArrayList();
    private static final List<LogMessage> MOD_LOG = Lists.newArrayList();
    private static final List<LogMessage> MOD_LOG_QUEUE = Lists.newArrayList();

    private static boolean clearFullLogCache = false;
    private static boolean paused = false;
    private static boolean onlyMod = false;

    protected EditBox txtRegex;
    private static String regexInput = "";
    private static boolean useRegex = false;
    private static final String REGEX_STRING_ENDS_DOLLAR = ".*[^\\\\](\\\\\\\\)*\\$$";

    private long lastCheck = 0;
    private long lastUpdate = 0;
    private static final int UPDATE_WAIT_MS = 1000;
    private static final String ss = "\u00a7";

    public LogScreen() {}

    @Override
    protected void init() {
        super.init();
        FortytwoEdit.quickScreen = FortytwoEdit.QuickScreen.LOG_SCREEN;

        logFile = new File(minecraft.gameDirectory.getAbsolutePath()+"\\logs\\latest.log");

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Back"), button -> changeScreen(new DebugScreen())).bounds(x+GUI_SPACE,y+GUI_SPACE,40,WID_HEIGHT).build());
        pauseBtn = this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Resume"),
                Component.literal("Pause")).withInitialValue(paused).displayOnlyValue().create(x+GUI_SPACE+40+WID_SPACE,y+GUI_SPACE,40,WID_HEIGHT, Component.nullToEmpty(""), (button, trackOutput) -> {
            paused = (boolean)trackOutput;
            updateBox();
            unsel();
            pauseBtn.setTooltip(paused ? RESUME_TOOLTIP : PAUSE_TOOLTIP);
        }));
        pauseBtn.setTooltip(PAUSE_TOOLTIP);
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Clear"), button -> btnClearLog()).bounds(x+backgroundWidth-GUI_SPACE-50-40-WID_SPACE,y+GUI_SPACE,40,WID_HEIGHT).build())
            .setTooltip(Tooltip.create(Component.nullToEmpty("Clear all logged messages\n\nShift click to restore all cleared messages")));
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("[42edit]"),
                Component.literal("[All]")).withInitialValue(onlyMod).displayOnlyValue().create(x+backgroundWidth-GUI_SPACE-50,y+GUI_SPACE,50,WID_HEIGHT, Component.nullToEmpty(""), (button, trackOutput) -> {
            onlyMod = (boolean)trackOutput;
            updateBox();
            unsel();
        }));
        box = this.addRenderableWidget(new MultiLineEditBox(this.minecraft.font, x+15-3, y+35, 240-24, ROW_HEIGHT*6, Component.nullToEmpty(""), Component.nullToEmpty("")));
        txtRegex = new EditBox(this.font,x+15-3,y+35+ROW_HEIGHT*6+1,160,WID_HEIGHT,Component.nullToEmpty(""));
        txtRegex.setMaxLength(MAX_TEXT_LENGTH);
        txtRegex.setValue(""+regexInput);
        txtRegex.setResponder(this::editTxtRegex);
        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("[Regex]"),
                Component.literal("[Search]")).withInitialValue(useRegex).displayOnlyValue().create(x+backgroundWidth-5-50-7,y+35+ROW_HEIGHT*6+1,50,WID_HEIGHT, Component.nullToEmpty(""), (button, trackOutput) -> {
            useRegex = (boolean)trackOutput;
            updateBox();
            unsel();
        }));
        this.addRenderableWidget(txtRegex);
        updateBox();
    }

    protected void updateBox() {
        StringBuilder sb = new StringBuilder();
        List<LogMessage> logList = onlyMod ? MOD_LOG : FULL_LOG;
        int logStart = onlyMod ? modLogStart : fullLogStart;

        boolean firstLog = true;
        String regexError = null;
        for(int i=logStart; i<logList.size(); i++) {

            if(regexInput.length()>0) {
                boolean matchRegex = false;
                if(useRegex) {
                    try {
                        String regexMod = regexInput + "";
                        "".matches(regexMod); // used to get errors on original input regex

                        if(!regexMod.startsWith("^"))
                            regexMod = ".*"+regexMod;
                        if(!regexMod.matches(REGEX_STRING_ENDS_DOLLAR))
                            regexMod = regexMod+".*";

                        if(logList.get(i).searchLine().matches(regexMod))
                            matchRegex = true;
                    }
                    catch(Exception ex) {
                        regexError = ex.getMessage();
                    }
                }
                else if(logList.get(i).searchLine().toLowerCase().contains(regexInput.toLowerCase()))
                    matchRegex = true;

                if(!matchRegex)
                    continue;
            }

            if(!firstLog)
                sb.append("\n\n");
            else
                firstLog = false;

            sb.append(logList.get(i).formattedLine());
        }
        // end log box with empty line unless no lines were added
        if(!firstLog)
            sb.append("\n");

        if(regexError != null) {
            if(regexError.length()>1 && regexError.endsWith("^"))
                regexError = regexError.substring(0,regexError.length()-1).trim();
            sb = new StringBuilder(ss+"cInvalid Regex\n\n"+regexError.replace("\r",""));
        }

        box.setValue(sb.toString());
    }

    protected void editTxtRegex(String text) {
        if(!text.equals(regexInput)) {
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

            if(line.matches("^\\[[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\] .+")) {
                timestamp = line.substring(0,line.indexOf("]")+1);
                line = line.replaceFirst("^\\[[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\] ","");

                if(line.matches("^\\[[^/\\]]+/[A-Z]+\\]:? .+")) {
                    logType = LogType.build(line.substring(line.indexOf("/")+1,line.indexOf("]")));
                    line = line.replaceFirst("^\\[[^/\\]]+/[A-Z]+\\]:? ","");
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

            if(timestamp != null) {
                formattedLine.append(ss).append("9").append(timestamp).append(ss).append("r ");
                searchLine.append(timestamp).append(" ");
            }

            if(type != null) {
                formattedLine.append(ss).append(type.formatCode()).append(type.text()).append(ss).append("r ");
                searchLine.append(type.text()).append(" ");
            }

            String formatMessage = message.replace("\t","  ");
            if(timestamp != null && type != null && formatMessage.matches("^\\([^)]+\\) .+")) {
                formattedLine.append(ss).append("3").append(formatMessage.substring(0,formatMessage.indexOf(")")+1)).append(ss).append("r ");
                searchLine.append(formatMessage.substring(0,formatMessage.indexOf(")")+1)).append(" ");

                formatMessage = formatMessage.replaceFirst("^\\([^)]+\\) ","");
            }
            formattedLine.append(formatMessage);
            searchLine.append(formatMessage);

            return new LogMessage(timestamp, type, message, formattedLine.toString(), searchLine.toString().replace("\n"," "));
        }
    }

    public record LogType(String text, String formatCode) {
        public static final LogType INFO = new LogType("[INFO]","2");
        public static final LogType WARN = new LogType("[WARN]","6");
        public static final LogType ERROR = new LogType("[ERROR]","c");
        public static final LogType DEBUG = new LogType("[DEBUG]","a");
        public static final LogType FATAL = new LogType("[FATAL]","4");
        public static final LogType UNKNOWN = new LogType("[UNKNOWN]","f");

        public static LogType build(String text) {
            if(text != null && text.length()>0) {
                switch(text) {
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
        MOD_LOG_QUEUE.add(LogMessage.build(type,message));
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

    private void btnClearLog() {
        if(hasShiftDown()) {
            unhideAllLogged();
        }
        else {
            hideCurrentlyLogged();
        }
        unsel();
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
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, Component.nullToEmpty("Log"), this.width / 2, y+11, TEXT_COLOR);
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return !(txtRegex.canConsumeInput() || box.isFocused());
    }

    @Override
    public void tick() {
        if(!paused) {

            if(!MOD_LOG_QUEUE.isEmpty()) {
                for(int i=0; i<MOD_LOG_QUEUE.size(); i++) {
                    MOD_LOG.add(MOD_LOG_QUEUE.get(i));
                }
                MOD_LOG_QUEUE.clear();
                if(onlyMod)
                    updateBox();
            }

            if(System.currentTimeMillis()-lastCheck >= UPDATE_WAIT_MS) {
                if(clearFullLogCache) {
                    clearFullLogCache = false;
                    refreshVariousOnTick();
                    updateBox();
                }

                lastCheck = System.currentTimeMillis();
                if(logFile.lastModified()>lastUpdate) {
                    lastUpdate = logFile.lastModified();

                    try(BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                        String line;
                        int i = 0;
                        while((line = reader.readLine()) != null) {
                            if(i>=fullLogLines) {
                                LogMessage temp = LogMessage.build(line);

                                if(temp.timestamp() != null || FULL_LOG.isEmpty())
                                    FULL_LOG.add(temp);
                                else {
                                    LogMessage lastLog = FULL_LOG.getLast();
                                    FULL_LOG.set(FULL_LOG.size()-1, LogMessage.build(lastLog.timestamp(), lastLog.type(), lastLog.message()+"\n"+line));
                                }

                                fullLogLines++;
                            }
                            i++;
                        }
                    }
                    catch(Exception ex) {
                        logModLog(LogType.ERROR, "Failed to read vanilla log file: " + (logFile == null ? "null" : logFile.getPath()));
                    }

                    if(!onlyMod)
                        updateBox();
                }
            }

        }

        super.tick();
    }

}
