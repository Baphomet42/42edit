package baphomethlabs.fortytwoedit.gui.screen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import baphomethlabs.fortytwoedit.FortytwoEdit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class LogScreen extends GenericScreen {

    private EditBoxWidget box;
    private static File logFile;
    private static final List<LogMessage> FULL_LOG = new ArrayList<>();
    private static final List<LogMessage> MOD_LOG = new ArrayList<>();
    private static final List<LogMessage> MOD_LOG_QUEUE = new ArrayList<>();

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
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("Resume"),
                Text.literal("Pause")).initially(paused).omitKeyText().build(x+5+40+5,y+5,40,20, Text.of(""), (button, trackOutput) -> {
            paused = (boolean)trackOutput;
            updateBox();
            unsel();
        })).setTooltip(Tooltip.of(Text.of("Temporarily freeze new messages from appearing")));
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(Text.literal("[42edit]"),
                Text.literal("[All]")).initially(onlyMod).omitKeyText().build(x+backgroundWidth-5-60,y+5,60,20, Text.of(""), (button, trackOutput) -> {
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

        boolean hideLastLog = false;
        boolean firstLog = true;
        String regexError = null;
        for(int i=0; i<logList.size(); i++) {
            if(logList.get(i).timestamp() != null) {
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

                            if(logList.get(i).plainLine().matches(regexMod))
                                matchRegex = true;
                        } catch(Exception ex) {
                            regexError = ex.getMessage();
                        }
                    }
                    else if(logList.get(i).plainLine().toLowerCase().contains(regexInput.toLowerCase()))
                        matchRegex = true;

                    if(!matchRegex) {
                        hideLastLog = true;
                        continue;
                    }
                }
                if(!firstLog)
                    sb.append("\n");
            }
            else if(hideLastLog)
                continue;

            hideLastLog = false;
            if(!firstLog)
                sb.append("\n");
            else
                firstLog = false;
            sb.append(logList.get(i).formattedLine());
        }
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

    private record LogMessage(String timestamp, LogType type, String message, String formattedLine, String plainLine) {

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
            return build(FortytwoEdit.getTimestamp(), type, message);
        }
        public static LogMessage build(String timestamp, LogType type, String message) {
            StringBuilder formattedLine = new StringBuilder();
            StringBuilder plainLine = new StringBuilder();

            if(timestamp != null) {
                formattedLine.append(ss).append("9").append(timestamp).append(ss).append("r ");
                plainLine.append(timestamp).append(" ");
            }
            
            if(type != null) {
                formattedLine.append(ss).append(type.formatCode()).append(type.text()).append(ss).append("r ");
                plainLine.append(type.text()).append(" ");
            }

            String formatMessage = message.replace("\t","  ");
            if(timestamp != null && type != null && formatMessage.matches("^\\([^)]+\\) .+")) {
                formattedLine.append(ss).append("3").append(formatMessage.substring(0,formatMessage.indexOf(")")+1)).append(ss).append("r ");
                plainLine.append(formatMessage.substring(0,formatMessage.indexOf(")")+1)).append(" ");

                formatMessage = formatMessage.replaceFirst("^\\([^)]+\\) ","");
            }
            formattedLine.append(formatMessage);
            plainLine.append(formatMessage);

            return new LogMessage(timestamp, type, message, formattedLine.toString(), plainLine.toString());
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
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.of("Output Log"), this.width / 2, y+11, TEXT_COLOR);
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
                    lastCheck = 0;
                    lastUpdate = 0;
                    clearFullLogCache = false;
                    FULL_LOG.clear();
                }

                lastCheck = System.currentTimeMillis();
                if(logFile.lastModified()>lastUpdate) {
                    lastUpdate = logFile.lastModified();

                    try(BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                        String line;
                        int i = 0;
                        while((line = reader.readLine()) != null) {
                            if(i>=FULL_LOG.size()) {
                                FULL_LOG.add(LogMessage.build(line));
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
