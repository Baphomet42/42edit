package baphomethlabs.fortytwoedit.gui.screen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.google.common.collect.Lists;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class LogScreen extends GenericScreen {

    private EditBoxWidget box;
    private ClickableWidget pauseBtn;
    private static final Tooltip PAUSE_TOOLTIP = Tooltip.of(Text.of("Temporarily freeze new messages from appearing"));
    private static final Tooltip RESUME_TOOLTIP = Tooltip.of(Text.of("Unpause log and show new messages"));
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

    protected TextFieldWidget txtRegex;
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

        logFile = new File(client.runDirectory.getAbsolutePath()+"\\logs\\latest.log");

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> changeScreen(new DebugScreen())).dimensions(x+5,y+5,40,20).build());
        pauseBtn = this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Resume"),
                Text.literal("Pause")).initially(paused).omitKeyText().build(x+5+40+5,y+5,40,20, Text.of(""), (button, trackOutput) -> {
            paused = (boolean)trackOutput;
            updateBox();
            unsel();
            pauseBtn.setTooltip(paused ? RESUME_TOOLTIP : PAUSE_TOOLTIP);
        }));
        pauseBtn.setTooltip(PAUSE_TOOLTIP);
        this.addDrawableChild(ButtonWidget.builder(Text.of("Clear"), button -> btnClearLog()).dimensions(x+backgroundWidth-5-50-40-5,y+5,40,20).build())
            .setTooltip(Tooltip.of(Text.of("Clear all logged messages\n\nShift click to restore all cleared messages")));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("[42edit]"),
                Text.literal("[All]")).initially(onlyMod).omitKeyText().build(x+backgroundWidth-5-50,y+5,50,20, Text.of(""), (button, trackOutput) -> {
            onlyMod = (boolean)trackOutput;
            updateBox();
            unsel();
        }));
        box = this.addDrawableChild(new EditBoxWidget(this.client.textRenderer, x+15-3, y+35, 240-24, 22*6, Text.of(""), Text.of("")));
        txtRegex = new TextFieldWidget(this.textRenderer,x+15-3,y+35+22*6+1,160,20,Text.of(""));
        txtRegex.setMaxLength(MAX_TEXT_LENGTH);
        txtRegex.setText(""+regexInput);
        txtRegex.setChangedListener(this::editTxtRegex);
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("[Regex]"),
                Text.literal("[Search]")).initially(useRegex).omitKeyText().build(x+backgroundWidth-5-50-7,y+35+22*6+1,50,20, Text.of(""), (button, trackOutput) -> {
            useRegex = (boolean)trackOutput;
            updateBox();
            unsel();
        }));
        this.addDrawableChild(txtRegex);
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
                    } catch(Exception ex) {
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

        box.setText(sb.toString());
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Log"), this.width / 2, y+11, TEXT_COLOR);
    }

    @Override
    public boolean shouldCloseOnKeybind() {
        return !(txtRegex.isActive() || box.isFocused());
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
